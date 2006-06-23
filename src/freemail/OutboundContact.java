package freemail;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import freemail.utils.EmailAddress;
import freemail.utils.PropsFile;
import freemail.utils.DateStringFactory;
import freemail.utils.ChainedAsymmetricBlockCipher;
import freemail.fcp.HighLevelFCPClient;
import freemail.fcp.SSKKeyPair;

import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.InvalidCipherTextException;

public class OutboundContact {
	private final PropsFile contactfile;
	private final File accdir;
	private final EmailAddress address;
	private static final String OUTBOUND_DIR = "outbound";
	
	public OutboundContact(File accdir, EmailAddress a) throws BadFreemailAddressException {
		this.address = a;
		
		this.accdir = accdir;
		
		if (this.address.getMailsiteKey() == null) {
			this.contactfile = null;
			throw new BadFreemailAddressException();
		} else {
			File contactsdir = new File(accdir, SingleAccountWatcher.CONTACTS_DIR);
			if (!contactsdir.exists())
				contactsdir.mkdir();
			File outbounddir = new File(contactsdir, OUTBOUND_DIR);
			
			if (!outbounddir.exists())
				outbounddir.mkdir();
			
			this.contactfile = new PropsFile(new File(outbounddir, this.address.getMailsiteKey()));
		}
	}
	
	/*
	 * Whether or not we're ready to communicate with the other party
	 */
	public boolean ready() {
		if (!this.contactfile.exists()) return false;
		
		String status = this.contactfile.get("status");
		if (status == null) return false;
		// don't wait for an ack before inserting the message, but be ready to insert it again
		// if the ack never arrives
		if (status.equals("rts-sent")) return true;
		return false;
	}
	
	private SSKKeyPair getCommKeyPair() {
		SSKKeyPair ssk = new SSKKeyPair();
		
		ssk.pubkey = this.contactfile.get("commssk.privkey");
		ssk.privkey = this.contactfile.get("commssk.pubkey");
		
		
		if (ssk.pubkey == null || ssk.privkey == null) {
			HighLevelFCPClient cli = new HighLevelFCPClient();
			ssk = cli.makeSSK();
			
			this.contactfile.put("commssk.privkey", ssk.privkey);
			this.contactfile.put("commssk.pubkey", ssk.pubkey);
			// we've just generated a new SSK, so the other party definately doesn't know about it
			this.contactfile.put("status", "notsent");
		} else {
			ssk = new SSKKeyPair();
		}
		
		return ssk;
	}
	
	private RSAKeyParameters getPubKey() throws OutboundContactFatalException {
		String mod_str = this.contactfile.get("asymkey.modulus");
		String exp_str = this.contactfile.get("asymkey.pubexponent");
		
		if (mod_str == null || exp_str == null) {
			// we don't have their mailsite - fetch it
			if (this.fetchMailSite()) {
				mod_str = this.contactfile.get("asymkey.modulus");
				exp_str = this.contactfile.get("asymkey.pubexponent");
				
				// must be present now, or exception would have been thrown
			} else {
				return null;
			}
		}
		
		return new RSAKeyParameters(false, new BigInteger(mod_str, 10), new BigInteger(exp_str, 10));
	}
	
	private String getRtsKsk() throws OutboundContactFatalException {
		String rtsksk = this.contactfile.get("rtsksk");
		
		if (rtsksk == null) {
			// get it from their mailsite
			if (!this.fetchMailSite()) return null;
			
			rtsksk = this.contactfile.get("rtsksk");
		}
		
		return rtsksk;
	}
	
	/**
	 * Set up an outbound contact. Fetch the mailsite, generate a new SSK keypair and post an RTS message to the appropriate KSK.
	 * Will block for mailsite retrieval and RTS insertion
	 *
	 * @return true for success
	 */
	public boolean init() throws OutboundContactFatalException {
		// try to fetch get all necessary info. will fetch mailsite / generate new keys if necessary
		SSKKeyPair ssk = this.getCommKeyPair();
		if (ssk == null) return false;
		RSAKeyParameters their_pub_key = this.getPubKey();
		if (their_pub_key == null) return false;
		String rtsksk = this.getRtsKsk();
		if (rtsksk == null) return false;
		
		StringBuffer rtsmessage = new StringBuffer();
		
		// the public part of the SSK keypair we generated
		// put this first to avoid messages with the same first block, since we don't (currently) use CBC
		rtsmessage.append("commssk="+ssk.pubkey+"\r\n");
		
		rtsmessage.append("messagetype=rts\r\n");
		
		// must include who this RTS is to, otherwise we're vulnerable to surruptitious forwarding
		rtsmessage.append("to="+this.address.getMailsiteKey()+"\r\n");
		
		// get our mailsite URI
		String our_mailsite_uri = AccountManager.getAccountFile(this.accdir).get("mailsite.pubkey");
		
		rtsmessage.append("mailsite="+our_mailsite_uri+"\r\n");
		
		rtsmessage.append("\r\n");
		
		// sign the message
		
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException alge) {
			System.out.println("No SHA 256 implementation available - no mail can be sent!");
			return false;
		}
		
		byte[] hash = md.digest(rtsmessage.toString().getBytes());
		
		RSAKeyParameters our_priv_key = AccountManager.getPrivateKey(this.accdir);
		
		AsymmetricBlockCipher sigcipher = new RSAEngine();
		sigcipher.init(true, our_priv_key);
		byte[] sig = null;
		try {
			sig = sigcipher.processBlock(hash, 0, hash.length);
		} catch (InvalidCipherTextException e) {
			e.printStackTrace();
			return false;
		}
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		try {
			bos.write(rtsmessage.toString().getBytes());
			bos.write(sig);
		} catch(IOException ioe) {
			ioe.printStackTrace();
			return false;
		}
		
		// now encrypt it
		AsymmetricBlockCipher enccipher = new RSAEngine();
		enccipher.init(true, their_pub_key);
		byte[] encmsg = null;
		try {
			encmsg = ChainedAsymmetricBlockCipher.encrypt(sigcipher, bos.toByteArray());
		} catch (InvalidCipherTextException e) {
			e.printStackTrace();
			return false;
		}
		
		// insert it!
		HighLevelFCPClient cli = new HighLevelFCPClient();
		if (cli.SlotInsert(encmsg, "KSK@"+rtsksk+"-"+DateStringFactory.getKeyString(), 1, "") < 0) {
			return false;
		}
		
		// remember the fact that we have successfully inserted the rts
		this.contactfile.put("status", "rts-sent");
		
		return true;
	}
	
	private boolean fetchMailSite() throws OutboundContactFatalException {
		HighLevelFCPClient cli = new HighLevelFCPClient();
		
		System.out.println("Attempting to fetch "+this.getMailpageKey());
		File mailsite_file = cli.fetch(this.getMailpageKey());
		
		if (mailsite_file == null) {
			// TODO: Give up for now, try later, count number of and limit attempts
			System.out.println("Failed to retrieve mailsite for "+this.address);
			return false;
		}
		
		System.out.println("got mailsite");
		
		PropsFile mailsite = new PropsFile(mailsite_file);
		
		String rtsksk = mailsite.get("rtsksk");
		String keymod_str = mailsite.get("asymkey.modulus");
		String keyexp_str = mailsite.get("asymkey.pubexponent");
		
		mailsite_file.delete();
		
		if (rtsksk == null || keymod_str == null || keyexp_str == null) {
			// TODO: More failure mechanisms - this is fatal.
			System.out.println("Mailsite for "+this.address+" does not contain all necessary iformation!");
			throw new OutboundContactFatalException("Mailsite for "+this.address+" does not contain all necessary iformation!");
		}
		
		// add this to a new outbound contact file
		this.contactfile.put("rtsksk", rtsksk);
		this.contactfile.put("asymkey.modulus", keymod_str);
		this.contactfile.put("asymkey.pubexponent", keyexp_str);
		
		return true;
	}
	
	private String getMailpageKey() {
		return "USK@"+this.address.getMailsiteKey()+"/"+AccountManager.MAILSITE_SUFFIX+"/1/"+MailSite.MAILPAGE;
	}
}

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
	
	public boolean exists() {
		return this.contactfile.exists();
	}
	
	/**
	 * Set up an outbound contact. Fetch the mailsite, generate a new SSK keypair and post an RTS message to the appropriate KSK.
	 * Will block for mailsite retrieval and RTS insertion
	 *
	 * @return true for success
	 */
	public boolean init() throws OutboundContactFatalException {
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
		
		String rtskey = mailsite.get("rtsksk");
		String keymod_str = mailsite.get("asymkey.modulus");
		String keyexp_str = mailsite.get("asymkey.pubexponent");
		
		if (rtskey == null || keymod_str == null || keyexp_str == null) {
			// TODO: More failure mechanisms - this is fatal.
			System.out.println("Mailsite for "+this.address+" does not contain all necessary iformation!");
			throw new OutboundContactFatalException("Mailsite for "+this.address+" does not contain all necessary iformation!");
		}
		mailsite_file.delete();
		
		SSKKeyPair ssk = cli.makeSSK();
		
		StringBuffer rtsmessage = new StringBuffer();
		
		rtsmessage.append("messagetype=rts\r\n");
		
		// must include who this RTS is to, otherwise we're vulnerable to surruptitious forwarding
		rtsmessage.append("to="+this.address.getMailsiteKey()+"\r\n");
		
		// get our mailsite URI
		String our_mailsite_uri = AccountManager.getAccountFile(this.accdir).get("mailsite.pubkey");
		
		rtsmessage.append("mailsite="+our_mailsite_uri+"\r\n");
		
		// the public part of the SSK keypair we generated
		rtsmessage.append("commssk="+ssk.pubkey+"\r\n");
		
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
		
		BigInteger keymodulus = new BigInteger(keymod_str, 10);
		BigInteger keyexponent = new BigInteger(keyexp_str, 10);
		
		//                                               is not private
		RSAKeyParameters their_pub_key = new RSAKeyParameters(false, keymodulus, keyexponent);
		
		AsymmetricBlockCipher enccipher = new RSAEngine();
		enccipher.init(true, their_pub_key);
		byte[] encmsg = null;
		try {
			encmsg = sigcipher.processBlock(bos.toByteArray(), 0, bos.toByteArray().length);
		} catch (InvalidCipherTextException e) {
			e.printStackTrace();
			return false;
		}
		
		// insert it!
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		
		if (cli.SlotInsert(bis, "KSK@"+rtskey+"-"+DateStringFactory.getKeyString(), 1, "") < 0) {
			return false;
		}
		
		// now we can create a new outbound contact file
		this.contactfile.put("rtskey", rtskey);
		this.contactfile.put("asymkey.modulus", keymod_str);
		this.contactfile.put("asymnkey.exponent", keyexp_str);
		this.contactfile.put("commssk", ssk.privkey);
		
		return true;
	}
	
	private String getMailpageKey() {
		return "USK@"+this.address.getMailsiteKey()+"/"+AccountManager.MAILSITE_SUFFIX+"/1/"+MailSite.MAILPAGE;
	}
}

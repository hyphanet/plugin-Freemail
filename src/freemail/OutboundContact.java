/*
 * OutboundContact.java
 * This file is part of Freemail
 * Copyright (C) 2006,2007,2008 Dave Baker
 * Copyright (C) 2007,2008 Alexander Lehmann
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package freemail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.io.PrintWriter;

import freemail.utils.EmailAddress;
import freemail.utils.PropsFile;
import freemail.utils.DateStringFactory;
import freemail.fcp.FCPException;
import freemail.fcp.FCPFetchException;
import freemail.fcp.HighLevelFCPClient;
import freemail.fcp.FCPPutFailedException;
import freemail.fcp.FCPBadFileException;
import freemail.fcp.SSKKeyPair;
import freemail.fcp.ConnectionTerminatedException;
import freemail.utils.Logger;

import org.archive.util.Base32;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.util.encoders.Base64;

public class OutboundContact {
	public static final String OUTBOX_DIR = "outbox";
	private final PropsFile contactfile;
	private final FreemailAccount account;
	private final File ctoutbox;
	private final EmailAddress address;
	
	// how long to wait for a CTS before sending the RTS again
	// slightly over 24 hours since some people are likely to fire Freemail
	// up and roughly the same time every day
	private static final long CTS_WAIT_TIME = 26 * 60 * 60 * 1000;
	
	private static final String PROPSFILE_NAME = "props";
	
	// How long to wait for a *message ack* before sending another RTS.
	private static final long RTS_RETRANSMIT_DELAY = 2 * 24 * 60 * 60 * 1000;
	
	// how long do we wait before we give up all hope and just bounce the message back?
	// 5 days is fairly standard, so we'll go with that for now, except that means things
	// bounce when the recipient goes to the Bahamas for a fortnight. Could be longer if
	// we have a GUI to see what messages are in what delivery state.
	private static final long FAIL_DELAY = 5 * 24 * 60 * 60 * 1000;
	
	private static final int AES_KEY_LENGTH = 256 / 8;
	
	// this is defined in the AES standard (although the Rijndael
	// algorithm does support other block sizes.
	// we read 128 bytes for our IV, so it needs to be constant.)
	private static final int AES_BLOCK_LENGTH = 128 / 8;
	
	// If we last fetched the mailsite longer than this number of milliseconds
    // ago, re-fetch it.
	private static final long MAILSITE_CACHE_TIME = 60 * 60 * 1000;

	/**
	 * Used to store the index of the next ack we should check. This is done so we won't start from
	 * the beginning if we stopped due to a timeout, but instead start where we left of.
	 */
	//FIXME: This behaves badly when the outbox changes
	private int nextAckIndex = 0;
	
	public OutboundContact(FreemailAccount acc, EmailAddress a) throws BadFreemailAddressException, IOException,
	                                                           OutboundContactFatalException, ConnectionTerminatedException,
	                                                           InterruptedException {
		this.address = a;
		
		this.account = acc;
		
		if (!this.address.is_freemail_address()) {
			this.contactfile = null;
			throw new BadFreemailAddressException();
		} else {
			File contactsdir = new File(account.getAccountDir(), SingleAccountWatcher.CONTACTS_DIR);
			if (!contactsdir.exists()) {
				if (!contactsdir.mkdir()) {
					throw new IOException("Couldn't create contacts dir!");
				}
			}
			File outbounddir = new File(contactsdir, SingleAccountWatcher.OUTBOUND_DIR);
			
			if (!outbounddir.exists()) {
				if (!outbounddir.mkdir()) {
					throw new IOException("Couldn't create outbound dir!");
				}
			}
			
			if (!this.address.is_ssk_address()) {
				String ssk_mailsite = this.fetchKSKRedirect(this.address.getMailpageKey());
				
				if (ssk_mailsite == null) throw new IOException();
				
				FreenetURI furi;
				try {
					furi = new FreenetURI(ssk_mailsite);
				} catch (MalformedURLException mfue) {
					throw new OutboundContactFatalException("The Freemail address points to an invalid redirect, and is therefore useless.");
				}
				
				this.address.domain = Base32.encode(furi.getKeyBody().getBytes())+".freemail";
			}
			
			File obctdir = new File(outbounddir, this.address.getSubDomain().toLowerCase());
			
			if (!obctdir.exists() && !obctdir.mkdir()) {
				throw new IOException("Couldn't create outbound contact dir!");
			}
			
			this.contactfile = PropsFile.createPropsFile(new File(obctdir, PROPSFILE_NAME));
			this.ctoutbox = new File(obctdir, OUTBOX_DIR);
			if (!this.ctoutbox.exists() && !this.ctoutbox.mkdir()) {
				throw new IOException("Couldn't create contact outbox!");
			}
		}
	}
	
	public OutboundContact(FreemailAccount acc, File ctdir) throws IOException {
		this.account = acc;
		this.address = new EmailAddress();
		this.address.domain = ctdir.getName()+".freemail";
		
		this.contactfile = PropsFile.createPropsFile(new File(ctdir, PROPSFILE_NAME));
		
		this.ctoutbox = new File(ctdir, OUTBOX_DIR);
		if (!this.ctoutbox.exists()) {
			if (!this.ctoutbox.mkdir()) {
				throw new IOException("Couldn't create contact outbox dir!");
			}
		}
	}
	
	public void checkCTS() throws OutboundContactFatalException, ConnectionTerminatedException,
	                              InterruptedException {
		String status = this.contactfile.get("status");
		if (status == null) {
			this.init();
			status = this.contactfile.get("status");
			if (status == null) return;
		}
		
		if (status.equals("cts-received")) {
			return;
		} else if (status.equals("rts-sent")) {
			// poll for the CTS message
			
			String ctskey = this.contactfile.get("ackssk.pubkey");
			if (ctskey == null) {
				this.init();
			}
			ctskey += "cts";
			
			HighLevelFCPClient fcpcli = new HighLevelFCPClient();
			
			Logger.minor(this,"polling for CTS message: "+ctskey);
			try {
				File cts = fcpcli.fetch(ctskey);
				
				Logger.normal(this,"Sucessfully received CTS for "+this.address.getSubDomain());
				cts.delete();
				this.contactfile.put("status", "cts-received");
				// delete initial slot for forward secrecy
				this.contactfile.remove("initialslot");
			} catch (FCPFetchException fe) {
				Logger.minor(this,"CTS not received");
				// haven't got the CTS message. should we give up yet?
				String senttime = this.contactfile.get("rts-sent-at");
				
				if (senttime == null || Long.parseLong(senttime) + CTS_WAIT_TIME < System.currentTimeMillis()) {
					// yes, send another RTS
					this.init();
				}
			} catch (FCPException e) {
				Logger.error(this, "Unknown error while checking CTS: " + e);
				//TODO: Should we resend the RTS like above?
			}
		} else {
			this.init();
		}
	}
	
	private SSKKeyPair getCommKeyPair() throws ConnectionTerminatedException, InterruptedException {
		SSKKeyPair ssk = new SSKKeyPair();
		
		ssk.pubkey = this.contactfile.get("commssk.pubkey");
		ssk.privkey = this.contactfile.get("commssk.privkey");
		
		
		if (ssk.pubkey == null || ssk.privkey == null) {
			HighLevelFCPClient cli = new HighLevelFCPClient();
			ssk = cli.makeSSK();
			
			this.contactfile.put("commssk.privkey", ssk.privkey);
			this.contactfile.put("commssk.pubkey", ssk.pubkey);
			// we've just generated a new SSK, so the other party definitely doesn't know about it
			this.contactfile.put("status", "notsent");
		}
		
		return ssk;
	}
	
	private SSKKeyPair getAckKeyPair() throws ConnectionTerminatedException, InterruptedException {
		SSKKeyPair ssk = new SSKKeyPair();
		
		ssk.pubkey = this.contactfile.get("ackssk.pubkey");
		ssk.privkey = this.contactfile.get("ackssk.privkey");
		
		
		if (ssk.pubkey == null || ssk.privkey == null) {
			HighLevelFCPClient cli = new HighLevelFCPClient();
			ssk = cli.makeSSK();
			
			this.contactfile.put("ackssk.privkey", ssk.privkey);
			this.contactfile.put("ackssk.pubkey", ssk.pubkey);
		}
		
		return ssk;
	}
	
	private RSAKeyParameters getPubKey() throws OutboundContactFatalException, ConnectionTerminatedException,
	                                            InterruptedException {
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
		
		return new RSAKeyParameters(false, new BigInteger(mod_str, 32), new BigInteger(exp_str, 32));
	}
	
	private String getRtsKsk() throws OutboundContactFatalException, ConnectionTerminatedException,
	                                  InterruptedException {
		String rtsksk = this.contactfile.get("rtsksk");
		
		if (rtsksk == null) {
			// get it from their mailsite
			if (!this.fetchMailSite()) return null;
			
			rtsksk = this.contactfile.get("rtsksk");
		}
		
		return rtsksk;
	}
	
	/**
	 * Get the first slot from which all messages that are still 'in transit' can be retrieved.
	 * That is to say, if we have message IDs 3,4 and 5 in transit, this would return the slot
	 * for message 3. If there are no messages in transit, returns the next slot on which a message will be inserted.
	 */
	private String getCurrentLowestSlot() {
		Set<QueuedMessage> queue = getSendQueue(null);
		int lowestUid = Integer.MAX_VALUE;
		String lowestSlot = null;

		for (QueuedMessage msg : queue) {
			if (msg.uid < lowestUid) {
				lowestUid = msg.uid;
				lowestSlot = msg.slot;
			}
		}
		if (lowestUid < Integer.MAX_VALUE) return lowestSlot;

		// No messages in the queue, so the current lowest slot is the
		// next slot we'll insert a message to.
		String retval = this.contactfile.get("nextslot");
		if (retval != null) {
			return retval;
		} else {
			return generateFirstSlot();
		}
	}
	
	private String generateFirstSlot() {
		Logger.minor(this, "Generating first slot for contact");
		SecureRandom rnd = new SecureRandom();
		SHA256Digest sha256 = new SHA256Digest();
		byte[] buf = new byte[sha256.getDigestSize()];
		
		rnd.nextBytes(buf);
		
		String firstSlot = Base32.encode(buf);
		
		this.contactfile.put("nextslot", Base32.encode(buf));
		
		return firstSlot;
	}
	
	private byte[] getAESParams() {
		String params = this.contactfile.get("aesparams");
		if (params != null) {
			return Base64.decode(params);
		}
		
		SecureRandom rnd = new SecureRandom();
		byte[] retval = new byte[AES_KEY_LENGTH + AES_BLOCK_LENGTH];
		rnd.nextBytes(retval);
		
		// save them for next time (if insertion fails) so we can
		// generate the same message, otherwise they'll collide
		// unnecessarily.
		this.contactfile.put("aesparams", new String(Base64.encode(retval)));
		
		return retval;
	}
	
	/**
	 * Set up an outbound contact. Fetch the mailsite, generate a new SSK keypair and post an RTS message to the appropriate KSK.
	 * Will block for mailsite retrieval and RTS insertion
	 *
	 * @return true for success
	 */
	private boolean init() throws OutboundContactFatalException, ConnectionTerminatedException,
	                              InterruptedException {
		Logger.normal(this, "Initialising Outbound Contact "+address.toString());
		
		// try to fetch get all necessary info. will fetch mailsite / generate new keys if necessary
		String initialslot = this.getCurrentLowestSlot();
		SSKKeyPair commssk = this.getCommKeyPair();
		if (commssk == null) return false;
		SSKKeyPair ackssk = this.getAckKeyPair();
		RSAKeyParameters their_pub_key = this.getPubKey();
		if (their_pub_key == null) return false;
		String rtsksk = this.getRtsKsk();
		if (rtsksk == null) return false;
		
		StringBuffer rtsmessage = new StringBuffer();
		
		// the public part of the SSK keypair we generated
		rtsmessage.append("commssk="+commssk.pubkey+"\r\n");
		
		rtsmessage.append("ackssk="+ackssk.privkey+"\r\n");
		
		rtsmessage.append("initialslot="+initialslot+"\r\n");
		
		rtsmessage.append("messagetype=rts\r\n");
		
		// must include who this RTS is to, otherwise we're vulnerable to surreptitious forwarding
		rtsmessage.append("to="+this.address.getSubDomain()+"\r\n");
		
		// get our mailsite URI
		String our_mailsite_uri = account.getProps().get("mailsite.pubkey");
		
		rtsmessage.append("mailsite="+our_mailsite_uri+"\r\n");
		
		rtsmessage.append("\r\n");
		//FreemailLogger.normal(this,rtsmessage.toString());
		
		// sign the message
		SHA256Digest sha256 = new SHA256Digest();
		sha256.update(rtsmessage.toString().getBytes(), 0, rtsmessage.toString().getBytes().length);
		byte[] hash = new byte[sha256.getDigestSize()];
		sha256.doFinal(hash, 0);
		
		RSAKeyParameters our_priv_key = AccountManager.getPrivateKey(account.getProps());
		
		AsymmetricBlockCipher sigcipher = new RSAEngine();
		sigcipher.init(true, our_priv_key);
		byte[] sig = null;
		try {
			sig = sigcipher.processBlock(hash, 0, hash.length);
		} catch (InvalidCipherTextException icte) {
			Logger.error(this, "Failed to RSA encrypt hash: "+icte.getMessage());
			icte.printStackTrace();
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
		
		// make up a symmetric key
		PaddedBufferedBlockCipher aescipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()), new PKCS7Padding());
		
		// quick paranoia check!
		if (aescipher.getBlockSize() != AES_BLOCK_LENGTH) {
			// bouncycastle must have changed their implementation, so 
			// we're in trouble
			Logger.normal(this,"Incompatible block size change detected in cryptography API! Are you using a newer version of the bouncycastle libraries? If so, we suggest you downgrade for now, or check for a newer version of Freemail.");
			return false;
		}
		
		byte[] aes_iv_and_key = this.getAESParams();
		
		// now encrypt that with our recipient's public key
		AsymmetricBlockCipher enccipher = new RSAEngine();
		enccipher.init(true, their_pub_key);
		byte[] encrypted_aes_params = null;
		try {
			encrypted_aes_params = enccipher.processBlock(aes_iv_and_key, 0, aes_iv_and_key.length);
		} catch (InvalidCipherTextException icte) {
			Logger.error(this, "Failed to perform asymmertic encryption on RTS symmetric key: "+icte.getMessage());
			icte.printStackTrace();
			return false;
		}
		
		// now encrypt the message with the symmetric key
		KeyParameter kp = new KeyParameter(aes_iv_and_key, aescipher.getBlockSize(), AES_KEY_LENGTH);
		ParametersWithIV kpiv = new ParametersWithIV(kp, aes_iv_and_key, 0, aescipher.getBlockSize());
		aescipher.init(true, kpiv);
		
		byte[] encmsg = new byte[aescipher.getOutputSize(bos.toByteArray().length)+encrypted_aes_params.length];
		System.arraycopy(encrypted_aes_params, 0, encmsg, 0, encrypted_aes_params.length);
		int offset = encrypted_aes_params.length;
		offset += aescipher.processBytes(bos.toByteArray(), 0, bos.toByteArray().length, encmsg, offset);
		
		try {
			aescipher.doFinal(encmsg, offset);
		} catch (InvalidCipherTextException icte) {
			Logger.error(this, "Failed to perform symmertic encryption on RTS data: "+icte.getMessage());
			icte.printStackTrace();
			return false;
		}
		
		// insert it!
		HighLevelFCPClient cli = new HighLevelFCPClient();
		if (cli.slotInsert(encmsg, "KSK@"+rtsksk+"-"+DateStringFactory.getKeyString(), 1, "") < 0) {
			// safe to copy the message into the contact outbox though
			return false;
		}
		
		// remember the fact that we have successfully inserted the rts
		this.contactfile.put("status", "rts-sent");
		// and remember when we sent it!
		this.contactfile.put("rts-sent-at", Long.toString(System.currentTimeMillis()));
		// and since that's been successfully inserted to that key, we can
		// throw away the symmetric key
		this.contactfile.remove("aesparams");
		
		Logger.normal(this, "Succesfully initialised Outbound Contact");
		
		return true;
	}
	
	// fetch the redirect (assumes that this is a KSK address)
	private String fetchKSKRedirect(String key) throws OutboundContactFatalException, ConnectionTerminatedException,
	                                                   InterruptedException {
		HighLevelFCPClient cli = new HighLevelFCPClient();
		
		Logger.normal(this,"Attempting to fetch mailsite redirect "+key);
		File result;
		try {
			result = cli.fetch(key);
		} catch (FCPFetchException fe) {
			Logger.normal(this,"Failed to retrieve mailsite redirect "+key+" ("+fe.getMessage()+")");
			return null;
		} catch (FCPException e) {
			Logger.error(this, "Unknown error while fetching mailsite redirect: " + e);
			return null;
		}
		
		if (result.length() > 512) {
			Logger.normal(this,"Fatal: mailsite redirect too long. Ignoring.");
			result.delete();
			throw new OutboundContactFatalException("Mailsite redirect too long.");
		}
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(result));
		} catch (FileNotFoundException fnfe) {
			// impossible
		}
		
		String addr;
		try {
			addr = br.readLine();
			br.close();
		} catch (IOException ioe) {
			Logger.normal(this,"Warning: IO exception whilst reading mailsite redirect file: "+ioe.getMessage());
			return null;
		}
		result.delete();
		Logger.normal(this,"Mailsite redirect fetched successfully");
		return addr;
	}
	
	private boolean fetchMailSite() throws OutboundContactFatalException, ConnectionTerminatedException,
	                                       InterruptedException {
		String lastFetchedStr = this.contactfile.get("lastfetched");
		long lastFetched = 0;
		if (lastFetchedStr != null) {
			lastFetched = Long.parseLong(lastFetchedStr);
		}
		if (lastFetched > System.currentTimeMillis()) {
			Logger.error(this, "Mailsite was apparently last fetched in the future! System time gone backwards? Refetching.");
			lastFetched = 0;
		}
		if (lastFetched > System.currentTimeMillis() - MAILSITE_CACHE_TIME) {
			return true;
		}
		
		HighLevelFCPClient cli = new HighLevelFCPClient();
		
		Logger.normal(this,"Attempting to fetch "+this.address.getMailpageKey());
		File mailsite_file;
		try {
			mailsite_file = cli.fetch(this.address.getMailpageKey());
		} catch (FCPFetchException fe) {
			Logger.normal(this,"Failed to retrieve mailsite "+this.address.getMailpageKey());
			return false;
		} catch (FCPException e) {
			Logger.error(this, "Unknown error while fetching mailsite: " + e);
			return false;
		}
		
		Logger.normal(this,"got mailsite");
		
		PropsFile mailsite = PropsFile.createPropsFile(mailsite_file);
		
		String rtsksk = mailsite.get("rtsksk");
		String keymod_str = mailsite.get("asymkey.modulus");
		String keyexp_str = mailsite.get("asymkey.pubexponent");
		
		mailsite_file.delete();
		
		if (rtsksk == null || keymod_str == null || keyexp_str == null) {
			// Not actually fatal - the other party could publish a new, valid mailsite
			Logger.normal(this,"Mailsite for "+this.address+" does not contain all necessary information!");
			return false;
		}
		
		// add this to a new outbound contact file
		this.contactfile.put("rtsksk", rtsksk);
		this.contactfile.put("asymkey.modulus", keymod_str);
		this.contactfile.put("asymkey.pubexponent", keyexp_str);
		this.contactfile.put("lastfetched", Long.toString(System.currentTimeMillis()));
		
		return true;
	}
	
	private String popNextSlot() {
		String slot = this.contactfile.get("nextslot");
		if (slot == null) {
			return generateFirstSlot();
		}
		SHA256Digest sha256 = new SHA256Digest();
		sha256.update(Base32.decode(slot), 0, Base32.decode(slot).length);
		byte[] nextslot = new byte[sha256.getDigestSize()];
		sha256.doFinal(nextslot, 0);
		this.contactfile.put("nextslot", Base32.encode(nextslot));
		
		return slot;
	}
	
	private int popNextUid() {
		String nextuid_s = this.contactfile.get("nextuid");
		
		int nextuid;
		if (nextuid_s == null)
			nextuid = 1;
		else
			nextuid = Integer.parseInt(nextuid_s);
		
		this.contactfile.put("nextuid", Integer.toString(nextuid + 1));
		return nextuid;
	}
	
	public boolean sendMessage(File body) {
		int uid = this.popNextUid();
		
		// create a new file that contains the complete Freemail
		// message, with Freemail headers
		QueuedMessage qm = new QueuedMessage(uid);
		
		File msg;
		PrintWriter pw;
		try {
			msg = File.createTempFile("ogm", "msg", Freemail.getTempDir());
			
			pw = new PrintWriter(new FileOutputStream(msg));
		} catch (IOException ioe) {
			Logger.normal(this,"IO Error encountered whilst trying to send message: "+ioe.getMessage()+" Will try again soon");
			return false;
		}
		
		try {
			pw.print("id="+uid+"\r\n\r\n");
			
			BufferedReader br = new BufferedReader(new FileReader(body));
			MailHeaderFilter filter = new MailHeaderFilter(br);
			
			String chunk;
			while ( (chunk = filter.readHeader()) != null ) {
				pw.print(chunk+"\r\n");
			}
			pw.print("\r\n");
			
			// Headers are done, copy the rest
			while ( (chunk = br.readLine()) != null ) {
				pw.print(chunk+"\r\n");
			}
			
			pw.close();
			br.close();
		} catch (IOException ioe) {
			Logger.normal(this,"IO Error encountered whilst trying to send message: "+ioe.getMessage()+" Will try again soon");
			qm.delete();
			msg.delete();
			return false;
		}
		
		String slot = this.popNextSlot();
		
		qm.slot = slot;
		
		if (qm.setMessageFile(msg) && qm.saveProps()) {
			return true;
		}
		return false;
	}
	
	public void doComm(long timeout) throws InterruptedException {
		try {
			this.sendQueued(timeout / 2);
			this.pollAcks(timeout / 2);
			this.checkCTS();
		} catch (OutboundContactFatalException fe) {
			Logger.error(this, "Fatal exception on outbound contact: "+fe.getMessage()+". This contact in invalid.");
			// TODO: probably bounce all the messages and delete the contact.
		} catch (ConnectionTerminatedException cte) {
			// just exit
		}
	}
	
	private void sendQueued(long timeout) throws ConnectionTerminatedException, OutboundContactFatalException,
	                                             InterruptedException {
		boolean ready;
		String ctstatus = this.contactfile.get("status");
		if (ctstatus == null) ctstatus = "notsent";
		if (ctstatus.equals("rts-sent") || ctstatus.equals("cts-received")) {
			ready = true;
		} else {
			ready = this.init();
		}
		
		HighLevelFCPClient fcpcli = null;
		
		/* We sort the messages by uid since this is the order the other side will
		 * attempt to fetch the messages. Using the same order improves performance
		 * when sending a lot of messages. */
		Set<QueuedMessage> msgs = this.getSendQueue(new MessageUidComparator());
		
		long start = System.nanoTime();
		for (QueuedMessage msg : msgs) {
			if (msg.last_send_time > 0) continue;
			
			if (!ready) {
				if (msg.added_time + FAIL_DELAY < System.currentTimeMillis()) {
					if (Postman.bounceMessage(msg.getMessageFile(), account.getMessageBank(),
							"Freemail has been trying to establish a communication channel with this party for too long "
							+"without success. Check that the Freemail address is valid, and that the recipient still runs "
							+"Freemail on at least a semi-regular basis.", true)) {
						msg.delete();
					}
				}
				continue;
			}
			
			if (fcpcli == null) fcpcli = new HighLevelFCPClient();
			
			String key = this.contactfile.get("commssk.privkey");
			
			if (key == null) {
				Logger.normal(this,"Contact file does not contain private communication key! It appears that your Freemail directory is corrupt!");
				continue;
			}
			
			if(msg.slot==null) {
				Logger.normal(this,"Index file does not contain slot name for this message, the mail cannot be sent this way.");
				Logger.debug(this,"Filename is "+contactfile.toString());
				continue;
			}
			
			key += msg.slot;
			
			FileInputStream fis;
			try {
				fis = new FileInputStream(msg.file);
			} catch (FileNotFoundException fnfe) {
				continue;
			}
			
			Logger.normal(this,"Inserting message to "+key);
			FCPPutFailedException err;
			try {
				err = fcpcli.put(fis, key);
			} catch (FCPBadFileException bfe) {
				Logger.normal(this,"Failed sending message. Will try again soon.");
				continue;
			} catch (FCPException e) {
				Logger.error(this, "Unknown error while sending message: " + e);
				continue;
			}
			if (err == null) {
				Logger.normal(this,"Successfully inserted "+key);
				if (msg.first_send_time < 0)
					msg.first_send_time = System.currentTimeMillis();
				msg.last_send_time = System.currentTimeMillis();
				msg.saveProps();
			} else if (err.errorcode == FCPPutFailedException.COLLISION) {
				msg.slot = popNextSlot();
				Logger.error(this, "Insert collided! Assigned new slot: "+msg.slot);
				msg.saveProps();
			} else if (msg.added_time + FAIL_DELAY < System.currentTimeMillis()) {
				Logger.normal(this,"Giving up on a message - been trying to send for too long. Bouncing.");
				if (Postman.bounceMessage(msg.getMessageFile(), account.getMessageBank(),
						"Freemail has been trying to deliver this message for too long without success. "
						+"This is likley to be due to a poor connection to Freenet. Check your Freenet node.", true)) {
					msg.delete();
				}
			} else {
				Logger.normal(this,"Failed to insert "+key+" (error code "+err.errorcode+") will try again soon.");
				if(err.errorcode==FCPPutFailedException.COLLISION) {
					Logger.error(this,"Failed to insert "+key+" will try again soon. (Collision, this shouldn't happen)");
				} else {
					Logger.normal(this,"Failed to insert "+key+" will try again soon. Error: "+err.errorcode);
				}
			}

			if (System.nanoTime() > (start + (timeout * 1000 * 1000))) {
				Logger.debug(this, "Stopping message sending due to timeout");
				break;
			}
		}
	}
	
	private void pollAcks(long timeout) throws ConnectionTerminatedException, OutboundContactFatalException,
	                                           InterruptedException {
		HighLevelFCPClient fcpcli = null;
		Set<QueuedMessage> msgs = this.getSendQueue(null);
		
		Logger.debug(this, "Starting from ack index " + nextAckIndex);
		long start = System.nanoTime();
		int ackIndex = 0;
		for (QueuedMessage msg : msgs) {
			if (ackIndex++ < nextAckIndex) continue;
			if (msg.first_send_time < 0) continue;
			
			if (fcpcli == null) fcpcli = new HighLevelFCPClient();
			
			String key = this.contactfile.get("ackssk.pubkey");
			if (key == null) {
				Logger.normal(this,"Contact file does not contain public ack key! It appears that your Freemail directory is corrupt!");
				continue;
			}
			
			key += "ack-"+msg.uid;
			
			Logger.minor(this,"Looking for message ack on "+key);
			
			try {
				File ack = fcpcli.fetch(key);
				Logger.normal(this,"Ack received for message "+msg.uid+" on contact "+this.address.domain+". Now that's a job well done.");
				ack.delete();
				msg.delete();
				// treat the ACK as a CTS too
				this.contactfile.put("status", "cts-received");
				// delete initial slot for forward secrecy
				this.contactfile.remove("initialslot");
			} catch (FCPFetchException fe) {
				Logger.minor(this,"Failed to receive ack on "+key+" ("+fe.getMessage()+")");
				if (!fe.isNetworkError()) {
					if (System.currentTimeMillis() > msg.first_send_time + FAIL_DELAY) {
						// give up and bounce the message
						File m = msg.getMessageFile();
						
						Postman.bounceMessage(m, account.getMessageBank(),
								"Freemail has been trying for too long to deliver this message, and has received no acknowledgement. "
								+"It is possible that the recipient has not run Freemail since you sent the message. "
								+"If you believe this is likely, try resending the message.", true);
						Logger.normal(this,"Giving up on message - been trying for too long.");
						msg.delete();
					} else if (System.currentTimeMillis() > msg.last_send_time + RTS_RETRANSMIT_DELAY) {
						Logger.normal(this, "Resending RTS for contact");
						init();

						// bit of a fudge - this won't actually be the last send time, since we won't
						// re-send messages at all now, it will be the last time the RTS was sent.
						// Hack: We update the time for all the messages that have been sent since
						// we only want to resend the RTS once, not once per message
						for(QueuedMessage message : msgs) {
							message.last_send_time = System.currentTimeMillis();
							message.saveProps();
						}
					}
				}
			} catch (FCPException e) {
				Logger.error(this, "Unknown error while fetching ack on key " + key + ": " + e);
				//Don't check the timeout here so we get at least one proper fetch attempt if this
				//is a temporary problem
			}

			if(System.nanoTime() > start + (timeout * 1000 * 1000)) {
				Logger.debug(this, "Stopping ack fetching due to timeout");
				break;
			}
		}

		nextAckIndex = ackIndex;
		if(nextAckIndex >= msgs.size()) {
			nextAckIndex = 0;
		}
	}
	
	/**
	 * Returns the send queue for this contact.
	 * @param comparator the Comparator used to sort the queue. If null, the queue will be unsorted.
	 * @return the send queue for this contact
	 */
	private Set<QueuedMessage> getSendQueue(Comparator<? super QueuedMessage> comparator) {
		File[] files = ctoutbox.listFiles();
		Set<QueuedMessage> msgs;
		if(comparator == null) {
			msgs = new HashSet<QueuedMessage>();
		} else {
			msgs = new TreeSet<QueuedMessage>(comparator);
		}
		
		int i;
		for (i = 0; i < files.length; i++) {
			if (files[i].getName().equals(QueuedMessage.INDEX_FILE)) continue;
				
			int uid;
			try {
				uid = Integer.parseInt(files[i].getName());
			} catch (NumberFormatException nfe) {
				// how did that get there? just delete it
				Logger.normal(this,"Found spurious file in send queue: '"+files[i].getName()+"' - deleting.");
				files[i].delete();
				continue;
			}
			
			msgs.add(new QueuedMessage(uid));
		}
		
		return msgs;
	}
	
	private class QueuedMessage {
		static final String INDEX_FILE = "_index";
	
		PropsFile index;
		
		final int uid;
		String slot;
		long added_time;
		long first_send_time;
		long last_send_time;
		private final File file;
		
		public QueuedMessage(int uid) {
			this.uid = uid;
			this.file = new File(ctoutbox, Integer.toString(uid));
			
			this.index = PropsFile.createPropsFile(new File(ctoutbox, INDEX_FILE));
			
			this.slot = this.index.get(uid+".slot");
			String s_first = this.index.get(uid+".first_send_time");
			if (s_first == null)
				this.first_send_time = -1;
			else
				this.first_send_time = Long.parseLong(s_first);
			
			String s_last = this.index.get(uid+".last_send_time");
			if (s_last == null)
				this.last_send_time = -1;
			else
				this.last_send_time = Long.parseLong(s_last);
			
			String s_added = this.index.get(uid+".added_time");
			if (s_added == null)
				this.added_time = System.currentTimeMillis();
			else
				this.added_time = Long.parseLong(s_added);
		}
		
		public FileInputStream getInputStream() throws FileNotFoundException {
			return new FileInputStream(this.file);
		}
		
		public File getMessageFile() {
			return this.file;
		}
		
		public boolean setMessageFile(File newfile) {
			return newfile.renameTo(this.file);
		}
	
		public boolean saveProps() {
			boolean suc = true;
			suc &= this.index.put(uid+".slot", this.slot);
			suc &= this.index.put(uid+".first_send_time", this.first_send_time);
			suc &= this.index.put(uid+".last_send_time", this.last_send_time);
			suc &= this.index.put(uid+".added_time", this.added_time);
			
			return suc;
		}
		
		public boolean delete() {
			this.index.remove(this.uid+".slot");
			this.index.remove(this.uid+".first_send_time");
			this.index.remove(this.uid+".last_send_time");
			
			return this.file.delete();
		}
	}

	private class MessageUidComparator implements Comparator<QueuedMessage> {
		@Override
		public int compare(QueuedMessage msg1, QueuedMessage msg2) {
			return msg1.uid - msg2.uid;
		}
	}
}

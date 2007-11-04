/*
 * OutboundContact.java
 * This file is part of Freemail, copyright (C) 2006 Dave Baker
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 * 
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
import java.io.PrintWriter;

import freemail.utils.EmailAddress;
import freemail.utils.PropsFile;
import freemail.utils.DateStringFactory;
import freemail.fcp.HighLevelFCPClient;
import freemail.fcp.FCPInsertErrorMessage;
import freemail.fcp.FCPBadFileException;
import freemail.fcp.SSKKeyPair;
import freemail.fcp.ConnectionTerminatedException;

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
	private final File accdir;
	private final File ctoutbox;
	private final EmailAddress address;
	// how long to wait for a CTS before sending the message again
	// slightly over 24 hours since some people are likley to fire Freemail
	// up and roughly the same time every day
	private static final long CTS_WAIT_TIME = 26 * 60 * 60 * 1000;
	private static final String PROPSFILE_NAME = "props";
	// how long do we wait before retransmitting the message? 26 hours allows for people starting Freemail at roughly the same time every day
	private static final long RETRANSMIT_DELAY = 26 * 60 * 60 * 1000;
	// how long do we wait before we give up all hope and just bounce the message back? 5 days is fairly standard, so we'll go with that for now, except that means things bounce when the recipient goes to the Bahamas for a fortnight. Could be longer if we have a GUI to see what messages are in what delivery state.
	private static final long FAIL_DELAY = 5 * 24 * 60 * 60 * 1000;
	private static final int AES_KEY_LENGTH = 256 / 8;
	// this is defined in the AES standard (although the Rijndael
	// algorithm does support other block sizes.
	// we read 128 bytes for our IV, so it needs to be constant.)
	private static final int AES_BLOCK_LENGTH = 128 / 8;
	
	public OutboundContact(File accdir, EmailAddress a) throws BadFreemailAddressException, IOException,
	                                                           OutboundContactFatalException, ConnectionTerminatedException {
		this.address = a;
		
		this.accdir = accdir;
		
		if (!this.address.is_freemail_address()) {
			this.contactfile = null;
			throw new BadFreemailAddressException();
		} else {
			File contactsdir = new File(accdir, SingleAccountWatcher.CONTACTS_DIR);
			if (!contactsdir.exists())
				contactsdir.mkdir();
			File outbounddir = new File(contactsdir, SingleAccountWatcher.OUTBOUND_DIR);
			
			if (!outbounddir.exists())
				outbounddir.mkdir();
			
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
			
			if (!obctdir.exists())
				obctdir.mkdir();
			
			this.contactfile = new PropsFile(new File(obctdir, PROPSFILE_NAME));
			this.ctoutbox = new File(obctdir, OUTBOX_DIR);
			if (!this.ctoutbox.exists())
				this.ctoutbox.mkdir();
		}
	}
	
	public OutboundContact(File accdir, File ctdir) {
		this.accdir = accdir;
		this.address = new EmailAddress();
		this.address.domain = ctdir.getName()+".freemail";
		
		this.contactfile = new PropsFile(new File(ctdir, PROPSFILE_NAME));
		
		this.ctoutbox = new File(ctdir, OUTBOX_DIR);
		if (!this.ctoutbox.exists())
			this.ctoutbox.mkdir();
	}
	
	public void checkCTS() throws OutboundContactFatalException, ConnectionTerminatedException {
		String status = this.contactfile.get("status");
		if (status == null) {
			this.init();
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
			
			System.out.println("polling for CTS message: "+ctskey);
			File cts = fcpcli.fetch(ctskey);
			
			if (cts == null) {
				System.out.println("CTS not received");
				// haven't got the CTS message. should we give up yet?
				String senttime = this.contactfile.get("rts-sent-at");
				
				if (senttime == null || Long.parseLong(senttime) > System.currentTimeMillis() + CTS_WAIT_TIME) {
					// yes, send another RTS
					this.init();
				}
				
			} else {
				System.out.println("Sucessfully received CTS for "+this.address.getSubDomain());
				cts.delete();
				this.contactfile.put("status", "cts-received");
				// delete inital slot for forward secrecy
				this.contactfile.remove("initialslot");
			}
		} else {
			this.init();
		}
	}
	
	private SSKKeyPair getCommKeyPair() throws ConnectionTerminatedException {
		SSKKeyPair ssk = new SSKKeyPair();
		
		ssk.pubkey = this.contactfile.get("commssk.pubkey");
		ssk.privkey = this.contactfile.get("commssk.privkey");
		
		
		if (ssk.pubkey == null || ssk.privkey == null) {
			HighLevelFCPClient cli = new HighLevelFCPClient();
			ssk = cli.makeSSK();
			
			this.contactfile.put("commssk.privkey", ssk.privkey);
			this.contactfile.put("commssk.pubkey", ssk.pubkey);
			// we've just generated a new SSK, so the other party definately doesn't know about it
			this.contactfile.put("status", "notsent");
		}
		
		return ssk;
	}
	
	private SSKKeyPair getAckKeyPair() throws ConnectionTerminatedException {
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
	
	private RSAKeyParameters getPubKey() throws OutboundContactFatalException, ConnectionTerminatedException {
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
	
	private String getRtsKsk() throws OutboundContactFatalException, ConnectionTerminatedException {
		String rtsksk = this.contactfile.get("rtsksk");
		
		if (rtsksk == null) {
			// get it from their mailsite
			if (!this.fetchMailSite()) return null;
			
			rtsksk = this.contactfile.get("rtsksk");
		}
		
		return rtsksk;
	}
	
	private String getInitialSlot() {
		String retval = this.contactfile.get("initialslot");
		
		if (retval != null) return retval;
		
		SecureRandom rnd = new SecureRandom();
		SHA256Digest sha256 = new SHA256Digest();
		byte[] buf = new byte[sha256.getDigestSize()];
		
		rnd.nextBytes(buf);
		
		this.contactfile.put("initialslot", Base32.encode(buf));
		
		return Base32.encode(buf);
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
	private boolean init() throws OutboundContactFatalException, ConnectionTerminatedException {
		// try to fetch get all necessary info. will fetch mailsite / generate new keys if necessary
		String initialslot = this.getInitialSlot();
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
		
		// must include who this RTS is to, otherwise we're vulnerable to surruptitious forwarding
		rtsmessage.append("to="+this.address.getSubDomain()+"\r\n");
		
		// get our mailsite URI
		String our_mailsite_uri = AccountManager.getAccountFile(this.accdir).get("mailsite.pubkey");
		
		rtsmessage.append("mailsite="+our_mailsite_uri+"\r\n");
		
		rtsmessage.append("\r\n");
		//System.out.println(rtsmessage.toString());
		
		// sign the message
		SHA256Digest sha256 = new SHA256Digest();
		sha256.update(rtsmessage.toString().getBytes(), 0, rtsmessage.toString().getBytes().length);
		byte[] hash = new byte[sha256.getDigestSize()];
		sha256.doFinal(hash, 0);
		
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
		
		// make up a symmetric key
		PaddedBufferedBlockCipher aescipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()), new PKCS7Padding());
		
		// quick paranoia check!
		if (aescipher.getBlockSize() != AES_BLOCK_LENGTH) {
			// bouncycastle must have changed their implementation, so 
			// we're in trouble
			System.out.println("Incompatible block size change detected in cryptography API! Are you using a newer version of the bouncycastle libraries? If so, we suggest you downgrade for now, or check for a newer version of Freemail.");
			return false;
		}
		
		byte[] aes_iv_and_key = this.getAESParams();
		
		// now encrypt that with our recipient's public key
		AsymmetricBlockCipher enccipher = new RSAEngine();
		enccipher.init(true, their_pub_key);
		byte[] encrypted_aes_params = null;
		try {
			encrypted_aes_params = enccipher.processBlock(aes_iv_and_key, 0, aes_iv_and_key.length);
		} catch (InvalidCipherTextException e) {
			e.printStackTrace();
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
			icte.printStackTrace();
			return false;
		}
		
		// insert it!
		HighLevelFCPClient cli = new HighLevelFCPClient();
		if (cli.SlotInsert(encmsg, "KSK@"+rtsksk+"-"+DateStringFactory.getKeyString(), 1, "") < 0) {
			// safe to copy the message into the contact outbox though
			return false;
		}
		
		// remember the fact that we have successfully inserted the rts
		this.contactfile.put("status", "rts-sent");
		// and remember when we sent it!
		this.contactfile.put("rts-sent-at", Long.toString(System.currentTimeMillis()));
		// and since that's been sucessfully inserted to that key, we can
		// throw away the symmetric key
		this.contactfile.remove("aesparams");
		return true;
	}
	
	// fetch the redirect (assumes that this is a KSK address)
	private String fetchKSKRedirect(String key) throws OutboundContactFatalException, ConnectionTerminatedException {
		HighLevelFCPClient cli = new HighLevelFCPClient();
		
		System.out.println("Attempting to fetch mailsite redirect "+key);
		File result = cli.fetch(key);
		
		if (result == null) {
			System.out.println("Failed to retrieve mailsite redirect "+key);
			return null;
		}
		
		if (result.length() > 512) {
			System.out.println("Fatal: mailsite redirect too long. Ignoring.");
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
			System.out.println("Warning: IO exception whilst reading mailsite redirect file: "+ioe.getMessage());
			return null;
		}
		result.delete();
		return addr;
	}
	
	private boolean fetchMailSite() throws OutboundContactFatalException, ConnectionTerminatedException {
		HighLevelFCPClient cli = new HighLevelFCPClient();
		
		System.out.println("Attempting to fetch "+this.address.getMailpageKey());
		File mailsite_file = cli.fetch(this.address.getMailpageKey());
		
		if (mailsite_file == null) {
			System.out.println("Failed to retrieve mailsite "+this.address.getMailpageKey());
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
	
	private String popNextSlot() {
		String slot = this.contactfile.get("nextslot");
		if (slot == null) {
			slot = this.getInitialSlot();
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
			System.out.println("IO Error encountered whilst trying to send message: "+ioe.getMessage()+" Will try again soon");
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
			System.out.println("IO Error encountered whilst trying to send message: "+ioe.getMessage()+" Will try again soon");
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
	
	public void doComm() {
		try {
			this.sendQueued();
			this.pollAcks();
			try {
				this.checkCTS();
			} catch (OutboundContactFatalException fe) {
			}
		} catch (ConnectionTerminatedException cte) {
			// just exit
		}
	}
	
	private void sendQueued() throws ConnectionTerminatedException {
		boolean ready;
		String ctstatus = this.contactfile.get("status");
		if (ctstatus == null) ctstatus = "notsent";
		if (ctstatus.equals("rts-sent") || ctstatus.equals("cts-received")) {
			ready = true;
		} else {
			try {
				ready = this.init();
			} catch (OutboundContactFatalException fe) {
				ready = false;
			}
		}
		
		HighLevelFCPClient fcpcli = null;
		
		QueuedMessage[] msgs = this.getSendQueue();
		
		int i;
		for (i = 0; i < msgs.length; i++) {
			if (msgs[i] == null) continue;
			if (msgs[i].last_send_time > 0) continue;
			
			if (!ready) {
				if (msgs[i].added_time + FAIL_DELAY < System.currentTimeMillis()) {
					if (Postman.bounceMessage(msgs[i].getMessageFile(), new MessageBank(this.accdir.getName()), "Freemail has been trying to establish a communication channel with this party for too long without success. Check that the Freemail address is valid, and that the recipient still runs Freemail on at least a semi-regular basis.", true)) {
						msgs[i].delete();
					}
				}
				continue;
			}
			
			if (fcpcli == null) fcpcli = new HighLevelFCPClient();
			
			String key = this.contactfile.get("commssk.privkey");
			
			if (key == null) {
				System.out.println("Contact file does not contain private communication key! It appears that your Freemail directory is corrupt!");
				continue;
			}
			
			key += msgs[i].slot;
			
			FileInputStream fis;
			try {
				fis = new FileInputStream(msgs[i].file);
			} catch (FileNotFoundException fnfe) {
				continue;
			}
			
			System.out.println("Inserting message to "+key);
			FCPInsertErrorMessage err;
			try {
				err = fcpcli.put(fis, key);
			} catch (FCPBadFileException bfe) {
				System.out.println("Failed sending message. Will try again soon.");
				continue;
			}
			if (err == null) {
				System.out.println("Successfully inserted "+key);
				if (msgs[i].first_send_time < 0)
					msgs[i].first_send_time = System.currentTimeMillis();
				msgs[i].last_send_time = System.currentTimeMillis();
				msgs[i].saveProps();
			} else if (msgs[i].added_time + FAIL_DELAY < System.currentTimeMillis()) {
				System.out.println("Giving up on a message - been trying to send for too long. Bouncing.");
				if (Postman.bounceMessage(msgs[i].getMessageFile(), new MessageBank(this.accdir.getName()), "Freemail has been trying to deliver this message for too long without success. This is likley to be due to a poor connection to Freenet. Check your Freenet node.", true)) {
					msgs[i].delete();
				}
			} else {
				System.out.println("Failed to insert "+key+" will try again soon.");
			}
		}
	}
	
	private void pollAcks() throws ConnectionTerminatedException {
		HighLevelFCPClient fcpcli = null;
		QueuedMessage[] msgs = this.getSendQueue();
		
		int i;
		for (i = 0; i < msgs.length; i++) {
			if (msgs[i] == null) continue;
			if (msgs[i].first_send_time < 0) continue;
			
			if (fcpcli == null) fcpcli = new HighLevelFCPClient();
			
			String key = this.contactfile.get("ackssk.pubkey");
			if (key == null) {
				System.out.println("Contact file does not contain public ack key! It appears that your Freemail directory is corrupt!");
				continue;
			}
			
			key += "ack-"+msgs[i].uid;
			
			System.out.println("Looking for message ack on "+key);
			
			File ack = fcpcli.fetch(key);
			if (ack != null) {
				System.out.println("Ack received for message "+msgs[i].uid+" on contact "+this.address.domain+". Now that's a job well done.");
				ack.delete();
				msgs[i].delete();
				// treat the ACK as a CTS too
				this.contactfile.put("status", "cts-received");
				// delete inital slot for forward secrecy
				this.contactfile.remove("initialslot");
			} else {
				System.out.println("Failed to receive ack on "+key);
				if (System.currentTimeMillis() > msgs[i].first_send_time + FAIL_DELAY) {
					// give up and bounce the message
					File m = msgs[i].getMessageFile();
					
					Postman.bounceMessage(m, new MessageBank(this.accdir.getName()), "Freemail has been trying for too long to deliver this message, and has received no acknowledgement. It is possivle that the recipient has not run Freemail since you sent the message. If you believe this is likely, try resending the message.", true);
					System.out.println("Giving up on message - been trying for too long.");
					msgs[i].delete();
				} else if (System.currentTimeMillis() > msgs[i].last_send_time + RETRANSMIT_DELAY) {
					// no ack yet - retransmit on another slot
					msgs[i].slot = this.popNextSlot();
					// mark for re-insertion
					msgs[i].last_send_time = -1;
					msgs[i].saveProps();
				}
			}
		}
	}
	
	private QueuedMessage[] getSendQueue() {
		File[] files = ctoutbox.listFiles();
		QueuedMessage[] msgs = new QueuedMessage[files.length];
		
		int i;
		for (i = 0; i < files.length; i++) {
			if (files[i].getName().equals(QueuedMessage.INDEX_FILE)) continue;
				
			int uid;
			try {
				uid = Integer.parseInt(files[i].getName());
			} catch (NumberFormatException nfe) {
				// how did that get there? just delete it
				System.out.println("Found spurious file in send queue - deleting.");
				files[i].delete();
				msgs[i] = null;
				continue;
			}
			
			msgs[i] = new QueuedMessage(uid);
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
			
			this.index = new PropsFile(new File(ctoutbox, INDEX_FILE));
			
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
}

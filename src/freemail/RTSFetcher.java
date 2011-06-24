/*
 * RTSFetcher.java
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

import freemail.fcp.FCPFetchException;
import freemail.fcp.HighLevelFCPClient;
import freemail.fcp.ConnectionTerminatedException;
import freemail.utils.DateStringFactory;
import freemail.utils.PropsFile;
import freemail.utils.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;
import java.math.BigInteger;
import java.net.MalformedURLException;

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
import org.bouncycastle.crypto.DataLengthException;

import freemail.support.io.LineReadingInputStream;
import freemail.support.io.TooLongException;

import org.archive.util.Base32;

public class RTSFetcher implements SlotSaveCallback {
	private String rtskey;
	private File contact_dir;
	private static final int POLL_AHEAD = 3;
	private static final int PASSES_PER_DAY = 3;
	private static final int MAX_DAYS_BACK = 30;
	public static final String LOGFILE = "rtslog";
	private static final int RTS_MAX_SIZE = 2 * 1024 * 1024;
	private static final String RTS_UNPROC_PREFIX = "unprocessed_rts";
	private static final int RTS_MAX_ATTEMPTS = 15;
	private FreemailAccount account;

	RTSFetcher(String key, File ctdir, FreemailAccount acc) {
		this.rtskey = key;
		this.contact_dir = ctdir;
		this.account = acc;
	}
	
	public void poll() throws ConnectionTerminatedException {
		this.fetch();
		this.handle_unprocessed();
	}
	
	private void handle_unprocessed() throws ConnectionTerminatedException {
		File[] files = this.contact_dir.listFiles();
		
		int i;
		for (i = 0; i < files.length; i++) {
			if (!files[i].getName().startsWith(RTS_UNPROC_PREFIX))
				continue;
			if (this.handle_rts(files[i])) {
				files[i].delete();
			} else {
				String[] parts = files[i].getName().split(",", 2);
				
				int tries;
				if (parts.length < 2) {
					tries = 0;
				} else {
					tries = Integer.parseInt(parts[1]);
				}
				tries++;
				if (tries > RTS_MAX_ATTEMPTS) {
					Logger.normal(this,"Maximum attempts at handling RTS reached - deleting RTS");
					files[i].delete();
				} else {
					File newname = new File(this.contact_dir, parts[0] + "," + tries);
					files[i].renameTo(newname);
				}
			}
		}
	}
	
	private void fetch() throws ConnectionTerminatedException {
		int i;
		RTSLog log = new RTSLog(new File(this.contact_dir, LOGFILE));
		for (i = 1 - MAX_DAYS_BACK; i <= 0; i++) {
			String datestr = DateStringFactory.getOffsetKeyString(i);
			if (log.getPasses(datestr) < PASSES_PER_DAY) {
				boolean successfulPoll = this.fetch_day(log, datestr);
				// don't count passes for today since more
				// mail may arrive
				if (i < 0 && successfulPoll) {
					log.incPasses(datestr);
				}
			}
		}
		
		TimeZone gmt = TimeZone.getTimeZone("GMT");
		Calendar cal = Calendar.getInstance(gmt);
		cal.setTime(new Date());
		
		cal.add(Calendar.DAY_OF_MONTH, 0 - MAX_DAYS_BACK);
		log.pruneBefore(cal.getTime());
	}
	
	private static class MySlotSaveData {
		RTSLog log;
		String date;
	}
	
	/**
	 * @return true if the day was sucessfully polled, false if there were network-type errors and the polling shouldn't count
	 *              as a valid check of that day's slots.
	 */
	private boolean fetch_day(RTSLog log, String date) throws ConnectionTerminatedException {
		HighLevelFCPClient fcpcli;
		fcpcli = new HighLevelFCPClient();
		
		String keybase;
		keybase = this.rtskey + date + "-";
		
		MySlotSaveData cbdata = new MySlotSaveData();
		cbdata.log = log;
		cbdata.date = date;
		
		NaturalSlotManager sm = new NaturalSlotManager(this, cbdata, log.getSlots(date));
		
		sm.setPollAhead(POLL_AHEAD);
		
		int slot;
		boolean success = true;
		while ( (slot = sm.getNextSlotNat()) > 0) {
			Logger.minor(this,"trying to fetch "+keybase+slot);
			
			try {
				File result = fcpcli.fetch(keybase+slot);
				
				Logger.normal(this,keybase+slot+": got RTS!");
				
				File rts_dest = new File(this.contact_dir, RTS_UNPROC_PREFIX + "-" + log.getAndIncUnprocNextId()+",0");
				
				// stick this message in the RTS 'inbox'
				if (result.renameTo(rts_dest)) {
					// provided that worked, we can move on to the next RTS message
					sm.slotUsed();
				}
			} catch (FCPFetchException fe) {
				if (fe.isFatal()) {
					Logger.error(this,keybase+slot+": fatal fetch error - marking slot as used.");
					sm.slotUsed();
				} else if (fe.getCode() == FCPFetchException.ALL_DATA_NOT_FOUND) {
					// This could be the node not managing to find the CHK containing the actual data (since RTS messages are
					// over 1KB, the node will opaquely insert them as a KSK redirect to a CHK, since a KSK/SSK can only hold
					// 1KB of data). It could also be someone inserting dummy redirects to our RTS queue. We'll have to keep
					// checking it, but we have to check slots until we find some that are really empty, we'd never manage
					// to fetch anything if they are dead keys.
					Logger.error(this,keybase+slot+": All Data not found - leaving slot in queue and will poll an extra key");
					sm.incPollAhead();
				} else if (fe.getCode() == FCPFetchException.DATA_NOT_FOUND || fe.getCode() == FCPFetchException.RECENTLY_FAILED) {
					Logger.minor(this,keybase+slot+": no RTS.");
				} else if (fe.isNetworkError()) {
					// Freenet is having special moment. This doesn't count as a valid poll.
					success = false;
				} else {
					// We've covered most things above, so I think this should a fairly exceptional case. Let's log it at error.
					Logger.error(this,keybase+slot+": other non-fatal fetch error:"+fe.getMessage());
				}
			}
		}
		return success;
	}
	
	public void saveSlots(String slots, Object userdata) {
		MySlotSaveData cbdata = (MySlotSaveData) userdata;
		
		cbdata.log.putSlots(cbdata.date, slots);
	}
	
	private boolean handle_rts(File rtsmessage) throws ConnectionTerminatedException {
		// sanity check!
		if (!rtsmessage.exists()) return false;
		
		if (rtsmessage.length() > RTS_MAX_SIZE) {
			Logger.normal(this,"RTS Message is too large - discarding!");
			return true;
		}
		
		// decrypt
		byte[] plaintext;
		try {
			plaintext = decrypt_rts(rtsmessage);
		} catch (IOException ioe) {
			Logger.normal(this,"Error reading RTS message!");
			return false;
		} catch (InvalidCipherTextException icte) {
			Logger.normal(this,"Could not decrypt RTS message - discarding."+icte.getMessage());
			return true;
		}
		
		File rtsfile = null;
		byte[] their_encrypted_sig;
		int messagebytes = 0;
		try {
			rtsfile = File.createTempFile("rtstmp", "tmp", Freemail.getTempDir());
			
			ByteArrayInputStream bis = new ByteArrayInputStream(plaintext);
			LineReadingInputStream lis = new LineReadingInputStream(bis);
			PrintStream ps = new PrintStream(new FileOutputStream(rtsfile));
			
			String line;
			while (true) {
				try {
					line = lis.readLine(200, 200, false);
				} catch (TooLongException tle) {
					Logger.normal(this,"RTS message has lines that are too long. Discarding.");
					rtsfile.delete();
					return true;
				}
				messagebytes += lis.getLastBytesRead();
				
				if (line == null || line.equals("")) break;
				//FreemailLogger.normal(this,line);
				
				ps.println(line);
			}
			
			ps.close();
			
			if (line == null) {
				// that's not right, we shouldn't have reached the end of the file, just the blank line before the signature
				
				Logger.normal(this,"Couldn't find signature on RTS message - ignoring!");
				rtsfile.delete();
				return true;
			}
			
			// read the rest of the file into a byte array.
			// will probably have extra stuff on the end because
			// the byte array returned by the decrypt function
			// isn't resized when we know how much plaintext
			// there is. It would be a waste of time, we know
			// we have to read exactly one RSA block's worth.
			their_encrypted_sig = new byte[bis.available()];
			
			int totalread = 0;
			while (true) {
				int read = bis.read(their_encrypted_sig, totalread, bis.available());
				if (read <= 0) break;
				totalread += read;
			}
			
			bis.close();
		} catch (IOException ioe) {
			Logger.normal(this,"IO error whilst handling RTS message. "+ioe.getMessage());
			ioe.printStackTrace();
			if (rtsfile != null) rtsfile.delete();
			return false;
		}
		
		PropsFile rtsprops = PropsFile.createPropsFile(rtsfile);
		
		try {
			validate_rts(rtsprops);
		} catch (Exception e) {
			Logger.normal(this,"RTS message does not contain vital information: "+e.getMessage()+" - discarding");
			rtsfile.delete();
			return true;
		}
		
		// verify the signature
		String their_mailsite_raw = rtsprops.get("mailsite");
		
		SHA256Digest sha256 = new SHA256Digest();
		sha256.update(plaintext, 0, messagebytes);
		byte[] our_hash = new byte[sha256.getDigestSize()];
		sha256.doFinal(our_hash, 0);
		
		HighLevelFCPClient fcpcli = new HighLevelFCPClient();
		
		FreenetURI their_mailsite_furi;
		try {
			their_mailsite_furi = new FreenetURI(their_mailsite_raw);
		} catch (MalformedURLException mfue) {
			Logger.normal(this,"Mailsite in the RTS message is not a valid Freenet URI. Discarding RTS message.");
			rtsfile.delete();
			return true;
		}
		
		String their_mailsite = "USK@"+their_mailsite_furi.getKeyBody()+"/"+their_mailsite_furi.getSuffix();
		
		if (!their_mailsite.endsWith("/")) {
			their_mailsite += "/";
		}
		their_mailsite += AccountManager.MAILSITE_VERSION+"/"+MailSite.MAILPAGE;
		
		
		Logger.normal(this,"Trying to fetch sender's mailsite: "+their_mailsite);
		File msfile;
		try {
			msfile = fcpcli.fetch(their_mailsite);
		} catch (FCPFetchException fe) {
			// oh well, try again in a bit
			rtsfile.delete();
			return false;
		}
		
		PropsFile mailsite = PropsFile.createPropsFile(msfile);
		String their_exponent = mailsite.get("asymkey.pubexponent");
		String their_modulus = mailsite.get("asymkey.modulus");
		
		if (their_exponent == null || their_modulus == null) {
			Logger.normal(this,"Mailsite fetched successfully but missing vital information! Discarding this RTS.");
			msfile.delete();
			rtsfile.delete();
			return true;
		}
		
		RSAKeyParameters their_pubkey = new RSAKeyParameters(false, new BigInteger(their_modulus, 32), new BigInteger(their_exponent, 32));
		AsymmetricBlockCipher deccipher = new RSAEngine();
		deccipher.init(false, their_pubkey);
		
		byte[] their_hash;
		try {
			their_hash = deccipher.processBlock(their_encrypted_sig, 0, deccipher.getInputBlockSize());
		} catch (InvalidCipherTextException icte) {
			Logger.normal(this,"It was not possible to decrypt the signature of this RTS message. Discarding the RTS message.");
			msfile.delete();
			rtsfile.delete();
			return true;
		}
		
		// finally we can now check that our hash and their hash
		// match!
		if (their_hash.length < our_hash.length) {
			Logger.normal(this,"The signature of the RTS message is not valid (our hash: "+our_hash.length+"bytes, their hash: "+their_hash.length+"bytes. Discarding the RTS message.");
			msfile.delete();
			rtsfile.delete();
			return true;
		}
		int i;
		for (i = 0; i < our_hash.length; i++) {
			if (their_hash[i] != our_hash[i]) {
				Logger.normal(this,"The signature of the RTS message is not valid. Discarding the RTS message.");
				msfile.delete();
				rtsfile.delete();
				return true;
			}
		}
		Logger.normal(this,"Signature valid :)");
		// the signature is valid! Hooray!
		// Now verify the message is for us
		String our_mailsite_keybody;
		try {
			our_mailsite_keybody = new FreenetURI(account.getProps().get("mailsite.pubkey")).getKeyBody();
		} catch (MalformedURLException mfue) {
			Logger.normal(this,"Local mailsite URI is invalid! Corrupt account file?");
			msfile.delete();
			rtsfile.delete();
			return false;
		}
		
		String our_domain_alias = account.getProps().get("domain_alias");
		FreenetURI mailsite_furi;
		try {
			mailsite_furi = new FreenetURI(our_mailsite_keybody);
		} catch (MalformedURLException mfe) {
			msfile.delete();
			rtsfile.delete();
			return false;
		}
		String our_subdomain = Base32.encode(mailsite_furi.getKeyBody().getBytes());
		
		if (!rtsprops.get("to").equalsIgnoreCase(our_subdomain) && our_domain_alias != null && !rtsprops.get("to").equals(our_domain_alias)) {
			Logger.normal(this,"Recieved an RTS message that was not intended for the recipient. Discarding.");
			msfile.delete();
			rtsfile.delete();
			return true;
		}
		
		Logger.normal(this,"Original message intended for us :)");
		
		// create the inbound contact
		InboundContact ibct = new InboundContact(this.contact_dir, their_mailsite_furi);
		
		ibct.setProp("commssk", rtsprops.get("commssk"));
		String ackssk = rtsprops.get("ackssk");
		if (!ackssk.endsWith("/")) ackssk += "/";
		ibct.setProp("ackssk", ackssk);
		ibct.setProp("slots", rtsprops.get("initialslot"));
		
		// insert the cts at some point
		AckProcrastinator.put(ackssk+"cts");
		
		msfile.delete();
		rtsfile.delete();
		
		Logger.normal(this,"Inbound contact created!");
		
		return true;
	}
	
	private byte[] decrypt_rts(File rtsmessage) throws IOException, InvalidCipherTextException {
		// initialise our ciphers
		RSAKeyParameters ourprivkey = AccountManager.getPrivateKey(account.getProps());
		AsymmetricBlockCipher deccipher = new RSAEngine();
		deccipher.init(false, ourprivkey);
		
		PaddedBufferedBlockCipher aescipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()), new PKCS7Padding());
		
		// first n bytes will be an encrypted RSA block containting the
		// AES IV and Key. Read that.
		byte[] encrypted_params = new byte[deccipher.getInputBlockSize()];
		FileInputStream fis = new FileInputStream(rtsmessage);
		int read = 0;
		
		while (read < encrypted_params.length) {
			read += fis.read(encrypted_params, read, encrypted_params.length - read);
			if (read < 0) break;
		}
		
		if (read < 0) {
			throw new InvalidCipherTextException("RTS Message too short");
		}
		
		byte[] aes_iv_and_key = deccipher.processBlock(encrypted_params, 0, encrypted_params.length);
		
		KeyParameter kp = new KeyParameter(aes_iv_and_key, aescipher.getBlockSize(), aes_iv_and_key.length - aescipher.getBlockSize());
		ParametersWithIV kpiv = new ParametersWithIV(kp, aes_iv_and_key, 0, aescipher.getBlockSize());
		try {
			aescipher.init(false, kpiv);
		} catch (IllegalArgumentException iae) {
			throw new InvalidCipherTextException(iae.getMessage());
		}
		
		byte[] plaintext = new byte[aescipher.getOutputSize((int)rtsmessage.length() - read)];
		
		int ptbytes = 0;
		while (read < rtsmessage.length()) {
			byte[] buf = new byte[(int)rtsmessage.length() - read];
			
			int thisread = fis.read(buf, 0, (int)rtsmessage.length() - read);
			ptbytes += aescipher.processBytes(buf, 0, thisread, plaintext, ptbytes);
			read += thisread;
		}
		
		fis.close();
		
		try {
			aescipher.doFinal(plaintext, ptbytes);
		} catch (DataLengthException dle) {
			throw new InvalidCipherTextException(dle.getMessage());
		}
		
		return plaintext;
	}
	
	/*
	 * Make sure an RTS file has all the right properties in it
	 * If any are missing, throw an exception which says which are missing
	 */
	private void validate_rts(PropsFile rts) throws Exception {
		StringBuffer missing = new StringBuffer();
		
		if (rts.get("mailsite") == null) {
			missing.append("mailsite, ");
		}
		if (rts.get("to") == null) {
			missing.append("to, ");
		}
		if (rts.get("channel") == null) {
			missing.append("channel, ");
		}
		if (rts.get("initiatorSlot") == null) {
			missing.append("initiatorSlot, ");
		}
		if (rts.get("responderSlot") == null) {
			missing.append("responderSlot, ");
		}
		
		if (missing.length() == 0) return;
		throw new Exception(missing.toString().substring(0, missing.length() - 2));
	}
}

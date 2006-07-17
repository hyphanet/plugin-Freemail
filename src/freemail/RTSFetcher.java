package freemail;

import freemail.fcp.FCPConnection;
import freemail.fcp.HighLevelFCPClient;
import freemail.utils.DateStringFactory;
import freemail.utils.PropsFile;
import freemail.utils.ChainedAsymmetricBlockCipher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.InvalidCipherTextException;

import freenet.support.io.LineReadingInputStream;

public class RTSFetcher {
	private String rtskey;
	private File contact_dir;
	private final SimpleDateFormat sdf;
	private static final int POLL_AHEAD = 3;
	private static int PASSES_PER_DAY = 3;
	private static int MAX_DAYS_BACK = 30;
	private static String LOGFILE = "rtslog";
	private static int RTS_MAX_SIZE = 2 * 1024 * 1024;
	private File accdir;
	private PropsFile accprops;

	RTSFetcher(String key, File ctdir, File ad) {
		this.rtskey = key;
		this.sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z");
		this.contact_dir = ctdir;
		this.accdir = ad;
		this.accprops = AccountManager.getAccountFile(this.accdir);
	}
	
	public void fetch() {
		int i;
		RTSLog log = new RTSLog(new File(this.contact_dir, LOGFILE));
		for (i = 1 - MAX_DAYS_BACK; i <= 0; i++) {
			String datestr = DateStringFactory.getOffsetKeyString(i);
			if (log.getPasses(datestr) < PASSES_PER_DAY) {
				this.fetch_day(log, datestr);
				// don't count passes for today since more
				// mail may arrive
				if (i < 0) {
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
	
	private void fetch_day(RTSLog log, String date) {
		HighLevelFCPClient fcpcli;
		fcpcli = new HighLevelFCPClient();
		
		String keybase;
		keybase = this.rtskey + date + "-";
		
		int startnum = log.getNextId(date);
		
		for (int i = startnum; i < startnum + POLL_AHEAD; i++) {
			System.out.println("trying to fetch "+keybase+i);
			
			File result = fcpcli.fetch(keybase+i);
			
			if (result != null) {
				System.out.println(keybase+i+": got RTS!");
				// if we didn't successfully handle (or fatally fail to handle) the RTS message, don't increment the id, so we'll get it again in a bit
				if (this.handle_rts(result)) {
					log.incNextId(date);
				}
				result.delete();
			} else {
				System.out.println(keybase+i+": no RTS.");
			}
		}
	}
	
	
	
	private boolean handle_rts(File rtsmessage) {
		// sanity check!
		if (!rtsmessage.exists()) return false;
		
		if (rtsmessage.length() > RTS_MAX_SIZE) {
			System.out.println("RTS Message is too large - discarding!");
			return true;
		}
		
		// decrypt
		byte[] plaintext;
		try {
			plaintext = decrypt_rts(rtsmessage);
		} catch (IOException ioe) {
			System.out.println("Error reading RTS message!");
			return false;
		} catch (InvalidCipherTextException icte) {
			System.out.println("Could not decrypt RTS message - discarding.");
			return true;
		}
		
		File rtsfile;
		byte[] sig;
		int messagebytes = 0;
		try {
			rtsfile = File.createTempFile("rtstmp", "tmp", Freemail.getTempDir());
			
			ByteArrayInputStream bis = new ByteArrayInputStream(plaintext);
			LineReadingInputStream lis = new LineReadingInputStream(bis);
			PrintStream ps = new PrintStream(new FileOutputStream(rtsfile));
			
			String line;
			while (true) {
				line = lis.readLine(200, 200);
				messagebytes += lis.getLastBytesRead();
				
				if (line == null || line.equals("")) break;
				
				ps.println(line);
			}
			
			ps.close();
			
			if (line == null) {
				// that's not right, we shouldn't have reached the end of the file, just the blank line before the signature
				
				System.out.println("Couldn't find signature on RTS message - ignoring!");
				return true;
			}
			
			sig = new byte[bis.available()];
			
			int read = 0;
			while (true) {
				read = bis.read(sig, 0, bis.available());
				if (read == 0) break;
			}
			bis.close();
		} catch (IOException ioe) {
			System.out.println("IO error whilst handling RTS message. "+ioe.getMessage());
			ioe.printStackTrace();
			return false;
		}
		
		PropsFile rtsprops = new PropsFile(rtsfile);
		
		try {
			validate_rts(rtsprops);
		} catch (Exception e) {
			System.out.println("RTS message does not contain vital information: "+e.getMessage()+" - discarding");
			return true;
		}
		
		// verify the signature
		String their_mailsite = rtsprops.get("mailsite");
		
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException alge) {
			System.out.println("No MD5 implementation available - sorry, Freemail cannot work!");
			return false;
		}
		md.update(plaintext, 0, messagebytes);
		byte[] our_hash = md.digest();
		
		HighLevelFCPClient fcpcli = new HighLevelFCPClient();
		fcpcli.fetch(their_mailsite);
		
		// TODO: finish.
		
		// verify the message is for us
		
		// create the inbound contact
		
		// move the props file to the right place
		
		return true;
	}
	
	private byte[] decrypt_rts(File rtsmessage) throws IOException, InvalidCipherTextException {
		byte[] ciphertext = new byte[(int)rtsmessage.length()];
		FileInputStream fis = new FileInputStream(rtsmessage);
		int read = 0;
		while (read < rtsmessage.length()) {
			read += fis.read(ciphertext, read, (int)rtsmessage.length() - read);
		}
		
		RSAKeyParameters ourprivkey = AccountManager.getPrivateKey(this.accdir);
		
		// decrypt it
		AsymmetricBlockCipher deccipher = new RSAEngine();
		deccipher.init(false, ourprivkey);
		byte[] plaintext = ChainedAsymmetricBlockCipher.decrypt(deccipher, ciphertext);
		
		return plaintext;
	}
	
	/*
	 * Make sure an RTS file has all the right properties in it
	 * If any are missing, throw an exception which says which are missing
	 */
	private void validate_rts(PropsFile rts) throws Exception {
		StringBuffer missing = new StringBuffer();
		
		if (rts.get("commssk") == null) {
			missing.append("commssk");
		}
		if (rts.get("ackssk") == null) {
			missing.append("ackssk");
		}
		if (rts.get("messagetype") == null) {
			missing.append("messagetype");
		}
		if (rts.get("to") == null) {
			missing.append("to");
		}
		if (rts.get("mailsite") == null) {
			missing.append("mailsite");
		}
		if (rts.get("ctsssk") == null) {
			missing.append("ctsssk");
		}
		
		if (missing.length() == 0) return;
		throw new Exception(missing.toString());
	}
}

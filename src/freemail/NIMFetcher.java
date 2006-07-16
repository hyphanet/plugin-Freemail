package freemail;

import freemail.fcp.FCPConnection;
import freemail.fcp.HighLevelFCPClient;
import freemail.utils.DateStringFactory;

import java.io.File;
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

public class NIMFetcher {
	private final MessageBank mb;
	private File contact_dir;
	private final SimpleDateFormat sdf;
	private static final int POLL_AHEAD = 3;
	private static int PASSES_PER_DAY = 3;
	private static int MAX_DAYS_BACK = 30;

	NIMFetcher(MessageBank m, File ctdir) {
		this.mb = m;
		this.sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z");
		this.contact_dir = ctdir;
	}
	
	public void fetch() {
		Contact contact = new Contact(this.contact_dir);
		
		int i;
		for (i = 1 - MAX_DAYS_BACK; i <= 0; i++) {
			String datestr = DateStringFactory.getOffsetKeyString(i);
			MailLog log = contact.getLog(datestr);
			
			if (log.getPasses() < PASSES_PER_DAY) {
				this.fetch_day(contact, log, datestr);
				// don't count passes for today since more
				// mail may arrive
				if (i < 0) log.incPasses();
			}
		}
		
		TimeZone gmt = TimeZone.getTimeZone("GMT");
		Calendar cal = Calendar.getInstance(gmt);
		cal.setTime(new Date());
		
		cal.add(Calendar.DAY_OF_MONTH, 0 - MAX_DAYS_BACK);
		contact.pruneLogs(cal.getTime());
	}
	
	private void fetch_day(Contact contact, MailLog log, String date) {
		HighLevelFCPClient fcpcli;
		fcpcli = new HighLevelFCPClient();
		
		String keybase;
		try {
			keybase = contact.getKey() + date + "-";
		} catch (IOException ioe) {
			// Jinkies, Scoob! No key!
			return;
		}
		
		int startnum = log.getNextMessageId();
		
		for (int i = startnum; i < startnum + POLL_AHEAD; i++) {
			System.out.println("trying to fetch "+keybase+i);
			
			File result = fcpcli.fetch(keybase+i);
			
			if (result != null) {
				System.out.println(keybase+i+": got message!");
				try {
					String checksum = this.storeMessage(result);
					log.addMessage(i, checksum);
				} catch (IOException ioe) {
					continue;
				}
			} else {
				System.out.println(keybase+i+": no message.");
			}
		}
	}
	
	private String storeMessage(File file) throws IOException {
		MailMessage newmsg = this.mb.createMessage();
		
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException alge) {
			System.out.println("No MD5 implementation available - can't checksum messages - not storing message.");
			return null;
		}
		
		// add our own headers first
		// recieved and date
		newmsg.addHeader("Received", "(Freemail); "+this.sdf.format(new Date()));
		
		BufferedReader rdr = new BufferedReader(new FileReader(file));
		
		newmsg.readHeaders(rdr);
		
		PrintStream ps = newmsg.writeHeadersAndGetStream();
		
		String line;
		while ( (line = rdr.readLine()) != null) {
			ps.println(line);
		}
		
		newmsg.commit();
		rdr.close();
		file.delete();
		
		byte[] checksum = md.digest();
		return new String(Hex.encode(checksum));
	}
}

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

public class RTSFetcher {
	private String rtskey;
	private File contact_dir;
	private final SimpleDateFormat sdf;
	private static final int POLL_AHEAD = 3;
	private static int PASSES_PER_DAY = 3;
	private static int MAX_DAYS_BACK = 30;
	private static String LOGFILE = "rtslog";

	RTSFetcher(String key, File ctdir) {
		this.rtskey = key;
		this.sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z");
		this.contact_dir = ctdir;
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
				log.incNextId(date);
				// TODO: handle the RTS!
			} else {
				System.out.println(keybase+i+": no RTS.");
			}
		}
	}
}

package freemail;

import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Date;

import freemail.utils.DateStringFactory;

public class NIMContact {
	public static final String KEYFILE = "key";
	private static final String LOGFILE_PREFIX = "log-";
	private final File contact_dir;
	private final File keyfile;

	NIMContact(File dir) {
		this.contact_dir = dir;
		this.keyfile = new File(dir, KEYFILE);
	}
	
	public String getKey() throws IOException {
		FileReader frdr = new FileReader(this.keyfile);
		BufferedReader br = new BufferedReader(frdr);
		String key =  br.readLine();
		frdr.close();
		return key;
	}
	
	public MailLog getLog(String date) {
		return new MailLog(new File(this.contact_dir, LOGFILE_PREFIX + date));
	}
	
	public void pruneLogs(Date keepafter) {
		File[] files = contact_dir.listFiles();
		
		int i;
		for (i = 0; i< files.length; i++) {
			if (!files[i].getName().startsWith(LOGFILE_PREFIX))
				continue;
			Date logdate = DateStringFactory.DateFromKeyString(files[i].getName().substring(LOGFILE_PREFIX.length()));
			if (logdate == null) {
				// couldn't parse the date... hmm
				files[i].delete();
			} else if (logdate.before(keepafter)) {
				files[i].delete();
			}
		}
	}
}

package fnmail;

import java.io.File;
import java.lang.InterruptedException;

public class SingleAccountWatcher implements Runnable {
	public static final String CONTACTS_DIR = "contacts";
	private static final int MIN_POLL_DURATION = 60000; // in milliseconds
	private static final int MAILSITE_UPLOAD_INTERVAL = 60 * 60 * 1000;
	private final MessageBank mb;
	private final MailFetcher mf;

	SingleAccountWatcher(File accdir) {
		File contacts_dir = new File(accdir, CONTACTS_DIR);
		
		/////////////////
		AccountManager.getMailsitePrivkey(accdir);
		
		this.mb = new MessageBank(accdir.getName());
		this.mf = new MailFetcher(this.mb, contacts_dir, FNMail.getFCPConnection());
	}
	
	public void run() {
		while (true) {
			long start = System.currentTimeMillis();
			
			mf.fetch_from_all();
			
			long runtime = System.currentTimeMillis() - start;
			
			if (MIN_POLL_DURATION - runtime > 0) {
				try {
					Thread.sleep(MIN_POLL_DURATION - runtime);
				} catch (InterruptedException ie) {
				}
			}
		}
	}
}

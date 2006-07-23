package freemail;

import java.io.File;
import java.lang.InterruptedException;

import freemail.utils.PropsFile;

public class SingleAccountWatcher implements Runnable {
	public static final String CONTACTS_DIR = "contacts";
	public static final String INBOUND_DIR = "inbound";
	private static final int MIN_POLL_DURATION = 60000; // in milliseconds
	private static final int MAILSITE_UPLOAD_INTERVAL = 60 * 60 * 1000;
	private final MessageBank mb;
	private final NIMFetcher nf;
	private final RTSFetcher rtsf;
	private long mailsite_last_upload;
	private final PropsFile accprops;

	SingleAccountWatcher(File accdir) {
		this.accprops = AccountManager.getAccountFile(accdir);
		File contacts_dir = new File(accdir, CONTACTS_DIR);
		File inbound_dir = new File(contacts_dir, INBOUND_DIR);
		this.mailsite_last_upload = 0;
		
		if (!inbound_dir.exists()) {
			inbound_dir.mkdir();
		}
		
		this.mb = new MessageBank(accdir.getName());
		
		File nimdir = new File(contacts_dir, AccountManager.NIMDIR);
		if (nimdir.exists()) {
			this.nf = new NIMFetcher(this.mb, nimdir);
		} else {
			this.nf = null;
		}
		
		
		this.rtsf = new RTSFetcher("KSK@"+this.accprops.get("rtskey")+"-", inbound_dir, accdir);
		
		//this.mf = new MailFetcher(this.mb, inbound_dir, Freemail.getFCPConnection());
	}
	
	public void run() {
		while (true) {
			long start = System.currentTimeMillis();
			
			if (System.currentTimeMillis() > this.mailsite_last_upload + MAILSITE_UPLOAD_INTERVAL) {
				MailSite ms = new MailSite(this.accprops);
				if (ms.Publish() > 0) {
					this.mailsite_last_upload = System.currentTimeMillis();
				}
			}
			
			if (this.nf != null) {
				nf.fetch();
			}
			
			this.rtsf.poll();
			
			//mf.fetch_from_all();
			
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

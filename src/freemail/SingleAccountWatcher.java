/*
 * SingleAccountWatcher.java
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
import java.io.FilenameFilter;
import java.lang.InterruptedException;

import freemail.utils.PropsFile;
import freemail.fcp.ConnectionTerminatedException;
import freemail.utils.Logger;

public class SingleAccountWatcher implements Runnable {
	/**
	 * Whether the thread this service runs in should stop.
	 */
	protected volatile boolean stopping = false;

	public static final String CONTACTS_DIR = "contacts";
	public static final String INBOUND_DIR = "inbound";
	public static final String OUTBOUND_DIR = "outbound";
	private static final int MIN_POLL_DURATION = 60000; // in milliseconds
	private static final int MAILSITE_UPLOAD_INTERVAL = 60 * 60 * 1000;
	private final MessageBank mb;
	private final NIMFetcher nf;
	private final RTSFetcher rtsf;
	private long mailsite_last_upload;
	private final PropsFile accprops;
	private final File obctdir;
	private final File ibctdir;
	private final File accdir;

	SingleAccountWatcher(File accdir) {
		this.accdir = accdir;
		this.accprops = AccountManager.getAccountFile(accdir);
		File contacts_dir = new File(accdir, CONTACTS_DIR);
		
		if (!contacts_dir.exists()) {
			contacts_dir.mkdir();
		}
		
		this.ibctdir = new File(contacts_dir, INBOUND_DIR);
		this.obctdir = new File(contacts_dir, OUTBOUND_DIR);
		this.mailsite_last_upload = 0;
		
		if (!this.ibctdir.exists()) {
			this.ibctdir.mkdir();
		}
		
		this.mb = new MessageBank(accdir.getName());
		
		File nimdir = new File(contacts_dir, AccountManager.NIMDIR);
		if (nimdir.exists()) {
			this.nf = new NIMFetcher(this.mb, nimdir);
		} else {
			this.nf = null;
		}
		
		String rtskey=this.accprops.get("rtskey");

		if(rtskey==null) {
			Logger.error(this,"Your accprops file is missing the rtskey entry. This means it is broken, you will not be able to receive new contact requests.");
		}

		this.rtsf = new RTSFetcher("KSK@"+rtskey+"-", this.ibctdir, accdir);
		
		//this.mf = new MailFetcher(this.mb, inbound_dir, Freemail.getFCPConnection());
		
		// temporary info message until there's a nicer UI :)
		String freemailDomain=AccountManager.getFreemailDomain(accdir);
		if(freemailDomain!=null) {
			Logger.normal(this,"Secure Freemail address: <anything>@"+AccountManager.getFreemailDomain(accdir));
		} else {
			Logger.error(this, "You do not have a freemail address USK. This account is really broken.");
		}
		
		String shortdomain = AccountManager.getKSKFreemailDomain(accdir);
		if (shortdomain != null) {
			Logger.normal(this,"Short Freemail address (*probably* secure): <anything>@"+shortdomain);

			String invalid=AccountManager.validateUsername(shortdomain);
			if(!invalid.equals("")) {
				Logger.normal(this,"Your short Freemail address contains invalid characters (\""+invalid+"\"), others may have problems sending you mail");
			}
		} else {
			Logger.normal(this,"You don't have a short Freemail address. You could get one by running Freemail with the --shortaddress option, followed by your account name and the name you'd like. For example, 'java -jar freemail.jar --shortaddress bob bob' will give you all addresses ending '@bob.freemail'. Try to pick something unique!");
		}
	}
	
	public void run() {
		while (!stopping) {
			try {
				long start = System.currentTimeMillis();
				
				// is it time we inserted the mailsite?
				if (System.currentTimeMillis() > this.mailsite_last_upload + MAILSITE_UPLOAD_INTERVAL) {
					MailSite ms = new MailSite(this.accprops);
					if (ms.Publish() > 0) {
						this.mailsite_last_upload = System.currentTimeMillis();
					}
				}
				if(stopping) {
					break;
				}
				// send any messages queued in contact outboxes
				File[] obcontacts = this.obctdir.listFiles(new outboundContactFilenameFilter());
				if (obcontacts != null) {
					int i;
					for (i = 0; i < obcontacts.length; i++) {
						OutboundContact obct = new OutboundContact(this.accdir, obcontacts[i]);
						
						obct.doComm();
					}
				}
				if (this.nf != null) {
					nf.fetch();
				}
				this.rtsf.poll();
				if(stopping) {
					break;
				}
				
				// poll for incoming message from all inbound contacts
				File[] ibcontacts = this.ibctdir.listFiles(new inboundContactFilenameFilter());
				if (ibcontacts != null) {
					int i;
					for (i = 0; i < ibcontacts.length; i++) {
						if (ibcontacts[i].getName().equals(RTSFetcher.LOGFILE)) continue;
						
						InboundContact ibct = new InboundContact(this.ibctdir, ibcontacts[i].getName());
						
						ibct.fetch(this.mb);
					}
				}
				if(stopping) {
					break;
				}
			
				long runtime = System.currentTimeMillis() - start;
				
				if (MIN_POLL_DURATION - runtime > 0) {
					try {
						Thread.sleep(MIN_POLL_DURATION - runtime);
					} catch (InterruptedException ie) {
					}
				}
			} catch (ConnectionTerminatedException cte) {

			}
		}
	}

	/**
	 * Terminate the run method
	 */
	public void kill() {
		stopping = true;
	}

	private class outboundContactFilenameFilter implements FilenameFilter {
		// check that each dir is a base32 encoded filename
		public boolean accept(File dir, String name ) {
			return name.matches("[A-Za-z2-7]+");
		}
	}

	private class inboundContactFilenameFilter implements FilenameFilter {
		// check that each dir is a freenet key
		public boolean accept(File dir, String name ) {
			return name.matches("[A-Za-z0-9~-]+,[A-Za-z0-9~-]+,[A-Za-z0-9~-]+");
		}
	}

}

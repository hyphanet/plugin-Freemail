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
import java.lang.InterruptedException;

import freemail.utils.PropsFile;
import freemail.utils.EmailAddress;

public class SingleAccountWatcher implements Runnable {
	/**
	 * Object that is used for syncing purposes.
	 */
	protected final Object syncObject = new Object();

	/**
	 * Whether the thread this service runs in should stop.
	 */
	protected volatile boolean stopping = false;

	/**
	 * The currently running threads.
	 */
	private Thread thread;
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
		
		
		this.rtsf = new RTSFetcher("KSK@"+this.accprops.get("rtskey")+"-", this.ibctdir, accdir);
		
		//this.mf = new MailFetcher(this.mb, inbound_dir, Freemail.getFCPConnection());
		
		// temporary info message until there's a nicer UI :)
		System.out.println("Secure Freemail address: "+AccountManager.getFreemailAddress(accdir));
		
		EmailAddress shortaddr = AccountManager.getKSKFreemailAddress(accdir);
		if (shortaddr != null) {
			System.out.println("Short Freemail address (*probably* secure): "+shortaddr);
		} else {
			System.out.println("You don't have a short Freemail address. You could get one by running Freemail with the --shortaddress option, followed by your account name and the name you'd like. For example, 'java -jar freemail.jar --shortaddress bob bob' will give you the address 'anything@bob.freemail'. Try to pick something unique!");
		}
	}
	
	public void run() {
		thread = Thread.currentThread();
		while (!stopping) {
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
			File[] obcontacts = this.obctdir.listFiles();
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
			File[] ibcontacts = this.ibctdir.listFiles();
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
		}
		synchronized (syncObject) {
			thread = null;
			syncObject.notify();
		}

	}

	/**
	 * This method will block until the
	 * thread has exited.
	 */
	public void kill() {
		synchronized (syncObject) {
			stopping = true;
			while (thread != null) {
				syncObject.notify();
				try {
					syncObject.wait(1000);
				} catch (InterruptedException ie1) {
				}
			}
		}
	}
}

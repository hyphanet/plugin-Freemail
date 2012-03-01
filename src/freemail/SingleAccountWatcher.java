/*
 * SingleAccountWatcher.java
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

import java.io.File;
import java.lang.InterruptedException;

import freemail.fcp.ConnectionTerminatedException;
import freemail.utils.Logger;
import freemail.wot.WoTConnection;
import freemail.wot.WoTProperties;
import freenet.pluginmanager.PluginNotFoundException;

public class SingleAccountWatcher implements Runnable {
	/**
	 * Whether the thread this service runs in should stop.
	 */
	protected volatile boolean stopping = false;

	public static final String RTS_DIR = "rts";
	private static final int MIN_POLL_DURATION = 5 * 60 * 1000; // in milliseconds
	private static final int MAILSITE_UPLOAD_INTERVAL = 60 * 60 * 1000;
	private final RTSFetcher rtsf;
	private long mailsite_last_upload;
	private final FreemailAccount account;
	private final Freemail freemail;
	private final File rtsdir;

	SingleAccountWatcher(FreemailAccount acc, Freemail freemail) {
		this.account = acc;
		this.freemail = freemail;
		this.mailsite_last_upload = 0;
		
		rtsdir = new File(account.getAccountDir(), RTS_DIR);
		
		String rtskey=account.getProps().get("rtskey");

		if(rtskey==null) {
			Logger.error(this,"Your accprops file is missing the rtskey entry. This means it is broken, you will not be able to receive new contact requests.");
		}

		this.rtsf = new RTSFetcher("KSK@"+rtskey+"-", rtsdir, account);
		
		//this.mf = new MailFetcher(this.mb, inbound_dir, Freemail.getFCPConnection());
		
		// temporary info message until there's a nicer UI :)
		String freemailDomain=account.getDomain();
		if(freemailDomain!=null) {
			Logger.normal(this,"Secure Freemail address: <anything>@"+freemailDomain);
		} else {
			Logger.error(this, "You do not have a freemail address USK. This account is really broken.");
		}
	}
	
	public void run() {
		while (!stopping) {
			try {
				long start = System.currentTimeMillis();
				
				// is it time we inserted the mailsite?
				if (System.currentTimeMillis() > this.mailsite_last_upload + MAILSITE_UPLOAD_INTERVAL) {
					int editionHint = 0;

					//Try to get the edition from WoT
					WoTConnection wotConnection = freemail.getWotConnection();
					if(wotConnection != null) {
						try {
							String hint = wotConnection.getProperty(
									account.getIdentity(), WoTProperties.MAILSITE_EDITION);
							editionHint = Integer.parseInt(hint);
						} catch (PluginNotFoundException e) {
							//Only means that we can't get the hint from WoT so ignore it
						} catch (NumberFormatException e) {
							//Same as above, so ignore this too
						}
					}

					//And from the account file
					String hint = account.getProps().get("mailsite.slot");
					try {
						int slot = Integer.parseInt(hint);
						if(slot > editionHint) {
							editionHint = slot;
						}
					} catch (NumberFormatException e) {
						//Same as for the WoT approach
					}

					MailSite ms = new MailSite(account.getProps());
					int edition = ms.publish(editionHint);
					if (edition > 0) {
						this.mailsite_last_upload = System.currentTimeMillis();
						if(wotConnection != null) {
							try {
								wotConnection.setProperty(account.getIdentity(), WoTProperties.MAILSITE_EDITION, "" + edition);
							} catch(PluginNotFoundException e) {
								//In most cases this doesn't matter since the edition doesn't
								//change very often anyway
								Logger.normal(this, "WoT plugin not loaded, can't save mailsite edition");
							}
						}
					}
				}
				if(stopping) {
					break;
				}
				Logger.debug(this, "polling rts");
				this.rtsf.poll();
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
}

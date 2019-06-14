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

package org.freenetproject.freemail;

import java.io.File;
import java.io.IOException;
import java.lang.InterruptedException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.freenetproject.freemail.fcp.ConnectionTerminatedException;
import org.freenetproject.freemail.utils.Logger;
import org.freenetproject.freemail.utils.Timer;
import org.freenetproject.freemail.wot.WoTConnection;
import org.freenetproject.freemail.wot.WoTException;
import org.freenetproject.freemail.wot.WoTProperties;

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
	private boolean hasSetWoTContext;

	SingleAccountWatcher(FreemailAccount acc, Freemail freemail) {
		this.account = acc;
		this.freemail = freemail;
		this.mailsite_last_upload = 0;

		rtsdir = new File(account.getAccountDir(), RTS_DIR);

		String rtskey=account.getProps().get("rtskey");

		if(rtskey==null) {
			Logger.error(this, "Your accprops file is missing the rtskey entry. This means it is broken, you will not be able to receive new contact requests.");
		}

		this.rtsf = new RTSFetcher("KSK@"+rtskey+"-", rtsdir, account);

		//this.mf = new MailFetcher(this.mb, inbound_dir, Freemail.getFCPConnection());

		// temporary info message until there's a nicer UI :)
		String freemailDomain=account.getDomain();
		if(freemailDomain!=null) {
			Logger.normal(this, "Secure Freemail address: <anything>@"+freemailDomain);
		} else {
			Logger.error(this, "You do not have a freemail address USK. This account is really broken.");
		}
	}

	@Override
	public void run() {
		while(!stopping) {
			try {
				long start = System.currentTimeMillis();
				WoTConnection wotConnection = freemail.getWotConnection();

				insertMailsite(wotConnection);
				setWoTContext(wotConnection);

				if(stopping) {
					break;
				}
				Logger.debug(this, "polling rts");
				this.rtsf.poll();
				if(stopping) {
					break;
				}

				long runtime = System.currentTimeMillis() - start;

				if(MIN_POLL_DURATION - runtime > 0) {
					Thread.sleep(MIN_POLL_DURATION - runtime);
				}
			} catch (ConnectionTerminatedException cte) {

			} catch (InterruptedException ie) {
				Logger.debug(this, "SingleAccountWatcher interrupted, stopping");
				kill();
				break;
			}
		}
	}

	private void insertMailsite(WoTConnection wotConnection) throws InterruptedException {
		// is it time we inserted the mailsite?
		if(System.currentTimeMillis() > this.mailsite_last_upload + MAILSITE_UPLOAD_INTERVAL) {
			int editionHint = 1;

			//Try to get the edition from WoT
			if(wotConnection != null) {
				Timer propertyRead = Timer.start();
				try {
					String hint = wotConnection.getProperty(
							account.getIdentity(), WoTProperties.MAILSITE_EDITION);
					editionHint = Integer.parseInt(hint);
				} catch (PluginNotFoundException | NumberFormatException | IOException | TimeoutException | WoTException e) {
					//Only means that we can't get the hint from WoT so ignore it
				}
				propertyRead.log(this, 1, TimeUnit.HOURS, "Time spent getting mailsite property");
			}

			//And from the account file
			try {
				int slot = Integer.parseInt(account.getProps().get("mailsite.slot"));
				if(slot > editionHint) {
					editionHint = slot;
				}
			} catch (NumberFormatException e) {
				//Same as for the WoT approach
			}

			MailSite ms = new MailSite(account.getProps());
			Timer mailsiteInsert = Timer.start();
			int edition = ms.publish(editionHint);
			mailsiteInsert.log(this, 1, TimeUnit.HOURS, "Time spent inserting mailsite");
			if(edition >= 0) {
				this.mailsite_last_upload = System.currentTimeMillis();
				if(wotConnection != null) {
					Timer propertyUpdate = Timer.start();
					try {
						wotConnection.setProperty(account.getIdentity(), WoTProperties.MAILSITE_EDITION, "" + edition);
					} catch(PluginNotFoundException | IOException | TimeoutException e) {
						//In most cases this doesn't matter since the edition doesn't
						//change very often anyway
						Logger.normal(this, "WoT plugin not loaded, can't save mailsite edition");
					}
					propertyUpdate.log(this, 1, TimeUnit.HOURS, "Time spent setting mailsite property");
				}
			}
		}
	}

	private void setWoTContext(WoTConnection wotConnection) {
		if(hasSetWoTContext) {
			return;
		}
		if(wotConnection == null) {
			return;
		}

		Timer contextWrite = Timer.start();
		try {
			if(!wotConnection.setContext(account.getIdentity(), WoTProperties.CONTEXT)) {
				Logger.error(this, "Setting WoT context failed");
			} else {
				hasSetWoTContext = true;
			}
		} catch (PluginNotFoundException | TimeoutException | IOException e) {
			Logger.normal(this, "WoT plugin not loaded, can't set Freemail context");
		}
		contextWrite.log(this, 1, TimeUnit.HOURS, "Time spent adding WoT context");
	}

	/**
	 * Terminate the run method
	 */
	public void kill() {
		stopping = true;
	}
}

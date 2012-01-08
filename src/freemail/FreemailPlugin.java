/*
 * FreemailPlugin.java
 * This file is part of Freemail
 * Copyright (C) 2006,2007,2008 Dave Baker
 * Copyright (C) 2007,2008 Alexander Lehmann
 * Copyright (C) 2009 Matthew Toseland
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


import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import freemail.l10n.FreemailL10n;
import freemail.ui.web.WebInterface;
import freemail.utils.Logger;
import freemail.wot.OwnIdentity;
import freemail.wot.WoTConnection;
import freemail.wot.WoTConnections;
import freenet.l10n.BaseL10n.LANGUAGE;
import freenet.pluginmanager.FredPlugin;
import freenet.pluginmanager.FredPluginBaseL10n;
import freenet.pluginmanager.FredPluginL10n;
import freenet.pluginmanager.FredPluginRealVersioned;
import freenet.pluginmanager.FredPluginThreadless;
import freenet.pluginmanager.FredPluginVersioned;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.pluginmanager.PluginRespirator;

// although we have threads, we still 'implement' FredPluginThreadless because our runPlugin method
// returns rather than just continuing to run for the lifetime of the plugin.
public class FreemailPlugin extends Freemail implements FredPlugin, FredPluginBaseL10n,
                                                        FredPluginThreadless, FredPluginVersioned,
                                                        FredPluginRealVersioned, FredPluginL10n {
	private final static ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(10, new FreemailThreadFactory());

	private WebInterface webInterface = null;
	private volatile PluginRespirator pluginRespirator = null;
	private WoTConnection wotConnection = null;
	
	public FreemailPlugin() throws IOException {
		super(CFGFILE);
	}
	
	public String getVersion() {
		return Version.getVersionString();
	}
	
	public void runPlugin(PluginRespirator pr) {
		pluginRespirator = pr;

		startFcp(true);
		startWorkers(true);
		startServers(true);
		startIdentityFetch(pr, getAccountManager());

		webInterface = new WebInterface(pr.getToadletContainer(), pr, this);
	}

	public static ScheduledExecutorService getExecutor() {
		return executor;
	}

	private void startIdentityFetch(final PluginRespirator pr, final AccountManager accountManager) {
		pr.getNode().executor.execute(new Runnable() {
			@Override
			public void run() {
				List<OwnIdentity> oids = null;
				while(oids == null) {
					WoTConnection wot = getWotConnection();
					if(wot != null) {
						try {
							oids = wot.getAllOwnIdentities();
						} catch(PluginNotFoundException e) {
							//Try again later
							oids = null;
						}
					}

					if(oids == null) {
						try {
							Thread.sleep(60 * 1000);
						} catch(InterruptedException e) {
							//Just try again
						}
					}
				}

				for(OwnIdentity oid : oids) {
					for(FreemailAccount account : accountManager.getAllAccounts()) {
						if(account.getIdentity().equals(oid.getIdentityID())) {
							account.setNickname(oid.getNickname());
						}
					}
				}
			}
		}, "Freemail OwnIdentity nickname fetcher");
	}

	public long getRealVersion() {
		return Version.BUILD_NO;
	}

	@Override
	public synchronized WoTConnection getWotConnection() {
		if(wotConnection == null) {
			if(pluginRespirator == null) {
				//runPlugin() hasn't been called yet
				return null;
			}

			wotConnection = WoTConnections.wotConnection(pluginRespirator);
		}
		return wotConnection;
	}

	@Override
	public void terminate() {
		Logger.debug(this, "terminate() called");
		executor.shutdownNow();
		
		long start = System.nanoTime();
		webInterface.terminate();
		long end = System.nanoTime();
		Logger.debug(this, "Web interface terminated (in " + (end - start) + "ns), proceeding with normal termination");

		start = System.nanoTime();
		super.terminate();
		end = System.nanoTime();
		Logger.debug(this, "Normal termination took " + (end - start) + "ns");

		start = System.nanoTime();
		try {
			executor.awaitTermination(1, TimeUnit.HOURS);
		} catch(InterruptedException e) {
			Logger.debug(this, "Thread was interrupted while waiting for excutor to terminate.");
		}
		end = System.nanoTime();
		Logger.debug(this, "Spent " + (end - start) + "ns waiting for executor to terminate");
	}

	@Override
	public String getString(String key) {
		return FreemailL10n.getString(key);
	}

	@Override
	public void setLanguage(LANGUAGE newLanguage) {
		FreemailL10n.setLanguage(this, newLanguage);
	}

	@Override
	public String getL10nFilesBasePath() {
		return FreemailL10n.getL10nFilesBasePath();
	}

	@Override
	public String getL10nFilesMask() {
		return FreemailL10n.getL10nFilesMask();
	}

	@Override
	public String getL10nOverrideFilesMask() {
		return FreemailL10n.getL10nOverrideFilesMask();
	}

	@Override
	public ClassLoader getPluginClassLoader() {
		return FreemailPlugin.class.getClassLoader();
	}

	private static class FreemailThreadFactory implements ThreadFactory {
		AtomicInteger threadCount = new AtomicInteger();

		@Override
		public Thread newThread(Runnable runnable) {
			return new Thread(runnable, "Freemail executor thread " + threadCount.getAndIncrement());
		}
	}
}

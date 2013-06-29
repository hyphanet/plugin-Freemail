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

package org.freenetproject.freemail;


import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.freenetproject.freemail.l10n.FreemailL10n;
import org.freenetproject.freemail.ui.web.WebInterface;
import org.freenetproject.freemail.utils.Logger;
import org.freenetproject.freemail.wot.OwnIdentity;
import org.freenetproject.freemail.wot.WoTConnection;
import org.freenetproject.freemail.wot.WoTConnections;

import freenet.l10n.BaseL10n.LANGUAGE;
import freenet.pluginmanager.FredPlugin;
import freenet.pluginmanager.FredPluginBaseL10n;
import freenet.pluginmanager.FredPluginL10n;
import freenet.pluginmanager.FredPluginRealVersioned;
import freenet.pluginmanager.FredPluginThreadless;
import freenet.pluginmanager.FredPluginVersioned;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.Timer;

// although we have threads, we still 'implement' FredPluginThreadless because our runPlugin method
// returns rather than just continuing to run for the lifetime of the plugin.
public class FreemailPlugin extends Freemail implements FredPlugin, FredPluginBaseL10n,
                                                        FredPluginThreadless, FredPluginVersioned,
                                                        FredPluginRealVersioned, FredPluginL10n {
	private static final ScheduledThreadPoolExecutor defaultExecutor =
			new ScheduledThreadPoolExecutor(10, new FreemailThreadFactory("Freemail executor thread"));
	private static final ScheduledThreadPoolExecutor senderExecutor =
			new ScheduledThreadPoolExecutor(10, new FreemailThreadFactory("Freemail sender thread"));

	private WebInterface webInterface = null;
	private volatile PluginRespirator pluginRespirator = null;
	private WoTConnection wotConnection = null;

	public FreemailPlugin() throws IOException {
		super(CFGFILE);

		/*
		 * We want the executor to vary the pool size even if the queue isn't
		 * full since the queue is unbounded. We do this by setting
		 * corePoolSize == maximumPoolSize and allowing core threads to time
		 * out. Allowing all the threads to time out is fine since none of the
		 * tasks are sensitive to the additional thread creation delay.
		 *
		 * Note: if there are queued tasks at least 1 thread will be alive, but
		 * unfortunately the timeout still applies to this thread so every time
		 * the timeout expires the executor creates a new thread. Because of
		 * this the timeout for the sender executor should be large to avoid
		 * creating a large amount of threads, and for the default it should be
		 * > Channel.TASK_RETRY_DELAY (since that makes the thread that runs
		 * the Fetcher never time out).
		 */
		defaultExecutor.setKeepAliveTime(10, TimeUnit.MINUTES);
		defaultExecutor.allowCoreThreadTimeOut(true);
		senderExecutor.setKeepAliveTime(1, TimeUnit.HOURS);
		senderExecutor.allowCoreThreadTimeOut(true);
	}

	@Override
	public String getVersion() {
		return Version.getVersionString();
	}

	@Override
	public void runPlugin(PluginRespirator pr) {
		Timer runTime = Timer.start();

		pluginRespirator = pr;

		updateFileFormat();

		startFcp();

		Timer workers = runTime.startSubTimer();
		startWorkers();
		workers.log(this, 1, TimeUnit.SECONDS, "Time spent starting workers");

		startServers(true);
		startIdentityFetch(pr, getAccountManager());

		webInterface = new WebInterface(pr.getToadletContainer(), pr, this);

		runTime.log(this, 1, TimeUnit.SECONDS, "Time spent in runPlugin()");
	}

	public static ScheduledExecutorService getExecutor(TaskType type) {
		switch (type) {
		case UNSPECIFIED:
			return defaultExecutor;
		case SENDER:
			return senderExecutor;
		default:
			throw new AssertionError("Missing case " + type);
		}
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

	@Override
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
		Timer terminateTimer = Timer.start();

		Logger.minor(this, "terminate() called");
		defaultExecutor.shutdownNow();
		senderExecutor.shutdownNow();

		Timer webUITermination = terminateTimer.startSubTimer();
		webInterface.terminate();
		webUITermination.log(this, 1, TimeUnit.SECONDS, "Time spent terminating web interface");

		Timer normalTermination = terminateTimer.startSubTimer();
		super.terminate();
		normalTermination.log(this, 1, TimeUnit.SECONDS, "Time spent in normal termination");

		Timer executorTermination = terminateTimer.startSubTimer();
		try {
			defaultExecutor.awaitTermination(1, TimeUnit.HOURS);
			senderExecutor.awaitTermination(1, TimeUnit.HOURS);
		} catch(InterruptedException e) {
			Logger.minor(this, "Thread was interrupted while waiting for excutors to terminate.");
		}
		executorTermination.log(this, 1, TimeUnit.SECONDS, "Time spent waiting for executor termination");

		terminateTimer.log(this, 1, TimeUnit.SECONDS, "Time spent terminating plugin");
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
		private final String prefix;
		AtomicInteger threadCount = new AtomicInteger();

		public FreemailThreadFactory(String prefix) {
			this.prefix = prefix;
		}

		@Override
		public Thread newThread(Runnable runnable) {
			String name = prefix + " " + threadCount.getAndIncrement();
			Logger.debug(this, "Creating new thread: " + name);
			return new Thread(runnable, name);
		}
	}

	public static enum TaskType {
		UNSPECIFIED,
		SENDER
	}
}

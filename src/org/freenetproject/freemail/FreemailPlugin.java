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
import java.util.Map;
import java.util.concurrent.*;

import org.freenetproject.freemail.l10n.FreemailL10n;
import org.freenetproject.freemail.ui.web.WebInterface;
import org.freenetproject.freemail.utils.Logger;
import org.freenetproject.freemail.utils.Timer;
import org.freenetproject.freemail.wot.WoTConnections;
import org.freenetproject.freemail.wot.OwnIdentity;
import org.freenetproject.freemail.wot.WoTConnection;

import freenet.l10n.BaseL10n.LANGUAGE;
import freenet.pluginmanager.FredPlugin;
import freenet.pluginmanager.FredPluginBaseL10n;
import freenet.pluginmanager.FredPluginL10n;
import freenet.pluginmanager.FredPluginRealVersioned;
import freenet.pluginmanager.FredPluginThreadless;
import freenet.pluginmanager.FredPluginVersioned;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.pluginmanager.PluginRespirator;
import org.freenetproject.freemail.wot.WoTException;

// although we have threads, we still 'implement' FredPluginThreadless because our runPlugin method
// returns rather than just continuing to run for the lifetime of the plugin.
public class FreemailPlugin extends Freemail implements FredPlugin, FredPluginBaseL10n,
                                                        FredPluginThreadless, FredPluginVersioned,
                                                        FredPluginRealVersioned, FredPluginL10n {
	private WebInterface webInterface = null;
	private volatile PluginRespirator pluginRespirator = null;
	private WoTConnection wotConnection = null;
	private Map<CountDownLatch, Thread> activeThreads = new ConcurrentHashMap<>();

	public FreemailPlugin() throws IOException {
		super(CFGFILE);
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

		Freemail.setRNG(pr.getNode().secureRandom);
		startServers(true);
		startIdentityFetch(pr, getAccountManager());

		webInterface = new WebInterface(pr.getToadletContainer(), pr, this, configurator);
		webInterface.registerToadlets();

		runTime.log(this, 1, TimeUnit.SECONDS, "Time spent in runPlugin()");
	}

	private void startIdentityFetch(final PluginRespirator pr, final AccountManager accountManager) {
		final CountDownLatch countDownLatch = new CountDownLatch(1);
		activeThreads.put(countDownLatch, Thread.currentThread());

		pr.getNode().executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					activeThreads.put(countDownLatch, Thread.currentThread());
					List<OwnIdentity> oids = null;
					while (oids == null) {
						if (Thread.currentThread().isInterrupted()) {
							return;
						}

						WoTConnection wot = getWotConnection();
						if (wot != null) {
							try {
								oids = wot.getAllOwnIdentities();
							} catch (PluginNotFoundException ignored) { // Try again later
							} catch (TimeoutException | IOException | WoTException e) { // Try again later
								Logger.debug(this, e.getMessage());
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
								return;
							}
						}

						if (oids == null) {
							try {
								Thread.sleep(60_000);
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
								return;
							}
						}
					}

					for (OwnIdentity oid : oids) {
						for (FreemailAccount account : accountManager.getAllAccounts()) {
							if (account.getIdentity().equals(oid.getIdentityID())) {
								account.setNickname(oid.getNickname());
							}
						}
					}
				} finally {
					countDownLatch.countDown();
					activeThreads.remove(countDownLatch);
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

			wotConnection = new WoTConnections(pluginRespirator);
		}
		return wotConnection;
	}

	@Override
	public void terminate() {
		Timer terminateTimer = Timer.start();
		Logger.minor(this, "terminate() called");

		Timer waitingForNormalShutdown = terminateTimer.startSubTimer();
		long timeout = TimeUnit.MINUTES.toMillis(1);
		while (!activeThreads.isEmpty()) {
			if (timeout <= 0) {
				break;
			}

			for (Map.Entry<CountDownLatch, Thread> countDownLatchThreadEntry : activeThreads.entrySet()) {
				if (timeout <= 0) {
					break;
				}

				long startTime = System.currentTimeMillis();
				try {
					if (!Thread.currentThread().equals(countDownLatchThreadEntry.getValue())) {
						countDownLatchThreadEntry.getValue().interrupt();
						countDownLatchThreadEntry.getKey().await(timeout, TimeUnit.MILLISECONDS);
					}
				} catch (InterruptedException ignored) {
				} finally {
					timeout -= System.currentTimeMillis() - startTime;
				}
			}
		}
		waitingForNormalShutdown.log(this, 1, TimeUnit.SECONDS, "Time spent waiting for normal shutdown");

		Timer webUITermination = terminateTimer.startSubTimer();
		webInterface.terminate();
		webUITermination.log(this, 1, TimeUnit.SECONDS, "Time spent terminating web interface");

		Timer normalTermination = terminateTimer.startSubTimer();
		super.terminate();
		normalTermination.log(this, 1, TimeUnit.SECONDS, "Time spent in normal termination");

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
}

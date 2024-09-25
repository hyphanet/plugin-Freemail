/*
 * Freemail.java
 * This file is part of Freemail
 * Copyright (C) 2006,2007,2008 Dave Baker
 * Copyright (C) 2007,2009 Matthew Toseland
 * Copyright (C) 2008 Alexander Lehmann
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
import java.security.SecureRandom;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.freenetproject.freemail.config.ConfigClient;
import org.freenetproject.freemail.config.Configurator;
import org.freenetproject.freemail.fcp.FCPConnection;
import org.freenetproject.freemail.fcp.FCPContext;
import org.freenetproject.freemail.imap.IMAPListener;
import org.freenetproject.freemail.smtp.SMTPListener;
import org.freenetproject.freemail.utils.Logger;
import org.freenetproject.freemail.utils.Timer;
import org.freenetproject.freemail.wot.WoTConnection;


public abstract class Freemail implements ConfigClient {
	private static final ScheduledThreadPoolExecutor defaultExecutor =
			new ScheduledThreadPoolExecutor(10, new FreemailThreadFactory("Freemail executor thread"));
	private static final ScheduledThreadPoolExecutor senderExecutor =
			new ScheduledThreadPoolExecutor(10, new FreemailThreadFactory("Freemail sender thread"));

	private static final String BASEDIR = "freemail-wot";
	private static final String TEMPDIRNAME = BASEDIR + "/temp";
	protected static final String DEFAULT_DATADIR = BASEDIR + "/data";
	protected static final String CFGFILE = BASEDIR + "/globalconfig";
	private static final long LATEST_FILE_FORMAT = 1;

	private File datadir;
	private static File tempdir;
	protected static FCPConnection fcpconn = null;

	private Thread fcpThread;
	private Thread smtpThread;
	private Thread imapThread;

	private final AccountManager accountManager;
	private final SMTPListener smtpl;
	private final IMAPListener imapl;

	protected final Configurator configurator;

	private static SecureRandom srng;

	protected Freemail(String cfgfile) throws IOException {
		configurator = new Configurator(new File(cfgfile));

		Logger.registerConfig(configurator);

		configurator.register(Configurator.DATA_DIR, this, Freemail.DEFAULT_DATADIR);
		if(!datadir.exists() && !datadir.mkdirs()) {
			Logger.error(this, "Freemail: Couldn't create data directory. Please ensure that the user you are running Freemail as has write access to its working directory");
			throw new IOException("Couldn't create data dir");
		}

		configurator.register(Configurator.TEMP_DIR, this, Freemail.TEMPDIRNAME);
		if(!tempdir.exists() && !tempdir.mkdirs()) {
			Logger.error(this, "Freemail: Couldn't create temporary directory. Please ensure that the user you are running Freemail as has write access to its working directory");
			throw new IOException("Couldn't create data dir");
		}

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

		FCPContext fcpctx = new FCPContext();
		configurator.register(Configurator.FCP_HOST, fcpctx, "localhost");
		configurator.register(Configurator.FCP_PORT, fcpctx, "9481");

		Freemail.fcpconn = new FCPConnection(fcpctx);

		accountManager = new AccountManager(datadir, this);

		imapl = new IMAPListener(accountManager, configurator);
		smtpl = new SMTPListener(accountManager, configurator, this);
	}

	public WoTConnection getWotConnection() {
		return null;
	}

	public static File getTempDir() {
		return Freemail.tempdir;
	}

	public static FCPConnection getFCPConnection() {
		return Freemail.fcpconn;
	}

	public AccountManager getAccountManager() {
		return accountManager;
	}

	@Override
	public void setConfigProp(String key, String val) {
		if(key.equalsIgnoreCase(Configurator.DATA_DIR)) {
			datadir = new File(val);
		} else if(key.equalsIgnoreCase(Configurator.TEMP_DIR)) {
			tempdir = new File(val);
		}
	}

	protected void startFcp() {
		fcpThread = new Thread(fcpconn, "Freemail FCP Connection");
		fcpThread.setDaemon(true);
		fcpThread.start();
	}

	/** Set once on startup */
	public static synchronized void setRNG(SecureRandom random) {
		srng = random;
	}

	public static synchronized SecureRandom getRNG() {
		if(srng == null) throw new NullPointerException();
		return srng;
	}

	// note that this relies on sender being initialized
	// (so startWorkers has to be called before)
	protected void startServers(boolean daemon) {
		// start the SMTP Listener

		smtpThread = new Thread(smtpl, "Freemail SMTP Listener");
		smtpThread.setDaemon(daemon);
		smtpThread.start();

		// start the IMAP listener
		imapThread = new Thread(imapl, "Freemail IMAP Listener");
		imapThread.setDaemon(daemon);
		imapThread.start();
	}

	protected void startWorkers() {
		//Start account watchers, channel tasks etc.
		accountManager.startTasks();
	}

	/**
	 * Updates the on-disk file format to the newest version if needed.
	 */
	protected void updateFileFormat() {
		long currentVersion;
		try {
			String rawVersion = configurator.get(Configurator.FILE_FORMAT);
			currentVersion = Long.parseLong(rawVersion);
		} catch(NumberFormatException e) {
			//Version is missing, so assume we just create a new config
			configurator.set(Configurator.FILE_FORMAT, Long.toString(LATEST_FILE_FORMAT));
			return;
		}

		if(currentVersion != LATEST_FILE_FORMAT) {
			Logger.error(this, "Read unknown file format from config: " + currentVersion);
		}
	}

	public void terminate() {
		Timer terminateTimer = Timer.start();

		defaultExecutor.shutdownNow();
		senderExecutor.shutdownNow();

		Timer accountManagerTermination = terminateTimer.startSubTimer();
		accountManager.terminate();
		accountManagerTermination.log(this, 1, TimeUnit.SECONDS, "Time spent killing account manager");

		Timer threadTermination = terminateTimer.startSubTimer();
		smtpl.kill();
		imapl.kill();
		// now kill the FCP thread - that's what all the other threads will be waiting on
		fcpconn.kill();
		threadTermination.log(this, 1, TimeUnit.SECONDS, "Time spent killing other threads");

		// now clean up all the threads
		try {
			Timer smtpThreadJoin = terminateTimer.startSubTimer();
			if(smtpThread != null) {
				smtpThread.join();
				smtpl.joinClientThreads();
				smtpThread = null;
			}
			smtpThreadJoin.log(this, 1, TimeUnit.SECONDS, "Time spent joining SMTP thread");

			Timer imapThreadJoin = terminateTimer.startSubTimer();
			if(imapThread != null) {
				imapThread.join();
				imapl.joinClientThreads();
				imapThread = null;
			}
			imapThreadJoin.log(this, 1, TimeUnit.SECONDS, "Time spent joining IMAP thread");

			Timer fcpThreadJoin = terminateTimer.startSubTimer();
			if(fcpThread != null) {
				fcpThread.join();
				fcpThread = null;
			}
			fcpThreadJoin.log(this, 1, TimeUnit.SECONDS, "Time spent joining FCP thread");
		} catch (InterruptedException ie) {

		}

		Timer executorTermination = terminateTimer.startSubTimer();
		try {
			defaultExecutor.awaitTermination(1, TimeUnit.HOURS);
			senderExecutor.awaitTermination(1, TimeUnit.HOURS);
		} catch(InterruptedException e) {
			Logger.minor(this, "Thread was interrupted while waiting for excutors to terminate.");
		}
		executorTermination.log(this, 1, TimeUnit.SECONDS, "Time spent waiting for executor termination");

		terminateTimer.log(this, 1, TimeUnit.SECONDS, "Time spent in Freemail.terminate()");
	}

	public ScheduledExecutorService getExecutor(TaskType type) {
		switch (type) {
		case UNSPECIFIED:
			return defaultExecutor;
		case SENDER:
			return senderExecutor;
		default:
			throw new AssertionError("Missing case " + type);
		}
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



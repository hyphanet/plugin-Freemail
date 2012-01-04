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

package freemail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import freemail.fcp.FCPConnection;
import freemail.fcp.FCPContext;
import freemail.imap.IMAPListener;
import freemail.smtp.SMTPListener;
import freemail.utils.Logger;
import freemail.config.ConfigClient;
import freemail.config.Configurator;

public abstract class Freemail implements ConfigClient {
	private static final String TEMPDIRNAME = "temp";
	protected static final String DEFAULT_DATADIR = "data";
	private static final String GLOBALDATADIR = "globaldata";
	private static final String ACKDIR = "delayedacks";
	protected static final String CFGFILE = "globalconfig";
	private File datadir;
	private static File globaldatadir;
	private static File tempdir;
	protected static FCPConnection fcpconn = null;
	
	private Thread fcpThread;
	private ArrayList /* of Thread */ singleAccountWatcherThreadList = new ArrayList();
	private Thread messageSenderThread;
	private Thread smtpThread;
	private Thread ackInserterThread;
	private Thread imapThread;
	
	private final AccountManager accountManager;
	private final ArrayList singleAccountWatcherList = new ArrayList();
	private final MessageSender sender;
	private final SMTPListener smtpl;
	private final AckProcrastinator ackinserter;
	private final IMAPListener imapl;
	
	protected final Configurator configurator;
	
	protected Freemail(String cfgfile) throws IOException {
		configurator = new Configurator(new File(cfgfile));
		
		configurator.register("loglevel", new Logger(), "normal|error");
		
		configurator.register("datadir", this, Freemail.DEFAULT_DATADIR);
		if (!datadir.exists() && !datadir.mkdirs()) {
			Logger.error(this,"Freemail: Couldn't create data directory. Please ensure that the user you are running Freemail as has write access to its working directory");
			throw new IOException("Couldn't create data dir");
		}
		
		configurator.register("globaldatadir", this, GLOBALDATADIR);
		if (!globaldatadir.exists() && !globaldatadir.mkdirs()) {
			Logger.error(this,"Freemail: Couldn't create global data directory. Please ensure that the user you are running Freemail as has write access to its working directory");
			throw new IOException("Couldn't create data dir");
		}
		
		configurator.register("tempdir", this, Freemail.TEMPDIRNAME);
		if (!tempdir.exists() && !tempdir.mkdirs()) {
			Logger.error(this,"Freemail: Couldn't create temporary directory. Please ensure that the user you are running Freemail as has write access to its working directory");
			throw new IOException("Couldn't create data dir");
		}
		
		FCPContext fcpctx = new FCPContext();
		configurator.register("fcp_host", fcpctx, "localhost");
		configurator.register("fcp_port", fcpctx, "9481");
		
		Freemail.fcpconn = new FCPConnection(fcpctx);
		
		accountManager = new AccountManager(datadir);
		
		sender = new MessageSender(accountManager);
		
		File ackdir = new File(globaldatadir, ACKDIR);
		AckProcrastinator.setAckDir(ackdir);
		ackinserter = new AckProcrastinator();
		
		
		imapl = new IMAPListener(accountManager, configurator);
		smtpl = new SMTPListener(accountManager, sender, configurator);
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

	public void setConfigProp(String key, String val) {
		if (key.equalsIgnoreCase("datadir")) {
			datadir = new File(val);
		} else if (key.equalsIgnoreCase("tempdir")) {
			tempdir = new File(val);
		} else if (key.equalsIgnoreCase("globaldatadir")) {
			globaldatadir = new File(val);
		}
	}
	
	protected void startFcp(boolean daemon) {
		fcpThread = new Thread(fcpconn, "Freemail FCP Connection");
		fcpThread.setDaemon(true);
		fcpThread.start();
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
	
	protected void startWorker(FreemailAccount account, boolean daemon) {
		SingleAccountWatcher saw = new SingleAccountWatcher(account); 
		singleAccountWatcherList.add(saw);
		Thread t = new Thread(saw, "Freemail Account Watcher for "+account.getUsername());
		t.setDaemon(daemon);
		t.start();
		singleAccountWatcherThreadList.add(t);
	}
	
	protected void startWorkers(boolean daemon) {
		System.out.println("This is Freemail version "+Version.getVersionString());
		System.out.println("Freemail is released under the terms of the GNU General Public License. Freemail is provided WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. For details, see the LICENSE file included with this distribution.");
		System.out.println("");
		
		// start a SingleAccountWatcher for each account
		Iterator i = accountManager.getAllAccounts().iterator();
		while (i.hasNext()) {
			FreemailAccount acc = (FreemailAccount)i.next();
			
			startWorker(acc, daemon);
		}
		
		// start the sender thread
		messageSenderThread = new Thread(sender, "Freemail Message sender");
		messageSenderThread.setDaemon(daemon);
		messageSenderThread.start();
		
		// start the delayed ACK inserter
		ackInserterThread = new Thread(ackinserter, "Freemail Delayed ACK Inserter");
		ackInserterThread.setDaemon(daemon);
		ackInserterThread.start();
	}
	
	public void terminate() {
		long start = System.nanoTime();
		Iterator it = singleAccountWatcherList.iterator();
		while(it.hasNext()) {
			((SingleAccountWatcher)it.next()).kill();
			it.remove();
		}
		long end = System.nanoTime();
		Logger.debug(this, "Spent " + (end - start) + "ns killing SingleAccountWatchers");

		start = System.nanoTime();
		sender.kill();
		ackinserter.kill();
		smtpl.kill();
		imapl.kill();
		// now kill the FCP thread - that's what all the other threads will be waiting on
		fcpconn.kill();
		end = System.nanoTime();
		Logger.debug(this, "Spent " + (end - start) + "ns killing other threads");
		
		// now clean up all the threads
		boolean cleanedUp = false;
		while (!cleanedUp) {
			try {
				start = System.nanoTime();
				it = singleAccountWatcherThreadList.iterator();
				while(it.hasNext()) {
					((Thread)it.next()).join();
					it.remove();
				}
				end = System.nanoTime();
				Logger.debug(this, "Spent " + (end - start) + "ns joining SingleAccountWatchers");
				
				start = System.nanoTime();
				if (messageSenderThread != null) {
					messageSenderThread.join();
					messageSenderThread = null;
				}
				if (ackInserterThread != null) {
					ackInserterThread.join();
					ackInserterThread = null;
				}
				if (smtpThread != null) {
					smtpThread.join();
					smtpl.joinClientThreads();
					smtpThread = null;
				}
				if (imapThread != null) {
					imapThread.join();
					imapl.joinClientThreads();
					imapThread = null;
				}
				if (fcpThread != null) {
					fcpThread.join();
					fcpThread = null;
				}
				end = System.nanoTime();
				Logger.debug(this, "Spent " + (end - start) + "ns joining other threads");
			} catch (InterruptedException ie) {
				
			}
			cleanedUp = true;
		}
	}
}



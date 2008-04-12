/*
 * Freemail.java
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
	public static final int VER_MAJOR = 0;
	public static final int VER_MINOR = 1;
	public static final int BUILD_NO = 9;
	public static final String VERSION_TAG = "Pet Shop";

	private static final String TEMPDIRNAME = "temp";
	protected static final String DATADIR = "data";
	private static final String GLOBALDATADIR = "globaldata";
	private static final String ACKDIR = "delayedacks";
	protected static final String CFGFILE = "globalconfig";
	private static File datadir;
	private static File globaldatadir;
	private static File tempdir;
	protected static FCPConnection fcpconn = null;
	
	private Thread fcpThread;
	private ArrayList /* of Thread */ singleAccountWatcherThreadList = new ArrayList();
	private Thread messageSenderThread;
	private Thread smtpThread;
	private Thread ackInserterThread;
	private Thread imapThread;
	
	private ArrayList singleAccountWatcherList = new ArrayList();
	private MessageSender sender;
	private SMTPListener smtpl;
	private AckProcrastinator ackinserter;
	private IMAPListener imapl;
	
	protected final Configurator configurator;
	
	protected Freemail(String cfgfile) {
		configurator = new Configurator(new File(cfgfile));
		
		configurator.register("loglevel", new Logger(), "normal|error");
		
		configurator.register("datadir", this, Freemail.DATADIR);
		if (!getDataDir().exists()) {
			if (!getDataDir().mkdir()) {
				Logger.error(this,"Freemail: Couldn't create data directory. Please ensure that the user you are running Freemail as has write access to its working directory");
				return;
			}
		}
		
		configurator.register("globaldatadir", this, GLOBALDATADIR);
		if (!getGlobalDataDir().exists()) {
			if (!getGlobalDataDir().mkdir()) {
				Logger.error(this,"Freemail: Couldn't create global data directory. Please ensure that the user you are running Freemail as has write access to its working directory");
			}
		}
		
		configurator.register("tempdir", this, Freemail.TEMPDIRNAME);
		if (!getTempDir().exists()) {
			if (!Freemail.getTempDir().mkdir()) {
				Logger.error(this,"Freemail: Couldn't create temporary directory. Please ensure that the user you are running Freemail as has write access to its working directory");
				return;
			}
		}
	}
	
	public static File getTempDir() {
		return Freemail.tempdir;
	}
	
	protected static File getGlobalDataDir() {
		return globaldatadir;
	}
	
	public static File getDataDir() {
		return datadir;
	}
	
	public static FCPConnection getFCPConnection() {
		return Freemail.fcpconn;
	}

	public void setConfigProp(String key, String val) {
		if (key.equalsIgnoreCase("datadir")) {
			Freemail.datadir = new File(val);
		} else if (key.equalsIgnoreCase("tempdir")) {
			Freemail.tempdir = new File(val);
		} else if (key.equalsIgnoreCase("globaldatadir")) {
			Freemail.globaldatadir = new File(val);
		}
	}
	
	protected void startFcp(boolean daemon) {
		FCPContext fcpctx = new FCPContext();
		configurator.register("fcp_host", fcpctx, "localhost");
		configurator.register("fcp_port", fcpctx, "9481");
		
		Freemail.fcpconn = new FCPConnection(fcpctx);
		fcpThread = new Thread(fcpconn, "Freemail FCP Connection");
		fcpThread.setDaemon(true);
		fcpThread.start();
	}
	
	// note that this relies on sender being initialized
	// (so startWorkers has to be called before)
	protected void startServers(boolean daemon) {
		// start the SMTP Listener
		smtpl = new SMTPListener(sender, configurator);
		smtpThread = new Thread(smtpl, "Freemail SMTP Listener");
		smtpThread.setDaemon(daemon);
		smtpThread.start();
		
		// start the IMAP listener
		imapl = new IMAPListener(configurator);
		imapThread = new Thread(imapl, "Freemail IMAP Listener");
		imapThread.setDaemon(daemon);
		imapThread.start();
	}
	
	protected void startWorkers(boolean daemon) {
		System.out.println("This is Freemail version "+VER_MAJOR+"."+VER_MINOR+" build #"+BUILD_NO+" ("+VERSION_TAG+")");
		System.out.println("Freemail is released under the terms of the GNU Lesser General Public License. Freemail is provided WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. For details, see the LICENSE file included with this distribution.");
		System.out.println("");
		
		// start a SingleAccountWatcher for each account
		File[] files = getDataDir().listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().equals(".") || files[i].getName().equals(".."))
				continue;
			if (!files[i].isDirectory()) continue;

			String invalid=AccountManager.validateUsername(files[i].getName());
			if(!invalid.equals("")) {
				Logger.error(this,"Account name "+files[i].getName()+" contains invalid chars (\""+invalid+"\"), you may get problems accessing the account.");
			}
			
			SingleAccountWatcher saw = new SingleAccountWatcher(files[i]); 
			singleAccountWatcherList.add(saw);
			Thread t = new Thread(saw, "Freemail Account Watcher for "+files[i].getName());
			t.setDaemon(daemon);
			t.start();
			singleAccountWatcherThreadList.add(t);
		}
		
		// and a sender thread
		sender = new MessageSender(getDataDir());
		messageSenderThread = new Thread(sender, "Freemail Message sender");
		messageSenderThread.setDaemon(daemon);
		messageSenderThread.start();
		
		// start the delayed ACK inserter
		File ackdir = new File(getGlobalDataDir(), ACKDIR);
		AckProcrastinator.setAckDir(ackdir);
		ackinserter = new AckProcrastinator();
		ackInserterThread = new Thread(ackinserter, "Freemail Delayed ACK Inserter");
		ackInserterThread.setDaemon(daemon);
		ackInserterThread.start();
	}
	
	public void terminate() {
		Iterator it = singleAccountWatcherList.iterator();
		while(it.hasNext()) {
			((SingleAccountWatcher)it.next()).kill();
			it.remove();
		}

		sender.kill();
		ackinserter.kill();
		smtpl.kill();
		imapl.kill();
		// now kill the FCP thread - that's what all the other threads will be waiting on
		fcpconn.kill();
		
		// now clean up all the threads
		boolean cleanedUp = false;
		while (!cleanedUp) {
			try {
				it = singleAccountWatcherThreadList.iterator();
				while(it.hasNext()) {
					((Thread)it.next()).join();
					it.remove();
				}
				
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
			} catch (InterruptedException ie) {
				
			}
			cleanedUp = true;
		}
	}
}

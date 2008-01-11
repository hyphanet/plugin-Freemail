/*
 * FreemailCli.java
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
import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Iterator;

import freemail.Freemail;
import freemail.fcp.FCPContext;
import freemail.fcp.FCPConnection;
import freemail.imap.IMAPListener;
import freemail.smtp.SMTPListener;
import freemail.config.Configurator;
//import freemail.config.ConfigClient;

public abstract class FreemailCli extends Freemail {
	public static void main(String[] args) {
		String cfgfile = CFGFILE;
		
		String action = "";
		String account = null;
		String newpasswd = null;
		String alias = null;
		
		System.out.println("This is Freemail version "+VER_MAJOR+"."+VER_MINOR+" build #"+BUILD_NO+" ("+VERSION_TAG+")");
		System.out.println("Freemail is released under the terms of the GNU Lesser General Public License. Freemail is provided WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. For details, see the LICENSE file included with this distribution.");
		System.out.println("");
		
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--newaccount")) {
				action = args[i];
				i++;
				if (args.length - 1 < i) {
					System.out.println("Usage: --newaccount <account name>");
					return;
				}
				
				account = args[i];
			} else if (args[i].equals("--passwd") || args[i].equals("--password")) {
				action = "--passwd";
				i = i + 2;
				if (args.length - 1 < i) {
					System.out.println("Usage: --passwd <account name> <password>");
					return;
				}
				account = args[i - 1];
				newpasswd = args[i];
			} else if (args[i].equals("--shortaddress")) {
				action = args[i];
				i = i + 2;
				if (args.length - 1 < i) {
					System.out.println("Usage: --shortaddress <name> <domain prefix>");
					return;
				}
				account = args[i - 1];
				alias = args[i];
			} else if (args[i].equals("-c")) {
				i++;
				if (args.length - 1 < i) {
					System.out.println("No config file supplied, using default");
					continue;
				}
				cfgfile = args[i];
			} else {
				System.out.println("Unknown option: '"+args[i]+"'");
				return;
			}
		}
		
		Configurator cfg = new Configurator(new File(cfgfile));
		
		FCPContext fcpctx = new FCPContext();
		
		cfg.register("fcp_host", fcpctx, "localhost");
		cfg.register("fcp_port", fcpctx, "9481");
		
		Freemail.fcpconn = new FCPConnection(fcpctx);
		Thread fcpthread  = new Thread(fcpconn);
		fcpthread.setDaemon(true);
		fcpthread.start();
		
		if (action.equals("--newaccount")) {
			try {
				AccountManager.Create(account);
				// by default, we'll not setup NIM now real mode works
				//AccountManager.setupNIM(account);
				System.out.println("Account created for "+account+". You may now set a password with --passwd <username> <password>");
				//System.out.println("For the time being, you address is "+account+"@nim.freemail");
			} catch (IOException ioe) {
				System.out.println("Couldn't create account. Please check write access to Freemail's working directory. If you want to overwrite your account, delete the appropriate directory manually in 'data' first. Freemail will intentionally not overwrite it. Error: "+ioe.getMessage());
			} catch (IllegalArgumentException iae) {
				System.out.println("Couldn't create account. Error: "+iae.getMessage());
			}
			return;
		} else if (action.equals("--passwd")) {
			try {
				AccountManager.ChangePassword(account, newpasswd);
				System.out.println("Password changed.");
			} catch (Exception e) {
				System.out.println("Couldn't change password for "+account+". "+e.getMessage());
				e.printStackTrace();
			}
			return;
		} else if (action.equals("--shortaddress")) {
			try {
				AccountManager.addShortAddress(account, alias);
			} catch (IllegalArgumentException iae) {
				System.out.println("Couldn't add short address for "+account+". Error: "+iae.getMessage());
				return;
			} catch (Exception e) {
				System.out.println("Couldn't add short address for "+account+". "+e.getMessage());
				e.printStackTrace();
				return;
			}
			System.out.println("You now have all Freemail addresses ending: '@"+alias+".freemail'. Your long address will continue to work.");
			return;
		}
		
		cfg.register("globaldatadir", new Freemail(), GLOBALDATADIR);
		if (!getGlobalDataDir().exists()) {
			getGlobalDataDir().mkdir();
		}
		
		// start a SingleAccountWatcher for each account
		cfg.register("datadir", new Freemail(), Freemail.DATADIR);
		if (!getDataDir().exists()) {
			System.out.println("Starting Freemail for the first time.");
			System.out.println("You will probably want to add an account by running Freemail with arguments --newaccount <username>");
			if (!getDataDir().mkdir()) {
				System.out.println("Couldn't create data directory. Please ensure that the user you are running Freemail as has write access to its working directory");
				System.exit(1);
			}
		}
		cfg.register("tempdir", new Freemail(), Freemail.TEMPDIRNAME);
		if (!getTempDir().exists()) {
			if (!getTempDir().mkdir()) {
				System.out.println("Couldn't create temporary directory. Please ensure that the user you are running Freemail as has write access to its working directory");
				System.exit(1);
			}
		}
		
		File[] files = getDataDir().listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().equals(".") || files[i].getName().equals(".."))
				continue;
			if (!files[i].isDirectory()) continue;
			
			if(!AccountManager.validateUsername(files[i].getName()).isEmpty()) {
				System.out.println("Account name "+files[i].getName()+" contains invalid chars, you may get problems accessing the account.");
			}
			
			Thread t = new Thread(new SingleAccountWatcher(files[i]), "Account Watcher for "+files[i].getName());
			t.setDaemon(true);
			t.start();
		}
		
		// and a sender thread
		MessageSender sender = new MessageSender(getDataDir());
		Thread senderthread = new Thread(sender, "Message sender");
		senderthread.setDaemon(true);
		senderthread.start();
		
		// start the SMTP Listener
		SMTPListener smtpl = new SMTPListener(sender, cfg);
		Thread smtpthread = new Thread(smtpl, "SMTP Listener");
		smtpthread.setDaemon(true);
		smtpthread.start();
		
		// start the delayed ACK inserter
		File ackdir = new File(getGlobalDataDir(), ACKDIR);
		AckProcrastinator.setAckDir(ackdir);
		AckProcrastinator ackinserter = new AckProcrastinator();
		Thread ackinsthread = new Thread(ackinserter, "Delayed ACK Inserter");
		ackinsthread.setDaemon(true);
		ackinsthread.start();
		
		
		// start the IMAP listener
		IMAPListener imapl = new IMAPListener(cfg);
		imapl.run();
	}
}

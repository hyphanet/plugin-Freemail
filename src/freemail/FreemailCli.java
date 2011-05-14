/*
 * FreemailCli.java
 * This file is part of Freemail
 * Copyright (C) 2006,2007,2008 Dave Baker
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

import java.io.IOException;

import freemail.Freemail;
import freemail.utils.Logger;

public class FreemailCli extends Freemail {
	public FreemailCli(String cfgfile) throws IOException {
		super(cfgfile);
	}
	
	public static void main(String[] args) {
		String action = "";
		String username = null;
		String newpasswd = null;
		String alias = null;
		String cfgfile = CFGFILE;

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--newaccount")) {
				action = args[i];
				i++;
				if (args.length - 1 < i) {
					System.out.println("Usage: --newaccount <account name>");
					return;
				}
				
				username = args[i];
			} else if (args[i].equals("--passwd") || args[i].equals("--password")) {
				action = "--passwd";
				i = i + 2;
				if (args.length - 1 < i) {
					System.out.println("Usage: --passwd <account name> <password>");
					return;
				}
				username = args[i - 1];
				newpasswd = args[i];
			} else if (args[i].equals("--shortaddress")) {
				action = args[i];
				i = i + 2;
				if (args.length - 1 < i) {
					System.out.println("Usage: --shortaddress <name> <domain prefix>");
					return;
				}
				username = args[i - 1];
				alias = args[i];
			} else if (args[i].equals("-c")) {
				i++;
				if (args.length - 1 < i) {
					System.out.println("No config file supplied, using default");
					continue;
				}
				cfgfile = args[i];
			} else if (args[i].equals("--help") || args[i].equals("-help") || args[i].equals("--h")) {
				System.out.println("Usage:");
				System.out.println(" java -jar Freemail.jar [-c config]");
				System.out.println("  Starts the Freemail daemon with config file 'config'");
				System.out.println(" java -jar Freemail.jar [-c config] --newaccount <account name>");
				System.out.println("  Creates an account");
				System.out.println(" java -jar Freemail.jar [-c config] --passwd <account name> <password>");
				System.out.println("  Changes the password for the given account");
				System.out.println(" java -jar Freemail.jar [-c config] --shortaddress <name> <domain prefix>");
				System.out.println("  Adds a short address or changes the short address for the given account.");
				return;
			} else {
				System.out.println("Unknown option: '"+args[i]+"'");
				return;
			}
		}
		
		
		FreemailCli freemail;
		try {
			freemail = new FreemailCli(cfgfile);
		} catch(IOException ioe) {
			Logger.error(FreemailCli.class, "Failed to start Freemail: "+ioe.getMessage());
			return;
		}
		freemail.startFcp(false);
		
		if (action.equals("--newaccount")) {
			try {
				freemail.getAccountManager().createAccount(username);
				// by default, we'll not setup NIM now real mode works
				//AccountManager.setupNIM(account);
				System.out.println("Account created for "+username+". You may now set a password with --passwd <username> <password>");
				//System.out.println("For the time being, you address is "+account+"@nim.freemail");
			} catch (IOException ioe) {
				System.out.println("Couldn't create account. Please check write access to Freemail's working directory. If you want to overwrite your account, delete the appropriate directory manually in 'data' first. Freemail will intentionally not overwrite it. Error: "+ioe.getMessage());
			} catch (IllegalArgumentException iae) {
				System.out.println("Couldn't create account. Error: "+iae.getMessage());
			}
			return;
		} else if (action.equals("--passwd")) {
			try {
				FreemailAccount account = freemail.getAccountManager().getAccount(username);
				AccountManager.changePassword(account, newpasswd);
				System.out.println("Password changed.");
			} catch (Exception e) {
				System.out.println("Couldn't change password for "+username+". "+e.getMessage());
				e.printStackTrace();
			}
			return;
		} else if (action.equals("--shortaddress")) {
			boolean success = false;
			FreemailAccount account = freemail.getAccountManager().getAccount(username);
			try {
				success = AccountManager.addShortAddress(account, alias);
			} catch (IllegalArgumentException iae) {
				System.out.println("Couldn't add short address for "+account+". Error: "+iae.getMessage());
				return;
			} catch (Exception e) {
				System.out.println("Couldn't add short address for "+account+". "+e.getMessage());
				e.printStackTrace();
				return;
			}
			if (success) {
				System.out.println("You now have all Freemail addresses ending: '@"+alias+".freemail'. Your long address will continue to work.");
			} else {
				System.out.println("Failed to add short address.");
			}
			return;
		}

		freemail.startWorkers(false);
		freemail.startServers(false);
	}
}

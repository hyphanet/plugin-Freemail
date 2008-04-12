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

import java.io.IOException;

import freemail.Freemail;

public class FreemailCli extends Freemail {
	public FreemailCli(String cfgfile) {
		super(cfgfile);
	}
	
	public static void main(String[] args) {
		String action = "";
		String account = null;
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
		
		FreemailCli freemail = new FreemailCli(cfgfile);
		freemail.startFcp(false);
		
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
			boolean success = false;
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

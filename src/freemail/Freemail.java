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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import freemail.fcp.FCPContext;
import freemail.fcp.FCPConnection;
import freemail.imap.IMAPListener;
import freemail.smtp.SMTPListener;
import freemail.config.Configurator;
import freemail.config.ConfigClient;
import freenet.pluginmanager.FredPlugin;
import freenet.pluginmanager.FredPluginHTTP;
import freenet.pluginmanager.FredPluginHTTPAdvanced;
import freenet.pluginmanager.FredPluginThreadless;
import freenet.pluginmanager.PluginHTTPException;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;

public class Freemail implements ConfigClient, FredPlugin, FredPluginHTTP, FredPluginHTTPAdvanced, FredPluginThreadless {
	// version info
	public static final int VER_MAJOR = 0;
	public static final int VER_MINOR = 1;
	public static final int BUILD_NO = 8;
	public static final String VERSION_TAG = "Pet Shop";

	private static final String TEMPDIRNAME = "temp";
	private static final String DATADIR = "data";
	private static final String GLOBALDATADIR = "globaldata";
	private static final String ACKDIR = "delayedacks";
	private static final String CFGFILE = "globalconfig";
	private static File datadir;
	private static File globaldatadir;
	private static File tempdir;
	private static FCPConnection fcpconn;
	
	
	//Plugin
	private static PluginRespirator pr;
	private ArrayList singleAccountWatcherList = new ArrayList();
	private MessageSender sender;
	private SMTPListener smtpl;
	private AckProcrastinator ackinserter;
	private IMAPListener imapl;
	
	public static File getTempDir() {
		return Freemail.tempdir;
	}
	
	public static FCPConnection getFCPConnection() {
		return Freemail.fcpconn;
	}

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
			} catch (Exception e) {
				System.out.println("Couldn't add short address for "+account+". "+e.getMessage());
				e.printStackTrace();
				return;
			}
			System.out.println("Your short Freemail address is: 'anything@"+alias+".freemail'. Your long address will continue to work.");
			return;
		}
		
		cfg.register("globaldatadir", new Freemail(), GLOBALDATADIR);
		if (!globaldatadir.exists()) {
			globaldatadir.mkdir();
		}
		
		// start a SingleAccountWatcher for each account
		cfg.register("datadir", new Freemail(), Freemail.DATADIR);
		if (!Freemail.datadir.exists()) {
			System.out.println("Starting Freemail for the first time.");
			System.out.println("You will probably want to add an account by running Freemail with arguments --newaccount <username>");
			if (!Freemail.datadir.mkdir()) {
				System.out.println("Couldn't create data directory. Please ensure that the user you are running Freemail as has write access to its working directory");
				System.exit(1);
			}
		}
		cfg.register("tempdir", new Freemail(), Freemail.TEMPDIRNAME);
		if (!Freemail.tempdir.exists()) {
			if (!Freemail.tempdir.mkdir()) {
				System.out.println("Couldn't create temporary directory. Please ensure that the user you are running Freemail as has write access to its working directory");
				System.exit(1);
			}
		}
		
		File[] files = Freemail.datadir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().equals(".") || files[i].getName().equals(".."))
				continue;
			if (!files[i].isDirectory()) continue;
			
			Thread t = new Thread(new SingleAccountWatcher(files[i]), "Account Watcher for "+files[i].getName());
			t.setDaemon(true);
			t.start();
		}
		
		// and a sender thread
		MessageSender sender = new MessageSender(Freemail.datadir);
		Thread senderthread = new Thread(sender, "Message sender");
		senderthread.setDaemon(true);
		senderthread.start();
		
		// start the SMTP Listener
		SMTPListener smtpl = new SMTPListener(sender, cfg);
		Thread smtpthread = new Thread(smtpl, "SMTP Listener");
		smtpthread.setDaemon(true);
		smtpthread.start();
		
		// start the delayed ACK inserter
		File ackdir = new File(globaldatadir, ACKDIR);
		AckProcrastinator.setAckDir(ackdir);
		AckProcrastinator ackinserter = new AckProcrastinator();
		Thread ackinsthread = new Thread(ackinserter, "Delayed ACK Inserter");
		ackinsthread.setDaemon(true);
		ackinsthread.start();
		
		
		// start the IMAP listener
		IMAPListener imapl = new IMAPListener(cfg);
		imapl.run();
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
	
	// Plugin
	public void runPlugin(PluginRespirator pr) {
		Freemail.pr = pr;
		String cfgfile = CFGFILE;
		Configurator cfg = new Configurator(new File(cfgfile));
		FCPContext fcpctx = new FCPContext();
		
		cfg.register("fcp_host", fcpctx, "localhost");
		cfg.register("fcp_port", fcpctx, "9481");
		
		Freemail.fcpconn = new FCPConnection(fcpctx);
		Thread fcpthread  = new Thread(fcpconn, "Freemail FCP Connection");
		fcpthread.setDaemon(true);
		fcpthread.start();
		cfg.register("globaldatadir", new Freemail(), GLOBALDATADIR);
		if (!globaldatadir.exists()) {
			if(!globaldatadir.mkdir()) {
				System.out.println("Freemail plugin: Couldn't create global data directory. Please ensure that the user you are running Freemail as has write access to its working directory");
				return;
			}
		}
		cfg.register("datadir", new Freemail(), Freemail.DATADIR);
		if (!Freemail.datadir.exists()) {
			if (!Freemail.datadir.mkdir()) {
				System.out.println("Freemail plugin: Couldn't create data directory. Please ensure that the user you are running Freemail as has write access to its working directory");
				return;
			}
		}
		cfg.register("tempdir", new Freemail(), Freemail.TEMPDIRNAME);
		if (!Freemail.tempdir.exists()) {
			if (!Freemail.tempdir.mkdir()) {
				System.out.println("Freemail plugin: Couldn't create temporary directory. Please ensure that the user you are running Freemail as has write access to its working directory");
				return;
			}
		}
		File[] files = Freemail.datadir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().equals(".") || files[i].getName().equals(".."))
				continue;
			if (!files[i].isDirectory()) continue;
			
			SingleAccountWatcher saw = new SingleAccountWatcher(files[i]); 
			singleAccountWatcherList.add(saw);
			Thread t = new Thread(saw, "Freemail Account Watcher for "+files[i].getName());
			t.setDaemon(true);
			t.start();
		}
		
		// and a sender thread
		sender = new MessageSender(Freemail.datadir);
		Thread senderthread = new Thread(sender, "Freemail Message sender");
		senderthread.setDaemon(true);
		senderthread.start();
		
		// start the SMTP Listener
		smtpl = new SMTPListener(sender, cfg);
		Thread smtpthread = new Thread(smtpl, "Freemail SMTP Listener");
		smtpthread.setDaemon(true);
		smtpthread.start();
		
		// start the delayed ACK inserter
		File ackdir = new File(globaldatadir, ACKDIR);
		AckProcrastinator.setAckDir(ackdir);
		ackinserter = new AckProcrastinator();
		Thread ackinsthread = new Thread(ackinserter, "Freemail Delayed ACK Inserter");
		ackinsthread.setDaemon(true);
		ackinsthread.start();
		
		// start the IMAP listener
		imapl = new IMAPListener(cfg);
		Thread imaplthread = new Thread(imapl, "Freemail IMAP Listener");
		imaplthread.setDaemon(true);
		imaplthread.start();
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
		fcpconn.kill();
		return;
	}

	public String handleHTTPGet(HTTPRequest request) throws PluginHTTPException {
		HTMLNode pageNode = pr.getPageMaker().getPageNode("Freemail plugin", false, null);
		HTMLNode contentNode = pr.getPageMaker().getContentNode(pageNode);

		if(request.getParam("add").equals("Add account")) {
			if(!(request.getParam("name").equals("") || request.getParam("password").equals(""))) {
				try {
					AccountManager.Create(request.getParam("name"));
					AccountManager.ChangePassword(request.getParam("name"), request.getParam("password"));
					if(!request.getParam("domain").equals("")) {
						AccountManager.addShortAddress(request.getParam("name"), request.getParam("domain"));
					}
					Thread t = new Thread(new SingleAccountWatcher(new File(DATADIR, request.getParam("name"))), "Account Watcher for "+request.getParam("name"));
					t.setDaemon(true);
					t.start();
					HTMLNode addedBox = contentNode.addChild("div", "class", "infobox");
					addedBox.addChild("div", "class", "infobox-header", "Added account");
					addedBox.addChild("div", "class", "infobox-content", "Account for " + request.getParam("name") + " is created");
					
				} catch (IOException ioe) {
					HTMLNode errorBox = contentNode.addChild("div", "class", "infobox-error");
					errorBox.addChild("div", "class", "infobox-header", "IO Error"); 
					errorBox.addChild("div", "class", "infobox-content", "Couldn't create account. Please check write access to Freemail's working directory. If you want to overwrite your account, delete the appropriate directory manually in 'data' first. Freemail will intentionally not overwrite it. Error: "+ioe.getMessage());
				} catch (Exception e) {
					HTMLNode errorBox = contentNode.addChild("div", "class", "infobox-error");
					errorBox.addChild("div", "class", "infobox-header", "Error"); 
					errorBox.addChild("div", "class", "infobox-content", "Couldn't change password for "+request.getParam("name")+". "+e.getMessage());
				}
			} else {
				HTMLNode errorBox = contentNode.addChild("div", "class", "infobox-error");
				errorBox.addChild("div", "class", "infobox-header", "Error"); 
				errorBox.addChild("div", "class", "infobox-content", "Couldn't create account, name or password is missing");
			}
		}

		HTMLNode addBox = contentNode.addChild("div", "class", "infobox");
		addBox.addChild("div", "class", "infobox-header", "Add account");
		HTMLNode table = addBox.addChild("div", "class", "infobox-content").addChild("form").addChild("table", "class", "plugintable");
		HTMLNode tableRowName = table.addChild("tr");
		tableRowName.addChild("td", "Name");
		tableRowName.addChild("td").addChild("input", new String[] { "type", "name", "value", "size" }, new String[] { "text", "name", "", "30" });
		HTMLNode tableRowPassword = table.addChild("tr");
		tableRowPassword.addChild("td", "Password");
		tableRowPassword.addChild("td").addChild("input", new String[] { "type", "name", "value", "size" }, new String[] { "password", "password", "", "30" });
		HTMLNode tableRowDomain = table.addChild("tr");
		tableRowDomain.addChild("td", "Domain");
		tableRowDomain.addChild("td").addChild("input", new String[] { "type", "name", "value", "size" }, new String[] { "text", "domain", "", "30" });
		HTMLNode tableRowSubmit = table.addChild("tr");
		tableRowSubmit.addChild("td");
		tableRowSubmit.addChild("td").addChild("input", new String[] { "type", "name", "value" }, new String[] { "submit", "add", "Add account"});
		
		return pageNode.generate();
	}

	public String handleHTTPPost(HTTPRequest request) throws PluginHTTPException {
		return null;
	}

	public String handleHTTPPut(HTTPRequest request) throws PluginHTTPException {
		return null;
	}

}

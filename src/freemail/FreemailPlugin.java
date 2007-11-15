/*
 * FreemailPlugin.java
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


import freemail.Freemail;
import freemail.fcp.FCPContext;
import freemail.fcp.FCPConnection;
import freemail.imap.IMAPListener;
import freemail.smtp.SMTPListener;
import freemail.config.Configurator;

import freenet.pluginmanager.FredPlugin;
import freenet.pluginmanager.FredPluginHTTP;
import freenet.pluginmanager.FredPluginThreadless;
import freenet.pluginmanager.PluginHTTPException;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;

// although we have threads, we still 'implement' FredPluginThreadless because our runPlugin method
// returns rather than just continuing to run for the lifetime of the plugin.
public class FreemailPlugin extends Freemail implements FredPlugin, FredPluginHTTP,
                                                        FredPluginThreadless {
	private static PluginRespirator pr;
	private ArrayList singleAccountWatcherList = new ArrayList();
	private MessageSender sender;
	private SMTPListener smtpl;
	private AckProcrastinator ackinserter;
	private IMAPListener imapl;
	
	private Thread fcpThread;
	private ArrayList /* of Thread */ singleAccountWatcherThreadList = new ArrayList();
	private Thread messageSenderThread;
	private Thread smtpThread;
	private Thread ackInserterThread;
	private Thread imapThread;
	
	public void runPlugin(PluginRespirator pr) {
		FreemailPlugin.pr = pr;
		String cfgfile = CFGFILE;
		Configurator cfg = new Configurator(new File(cfgfile));
		FCPContext fcpctx = new FCPContext();
		
		cfg.register("fcp_host", fcpctx, "localhost");
		cfg.register("fcp_port", fcpctx, "9481");
		
		Freemail.fcpconn = new FCPConnection(fcpctx);
		fcpThread = new Thread(fcpconn, "Freemail FCP Connection");
		fcpThread.setDaemon(true);
		fcpThread.start();
		cfg.register("globaldatadir", new Freemail(), GLOBALDATADIR);
		if (!getGlobalDataDir().exists()) {
			if(!getGlobalDataDir().mkdir()) {
				System.out.println("Freemail plugin: Couldn't create global data directory. Please ensure that the user you are running Freemail as has write access to its working directory");
				return;
			}
		}
		cfg.register("datadir", new Freemail(), Freemail.DATADIR);
		if (!getDataDir().exists()) {
			if (!getDataDir().mkdir()) {
				System.out.println("Freemail plugin: Couldn't create data directory. Please ensure that the user you are running Freemail as has write access to its working directory");
				return;
			}
		}
		cfg.register("tempdir", new Freemail(), Freemail.TEMPDIRNAME);
		if (!getTempDir().exists()) {
			if (!Freemail.getTempDir().mkdir()) {
				System.out.println("Freemail plugin: Couldn't create temporary directory. Please ensure that the user you are running Freemail as has write access to its working directory");
				return;
			}
		}
		File[] files = getDataDir().listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().equals(".") || files[i].getName().equals(".."))
				continue;
			if (!files[i].isDirectory()) continue;
			
			SingleAccountWatcher saw = new SingleAccountWatcher(files[i]); 
			singleAccountWatcherList.add(saw);
			Thread t = new Thread(saw, "Freemail Account Watcher for "+files[i].getName());
			t.setDaemon(true);
			t.start();
			singleAccountWatcherThreadList.add(t);
		}
		
		// and a sender thread
		sender = new MessageSender(getDataDir());
		messageSenderThread = new Thread(sender, "Freemail Message sender");
		messageSenderThread.setDaemon(true);
		messageSenderThread.start();
		
		// start the SMTP Listener
		smtpl = new SMTPListener(sender, cfg);
		smtpThread = new Thread(smtpl, "Freemail SMTP Listener");
		smtpThread.setDaemon(true);
		smtpThread.start();
		
		// start the delayed ACK inserter
		File ackdir = new File(getGlobalDataDir(), ACKDIR);
		AckProcrastinator.setAckDir(ackdir);
		ackinserter = new AckProcrastinator();
		ackInserterThread = new Thread(ackinserter, "Freemail Delayed ACK Inserter");
		ackInserterThread.setDaemon(true);
		ackInserterThread.start();
		
		// start the IMAP listener
		imapl = new IMAPListener(cfg);
		imapThread = new Thread(imapl, "Freemail IMAP Listener");
		imapThread.setDaemon(true);
		imapThread.start();
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
		
		return;
	}

	public String handleHTTPGet(HTTPRequest request) throws PluginHTTPException {
		HTMLNode pageNode = pr.getPageMaker().getPageNode("Freemail plugin", false, null);
		HTMLNode contentNode = pr.getPageMaker().getContentNode(pageNode);

		HTMLNode addBox = contentNode.addChild("div", "class", "infobox");
		addBox.addChild("div", "class", "infobox-header", "Add account");
		
		HTMLNode boxContent = addBox.addChild("div", "class", "infobox-content");
		HTMLNode form = pr.addFormChild(boxContent, "", "addAccountForm");
		
		HTMLNode table = form.addChild("table", "class", "plugintable");
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
		HTMLNode pageNode = pr.getPageMaker().getPageNode("Freemail plugin", false, null);
		HTMLNode contentNode = pr.getPageMaker().getContentNode(pageNode);
		
		String add = request.getPartAsString("add", 100);
		String name = request.getPartAsString("name", 100);
		String password = request.getPartAsString("password", 100);
		String domain = request.getPartAsString("domain", 100);
		
		if(add.equals("Add account")) {
			if(!(name.equals("") || password.equals(""))) {
				try {
					AccountManager.Create(name);
					AccountManager.ChangePassword(name, password);
					if(!domain.equals("")) {
						AccountManager.addShortAddress(name, domain);
					}
					Thread t = new Thread(new SingleAccountWatcher(new File(DATADIR, name)), "Account Watcher for "+name);
					t.setDaemon(true);
					t.start();

					HTMLNode successBox = contentNode.addChild("div", "class", "infobox infobox-success");
					successBox.addChild("div", "class", "infobox-header", "Account Created");
					// TODO: This is not the world's best into message, but it's only temporary (hopefully...)
					HTMLNode text = successBox.addChild("div", "class", "infobox-content");
					text.addChild("#", "The account ");
					text.addChild("i", name);
					text.addChild("#", " was created successfully.");
					text.addChild("br");
					text.addChild("br");
					text.addChild("#", "You now need to configure your email client to send and receive email through "
							+ "Freemail using IMAP and SMTP. Freemail uses ports 3143 and 3025 for these "
							+ "respectively by default.");
				} catch (IOException ioe) {
					HTMLNode errorBox = contentNode.addChild("div", "class", "infobox infobox-error");
					errorBox.addChild("div", "class", "infobox-header", "IO Error"); 
					errorBox.addChild("div", "class", "infobox-content", "Couldn't create account. Please check write access to Freemail's working directory. If you want to overwrite your account, delete the appropriate directory manually in 'data' first. Freemail will intentionally not overwrite it. Error: "+ioe.getMessage());
				} catch (Exception e) {
					HTMLNode errorBox = contentNode.addChild("div", "class", "infobox-error");
					errorBox.addChild("div", "class", "infobox-header", "Error"); 
					errorBox.addChild("div", "class", "infobox-content", "Couldn't change password for "+name+". "+e.getMessage());
				}
				
				// XXX: There doesn't seem to be a way to get (or set) our root in the web interface,
				//      so we'll just have to assume it's this and won't change
				contentNode.addChild("a", "href", "/plugins/freemail.FreemailPlugin",
						"Freemail Home");
			} else {
				HTMLNode errorBox = contentNode.addChild("div", "class", "infobox infobox-error");
				errorBox.addChild("div", "class", "infobox-header", "Error"); 
				errorBox.addChild("div", "class", "infobox-content", "Couldn't create account, name or password is missing");
			}
		}
		
		return pageNode.generate();
	}

	public String handleHTTPPut(HTTPRequest request) throws PluginHTTPException {
		return null;
	}

}

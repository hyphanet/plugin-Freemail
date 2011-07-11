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

package freemail;


import java.io.IOException;

import freenet.clients.http.PageNode;
import freenet.pluginmanager.FredPlugin;
import freenet.pluginmanager.FredPluginHTTP;
import freenet.pluginmanager.FredPluginRealVersioned;
import freenet.pluginmanager.FredPluginThreadless;
import freenet.pluginmanager.FredPluginVersioned;
import freenet.pluginmanager.PluginHTTPException;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;

// although we have threads, we still 'implement' FredPluginThreadless because our runPlugin method
// returns rather than just continuing to run for the lifetime of the plugin.
public class FreemailPlugin extends Freemail implements FredPlugin, FredPluginHTTP,
                                                        FredPluginThreadless, FredPluginVersioned, FredPluginRealVersioned {
	private PluginRespirator pluginResp;
	
	public FreemailPlugin() throws IOException {
		super(CFGFILE);
	}
	
	public String getVersion() {
		return Version.getVersionString();
	}
	
	public void runPlugin(PluginRespirator pr) {
		pluginResp = pr;
		
		startFcp(true);
		startWorkers(true);
		startServers(true);
	}

	public String handleHTTPGet(HTTPRequest request) throws PluginHTTPException {
		PageNode page = pluginResp.getPageMaker().getPageNode("Freemail plugin", false, null);
		HTMLNode pageNode = page.outer;
		HTMLNode contentNode = page.content;

		HTMLNode addBox = contentNode.addChild("div", "class", "infobox");
		addBox.addChild("div", "class", "infobox-header", "Add account");
		
		HTMLNode boxContent = addBox.addChild("div", "class", "infobox-content");
		HTMLNode form = pluginResp.addFormChild(boxContent, "", "addAccountForm");
		
		HTMLNode table = form.addChild("table", "class", "plugintable");
		HTMLNode tableRowName = table.addChild("tr");
		tableRowName.addChild("td", "Username");
		tableRowName.addChild("td").addChild("input", new String[] { "type", "name", "value", "size" }, new String[] { "text", "name", "", "30" });
		HTMLNode tableRowPassword = table.addChild("tr");
		tableRowPassword.addChild("td", "Password");
		tableRowPassword.addChild("td").addChild("input", new String[] { "type", "name", "value", "size" }, new String[] { "password", "password", "", "30" });
		HTMLNode tableRowDomain = table.addChild("tr");
		tableRowDomain.addChild("td", "Short address");
		tableRowDomain.addChild("td").addChild("input", new String[] { "type", "name", "value", "size" }, new String[] { "text", "domain", "", "30" });
		HTMLNode tableRowSubmit = table.addChild("tr");
		tableRowSubmit.addChild("td");
		tableRowSubmit.addChild("td").addChild("input", new String[] { "type", "name", "value" }, new String[] { "submit", "add", "Add account"});

		HTMLNode clientConfigHelp = contentNode.addChild("div", "class", "infobox");
		clientConfigHelp.addChild("div", "class", "infobox-header", "Configuring your email client");
		clientConfigHelp.addChild("div", "class", "infobox-content").addChild("p",
				"The username and password you select will be used both for sending and receiving " +
				"email, and the username will also be the name of the new account. For receiving email " +
				"the server is " + getIMAPServerAddress() + " and the port is " +
				configurator.get("imap_bind_port") + ". For sending the values are " +
				getSMTPServerAddress() + " and " + configurator.get("smtp_bind_port")
				+ " respectively.");

		HTMLNode shortnameHelp = contentNode.addChild("div", "class", "infobox");
		shortnameHelp.addChild("div", "class", "infobox-header", "Short address");
		HTMLNode shortnameContent = shortnameHelp.addChild("div", "class", "infobox-content");
		shortnameContent.addChild("p",
				"The short address is a shorter and more convenient form of your new email address." +
				"If you select a short address domain you will get an additional email address " +
				"that looks like this: <anything>@<short address>.freemail");
		shortnameContent.addChild("p",
				"Unfortunately using the short address is also less secure than using the long form " +
				"address");

		return pageNode.generate();
	}

	public String handleHTTPPost(HTTPRequest request) throws PluginHTTPException {
		PageNode page = pluginResp.getPageMaker().getPageNode("Freemail plugin", false, null);
		HTMLNode pageNode = page.outer;
		HTMLNode contentNode = page.content;
		
		String add = request.getPartAsString("add", 100);
		String name = request.getPartAsString("name", 100);
		String password = request.getPartAsString("password", 100);
		String domain = request.getPartAsString("domain", 100);
		
		if(add.equals("Add account")) {
			if(!(name.equals("") || password.equals(""))) {
				try {
					FreemailAccount newAccount = getAccountManager().createAccount(name);
					AccountManager.changePassword(newAccount, password);
					boolean tryShortAddress = false;
					boolean shortAddressWorked = false;
					if(!domain.equals("")) {
						tryShortAddress = true;
						shortAddressWorked = AccountManager.addShortAddress(newAccount, domain);
					}
					startWorker(newAccount, true);

					HTMLNode successBox = contentNode.addChild("div", "class", "infobox infobox-success");
					successBox.addChild("div", "class", "infobox-header", "Account Created");
					// TODO: This is not the world's best into message, but it's only temporary (hopefully...)
					HTMLNode text = successBox.addChild("div", "class", "infobox-content");
					text.addChild("#", "The account ");
					text.addChild("i", name);
					String shortAddrMsg = "";
					if (tryShortAddress && ! shortAddressWorked) {
						shortAddrMsg = ", but your short address could NOT be created";
					}
					text.addChild("#", " was created successfully"+shortAddrMsg+".");
					text.addChild("br");
					text.addChild("br");
					text.addChild("#", "You now need to configure your email client to send and receive email through "
							+ "Freemail using IMAP and SMTP. For IMAP the server is "
							+ getIMAPServerAddress() + " and the port is " +
							configurator.get("imap_bind_port") + ". For SMTP the values are " +
							getSMTPServerAddress() + " and " + configurator.get("smtp_bind_port")
							+ " respectively.");
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

	public long getRealVersion() {
		return Version.BUILD_NO;
	}

	private String getIMAPServerAddress() {
		String address = configurator.get("imap_bind_address");

		if("0.0.0.0".equals(address)) {
			address = "127.0.0.1";
		}

		return address;
	}

	private String getSMTPServerAddress() {
		String address = configurator.get("smtp_bind_address");

		if("0.0.0.0".equals(address)) {
			address = "127.0.0.1";
		}

		return address;
	}
}

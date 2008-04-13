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


import java.io.IOException;

import freenet.pluginmanager.FredPlugin;
import freenet.pluginmanager.FredPluginHTTP;
import freenet.pluginmanager.FredPluginThreadless;
import freenet.pluginmanager.FredPluginVersioned;
import freenet.pluginmanager.PluginHTTPException;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;

// although we have threads, we still 'implement' FredPluginThreadless because our runPlugin method
// returns rather than just continuing to run for the lifetime of the plugin.
public class FreemailPlugin extends Freemail implements FredPlugin, FredPluginHTTP,
                                                        FredPluginThreadless, FredPluginVersioned {
	private PluginRespirator pluginResp;
	
	public FreemailPlugin() throws IOException {
		super(CFGFILE);
	}
	
	public String getVersion() {
		return getVersionString();
	}
	
	public void runPlugin(PluginRespirator pr) {
		pluginResp = pr;
		
		startFcp(true);
		startWorkers(true);
		startServers(true);
	}

	public String handleHTTPGet(HTTPRequest request) throws PluginHTTPException {
		HTMLNode pageNode = pluginResp.getPageMaker().getPageNode("Freemail plugin", false, null);
		HTMLNode contentNode = pluginResp.getPageMaker().getContentNode(pageNode);

		HTMLNode addBox = contentNode.addChild("div", "class", "infobox");
		addBox.addChild("div", "class", "infobox-header", "Add account");
		
		HTMLNode boxContent = addBox.addChild("div", "class", "infobox-content");
		HTMLNode form = pluginResp.addFormChild(boxContent, "", "addAccountForm");
		
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
		HTMLNode pageNode = pluginResp.getPageMaker().getPageNode("Freemail plugin", false, null);
		HTMLNode contentNode = pluginResp.getPageMaker().getContentNode(pageNode);
		
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

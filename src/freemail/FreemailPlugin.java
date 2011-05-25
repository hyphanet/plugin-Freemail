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
import java.util.List;

import freemail.ui.web.WebInterface;
import freemail.utils.Logger;
import freemail.wot.OwnIdentity;
import freemail.wot.WoTConnection;
import freenet.clients.http.PageNode;
import freenet.l10n.BaseL10n.LANGUAGE;
import freenet.pluginmanager.FredPlugin;
import freenet.pluginmanager.FredPluginHTTP;
import freenet.pluginmanager.FredPluginL10n;
import freenet.pluginmanager.FredPluginRealVersioned;
import freenet.pluginmanager.FredPluginThreadless;
import freenet.pluginmanager.FredPluginVersioned;
import freenet.pluginmanager.PluginHTTPException;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.Executor;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;

// although we have threads, we still 'implement' FredPluginThreadless because our runPlugin method
// returns rather than just continuing to run for the lifetime of the plugin.
public class FreemailPlugin extends Freemail implements FredPlugin, FredPluginHTTP,
                                                        FredPluginThreadless, FredPluginVersioned,
                                                        FredPluginRealVersioned, FredPluginL10n {
	private PluginRespirator pluginResp;
	private WebInterface webInterface = null;
	
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
		startIdentityFetch(pr, getAccountManager());

		webInterface = new WebInterface(pr.getToadletContainer(), pr.getPageMaker(), this);
	}

	private void startIdentityFetch(final PluginRespirator pr, final AccountManager accountManager) {
		pr.getNode().executor.execute(new Runnable() {
			@Override
			public void run() {
				WoTConnection wot = null;
				while(wot == null) {
					try {
						wot = new WoTConnection(pr);
					} catch (PluginNotFoundException e) {
						Logger.error(this, "Couldn't find the Web of Trust plugin");
						try {
							Thread.sleep(60 * 1000);
						} catch (InterruptedException ie) {
							//Just try again
						}
					}
				}
				List<OwnIdentity> oids = wot.getAllOwnIdentities();
				accountManager.addIdentities(oids);
			}
		}, "Freemail OwnIdentity fetcher");
	}

	public String handleHTTPGet(HTTPRequest request) throws PluginHTTPException {
		PageNode page = pluginResp.getPageMaker().getPageNode("Freemail plugin", false, null);
		HTMLNode pageNode = page.outer;
		HTMLNode contentNode = page.content;

		HTMLNode addBox = contentNode.addChild("div", "class", "infobox");
		addBox.addChild("div", "class", "infobox-header", "Adding accounts");
		
		HTMLNode boxContent = addBox.addChild("div", "class", "infobox-content");
		boxContent.addChild("p", "Please use the Web of Trust plugin to add accounts");

		return pageNode.generate();
	}

	public String handleHTTPPost(HTTPRequest request) throws PluginHTTPException {
		return handleHTTPGet(request);
	}

	public String handleHTTPPut(HTTPRequest request) throws PluginHTTPException {
		return null;
	}

	public long getRealVersion() {
		return Version.BUILD_NO;
	}

	@Override
	public String getString(String key) {
		Logger.error(this, "Missing translation for key " + key);
		return key;
	}

	@Override
	public void setLanguage(LANGUAGE newLanguage) {
		Logger.error(this, "Got new language " + newLanguage + ", but can't handle it");
	}

	@Override
	public void terminate() {
		Logger.error(this, "terminate() called");
		webInterface.terminate();
		Logger.error(this, "Web interface terminated, proceeding with normal termination");
		super.terminate();
	}
}

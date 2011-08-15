/*
 * WebPage.java
 * This file is part of Freemail
 * Copyright (C) 2011 Martin Nyhus
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

package freemail.ui.web;

import java.io.IOException;
import java.net.URI;

import freemail.l10n.FreemailL10n;
import freemail.utils.Logger;
import freenet.clients.http.LinkEnabledCallback;
import freenet.clients.http.PageMaker;
import freenet.clients.http.PageNode;
import freenet.clients.http.SessionManager;
import freenet.clients.http.Toadlet;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;

public abstract class WebPage extends Toadlet implements LinkEnabledCallback {
	private final PageMaker pageMaker;
	final PluginRespirator pluginRespirator;
	final SessionManager sessionManager;

	WebPage(PluginRespirator pluginRespirator) {
		super(null);
		this.pageMaker = pluginRespirator.getPageMaker();
		this.sessionManager = pluginRespirator.getSessionManager(WebInterface.COOKIE_NAMESPACE);
		this.pluginRespirator = pluginRespirator;
	}

	abstract void makeWebPageGet(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws ToadletContextClosedException, IOException;
	abstract void makeWebPagePost(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws ToadletContextClosedException, IOException;
	abstract boolean requiresValidSession();

	public final void handleMethodGET(URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException {
		if(requiresValidSession() && !sessionManager.sessionExists(ctx)) {
			writeTemporaryRedirect(ctx, "This page requires a valid session", LogInToadlet.getPath());
			return;
		}

		PageNode page = pageMaker.getPageNode("Freemail", ctx);
		page.addCustomStyleSheet(CSSToadlet.getPath() + "/freemail.css");

		long start = System.nanoTime();
		makeWebPageGet(uri, req, ctx, page);
		long time = ((System.nanoTime() - start) / 1000) / 1000;
		Logger.debug(this, "Page generation (get) took " + time  + " ms");
	}

	public final void handleMethodPOST(URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException {
		//Check the form password
		String formPassword = pluginRespirator.getNode().clientCore.formPassword;
		String pass = req.getPartAsStringFailsafe("formPassword", formPassword.length());

		if(!pass.equals(formPassword)) {
			writeHTMLReply(ctx, 403, "Forbidden", "Invalid form password.");
			return;
		}

		if(requiresValidSession() && !sessionManager.sessionExists(ctx)) {
			writeTemporaryRedirect(ctx, "This page requires a valid session", LogInToadlet.getPath());
			return;
		}

		PageNode page = pageMaker.getPageNode("Freemail", ctx);
		page.addCustomStyleSheet(CSSToadlet.getPath() + "/freemail.css");

		long start = System.nanoTime();
		makeWebPagePost(uri, req, ctx, page);
		long time = ((System.nanoTime() - start) / 1000) / 1000;
		Logger.debug(this, "Page generation (post) took " + time  + " ms");
	}

	static HTMLNode addInfobox(HTMLNode parent, String title) {
		HTMLNode infobox = parent.addChild("div", "class", "infobox");
		infobox.addChild("div", "class", "infobox-header", title);
		return infobox.addChild("div", "class", "infobox-content");
	}

	static HTMLNode addErrorbox(HTMLNode parent, String title) {
		HTMLNode infobox = parent.addChild("div", "class", "infobox infobox-alert");
		infobox.addChild("div", "class", "infobox-header", title);
		return infobox.addChild("div", "class", "infobox-content");
	}

	protected void addWoTNotLoadedMessage(HTMLNode parent) {
		HTMLNode errorbox = addErrorbox(parent, FreemailL10n.getString("Freemail.Global.WoTNotLoadedTitle"));
		HTMLNode text = errorbox.addChild("p");
		FreemailL10n.addL10nSubstitution(text, "Freemail.Global.WoTNotLoaded",
				new String[] {"link"},
				new HTMLNode[] {HTMLNode.link("/plugins")});
	}
}

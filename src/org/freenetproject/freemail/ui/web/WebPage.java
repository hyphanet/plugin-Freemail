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

package org.freenetproject.freemail.ui.web;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.freenetproject.freemail.l10n.FreemailL10n;
import org.freenetproject.freemail.utils.Timer;

import freenet.clients.http.LinkEnabledCallback;
import freenet.clients.http.PageMaker;
import freenet.clients.http.PageNode;
import freenet.clients.http.Toadlet;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;

public abstract class WebPage extends Toadlet implements LinkEnabledCallback {
	private final PageMaker pageMaker;
	final PluginRespirator pluginRespirator;
	final LoginManager loginManager;

	WebPage(PluginRespirator pluginRespirator, LoginManager loginManager) {
		super(null);
		this.pageMaker = pluginRespirator.getPageMaker();
		this.loginManager = loginManager;
		this.pluginRespirator = pluginRespirator;
	}

	abstract void makeWebPageGet(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws ToadletContextClosedException, IOException;
	abstract void makeWebPagePost(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws ToadletContextClosedException, IOException;
	abstract boolean requiresValidSession();

	public final void handleMethodGET(URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException {
		if(requiresFullAccess() && !ctx.isAllowedFullAccess()) {
			writeTemporaryRedirect(ctx, "This page requires full access", "/");
			return;
		}

		if(requiresValidSession() && !loginManager.sessionExists(ctx)) {
			writeTemporaryRedirect(ctx, "This page requires a valid session", LogInToadlet.getPath());
			return;
		}

		PageNode page = pageMaker.getPageNode("Freemail", ctx);
		page.addCustomStyleSheet(StaticToadlet.getPath() + "/css/freemail.css");

		Timer pageGeneration = Timer.start();
		makeWebPageGet(uri, req, ctx, page);
		pageGeneration.log(this, 1, TimeUnit.SECONDS, "Time spent serving get request");
	}

	public final void handleMethodPOST(URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException {
		if(requiresFullAccess() && !ctx.isAllowedFullAccess()) {
			writeTemporaryRedirect(ctx, "This page requires full access", "/");
			return;
		}

		//Check the form password
		String formPassword = pluginRespirator.getNode().clientCore.formPassword;
		String pass = req.getPartAsStringFailsafe("formPassword", formPassword.length());

		if(!pass.equals(formPassword)) {
			writeHTMLReply(ctx, 403, "Forbidden", "Invalid form password.");
			return;
		}

		if(requiresValidSession() && !loginManager.sessionExists(ctx)) {
			writeTemporaryRedirect(ctx, "This page requires a valid session", LogInToadlet.getPath());
			return;
		}

		PageNode page = pageMaker.getPageNode("Freemail", ctx);
		page.addCustomStyleSheet(StaticToadlet.getPath() + "/css/freemail.css");

		Timer pageGeneration = Timer.start();
		makeWebPagePost(uri, req, ctx, page);

		long timeout = 1;
		TimeUnit unit = TimeUnit.SECONDS;
		if(this instanceof AddAccountToadlet) {
			//Override time threshold since account creation is known to be slow
			//FIXME: Fix account creation instead of hiding the warning
			timeout = 5;
			unit = TimeUnit.MINUTES;
		}
		pageGeneration.log(this, timeout, unit, "Time spent serving post request");
	}

	@Override
	public boolean isEnabled(ToadletContext ctx) {
		//Implement this in the superclass so pages that don't appear
		//in the menu won't have to
		throw new UnsupportedOperationException(
				"Web pages that appear in the menu must override isEnabled(ToadletContext ctx)");
	}

	boolean requiresFullAccess() {
		return true;
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

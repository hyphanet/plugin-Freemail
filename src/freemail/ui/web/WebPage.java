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

import freemail.utils.Logger;
import freenet.client.HighLevelSimpleClient;
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

	WebPage(HighLevelSimpleClient client, PageMaker pageMaker, SessionManager sessionManager, PluginRespirator pluginRespirator) {
		super(client);
		this.pageMaker = pageMaker;
		this.sessionManager = sessionManager;
		this.pluginRespirator = pluginRespirator;
	}

	abstract void makeWebPage(URI uri, HTTPRequest req, ToadletContext ctx, HTTPMethod method, PageNode page) throws ToadletContextClosedException, IOException;
	abstract boolean requiresValidSession();

	public final void handleMethodGET(URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException {
		if(requiresValidSession() && !sessionManager.sessionExists(ctx)) {
			writeTemporaryRedirect(ctx, "This page requires a valid session", "/Freemail/Login");
			return;
		}

		PageNode page = pageMaker.getPageNode("Freemail", ctx);
		page.addCustomStyleSheet("/Freemail/static/css/freemail.css");

		long start = System.nanoTime();
		makeWebPage(uri, req, ctx, HTTPMethod.GET, page);
		long time = ((System.nanoTime() - start) / 1000) / 1000;
		Logger.debug(this, "Page generation (get) took " + time  + " ms");
	}

	public final void handleMethodPOST(URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException {
		if(requiresValidSession() && !sessionManager.sessionExists(ctx)) {
			writeTemporaryRedirect(ctx, "This page requires a valid session", "/Freemail/Login");
			return;
		}

		PageNode page = pageMaker.getPageNode("Freemail", ctx);
		page.addCustomStyleSheet("/Freemail/static/css/freemail.css");

		long start = System.nanoTime();
		makeWebPage(uri, req, ctx, HTTPMethod.POST, page);
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

	enum HTTPMethod {
		GET,
		POST;
	}
}

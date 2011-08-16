/*
 * HomeToadlet.java
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

import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;

public class HomeToadlet extends WebPage {
	private static final String PATH = WebInterface.PATH + "/";

	public HomeToadlet(PluginRespirator pluginRespirator) {
		super(pluginRespirator);
	}

	@Override
	public String path() {
		return PATH;
	}

	static String getPath() {
		return PATH;
	}

	@Override
	public boolean isEnabled(ToadletContext ctx) {
		return true;
	}

	@Override
	void makeWebPageGet(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws ToadletContextClosedException, IOException {
		HTMLNode pageNode = page.outer;
		HTMLNode contentNode = page.content;

		addWelcomeBox(contentNode);

		writeHTMLReply(ctx, 200, "OK", pageNode.generate());
	}

	@Override
	void makeWebPagePost(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws ToadletContextClosedException, IOException {
		makeWebPageGet(uri, req, ctx, page);
	}

	private void addWelcomeBox(HTMLNode contentNode) {
		HTMLNode boxContent = addInfobox(contentNode, FreemailL10n.getString("Freemail.HomeToadlet.welcomeTitle"));
		boxContent.addChild("p", FreemailL10n.getString("Freemail.HomeToadlet.welcome"));
	}

	@Override
	boolean requiresValidSession() {
		return false;
	}
}

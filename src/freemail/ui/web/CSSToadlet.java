/*
 * CSSToadlet.java
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
import java.io.InputStream;
import java.net.URI;

import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.PageMaker;
import freenet.clients.http.PageNode;
import freenet.clients.http.SessionManager;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.api.Bucket;
import freenet.support.api.HTTPRequest;
import freenet.support.io.BucketTools;

public class CSSToadlet extends WebPage {
	private static final String PATH = "/Freemail/static/css/";

	CSSToadlet(HighLevelSimpleClient client, PageMaker pageMaker, SessionManager sessionManager, PluginRespirator pluginRespirator) {
		super(client, pageMaker, sessionManager, pluginRespirator);
	}

	@Override
	public boolean isEnabled(ToadletContext ctx) {
		return true;
	}

	@Override
	void makeWebPage(URI uri, HTTPRequest req, ToadletContext ctx, HTTPMethod method, PageNode page) throws ToadletContextClosedException, IOException {
		String filename = uri.getPath().substring(PATH.length());

		//Check that the filename has the expected format
		if(!filename.matches("[a-zA-Z0-9]+\\.css")) {
			writeHTMLReply(ctx, 403, "Forbidden", "Invalid filename format");
			return;
		}

		InputStream is = getClass().getResourceAsStream("/freemail/ui/web/css/" + filename);

		Bucket b = ctx.getBucketFactory().makeBucket(-1);
		BucketTools.copyFrom(b, is, -1);

		writeReply(ctx, 200, "text/css", "OK", null, b);
	}

	@Override
	public String path() {
		return PATH;
	}

	static String getPath() {
		return PATH;
	}

	@Override
	boolean requiresValidSession() {
		return false;
	}
}

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

import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.api.Bucket;
import freenet.support.api.HTTPRequest;
import freenet.support.io.BucketTools;

public class CSSToadlet extends WebPage {
	private static final String PATH = WebInterface.PATH + "/static/css";

	CSSToadlet(PluginRespirator pluginRespirator) {
		super(pluginRespirator);
	}

	@Override
	public boolean isEnabled(ToadletContext ctx) {
		return true;
	}

	@Override
	void makeWebPageGet(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws ToadletContextClosedException, IOException {
		String filename = uri.getPath().substring(PATH.length() + "/".length());

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
	void makeWebPagePost(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws ToadletContextClosedException, IOException {
		makeWebPageGet(uri, req, ctx, page);
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

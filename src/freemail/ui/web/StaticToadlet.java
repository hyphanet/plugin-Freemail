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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.api.Bucket;
import freenet.support.api.HTTPRequest;
import freenet.support.io.BucketTools;

public class StaticToadlet extends WebPage {
	private static final String PATH = WebInterface.PATH + "/static";

	private final List<Mapping> requests = new CopyOnWriteArrayList<Mapping>();

	StaticToadlet(PluginRespirator pluginRespirator) {
		super(pluginRespirator);
	}

	@Override
	public boolean isEnabled(ToadletContext ctx) {
		return true;
	}

	@Override
	void makeWebPageGet(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws ToadletContextClosedException, IOException {
		String path = uri.getPath();

		//Check for path matches
		Mapping request = null;
		String filename = null;
		for(Mapping r : requests) {
			if(!path.startsWith(r.path)) {
				continue;
			}

			filename = path.substring(r.path.length());
			if(!r.filename.matcher(filename).matches()) {
				continue;
			}

			request = r;
			break;
		}

		if(request == null) {
			writeHTMLReply(ctx, 403, "Forbidden", "No match found for that path");
			return;
		}

		InputStream is = getClass().getResourceAsStream(request.source + filename);

		Bucket b = ctx.getBucketFactory().makeBucket(-1);
		BucketTools.copyFrom(b, is, -1);

		writeReply(ctx, 200, request.mime, "OK", null, b);
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

	void handle(String path, String filename, String source, String mime) {
		if(!path.startsWith(WebInterface.PATH + "/static/")) {
			throw new IllegalArgumentException("Path must be within /static/");
		}
		if(!path.endsWith("/")) {
			throw new IllegalArgumentException("Path must end with /");
		}

		if(!source.startsWith("/freemail/")) {
			throw new IllegalArgumentException("Source must be within /freemail/");
		}
		if(!source.endsWith("/")) {
			throw new IllegalArgumentException("Source must end with /");
		}

		requests.add(new Mapping(path, Pattern.compile(filename), source, mime));
	}

	private class Mapping {
		private final String path;
		private final Pattern filename;
		private final String source;
		private final String mime;

		Mapping(String path, Pattern filename, String source, String mime) {
			this.path = path;
			this.filename = filename;
			this.source = source;
			this.mime = mime;
		}
	}
}

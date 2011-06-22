/*
 * AddAccountToadlet.java
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
import freenet.clients.http.PageMaker;
import freenet.clients.http.PageNode;
import freenet.clients.http.SessionManager;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.support.api.HTTPRequest;

public class AddAccountToadlet extends WebPage {
	AddAccountToadlet(HighLevelSimpleClient client, PageMaker pageMaker, SessionManager sessionManager) {
		super(client, pageMaker, sessionManager);
	}

	@Override
	public boolean isEnabled(ToadletContext ctx) {
		return true;
	}

	@Override
	void makeWebPage(URI uri, HTTPRequest req, ToadletContext ctx, HTTPMethod method, PageNode page) throws ToadletContextClosedException, IOException {
		switch(method) {
		case GET:
			makeWebPageGet(ctx);
			break;
		case POST:
			makeWebPagePost();
			break;
		default:
			//This will only happen if a new value is added to HTTPMethod, so log it and send an
			//error message
			assert false : "HTTPMethod has unknown value: " + method;
			Logger.error(this, "HTTPMethod has unknown value: " + method);
			writeHTMLReply(ctx, 200, "OK", "Unknown HTTP method " + method + ". This is a bug in Freemail");
		}
	}

	private void makeWebPageGet(ToadletContext ctx) throws ToadletContextClosedException, IOException {
		writeTemporaryRedirect(ctx, "Redirecting to login page", "/Freemail/Login");
	}

	private void makeWebPagePost() {
		throw new UnsupportedOperationException();
	}

	@Override
	boolean requiresValidSession() {
		return false;
	}

	@Override
	public String path() {
		return "/Freemail/AddAccount";
	}
}

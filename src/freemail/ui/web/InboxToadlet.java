/*
 * InboxToadlet.java
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

import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.SessionManager;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.support.api.HTTPRequest;

public class InboxToadlet extends WebPage {
	private final SessionManager sessionManager;

	InboxToadlet(HighLevelSimpleClient client, SessionManager sessionManager) {
		super(client);
		this.sessionManager = sessionManager;
	}

	@Override
	public void handleMethodGET(URI uri, HTTPRequest req, ToadletContext ctx)
			throws ToadletContextClosedException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void handleMethodPOST(URI uri, HTTPRequest req, ToadletContext ctx)
			throws ToadletContextClosedException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEnabled(ToadletContext ctx) {
		return sessionManager.sessionExists(ctx);
	}

	@Override
	public String path() {
		return "/Freemail/Inbox";
	}
}

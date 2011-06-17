/*
 * NewMessageToadlet.java
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
import freenet.clients.http.PageMaker;
import freenet.clients.http.PageNode;
import freenet.clients.http.SessionManager;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;

public class NewMessageToadlet extends WebPage {
	NewMessageToadlet(HighLevelSimpleClient client, SessionManager sessionManager, PageMaker pageMaker) {
		super(client, pageMaker, sessionManager);
	}

	@Override
	public void makeWebPage(URI uri, HTTPRequest req, ToadletContext ctx, HTTPMethod method, PageNode page) throws ToadletContextClosedException, IOException {
		HTMLNode pageNode = page.outer;
		HTMLNode contentNode = page.content;

		HTMLNode messageBox = addInfobox(contentNode, "New message");
		HTMLNode messageForm = ctx.addFormChild(messageBox, path(), "newMessage");

		HTMLNode subjectBox = addInfobox(messageForm, "Subject");
		subjectBox.addChild("input", new String[] {"name",    "type", "size"},
		                             new String[] {"subject", "text", "100"});

		HTMLNode messageBodyBox = addInfobox(messageForm, "Text");
		messageBodyBox.addChild("textarea", new String[] {"name",         "cols", "rows", "class"},
		                                    new String[] {"message-text", "100",  "30",   "message-text"});

		messageForm.addChild("input", new String[] {"type",   "value"},
		                              new String[] {"submit", "Send"});

		writeHTMLReply(ctx, 200, "OK", pageNode.generate());
	}

	@Override
	public boolean isEnabled(ToadletContext ctx) {
		return sessionManager.sessionExists(ctx);
	}

	@Override
	public String path() {
		return "/Freemail/NewMessage";
	}

	@Override
	boolean requiresValidSession() {
		return true;
	}
}

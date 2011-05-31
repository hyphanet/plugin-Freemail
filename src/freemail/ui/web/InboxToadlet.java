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
import freenet.clients.http.PageMaker;
import freenet.clients.http.PageNode;
import freenet.clients.http.SessionManager;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;

public class InboxToadlet extends WebPage {
	InboxToadlet(HighLevelSimpleClient client, SessionManager sessionManager, PageMaker pageMaker) {
		super(client, pageMaker, sessionManager);
	}

	@Override
	public void makeWebPage(URI uri, HTTPRequest req, ToadletContext ctx, HTTPMethod method, PageNode page) throws ToadletContextClosedException, IOException {
		HTMLNode pageNode = page.outer;
		HTMLNode contentNode = page.content;

		HTMLNode container = contentNode.addChild("div", "class", "container");

		//Add the list of folders
		HTMLNode folderList = container.addChild("div", "class", "folderlist");
		addFolder(folderList, "Inbox");
		HTMLNode freenet = addFolder(folderList, "Freenet");
		HTMLNode freemail = addFolder(freenet, "Freemail");
		addFolder(freemail, "Bugs");

		//Add the messages
		HTMLNode messageList = container.addChild("div", "class", "messagelist");
		for(int i = 1; i <= 10; i++) {
			addMessage(messageList, "Test message " + i, "Zidel", "2011-05-31 12:30");
		}

		writeHTMLReply(ctx, 200, "OK", pageNode.generate());
	}

	private HTMLNode addFolder(HTMLNode parent, String folderName) {
		HTMLNode folderDiv = parent.addChild("div", "class", "folder");
		folderDiv.addChild("p", folderName);
		return folderDiv;
	}

	private void addMessage(HTMLNode parent, String title, String author, String date) {
		HTMLNode message = parent.addChild("div", "class", "message");
		HTMLNode titleDiv = message.addChild("div", "class", "title");
		titleDiv.addChild("p", title);
		HTMLNode authorDiv = message.addChild("div", "class", "author");
		authorDiv.addChild("p", author);
		HTMLNode dateDiv = message.addChild("div", "class", "date");
		dateDiv.addChild("p", date);
	}

	@Override
	public boolean isEnabled(ToadletContext ctx) {
		return sessionManager.sessionExists(ctx);
	}

	@Override
	public String path() {
		return "/Freemail/Inbox";
	}

	@Override
	boolean requiresValidSession() {
		return true;
	}
}

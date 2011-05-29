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

import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.LinkEnabledCallback;
import freenet.clients.http.Toadlet;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;

abstract class WebPage extends Toadlet implements LinkEnabledCallback {
	WebPage(HighLevelSimpleClient client) {
		super(client);
	}

	//All web pages must be able to handle both get and post (even if that means eg. redirecting
	//post to get).
	public abstract void handleMethodGET(URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException;
	public abstract void handleMethodPOST(URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException;

	static HTMLNode addInfobox(HTMLNode parent, String title) {
		HTMLNode infobox = parent.addChild("div", "class", "infobox");
		infobox.addChild("div", "class", "infobox-header", title);
		return infobox.addChild("div", "class", "infobox-content");
	}
}

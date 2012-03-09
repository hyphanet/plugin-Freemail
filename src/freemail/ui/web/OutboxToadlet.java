/*
 * OutboxToadlet.java
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

import freemail.AccountManager;
import freemail.FreemailAccount;
import freemail.FreemailPlugin;
import freemail.l10n.FreemailL10n;
import freemail.transport.MessageHandler.OutboxMessage;
import freemail.wot.Identity;
import freemail.wot.WoTConnection;
import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;

public class OutboxToadlet extends WebPage {
	private static final String PATH = WebInterface.PATH + "/Outbox";

	private final AccountManager accountManager;
	private final FreemailPlugin freemailPlugin;

	OutboxToadlet(PluginRespirator pluginRespirator, AccountManager accountManager, FreemailPlugin freemailPlugin) {
		super(pluginRespirator);
		this.accountManager = accountManager;
		this.freemailPlugin = freemailPlugin;
	}

	@Override
	void makeWebPageGet(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws ToadletContextClosedException, IOException {
		String identity = sessionManager.useSession(ctx).getUserID();
		FreemailAccount account = accountManager.getAccount(identity);

		HTMLNode messageTable = page.content.addChild("table");

		//Add the message list header
		HTMLNode header = messageTable.addChild("tr");
		header.addChild("th", FreemailL10n.getString("Freemail.OutboxToadlet.recipient"));
		header.addChild("th", FreemailL10n.getString("Freemail.OutboxToadlet.subject"));
		header.addChild("th", FreemailL10n.getString("Freemail.OutboxToadlet.firstSendTime"));
		header.addChild("th", FreemailL10n.getString("Freemail.OutboxToadlet.lastSendTime"));

		WoTConnection wotConnection = freemailPlugin.getWotConnection();
		for(OutboxMessage message : account.getMessageHandler().listOutboxMessages()) {
			HTMLNode row = messageTable.addChild("tr");

			String recipient;
			try {
				Identity i = wotConnection.getIdentity(message.recipient, account.getIdentity());
				String domain = i.getBase32IdentityID();
				recipient = i.getNickname() + "@" + domain + ".freemail";
			} catch(PluginNotFoundException e) {
				//Fall back to only showing the identity id
				recipient = "unknown@" + message.recipient + ".freemail";
			}

			String firstSendTime;
			if(message.getFirstSendTime() == null) {
				firstSendTime = FreemailL10n.getString("Freemail.OutboxToadlet.neverSent");
			} else {
				firstSendTime = message.getFirstSendTime().toString();
			}

			String lastSendTime;
			if(message.getFirstSendTime() == null) {
				lastSendTime = FreemailL10n.getString("Freemail.OutboxToadlet.neverSent");
			} else {
				lastSendTime = message.getLastSendTime().toString();
			}

			row.addChild("td", recipient);
			row.addChild("td", message.subject);
			row.addChild("td", firstSendTime);
			row.addChild("td", lastSendTime);
		}

		writeHTMLReply(ctx, 200, "OK", page.outer.generate());
	}

	@Override
	void makeWebPagePost(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws ToadletContextClosedException, IOException {
		makeWebPageGet(uri, req, ctx, page);
	}

	@Override
	public boolean isEnabled(ToadletContext ctx) {
		return sessionManager.sessionExists(ctx);
	}

	@Override
	boolean requiresValidSession() {
		return true;
	}

	@Override
	public String path() {
		return PATH;
	}
}

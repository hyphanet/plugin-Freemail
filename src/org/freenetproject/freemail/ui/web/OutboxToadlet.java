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

package org.freenetproject.freemail.ui.web;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.freenetproject.freemail.AccountManager;
import org.freenetproject.freemail.FreemailAccount;
import org.freenetproject.freemail.FreemailPlugin;
import org.freenetproject.freemail.l10n.FreemailL10n;
import org.freenetproject.freemail.transport.MessageHandler.OutboxMessage;
import org.freenetproject.freemail.wot.Identity;
import org.freenetproject.freemail.wot.WoTConnection;

import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.HTMLNode;
import freenet.support.Timer;
import freenet.support.api.HTTPRequest;

public class OutboxToadlet extends WebPage {
	private static final String PATH = WebInterface.PATH + "/Outbox";

	private final AccountManager accountManager;
	private final FreemailPlugin freemailPlugin;

	OutboxToadlet(PluginRespirator pluginRespirator, AccountManager accountManager, FreemailPlugin freemailPlugin,
	              LoginManager loginManager) {
		super(pluginRespirator, loginManager);
		this.accountManager = accountManager;
		this.freemailPlugin = freemailPlugin;
	}

	@Override
	void makeWebPageGet(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws ToadletContextClosedException, IOException {
		Timer outboxTimer = Timer.start();

		String identity = loginManager.getSession(ctx).getUserID();
		FreemailAccount account = accountManager.getAccount(identity);

		HTMLNode messageTable = page.content.addChild("table");

		//Add the message list header
		HTMLNode header = messageTable.addChild("tr");
		header.addChild("th", FreemailL10n.getString("Freemail.OutboxToadlet.recipient"));
		header.addChild("th", FreemailL10n.getString("Freemail.OutboxToadlet.subject"));
		header.addChild("th", FreemailL10n.getString("Freemail.OutboxToadlet.firstSendTime"));
		header.addChild("th", FreemailL10n.getString("Freemail.OutboxToadlet.lastSendTime"));

		WoTConnection wotConnection = freemailPlugin.getWotConnection();

		Timer messageRead = outboxTimer.startSubTimer();
		List<OutboxMessage> messages = account.getMessageHandler().listOutboxMessages();
		messageRead.log(this, 1, TimeUnit.SECONDS, "Time spent reading outbox messages");

		Timer messageListing = outboxTimer.startSubTimer();
		for(OutboxMessage message : messages) {
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
		messageListing.log(this, "Time spent adding messages to page");

		writeHTMLReply(ctx, 200, "OK", page.outer.generate());

		outboxTimer.log(this, 1, TimeUnit.SECONDS, "Time spent generating outbox page");
	}

	@Override
	void makeWebPagePost(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws ToadletContextClosedException, IOException {
		makeWebPageGet(uri, req, ctx, page);
	}

	@Override
	public boolean isEnabled(ToadletContext ctx) {
		return ctx.isAllowedFullAccess() && loginManager.sessionExists(ctx);
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

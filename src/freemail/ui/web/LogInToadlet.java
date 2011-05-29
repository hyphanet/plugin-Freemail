/*
 * LogInToadlet.java
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
import java.util.NoSuchElementException;

import javax.naming.SizeLimitExceededException;

import freemail.AccountManager;
import freemail.FreemailAccount;
import freemail.utils.Logger;
import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;

public class LogInToadlet extends WebPage {
	private final AccountManager accountManager;
	private final PluginRespirator pluginRespirator;

	public LogInToadlet(HighLevelSimpleClient client, PluginRespirator pluginRespirator, AccountManager accountManager) {
		super(client);
		this.pluginRespirator = pluginRespirator;
		this.accountManager = accountManager;
	}

	@Override
	public String path() {
		return "/Freemail/Login";
	}

	@Override
	public boolean isEnabled(ToadletContext ctx) {
		return true;
	}

	@Override
	public void handleMethodGET(URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException {
		PageNode page = pluginRespirator.getPageMaker().getPageNode("Freemail", ctx);
		HTMLNode pageNode = page.outer;
		HTMLNode contentNode = page.content;

		addLoginBox(contentNode);

		writeHTMLReply(ctx, 200, "OK", pageNode.generate());
	}

	private void addLoginBox(HTMLNode contentNode) {
		HTMLNode welcomeBox = contentNode.addChild("div", "class", "infobox");
		welcomeBox.addChild("div", "class", "infobox-header", "Login");
		HTMLNode boxContent = welcomeBox.addChild("div", "class", "infobox-content");

		HTMLNode loginForm = pluginRespirator.addFormChild(boxContent, "/Freemail/Login", "login");
		HTMLNode ownIdSelector = loginForm.addChild("select", "name", "OwnIdentityID");
		for(FreemailAccount account : accountManager.getAllAccounts()) {
			//TODO: Show a better name, preferably the same as Freetalk
			ownIdSelector.addChild("option", "value", account.getUsername(), account.getUsername());
		}
		loginForm.addChild("input", new String[] { "type", "name", "value" }, new String[] { "submit", "submit", "Login" });
	}

	@Override
	public void handleMethodPOST(URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException {
		String pass;
		try {
			pass = req.getPartAsStringThrowing("formPassword", 32);
		} catch (SizeLimitExceededException e) {
			writeHTMLReply(ctx, 403, "Forbidden", "Form password too long");
			return;
		} catch (NoSuchElementException e) {
			writeHTMLReply(ctx, 403, "Forbidden", "Missing form password");
			return;
		}

		if((pass.length() == 0) || !pass.equals(pluginRespirator.getNode().clientCore.formPassword)) {
			writeHTMLReply(ctx, 403, "Forbidden", "Invalid form password.");
			return;
		}

		String identity;
		try {
			identity = req.getPartAsStringThrowing("OwnIdentityID", 64);
		} catch (SizeLimitExceededException e) {
			//Someone is deliberately passing bad data, or there is a bug in the PUT code
			Logger.error(this, "Got OwnIdentityID that was too long. First 100 bytes: " + req.getPartAsStringFailsafe("OwnIdentityID", 100));

			//TODO: Write a better message
			writeHTMLReply(ctx, 200, "Bad data", "The request contained bad data. This is probably a bug in Freemail");
			return;
		} catch (NoSuchElementException e) {
			//Someone is deliberately passing bad data, or there is a bug in the PUT code
			Logger.error(this, "Got POST request without OwnIdentityID");

			//TODO: Write a better message
			writeHTMLReply(ctx, 200, "Bad data", "The request didn't contain the expected data. This is probably a bug in Freemail");
			return;
		}

		pluginRespirator.getSessionManager("Freemail").createSession(accountManager.getAccount(identity).getUsername(), ctx);
		writeTemporaryRedirect(ctx, "Login successful, redirecting to home page", "/Freemail/");
	}
}

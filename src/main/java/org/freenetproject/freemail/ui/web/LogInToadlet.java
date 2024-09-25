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

package org.freenetproject.freemail.ui.web;

import java.net.URI;
import java.util.NoSuchElementException;

import javax.naming.SizeLimitExceededException;

import org.freenetproject.freemail.AccountManager;
import org.freenetproject.freemail.FreemailAccount;
import org.freenetproject.freemail.l10n.FreemailL10n;
import org.freenetproject.freemail.utils.Logger;

import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;

public class LogInToadlet extends WebPage {
	private static final String PATH = WebInterface.PATH + "/Login";

	private final AccountManager accountManager;

	public LogInToadlet(PluginRespirator pluginRespirator, AccountManager accountManager, LoginManager loginManager) {
		super(pluginRespirator, loginManager);

		this.accountManager = accountManager;
	}

	@Override
	public String path() {
		return PATH;
	}

	static String getPath() {
		return PATH;
	}

	@Override
	public boolean isEnabled(ToadletContext ctx) {
		return ctx.isAllowedFullAccess() && !loginManager.sessionExists(ctx);
	}

	@Override
	HTTPResponse makeWebPageGet(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) {
		HTMLNode pageNode = page.outer;
		HTMLNode contentNode = page.content;

		if(accountManager.getAllAccounts().size() > 0) {
			addLoginBox(contentNode);
		}
		addNewAccountBox(contentNode);

		return new GenericHTMLResponse(ctx, 200, "OK", pageNode.generate());
	}

	private void addLoginBox(HTMLNode contentNode) {
		HTMLNode boxContent = addInfobox(contentNode, FreemailL10n.getString("Freemail.LoginToadlet.LoginBox"));

		HTMLNode loginForm = pluginRespirator.addFormChild(boxContent, LogInToadlet.getPath(), "login");
		HTMLNode ownIdSelector = loginForm.addChild("select", "name", "OwnIdentityID");
		for(FreemailAccount account : accountManager.getAllAccounts()) {
			//FIXME: Nickname might be ambiguous
			String nickname = account.getNickname();
			if(nickname == null) {
				nickname = account.getIdentity();
			}
			ownIdSelector.addChild("option", "value", account.getIdentity(), nickname);
		}
		loginForm.addChild("input", new String[] {"type",   "name",   "value"},
		                            new String[] {"submit", "submit", FreemailL10n.getString("Freemail.LoginToadlet.LoginButton")});
	}

	private void addNewAccountBox(HTMLNode parent) {
		HTMLNode boxContent = addInfobox(parent, "Add account");
		boxContent.addChild("a", "href", AddAccountToadlet.getPath(), "You can add another account here");
	}

	@Override
	HTTPResponse makeWebPagePost(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) {
		String identity;
		try {
			identity = req.getPartAsStringThrowing("OwnIdentityID", 64);
		} catch (SizeLimitExceededException e) {
			//Someone is deliberately passing bad data, or there is a bug in the PUT code
			Logger.error(this, "Got OwnIdentityID that was too long. First 100 bytes: " + req.getPartAsStringFailsafe("OwnIdentityID", 100));

			//TODO: Write a better message
			return new GenericHTMLResponse(ctx, 200, "OK", "The request contained bad data. This is probably a bug in Freemail");
		} catch (NoSuchElementException e) {
			//Someone is deliberately passing bad data, or there is a bug in the PUT code
			Logger.error(this, "Got POST request without OwnIdentityID");

			//TODO: Write a better message
			return new GenericHTMLResponse(ctx, 200, "OK", "The request didn't contain the expected data. This is probably a bug in Freemail");
		}

		loginManager.createSession(ctx, accountManager.getAccount(identity));
		return new HTTPRedirectResponse(ctx, "Login successful, redirecting to inbox", InboxToadlet.getPath());
	}

	@Override
	boolean requiresValidSession() {
		return false;
	}
}

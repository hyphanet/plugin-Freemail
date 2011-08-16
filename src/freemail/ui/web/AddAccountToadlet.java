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
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.naming.SizeLimitExceededException;

import freemail.AccountManager;
import freemail.FreemailAccount;
import freemail.l10n.FreemailL10n;
import freemail.utils.Logger;
import freemail.wot.OwnIdentity;
import freemail.wot.WoTConnection;
import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;

public class AddAccountToadlet extends WebPage {
	private static final String PATH = WebInterface.PATH + "/AddAccount";

	private final WoTConnection wotConnection;
	private final AccountManager accountManager;

	AddAccountToadlet(PluginRespirator pluginRespirator, WoTConnection wotConnection, AccountManager accountManager) {
		super(pluginRespirator);

		this.wotConnection = wotConnection;
		this.accountManager = accountManager;
	}

	@Override
	public boolean isEnabled(ToadletContext ctx) {
		return true;
	}

	@Override
	void makeWebPageGet(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws ToadletContextClosedException, IOException {
		HTMLNode pageNode = page.outer;
		HTMLNode contentNode = page.content;

		List<OwnIdentity> ownIdentities;
		try {
			ownIdentities = wotConnection.getAllOwnIdentities();
		} catch(PluginNotFoundException e) {
			addWoTNotLoadedMessage(contentNode);
			writeHTMLReply(ctx, 200, "OK", pageNode.generate());
			return;
		}

		List<OwnIdentity> identitiesWithoutAccount = new LinkedList<OwnIdentity>();
		for(OwnIdentity oid : ownIdentities) {
			if(accountManager.getAccount(oid.getIdentityID()) == null) {
				identitiesWithoutAccount.add(oid);
			}
		}

		if(identitiesWithoutAccount.size() == 0) {
			HTMLNode infobox = addInfobox(contentNode, FreemailL10n.getString("Freemail.AddAccountToadlet.noIdentitiesTitle"));
			infobox.addChild("p", FreemailL10n.getString("Freemail.AddAccountToadlet.noIdentities"));
			writeHTMLReply(ctx, 200, "OK", pageNode.generate());
			return;
		}

		HTMLNode boxContent = addInfobox(contentNode, FreemailL10n.getString("Freemail.AddAccountToadlet.boxTitle"));

		HTMLNode addAccountForm = pluginRespirator.addFormChild(boxContent, AddAccountToadlet.getPath(), "addAccount");

		HTMLNode identity = addAccountForm.addChild("p", FreemailL10n.getString("Freemail.AddAccountToadlet.selectIdentity") + " ");
		HTMLNode ownIdSelector = identity.addChild("select", "name", "OwnIdentityID");

		for(OwnIdentity oid : identitiesWithoutAccount) {
			//FIXME: Nickname might be ambiguous
			ownIdSelector.addChild("option", "value", oid.getIdentityID(), oid.getNickname());
		}

		HTMLNode password = addAccountForm.addChild("p", FreemailL10n.getString("Freemail.AddAccountToadlet.password") + " ");
		password.addChild("input", new String[] {"type",     "name"},
		                           new String[] {"password", "password"});

		HTMLNode confirmPassword = addAccountForm.addChild("p", FreemailL10n.getString("Freemail.AddAccountToadlet.confirmPassword") + " ");
		confirmPassword.addChild("input", new String[] {"type",     "name"},
		                                  new String[] {"password", "passwordVerification"});

		addAccountForm.addChild("input", new String[] { "type", "name", "value" }, new String[] { "submit", "submit", FreemailL10n.getString("Freemail.AddAccountToadlet.submit") });

		writeHTMLReply(ctx, 200, "OK", pageNode.generate());
	}

	@Override
	void makeWebPagePost(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws ToadletContextClosedException, IOException {
		//Check the form password
		String pass;
		try {
			pass = req.getPartAsStringThrowing("formPassword", 32);
		} catch(SizeLimitExceededException e) {
			writeHTMLReply(ctx, 403, "Forbidden", "Form password too long");
			return;
		} catch(NoSuchElementException e) {
			writeHTMLReply(ctx, 403, "Forbidden", "Missing form password");
			return;
		}

		if((pass.length() == 0) || !pass.equals(pluginRespirator.getNode().clientCore.formPassword)) {
			writeHTMLReply(ctx, 403, "Forbidden", "Invalid form password.");
			return;
		}

		//Get the identity id
		String identity = req.getPartAsStringFailsafe("OwnIdentityID", 64);

		//Check that the passwords match
		String password = req.getPartAsStringFailsafe("password", 1000);
		String password2 = req.getPartAsStringFailsafe("passwordVerification", 1000);

		if(password.equals("") || (!password.equals(password2))) {
			//FIXME: Write a better error message
			writeHTMLReply(ctx, 200, "OK", "The passwords were different, or you need to choose a password");
		}

		//Fetch identity from WoT
		List<OwnIdentity> ownIdentities;
		try {
			ownIdentities = wotConnection.getAllOwnIdentities();
		} catch(PluginNotFoundException e) {
			HTMLNode pageNode = page.outer;
			addWoTNotLoadedMessage(page.content);
			writeHTMLReply(ctx, 200, "OK", pageNode.generate());
			return;
		}

		OwnIdentity ownIdentity = null;
		for(OwnIdentity oid : ownIdentities) {
			if(oid.getIdentityID().equals(identity)) {
				ownIdentity = oid;
				break;
			}
		}

		if(ownIdentity == null) {
			Logger.error(this, "Requested identity (" + identity + ") doesn't exist");

			//TODO: Write a better message
			writeHTMLReply(ctx, 200, "OK", "The specified identitiy doesn't exist");
			return;
		}

		List<OwnIdentity> toAdd = new LinkedList<OwnIdentity>();
		toAdd.add(ownIdentity);
		accountManager.addIdentities(toAdd);
		FreemailAccount account = accountManager.getAccount(ownIdentity.getIdentityID());
		try {
			AccountManager.changePassword(account, password);
		} catch(Exception e) {
			//This is never actually thrown
			throw new AssertionError();
		}

		writeTemporaryRedirect(ctx, "Account added, redirecting to login page", LogInToadlet.getPath());
	}

	@Override
	boolean requiresValidSession() {
		return false;
	}

	@Override
	public String path() {
		return PATH;
	}

	static String getPath() {
		return PATH;
	}

	static String getIdentityStatusPath(String identityId) {
		return getPath() + "?identity=" + identityId;
	}
}

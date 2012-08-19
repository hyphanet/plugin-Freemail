/*
 * InfoToadlet.java
 * This file is part of Freemail, copyright (C) 2012
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

import org.freenetproject.freemail.AccountManager;
import org.freenetproject.freemail.FreemailAccount;
import org.freenetproject.freemail.config.Configurator;
import org.freenetproject.freemail.l10n.FreemailL10n;
import org.freenetproject.freemail.utils.EmailAddress;

import freenet.clients.http.PageNode;
import freenet.clients.http.SessionManager.Session;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;

class InfoToadlet extends WebPage {
	private static final String PATH = WebInterface.PATH + "/Info";

	private final AccountManager accountManager;
	private final Configurator config;

	InfoToadlet(PluginRespirator pluginRespirator, LoginManager loginManager, AccountManager accountManager,
			Configurator config) {
		super(pluginRespirator, loginManager);
		this.accountManager = accountManager;
		this.config = config;
	}

	private void addInfoLine(HTMLNode parent, String title, String content, String className) {
		HTMLNode line = parent.addChild("span", "class", className);
		line.addChild("span", "class", "title", title);
		line.addChild("#", " " + content);
		parent.addChild("br");
	}

	private void addAccountBox(HTMLNode parent, FreemailAccount account) {
		HTMLNode accountBox = addInfobox(parent, FreemailL10n.getString("Freemail.InfoToadlet.account.title"));

		String local = EmailAddress.cleanLocalPart(account.getNickname());
		if(local.length() == 0) {
			local = "mail";
		}
		EmailAddress address = new EmailAddress(local + "@" + account.getDomain());

		addInfoLine(accountBox, FreemailL10n.getString("Freemail.InfoToadlet.email-title"), address.toString(),
				"email");
	}

	private void addServerInfo(HTMLNode parent) {
		HTMLNode serverBox = addInfobox(parent, FreemailL10n.getString("Freemail.InfoToadlet.server.title"));
		addInfoLine(serverBox, FreemailL10n.getString("Freemail.InfoToadlet.imap-addr.title"),
				config.get(Configurator.IMAP_BIND_ADDRESS), "imapAddr");
		addInfoLine(serverBox, FreemailL10n.getString("Freemail.InfoToadlet.imap-port.title"),
				config.get(Configurator.IMAP_BIND_PORT), "imapPort");
		addInfoLine(serverBox, FreemailL10n.getString("Freemail.InfoToadlet.imap-addr.title"),
				config.get(Configurator.SMTP_BIND_ADDRESS), "imapPort");
		addInfoLine(serverBox, FreemailL10n.getString("Freemail.InfoToadlet.smtp-port.title"),
				config.get(Configurator.SMTP_BIND_PORT), "smtpPort");
	}

	@Override
	void makeWebPageGet(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page)
			throws ToadletContextClosedException, IOException {
		//Add account info if one is logged in
		Session session = loginManager.getSession(ctx);
		if(session != null) {
			String identity = session.getUserID();
			FreemailAccount account = accountManager.getAccount(identity);
			addAccountBox(page.content, account);
		}

		//Add general Freemail server info
		addServerInfo(page.content);

		writeHTMLReply(ctx, 200, "OK", page.outer.generate());
	}

	@Override
	void makeWebPagePost(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page)
			throws ToadletContextClosedException, IOException {
		makeWebPageGet(uri, req, ctx, page);
	}

	@Override
	public boolean isEnabled(ToadletContext ctx) {
		return ctx.isAllowedFullAccess();
	}

	@Override
	boolean requiresValidSession() {
		return false;
	}

	@Override
	public String path() {
		return PATH;
	}
}

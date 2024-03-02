/*
 * WebInterface.java
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

import freenet.clients.http.Toadlet;
import freenet.clients.http.ToadletContainer;
import freenet.pluginmanager.PluginRespirator;
import org.freenetproject.freemail.FreemailPlugin;
import org.freenetproject.freemail.config.Configurator;
import org.freenetproject.freemail.utils.Logger;

import java.util.HashSet;
import java.util.Set;

public class WebInterface {
	static final String COOKIE_NAMESPACE = "Freemail";
	static final String PATH = "/Freemail";

	private static final String FREEMAIL_CATEGORY_NAME = "Freemail.Menu.Name";
	private static final String CATEGORY_TITLE = "Freemail.Menu.Title";

	/**
	 * Holds all the Toadlets that must be unregistered when the web interface terminates
	 */
	private final Set<Toadlet> registeredToadlets = new HashSet<Toadlet>();
	private final ToadletContainer container;
	private final PluginRespirator pluginRespirator;
	private final FreemailPlugin freemail;
	private final LoginManager loginManager;
	private final Configurator config;

	public WebInterface(ToadletContainer container, PluginRespirator pluginRespirator, FreemailPlugin freemail,
			Configurator config) {
		this.container = container;
		this.pluginRespirator = pluginRespirator;
		loginManager = new LoginManager(freemail.getAccountManager(),
		                                pluginRespirator.getSessionManager(COOKIE_NAMESPACE));
		this.freemail = freemail;
		this.config = config;
	}

	public void registerToadlets() {
		//Register our menu
		pluginRespirator.getPageMaker().addNavigationCategory(InboxToadlet.getPath(), FREEMAIL_CATEGORY_NAME, CATEGORY_TITLE, freemail);

		//Register the toadlets that should be visible in the menu
		LogInToadlet loginToadlet = new LogInToadlet(pluginRespirator, freemail.getAccountManager(), loginManager);
		LogOutToadlet logoutToadlet = new LogOutToadlet(pluginRespirator, loginManager);
		InboxToadlet inboxToadlet = new InboxToadlet(freemail.getAccountManager(), pluginRespirator, loginManager);
		OutboxToadlet outboxToadlet = new OutboxToadlet(pluginRespirator, freemail.getAccountManager(), freemail, loginManager);
		NewMessageToadlet newMessageToadlet = new NewMessageToadlet(freemail.getWotConnection(), freemail, pluginRespirator, loginManager);
		InfoToadlet infoToadlet = new InfoToadlet(pluginRespirator, loginManager, freemail.getAccountManager(), config);
		SettingsToadlet settingsToadlet = new SettingsToadlet(pluginRespirator, loginManager, config, container);

		registerToadlet(loginToadlet, FREEMAIL_CATEGORY_NAME, true, "Freemail.LoginToadlet.name", "Freemail.LoginToadlet.title", false);
		registerToadlet(logoutToadlet, FREEMAIL_CATEGORY_NAME, true, "Freemail.LogoutToadlet.name", "Freemail.LogoutToadlet.title", false);
		registerToadlet(inboxToadlet, FREEMAIL_CATEGORY_NAME, true, "Freemail.InboxToadlet.name", "Freemail.InboxToadlet.title", false);
		registerToadlet(outboxToadlet, FREEMAIL_CATEGORY_NAME, true, "Freemail.OutboxToadlet.name", "Freemail.OutboxToadlet.title", false);
		registerToadlet(newMessageToadlet, FREEMAIL_CATEGORY_NAME, true, "Freemail.NewMessageToadlet.name", "Freemail.NewMessageToadlet.title", false);
		registerToadlet(settingsToadlet, FREEMAIL_CATEGORY_NAME, true, "Freemail.SettingsToadlet.name", "Freemail.SettingsToadlet.title", false);
		registerToadlet(infoToadlet, FREEMAIL_CATEGORY_NAME, true, "Freemail.InfoToadlet.name", "Freemail.InfoToadlet.title", false);

		//Toadlets that don't go in the menu
		MessageToadlet messageToadlet = new MessageToadlet(freemail.getAccountManager(), pluginRespirator, loginManager);
		AddAccountToadlet addAccountToadlet = new AddAccountToadlet(pluginRespirator, freemail.getWotConnection(), freemail.getAccountManager(), loginManager);
		registerInvisibleToadlet(messageToadlet, true, false);
		registerInvisibleToadlet(addAccountToadlet, true, false);

		StaticToadlet staticToadlet = new StaticToadlet(pluginRespirator, loginManager);
		staticToadlet.handle(WebInterface.PATH + "/static/css/", "[a-zA-Z0-9]+\\.css",
		                     "/org/freenetproject/freemail/ui/web/css/", "text/css");
		staticToadlet.handle(WebInterface.PATH + "/static/images/svg/", "[a-zA-Z0-9]+\\.svg",
		                     "/org/freenetproject/freemail/ui/web/images/svg/", "image/svg+xml");
		registerInvisibleToadlet(staticToadlet, true, false);
	}

	/**
	 * Register a new menu item with the web interface
	 * @param webPage the web page to add
	 * @param menu the menu to add the page to
	 * @param atFront {@code true} if the page should be added to the front of the path-check queue
	 * @param name the name of the menu item
	 * @param tooltip the tooltip of the menu item
	 * @param fullOnly {@code true} if the item should only be shown if the client has full access
	 */
	private void registerToadlet(WebPage webPage, String menu, boolean atFront, String name, String tooltip, boolean fullOnly) {
		container.register(webPage, menu, webPage.path(), atFront, name, tooltip, fullOnly, webPage);

		synchronized (registeredToadlets) {
			registeredToadlets.add(webPage);
		}
	}

	/**
	 * Register a Toadlet with the web interface
	 * @param webPage the web page to register
	 * @param atFront {@code true} if the page should be added to the front of the path-check queue
	 * @param fullAccessOnly {@code true} if the item should only be shown if the client has full access
	 */
	private void registerInvisibleToadlet(WebPage webPage, boolean atFront, boolean fullAccessOnly) {
		container.register(webPage, null, webPage.path(), atFront, fullAccessOnly);

		synchronized (registeredToadlets) {
			registeredToadlets.add(webPage);
		}
	}

	public void terminate() {
		Logger.debug(this, "Unregistering toadlets");
		synchronized (registeredToadlets) {
			for(Toadlet t : registeredToadlets) {
				container.unregister(t);
			}
			registeredToadlets.clear();
		}

		Logger.debug(this, "Removing navigation category");
		pluginRespirator.getPageMaker().removeNavigationCategory(FREEMAIL_CATEGORY_NAME);
	}
}

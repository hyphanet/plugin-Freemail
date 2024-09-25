/*
 * LogOutToadlet.java
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

import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.api.HTTPRequest;

public class LogOutToadlet extends WebPage {
	public LogOutToadlet(PluginRespirator pluginRespirator, LoginManager loginManager) {
		super(pluginRespirator, loginManager);
	}

	@Override
	public String path() {
		return WebInterface.PATH + "/Logout";
	}

	@Override
	public boolean isEnabled(ToadletContext ctx) {
		return ctx.isAllowedFullAccess() && loginManager.sessionExists(ctx);
	}

	@Override
	HTTPResponse makeWebPageGet(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) {
		loginManager.deleteSession(ctx);
		return new HTTPRedirectResponse(ctx, "Logged out, redirecting to login page", LogInToadlet.getPath());
	}

	@Override
	HTTPResponse makeWebPagePost(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) {
		return makeWebPageGet(uri, req, ctx, page);
	}

	@Override
	boolean requiresValidSession() {
		return false;
	}
}

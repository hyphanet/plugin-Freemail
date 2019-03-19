package org.freenetproject.freemail.ui.web;

import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;
import org.freenetproject.freemail.config.Configurator;
import org.freenetproject.freemail.l10n.FreemailL10n;

import java.net.URI;

public class SettingsToadlet extends WebPage {

	private static final String PATH = WebInterface.PATH + "/Setting";

	private final Configurator config;

	SettingsToadlet(PluginRespirator pluginRespirator, LoginManager loginManager, Configurator config) {
		super(pluginRespirator, loginManager);
		this.config = config;
	}

	@Override
	HTTPResponse makeWebPageGet(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) {
		HTMLNode settingsBox = addInfobox(page.content, FreemailL10n.getString("Freemail.SettingsToadlet.title"));
		return new GenericHTMLResponse(ctx, 200, "OK", page.outer.generate());
	}

	@Override
	HTTPResponse makeWebPagePost(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) {
		return null;
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

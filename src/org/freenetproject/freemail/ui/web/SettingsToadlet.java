package org.freenetproject.freemail.ui.web;

import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContainer;
import freenet.clients.http.ToadletContext;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;
import org.freenetproject.freemail.config.Configurator;
import org.freenetproject.freemail.l10n.FreemailL10n;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

public class SettingsToadlet extends WebPage {

	private static final String PATH = WebInterface.PATH + "/Setting";

	private final Configurator config;

	ToadletContainer toadletContainer;

	SettingsToadlet(PluginRespirator pluginRespirator, LoginManager loginManager, Configurator config,
									ToadletContainer toadletContainer) {
		super(pluginRespirator, loginManager);
		this.config = config;
		this.toadletContainer = toadletContainer;
	}

	@Override
	HTTPResponse makeWebPageGet(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws IOException {
		HTMLNode settingsBox = addInfobox(page.content, FreemailL10n.getString("Freemail.SettingsToadlet.title"));
		addChild(settingsBox, "settings-form",
			 new HashMap<String, String>() {{
			 	put("formPassword", toadletContainer.getFormPassword());
			 	put("smtpBindPort", config.get("smtp_bind_port"));
			 	put("smtpBindAddress", config.get("smtp_bind_address"));
			 	put("imapBindPort", config.get("imap_bind_port"));
			 	put("imapBindAddress", config.get("imap_bind_address"));
		}});
		return new GenericHTMLResponse(ctx, 200, "OK", page.outer.generate());
	}

	@Override
	HTTPResponse makeWebPagePost(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws IOException {
		config.set("smtp_bind_port", NewMessageToadlet.getBucketAsString(req.getPart("smtp-bind-port")));
		config.set("smtp_bind_address", NewMessageToadlet.getBucketAsString(req.getPart("smtp-bind-address")));
		config.set("imap_bind_port", NewMessageToadlet.getBucketAsString(req.getPart("imap-bind-port")));
		config.set("imap_bind_address", NewMessageToadlet.getBucketAsString(req.getPart("imap-bind-address")));

		return makeWebPageGet(uri, req, ctx, page);
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

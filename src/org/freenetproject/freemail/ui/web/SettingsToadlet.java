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

	private static final String PATH = WebInterface.PATH + "/Settings";

	private final Configurator config;

	private final ToadletContainer toadletContainer;

	SettingsToadlet(PluginRespirator pluginRespirator, LoginManager loginManager, Configurator config,
									ToadletContainer toadletContainer) {
		super(pluginRespirator, loginManager);
		this.config = config;
		this.toadletContainer = toadletContainer;
	}

	@Override
	HTTPResponse makeWebPageGet(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws IOException {
		HTMLNode settingsBox = addInfobox(page.content, FreemailL10n.getString("Freemail.SettingsToadlet.title"));

		HashMap<String, String> settings = new HashMap<String, String>();
		settings.put("formPassword", toadletContainer.getFormPassword());
		settings.put("smtpBindPort", config.get("smtp_bind_port"));
		settings.put("smtpBindAddress", config.get("smtp_bind_address"));
		settings.put("imapBindPort", config.get("imap_bind_port"));
		settings.put("imapBindAddress", config.get("imap_bind_address"));

		addChild(settingsBox, "settings-form", settings);
		return new GenericHTMLResponse(ctx, 200, "OK", page.outer.generate());
	}

	@Override
	HTTPResponse makeWebPagePost(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws IOException {
		String smtpPortStr = "";
		String smtpAddressStr = "";
		String imapPortStr = "";
		String imapAddressStr = "";

		smtpPortStr = req.getPartAsStringFailsafe("smtp-bind-port", 5);
		smtpAddressStr = req.getPartAsStringFailsafe("smtp-bind-address", 2000);
		imapPortStr = req.getPartAsStringFailsafe("imap-bind-port", 5);
		imapAddressStr = req.getPartAsStringFailsafe("imap-bind-address", 2000);

		config.set("smtp_bind_address", smtpAddressStr);
		config.set("imap_bind_address", imapAddressStr);

		if (isPort(smtpPortStr))
			config.set("smtp_bind_port", smtpPortStr);
		if (isPort(imapPortStr))
			config.set("imap_bind_port", imapPortStr);

        return new HTTPRedirectResponse(ctx, "", PATH);
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

	private static boolean isPort(String port) {
		try {
			int num = Integer.parseInt(port);

			return (num > 0 && num <= 65535);
		} catch (NumberFormatException e) {
			return false;
		}
	}
}

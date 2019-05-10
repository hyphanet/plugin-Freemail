package org.freenetproject.freemail.ui.web;

import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContainer;
import freenet.clients.http.ToadletContext;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;
import org.freenetproject.freemail.config.Configurator;
import org.freenetproject.freemail.l10n.FreemailL10n;

import javax.naming.SizeLimitExceededException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
		String smtpPortStr = "";
		String smtpAddressStr = "";
		String imapPortStr = "";
		String imapAddressStr = "";

		try {
			smtpPortStr = req.getPartAsStringThrowing("smtp-bind-port", 5);
			smtpAddressStr = req.getPartAsStringThrowing("smtp-bind-address", 2000);
			imapPortStr = req.getPartAsStringThrowing("imap-bind-port", 5);
			imapAddressStr = req.getPartAsStringThrowing("imap-bind-address", 2000);
		} catch (SizeLimitExceededException e) {
			e.printStackTrace();
		}

		config.set("smtp_bind_address", smtpAddressStr);
		config.set("imap_bind_address", imapAddressStr);

		if (isPort(smtpPortStr))
			config.set("smtp_bind_port", smtpPortStr);
		if (isPort(imapPortStr))
			config.set("imap_bind_port", imapPortStr);

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

	private static boolean isURL(String url) {
		try {
			new URL(url).toURI();
			return true;
		} catch (URISyntaxException | MalformedURLException exception) {
			return false;
		}
	}

	private static boolean isPort(String port) {
		try {
			int num = Integer.parseInt(port);

			if(num > 0 && num <= 65535) {
				return true;
			}
		} catch (NumberFormatException e) {
			return false;
		}
		return false;
	}
}

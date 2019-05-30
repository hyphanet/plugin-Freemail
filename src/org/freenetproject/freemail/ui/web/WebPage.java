/*
 * WebPage.java
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

import freenet.clients.http.*;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.HTMLNode;
import freenet.support.MultiValueTable;
import freenet.support.api.Bucket;
import freenet.support.api.HTTPRequest;
import org.freenetproject.freemail.MailMessage;
import org.freenetproject.freemail.l10n.FreemailL10n;
import org.freenetproject.freemail.utils.Logger;
import org.freenetproject.freemail.utils.Timer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class WebPage extends Toadlet implements LinkEnabledCallback {
	private final PageMaker pageMaker;
	final PluginRespirator pluginRespirator;
	final LoginManager loginManager;

	WebPage(PluginRespirator pluginRespirator, LoginManager loginManager) {
		super(null);
		this.pageMaker = pluginRespirator.getPageMaker();
		this.loginManager = loginManager;
		this.pluginRespirator = pluginRespirator;
	}

	abstract HTTPResponse makeWebPageGet(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws IOException;
	abstract HTTPResponse makeWebPagePost(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws IOException;
	abstract boolean requiresValidSession();

	public final void handleMethodGET(URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException {
		if(requiresFullAccess() && !ctx.isAllowedFullAccess()) {
			writeTemporaryRedirect(ctx, "This page requires full access", "/");
			return;
		}

		if(requiresValidSession() && !loginManager.sessionExists(ctx)) {
			writeTemporaryRedirect(ctx, "This page requires a valid session", LogInToadlet.getPath());
			return;
		}

		PageNode page = pageMaker.getPageNode("Freemail", ctx);
		page.addCustomStyleSheet(StaticToadlet.getPath() + "/css/freemail.css");

		Timer pageGeneration = Timer.start();
		HTTPResponse r = makeWebPageGet(uri, req, ctx, page);
		r.writeResponse();
		pageGeneration.log(this, 1, TimeUnit.SECONDS, "Time spent serving get request");
	}

	public final void handleMethodPOST(URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException {
		if(requiresFullAccess() && !ctx.isAllowedFullAccess()) {
			writeTemporaryRedirect(ctx, "This page requires full access", "/");
			return;
		}

		//Check the form password
		String formPassword = pluginRespirator.getNode().clientCore.formPassword;
		String pass = req.getPartAsStringFailsafe("formPassword", formPassword.length());

		if(!pass.equals(formPassword)) {
			writeHTMLReply(ctx, 403, "Forbidden", "Invalid form password.");
			return;
		}

		if(requiresValidSession() && !loginManager.sessionExists(ctx)) {
			writeTemporaryRedirect(ctx, "This page requires a valid session", LogInToadlet.getPath());
			return;
		}

		PageNode page = pageMaker.getPageNode("Freemail", ctx);
		page.addCustomStyleSheet(StaticToadlet.getPath() + "/css/freemail.css");

		Timer pageGeneration = Timer.start();
		HTTPResponse r = makeWebPagePost(uri, req, ctx, page);
		r.writeResponse();

		long timeout = 1;
		TimeUnit unit = TimeUnit.SECONDS;
		if(this instanceof AddAccountToadlet) {
			//Override time threshold since account creation is known to be slow
			//FIXME: Fix account creation instead of hiding the warning
			timeout = 5;
			unit = TimeUnit.MINUTES;
		}
		pageGeneration.log(this, timeout, unit, "Time spent serving post request");
	}

	@Override
	public boolean isEnabled(ToadletContext ctx) {
		//Implement this in the superclass so pages that don't appear
		//in the menu won't have to
		throw new UnsupportedOperationException(
				"Web pages that appear in the menu must override isEnabled(ToadletContext ctx)");
	}

	boolean requiresFullAccess() {
		return true;
	}

	static HTMLNode addInfobox(HTMLNode parent, String title) {
		HTMLNode infobox = parent.addChild("div", "class", "infobox");
		infobox.addChild("div", "class", "infobox-header", title);
		return infobox.addChild("div", "class", "infobox-content");
	}

	static HTMLNode addErrorbox(HTMLNode parent, String title) {
		HTMLNode infobox = parent.addChild("div", "class", "infobox infobox-alert");
		infobox.addChild("div", "class", "infobox-header", title);
		return infobox.addChild("div", "class", "infobox-content");
	}

	/**
	 * Returns the date of the given message, properly formatted for display.
	 * If the message is missing the date header, {@code fallback} is returned.
	 * @param message the message to format the date for
	 * @param fallback the string returned if no date was found
	 * @return the formatted date, or the fallback date
	 */
	static String getMessageDateAsString(MailMessage message, String fallback) {
		Date msgDate = message.getDate();
		if(msgDate != null) {
			DateFormat df = DateFormat.getDateTimeInstance(
					DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault());
			return df.format(msgDate);
		}

		/* Use the raw date header if possible. If it is null
		 * the field will  simply be left blank */
		//TODO: This should probably be removed once getDate() has been tested with real world messages
		String rawDate = message.getFirstHeader("Date");
		if(rawDate != null) {
			Logger.error(WebPage.class, "Displaying raw date: " + rawDate);
			return rawDate;
		}

		return fallback;
	}

	protected void addWoTNotLoadedMessage(HTMLNode parent) {
		HTMLNode errorbox = addErrorbox(parent, FreemailL10n.getString("Freemail.Global.WoTNotLoadedTitle"));
		HTMLNode text = errorbox.addChild("p");
		FreemailL10n.addL10nSubstitution(text, "Freemail.Global.WoTNotLoaded",
				new String[] {"link"},
				new HTMLNode[] {HTMLNode.link("/plugins")});
	}

	void addChild(HTMLNode parent, String templateName, Map<String, String> model) throws IOException {
		try (InputStream stream =
						getClass().getResourceAsStream("/templates/" + templateName + ".html")) {
			ByteArrayOutputStream content = new ByteArrayOutputStream();

			int len;
			byte[] contentBytes = new byte[1024];
			while ((len = stream.read(contentBytes)) != -1)
				content.write(contentBytes, 0, len);

			String template = content.toString(StandardCharsets.UTF_8.name());

			for (Map.Entry<String, String> entry : model.entrySet())
				template = template.replaceAll("\\$\\{" + entry.getKey() + "}", entry.getValue());

			String key;
			while ((key = getL10nKey(template)) != null)
				template = template.replaceAll("#\\{" + key + "}", FreemailL10n.getString(key));

			parent.addChild("%", template);
		}
	}

	private String getL10nKey(String template) {
		Pattern pattern = Pattern.compile("#\\{.*}");
		Matcher matcher = pattern.matcher(template);
		if (matcher.find()) {
			String key = matcher.group();
			return key.substring(2, key.length() - 1);
		}

		return null;
	}

	protected abstract class HTTPResponse {
		public abstract void writeResponse() throws ToadletContextClosedException, IOException;
	}

	protected class GenericHTMLResponse extends HTTPResponse {
		private final ToadletContext ctx;
		private final int returnCode;
		private final String description;
		private final String data;

		protected GenericHTMLResponse(ToadletContext ctx, int returnCode, String description, String data) {
			this.ctx = ctx;
			this.returnCode = returnCode;
			this.description = description;
			this.data = data;
		}

		@Override
		public void writeResponse() throws ToadletContextClosedException, IOException {
			writeHTMLReply(ctx, returnCode, description, data);
		}
	}

	protected class HTTPRedirectResponse extends HTTPResponse {
		private final ToadletContext ctx;
		private final String msg;
		private final String path;

		protected HTTPRedirectResponse(ToadletContext ctx, String msg, String path) {
			this.ctx = ctx;
			this.msg = msg;
			this.path = path;
		}

		@Override
		public void writeResponse() throws ToadletContextClosedException, IOException {
			writeTemporaryRedirect(ctx, msg, path);
		}
	}

	protected class GenericHTTPResponse extends HTTPResponse {
		private final ToadletContext ctx;
		private final int returnCode;
		private final String mime;
		private final String description;
		private final MultiValueTable<String, String> headers;
		private final Bucket data;

		protected GenericHTTPResponse(ToadletContext ctx, int returnCode, String mime, String description,
		                              MultiValueTable<String, String> headers, Bucket data) {
			this.ctx = ctx;
			this.returnCode = returnCode;
			this.mime = mime;
			this.description = description;
			this.headers = headers;
			this.data = data;
		}

		@Override
		public void writeResponse() throws ToadletContextClosedException, IOException {
			writeReply(ctx, returnCode, mime, description, headers, data);
		}
	}
}

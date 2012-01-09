/*
 * NewMessageToadlet.java
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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import freemail.Freemail;
import freemail.FreemailAccount;
import freemail.MailMessage;
import freemail.MessageBank;
import freemail.l10n.FreemailL10n;
import freemail.support.MessageBankTools;
import freemail.utils.Logger;
import freemail.wot.Identity;
import freemail.wot.IdentityMatcher;
import freemail.wot.WoTConnection;
import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.HTMLNode;
import freenet.support.api.Bucket;
import freenet.support.api.HTTPRequest;
import freenet.support.io.ArrayBucket;
import freenet.support.io.BucketTools;

public class NewMessageToadlet extends WebPage {
	private static final String PATH = WebInterface.PATH + "/NewMessage";

	private final WoTConnection wotConnection;
	private final Freemail freemail;

	NewMessageToadlet(WoTConnection wotConnection, Freemail freemail, PluginRespirator pluginRespirator) {
		super(pluginRespirator);
		this.wotConnection = wotConnection;
		this.freemail = freemail;
	}

	@Override
	void makeWebPageGet(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws ToadletContextClosedException, IOException {
		HTMLNode pageNode = page.outer;
		HTMLNode contentNode = page.content;

		String recipient = req.getParam("to");
		if(!recipient.equals("")) {
			Identity identity;
			try {
				identity = wotConnection.getIdentity(recipient, sessionManager.useSession(ctx).getUserID());
			} catch(PluginNotFoundException e) {
				addWoTNotLoadedMessage(contentNode);
				writeHTMLReply(ctx, 200, "OK", pageNode.generate());
				return;
			}
			recipient = identity.getNickname() + "@" + identity.getIdentityID() + ".freemail";
		}

		HTMLNode messageBox = addInfobox(contentNode, FreemailL10n.getString("Freemail.NewMessageToadlet.boxTitle"));
		addMessageForm(messageBox, ctx, recipient, "", "", "");

		writeHTMLReply(ctx, 200, "OK", pageNode.generate());
	}

	@Override
	void makeWebPagePost(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws ToadletContextClosedException, IOException {
		String action = getBucketAsString(req.getPart("action"));
		if("sendMessage".equals(action)) {
			sendMessage(req, ctx, page);
		} else if("reply".equals(action)) {
			createReply(req, ctx, page);
		} else {
			Logger.error(this, "Unknown action requested: " + action);

			String boxTitle = FreemailL10n.getString("Freemail.NewMessageToadlet.unknownActionTitle");
			HTMLNode errorBox = addErrorbox(page.content, boxTitle);
			errorBox.addChild("p", FreemailL10n.getString("Freemail.NewMessageToadlet.unknownAction"));
			errorBox.addChild("p", FreemailL10n.getString("Freemail.NewMessageToadlet.unknownAction", "action", action));

			writeHTMLReply(ctx, 200, "OK", page.outer.generate());
		}
	}

	private void sendMessage(HTTPRequest req, ToadletContext ctx, PageNode page) throws ToadletContextClosedException, IOException {
		//Read list of recipients. Whitespace seems to be the only reasonable way to separate
		//identities, but people will probably use all sorts of characters that can also appear in
		//nicknames, so the matching should be sufficiently fuzzy to handle that
		Set<String> identities = new HashSet<String>();

		Bucket b = req.getPart("to");
		BufferedReader data;
		try {
			data = new BufferedReader(new InputStreamReader(b.getInputStream(), "UTF-8"));
		} catch(UnsupportedEncodingException e) {
			throw new AssertionError();
		} catch(IOException e) {
			throw new AssertionError();
		}

		String line = data.readLine();
		while(line != null) {
			String[] parts = line.split("\\s");
			for(String part : parts) {
				identities.add(part);
			}
			line = data.readLine();
		}

		IdentityMatcher messageSender = new IdentityMatcher(wotConnection);
		Map<String, List<Identity>> matches;
		try {
			EnumSet<IdentityMatcher.MatchMethod> methods = EnumSet.allOf(IdentityMatcher.MatchMethod.class);
			matches = messageSender.matchIdentities(identities, sessionManager.useSession(ctx).getUserID(), methods);
		} catch(PluginNotFoundException e) {
			addWoTNotLoadedMessage(page.content);
			writeHTMLReply(ctx, 200, "OK", page.outer.generate());
			return;
		}

		//Check if there were any unknown or ambiguous identities
		List<String> failedRecipients = new LinkedList<String>();
		for(Map.Entry<String, List<Identity>> entry : matches.entrySet()) {
			if(entry.getValue().size() != 1) {
				failedRecipients.add(entry.getKey());
			}
		}

		if(failedRecipients.size() != 0) {
			//TODO: Handle this properly
			HTMLNode pageNode = page.outer;
			HTMLNode contentNode = page.content;

			HTMLNode errorBox = addErrorbox(contentNode, FreemailL10n.getString("Freemail.NewMessageToadlet.ambigiousIdentitiesTitle"));
			HTMLNode errorPara = errorBox.addChild("p", FreemailL10n.getString("Freemail.NewMessageToadlet.ambigiousIdentities", "count", "" + failedRecipients.size()));
			HTMLNode identityList = errorPara.addChild("ul");
			for(String s : failedRecipients) {
				identityList.addChild("li", s);
			}

			writeHTMLReply(ctx, 200, "OK", pageNode.generate());
			return;
		}

		//Build message header
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		String dateHeader = "Date: " + sdf.format(new Date()) + "\r\n";

		FreemailAccount account = freemail.getAccountManager().getAccount(sessionManager.useSession(ctx).getUserID());
		//TODO: Check for newlines etc.
		Bucket messageHeader = new ArrayBucket(
				("Subject: " + getBucketAsString(req.getPart("subject")) + "\r\n" +
				"From: " + account.getNickname() + " <" + account.getNickname() + "@" + account.getDomain() + ">\r\n" +
				"To: " + getBucketAsString(b) + "\r\n" +
				dateHeader +
				"Message-ID: <" + UUID.randomUUID() + "@" + account.getDomain() + ">\r\n" +
				"\r\n").getBytes("UTF-8"));
		Bucket messageText = req.getPart("message-text");

		//Now combine them in a single bucket
		Bucket message = new ArrayBucket();
		OutputStream messageOutputStream = message.getOutputStream();
		BucketTools.copyTo(messageHeader, messageOutputStream, -1);
		BucketTools.copyTo(messageText, messageOutputStream, -1);
		messageOutputStream.close();

		List<Identity> recipients = new LinkedList<Identity>();
		for(List<Identity> identityList : matches.values()) {
			assert (identityList.size() == 1);
			recipients.add(identityList.get(0));
		}

		account.getMessageHandler().sendMessage(recipients, message);
		message.free();

		HTMLNode pageNode = page.outer;
		HTMLNode contentNode = page.content;

		HTMLNode infobox = addInfobox(contentNode, FreemailL10n.getString("Freemail.NewMessageToadlet.messageQueuedTitle"));
		infobox.addChild("p", FreemailL10n.getString("Freemail.NewMessageToadlet.messageQueued"));

		writeHTMLReply(ctx, 200, "OK", pageNode.generate());
	}

	private void createReply(HTTPRequest req, ToadletContext ctx, PageNode page) throws ToadletContextClosedException, IOException {
		HTMLNode pageNode = page.outer;
		HTMLNode contentNode = page.content;

		String folder = getBucketAsString(req.getPart("folder"));
		String message = getBucketAsString(req.getPart("message"));

		MessageBank mb = MessageBankTools.getMessageBank(getFreemailAccount(ctx).getMessageBank(), folder);
		MailMessage msg = MessageBankTools.getMessage(mb, Integer.parseInt(message));
		msg.readHeaders();

		String recipient = msg.getFirstHeader("From");
		String inReplyTo = msg.getFirstHeader("message-id");

		String subject = msg.getFirstHeader("Subject");
		if(!subject.toLowerCase().startsWith("re: ")) {
			subject = "Re: " + subject;
		}

		StringBuilder body = new StringBuilder();

		//First we have to read past the header
		String line = msg.readLine();
		while((line != null) && (!line.equals(""))) {
			line = msg.readLine();
		}

		//Now add the actual message content
		line = msg.readLine();
		while(line != null) {
			body.append(">" + line + "\r\n");
			line = msg.readLine();
		}
		msg.closeStream();

		HTMLNode messageBox = addInfobox(contentNode, FreemailL10n.getString("Freemail.NewMessageToadlet.boxTitle"));
		addMessageForm(messageBox, ctx, recipient, subject, body.toString(), inReplyTo);

		writeHTMLReply(ctx, 200, "OK", pageNode.generate());
	}

	private void addMessageForm(HTMLNode parent, ToadletContext ctx, String recipient, String subject, String body, String inReplyTo) {
		assert (recipient != null);
		assert (subject != null);
		assert (body != null);

		HTMLNode messageForm = ctx.addFormChild(parent, path(), "newMessage");
		messageForm.addChild("input", new String[] {"type",   "name",   "value"},
		                              new String[] {"hidden", "action", "sendMessage"});
		messageForm.addChild("input", new String[] {"type",   "name",      "value"},
		                              new String[] {"hidden", "inReplyTo", inReplyTo});

		HTMLNode recipientBox = addInfobox(messageForm, FreemailL10n.getString("Freemail.NewMessageToadlet.to"));
		recipientBox.addChild("input", new String[] {"name", "type", "size", "value"},
		                               new String[] {"to",   "text", "100",  recipient});

		HTMLNode subjectBox = addInfobox(messageForm, FreemailL10n.getString("Freemail.NewMessageToadlet.subject"));
		subjectBox.addChild("input", new String[] {"name",    "type", "size", "value"},
		                             new String[] {"subject", "text", "100",  subject});

		HTMLNode messageBodyBox = addInfobox(messageForm, FreemailL10n.getString("Freemail.NewMessageToadlet.body"));
		messageBodyBox.addChild("textarea", new String[] {"name",         "cols", "rows", "class"},
		                                    new String[] {"message-text", "100",  "30",   "message-text"},
		                                    body);

		messageForm.addChild("input", new String[] {"type",   "value"},
		                              new String[] {"submit", FreemailL10n.getString("Freemail.NewMessageToadlet.send")});
	}

	private String getBucketAsString(Bucket b) {
		InputStream is;
		try {
			is = b.getInputStream();
		} catch(IOException e1) {
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		byte[] buffer = new byte[1024];
		while(true) {
			int read;
			try {
				read = is.read(buffer);
			} catch(IOException e) {
				return null;
			}
			if(read == -1) {
				break;
			}

			baos.write(buffer, 0, read);
		}

		try {
			return new String(baos.toByteArray(), "UTF-8");
		} catch(UnsupportedEncodingException e) {
			return null;
		}
	}

	private FreemailAccount getFreemailAccount(ToadletContext ctx) {
		return freemail.getAccountManager().getAccount(sessionManager.useSession(ctx).getUserID());
	}

	@Override
	public boolean isEnabled(ToadletContext ctx) {
		return sessionManager.sessionExists(ctx);
	}

	@Override
	public String path() {
		return PATH;
	}

	static String getPath() {
		return PATH;
	}

	@Override
	boolean requiresValidSession() {
		return true;
	}
}

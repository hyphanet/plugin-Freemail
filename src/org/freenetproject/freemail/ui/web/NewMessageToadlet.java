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

package org.freenetproject.freemail.ui.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.freenetproject.freemail.Freemail;
import org.freenetproject.freemail.FreemailAccount;
import org.freenetproject.freemail.MailMessage;
import org.freenetproject.freemail.MessageBank;
import org.freenetproject.freemail.l10n.FreemailL10n;
import org.freenetproject.freemail.support.MessageBankTools;
import org.freenetproject.freemail.utils.Logger;
import org.freenetproject.freemail.utils.Timer;
import org.freenetproject.freemail.wot.Identity;
import org.freenetproject.freemail.wot.IdentityMatcher;
import org.freenetproject.freemail.wot.WoTConnection;

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
import freenet.support.io.Closer;

public class NewMessageToadlet extends WebPage {
	private static final String PATH = WebInterface.PATH + "/NewMessage";
	private static final String SEND_COPY_FOLDER = "Sent";

	private final WoTConnection wotConnection;
	private final Freemail freemail;

	NewMessageToadlet(WoTConnection wotConnection, Freemail freemail, PluginRespirator pluginRespirator,
	                  LoginManager loginManager) {
		super(pluginRespirator, loginManager);
		this.wotConnection = wotConnection;
		this.freemail = freemail;
	}

	@Override
	void makeWebPageGet(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws ToadletContextClosedException, IOException {
		HTMLNode pageNode = page.outer;
		HTMLNode contentNode = page.content;

		List<String> recipients = new LinkedList<String>();
		String recipient = req.getParam("to");
		if(!recipient.equals("")) {
			Identity identity;
			try {
				identity = wotConnection.getIdentity(recipient, loginManager.getSession(ctx).getUserID());
			} catch(PluginNotFoundException e) {
				addWoTNotLoadedMessage(contentNode);
				writeHTMLReply(ctx, 200, "OK", pageNode.generate());
				return;
			}
			recipients.add(identity.getNickname() + "@" + identity.getIdentityID() + ".freemail");
		} else {
			recipients.add("");
		}

		HTMLNode messageBox = addInfobox(contentNode, FreemailL10n.getString("Freemail.NewMessageToadlet.boxTitle"));
		addMessageForm(messageBox, ctx, recipients, "", bucketFromString(""), Collections.<String>emptyList());

		writeHTMLReply(ctx, 200, "OK", pageNode.generate());
	}

	@Override
	void makeWebPagePost(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws ToadletContextClosedException, IOException {
		if(req.isPartSet("sendMessage")) {
			sendMessage(req, ctx, page);
			return;
		}

		if(req.isPartSet("reply")) {
			createReply(req, ctx, page);
			return;
		}

		List<String> recipients = new LinkedList<String>();
		for(int i = 0; req.isPartSet("to" + i); i++) {
			recipients.add(getBucketAsString(req.getPart("to" + i)));
		}
		Logger.debug(this, "Found " + recipients.size() + " recipients");

		String subject = getBucketAsString(req.getPart("subject"));
		Bucket body = req.getPart("message-text");

		List<String> extraHeaders = readExtraHeaders(req);

		//Because the button is an image we get x/y coordinates as addRcpt.x and addRcpt.y
		if(req.isPartSet("addRcpt.x") && req.isPartSet("addRcpt.y")) {
			Logger.debug(this, "Adding new recipient");

			recipients.add("");
			HTMLNode pageNode = page.outer;
			HTMLNode contentNode = page.content;

			HTMLNode messageBox = addInfobox(contentNode, FreemailL10n.getString("Freemail.NewMessageToadlet.boxTitle"));
			addMessageForm(messageBox, ctx, recipients, subject, body, extraHeaders);

			writeHTMLReply(ctx, 200, "OK", pageNode.generate());
			return;
		}

		for(int i = 0; i < recipients.size(); i++) {
			//Same as above
			if(req.isPartSet("removeRcpt" + i + ".x") && req.isPartSet("removeRcpt" + i + ".y")) {
				Logger.debug(this, "Removing recipient " + i);

				recipients.remove(i);
				HTMLNode pageNode = page.outer;
				HTMLNode contentNode = page.content;

				HTMLNode messageBox = addInfobox(contentNode, FreemailL10n.getString("Freemail.NewMessageToadlet.boxTitle"));
				addMessageForm(messageBox, ctx, recipients, subject, body, extraHeaders);

				writeHTMLReply(ctx, 200, "OK", pageNode.generate());
				return;
			}
		}

		String parts = "";
		for(String part : req.getParts()) {
			parts += part + "=\"" + getBucketAsString(req.getPart(part)) + "\" ";
		}
		Logger.error(this, "Unknown action requested. Set parts: " + parts);

		String boxTitle = FreemailL10n.getString("Freemail.NewMessageToadlet.unknownActionTitle");
		HTMLNode errorBox = addErrorbox(page.content, boxTitle);
		errorBox.addChild("p", FreemailL10n.getString("Freemail.NewMessageToadlet.unknownAction"));

		writeHTMLReply(ctx, 200, "OK", page.outer.generate());
	}

	private boolean copyMessageToSentFolder(Bucket message, MessageBank parentMb) {
		MessageBank target = parentMb.makeSubFolder(SEND_COPY_FOLDER);
		if(target == null) {
			target = parentMb.getSubFolder(SEND_COPY_FOLDER);
		}

		//If target still is null it couldn't be created
		if(target == null) {
			return false;
		}

		//Write a copy of the message
		MailMessage msg = target.createMessage();
		PrintStream ps = null;
		try {
			ps = msg.getRawStream();
			BucketTools.copyTo(message, ps, message.size());
		} catch (IOException e) {
			Logger.error(this, "Caugth exception while copying message to sent folder", e);
			Closer.close(ps);
			msg.cancel();
			return false;
		}

		Closer.close(ps);
		msg.commit();

		return true;
	}

	private void sendMessage(HTTPRequest req, ToadletContext ctx, PageNode page) throws ToadletContextClosedException, IOException {
		//FIXME: Consider how to handle duplicate recipients
		Timer sendMessageTimer = Timer.start();

		Timer recipientHandling = sendMessageTimer.startSubTimer();
		Map<String, String> recipients = new HashMap<String, String>();
		for(int i = 0; req.isPartSet("to" + i); i++) {
			String recipient = getBucketAsString(req.getPart("to" + i));
			if(recipient.equals("")) {
				//Skip empty fields
				continue;
			}

			//Strip parts if needed
			String address = recipient;
			if(address.contains("<") && address.contains(">")) {
				address = address.substring(address.indexOf("<") + 1, address.indexOf(">"));
			}

			recipients.put(address, recipient);
		}
		recipientHandling.log(this, "Time spent handling " + recipients.size() + " recipients");

		Timer identityMatching = sendMessageTimer.startSubTimer();
		IdentityMatcher messageSender = new IdentityMatcher(wotConnection);
		Map<String, List<Identity>> matches;
		try {
			EnumSet<IdentityMatcher.MatchMethod> methods = EnumSet.allOf(IdentityMatcher.MatchMethod.class);
			matches = messageSender.matchIdentities(recipients.keySet(), loginManager.getSession(ctx).getUserID(), methods);
		} catch(PluginNotFoundException e) {
			addWoTNotLoadedMessage(page.content);
			writeHTMLReply(ctx, 200, "OK", page.outer.generate());
			sendMessageTimer.log(this, 1, TimeUnit.SECONDS, "Time spent sending message (WoT not loaded)");
			return;
		}
		identityMatching.log(this, "Time spent matching identities");

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
			sendMessageTimer.log(this, 1, TimeUnit.SECONDS, "Time spent sending message (with failed recipient)");
			return;
		}

		//Build message header
		StringBuilder header = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		FreemailAccount account = freemail.getAccountManager().getAccount(loginManager.getSession(ctx).getUserID());

		//TODO: Check for newlines etc.
		for(String recipient : recipients.values()) {
			//Use the values so we get what the user typed
			header.append("To: " + recipient + "\r\n");
		}
		header.append("Subject: " + getBucketAsString(req.getPart("subject")) + "\r\n");
		header.append("Date: " + sdf.format(new Date()) + "\r\n");
		header.append("From: " + account.getNickname() + " <" + account.getNickname() + "@" + account.getDomain() + ">\r\n");
		header.append("Message-ID: <" + UUID.randomUUID() + "@" + account.getDomain() + ">\r\n");

		//Add extra headers from request. Very little checking is done here since we want flexibility, and anything
		//that can be added here could also be sent using the SMTP server, so security should not be an issue.
		List<String> extraHeaders = readExtraHeaders(req);
		for(String extraHeader : extraHeaders) {
			if(extraHeader.matches("[^\\u0000-\\u007F]")) {
				throw new IllegalArgumentException("Header contains 8bit character(s)");
			}
			header.append(extraHeader + "\r\n");
		}
		header.append("\r\n");

		Bucket messageHeader = new ArrayBucket(header.toString().getBytes("UTF-8"));
		Bucket messageText = req.getPart("message-text");

		//Now combine them in a single bucket
		Bucket message = new ArrayBucket();
		OutputStream messageOutputStream = message.getOutputStream();
		BucketTools.copyTo(messageHeader, messageOutputStream, -1);
		BucketTools.copyTo(messageText, messageOutputStream, -1);
		messageOutputStream.close();

		List<Identity> identities = new LinkedList<Identity>();
		for(List<Identity> identityList : matches.values()) {
			assert (identityList.size() == 1);
			identities.add(identityList.get(0));
		}

		copyMessageToSentFolder(message, account.getMessageBank());

		account.getMessageHandler().sendMessage(identities, message);
		message.free();

		HTMLNode pageNode = page.outer;
		HTMLNode contentNode = page.content;

		HTMLNode infobox = addInfobox(contentNode, FreemailL10n.getString("Freemail.NewMessageToadlet.messageQueuedTitle"));
		infobox.addChild("p", FreemailL10n.getString("Freemail.NewMessageToadlet.messageQueued"));

		writeHTMLReply(ctx, 200, "OK", pageNode.generate());

		sendMessageTimer.log(this, 1, TimeUnit.SECONDS, "Time spent sending message");
	}

	private void createReply(HTTPRequest req, ToadletContext ctx, PageNode page) throws ToadletContextClosedException, IOException {
		HTMLNode pageNode = page.outer;
		HTMLNode contentNode = page.content;

		String folder = getBucketAsString(req.getPart("folder"));
		String message = getBucketAsString(req.getPart("message"));

		Logger.debug(this, "Replying to message " + message + " in folder " + folder);

		MessageBank mb = MessageBankTools.getMessageBank(getFreemailAccount(ctx), folder);
		MailMessage msg = MessageBankTools.getMessage(mb, Integer.parseInt(message));
		msg.readHeaders();

		String recipient = msg.getFirstHeader("From");

		String subject = msg.getFirstHeader("Subject");
		if(!subject.toLowerCase().startsWith("re: ")) {
			subject = "Re: " + subject;
		}

		StringBuilder body = new StringBuilder();

		try {
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
		} finally {
			msg.closeStream();
		}

		List<String> extraHeaders = readExtraHeaders(req);
		extraHeaders.add("In-Reply-To: " + msg.getFirstHeader("message-id"));

		//Add the references header. This uses folding white space between each
		//reference as a simple way of avoiding long lines.
		String references = "";
		if(msg.getFirstHeader("References") != null) {
			references += msg.getFirstHeader("References");
		}
		references += msg.getFirstHeader("message-id");

		extraHeaders.add("References:");
		for(String part : references.split(" ")) {
			extraHeaders.add(" " + part);
		}

		HTMLNode messageBox = addInfobox(contentNode, FreemailL10n.getString("Freemail.NewMessageToadlet.boxTitle"));
		addMessageForm(messageBox, ctx, Collections.singletonList(recipient), subject,
		               bucketFromString(body.toString()), extraHeaders);

		writeHTMLReply(ctx, 200, "OK", pageNode.generate());
	}

	/**
	 * @param headers Contains a list of headers that should be added to the final message
	 */
	private void addMessageForm(HTMLNode parent, ToadletContext ctx, List<String> recipients, String subject,
	                            Bucket body, List<String> headers) {
		assert (parent != null);
		assert (ctx != null);
		assert (recipients != null);
		assert (subject != null);
		assert (body != null);
		assert (headers != null);

		HTMLNode messageForm = ctx.addFormChild(parent, path(), "newMessage");

		//Add the extra headers as hidden fields
		int i = 0;
		for(String header : headers) {
			messageForm.addChild("input", new String[] {"type",   "name",            "value"},
			                              new String[] {"hidden", "extraHeader" + i, header});
			i++;
		}

		HTMLNode recipientBox = addInfobox(messageForm, FreemailL10n.getString("Freemail.NewMessageToadlet.to"));

		//Add one field per recipient
		int recpNum = 0;
		Iterator<String> recipientsIt = recipients.iterator();
		while(recipientsIt.hasNext()) {
			String recipient = recipientsIt.next();

			HTMLNode recipientDiv = recipientBox.addChild("div");
			recipientDiv.addChild("input", new String[] {"name",         "type", "size", "value"},
			                               new String[] {"to" + recpNum, "text", "100",  recipient});

			if(recipientsIt.hasNext()) {
				String buttonName = "removeRcpt" + recpNum;
				recipientDiv.addChild("input", new String[] {"type",  "name",     "class",       "src"},
				                               new String[] {"image", buttonName, "removeRcpt",  "/Freemail/static/images/svg/minus.svg"});
			} else {
				recipientDiv.addChild("input", new String[] {"type",  "name",     "class",   "src"},
				                               new String[] {"image", "addRcpt",  "addRcpt", "/Freemail/static/images/svg/plus.svg"});
			}
			recpNum++;
		}

		HTMLNode subjectBox = addInfobox(messageForm, FreemailL10n.getString("Freemail.NewMessageToadlet.subject"));
		subjectBox.addChild("input", new String[] {"name",    "type", "size", "value"},
		                             new String[] {"subject", "text", "100",  subject});

		HTMLNode messageBodyBox = addInfobox(messageForm, FreemailL10n.getString("Freemail.NewMessageToadlet.body"));
		messageBodyBox.addChild("textarea", new String[] {"name",         "cols", "rows", "class"},
		                                    new String[] {"message-text", "100",  "30",   "message-text"},
		                                    getBucketAsString(body));

		String sendText = FreemailL10n.getString("Freemail.NewMessageToadlet.send");
		messageForm.addChild("input", new String[] {"type",   "name",        "value"},
		                              new String[] {"submit", "sendMessage", sendText});
	}

	/**
	 * Returns the contents of the {@code Bucket} as a {@code String}. {@code null} is returned if b is {@code null} or
	 * if the JVM doesn't support the UTF-8 encoding.
	 * @param b the bucket to read
	 * @return the contents of the {@code Bucket} as a {@code String}
	 */
	private String getBucketAsString(Bucket b) {
		if(b == null) {
			return null;
		}

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
		return freemail.getAccountManager().getAccount(loginManager.getSession(ctx).getUserID());
	}

	@Override
	public boolean isEnabled(ToadletContext ctx) {
		return ctx.isAllowedFullAccess() && loginManager.sessionExists(ctx);
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

	private Bucket bucketFromString(String data) {
		return new ArrayBucket(data.getBytes());
	}

	private List<String> readExtraHeaders(HTTPRequest req) {
		List<String> extraHeaders = new LinkedList<String>();
		for(int i = 0;; i++) {
			String header = getBucketAsString(req.getPart("extraHeader" + i));
			if(header == null) {
				break;
			}

			extraHeaders.add(header);
		}

		return extraHeaders;
	}
}

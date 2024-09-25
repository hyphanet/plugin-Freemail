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

import java.io.BufferedReader;
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
import java.util.Locale;
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
import org.freenetproject.freemail.utils.EmailAddress;
import org.freenetproject.freemail.utils.Logger;
import org.freenetproject.freemail.utils.Timer;
import org.freenetproject.freemail.wot.Identity;
import org.freenetproject.freemail.wot.IdentityMatcher;
import org.freenetproject.freemail.wot.WoTConnection;

import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
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
	HTTPResponse makeWebPageGet(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) {
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
				return new GenericHTMLResponse(ctx, 200, "OK", pageNode.generate());
			}
			recipients.add(identity.getNickname() + "@" + identity.getIdentityID() + ".freemail");
		} else {
			recipients.add("");
		}

		HTMLNode messageBox = addInfobox(contentNode, FreemailL10n.getString("Freemail.NewMessageToadlet.boxTitle"));
		addMessageForm(messageBox, ctx, recipients, "", bucketFromString(""), Collections.<String>emptyList());

		return new GenericHTMLResponse(ctx, 200, "OK", pageNode.generate());
	}

	@Override
	HTTPResponse makeWebPagePost(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws IOException {
		if(req.isPartSet("sendMessage")) {
			return sendMessage(req, ctx, page);
		}

		if(req.isPartSet("reply")) {
			return createReply(req, ctx, page);
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

			return new GenericHTMLResponse(ctx, 200, "OK", pageNode.generate());
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

				return new GenericHTMLResponse(ctx, 200, "OK", pageNode.generate());
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

		return new GenericHTMLResponse(ctx, 200, "OK", page.outer.generate());
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

		msg.flags.setSeen();
		msg.commit();

		return true;
	}

	private HTTPResponse sendMessage(HTTPRequest req, ToadletContext ctx, PageNode page) throws IOException {
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

			//Split the address into parts
			String name = "";
			String address = recipient;
			if(recipient.contains("<") && recipient.contains(">")) {
				name = recipient.substring(0, recipient.indexOf("<"));
				name = name.trim();

				address = recipient.substring(recipient.indexOf("<") + 1, recipient.indexOf(">"));
				address = address.trim();
			}
			String localPart = address.split("@", 2)[0];
			String domainPart = address.split("@", 2)[1];

			//Handle non-ascii characters
			name = MailMessage.encodeHeader(name);
			if(localPart.matches(".*[^\\u0000-\\u007F]+.*")) {
				//Allow this due to earlier bugs, but drop the non-ascii
				//characters. We can do this since we don't care about the
				//local part anyway
				localPart = EmailAddress.cleanLocalPart(localPart);
				if(localPart.equals("")) {
					localPart = "mail";
				}
			}
			//If the domain part has non-ascii characters we won't find any
			//matches, so handle it that way

			String checkedAddress = localPart + "@" + domainPart;
			String checkedRecipient;
			if(name.equals("")) {
				checkedRecipient = checkedAddress;
			} else {
				checkedRecipient = name + " <" + checkedAddress + ">";
			}
			recipients.put(checkedAddress, checkedRecipient);
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
			sendMessageTimer.log(this, 1, TimeUnit.SECONDS, "Time spent sending message (WoT not loaded)");
			return new GenericHTMLResponse(ctx, 200, "OK", page.outer.generate());
		}
		identityMatching.log(this, "Time spent matching identities");

		//Check if there were any unknown or ambiguous identities
		List<String> failedRecipients = new LinkedList<String>();
		List<Identity> knownRecipients = new LinkedList<Identity>();
		for(Map.Entry<String, List<Identity>> entry : matches.entrySet()) {
			if(entry.getValue().size() == 1)
				knownRecipients.add(entry.getValue().get(0));
			else
				failedRecipients.add(entry.getKey());
		}

		HTMLNode pageNode = page.outer;
		HTMLNode contentNode = page.content;

		if(!failedRecipients.isEmpty()) {
			// TODO: Handle this properly
			HTMLNode errorBox = addErrorbox(contentNode,
				 FreemailL10n.getString("Freemail.NewMessageToadlet.ambigiousIdentitiesTitle"));
			HTMLNode errorPara = errorBox.addChild("p",
				 FreemailL10n.getString("Freemail.NewMessageToadlet.ambigiousIdentities",
						"count", "" + failedRecipients.size()));
			HTMLNode identityList = errorPara.addChild("ul");
			for(String s : failedRecipients) {
				identityList.addChild("li", s);
			}

			sendMessageTimer.log(this, 1, TimeUnit.SECONDS,
				 "Time spent sending message (with failed recipient)");

			if (knownRecipients.isEmpty())
				return new GenericHTMLResponse(ctx, 200, "OK", pageNode.generate());
		}

		//Build message header
		StringBuilder header = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z", Locale.ROOT);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		FreemailAccount account = freemail.getAccountManager().getAccount(loginManager.getSession(ctx).getUserID());

		//TODO: Check for newlines etc.
		for(String recipient : recipients.values()) {
			//Use the values so we get what the user typed
			header.append("To: " + recipient + "\r\n");
		}

		String local = EmailAddress.cleanLocalPart(account.getNickname());
		if(local.length() == 0) {
			local = "mail";
		}
		header.append("From: " + MailMessage.encodeHeader(account.getNickname())
				+ " <" + local + "@" + account.getDomain() + ">" + "\r\n");

		header.append("Subject: " + MailMessage.encodeHeader(getBucketAsString(req.getPart("subject"))) + "\r\n");
		header.append("Date: " + sdf.format(new Date()) + "\r\n");
		header.append("Message-ID: <" + UUID.randomUUID() + "@" + account.getDomain() + ">\r\n");
		header.append("Content-Type: text/plain; charset=UTF-8\r\n");
		header.append("Content-Transfer-Encoding: quoted-printable\r\n");

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
		BucketTools.copyTo(messageText, new MailMessage.EncodingOutputStream(messageOutputStream), -1);
		messageOutputStream.close();

		copyMessageToSentFolder(message, account.getMessageBank());

		account.getMessageHandler().sendMessage(knownRecipients, message);
		message.free();

		HTMLNode infobox = addInfobox(contentNode, FreemailL10n.getString("Freemail.NewMessageToadlet.messageQueuedTitle"));
		infobox.addChild("p", FreemailL10n.getString("Freemail.NewMessageToadlet.messageQueued"));

		sendMessageTimer.log(this, 1, TimeUnit.SECONDS, "Time spent sending message");

		return new GenericHTMLResponse(ctx, 200, "OK", pageNode.generate());
	}

	private HTTPResponse createReply(HTTPRequest req, ToadletContext ctx, PageNode page) throws IOException {
		HTMLNode pageNode = page.outer;
		HTMLNode contentNode = page.content;

		String folder = getBucketAsString(req.getPart("folder"));
		String message = getBucketAsString(req.getPart("message"));

		Logger.debug(this, "Replying to message " + message + " in folder " + folder);

		MessageBank mb = MessageBankTools.getMessageBank(getFreemailAccount(ctx), folder);
		MailMessage msg = MessageBankTools.getMessage(mb, Integer.parseInt(message));
		msg.readHeaders();

		String recipient;
		try {
			recipient = MailMessage.decodeHeader(msg.getFirstHeader("From"));
		} catch(UnsupportedEncodingException e) {
			recipient = msg.getFirstHeader("From");
		}

		String subject;
		try {
			subject = MailMessage.decodeHeader(msg.getFirstHeader("Subject"));
		} catch(UnsupportedEncodingException e) {
			subject = msg.getFirstHeader("Subject");
		}
		if(!subject.toLowerCase(Locale.ROOT).startsWith("re: ")) {
			subject = "Re: " + subject;
		}

		StringBuilder body = new StringBuilder();
		BufferedReader bodyReader = msg.getBodyReader();
		try {
			String line = bodyReader.readLine();
			while(line != null) {
				body.append(">" + line + "\r\n");
				line = bodyReader.readLine();
			}
		} finally {
			bodyReader.close();
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

		return new GenericHTMLResponse(ctx, 200, "OK", pageNode.generate());
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
		try {
			return new ArrayBucket(data.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			//JVMs are required to support UTF-8, so we can assume it is always available
			throw new AssertionError("JVM doesn't support UTF-8 charset");
		}
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

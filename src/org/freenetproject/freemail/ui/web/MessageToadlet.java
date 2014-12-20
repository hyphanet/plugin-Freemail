/*
 * MessageToadlet.java
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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.freenetproject.freemail.AccountManager;
import org.freenetproject.freemail.FreemailAccount;
import org.freenetproject.freemail.MailMessage;
import org.freenetproject.freemail.MessageBank;
import org.freenetproject.freemail.l10n.FreemailL10n;
import org.freenetproject.freemail.support.MessageBankTools;
import org.freenetproject.freemail.utils.Logger;

import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;

public class MessageToadlet extends WebPage {
	private static final String PATH = WebInterface.PATH + "/Message";

	private final AccountManager accountManager;

	MessageToadlet(AccountManager accountManager, PluginRespirator pluginRespirator, LoginManager loginManager) {
		super(pluginRespirator, loginManager);
		this.accountManager = accountManager;
	}

	@Override
	HTTPResponse makeWebPageGet(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) {
		HTMLNode pageNode = page.outer;
		HTMLNode contentNode = page.content;

		HTMLNode container = contentNode.addChild("div", "class", "container");

		//Add the list of folders
		HTMLNode folderList = container.addChild("div", "class", "folderlist");

		String identity = loginManager.getSession(ctx).getUserID();
		FreemailAccount account = accountManager.getAccount(identity);
		MessageBank topLevelMessageBank = account.getMessageBank();
		addMessageBank(folderList, topLevelMessageBank, "inbox");

		//Add the message
		String folderName = req.getParam("folder", "inbox");
		MessageBank messageBank = MessageBankTools.getMessageBank(account, folderName);

		int messageUid;
		try {
			messageUid = Integer.parseInt(req.getParam("uid"));
		} catch(NumberFormatException e) {
			Logger.error(this, "Got invalid uid: " + req.getParam("uid"));
			messageUid = 0;
		}
		MailMessage msg = MessageBankTools.getMessage(messageBank, messageUid);

		if(msg == null) {
			/* FIXME: L10n */
			HTMLNode infobox = addErrorbox(container, "Message doesn't exist");
			infobox.addChild("p", "The message you requested doesn't exist");
			return new GenericHTMLResponse(ctx, 200, "OK", pageNode.generate());
		}

		HTMLNode messageNode = container.addChild("div", "class", "message");

		addMessageButtons(ctx, messageNode, folderName, messageUid);
		addMessageHeaders(messageNode, msg);
		addMessageContents(messageNode, msg);

		//Mark message as read
		if(!msg.flags.isSeen()) {
			msg.flags.set("\\seen", true);
			msg.storeFlags();
		}

		return new GenericHTMLResponse(ctx, 200, "OK", pageNode.generate());
	}

	@Override
	HTTPResponse makeWebPagePost(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) {
		return makeWebPageGet(uri, req, ctx, page);
	}

	private void addMessageButtons(ToadletContext ctx, HTMLNode parent, String folderName, int uid) {
		HTMLNode buttonBox = parent.addChild("div", "class", "message-buttons");

		//Add reply button
		HTMLNode replyForm = ctx.addFormChild(buttonBox, NewMessageToadlet.getPath(), "reply");
		replyForm.addChild("input", new String[] {"type",   "name",   "value"},
		                            new String[] {"hidden", "folder", folderName});
		replyForm.addChild("input", new String[] {"type",   "name",    "value"},
		                            new String[] {"hidden", "message", "" + uid});

		String replyText = FreemailL10n.getString("Freemail.MessageToadlet.reply");
		replyForm.addChild("input", new String[] {"type",   "name",  "value"},
		                            new String[] {"submit", "reply", replyText});
	}

	private void addMessageHeaders(HTMLNode messageNode, MailMessage message) {
		HTMLNode headerBox = messageNode.addChild("div", "class", "message-headers");

		try {
			message.readHeaders();
		} catch(IOException e) {
			/* FIXME: L10n */
			Logger.error(this, "Caugth IOException reading headers for " + message);
			headerBox.addChild("p", "There was a problem reading the message headers");
			return;
		}

		HTMLNode fromPara = headerBox.addChild("p");
		fromPara.addChild("strong", "From:");
		try {
			String from = MailMessage.decodeHeader(message.getFirstHeader("from"));
			if(from == null) {
				from = FreemailL10n.getString("Freemail.MessageToadlet.fromMissing");
			}

			fromPara.addChild("#", " " + from);
		} catch (UnsupportedEncodingException e1) {
			fromPara.addChild("#", " " + message.getFirstHeader("from"));
		}

		for(String header : new String[] {"To", "CC", "BCC"}) {
			for(String recipient : message.getHeadersByName(header)) {
				HTMLNode toPara = headerBox.addChild("p");
				toPara.addChild("strong", header + ":");
				try {
					toPara.addChild("#", " " + MailMessage.decodeHeader(recipient));
				} catch (UnsupportedEncodingException e) {
					toPara.addChild("#", " " + recipient);
				}
			}
		}

		HTMLNode subjectPara = headerBox.addChild("p");
		subjectPara.addChild("strong", "Subject:");

		String subject;
		try {
			subject = message.getSubject();
		} catch (UnsupportedEncodingException e) {
			subject = message.getFirstHeader("subject");
		}
		if((subject == null) || (subject.equals(""))) {
			subject = FreemailL10n.getString("Freemail.Web.Common.defaultSubject");
		}
		subjectPara.addChild("#", " " + subject);

		HTMLNode datePara = headerBox.addChild("p");
		datePara.addChild("strong", FreemailL10n.getString("Freemail.MessageToadlet.date"));
		datePara.addChild("#", " " + getMessageDateAsString(message,
				FreemailL10n.getString("Freemail.MessageToadlet.dateMissing")));
	}

	private void addMessageContents(HTMLNode messageNode, MailMessage message) {
		HTMLNode messageContents = messageNode.addChild("div", "class", "message-content").addChild("p");

		try {
			BufferedReader body = message.getBodyReader();
			try {
				String line = body.readLine();
				boolean added = false;
				while(line != null) {
					if(added) messageContents.addChild("br");
					messageContents.addChild("#", line);
					added = true;
					line = body.readLine();
				}
			} finally {
				body.close();
			}
		} catch(IOException e) {
			//TODO: Better error message
			messageContents.removeChildren();
			HTMLNode errorBox = addErrorbox(messageContents, "Couldn't read message");
			errorBox.addChild("p", "Couldn't read the message: " + e);
			return;
		}
	}

	private HTMLNode addMessageBank(HTMLNode parent, MessageBank messageBank, String folderName) {
		//First add this message bank
		HTMLNode folderDiv = parent.addChild("div", "class", "folder");
		HTMLNode folderPara = folderDiv.addChild("p");
		folderPara.addChild("a", "href", InboxToadlet.getFolderPath(folderName), messageBank.getName());

		//Then add all the children recursively
		for(MessageBank child : messageBank.listSubFolders()) {
			addMessageBank(folderDiv, child, folderName + "." + child.getName());
		}

		return folderDiv;
	}

	@Override
	public String path() {
		return PATH;
	}

	static String getPath() {
		return PATH;
	}

	static String getMessagePath(String folderName, int messageNum) {
		return getPath() + "?folder=" + folderName + "&uid=" + messageNum;
	}

	@Override
	boolean requiresValidSession() {
		return true;
	}
}

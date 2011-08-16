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

package freemail.ui.web;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import freemail.AccountManager;
import freemail.FreemailAccount;
import freemail.MailMessage;
import freemail.MessageBank;
import freemail.support.MessageBankTools;
import freemail.l10n.FreemailL10n;
import freemail.utils.Logger;
import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;

public class MessageToadlet extends WebPage {
	private static final String PATH = WebInterface.PATH + "/Message";

	private final AccountManager accountManager;

	MessageToadlet(AccountManager accountManager, PluginRespirator pluginRespirator) {
		super(pluginRespirator);
		this.accountManager = accountManager;
	}

	@Override
	void makeWebPageGet(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws ToadletContextClosedException, IOException {
		HTMLNode pageNode = page.outer;
		HTMLNode contentNode = page.content;

		HTMLNode container = contentNode.addChild("div", "class", "container");

		//Add the list of folders
		HTMLNode folderList = container.addChild("div", "class", "folderlist");

		String identity = sessionManager.useSession(ctx).getUserID();
		FreemailAccount account = accountManager.getAccount(identity);
		MessageBank topLevelMessageBank = account.getMessageBank();
		addMessageBank(folderList, topLevelMessageBank, "inbox");

		//Add the message
		String folderName = req.getParam("folder", "inbox");
		MessageBank messageBank;
		if(folderName.equals("inbox")) {
			messageBank = account.getMessageBank();
		} else {
			folderName = folderName.substring("index.".length());
			messageBank = MessageBankTools.getMessageBank(account.getMessageBank(), folderName);
		}

		int messageUid;
		try {
			messageUid = Integer.parseInt(req.getParam("uid"));
		} catch(NumberFormatException e) {
			Logger.error(this, "Got invalid uid: " + req.getParam("uid"));
			messageUid = 0;
		}
		MailMessage msg = MessageBankTools.getMessage(messageBank, messageUid);

		if(msg == null) {
			HTMLNode infobox = addErrorbox(container, "Message doesn't exist");
			infobox.addChild("p", "The message you requested doesn't exist");
			writeHTMLReply(ctx, 200, "OK", pageNode.generate());
			return;
		}

		HTMLNode messageNode = container.addChild("div", "class", "message");

		addMessageButtons(ctx, messageNode, folderName, messageUid);
		addMessageHeaders(messageNode, msg);
		addMessageContents(messageNode, msg);

		//Mark message as read
		if(!msg.flags.get("\\seen")) {
			msg.flags.set("\\seen", true);
			msg.storeFlags();
		}

		writeHTMLReply(ctx, 200, "OK", pageNode.generate());
	}

	@Override
	void makeWebPagePost(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws ToadletContextClosedException, IOException {
		makeWebPageGet(uri, req, ctx, page);
	}

	private void addMessageButtons(ToadletContext ctx, HTMLNode parent, String folderName, int uid) {
		HTMLNode buttonBox = parent.addChild("div", "class", "message-buttons");

		//Add reply button
		HTMLNode replyForm = ctx.addFormChild(buttonBox, NewMessageToadlet.getPath(), "reply");
		replyForm.addChild("input", new String[] {"type",   "name",   "value"},
		                            new String[] {"hidden", "action", "reply"});
		replyForm.addChild("input", new String[] {"type",   "name",   "value"},
		                            new String[] {"hidden", "folder", folderName});
		replyForm.addChild("input", new String[] {"type",   "name",    "value"},
		                            new String[] {"hidden", "message", "" + uid});
		replyForm.addChild("input", new String[] {"type",   "value"},
		                            new String[] {"submit", FreemailL10n.getString("Freemail.MessageToadlet.reply")});
	}

	private void addMessageHeaders(HTMLNode messageNode, MailMessage message) {
		HTMLNode headerBox = messageNode.addChild("div", "class", "message-headers");

		try {
			message.readHeaders();
		} catch(IOException e) {
			Logger.error(this, "Caugth IOException reading headers for " + message);
			headerBox.addChild("p", "There was a problem reading the message headers");
			return;
		}

		HTMLNode toPara = headerBox.addChild("p");
		toPara.addChild("strong", "To:");
		toPara.addChild("#", " " + message.getFirstHeader("to"));

		HTMLNode fromPara = headerBox.addChild("p");
		fromPara.addChild("strong", "From:");
		fromPara.addChild("#", " " + message.getFirstHeader("from"));

		if(message.getFirstHeader("cc") != null) {
			HTMLNode ccPara = headerBox.addChild("p");
			ccPara.addChild("strong", "CC:");
			ccPara.addChild("#", " " + message.getFirstHeader("cc"));
		}

		if(message.getFirstHeader("bcc") != null) {
			HTMLNode bccPara = headerBox.addChild("p");
			bccPara.addChild("strong", "BCC:");
			bccPara.addChild("#", " " + message.getFirstHeader("bcc"));
		}

		HTMLNode subjectPara = headerBox.addChild("p");
		subjectPara.addChild("strong", "Subject:");
		subjectPara.addChild("#", " " + message.getFirstHeader("subject"));
	}

	private void addMessageContents(HTMLNode messageNode, MailMessage message) {
		HTMLNode messageContents = messageNode.addChild("div", "class", "message-content").addChild("p");

		List<String> lines = new LinkedList<String>();
		try {
			boolean inHeader = true;
			while(true) {
				String line = message.readLine();
				if(line == null) break;

				if((line.equals("")) && inHeader) {
					inHeader = false;
					continue;
				}
				if(inHeader) {
					continue;
				}

				lines.add(line);
			}
		} catch(IOException e) {
			//TODO: Better error message
			HTMLNode errorBox = addErrorbox(messageContents, "Couldn't read message");
			errorBox.addChild("p", "Couldn't read the message: " + e);
			return;
		}

		Iterator<String> lineIterator = lines.iterator();
		while(lineIterator.hasNext()) {
			messageContents.addChild("#", lineIterator.next());
			if(lineIterator.hasNext()) {
				messageContents.addChild("br");
			}
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

	static String getMessagePath(String folderName, int messageNum) {
		return getPath() + "?folder=" + folderName + "&uid=" + messageNum;
	}

	@Override
	boolean requiresValidSession() {
		return true;
	}
}

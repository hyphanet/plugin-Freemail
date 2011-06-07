/*
 * InboxToadlet.java
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
import java.util.SortedMap;

import freemail.AccountManager;
import freemail.FreemailAccount;
import freemail.MailMessage;
import freemail.MessageBank;
import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.PageMaker;
import freenet.clients.http.PageNode;
import freenet.clients.http.SessionManager;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;

public class InboxToadlet extends WebPage {
	private final AccountManager accountManager;

	InboxToadlet(HighLevelSimpleClient client, SessionManager sessionManager, PageMaker pageMaker, AccountManager accountManager) {
		super(client, pageMaker, sessionManager);
		this.accountManager = accountManager;
	}

	@Override
	public void makeWebPage(URI uri, HTTPRequest req, ToadletContext ctx, HTTPMethod method, PageNode page) throws ToadletContextClosedException, IOException {
		HTMLNode pageNode = page.outer;
		HTMLNode contentNode = page.content;

		HTMLNode container = contentNode.addChild("div", "class", "container");

		//Add the list of folders
		HTMLNode folderList = container.addChild("div", "class", "folderlist");

		String identity = sessionManager.useSession(ctx).getUserID();

		//FIXME: Handle invalid sessions (account will be null)
		FreemailAccount account = accountManager.getAccount(identity);

		MessageBank topLevelMessageBank = account.getMessageBank();
		addMessageBank(folderList, topLevelMessageBank, "inbox");

		//Add the messages
		String folderName = req.getParam("folder", "inbox");
		MessageBank messageBank = getMessageBank(account, folderName);
		HTMLNode messageList = container.addChild("div", "class", "messagelist");
		SortedMap<Integer, MailMessage> messages = messageBank.listMessages();
		for(MailMessage msg : messages.values()) {
			//FIXME: Initialization of MailMessage should be in MailMessage
			msg.readHeaders();

			addMessage(messageList, msg, folderName);
		}

		writeHTMLReply(ctx, 200, "OK", pageNode.generate());
	}

	//TODO: Handle cases where folderName doesn't start with inbox
	private MessageBank getMessageBank(FreemailAccount account, String folderName) {
		if(folderName.equalsIgnoreCase("inbox")) {
			return account.getMessageBank();
		}

		if(!folderName.startsWith("inbox")) {
			return null;
		}

		//Find the correct subfolder. The account message bank is inbox, so strip it
		MessageBank messageBank = account.getMessageBank();
		for(String name : folderName.substring("index.".length()).split("\\.")) {
			messageBank = messageBank.getSubFolder(name);
		}
		return messageBank;
	}

	private HTMLNode addMessageBank(HTMLNode parent, MessageBank messageBank, String link) {
		//First add this message bank
		HTMLNode folderDiv = parent.addChild("div", "class", "folder");
		HTMLNode folderPara = folderDiv.addChild("p");
		folderPara.addChild("a", "href", "?folder=" + link, messageBank.getName());

		//Then add all the children recursively
		for(MessageBank child : messageBank.listSubFolders()) {
			addMessageBank(folderDiv, child, link + "." + child.getName());
		}

		return folderDiv;
	}

	//FIXME: Handle messages without message-id. This applies to MessageToadlet as well
	private void addMessage(HTMLNode parent, MailMessage msg, String folderLink) {
		HTMLNode message = parent.addChild("div", "class", "message");

		HTMLNode titleDiv = message.addChild("div", "class", "title");
		String messageLink = "/Freemail/Message?folder=" + folderLink + "&messageid=" + msg.getFirstHeader("message-id");
		titleDiv.addChild("p").addChild("a", "href", messageLink, msg.getFirstHeader("Subject"));

		HTMLNode authorDiv = message.addChild("div", "class", "author");
		authorDiv.addChild("p", msg.getFirstHeader("From"));
		HTMLNode dateDiv = message.addChild("div", "class", "date");
		dateDiv.addChild("p", msg.getFirstHeader("Date"));
	}

	@Override
	public boolean isEnabled(ToadletContext ctx) {
		return sessionManager.sessionExists(ctx);
	}

	@Override
	public String path() {
		return "/Freemail/Inbox";
	}

	@Override
	boolean requiresValidSession() {
		return true;
	}
}

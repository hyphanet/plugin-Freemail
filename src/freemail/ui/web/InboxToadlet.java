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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;

import javax.naming.SizeLimitExceededException;

import freemail.AccountManager;
import freemail.FreemailAccount;
import freemail.MailMessage;
import freemail.MessageBank;
import freemail.l10n.FreemailL10n;
import freemail.utils.Logger;
import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.PageMaker;
import freenet.clients.http.PageNode;
import freenet.clients.http.SessionManager;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;

public class InboxToadlet extends WebPage {
	private final AccountManager accountManager;
	private final PluginRespirator pluginRespirator;

	InboxToadlet(HighLevelSimpleClient client, SessionManager sessionManager, PageMaker pageMaker, AccountManager accountManager, PluginRespirator pluginRespirator) {
		super(client, pageMaker, sessionManager);
		this.accountManager = accountManager;
		this.pluginRespirator = pluginRespirator;
	}

	@Override
	public void makeWebPage(URI uri, HTTPRequest req, ToadletContext ctx, HTTPMethod method, PageNode page) throws ToadletContextClosedException, IOException {
		switch(method) {
		case GET:
			makeWebPageGet(req, ctx, page);
			break;
		case POST:
			makeWebPagePost(req, ctx);
			break;
		default:
			//This will only happen if a new value is added to HTTPMethod, so log it and send an
			//error message
			assert false : "HTTPMethod has unknown value: " + method;
			Logger.error(this, "HTTPMethod has unknown value: " + method);
			writeHTMLReply(ctx, 200, "OK", "Unknown HTTP method " + method + ". This is a bug in Freemail");
		}
	}

	private void makeWebPageGet(HTTPRequest req, ToadletContext ctx, PageNode page) throws ToadletContextClosedException, IOException {
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

		//Add the container for the message list and the buttons
		String folderName = req.getParam("folder", "inbox");
		MessageBank messageBank = getMessageBank(account, folderName);
		HTMLNode messageList = container.addChild("div", "class", "messagelist");
		messageList = pluginRespirator.addFormChild(messageList, "InboxToadlet", "action");
		messageList.addChild("input", new String[] {"type",   "name",   "value"},
		                              new String[] {"hidden", "folder", folderName});

		//Add buttons
		messageList.addChild("input", new String[] {"type",   "name",   "value"},
		                              new String[] {"submit", "delete", FreemailL10n.getString("Freemail.InboxToadlet.delete")});

		HTMLNode folderDropdown = messageList.addChild("select", "name", "destination");
		for(String folder : getAllFolders(account)) {
			if(folder.equals(folderName)) {
				//Skip the current folder, since we don't want to move messages there
				continue;
			}
			folderDropdown.addChild("option", "value", folder, folder.replace(".", "/"));
		}
		messageList.addChild("input", new String[] {"type",   "name", "value"},
		                              new String[] {"submit", "move", FreemailL10n.getString("Freemail.InboxToadlet.move")});

		//Add the message list
		HTMLNode messageTable = messageList.addChild("table");
		SortedMap<Integer, MailMessage> messages = messageBank.listMessages();
		for(Entry<Integer, MailMessage> message : messages.entrySet()) {
			//FIXME: Initialization of MailMessage should be in MailMessage
			message.getValue().readHeaders();

			addMessage(messageTable, message.getValue(), folderName, message.getKey());
		}

		writeHTMLReply(ctx, 200, "OK", pageNode.generate());
	}

	@SuppressWarnings("deprecation")
	private void makeWebPagePost(HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException {
		String pass;
		try {
			pass = req.getPartAsStringThrowing("formPassword", 32);
		} catch (SizeLimitExceededException e) {
			writeHTMLReply(ctx, 403, "Forbidden", "Form password too long");
			return;
		} catch (NoSuchElementException e) {
			writeHTMLReply(ctx, 403, "Forbidden", "Missing form password");
			return;
		}

		if((pass.length() == 0) || !pass.equals(pluginRespirator.getNode().clientCore.formPassword)) {
			writeHTMLReply(ctx, 403, "Forbidden", "Invalid form password.");
			return;
		}

		String identity = sessionManager.useSession(ctx).getUserID();
		FreemailAccount account = accountManager.getAccount(identity);
		String folderName = req.getParam("folder", "inbox");
		MessageBank messageBank = getMessageBank(account, folderName);

		Set<MailMessage> selectedMessages = new HashSet<MailMessage>();
		for(Entry<Integer, MailMessage> messageEntry : messageBank.listMessages().entrySet()) {
			int num = messageEntry.getKey();
			try {
				//If this doesn't throw NoSuchElementException the box was checked
				req.getPartAsStringThrowing("msg-" + num, 100);
				selectedMessages.add(messageEntry.getValue());
			} catch(SizeLimitExceededException e) {
				Logger.debug(this, "msg-" + num + ": Size limit");
			} catch(NoSuchElementException e) {
				Logger.debug(this, "msg-" + num + ": No such element");
			}
		}

		for(MailMessage message : selectedMessages) {
			if(!req.getPartAsString("move", 100).equals("")) {
				MessageBank destination = getMessageBank(account, req.getPartAsString("destination", 100));
				message.copyTo(destination.createMessage());
				message.delete();
			} else if(!req.getPartAsString("delete", 100).equals("")) {
				message.delete();
			}
		}

		writeTemporaryRedirect(ctx, "", "/Freemail/Inbox?folder=" + req.getPartAsString("folder", 100));
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
	private void addMessage(HTMLNode parent, MailMessage msg, String folderLink, int messageNum) {
		HTMLNode message = parent.addChild("tr", "class", "message");
		boolean read = msg.flags.get("\\seen");

		HTMLNode checkBox = message.addChild("td");
		checkBox.addChild("input", new String[] {"type",     "name"},
		                           new String[] {"checkbox", "msg-" + messageNum});

		String messageLink = "/Freemail/Message?folder=" + folderLink + "&uid=" + messageNum;
		HTMLNode title = message.addChild("td", "class", "title");
		title = title.addChild("p");
		if(!read) {
			title = title.addChild("strong");
		}
		title.addChild("a", "href", messageLink, msg.getFirstHeader("Subject"));

		HTMLNode author = message.addChild("td", "class", "author");
		author = author.addChild("p");
		if(!read) {
			author = author.addChild("strong");
		}
		author.addChild("#", msg.getFirstHeader("From"));

		HTMLNode date = message.addChild("td", "class", "date");
		date = date.addChild("p");
		if(!read) {
			date = date.addChild("strong");
		}
		date.addChild("#", msg.getFirstHeader("Date"));
	}

	private List<String> getAllFolders(FreemailAccount account) {
		List<String> folderList = new LinkedList<String>();
		MessageBank topLevel = account.getMessageBank();
		folderList.add(topLevel.getName());
		addSubfolders(folderList, topLevel, topLevel.getName());
		return folderList;
	}

	private void addSubfolders(List<String> folders, MessageBank folder, String name) {
		for(MessageBank mb : folder.listSubFolders()) {
			folders.add(name + "." + mb.getName());
			addSubfolders(folders, mb, name + "." + mb.getName());
		}
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

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

package org.freenetproject.freemail.ui.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.naming.SizeLimitExceededException;

import org.freenetproject.freemail.AccountManager;
import org.freenetproject.freemail.FreemailAccount;
import org.freenetproject.freemail.MailMessage;
import org.freenetproject.freemail.MessageBank;
import org.freenetproject.freemail.l10n.FreemailL10n;
import org.freenetproject.freemail.utils.Logger;

import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.HTMLNode;
import freenet.support.api.HTTPRequest;

public class InboxToadlet extends WebPage {
	private static final String PATH = WebInterface.PATH + "/Inbox";
	private static final String TRASH_FOLDER = "Trash";

	private final AccountManager accountManager;

	InboxToadlet(AccountManager accountManager, PluginRespirator pluginRespirator, LoginManager loginManager) {
		super(pluginRespirator, loginManager);
		this.accountManager = accountManager;
	}

	@Override
	HTTPResponse makeWebPageGet(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) throws IOException {
		HTMLNode pageNode = page.outer;
		HTMLNode contentNode = page.content;

		HTMLNode container = contentNode.addChild("div", "class", "container");

		//Add the list of folders
		HTMLNode folderList = container.addChild("div", "class", "folderlist");

		String identity = loginManager.getSession(ctx).getUserID();

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
		addMoveMessageFunction(messageList, account, folderName);

		HTMLNode messageTable = messageList.addChild("table");

		//Add the message list header
		HTMLNode header = messageTable.addChild("tr");
		header.addChild("th");
		header.addChild("th").addChild("a", "href", getSortLink(SortField.SUBJECT, !getSortDirection(req)), FreemailL10n.getString("Freemail.InboxToadlet.subject"));
		header.addChild("th").addChild("a", "href", getSortLink(SortField.FROM, !getSortDirection(req)), FreemailL10n.getString("Freemail.InboxToadlet.from"));
		header.addChild("th").addChild("a", "href", getSortLink(SortField.DATE, !getSortDirection(req)), FreemailL10n.getString("Freemail.InboxToadlet.date"));

		//Sort the messages correctly
		SortedMap<MailMessage, Integer> messages = new TreeMap<MailMessage, Integer>(new MailMessageComparator(getSortField(req), getSortDirection(req)));
		for(Entry<Integer, MailMessage> messageEntry : messageBank.listMessages().entrySet()) {
			Integer messageNum = messageEntry.getKey();
			MailMessage message = messageEntry.getValue();

			//FIXME: Initialization of MailMessage should be in MailMessage
			message.readHeaders();

			if(message.flags.isDeleted()) {
				continue;
			}

			messages.put(message, messageNum);
		}

		//Add messages
		for(Entry<MailMessage, Integer> message : messages.entrySet()) {
			addMessage(messageTable, message.getKey(), folderName, message.getValue());
		}

		return new GenericHTMLResponse(ctx, 200, "OK", pageNode.generate());
	}

	private String getSortLink(SortField field, boolean ascending) {
		return path() + "?sort=" + field.name + "&direction=" + (ascending ? "ascending" : "descending");
	}

	private boolean getSortDirection(HTTPRequest req) {
		return "ascending".equals(req.getParam("direction"));
	}

	private SortField getSortField(HTTPRequest req) {
		return SortField.fromString(req.getParam("sort"));
	}

	private void addMoveMessageFunction(HTMLNode parent, FreemailAccount account, String currentFolder) {
		List<String> folderList = getAllFolders(account);
		//FIXME: It might be clearer if the button is just disabled in this case
		if((folderList.size() == 1) && (folderList.contains(currentFolder))) {
			return;
		}

		HTMLNode folderDropdown = parent.addChild("select", "name", "destination");
		for(String folder : folderList) {
			if(folder.equals(currentFolder)) {
				//Skip the current folder, since we don't want to move messages there
				continue;
			}
			folderDropdown.addChild("option", "value", folder, folder.replace(".", "/"));
		}
		parent.addChild("input", new String[] {"type",   "name", "value"},
		                         new String[] {"submit", "move", FreemailL10n.getString("Freemail.InboxToadlet.move")});
	}

	@Override
	@SuppressWarnings("deprecation")
	HTTPResponse makeWebPagePost(URI uri, HTTPRequest req, ToadletContext ctx, PageNode page) {
		String identity = loginManager.getSession(ctx).getUserID();
		FreemailAccount account = accountManager.getAccount(identity);

		String folderName = req.getPartAsStringFailsafe("folder", 1000);
		Logger.debug(this, "Folder name in request: " + folderName);
		if(folderName.equals("")) {
			folderName = "inbox";
		}
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
				if(folderName.equals("inbox." + TRASH_FOLDER)) {
					Logger.debug(this, "Deleting [" + message + "]");
					message.delete();
				} else {
					Logger.debug(this, "Moving [" + message + "] to trash");

					MessageBank inbox = account.getMessageBank();
					MessageBank target = inbox.makeSubFolder(TRASH_FOLDER);
					if(target == null) {
						target = inbox.getSubFolder(TRASH_FOLDER);
					}

					//If target still is null it couldn't be created
					if(target != null) {
						message.copyTo(target.createMessage());
						message.delete();
					} else {
						//TODO: Show an error message
						Logger.error(this, "Couldn't create folder " + TRASH_FOLDER);
					}
				}
			}
		}

		return new HTTPRedirectResponse(ctx, "", getFolderPath(folderName));
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

	private HTMLNode addMessageBank(HTMLNode parent, MessageBank messageBank, String folderName) {
		//First add this message bank
		HTMLNode folderDiv = parent.addChild("div", "class", "folder");
		HTMLNode folderPara = folderDiv.addChild("p");
		folderPara.addChild("a", "href", getFolderPath(folderName), messageBank.getName());

		//Then add all the children recursively
		for(MessageBank child : messageBank.listSubFolders()) {
			addMessageBank(folderDiv, child, folderName + "." + child.getName());
		}

		return folderDiv;
	}

	private String getFromHeaderForDisplay(MailMessage msg) {
		String from = msg.getFirstHeader("From");
		if (from == null) {
			return FreemailL10n.getString("Freemail.InboxToadlet.fromMissing");
		}
		try {
			return MailMessage.decodeHeader(from);
		} catch (UnsupportedEncodingException e) {
			return from;
		}
	}

	//FIXME: Handle messages without message-id. This applies to MessageToadlet as well
	private void addMessage(HTMLNode parent, MailMessage msg, String folderName, int messageNum) {
		String msgClass = "message";
		if(!msg.flags.isSeen()) {
			msgClass += " message-unread";
		}
		if(msg.flags.isRecent()) {
			msgClass += " message-recent";
			msg.flags.clearRecent();
			msg.storeFlags();
		}
		HTMLNode message = parent.addChild("tr", "class", msgClass);

		HTMLNode checkBox = message.addChild("td");
		checkBox.addChild("input", new String[] {"type",     "name"},
		                           new String[] {"checkbox", "msg-" + messageNum});

		String messageLink = MessageToadlet.getMessagePath(folderName, messageNum);
		HTMLNode title = message.addChild("td", "class", "title");
		String subject;
		try {
			subject = msg.getSubject();
		} catch (UnsupportedEncodingException e) {
			subject = msg.getFirstHeader("subject");
		}
		if((subject == null) || (subject.equals(""))) {
			subject = FreemailL10n.getString("Freemail.Web.Common.defaultSubject");
		}
		title.addChild("a", "href", messageLink, subject);

		HTMLNode author = message.addChild("td", "class", "author");
		author.addChild("#", getFromHeaderForDisplay(msg));

		HTMLNode date = message.addChild("td", "class", "date");
		date.addChild("#", getMessageDateAsString(msg,
				FreemailL10n.getString("Freemail.InboxToadlet.dateMissing")));
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
		return ctx.isAllowedFullAccess() && loginManager.sessionExists(ctx);
	}

	@Override
	public String path() {
		return PATH;
	}

	static String getPath() {
		return PATH;
	}

	static String getFolderPath(String folderName) {
		return getPath() + "?folder=" + folderName;
	}

	@Override
	boolean requiresValidSession() {
		return true;
	}

	private enum SortField {
		SUBJECT("subject"),
		FROM("from"),
		DATE("date");

		private final String name;
		private SortField(String name) {
			this.name = name;
		}

		public static SortField fromString(String field) {
			if("subject".equals(field)) {
				return SUBJECT;
			}
			if("from".equals(field)) {
				return FROM;
			}
			if("date".equals(field)) {
				return DATE;
			}
			return null;
		}
	}

	private static class MailMessageComparator implements Comparator<MailMessage> {
		private final SortField field;
		private final boolean ascending;

		private MailMessageComparator(SortField field, boolean ascending) {
			if(field == null) {
				this.field = SortField.DATE;
			} else {
				this.field = field;
			}
			this.ascending = ascending;
		}

		@Override
		public int compare(MailMessage msg0, MailMessage msg1) {
			if(!ascending) {
				//Swap the two so we get the opposite ordering
				MailMessage temp = msg0;
				msg0 = msg1;
				msg1 = temp;
			}

			int result;
			if(field == SortField.DATE) {
				Date msg1Date = msg0.getDate();
				Date msg2Date = msg1.getDate();

				result = compare(msg1Date, msg2Date);
			} else {
				String msg0Header = msg0.getFirstHeader(field.name);
				String msg1Header = msg1.getFirstHeader(field.name);

				result = compare(msg0Header, msg1Header);
			}

			if(result == 0) {
				//If the sort key is equal, use the id to compare since we can't return 0 for messages
				//that aren't really the same. This is because e.g. TreeSet (which is used above)
				//assumes that the comparison is consistent with equals.
				return msg1.getUID() - msg0.getUID();
			}

			return result;
		}

		private <T extends Comparable<T>> int compare(T o1, T o2) {
			if(o1 == null && o2 == null) {
				return 0;
			}

			//Sort non-null before null
			if(o1 == null || o2 == null) {
				return (o1 == null) ? -1 : 1;
			}

			return o1.compareTo(o2);
		}
	}
}

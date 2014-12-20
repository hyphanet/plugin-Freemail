/*
 * IMAPHandler.java
 * This file is part of Freemail
 * Copyright (C) 2006,2007,2008 Dave Baker
 * Copyright (C) 2007,2008 Alexander Lehmann
 * Copyright (C) 2008 Martin Nyhus
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

package org.freenetproject.freemail.imap;

import java.net.Socket;
import java.net.SocketException;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.lang.NumberFormatException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.archive.util.Base32;
import org.freenetproject.freemail.AccountManager;
import org.freenetproject.freemail.FreemailAccount;
import org.freenetproject.freemail.MailMessage;
import org.freenetproject.freemail.MessageBank;
import org.freenetproject.freemail.ServerHandler;
import org.freenetproject.freemail.utils.EmailAddress;
import org.freenetproject.freemail.utils.Logger;

import freenet.support.Base64;

public class IMAPHandler extends ServerHandler implements Runnable {
	private static final String CAPABILITY = "IMAP4rev1 CHILDREN NAMESPACE";

	private final PrintStream ps;
	private final BufferedReader bufrdr;
	private MessageBank mb;
	private MessageBank inbox;
	private final AccountManager accountManager;

	IMAPHandler(AccountManager accMgr, Socket client) throws IOException {
		super(client);
		accountManager = accMgr;
		this.ps = new PrintStream(client.getOutputStream());
		this.bufrdr = new BufferedReader(new InputStreamReader(client.getInputStream()));
		this.mb = null;
	}

	@Override
	public void run() {
		this.sendWelcome();

		String line;
		try {
			while(!stopping && !this.client.isClosed() && (line = this.bufrdr.readLine()) != null) {
				IMAPMessage msg = null;
				try {
					msg = new IMAPMessage(line);
				} catch (IMAPBadMessageException bme) {
					continue;
				}

				this.dispatch(msg);
			}

			this.client.close();
		} catch (IOException ioe) {
			//If we are stopping and get a SocketException it is probable that
			//the socket was closed while readLine() was blocked, so don't log
			if(!(stopping && ioe instanceof SocketException)) {
				Logger.error(this, "Caught IOException while reading imap data: " + ioe.getMessage(), ioe);
			}
		}
	}

	private void sendWelcome() {
		this.ps.print("* OK [CAPABILITY "+CAPABILITY+"] Freemail ready - hit me with your rhythm stick.\r\n");
	}

	private void dispatch(IMAPMessage msg) {
		Logger.debug(this, "Received: " + msg);
		if(msg.type.equals("login")) {
			this.handleLogin(msg);
		} else if(msg.type.equals("logout")) {
			this.handleLogout(msg);
		} else if(msg.type.equals("capability")) {
			this.handleCapability(msg);
		} else if(msg.type.equals("list")) {
			this.handleList(msg);
		} else if(msg.type.equals("select")) {
			this.handleSelect(msg);
		} else if(msg.type.equals("noop")) {
			this.handleNoop(msg);
		} else if(msg.type.equals("check")) {
			this.handleCheck(msg);
		} else if(msg.type.equals("uid")) {
			this.handleUid(msg);
		} else if(msg.type.equals("fetch")) {
			this.handleFetch(msg);
		} else if(msg.type.equals("store")) {
			this.handleStore(msg);
		} else if(msg.type.equals("close")) {
			this.handleClose(msg);
		} else if(msg.type.equals("expunge")) {
			this.handleExpunge(msg);
		} else if(msg.type.equals("namespace")) {
			this.handleNamespace(msg);
		} else if(msg.type.equals("lsub")) {
			this.handleLsub(msg);
		} else if(msg.type.equals("status")) {
			this.handleStatus(msg);
		} else if(msg.type.equals("create")) {
			this.handleCreate(msg);
		} else if(msg.type.equals("delete")) {
			this.handleDelete(msg);
		} else if(msg.type.equals("copy")) {
			this.handleCopy(msg);
		} else if(msg.type.equals("append")) {
			this.handleAppend(msg);
		} else if(msg.type.equals("search")) {
			handleSearch(msg);
		} else {
			Logger.error(this, "Unknown IMAP command: " + msg.type);
			this.reply(msg, "NO Sorry - not implemented");
		}
	}

	private void handleLogin(IMAPMessage msg) {
		if(msg.args == null || msg.args.length < 2) {
			this.reply(msg, "BAD Not enough arguments");
			return;
		}

		String username = trimQuotes(msg.args[0]);
		String password = trimQuotes(msg.args[1]);

		if(username.contains("@") && username.endsWith(".freemail")) {
			//Extract the base32 identity string and convert it to base64
			username = username.substring(username.indexOf("@") + 1,
			                              username.length() - ".freemail".length());

			//We need to use the Freenet Base64 encoder here since it uses a slightly different set
			//of characters
			username = Base64.encode(Base32.decode(username));

			Logger.debug(this, "Extracted Identity string: " + username);
		}

		FreemailAccount account = accountManager.authenticate(username, password);
		if(account != null) {
			this.inbox = account.getMessageBank();

			this.reply(msg, "OK Logged in");
		} else {
			this.reply(msg, "NO Login failed");
		}
	}

	private void handleLogout(IMAPMessage msg) {
		this.sendState("BYE");
		this.reply(msg, "OK Bye");
		try {
			this.client.close();
		} catch (IOException ioe) {
			Logger.error(this, "Caugth IOException while closing socket: " + ioe.getMessage(), ioe);
		}
	}

	private void handleCapability(IMAPMessage msg) {
		this.sendState("CAPABILITY "+CAPABILITY);

		this.reply(msg, "OK Capability completed");
	}

	private void handleLsub(IMAPMessage msg) {
		this.handleList(msg);
	}

	private void handleList(IMAPMessage msg) {
		String refname;
		String mbname;

		if(!this.verifyAuth(msg)) {
			return;
		}

		if(msg.args == null || msg.args.length < 1) {
			refname = null;
			mbname = null;
		} else if(msg.args.length < 2) {
			refname = msg.args[0];
			mbname = null;
		} else {
			refname = msg.args[0];
			mbname = msg.args[1];
		}

		String replyprefix = "LIST";
		if(msg.type.equals("lsub")) {
			replyprefix = "LSUB";
		}

		if(refname != null) refname = trimQuotes(refname);
		if(refname!= null && refname.length() == 0) refname = null;

		if(mbname != null) mbname = trimQuotes(mbname);
		if(mbname != null && mbname.length() == 0) mbname = null;

		if(mbname == null) {
			// return hierarchy delimiter
			this.sendState(replyprefix+" (\\Noselect) \".\" \"\"");
		} else {
			// transform mailbox name into a regex

			// '*' needs to be '.*'
			mbname = mbname.replaceAll("\\*", ".*");

			// and % is a wildcard not including the hierarchy delimiter
			mbname = mbname.replaceAll("%", "[^\\.]*");


			this.listMatchingFolders(this.inbox, mbname, replyprefix, "INBOX.");

			/// and send the inbox too, if it matches
			if("INBOX".matches(mbname)) {
				this.sendState(replyprefix+" "+this.inbox.getFolderFlagsString()+" \".\" \"INBOX\"");
			}
		}

		this.reply(msg, "OK "+replyprefix+" completed");
	}

	private void listMatchingFolders(MessageBank folder, String pattern, String replyprefix, String folderpath) {
		MessageBank[] folders = folder.listSubFolders();

		for(int i = 0; i < folders.length; i++) {
			String fullpath = folderpath+folders[i].getName();

			this.listMatchingFolders(folders[i], pattern, replyprefix, fullpath+".");
			if(fullpath.matches(pattern)) {
				this.sendState(replyprefix+" "+folders[i].getFolderFlagsString()+" \".\" \""+fullpath+"\"");
			}
		}
	}

	private MessageBank getMailboxFromPath(String path) {
		MessageBank tempmb = this.inbox;

		String[] mbparts = path.split("\\.");

		if(!mbparts[0].equalsIgnoreCase("inbox")) {
			return null;
		}

		int i;
		for(i = 1; i < mbparts.length; i++) {
			tempmb = tempmb.getSubFolder(mbparts[i]);
			if(tempmb == null) {
				return null;
			}
		}

		return tempmb;
	}

	private void handleSelect(IMAPMessage msg) {
		String mbname;

		if(!this.verifyAuth(msg)) {
			return;
		}

		if(msg.args == null || msg.args.length < 1) {
			this.reply(msg, "NO What mailbox?");
			return;
		}

		mbname = trimQuotes(msg.args[0]);

		MessageBank tempmb = this.getMailboxFromPath(mbname);

		if(tempmb == null) {
			this.reply(msg, "NO No such mailbox");
			return;
		} else {
			this.mb = tempmb;
		}

		this.sendState("FLAGS ("+IMAPMessageFlags.getAllFlagsAsString()+")");
		this.sendState("OK [PERMANENTFLAGS ("+IMAPMessageFlags.getPermanentFlagsAsString()+")] Limited");

		SortedMap<Integer, MailMessage> msgs = this.mb.listMessages();

		int numrecent = 0;
		int numexists = msgs.size();
		while(msgs.size() > 0) {
			Integer current = msgs.firstKey();
			MailMessage m =msgs.get(msgs.firstKey());

			// if it's recent, add to the tally
			if(m.flags.isRecent()) {
				numrecent++;

				// remove the recent flag
				m.flags.set("\\Recent", false);
				m.storeFlags();
			}

			msgs = msgs.tailMap(new Integer(current.intValue()+1));
		}

		this.sendState(numexists+" EXISTS");
		this.sendState(numrecent+" RECENT");

		this.sendState("OK [UIDVALIDITY " + mb.getUidValidity() + "] Ok");

		this.reply(msg, "OK [READ-WRITE] Done");
	}

	private void handleNoop(IMAPMessage msg) {
		this.reply(msg, "OK NOOP completed");
	}

	private void handleCheck(IMAPMessage msg) {
		if(!this.verifyAuth(msg)) {
			return;
		}

		if(this.mb == null) {
			this.reply(msg, "NO No mailbox selected");
			return;
		}

		this.reply(msg, "OK Check completed");
	}

	private void handleFetch(IMAPMessage msg) {
		handleFetch(msg, false);
	}

	private void handleFetch(IMAPMessage msg, boolean uid) {
		if(!this.verifyAuth(msg)) {
			return;
		}

		if(this.mb == null) {
			this.reply(msg, "NO No mailbox selected");
			return;
		}

		SortedMap<Integer, MailMessage> msgs = this.mb.listMessages();

		if(msgs.size() == 0) {
			this.reply(msg, "OK Fetch completed");
			return;
		}

		if(msg.args == null || msg.args.length < 2) {
			this.reply(msg, "BAD Not enough arguments");
			return;
		}

		MailMessage lastMessage = msgs.get(msgs.lastKey());
		SortedSet<Integer> sequenceNumbers;
		try {
			sequenceNumbers = parseSequenceSet(msg.args[0],
					uid ? lastMessage.getUID() : lastMessage.getSeqNum());
		} catch(NumberFormatException e) {
			this.reply(msg, "BAD Illegal sequence number set");
			return;
		} catch (IllegalSequenceNumberException e) {
			this.reply(msg, "NO Invalid message ID");
			return;
		}

		if(!uid) {
			if(sequenceNumbers.first() < 1 || sequenceNumbers.last() > lastMessage.getSeqNum()) {
				reply(msg, "NO Invalid message ID");
				return;
			}
		}

		//Return the messages in the range
		for(MailMessage message : msgs.values()) {
			if(uid) {
				if(!sequenceNumbers.contains(message.getUID())) {
					continue;
				}
			} else {
				if(!sequenceNumbers.contains(message.getSeqNum())) {
					continue;
				}
			}

			if(!this.fetchSingle(message, msg.args, 1, uid)) {
				this.reply(msg, "BAD Unknown attribute in list or unterminated list");
				return;
			}
		}

		this.reply(msg, "OK Fetch completed");
	}

	private void handleUid(IMAPMessage msg) {
		if(msg.args == null || msg.args.length < 1) {
			this.reply(msg, "BAD Not enough arguments for uid command");
			return;
		}

		//Handle fetch and search in the new way
		if(msg.args[0].equalsIgnoreCase("fetch")) {
			String[] commandArgs = new String[msg.args.length - 1];
			System.arraycopy(msg.args, 1, commandArgs, 0, commandArgs.length);
			IMAPMessage command = new IMAPMessage(msg.tag, msg.args[0], commandArgs);

			handleFetch(command, true);
			return;
		}
		if(msg.args[0].equalsIgnoreCase("search")) {
			String[] commandArgs = new String[msg.args.length - 1];
			System.arraycopy(msg.args, 1, commandArgs, 0, commandArgs.length);
			IMAPMessage command = new IMAPMessage(msg.tag, msg.args[0], commandArgs);

			handleSearch(command, true);
			return;
		}
		if(msg.args[0].equalsIgnoreCase("copy")) {
			String[] commandArgs = new String[msg.args.length - 1];
			System.arraycopy(msg.args, 1, commandArgs, 0, commandArgs.length);
			IMAPMessage command = new IMAPMessage(msg.tag, msg.args[0], commandArgs);

			handleCopy(command, true);
			return;
		}

		if(!msg.args[0].equalsIgnoreCase("store")) {
			this.reply(msg, "BAD Unknown command");
			return;
		}

		//And the rest in the old way for now
		if(msg.args.length < 3) {
			this.reply(msg, "BAD Not enough arguments for uid command");
			return;
		}

		if(!this.verifyAuth(msg)) {
			return;
		}

		if(this.mb == null) {
			this.reply(msg, "NO No mailbox selected");
			return;
		}

		SortedMap<Integer, MailMessage> msgs = this.mb.listMessages();
		if(msgs.size() == 0) {
			this.reply(msg, "NO No such message");
			return;
		}

		Set<Integer> ts;
		try {
			MailMessage lastMessage = msgs.get(msgs.lastKey());
			ts = parseSequenceSet(msg.args[1], lastMessage.getUID());
		} catch(NumberFormatException e) {
			this.reply(msg, "BAD Illegal sequence number set");
			return;
		} catch (IllegalSequenceNumberException e) {
			this.reply(msg, "NO Invalid message ID");
			return;
		}

		Iterator<MailMessage> msgIt = msgs.values().iterator();
		while(msgIt.hasNext()) {
			if(!ts.contains(msgIt.next().getUID())) {
				msgIt.remove();
			}
		}

		if(!this.doStore(msg.args, 2, msgs.values(), msg, true)) {
			return;
		}

		this.reply(msg, "OK Store completed");
	}

	private boolean fetchSingle(MailMessage msg, String[] args, int firstarg, boolean send_uid_too) {
		String[] imap_args = args.clone();
		this.ps.print("* "+msg.getSeqNum()+" FETCH (");

		// do the first attribute, if it's a loner.
		if(!imap_args[firstarg].startsWith("(")) {
			// It's a loner
			this.ps.flush();
			if(!this.sendAttr(msg, imap_args[firstarg])){
				// send fake end delimiter, so we do not break the protocol
				this.ps.print(")\r\n");
				this.ps.flush();
				return false;
			}
			if(send_uid_too && !imap_args[firstarg].equalsIgnoreCase("uid")) {
				this.ps.print(" UID "+msg.getUID());
			}

			this.ps.print(")\r\n");
			this.ps.flush();

			return true;
		} else {
			imap_args[firstarg] = imap_args[firstarg].substring(1);
		}

		// go through the parenthesized list
		for(int i = firstarg; i < imap_args.length; i++) {
			String attr;
			boolean finish = false;

			if(imap_args[i].endsWith(")")) {
				finish = true;
				attr = imap_args[i].substring(0, imap_args[i].length() - 1);
			} else {
				attr = imap_args[i];
			}

			//this.ps.print(attr+" ");
			this.ps.flush();
			if(!this.sendAttr(msg, attr)) {
				// send fake end delimiter, so we do not break the protocol
				this.ps.print(")\r\n");
				this.ps.flush();
				return false;
			}

			if(attr.equalsIgnoreCase("uid")) {
				send_uid_too = false;
			}

			if(finish) {
				if(send_uid_too) {
					this.ps.print(" UID "+msg.getUID());
				}

				this.ps.print(")\r\n");
				this.ps.flush();
				return true;
			} else if((i + 1) < imap_args.length) {
				//Only print a space if there are more arguments to deal with
				this.ps.print(" ");
			}
		}

		// if we get here, we've reached the end of the list without a terminating parenthesis. Naughty client.
		if(send_uid_too) {
			this.ps.print(" UID "+msg.getUID());
		}
		this.ps.print(")\r\n");
		this.ps.flush();

		return false;
	}

	private boolean sendAttr(MailMessage mmsg, String a) {
		String attr = a.toLowerCase(Locale.ROOT);
		String val = null;

		if(attr.equals("uid")) {
			val = Integer.toString(mmsg.getUID());
		} else if(attr.equals("flags")) {
			val = "(" + mmsg.flags.getFlags() + ")";
		} else if(attr.equals("rfc822.size")) {
			try {
				val = Long.toString(mmsg.getSize());
			} catch (IOException ioe) {
				val = "0";
			}
		} else if(attr.equals("envelope")) {
			val = this.getEnvelope(mmsg);
		} else if(attr.startsWith("body.peek")) {
			this.ps.print(a.substring(0, "body".length()));
			this.ps.flush();
			a = a.substring("body.peek".length());
			return this.sendBody(mmsg, a, false);
		} else if(attr.startsWith("bodystructure")) {
			// TODO: we blatantly lie about the message structure
			this.ps.print(a.substring(0, "bodystructure".length()));
			this.ps.print(" (\"TEXT\" \"PLAIN\" (\"CHARSET\" \"ISO-8859-1\") NIL NIL \"8BIT\" 1024 10)");
			return true;
		} else if(attr.startsWith("body")) {
			// TODO: this is not quite right since it will match bodyanything
			mmsg.flags.setSeen();

			this.ps.print(a.substring(0, "body".length()));
			this.ps.flush();
			a = a.substring("body".length());
			if(this.sendBody(mmsg, a, false)) {
				mmsg.flags.setSeen();
				mmsg.storeFlags();
				return true;
			}
			return false;
		} else if(attr.startsWith("rfc822.header")) {
			this.ps.print(a.substring(0, "rfc822.header".length()));
			this.ps.flush();
			return this.sendBody(mmsg, "header", true);
		} else if(attr.startsWith("internaldate")) {
			/*
			 * FIXME: Internaldate should not return Date from the message
			 * For messages received though SMTP we want the date when we received it, for messages
			 * added by COPY it should be the internal date of the source message, and for messages
			 * added by APPEND it should either be the specified date or the date of the APPEND.
			 * See RFC 3501 section 2.3.3 (Internal Date Message Attribute).
			 */
			val = mmsg.getFirstHeader("Date");
			if(val == null) {
				// possibly should keep our own dates...
				SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z", Locale.ROOT);

				val = sdf.format(new Date());
			}
			val = "\""+val+"\"";
		}

		if(val == null)
			return false;
		this.ps.print(a+" "+val);
		return true;
	}

	private boolean sendBody(MailMessage mmsg, String attr, boolean hasSentDataName) {
		if(attr.length() < 1) return false;

		// handle byte ranges (e.g. body.peek[]<0.10240>

		int range_start=-1;
		int range_len=-1;

		if(attr.matches(".*<\\d+\\.\\d+>$")) {
			String range=attr.substring(attr.indexOf("<")+1, attr.length()-1);
			attr=attr.substring(0, attr.indexOf("<"));

			String r_start=range.substring(0, range.indexOf("."));
			String r_end=range.substring(range.indexOf(".")+1);
			try {
				range_start=Integer.parseInt(r_start);
				range_len=Integer.parseInt(r_end);
			} catch(NumberFormatException nfe) {
				// just ignore the range, this may problems though
				range_start=-1;
				range_len=-1;
			}
		}

		if(attr.charAt(0) == '[') attr = attr.substring(1);
		if(attr.charAt(attr.length() - 1) == ']')
			attr = attr.substring(0, attr.length() - 1);

		if(attr.trim().length() == 0) {
			try {
				if(!hasSentDataName) {
					this.ps.print("[]");
				}
				if(range_start!=-1) {
					this.ps.print("<"+range_start+">");
				}

				long partsize=0;
				if(range_start==-1) {
					partsize=mmsg.getSize();
				} else {
					partsize=range_len;
					if(mmsg.getSize()-range_start<partsize) {
						partsize=mmsg.getSize()-range_start;
					}
				}

				this.ps.print(" {"+partsize+"}\r\n");

				String line;
				while((line = mmsg.readLine()) != null) {
					line=line+"\r\n";
					if(range_start>0) {
						if(range_start>=line.length()) {
							range_start-=line.length();
							line="";
						} else {
							line=line.substring(range_start);
							range_start=0;
						}
					}
					if(range_start==0 || range_start==-1) {
						if(range_len==-1) {
							this.ps.print(line);
						} else {
							if(range_len>0) {
								if(range_len<line.length()) {
									line=line.substring(0, range_len);
									range_len=line.length();
								}
								this.ps.print(line);
								range_len-=line.length();
								if(range_len<0) {
									range_len=0;
								}
							}
						}
					}
				}
			} catch (IOException ioe) {
				return false;
			} finally {
				mmsg.closeStream();
			}
			return true;
		}

		StringBuffer buf = new StringBuffer("");

		String[] parts = IMAPMessage.doSplit(attr, '(', ')');
		if(parts.length > 0) {
			if(parts[0].equalsIgnoreCase("header.fields")) {
				if(!hasSentDataName) {
					this.ps.print("[HEADER.FIELDS "+parts[1]+"]");
				}
				if(parts[1].charAt(0) == '(')
					parts[1] = parts[1].substring(1);
				if(parts[1].charAt(parts[1].length() - 1) == ')')
					parts[1] = parts[1].substring(0, parts[1].length() - 1);

				try {
					mmsg.readHeaders();
				} catch (IOException ioe) {
					//FIXME: Handle IOException properly
					Logger.error(this, "Caught IOException while reading message headers: " + ioe.getMessage(), ioe);
				}

				String[] fields = parts[1].split(" ");
				for(int j = 0; j < fields.length; j++) {
					buf.append(mmsg.getHeaders(fields[j]));
				}
				buf.append("\r\n");
			} else if(parts[0].equalsIgnoreCase("header")) {
				if(!hasSentDataName) {
					this.ps.print("[HEADER]");
				}

				// send all the header fields
				try {
					mmsg.readHeaders();
				} catch (IOException ioe) {
					//FIXME: Handle IOException properly
					Logger.error(this, "Caught IOException while reading message headers: " + ioe.getMessage(), ioe);
				}

				buf.append(mmsg.getAllHeadersAsString());
				buf.append("\r\n");
			} else if(parts[0].equalsIgnoreCase("text")) {
				if(!hasSentDataName) {
					this.ps.print("[TEXT]");
				}

				// just send the text of the message without headers
				mmsg.closeStream();
				String line;
				// fast forward past the headers
				try {
					while((line = mmsg.readLine()) != null) {
						if(line.length() == 0) break;
					}
					while((line = mmsg.readLine()) != null) {
						buf.append(line+"\r\n");
					}
				} catch (IOException ioe) {
					// just return whatever we got
				} finally {
					mmsg.closeStream();
				}
			}

			this.ps.print(" {"+buf.length()+"}\r\n"+buf.toString());
			return true;
		}

		return false;
	}

	private void handleStore(IMAPMessage msg) {
		if(msg.args == null || msg.args.length < 2) {
			this.reply(msg, "BAD Not enough arguments");
			return;
		}

		if(!this.verifyAuth(msg)) {
			return;
		}

		if(this.mb == null) {
			this.reply(msg, "NO No mailbox selected");
			return;
		}

		SortedMap<Integer, MailMessage> msgs = this.mb.listMessages();

		Set<Integer> ts;
		try {
			MailMessage lastMessage = msgs.get(msgs.lastKey());
			ts = parseSequenceSet(msg.args[0], lastMessage.getUID());
		} catch(NumberFormatException e) {
			this.reply(msg, "BAD Illegal sequence number set");
			return;
		} catch (IllegalSequenceNumberException e) {
			this.reply(msg, "NO Invalid message ID");
			return;
		}

		Iterator<MailMessage> msgIt = msgs.values().iterator();
		while(msgIt.hasNext()) {
			if(!ts.contains(msgIt.next().getSeqNum())) {
				msgIt.remove();
			}
		}

		if(!doStore(msg.args, 1, msgs.values(), msg, false)) {
			return;
		}

		this.reply(msg, "OK Store completed");
	}

	private boolean doStore(String[] args, int offset, Collection<MailMessage> mmsgs, IMAPMessage msg, boolean senduid) {
		if(args[offset].toLowerCase(Locale.ROOT).indexOf("flags") < 0) {
			// IMAP4Rev1 can only store flags, so you're
			// trying something crazy
			this.reply(msg, "BAD Can't store that");
			return false;
		}

		if(args.length - offset < 2) {
			this.reply(msg, "BAD Not enough arguments to store flags");
			return false;
		}

		if(args[offset + 1].startsWith("("))
			args[offset + 1] = args[offset + 1].substring(1);

		boolean setFlagTo;
		if(args[offset].startsWith("-")) {
			setFlagTo = false;
		} else if(args[offset].startsWith("+")) {
			setFlagTo = true;
		} else {
			for(MailMessage message : mmsgs) {
				message.flags.clear();
			}
			setFlagTo = true;
		}


		for(int i = offset; i < args.length; i++) {
			String flag = args[i];
			if(flag.endsWith(")")) {
				flag = flag.substring(0, flag.length() - 1);
			}

			for(MailMessage message : mmsgs) {
				message.flags.set(flag, setFlagTo);
				message.storeFlags();
			}
		}

		if(msg.args[offset].toLowerCase(Locale.ROOT).indexOf("silent") < 0) {
			for(MailMessage message : mmsgs) {
				StringBuffer buf = new StringBuffer("");

				buf.append(message.getSeqNum());
				if(senduid) {
					buf.append(" FETCH (UID ");
					buf.append(message.getUID());
					buf.append(" FLAGS (");
					buf.append(message.flags.getFlags());
					buf.append("))");
				} else {

					buf.append(" FETCH FLAGS (");
					buf.append(message.flags.getFlags());
					buf.append(")");
				}

				this.sendState(buf.toString());
			}
		}

		return true;
	}

	private void handleExpunge(IMAPMessage msg) {
		if(!this.verifyAuth(msg)) {
			return;
		}

		if(this.mb == null) {
			this.reply(msg, "NO No mailbox selected");
			return;
		}

		this.expunge(true);
		this.reply(msg, "OK Expunge complete");
	}

	private void handleClose(IMAPMessage msg) {
		if(!this.verifyAuth(msg)) {
			return;
		}

		if(this.mb == null) {
			this.reply(msg, "NO No mailbox selected");
			return;
		}

		this.expunge(false);
		this.mb = null;

		this.reply(msg, "OK Mailbox closed");
	}

	private void expunge(boolean verbose) {
		MailMessage[] mmsgs = this.mb.listMessagesArray();

		int count_correction=0;
		for(int i = 0; i < mmsgs.length; i++) {
			if(mmsgs[i].flags.isDeleted()) {
				mmsgs[i].delete();
				if(verbose) this.sendState((i+1-count_correction)+" EXPUNGE");
				count_correction++;
			}
		}
	}

	private void handleNamespace(IMAPMessage msg) {
		if(!this.verifyAuth(msg)) {
			return;
		}

		this.sendState("NAMESPACE ((\"INBOX.\" \".\")) NIL NIL");
		this.reply(msg, "OK Namespace completed");
	}

	private void handleStatus(IMAPMessage msg) {
		if(!this.verifyAuth(msg)) {
			return;
		}

		if(msg.args == null || msg.args.length < 2) {
			this.reply(msg, "BAD Not enough arguments");
			return;
		}

		String mbname = trimQuotes(msg.args[0]);

		MessageBank statmb = this.getMailboxFromPath(mbname);

		if(statmb == null) {
			this.reply(msg, "NO Could not find mailbox");
			return;
		}

		SortedMap<Integer, MailMessage> msgs = statmb.listMessages();

		// gather statistics
		int numrecent = 0;
		int numunseen = 0;
		int nummessages = msgs.size();
		int lastuid = 0;
		while(msgs.size() > 0) {
			Integer current = msgs.firstKey();
			MailMessage m =msgs.get(msgs.firstKey());

			// if it's recent, add to the tally
			if(m.flags.isRecent()) numrecent++;

			// is it unseen?
			if(!m.flags.isSeen()) numunseen++;

			if(m.getUID() > lastuid) lastuid = m.getUID();

			msgs = msgs.tailMap(new Integer(current.intValue()+1));
		}

		StringBuffer buf = new StringBuffer();
		buf.append("STATUS ");
		buf.append(msg.args[0]);
		buf.append(" (");


		// output the required information
		int i;
		boolean first = true;
		for(i = 1; i < msg.args.length; i++) {
			String arg = msg.args[i];

			if(arg.startsWith("(")) arg = arg.substring(1);
			if(arg.endsWith(")")) arg = arg.substring(0, arg.length() - 1);

			if(!first) buf.append(" ");
			first = false;
			buf.append(arg);
			buf.append(" ");
			if(arg.equalsIgnoreCase("messages")) {
				buf.append(Integer.toString(nummessages));
			} else if(arg.equalsIgnoreCase("recent")) {
				buf.append(Integer.toString(numrecent));
			} else if(arg.equalsIgnoreCase("unseen")) {
				buf.append(Integer.toString(numunseen));
			} else if(arg.equalsIgnoreCase("uidnext")) {
				buf.append(Integer.toString(lastuid + 1));
			} else if(arg.equalsIgnoreCase("uidvalidity")) {
				buf.append("1");
			}
		}

		buf.append(")");
		this.sendState(buf.toString());
		this.reply(msg, "OK STATUS completed");
	}

	private void handleCreate(IMAPMessage msg) {
		if(!this.verifyAuth(msg)) {
			return;
		}

		if(msg.args == null || msg.args.length < 1) {
			this.reply(msg, "BAD Not enough arguments");
			return;
		}

		msg.args[0] = trimQuotes(msg.args[0]);

		if(msg.args[0].endsWith(".")) {
			// ends with a hierarchy delimiter. Ignore it
			this.reply(msg, "OK Nothing done");
			return;
		}

		String[] mbparts = msg.args[0].split("\\.");
		if(!mbparts[0].equalsIgnoreCase("inbox")) {
			this.reply(msg, "NO Invalid mailbox name");
			return;
		}

		if(mbparts.length < 2) {
			this.reply(msg, "NO Inbox already exists!");
			return;
		}

		int i;
		MessageBank tempmb = this.inbox;
		for(i = 1; i < mbparts.length; i++) {
			MessageBank existingmb = tempmb.getSubFolder(mbparts[i]);
			if(existingmb != null) {
				tempmb = existingmb;
			} else {
				tempmb = tempmb.makeSubFolder(mbparts[i]);
				if(tempmb == null) {
					this.reply(msg, "NO couldn't create mailbox");
					return;
				}
			}
		}
		this.reply(msg, "OK Mailbox created");
	}

	private void handleDelete(IMAPMessage msg) {
		if(!this.verifyAuth(msg)) {
			return;
		}

		if(msg.args == null || msg.args.length < 1) {
			this.reply(msg, "BAD Not enough arguments");
			return;
		}

		MessageBank target = getMailboxFromPath(trimQuotes(msg.args[0]));
		if(target == null) {
			this.reply(msg, "NO No such mailbox.");
			return;
		}

		if(target.listSubFolders().length > 0) {
			this.reply(msg, "NO Mailbox has inferiors.");
			return;
		}

		if(target.delete()) {
			this.reply(msg, "OK Mailbox deleted");
		} else {
			this.reply(msg, "NO Unable to delete mailbox");
		}
		return;
	}

	private void handleCopy(IMAPMessage msg) {
		handleCopy(msg, false);
	}

	private void handleCopy(IMAPMessage msg, boolean uid) {
		if(!this.verifyAuth(msg)) {
			return;
		}

		if(this.mb == null) {
			this.reply(msg, "NO No mailbox selected");
			return;
		}

		if(msg.args == null || msg.args.length < 2) {
			this.reply(msg, "BAD Not enough arguments");
			return;
		}

		SortedMap<Integer, MailMessage> msgs = this.mb.listMessages();
		MailMessage lastMessage = msgs.get(msgs.lastKey());

		SortedSet<Integer> ts;
		try {
			ts = parseSequenceSet(msg.args[0], uid ? lastMessage.getUID() : lastMessage.getSeqNum());
		} catch(NumberFormatException e) {
			this.reply(msg, "BAD Illegal sequence number set");
			return;
		} catch (IllegalSequenceNumberException e) {
			this.reply(msg, "NO Invalid message ID");
			return;
		}

		if(!uid) {
			if(ts.first() < 1 || ts.last() > lastMessage.getSeqNum()) {
				reply(msg, "NO Invalid message ID");
				return;
			}
		}

		Iterator<MailMessage> msgIt = msgs.values().iterator();
		while(msgIt.hasNext()) {
			MailMessage message = msgIt.next();
			if(uid) {
				if(!ts.contains(message.getUID())) {
					msgIt.remove();
				}
			} else {
				if(!ts.contains(message.getSeqNum())) {
					msgIt.remove();
				}
			}
		}

		MessageBank target = getMailboxFromPath(trimQuotes(msg.args[1]));
		if(target == null) {
			this.reply(msg, "NO [TRYCREATE] No such mailbox.");
			return;
		}

		for(MailMessage src : msgs.values()) {
			MailMessage copy = target.createMessage();

			src.copyTo(copy);
			copy.flags.set("\\Recent", true);
			copy.storeFlags();
		}
		this.reply(msg, "OK COPY completed");
	}

	private void handleAppend(IMAPMessage msg) {
		if(!this.verifyAuth(msg)) {
			return;
		}

		if(msg.args == null || msg.args.length < 2) {
			this.reply(msg, "BAD Not enough arguments");
			return;
		}

		//args[0] is always the mailbox
		String mbname = trimQuotes(msg.args[0]);

		String sdatalen = "";
		List<String> flags = new LinkedList<String>();

		for(int i = 1; i < msg.args.length; i++) {
			if(msg.args[i].startsWith("(")) {
				if(msg.args[i].endsWith(")")) {
					//Only flag
					flags.add(msg.args[i].substring(1, msg.args[i].length() - 1));
				} else {
					//Add all the flags
					flags.add(msg.args[i].substring(1, msg.args[i].length()));
					i++;
					while(!msg.args[i].endsWith(")")) {
						flags.add(msg.args[i]);
						i++;
					}
					flags.add(msg.args[i].substring(0, msg.args[i].length() - 1));
				}
			} else if(msg.args[i].startsWith("{")) {
				//Data length
				sdatalen = msg.args[i].substring(1, msg.args[i].length() -1);
			}
		}

		int datalen;
		try {
			datalen = Integer.parseInt(sdatalen);
		} catch (NumberFormatException nfe) {
			this.reply(msg, "BAD Unable to parse literal length");
			return;
		}

		MessageBank destmb = this.getMailboxFromPath(mbname);
		if(destmb == null) {
			this.reply(msg, "NO [TRYCREATE] No such mailbox");
			return;
		}

		MailMessage newmsg = destmb.createMessage();
		this.ps.print("+ OK\r\n");
		try {
			PrintStream msgps = newmsg.getRawStream();

			String line;
			int bytesread = 0;
			while((line = this.bufrdr.readLine()) != null) {
				msgps.println(line);

				bytesread += line.getBytes("UTF-8").length;
				bytesread += "\r\n".length();

				if(bytesread >= datalen) break;
			}

			newmsg.commit();
		} catch (IOException ioe) {
			this.reply(msg, "NO Failed to write message");
			newmsg.cancel();
			return;
		}

		for(String flag : flags) {
			newmsg.flags.set(flag, true);
		}
		newmsg.storeFlags();
		this.reply(msg, "OK APPEND completed");
	}

	private void handleSearch(IMAPMessage msg) {
		handleSearch(msg, false);
	}

	private void handleSearch(IMAPMessage msg, boolean uid) {
		if(!this.verifyAuth(msg)) {
			return;
		}

		if(this.mb == null) {
			this.reply(msg, "NO No mailbox selected");
			return;
		}

		if(msg.args.length < 1) {
			reply(msg, "BAD Missing arguments for SEARCH command");
			return;
		}

		if(msg.args[0].equalsIgnoreCase("CHARSET")) {
			reply(msg, "NO [BADCHARSET] Freemail doesn't support specifying CHARSET");
			return;
		}

		Map<Integer, MailMessage> messages = mb.listMessages();
		try {
			for(MailMessage message : messages.values()) {
				message.readHeaders();
			}
		} catch(IOException e) {
			sendState("BAD Internal server error while searching messages");
			reply(msg, "NO Internal server error while searching messages");
		}

		{
			/*
			 * If there is only a single parenthetical expression which encloses the entire search,
			 * it is only syntactic and does not increase the complexity. Therefore allow it by removing the
			 * parentheses before further processing.
			 * Commons Lang StringUtils.countMatches() would make this nicer.
			 */
			int parenthesisCount = 0;
			for(final String arg : msg.args) {
				for(final char character : arg.toCharArray()) {
					if(character == '(' || character == ')') {
						parenthesisCount++;
					}
				}
			}

			final String firstArg = msg.args[0];
			final int lastArgIndex = msg.args.length - 1;
			String lastArg = msg.args[lastArgIndex];
			if(parenthesisCount == 2 && firstArg.startsWith("(") && lastArg.endsWith(")")) {
				// Remove parenthesis: first character from first arg, last from last.
				msg.args[0] = firstArg.substring(1);

				lastArg = msg.args[lastArgIndex];
				msg.args[lastArgIndex] = lastArg.substring(0, lastArg.length() - 1);

				/*
				 * If anything were parsed such that it's only a parenthesis, it would be empty now. That's not
				 * desirable, so remove it from the argument list.
				 */
				final boolean firstEmpty = msg.args[0].isEmpty();
				final boolean lastEmpty = msg.args[lastArgIndex].isEmpty();
				msg = new IMAPMessage(msg.tag, msg.type, Arrays.copyOfRange(msg.args, firstEmpty ? 1 : 0,
						lastEmpty ? lastArgIndex : msg.args.length));
			}
		}

		//Index of the next search key
		int offset = 0;
		while(offset < msg.args.length) {
			//If it starts or ends with a paran, fail
			if(msg.args[offset].startsWith("(") || msg.args[offset].endsWith(")")) {
				reply(msg, "NO Freemail doesn't support parentheses in search yet");
				return;
			}

			if(msg.args[offset].equalsIgnoreCase("ALL")) {
				//Matches all messages, so do nothing
				offset++;
				continue;
			}

			//Check the various flag state filters
			if(msg.args[offset].equalsIgnoreCase("ANSWERED")) {
				filterMessagesOnFlag(messages.values(), "\\Answered", true);
				offset++;
				continue;
			}

			if(msg.args[offset].equalsIgnoreCase("DELETED")) {
				filterMessagesOnFlag(messages.values(), "\\Deleted", true);
				offset++;
				continue;
			}

			if(msg.args[offset].equalsIgnoreCase("FLAGGED")) {
				filterMessagesOnFlag(messages.values(), "\\Flagged", true);
				offset++;
				continue;
			}

			if(msg.args[offset].equalsIgnoreCase("RECENT")) {
				filterMessagesOnFlag(messages.values(), "\\Recent", true);
				offset++;
				continue;
			}

			if(msg.args[offset].equalsIgnoreCase("SEEN")) {
				filterMessagesOnFlag(messages.values(), "\\Seen", true);
				offset++;
				continue;
			}

			if(msg.args[offset].equalsIgnoreCase("UNANSWERED")) {
				filterMessagesOnFlag(messages.values(), "\\Answered", false);
				offset++;
				continue;
			}

			if(msg.args[offset].equalsIgnoreCase("UNDELETED")) {
				filterMessagesOnFlag(messages.values(), "\\Deleted", false);
				offset++;
				continue;
			}

			if(msg.args[offset].equalsIgnoreCase("UNFLAGGED")) {
				filterMessagesOnFlag(messages.values(), "\\Flagged", false);
				offset++;
				continue;
			}

			if(msg.args[offset].equalsIgnoreCase("UNSEEN")) {
				filterMessagesOnFlag(messages.values(), "\\Seen", false);
				offset++;
				continue;
			}

			if(msg.args[offset].equalsIgnoreCase("DRAFT")) {
				filterMessagesOnFlag(messages.values(), "\\Draft", true);
				offset++;
				continue;
			}

			if(msg.args[offset].equalsIgnoreCase("UNDRAFT")) {
				filterMessagesOnFlag(messages.values(), "\\Draft", false);
				offset++;
				continue;
			}

			if(msg.args[offset].equalsIgnoreCase("KEYWORD")) {
				filterMessagesOnFlag(messages.values(), msg.args[offset + 1], true);
				offset += 2;
				continue;
			}

			if(msg.args[offset].equalsIgnoreCase("UNKEYWORD")) {
				filterMessagesOnFlag(messages.values(), msg.args[offset + 1], false);
				offset += 2;
				continue;
			}

			//Header searches
			if(msg.args[offset].equalsIgnoreCase("BCC")) {
				String searchString = msg.args[offset + 2];
				filterMessagesOnHeader(messages.values(), "BCC", searchString);
				offset += 2;
				continue;
			}

			if(msg.args[offset].equalsIgnoreCase("CC")) {
				String searchString = msg.args[offset + 1];
				filterMessagesOnHeader(messages.values(), "CC", searchString);
				offset += 2;
				continue;
			}

			if(msg.args[offset].equalsIgnoreCase("FROM")) {
				String searchString = msg.args[offset + 1];
				filterMessagesOnHeader(messages.values(), "FROM", searchString);
				offset += 2;
				continue;
			}

			if(msg.args[offset].equalsIgnoreCase("SUBJECT")) {
				String searchString = msg.args[offset + 1];
				filterMessagesOnHeader(messages.values(), "SUBJECT", searchString);
				offset += 2;
				continue;
			}

			if(msg.args[offset].equalsIgnoreCase("TO")) {
				String searchString = msg.args[offset + 1];
				filterMessagesOnHeader(messages.values(), "TO", searchString);
				offset += 2;
				continue;
			}

			if(msg.args[offset].equalsIgnoreCase("HEADER")) {
				String headerName = msg.args[offset + 1];
				String searchString = msg.args[offset + 2];
				filterMessagesOnHeader(messages.values(), headerName, searchString);
				offset += 3;
				continue;
			}

			//For now we don't support any of the rest
			reply(msg, "NO Criteria " + msg.args[offset] + " hasn't been implemented");
			return;
		}

		//Handled all the criteria, so lets send the results back
		StringBuilder reply = new StringBuilder("SEARCH");
		for(MailMessage message : messages.values()) {
			if(uid) {
				reply.append(" " + message.getUID());
			} else {
				reply.append(" " + message.getSeqNum());
			}
		}
		sendState(reply.toString());
		reply(msg, "OK Search completed");
	}

	private void filterMessagesOnFlag(Collection<MailMessage> messages, String flag, boolean state) {
		Iterator<MailMessage> it = messages.iterator();
		while(it.hasNext()) {
			if(it.next().flags.get(flag) != state) {
				it.remove();
			}
		}
	}

	private void filterMessagesOnHeader(Collection<MailMessage> messages, String headerName, String searchString) {
		Iterator<MailMessage> it = messages.iterator();
		while(it.hasNext()) {
			boolean found = false;
			for(String headerValue : it.next().getHeadersByName(headerName)) {
				if(headerValue.toLowerCase(Locale.ROOT).contains(searchString.toLowerCase(Locale.ROOT))) {
					found = true;
					break;
				}
			}
			if(!found) {
				it.remove();
			}
		}
	}

	private String getEnvelope(MailMessage mmsg) {
		StringBuffer buf = new StringBuffer("(");

		try {
			mmsg.readHeaders();
		} catch (IOException ioe) {
			//FIXME: Handle IOException properly
			Logger.error(this, "Caught IOException while reading message headers: " + ioe.getMessage(), ioe);
		}

		buf.append(IMAPifyString(mmsg.getFirstHeader("Date"))+" ");
		buf.append(IMAPifyString(mmsg.getFirstHeader("Subject"))+" ");
		// from
		buf.append(this.IMAPifyAddress(mmsg.getFirstHeader("From"))+" ");
		// sender (this should probably be the Freemail address that
		// we got it from, except I haven't found a mail client that
		// actually uses this part yet, so it might be pointless
		buf.append(this.IMAPifyAddress(mmsg.getFirstHeader("x-freemail-sender"))+" ");
		buf.append(this.IMAPifyAddress(mmsg.getFirstHeader("Reply-To"))+" ");

		buf.append(this.IMAPifyAddress(mmsg.getFirstHeader("To"))+" ");
		buf.append(this.IMAPifyAddress(mmsg.getFirstHeader("CC"))+" ");
		buf.append(this.IMAPifyAddress(mmsg.getFirstHeader("BCC"))+" ");
		buf.append(IMAPifyString(mmsg.getFirstHeader("In-Reply-To"))+" ");
		buf.append(IMAPifyString(mmsg.getFirstHeader("Message-ID")));
		buf.append(")");

		return buf.toString();
	}

	private String IMAPifyString(String in) {
		if(in == null) return "NIL";
		return "\""+in.trim()+"\"";
	}

	private String IMAPifyAddress(String address) {
		if(address == null || address.length() == 0) return "NIL";

		EmailAddress addr = new EmailAddress(address);

		String retval = "((";
		retval += this.IMAPifyString(addr.realname)+" ";
		// SMTP Source Route. Whatever this is, it's not relevant!
		retval += "NIL ";
		retval += this.IMAPifyString(addr.user)+" ";
		retval += this.IMAPifyString(addr.domain);
		retval += "))";

		return retval;
	}

	private void reply(IMAPMessage msg, String reply) {
		Logger.debug(this, "Reply: " + msg.tag + " " + reply);
		this.ps.print(msg.tag + " " + reply + "\r\n");
	}

	private void sendState(String txt) {
		Logger.debug(this, "Reply: * " + txt);
		this.ps.print("* "+txt+"\r\n");
	}

	private static String trimQuotes(String in) {
		if(in.length() == 0) return in;
		if(in.charAt(0) == '"') {
			in = in.substring(1);
		}
		if(in.charAt(in.length() - 1) == '"') {
			in = in.substring(0, in.length() - 1);
		}
		return in;
	}

	private boolean verifyAuth(IMAPMessage msg) {
		if(this.inbox == null) {
			this.reply(msg, "NO Must be authenticated");
			return false;
		}
		return true;
	}

	private SortedSet<Integer> parseSequenceSet(String seqNum, int maxSeqNum) throws IllegalSequenceNumberException {
		SortedSet<Integer> result = new TreeSet<Integer>();

		//Split on , to get the ranges
		for(String range : seqNum.split(",")) {
			String fromSeqNum;
			String toSeqNum;

			if(!range.contains(":")) {
				fromSeqNum = range;
				toSeqNum = range;
			} else {
				String[] parts = range.split(":");
				if(parts.length != 2) {
					throw new NumberFormatException();
				}

				fromSeqNum = parts[0];
				toSeqNum = parts[1];
			}

			int from = parseSequenceNumber(fromSeqNum, maxSeqNum);
			int to = parseSequenceNumber(toSeqNum, maxSeqNum);
			if(from <= 0 || to <= 0) {
				throw new IllegalSequenceNumberException("Sequence number must be greater than zero");
			}

			if(from > to) {
				int temp = from;
				from = to;
				to = temp;
			}

			for(int i = from; i <= to; i++) {
				result.add(i);
			}
		}

		return result;
	}

	private int parseSequenceNumber(String seqNum, int maxSeqNum) {
		if(seqNum.equals("*")) {
			return maxSeqNum;
		}

		return Integer.parseInt(seqNum);
	}

	private class IllegalSequenceNumberException extends Exception {
		public IllegalSequenceNumberException(String msg) {
			super(msg);
		}

		private static final long serialVersionUID = 5604708058788273676L;
	}
}

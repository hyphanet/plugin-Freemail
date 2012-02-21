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

package freemail.imap;

import java.net.Socket;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeSet;
import java.lang.NumberFormatException;
import java.text.SimpleDateFormat;
import java.util.Date;

import freemail.FreemailAccount;
import freemail.MessageBank;
import freemail.MailMessage;
import freemail.AccountManager;
import freemail.ServerHandler;
import freemail.utils.EmailAddress;
import freemail.utils.Logger;

public class IMAPHandler extends ServerHandler implements Runnable {
	private static final String CAPABILITY = "IMAP4rev1 CHILDREN NAMESPACE";

	private final OutputStream os;
	private final PrintStream ps;
	private final BufferedReader bufrdr;
	private MessageBank mb;
	private MessageBank inbox;
	private final AccountManager accountManager;

	IMAPHandler(AccountManager accMgr, Socket client) throws IOException {
		super(client);
		accountManager = accMgr;
		this.os = client.getOutputStream();
		this.ps = new PrintStream(this.os);
		this.bufrdr = new BufferedReader(new InputStreamReader(client.getInputStream()));
		this.mb = null;
	}
	
	@Override
	public void run() {
		this.sendWelcome();
		
		String line;
		try {
			while ( !this.client.isClosed() && (line = this.bufrdr.readLine()) != null) {
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
			Logger.error(this, "Caught IOException while reading imap data: " + ioe.getMessage(), ioe);
		}
	}
	
	private void sendWelcome() {
		this.ps.print("* OK [CAPABILITY "+CAPABILITY+"] Freemail ready - hit me with your rhythm stick.\r\n");
	}
	
	private void dispatch(IMAPMessage msg) {
		Logger.debug(this, "Received: " + msg);
		if (msg.type.equals("login")) {
			this.handle_login(msg);
		} else if (msg.type.equals("logout")) {
			this.handle_logout(msg);
		} else if (msg.type.equals("capability")) {
			this.handle_capability(msg);
		} else if (msg.type.equals("list")) {
			this.handle_list(msg);
		} else if (msg.type.equals("select")) {
			this.handle_select(msg);
		} else if (msg.type.equals("noop")) {
			this.handle_noop(msg);
		} else if (msg.type.equals("check")) {
			this.handle_check(msg);
		} else if (msg.type.equals("uid")) {
			this.handle_uid(msg);
		} else if (msg.type.equals("fetch")) {
			this.handle_fetch(msg);
		} else if (msg.type.equals("store")) {
			this.handle_store(msg);
		} else if (msg.type.equals("close")) {
			this.handle_close(msg);
		} else if (msg.type.equals("expunge")) {
			this.handle_expunge(msg);
		} else if (msg.type.equals("namespace")) {
			this.handle_namespace(msg);
		} else if (msg.type.equals("lsub")) {
			this.handle_lsub(msg);
		} else if (msg.type.equals("status")) {
			this.handle_status(msg);
		} else if (msg.type.equals("create")) {
			this.handle_create(msg);
		} else if (msg.type.equals("delete")) {
			this.handle_delete(msg);
		} else if (msg.type.equals("copy")) {
			this.handle_copy(msg);
		} else if (msg.type.equals("append")) {
			this.handle_append(msg);
		} else {
			Logger.error(this, "Unknown IMAP command: " + msg.type);
			this.reply(msg, "NO Sorry - not implemented");
		}
	}
	
	private void handle_login(IMAPMessage msg) {
		if (msg.args == null || msg.args.length < 2) {
			this.reply(msg, "BAD Not enough arguments");
			return;
		}
		
		FreemailAccount account = accountManager.authenticate(trimQuotes(msg.args[0]), trimQuotes(msg.args[1]));
		if (account != null) {
			this.inbox = account.getMessageBank();
			
			this.reply(msg, "OK Logged in");
		} else {
			this.reply(msg, "NO Login failed");
		}
	}
	
	private void handle_logout(IMAPMessage msg) {
		this.sendState("BYE");
		this.reply(msg, "OK Bye");
		try {
			this.client.close();
		} catch (IOException ioe) {
			Logger.error(this, "Caugth IOException while closing socket: " + ioe.getMessage(), ioe);
		}
	}
	
	private void handle_capability(IMAPMessage msg) {
		this.sendState("CAPABILITY "+CAPABILITY);
		
		this.reply(msg, "OK Capability completed");
	}
	
	private void handle_lsub(IMAPMessage msg) {
		this.handle_list(msg);
	}
	
	private void handle_list(IMAPMessage msg) {
		String refname;
		String mbname;
		
		if (!this.verify_auth(msg)) {
			return;
		}
		
		if (msg.args == null || msg.args.length < 1) {
			refname = null;
			mbname = null;
		} else if (msg.args.length < 2) {
			refname = msg.args[0];
			mbname = null;
		} else {
			refname = msg.args[0];
			mbname = msg.args[1];
		}
		
		String replyprefix = "LIST";
		if (msg.type.equals("lsub")) {
			replyprefix = "LSUB";
		}
		
		if (refname != null) refname = trimQuotes(refname);
		if (refname!= null && refname.length() == 0) refname = null;
		
		if (mbname != null) mbname = trimQuotes(mbname);
		if (mbname != null && mbname.length() == 0) mbname = null;
		
		if (mbname == null) {
			// return hierarchy delimiter
			this.sendState(replyprefix+" (\\Noselect) \".\" \"\"");
		} else {
			// transform mailbox name into a regex
			
			// '*' needs to be '.*'
			mbname = mbname.replaceAll("\\*", ".*");
			
			// and % is a wildcard not including the hierarchy delimiter
			mbname = mbname.replaceAll("%", "[^\\.]*");
			
			
			this.list_matching_folders(this.inbox, mbname, replyprefix, "INBOX.");
			
			/// and send the inbox too, if it matches
			if ("INBOX".matches(mbname)) {
				this.sendState(replyprefix+" "+this.inbox.getFolderFlagsString()+" \".\" \"INBOX\"");
			}
		}
		
		this.reply(msg, "OK "+replyprefix+" completed");
	}
	
	private void list_matching_folders(MessageBank folder, String pattern, String replyprefix, String folderpath) {
		MessageBank[] folders = folder.listSubFolders();
			
		for (int i = 0; i < folders.length; i++) {
			String fullpath = folderpath+folders[i].getName();
			
			this.list_matching_folders(folders[i], pattern, replyprefix, fullpath+".");
			if (fullpath.matches(pattern)) {
				this.sendState(replyprefix+" "+folders[i].getFolderFlagsString()+" \".\" \""+fullpath+"\"");
			}
		}
	}
	
	private MessageBank getMailboxFromPath(String path) {
		MessageBank tempmb = this.inbox;
		
		String[] mbparts = path.split("\\.");
		
		if (!mbparts[0].equalsIgnoreCase("inbox")) {
			return null;
		}
		
		int i;
		for (i = 1; i < mbparts.length; i++) {
			tempmb = tempmb.getSubFolder(mbparts[i]);
			if (tempmb == null) {
				return null;
			}
		}
		
		return tempmb;
	}
	
	private void handle_select(IMAPMessage msg) {
		String mbname;
		
		if (!this.verify_auth(msg)) {
			return;
		}
		
		if (msg.args == null || msg.args.length < 1) {
			this.reply(msg, "NO What mailbox?");
			return;
		}
		
		mbname = trimQuotes(msg.args[0]);
		
		MessageBank tempmb = this.getMailboxFromPath(mbname);
		
		if (tempmb == null) {
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
		while (msgs.size() > 0) {
			Integer current = msgs.firstKey();
			MailMessage m =msgs.get(msgs.firstKey());
				
			// if it's recent, add to the tally
			if (m.flags.get("\\Recent")) numrecent++;
				
			// remove the recent flag
			m.flags.set("\\Recent", false);
			m.storeFlags();
				
			msgs = msgs.tailMap(new Integer(current.intValue()+1));
		}
			
		this.sendState(numexists+" EXISTS");
		this.sendState(numrecent+" RECENT");
			
		this.sendState("OK [UIDVALIDITY " + mb.getUidValidity() + "] Ok");
			
		this.reply(msg, "OK [READ-WRITE] Done");
	}
	
	private void handle_noop(IMAPMessage msg) {
		this.reply(msg, "OK NOOP completed");
	}
	
	private void handle_check(IMAPMessage msg) {
		if(!this.verify_auth(msg)) {
			return;
		}

		if(this.mb == null) {
			this.reply(msg, "NO No mailbox selected");
			return;
		}

		this.reply(msg, "OK Check completed");
	}
	
	private void handle_fetch(IMAPMessage msg) {
		int from;
		int to;
		
		if (!this.verify_auth(msg)) {
			return;
		}
		
		if (this.mb == null) {
			this.reply(msg, "NO No mailbox selected");
			return;
		}
		
		SortedMap<Integer, MailMessage> msgs = this.mb.listMessages();
		
		if (msgs.size() == 0) {
			this.reply(msg, "OK Fetch completed");
			return;
		}
		
		if (msg.args == null || msg.args.length < 2) {
			this.reply(msg, "BAD Not enough arguments");
			return;
		}
		
		String[] parts = msg.args[0].split(":");
		try {
			from = Integer.parseInt(parts[0]);
		} catch (NumberFormatException nfe) {
			this.reply(msg, "BAD Bad number: "+parts[0]+". Please report this error!");
			return;
		}
		if (parts.length < 2) {
			to = from;
		} else if (parts[1].equals("*")) {
			to = msgs.size();
		} else {
			try {
				to = Integer.parseInt(parts[1]);
			} catch (NumberFormatException nfe) {
				this.reply(msg, "BAD Bad number: "+parts[1]+". Please report this error!");
				return;
			}
		}
		
		if (from == 0 || to == 0 || from > msgs.size() || to > msgs.size()) {
			this.reply(msg, "NO Invalid message ID");
			return;
		}
		
		for (int i = 1; msgs.size() > 0; i++) {
			Integer current = msgs.firstKey();
			if (i < from) {
				msgs = msgs.tailMap(new Integer(current.intValue()+1));
				continue;
			}
			if (i > to) break;
			
			if (!this.fetch_single(msgs.get(msgs.firstKey()), msg.args, 1, false)) {
				this.reply(msg, "BAD Unknown attribute in list or unterminated list");
				return;
			}
			
			msgs = msgs.tailMap(new Integer(current.intValue()+1));
		}
		
		this.reply(msg, "OK Fetch completed");
	}
	
	private void handle_uid(IMAPMessage msg) {
		int from;
		int to;
		
		if (msg.args == null || msg.args.length < 3) {
			this.reply(msg, "BAD Not enough arguments to uid command");
			return;
		}
		
		if (!this.verify_auth(msg)) {
			return;
		}
		
		if (this.mb == null) {
			this.reply(msg, "NO No mailbox selected");
			return;
		}
		
		SortedMap<Integer, MailMessage> msgs = this.mb.listMessages();
		
		if (msgs.size() == 0) {
			if (msg.args[0].toLowerCase().equals("fetch")) {
				this.reply(msg, "OK Fetch completed");
			} else if (msg.args[0].toLowerCase().equals("store")) {
				// hmm...?
				this.reply(msg, "NO No such message");
			}
			return;
		}

		// until a proper search function is implemented we could return an empty
		// result, but this confuses Thunderbird
//		if (msg.args[0].toLowerCase().equals("search")) {
//			// return a dummy result
//			this.sendState("SEARCH");
//			this.reply(msg, "OK SEARCH completed");
//			return;
//		}
			
		// build a set from the uid ranges, first separated by , then by :
		// if that fails, its probably an unsupported command

		TreeSet<Integer> ts=new TreeSet<Integer>();
		try {
			String[] rangeparts = msg.args[1].split(",");
		
			for(int i=0;i<rangeparts.length;i++) {
				String vals[]=rangeparts[i].split(":");
				if(vals.length==1) {
					ts.add(new Integer(vals[0]));
				} else {
					int maxMsgNum = msgs.lastKey().intValue();
					from = parseSequenceNumber(vals[0], maxMsgNum);
					to = parseSequenceNumber(vals[1], maxMsgNum);

					if(from > to) {
						int temp = to;
						to = from;
						from = temp;
					}

					for(int j=from;j<=to;j++) {
						ts.add(new Integer(j));
					}
				}
			}
		}
		catch(NumberFormatException ex) {
			this.reply(msg, "BAD Unknown command");
			return;
		}

		if (msg.args[0].equalsIgnoreCase("fetch")) {

			Iterator<Integer> it=ts.iterator();
			
			while(it.hasNext()) {
				Integer curuid = it.next();

				MailMessage mm=msgs.get(curuid);
				
				if(mm!=null) {
					if (!this.fetch_single(msgs.get(curuid), msg.args, 2, true)) {
						this.reply(msg, "BAD Unknown attribute in list or unterminated list");
						return;
					}
				}
			}
			
			this.reply(msg, "OK Fetch completed");
		} else if (msg.args[0].equalsIgnoreCase("store")) {
			MailMessage[] targetmsgs = new MailMessage[ts.size()];

			Iterator<Integer> it=ts.iterator();

			int count=0;
			while(it.hasNext()) {
				Integer curuid = it.next();
				MailMessage m=msgs.get(curuid);
				if(m!=null) {
					targetmsgs[count] = m;
					count++;
				}
			}
			if(count>0) {
				if(count<ts.size()) {
					MailMessage[] t = new MailMessage[count];
					for(int i=0;i<count;i++) {
						t[i]=targetmsgs[i];
					}
					targetmsgs=t;
				}
				this.do_store(msg.args, 2, targetmsgs, msg, true);
			}

			this.reply(msg, "OK Store completed");
		} else if (msg.args[0].equalsIgnoreCase("copy")) {

			if (msg.args.length < 3) {
				this.reply(msg, "BAD Not enough arguments");
				return;
			}
			
			MessageBank target = getMailboxFromPath(trimQuotes(msg.args[2]));
			if (target == null) {
				this.reply(msg, "NO [TRYCREATE] No such mailbox.");
				return;
			}
			
			int copied = 0;

			Iterator<Integer> it=ts.iterator();

			while(it.hasNext()) {
				Integer curuid = it.next();
								
				MailMessage srcmsg = msgs.get(curuid);
				
				if(srcmsg!=null) {
					MailMessage copymsg = target.createMessage();
					srcmsg.copyTo(copymsg);
					
					copied++;
				}
			}
			
			if (copied > 0)
				this.reply(msg, "OK COPY completed");
			else
				this.reply(msg, "NO No messages copied");
		} else {
			this.reply(msg, "BAD Unknown command");
		}
	}

	private int parseSequenceNumber(String seqNum, int maxMessageNum) {
		if(seqNum.equals("*")) {
			return maxMessageNum;
		} else {
			return Integer.parseInt(seqNum);
		}
	}
	
	private boolean fetch_single(MailMessage msg, String[] args, int firstarg, boolean send_uid_too) {
		String[] imap_args = args.clone();
		this.ps.print("* "+msg.getSeqNum()+" FETCH (");
		
		// do the first attribute, if it's a loner.
		if (!imap_args[firstarg].startsWith("(")) {
			// It's a loner
			this.ps.flush();
			if (!this.send_attr(msg, imap_args[firstarg])){
				// send fake end delimiter, so we do not break the protocol
				this.ps.print(")\r\n");
				this.ps.flush();
				return false;
			}
			if (send_uid_too && !imap_args[firstarg].equalsIgnoreCase("uid")) {
				this.ps.print(" UID "+msg.getUID());
			}
			
			this.ps.print(")\r\n");
			this.ps.flush();
			
			return true;
		} else {
			imap_args[firstarg] = imap_args[firstarg].substring(1);
		}
		
		// go through the parenthesized list
		for (int i = firstarg; i < imap_args.length; i++) {
			String attr;
			boolean finish = false;
			
			if (imap_args[i].endsWith(")")) {
				finish = true;
				attr = imap_args[i].substring(0, imap_args[i].length() - 1);
			} else {
				attr = imap_args[i];
			}
			
			//this.ps.print(attr+" ");
			this.ps.flush();
			if (!this.send_attr(msg, attr)) {
				// send fake end delimiter, so we do not break the protocol
				this.ps.print(")\r\n");
				this.ps.flush();
				return false;
			}
			
			if (attr.equalsIgnoreCase("uid")) {
				send_uid_too = false;
			}
			
			if (finish) {
				if (send_uid_too) {
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
		if (send_uid_too) {
			this.ps.print(" UID "+msg.getUID());
		}
		this.ps.print(")\r\n");
		this.ps.flush();

		return false;
	}
	
	private boolean send_attr(MailMessage mmsg, String a) {
		String attr = a.toLowerCase();
		String val = null;
		
		if (attr.equals("uid")) {
			val = Integer.toString(mmsg.getUID());
		} else if (attr.equals("flags")) {
			val = "(" + mmsg.flags.getFlags() + ")";
		} else if (attr.equals("rfc822.size")) {
			try {
				val = Long.toString(mmsg.getSize());
			} catch (IOException ioe) {
				val = "0";
			}
		} else if (attr.equals("envelope")) {
			val = this.getEnvelope(mmsg);
		} else if (attr.startsWith("body.peek")) {
			this.ps.print(a.substring(0, "body".length()));
			this.ps.flush();
			a = a.substring("body.peek".length());
			return this.sendBody(mmsg, a);
		} else if (attr.startsWith("bodystructure")) {
			// TODO: we blatantly lie about the message structure
			this.ps.print(a.substring(0, "bodystructure".length()));
			this.ps.print(" (\"TEXT\" \"PLAIN\" (\"CHARSET\" \"ISO-8859-1\") NIL NIL \"8BIT\" 1024 10)");
			return true;
		} else if (attr.startsWith("body")) {
			// TODO: this is not quite right since it will match bodyanything
			mmsg.flags.set("\\Seen", true);
			
			this.ps.print(a.substring(0, "body".length()));
			this.ps.flush();
			a = a.substring("body".length());
			if (this.sendBody(mmsg, a)) {
				mmsg.flags.set("\\Seen", true);
				mmsg.storeFlags();
				return true;
			}
			return false;
		} else if (attr.startsWith("rfc822.header")) {
			this.ps.print(a.substring(0, "rfc822.header".length()));
			this.ps.flush();
			return this.sendBody(mmsg, "header");
		} else if (attr.startsWith("internaldate")) {
			/*
			 * FIXME: Internaldate should not return Date from the message
			 * For messages received though SMTP we want the date when we received it, for messages
			 * added by COPY it should be the internal date of the source message, and for messages
			 * added by APPEND it should either be the specified date or the date of the APPEND.
			 * See RFC 3501 section 2.3.3 (Internal Date Message Attribute).
			 */
			val = mmsg.getFirstHeader("Date");
			if (val == null) {
				// possibly should keep our own dates...
				SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z");
				
				val = sdf.format(new Date());
			}
			val = "\""+val+"\"";
		}
		
		if (val == null)
			return false;
		this.ps.print(a+" "+val);
		return true;
	}
	
	private boolean sendBody(MailMessage mmsg, String attr) {
		if (attr.length() < 1) return false;
		
		// handle byte ranges (e.g. body.peek[]<0.10240>

		int range_start=-1;
		int range_len=-1;

		if(attr.matches(".*<\\d+\\.\\d+>$")) {
			String range=attr.substring(attr.indexOf("<")+1,attr.length()-1);
			attr=attr.substring(0, attr.indexOf("<"));
			
			String r_start=range.substring(0,range.indexOf("."));
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
		
 		if (attr.charAt(0) == '[') attr = attr.substring(1);
		if (attr.charAt(attr.length() - 1) == ']')
			attr = attr.substring(0, attr.length() - 1);
		
		if (attr.trim().length() == 0) {
			try {
				this.ps.print("[]");
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
				while ( (line = mmsg.readLine()) != null) {
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
				mmsg.closeStream();
			} catch (IOException ioe) {
				return false;
			}
			return true;
		}
		
		StringBuffer buf = new StringBuffer("");
		
		String[] parts = IMAPMessage.doSplit(attr, '(', ')');
		if (parts.length > 0) {
			if (parts[0].equalsIgnoreCase("header.fields")) {
				this.ps.print("[HEADER.FIELDS "+parts[1]+"]");
				if (parts[1].charAt(0) == '(')
					parts[1] = parts[1].substring(1);
				if (parts[1].charAt(parts[1].length() - 1) == ')')
					parts[1] = parts[1].substring(0, parts[1].length() - 1);
				
				try {
					mmsg.readHeaders();
				} catch (IOException ioe) {
					//FIXME: Handle IOException properly
					Logger.error(this, "Caught IOException while reading message headers: " + ioe.getMessage(), ioe);
				}
				
				String[] fields = parts[1].split(" ");
				for (int j = 0; j < fields.length; j++) {
					buf.append(mmsg.getHeaders(fields[j]));
				}
				buf.append("\r\n");
			} else if (parts[0].equalsIgnoreCase("header")) {
				// send all the header fields
				try {
					mmsg.readHeaders();
				} catch (IOException ioe) {
					//FIXME: Handle IOException properly
					Logger.error(this, "Caught IOException while reading message headers: " + ioe.getMessage(), ioe);
				}
				
				buf.append(mmsg.getAllHeadersAsString());
				buf.append("\r\n");
			} else if (parts[0].equalsIgnoreCase("text")) {
				// just send the text of the message without headers
				mmsg.closeStream();
				String line;
				// fast forward past the headers
				try {
					while ( (line = mmsg.readLine()) != null) {
						if (line.length() == 0) break;
					}
					while ( (line = mmsg.readLine()) != null) {
						buf.append(line+"\r\n");
					}
					mmsg.closeStream();
				} catch (IOException ioe) {
					// just return whatever we got
				}
			}
			
			this.ps.print(" {"+buf.length()+"}\r\n"+buf.toString());
			return true;
		}
		
		return false;
	}
	
	private void handle_store(IMAPMessage msg) {
		if (msg.args == null || msg.args.length < 2) {
			this.reply(msg, "BAD Not enough arguments");
			return;
		}
		
		if (!this.verify_auth(msg)) {
			return;
		}
		
		if (this.mb == null) {
			this.reply(msg, "NO No mailbox selected");
			return;
		}
		
		String rangeparts[] = msg.args[0].split(":");
		
		Object[] allmsgs = this.mb.listMessages().values().toArray();
		
		int from;
		int to;
		try {
			from = Integer.parseInt(rangeparts[0]);
		} catch (NumberFormatException nfe) {
			this.reply(msg, "BAD That's not a number!");
			return;
		}
		if (rangeparts.length > 1) {
			if (rangeparts[1].equals("*")) {
				to = allmsgs.length;
			} else {
				try {
					to = Integer.parseInt(rangeparts[1]);
				} catch (NumberFormatException nfe) {
					this.reply(msg, "BAD That's not a number!");
					return;
				}
			}
		} else {
			to = from;
		}
		
		// convert to zero based array
		from--;
		to--;
		
		if (from < 0 || to < 0 || from > allmsgs.length || to > allmsgs.length) {
			this.reply(msg, "NO No such message");
			return;
		}
		
		MailMessage[] msgs = new MailMessage[(to - from) + 1];
		for (int i = from; i <= to; i++) {
			msgs[i - from] = (MailMessage) allmsgs[i];
		}
		
		if(!do_store(msg.args, 1, msgs, msg, false)) {
			return;
		}
		
		this.reply(msg, "OK Store completed");
	}
	
	private boolean do_store(String[] args, int offset, MailMessage[] mmsgs, IMAPMessage msg, boolean senduid) {
		if (args[offset].toLowerCase().indexOf("flags") < 0) {
			// IMAP4Rev1 can only store flags, so you're
			// trying something crazy
			this.reply(msg, "BAD Can't store that");
			return false;
		}
		
		if (args[offset + 1].startsWith("("))
			args[offset + 1] = args[offset + 1].substring(1);
		
		boolean setFlagTo;
		if (args[offset].startsWith("-")) {
			setFlagTo = false;
		} else if (args[offset].startsWith("+")) {
			setFlagTo = true;
		} else {
			for (int i = 0; i < mmsgs.length; i++) {
				mmsgs[i].flags.clear();
			}
			setFlagTo = true;
		}
		
		
		for (int i = offset; i < args.length; i++) {
			String flag = args[i];
			if (flag.endsWith(")")) {
				flag = flag.substring(0, flag.length() - 1);
			}
			
			for (int j = 0; j < mmsgs.length; j++) {
				mmsgs[j].flags.set(flag, setFlagTo);
				mmsgs[j].storeFlags();
			}
		}
		
		if (msg.args[offset].toLowerCase().indexOf("silent") < 0) {
			for (int i = 0; i < mmsgs.length; i++) {
				StringBuffer buf = new StringBuffer("");
				
				buf.append(mmsgs[i].getSeqNum());
				if (senduid) {
					buf.append(" FETCH (UID ");
					buf.append(mmsgs[i].getUID());
					buf.append(" FLAGS (");
					buf.append(mmsgs[i].flags.getFlags());
					buf.append("))");
				} else {
					
					buf.append(" FETCH FLAGS (");
					buf.append(mmsgs[i].flags.getFlags());
					buf.append(")");
				}
				
				this.sendState(buf.toString());
			}
		}
		
		return true;
	}
	
	private void handle_expunge(IMAPMessage msg) {
		if (!this.verify_auth(msg)) {
			return;
		}

		if (this.mb == null) {
			this.reply(msg, "NO No mailbox selected");
			return;
		}
		
		this.expunge(true);
		this.reply(msg, "OK Expunge complete");
	}
	
	private void handle_close(IMAPMessage msg) {
		if (!this.verify_auth(msg)) {
			return;
		}

		if (this.mb == null) {
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
		for (int i = 0; i < mmsgs.length; i++) {
			if (mmsgs[i].flags.get("\\Deleted")) {
				mmsgs[i].delete();
				if (verbose) this.sendState((i+1-count_correction)+" EXPUNGE");
				count_correction++;
			}
		}
	}
	
	private void handle_namespace(IMAPMessage msg) {
		if(!this.verify_auth(msg)) {
			return;
		}

		this.sendState("NAMESPACE ((\"INBOX.\" \".\")) NIL NIL");
		this.reply(msg, "OK Namespace completed");
	}
	
	private void handle_status(IMAPMessage msg) {
		if (!this.verify_auth(msg)) {
			return;
		}
		
		if (msg.args == null || msg.args.length < 2) {
			this.reply(msg, "BAD Not enough arguments");
			return;
		}
		
		String mbname = trimQuotes(msg.args[0]);
		
		MessageBank statmb = this.getMailboxFromPath(mbname);
		
		if (statmb == null) {
			this.reply(msg, "NO Could not find mailbox");
			return;
		}
		
		SortedMap<Integer, MailMessage> msgs = statmb.listMessages();
		
		// gather statistics
		int numrecent = 0;
		int numunseen = 0;
		int nummessages = msgs.size();
		int lastuid = 0;
		while (msgs.size() > 0) {
			Integer current = msgs.firstKey();
			MailMessage m =msgs.get(msgs.firstKey());
				
			// if it's recent, add to the tally
			if (m.flags.get("\\Recent")) numrecent++;
			
			// is it unseen?
			if (!m.flags.get("\\Seen")) numunseen++;
			
			if (m.getUID() > lastuid) lastuid = m.getUID();
				
			msgs = msgs.tailMap(new Integer(current.intValue()+1));
		}
		
		StringBuffer buf = new StringBuffer();
		buf.append("STATUS ");
		buf.append(msg.args[0]);
		buf.append(" (");
		
		
		// output the required information
		int i;
		boolean first = true;
		for (i = 1; i < msg.args.length; i++) {
			String arg = msg.args[i];
			
			if (arg.startsWith("(")) arg = arg.substring(1);
			if (arg.endsWith(")")) arg = arg.substring(0, arg.length() - 1);
			
			if (!first) buf.append(" ");
			first = false;
			buf.append(arg);
			buf.append(" ");
			if (arg.equalsIgnoreCase("messages")) {
				buf.append(Integer.toString(nummessages));
			} else if (arg.equalsIgnoreCase("recent")) {
				buf.append(Integer.toString(numrecent));
			} else if (arg.equalsIgnoreCase("unseen")) {
				buf.append(Integer.toString(numunseen));
			} else if (arg.equalsIgnoreCase("uidnext")) {
				buf.append(Integer.toString(lastuid + 1));
			} else if (arg.equalsIgnoreCase("uidvalidity")) {
				buf.append("1");
			}
		}
		
		buf.append(")");
		this.sendState(buf.toString());
		this.reply(msg, "OK STATUS completed");
	}
	
	private void handle_create(IMAPMessage msg) {
		if (!this.verify_auth(msg)) {
			return;
		}
		
		if (msg.args == null || msg.args.length < 1) {
			this.reply(msg, "NO Not enough arguments");
			return;
		}
		
		msg.args[0] = trimQuotes(msg.args[0]);
		
		if (msg.args[0].endsWith(".")) {
			// ends with a hierarchy delimiter. Ignore it
			this.reply(msg, "OK Nothing done");
			return;
		}
		
		String[] mbparts = msg.args[0].split("\\.");
		if (!mbparts[0].equalsIgnoreCase("inbox")) {
			this.reply(msg, "NO Invalid mailbox name");
			return;
		}
		
		if (mbparts.length < 2) {
			this.reply(msg, "NO Inbox already exists!");
			return;
		}
		
		int i;
		MessageBank tempmb = this.inbox;
		for (i = 1; i < mbparts.length; i++) {
			MessageBank existingmb = tempmb.getSubFolder(mbparts[i]);
			if (existingmb != null) {
				tempmb = existingmb;
			} else {
				tempmb = tempmb.makeSubFolder(mbparts[i]);
				if (tempmb == null) {
					this.reply(msg, "NO couldn't create mailbox");
					return;
				}
			}
		}
		this.reply(msg, "OK Mailbox created");
	}
	
	private void handle_delete(IMAPMessage msg) {
		if (!this.verify_auth(msg)) {
			return;
		}
		
		if (msg.args == null || msg.args.length < 1) {
			this.reply(msg, "NO Not enough arguments");
			return;
		}
		
		MessageBank target = getMailboxFromPath(trimQuotes(msg.args[0]));
		if (target == null) {
			this.reply(msg, "NO No such mailbox.");
			return;
		}
		
		if (target.listSubFolders().length > 0) {
			this.reply(msg, "NO Mailbox has inferiors.");
			return;
		}
		
		if (target.delete()) {
			this.reply(msg, "OK Mailbox deleted");
		} else {
			this.reply(msg, "NO Unable to delete mailbox");	
		}
		return;
	}
	
	private void handle_copy(IMAPMessage msg) {
		if (!this.verify_auth(msg)) {
			return;
		}
		
		if (this.mb == null) {
			this.reply(msg, "NO No mailbox selected");
			return;
		}
		
		if (msg.args == null || msg.args.length < 2) {
			this.reply(msg, "NO Not enough arguments");
			return;
		}
		
		Object[] allmsgs = this.mb.listMessages().values().toArray();
		String rangeparts[] = msg.args[0].split(":");
		
		int from;
		int to;
		try {
			from = Integer.parseInt(rangeparts[0]);
		} catch (NumberFormatException nfe) {
			this.reply(msg, "BAD That's not a number!");
			return;
		}
		if (rangeparts.length > 1) {
			if (rangeparts[1].equals("*")) {
				to = allmsgs.length;
			} else {
				try {
					to = Integer.parseInt(rangeparts[1]);
				} catch (NumberFormatException nfe) {
					this.reply(msg, "BAD That's not a number!");
					return;
				}
			}
		} else {
			to = from;
		}
		
		// convert to zero based array
		from--;
		to--;
		
		if (from < 0 || to < 0 || from > allmsgs.length || to > allmsgs.length) {
			this.reply(msg, "NO No such message");
			return;
		}
		
		MessageBank target = getMailboxFromPath(trimQuotes(msg.args[1]));
		if (target == null) {
			this.reply(msg, "NO [TRYCREATE] No such mailbox.");
			return;
		}
		
		for (int i = from; i <= to; i++) {
			MailMessage src = (MailMessage)allmsgs[i];
			MailMessage copy = target.createMessage();
			
			src.copyTo(copy);
		}
		this.reply(msg, "OK COPY completed");
	}
	
	private void handle_append(IMAPMessage msg) {
		if(!this.verify_auth(msg)) {
			return;
		}

		if (msg.args == null || msg.args.length < 2) {
			this.reply(msg, "BAD Not enough arguments");
			return;
		}
		
		//args[0] is always the mailbox
		String mbname = trimQuotes(msg.args[0]);
		
		String sdatalen = "";
		List<String> flags = new LinkedList<String>();
		
		for (int i = 1; i < msg.args.length; i++) {
			if (msg.args[i].startsWith("(")) {
				if(msg.args[i].endsWith(")")) {
					//Only flag
					flags.add(msg.args[i].substring(1, msg.args[i].length() - 1));
					i++;
				} else {
					//Add all the flags
					flags.add(msg.args[i].substring(1, msg.args[i].length()));
					i++;
					while(!msg.args[i].endsWith(")")) {
						flags.add(msg.args[i]);
						i++;
					}
					flags.add(msg.args[i].substring(0, msg.args[i].length() - 1));
					i++;
				}
			} else if (msg.args[i].startsWith("{")) {
				//Data length
				sdatalen = msg.args[i].substring(1, msg.args[i].length() -1);
			}
		}
		
		int datalen;
		try {
			datalen = Integer.parseInt(sdatalen);
		} catch (NumberFormatException nfe) {
			//FIXME: Send error message
			datalen = 0;
		}
		
		MessageBank destmb = this.getMailboxFromPath(mbname);
		if (destmb == null) {
			this.reply(msg, "NO [TRYCREATE] No such mailbox");
			return;
		}
		
		MailMessage newmsg = destmb.createMessage();
		this.ps.print("+ OK\r\n");
		try {
			PrintStream msgps = newmsg.getRawStream();
			
			String line;
			int bytesread = 0;
			while ( (line = this.bufrdr.readLine()) != null) {
				msgps.println(line);
				
				bytesread += line.getBytes().length;
				bytesread += "\r\n".length();
				
				if (bytesread >= datalen) break;
			}
			
			newmsg.commit();
		} catch (IOException ioe) {
			this.reply(msg, "NO Failed to write message");
			newmsg.cancel();
			return;
		}
		
		for (String flag : flags) {
			newmsg.flags.set(flag, true);
		}
		newmsg.storeFlags();
		this.reply(msg, "OK APPEND completed");
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
		if (in == null) return "NIL";
		return "\""+in.trim()+"\"";
	}
	
	private String IMAPifyAddress(String address) {
		if (address == null || address.length() == 0) return "NIL";
		
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
		if (in.length() == 0) return in;
		if (in.charAt(0) == '"') {
			in = in.substring(1);
		}
		if (in.charAt(in.length() - 1) == '"') {
			in = in.substring(0, in.length() - 1);
		}
		return in;
	}
	
	private boolean verify_auth(IMAPMessage msg) {
		if (this.inbox == null) {
			this.reply(msg, "NO Must be authenticated");
			return false;
		}
		return true;
	}
}

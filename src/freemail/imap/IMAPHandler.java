package freemail.imap;

import java.net.Socket;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.SortedMap;
import java.lang.NumberFormatException;
import java.text.SimpleDateFormat;
import java.util.Date;

import freemail.MessageBank;
import freemail.MailMessage;
import freemail.AccountManager;
import freemail.utils.EmailAddress;

public class IMAPHandler implements Runnable {
	private static final String CAPABILITY = "IMAP4rev1 AUTH=LOGIN CHILDREN NAMESPACE";

	private final Socket client;
	private final OutputStream os;
	private final PrintStream ps;
	private final BufferedReader bufrdr;
	private MessageBank mb;
	private MessageBank inbox;
	

	IMAPHandler(Socket client) throws IOException {
		this.client = client;
		this.os = client.getOutputStream();
		this.ps = new PrintStream(this.os);
		this.bufrdr = new BufferedReader(new InputStreamReader(client.getInputStream()));
		this.mb = null;
	}
	
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
			
		}
	}
	
	private void sendWelcome() {
		this.ps.print("* OK [CAPABILITY "+CAPABILITY+"] Freemail ready - hit me with your rhythm stick.\r\n");
	}
	
	private void dispatch(IMAPMessage msg) {
		//System.out.println(msg.toString());
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
			this.reply(msg, "NO Sorry - not implemented");
		}
	}
	
	private void handle_login(IMAPMessage msg) {
		if (msg.args.length < 2) return;
		if (AccountManager.authenticate(trimQuotes(msg.args[0]), trimQuotes(msg.args[1]))) {
			this.inbox = new MessageBank(trimQuotes(msg.args[0]));
			
			this.reply(msg, "OK Logged in");
		} else {
			this.reply(msg, "NO Login failed");
		}
	}
	
	private void handle_logout(IMAPMessage msg) {
		this.reply(msg, "OK Bye");
		try {
			this.client.close();
		} catch (IOException ioe) {
			
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
		
		if (msg.args.length < 1) {
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
		if (refname.length() == 0) refname = null;
		
		if (mbname != null) mbname = trimQuotes(mbname);
		if (mbname.length() == 0) mbname = null;
		
		if (mbname == null) {
			// return hierarchy delimiter
			this.sendState(replyprefix+" (\\Noselect) \".\" \"\"");
		} else {
			// transform mailbox name into a regex
			
			// '*' needs to be '.*'
			mbname = mbname.replaceAll("\\*", ".*");
			
			// and % is a wildcard not inclusing the hierarchy delimiter
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
		
		if (msg.args.length < 1) {
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
		this.sendState("OK [PERMANENTFLAGS (\\* "+IMAPMessageFlags.getPermanentFlagsAsString()+")] Limited");
			
		SortedMap msgs = this.mb.listMessages();
			
		int numrecent = 0;
		int numexists = msgs.size();
		while (msgs.size() > 0) {
			Integer current = (Integer)(msgs.firstKey());
			MailMessage m =(MailMessage)msgs.get(msgs.firstKey());
				
			// if it's recent, add to the tally
			if (m.flags.get("\\Recent")) numrecent++;
				
			// remove the recent flag
			m.flags.set("\\Recent", false);
			m.storeFlags();
				
			msgs = msgs.tailMap(new Integer(current.intValue()+1));
		}
			
		this.sendState(numexists+" EXISTS");
		this.sendState(numrecent+" RECENT");
			
		this.sendState("OK [UIDVALIDITY 1] Ok");
			
		this.reply(msg, "OK [READ-WRITE] Done");
	}
	
	private void handle_noop(IMAPMessage msg) {
		this.reply(msg, "OK NOOP completed");
	}
	
	private void handle_check(IMAPMessage msg) {
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
		
		SortedMap msgs = this.mb.listMessages();
		
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
			this.reply(msg, "BAD Bad number");
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
				this.reply(msg, "BAD Bad number");
				return;
			}
		}
		
		if (from == 0 || to == 0 || from > msgs.size() || to > msgs.size()) {
			this.reply(msg, "NO Invalid message ID");
			return;
		}
		
		for (int i = 1; msgs.size() > 0; i++) {
			Integer current = (Integer)(msgs.firstKey());
			if (i < from) {
				msgs = msgs.tailMap(new Integer(current.intValue()+1));
				continue;
			}
			if (i > to) break;
			
			if (!this.fetch_single((MailMessage)msgs.get(msgs.firstKey()), i, msg.args, 1, false)) {
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
		
		SortedMap msgs = this.mb.listMessages();
		
		if (msgs.size() == 0) {
			if (msg.args[0].toLowerCase().equals("fetch")) {
				this.reply(msg, "OK Fetch completed");
			} else if (msg.args[0].toLowerCase().equals("store")) {
				// hmm...?
				this.reply(msg, "NO No such message");
			}
			return;
		}
		
		String[] parts = msg.args[1].split(":");
		try {
			from = Integer.parseInt(parts[0]);
		} catch (NumberFormatException nfe) {
			this.reply(msg, "BAD Bad number");
			return;
		}
		if (parts.length < 2) {
			to = from;
		} else if (parts[1].equals("*")) {
			Integer tmp = (Integer)msgs.lastKey();
			to = tmp.intValue();
		} else {
			try {
				to = Integer.parseInt(parts[1]);
			} catch (NumberFormatException nfe) {
				this.reply(msg, "BAD Bad number");
				return;
			}
		}
		
		int msgnum = 1;
		if (msg.args[0].equalsIgnoreCase("fetch")) {
			int oldsize = msgs.size();
			msgs = msgs.tailMap(new Integer(from));
			msgnum += (oldsize - msgs.size());
			while (msgs.size() > 0) {
				Integer curuid = (Integer)msgs.firstKey();
				if (curuid.intValue() > to) {
					break;
				}
				
				if (!this.fetch_single((MailMessage)msgs.get(msgs.firstKey()), msgnum, msg.args, 2, true)) {
					this.reply(msg, "BAD Unknown attribute in list or unterminated list");
					return;
				}
				
				msgs = msgs.tailMap(new Integer(curuid.intValue()+1));
				msgnum++;
			}
			
			this.reply(msg, "OK Fetch completed");
		} else if (msg.args[0].equalsIgnoreCase("store")) {
			int oldsize = msgs.size();
			msgs = msgs.tailMap(new Integer(from));
			int firstmsg = oldsize - msgs.size();
			msgs = msgs.headMap(new Integer(to + 1));
			
			MailMessage[] targetmsgs = new MailMessage[msgs.size()];
			
			for (int i = 0; i < targetmsgs.length; i++) {
				targetmsgs[i] = (MailMessage)msgs.values().toArray()[i];
			}
			
			this.do_store(msg.args, 2, targetmsgs, msg, firstmsg, true);
			
			this.reply(msg, "OK Store completed");
		} else if (msg.args[0].equalsIgnoreCase("copy")) {
			msgs = msgs.tailMap(new Integer(from));
			
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
			
			while (msgs.size() > 0) {
				Integer curuid = (Integer)msgs.firstKey();
				if (curuid.intValue() > to) {
					break;
				}
				
				MailMessage srcmsg = (MailMessage)msgs.get(msgs.firstKey());
				
				MailMessage copymsg = target.createMessage();
				srcmsg.copyTo(copymsg);
				
				copied++;
				
				msgs = msgs.tailMap(new Integer(curuid.intValue()+1));
				msgnum++;
			}
			
			if (copied > 0)
				this.reply(msg, "OK COPY completed");
			else
				this.reply(msg, "NO No messages copied");
		} else {
			this.reply(msg, "BAD Unknown command");
		}
	}
	
	private boolean fetch_single(MailMessage msg, int id, String[] args, int firstarg, boolean send_uid_too) {
		String[] imap_args = (String[]) args.clone();
		this.ps.print("* "+id+" FETCH (");
		
		// do the first attribute, if it's a loner.
		if (!imap_args[firstarg].startsWith("(")) {
			// It's a loner
			this.ps.flush();
			if (!this.send_attr(msg, imap_args[firstarg]))
				return false;
			
			if (send_uid_too && !imap_args[firstarg].equalsIgnoreCase("uid")) {
				this.ps.print(" UID "+msg.getUID());
			}
			
			this.ps.print(")\r\n");
			this.ps.flush();
			
			return true;
		} else {
			imap_args[firstarg] = imap_args[firstarg].substring(1);
		}
		
		// go through the parenthesised list
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
			if (!this.send_attr(msg, attr))
				return false;
			
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
			} else {
				this.ps.print(" ");
			}
		}
		
		// if we get here, we've reached the end of the list without a terminating parenthesis. Naughty client.
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
		} else if (attr.startsWith("body")) {
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
		
 		if (attr.charAt(0) == '[') attr = attr.substring(1);
		if (attr.charAt(attr.length() - 1) == ']')
			attr = attr.substring(0, attr.length() - 1);
		
		if (attr.trim().length() == 0) {
			try {
				this.ps.print("[] ");
				this.ps.print("{"+mmsg.getSize()+"}\r\n");
				
				String line;
				while ( (line = mmsg.readLine()) != null) {
					this.ps.print(line+"\r\n");
				}
				mmsg.closeStream();
			} catch (IOException ioe) {
				return false;
			}
			return true;
		}
		
		StringBuffer buf = new StringBuffer("");
		
		String[] parts = IMAPMessage.doSplit(attr, '(', ')');
		for (int i = 0; i < parts.length; i++) {
			if (parts[i].equalsIgnoreCase("header.fields")) {
				i++;
				this.ps.print("[HEADER.FIELDS "+parts[i]+"] ");
				if (parts[i].charAt(0) == '(')
					parts[i] = parts[i].substring(1);
				if (parts[i].charAt(parts[i].length() - 1) == ')')
					parts[i] = parts[i].substring(0, parts[i].length() - 1);
				
				try {
					mmsg.readHeaders();
				} catch (IOException ioe) {
					
				}
				
				String[] fields = parts[i].split(" ");
				for (int j = 0; j < fields.length; j++) {
					buf.append(mmsg.getHeaders(fields[j]));
				}
				buf.append("\r\n");
			} else if (parts[i].equalsIgnoreCase("header")) {
				// send all the header fields
				try {
					mmsg.readHeaders();
				} catch (IOException ioe) {
				}
				
				buf.append(mmsg.getAllHeadersAsString());
				buf.append("\r\n");
			} else if (parts[i].equalsIgnoreCase("text")) {
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
			
			this.ps.print("{"+buf.length()+"}\r\n"+buf.toString());
			return true;
		}
		
		return false;
	}
	
	public void handle_store(IMAPMessage msg) {
		if (msg.args.length < 2) {
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
		
		do_store(msg.args, 1, msgs, msg, from + 1, false);
		
		this.reply(msg, "OK Store completed");
	}
	
	private void do_store(String[] args, int offset, MailMessage[] mmsgs, IMAPMessage msg, int firstmsgnum, boolean senduid) {
		if (args[offset].toLowerCase().indexOf("flags") < 0) {
			// IMAP4Rev1 can only store flags, so you're
			// trying something crazy
			this.reply(msg, "BAD Can't store that");
			return;
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
				
				buf.append((i+firstmsgnum));
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
	}
	
	private void handle_expunge(IMAPMessage msg) {
		if (this.mb == null) {
			this.reply(msg, "NO No mailbox selected");
			return;
		}
		
		this.expunge(true);
		this.reply(msg, "OK Expunge complete");
	}
	
	private void handle_close(IMAPMessage msg) {
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
		
		for (int i = 0; i < mmsgs.length; i++) {
			if (mmsgs[i].flags.get("\\Deleted"))
				mmsgs[i].delete();
			if (verbose) this.sendState(i+" EXPUNGE");
		}
	}
	
	private void handle_namespace(IMAPMessage msg) {
		this.sendState("NAMESPACE ((\"INBOX.\" \".\")) NIL NIL");
		this.reply(msg, "OK Namespace completed");
	}
	
	private void handle_status(IMAPMessage msg) {
		if (!this.verify_auth(msg)) {
			return;
		}
		
		if (msg.args.length < 2) {
			this.reply(msg, "BAD Not enough arguments");
			return;
		}
		
		String mbname = trimQuotes(msg.args[0]);
		
                MessageBank statmb = this.getMailboxFromPath(mbname);
		
		SortedMap msgs = statmb.listMessages();
		
		// gather statistics
		int numrecent = 0;
		int numunseen = 0;
		int nummessages = msgs.size();
		int lastuid = 0;
		while (msgs.size() > 0) {
			Integer current = (Integer)(msgs.firstKey());
			MailMessage m =(MailMessage)msgs.get(msgs.firstKey());
				
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
		
		if (msg.args.length < 1) {
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
		
		if (msg.args.length < 1) {
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
		
		if (msg.args.length < 2) {
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
		if (msg.args.length < 3) {
			this.reply(msg, "NO Not enough arguments");
			return;
		}
		
		String mbname = trimQuotes(msg.args[0]);
		String sflags = msg.args[1];
		if (sflags.startsWith("(")) sflags = sflags.substring(1);
		if (sflags.endsWith(")")) sflags = sflags.substring(0, sflags.length() - 1);
		String sdatalen = msg.args[2];
		if (sdatalen.startsWith("{")) sdatalen = sdatalen.substring(1);
		if (sdatalen.endsWith("}")) sdatalen = sdatalen.substring(0, sdatalen.length() - 1);
		int datalen;
		try {
			datalen = Integer.parseInt(sdatalen);
		} catch (NumberFormatException nfe) {
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
		
		String[] flags = sflags.split(" ");
		for (int i = 0; i < flags.length; i++) {
			newmsg.flags.set(flags[i], true);
		}
		newmsg.storeFlags();
		this.reply(msg, "OK APPEND completed");
	}
	
	private String getEnvelope(MailMessage mmsg) {
		StringBuffer buf = new StringBuffer("(");
		
		try {
			mmsg.readHeaders();
		} catch (IOException ioe) {
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
		
		String retval = new String("((");
		retval += this.IMAPifyString(addr.realname)+" ";
		// SMTP Source Route. Whatever this is, it's not relevant!
		retval += "NIL ";
		retval += this.IMAPifyString(addr.user)+" ";
		retval += this.IMAPifyString(addr.domain);
		retval += "))";
		
		return retval;
	}
	
	private void reply(IMAPMessage msg, String reply) {
		this.ps.print(msg.tag + " " + reply + "\r\n");
	}
	
	private void sendState(String txt) {
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

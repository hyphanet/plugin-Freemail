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
	private static final String CAPABILITY = "IMAP4rev1 AUTH=LOGIN";

	final Socket client;
	final OutputStream os;
	final PrintStream ps;
	final BufferedReader bufrdr;
	MessageBank mb;
	

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
		} else {
			this.reply(msg, "NO Sorry - not implemented");
		}
	}
	
	private void handle_login(IMAPMessage msg) {
		if (msg.args.length < 2) return;
		if (AccountManager.authenticate(trimQuotes(msg.args[0]), trimQuotes(msg.args[1]))) {
			this.mb = new MessageBank(trimQuotes(msg.args[0]));
			
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
		
		refname = trimQuotes(refname);
		if (refname.length() == 0) refname = null;
		
		mbname = trimQuotes(mbname);
		if (mbname.length() == 0) mbname = null;
		
		if (mbname == null) {
			// return hierarchy delimiter
			this.sendState(replyprefix+" (\\Noselect) \".\" \"\"");
		} else if (mbname.equals("%") || mbname.equals("INBOX") || mbname.equals("*") || mbname.equals("INBOX*")) {
			this.sendState(replyprefix+" (\\NoInferiors) \".\" \"INBOX\"");
		}
		
		this.reply(msg, "OK "+replyprefix+" completed");
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
		
		mbname = trimQuotes(msg.args[0]).toLowerCase();
		
		if (mbname.equals("inbox")) {
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
		} else {
			this.reply(msg, "NO No such mailbox");
		}
	}
	
	private void handle_noop(IMAPMessage msg) {
		this.reply(msg, "OK NOOP completed");
	}
	
	private void handle_fetch(IMAPMessage msg) {
		int from;
		int to;
		
		if (!this.verify_auth(msg)) {
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
		if (msg.args[0].toLowerCase().equals("fetch")) {
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
		} else if (msg.args[0].toLowerCase().equals("store")) {
			msgs = msgs.tailMap(new Integer(from));
			msgs = msgs.headMap(new Integer(to + 1));
			
			MailMessage[] targetmsgs = new MailMessage[msgs.size()];
			
			for (int i = 0; i < targetmsgs.length; i++) {
				targetmsgs[i] = (MailMessage)msgs.values().toArray()[i];
			}
			
			this.do_store(msg.args, 2, targetmsgs, msg, -1);
			
			this.reply(msg, "OK Store completed");
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
			return this.sendBody(mmsg, a);
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
		
		do_store(msg.args, 1, msgs, msg, from + 1);
		
		this.reply(msg, "OK Store completed");
	}
	
	private void do_store(String[] args, int offset, MailMessage[] mmsgs, IMAPMessage msg, int firstmsgnum) {
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
				
				if (firstmsgnum < 0) {
					buf.append(mmsgs[i].getUID() + " FETCH FLAGS (");
				} else {
					buf.append((i+firstmsgnum) + " FETCH FLAGS (");
				}
				
				buf.append(mmsgs[i].flags.getFlags());
				
				buf.append(")");
				
				this.sendState(buf.toString());
			}
		}
	}
	
	private void handle_expunge(IMAPMessage msg) {
		MailMessage[] mmsgs = this.mb.listMessagesArray();
		
		for (int i = 0; i < mmsgs.length; i++) {
			if (mmsgs[i].flags.get("\\Deleted"))
				mmsgs[i].delete();
			this.sendState(i+" EXPUNGE");
		}
		this.reply(msg, "OK Mailbox closed");
	}
	
	private void handle_close(IMAPMessage msg) {
		MailMessage[] mmsgs = this.mb.listMessagesArray();
		
		for (int i = 0; i < mmsgs.length; i++) {
			if (mmsgs[i].flags.get("\\Deleted"))
				mmsgs[i].delete();
		}
		this.reply(msg, "OK Mailbox closed");
	}
	
	private void handle_namespace(IMAPMessage msg) {
		this.sendState("NAMESPACE ((\"\" \"/\")) NIL NIL");
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
		
		// for now
		if (!mbname.equalsIgnoreCase("INBOX")) {
			this.reply(msg, "BAD No such mailbox");
			return;
		}
		
		SortedMap msgs = this.mb.listMessages();
		
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
		if (this.mb == null) {
			this.reply(msg, "NO Must be authenticated");
			return false;
		}
		return true;
	}
}

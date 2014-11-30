/*
 * SMTPHandler.java
 * This file is part of Freemail
 * Copyright (C) 2006,2007,2008 Dave Baker
 * Copyright (C) 2007 Alexander Lehmann
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

package org.freenetproject.freemail.smtp;

import java.net.Socket;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import freenet.pluginmanager.PluginNotFoundException;
import freenet.support.api.Bucket;
import freenet.support.io.FileBucket;

import org.archive.util.Base32;
import org.bouncycastle.util.encoders.Base64;
import org.freenetproject.freemail.AccountManager;
import org.freenetproject.freemail.Freemail;
import org.freenetproject.freemail.FreemailAccount;
import org.freenetproject.freemail.ServerHandler;
import org.freenetproject.freemail.transport.MessageHandler;
import org.freenetproject.freemail.utils.Logger;
import org.freenetproject.freemail.wot.Identity;
import org.freenetproject.freemail.wot.IdentityMatcher;

public class SMTPHandler extends ServerHandler implements Runnable {
	private final OutputStream os;
	private final PrintStream ps;
	private final BufferedReader bufrdr;
	private FreemailAccount account;
	public static final String MY_HOSTNAME = "localhost";

	private final AccountManager accountmanager;
	private final IdentityMatcher identityMatcher;

	private Vector<Identity> to;

	public SMTPHandler(AccountManager accMgr, Socket client, IdentityMatcher identityMatcher) throws IOException {
		super(client);
		accountmanager = accMgr;
		this.account = null;
		this.os = client.getOutputStream();
		this.ps = new PrintStream(this.os);
		this.bufrdr = new BufferedReader(new InputStreamReader(client.getInputStream()));
		this.identityMatcher = identityMatcher;

		this.to = new Vector<Identity>();
	}

	@Override
	public void run() {
		this.sendWelcome();

		String line;
		try {
			while(!stopping && !this.client.isClosed() && (line = this.bufrdr.readLine()) != null) {
				SMTPCommand msg = null;
				try {
					//Logger.normal(this,line);
					msg = new SMTPCommand(line);
				} catch (SMTPBadCommandException bce) {
					Logger.debug(this, "Parsing failed, line was: " + line);
					continue;
				}

				Logger.debug(this, "Received: " + line);
				this.dispatch(msg);
			}

			this.client.close();
		} catch (IOException ioe) {

		}
	}

	private void dispatch(SMTPCommand cmd) {
		if(cmd.command.equals("helo")) {
			this.handle_helo();
		} else if(cmd.command.equals("ehlo")) {
			this.handle_ehlo();
		} else if(cmd.command.equals("quit")) {
			this.handle_quit();
		} else if(cmd.command.equals("turn")) {
			this.handle_turn();
		} else if(cmd.command.equals("auth")) {
			this.handle_auth(cmd);
		} else if(cmd.command.equals("mail")) {
			this.handle_mail();
		} else if(cmd.command.equals("rcpt")) {
			this.handle_rcpt(cmd);
		} else if(cmd.command.equals("data")) {
			this.handle_data();
		} else if(cmd.command.equals("rset")) {
			this.handle_rset();
		} else {
			Logger.normal(this, "Unknown command: " + cmd.command);
			this.ps.print("502 Unimplemented\r\n");
		}
	}

	private void handle_helo() {
		this.ps.print("250 "+MY_HOSTNAME+"\r\n");
	}

	private void handle_ehlo() {
		this.ps.print("250-"+MY_HOSTNAME+"\r\n");
		this.ps.print("250 AUTH LOGIN PLAIN\r\n");
	}

	private void handle_quit() {
		this.ps.print("221 "+MY_HOSTNAME+"\r\n");
		try {
			this.client.close();
		} catch (IOException ioe) {

		}
	}

	private void handle_turn() {
		this.ps.print("502 No\r\n");
	}

	private void handle_auth(SMTPCommand cmd) {
		String uname;
		String password;

		if(cmd.args.length == 0) {
			this.ps.print("504 No auth type given\r\n");
			return;
		} else if(cmd.args[0].equalsIgnoreCase("login")) {
			try {
				this.ps.print("334 "+new String(Base64.encode("Username:".getBytes("UTF-8")))+"\r\n");
			} catch(UnsupportedEncodingException e) {
				//JVMs are required to support UTF-8, so we can assume it is always available
				throw new AssertionError("JVM doesn't support UTF-8 charset");
			}

			String b64username;
			String b64password;
			try {
				b64username = this.bufrdr.readLine();
			} catch (IOException ioe) {
				return;
			}
			if(b64username == null) return;

			try {
				this.ps.print("334 "+new String(Base64.encode("Password:".getBytes("UTF-8")))+"\r\n");
			} catch(UnsupportedEncodingException e) {
				//JVMs are required to support UTF-8, so we can assume it is always available
				throw new AssertionError("JVM doesn't support UTF-8 charset");
			}
			try {
				b64password = this.bufrdr.readLine();
			} catch (IOException ioe) {
				return;
			}
			if(b64password == null) return;

			try {
				uname = new String(Base64.decode(b64username.getBytes("UTF-8")));
				password = new String(Base64.decode(b64password.getBytes("UTF-8")));
			} catch(UnsupportedEncodingException e) {
				//JVMs are required to support UTF-8, so we can assume it is always available
				throw new AssertionError("JVM doesn't support UTF-8 charset");
			}
		} else if(cmd.args[0].equalsIgnoreCase("plain")) {
			String b64creds;

			if(cmd.args.length > 1) {
				b64creds = cmd.args[1];
			} else {
				this.ps.print("334 \r\n");
				try {
					b64creds = this.bufrdr.readLine();
					if(b64creds == null) return;
				} catch (IOException ioe) {
					return;
				}
			}

			String creds_plain;
			try {
				creds_plain = new String(Base64.decode(b64creds.getBytes("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				//JVMs are required to support UTF-8, so we can assume it is always available
				throw new AssertionError("JVM doesn't support UTF-8 charset");
			}
			String[] creds = creds_plain.split("\0");

			if(creds.length < 2) return;

			if(creds.length == 3 && !creds[0].isEmpty()) {
				if(!creds[0].equals(creds[1])) {
					this.ps.print("535 Authentication failed\r\n");
					return;
				}
			}

			// most documents seem to reckon you send the
			// username twice. Some think only once.
			// This will work either way.
			uname = creds[0];
			// there may be a null first (is this always the case?)
			if(uname.length() < 1) {
				uname = creds[1];
			}
			password = creds[creds.length - 1];
		} else {
			this.ps.print("504 Auth type unimplemented - weren't you listening?\r\n");
			return;
		}

		if(uname.contains("@") && uname.endsWith(".freemail")) {
			//Extract the base32 identity string and convert it to base64
			uname = uname.substring(uname.indexOf("@") + 1,
					uname.length() - ".freemail".length());

			//We need to use the Freenet Base64 encoder here since it uses a slightly different set
			//of characters
			uname = freenet.support.Base64.encode(Base32.decode(uname));

			Logger.debug(this, "Extracted Identity string: " + uname);
		}

		account = accountmanager.authenticate(uname, password);
		if(account != null) {
			this.ps.print("235 Authenticated\r\n");
		} else {
			this.ps.print("535 Authentication failed\r\n");
		}
	}

	private void handle_mail() {
		if(this.account == null) {
			this.ps.print("530 Authentication required\r\n");
			return;
		}

		this.to.clear();

		// we don't really care.
		this.ps.print("250 OK\r\n");
	}

	private void handle_rcpt(SMTPCommand cmd) {
		if(cmd.args.length < 1) {
			this.ps.print("504 Insufficient arguments\r\n");
			return;
		}

		if(this.account == null) {
			this.ps.print("530 Authentication required\r\n");
			return;
		}

		String allargs = new String();
		for(int i = 0; i < cmd.args.length; i++) {
		    allargs += cmd.args[i];
		}

		String[] parts = allargs.split(":", 2);
		if(parts.length < 2) {
			this.ps.print("504 Can't understand that syntax\r\n");
			return;
		}

		String address = parts[1];
		if(address.startsWith("<") && address.endsWith(">")) {
			address = address.substring(1, address.length() - 1);
		}

		//Check if the identity is in WoT
		Set<String> recipient = new HashSet<String>();
		recipient.add(address);
		Map<String, List<Identity>> matches;
		try {
			EnumSet<IdentityMatcher.MatchMethod> methods = EnumSet.of(IdentityMatcher.MatchMethod.FULL_BASE32);
			matches = identityMatcher.matchIdentities(recipient, account.getIdentity(), methods);
		} catch(PluginNotFoundException e) {
			this.ps.print("554 WoT plugin not loaded\r\n");
			return;
		}
		if(matches.get(address).size() != 1) {
			this.ps.print("550 No such user\r\n");
			return;
		}

		this.to.add(matches.get(address).get(0));

		this.ps.print("250 OK\r\n");
	}

	private void handle_data() {
		if(this.account == null) {
			this.ps.print("530 Authentication required\r\n");
			return;
		}

		if(this.to.size() == 0) {
			this.ps.print("503 RCPT first\r\n");
			return;
		}

		File tempfile = null;
		try {
			tempfile = File.createTempFile("freemail-", ".message", Freemail.getTempDir());
			PrintWriter pw = new PrintWriter(new FileOutputStream(tempfile));

			this.ps.print("354 Go crazy\r\n");

			String line;
			boolean done = false;
			while((line = this.bufrdr.readLine()) != null) {
				if(line.equals(".")) {
					done = true;
					break;
				}
				if(line.startsWith(".")) {
					line = line.substring(1);
				}
				pw.print(line+"\r\n");
			}

			pw.close();
			if(!done) {
				// connection closed before the message was
				// finished. bail out.
				tempfile.delete();
				return;
			}

			MessageHandler messageSender = account.getMessageHandler();
			Bucket data = new FileBucket(tempfile, false, false, false, false, true);
			try {
				if(messageSender.sendMessage(to, data)) {
					this.ps.print("250 So be it\r\n");
				} else {
					this.ps.print("452 Message sending failed\r\n");
				}
			} finally {
				data.free();
			}
		} catch (IOException ioe) {
			this.ps.print("452 Can't store message\r\n");
		} finally {
			if(tempfile != null) {
				tempfile.delete();
			}
		}
	}

	private void handle_rset() {
		this.to.clear();
		this.ps.print("250 Reset\r\n");
	}

	private void sendWelcome() {
		this.ps.print("220 "+MY_HOSTNAME+" ready\r\n");
	}
}

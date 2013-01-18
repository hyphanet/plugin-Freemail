/*
 * SMTPSessionTest.java
 * This file is part of Freemail
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

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bouncycastle.util.encoders.Base64;
import org.freenetproject.freemail.AccountManager;
import org.freenetproject.freemail.NullFreemailAccount;
import org.freenetproject.freemail.FreemailAccount;
import org.freenetproject.freemail.transport.MessageHandler;
import org.freenetproject.freemail.wot.Identity;
import org.freenetproject.freemail.wot.IdentityMatcher;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import utils.Utils;
import fakes.NullIdentity;
import fakes.NullMessageHandler;
import fakes.FakeSocket;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.support.api.Bucket;
import freenet.support.io.BucketTools;

public class SMTPSessionTest {
	private static final boolean EXTENSIVE = Boolean.parseBoolean(System.getenv("test.extensive"));
	private static final String BASE64_USERNAME = "D3MrAR-AVMqKJRjXnpKW2guW9z1mw5GZ9BB15mYVkVc";
	private static final String BASE32_USERNAME = "b5zswai7ybkmvcrfddlz5euw3ifzn5z5m3bzdgpucb26mzqvsflq";
	private static final String PASSWORD = "oaJ4Aa1b";

	/*
	 * Directory structure:
	 * smtptest/
	 *   account_manager_dir/
	 *     account_dir/
	 *       channels/
	 */
	private final File testDir = new File("smtptest");
	private final File accountManagerDir = new File(testDir, "account_manager_dir");
	private final File accountDir = new File(accountManagerDir, "account_dir");
	private final File channelDir = new File(accountDir, "channels");

	@Before
	public void before() {
		Utils.createDir(testDir);
		Utils.createDir(accountManagerDir);
		Utils.createDir(accountDir);
		Utils.createDir(channelDir);
	}

	@After
	public void after() {
		Utils.delete(testDir);
	}

	/**
	 * Runs through a full session sending a simple message to one recipient.
	 *
	 * @throws IOException on IO errors with SMTP thread, should never happen
	 */
	@Test
	public void simpleSession() throws IOException {
		Assume.assumeTrue(EXTENSIVE);

		final String message =
				  "Date: Thu, 21 May 1998 05:33:29 -0700\r\n"
				+ "From: zidel <zidel@" + BASE32_USERNAME + ".freemail>\r\n"
				+ "Subject: Freemail SMTP test\r\n"
				+ "To: zidel <zidel@" + BASE32_USERNAME + ".freemail>\r\n"
				+ "\r\n"
				+ "This is a simple SMTP test for Freemail\r\n";

		String authData = new String(Base64.encode(("\0" + BASE64_USERNAME + "\0" + PASSWORD).getBytes("ASCII")), "ASCII");
		List<Command> commands = new LinkedList<Command>();
		commands.add(new Command(null, "220 localhost ready"));
		commands.add(new Command("EHLO", "250-localhost",
		                                 "250 AUTH LOGIN PLAIN"));
		commands.add(new Command("AUTH PLAIN " + authData, "235 Authenticated"));
		commands.add(new Command("MAIL FROM:<zidel@" + BASE32_USERNAME + ".freemail>", "250 OK"));
		commands.add(new Command("RCPT TO:<zidel@" + BASE32_USERNAME + ".freemail>", "250 OK"));
		commands.add(new Command("DATA", "354 Go crazy"));
		commands.add(new Command(message + ".\r\n", "250 So be it"));

		runSimpleSessionTest(commands, true, message);
	}

	/**
	 * Sets up the SMTP server and supporting mocks and runs through the commands and the expected
	 * responses, then quit.
	 *
	 * @param commands the commands to be sent and their responses
	 * @param sendResult the result that will be returned from MessageHandler.sendMessage()
	 * @param message the message that the MessageHandler should receive
	 * @throws IOException on IO errors with SMTP thread, should never happen
	 */
	public void runSimpleSessionTest(List<Command> commands, final boolean sendResult, final String message)
			throws IOException {
		final Identity recipient = new NullIdentity(BASE64_USERNAME, null, null) {
			@Override
			public boolean equals(Object o) {
				return o == this;
			}

			@Override
			public int hashCode() {
				throw new UnsupportedOperationException();
			}
		};

		final MessageHandler messageHandler = new NullMessageHandler(null, null, channelDir, null, null) {
			@Override
			public boolean sendMessage(List<Identity> recipients, Bucket msg) throws IOException {
				assertEquals(1, recipients.size());
				assertEquals(recipient, recipients.get(0));
				assertEquals(message, new String(BucketTools.toByteArray(msg), "ASCII"));

				return sendResult;
			}
		};

		final FreemailAccount account = new NullFreemailAccount(BASE64_USERNAME, null, null, null) {
			@Override
			public MessageHandler getMessageHandler() {
				return messageHandler;
			}

			@Override
			public File getAccountDir() {
				return accountDir;
			}

			@Override
			public String getIdentity() {
				return BASE64_USERNAME;
			}
		};

		AccountManager accManager = new AccountManager(accountManagerDir, null) {
			@Override
			public FreemailAccount authenticate(String username, String password) {
				if(username.equals(BASE64_USERNAME) && password.equals(PASSWORD)) {
					return account;
				}

				return null;
			}
		};

		IdentityMatcher matcher = new IdentityMatcher(null) {
			@Override
			public Map<String, List<Identity>> matchIdentities(Set<String> recipients, String wotOwnIdentity, EnumSet<MatchMethod> methods) throws PluginNotFoundException {
				assertEquals(1, recipients.size());
				assertEquals("zidel@" + BASE32_USERNAME + ".freemail", recipients.iterator().next());
				assertEquals(BASE64_USERNAME, wotOwnIdentity);

				Map<String, List<Identity>> result = new HashMap<String, List<Identity>>();
				List<Identity> ids = new LinkedList<Identity>();
				ids.add(recipient);
				result.put("zidel@" + BASE32_USERNAME + ".freemail", ids);
				return result;
			}
		};

		FakeSocket sock = new FakeSocket();
		SMTPHandler handler = new SMTPHandler(accManager, sock, matcher);
		Thread smtpThread = new Thread(handler);
		smtpThread.start();
		PrintWriter toHandler = new PrintWriter(sock.getOutputStreamOtherSide());
		BufferedReader fromHandler = new BufferedReader(new InputStreamReader(sock.getInputStreamOtherSide()));

		for(Command cmd : commands) {
			if(cmd.command != null) {
				toHandler.write(cmd.command + "\r\n");
				toHandler.flush();
			}

			for(String reply : cmd.replies) {
				assertEquals(reply, fromHandler.readLine());
			}
		}

		//QUIT
		toHandler.write("QUIT\r\n");
		toHandler.flush();

		//Accept null here since the socket might have closed before we read
		String line = fromHandler.readLine();
		if(line != null && !line.equals("221 localhost")) {
			fail("Expected final line to be 221 localhost, but was " + line);
		}

		handler.kill();
		sock.close();
		try {
			smtpThread.join();
		} catch(InterruptedException e) {
			fail("Caught unexpected InterruptedException");
		}
	}

	private class Command {
		private final String command;
		private final List<String> replies = new LinkedList<String>();

		private Command(String command, String ... replies) {
			this.command = command;
			for(String reply : replies) {
				this.replies.add(reply);
			}
		}
	}
}

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

import utils.TextProtocolTester.Command;
import utils.TextProtocolTester;
import utils.UnitTestParameters;
import utils.Utils;
import fakes.NullIdentity;
import fakes.NullMessageHandler;
import fakes.FakeSocket;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.support.api.Bucket;
import freenet.support.io.BucketTools;

public class SMTPSessionTest {
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
		Assume.assumeTrue(UnitTestParameters.EXTENSIVE);

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

		runSimpleSessionTest(commands, true, Collections.singletonList(message),
		                     Collections.singletonList("zidel@" + BASE32_USERNAME + ".freemail"));
	}

	/**
	 * Runs through a full session sending a simple message to one recipient where MessageHandler
	 * returns a failure.
	 *
	 * @throws IOException on IO errors with SMTP thread, should never happen
	 */
	@Test
	public void simpleSessionSendFails() throws IOException {
		Assume.assumeTrue(UnitTestParameters.EXTENSIVE);

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
		commands.add(new Command(message + ".\r\n", "452 Message sending failed"));

		runSimpleSessionTest(commands, false, Collections.singletonList(message),
		                Collections.singletonList("zidel@" + BASE32_USERNAME + ".freemail"));
	}

	/**
	 * Tests that the correct message is handed to the MessageHandler when the message contains
	 * dot padding (see <a href="https://tools.ietf.org/html/rfc5321#section-4.5.2">RFC5321 section 4.5.2</a>).
	 *
	 * @throws IOException on IO errors with SMTP thread, should never happen
	 */
	@Test
	public void messageWithDotPadding() throws IOException {
		Assume.assumeTrue(UnitTestParameters.EXTENSIVE);

		final String message =
				  "Date: Thu, 21 May 1998 05:33:29 -0700\r\n"
				+ "From: zidel <zidel@" + BASE32_USERNAME + ".freemail>\r\n"
				+ "Subject: Freemail SMTP test\r\n"
				+ "To: zidel <zidel@" + BASE32_USERNAME + ".freemail>\r\n"
				+ "\r\n"
				+ "This is a simple SMTP test for Freemail\r\n"
				+ ".\r\n";
		final String padded =
				  "Date: Thu, 21 May 1998 05:33:29 -0700\r\n"
				+ "From: zidel <zidel@" + BASE32_USERNAME + ".freemail>\r\n"
				+ "Subject: Freemail SMTP test\r\n"
				+ "To: zidel <zidel@" + BASE32_USERNAME + ".freemail>\r\n"
				+ "\r\n"
				+ "This is a simple SMTP test for Freemail\r\n"
				+ "..\r\n";

		String authData = new String(Base64.encode(("\0" + BASE64_USERNAME + "\0" + PASSWORD).getBytes("ASCII")), "ASCII");
		List<Command> commands = new LinkedList<Command>();
		commands.add(new Command(null, "220 localhost ready"));
		commands.add(new Command("EHLO", "250-localhost",
		                                 "250 AUTH LOGIN PLAIN"));
		commands.add(new Command("AUTH PLAIN " + authData, "235 Authenticated"));
		commands.add(new Command("MAIL FROM:<zidel@" + BASE32_USERNAME + ".freemail>", "250 OK"));
		commands.add(new Command("RCPT TO:<zidel@" + BASE32_USERNAME + ".freemail>", "250 OK"));
		commands.add(new Command("DATA", "354 Go crazy"));
		commands.add(new Command(padded + ".\r\n", "250 So be it"));

		runSimpleSessionTest(commands, true, Collections.singletonList(message),
		                Collections.singletonList("zidel@" + BASE32_USERNAME + ".freemail"));
	}

	/**
	 * Sends two messages in the same session to test that the state is reset properly. This checks
	 * for bugs like the one fixed in 76444b878e where the state isn't cleared properly before the
	 * second mail transaction starts.
	 *
	 * @throws IOException on IO errors with SMTP thread, should never happen
	 */
	@Test
	public void twoMessagesInOneSession() throws IOException {
		Assume.assumeTrue(UnitTestParameters.EXTENSIVE);

		final String message1 =
				  "Date: Thu, 21 May 1998 05:33:29 -0700\r\n"
				+ "From: zidel <zidel1@" + BASE32_USERNAME + ".freemail>\r\n"
				+ "Subject: Freemail SMTP test\r\n"
				+ "To: zidel <zidel1@" + BASE32_USERNAME + ".freemail>\r\n"
				+ "\r\n"
				+ "This is test message 1.\r\n";

		final String message2 =
				  "Date: Thu, 21 May 1998 05:33:29 -0700\r\n"
				+ "From: zidel <zidel2@" + BASE32_USERNAME + ".freemail>\r\n"
				+ "Subject: Freemail SMTP test\r\n"
				+ "To: zidel <zidel2@" + BASE32_USERNAME + ".freemail>\r\n"
				+ "\r\n"
				+ "This is test message 2.\r\n";

		String authData = new String(Base64.encode(("\0" + BASE64_USERNAME + "\0" + PASSWORD).getBytes("ASCII")), "ASCII");
		List<Command> commands = new LinkedList<Command>();
		commands.add(new Command(null, "220 localhost ready"));
		commands.add(new Command("EHLO", "250-localhost",
		                                 "250 AUTH LOGIN PLAIN"));
		commands.add(new Command("AUTH PLAIN " + authData, "235 Authenticated"));

		//Message 1
		commands.add(new Command("MAIL FROM:<zidel@" + BASE32_USERNAME + ".freemail>", "250 OK"));
		commands.add(new Command("RCPT TO:<zidel@" + BASE32_USERNAME + ".freemail>", "250 OK"));
		commands.add(new Command("DATA", "354 Go crazy"));
		commands.add(new Command(message1 + ".\r\n", "250 So be it"));

		//Message 2
		commands.add(new Command("MAIL FROM:<zidel2@" + BASE32_USERNAME + ".freemail>", "250 OK"));
		commands.add(new Command("RCPT TO:<zidel2@" + BASE32_USERNAME + ".freemail>", "250 OK"));
		commands.add(new Command("DATA", "354 Go crazy"));
		commands.add(new Command(message2 + ".\r\n", "250 So be it"));

		List<String> messages = new ArrayList<String>(2);
		messages.add(message1);
		messages.add(message2);

		List<String> recipients = new ArrayList<String>(2);
		recipients.add("zidel@" + BASE32_USERNAME + ".freemail");
		recipients.add("zidel2@" + BASE32_USERNAME + ".freemail");

		runSimpleSessionTest(commands, true, messages, recipients);
	}

	/**
	 * Sets up the SMTP server and supporting mocks and runs through the commands and the expected
	 * responses, then quit. The list of recipients can only contain one recipient per message.
	 *
	 * @param commands the commands to be sent and their responses
	 * @param sendResult the result that will be returned from MessageHandler.sendMessage()
	 * @param messages a list of the messages that the MessageHandler should receive
	 * @param rcpts a list of the recipients that are expected
	 * @throws IOException on IO errors with SMTP thread, should never happen
	 */
	public void runSimpleSessionTest(List<Command> commands, final boolean sendResult, final List<String> messages,
	                                 final List<String> rcpts) throws IOException {
		assertEquals(messages.size(), rcpts.size());

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
			private int offset = 0;

			@Override
			public boolean sendMessage(List<Identity> recipients, Bucket msg) throws IOException {
				assertEquals(1, recipients.size());
				assertEquals(recipient, recipients.get(0));
				assertEquals(messages.get(offset++), new String(BucketTools.toByteArray(msg), "ASCII"));

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
			private int offset = 0;

			@Override
			public Map<String, List<Identity>> matchIdentities(Set<String> recipients, String wotOwnIdentity, EnumSet<MatchMethod> methods) throws PluginNotFoundException {
				String curRcpt = rcpts.get(offset++);

				assertEquals(1, recipients.size());
				assertEquals(curRcpt, recipients.iterator().next());
				assertEquals(BASE64_USERNAME, wotOwnIdentity);

				Map<String, List<Identity>> result = new HashMap<String, List<Identity>>();
				List<Identity> ids = new LinkedList<Identity>();
				ids.add(recipient);
				result.put(curRcpt, ids);
				return result;
			}
		};

		FakeSocket sock = new FakeSocket();
		SMTPHandler handler = new SMTPHandler(accManager, sock, matcher);
		Thread smtpThread = new Thread(handler);
		smtpThread.start();

		try {
			TextProtocolTester protocolTester = new TextProtocolTester(sock);
			protocolTester.runProtocolTest(commands);
		} finally {
			handler.kill();
			sock.close();
			try {
				smtpThread.join();
			} catch(InterruptedException e) {
				fail("Caught unexpected InterruptedException");
			}
		}
	}
}

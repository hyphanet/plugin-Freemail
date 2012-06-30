/*
 * IMAPHandlerTest.java
 * This file is part of Freemail, copyright (C) 2011 Martin Nyhus
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import org.freenetproject.freemail.AccountManager;
import org.freenetproject.freemail.imap.IMAPHandler;

import fakes.ConfigurableAccountManager;
import fakes.FakeSocket;

public class IMAPHandlerTest extends IMAPTestWithMessages {
	public void testIMAPGreeting() throws IOException {
		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("* OK [CAPABILITY IMAP4rev1 CHILDREN NAMESPACE] Freemail ready - hit me with your rhythm stick.");

		runSimpleTest(new LinkedList<String>(), expectedResponse);
	}

	public void testIMAPLogin() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("* OK [CAPABILITY IMAP4rev1 CHILDREN NAMESPACE] Freemail ready - hit me with your rhythm stick.");
		expectedResponse.add("0001 OK Logged in");

		runSimpleTest(commands, expectedResponse);
	}

	public void testFailedIMAPLogin() throws IOException {
		FakeSocket sock = new FakeSocket();
		AccountManager accManager = new ConfigurableAccountManager(accountManagerDir, true, accountDirs);

		new Thread(new IMAPHandler(accManager, sock)).start();

		PrintWriter toHandler = new PrintWriter(sock.getOutputStreamOtherSide());
		BufferedReader fromHandler = new BufferedReader(new InputStreamReader(sock.getInputStreamOtherSide()));

		//Read the greeting
		String line = fromHandler.readLine();

		send(toHandler, "0001 LOGIN " + IMAP_USERNAME + " test\r\n");

		line = readTaggedResponse(fromHandler);
		assertEquals("0001 NO Login failed", line);

		assertFalse(fromHandler.ready());
	}

	public void testIMAPSelect() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");

		runSimpleTest(commands, INITIAL_RESPONSES);
	}

	/*
	 * This checks for the bug fixed in commit ad0b9aedf34f19ba7ed06757cdb53ca9d5614add.
	 * The IMAP thread would crash with a NullPointerException when receiving list with no arguments
	 */
	public void testIMAPListWithNoArguments() throws IOException, InterruptedException {
		FakeSocket sock = new FakeSocket();
		AccountManager accManager = new ConfigurableAccountManager(accountManagerDir, false, accountDirs);

		Thread imapThread = new Thread(new IMAPHandler(accManager, sock));
		imapThread.start();

		PrintWriter toHandler = new PrintWriter(sock.getOutputStreamOtherSide());
		BufferedReader fromHandler = new BufferedReader(new InputStreamReader(sock.getInputStreamOtherSide()));

		fromHandler.readLine(); //Greeting

		send(toHandler, "0001 LOGIN " + IMAP_USERNAME + " test\r\n");
		readTaggedResponse(fromHandler);

		send(toHandler, "0002 SELECT INBOX\r\n");
		readTaggedResponse(fromHandler);

		//This would crash the IMAP thread
		send(toHandler, "0003 LIST\r\n");

		Thread.sleep(100);

		//Check the state of the imap thread. Hopefully it will have had time to deal with the
		//command by now.
		assertFalse(imapThread.getState().equals(Thread.State.TERMINATED));
	}

	public void testIMAPSelectUnknown() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT ShouldNotExist\r\n");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("* OK [CAPABILITY IMAP4rev1 CHILDREN NAMESPACE] Freemail ready - hit me with your rhythm stick.");
		expectedResponse.add("0001 OK Logged in");
		expectedResponse.add("0002 NO No such mailbox");

		runSimpleTest(commands, expectedResponse);
	}

	public void testUnimplementedCommand() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 NoSuchCommand");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("* OK [CAPABILITY IMAP4rev1 CHILDREN NAMESPACE] Freemail ready - hit me with your rhythm stick.");
		expectedResponse.add("0001 NO Sorry - not implemented");

		runSimpleTest(commands, expectedResponse);
	}

	public void testLogout() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGOUT");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("* OK [CAPABILITY IMAP4rev1 CHILDREN NAMESPACE] Freemail ready - hit me with your rhythm stick.");
		expectedResponse.add("* BYE");
		expectedResponse.add("0001 OK Bye");

		runSimpleTest(commands, expectedResponse);
	}

	public void testCapability() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 CAPABILITY");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("* OK [CAPABILITY IMAP4rev1 CHILDREN NAMESPACE] Freemail ready - hit me with your rhythm stick.");
		expectedResponse.add("* CAPABILITY IMAP4rev1 CHILDREN NAMESPACE");
		expectedResponse.add("0001 OK Capability completed");

		runSimpleTest(commands, expectedResponse);
	}

	public void testNoop() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 NOOP");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("* OK [CAPABILITY IMAP4rev1 CHILDREN NAMESPACE] Freemail ready - hit me with your rhythm stick.");
		expectedResponse.add("0001 OK NOOP completed");

		runSimpleTest(commands, expectedResponse);
	}

	public void testLoginWithoutArguments() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("* OK [CAPABILITY IMAP4rev1 CHILDREN NAMESPACE] Freemail ready - hit me with your rhythm stick.");
		expectedResponse.add("0001 BAD Not enough arguments");

		runSimpleTest(commands, expectedResponse);
	}

	public void testLoginWithoutPassword() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME);

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("* OK [CAPABILITY IMAP4rev1 CHILDREN NAMESPACE] Freemail ready - hit me with your rhythm stick.");
		expectedResponse.add("0001 BAD Not enough arguments");

		runSimpleTest(commands, expectedResponse);
	}

	public void testImplicitExpungeOnClose() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 STORE 1 +FLAGS (\\Deleted)");
		commands.add("0004 CLOSE");
		commands.add("0005 SELECT INBOX");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 1 FETCH FLAGS (\\Seen \\Deleted)");
		expectedResponse.add("0003 OK Store completed");
		expectedResponse.add("0004 OK Mailbox closed");
		expectedResponse.add("* FLAGS (\\Seen \\Answered \\Flagged \\Deleted \\Draft \\Recent)");
		expectedResponse.add("* OK [PERMANENTFLAGS (\\Seen \\Answered \\Flagged \\Deleted \\Draft \\Recent)] Limited");
		expectedResponse.add("* 8 EXISTS");
		expectedResponse.add("* 0 RECENT");
		expectedResponse.add("* OK [UIDVALIDITY 1] Ok");
		expectedResponse.add("0005 OK [READ-WRITE] Done");

		runSimpleTest(commands, expectedResponse);
	}

	public void testNoImplicitExpungeOnSelect() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 STORE 1 +FLAGS (\\Deleted)");
		commands.add("0004 SELECT INBOX");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 1 FETCH FLAGS (\\Seen \\Deleted)");
		expectedResponse.add("0003 OK Store completed");
		expectedResponse.add("* FLAGS (\\Seen \\Answered \\Flagged \\Deleted \\Draft \\Recent)");
		expectedResponse.add("* OK [PERMANENTFLAGS (\\Seen \\Answered \\Flagged \\Deleted \\Draft \\Recent)] Limited");
		expectedResponse.add("* 9 EXISTS");
		expectedResponse.add("* 0 RECENT");
		expectedResponse.add("* OK [UIDVALIDITY 1] Ok");
		expectedResponse.add("0004 OK [READ-WRITE] Done");

		runSimpleTest(commands, expectedResponse);
	}

	public void testExplicitExpunge() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 STORE 1:2 +FLAGS (\\Deleted)");
		commands.add("0004 STORE 4 +FLAGS (\\Deleted)");
		commands.add("0005 EXPUNGE");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 1 FETCH FLAGS (\\Seen \\Deleted)");
		expectedResponse.add("* 2 FETCH FLAGS (\\Seen \\Deleted)");
		expectedResponse.add("0003 OK Store completed");
		expectedResponse.add("* 4 FETCH FLAGS (\\Seen \\Deleted)");
		expectedResponse.add("0004 OK Store completed");
		expectedResponse.add("* 1 EXPUNGE");
		expectedResponse.add("* 1 EXPUNGE");
		expectedResponse.add("* 2 EXPUNGE");
		expectedResponse.add("0005 OK Expunge complete");

		runSimpleTest(commands, expectedResponse);
	}

	public void testLiteralWithoutEndingLinebreak() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");

		/*
		 * Now send the literal that doesn't end with \r\n. Note that the 'A' belongs to the
		 * literal, not to the tag of the logout command. If the logout command is read as part of
		 * the literal (which is a bug) the last command will be read instead and the test will fail
		 * due to the unexpected output ("0004 NO Sorry - not implemented").
		 */
		commands.add("0002 APPEND INBOX {1}");
		commands.add("A0003 LOGOUT");
		commands.add("0004 ShouldNotRun");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("* OK [CAPABILITY IMAP4rev1 CHILDREN NAMESPACE] Freemail ready - hit me with your rhythm stick.");
		expectedResponse.add("0001 OK Logged in");
		expectedResponse.add("+ OK");
		expectedResponse.add("0002 OK APPEND completed");
		expectedResponse.add("* BYE");
		expectedResponse.add("0003 OK Bye");

		runSimpleTest(commands, expectedResponse);
	}
}

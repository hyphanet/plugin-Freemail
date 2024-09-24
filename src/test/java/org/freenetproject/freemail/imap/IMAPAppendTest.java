/*
 * IMAPAppendTest.java
 * This file is part of Freemail, copyright (C) 2012 Martin Nyhus
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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import utils.TextProtocolTester.Command;

public class IMAPAppendTest extends IMAPTestWithMessages {
	@Test
	public void basicAppendFromSelectedState() throws IOException {
		List<Command> commands = new LinkedList<Command>();
		commands.addAll(connectSequence());
		commands.addAll(loginSequence("0001"));
		commands.addAll(selectInboxSequence("0002"));
		commands.add(new Command("0003 APPEND INBOX {23}",
		                         "+ OK"));
		commands.add(new Command("Subject: Test message",
		                         "0003 OK APPEND completed"));
		commands.add(new Command("0004 UID FETCH 10:* FLAGS",
		                         "* 9 FETCH (FLAGS () UID 10)",
		                         "* 10 FETCH (FLAGS (\\Recent) UID 11)",
		                         "0004 OK Fetch completed"));

		runSimpleTest(commands);
	}

	@Test
	public void appendWithFlag() throws IOException {
		List<Command> commands = new LinkedList<Command>();
		commands.addAll(connectSequence());
		commands.addAll(loginSequence("0001"));
		commands.addAll(selectInboxSequence("0002"));
		commands.add(new Command("0003 APPEND INBOX (\\Seen) {23}",
		                         "+ OK"));
		commands.add(new Command("Subject: Test message",
		                         "0003 OK APPEND completed"));
		commands.add(new Command("0004 UID FETCH 10:* FLAGS",
		                         "* 9 FETCH (FLAGS () UID 10)",
		                         "* 10 FETCH (FLAGS (\\Seen \\Recent) UID 11)",
		                         "0004 OK Fetch completed"));

		runSimpleTest(commands);
	}

	/*
	 * The expectation here is that the custom flag is ignored since they aren't supported, but the
	 * \Seen flag should still be saved.
	 */
	@Test
	public void appendWithCustomFlag() throws IOException {
		List<Command> commands = new LinkedList<Command>();
		commands.addAll(connectSequence());
		commands.addAll(loginSequence("0001"));
		commands.addAll(selectInboxSequence("0002"));
		commands.add(new Command("0003 APPEND INBOX (\\Seen custom) {23}",
		                         "+ OK"));
		commands.add(new Command("Subject: Test message",
		                         "0003 OK APPEND completed"));
		commands.add(new Command("0004 UID FETCH 10:* FLAGS",
		                         "* 9 FETCH (FLAGS () UID 10)",
		                         "* 10 FETCH (FLAGS (\\Seen \\Recent) UID 11)",
		                         "0004 OK Fetch completed"));

		runSimpleTest(commands);
	}

	@Test
	public void appendWithTwoStandardFlags() throws IOException {
		List<Command> commands = new LinkedList<Command>();
		commands.addAll(connectSequence());
		commands.addAll(loginSequence("0001"));
		commands.addAll(selectInboxSequence("0002"));
		commands.add(new Command("0003 APPEND INBOX (\\Seen \\Flagged) {23}",
		                         "+ OK"));
		commands.add(new Command("Subject: Test message",
		                         "0003 OK APPEND completed"));
		commands.add(new Command("0004 UID FETCH 10:* FLAGS",
		                         "* 9 FETCH (FLAGS () UID 10)",
		                         "* 10 FETCH (FLAGS (\\Seen \\Flagged \\Recent) UID 11)",
		                         "0004 OK Fetch completed"));

		runSimpleTest(commands);
	}

	@Test
	public void appendWithFlagAndDate() throws IOException {
		List<Command> commands = new LinkedList<Command>();
		commands.addAll(connectSequence());
		commands.addAll(loginSequence("0001"));
		commands.addAll(selectInboxSequence("0002"));
		commands.add(new Command("0003 APPEND INBOX (\\Seen) \"23-Oct-2007 19:05:17 +0100\" {39}",
		                         "+ OK"));
		commands.add(new Command("Subject: Test message"));
		commands.add(new Command(""));
		commands.add(new Command("Test message",
		                         "0003 OK APPEND completed"));
		commands.add(new Command("0004 UID FETCH 10:* FLAGS",
		                         "* 9 FETCH (FLAGS () UID 10)",
		                         "* 10 FETCH (FLAGS (\\Seen \\Recent) UID 11)",
		                         "0004 OK Fetch completed"));

		runSimpleTest(commands);
	}

	@Test
	public void appendWithBadLiteralLength() throws IOException {
		List<Command> commands = new LinkedList<Command>();
		commands.addAll(connectSequence());
		commands.addAll(loginSequence("0001"));
		commands.addAll(selectInboxSequence("0002"));
		commands.add(new Command("0003 APPEND INBOX {BAD}",
		                         "0003 BAD Unable to parse literal length"));

		runSimpleTest(commands);
	}

	@Test
	public void multilineAppend() throws IOException {
		List<Command> commands = new LinkedList<Command>();
		commands.addAll(connectSequence());
		commands.addAll(loginSequence("0001"));
		commands.addAll(selectInboxSequence("0002"));
		commands.add(new Command("7 append \"INBOX\" (\\Seen) {42}",
		                         "+ OK"));
		commands.add(new Command("To: zidel@zidel.freemail"));
		commands.add(new Command(""));
		commands.add(new Command("Test message",
		                         "7 OK APPEND completed"));

		runSimpleTest(commands);
	}

	@Test
	public void multilineAppendWithTwoFlags() throws IOException {
		List<Command> commands = new LinkedList<Command>();
		commands.addAll(connectSequence());
		commands.addAll(loginSequence("0001"));
		commands.addAll(selectInboxSequence("0002"));
		commands.add(new Command("7 append \"INBOX\" (\\Seen \\Deleted) {42}",
		                         "+ OK"));
		commands.add(new Command("To: zidel@zidel.freemail"));
		commands.add(new Command(""));
		commands.add(new Command("Test message",
		                         "7 OK APPEND completed"));

		runSimpleTest(commands);
	}

	/*
	 * This checks for the bug fixed in c43fcb18df185a5c67fbfcabf31eca22f44b7493.
	 * The IMAP handler thread would crash with a NullPointerException if
	 * append was called with a subfolder of index before logging in.
	 */
	@Test
	public void appendWithSubfolderBeforeLogin() throws IOException {
		List<Command> commands = new LinkedList<Command>();
		commands.addAll(connectSequence());
		commands.add(new Command("0001 APPEND inbox.folder arg2",
		                         "0001 NO Must be authenticated"));

		runSimpleTest(commands);
	}

	@Test
	public void appendWithoutArguments() throws IOException {
		List<Command> commands = new LinkedList<Command>();
		commands.addAll(connectSequence());
		commands.addAll(loginSequence("0001"));
		commands.addAll(selectInboxSequence("0002"));
		commands.add(new Command("0002 APPEND",
		                         "0002 BAD Not enough arguments"));

		runSimpleTest(commands);
	}

	@Test
	public void appendWithoutMessageLiteral() throws IOException {
		List<Command> commands = new LinkedList<Command>();
		commands.addAll(connectSequence());
		commands.addAll(loginSequence("0001"));
		commands.addAll(selectInboxSequence("0002"));
		commands.add(new Command("0002 APPEND inbox",
		                         "0002 BAD Not enough arguments"));

		runSimpleTest(commands);
	}

	@Test
	public void appendWithMoreThan3Flags() throws IOException {
		List<Command> commands = new LinkedList<Command>();
		commands.addAll(connectSequence());
		commands.addAll(loginSequence("0001"));
		commands.addAll(selectInboxSequence("0002"));
		commands.add(new Command("0003 APPEND \"INBOX\" (\\Seen \\Answered \\Flagged \\Deleted \\Draft) {42}",
		                         "+ OK"));
		commands.add(new Command("To: zidel@zidel.freemail"));
		commands.add(new Command(""));
		commands.add(new Command("Test message",
		                         "0003 OK APPEND completed"));
		commands.add(new Command("0004 FETCH * (UID FLAGS)",
		                         "* 10 FETCH (UID 11 FLAGS (\\Seen \\Answered \\Flagged \\Deleted \\Draft \\Recent))",
		                         "0004 OK Fetch completed"));

		runSimpleTest(commands);
	}

	@Test
	public void appendToMailboxThatDoesntExist() throws IOException {
		List<Command> commands = new LinkedList<Command>();
		commands.addAll(connectSequence());
		commands.addAll(loginSequence("0001"));
		commands.addAll(selectInboxSequence("0002"));
		commands.add(new Command("0003 APPEND \"INBOX.NoSuchMailbox\" {42}",
		                         "0003 NO [TRYCREATE] No such mailbox"));

		runSimpleTest(commands);
	}
}

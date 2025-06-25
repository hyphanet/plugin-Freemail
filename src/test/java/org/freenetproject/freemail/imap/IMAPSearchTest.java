/*
 * IMAPSearchTest.java
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

package org.freenetproject.freemail.imap;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import utils.TextProtocolTester.Command;

public class IMAPSearchTest extends IMAPTestWithMessages {
	@Test
	public void searchForUndeleted() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 SEARCH UNDELETED");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* SEARCH 1 2 3 4 5 6 7 8 9");
		expectedResponse.add("0003 OK Search completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidSearchForUndeleted() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID SEARCH UNDELETED");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* SEARCH 1 2 3 4 6 7 8 9 10");
		expectedResponse.add("0003 OK Search completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void searchWithNoMatches() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 SEARCH DELETED UNDELETED");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* SEARCH");
		expectedResponse.add("0003 OK Search completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void searchWithExtraParansAndOneKey() throws IOException {
		List<Command> commands = new LinkedList<Command>();
		commands.addAll(connectSequence());
		commands.addAll(loginSequence("0001"));
		commands.addAll(selectInboxSequence("0002"));

		commands.add(new Command("0003 SEARCH (ALL)",
				"* SEARCH 1 2 3 4 5 6 7 8 9",
				"0003 OK Search completed"));

		runSimpleTest(commands);
	}

	@Test
	public void searchWithExtraParansAndTwoKeys() throws IOException {
		List<Command> commands = new LinkedList<Command>();
		commands.addAll(connectSequence());
		commands.addAll(loginSequence("0001"));
		commands.addAll(selectInboxSequence("0002"));

		commands.add(new Command("0003 SEARCH (ALL ALL)",
				"* SEARCH 1 2 3 4 5 6 7 8 9",
				"0003 OK Search completed"));

		runSimpleTest(commands);
	}

	@Test
	public void searchWithExtraParansAndIllegalWhitespace() throws IOException {
		List<Command> commands = new LinkedList<Command>();
		commands.addAll(connectSequence());
		commands.addAll(loginSequence("0001"));
		commands.addAll(selectInboxSequence("0002"));

		commands.add(new Command("0003 SEARCH ( ALL ALL )",
				"0003 BAD Extra space between paranthesis and search-key"));

		runSimpleTest(commands);
	}
}

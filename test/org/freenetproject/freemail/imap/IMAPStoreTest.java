/*
 * IMAPStoreTest.java
 * This file is part of Freemail, copyright (C) 2012
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

public class IMAPStoreTest extends IMAPTestWithMessages {
	@Test
	public void storeWithoutArguments() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 STORE");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 BAD Not enough arguments");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void storeWithoutFlags() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 STORE 1");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 BAD Not enough arguments");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void storeWithoutFlagsList() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 STORE 1 FLAGS");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 BAD Not enough arguments to store flags");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void simpleStore() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 STORE 1 FLAGS \\Seen");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 1 FETCH FLAGS (\\Seen)");
		expectedResponse.add("0003 OK Store completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void addMessageFlags() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 STORE 1 +FLAGS (\\Seen \\Flagged \\Answered)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 1 FETCH FLAGS (\\Seen \\Answered \\Flagged)");
		expectedResponse.add("0003 OK Store completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void storeWithBadRangeStart() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 STORE BAD:12 +FLAGS (\\Seen \\Flagged \\Answered)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 BAD Illegal sequence number set");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void storeWithBadRangeEnd() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 STORE 11:BAD +FLAGS (\\Seen \\Flagged \\Answered)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 BAD Illegal sequence number set");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void storeToMsgWithDifferentSeqNumAndUid() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 STORE 7 Flags (\\Seen)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 7 FETCH FLAGS (\\Seen)");
		expectedResponse.add("0003 OK Store completed");

		runSimpleTest(commands, expectedResponse);
	}

	/*
	 * This tests for the bug fixed in commit 6219756e where \Seen was added
	 * automatically when \Deleted was added
	 */
	@Test
	public void storeDeleteFlag() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 STORE 7 +FLAGS (\\Deleted)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 7 FETCH FLAGS (\\Deleted)");
		expectedResponse.add("0003 OK Store completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void storeWithWildcardFirst() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 STORE *:7 FLAGS (\\Seen)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 7 FETCH FLAGS (\\Seen)");
		expectedResponse.add("* 8 FETCH FLAGS (\\Seen)");
		expectedResponse.add("* 9 FETCH FLAGS (\\Seen)");
		expectedResponse.add("0003 OK Store completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void storeWithWildcardLast() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 STORE 7:* FLAGS (\\Seen)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 7 FETCH FLAGS (\\Seen)");
		expectedResponse.add("* 8 FETCH FLAGS (\\Seen)");
		expectedResponse.add("* 9 FETCH FLAGS (\\Seen)");
		expectedResponse.add("0003 OK Store completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void storeWithMessageId0() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 STORE 0 FLAGS \\Seen");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 NO Invalid message ID");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void storeWithRangeFrom0() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT inbox");
		commands.add("0003 STORE 0:1 FLAGS (\\Seen)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 NO Invalid message ID");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void storeWithRangeTo0() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT inbox");
		commands.add("0003 STORE 1:0 FLAGS (\\Seen)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 NO Invalid message ID");

		runSimpleTest(commands, expectedResponse);
	}
}

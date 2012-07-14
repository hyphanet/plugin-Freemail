/*
 * IMAPUidStoreTest.java
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

public class IMAPUidStoreTest extends IMAPTestWithMessages {
	public void testUidStoreWithoutArguments() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID STORE");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 BAD Not enough arguments to uid command");

		runSimpleTest(commands, expectedResponse);
	}

	public void testUidStoreWithoutFlags() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID STORE 1");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 BAD Not enough arguments to uid command");

		runSimpleTest(commands, expectedResponse);
	}

	public void testUidStoreWithoutFlagsList() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID STORE 1 FLAGS");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 BAD Not enough arguments to uid command");

		runSimpleTest(commands, expectedResponse);
	}

	public void testSimpleUidStore() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID STORE 1 FLAGS \\Seen");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 1 FETCH (UID 1 FLAGS (\\Seen))");
		expectedResponse.add("0003 OK Store completed");

		runSimpleTest(commands, expectedResponse);
	}

	public void testAddMessageFlags() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID STORE 1 +FLAGS (\\Seen \\Flagged \\Answered)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 1 FETCH (UID 1 FLAGS (\\Seen \\Answered \\Flagged))");
		expectedResponse.add("0003 OK Store completed");

		runSimpleTest(commands, expectedResponse);
	}

	public void testUidStoreWithEmptySet() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID STORE 11:12 +FLAGS (\\Seen \\Flagged \\Answered)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 OK Store completed");

		runSimpleTest(commands, expectedResponse);
	}

	public void testUidStoreWithBadRangeStart() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID STORE BAD:12 +FLAGS (\\Seen \\Flagged \\Answered)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 BAD Illegal sequence number set");

		runSimpleTest(commands, expectedResponse);
	}

	public void testUidStoreWithBadRangeEnd() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID STORE 11:BAD +FLAGS (\\Seen \\Flagged \\Answered)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 BAD Illegal sequence number set");

		runSimpleTest(commands, expectedResponse);
	}

	public void testUidStoreToMsgWithDifferentSeqNumAndUid() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID STORE 7 Flags (\\Seen)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 6 FETCH (UID 7 FLAGS (\\Seen))");
		expectedResponse.add("0003 OK Store completed");

		runSimpleTest(commands, expectedResponse);
	}

	/*
	 * This tests for the bug fixed in commit 6219756e where \Seen was added
	 * automatically when \Deleted was added
	 */
	public void testUidStoreDeleteFlag() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID STORE 7 +FLAGS (\\Deleted)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 6 FETCH (UID 7 FLAGS (\\Deleted))");
		expectedResponse.add("0003 OK Store completed");

		runSimpleTest(commands, expectedResponse);
	}

	public void testUidStoreWithUidsThatDontExist() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID STORE 3:7 +Flags (\\Seen)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 3 FETCH (UID 3 FLAGS (\\Seen))");
		expectedResponse.add("* 4 FETCH (UID 4 FLAGS (\\Seen))");
		expectedResponse.add("* 5 FETCH (UID 6 FLAGS (\\Seen))");
		expectedResponse.add("* 6 FETCH (UID 7 FLAGS (\\Seen))");
		expectedResponse.add("0003 OK Store completed");

		runSimpleTest(commands, expectedResponse);
	}

	public void testUidStoreOtherThanFlag() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID STORE 3:7 Message");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 BAD Can't store that");

		runSimpleTest(commands, expectedResponse);
	}

	public void testUidStoreRemoveFlags() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID STORE 3 FLAGS (\\Seen)");
		commands.add("0004 UID STORE 3 -FLAGS (\\Seen)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 3 FETCH (UID 3 FLAGS (\\Seen))");
		expectedResponse.add("0003 OK Store completed");
		expectedResponse.add("* 3 FETCH (UID 3 FLAGS ())");
		expectedResponse.add("0004 OK Store completed");

		runSimpleTest(commands, expectedResponse);
	}

	public void testUidStoreRemoveNonexistentFlag() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID STORE 3 -FLAGS (\\Seen)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 3 FETCH (UID 3 FLAGS ())");
		expectedResponse.add("0004 OK Store completed");

		runSimpleTest(commands, expectedResponse);
	}
}

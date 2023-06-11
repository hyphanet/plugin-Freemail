/*
 * IMAPUidCopyTest.java
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

public class IMAPUidCopyTest extends IMAPTestWithMessages {
	@Test
	public void uidCopySingleMessageToSameFolder() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 SEARCH ALL");
		commands.add("0004 UID COPY 1 INBOX");
		commands.add("0005 SEARCH ALL");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* SEARCH 1 2 3 4 5 6 7 8 9");
		expectedResponse.add("0003 OK Search completed");
		expectedResponse.add("0004 OK COPY completed");
		expectedResponse.add("* SEARCH 1 2 3 4 5 6 7 8 9 10");
		expectedResponse.add("0005 OK Search completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidCopyWithMessageId0() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID COPY 0 INBOX");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 NO Invalid message ID");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidCopyWithMessageIdRangeFrom0() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID COPY 0:* INBOX");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 NO Invalid message ID");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidCopyWithMessageIdRangeTo0() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID COPY *:0 INBOX");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 NO Invalid message ID");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidCopyWithTooHighMessageIdFirstInRange() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID COPY 11:10 INBOX");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 OK COPY completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidCopyWithTooHighMessageIdLastInRange() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID COPY 10:11 INBOX");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 OK COPY completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidCopyToNonexistentMailbox() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID COPY 1 INBOX.abc");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 NO [TRYCREATE] No such mailbox.");

		runSimpleTest(commands, expectedResponse);
	}

	/**
	 * Check that the \Recent flag is set on the message that results from a
	 * uid copy command, as specified in RFC3501 section 6.4.7.
	 */
	@Test
	public void uidCopySetsRecentFlag() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID COPY 1 INBOX");
		commands.add("0004 FETCH * FLAGS");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 OK COPY completed");
		expectedResponse.add("* 10 FETCH (FLAGS (\\Recent))");
		expectedResponse.add("0004 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidCopyDoesntCreateMailbox() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID COPY 1 INBOX.abc");
		commands.add("0004 SELECT INBOX.abc");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 NO [TRYCREATE] No such mailbox.");
		expectedResponse.add("0004 NO No such mailbox");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidAfterUidCopy() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID SEARCH ALL");
		commands.add("0004 UID COPY 1 INBOX");
		commands.add("0005 UID SEARCH ALL");
		commands.add("0005 FETCH 10 (UID)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* SEARCH 1 2 3 4 6 7 8 9 10");
		expectedResponse.add("0003 OK Search completed");
		expectedResponse.add("0004 OK COPY completed");
		expectedResponse.add("* SEARCH 1 2 3 4 6 7 8 9 10 11");
		expectedResponse.add("0005 OK Search completed");
		expectedResponse.add("* 10 FETCH (UID 11)");
		expectedResponse.add("0005 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidCopyPreservesFlags() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 STORE 1 FLAGS (\\Seen)");
		commands.add("0004 STORE 2 FLAGS (\\Deleted)");
		commands.add("0005 STORE 3 FLAGS (\\Recent)");
		commands.add("0006 UID COPY 1:3 INBOX");
		commands.add("0007 UID FETCH 11:* (UID FLAGS)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 1 FETCH FLAGS (\\Seen)");
		expectedResponse.add("0003 OK Store completed");
		expectedResponse.add("* 2 FETCH FLAGS (\\Deleted)");
		expectedResponse.add("0004 OK Store completed");
		expectedResponse.add("* 3 FETCH FLAGS (\\Recent)");
		expectedResponse.add("0005 OK Store completed");
		expectedResponse.add("0006 OK COPY completed");
		expectedResponse.add("* 10 FETCH (UID 11 FLAGS (\\Seen \\Recent))");
		expectedResponse.add("* 11 FETCH (UID 12 FLAGS (\\Deleted \\Recent))");
		expectedResponse.add("* 12 FETCH (UID 13 FLAGS (\\Recent))");
		expectedResponse.add("0007 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidCopyWithNoArgs() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0006 UID COPY");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0006 BAD Not enough arguments");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidCopyWithoutDestination() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0006 UID COPY *");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0006 BAD Not enough arguments");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidCopyWithIllegalSequenceNumber() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID COPY BAD INBOX");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 BAD Illegal sequence number set");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidCopyWithIllegalSequenceNumberFirstInRange() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID COPY BAD:* INBOX");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 BAD Illegal sequence number set");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidCopyWithIllegalSequenceNumberLastInRange() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID COPY *:BAD INBOX");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 BAD Illegal sequence number set");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidCopyWithIllegalSequenceNumberFirstInList() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID COPY BAD,* INBOX");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 BAD Illegal sequence number set");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidCopyWithIllegalSequenceNumberLastInList() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID COPY *,BAD INBOX");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 BAD Illegal sequence number set");

		runSimpleTest(commands, expectedResponse);
	}
}

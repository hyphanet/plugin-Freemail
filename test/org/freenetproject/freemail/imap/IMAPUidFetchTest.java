/*
 * IMAPUidFetchTest.java
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

public class IMAPUidFetchTest extends IMAPTestWithMessages {
	@Test
	public void uidFetchBodyPeek() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID FETCH 1 (BODY.PEEK[])");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 1 FETCH (BODY[] {32}");
		expectedResponse.add("Subject: IMAP test message 0");
		expectedResponse.add("");
		expectedResponse.add(" UID 1)");
		expectedResponse.add("0003 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidFetchSingleArg() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT inbox");
		commands.add("0003 UID FETCH 10:* BODY.PEEK[]");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 9 FETCH (BODY[] {32}");
		expectedResponse.add("Subject: IMAP test message 9");
		expectedResponse.add("");
		expectedResponse.add(" UID 10)");
		expectedResponse.add("0003 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidFetchWithOnlyUid() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT inbox");
		commands.add("0003 UID FETCH 10:* UID");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 9 FETCH (UID 10)");
		expectedResponse.add("0003 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	/*
	 * In the sequence number range * is the highest sequence number in use and the order of the two
	 * doesn't matter (i.e. 2:4 == 4:2), so 20:* should return the highest numbered message
	 * (assuming * < 20).
	 */
	@Test
	public void uidSequenceNumberRangeWithFirstAboveMax() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT inbox");
		commands.add("0003 UID FETCH 20:* (UID FLAGS)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 9 FETCH (UID 10 FLAGS ())");
		expectedResponse.add("0003 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidSequenceNumberRangeWithWildcardFirst() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT inbox");
		commands.add("0003 UID FETCH *:10 (UID FLAGS)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 9 FETCH (UID 10 FLAGS ())");
		expectedResponse.add("0003 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidFetchBodyStartRange() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID FETCH 1 (BODY.PEEK[]<0.15>)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 1 FETCH (BODY[]<0> {15}");
		expectedResponse.add("Subject: IMAP t UID 1)");
		expectedResponse.add("0003 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidFetchBodyMiddleRange() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID FETCH 1 (BODY.PEEK[]<1.15>)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 1 FETCH (BODY[]<1> {15}");
		expectedResponse.add("ubject: IMAP te UID 1)");
		expectedResponse.add("0003 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidFetchBodyEndRange() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID FETCH 1 (BODY.PEEK[]<15.1000>)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 1 FETCH (BODY[]<15> {17}");
		expectedResponse.add("est message 0");
		expectedResponse.add("");
		expectedResponse.add(" UID 1)");
		expectedResponse.add("0003 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidFetchSequenceNumberRangeWithWildcard() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID FETCH 10:* (UID)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 9 FETCH (UID 10)");
		expectedResponse.add("0003 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidFetchWithSequenceNumberRange() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID FETCH 9:10 (UID)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 8 FETCH (UID 9)");
		expectedResponse.add("* 9 FETCH (UID 10)");
		expectedResponse.add("0003 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidFetchWithOutOfBoundsSequenceNumber() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID FETCH 9:11 (UID)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 8 FETCH (UID 9)");
		expectedResponse.add("* 9 FETCH (UID 10)");
		expectedResponse.add("0003 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidFetchWithInvalidSequenceNumberRange() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID FETCH 9:BAD (UID)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 BAD Illegal sequence number set");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidFetchWithUnterminatedArgumentList() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT inbox");
		commands.add("0003 UID FETCH 10:* (BODY.PEEK[]");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 9 FETCH (BODY[] {32}");
		expectedResponse.add("Subject: IMAP test message 9");
		expectedResponse.add("");
		expectedResponse.add(" UID 10)");
		expectedResponse.add("0003 BAD Unknown attribute in list or unterminated list");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidFetchWithLongArgumentList() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT inbox");
		commands.add("0003 UID FETCH 10:* (UID FLAGS BODY.PEEK[]<0.1>)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 9 FETCH (UID 10 FLAGS () BODY[]<0> {1}");
		expectedResponse.add("S)");
		expectedResponse.add("0003 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidFetchWithoutArguments() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT inbox");
		commands.add("0003 UID FETCH");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 BAD Not enough arguments");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidFetchWithoutDataItems() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT inbox");
		commands.add("0003 UID FETCH *:10");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 BAD Not enough arguments");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidFetchWithInvalidMessageNumberFirst() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT inbox");
		commands.add("0003 UID FETCH BAD:10 UID");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 BAD Illegal sequence number set");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidFetchWithListOfUids() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT inbox");
		commands.add("0003 UID FETCH 7,8 (UID)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 6 FETCH (UID 7)");
		expectedResponse.add("* 7 FETCH (UID 8)");
		expectedResponse.add("0003 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidFetchBodyPeekHeader() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID FETCH 1 (BODY.PEEK[HEADER])");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 1 FETCH (BODY[HEADER] {32}");
		expectedResponse.add("Subject: IMAP test message 0");
		expectedResponse.add("");
		expectedResponse.add(" UID 1)");
		expectedResponse.add("0003 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidFetchWithMessageId0() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID FETCH 0 INBOX");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 NO Invalid message ID");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidFetchWithMessageIdRangeFrom0() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID FETCH 0:* INBOX");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 NO Invalid message ID");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidFetchWithMessageIdRangeTo0() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID FETCH *:0 INBOX");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 NO Invalid message ID");

		runSimpleTest(commands, expectedResponse);
	}
}

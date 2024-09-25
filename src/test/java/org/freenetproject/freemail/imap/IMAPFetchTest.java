/*
 * IMAPFetchTest.java
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

public class IMAPFetchTest extends IMAPTestWithMessages {
	@Test
	public void fetchBodyPeek() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 FETCH 1 (BODY.PEEK[])");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 1 FETCH (BODY[] {32}");
		expectedResponse.add("Subject: IMAP test message 0");
		expectedResponse.add("");
		expectedResponse.add(")");
		expectedResponse.add("0003 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void fetchBodyPeekHeader() throws IOException {
		List<Command> commands = new LinkedList<Command>();
		commands.addAll(connectSequence());
		commands.addAll(loginSequence("0001"));
		commands.addAll(selectInboxSequence("0002"));

		commands.add(new Command("0003 FETCH 1 (BODY.PEEK[HEADER])",
				"* 1 FETCH (BODY[HEADER] {32}",
				"Subject: IMAP test message 0",
				"",
				")",
				"0003 OK Fetch completed"));

		runSimpleTest(commands);
	}

	@Test
	public void fetchBodyPeekText() throws IOException {
		List<Command> commands = new LinkedList<Command>();
		commands.addAll(connectSequence());
		commands.addAll(loginSequence("0001"));
		commands.addAll(selectInboxSequence("0002"));

		commands.add(new Command("0003 FETCH 1 (BODY.PEEK[TEXT])",
				"* 1 FETCH (BODY[TEXT] {0}",
				")",
				"0003 OK Fetch completed"));

		runSimpleTest(commands);
	}

	@Test
	public void fetchBodyStartRange() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 FETCH 1 (BODY.PEEK[]<0.15>)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 1 FETCH (BODY[]<0> {15}");
		expectedResponse.add("Subject: IMAP t)");
		expectedResponse.add("0003 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void fetchBodyMiddleRange() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 FETCH 1 (BODY.PEEK[]<1.15>)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 1 FETCH (BODY[]<1> {15}");
		expectedResponse.add("ubject: IMAP te)");
		expectedResponse.add("0003 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void fetchBodyEndRange() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 FETCH 1 (BODY.PEEK[]<15.1000>)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 1 FETCH (BODY[]<15> {17}");
		expectedResponse.add("est message 0");
		expectedResponse.add("");
		expectedResponse.add(")");
		expectedResponse.add("0003 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void fetchSequenceNumberRangeWithWildcard() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 FETCH 9:* (UID)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 9 FETCH (UID 10)");
		expectedResponse.add("0003 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void fetchWithSequenceNumberRange() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 FETCH 8:9 (UID)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 8 FETCH (UID 9)");
		expectedResponse.add("* 9 FETCH (UID 10)");
		expectedResponse.add("0003 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void fetchWithOutOfBoundsSequenceNumber() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 FETCH 9:11 (UID)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 NO Invalid message ID");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void fetchWithInvalidSequenceNumberRange() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 FETCH 9:BAD (UID)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 BAD Illegal sequence number set");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void fetchWithOnlyUid() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT inbox");
		commands.add("0003 FETCH 9:* UID");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 9 FETCH (UID 10)");
		expectedResponse.add("0003 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void fetchWithUnterminatedArgumentList() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT inbox");
		commands.add("0003 FETCH 9:* (BODY.PEEK[]");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 9 FETCH (BODY[] {32}");
		expectedResponse.add("Subject: IMAP test message 9");
		expectedResponse.add("");
		expectedResponse.add(")");
		expectedResponse.add("0003 BAD Unknown attribute in list or unterminated list");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void fetchWithLongArgumentList() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT inbox");
		commands.add("0003 FETCH 9:* (UID FLAGS BODY.PEEK[]<0.1>)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 9 FETCH (UID 10 FLAGS () BODY[]<0> {1}");
		expectedResponse.add("S)");
		expectedResponse.add("0003 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void fetchDataItem() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT inbox");
		commands.add("0003 FETCH 9:* BODY.PEEK[]");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 9 FETCH (BODY[] {32}");
		expectedResponse.add("Subject: IMAP test message 9");
		expectedResponse.add("");
		expectedResponse.add(")");
		expectedResponse.add("0003 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	/*
	 * In the sequence number range * is the highest sequence number in use and
	 * the order of the two doesn't matter (i.e. 2:4 == 4:2), so 20:* should be
	 * the same as 20:10 in this case which is illegal since the highest
	 * message id is 10.
	 */
	@Test
	public void sequenceNumberRangeWithFirstAboveMax() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT inbox");
		commands.add("0003 FETCH 20:* (UID FLAGS)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 NO Invalid message ID");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void sequenceNumberRangeWithWildcardFirst() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT inbox");
		commands.add("0003 FETCH *:9 (UID FLAGS)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 9 FETCH (UID 10 FLAGS ())");
		expectedResponse.add("0003 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void fetchWithoutArguments() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT inbox");
		commands.add("0003 FETCH");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 BAD Not enough arguments");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void fetchWithoutDataItems() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT inbox");
		commands.add("0003 FETCH *:10");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 BAD Not enough arguments");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void fetchWithMessageId0() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 FETCH 0 INBOX");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 NO Invalid message ID");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void fetchWithRangeFrom0() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT inbox");
		commands.add("0003 FETCH 0:10 UID");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 NO Invalid message ID");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void fetchWithRangeTo0() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT inbox");
		commands.add("0003 FETCH 10:0 UID");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 NO Invalid message ID");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void fetchWithInvalidMessageNumberFirst() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT inbox");
		commands.add("0003 FETCH BAD:10 UID");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("0003 BAD Illegal sequence number set");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void fetchRfc822Header() throws IOException {
		List<Command> commands = new LinkedList<Command>();
		commands.addAll(connectSequence());
		commands.addAll(loginSequence("0001"));
		commands.addAll(selectInboxSequence("0002"));

		commands.add(new Command("0003 FETCH 1 (RFC822.HEADER)",
				"* 1 FETCH (RFC822.HEADER {32}",
				"Subject: IMAP test message 0",
				"",
				")",
				"0003 OK Fetch completed"));

		runSimpleTest(commands);
	}

	@Test
	public void fetchRfc822Size() throws IOException {
		List<Command> commands = new LinkedList<Command>();
		commands.addAll(connectSequence());
		commands.addAll(loginSequence("0001"));
		commands.addAll(selectInboxSequence("0002"));

		commands.add(new Command("0003 FETCH 1 (RFC822.SIZE)",
				"* 1 FETCH (RFC822.SIZE 32)",
				"0003 OK Fetch completed"));

		runSimpleTest(commands);
	}
}

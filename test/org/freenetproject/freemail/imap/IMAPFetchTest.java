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

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.freenetproject.freemail.AccountManager;

import fakes.ConfigurableAccountManager;
import fakes.FakeSocket;

import org.junit.Test;

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
		expectedResponse.add("0003 BAD Bad number: BAD. Please report this error!");

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
		expectedResponse.add("0003 BAD Bad number: BAD. Please report this error!");

		runSimpleTest(commands, expectedResponse);
	}

	/**
	 * Tests that the IMAP server returns an internaldate with the correct format. The actual value
	 * of the returned date isn't checked.
	 * @throws IOException on I/O error, should never happen
	 * @throws ParseException on test failure
	 */
	//TODO: Convert to proper parameterized test so we can run this with more locale variations
	//(see http://junit.sourceforge.net/javadoc/org/junit/runners/Parameterized.html)
	@Test
	public void internaldateFormatWithFrenchLocale() throws IOException, ParseException {
		Locale orig = Locale.getDefault();
		try {
			Locale.setDefault(Locale.FRENCH);

			//Setup
			FakeSocket sock = new FakeSocket();
			AccountManager accManager = new ConfigurableAccountManager(accountManagerDir, false, accountDirs);

			new Thread(new IMAPHandler(accManager, sock)).start();

			PrintWriter toHandler = new PrintWriter(sock.getOutputStreamOtherSide());
			BufferedReader fromHandler = new BufferedReader(new InputStreamReader(sock.getInputStreamOtherSide()));

			//Send commands
			send(toHandler, "0001 LOGIN " + IMAP_USERNAME + " test\r\n");
			send(toHandler, "0002 SELECT INBOX\r\n");
			send(toHandler, "0003 FETCH 1 (INTERNALDATE)\r\n");

			//Read all the initial responses
			List<String> expectedResponse = new LinkedList<String>();
			expectedResponse.addAll(INITIAL_RESPONSES);
			int lineNum = 0;
			for(String response : expectedResponse) {
				String line = fromHandler.readLine();
				assertEquals("Failed at line " + lineNum++, response, line);
			}

			//Read and parse the INTERNALDATE line which should be of the form:
			//* 1 FETCH (INTERNALDATE "dd MMM yyyy HH:mm:ss Z")
			String line = fromHandler.readLine();
			String[] parts = line.split("\"");
			assertEquals(3, parts.length);
			String date = parts[1];
			SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z", Locale.ROOT);
			sdf.parse(date);

			//Read final line of expected output
			line = fromHandler.readLine();
			assertEquals("0003 OK Fetch completed", line);

			assertFalse("IMAP socket has more data", fromHandler.ready());
			fromHandler.close();
			toHandler.close();
		} finally {
			Locale.setDefault(orig);
		}
	}
}

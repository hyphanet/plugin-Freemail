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

package freemail.imap;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class IMAPFetchTest extends IMAPTestBase {
	private static final List<String> INITIAL_RESPONSES;
	static {
		List<String> backing = new LinkedList<String>();
		backing.add("* OK [CAPABILITY IMAP4rev1 CHILDREN NAMESPACE] Freemail ready - hit me with your rhythm stick.");
		backing.add("0001 OK Logged in");
		backing.add("* FLAGS (\\Seen \\Answered \\Flagged \\Deleted \\Draft \\Recent)");
		backing.add("* OK [PERMANENTFLAGS (\\* \\Seen \\Answered \\Flagged \\Deleted \\Draft \\Recent)] Limited");
		backing.add("* 10 EXISTS");
		backing.add("* 10 RECENT");
		backing.add("* OK [UIDVALIDITY 1] Ok");
		backing.add("0002 OK [READ-WRITE] Done");
		INITIAL_RESPONSES = Collections.unmodifiableList(backing);
	}

	public void testFetchBodyPeek() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + USERNAME + " test");
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

	public void testUidFetchBodyPeek() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + USERNAME + " test");
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

	public void testFetchBodyStartRange() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 FETCH 1 (BODY.PEEK[]<0.15>)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 1 FETCH (BODY[]<0> {15}");
		expectedResponse.add("Subject: IMAP t)");
		expectedResponse.add("0003 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	public void testFetchBodyMiddleRange() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 FETCH 1 (BODY.PEEK[]<1.15>)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		expectedResponse.add("* 1 FETCH (BODY[]<1> {15}");
		expectedResponse.add("ubject: IMAP te)");
		expectedResponse.add("0003 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	public void testFetchBodyEndRange() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + USERNAME + " test");
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
}

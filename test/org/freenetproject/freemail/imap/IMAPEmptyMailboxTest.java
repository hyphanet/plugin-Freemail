/*
 * IMAPEmptyMailboxTest.java
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

public class IMAPEmptyMailboxTest extends IMAPTestBase {
	@Test
	public void fetchFromEmptyFolder() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 FETCH 1:* (UID)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("* OK [CAPABILITY IMAP4rev1 CHILDREN NAMESPACE] Freemail ready - hit me with your rhythm stick.");
		expectedResponse.add("0001 OK Logged in");
		expectedResponse.add("* FLAGS (\\Seen \\Answered \\Flagged \\Deleted \\Draft \\Recent)");
		expectedResponse.add("* OK [PERMANENTFLAGS (\\Seen \\Answered \\Flagged \\Deleted \\Draft \\Recent)] Limited");
		expectedResponse.add("* 0 EXISTS");
		expectedResponse.add("* 0 RECENT");
		expectedResponse.add("* OK [UIDVALIDITY 1] Ok");
		expectedResponse.add("0002 OK [READ-WRITE] Done");
		expectedResponse.add("0003 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidFetchFromEmptyFolder() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID FETCH 1:* (UID)");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("* OK [CAPABILITY IMAP4rev1 CHILDREN NAMESPACE] Freemail ready - hit me with your rhythm stick.");
		expectedResponse.add("0001 OK Logged in");
		expectedResponse.add("* FLAGS (\\Seen \\Answered \\Flagged \\Deleted \\Draft \\Recent)");
		expectedResponse.add("* OK [PERMANENTFLAGS (\\Seen \\Answered \\Flagged \\Deleted \\Draft \\Recent)] Limited");
		expectedResponse.add("* 0 EXISTS");
		expectedResponse.add("* 0 RECENT");
		expectedResponse.add("* OK [UIDVALIDITY 1] Ok");
		expectedResponse.add("0002 OK [READ-WRITE] Done");
		expectedResponse.add("0003 OK Fetch completed");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void uidStoreInEmptyFolder() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 SELECT INBOX");
		commands.add("0003 UID STORE 1 FLAGS \\Seen");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("* OK [CAPABILITY IMAP4rev1 CHILDREN NAMESPACE] Freemail ready - hit me with your rhythm stick.");
		expectedResponse.add("0001 OK Logged in");
		expectedResponse.add("* FLAGS (\\Seen \\Answered \\Flagged \\Deleted \\Draft \\Recent)");
		expectedResponse.add("* OK [PERMANENTFLAGS (\\Seen \\Answered \\Flagged \\Deleted \\Draft \\Recent)] Limited");
		expectedResponse.add("* 0 EXISTS");
		expectedResponse.add("* 0 RECENT");
		expectedResponse.add("* OK [UIDVALIDITY 1] Ok");
		expectedResponse.add("0002 OK [READ-WRITE] Done");
		expectedResponse.add("0003 NO No such message");

		runSimpleTest(commands, expectedResponse);
	}
}

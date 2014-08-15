/*
 * BadIMAPHandlerTest.java
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

public class BadIMAPHandlerTest extends IMAPTestWithMessages {
	@Test
	public void literalWithoutEndingLinebreak() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");

		/*
		 * Now send the literal that doesn't end with \r\n. Note that the 'A' belongs to the
		 * literal, not to the tag of the logout command. If the logout command is read as part of
		 * the literal (which is a bug) the last command will be read instead and the test will fail
		 * due to the unexpected output ("0004 NO Sorry - not implemented").
		 */
		commands.add("0002 APPEND INBOX {1}");
		commands.add("A0003 LOGOUT");
		commands.add("0004 ShouldNotRun");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("* OK [CAPABILITY IMAP4rev1 CHILDREN NAMESPACE] Freemail ready - hit me with your rhythm stick.");
		expectedResponse.add("0001 OK Logged in");
		expectedResponse.add("+ OK");
		expectedResponse.add("0002 OK APPEND completed");
		expectedResponse.add("* BYE");
		expectedResponse.add("0003 OK Bye");

		runSimpleTest(commands, expectedResponse);
	}
}

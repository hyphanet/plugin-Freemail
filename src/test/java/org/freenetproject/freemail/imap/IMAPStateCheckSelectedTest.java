/*
 * IMAPStateCheckSelectedTest.java
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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class IMAPStateCheckSelectedTest extends IMAPTestWithMessages {
	@Parameters
	public static List<String[]> data() {
		String[][] data = new String[][] {
				{"CHECK"},
				{"CLOSE"},
				{"COPY"},
				{"EXPUNGE"},
				{"FETCH"},
				{"STORE arg1 arg2"},
				{"UID fetch"},
				{"UID search"},
				{"UID copy"},
				{"UID store arg1 arg2"},
			};
		return Arrays.asList(data);
	}

	private final String command;

	public IMAPStateCheckSelectedTest(String command) {
		this.command = command;
	}

	@Before
	@Override
	public void before() {
		super.before();
	}

	@After
	@Override
	public void after() {
		super.after();
	}

	@Test
	public void failsWithoutSelect() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 " + command);

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("* OK [CAPABILITY IMAP4rev1 CHILDREN NAMESPACE] Freemail ready - hit me with your rhythm stick.");
		expectedResponse.add("0001 OK Logged in");
		expectedResponse.add("0002 NO No mailbox selected");

		runSimpleTest(commands, expectedResponse);
	}
}

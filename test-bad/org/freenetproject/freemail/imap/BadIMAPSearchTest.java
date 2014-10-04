/*
 * BadIMAPSearchTest.java
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

import utils.TextProtocolTester.Command;

public class BadIMAPSearchTest extends IMAPTestWithMessages {
	@Test
	public void searchWithExtraParansAndIllegalWhitespace() throws IOException {
		List<Command> commands = new LinkedList<Command>();
		commands.addAll(connectSequence());
		commands.addAll(loginSequence("0001"));
		commands.addAll(selectInboxSequence("0002"));

		commands.add(new Command("0003 SEARCH ( ALL ALL )",
				"0003 NO Criteria ( hasn't been implemented"));

		runSimpleTest(commands);
	}
}

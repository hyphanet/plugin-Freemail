/*
 * IMAPMessageTest.java
 * This file is part of Freemail, copyright (C) 2011 Martin Nyhus
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

import org.junit.Test;

import org.freenetproject.freemail.imap.IMAPBadMessageException;
import org.freenetproject.freemail.imap.IMAPMessage;

public class IMAPMessageTest {
	@Test
	public void split() {
		String input = "a 'b c' d";
		String [] correct = {"a", "'b c'", "d"};

		String [] actual = IMAPMessage.doSplit(input, new char[] {'\''}, new char[] {'\''});
		for(int i = 0; i < actual.length; i++) {
			assertEquals(correct[i], actual[i]);
		}
	}

	@Test
	public void splitWithoutClose() {
		String input = "a 'b c";
		String [] correct = {"a", "'b c"};

		String [] actual = IMAPMessage.doSplit(input, new char[] {'\''}, new char[] {'\''});
		for(int i = 0; i < actual.length; i++) {
			assertEquals(correct[i], actual[i]);
		}
	}

	@Test
	public void parseTag() throws IMAPBadMessageException {
		IMAPMessage msg = new IMAPMessage("0003 APPEND INBOX (\\Seen custom)");
		assertEquals("0003", msg.tag);
	}

	@Test
	public void parseCommand() throws IMAPBadMessageException {
		IMAPMessage msg = new IMAPMessage("0003 APPEND INBOX (\\Seen custom)");
		assertEquals("APPEND", msg.type.toUpperCase());
	}

	@Test
	public void parseQuotedString() throws IMAPBadMessageException {
		IMAPMessage msg = new IMAPMessage("0003 APPEND \"IN BOX\"");
		assertEquals("\"IN BOX\"", msg.args[0]);
	}

	@Test
	public void parseParenthesesList() throws IMAPBadMessageException {
		IMAPMessage msg = new IMAPMessage("0003 APPEND INBOX (\\Seen custom)");
		assertEquals(3, msg.args.length);
		assertEquals("(\\Seen", msg.args[1]);
		assertEquals("custom)", msg.args[2]);
	}
}

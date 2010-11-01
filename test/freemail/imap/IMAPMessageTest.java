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

package freemail.imap;

import junit.framework.TestCase;

public class IMAPMessageTest extends TestCase {
	public void testSplit() {
		String input = "a 'b c' d";
		String [] correct = {"a", "'b c'", "d"};

		String [] actual = IMAPMessage.doSplit(input, new char[] {'\''}, new char[] {'\''});
		for(int i = 0; i < actual.length; i++) {
			assertEquals(correct[i], actual[i]);
		}
	}

	public void testSplitWithoutClose() {
		String input = "a 'b c";
		String [] correct = {"a", "'b c"};

		String [] actual = IMAPMessage.doSplit(input, new char[] {'\''}, new char[] {'\''});
		for(int i = 0; i < actual.length; i++) {
			assertEquals(correct[i], actual[i]);
		}
	}
}

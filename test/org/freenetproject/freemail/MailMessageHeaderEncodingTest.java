/*
 * MailMessageHeaderEncodingTestTest.java
 * This file is part of Freemail, copyright (C) 2011, 2012
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

package org.freenetproject.freemail;

import org.freenetproject.freemail.MailMessage;

import junit.framework.TestCase;

public class MailMessageHeaderEncodingTest extends TestCase {
	public void testEncodeAsciiHeader() {
		assertEquals("testHeader", MailMessage.encodeHeader("testHeader"));
	}

	public void testEncodeAsciiHeaderWithSpace() {
		assertEquals("test=?UTF-8?Q?=20?=Header", MailMessage.encodeHeader("test Header"));
	}

	public void testEncodeHeaderWithSingleUTF8Character() {
		assertEquals("test=?UTF-8?Q?=C3=A6?=Header", MailMessage.encodeHeader("testæHeader"));
	}

	public void testEncodeHeaderWithMultipleUTF8Character() {
		assertEquals("=?UTF-8?Q?=C3=A6?==?UTF-8?Q?=E2=88=80?=", MailMessage.encodeHeader("æ∀"));
	}
}

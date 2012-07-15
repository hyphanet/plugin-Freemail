/*
 * MailMessageTest.java
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

package org.freenetproject.freemail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import org.freenetproject.freemail.MailMessage;

import utils.Utils;

import junit.framework.TestCase;

public class MailMessageTest extends TestCase {
	private static final String MESSAGE_DIR = "msg_dir";

	private File msgDir = null;

	@Override
	public void setUp() {
		// Create a directory for messages so it is easier to list files, clean up etc.
		msgDir = new File(MESSAGE_DIR);
		if(msgDir.exists()) {
			System.out.println("WARNING: Message directory exists, deleting");
			Utils.delete(msgDir);
		}

		if(!msgDir.mkdir()) {
			System.out.println("WARNING: Could not create message directory, tests will probably fail");
		}
	}

	@Override
	public void tearDown() {
		Utils.delete(msgDir);
	}

	/*
	 * Test for the bug that was fixed in add3f39743a303a748813666f1e1de6a25ca29aa. MailMessage
	 * would lose track of the file when storing a different set of flags, so the second attempt
	 * would fail silently.
	 */
	public void testStoreFlagsTwice() throws IOException {
		File messageFile = new File(msgDir, "0");
		messageFile.createNewFile();

		//Create new message and clear flags in case any were set
		MailMessage msg = new MailMessage(messageFile, 0);
		msg.flags.clear();

		msg.flags.set("\\Seen", true);
		msg.storeFlags();
		assertEquals(new File(msgDir, "0,S"), msgDir.listFiles()[0]);

		msg.flags.set("\\Deleted", true);
		msg.storeFlags();
		assertEquals(new File(msgDir, "0,SX"), msgDir.listFiles()[0]);
	}

	public void testSingleLineReferencesHeader() throws IOException {
		File messageFile = new File(msgDir, "0");
		messageFile.createNewFile();

		PrintWriter pw = new PrintWriter(messageFile);
		pw.print("To: local@domain\r\n");
		pw.print("References: <abc@domain>\r\n");
		pw.close();

		//Create new message and clear flags in case any were set
		MailMessage msg = new MailMessage(messageFile, 0);
		msg.readHeaders();
		assertEquals(1, msg.getHeadersByName("References").size());
		assertEquals("<abc@domain>", msg.getFirstHeader("References"));
	}

	public void testMultiLineReferencesHeader() throws IOException {
		File messageFile = new File(msgDir, "0");
		messageFile.createNewFile();

		PrintWriter pw = new PrintWriter(messageFile);
		pw.print("To: local@domain\r\n");
		pw.print("References: <1234@abc.com>\r\n");
		pw.print(" <5678@def.com>\r\n");
		pw.print(" <9123@ghi.com> <4567@jkl.com>\r\n");
		pw.close();

		//Create new message and clear flags in case any were set
		MailMessage msg = new MailMessage(messageFile, 0);
		msg.readHeaders();
		assertEquals(1, msg.getHeadersByName("References").size());

		String expected =
				"<1234@abc.com> "
				+ "<5678@def.com> "
				+ "<9123@ghi.com> "
				+ "<4567@jkl.com>";
		assertEquals(expected, msg.getFirstHeader("References"));
	}

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

	public void testEncodeDecodeMultipleStrings() throws UnsupportedEncodingException {
		List<String> input = new LinkedList<String>();
		input.add("Test message");
		input.add("Test message (æøå)");
		input.add("testæHeader∀");
		input.add("æ∀");

		for(String s : input) {
			String encoded = MailMessage.encodeHeader(s);
			String decoded = MailMessage.decodeHeader(encoded);
			assertEquals(s, decoded);
		}
	}

	public void testDecodeQpAndAsciiMessageBody() throws IOException {
		File messageFile = new File(msgDir, "0");
		messageFile.createNewFile();

		PrintWriter pw = new PrintWriter(messageFile);
		pw.print("Content-Transfer-Encoding: quoted-printable\r\n");
		pw.print("Content-Type: text/plain; charset=us-ascii\r\n");
		pw.print("\r\n");
		pw.print("Test message, line 1\r\n");
		pw.print("Test message, line 2\r\n");
		pw.close();

		MailMessage msg = new MailMessage(messageFile, 0);
		BufferedReader reader = msg.getBodyReader();

		assertEquals("Test message, line 1", reader.readLine());
		assertEquals("Test message, line 2", reader.readLine());
		assertEquals(null, reader.readLine());
	}

	public void testDecodeQpAndUtf8MessageBody() throws IOException {
		File messageFile = new File(msgDir, "0");
		messageFile.createNewFile();

		PrintWriter pw = new PrintWriter(messageFile);
		pw.print("Content-Transfer-Encoding: quoted-printable\r\n");
		pw.print("Content-Type: text/plain; charset=utf-8\r\n");
		pw.print("\r\n");
		pw.print("Test message (=C3=A6), line 1\r\n");
		pw.print("Test message (=C3=A6), line 2\r\n");
		pw.close();

		MailMessage msg = new MailMessage(messageFile, 0);
		BufferedReader reader = msg.getBodyReader();

		assertEquals("Test message (æ), line 1", reader.readLine());
		assertEquals("Test message (æ), line 2", reader.readLine());
		assertEquals(null, reader.readLine());
	}
}

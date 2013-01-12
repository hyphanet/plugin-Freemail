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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.freenetproject.freemail.MailMessage;

import utils.Utils;

public class MailMessageTest {
	private static final String MESSAGE_DIR = "msg_dir";

	private File msgDir = null;

	@Before
	public void before() {
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

	@After
	public void after() {
		Utils.delete(msgDir);
	}

	/*
	 * Test for the bug that was fixed in add3f39743a303a748813666f1e1de6a25ca29aa. MailMessage
	 * would lose track of the file when storing a different set of flags, so the second attempt
	 * would fail silently.
	 */
	@Test
	public void storeFlagsTwice() throws IOException {
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

	@Test
	public void singleLineReferencesHeader() throws IOException {
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

	@Test
	public void multiLineReferencesHeader() throws IOException {
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

	@Test
	public void encodeDecodeMultipleStrings() throws UnsupportedEncodingException {
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
}

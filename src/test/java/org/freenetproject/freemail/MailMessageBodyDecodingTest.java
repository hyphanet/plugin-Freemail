/*
 * MailMessageBodyDecodingTest.java
 * This file is part of Freemail, copyright (C) 2011,2012
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.freenetproject.freemail.MailMessage;

import utils.Utils;

public class MailMessageBodyDecodingTest {
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

	@Test
	public void decodeQpAndAsciiMessageBody() throws IOException {
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

	@Test
	public void decodeQpAndUtf8MessageBody() throws IOException {
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

	@Test
	public void decodeQpAndIso8859_1MessageBody() throws IOException {
		File messageFile = new File(msgDir, "0");
		messageFile.createNewFile();

		PrintWriter pw = new PrintWriter(messageFile);
		pw.print("Content-Transfer-Encoding: quoted-printable\r\n");
		pw.print("Content-Type: text/plain; charset=iso-8859-1\r\n");
		pw.print("\r\n");
		pw.print("Test message (=E6=F8=E5), line 1\r\n");
		pw.close();

		MailMessage msg = new MailMessage(messageFile, 0);
		BufferedReader reader = msg.getBodyReader();

		assertEquals("Test message (æøå), line 1", reader.readLine());
		assertEquals(null, reader.readLine());
	}

	@Test
	public void decodeQpWithSoftLineBreak() throws IOException {
		File messageFile = new File(msgDir, "0");
		messageFile.createNewFile();

		PrintWriter pw = new PrintWriter(messageFile);
		pw.print("Content-Transfer-Encoding: quoted-printable\r\n");
		pw.print("Content-Type: text/plain; charset=iso-8859-1\r\n");
		pw.print("\r\n");
		pw.print("Test message (=E6=F8=E5), line 1 =\r\n");
		pw.print("was split across two lines\r\n");
		pw.close();

		MailMessage msg = new MailMessage(messageFile, 0);
		BufferedReader reader = msg.getBodyReader();

		assertEquals("Test message (æøå), line 1 was split across two lines", reader.readLine());
		assertEquals(null, reader.readLine());
	}

	@Test
	public void decodeBase64AndAsciiMessageBody() throws IOException {
		File messageFile = new File(msgDir, "0");
		messageFile.createNewFile();

		PrintWriter pw = new PrintWriter(messageFile);
		pw.print("Content-Transfer-Encoding: base64\r\n");
		pw.print("Content-Type: text/plain; charset=us-ascii\r\n");
		pw.print("\r\n");
		pw.print("VGVzdCBtZXNzYWdlLCBsaW5lIDENClRlc3QgbWVzc2FnZSwgbGluZSAyDQo=");
		pw.close();

		MailMessage msg = new MailMessage(messageFile, 0);
		BufferedReader reader = msg.getBodyReader();

		assertEquals("Test message, line 1", reader.readLine());
		assertEquals("Test message, line 2", reader.readLine());
		assertEquals(null, reader.readLine());
	}

	@Test
	public void decodeBase64AndUtf8MessageBody() throws IOException {
		File messageFile = new File(msgDir, "0");
		messageFile.createNewFile();

		PrintWriter pw = new PrintWriter(messageFile);
		pw.print("Content-Transfer-Encoding: base64\r\n");
		pw.print("Content-Type: text/plain; charset=utf-8\r\n");
		pw.print("\r\n");
		pw.print("VGVzdCBtZXNzYWdlICjDpiksIGxpbmUgMQ0KVGVzdCBt\r\n");
		pw.print("ZXNzYWdlICjDpiksIGxpbmUgMg0K\r\n");
		pw.close();

		MailMessage msg = new MailMessage(messageFile, 0);
		BufferedReader reader = msg.getBodyReader();

		assertEquals("Test message (æ), line 1", reader.readLine());
		assertEquals("Test message (æ), line 2", reader.readLine());
		assertEquals(null, reader.readLine());
	}

	@Test
	public void decodeBase64WithLineBuffering() throws IOException {
		File messageFile = new File(msgDir, "0");
		messageFile.createNewFile();

		PrintWriter pw = new PrintWriter(messageFile);
		pw.print("Content-Transfer-Encoding: base64\r\n");
		pw.print("Content-Type: text/plain; charset=us-ascii\r\n");
		pw.print("\r\n");
		pw.print("VGVzdCBtZXNzYWdlLCBsaW5lIDENClRlc3QgbWVzc2FnZSwgbGlu"
				+ "ZSAyDQpUZXN0IG1lc3NhZ2UsIGxpbmUgMw0K\r\n");
		pw.close();

		MailMessage msg = new MailMessage(messageFile, 0);
		BufferedReader reader = msg.getBodyReader();

		assertEquals("Test message, line 1", reader.readLine());
		assertEquals("Test message, line 2", reader.readLine());
		assertEquals("Test message, line 3", reader.readLine());
		assertEquals(null, reader.readLine());
	}

	/**
	 * Tests that the implementation can handle body lines that don't contain
	 * hard line breaks when decoded.
	 */
	@Test
	public void decodeBase64WithShortBodyLines() throws IOException {
		File messageFile = new File(msgDir, "0");
		messageFile.createNewFile();

		PrintWriter pw = new PrintWriter(messageFile);
		pw.print("Content-Transfer-Encoding: base64\r\n");
		pw.print("Content-Type: text/plain; charset=us-ascii\r\n");
		pw.print("\r\n");
		pw.print("VGVzdCBtZXNz\r\n");
		pw.print("YWdlLCBsaW5l\r\n");
		pw.print("IDENClRlc3Qg\r\n");
		pw.print("bWVzc2FnZSwg\r\n");
		pw.print("bGluZSAyDQpU\r\n");
		pw.print("ZXN0IG1lc3Nh\r\n");
		pw.print("Z2UsIGxpbmUg\r\n");
		pw.print("Mw0K\r\n");
		pw.close();

		MailMessage msg = new MailMessage(messageFile, 0);
		BufferedReader reader = msg.getBodyReader();

		assertEquals("Test message, line 1", reader.readLine());
		assertEquals("Test message, line 2", reader.readLine());
		assertEquals("Test message, line 3", reader.readLine());
		assertEquals(null, reader.readLine());
	}

	@Test
	public void decodeBase64WithoutTrailingHardLinebreak() throws IOException {
		File messageFile = new File(msgDir, "0");
		messageFile.createNewFile();

		PrintWriter pw = new PrintWriter(messageFile);
		pw.print("Content-Transfer-Encoding: base64\r\n");
		pw.print("Content-Type: text/plain; charset=us-ascii\r\n");
		pw.print("\r\n");
		pw.print("VGVzdCBtZXNzYWdlLCBsaW5lIDENClRlc3QgbWVzc2Fn\r\n");
		pw.print("ZSwgbGluZSAyDQpUZXN0IG1lc3NhZ2UsIGxpbmUgMw==\r\n");
		pw.close();

		MailMessage msg = new MailMessage(messageFile, 0);
		BufferedReader reader = msg.getBodyReader();

		assertEquals("Test message, line 1", reader.readLine());
		assertEquals("Test message, line 2", reader.readLine());
		assertEquals("Test message, line 3", reader.readLine());
		assertEquals(null, reader.readLine());
	}

	/**
	 * 7bit encoding is essentially a no-op and should return the exact content
	 */
	@Test
	public void decode7bitBody() throws IOException {
		File messageFile = new File(msgDir, "0");
		messageFile.createNewFile();

		PrintWriter pw = new PrintWriter(messageFile);
		pw.print("Content-Transfer-Encoding: 7bit\r\n");
		pw.print("Content-Type: text/plain; charset=us-ascii\r\n");
		pw.print("\r\n");
		pw.print("Test message, line 1\r\n");
		pw.print("Test message, line 2\r\n");
		pw.print("Test message, line 3");
		pw.close();

		MailMessage msg = new MailMessage(messageFile, 0);
		BufferedReader reader = msg.getBodyReader();

		assertEquals("Test message, line 1", reader.readLine());
		assertEquals("Test message, line 2", reader.readLine());
		assertEquals("Test message, line 3", reader.readLine());
		assertEquals(null, reader.readLine());
	}

	/**
	 * Transfer encodings that are unsupported should cause the Reader to
	 * return the raw content.
	 */
	@Test
	public void decodeUnsupportedTransferEncoding() throws IOException {
		File messageFile = new File(msgDir, "0");
		messageFile.createNewFile();

		PrintWriter pw = new PrintWriter(messageFile);
		pw.print("Content-Transfer-Encoding: unsupported\r\n");
		pw.print("Content-Type: text/plain; charset=us-ascii\r\n");
		pw.print("\r\n");
		pw.print("Test message, line 1\r\n");
		pw.print("Test message, line 2\r\n");
		pw.print("Test message, line 3");
		pw.close();

		MailMessage msg = new MailMessage(messageFile, 0);
		BufferedReader reader = msg.getBodyReader();

		assertEquals("Test message, line 1", reader.readLine());
		assertEquals("Test message, line 2", reader.readLine());
		assertEquals("Test message, line 3", reader.readLine());
		assertEquals(null, reader.readLine());
	}

	/**
	 * Charsets that are unsupported should cause the Reader to return the raw
	 * content.
	 */
	@Test
	public void decodeUnsupportedCharset() throws IOException {
		File messageFile = new File(msgDir, "0");
		messageFile.createNewFile();

		PrintWriter pw = new PrintWriter(messageFile);
		pw.print("Content-Transfer-Encoding: quoted-printable\r\n");
		pw.print("Content-Type: text/plain; charset=unsupported\r\n");
		pw.print("\r\n");
		pw.print("Test message, line 1\r\n");
		pw.print("Test message, line 2\r\n");
		pw.print("Test message, line 3");
		pw.close();

		MailMessage msg = new MailMessage(messageFile, 0);
		BufferedReader reader = msg.getBodyReader();

		assertEquals("Test message, line 1", reader.readLine());
		assertEquals("Test message, line 2", reader.readLine());
		assertEquals("Test message, line 3", reader.readLine());
		assertEquals(null, reader.readLine());
	}

	/**
	 * Tests reading of messages without the extra MIME headers
	 */
	@Test
	public void readPlainRFC822Message() throws IOException {
		File messageFile = new File(msgDir, "0");
		messageFile.createNewFile();

		PrintWriter pw = new PrintWriter(messageFile);
		pw.print("\r\n");
		pw.print("Test message, line 1\r\n");
		pw.print("Test message, line 2\r\n");
		pw.print("Test message, line 3");
		pw.close();

		MailMessage msg = new MailMessage(messageFile, 0);
		BufferedReader reader = msg.getBodyReader();

		assertEquals("Test message, line 1", reader.readLine());
		assertEquals("Test message, line 2", reader.readLine());
		assertEquals("Test message, line 3", reader.readLine());
		assertEquals(null, reader.readLine());
	}
}

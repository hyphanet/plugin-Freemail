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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
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

	public void testDecodeQpAndIso8859_1MessageBody() throws IOException {
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

	public void testDecodeQpWithSoftLineBreak() throws IOException {
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

	public void testEncodeAsciiText() throws IOException {
		byte[] input = "Test message 123".getBytes();
		runEncoderTest(input, input);
	}

	public void testEncodeTrailingLineBreak() throws IOException {
		byte[] input = "Test message\r\n".getBytes();
		runEncoderTest(input, input);
	}

	public void testEncodeEOLWhitespace() throws IOException {
		byte[] input = "Test message \r\n".getBytes();
		byte[] expected = "Test message=20\r\n".getBytes();
		runEncoderTest(expected, input);
	}

	public void testEncodeUTF8() throws IOException {
		byte[] input = "æ∀\r\n".getBytes();
		byte[] expected = "=C3=A6=E2=88=80\r\n".getBytes();
		runEncoderTest(expected, input);
	}

	public void testEncodeSingleCarriageReturn() throws IOException {
		byte[] input = "Test\r\r\n".getBytes();
		byte[] expected = "Test=0D\r\n".getBytes();
		runEncoderTest(expected, input);
	}

	public void testEncodeSingleNewline() throws IOException {
		byte[] input = "Test\n\r\n".getBytes();
		byte[] expected = "Test=0A\r\n".getBytes();
		runEncoderTest(expected, input);
	}

	public void testEncodeLongLine() throws IOException {
		byte[] input = (
				"Test of a long line that will require a soft line break because it is more "
				+ "than 78 characters long").getBytes();
		byte[] expected = (
				"Test of a long line that will require a soft line break because it is more =\r\n"
				+ "than 78 characters long").getBytes();
		runEncoderTest(expected, input);
	}

	public void testEncodeEquals() throws IOException {
		byte[] input = "Test=message\r\n".getBytes();
		byte[] expected = "Test=3Dmessage\r\n".getBytes();
		runEncoderTest(expected, input);
	}

	public void testEncodeAsciiDel() throws IOException {
		byte[] input = "Test?message\r\n".getBytes();
		input[4] = 0x7F;

		byte[] expected = "Test=7Fmessage\r\n".getBytes();
		runEncoderTest(expected, input);
	}

	public void testEncodeTab() throws IOException {
		byte[] input = "Test\tmessage\r\n".getBytes();
		byte[] expected = "Test\tmessage\r\n".getBytes();
		runEncoderTest(expected, input);
	}

	public void testEncodeTabAtEOL() throws IOException {
		byte[] input = "Test message\t\r\n".getBytes();
		byte[] expected = "Test message=09\r\n".getBytes();
		runEncoderTest(expected, input);
	}

	public void testEncodeSpaceAndNewline() throws IOException {
		byte[] input = " \n\r\n".getBytes();
		byte[] expected = " =0A\r\n".getBytes();
		runEncoderTest(expected, input);
	}

	public void testBufferExpansionWhileEncoding() throws IOException {
		byte[] input = "          ".getBytes();
		byte[] expected = "         =20".getBytes();
		runEncoderTest(expected, input);
	}

	public void testInsertSoftLineBreakBeforeEncodedChar() throws IOException {
		byte[] input = "Test of a long line that will require a soft line before the encoded char: æ".getBytes();
		byte[] expected =
				"Test of a long line that will require a soft line before the encoded char: =\r\n=C3=A6".getBytes();
		runEncoderTest(expected, input);
	}

	private void runEncoderTest(byte[] expected, byte[] input) throws IOException {
		for(int i = 0; i < expected.length; i++) {
			if(expected[i] >= 0x80) {
				fail("Expected output can't contain 8bit characters");
			}
		}

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		OutputStream encoder = new MailMessage.EncodingOutputStream(output);
		encoder.write(input);
		encoder.close();

		Utils.assertEquals(expected, output.toByteArray());
	}
}

/*
 * MailMessageBodyEncodingTest.java
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.freenetproject.freemail.MailMessage;

import utils.Utils;

import junit.framework.TestCase;

public class MailMessageBodyEncodingTest extends TestCase {
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

	public void testHardLineBreakResetsOutputCharCount() throws IOException {
		byte[] input = ("This test checks for the bug that was fixed in \r\n"
				+ "commit 4d6245a3921c3711e68c419856edd47fb2404e19, where \r\n"
				+ "writing a hard line break wouldn't reset the output \r\n"
				+ "character count, resulting in extra soft line breaks \r\n"
				+ "being inserted\r\n").getBytes();
		byte[] expected = ("This test checks for the bug that was fixed in=20\r\n"
				+ "commit 4d6245a3921c3711e68c419856edd47fb2404e19, where=20\r\n"
				+ "writing a hard line break wouldn't reset the output=20\r\n"
				+ "character count, resulting in extra soft line breaks=20\r\n"
				+ "being inserted\r\n").getBytes();
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

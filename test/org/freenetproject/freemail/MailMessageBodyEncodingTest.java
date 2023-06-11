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

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.Test;

import org.freenetproject.freemail.MailMessage;

public class MailMessageBodyEncodingTest {
	@Test
	public void encodeAsciiText() throws IOException {
		byte[] input = "Test message 123".getBytes("UTF-8");
		runEncoderTest(input, input);
	}

	@Test
	public void encodeTrailingLineBreak() throws IOException {
		byte[] input = "Test message\r\n".getBytes("UTF-8");
		runEncoderTest(input, input);
	}

	@Test
	public void encodeEOLWhitespace() throws IOException {
		byte[] input = "Test message \r\n".getBytes("UTF-8");
		byte[] expected = "Test message=20\r\n".getBytes("UTF-8");
		runEncoderTest(expected, input);
	}

	@Test
	public void encodeUTF8() throws IOException {
		byte[] input = "æ∀\r\n".getBytes("UTF-8");
		byte[] expected = "=C3=A6=E2=88=80\r\n".getBytes("UTF-8");
		runEncoderTest(expected, input);
	}

	@Test
	public void encodeSingleCarriageReturn() throws IOException {
		byte[] input = "Test\r\r\n".getBytes("UTF-8");
		byte[] expected = "Test=0D\r\n".getBytes("UTF-8");
		runEncoderTest(expected, input);
	}

	@Test
	public void encodeSingleNewline() throws IOException {
		byte[] input = "Test\n\r\n".getBytes("UTF-8");
		byte[] expected = "Test=0A\r\n".getBytes("UTF-8");
		runEncoderTest(expected, input);
	}

	@Test
	public void encodeLongLine() throws IOException {
		byte[] input = (
				"Test of a long line that will require a soft line break because it is more "
				+ "than 78 characters long").getBytes("UTF-8");
		byte[] expected = (
				"Test of a long line that will require a soft line break because it is more =\r\n"
				+ "than 78 characters long").getBytes("UTF-8");
		runEncoderTest(expected, input);
	}

	@Test
	public void encodeEquals() throws IOException {
		byte[] input = "Test=message\r\n".getBytes("UTF-8");
		byte[] expected = "Test=3Dmessage\r\n".getBytes("UTF-8");
		runEncoderTest(expected, input);
	}

	@Test
	public void encodeAsciiDel() throws IOException {
		byte[] input = "Test?message\r\n".getBytes("UTF-8");
		input[4] = 0x7F;

		byte[] expected = "Test=7Fmessage\r\n".getBytes("UTF-8");
		runEncoderTest(expected, input);
	}

	@Test
	public void encodeTab() throws IOException {
		byte[] input = "Test\tmessage\r\n".getBytes("UTF-8");
		byte[] expected = "Test\tmessage\r\n".getBytes("UTF-8");
		runEncoderTest(expected, input);
	}

	@Test
	public void encodeTabAtEOL() throws IOException {
		byte[] input = "Test message\t\r\n".getBytes("UTF-8");
		byte[] expected = "Test message=09\r\n".getBytes("UTF-8");
		runEncoderTest(expected, input);
	}

	@Test
	public void encodeSpaceAndNewline() throws IOException {
		byte[] input = " \n\r\n".getBytes("UTF-8");
		byte[] expected = " =0A\r\n".getBytes("UTF-8");
		runEncoderTest(expected, input);
	}

	@Test
	public void bufferExpansionWhileEncoding() throws IOException {
		byte[] input = "          ".getBytes("UTF-8");
		byte[] expected = "         =20".getBytes("UTF-8");
		runEncoderTest(expected, input);
	}

	@Test
	public void insertSoftLineBreakBeforeEncodedChar() throws IOException {
		byte[] input = "Test of a long line that will require a soft line before the encoded char: æ".getBytes("UTF-8");
		byte[] expected = ("Test of a long line that will require a soft line before the encoded char: "
		                 + "=\r\n=C3=A6").getBytes("UTF-8");
		runEncoderTest(expected, input);
	}

	@Test
	public void hardLineBreakResetsOutputCharCount() throws IOException {
		byte[] input = ("This test checks for the bug that was fixed in \r\n"
				+ "commit 4d6245a3921c3711e68c419856edd47fb2404e19, where \r\n"
				+ "writing a hard line break wouldn't reset the output \r\n"
				+ "character count, resulting in extra soft line breaks \r\n"
				+ "being inserted\r\n").getBytes("UTF-8");
		byte[] expected = ("This test checks for the bug that was fixed in=20\r\n"
				+ "commit 4d6245a3921c3711e68c419856edd47fb2404e19, where=20\r\n"
				+ "writing a hard line break wouldn't reset the output=20\r\n"
				+ "character count, resulting in extra soft line breaks=20\r\n"
				+ "being inserted\r\n").getBytes("UTF-8");
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

		assertArrayEquals(expected, output.toByteArray());
	}
}

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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class MailMessageBodyDecodingTest {

	private static final String HEADER_BODY_SEPARATOR = "";
	private static final String LINE_ENDING = "\r\n";

	@Rule
	public TemporaryFolder messageDirectory = new TemporaryFolder();

	@Test
	public void decodeQpAndAsciiMessageBody() throws IOException {
		parseMailFromLinesAndVerifyBody(asList(
				"Content-Transfer-Encoding: quoted-printable",
				"Content-Type: text/plain; charset=us-ascii",
				HEADER_BODY_SEPARATOR,
				"Test message, line 1",
				"Test message, line 2"
		), reader -> {
			assertThat(reader.readLine(), equalTo("Test message, line 1"));
			assertThat(reader.readLine(), equalTo("Test message, line 2"));
			assertThat(reader.readLine(), nullValue());
		});
	}

	@Test
	public void decodeQpAndUtf8MessageBody() throws IOException {
		parseMailFromLinesAndVerifyBody(asList(
				"Content-Transfer-Encoding: quoted-printable",
				"Content-Type: text/plain; charset=utf-8",
				HEADER_BODY_SEPARATOR,
				"Test message (=C3=A6), line 1",
				"Test message (=C3=A6), line 2"
		), reader -> {
			assertThat(reader.readLine(), equalTo("Test message (æ), line 1"));
			assertThat(reader.readLine(), equalTo("Test message (æ), line 2"));
			assertThat(reader.readLine(), nullValue());
		});
	}

	@Test
	public void decodeQpAndIso8859_1MessageBody() throws IOException {
		parseMailFromLinesAndVerifyBody(asList(
				"Content-Transfer-Encoding: quoted-printable",
				"Content-Type: text/plain; charset=iso-8859-1",
				HEADER_BODY_SEPARATOR,
				"Test message (=E6=F8=E5), line 1"
		), reader -> {
			assertThat(reader.readLine(), equalTo("Test message (æøå), line 1"));
			assertThat(reader.readLine(), nullValue());
		});
	}

	@Test
	public void decodeQpWithSoftLineBreak() throws IOException {
		parseMailFromLinesAndVerifyBody(asList(
				"Content-Transfer-Encoding: quoted-printable",
				"Content-Type: text/plain; charset=iso-8859-1",
				HEADER_BODY_SEPARATOR,
				"Test message (=E6=F8=E5), line 1 =",
				"was split across two lines"
		), reader -> {
			assertThat(reader.readLine(), equalTo("Test message (æøå), line 1 was split across two lines"));
			assertThat(reader.readLine(), nullValue());
		});
	}

	@Test
	public void decodeBase64AndAsciiMessageBody() throws IOException {
		parseMailFromLinesAndVerifyBody(asList(
				"Content-Transfer-Encoding: base64",
				"Content-Type: text/plain; charset=us-ascii",
				HEADER_BODY_SEPARATOR,
				"VGVzdCBtZXNzYWdlLCBsaW5lIDENClRlc3QgbWVzc2FnZSwgbGluZSAyDQo="
		), reader -> {
			assertThat(reader.readLine(), equalTo("Test message, line 1"));
			assertThat(reader.readLine(), equalTo("Test message, line 2"));
			assertThat(reader.readLine(), nullValue());
		});
	}

	@Test
	public void decodeBase64AndUtf8MessageBody() throws IOException {
		parseMailFromLinesAndVerifyBody(asList(
				"Content-Transfer-Encoding: base64",
				"Content-Type: text/plain; charset=utf-8",
				HEADER_BODY_SEPARATOR,
				"VGVzdCBtZXNzYWdlICjDpiksIGxpbmUgMQ0KVGVzdCBt",
				"ZXNzYWdlICjDpiksIGxpbmUgMg0K"
		), reader -> {
			assertThat(reader.readLine(), equalTo("Test message (æ), line 1"));
			assertThat(reader.readLine(), equalTo("Test message (æ), line 2"));
			assertThat(reader.readLine(), nullValue());
		});
	}

	@Test
	public void decodeBase64WithLineBuffering() throws IOException {
		parseMailFromLinesAndVerifyBody(asList(
				"Content-Transfer-Encoding: base64",
				"Content-Type: text/plain; charset=us-ascii",
				HEADER_BODY_SEPARATOR,
				"VGVzdCBtZXNzYWdlLCBsaW5lIDENClRlc3QgbWVzc2FnZSwgbGluZSAyDQpUZXN0IG1lc3NhZ2UsIGxpbmUgMw0K"
		), reader -> {
			assertThat(reader.readLine(), equalTo("Test message, line 1"));
			assertThat(reader.readLine(), equalTo("Test message, line 2"));
			assertThat(reader.readLine(), equalTo("Test message, line 3"));
			assertThat(reader.readLine(), nullValue());
		});
	}

	/**
	 * Tests that the implementation can handle body lines that don't contain
	 * hard line breaks when decoded.
	 */
	@Test
	public void decodeBase64WithShortBodyLines() throws IOException {
		parseMailFromLinesAndVerifyBody(asList(
				"Content-Transfer-Encoding: base64",
				"Content-Type: text/plain; charset=us-ascii",
				HEADER_BODY_SEPARATOR,
				"VGVzdCBtZXNz",
				"YWdlLCBsaW5l",
				"IDENClRlc3Qg",
				"bWVzc2FnZSwg",
				"bGluZSAyDQpU",
				"ZXN0IG1lc3Nh",
				"Z2UsIGxpbmUg",
				"Mw0K"
		), reader -> {
			assertThat(reader.readLine(), equalTo("Test message, line 1"));
			assertThat(reader.readLine(), equalTo("Test message, line 2"));
			assertThat(reader.readLine(), equalTo("Test message, line 3"));
			assertThat(reader.readLine(), nullValue());
		});
	}

	@Test
	public void decodeBase64WithoutTrailingHardLinebreak() throws IOException {
		parseMailFromLinesWithoutTrailingNewLineAndVerifyBody(asList(
				"Content-Transfer-Encoding: base64",
				"Content-Type: text/plain; charset=us-ascii",
				HEADER_BODY_SEPARATOR,
				"VGVzdCBtZXNzYWdlLCBsaW5lIDENClRlc3QgbWVzc2Fn",
				"ZSwgbGluZSAyDQpUZXN0IG1lc3NhZ2UsIGxpbmUgMw=="
		), reader -> {
			assertThat(reader.readLine(), equalTo("Test message, line 1"));
			assertThat(reader.readLine(), equalTo("Test message, line 2"));
			assertThat(reader.readLine(), equalTo("Test message, line 3"));
			assertThat(reader.readLine(), nullValue());
		});
	}

	/**
	 * 7bit encoding is essentially a no-op and should return the exact content
	 */
	@Test
	public void decode7bitBody() throws IOException {
		parseMailFromLinesAndVerifyBody(asList(
				"Content-Transfer-Encoding: 7bit",
				"Content-Type: text/plain; charset=us-ascii",
				HEADER_BODY_SEPARATOR,
				"Test message, line 1",
				"Test message, line 2",
				"Test message, line 3"
		), reader -> {
			assertThat(reader.readLine(), equalTo("Test message, line 1"));
			assertThat(reader.readLine(), equalTo("Test message, line 2"));
			assertThat(reader.readLine(), equalTo("Test message, line 3"));
			assertThat(reader.readLine(), nullValue());
		});
	}

	/**
	 * Transfer encodings that are unsupported should cause the Reader to
	 * return the raw content.
	 */
	@Test
	public void decodeUnsupportedTransferEncoding() throws IOException {
		parseMailFromLinesAndVerifyBody(asList(
				"Content-Transfer-Encoding: unsupported",
				"Content-Type: text/plain; charset=us-ascii",
				HEADER_BODY_SEPARATOR,
				"Test message, line 1",
				"Test message, line 2",
				"Test message, line 3"
		), reader -> {
			assertThat(reader.readLine(), equalTo("Test message, line 1"));
			assertThat(reader.readLine(), equalTo("Test message, line 2"));
			assertThat(reader.readLine(), equalTo("Test message, line 3"));
			assertThat(reader.readLine(), nullValue());
		});
	}

	/**
	 * Charsets that are unsupported should cause the Reader to return the raw
	 * content.
	 */
	@Test
	public void decodeUnsupportedCharset() throws IOException {
		parseMailFromLinesAndVerifyBody(asList(
				"Content-Transfer-Encoding: quoted-printable",
				"Content-Type: text/plain; charset=unsupported",
				HEADER_BODY_SEPARATOR,
				"Test message, line 1",
				"Test message, line 2",
				"Test message, line 3"
		), reader -> {
			assertThat(reader.readLine(), equalTo("Test message, line 1"));
			assertThat(reader.readLine(), equalTo("Test message, line 2"));
			assertThat(reader.readLine(), equalTo("Test message, line 3"));
			assertThat(reader.readLine(), nullValue());
		});
	}

	/**
	 * Tests reading of messages without the extra MIME headers
	 */
	@Test
	public void readPlainRFC822Message() throws IOException {
		parseMailFromLinesAndVerifyBody(asList(
				HEADER_BODY_SEPARATOR,
				"Test message, line 1",
				"Test message, line 2",
				"Test message, line 3"
		), reader -> {
			assertThat(reader.readLine(), equalTo("Test message, line 1"));
			assertThat(reader.readLine(), equalTo("Test message, line 2"));
			assertThat(reader.readLine(), equalTo("Test message, line 3"));
			assertThat(reader.readLine(), nullValue());
		});
	}

	/* Fix for https://freenet.mantishub.io/view.php?id=7189 */
	@Test
	public void mailWithoutCharsetInContentTypeCanBeParsed() throws IOException {
		parseMailFromLinesAndVerifyBody(asList(
				"Content-Type: text/plain",
				HEADER_BODY_SEPARATOR,
				"Body"
		), reader -> {
			assertThat(reader.readLine(), equalTo("Body"));
			assertThat(reader.readLine(), nullValue());
		});
	}

	@Test
	public void mailWithoutCharsetInContentTypeIsTreatedAsCharsetUtf8() throws IOException {
		parseMailFromLinesAndVerifyBody(asList(
				"Content-Type: text/plain",
				HEADER_BODY_SEPARATOR,
				"äöü"
		), reader -> {
			assertThat(reader.readLine(), equalTo("äöü"));
			assertThat(reader.readLine(), nullValue());
		});
	}

	private interface ThrowingConsumer<T, E extends Exception> {
		void accept(T t) throws E;
	}

	private void parseMailFromLinesAndVerifyBody(List<String> lines, ThrowingConsumer<BufferedReader, IOException> bodyReader) throws IOException {
		try (BufferedReader bufferedReader = createMailFileAndParseIt(lines.stream().map(line -> line + LINE_ENDING).collect(toList()))) {
			bodyReader.accept(bufferedReader);
		}
	}

	private void parseMailFromLinesWithoutTrailingNewLineAndVerifyBody(List<String> lines, ThrowingConsumer<BufferedReader, IOException> bodyReader) throws IOException {
		try (BufferedReader bufferedReader = createMailFileAndParseIt(singletonList(String.join(LINE_ENDING, lines)))) {
			bodyReader.accept(bufferedReader);
		}
	}

	private BufferedReader createMailFileAndParseIt(List<String> lines) throws IOException {
		File messageFile = messageDirectory.newFile();
		try (PrintWriter printWriter = new PrintWriter(messageFile, "UTF-8")) {
			lines.forEach(printWriter::write);
		}
		return new MailMessage(messageFile, 0).getBodyReader();
	}

}

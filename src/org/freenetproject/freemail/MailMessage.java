/*
 * MailMessage.java
 * This file is part of Freemail
 * Copyright (C) 2006,2008 Dave Baker
 * Copyright (C) 2008 Alexander Lehmann
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

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.freenetproject.freemail.imap.IMAPMessageFlags;
import org.freenetproject.freemail.utils.Logger;


public class MailMessage {
	private static final Set<String> dateFormats;
	static {
		Set<String> backing = new HashSet<String>();
		backing.add("EEE, d MMM yyyy HH:mm:ss Z"); //Mon, 17 Oct 2011 10:24:14 +0200
		backing.add("d MMM yyyy HH:mm:ss Z");      //     18 Feb 2012 03:32:22 +0100
		dateFormats = Collections.unmodifiableSet(backing);
	}

	private File file;
	private OutputStream os;
	private PrintStream ps;
	private final List<MailMessageHeader> headers;
	private BufferedReader brdr;
	private int msg_seqnum = 0;
	public IMAPMessageFlags flags;
	private static final Random messageIdRandom = new Random();

	public MailMessage(File f, int msg_seqnum) {
		this.file = f;
		this.headers = new Vector<MailMessageHeader>();
		this.msg_seqnum=msg_seqnum;

		// initialize flags from filename
		String[] parts = f.getName().split(",");
		if(parts.length < 2 && !f.getName().endsWith(",")) {
			// treat it as a new message
			this.flags = new IMAPMessageFlags();
			this.flags.setRecent();
		} else if(parts.length < 2) {
			// just doesn't have any flags set
			this.flags = new IMAPMessageFlags();
		} else {
			this.flags = new IMAPMessageFlags(parts[1]);
		}
		this.brdr = null;
	}

	public void addHeader(String name, String val) {
		this.headers.add(new MailMessageHeader(name, val));
	}

	// get the first header of a given name
	public String getFirstHeader(String name) {
		for(MailMessageHeader header : headers) {
			if(header.name.equalsIgnoreCase(name)) {
				return header.val;
			}
		}

		return null;
	}

	public String getHeaders(String name) {
		StringBuffer buf = new StringBuffer("");

		for(MailMessageHeader header : headers) {
			if(header.name.equalsIgnoreCase(name)) {
				buf.append(header.name);
				buf.append(": ");
				buf.append(header.val);
				buf.append("\r\n");
			}
		}

		return buf.toString();
	}

	@Override
	public int hashCode() {
		if(file == null) {
			return 0;
		}

		return file.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		if(!(obj instanceof MailMessage)) {
			return false;
		}
		MailMessage other = (MailMessage) obj;
		if(file == null) {
			if(other.file != null) {
				return false;
			}
		} else if(!file.equals(other.file)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns a list of the values of all headers with the given name.
	 * @param name the name of the headers to return
	 * @return a list of the values of all headers with the given name
	 */
	public List<String> getHeadersByName(String name) {
		List<String> matches = new LinkedList<String>();

		for(MailMessageHeader header : headers) {
			if(header.name.equalsIgnoreCase(name)) {
				matches.add(header.val);
			}
		}

		return matches;
	}

	public void removeHeader(String name, String val) {
		Iterator<MailMessageHeader> headerIt = headers.iterator();
		while(headerIt.hasNext()) {
			MailMessageHeader header = headerIt.next();
			if(header.name.equalsIgnoreCase(name) && header.val.equalsIgnoreCase(val)) {
				headerIt.remove();
			}
		}
	}

	public String getAllHeadersAsString() {
		StringBuffer buf = new StringBuffer();

		for(MailMessageHeader header : headers) {
			buf.append(header.name);
			buf.append(": ");
			buf.append(header.val);
			buf.append("\r\n");
		}

		return buf.toString();
	}

	public PrintStream writeHeadersAndGetStream() throws FileNotFoundException {
		this.os = new FileOutputStream(this.file);
		this.ps = new PrintStream(this.os);

		for(MailMessageHeader header : headers) {
			this.ps.println(header.name + ": " + header.val);
		}

		this.ps.println("");

		return this.ps;
	}

	/**
	 * Returns a {@code PrintStream} to the backing file. The returned stream
	 * must be closed by the caller before commit() is called.
	 *
	 * @return a {@code PrintStream} to the backing file
	 * @throws FileNotFoundException if the backing file doesn't exist
	 */
	public PrintStream getRawStream() throws FileNotFoundException {
		this.os = new FileOutputStream(this.file);
		this.ps = new PrintStream(this.os);

		return this.ps;
	}

	public void commit() {
		try {
			this.os.close();
			// also potentially move from a temp dir to real inbox
			// to do safer inbox access
		} catch (IOException ioe) {

		}
	}

	public void cancel() {
		try {
			this.os.close();
		} catch (IOException ioe) {
		}
		this.file.delete();
	}

	public void readHeaders() throws IOException {
		BufferedReader bufrdr = new BufferedReader(new InputStreamReader(new FileInputStream(this.file), "UTF-8"));

		this.readHeaders(bufrdr);
		bufrdr.close();
	}

	public void readHeaders(BufferedReader bufrdr) throws IOException {
		if(this.headers.size() > 0) return;

		String line;
		String[] parts = null;
		while((line = bufrdr.readLine()) != null) {
			if(line.length() == 0) {
				if(parts != null)
					this.addHeader(parts[0], parts[1]);
				parts = null;
				break;
			} else if(line.startsWith(" ") || line.startsWith("\t")) {
				// continuation of previous line
				if(parts == null || parts[1] == null)
					continue;
				parts[1] += " "+line.trim();
			} else {
				if(parts != null)
					this.addHeader(parts[0], parts[1]);
				parts = null;
				parts = line.split(": ", 2);

				if(parts.length < 2)
					parts = null;
			}
		}

		if(parts != null) {
			this.addHeader(parts[0], parts[1]);
		}
	}

	public int getUID() {
		String[] parts = this.file.getName().split(",");

		return Integer.parseInt(parts[0]);
	}

	public int getSeqNum() {
		return msg_seqnum;
	}

	public long getSize() throws IOException {
		// this is quite arduous since we have to send the message
		// with \r\n's, and hence it may not be the size it is on disk
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(this.file), "UTF-8"));
		try {
			long counter = 0;
			String line;

			while((line = br.readLine()) != null) {
				counter += line.getBytes("UTF-8").length;
				counter += "\r\n".getBytes("UTF-8").length;
			}

			return counter;
		} finally {
			br.close();
		}
	}

	public void closeStream() {
		try {
			if(this.brdr != null) this.brdr.close();
		} catch (IOException ioe) {

		}
		this.brdr = null;
	}

	public String readLine() throws IOException {
		if(this.brdr == null) {
			this.brdr = new BufferedReader(new InputStreamReader(new FileInputStream(this.file), "UTF-8"));
		}

		return this.brdr.readLine();
	}

	public boolean copyTo(MailMessage msg) {
		this.closeStream();
		String line;
		try {
			PrintStream copyps = msg.getRawStream();
			try {
				while((line = this.readLine()) != null) {
					copyps.println(line);
				}
			} finally {
				copyps.close();
			}
			msg.commit();
		} catch (IOException ioe) {
			msg.cancel();
			return false;
		} finally {
			closeStream();
		}

		msg.flags = this.flags;
		msg.storeFlags();
		return true;
	}

	// programming-by-contract - anything that tries to read the message
	// or suchlike after calling this method is responsible for the
	// torrent of exceptions they'll get thrown at them!
	public void delete() {
		this.file.delete();
	}

	public void storeFlags() {
		String[] parts = this.file.getName().split(",");

		String newname = parts[0] + "," + this.flags.getShortFlagString();
		File newfile = new File(this.file.getParentFile(), newname);

		if(!file.getName().equals(newfile.getName())) {
			if(this.file.renameTo(newfile)) {
				Logger.debug(this, "Message moved from " + file + " to " + newfile);
				this.file = newfile;
			} else {
				Logger.error(this, "Rename failed (from " + file + " to " + newfile + ")");
			}
		}
	}

	@Override
	public String toString() {
		return "MailMessage backed by " + file;
	}

	public Date getDate() {
		String date = getFirstHeader("Date");
		return MailMessage.parseDate(date);
	}

	/**
	 * Parses the given string using the date formats valid in email messages
	 * and returns a {@code Date}, or {@code null} if the date isn't valid.
	 * @param date the date that should be parsed
	 * @return the parsed date
	 */
	public static Date parseDate(String date) {
		if(date == null) {
			return null;
		}

		for(String format : dateFormats) {
			SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ROOT);
			try {
				return sdf.parse(date);
			} catch (ParseException e) {
				//Try next format
			}
		}

		Logger.minor(MailMessage.class, "No format matched for date " + date);
		return null;
	}

	/**
	 * Checks if the header has an encoded word starting at offset. If an encoded word is found it
	 * is returned without the =? and ?= markers
	 * (i.e. &lt;charset&gt;?&lt;encoding&gt;?&lt;encoded-text&gt;)
	 */
	private static String getEncodedWord(String header, int offset) {
		assert (header != null);
		assert (header.length() > offset) : "length=" + header.length() + ", offset=" + offset;
		assert (offset >= 0);

		if(!header.substring(offset, offset + "=?".length()).equals("=?")) {
			//It makes no sense to call the function if this is the case
			Logger.warning(MailMessage.class, "Offset didn't point to =? in isEncodedWord");
			assert false : "Offset didn't point to =? in isEncodedWord";
			return null;
		}

		int charsetEnd = header.indexOf("?", offset + "=?".length());
		if(charsetEnd == -1) {
			return null;
		}

		int encodingEnd = header.indexOf("?", charsetEnd + "?".length());
		if(encodingEnd == -1) {
			return null;
		}

		int wordEnd = header.indexOf("?=", encodingEnd + "?".length());
		if(wordEnd == -1) {
			return null;
		}

		String word = header.substring(offset + 2, wordEnd);
		Logger.debug(MailMessage.class, "Found possible encoded word: " + word);

		//75 - 4 since we removed the =? and ?= (75 is the limit from RFC2047)
		if(word.length() > (71)) {
			Logger.debug(MailMessage.class, "Found possible encoded word but it was too long");
			return null;
		}
		if(word.contains(" ")) {
			Logger.debug(MailMessage.class, "Found possible encoded word but it contained spaces");
			return null;
		}

		return word;
	}

	private static String decodeMIMEEncodedWord(String charsetName, String encoding, String text)
			throws UnsupportedEncodingException {

		Charset charset = Charset.forName(charsetName);

		if(encoding.equalsIgnoreCase("B")) {
			//Base64 encoding
			byte[] bytes = Base64.decode(text.getBytes("UTF-8"));
			return new String(bytes, charset);
		}

		if(encoding.equalsIgnoreCase("Q")) {
			byte[] buffer = new byte[text.length()];
			int bufIndex = 0;
			int offset = 0;
			while(offset < text.length()) {
				char c = text.charAt(offset);
				if(c == '=') {
					byte[] value = Hex.decode(text.substring(offset + 1, offset + 3));
					assert (value.length == 1);
					buffer[bufIndex++] = value[0];
					offset += 3;
				} else {
					assert (c < 128);
					buffer[bufIndex++] = (byte)c;
					offset++;
				}
			}

			return new String(buffer, 0, bufIndex, charset);
		}

		Logger.warning(MailMessage.class, "Freemail doesn't support encoding: " + encoding);
		throw new UnsupportedEncodingException(
				"MIME header encoding " + encoding + " not supported");
	}

	/**
	 * Returns the subject of this message after decoding it, or {@code null} if the subject header
	 * is missing.
	 * @return the subject of this message after decoding it
	 * @throws UnsupportedEncodingException if the encoding used in the subject isn't unsupported
	 */
	public String getSubject() throws UnsupportedEncodingException {
		String rawSubject = getFirstHeader("Subject");
		if(rawSubject == null) {
			return null;
		}

		return decodeHeader(rawSubject);
	}

	public static String decodeHeader(String rawHeader) throws UnsupportedEncodingException {
		if(rawHeader == null) {
			return null;
		}

		StringBuffer subject = new StringBuffer();

		int offset = 0;
		while(offset < rawHeader.length()) {
			//First, copy anything that we know isn't an encoded word
			int index = rawHeader.indexOf("=?", offset);
			if(index == -1) {
				subject.append(rawHeader.substring(offset));
				break;
			}
			subject.append(rawHeader.substring(offset, index));
			offset = index;

			//Check if we have an encoded word
			String word = getEncodedWord(rawHeader, offset);
			if(word == null) {
				subject.append("=?");
				offset += "=?".length();
				continue;
			}
			offset += word.length() + "=??=".length();

			//Decode the encoded word
			String[] parts = word.split("\\?");
			subject.append(decodeMIMEEncodedWord(parts[0], parts[1], parts[2]));
		}

		return subject.toString();
	}

	/**
	 * Generated a message-id from the specified domain. The generated message-id will be of the
	 * form &lt;local part&gt;@&lt;domain&gt;, where the local part is generated using a random
	 * number large enough that collisions are unlikely.
	 * @param domain the domain part of the message-id
	 * @return the generated message-id
	 */
	public static String generateMessageID(String domain) {
		if(domain == null) {
			Logger.error(MailMessage.class, "Domain passed to generateMessageID() was null", new Exception());
		}

		return messageIdRandom.nextLong() + "." + messageIdRandom.nextLong() + "@" + domain;
	}

	public static String encodeHeader(String header) {
		StringBuilder result = new StringBuilder();

		for(char c : header.toCharArray()) {
			//ASCII printable characters except ? and SPACE can be passed
			//though as is. _ can be used to encode SPACE, so encode that as
			//well to avoid ambiguity
			if(0x21 <= c && c <= 0x7e && c != '?' && c != '_') {
				result.append(c);
				continue;
			}

			//Encode the rest
			//FIXME: There has to be a better way than wrapping with arrays everywhere...
			Charset utf8 = Charset.forName("UTF-8");
			ByteBuffer bytes = utf8.encode(CharBuffer.wrap(new char[] {c}));
			result.append("=?UTF-8?Q?");
			while(bytes.hasRemaining()) {
				byte b = bytes.get();
				result.append("=");
				String encodedString = new String(Hex.encode(new byte[] {b}), utf8).toUpperCase(Locale.ROOT);
				result.append(encodedString);
			}
			result.append("?=");
		}

		return result.toString();
	}

	public BufferedReader getBodyReader() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));

		//Read past the headers and store them if they haven't been read
		//already
		if(headers.size() > 0) {
			String line = reader.readLine();
			while(line != null && !line.equals("")) {
				line = reader.readLine();
			}
		} else {
			readHeaders(reader);
		}

		try {
			return new MessageBodyReader(reader, this);
		} catch(UnsupportedEncodingException e) {
			Logger.warning(this, "Message transfer encoding isn't supported, will display raw content", e);
			return reader;
		} catch(IllegalCharsetNameException e) {
			Logger.warning(this, "Message charset name contains illegal characters, will display raw content", e);
			return reader;
		} catch(UnsupportedCharsetException e) {
			Logger.warning(this, "Message charset isn't supported, will display raw content", e);
			return reader;
		}
	}

	private static class MailMessageHeader {
		public String name;
		public String val;

		public MailMessageHeader(String n, String v) {
			this.name = n;
			this.val = v;
		}
	}

	private static class MessageBodyReader extends BufferedReader {
		private final Charset charset;
		private final ContentTransferEncoding transferEncoding;

		/**
		 * Used to store data e.g. when the hard line break is in the middle of
		 * an encoded line as can happen in e.g. base64.
		 */
		private String buffer;

		public MessageBodyReader(Reader in, MailMessage msg) throws UnsupportedEncodingException {
			super(in);
			transferEncoding = ContentTransferEncoding.parse(msg.getFirstHeader("Content-Transfer-Encoding"));

			String contentType = msg.getFirstHeader("Content-Type");
			if(contentType == null) {
				contentType = "text/plain; charset=utf-8";
			}
			String[] parts = contentType.split(";");
			if(!parts[0].equalsIgnoreCase("text/plain")) {
				throw new UnsupportedEncodingException("Can't handle content types other than text/plain. Type was "
						+ parts[0]);
			}

			String[] charsetParts;
			if(parts.length > 1) {
				charsetParts = parts[1].trim().split("=", 2);
			} else {
				charsetParts = new String[]{"charset", "utf-8"};
			}
			if(!charsetParts[0].equalsIgnoreCase("charset")) {
				throw new UnsupportedEncodingException("Can't handle text/plain with parameter other than charset. "
						+ "Parameter was " + charsetParts[0]);
			}

			String charsetName = charsetParts[1];
			if(charsetName.startsWith("\"") && charsetName.endsWith("\"")) {
				charsetName = charsetName.substring(1, charsetName.length() - 1);
			}
			charset = Charset.forName(charsetName);
		}

		@Override
		public String readLine() throws IOException {
			switch(transferEncoding) {
			case BASE64:
				return readBase64Line();
			case QUOTED_PRINTABLE:
				return readQpLine();
			case SEVEN_BIT:
				return super.readLine();
			default:
				Logger.error(this, "Missing case in transfer encoding switch: " + transferEncoding);
				assert (false);
				return super.readLine();
			}
		}

		/**
		 * Reads and decodes quoted-printable data until a canonical line has
		 * been decoded.
		 * @return a canonical line of message text
		 * @throws IOException if an I/O error occurs while reading the input
		 */
		private String readQpLine() throws IOException {
			byte[] outputBuf = new byte[0];
			int bufOffset = 0;

			while(true) {
				String line = super.readLine();
				if(line == null) {
					//TODO: What if this should have been a continuation line?
					return null;
				}

				byte[] buf = new byte[bufOffset + line.length()];
				System.arraycopy(outputBuf, 0, buf, 0, bufOffset);
				outputBuf = buf;
				bufOffset = decodeQpLine(line, outputBuf, bufOffset);

				if(bufOffset < 0) {
					return new String(outputBuf, 0, -bufOffset, charset);
				}
			}
		}

		private String readBase64Line() throws IOException {
			String result = "";
			boolean readData = false;

			if(buffer != null) {
				result = buffer;
				buffer = null;
				int linebreakIndex = result.indexOf("\r\n");
				if(linebreakIndex != -1) {
					//Buffer already contains a line
					assert (buffer == null);
					buffer = result.substring(linebreakIndex + "\r\n".length());
					if(buffer.equals("")) {
						buffer = null;
					}
					return result.substring(0, linebreakIndex);
				}
				readData = true;
			}

			//Read more data from the encoded body
			while(true) {
				String encodedLine = super.readLine();
				if(encodedLine == null) {
					return readData ? result : null;
				}
				readData = true;

				byte[] data = Base64.decode(encodedLine);
				String line = new String(data, 0, data.length, charset);

				int linebreakIndex = line.indexOf("\r\n");
				if(linebreakIndex != -1) {
					//Put extra data into buffer and return the rest
					assert (buffer == null);
					buffer = line.substring(linebreakIndex + "\r\n".length());
					if(buffer.equals("")) {
						buffer = null;
					}
					return result + line.substring(0, linebreakIndex);
				}

				result = result + line;
			}
		}

		/**
		 * Decode a single line of quoted-printable data. If the line ends with
		 * a soft line break the new buffer offset is returned, otherwise the
		 * new offset is returned as a negative number.
		 *
		 * @param line the input line in quoted-printable form
		 * @param outputBuf the destination buffer
		 * @param bufOffset the first index that should be written to
		 * @return the new offset
		 */
		private int decodeQpLine(String line, byte[] outputBuf, int bufOffset) {
			int lineOffset = 0;
			while(lineOffset < line.length()) {
				char c = line.charAt(lineOffset);
				if(c == '=') {
					if(lineOffset + 1 == line.length()) {
						//Soft line break, so read another input line
						return bufOffset;
					}

					byte[] value = Hex.decode(line.substring(lineOffset + 1, lineOffset + 3));
					assert (value.length == 1);
					outputBuf[bufOffset++] = value[0];
					lineOffset += 3;
				} else {
					assert (c < 128);
					outputBuf[bufOffset++] = (byte)c;
					lineOffset++;
				}
			}

			return -bufOffset;
		}
	}

	private enum ContentTransferEncoding {
		SEVEN_BIT,
		QUOTED_PRINTABLE,
		BASE64;

		public static ContentTransferEncoding parse(String encoding) throws UnsupportedEncodingException {
			if(encoding == null) {
				return ContentTransferEncoding.SEVEN_BIT;
			}
			if(encoding.equalsIgnoreCase("7bit")) {
				return ContentTransferEncoding.SEVEN_BIT;
			}
			if(encoding.equalsIgnoreCase("quoted-printable")) {
				return ContentTransferEncoding.QUOTED_PRINTABLE;
			}
			if(encoding.equalsIgnoreCase("base64")) {
				return ContentTransferEncoding.BASE64;
			}

			throw new UnsupportedEncodingException();
		}
	}

	public static class EncodingOutputStream extends OutputStream {
		private final OutputStream out;

		private byte[] buffer = new byte[4];
		private int bufOffset = 0;

		private int outputLineLength = 0;

		public EncodingOutputStream(OutputStream destination) {
			this.out = destination;
		}

		@Override
		public void close() throws IOException {
			writeBuffer(false);
		}

		@Override
		public void write(int data) throws IOException {
			byte b = (byte)data;

			//Literal representation. Write the buffer first on the assumption
			//that it contains buffered whitespace.
			if((33 <= b && b <= 60) || (62 <= b && b <= 126)) {
				writeBuffer(true);
				insertSoftLineBreak(false);
				out.write(b);
				outputLineLength++;
				return;
			}

			//Whitespace. Buffer the whitespace since it must be followed by a
			//printable character
			if(b == 9 || b == 32) {
				insertIntoBuffer(b);
				return;
			}

			//Line break. \r or \n alone must be encoded, while \r\n must be
			//inserted directly into the output
			if(b == '\r') {
				insertIntoBuffer(b);
				return;
			}
			if((b == '\n') && (bufOffset > 0) && (buffer[bufOffset - 1] == '\r')) {
				//If the last character in the buffer is \r we must insert a
				//hard line break, if it isn't just encode the \n
				bufOffset--;
				writeBuffer(false);
				out.write('\r');
				out.write('\n');
				outputLineLength = 0;
				return;
			}

			//Next char will always be =
			writeBuffer(true);

			//Encode the rest
			writeEncoded(b);
		}

		private void writeEncoded(byte b) throws IOException {
			if(outputLineLength > (76 - 3)) {
				insertSoftLineBreak(true);
			}

			out.write('=');
			byte[] encoded = Hex.encode(new byte[] {b});
			for(int i = 0; i < 2; i++) {
				//Make sure it is upper case
				if(encoded[i] >= 'a') {
					encoded[i] -= 0x20;
				}

				out.write(encoded[i]);
			}
		}

		/**
		 * Check that the buffer only contains only whitespace characters
		 * @param buf the buffer
		 * @param off the start offset in the buffer
		 * @param len the number of bytes to check
		 * @return {@code true} if the buffer only contains whitespace characters
		 */
		private boolean onlyWhitespace(byte[] buf, int off, int len) {
			for(int i = off; i < (off + len); i++) {
				if(buf[i] != '\t' && buf[i] != '\r' && buf[i] != ' ') {
					assert false : "Buffer contained illegal character " + buf[i];
				}
			}

			return true;
		}

		private boolean insertSoftLineBreak(boolean always) throws IOException {
			if(always || outputLineLength >= 75) {
				//Insert soft line break
				out.write(new byte[] {'=', '\r', '\n'});
				outputLineLength = 0;
				return true;
			}

			return false;
		}

		private void insertIntoBuffer(byte b) {
			if(bufOffset == buffer.length) {
				byte[] temp = new byte[buffer.length * 2];
				System.arraycopy(buffer, 0, temp, 0, bufOffset);
				buffer = temp;
			}

			buffer[bufOffset++] = b;
		}

		private void writeBuffer(boolean nextPrintable) throws IOException {
			assert onlyWhitespace(buffer, 0, bufOffset);
			if(bufOffset == 0) {
				return;
			}

			for(int i = 0; i < bufOffset - 1; i++) {
				byte b = buffer[i];
				insertSoftLineBreak(false);

				if((0x20 <= b && b <= 0x3C)
						|| (0x3E <= b && b <= 0x7F)
						|| (b == '\t')) {
					out.write(b);
					outputLineLength++;
				} else {
					writeEncoded(b);
				}
			}

			//If we need a printable character after the last buffered
			//character, encode it unless it is printable (again except space)
			byte b = buffer[bufOffset - 1];
			if(!nextPrintable && (b <= 0x20 || b == 0x7F)) {
				writeEncoded(b);
			} else {
				out.write(b);
				outputLineLength++;
			}
			bufOffset = 0;
		}
	}
}

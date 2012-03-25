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

package freemail;

import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.Enumeration;

import org.bouncycastle.util.encoders.Base64;

import freemail.imap.IMAPMessageFlags;
import freemail.utils.Logger;

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
	private final Vector<MailMessageHeader> headers;
	private BufferedReader brdr;
	private int msg_seqnum=0;
	public IMAPMessageFlags flags;
	private static final Random messageIdRandom = new Random();

	public MailMessage(File f, int msg_seqnum) {
		this.file = f;
		this.headers = new Vector<MailMessageHeader>();
		this.msg_seqnum=msg_seqnum;

		// initialize flags from filename
		String[] parts = f.getName().split(",");
		if (parts.length < 2 && !f.getName().endsWith(",")) {
			// treat it as a new message
			this.flags = new IMAPMessageFlags();
			this.flags.set("\\Recent", true);
		} else if (parts.length < 2) {
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
		Enumeration<MailMessageHeader> e = this.headers.elements();

		while (e.hasMoreElements()) {
			MailMessageHeader h = e.nextElement();

			if (h.name.equalsIgnoreCase(name)) {
				return h.val;
			}
		}

		return null;
	}

	public String getHeaders(String name) {
		StringBuffer buf = new StringBuffer("");

		Enumeration<MailMessageHeader> e = this.headers.elements();

		while (e.hasMoreElements()) {
			MailMessageHeader h = e.nextElement();

			if (h.name.equalsIgnoreCase(name)) {
				buf.append(h.name);
				buf.append(": ");
				buf.append(h.val);
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

	public String[] getHeadersAsArray(String name) {
		Vector<String> hdrs = new Vector<String>();

		Enumeration<MailMessageHeader> e = this.headers.elements();

		while (e.hasMoreElements()) {
			MailMessageHeader h = e.nextElement();

			if (h.name.equalsIgnoreCase(name)) {
				hdrs.add(h.val);
			}
		}

		String[] retval = new String[hdrs.size()];

		Enumeration<String> headers = hdrs.elements();

		int i = 0;
		while (headers.hasMoreElements()) {
			retval[i] = headers.nextElement();
			i++;
		}

		return retval;
	}

	public void removeHeader(String name, String val) {
		int i;

		for (i = 0; i < this.headers.size(); i++) {
			MailMessageHeader h = this.headers.elementAt(i);

			if (h.name.equalsIgnoreCase(name) && h.val.equalsIgnoreCase(val)) {
				this.headers.remove(i);
				i--;
			}
		}
	}

	public String getAllHeadersAsString() {
		Enumeration<MailMessageHeader> e = this.headers.elements();
		StringBuffer buf = new StringBuffer();

		while (e.hasMoreElements()) {
			MailMessageHeader h = e.nextElement();

			buf.append(h.name);
			buf.append(": ");
			buf.append(h.val);
			buf.append("\r\n");
		}

		return buf.toString();
	}

	public PrintStream writeHeadersAndGetStream() throws FileNotFoundException {
		this.os = new FileOutputStream(this.file);
		this.ps = new PrintStream(this.os);

		Enumeration<MailMessageHeader>  e = this.headers.elements();

		while (e.hasMoreElements()) {
			MailMessageHeader h = e.nextElement();

			this.ps.println(h.name + ": " + h.val);
		}

		this.ps.println("");

		return this.ps;
	}

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
		BufferedReader bufrdr = new BufferedReader(new FileReader(this.file));

		this.readHeaders(bufrdr);
		bufrdr.close();
	}

	public void readHeaders(BufferedReader bufrdr) throws IOException {
		if (this.headers.size() > 0) return;

		String line;
		String[] parts = null;
		while ((line = bufrdr.readLine()) != null) {
			if (line.length() == 0) {
				if (parts != null)
					this.addHeader(parts[0], parts[1]);
				parts = null;
				break;
			} else if (line.startsWith(" ") || line.startsWith("\t")) {
				// continuation of previous line
				if (parts == null || parts[1] == null)
					continue;
				parts[1] += " "+line.trim();
			} else {
				if (parts != null)
					this.addHeader(parts[0], parts[1]);
				parts = null;
				parts = line.split(": ", 2);

				if (parts.length < 2)
					parts = null;
			}
		}

		if (parts != null) {
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
		BufferedReader br = new BufferedReader(new FileReader(this.file));

		long counter = 0;
		String line;

		while ((line = br.readLine()) != null) {
			counter += line.getBytes().length;
			counter += "\r\n".getBytes().length;
		}

		br.close();
		return counter;
	}

	public void closeStream() {
		try {
			if (this.brdr != null) this.brdr.close();
		} catch (IOException ioe) {

		}
		this.brdr = null;
	}

	public String readLine() throws IOException {
		if (this.brdr == null) {
			this.brdr = new BufferedReader(new FileReader(this.file));
		}

		return this.brdr.readLine();
	}

	public boolean copyTo(MailMessage msg) {
		this.closeStream();
		String line;
		try {
			PrintStream copyps = msg.getRawStream();
			while ((line = this.readLine()) != null) {
				copyps.println(line);
			}
			msg.commit();
		} catch (IOException ioe) {
			msg.cancel();
			return false;
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

		if(this.file.renameTo(newfile)) {
			this.file = newfile;
		}
	}

	@Override
	public String toString() {
		return "MailMessage backed by " + file;
	}

	public Date getDate() {
		String date = getFirstHeader("Date");
		if(date == null) {
			return null;
		}

		for(String format : dateFormats) {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			try {
				return sdf.parse(date);
			} catch (ParseException e) {
				//Try next format
			}
		}

		Logger.minor(this, "No format matched for date " + date);
		return null;
	}

	/**
	 * Checks if the header has an encoded word starting at offset. If an encoded word is found it
	 * is returned without the =? and ?= markers
	 * (i.e. &lt;charset&gt;?&lt;encoding&gt;?&lt;encoded-text&gt;)
	 */
	private String getEncodedWord(String header, int offset) {
		assert (header != null);
		assert (header.length() > offset) : "length=" + header.length() + ", offset=" + offset;
		assert (offset >= 0);

		if(!header.substring(offset, offset + "=?".length()).equals("=?")) {
			//It makes no sense to call the function if this is the case
			Logger.warning(this, "Offset didn't point to =? in isEncodedWord");
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
		Logger.debug(this, "Found possible encoded word: " + word);

		//75 - 4 since we removed the =? and ?= (75 is the limit from RFC2047)
		if(word.length() > (71)) {
			Logger.debug(this, "Found possible encoded word but it was too long");
			return null;
		}
		if(word.contains(" ")) {
			Logger.debug(this, "Found possible encoded word but it contained spaces");
			return null;
		}

		return word;
	}

	private String decodeMIMEEncodedWord(String charset, String encoding, String text)
			throws UnsupportedEncodingException {

		if(encoding.equalsIgnoreCase("B")) {
			//Base64 encoding
			byte[] bytes = Base64.decode(text.getBytes("UTF-8"));
			return new String(bytes, charset);
		}

		Logger.warning(this, "Freemail doesn't support encoding: " + encoding);
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

		StringBuffer subject = new StringBuffer();

		int offset = 0;
		while(offset < rawSubject.length()) {
			//First, copy anything that we know isn't an encoded word
			int index = rawSubject.indexOf("=?", offset);
			if(index == -1) {
				subject.append(rawSubject.substring(offset));
				break;
			}
			subject.append(rawSubject.substring(offset, index));
			offset = index;

			//Check if we have an encoded word
			String word = getEncodedWord(rawSubject, offset);
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
	 * Generated a message-id from the specified domain and date. The generated message-id will be
	 * of the form &lt;local part&gt;@&lt;domain&gt;, where the local part is generated using the
	 * specified date and a random number large enough that collisions are unlikely.
	 * @param domain the domain part of the message-id
	 * @param date the date used in the message-id
	 * @return the generated message-id
	 */
	public static String generateMessageID(String domain, Date date) {
		if(domain == null) {
			Logger.error(MailMessage.class, "Domain passed to generateMessageID() was null", new Exception());
		}

		return date.getTime() + messageIdRandom.nextLong() + "@" + domain;
	}

	private static class MailMessageHeader {
		public String name;
		public String val;

		public MailMessageHeader(String n, String v) {
			this.name = n;
			this.val = v;
		}
	}
}

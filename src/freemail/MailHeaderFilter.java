/*
 * MailHeaderFilter.java
 * This file is part of Freemail
 * Copyright (C) 2006,2007,2008 Dave Baker
 * Copyright (C) 2007 Alexander Lehmann
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

/*
 * MailHeaderFilter - A class to parse an Email message line by line
 * and strip out information we'd rather not send
 */

package freemail;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.StringBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import freemail.utils.Logger;

class MailHeaderFilter {
	private final BufferedReader reader;
	private final StringBuffer buffer;
	private boolean foundEnd;
	private static final SimpleDateFormat sdf;
	private static final TimeZone gmt;
	private static final Pattern messageIdPattern = Pattern.compile("<?([^\\@])*\\@([^>]*)>?");

	static {
		sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
		gmt = TimeZone.getTimeZone("GMT");
		sdf.setTimeZone(gmt);
	}

	public MailHeaderFilter(BufferedReader rdr) {
		this.reader = rdr;
		this.buffer = new StringBuffer();
		this.foundEnd = false;
	}

	public String readHeader() throws IOException {
		String retval = null;

		while (retval == null) {
			if (this.foundEnd) {
				return this.flush();
			}

			String line = this.reader.readLine();
			if (line == null) {
				Logger.error(this, "Warning - reached end of message file before reaching end of headers! This shouldn't happen!");
				throw new IOException("Header filter reached end of message file before reaching end of headers");
			}

			if (line.length() == 0) {
				// end of the headers
				this.foundEnd = true;
				retval = this.flush();
			} else if (line.startsWith(" ") || line.startsWith("\t")) {
				// continuation of the previous header
				this.buffer.append("\r\n "+line.trim());
			} else {
				retval = this.flush();
				this.buffer.append(line);
			}
		}
		return retval;
	}

	// this is called once a header is in the buffer
	// if the header is invalid or filtered out entirely,
	// return null. Otherwise return the filtered header.
	private String flush() {
		if (this.buffer.length() == 0) return null;

		String[] bits = this.buffer.toString().split(": ", 2);
		this.buffer.delete(0, this.buffer.length());

		// invalid header - ditch it.
		if (bits.length < 2) return null;

		bits[1] = this.filterHeader(bits[0], bits[1]);
		if (bits[1] == null) return null;

		return bits[0]+": "+bits[1];
	}

	private String filterHeader(String name, String val) {
		// Whitelist filter
		if (name.equalsIgnoreCase("Date")) {
			// the norm is to put the sender's local time here, with the sender's local time offset
			// at the end. Rather than giving away what time zone we're in, parse the date in
			// and return it as a GMT time.

			Date d = null;
			try {
				synchronized(sdf) {
					d = sdf.parse(val);
				}
			} catch (ParseException pe) {
				// ...the compiler whinges unless we catch this exception...
				Logger.normal(this, "Warning: couldn't parse date: "+val+" (caught exception)");
				return null;
			}
			// but the docs don't say that it throws it, but says that it return null
			// http://java.sun.com/j2se/1.5.0/docs/api/java/text/SimpleDateFormat.html#parse(java.lang.String, java.text.ParsePosition)
			if (d == null) {
				// invalid date - ditch the header
				Logger.normal(this, "Warning: couldn't parse date: "+val+" (got null)");
				return null;
			}
			String strDate;
			synchronized(sdf) {
				strDate = sdf.format(d);
			}
			return strDate;
		} else if (name.equalsIgnoreCase("Message-ID")) {
			// We want to keep message-ids for in-reply-to and hence message threading to work, but we need to make sure the
			// mail client hasn't put in a real hostname, as some have been known to.
			Matcher m = messageIdPattern.matcher(val);
			if (!m.matches() || m.groupCount() < 2) {
				// couldn't make any sense of it, so just drop it
				return null;
			} else {
				if (m.group(2).endsWith("freemail")) {
					// okay, the hostname part ends with freemail, so it's a fake Freemail domain and not a real one
					return val;
				} else {
					// It's something else, so just replace it with 'freemail', although this might not actually be any more
					// useful than dropping it, since the mail client will be looking for the unmangled header.
					return "<"+m.group(1)+"@freemail>";
				}

			}
		} else if (name.equalsIgnoreCase("From")) {
			return val;
		} else if (name.equalsIgnoreCase("To")) {
			return val;
		} else if (name.equalsIgnoreCase("CC")) {
			return val;
		} else if (name.equalsIgnoreCase("BCC")) {
			//The BCC field should not be sent
			return null;
		} else if (name.equalsIgnoreCase("Subject")) {
			return val;
		} else if (name.equalsIgnoreCase("MIME-Version")) {
			return val;
		} else if (name.equalsIgnoreCase("Content-Type")) {
			return val;
		} else if (name.equalsIgnoreCase("Content-Transfer-Encoding")) {
			return val;
		} else if (name.equalsIgnoreCase("In-Reply-To")) {
			return val;
		} else {
			Logger.minor(this, "Dropping header " + name + " because it isn't on the whitelist");
			return null;
		}
	}
}

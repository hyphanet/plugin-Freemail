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

package org.freenetproject.freemail;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.StringBuffer;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.freenetproject.freemail.utils.EmailAddress;
import org.freenetproject.freemail.utils.Logger;


public class MailHeaderFilter {
	private final BufferedReader reader;
	private final StringBuffer buffer;
	private boolean foundEnd;
	private static final SimpleDateFormat sdf;
	private static final TimeZone utc;

	private static final Pattern messageIdPattern = Pattern.compile("<?([^\\@>])*\\@([^>]*)>?");
	static {
		sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ROOT);
		utc = TimeZone.getTimeZone("UTC");
		sdf.setTimeZone(utc);
	}

	/** List of headers that can be passed though without being checked */
	private static final Set<String> headerWhitelist;
	static {
		Set<String> backing = new HashSet<String>();
		backing.add("To");
		backing.add("CC");
		backing.add("Subject");
		backing.add("MIME-Version");
		backing.add("Content-Type");
		backing.add("Content-Transfer-Encoding");
		backing.add("In-Reply-To");
		backing.add("References");
		headerWhitelist = Collections.unmodifiableSet(backing);
	}

	/** List of headers that must never be passed though */
	private static final Set<String> headerBlacklist;
	static {
		Set<String> backing = new HashSet<String>();
		backing.add("BCC");
		headerBlacklist = Collections.unmodifiableSet(backing);
	}

	private final FreemailAccount sender;

	public MailHeaderFilter(BufferedReader rdr, FreemailAccount sender) {
		this.reader = rdr;
		this.buffer = new StringBuffer();
		this.foundEnd = false;
		this.sender = sender;
	}

	public String readHeader() throws IOException {
		String retval = null;

		while(retval == null) {
			if(this.foundEnd) {
				return this.flush();
			}

			String line = this.reader.readLine();
			if(line == null) {
				Logger.error(this, "Warning - reached end of message file before reaching end of headers! This shouldn't happen!");
				throw new IOException("Header filter reached end of message file before reaching end of headers");
			}

			if(line.length() == 0) {
				// end of the headers
				this.foundEnd = true;
				retval = this.flush();
			} else if(line.startsWith(" ") || line.startsWith("\t")) {
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
		if(this.buffer.length() == 0) return null;

		String header = this.buffer.toString();
		String[] bits = header.split(":", 2);
		this.buffer.delete(0, this.buffer.length());

		// invalid header - ditch it.
		if(bits.length < 2) {
			Logger.warning(this, "Dropping header due to unknown format: " + header);
			return null;
		}

		bits[1] = bits[1].trim();
		bits[1] = this.filterHeader(bits[0], bits[1]);
		if(bits[1] == null) return null;

		return bits[0]+": "+bits[1];
	}

	private String filterHeader(String name, String val) {
		//Check for illegal characters
		if(name.matches(".*[^\\u0000-\\u007F]+.*")) {
			Logger.error(this, "Header name contains 8bit character(s), dropping (name=" + name + ")");
			return null;
		}
		if(val.matches(".*[^\\u0000-\\u007F]+.*")) {
			Logger.error(this, "Header value contains 8bit character(s) (name=" + name + ", value=" + val + ")");
			//These should be dropped eventually, but we still have bugs
			//related to this so just log for now
		}

		//Drop headers in the blacklist
		for(String header : headerBlacklist) {
			if(name.equalsIgnoreCase(header)) {
				Logger.minor(this, "Dropping header " + name + " because it is blacklisted");
				return null;
			}
		}

		//Pass though headers in the whitelist
		for(String header : headerWhitelist) {
			if(name.equalsIgnoreCase(header)) {
				Logger.minor(this, "Keeping header " + name + " because it is whitelisted");
				return val;
			}
		}

		//Rewrite or filter the rest
		if(name.equalsIgnoreCase("Date")) {
			// the norm is to put the sender's local time here, with the sender's local time offset
			// at the end. Rather than giving away what time zone we're in, parse the date in
			// and return it as a GMT time.

			Date d = MailMessage.parseDate(val);
			if(d == null) {
				Logger.warning(this, "Dropping date because we couldn't parse it (" + val + ")");
				return null;
			}

			String strDate;
			synchronized(sdf) {
				strDate = sdf.format(d);
			}
			return strDate;
		} else if(name.equalsIgnoreCase("Message-ID")) {
			// We want to keep message-ids for in-reply-to and hence message threading to work, but
			// we need to make sure the mail client hasn't put in a real hostname, as some have been
			// known to.
			Matcher m = messageIdPattern.matcher(val);
			if(m.matches() && m.groupCount() == 2 && m.group(2).endsWith(".freemail")) {
				// okay, the hostname part ends with .freemail, so it's a fake Freemail domain and
				// not a real one
				return val;
			}

			// It's something else, so just replace it with a new message-id
			Logger.normal(this, "Replacing message id header");
			return "<" + MailMessage.generateMessageID(sender.getDomain()) + ">";
		} else if(name.equalsIgnoreCase("From")) {
			EmailAddress address;
			try {
				address = new EmailAddress(val);
			} catch (IllegalArgumentException e) {
				Logger.minor(this, "From header didn't contain a valid email, dropping");
				return sender.getNickname() + "@" + sender.getDomain();
			}

			if(!address.domain.equalsIgnoreCase(sender.getDomain())) {
				Logger.minor(this,  "");
				return sender.getNickname() + "@" + sender.getDomain();
			}

			return val;
		} else {
			Logger.warning(this, "Dropping unknown header " + name);
			return null;
		}
	}
}

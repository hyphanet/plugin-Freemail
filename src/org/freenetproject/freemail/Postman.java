/*
 * Postman.java
 * This file is part of Freemail
 * Copyright (C) 2006,2007 Dave Baker
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.IOException;

import org.freenetproject.freemail.utils.EmailAddress;

import freenet.support.Logger;


/** A postman is any class that delivers mail to an inbox. Simple,
 *  if not politically correct.
 */
public abstract class Postman {
	private static final int BOUNDARY_LENGTH = 32;

	protected void storeMessage(BufferedReader brdr, MessageBank mb) throws IOException {
		MailMessage newmsg = mb.createMessage();

		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z", Locale.US);

		newmsg.readHeaders(brdr);

		// add our own headers
		// received and date
		newmsg.addHeader("Received", "(Freemail); "+sdf.format(new Date()));

		// validate the from header - or headers. There could be several.
		List<String> froms = newmsg.getHeadersByName("From");

		boolean first = true;
		for(String from : froms) {
			EmailAddress addr = null;
			try {
				addr = new EmailAddress(from);
			} catch (IllegalArgumentException e) {
				//Invalid address, remove it and keep going
				Logger.warning(this, "Ignoring invalid address from received message: " + from);
				newmsg.removeHeader("From", from);
				continue;
			}

			if(first) {
				if(!this.validateFrom(addr)) {
					newmsg.removeHeader("From", from);
					if(addr.realname == null) addr.realname = "";
					addr.realname = "**SPOOFED** "+addr.realname;
					addr.realname = addr.realname.trim();
					newmsg.addHeader("From", addr.toLongString());
				}
			} else {
				newmsg.removeHeader("From", from);
			}
			first = false;
		}


		PrintStream ps = newmsg.writeHeadersAndGetStream();

		String line;
		while((line = brdr.readLine()) != null) {
			ps.println(line);
		}

		newmsg.commit();
		brdr.close();
	}

	public static boolean bounceMessage(File origmsg, MessageBank mb, String errmsg) {
		return bounceMessage(origmsg, mb, errmsg, false);
	}

	public static boolean bounceMessage(File origmsg, MessageBank mb, String errmsg, boolean isFreemailFormat) {
		MailMessage bmsg = null;
		try {
			bmsg = mb.createMessage();

			SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z", Locale.US);
			Date currentDate = new Date();

			bmsg.addHeader("From", "Freemail Postmaster <postmaster@freemail>");
			bmsg.addHeader("Subject", "Undeliverable Freemail");
			String origFrom = extractFromAddress(origmsg, isFreemailFormat);
			if(origFrom != null) {
				bmsg.addHeader("To", origFrom);

				//FIXME: We should add a message id even if we don't get the from address
				String toDomain = origFrom.substring(origFrom.lastIndexOf("@") + 1);
				bmsg.addHeader("Message-id", "<" + MailMessage.generateMessageID(toDomain, currentDate) + ">");
			}
			bmsg.addHeader("Date", sdf.format(currentDate));
			bmsg.addHeader("MIME-Version", "1.0");
			String boundary="boundary-";
			Random rnd = new Random();
			int i;
			for(i = 0; i < BOUNDARY_LENGTH; i++) {
				boundary += (char)(rnd.nextInt(25) + (int)'a');
			}
			bmsg.addHeader("Content-Type", "Multipart/Mixed; boundary=\""+boundary+"\"");

			PrintStream ps = bmsg.writeHeadersAndGetStream();

			ps.println("--"+boundary);
			ps.println("Content-Type: text/plain");
			ps.println("Content-Disposition: inline");
			ps.println("");
			ps.println("Freemail was unable to deliver your message. The problem was:");
			ps.println("");
			ps.println(errmsg);
			ps.println("");
			ps.println("The original message is included below.");
			ps.println("");
			ps.println("--"+boundary);
			ps.println("Content-Type: message/rfc822;");
			ps.println("Content-Disposition: inline");
			ps.println("");

			BufferedReader br = new BufferedReader(new FileReader(origmsg));

			String line;
			if(isFreemailFormat) {
				while((line = br.readLine()) != null) {
					if(line.length() == 0) break;
				}
			}

			while((line = br.readLine()) != null) {
				if(line.indexOf(boundary) > 0) {
					// The random boundary string appears in the
					// message! What are the odds!?
					// try again
					br.close();
					bmsg.cancel();
					bounceMessage(origmsg, mb, errmsg);
				}
				ps.println(line);
			}

			br.close();
			ps.println("--"+boundary);
			bmsg.commit();
		} catch (IOException ioe) {
			if(bmsg != null) bmsg.cancel();
			return false;
		}
		return true;
	}

	private static String extractFromAddress(File msg, boolean isFreemailFormat) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(msg));

			String line;
			if(isFreemailFormat) {
				while((line = br.readLine()) != null) {
					if(line.length() == 0) break;
				}
			}

			while((line = br.readLine()) != null) {
				if(line.length() == 0) return null;
				String[] parts = line.split(": ", 2);
				if(parts.length < 2) continue;
				if(parts[0].equalsIgnoreCase("From")) {
					return parts[1];
				}
			}
		} catch (IOException ioe) {
		} finally {
			if(br != null) {
				try {
					br.close();
				} catch (IOException e) {
					Logger.error(Postman.class, "Caugth IOException while closing " + br, e);
				}
			}
		}
		return null;
	}

	public abstract boolean validateFrom(EmailAddress from);
}

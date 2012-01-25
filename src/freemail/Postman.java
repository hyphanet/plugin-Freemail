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

package freemail;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.IOException;

import freemail.utils.EmailAddress;

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
		String[] froms = newmsg.getHeadersAsArray("From");
		
		int i;
		boolean first = true;
		for (i = 0; i < froms.length; i++) {
			EmailAddress addr = new EmailAddress(froms[i]);
			
			if (first) {
				if (!this.validateFrom(addr)) {
					newmsg.removeHeader("From", froms[i]);
					EmailAddress e = new EmailAddress(froms[i]);
					if (e.realname == null) e.realname = "";
					e.realname = "**SPOOFED** "+e.realname;
					e.realname = e.realname.trim();
					newmsg.addHeader("From", e.toLongString());
				}
			} else {
				newmsg.removeHeader("From", froms[i]);
			}
			first = false;
		}
		
		
		PrintStream ps = newmsg.writeHeadersAndGetStream();
		
		String line;
		while ( (line = brdr.readLine()) != null) {
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
			if (origFrom != null) {
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
			for (i = 0; i < BOUNDARY_LENGTH; i++) {
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
			if (isFreemailFormat) {
				while ( (line = br.readLine()) != null) {
					if (line.length() == 0) break;
				}
			}
			
			while ( (line = br.readLine()) != null) {
				if (line.indexOf(boundary) > 0) {
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
			if (bmsg != null) bmsg.cancel();
			return false;
		}
		return true;
	}
	
	private static String extractFromAddress(File msg, boolean isFreemailFormat) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(msg));
			
			String line;
			if (isFreemailFormat) {
				while ( (line = br.readLine()) != null) {
					if (line.length() == 0) break;
				}
			}
			
			while ( (line = br.readLine()) != null) {
				if (line.length() == 0) return null;
				String[] parts = line.split(": ", 2);
				if (parts.length < 2) continue;
				if (parts[0].equalsIgnoreCase("From")) {
					br.close();
					return parts[1];
				}
			}
			br.close();
		} catch (IOException ioe) {
		}
		return null;
	}
	
	public abstract boolean validateFrom(EmailAddress from);
}

/*
 * MailLog.java
 * This file is part of Freemail, copyright (C) 2006,2008 Dave Baker
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import static java.nio.charset.StandardCharsets.UTF_8;

class MailLog {
	private final File logfile;
	private HashMap<Integer, String> messages;
	private int lastMessageId;
	private int passes;

	MailLog(File logfile) {
		this.lastMessageId = 0;
		this.passes = 0;

		this.messages = new HashMap<>();
		this.logfile = logfile;

		try (InputStream frdr = new FileInputStream(this.logfile);
				BufferedReader br = new BufferedReader(new InputStreamReader(frdr, UTF_8))) {
			String line;

			while((line = br.readLine()) != null) {
				String[] parts = line.split("=");

				if(parts.length != 2) continue;

				if(parts[0].equalsIgnoreCase("passes")) {
					this.passes = Integer.parseInt(parts[1]);
					continue;
				}

				int thisnum = Integer.parseInt(parts[0]);
				if(thisnum > this.lastMessageId)
					this.lastMessageId = thisnum;
				this.messages.put(thisnum, parts[1]);
			}

		} catch (IOException ioe) {
			return;
		}
	}

	public int incPasses() {
		this.passes++;
		this.writeLogFile();
		return this.passes;
	}

	public int getPasses() {
		return this.passes;
	}

	public int getNextMessageId() {
		return this.lastMessageId + 1;
	}

	public void addMessage(int num, String checksum) {
		this.messages.put(num, checksum);
		if(num > this.lastMessageId)
			this.lastMessageId = num;
		this.writeLogFile();
	}

	private void writeLogFile() {
		try (FileOutputStream fos = new FileOutputStream(this.logfile);
				PrintWriter pw = new PrintWriter(fos)) {

			pw.println("passes="+this.passes);

			this.messages.forEach((num, checksum) -> pw.println(num + "=" + checksum));

			pw.flush();

		} catch (IOException ioe) {
			return;
		}
	}
}

/*
 * MessageLog.java
 * This file is part of Freemail
 * Copyright (C) 2011 Martin Nyhus
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

package freemail.transport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

class MessageLog {
	private final File logfile;

	public MessageLog(File logFile) {
		this.logfile = logFile;
	}

	public boolean isPresent(long targetid) throws IOException {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(this.logfile));
		} catch (FileNotFoundException fnfe) {
			return false;
		}

		String line;
		while ( (line = br.readLine()) != null) {
			long curid = Long.parseLong(line);
			if (curid == targetid) {
				br.close();
				return true;
			}
		}

		br.close();
		return false;
	}

	public void add(long id) throws IOException {
		FileOutputStream fos = new FileOutputStream(this.logfile, true);

		PrintStream ps = new PrintStream(fos);
		ps.println(id);
		ps.close();
	}
}
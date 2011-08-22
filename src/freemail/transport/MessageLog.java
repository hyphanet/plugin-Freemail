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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

class MessageLog {
	private final File logfile;

	private Set<Long> presentIds = null;

	public MessageLog(File logFile) {
		this.logfile = logFile;
	}

	public boolean isPresent(long targetid) throws IOException {
		if(presentIds == null) {
			readIds();
		}

		return presentIds.contains(Long.valueOf(targetid));
	}

	public void add(long id) throws IOException {
		if(presentIds == null) {
			readIds();
		}

		presentIds.add(Long.valueOf(id));
		writeIds();
	}

	public void remove(long id) throws IOException {
		if(presentIds == null) {
			readIds();
		}

		presentIds.remove(Long.valueOf(id));
		writeIds();
	}

	private void readIds() throws IOException {
		presentIds = new HashSet<Long>();

		if(!logfile.exists()) {
			logfile.createNewFile();
		}
		BufferedReader br = new BufferedReader(new FileReader(this.logfile));

		String line;
		while ( (line = br.readLine()) != null) {
			long curid = Long.parseLong(line);
			presentIds.add(Long.valueOf(curid));
		}

		br.close();
	}

	private void writeIds() throws IOException {
		if(!logfile.exists()) {
			logfile.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(this.logfile, false);

		PrintStream ps = new PrintStream(fos);
		for(Long id : presentIds) {
			ps.println(id);
		}
		ps.close();
	}
}

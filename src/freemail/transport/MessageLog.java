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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

class MessageLog {
	private static final String SEPARATOR = ", ";

	private final File logfile;

	private Map<Long, String> presentIds = null;

	public MessageLog(File logFile) {
		this.logfile = logFile;
	}

	public boolean isPresent(long targetid) throws IOException {
		if(presentIds == null) {
			readIds();
		}

		return presentIds.containsKey(Long.valueOf(targetid));
	}

	public void add(long id, String data) throws IOException {
		if(presentIds == null) {
			readIds();
		}
		if((data != null) && (data.contains("\n"))) {
			throw new IllegalArgumentException("Argument data contained newline");
		}

		presentIds.put(Long.valueOf(id), data);
		writeIds();
	}

	public void remove(long id) throws IOException {
		if(presentIds == null) {
			readIds();
		}

		presentIds.remove(Long.valueOf(id));
		writeIds();
	}

	public Iterator<Entry<Long, String>> iterator() throws IOException {
		if(presentIds == null) {
			readIds();
		}

		return presentIds.entrySet().iterator();
	}

	public Iterator<Long> keyIterator() throws IOException {
		if(presentIds == null) {
			readIds();
		}

		return presentIds.keySet().iterator();
	}

	private void readIds() throws IOException {
		presentIds = new HashMap<Long, String>();

		if(!logfile.exists()) {
			logfile.createNewFile();
		}
		BufferedReader br = new BufferedReader(new FileReader(this.logfile));

		String line;
		while ( (line = br.readLine()) != null) {
			int sepIndex = line.indexOf(SEPARATOR);
			long curid = Long.parseLong(line.substring(0, sepIndex));

			String data;
			if(sepIndex + SEPARATOR.length() > line.length()) {
				data = null;
			} else {
				data = line.substring(sepIndex + SEPARATOR.length());
			}

			presentIds.put(Long.valueOf(curid), data);
		}

		br.close();
	}

	private void writeIds() throws IOException {
		if(!logfile.exists()) {
			logfile.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(this.logfile, false);

		PrintStream ps = new PrintStream(fos);
		for(Entry<Long, String> entry : presentIds.entrySet()) {
			String line = entry.getKey() + SEPARATOR;
			if(entry.getValue() != null) {
				line += entry.getValue();
			}
			ps.println(line);
		}
		ps.close();
	}
}

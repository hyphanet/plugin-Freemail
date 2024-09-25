/*
 * LineReadingInputStream.java
 * This file is part of Freemail
 * Copyright (C) 2006,2007 Matthew Toseland
 * Copyright (C) 2006 syoung
 * Copyright (C) 2006 Florent Daignière
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

package org.freenetproject.freemail.support.io;

/*
 * This file originates from the main Freenet distribution, originally in freenet.support.io
 */

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A FilterInputStream which provides readLine().
 */
public class LineReadingInputStream extends FilterInputStream implements LineReader {
	private int lastBytesRead;

	public LineReadingInputStream(InputStream in) {
		super(in);
	}

	private byte[] buf;

	/**
	 * Read a \n or \r\n terminated line of UTF-8 or ISO-8859-1.
	 */
	@Override
	public String readLine(int maxLength, int bufferSize, boolean utf) throws IOException {
		if(buf == null)
			buf = new byte[Math.max(128, Math.min(1024, bufferSize))];
		int ctr = 0;
		this.lastBytesRead = 0;
		while(true) {
			int x = read();
			this.lastBytesRead++;
			if(x == -1) {
				if(ctr == 0) return null;
				return new String(buf, 0, ctr, utf ? "UTF-8" : "ISO-8859-1");
			}
			// REDFLAG this is definitely safe with the above charsets, it may not be safe with some wierd ones.
			if(x == (int)'\n') {
				if(ctr == 0) return "";
				if(buf[ctr-1] == '\r') ctr--;
				return new String(buf, 0, ctr, utf ? "UTF-8" : "ISO-8859-1");
			}
			if(ctr >= buf.length) {
				if(buf.length == bufferSize) throw new TooLongException();
				byte[] newBuf = new byte[Math.min(buf.length * 2, bufferSize)];
				System.arraycopy(buf, 0, newBuf, 0, buf.length);
				buf = newBuf;
			}
			buf[ctr++] = (byte)x;
		}
	}

	public int getLastBytesRead() {
		return this.lastBytesRead;
	}
}

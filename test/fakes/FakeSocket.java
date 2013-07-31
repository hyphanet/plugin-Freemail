/*
 * FakeSocket.java
 * This file is part of Freemail, copyright (C) 2011 Martin Nyhus
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

package fakes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.Socket;

public class FakeSocket extends Socket {
	/** Delivers data to the class under test, from fromTester */
	private PipedOutputStream toTested = new PipedOutputStream();

	/** Delivers data to the controlling class, from fromTested */
	private PipedOutputStream toTester = new PipedOutputStream();

	/** Receives data from the class under test, delivers to toTester */
	private PipedInputStream fromTested = new PipedInputStream();

	/** Receives data from the controlling class, delivers to toTested */
	private PipedInputStream fromTester = new PipedInputStream();

	public FakeSocket() throws IOException {
		toTested.connect(fromTester);
		toTester.connect(fromTested);
	}

	// These are the two methods used by the class being tested
	@Override
	public OutputStream getOutputStream() {
		return toTested;
	}

	@Override
	public InputStream getInputStream() {
		return fromTested;
	}

	//These two are used by the controller to pass data to/from the class being tested
	public OutputStream getOutputStreamOtherSide() {
		return toTester;
	}

	public InputStream getInputStreamOtherSide() {
		return fromTester;
	}

	@Override
	public synchronized void close() throws IOException {
		super.close();
		toTested.close();
		toTester.close();
		fromTested.close();
		fromTester.close();
	}
}

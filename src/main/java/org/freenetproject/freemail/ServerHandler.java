/*
 * ServerHandler.java
 * This file is part of Freemail, copyright (C) 2007 Dave Baker
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

import java.net.Socket;
import java.io.IOException;

public abstract class ServerHandler {
	protected final Socket client;
	protected volatile boolean stopping = false;

	public ServerHandler(Socket c) {
		client = c;
	}

	public boolean isAlive() {
		return !client.isClosed();
	}

	public void kill() {
		stopping = true;
		try {
			client.close();
		} catch (IOException ioe) {

		}
	}
}

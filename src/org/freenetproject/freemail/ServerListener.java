/*
 * ServerListener.java
 * This file is part of Freemail, copyright (C) 2007, 2008 Dave Baker
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

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class ServerListener {
	protected ServerSocket sock;
	private final ArrayList<ServerHandler> handlers;
	private final ArrayList<Thread> handlerThreads;

	protected ServerListener() {
		handlers = new ArrayList<ServerHandler>();
		handlerThreads = new ArrayList<Thread>();
	}

	/**
	 * Terminate the run method
	 */
	public void kill() {
		try {
			if (sock != null) sock.close();
		} catch (IOException ioe) {

		}
		// kill all our handlers too
		synchronized(handlers) {
			for (Iterator<ServerHandler> i = handlers.iterator(); i.hasNext(); ) {
				ServerHandler handler = i.next();
				handler.kill();
			}
		}
	}

	/**
	 * Wait for all our client threads to terminate
	 */
	public void joinClientThreads() {
		for (Iterator<Thread> i = handlerThreads.iterator(); i.hasNext(); ) {
			Thread t = i.next();
			while (t != null) {
				try {
					t.join();
					t = null;
				} catch (InterruptedException ie) {

				}
			}
		}
	}

	protected void addHandler(ServerHandler hdlr, Thread thrd) {
		synchronized(handlers) {
			handlers.add(hdlr);
		}
		handlerThreads.add(thrd);
	}

	protected void reapHandlers() {
		// clean up dead handlers...
		synchronized(handlers) {
			for (Iterator<ServerHandler> i = handlers.iterator(); i.hasNext(); ) {
				ServerHandler handler = i.next();
				if (!handler.isAlive()) {
					i.remove();
				}
			}
		}

		// ...and threads...
		for (Iterator<Thread> i = handlerThreads.iterator(); i.hasNext(); ) {
			Thread t = i.next();
			if (!t.isAlive()) {
				i.remove();
			}
		}
	}
}

/*
 * TextProtocolTester.java
 * This file is part of Freemail
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

package utils;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TextProtocolTester {
	private final PrintWriter toHandler;
	private final BufferedReader fromHandler;

	public TextProtocolTester(PrintWriter toHandler, BufferedReader fromHandler) {
		this.toHandler = toHandler;
		this.fromHandler = fromHandler;
	}

	/**
	 * Runs a tests by sending all the commands then checking that the expected
	 * replies were returned. Use {@link #runProtocolTest(List)} instead since
	 * it allows for more accurate checks of the responses.
	 */
	@Deprecated
	public void runSimpleTest(List<String> commands, List<String> expectedResponse) throws IOException {
		List<Command> combined = new LinkedList<Command>();

		//Add all the commands first, then the replies, ensuring all the
		//commands will be sent before checking the replies
		for(String cmd : commands) {
			combined.add(new Command(cmd));
		}
		combined.add(new Command(null, expectedResponse));
		runProtocolTest(combined);
	}

	private void checkReply(int lineNum, String expected) throws IOException {
		try {
			waitForReady(10, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			fail("Test timed out waiting for response");
		} catch (InterruptedException e) {
			fail("Thread interrupted unexpectedly");
		}

		String line = fromHandler.readLine();
		assertEquals("Failed at line " + lineNum, expected, line);
	}

	public void runProtocolTest(List<Command> commands) throws IOException {
		//Check the replies after each command only when EXTENSIVE is set since
		//it's a lot slower
		int lineNum = 1;
		for(Command cmd : commands) {
			if(cmd.command != null) {
				toHandler.write(cmd.command + "\r\n");
				toHandler.flush();
			}

			for(String reply : cmd.replies) {
				checkReply(lineNum++, reply);
			}
		}

		//Read any extra data if possible. This is not reliable due to race
		//conditions between this thread and the thread that writes data to the
		//connection, but it is better than nothing.
		if(fromHandler.ready()) {
			String data = "";
			while(fromHandler.ready()) {
				char[] tmp = new char[1024];
				int read = fromHandler.read(tmp, 0, tmp.length);
				data += new String(tmp, 0, read);
			}
			fail("Socket has more data: " + data);
		}
	}

	private void waitForReady(int timeout, TimeUnit unit) throws IOException, TimeoutException, InterruptedException {
		long start = System.nanoTime();
		int sleepTime = 1;

		while(!fromHandler.ready()) {
			//Check if we have timed out
			long waited = System.nanoTime() - start;
			if(unit.convert(waited, TimeUnit.NANOSECONDS) > timeout) {
				throw new TimeoutException();
			}

			//If not sleep a little
			Thread.sleep(sleepTime);
			sleepTime = Math.max(sleepTime * 2, 100);
		}
	}

	public static final class Command {
		private final String command;
		private final List<String> replies = new LinkedList<String>();

		public Command(String command, String ... replies) {
			this.command = command;
			for(String reply : replies) {
				this.replies.add(reply);
			}
		}

		public Command(String command, List<String> replies) {
			this.command = command;
			this.replies.addAll(replies);
		}
	}
}

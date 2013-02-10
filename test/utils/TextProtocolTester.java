package utils;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import fakes.FakeSocket;

public class TextProtocolTester {
	private final PrintWriter toHandler;
	private final BufferedReader fromHandler;

	public TextProtocolTester(FakeSocket socket) {
		toHandler = new PrintWriter(socket.getOutputStreamOtherSide());
		fromHandler = new BufferedReader(new InputStreamReader(socket.getInputStreamOtherSide()));
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

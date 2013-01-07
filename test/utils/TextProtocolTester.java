package utils;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

	public void runSimpleTest(List<String> commands, List<String> expectedResponse) throws IOException {
		for(String cmd : commands) {
			send(toHandler, cmd + "\r\n");
		}

		int lineNum = 0;
		for(String response : expectedResponse) {
			try {
				waitForReady(1, TimeUnit.SECONDS);
			} catch (TimeoutException e) {
				fail("Test timed out waiting for response");
			} catch (InterruptedException e) {
				fail("Thread interrupted unexpectedly");
			}

			String line = fromHandler.readLine();
			assertEquals("Failed at line " + lineNum++, response, line);
		}

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

	private void send(PrintWriter out, String msg) {
		out.print(msg);
		out.flush();
	}

	private void waitForReady(int timeout, TimeUnit unit)
			throws IOException, TimeoutException, InterruptedException {
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
}

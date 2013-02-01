import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ReceiveMessage {
	public static void main(String[] args) throws IOException {
		//Arguments:
		// 1: Server address
		// 2: Server port
		// 3: Username
		// 4: Password
		// 5: Message id

		if(args.length < 5) {
			System.err.println("Wrong number of arguments");
			System.exit(1);
			return;
		}

		final String serverAddress = args[0];

		final int serverPort;
		try {
			serverPort = Integer.parseInt(args[1]);
		} catch(NumberFormatException e) {
			System.err.println("Couldn't parse port number: " + args[1]);
			System.exit(1);
			return;
		}

		final String username = args[2];
		final String password = args[3];
		final String messageId = args[4];

		final Socket sock;
		try {
			sock = new Socket(serverAddress, serverPort);
		} catch (UnknownHostException e) {
			throw new AssertionError("Connection failure path not implemented");
		}

		final IMAPSession s = new IMAPSession(sock);
		s.connect();
		if(!s.authenticate(username, password)) {
			throw new AssertionError("Auth failure path not implemented");
		}

		while(!s.checkForMessage("INBOX", messageId)) {
			try {
				Thread.sleep(TimeUnit.MILLISECONDS.convert(15, TimeUnit.SECONDS));
			} catch(InterruptedException e) {
				throw new AssertionError("Unexpected InterruptedException", e);
			}
		}
	}

	private static class IMAPSession {
		private final BufferedReader input;
		private final PrintWriter output;
		private final List<String> capabilities = new LinkedList<String>();

		private int nextTagToSend = 0;
		private IMAPState state = IMAPState.UNKNOWN;

		/**
		 * The folder that is currently selected. Only valid if state == IMAPState.SELECTED.
		 */
		private String curFolder;

		public IMAPSession(final Socket sock) throws IOException {
			input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			output = new PrintWriter(sock.getOutputStream());
		}

		public void connect() throws IOException {
			//Read greeting
			String line = readLine();
			String[] parts = line.split(" ", 3);

			if(!parts[0].equals("*")) {
				throw new AssertionError("Tagged greeting line path isn't implemented");
			}
			if(parts[1].equals("PREAUTH")) {
				throw new AssertionError("PREAUTH greeting path isn't implemented");
			}
			if(parts[1].equals("BYE")) {
				throw new AssertionError("BYE greeting path isn't implemented");
			}
			if(!parts[1].equals("OK")) {
				throw new AssertionError("Received unknown greeting (parts[1]=" + parts[1] + ")");
			}

			//Check if greeting contains capabilities
			if(parts[2].startsWith("[CAPABILITY ")) {
				String caps = parts[2].substring(1, parts[2].indexOf(']'));
				parseCapabilityLine(caps);
			} else {
				//If not, ask the server explicitly
				sendCapability();
			}

			state = IMAPState.NOT_AUTHENTICATED;
		}

		public boolean authenticate(final String username, final String password) throws IOException {
			if(state != IMAPState.NOT_AUTHENTICATED) {
				throw new IllegalStateException("IMAPSession in wrong state: " + state);
			}

			String tag = sendCommand("LOGIN \"" + username + "\" \"" + password + "\"");
			String reply = readLine();

			if(!reply.startsWith(tag)) {
				throw new AssertionError("Wrong tag in response to CAPABILITY command");
			}

			return reply.startsWith(tag + " OK");
		}

		public boolean checkForMessage(String folder, String messageId) throws IOException {
			if(state != IMAPState.SELECTED || !folder.equals(curFolder)) {
				selectFolder(folder);
			}

			//Search for a message with the correct message id
			String tag = sendCommand("SEARCH HEADER Message-ID " + messageId);

			String searchResult = readLine();
			if(!searchResult.startsWith("* SEARCH")) {
				throw new AssertionError("First reply after search was not * SEARCH: " + searchResult);
			}

			String result = readLine();
			if(!result.startsWith(tag)) {
				throw new AssertionError("Wrong tag in response to SEARCH command");
			}

			return !searchResult.substring("* SEARCH".length()).isEmpty();
		}


		private void selectFolder(final String folder) throws IOException {
			String tag = sendCommand("SELECT " + folder);

			//Read and parse untagged responses until we get to the tagged reply
			String reply;
			for(reply = readLine(); reply.startsWith("* "); reply = readLine()) {
				parseUntagged(reply);
			}

			if(!reply.startsWith(tag)) {
				throw new AssertionError("Wrong tag in response to SELECT command");
			}

			if(reply.startsWith(tag + " OK")) {
				state = IMAPState.SELECTED;
				curFolder = folder;
				return;
			}

			state = IMAPState.AUTHENTICATED;
			System.err.println("Select of " + folder + " failed: " + reply);
		}

		private void parseUntagged(final String line) {
			if(!line.split(" ", 2)[0].equals("*")) {
				throw new IllegalArgumentException("Line passed to parseUntagged was not untagged");
			}

			if(line.startsWith("* FLAGS")) {
				//Ignore
			} else if(line.matches("\\* \\d+ RECENT")) {
				//Ignore
			} else if(line.matches("\\* \\d+ EXISTS")) {
				//Ignore
			} else if(line.startsWith("* OK")) {
				//Ignore
			} else {
				throw new AssertionError("parseUntagged not implemented (line=" + line + ")");
			}
		}

		private void sendCapability() throws IOException {
			String tag = sendCommand("CAPABILITY");

			//First we get an untagged capability line
			String capabilitiesLine = readLine();

			//Then the tagged response
			String reply = readLine();
			if(!reply.startsWith(tag)) {
				throw new AssertionError("Wrong tag in response to CAPABILITY command");
			}

			parseCapabilityLine(capabilitiesLine.substring(2));
		}

		private void parseCapabilityLine(final String line) {
			String[] parts = line.split(" ");
			capabilities.clear();
			for(int i = 1; i < parts.length; i++) {
				capabilities.add(parts[i]);
			}
		}

		private String sendCommand(final String cmd) {
			final String tag = Integer.toString(nextTagToSend);
			nextTagToSend++;

			System.out.println(">>> " + tag + " " + cmd);
			output.print(tag + " " + cmd + "\r\n");
			output.flush();

			return tag;
		}

		private String readLine() throws IOException {
			String line = input.readLine();
			System.out.println("<<< " + line);
			return line;
		}

		private enum IMAPState {
			NOT_AUTHENTICATED,
			AUTHENTICATED,
			SELECTED,
			LOGOUT,

			UNKNOWN
		}
	}
}

/*
 * SendMessage.java
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.bouncycastle.util.encoders.Base64;

public class SendMessage {
	public static void main(String[] args) throws IOException, ServerClosedConnectionException {
		//Arguments:
		// 1: Server address
		// 2: Server port
		// 3: Username
		// 4: Password
		// 5: from address
		// 6...n: Recipients

		if(args.length < 6) {
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
		final String from = args[4];

		final List<String> recipients;
		{
			List<String> temp = new ArrayList<String>(args.length - 5);
			for(int i = 5; i < args.length; i++) {
				temp.add(args[i]);
			}
			recipients = Collections.unmodifiableList(temp);
		}

		final Socket sock;
		try {
			sock = new Socket(serverAddress, serverPort);
		} catch (UnknownHostException e) {
			throw new AssertionError("Connection failure path not implemented");
		}

		final SMTPSession s = new SMTPSession(sock);
		s.connect();
		if(!s.authenticate(username, password)) {
			throw new AssertionError("Auth failure path not implemented");
		}

		final BufferedReader message = new BufferedReader(new InputStreamReader(System.in));
		s.sendMessage(from, recipients, message);
	}

	private static class SMTPSession {
		private final BufferedReader input;
		private final PrintWriter output;
		private final List<String> ehloLines = new LinkedList<String>();

		public SMTPSession(final Socket sock) throws IOException {
			input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			output = new PrintWriter(sock.getOutputStream());
		}

		public void connect() throws IOException, ServerClosedConnectionException {
			readGreeting();
			sendEhlo();
		}

		public boolean authenticate(final String username, final String password) throws IOException {
			boolean foundAuthSupport = false;
			for(String ehloLine : ehloLines) {
				System.err.println("Checking ehlo line: " + ehloLine);
				String[] parts = ehloLine.split(" ");
				if(parts[0].equalsIgnoreCase("AUTH")) {
					for(String param : parts) {
						if(param.equals("PLAIN")) {
							foundAuthSupport = true;
						}
					}
				}
			}
			if(!foundAuthSupport) {
				throw new UnsupportedOperationException("Server doesn't support plain auth");
			}

			final String authData;
			try {
				final byte[] authBytes = ("\0" + username + "\0" + password).getBytes("UTF-8");
				authData = new String(Base64.encode(authBytes), "ASCII");
			} catch (UnsupportedEncodingException e) {
				//Both ASCII and UTF-8 charsets are guaranteed to be available
				throw new AssertionError(e);
			}

			System.out.println(">>> AUTH PLAIN " + authData);
			output.print("AUTH PLAIN " + authData + "\r\n");
			output.flush();
			final String response = input.readLine();
			System.out.println("<<< " + response);
			final int code = getResponseCode(response);
			if(code != 235) {
				System.err.println("Authentication failed, server replied: " + response);
				return false;
			}

			return true;
		}

		public void sendMessage(final String from, final List<String> recipients,
		                        final BufferedReader message) throws IOException {
			//MAIL FROM
			System.out.println(">>> MAIL FROM:<" + from + ">");
			output.print("MAIL FROM:<" + from + ">\r\n");
			output.flush();
			String line = input.readLine();
			System.out.println("<<< " + line);
			if(!line.startsWith("250 ")) {
				throw new AssertionError("MAIL FROM failure path not implemented");
			}

			//RCPT TO
			for(String recipient : recipients) {
				System.out.println(">>> RCPT TO:<" + recipient  + ">");
				output.print("RCPT TO:<" + recipient + ">\r\n");
				output.flush();
				line = input.readLine();
				System.out.println("<<< " + line);
				if(!line.startsWith("250 ")) {
					throw new AssertionError("RCPT TO failure path not implemented");
				}
			}

			//DATA
			System.out.println(">>> DATA");
			output.print("DATA\r\n");
			output.flush();
			line = input.readLine();
			System.out.println("<<< " + line);
			if(!line.startsWith("354 ")) {
				throw new AssertionError("DATA failure path not implemented");
			}

			for(line = message.readLine(); line != null; line = message.readLine()) {
				//Dot pad if needed
				System.out.print(">>> ");
				if(line.startsWith(".")) {
					System.out.print(".");
					output.print(".");
				}
				System.out.println(line);
				output.print(line + "\r\n");
			}

			System.out.println(">>> ");
			System.out.println(">>> .");
			output.print("\r\n.\r\n");
			output.flush();
			line = input.readLine();
			System.out.println("<<< " + line);
			if(!line.startsWith("250 ")) {
				throw new AssertionError("DATA failure path not implemented");
			}
		}


		private void readGreeting() throws IOException, ServerClosedConnectionException {
			final String greeting = input.readLine();
			System.out.println("<<< " + greeting);
			int code = getResponseCode(greeting);
			if(code == 220) {
				return;
			}

			//Server doesn't want the connection, send quit and wait for
			//response or closed connection
			System.out.println(">>> QUIT");
			output.print("QUIT\r\n");
			output.flush();

			try {
				System.out.println("<<< " + input.readLine());
			} catch(IOException e) {
				//Most likely because the connection was closed
			}

			throw new ServerClosedConnectionException("Server rejected connection: " + greeting);
		}

		private void sendEhlo() throws IOException, ServerClosedConnectionException {
			//First try using EHLO
			System.out.println(">>> EHLO localhost");
			output.print("EHLO localhost\r\n");
			output.flush();
			String line = input.readLine();
			System.out.println("<<< " + line);
			if(getResponseCode(line) != 250) {
				//Server might not support EHLO, try HELO instead
				sendHelo();
				return;
			}

			//Read the ehlo-keywords if there are any
			ehloLines.clear();
			while(line.startsWith("250-")) {
				line = input.readLine();
				System.out.println("<<< " + line);
				String ehloLine = line.substring(4); //Strips both 250- and 250<SP> prefixes
				ehloLines.add(ehloLine);
			}

			if(getResponseCode(line) != 250) {
				//Some error, close connection
				System.out.println(">>> QUIT");
				output.print("QUIT\r\n");
				output.flush();

				try {
					System.out.println("<<< " + input.readLine());
				} catch(IOException e) {
					//Most likely because the connection was closed
				}

				throw new ServerClosedConnectionException("Server rejected connection: " + line);
			}
		}

		private void sendHelo() {
			throw new AssertionError("HELO not supported yet");
		}

		private int getResponseCode(final String line) {
			final String code = line.substring(0, 3);
			return Integer.parseInt(code);
		}
	}

	private static class ServerClosedConnectionException extends Exception {
		private static final long serialVersionUID = 1L;

		public ServerClosedConnectionException(final String string) {
			super(string);
		}
	}
}

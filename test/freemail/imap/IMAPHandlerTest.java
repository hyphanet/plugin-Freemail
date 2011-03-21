/*
 * IMAPHandlerTest.java
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

package freemail.imap;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;

import fakes.FakeSocket;
import fakes.NullAccountManager;
import freemail.AccountManager;
import freemail.FreemailAccount;
import freemail.utils.PropsFile;

import junit.framework.TestCase;

public class IMAPHandlerTest extends TestCase {
	private static final String ACCOUNT_MANAGER_DIR = "account_manager_dir";
	private static final String ACCOUNT_DIR = "account_dir";

	private File accountManagerDir;
	private File accountDir;

	public void setUp() {
		// Set up account manager directory
		accountManagerDir = new File(ACCOUNT_MANAGER_DIR);
		if(accountManagerDir.exists()) {
			System.out.println("WARNING: Account manager directory exists, deleting");
			delete(accountManagerDir);
		}

		if(!accountManagerDir.mkdir()) {
			System.out.println("WARNING: Could not create account manager directory, tests will probably fail");
		}

		// Set up account directory
		accountDir = new File(ACCOUNT_DIR);
		if(accountDir.exists()) {
			System.out.println("WARNING: Account directory exists, deleting");
			delete(accountDir);
		}

		if(!accountDir.mkdir()) {
			System.out.println("WARNING: Could not create account directory, tests will probably fail");
		}
	}

	public void tearDown() {
		delete(accountManagerDir);
		delete(accountDir);
	}

	/**
	 * Deletes a File, including all its contents if it is a directory.
	 * Prints the path of any Files that can't be deleted to System.out
	 */
	private boolean delete(File file) {
		if(!file.exists()) {
			return true;
		}

		if(!file.isDirectory()) {
			if(!file.delete()) {
				System.out.println("Failed to delete " + file);
				return false;
			}
			return true;
		}

		for(File f : file.listFiles()) {
			if(!delete(f)) {
				return false;
			}
		}
		file.delete();

		return true;
	}

	public void testIMAPGreeting() throws IOException {
		FakeSocket sock = new FakeSocket();

		new Thread(new IMAPHandler(null, sock)).start();

		BufferedReader fromHandler = new BufferedReader(new InputStreamReader(sock.getInputStreamOtherSide()));

		assertEquals("* OK [CAPABILITY IMAP4rev1 CHILDREN NAMESPACE] Freemail ready - hit me with your rhythm stick.", fromHandler.readLine());
	}

	public void testIMAPLogin() throws IOException {
		FakeSocket sock = new FakeSocket();
		AccountManager accManager = new ConfigurableAccountManager(accountManagerDir, false);

		new Thread(new IMAPHandler(accManager, sock)).start();

		PrintWriter toHandler = new PrintWriter(sock.getOutputStreamOtherSide());
		BufferedReader fromHandler = new BufferedReader(new InputStreamReader(sock.getInputStreamOtherSide()));

		//Read the greeting
		String line = fromHandler.readLine();

		send(toHandler, "0001 LOGIN test test\r\n");

		line = fromHandler.readLine();
		assertEquals("0001 OK Logged in", line);

		assertFalse(fromHandler.ready());
	}

	public void testFailedIMAPLogin() throws IOException {
		FakeSocket sock = new FakeSocket();
		AccountManager accManager = new ConfigurableAccountManager(accountManagerDir, true);

		new Thread(new IMAPHandler(accManager, sock)).start();

		PrintWriter toHandler = new PrintWriter(sock.getOutputStreamOtherSide());
		BufferedReader fromHandler = new BufferedReader(new InputStreamReader(sock.getInputStreamOtherSide()));

		//Read the greeting
		String line = fromHandler.readLine();

		send(toHandler, "0001 LOGIN test test\r\n");

		line = readTaggedResponse(fromHandler);
		assertEquals("0001 NO Login failed", line);

		assertFalse(fromHandler.ready());
	}

	public void testIMAPSelect() throws IOException {
		FakeSocket sock = new FakeSocket();
		AccountManager accManager = new ConfigurableAccountManager(accountManagerDir, false);

		new Thread(new IMAPHandler(accManager, sock)).start();

		PrintWriter toHandler = new PrintWriter(sock.getOutputStreamOtherSide());
		BufferedReader fromHandler = new BufferedReader(new InputStreamReader(sock.getInputStreamOtherSide()));

		fromHandler.readLine(); //Greeting

		//Login
		send(toHandler, "0001 LOGIN test test\r\n");
		readTaggedResponse(fromHandler);

		send(toHandler, "0002 SELECT INBOX\r\n");
		String line = readTaggedResponse(fromHandler);
		assertEquals("0002 OK [READ-WRITE] Done", line);

		assertFalse(fromHandler.ready());
	}

	public void testIMAPSelectUnknown() throws IOException {
		FakeSocket sock = new FakeSocket();
		AccountManager accManager = new ConfigurableAccountManager(accountManagerDir, false);

		new Thread(new IMAPHandler(accManager, sock)).start();

		PrintWriter toHandler = new PrintWriter(sock.getOutputStreamOtherSide());
		BufferedReader fromHandler = new BufferedReader(new InputStreamReader(sock.getInputStreamOtherSide()));

		fromHandler.readLine(); //Greeting

		//Login
		send(toHandler, "0001 LOGIN test test\r\n");
		readTaggedResponse(fromHandler);

		send(toHandler, "0002 SELECT ShouldNotExist\r\n");
		String line = readTaggedResponse(fromHandler);
		assertEquals("0002 NO No such mailbox", line);

		assertFalse(fromHandler.ready());
	}

	private static void send(PrintWriter out, String msg) {
		out.print(msg);
		out.flush();
	}

	private static String readTaggedResponse(BufferedReader in) throws IOException {
		String line = in.readLine();
		while(line.startsWith("*")) {
			line = in.readLine();
		}
		return line;
	}

	private class ConfigurableAccountManager extends NullAccountManager {
		private boolean failAuth;

		public ConfigurableAccountManager(File datadir, boolean failAuth) {
			super(datadir);

			this.failAuth = failAuth;
		}

		public FreemailAccount authenticate(String username, String password) {
			if(failAuth) return null;

			//FreemailAccount constructor is package-protected and
			//there is no reason to change that, so use reflection
			//to construct a new account
			try {
				Class<FreemailAccount> freemailAccount = FreemailAccount.class;
				Constructor<FreemailAccount> constructor = freemailAccount.getDeclaredConstructor(String.class, File.class, PropsFile.class);
				constructor.setAccessible(true);
				return constructor.newInstance(username, accountDir, null);
			} catch (Exception e) {
				e.printStackTrace();
				fail(e.toString());
			}

			return null;
		}
	}
}

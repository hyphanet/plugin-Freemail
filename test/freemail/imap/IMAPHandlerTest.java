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
			if(!accountManagerDir.delete()) {
				System.out.println("WARNING: Could not remove account manager directory");
			}
		}

		if(!accountManagerDir.mkdir()) {
			System.out.println("WARNING: Could not create account manager directory, tests will probably fail");
		}

		// Set up account directory
		accountDir = new File(ACCOUNT_DIR);
		if(accountDir.exists()) {
			System.out.println("WARNING: Account directory exists, deleting");
			if(!accountDir.delete()) {
				System.out.println("WARNING: Could not remove account directory");
			}
		}

		if(!accountDir.mkdir()) {
			System.out.println("WARNING: Could not create account directory, tests will probably fail");
		}
	}

	public void tearDown() {
		if(!accountManagerDir.delete()) {
			System.out.println("WARNING: Could not remove account manager directory");
		}

		if(!accountDir.delete()) {
			System.out.println("WARNING: Could not remove account directory");
		}
	}

	public void testIMAPGreeting() throws IOException {
		FakeSocket sock = new FakeSocket();

		new Thread(new IMAPHandler(null, sock)).start();

		BufferedReader fromHandler = new BufferedReader(new InputStreamReader(sock.getInputStreamOtherSide()));

		assertEquals("* OK [CAPABILITY IMAP4rev1 CHILDREN NAMESPACE] Freemail ready - hit me with your rhythm stick.", fromHandler.readLine());
	}

	public void testIMAPLogin() throws IOException {
		FakeSocket sock = new FakeSocket();
		AccountManager accManager = new PremissiveAccountManager(accountManagerDir);

		new Thread(new IMAPHandler(accManager, sock)).start();

		PrintWriter toHandler = new PrintWriter(sock.getOutputStreamOtherSide());
		BufferedReader fromHandler = new BufferedReader(new InputStreamReader(sock.getInputStreamOtherSide()));

		//Read the greeting
		String line = fromHandler.readLine();

		toHandler.print("0001 LOGIN test test\r\n");
		toHandler.flush();

		line = fromHandler.readLine();
		assertEquals("0001 OK Logged in", line);
	}

	private class PremissiveAccountManager extends NullAccountManager {
		public PremissiveAccountManager(File datadir) {
			super(datadir);
		}

		public FreemailAccount authenticate(String username, String password) {
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

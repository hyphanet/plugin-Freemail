/*
 * IMAPTestBase.java
 * This file is part of Freemail
 * Copyright (C) 2011,2012 Martin Nyhus
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

package org.freenetproject.freemail.imap;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.freenetproject.freemail.AccountManager;
import org.freenetproject.freemail.imap.IMAPHandler;

import fakes.ConfigurableAccountManager;
import fakes.FakeSocket;

import junit.framework.TestCase;
import utils.Utils;

/**
 * Class that handles a lot of the setup needed by all the various IMAP tests.
 * Extend this and add the tests to the subclass.
 */
public abstract class IMAPTestBase extends TestCase {
	protected static final String BASE64_USERNAME = "D3MrAR-AVMqKJRjXnpKW2guW9z1mw5GZ9BB15mYVkVc";
	protected static final String BASE32_USERNAME = "b5zswai7ybkmvcrfddlz5euw3ifzn5z5m3bzdgpucb26mzqvsflq";
	protected static final String IMAP_USERNAME = "zidel@" + BASE32_USERNAME + ".freemail";

	private static final File TEST_DIR = new File("imaptest");
	private static final String ACCOUNT_MANAGER_DIR = "account_manager_dir";
	private static final String ACCOUNT_DIR = "account_dir";

	protected final Map<String, File> accountDirs = new HashMap<String, File>();
	protected File accountManagerDir;

	@Override
	public void setUp() {
		assertFalse(TEST_DIR.getAbsolutePath() + " exists", TEST_DIR.exists());
		assertTrue(TEST_DIR.mkdir());

		accountManagerDir = createDir(TEST_DIR, ACCOUNT_MANAGER_DIR);
		File accountDir = createDir(TEST_DIR, ACCOUNT_DIR);
		accountDirs.put(BASE64_USERNAME, accountDir);
	}

	private File createDir(File parent, String name) {
		File dir = new File(parent, name);
		assertFalse(dir + " already exists", dir.exists());
		assertTrue("Couldn't create " + dir, dir.mkdir());
		return dir;
	}

	@Override
	public void tearDown() {
		Utils.delete(TEST_DIR);
	}

	protected static void send(PrintWriter out, String msg) {
		out.print(msg);
		out.flush();
	}

	protected static String readTaggedResponse(BufferedReader in) throws IOException {
		String line = in.readLine();
		while(line.startsWith("*")) {
			line = in.readLine();
		}
		return line;
	}

	protected void runSimpleTest(List<String> commands, List<String> expectedResponse) throws IOException {
		FakeSocket sock = new FakeSocket();
		AccountManager accManager = new ConfigurableAccountManager(accountManagerDir, false, accountDirs);

		IMAPHandler handler = new IMAPHandler(accManager, sock);
		Thread imapThread = new Thread(handler);
		imapThread.start();

		PrintWriter toHandler = new PrintWriter(sock.getOutputStreamOtherSide());
		BufferedReader fromHandler = new BufferedReader(new InputStreamReader(sock.getInputStreamOtherSide()));

		for(String cmd : commands) {
			send(toHandler, cmd + "\r\n");
		}

		try {
			int lineNum = 0;
			for(String response : expectedResponse) {
				String line = fromHandler.readLine();
				assertEquals("Failed at line " + lineNum++, response, line);
			}

			assertFalse("IMAP socket has more data", fromHandler.ready());
		} finally {
			handler.kill();
			sock.close();
			try {
				imapThread.join();
			} catch(InterruptedException e) {
				fail("Caught unexpected InterruptedException");
			}
		}
	}
}

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

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;

import org.freenetproject.freemail.AccountManager;

import data.TestId1Data;

import fakes.ConfigurableAccountManager;
import fakes.FakeSocket;
import utils.TextProtocolTester;
import utils.TextProtocolTester.Command;
import utils.Utils;

/**
 * Class that handles a lot of the setup needed by all the various IMAP tests.
 * Extend this and add the tests to the subclass.
 */
public abstract class IMAPTestBase {
	protected static final String BASE64_USERNAME = TestId1Data.Identity.ID;
	protected static final String IMAP_USERNAME = TestId1Data.FreemailAccount.ADDRESS;

	private static final File TEST_DIR = new File("imaptest");
	private static final String ACCOUNT_MANAGER_DIR = "account_manager_dir";
	private static final String ACCOUNT_DIR = "account_dir";

	protected final Map<String, File> accountDirs = new HashMap<String, File>();
	protected File accountManagerDir;

	@Before
	public void before() {
		Utils.createDir(TEST_DIR);

		accountManagerDir = Utils.createDir(TEST_DIR, ACCOUNT_MANAGER_DIR);
		File accountDir = Utils.createDir(TEST_DIR, ACCOUNT_DIR);
		accountDirs.put(BASE64_USERNAME, accountDir);
	}

	@After
	public void after() {
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

	@Deprecated
	protected void runSimpleTest(List<String> commands, List<String> expectedResponse) throws IOException {
		List<Command> combined = new LinkedList<Command>();

		//Add all the commands first, then the replies, ensuring all the
		//commands will be sent before checking the replies
		for(String cmd : commands) {
			combined.add(new Command(cmd));
		}
		combined.add(new Command(null, expectedResponse));
		runSimpleTest(combined);
	}

	protected void runSimpleTest(List<Command> commands) throws IOException {
		FakeSocket sock = new FakeSocket();
		AccountManager accManager = new ConfigurableAccountManager(accountManagerDir, false, accountDirs);

		IMAPHandler handler = new IMAPHandler(accManager, sock);
		Thread imapThread = new Thread(handler);
		imapThread.start();

		try {
			PrintWriter toHandler = new PrintWriter(sock.getOutputStreamOtherSide());
			BufferedReader fromHandler = new BufferedReader(new InputStreamReader(sock.getInputStreamOtherSide()));
			TextProtocolTester tester = new TextProtocolTester(toHandler, fromHandler);
			tester.runProtocolTest(commands);
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

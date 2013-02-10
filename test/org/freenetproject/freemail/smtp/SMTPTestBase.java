/*
 * SMTPTestBase.java
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

package org.freenetproject.freemail.smtp;

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

import fakes.ConfigurableAccountManager;
import fakes.FakeSocket;
import fakes.NullIdentityMatcher;
import utils.TextProtocolTester;
import utils.Utils;
import utils.TextProtocolTester.Command;

/**
 * Class that handles a lot of the setup needed by all the various SMTP tests.
 * Extend this and add the tests to the subclass.
 */
public abstract class SMTPTestBase {
	//FIXME: Remove these in favor of TestId1Data
	protected static final String[] BASE64_USERNAMES = new String[] {
		"D3MrAR-AVMqKJRjXnpKW2guW9z1mw5GZ9BB15mYVkVc",
		"1unnefKX8TOorAm5-c0lc6BGT9-6kBucO5f6dLJ9EZA",
	};
	protected static final String BASE64_USERNAME = BASE64_USERNAMES[0];

	private static final File TEST_DIR = new File("smtptest");
	private static final String ACCOUNT_MANAGER_DIR = "account_manager_dir";

	private final Map<String, File> accountDirs = new HashMap<String, File>();
	private File accountManagerDir;

	@Before
	public void before() {
		Utils.createDir(TEST_DIR);

		accountManagerDir = Utils.createDir(TEST_DIR, ACCOUNT_MANAGER_DIR);

		for(int i = 0; i < BASE64_USERNAMES.length; i++) {
			File accountDir = Utils.createDir(TEST_DIR, "account_" + i + "_dir");
			accountDirs.put(BASE64_USERNAMES[i], accountDir);
		}
	}

	@After
	public void after() {
		Utils.delete(TEST_DIR);
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

		SMTPHandler handler = new SMTPHandler(accManager, sock, new NullIdentityMatcher());
		Thread smtpThread = new Thread(handler);
		smtpThread.start();

		try {
			PrintWriter toHandler = new PrintWriter(sock.getOutputStreamOtherSide());
			BufferedReader fromHandler = new BufferedReader(new InputStreamReader(sock.getInputStreamOtherSide()));
			TextProtocolTester tester = new TextProtocolTester(toHandler, fromHandler);
			tester.runProtocolTest(commands);
		} finally {
			handler.kill();
			sock.close();
			try {
				smtpThread.join();
			} catch(InterruptedException e) {
				fail("Caught unexpected InterruptedException");
			}
		}
	}
}

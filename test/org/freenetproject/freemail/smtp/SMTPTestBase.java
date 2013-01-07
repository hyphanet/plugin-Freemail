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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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

/**
 * Class that handles a lot of the setup needed by all the various SMTP tests.
 * Extend this and add the tests to the subclass.
 */
public abstract class SMTPTestBase {
	protected static final String BASE64_USERNAME = "D3MrAR-AVMqKJRjXnpKW2guW9z1mw5GZ9BB15mYVkVc";

	private static final File TEST_DIR = new File("smtptest");
	private static final String ACCOUNT_MANAGER_DIR = "account_manager_dir";
	private static final String ACCOUNT_DIR = "account_dir";

	private final Map<String, File> accountDirs = new HashMap<String, File>();
	private File accountManagerDir;

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

	protected void runSimpleTest(List<String> commands, List<String> expectedResponse) throws IOException {
		FakeSocket sock = new FakeSocket();
		AccountManager accManager = new ConfigurableAccountManager(accountManagerDir, false, accountDirs);

		SMTPHandler handler = new SMTPHandler(accManager, sock, new NullIdentityMatcher());
		Thread smtpThread = new Thread(handler);
		smtpThread.start();

		try {
			TextProtocolTester tester = new TextProtocolTester(sock);
			tester.runSimpleTest(commands, expectedResponse);
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

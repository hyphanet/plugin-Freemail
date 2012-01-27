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

package freemail.imap;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import utils.Utils;

/**
 * Class that handles a lot of the setup needed by all the various IMAP tests.
 * Extend this and add the tests to the subclass.
 */
public abstract class IMAPTestBase extends TestCase {
	protected static final String USERNAME = "test";

	private static final File TEST_DIR = new File("imaptest");
	private static final String ACCOUNT_MANAGER_DIR = "account_manager_dir";
	private static final String ACCOUNT_DIR = "account_dir";

	protected final Map<String, File> accountDirs = new HashMap<String, File>();
	protected File accountManagerDir;

	private File accountDir;

	@Override
	public void setUp() {
		if(TEST_DIR.exists()) {
			System.out.println("WARNING: Test directory exists, deleting");
			Utils.delete(TEST_DIR);
		}

		if(!TEST_DIR.mkdir()) {
			System.out.println("WARNING: Could not create test directory, tests will probably fail");
		}

		// Set up account manager directory
		accountManagerDir = new File(TEST_DIR, ACCOUNT_MANAGER_DIR);
		if(accountManagerDir.exists()) {
			System.out.println("WARNING: Account manager directory exists, deleting");
			Utils.delete(accountManagerDir);
		}

		if(!accountManagerDir.mkdir()) {
			System.out.println("WARNING: Could not create account manager directory, tests will probably fail");
		}

		// Set up account directory
		accountDir = new File(TEST_DIR, ACCOUNT_DIR);
		if(accountDir.exists()) {
			System.out.println("WARNING: Account directory exists, deleting");
			Utils.delete(accountDir);
		}

		if(!accountDir.mkdir()) {
			System.out.println("WARNING: Could not create account directory, tests will probably fail");
		}

		accountDirs.put(USERNAME, accountDir);
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

}

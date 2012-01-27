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

	@Override
	public void setUp() {
		accountManagerDir = createDir(TEST_DIR, ACCOUNT_MANAGER_DIR);
		File accountDir = createDir(TEST_DIR, ACCOUNT_DIR);
		accountDirs.put(USERNAME, accountDir);
	}

	private File createDir(File parent, String name) {
		File dir = new File(parent, name);
		if(dir.exists()) {
			System.out.println("WARNING: Directory (" + dir + ") exists, deleting");
			Utils.delete(dir);
		}

		if(!dir.mkdirs()) {
			System.out.println("WARNING: Could not create directory (" + dir + "), tests will probably fail");
		}

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

}

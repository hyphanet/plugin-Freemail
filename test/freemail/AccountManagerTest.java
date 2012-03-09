/*
 * AccountManagerTest.java
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

package freemail;

import java.io.File;
import java.io.IOException;

import utils.Utils;

import junit.framework.TestCase;

public class AccountManagerTest extends TestCase {
	private static final String BASE64_USERNAME = "D3MrAR-AVMqKJRjXnpKW2guW9z1mw5GZ9BB15mYVkVc";
	private static final String BASE32_USERNAME = "b5zswai7ybkmvcrfddlz5euw3ifzn5z5m3bzdgpucb26mzqvsflq";

	private File dataDir;

	@Override
	public void setUp() {
		dataDir = new File("data");
		if(dataDir.exists()) {
			System.out.println("WARNING: Account manager directory exists, deleting");
			Utils.delete(dataDir);
		}

		if(!dataDir.mkdir()) {
			System.out.println("WARNING: Could not create account manager directory, tests will probably fail");
		}
	}

	@Override
	public void tearDown() {
		Utils.delete(dataDir);
	}

	public void testAuthenticateSimpleUsername() throws IOException {
		// Creating accounts the real way doesn't work because there is no fcp connection to the
		// node, so we have to do it the hard way
		File accDir = new File(dataDir, BASE32_USERNAME);
		accDir.mkdir();
		File accProps = new File(accDir, AccountManager.ACCOUNT_FILE);
		accProps.createNewFile();

		AccountManager manager = new AccountManager(dataDir, null);
		FreemailAccount acc = manager.getAccount(BASE64_USERNAME);
		AccountManager.changePassword(acc, "test");

		assertNotNull(manager.authenticate(BASE64_USERNAME, "test"));
	}

	public void testAccountManager() {
		AccountManager manager = new AccountManager(dataDir, null);
		assertTrue(manager.getAllAccounts().isEmpty());
	}

	/*
	 * This checks for the bug fixed in commit a5fda3d0cd799d105447f7ff83361cd9600e80a0.
	 * AccountManager used two different methods for validating usernames, so accounts with - in the
	 * username could be created, but couldn't be authenticated.
	 */
	public void testAuthenticateUsernameWithMinus() throws IOException {
		final String ACCOUNT_PASSWORD = "test-user";

		// Creating accounts the real way doesn't work because there is no fcp connection to the
		// node, so we have to do it the hard way
		File accDir = new File(dataDir, BASE32_USERNAME);
		accDir.mkdir();
		File accProps = new File(accDir, AccountManager.ACCOUNT_FILE);
		accProps.createNewFile();

		AccountManager manager = new AccountManager(dataDir, null);
		FreemailAccount acc = manager.getAccount(BASE64_USERNAME);
		AccountManager.changePassword(acc, ACCOUNT_PASSWORD);

		assertNotNull(manager.authenticate(BASE64_USERNAME, ACCOUNT_PASSWORD));
	}
}

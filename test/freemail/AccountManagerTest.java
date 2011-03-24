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

import utils.Utils;

import junit.framework.TestCase;

public class AccountManagerTest extends TestCase {
	private File dataDir;

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

	public void tearDown() {
		Utils.delete(dataDir);
	}

	public void testUsernameValidation() {
		assertEquals("", AccountManager.validateUsername("testuser"));
		assertEquals("", AccountManager.validateUsername("test-user"));
	}

	public void testAccountManager() {
		AccountManager manager = new AccountManager(dataDir);
		assertTrue(manager.getAllAccounts().isEmpty());
	}
}

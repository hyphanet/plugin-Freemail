/*
 * MessageBankTest.java
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

public class MessageBankTest extends TestCase {
	private final static String ACCOUNT_DIR = "accdir";

	private File accountDir;
	private MessageBank rootMessageBank;

	public void setUp() {
		// Set up account directory
		accountDir = new File(ACCOUNT_DIR);
		if(accountDir.exists()) {
			System.out.println("WARNING: Account directory exists, deleting");
			Utils.delete(accountDir);
		}

		if(!accountDir.mkdir()) {
			System.out.println("WARNING: Could not create account directory, tests will probably fail");
		}

		//Create the root message bank
		rootMessageBank = new MessageBank(new FreemailAccount(null, accountDir, null));
	}

	public void tearDown() {
		Utils.delete(accountDir);
	}

	/*
	 * This checks for the bug fixed in commit 06452844154a11b605708eeb4fc7bd1756b47d7a.
	 * MessageBank would try to delete the shadow folder left behind when deleting a MessageBank
	 * which would fail because the folder wasn't empty.
	 */
	public void testDeleteFolderTree() {
		MessageBank subFolder = rootMessageBank.makeSubFolder("subfolder");
		MessageBank subSubFolder = subFolder.makeSubFolder("subsubfolder");

		//Make sure we use at least one UID in subSubFolder
		assertNotNull(subSubFolder.createMessage());

		//Deleting subSubFolder leaves behind .subsubfolder with the .nextuid file intact
		assertTrue(subSubFolder.delete());

		//This would fail because .subsubfolder couldn't be deleted
		assertTrue(subFolder.delete());
	}
}

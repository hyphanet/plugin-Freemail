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

package org.freenetproject.freemail;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.freenetproject.freemail.FreemailAccount;
import org.freenetproject.freemail.MessageBank;

import data.TestId1Data;

import utils.Utils;

public class MessageBankTest {
	private static final String ACCOUNT_DIR = "accdir";

	private File accountDir;
	private MessageBank rootMessageBank;

	@Before
	public void before() {
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
		rootMessageBank = new MessageBank(new FreemailAccount(TestId1Data.Identity.ID, accountDir, null, null));
	}

	@After
	public void after() {
		Utils.delete(accountDir);
	}

	/*
	 * This checks for the bug fixed in commit 06452844154a11b605708eeb4fc7bd1756b47d7a.
	 * MessageBank would try to delete the shadow folder left behind when deleting a MessageBank
	 * which would fail because the folder wasn't empty.
	 */
	@Test
	public void deleteFolderTree() {
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

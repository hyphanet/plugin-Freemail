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

package org.freenetproject.freemail;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Locale;
import org.freenetproject.freemail.wot.OwnIdentity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import data.TestId1Data;

import utils.Utils;

public class AccountManagerTest {
	private File dataDir;

	@Before
	public void before() {
		dataDir = new File("data");
		if(dataDir.exists()) {
			System.out.println("WARNING: Account manager directory exists, deleting");
			Utils.delete(dataDir);
		}

		if(!dataDir.mkdir()) {
			System.out.println("WARNING: Could not create account manager directory, tests will probably fail");
		}
	}

	@After
	public void after() {
		Utils.delete(dataDir);
	}

	@Test
	public void authenticateSimpleUsername() throws IOException {
		// Creating accounts the real way doesn't work because there is no fcp connection to the
		// node, so we have to do it the hard way
		File accDir = new File(dataDir, TestId1Data.BASE32_ID);
		accDir.mkdir();
		File accProps = new File(accDir, AccountManager.ACCOUNT_FILE);
		accProps.createNewFile();

		AccountManager manager = new AccountManager(dataDir, null);
		FreemailAccount acc = manager.getAccount(TestId1Data.BASE64_ID);
		AccountManager.changePassword(acc, "test");

		assertNotNull(manager.authenticate(TestId1Data.BASE64_ID, "test"));
	}

	@Test
	public void createAccountManager() {
		AccountManager manager = new AccountManager(dataDir, null);
		assertTrue(manager.getAllAccounts().isEmpty());
	}

	/*
	 * This checks for the bug fixed in commit a5fda3d0cd799d105447f7ff83361cd9600e80a0.
	 * AccountManager used two different methods for validating usernames, so accounts with - in the
	 * username could be created, but couldn't be authenticated.
	 */
	@Test
	public void authenticateUsernameWithMinus() throws IOException {
		final String ACCOUNT_PASSWORD = "test-user";

		// Creating accounts the real way doesn't work because there is no fcp connection to the
		// node, so we have to do it the hard way
		File accDir = new File(dataDir, TestId1Data.BASE32_ID);
		accDir.mkdir();
		File accProps = new File(accDir, AccountManager.ACCOUNT_FILE);
		accProps.createNewFile();

		AccountManager manager = new AccountManager(dataDir, null);
		FreemailAccount acc = manager.getAccount(TestId1Data.BASE64_ID);
		AccountManager.changePassword(acc, ACCOUNT_PASSWORD);

		assertNotNull(manager.authenticate(TestId1Data.BASE64_ID, ACCOUNT_PASSWORD));
	}

	@Test
	public void welcomeMailGeneratesRandomMessageId() throws IOException {
		var manager = new AccountManager(dataDir, null);
		var ownIdentity1 = new OwnIdentity("EcLVfft8UqOpQSLM6kjXfUmPuh4hFshNq84M~8EDmaE", "SSK@EcLVfft8UqOpQSLM6kjXfUmPuh4hFshNq84M~8EDmaE,Q7mrjNlzKkaUUdm8VSg5Ax90jOBwI10Ma3BIzSPCtm4,AQACAAE/", "SSK@UDjIgNFEXl-rdN8nIpMrx-C0kcogLL8rXi2m3ESfbJ8,Q7mrjNlzKkaUUdm8VSg5Ax90jOBwI10Ma3BIzSPCtm4,AQECAAE/", "OwnId");
		var ownIdentity2 = new OwnIdentity("ReRtZLTNVwUYdG-PTul2mmAPjQPkym7hRcm~wEk85I8", "SSK@ReRtZLTNVwUYdG-PTul2mmAPjQPkym7hRcm~wEk85I8,pNwknsSCUWKMXtja29kuUYza6~gRKYaBFw2-OWKSFyI,AQACAAE/", "SSK@aQzNb4xJEP2a3JGcDDUY~6WXYgf-THvtwt~KUQLVluk,pNwknsSCUWKMXtja29kuUYza6~gRKYaBFw2-OWKSFyI,AQECAAE/", "OwnId");
		Freemail.setRNG(new SecureRandom());
		manager.addIdentities(asList(ownIdentity1, ownIdentity2));
		var messageId1 = extractMessageIdFromFirstMessageInInbox(ownIdentity1.getBase32IdentityID());
		var messageId2 = extractMessageIdFromFirstMessageInInbox(ownIdentity2.getBase32IdentityID());
		assertThat(messageId1, not(equalTo(messageId2)));
	}

	private String extractMessageIdFromFirstMessageInInbox(String identity) throws IOException {
		var messagePath = dataDir.toPath().resolve(identity).resolve("inbox").resolve("1");
		var messageLines = Files.readAllLines(messagePath, UTF_8);
		return messageLines.stream().filter(line -> line.toLowerCase(Locale.ROOT).startsWith("message-id:")).findFirst().get().split(":", 2)[1].trim().replaceAll("(^<|>$)", "");
	}

}

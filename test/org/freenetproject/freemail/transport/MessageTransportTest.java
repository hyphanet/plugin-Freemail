/*
 * MessageTransportTest.java
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

package org.freenetproject.freemail.transport;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.freenetproject.freemail.utils.PropsFile;
import org.freenetproject.freemail.wot.Identity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import data.TestId1Data;

import fakes.MockFreemail;
import fakes.MockFreemailAccount;
import fakes.MockHighLevelFCPClient;
import fakes.MockHighLevelFCPClientFactory;
import fakes.MockIdentity;
import fakes.MockWoTConnection;
import fakes.MockHighLevelFCPClient.Insert;
import freenet.support.Logger;
import freenet.support.Logger.LogLevel;
import freenet.support.LoggerHook.InvalidThresholdException;
import freenet.support.api.Bucket;
import freenet.support.io.ArrayBucket;

import utils.UnitTestParameters;
import utils.Utils;

public class MessageTransportTest {
	/*
	 * Directory structure:
	 * smtptest/
	 *   account_manager_dir/
	 *     account_dir/
	 *       channels/
	 *       outbox/
	 */
	private final File testDir = new File("transport_test");
	private final File accountManagerDir = new File(testDir, "account_manager_dir");
	private final File accountDir = new File(accountManagerDir, "account_dir");
	private final File channelDir = new File(accountDir, "channels");
	private final File outboxDir = new File(accountDir, "outbox");

	@Before
	public void before() throws InvalidThresholdException {
		if(UnitTestParameters.VERBOSE) {
			Logger.setupStdoutLogging(LogLevel.MINIMAL, null);
		}

		Utils.createDir(testDir);
		Utils.createDir(accountManagerDir);
		Utils.createDir(accountDir);
		Utils.createDir(channelDir);
		Utils.createDir(outboxDir);
	}

	@After
	public void after() {
		Utils.delete(testDir);
	}

	@Test
	public void messageHandlerTest() throws IOException, InterruptedException, TimeoutException {
		//Set up the fake FCP client
		Map<String, File> fetchResults = new HashMap<String, File>();
		{
			File mailpage = new File(testDir, "mailsite");
			PrintWriter pw = new PrintWriter(mailpage);
			pw.write(TestId1Data.Mailsite.CONTENT);
			pw.close();
			fetchResults.put(TestId1Data.Mailsite.REQUEST_KEY, mailpage);
		}
		final MockHighLevelFCPClient fcpClient = new MockHighLevelFCPClient(fetchResults);

		//A factory for our HighLevelFCPClient
		final MockHighLevelFCPClientFactory hlFcpClientFactory = new MockHighLevelFCPClientFactory(fcpClient);

		//An identity for our user
		final MockIdentity id = new MockIdentity(TestId1Data.Identity.ID, TestId1Data.Identity.REQUEST_URI, TestId1Data.Identity.NICKNAME);

		//The WoT connection, seeded with our only id
		final MockWoTConnection wotConnection;
		{
			Map<String, Map<String, Identity>> identites = new HashMap<String, Map<String, Identity>>();
			Map<String, Identity> ids = new HashMap<String, Identity>();
			ids.put(TestId1Data.Identity.ID, id);
			identites.put(TestId1Data.Identity.ID, ids);

			Map<String, Map<String, String>> properties = new HashMap<String, Map<String, String>>();
			Map<String, String> props = new HashMap<String, String>();
			props.put("Freemail.mailsite", TestId1Data.Mailsite.EDITION + "");
			properties.put(TestId1Data.Identity.ID, props);

			wotConnection = new MockWoTConnection(identites, properties);
		}

		//A fake Freemail object
		final MockFreemail freemail = new MockFreemail(testDir.getAbsolutePath() + "/config", wotConnection);

		//Then a fake Freemail account
		final MockFreemailAccount account;
		{
			File accProps = new File(accountDir, "accprops");
			PrintWriter pw = new PrintWriter(accProps);
			pw.write(TestId1Data.FreemailAccount.ACCPROPS_CONTENT);
			pw.close();
			account = new MockFreemailAccount(TestId1Data.FreemailAccount.IDENTITY, accountDir, PropsFile.createPropsFile(accProps), freemail);
		}

		MessageHandler handler = new MessageHandler(outboxDir, freemail, channelDir, account, hlFcpClientFactory);

		//Now send the actual message
		List<Identity> recipients = new ArrayList<Identity>(1);
		for(int i = 0; i < 1; i++) {
			recipients.add(new MockIdentity(TestId1Data.Identity.ID, TestId1Data.Identity.REQUEST_URI, TestId1Data.Identity.NICKNAME));
		}
		final String msg =
				  "Subject: Test message\r\n"
				+ "\r\n"
				+ "Test message\r\n";
		Bucket message = new ArrayBucket(msg.getBytes("UTF-8"));

		handler.sendMessage(recipients, message);

		//First we wait for the mailsite fetch request
		fcpClient.awaitFetch(TestId1Data.Mailsite.REQUEST_KEY, 1, TimeUnit.MINUTES);

		//Then the RTS key insert
		fcpClient.awaitInsert(TestId1Data.RTSKEY + "-1", 1, TimeUnit.MINUTES);

		//Then an insert of any key, which should be the message. Since we don't bother to decrypt
		//the RTS we don't actually know which key this is inserted to.
		Insert i = fcpClient.awaitInsert(null, 1, TimeUnit.MINUTES);
		assertEquals(new String(i.data, "UTF-8"),
				  "messagetype=message\r\n"
				+ "id=0\r\n"
				+ "\r\n"
				+ msg);
	}
}

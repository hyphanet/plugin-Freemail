/*
 * IMAPFetchTest.java
 * This file is part of Freemail, copyright (C) 2012 Martin Nyhus
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

package org.freenetproject.freemail.imap;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import org.freenetproject.freemail.AccountManager;

import fakes.ConfigurableAccountManager;
import fakes.FakeSocket;

/**
 * Contains regression tests for locale dependent bugs that have been found in the IMAP handler code.
 */
@RunWith(value = Parameterized.class)
public class IMAPLocaleDependentTest extends IMAPTestWithMessages {
	@Parameters
	public static List<Locale[]> data() {
		List<Locale[]> list = new LinkedList<Locale[]>();

		for(Locale l : Locale.getAvailableLocales()) {
			list.add(new Locale[] {l});
		}

		return list;
	}

	private final Locale locale;
	private final Locale defaultLocale;

	public IMAPLocaleDependentTest(Locale locale) {
		this.locale = locale;
		this.defaultLocale = Locale.getDefault();
	}

	@Before
	@Override
	public void before() {
		super.before();
		Locale.setDefault(locale);
	}

	@After
	@Override
	public void after() {
		try {
			Locale.setDefault(defaultLocale);
		} finally {
			super.after();
		}
	}

	/**
	 * Tests that the IMAP server returns an internaldate with the correct format. The actual value
	 * of the returned date isn't checked.
	 * @throws IOException on I/O error, should never happen
	 * @throws ParseException on test failure
	 */
	@Test
	public void internaldateFormat() throws IOException, ParseException {
		//Setup
		FakeSocket sock = new FakeSocket();
		AccountManager accManager = new ConfigurableAccountManager(accountManagerDir, false, accountDirs);

		new Thread(new IMAPHandler(accManager, sock)).start();

		PrintWriter toHandler = new PrintWriter(sock.getOutputStreamOtherSide());
		BufferedReader fromHandler = new BufferedReader(new InputStreamReader(sock.getInputStreamOtherSide()));

		//Send commands
		send(toHandler, "0001 LOGIN " + IMAP_USERNAME + " test\r\n");
		send(toHandler, "0002 SELECT INBOX\r\n");
		send(toHandler, "0003 FETCH 1 (INTERNALDATE)\r\n");

		//Read all the initial responses
		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.addAll(INITIAL_RESPONSES);
		int lineNum = 0;
		for(String response : expectedResponse) {
			String line = fromHandler.readLine();
			assertEquals("[locale=" + locale + "] Failed at line " + lineNum++, response, line);
		}

		//Read and parse the INTERNALDATE line which should be of the form:
		//* 1 FETCH (INTERNALDATE "dd MMM yyyy HH:mm:ss Z")
		String line = fromHandler.readLine();
		String[] parts = line.split("\"");
		assertEquals("[locale=" + locale + "] " + line, 3, parts.length);
		String date = parts[1];
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z", Locale.ROOT);
		sdf.parse(date);

		//Read final line of expected output
		line = fromHandler.readLine();
		assertEquals("[locale=" + locale + "] Incorrect final output", "0003 OK Fetch completed", line);

		assertFalse("[locale=" + locale + "] IMAP socket has more data", fromHandler.ready());
		fromHandler.close();
		toHandler.close();
	}
}

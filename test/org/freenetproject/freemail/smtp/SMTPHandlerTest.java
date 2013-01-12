/*
 * SMTPHandlerTest.java
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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.bouncycastle.util.encoders.Base64;
import org.junit.Test;

public class SMTPHandlerTest extends SMTPTestBase {
	@Test
	public void checkHeloReply() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("HELO");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("220 localhost ready");
		expectedResponse.add("250 localhost");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void checkEhloReply() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("EHLO");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("220 localhost ready");
		expectedResponse.add("250-localhost");
		expectedResponse.add("250 AUTH LOGIN PLAIN");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void checkRejectsTurn() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("TURN");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("220 localhost ready");
		expectedResponse.add("502 No");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void mailAfterAuth() throws IOException {
		List<String> commands = new LinkedList<String>();
		final String authData = new String(Base64.encode((BASE64_USERNAME + "\0nouser\0password").getBytes("ASCII")), "ASCII");
		commands.add("AUTH PLAIN " + authData);
		commands.add("MAIL");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("220 localhost ready");
		expectedResponse.add("235 Authenticated");
		expectedResponse.add("250 OK");

		runSimpleTest(commands, expectedResponse);
	}
}

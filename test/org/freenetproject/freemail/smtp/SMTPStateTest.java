/*
 * SMTPStateTest.java
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

public class SMTPStateTest extends SMTPTestBase {
	@Test
	public void mailWithoutAuthentication() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("MAIL");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("220 localhost ready");
		expectedResponse.add("530 Authentication required");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void rcptWithoutAuthentication() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("RCPT arg1");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("220 localhost ready");
		expectedResponse.add("530 Authentication required");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void dataWithoutAuthentication() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("DATA");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("220 localhost ready");
		expectedResponse.add("530 Authentication required");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void dataWithoutRcpt() throws IOException {
		final String username = BASE64_USERNAME;
		final String password = "password";
		final byte[] concatedBytes = ("\0" + username + "\0" + password).getBytes("ASCII");
		final String encoded = new String(Base64.encode(concatedBytes), "ASCII");

		List<String> commands = new LinkedList<String>();
		commands.add("AUTH PLAIN " + encoded);
		commands.add("DATA");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("220 localhost ready");
		expectedResponse.add("235 Authenticated");
		expectedResponse.add("503 RCPT first");

		runSimpleTest(commands, expectedResponse);
	}
}

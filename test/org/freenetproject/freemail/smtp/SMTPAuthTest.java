/*
 * SMTPAuthTest.java
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

public class SMTPAuthTest extends SMTPTestBase {
	/* *************************************************** *
	 * Tests that don't work with any specific auth method *
	 * *************************************************** */
	@Test
	public void authWithoutType() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("AUTH");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("220 localhost ready");
		expectedResponse.add("504 No auth type given");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void rejectsInvalidAuthMethod() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("AUTH Unsupported");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("220 localhost ready");
		expectedResponse.add("504 Auth type unimplemented - weren't you listening?");

		runSimpleTest(commands, expectedResponse);
	}


	/* ****************************************** *
	 * Tests that work with the plain auth method *
	 * ****************************************** */
	@Test
	public void correctAuthPlainNoInitial() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("AUTH PLAIN");

		final String authData = new String(Base64.encode((BASE64_USERNAME + "\0" + BASE64_USERNAME + "\0password").getBytes("ASCII")), "ASCII");
		commands.add(authData);

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("220 localhost ready");
		expectedResponse.add("334 ");
		expectedResponse.add("235 Authenticated");

		runSimpleTest(commands, expectedResponse);
	}

	@Test
	public void correctAuthPlainInitial() throws IOException {
		List<String> commands = new LinkedList<String>();

		final String authData = new String(Base64.encode((BASE64_USERNAME + "\0" + BASE64_USERNAME + "\0password").getBytes("ASCII")), "ASCII");
		commands.add("AUTH PLAIN " + authData);

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("220 localhost ready");
		expectedResponse.add("235 Authenticated");

		runSimpleTest(commands, expectedResponse);
	}


	/* ****************************************** *
	 * Tests that work with the login auth method *
	 * ****************************************** */
	@Test
	public void correctAuthLogin() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("AUTH LOGIN");
		commands.add(new String(Base64.encode(BASE64_USERNAME.getBytes("ASCII")), "ASCII"));
		commands.add(new String(Base64.encode("password".getBytes("ASCII")), "ASCII"));

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("220 localhost ready");
		expectedResponse.add("334 " + new String(Base64.encode("Username:".getBytes("ASCII")), "ASCII"));
		expectedResponse.add("334 " + new String(Base64.encode("Password:".getBytes("ASCII")), "ASCII"));
		expectedResponse.add("235 Authenticated");

		runSimpleTest(commands, expectedResponse);
	}
}

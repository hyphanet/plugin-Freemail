/*
 * BadSMTPAuthTest.java
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

/**
 * All tests in this class fail because the smtp code can't handle certain
 * corner cases of the spec.
 */
public class BadSMTPAuthTest extends SMTPTestBase {
	/* *************************************************** *
	 * Tests that don't work with any specific auth method *
	 * *************************************************** */

	/**
	 * Checks that the server rejects the auth command after the client has already been authenticated.
	 * @see <a href="https://tools.ietf.org/html/rfc4954#section-4">RFC4954 Section 4</a>
	 */
	@Test
	public void rejectsSecondAuth() throws IOException {
		final String authData = new String(Base64.encode(("\0" + BASE64_USERNAME + "\0password").getBytes("ASCII")), "ASCII");

		List<String> commands = new LinkedList<String>();
		commands.add("AUTH PLAIN " + authData);
		commands.add("AUTH PLAIN " + authData);

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("220 localhost ready");
		expectedResponse.add("235 Authenticated");
		expectedResponse.add("503 Already authenticated");

		runSimpleTest(commands, expectedResponse);
	}

	/**
	 * Tests case where:
	 *   * authzid is valid
	 *   * authcid is invalid
	 *   * passwd is valid (for authzid)
	 *
	 * This should fail since
	 *   1. Freemail doesn't support authenticating as one user while authorizing (acting) as another
	 *   2. Authenticating user is invalid
	 */
	@Test
	public void plainAuthWithValidAuthzidInValidAuthcid() throws IOException {
		String authData = new String(Base64.encode((BASE64_USERNAME + "\0nosuchuser\0password").getBytes("ASCII")), "ASCII");

		List<String> commands = new LinkedList<String>();
		commands.add("AUTH PLAIN " + authData);

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("220 localhost ready");
		expectedResponse.add("535 Authentication failed");

		runSimpleTest(commands, expectedResponse);
	}

	/**
	 * Tests case where:
	 *   * authzid is valid
	 *   * authcid is valid (but != authzid)
	 *   * passwd is valid (for both user)
	 *
	 * This should fail since
	 *   1. Freemail doesn't support authenticating as one user while authorizing (acting) as another
	 *   2. Password is invalid
	 */
	@Test
	public void plainAuthTwoUsersValidPassword() throws IOException {
		String authData = new String(Base64.encode((BASE64_USERNAMES[0] + "\0" + BASE64_USERNAMES[1] + "\0password").getBytes("ASCII")), "ASCII");

		List<String> commands = new LinkedList<String>();
		commands.add("AUTH PLAIN " + authData);

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("220 localhost ready");
		expectedResponse.add("535 Authentication failed");

		runSimpleTest(commands, expectedResponse);
	}

	/**
	 * Checks that the server handles receiving AUTH PLAIN data with only the username
	 */
	@Test
	public void plainAuthOnlyUsername() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("AUTH PLAIN " + new String(Base64.encode((BASE64_USERNAME).getBytes("ASCII")), "ASCII"));

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("220 localhost ready");
		expectedResponse.add("535 No username/password received");

		runSimpleTest(commands, expectedResponse);
	}

	/**
	 * Checks the server response when the client cancels an auth plain exchange instead of sending the data
	 */
	@Test
	public void clientCancelsPlainAuthAfterChallenge() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("AUTH PLAIN");
		commands.add("*");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("220 localhost ready");
		expectedResponse.add("334 ");
		expectedResponse.add("501 Authentication canceled");

		runSimpleTest(commands, expectedResponse);
	}
}

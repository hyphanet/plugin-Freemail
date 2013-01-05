/*
 * IMAPStateCheckTest.java
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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class IMAPStateCheckTest extends IMAPTestWithMessages {
	/* Append is valid in authenticated and selected states */
	@Test
	public void append() throws IOException {
		assertFailsWithoutLogin("APPEND arg1 arg2");
	}

	/* Check is only valid in selected state */
	@Test
	public void check() throws IOException {
		assertFailsWithoutLogin("CHECK");
		assertFailsWithoutSelect("CHECK");
	}

	/* Close is only valid in selected state */
	@Test
	public void close() throws IOException {
		assertFailsWithoutLogin("CLOSE");
		assertFailsWithoutSelect("CLOSE");
	}

	/* Copy is only valid in selected state */
	@Test
	public void copy() throws IOException {
		assertFailsWithoutLogin("COPY");
		assertFailsWithoutSelect("COPY");
	}

	/* Create is valid in authenticated and selected states */
	@Test
	public void create() throws IOException {
		assertFailsWithoutLogin("CREATE");
	}

	/* Delete is valid in authenticated and selected states */
	@Test
	public void delete() throws IOException {
		assertFailsWithoutLogin("DELETE");
	}

	/* Expunge is valid in selected state */
	@Test
	public void expunge() throws IOException {
		assertFailsWithoutLogin("EXPUNGE");
		assertFailsWithoutSelect("EXPUNGE");
	}

	/* Fetch is valid in selected state */
	@Test
	public void fetch() throws IOException {
		assertFailsWithoutLogin("FETCH");
		assertFailsWithoutSelect("FETCH");
	}

	/* List is valid in authenticated and selected states */
	@Test
	public void list() throws IOException {
		assertFailsWithoutLogin("LIST");
	}

	/* Lsub is valid in authenticated and selected states */
	@Test
	public void lsub() throws IOException {
		assertFailsWithoutLogin("LSUB");
	}

	/* Namespace is valid in authenticated and selected states */
	@Test
	public void namespace() throws IOException {
		assertFailsWithoutLogin("NAMESPACE");
	}

	/* Select is valid in authenticated and selected states */
	@Test
	public void select() throws IOException {
		assertFailsWithoutLogin("SELECT");
	}

	/* Status is valid in authenticated and selected states */
	@Test
	public void status() throws IOException {
		assertFailsWithoutLogin("STATUS");
	}

	/* Store is valid in selected state */
	@Test
	public void store() throws IOException {
		assertFailsWithoutLogin("STORE arg1 arg2");
		assertFailsWithoutSelect("STORE arg1 arg2");
	}

	/* UID is valid in selected state */
	@Test
	public void uid() throws IOException {
		assertFailsWithoutLogin("UID arg1 arg2 arg3");
		assertFailsWithoutSelect("UID arg1 arg2 arg3");
	}

	private void assertFailsWithoutLogin(String cmd) throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 " + cmd);

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("* OK [CAPABILITY IMAP4rev1 CHILDREN NAMESPACE] Freemail ready - hit me with your rhythm stick.");
		expectedResponse.add("0001 NO Must be authenticated");

		runSimpleTest(commands, expectedResponse);
	}

	private void assertFailsWithoutSelect(String cmd) throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("0001 LOGIN " + IMAP_USERNAME + " test");
		commands.add("0002 " + cmd);

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("* OK [CAPABILITY IMAP4rev1 CHILDREN NAMESPACE] Freemail ready - hit me with your rhythm stick.");
		expectedResponse.add("0001 OK Logged in");
		expectedResponse.add("0002 NO No mailbox selected");

		runSimpleTest(commands, expectedResponse);
	}
}

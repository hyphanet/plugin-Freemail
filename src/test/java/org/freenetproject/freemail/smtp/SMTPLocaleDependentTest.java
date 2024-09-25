/*
 * SMTPLocaleDependentTest.java
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
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import utils.LocaleDependentTest;

/**
 * Contains regression tests for locale dependent bugs that have been found in the SMTP code.
 */
@RunWith(value = Parameterized.class)
public class SMTPLocaleDependentTest extends SMTPTestBase {
	private final LocaleDependentTest localeDependentTest;

	@Parameters
	public static List<Locale[]> data() {
		List<Locale[]> data = new LinkedList<Locale[]>();
		for(Locale l : LocaleDependentTest.data()) {
			data.add(new Locale[] {l});
		}
		return data;
	}

	public SMTPLocaleDependentTest(Locale locale) {
		this.localeDependentTest = new LocaleDependentTest(locale);
	}

	@Before
	@Override
	public void before() {
		super.before();
		localeDependentTest.before();
	}

	@After
	@Override
	public void after() {
		try {
			localeDependentTest.after();
		} finally {
			super.after();
		}
	}

	/**
	 * Checks that the mail command is handled correctly. This failed earlier when running with the
	 * Turkish locale because of locale dependent string comparisons and the dotless i.
	 */
	@Test
	public void checkMailCommandRecognized() throws IOException {
		List<String> commands = new LinkedList<String>();
		commands.add("MAIL");

		List<String> expectedResponse = new LinkedList<String>();
		expectedResponse.add("220 localhost ready");
		expectedResponse.add("530 Authentication required");

		runSimpleTest(commands, expectedResponse);
	}
}

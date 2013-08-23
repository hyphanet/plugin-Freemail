/*
 * MailHeaderFilter.java
 * This file is part of Freemail, Copyright (C) 2012
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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import data.TestId1Data;
import data.TestId2Data;

public class MailHeaderFilterTest {
	private static FreemailAccount sender;

	@Before
	public void before() {
		sender = new FreemailAccount(TestId1Data.BASE64_ID, null, null, null);
	}

	@Test
	public void filteringOfWhitelistedHeader() throws IOException {
		List<String> input = new LinkedList<String>();
		input.add("Subject: Test message");

		List<String> output = new LinkedList<String>();
		output.add("Subject: Test message");

		runSimpleTest(input, output);
	}

	@Test
	public void filteringOfBlacklistedHeader() throws IOException {
		List<String> input = new LinkedList<String>();
		input.add("BCC: local@domain.freemail");

		List<String> output = new LinkedList<String>();

		runSimpleTest(input, output);
	}

	@Test
	public void dateWithCorrectFormat() throws IOException {
		List<String> input = new LinkedList<String>();
		input.add("Date: Tue, 10 Jul 2012 16:37:19 +0000");

		List<String> output = new LinkedList<String>();
		output.add("Date: Tue, 10 Jul 2012 16:37:19 +0000");

		runSimpleTest(input, output);
	}

	@Test
	public void dateWithMissingDay() throws IOException {
		List<String> input = new LinkedList<String>();
		input.add("Date: 10 Jul 2012 16:37:19 +0000");

		List<String> output = new LinkedList<String>();
		output.add("Date: Tue, 10 Jul 2012 16:37:19 +0000");

		runSimpleTest(input, output);
	}

	@Test
	public void dateHeaderWithTimezone() throws IOException {
		List<String> input = new LinkedList<String>();
		input.add("Date: Tue, 10 Jul 2012 16:37:19 +0200");

		List<String> output = new LinkedList<String>();
		output.add("Date: Tue, 10 Jul 2012 14:37:19 +0000");

		runSimpleTest(input, output);
	}

	@Test
	public void invalidDateHeader() throws IOException {
		List<String> input = new LinkedList<String>();
		input.add("Date: Thu, 10 Juli 2012 16:37:19 +0200");

		List<String> output = new LinkedList<String>();

		runSimpleTest(input, output);
	}

	@Test
	public void multilineReferencesHeader() throws IOException {
		List<String> input = new LinkedList<String>();
		input.add("References:");
		input.add(" <message-id1@domain.freemail>");
		input.add(" <message-id2@domain.freemail>");

		List<String> output = new LinkedList<String>();
		output.add("References: <message-id1@domain.freemail>\r\n"
				+ " <message-id2@domain.freemail>");

		runSimpleTest(input, output);
	}

	@Test
	public void messageIdWithFreemailDomain() throws IOException {
		List<String> input = new LinkedList<String>();
		input.add("Message-ID: <message-id1@domain.freemail>");

		List<String> output = new LinkedList<String>();
		output.add("Message-ID: <message-id1@domain.freemail>");

		runSimpleTest(input, output);
	}

	@Test
	public void messageIdWithRealDomain() throws IOException {
		List<String> input = new LinkedList<String>();
		input.add("Message-ID: <20130225001826.78db4f15@example.com>");

		List<Pattern> output = new LinkedList<Pattern>();

		//The message id should have been replaced with a completely new one
		output.add(Pattern.compile("Message-ID: <-?[0-9]+\\.-?[0-9]+@"
				+ TestId1Data.BASE32_ID.toLowerCase(Locale.ROOT) + "\\.freemail>"));

		runRegexTest(input, output);
	}

	/**
	 * Checks that a From header with a non-Freemail address is filtered. Related to bug 5834.
	 */
	@Test
	public void nonFreemailFromAddress() throws IOException {
		List<String> input = new LinkedList<String>();
		input.add("From: example@example.com");

		List<String> output = new LinkedList<String>();
		output.add("From: null@" + TestId1Data.BASE32_ID.toLowerCase(Locale.ROOT) + ".freemail");

		runSimpleTest(input, output);
	}

	@Test
	public void invalidFromAddress() throws IOException {
		List<String> input = new LinkedList<String>();
		input.add("From: example.com");

		List<String> output = new LinkedList<String>();
		output.add("From: null@" + TestId1Data.BASE32_ID.toLowerCase(Locale.ROOT) + ".freemail");

		runSimpleTest(input, output);
	}

	@Test
	public void fromAddressWrongDomain() throws IOException {
		List<String> input = new LinkedList<String>();
		input.add("From: " + TestId2Data.FreemailAccount.ADDRESS);

		List<String> output = new LinkedList<String>();
		output.add("From: null@" + TestId1Data.BASE32_ID.toLowerCase(Locale.ROOT) + ".freemail");

		runSimpleTest(input, output);
	}

	@Test
	public void fromAddressUppercaseDomain() throws IOException {
		List<String> input = new LinkedList<String>();
		input.add("From: " + TestId1Data.Identity.NICKNAME + "@" + TestId1Data.BASE32_ID.toUpperCase(Locale.ROOT) + ".freemail");

		List<String> output = new LinkedList<String>();
		output.add("From: " + TestId1Data.Identity.NICKNAME + "@" + TestId1Data.BASE32_ID.toUpperCase(Locale.ROOT) + ".freemail");

		runSimpleTest(input, output);
	}

	@Test
	public void fromAddressIgnoresLocalPart() throws IOException {
		List<String> input = new LinkedList<String>();
		input.add("From: garbage@" + TestId1Data.BASE32_ID + ".freemail");

		List<String> output = new LinkedList<String>();
		output.add("From: garbage@" + TestId1Data.BASE32_ID + ".freemail");

		runSimpleTest(input, output);
	}

	private void runSimpleTest(List<String> inputLines, List<String> outputLines) throws IOException {
		MailHeaderFilter filter = setupFilter(inputLines);
		for(String header : outputLines) {
			String filteredLine = filter.readHeader();
			if(filteredLine == null) {
				fail("Reached end of filtered headers before end of output lines");
			}

			assertEquals(header, filteredLine);
		}

		String filteredLine = filter.readHeader();
		if(filteredLine != null) {
			System.err.println("More filtered lines left after output ended:");
			while(filteredLine != null) {
				System.err.println(filteredLine);
				filteredLine = filter.readHeader();
			}
			fail("Output lines ended before filtered lines");
		}
	}

	private void runRegexTest(List<String> inputLines, List<Pattern> outputExpressions) throws IOException {
		MailHeaderFilter filter = setupFilter(inputLines);
		for(Pattern regex : outputExpressions) {
			String filteredLine = filter.readHeader();
			if(filteredLine == null) {
				fail("Reached end of filtered headers before end of output lines");
			}

			assertTrue("Filter output [" + filteredLine + "] didn't match " + regex, regex.matcher(filteredLine).matches());
		}

		String filteredLine = filter.readHeader();
		if(filteredLine != null) {
			System.err.println("More filtered lines left after output ended:");
			while(filteredLine != null) {
				System.err.println(filteredLine);
				filteredLine = filter.readHeader();
			}
			fail("Output lines ended before filtered lines");
		}
	}

	private MailHeaderFilter setupFilter(List<String> inputLines) throws IOException {
		StringBuilder inputBuilder = new StringBuilder();
		for(String input : inputLines) {
			inputBuilder.append(input + "\r\n");
		}
		inputBuilder.append("\r\n");

		byte[] data = inputBuilder.toString().getBytes("UTF-8");
		ByteArrayInputStream is = new ByteArrayInputStream(data);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		return new MailHeaderFilter(reader, sender);
	}
}

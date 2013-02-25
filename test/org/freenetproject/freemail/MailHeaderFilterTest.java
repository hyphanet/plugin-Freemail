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

import org.junit.Test;

public class MailHeaderFilterTest {
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

	private MailHeaderFilter setupFilter(List<String> inputLines) throws IOException {
		StringBuilder inputBuilder = new StringBuilder();
		for(String input : inputLines) {
			inputBuilder.append(input + "\r\n");
		}
		inputBuilder.append("\r\n");

		byte[] data = inputBuilder.toString().getBytes("UTF-8");
		ByteArrayInputStream is = new ByteArrayInputStream(data);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		return new MailHeaderFilter(reader, "domain.freemail");
	}
}

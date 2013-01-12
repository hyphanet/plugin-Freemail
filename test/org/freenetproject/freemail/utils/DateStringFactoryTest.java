/*
 * DateStringFactoryTest.java
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

package org.freenetproject.freemail.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import junit.framework.TestCase;

public class DateStringFactoryTest extends TestCase {
	public void testOffsetKeyString() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.ROOT);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		String s = DateStringFactory.getOffsetKeyString(0);

		Date actual = sdf.parse(s);
		Date expected = sdf.parse(sdf.format(new Date()));

		if(expected.getTime() - actual.getTime() > 60 * 60 * 1000) {
			fail("Difference between expected and actual dates was more than 1 hour. Expected " + expected + ", was "
			     + actual);
		}
	}

	public void testOffsetKeyStringWithFrenchLocale() throws ParseException {
		Locale orig = Locale.getDefault();
		try {
			Locale.setDefault(Locale.FRENCH);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.ROOT);
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			String s = DateStringFactory.getOffsetKeyString(0);

			Date actual = sdf.parse(s);
			Date expected = sdf.parse(sdf.format(new Date()));

			if(expected.getTime() - actual.getTime() > 60 * 60 * 1000) {
				fail("Difference between expected and actual dates was more than 1 hour. Expected " + expected + ", was "
				     + actual);
			}
		} finally {
			Locale.setDefault(orig);
		}
	}
}

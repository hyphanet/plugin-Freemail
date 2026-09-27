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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;

public class DateStringFactoryTest {
	@Test
	public void offsetKeyString() throws ParseException {
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

	@Test
	public void offsetKeyStringWithFrenchLocale() throws ParseException {
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

	@Test
	public void fullDateIsCreatedCorrectly() {
		var dateTime = OffsetDateTime.of(2026, 9, 24, 20, 27, 45, 0, ZoneOffset.ofHours(0));
		assertThat(DateStringFactory.formatFullDate(dateTime), equalTo("Thu, 24 Sep 2026 20:27:45 +0000"));
	}

	@Test
	public void fullDateWithSingleDigitDayIsCreatedCorrectly() {
		var dateTime = OffsetDateTime.of(2015, 10, 1, 18, 56, 16, 0, ZoneOffset.ofHours(2));
		assertThat(DateStringFactory.formatFullDate(dateTime), equalTo("Thu, 1 Oct 2015 16:56:16 +0000"));
	}

	@Test
	public void fullDateRemovesTimezoneOffset() {
		var dateTime = OffsetDateTime.of(2026, 9, 24, 20, 27, 45, 0, ZoneOffset.ofHours(2));
		assertThat(DateStringFactory.formatFullDate(dateTime), equalTo("Thu, 24 Sep 2026 18:27:45 +0000"));
	}

	@Test
	public void fullDateCanBeCreatedFromZonedDateTime() {
		var dateTime = ZonedDateTime.of(2026, 9, 24, 20, 27, 45, 0, ZoneOffset.ofHours(2));
		assertThat(DateStringFactory.formatFullDate(dateTime), equalTo("Thu, 24 Sep 2026 18:27:45 +0000"));
	}

	@Test
	public void fullDateIsParsedCorrectly() {
		var parsedDateTime = DateStringFactory.parseFullDate("Thu, 24 Sep 2026 20:27:45 +0200").get();
		assertThat(parsedDateTime, equalTo(OffsetDateTime.of(2026, 9, 24, 20, 27, 45, 0, ZoneOffset.ofHours(2))));
	}

	@Test
	public void fullDateWithSingleDigitDayIsParsedCorrectly() {
		var parsedDateTime = DateStringFactory.parseFullDate("Thu, 1 Oct 2015 18:56:16 +0200").get();
		assertThat(parsedDateTime, equalTo(OffsetDateTime.of(2015, 10, 1, 18, 56, 16, 0, ZoneOffset.ofHours(2))));
	}

	@Test
	public void fullDateWithoutWeekdayIsParsedCorrectly() {
		var parsedDateTime = DateStringFactory.parseFullDate("24 Sep 2026 20:27:45 +0200").get();
		assertThat(parsedDateTime, equalTo(OffsetDateTime.of(2026, 9, 24, 20, 27, 45, 0, ZoneOffset.ofHours(2))));
	}

	@Test
	public void invalidFullDateReturnsEmptyOptional() {
		var parsedDateTime = DateStringFactory.parseFullDate("not a valid date");
		assertThat(parsedDateTime.isPresent(), equalTo(false));
	}

}

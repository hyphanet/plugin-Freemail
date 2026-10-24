/*
 * DateStringFactory.java
 * This file is part of Freemail, copyright (C) 2006,2007,2008 Dave Baker
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

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class DateStringFactory {
	private static final TimeZone utc = TimeZone.getTimeZone("UTC");
	private static final Calendar cal = Calendar.getInstance(utc);

	public static String getKeyString() {
		return getOffsetKeyString(0);
	}

	// get a date in a format we use for keys, offset from today
	public static synchronized String getOffsetKeyString(int offset) {
		cal.setTime(new Date());
		cal.add(Calendar.DAY_OF_MONTH, offset);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.ROOT);
		sdf.setTimeZone(utc);

		return sdf.format(cal.getTime());
	}

	public static Date dateFromKeyString(String str) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.ROOT);
			sdf.setTimeZone(utc);

			sdf.setLenient(false);
			return sdf.parse(str);
		} catch (ParseException pe) {
			return null;
		}
	}

	/**
	 * Formats the given {@link TemporalAccessor} into a full,
	 * <a href="https://www.rfc-editor.org/info/rfc5322/">RFC
	 * 5322</a>-compliant date string.
	 * <p>
	 * The formatted date will always have an offset of 0, in order to not
	 * expose the user’s local timezone offset. Temporal accessors that have
	 * an offset (such as {@link ZonedDateTime} or {@link OffsetDateTime})
	 * will be translated correctly; others (like {@link Instant} or {@link
	 * LocalDateTime}) will be assumed to already be at an offset of 0.
	 * </p>
	 *
	 * @param dateTime The temporal accessor to format
	 * @return The formatted date string
	 */
	public static String formatFullDate(TemporalAccessor dateTime) {
		return formatWithFormatter(dateTime, fullDateFormat);
	}

	/**
	 * Parses the given {@link String} as a full date, according to < a
	 * href="https://www.rfc-editor.org/info/rfc5322/">RFC 5322</a>.
	 *
	 * @param dateTimeString The string to parse
	 * @return A parsed {@link OffsetDateTime date with offset}, or
	 *        {@link Optional#empty()} if the date could not be parsed
	 */
	public static Optional<OffsetDateTime> parseFullDate(String dateTimeString) {
		try {
			return Optional.of(fullDateFormat.parse(dateTimeString, OffsetDateTime::from));
		} catch(DateTimeException e) {
			return Optional.empty();
		}
	}

	/**
	 * Formats the given {@link TemporalAccessor} into a full,
	 * <a href="https://www.rfc-editor.org/info/rfc9051/">RFC
	 * 9051</a>-compliant string, usable with IMAP’s INTERNALDATE attribute.
	 * <p>
	 * The formatted date will always have an offset of 0, in order to not
	 * expose the user’s local timezone offset. Temporal accessors that have
	 * an offset (such as {@link ZonedDateTime} or {@link OffsetDateTime})
	 * will be translated correctly; others (like {@link Instant} or {@link
	 * LocalDateTime}) will be assumed to already be at an offset of 0.
	 * </p>
	 *
	 * @param dateTime The temporal accessor to format
	 * @return The formatted date string
	 */
	public static String formatInternalDate(TemporalAccessor dateTime) {
		return formatWithFormatter(dateTime, internalDateFormat);
	}

	/**
	 * Parses the given {@link String} as internal date, according to < a
	 * href="https://www.rfc-editor.org/info/rfc9051/">RFC 9051</a>.
	 *
	 * @param dateTimeString The string to parse
	 * @return A parsed {@link OffsetDateTime date with offset}, or
	 *        {@link Optional#empty()} if the date could not be parsed
	 */
	public static Optional<OffsetDateTime> parseInternalDate(String dateTimeString) {
		try {
			return Optional.of(internalDateFormat.parse(dateTimeString, OffsetDateTime::from));
		} catch(DateTimeException e) {
			return Optional.empty();
		}
	}

	private static String formatWithFormatter(TemporalAccessor dateTime, DateTimeFormatter internalDateFormat) {
		if (dateTime.isSupported(ChronoField.OFFSET_SECONDS)) {
			return internalDateFormat.format(dateTime.query(OffsetDateTime::from).withOffsetSameInstant(ZoneOffset.ofHours(0)));
		} else if (dateTime.isSupported(ChronoField.HOUR_OF_DAY)) {
			return internalDateFormat.format(dateTime.query(LocalDateTime::from).atOffset(ZoneOffset.ofHours(0)));
		} else {
			return internalDateFormat.format(dateTime.query(Instant::from).atOffset(ZoneOffset.ofHours(0)));
		}
	}

	private static final DateTimeFormatter fullDateFormat = DateTimeFormatter.ofPattern("[EEE, ]d MMM yyyy HH:mm:ss ZZZ", Locale.ROOT);
	private static final DateTimeFormatter internalDateFormat = DateTimeFormatter.ofPattern("d-MMM-yyyy HH:mm:ss ZZZ", Locale.ROOT);

}

/*
 * DateStringFactory.java
 * This file is part of Freemail, copyright (C) 2006 Dave Baker
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 * 
 */

package freemail.utils;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class DateStringFactory {
	private static final TimeZone gmt = TimeZone.getTimeZone("GMT");
	private static final Calendar cal = Calendar.getInstance(gmt);

	public static String getKeyString() {
		return getOffsetKeyString(0);
	}
	
	// get a date in a format we use for keys, offset from today
	public static synchronized String getOffsetKeyString(int offset) {
		cal.setTime(new Date());
		cal.add(Calendar.DAY_OF_MONTH, offset);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		sdf.setTimeZone(gmt);
		
		return sdf.format(cal.getTime());
	}
	
	public static Date dateFromKeyString(String str) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			sdf.setTimeZone(gmt);
			
			sdf.setLenient(false);
			return sdf.parse(str);
		} catch (ParseException pe) {
			return null;
		}
	}
}

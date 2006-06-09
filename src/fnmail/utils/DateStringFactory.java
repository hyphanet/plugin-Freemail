package fnmail.utils;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class DateStringFactory {
	private static final TimeZone gmt = TimeZone.getTimeZone("GMT");
	private static final Calendar cal = Calendar.getInstance(gmt);
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

	public static String getKeyString() {
		return getOffsetKeyString(0);
	}
	
	// get a date in a format we use for keys, offset from today
	public static synchronized String getOffsetKeyString(int offset) {
		cal.setTime(new Date());
		cal.add(Calendar.DAY_OF_MONTH, offset);
		
		return sdf.format(cal.getTime());
	}
	
	public static Date DateFromKeyString(String str) {
		try {
			sdf.setLenient(false);
			return sdf.parse(str);
		} catch (ParseException pe) {
			return null;
		}
	}
}

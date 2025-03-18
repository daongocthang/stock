package com.standalone.core.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeMillis {
    public static long parse(String s, String fmt) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(fmt, Locale.US);
        Date date = sdf.parse(s);
        if (date == null) throw new RuntimeException();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        return calendar.getTimeInMillis();
    }

    public static String format(long millis, String fmt) {
        SimpleDateFormat sdf = new SimpleDateFormat(fmt, Locale.US);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return sdf.format(calendar.getTime());
    }
}

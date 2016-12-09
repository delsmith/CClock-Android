package com.antares.dsmith.cclock.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static java.lang.Math.abs;

/**
 * Created by dsmith on 28/11/16.
 */


public class text {
    // format lat or long as degrees/minutes/seconds
    public static String dms_format(Double degrees, boolean isLongitude) {
        Long seconds = new Double(degrees * 3600).longValue();
        String suffix = (isLongitude)?
                ((seconds < 0) ? "\" W" : "\" E"):
                ((seconds < 0) ? "\" S" : "\" N");
        Long value = abs(seconds), degree = value / 3600, minute = (value % 3600) / 60, second = value % 60;
        return (degree.toString() + degsym + minute.toString() + "\' " + second.toString() + suffix);
    }

    private static String degsym = String.format("%c ",176);

    // format time in seconds as 24-hour time
    public static String t24hr_format(Long seconds, boolean utc) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss z");
        Locale l = utc?Locale.UK:Locale.getDefault();
        Calendar c = utc?
            Calendar.getInstance(TimeZone.getTimeZone("+00"),Locale.UK):
            Calendar.getInstance();
        c.setTimeInMillis(1000*seconds);
        String t = sdf.format(c.getTime());
        return sdf.format(c.getTime());
        //Long time = seconds % 86400, hour = time/3600, minute = (time%3600)/60, second = time%60;
        //return String.format("%02d:%02d:%02d",hour,minute,second);
    }

    // format date as YYYY.MM.DD
    public static String tYMD_format(Long seconds, boolean utc) {
        Calendar c = Calendar.getInstance((utc? Locale.UK: Locale.getDefault()));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        c.setTimeInMillis(seconds*1000);
        // note: Month range 0-11
        //return String.format("%04d.%02d.%02d",c.get(Calendar.YEAR),1+c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));
        return sdf.format(c.getTime());
    }

}
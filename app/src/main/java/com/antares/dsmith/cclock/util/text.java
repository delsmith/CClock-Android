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
    // format date string the old way - No timezone processing involved
    public static String tHMS_format(Long seconds){
        Long second = seconds % 60;
        Long minute = (seconds/60) % 60;
        Long hour = (seconds/3600) % 24;
        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

    // format time in seconds as 24-hour time with timezone applied
    public static String t24hr_format(Long seconds) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss z");
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(1000*seconds);
        String t = sdf.format(c.getTime());
        return sdf.format(c.getTime());
    }

    // format date as YYYY.MM.DD
    public static String tYMD_format(Long seconds) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        c.setTimeInMillis(seconds*1000);
        return sdf.format(c.getTime());
    }

    // format lat or long as degrees/minutes/seconds
    public static String dms_format(Double degrees, boolean isLongitude) {
        Long seconds = Double.valueOf(degrees * 3600).longValue();
        String suffix = (isLongitude)?
                ((seconds < 0) ? "\" W" : "\" E"):
                ((seconds < 0) ? "\" S" : "\" N");
        Long value = abs(seconds), degree = value / 3600, minute = (value % 3600) / 60, second = value % 60;
        return (degree.toString() + degsym + minute.toString() + "\' " + second.toString() + suffix);
    }

    private static String degsym = String.format("%c ",176);

}
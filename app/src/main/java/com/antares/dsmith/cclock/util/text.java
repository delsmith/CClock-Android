package com.antares.dsmith.cclock.util;

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
    public static String t24hr_format(Long seconds) {
        Long time = seconds % 86400, hour = time/3600, minute = (time%3600)/60, second = time%60;
        return String.format("%02d:%02d:%02d",hour,minute,second);
    }
}
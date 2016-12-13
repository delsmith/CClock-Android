package com.antares.dsmith.cclock.util;

import java.util.Calendar;

import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.tan;

/**
 * Created by dsmith on 27/11/16.
 */

public class calcs {
    // convert a Lat or Long to integer seconds
    public static Long coordAsLong( String coord){
        // original value is a string, as degrees
        // convert the string to Double then multiply by 3600
        // round to nearest integer
        try {
            return ( (Double.valueOf(Double.parseDouble(coord)*3600. +0.5)).longValue());
        }
        catch (Exception e) {
            return Long.MIN_VALUE;
        }
    }
/*
from WikiPedia  (amended by Del Smith to use radians)

    Another calculation of the equation of time can be done as follows.[41] Angles are in degrees; the conventional order of operations applies.
    W = 360° / 365.24 days
    //W is the Earth's mean angular orbital velocity in degrees per day.

    A = W × (D + 10)
    // D is the date, in days starting at zero on 1 January (i.e. the days part of the ordinal date minus 1). 10 is the approximate number of days from the December solstice to 1 January.
    // A is the angle the earth would move on its orbit at its average speed from the December solstice to date D.

    B = A + 360° / π × 0.0167 × sin [W(D − 2)]
    // B is the angle the Earth moves from the solstice to date D, including a first-order correction for the Earth's orbital eccentricity, 0.0167.
    // The number 2 is the number of days from 1 January to the date of the Earth's perihelion.
    // This expression for B can be simplified by combining constants to:
    B = A + 1.914° × sin [W(D − 2)].

    C = (A - atan( tan(B)/ cos(23.44deg)))/180
    C is the difference between the angles moved at mean speed, and at the corrected speed projected onto the equatorial plane, and divided by 180 to get the difference in "half turns".
    The value 23.44° is the obliquity (tilt) of the Earth's axis.
    The subtraction gives the conventional sign to the equation of time.
    For any given value of x, arctan x (sometimes written as tan−1 x) has multiple values, differing from each other by integer numbers of half turns.
    The value generated by a calculator or computer may not be the appropriate one for this calculation.
    This may cause C to be wrong by an integer number of half turns.
    The excess half turns are removed in the next step of the calculation to give the equation of time:

    EOT = 720 × (C − nint(C)) minutes
*/
    // calculate solar time from UTC time and longitude
    public static Long solar_time( Long utc /*seconds*/, Double longitude /*degrees*/){
        // add longitude offset to the UTC time and adjust for Equation Of Time
        Long meanSolarTime = Double.valueOf(utc - 43200. + longitude*240.).longValue();
        // adjust for Equation of Time
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(1000*utc);
        int day_num = c.get(Calendar.DAY_OF_YEAR);
        Long solarTime = Double.valueOf(meanSolarTime+EqnOfTime(day_num)).longValue();
        return solarTime;
    }

    /*
        Equation of Time calculation
        *** use at your own risk ***

        written by Del Smith, 29 November 2016

        based on WikiPedia article referenced 28/11/2016
        (which describes angles in a bewildering mixture of degrees and radians)

        it appears to give a good result, but I make no claims for accuracy
     */
    private static final Double pi = atan(1.0)*4;             // tan(pi/4) = 1  (45 degrees)
    private static final Double lambda = 23.44 * pi / 180;    // earth's inclination in radians
    private static final Double omega = 2*pi/365.25;          // angular velocity of annual revolution (radians/day)
    // calculate the Equation of Time from 'day of year'
    private static Double EqnOfTime(int day_number) {
        // D                                                          day of the year
        // A = W*(D+10)                                               angle of revolution (circular)
        // B = A + 0.0333*sin(W*(D-2))                                angle of revolution (elliptical)
        // C = ( A - atan[  tan(B) / cos (inclination) ] ) / 'pi'     angular correction
        // EOT = 43200 × (C − int(C+0.5))                             Equation of Time in seconds
        Double delta = Integer.valueOf(day_number).doubleValue();
        Double alpha = omega*((delta+10)%365);                    // angle in (mean) circular orbit, solar year starts 21.Dec
        Double beta = alpha + 0.0333*sin(omega*((delta-2)%365));  // angle in elliptical orbit, from perigee 3.Jan (radians)
        Double gamma = ( alpha - atan( tan(beta) / cos(lambda))) / pi;  // the angular correction
        Double EoT = (43200. * (gamma - Double.valueOf(gamma+0.5).intValue()));
        return EoT;
    }

    // calculate solar time from UTC time and longitude
    // double epochTime = 262980;
    // double epochSidTime = 6.697374558;
    // double deltaT = 0.002737909350795;
    // double now = (double)utcMilliseconds / 3600000.;
    // return (long) (3600000 * ((epochSidTime + deltaT*(now - epochTime) + now + longitude/15.) % 24.));

    public static Long sidereal_time( Long utc, Double longitude){
        // calculate the sidereal time at a specific place and time
        Double time =           utc / 3600.;            // hours since 1/1/1970
        Double epochTime =      262980.;                // hours from 1/1/1970 to  1/1/2000 12:00:00
        Double epochSidTime =   6.697374558;            // sidereal time at epoch (hours)
        Double sidRatio =       1.002737909350795;      // ratio of sidereal time unit to calendar unit  (366.25/365.25)
        // Double siderealTime =   3600 * ( (epochTime + (epochSidTime + sidRatio*(time - epochTime)) + longitude/15.) % 24.);  // in hours
        Double siderealTime =   3600 * (epochTime + epochSidTime + sidRatio*(time - epochTime) + longitude/15.);  // in hours
        Long sidereal = siderealTime.longValue();
        return sidereal;
     }
}

    /*
        private long MSEC_PER_DAY = 86400000;

        /*** javascript code
         var epochHour = 262980;     // 1.Jan.2000 12:00 UT as Unix time in hours.
         var epochST = 6.697374558   // GMST at epoch Time
         var deltaT = 0.002737909350795;
         function getLAST(d,l){

         * Local Apparent Sidereal Time (for Date 'd' at Longitude 'l')
         * ref: http://aa.usno.navy.mil/faq/docs/GAST.php
         *
         * Given below is a simple algorithm for computing apparent sidereal time
         * to an accuracy of about 0.1 second, equivalent to about 1.5 arcseconds of sky.
         *
         * The algorithm uses an epoch of Jan 1, 2000 12:00 UT
         *
         * It derives two values representing the current time
         *      epDays  is the number of (mean solar) days from the epoch to 0000 UT of the current day
         *              (always has a fraction of .5)
         *      utHours is the number of hours since 0000 UT
         *
         * Then the Greenwich mean sidereal time in hours is
         *      GMST = 6.697374558 + 0.06570982441908 x epDays + 1.00273790935 x utHours
         * Local Apparent Sidereal Time is found by adding 4 minutes per degree of East longitude (1 hour per 15 degrees)
         *      LAST = GMST + longitude / 15
         * A modulus operation is applied to yield : 0.0 <= LAST < 24.0
         *
         * In simple terms,
         *      epochHour = 262980;         // Epoch time (1.Jan.2000 12:00 UT) as Unix time in hours.
         *      epochST = 6.697374558       // GMST at epoch Time
         *      deltaT = 0.002737909350795; // ( Sidereal hour - Solar Hour) (as Sidereal hours)
         * then
         *      let nowHour be the current UT time in hours
         *      GMST = epochST + (nowHour-epochHour)*deltaT + nowHour modulo 24
         *      LAST = (GMST + longitude/15) modulo 24

         nowHour = d.getTime()/3600000.;     // current Unix UT time in hours
         LAST = ( epochST + deltaT*(nowHour - epochHour) + nowHour + l/15 ) % 24;
         // Format this as HH:MM:SS
         r = new Date(LAST*3600000);
         return r.toUTCString().substr(17,8);
         *****
    private long sidereal( long utcMilliseconds, double longitude ) {
        // compute sidereal time at the given longitude

        double epochTime = 262980;
        double epochSidTime = 6.697374558;
        double deltaT = 0.002737909350795;
        double now = (double)utcMilliseconds / 3600000.;

        return (long) (3600000 * ((epochSidTime + deltaT*(now - epochTime) + now + longitude/15.) % 24.));
    }

    @SuppressLint("DefaultLocale")
    private String timeString( long ms ) {
        long h = ms / 3600000;
        ms %= 3600000;
        long m = ms / 60000;
        long s = (ms % 60000) / 1000;
        return String.format("%02d:%02d:%02d",h,m,s);
    }

*/
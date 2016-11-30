package com.antares.dsmith.cclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.StringTokenizer;
import java.util.Timer;
import java.util.Date;
import java.util.Calendar;

import static com.antares.dsmith.cclock.util.calcs.coordAsLong;
import static com.antares.dsmith.cclock.util.calcs.sidereal_time;
import static com.antares.dsmith.cclock.util.calcs.solar_time;
import static com.antares.dsmith.cclock.util.text.dms_format;
import static com.antares.dsmith.cclock.util.text.t24hr_format;

public class MainActivity extends AppCompatActivity {
    // define preferences
    public static final String APP_PREFERENCES = "CClockPrefs";
    public static final String APP_PREFERENCES_clock_mode = "clock_mode"; // Boolean
    public static final String APP_PREFERENCES_UTC_mode = "UTC_mode"; // Boolean
    public static final String APP_PREFERENCES_locn_mode = "locn_mode"; // Boolean
    public static final String APP_PREFERENCES_night_mode = "night_mode"; // Boolean
    public static final String APP_PREFERENCES_UTC_time = "UTC_time"; // Long (seconds)
    public static final String APP_PREFERENCES_TZ_offset = "TZ_offset"; // Long (seconds)
    public static final String APP_PREFERENCES_latitude = "latitude"; // Long (seconds)
    public static final String APP_PREFERENCES_longitude = "longitude"; // Long (seconds)
    public static final String APP_PREFERENCES_target_RA = "target_RA"; // Long (seconds)

    // working context objects
    private static TimerUpdateTask timerUpdate;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;
    private Location mLastLocation;
    private EditText mLatitudeField, mLongitudeField, mLocalTimeField, mUtcTimeField, mSolarTimeField, mSiderealTimeField;

    // state information
    public boolean clock_mode = true;      // [Auto | Select]
    private boolean UTC_mode = false;       // [UTC | Local]
    private boolean locn_mode = true;       // [Auto | Select]
    private boolean night_mode = false;     // [Night | Day]
    private Long local_time, UTC_time, TZ_offset;   // times in seconds
    private Double mLatitude, mLongitude;           // in degrees                                       // coords in seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        /*
        timerUpdate = new TimerUpdateTask(this);
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(timerUpdate, 0, 1000);
        */
    }

    // respond to the UPDATE button
    public void Refresh(View view) {
        // if AUTO mode, get the current time and location
        if (clock_mode)
            initTimeSetting();
        if (locn_mode)
            initLocnSetting();
        if (clock_mode || locn_mode)
            mEditor.commit();
        refresh();
    }


    // respond to the Time 'Set' button
    public void SetTime(View view) {
        Intent intent = new Intent(this, SetTimeActivity.class);
        startActivity(intent);
    }

    // respond to the Location 'Set' button
    public void SetLocation(View view) {
        Intent intent = new Intent(this, SetLocationActivity.class);
        // hint: look at 'https://github.com/commonsguy/cw-omnibus/tree/master/Jank/ThreePaneBC'

        // startActivity(intent);
    }

    protected void initLocnSetting() {
        // check that LOCATION services are Permitted
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            mLastLocation = null;
        }
        else
        {
            LocationManager locMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
            mLastLocation = locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (mLastLocation != null) {
            String latitude = String.valueOf(mLastLocation.getLatitude());
            mEditor.putLong(APP_PREFERENCES_latitude,coordAsLong(latitude));
            String longitude = String.valueOf(mLastLocation.getLongitude());
            mEditor.putLong(APP_PREFERENCES_longitude,coordAsLong(longitude));
            mEditor.putLong(APP_PREFERENCES_target_RA,0);
        }
    }

    protected void initTimeSetting() {
        Calendar calendar = Calendar.getInstance();
        Long TZ_offset = new Integer((calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / 1000).longValue();
        Date now = new Date();
        mEditor.putLong(APP_PREFERENCES_UTC_time,now.getTime()/1000);
        mEditor.putLong(APP_PREFERENCES_TZ_offset,TZ_offset);
    }

    protected void onStart() {
        mEditor = mPreferences.edit();
        // create preferences with default values unless they exist
        if (! mPreferences.contains("clock_mode")) {
            // initialise preferences with default settings
            mEditor.putBoolean(APP_PREFERENCES_clock_mode,true);     // Auto
            mEditor.putBoolean(APP_PREFERENCES_UTC_mode,false);      // Local
            mEditor.putBoolean(APP_PREFERENCES_locn_mode,true);      // Auto
            mEditor.putBoolean(APP_PREFERENCES_night_mode,false);    // Day
            // save current time and TZ, as seconds
            initTimeSetting();
            // save current location, as seconds
            initLocnSetting();
            mEditor.apply();
        } // create preferences

        // setup mode options
        clock_mode = mPreferences.getBoolean(APP_PREFERENCES_clock_mode,true);
        UTC_mode = mPreferences.getBoolean(APP_PREFERENCES_UTC_mode,true);
        locn_mode = mPreferences.getBoolean(APP_PREFERENCES_locn_mode,true);
        night_mode = mPreferences.getBoolean(APP_PREFERENCES_night_mode,true);

        // setup the text field references
        mLocalTimeField = (EditText) findViewById(R.id.time_value);
        mUtcTimeField = (EditText) findViewById(R.id.UTC_value);
        mSolarTimeField = (EditText) findViewById(R.id.sol_time_value);
        mSiderealTimeField = (EditText) findViewById(R.id.sid_time_value);
        mLatitudeField = (EditText) findViewById(R.id.lat_value);
        mLongitudeField = (EditText) findViewById(R.id.long_value);

        // if AUTO mode, get the current time and location
        if (clock_mode)
            initTimeSetting();
        if (locn_mode)
            initLocnSetting();
        if (clock_mode || locn_mode)
            mEditor.commit();

        // get location coordinates from preferences, in degrees
        mLatitude = mPreferences.getLong(APP_PREFERENCES_latitude,(-38*3600)) / 3600.;
        mLongitude = mPreferences.getLong(APP_PREFERENCES_longitude,(145*3600)) / 3600.;
        // format the coordinates fields
        String coord = dms_format(mLatitude,false);
        mLatitudeField.setText(coord);
        coord = dms_format(mLongitude,true);
        mLongitudeField.setText(coord);

        super.onStart();

        refresh();
    }

    // update the text fields on the MainActivity view with current calculations
    public void refresh(){
        // get UTC time and timezone offset from preferences,
        UTC_time = mPreferences.getLong(APP_PREFERENCES_UTC_time,0);
        // format the UTC time field
        mUtcTimeField.setText(t24hr_format(UTC_time));
        // calculate the local time
        TZ_offset = mPreferences.getLong(APP_PREFERENCES_TZ_offset,(150*3600));
        local_time = UTC_time + TZ_offset;
        // format the local time field
        mLocalTimeField.setText(t24hr_format(local_time));
        // format the solar time field
        mSolarTimeField.setText(t24hr_format(solar_time(UTC_time,mLongitude)));
       //format the sidereal time field
        mSiderealTimeField.setText(t24hr_format(sidereal_time(UTC_time,mLongitude)));
    }

}
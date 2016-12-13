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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.sql.Ref;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.Date;
import java.util.Calendar;

import static com.antares.dsmith.cclock.util.calcs.coordAsLong;
import static com.antares.dsmith.cclock.util.calcs.sidereal_time;
import static com.antares.dsmith.cclock.util.calcs.solar_time;
import static com.antares.dsmith.cclock.util.text.dms_format;
import static com.antares.dsmith.cclock.util.text.t24hr_format;
import static com.antares.dsmith.cclock.util.text.tHMS_format;
import static com.antares.dsmith.cclock.util.text.tYMD_format;

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
    private TextView mLatitudeField, mLongitudeField;
    private TextView mLocalDateField, mLocalTimeField, mUtcTimeField, mSolarTimeField, mSiderealTimeField;
    private Button mUpdateButton;

    // state information
    public boolean clock_mode = true;      // [Auto | Select]
    private boolean UTC_mode = false;       // [UTC | Local]
    private boolean locn_mode = true;       // [Auto | Select]
    private boolean night_mode = false;     // [Night | Day]
    private Long UTC_time, TZ_offset;   // times in seconds
    private Double mLatitude, mLongitude;           // in degrees                                       // coords in seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setLocalData();
    }

    private void setLocalData() {

        mPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        // create preferences with default values unless they exist
        if (! mPreferences.contains("clock_mode")) {
            mEditor = mPreferences.edit();
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

        // setup the view object references
        mLocalTimeField = (TextView) findViewById(R.id.time_value);
        mLocalDateField = (TextView) findViewById(R.id.date_value);
        mUtcTimeField = (TextView) findViewById(R.id.UTC_value);
        mSolarTimeField = (TextView) findViewById(R.id.sol_time_value);
        mSiderealTimeField = (TextView) findViewById(R.id.sid_time_value);
        mLatitudeField = (TextView) findViewById(R.id.lat_value);
        mLongitudeField = (TextView) findViewById(R.id.long_value);
        mUpdateButton = (Button) findViewById((R.id.UpdateButton));

        // get location coordinates from preferences, in degrees
        mLatitude = mPreferences.getLong(APP_PREFERENCES_latitude,(-38*3600)) / 3600.;
        mLongitude = mPreferences.getLong(APP_PREFERENCES_longitude,(145*3600)) / 3600.;
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

    // update the text fields on the MainActivity view with current calculations
    public void refresh(){
        // get UTC time and timezone offset from preferences,
        UTC_time = mPreferences.getLong(APP_PREFERENCES_UTC_time,0);
        TZ_offset = mPreferences.getLong(APP_PREFERENCES_TZ_offset,(150*3600));

        // format the local date & time fields
        mLocalTimeField.setText(t24hr_format(UTC_time));   //(local_time));
        mLocalDateField.setText(tYMD_format(UTC_time));

        // format the UTC time field
        mUtcTimeField.setText(tHMS_format(UTC_time));
        // format the solar time field
        mSolarTimeField.setText(tHMS_format(solar_time(UTC_time,mLongitude)));
        //format the sidereal time field
        mSiderealTimeField.setText(tHMS_format(sidereal_time(UTC_time,mLongitude)));
        // format the coordinates fields
        String coord = dms_format(mLatitude,false);
        mLatitudeField.setText(coord);
        coord = dms_format(mLongitude,true);
        mLongitudeField.setText(coord);

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
        Calendar c = Calendar.getInstance();
        Long TZ_offset = Integer.valueOf((c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET)) / 1000).longValue();
        mEditor.putLong(APP_PREFERENCES_UTC_time,c.getTimeInMillis()/1000);
        mEditor.putLong(APP_PREFERENCES_TZ_offset,TZ_offset);
    }

    protected void onStart() {
        // re-load mode preferences
        clock_mode = mPreferences.getBoolean(APP_PREFERENCES_clock_mode,true);
        UTC_mode = mPreferences.getBoolean(APP_PREFERENCES_UTC_mode,true);
        locn_mode = mPreferences.getBoolean(APP_PREFERENCES_locn_mode,true);
        night_mode = mPreferences.getBoolean(APP_PREFERENCES_night_mode,true);
        // mUpdateButton.setVisibility(clock_mode?View.VISIBLE:View.INVISIBLE);
        mUpdateButton.setEnabled(clock_mode);

        // if AUTO mode, get the current time and location
        if (clock_mode)
            initTimeSetting();
        if (locn_mode)
            initLocnSetting();
        if (clock_mode || locn_mode)
            mEditor.commit();

        super.onStart();

        refresh();
    }

}

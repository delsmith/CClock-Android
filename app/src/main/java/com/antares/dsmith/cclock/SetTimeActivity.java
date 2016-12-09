package com.antares.dsmith.cclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import java.util.Calendar;
import java.util.Date;

/*
 * The 'SetTime' activity
 *  - allows User to toggle AUTO/SELECT option
 *  - allows User to toggle Local/UTC option
 *  - allows User to set the date and time, when in SELECT mode
 */
public class SetTimeActivity extends AppCompatActivity {

    private boolean isChanged = false;
    private boolean clock_mode = true;      // [Auto | Select]
    private boolean UTC_mode = false;       // [UTC | Local]
    private boolean locn_mode = true;       // [Auto | Select]
    private boolean night_mode = false;     // [Night | Day]
    private Long local_time, UTC_time, TZ_offset;   // times in seconds

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;
    public static final String APP_PREFERENCES = "CClockPrefs";
    public static final String APP_PREFERENCES_clock_mode = "clock_mode"; // Boolean
    public static final String APP_PREFERENCES_UTC_mode = "UTC_mode"; // Boolean
    public static final String APP_PREFERENCES_UTC_time = "UTC_time"; // Long (seconds)
    public static final String APP_PREFERENCES_TZ_offset = "TZ_offset"; // Long (seconds)
    public static final String APP_PREFERENCES_night_mode = "night_mode"; // Boolean

    private ToggleButton mUtcButton;
    private ToggleButton mAutoButton;
    private Button mSetButton;
    private Button mResetButton;
    private TimePicker mTimePicker;
    private DatePicker mDatePicker;

    /* activity level event handlers ==============================================================
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_time);

        mPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        mUtcButton = (ToggleButton) findViewById(R.id.toggleUTC);
        mAutoButton = (ToggleButton) findViewById(R.id.toggleTimer);
        mTimePicker = (TimePicker) findViewById(R.id.timePicker);
        mDatePicker = (DatePicker) findViewById(R.id.datePicker);
        mSetButton = (Button) findViewById(R.id.SetButton);
        mResetButton = (Button) findViewById(R.id.ResetButton);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // setup mode options
        clock_mode = mPreferences.getBoolean(APP_PREFERENCES_clock_mode,true);
        UTC_mode = mPreferences.getBoolean(APP_PREFERENCES_UTC_mode,true);
        night_mode = mPreferences.getBoolean(APP_PREFERENCES_night_mode,true);
        mAutoButton.setChecked(clock_mode);
        mUtcButton.setChecked(UTC_mode);
        isChanged = false;
        ApplyChanges();
        initPickers();
    }

    @Override
    public void onBackPressed() {
        ApplyChanges();
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        // if changes were made, update shared prefs (Button states, date, time)
        ApplyChanges();
        super.onStop();
    }

    /* view level event handlers ==================================================================
    */
    // toggle auto/select mode
    public void toggleTimer(View view) {
        clock_mode = !clock_mode;
        isChanged = true;
        ApplyChanges();
    }

    // toggle UTC/Local mode
    public void toggleUTC(View view) {
        UTC_mode = !UTC_mode;
        isChanged = true;
    }

    // respond to the 'SET' button
    public void SetTime(View view) {
        // update the UTC Time preference from the pickers
        Integer year = mDatePicker.getYear();
        Integer month = mDatePicker.getMonth();
        Integer day = mDatePicker.getDayOfMonth();
        Integer hour = mTimePicker.getCurrentHour();
        Integer minute = mTimePicker.getCurrentMinute();
        Integer second = 0;
        Calendar c = Calendar.getInstance();
        c.set(year,month,day,hour,minute,second);
        TZ_offset = new Integer((c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET)) / 1000).longValue();
        // NOTE: Calendar assumes these values are for local time and calculates UTC time internally
        UTC_time = Long.valueOf(c.getTimeInMillis()/1000);
        if (UTC_mode) UTC_time += TZ_offset;
        isChanged = true;
        ApplyChanges();
        finish();

    }

    // respond to the 'Reset' button
    // set the spinners to the current time (for selected UTC mode)
    protected void Reset(View view) {
        // initialise the pickers with the current date & time
        Calendar c = Calendar.getInstance();
        UTC_time = Long.valueOf(c.getTimeInMillis()/1000);
        isChanged = true;
        ApplyChanges();
        initPickers();
    }

    /* shared methods for event handlers ==========================================================
    */
    protected void ApplyChanges() {
        if (isChanged) {
            mEditor = mPreferences.edit();
            mEditor.putBoolean(APP_PREFERENCES_clock_mode,clock_mode);
            mEditor.putBoolean(APP_PREFERENCES_UTC_mode,UTC_mode);
            mEditor.putLong(APP_PREFERENCES_UTC_time,UTC_time);
            mEditor.putLong(APP_PREFERENCES_TZ_offset,TZ_offset);
            mEditor.commit();
            isChanged = false;
        }
        mUtcButton.setEnabled(!clock_mode);
        mSetButton.setEnabled(!clock_mode);
        mResetButton.setEnabled(!clock_mode);
        mTimePicker.setEnabled(!clock_mode);
        mDatePicker.setEnabled(!clock_mode);
        // use GONE instead of INVISIBLE
        mSetButton.setVisibility(clock_mode?View.GONE:View.VISIBLE);
        mResetButton.setVisibility(clock_mode?View.GONE:View.VISIBLE);
        //mUtcButton.setVisibility(clock_mode?View.GONE:View.VISIBLE);
    }

    protected void initPickers() {
        //  initialise Time Picker and Date Picker
        UTC_time = mPreferences.getLong(APP_PREFERENCES_UTC_time,0);
        TZ_offset = mPreferences.getLong(APP_PREFERENCES_TZ_offset,(150*3600));
        // spinner will assume local time so fudge time to make it display UTC
        Long timeValue = UTC_mode? (UTC_time-TZ_offset): UTC_time;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timeValue*1000);

        mDatePicker.updateDate(c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));
        mDatePicker.setEnabled(!clock_mode);

        mTimePicker.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
        mTimePicker.setCurrentMinute(c.get(Calendar.MINUTE));
        mTimePicker.setIs24HourView(true);
        mTimePicker.setEnabled(!clock_mode);
    }

}

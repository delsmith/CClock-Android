package com.antares.dsmith.cclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.ToggleButton;

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

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;
    public static final String APP_PREFERENCES = "CClockPrefs";
    public static final String APP_PREFERENCES_clock_mode = "clock_mode"; // Boolean
    public static final String APP_PREFERENCES_UTC_mode = "UTC_mode"; // Boolean
    public static final String APP_PREFERENCES_locn_mode = "locn_mode"; // Boolean
    public static final String APP_PREFERENCES_night_mode = "night_mode"; // Boolean

    private ToggleButton mUtcButton;
    private ToggleButton mAutoButton;
    private Button mApplyButton;
    private TimePicker mTimePicker;
    private DatePicker mDatePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_time);

        mPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        mUtcButton = (ToggleButton) findViewById(R.id.toggleUTC);
        mAutoButton = (ToggleButton) findViewById(R.id.toggleTimer);
        mTimePicker = (TimePicker) findViewById(R.id.timePicker);
        mDatePicker = (DatePicker) findViewById(R.id.datePicker);
        mApplyButton = (Button) findViewById(R.id.ApplyButton);
    }

    public void toggleTimer(View view) {
        // manage timer on/off transition
        clock_mode = !clock_mode;
        isChanged = true;
        mTimePicker.setEnabled(!clock_mode);
        mDatePicker.setEnabled(!clock_mode);
        mApplyButton.setVisibility(clock_mode?View.INVISIBLE:View.VISIBLE);
    }

    public void toggleUTC(View view) {
        UTC_mode = !UTC_mode;
        isChanged = true;
        // if we want to, manage timer on/off transition
    }

    // respond to the 'Update' button
    public void Update(View view) {
        // read the new date/time selection and update the UTC Time preference
        Integer year = mDatePicker.getYear();
        Integer month = mDatePicker.getMonth();
        Integer day = mDatePicker.getDayOfMonth();
        Integer hour = mTimePicker.getCurrentHour();
        Integer minute = mTimePicker.getCurrentMinute();
    }

    protected void onStart() {

        // setup mode options
        isChanged = false;
        clock_mode = mPreferences.getBoolean(APP_PREFERENCES_clock_mode,true);
        mAutoButton.setChecked(clock_mode);
        mApplyButton.setVisibility(clock_mode?View.INVISIBLE:View.VISIBLE);
        UTC_mode = mPreferences.getBoolean(APP_PREFERENCES_UTC_mode,true);
        mUtcButton.setChecked(UTC_mode);
        night_mode = mPreferences.getBoolean(APP_PREFERENCES_night_mode,true);

        mTimePicker.setIs24HourView(true);
        mTimePicker.setEnabled(!clock_mode);

        mDatePicker.setEnabled(!clock_mode);
        /*  initialise Time Picker and Date Picker

        SimpleDateFormat sdf = new SimpleDateFormat("hh:ss");
        Date date = null;
        try {
            date = sdf.parse("07:00");
        } catch (ParseException e) {
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);

        TimePicker picker = new TimePicker(getApplicationContext());
        picker.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
        picker.setCurrentMinute(c.get(Calendar.MINUTE));

         */
        super.onStart();
    }

    protected void onStop() {
        // if changes were made, update shared prefs (Button states, date, time)
        if (isChanged) {
            mEditor = mPreferences.edit();
            mEditor.putBoolean(APP_PREFERENCES_clock_mode,clock_mode);
            mEditor.putBoolean(APP_PREFERENCES_UTC_mode,UTC_mode);
            mEditor.apply();
            isChanged = false;
        }
        super.onStop();
    }

}

/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.apis.view;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.android.apis.R;

import java.util.Calendar;

/**
 * Basic example of using date and time widgets, including android.app.TimePickerDialog and
 * android.widget.DatePicker. Also provides a good example of using Activity.onCreateDialog,
 * Activity.onPrepareDialog and Activity.showDialog to have the activity automatically save
 * and restore the state of the dialogs. Nifty TimePickerDialog on M, lame one on Jellybean
 */
public class DateWidgets1 extends Activity {
    /**
     * where we display the selected date and time
     */
    private TextView mDateDisplay;

    // date and time
    /**
     * Year to display
     */
    private int mYear;
    /**
     * Month to display
     */
    private int mMonth;
    /**
     * Day of the month to display
     */
    private int mDay;
    /**
     * Hour of the day
     */
    private int mHour;
    /**
     * Minute of the day
     */
    private int mMinute;

    /**
     * Dialog ID for the "change the time (12 hour)" dialog which is launched by the Button with
     * ID R.id.pickTime12
     */
    static final int TIME_12_DIALOG_ID = 0;
    /**
     * Dialog ID for the "change the time (24 hour)" dialog which is launched by the Button with
     * ID R.id.pickTime24
     */
    static final int TIME_24_DIALOG_ID = 1;
    /**
     * Dialog ID for the "change the date" dialog which is launched by the Button with
     * ID R.id.pickDate
     */
    static final int DATE_DIALOG_ID = 2;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.date_widgets_example_1.
     * We initialize our field {@code TextView mDateDisplay} by finding the view with ID R.id.dateDisplay,
     * then call our method {@code setDialogOnClickListener} to set the {@code OnClickListener} of the
     * three buttons in our layout as follows:
     * <ul>
     * <li>ID R.id.pickDate "change the date" button, starts the DATE_DIALOG_ID dialog.</li>
     * <li>ID R.id.pickTime12 "change the time (12 hour)" button, starts the TIME_12_DIALOG_ID dialog</li>
     * <li>ID R.id.pickTime24 "change the time (24 hour)" button, starts the TIME_24_DIALOG_ID dialog</li>
     * </ul>
     * Next we initialize {@code Calendar c} with a Calendar with current time in the default time zone
     * with the default locale. We then use {@code c} to initialize {@code mYear} with the current YEAR,
     * {@code mMonth} with the current MONTH, {@code mDay} with the current DAY_OF_MONTH, {@code mHour}
     * with the current HOUR_OF_DAY, and {@code mMinute} with MINUTE.
     * <p>
     * Finally we call our method {@code updateDisplay} to update {@code TextView mDateDisplay} with
     * the date and time values we just initialized.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.date_widgets_example_1);

        mDateDisplay = (TextView) findViewById(R.id.dateDisplay);

        setDialogOnClickListener(R.id.pickDate, DATE_DIALOG_ID);
        setDialogOnClickListener(R.id.pickTime12, TIME_12_DIALOG_ID);
        setDialogOnClickListener(R.id.pickTime24, TIME_24_DIALOG_ID);

        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        updateDisplay();
    }

    /**
     * Sets the {@code OnClickListener} of the button with ID {@code buttonId} to an anonymous class
     * which will call {@code showDialog} using {@code dialogId} as the ID of the dialog to launch.
     * <p>
     * First we initialize {@code Button b} by finding the view with ID {@code buttonId}, then we set
     * its {@code OnClickListener} to an anonymous class which will call {@code showDialog} using
     * {@code dialogId} as the ID of the dialog to launch.
     *
     * @param buttonId ID of the button in our layout whose {@code OnClickListener} we are to set
     * @param dialogId dialog ID that should be launched when the button is clicked.
     */
    private void setDialogOnClickListener(int buttonId, final int dialogId) {
        Button b = (Button) findViewById(buttonId);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //noinspection deprecation
                showDialog(dialogId);
            }
        });
    }

    /**
     * Callback for creating dialogs that are managed (saved and restored) for you by the activity.
     * We switch based on the value of our parameter {@code id}:
     * <ul>
     * <li>TIME_12_DIALOG_ID - we fall through to the TIME_24_DIALOG_ID case</li>
     * <li>
     * TIME_24_DIALOG_ID - we return a new instance of {@code TimePickerDialog}, using
     * {@code OnTimeSetListener mTimeSetListener} as the listener to call when the time is set,
     * {@code mHour} as the initial hour, and {@code mMinute} as the initial minute, and
     * true if our parameter {@code id} is equal to TIME_24_DIALOG_ID (this is a 24 hour view)
     * or false if we fell through from the TIME_12_DIALOG_ID case (AM/PM view).
     * </li>
     * <li>
     * DATE_DIALOG_ID - we return a new instance of {@code DatePickerDialog} using
     * {@code OnDateSetListener mDateSetListener} as the listener to call when the date is set,
     * {@code mYear} as the initial year, {@code mMonth} as the initial month, and {@code mDay}
     * as the initial day of the month.
     * </li>
     * </ul>
     * If the dialog ID is not one of ours, we return null.
     *
     * @param id The id of the dialog.
     * @return The dialog.  If you return null, the dialog will not be created.
     */
    @SuppressWarnings("deprecation")
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case TIME_12_DIALOG_ID:
            case TIME_24_DIALOG_ID:
                return new TimePickerDialog(this,
                        mTimeSetListener, mHour, mMinute, id == TIME_24_DIALOG_ID);
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                        mDateSetListener,
                        mYear, mMonth, mDay);
        }
        return null;
    }

    /**
     * Provides an opportunity to prepare a managed dialog before it is shown. We switch based on the
     * value of our parameter {@code id}:
     * <ul>
     * <li>TIME_12_DIALOG_ID - we fall through to the TIME_24_DIALOG_ID case</li>
     * <li>
     * TIME_24_DIALOG_ID - we call the {@code updateTime} method of our parameter {@code dialog}
     * in order to set the current time to {@code mHour} and {@code mMinute} then break.
     * </li>
     * <li>
     * DATE_DIALOG_ID - we call the {@code updateDate} method of our parameter {@code dialog}
     * in order to set the current date to {@code mYear}, {@code mMonth}, and {@code mDay} then break.
     * </li>
     * </ul>
     *
     * @param id     The id of the managed dialog.
     * @param dialog The dialog.
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case TIME_12_DIALOG_ID:
            case TIME_24_DIALOG_ID:
                ((TimePickerDialog) dialog).updateTime(mHour, mMinute);
                break;
            case DATE_DIALOG_ID:
                ((DatePickerDialog) dialog).updateDate(mYear, mMonth, mDay);
                break;
        }
    }

    /**
     * Updates the date and time displayed in {@code TextView mDateDisplay} to a {@code CharSequence}
     * built from the current values of {@code mMonth}, {@code mDay}, {@code mYear}, {@code mHour},
     * and {@code mMinute}.
     */
    private void updateDisplay() {
        mDateDisplay.setText(
                new StringBuilder()
                        // Month is 0 based so add 1
                        .append(mMonth + 1).append("-")
                        .append(mDay).append("-")
                        .append(mYear).append(" ")
                        .append(pad(mHour)).append(":")
                        .append(pad(mMinute)));
    }

    /**
     * {@code OnDateSetListener} for our {@code DatePickerDialog} dialog, when called we save our
     * {@code year}, {@code monthOfYear}, and {@code dayOfMonth} parameters in our fields {@code mYear},
     * {@code monthOfYear}, and {@code mDay} respectively then call our method {@code updateDisplay}
     * to update our display of these fields.
     */
    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                /**
                 * The callback used to indicate the user is done filling in the date. We simply save
                 * our {@code year}, {@code monthOfYear}, and {@code dayOfMonth} parameters in our
                 * fields {@code mYear}, {@code monthOfYear}, and {@code mDay} respectively then call
                 * our method {@code updateDisplay} to update our display of these fields.
                 *
                 * @param view        The view associated with this listener.
                 * @param year        The year that was set.
                 * @param monthOfYear The month that was set (0-11) for compatibility
                 *                    with {@link java.util.Calendar}.
                 * @param dayOfMonth  The day of the month that was set.
                 */
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                    updateDisplay();
                }
            };

    /**
     * {@code OnTimeSetListener} for our {@code TimePickerDialog} dialog, when called we save our
     * {@code hourOfDay}, and {@code minute} parameters in our fields {@code mHour}, and {@code mMinute}
     * respectively then call our method {@code updateDisplay} to update our display of these fields.
     */
    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {
                /**
                 * Called when the user is done setting a new time and the dialog has closed. We simply
                 * save our {@code hourOfDay}, and {@code minute} parameters in our fields {@code mHour},
                 * and {@code mMinute} respectively then call our method {@code updateDisplay} to update
                 * our display of these fields.
                 *
                 * @param view      the view associated with this listener
                 * @param hourOfDay the hour that was set
                 * @param minute    the minute that was set
                 */
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    mHour = hourOfDay;
                    mMinute = minute;
                    updateDisplay();
                }
            };

    /**
     * Converts int to String, and Zero Pads numbers under 10. If our parameter {@code c} is greater
     * than of equal to 10 we just return the String version of {@code c}, otherwise we append a "0"
     * to the String version of {@code c} and return that.
     *
     * @param c number to convert to zero padded String
     * @return zero padded string version of number
     */
    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }
}

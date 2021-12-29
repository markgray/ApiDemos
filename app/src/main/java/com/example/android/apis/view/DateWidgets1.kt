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
package com.example.android.apis.view
// TODO: replace deprecated showDialog with the new DialogFragment class
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R
import java.util.Calendar

/**
 * Basic example of using date and time widgets, including android.app.TimePickerDialog and
 * android.widget.DatePicker. Also provides a good example of using Activity.onCreateDialog,
 * Activity.onPrepareDialog and Activity.showDialog to have the activity automatically save
 * and restore the state of the dialogs. Nifty TimePickerDialog on M, lame one on Jellybean
 */
class DateWidgets1 : AppCompatActivity() {
    /**
     * [TextView] where we display the selected date and time
     */
    private var mDateDisplay: TextView? = null

    // date and time
    /**
     * Year to display
     */
    private var mYear = 0

    /**
     * Month to display
     */
    private var mMonth = 0

    /**
     * Day of the month to display
     */
    private var mDay = 0

    /**
     * Hour of the day
     */
    private var mHour = 0

    /**
     * Minute of the day
     */
    private var mMinute = 0

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.date_widgets_example_1.
     * We initialize our [TextView] field [mDateDisplay] by finding the view with ID R.id.dateDisplay,
     * then call our method [setDialogOnClickListener] to set the [View.OnClickListener] of the
     * three buttons in our layout as follows:
     *
     *  * ID R.id.pickDate "change the date" button, starts the DATE_DIALOG_ID dialog.
     *  * ID R.id.pickTime12 "change the time (12 hour)" button, starts the TIME_12_DIALOG_ID dialog
     *  * ID R.id.pickTime24 "change the time (24 hour)" button, starts the TIME_24_DIALOG_ID dialog
     *
     * Next we initialize [Calendar] `val c` with a [Calendar] with current time in the default time
     * zone with the default locale. We then use `c` to initialize [mYear] with the current YEAR,
     * [mMonth] with the current MONTH, [mDay] with the current DAY_OF_MONTH, [mHour] with the
     * current HOUR_OF_DAY, and [mMinute] with MINUTE.
     *
     *
     * Finally we call our method [updateDisplay] to update [TextView] field [mDateDisplay] with
     * the date and time values we just initialized.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.date_widgets_example_1)
        mDateDisplay = findViewById(R.id.dateDisplay)
        setDialogOnClickListener(R.id.pickDate, DATE_DIALOG_ID)
        setDialogOnClickListener(R.id.pickTime12, TIME_12_DIALOG_ID)
        setDialogOnClickListener(R.id.pickTime24, TIME_24_DIALOG_ID)
        val c = Calendar.getInstance()
        mYear = c[Calendar.YEAR]
        mMonth = c[Calendar.MONTH]
        mDay = c[Calendar.DAY_OF_MONTH]
        mHour = c[Calendar.HOUR_OF_DAY]
        mMinute = c[Calendar.MINUTE]
        updateDisplay()
    }

    /**
     * Sets the [View.OnClickListener] of the button with ID [buttonId] to a lambda which will call
     * [showDialog] using [dialogId] as the ID of the dialog to launch.
     *
     * First we initialize [Button] `val b` by finding the view with ID [buttonId], then we set its
     * [View.OnClickListener] to a lambda which will call [showDialog] using [dialogId] as the ID of
     * the dialog to launch.
     *
     * @param buttonId ID of the button in our layout whose [View.OnClickListener] we are to set
     * @param dialogId dialog ID that should be launched when the button is clicked.
     */
    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    private fun setDialogOnClickListener(buttonId: Int, dialogId: Int) {
        val b = findViewById<Button>(buttonId)
        b.setOnClickListener { v: View? ->
            @Suppress("DEPRECATION")
            showDialog(dialogId)
        }
    }

    /**
     * Callback for creating dialogs that are managed (saved and restored) for you by the activity.
     * We switch based on the value of our parameter [id]:
     *
     *  * TIME_12_DIALOG_ID - we fall through to the TIME_24_DIALOG_ID case
     *
     *  * TIME_24_DIALOG_ID - we return a new instance of [TimePickerDialog], using
     *  [OnTimeSetListener] field [mTimeSetListener] as the listener to call when the
     *  time is set, [mHour] as the initial hour, and [mMinute] as the initial minute, and
     *  true if our parameter [id] is equal to TIME_24_DIALOG_ID (this is a 24 hour view)
     *  or false if we fell through from the TIME_12_DIALOG_ID case (AM/PM view).
     *
     *  * DATE_DIALOG_ID - we return a new instance of [DatePickerDialog] using
     *  [OnDateSetListener] field [mDateSetListener] as the listener to call when
     *  the date is set, [mYear] as the initial year, [mMonth] as the initial month,
     *  and [mDay] as the initial day of the month.
     *
     * If the dialog ID is not one of ours, we return null.
     *
     * @param id The id of the dialog.
     * @return The dialog. If you return null, the dialog will not be created.
     */
    override fun onCreateDialog(id: Int): Dialog? {
        when (id) {
            TIME_12_DIALOG_ID, TIME_24_DIALOG_ID -> return TimePickerDialog(
                    this,
                    mTimeSetListener,
                    mHour, mMinute, id == TIME_24_DIALOG_ID
            )
            DATE_DIALOG_ID -> return DatePickerDialog(
                    this,
                    mDateSetListener,
                    mYear, mMonth, mDay
            )
        }
        return null
    }

    /**
     * Provides an opportunity to prepare a managed dialog before it is shown. We switch based on the
     * value of our parameter `id`:
     *
     *  * TIME_12_DIALOG_ID - we fall through to the TIME_24_DIALOG_ID case
     *
     *  * TIME_24_DIALOG_ID - we call the `updateTime` method of our [Dialog] parameter [dialog]
     *  in order to set the current time to [mHour] and [mMinute] then break.
     *
     *  * DATE_DIALOG_ID - we call the `updateDate` method of our [Dialog] parameter [dialog]
     *  in order to set the current date to [mYear], [mMonth], and [mDay] then break.
     *
     * @param id     The id of the managed dialog.
     * @param dialog The dialog.
     */
    override fun onPrepareDialog(id: Int, dialog: Dialog) {
        when (id) {
            TIME_12_DIALOG_ID, TIME_24_DIALOG_ID -> (dialog as TimePickerDialog).updateTime(mHour, mMinute)
            DATE_DIALOG_ID -> (dialog as DatePickerDialog).updateDate(mYear, mMonth, mDay)
        }
    }

    /**
     * Updates the date and time displayed in [TextView] field [mDateDisplay] to a [CharSequence]
     * built from the current values of [mMonth], [mDay], [mYear], [mHour], and [mMinute].
     */
    private fun updateDisplay() {
        mDateDisplay!!.text = StringBuilder() // Month is 0 based so add 1
                .append(mMonth + 1).append("-")
                .append(mDay).append("-")
                .append(mYear).append(" ")
                .append(pad(mHour)).append(":")
                .append(pad(mMinute))
    }

    /**
     * [OnDateSetListener] for our [DatePickerDialog] dialog, when called we save our `year`,
     * `monthOfYear`, and `dayOfMonth` parameters in our fields [mYear], [mMonth], and [mDay]
     * respectively then call our method [updateDisplay] to update our display of these fields.
     */
    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    private val mDateSetListener = OnDateSetListener {
        view,
        year,
        monthOfYear,
        dayOfMonth ->

        /**
         * The callback used to indicate the user is done filling in the date. We simply save our
         * `year`, `monthOfYear`, and `dayOfMonth` parameters in our fields `mYear`, `mMonth`, and
         * `mDay` respectively then call our method [updateDisplay] to update our display of these
         * fields.
         *
         * @param view        The view associated with this listener.
         * @param year        The year that was set.
         * @param monthOfYear The month that was set (0-11) for compatibility with [Calendar].
         * @param dayOfMonth  The day of the month that was set.
         */
        mYear = year
        mMonth = monthOfYear
        mDay = dayOfMonth
        updateDisplay()
    }

    /**
     * [OnTimeSetListener] for our [TimePickerDialog] dialog, when called we save our `hourOfDay`,
     * and `minute` parameters in our fields `mHour`, and `mMinute` respectively then call our
     * method [updateDisplay] to update our display of these fields.
     */
    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    private val mTimeSetListener = OnTimeSetListener {
        view,
        hourOfDay,
        minute ->

        /**
         * Called when the user is done setting a new time and the dialog has closed. We simply save
         * our `hourOfDay`, and `minute` parameters in our fields `mHour`, and `mMinute` respectively
         * then call our method [updateDisplay] to update our display of these fields.
         *
         * @param view      the view associated with this listener
         * @param hourOfDay the hour that was set
         * @param minute    the minute that was set
         */
        mHour = hourOfDay
        mMinute = minute
        updateDisplay()
    }

    companion object {
        /**
         * Dialog ID for the "change the time (12 hour)" dialog which is launched by the Button with
         * ID R.id.pickTime12
         */
        const val TIME_12_DIALOG_ID = 0

        /**
         * Dialog ID for the "change the time (24 hour)" dialog which is launched by the Button with
         * ID R.id.pickTime24
         */
        const val TIME_24_DIALOG_ID = 1

        /**
         * Dialog ID for the "change the date" dialog which is launched by the Button with
         * ID R.id.pickDate
         */
        const val DATE_DIALOG_ID = 2

        /**
         * Converts int to String, and Zero Pads numbers under 10. If our parameter [c] is greater
         * than or equal to 10 we just return the String version of [c], otherwise we append a "0"
         * to the String version of [c] and return that.
         *
         * @param c number to convert to zero padded String
         * @return zero padded string version of number
         */
        private fun pad(c: Int): String {
            return if (c >= 10) c.toString() else "0$c"
        }
    }
}
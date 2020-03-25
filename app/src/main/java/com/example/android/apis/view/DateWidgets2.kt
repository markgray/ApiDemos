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

import android.os.Bundle
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Shows the use of an inline TimePicker. Neat on M lame on J
 */
class DateWidgets2 : AppCompatActivity() {
    /**
     * where we display the selected date and time
     */
    private var mTimeDisplay: TextView? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.date_widgets_example_2.
     * Set initialize [TimePicker] `val timePicker` by finding the view with ID R.id.timePicker in
     * our layout, set its current hour to 12 and current minute to 15. Then we initialize our
     * [TextView] field [mTimeDisplay] by finding the view with ID R.id.dateDisplay. We call our
     * method [updateDisplay] to display the hour of 12, and minute of 15 in [mTimeDisplay].
     * Finally we set the `OnTimeChangedListener` of `timePicker` to an anonymous class which calls
     * our method [updateDisplay] to display the hour and minute that the user has chosen.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    @Suppress("UNUSED_ANONYMOUS_PARAMETER", "DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.date_widgets_example_2)
        val timePicker = findViewById<TimePicker>(R.id.timePicker)
        timePicker.currentHour = 12
        timePicker.currentMinute = 15
        mTimeDisplay = findViewById(R.id.dateDisplay)
        updateDisplay(12, 15)
        timePicker.setOnTimeChangedListener {
            view,
            hourOfDay,
            minute ->

            /**
             * The callback interface used to indicate the time has been adjusted. We call our method
             * [updateDisplay] to display the `hourOfDay` and `minute` that the user has chosen.
             *
             * @param view The view associated with this listener.
             * @param hourOfDay The current hour.
             * @param minute The current minute.
             */
            updateDisplay(hourOfDay, minute)
        }
    }

    /**
     * Sets the text of [TextView] field [mTimeDisplay] to a string built to display our parameters.
     *
     * @param hourOfDay hour of the day to display
     * @param minute    minute of the hour to display
     */
    private fun updateDisplay(hourOfDay: Int, minute: Int) {
        mTimeDisplay!!.text = StringBuilder()
                .append(pad(hourOfDay)).append(":")
                .append(pad(minute))
    }

    companion object {
        /**
         * Converts int to String, and Zero Pads numbers under 10. If our parameter [c] is greater
         * than of equal to 10 we just return the String version of [c], otherwise we append a "0"
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
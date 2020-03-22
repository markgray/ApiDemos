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
import android.os.SystemClock
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import androidx.appcompat.app.AppCompatActivity

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R

/**
 * Demonstrates the Chronometer class.
 */
@Suppress("MemberVisibilityCanBePrivate")
class ChronometerDemo : AppCompatActivity() {
    /**
     * `Chronometer` in our layout file with ID R.id.chronometer
     */
    var mChronometer: Chronometer? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.chronometer. We
     * initialize our field `Chronometer mChronometer` by locating the view in our layout with
     * ID R.id.chronometer. We declare `Button button`, then proceed to use it to set the
     * `OnClickListener` for the five buttons in our layout:
     *
     *  *
     * R.id.start "Start" - `OnClickListener mStartListener`
     *
     *  *
     * R.id.stop "Stop" - `OnClickListener mStopListener`
     *
     *  *
     * R.id.reset "Reset" - `OnClickListener mResetListener`
     *
     *  *
     * R.id.set_format "Set format string" - `OnClickListener mSetFormatListener`
     *
     *  *
     * R.id.clear_format "Clear format string" - `OnClickListener mClearFormatListener`
     *
     *
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chronometer)
        mChronometer = findViewById(R.id.chronometer)

        // Watch for button clicks.
        var button: Button = findViewById(R.id.start)
        button.setOnClickListener(mStartListener)
        button = findViewById(R.id.stop)
        button.setOnClickListener(mStopListener)
        button = findViewById(R.id.reset)
        button.setOnClickListener(mResetListener)
        button = findViewById(R.id.set_format)
        button.setOnClickListener(mSetFormatListener)
        button = findViewById(R.id.clear_format)
        button.setOnClickListener(mClearFormatListener)
    }

    /**
     * `OnClickListener` for the button R.id.start "Start"
     */
    /**
     * Called when the button R.id.start "Start" is clicked.
     *
     *  v view that was clicked
     */
    var mStartListener: View.OnClickListener = View.OnClickListener {
        mChronometer!!.start()
    }

    /**
     * `OnClickListener` for the button R.id.stop "Stop"
     */
    /**
     * Called when the button R.id.stop "Stop" is clicked. We simply call the `stop` method
     * of `Chronometer mChronometer`.
     *
     *  v view that was clicked
     */
    var mStopListener: View.OnClickListener = View.OnClickListener { mChronometer!!.stop() }

    /**
     * `OnClickListener` for the button R.id.reset "Reset"
     */
    /**
     * Called when the button R.id.reset "Reset" is clicked. We simply call the `setBase`
     * method of `Chronometer mChronometer` with the elapsed milliseconds since boot as
     * its argument.
     *
     *  v view that was clicked
     */
    var mResetListener: View.OnClickListener = View.OnClickListener { mChronometer!!.base = SystemClock.elapsedRealtime() }

    /**
     * `OnClickListener` for the button R.id.set_format "Set format string"
     */
    /**
     * Called when the button R.id.set_format "Set format string" is clicked. We call the method
     * `setFormat` of `Chronometer mChronometer` with the string "Formatted time (%s)".
     *
     *  v view that was clicked
     */
    var mSetFormatListener: View.OnClickListener = View.OnClickListener{ mChronometer!!.format = "Formatted time (%s)"
    }

    /**
     * `OnClickListener` for the button R.id.clear_format "Clear format string"
     */
    /**
     * Called when the button R.id.clear_format "Clear format string" is clicked. We call the
     * method `setFormat` of `Chronometer mChronometer` with null as the argument.
     *
     *  v view that was clicked
     */
    var mClearFormatListener: View.OnClickListener = View.OnClickListener { mChronometer!!.format = null }
}
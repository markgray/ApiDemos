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

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.apis.R;

/**
 * Demonstrates the Chronometer class.
 */
public class ChronometerDemo extends AppCompatActivity {
    /**
     * {@code Chronometer} in our layout file with ID R.id.chronometer
     */
    Chronometer mChronometer;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.chronometer. We
     * initialize our field {@code Chronometer mChronometer} by locating the view in our layout with
     * ID R.id.chronometer. We declare {@code Button button}, then proceed to use it to set the
     * {@code OnClickListener} for the five buttons in our layout:
     * <ul>
     * <li>
     * R.id.start "Start" - {@code OnClickListener mStartListener}
     * </li>
     * <li>
     * R.id.stop "Stop" - {@code OnClickListener mStopListener}
     * </li>
     * <li>
     * R.id.reset "Reset" - {@code OnClickListener mResetListener}
     * </li>
     * <li>
     * R.id.set_format "Set format string" - {@code OnClickListener mSetFormatListener}
     * </li>
     * <li>
     * R.id.clear_format "Clear format string" - {@code OnClickListener mClearFormatListener}
     * </li>
     * </ul>
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chronometer);

        mChronometer = findViewById(R.id.chronometer);

        Button button;

        // Watch for button clicks.
        button = findViewById(R.id.start);
        button.setOnClickListener(mStartListener);

        button = findViewById(R.id.stop);
        button.setOnClickListener(mStopListener);

        button = findViewById(R.id.reset);
        button.setOnClickListener(mResetListener);

        button = findViewById(R.id.set_format);
        button.setOnClickListener(mSetFormatListener);

        button = findViewById(R.id.clear_format);
        button.setOnClickListener(mClearFormatListener);
    }

    /**
     * {@code OnClickListener} for the button R.id.start "Start"
     */
    View.OnClickListener mStartListener = new OnClickListener() {
        /**
         * Called when the button R.id.start "Start" is clicked.
         *
         * @param v view that was clicked
         */
        @Override
        public void onClick(View v) {
            mChronometer.start();
        }
    };

    /**
     * {@code OnClickListener} for the button R.id.stop "Stop"
     */
    View.OnClickListener mStopListener = new OnClickListener() {
        /**
         * Called when the button R.id.stop "Stop" is clicked. We simply call the {@code stop} method
         * of {@code Chronometer mChronometer}.
         *
         * @param v view that was clicked
         */
        @Override
        public void onClick(View v) {
            mChronometer.stop();
        }
    };

    /**
     * {@code OnClickListener} for the button R.id.reset "Reset"
     */
    View.OnClickListener mResetListener = new OnClickListener() {
        /**
         * Called when the button R.id.reset "Reset" is clicked. We simply call the {@code setBase}
         * method of {@code Chronometer mChronometer} with the elapsed milliseconds since boot as
         * its argument.
         *
         * @param v view that was clicked
         */
        @Override
        public void onClick(View v) {
            mChronometer.setBase(SystemClock.elapsedRealtime());
        }
    };

    /**
     * {@code OnClickListener} for the button R.id.set_format "Set format string"
     */
    View.OnClickListener mSetFormatListener = new OnClickListener() {
        /**
         * Called when the button R.id.set_format "Set format string" is clicked. We call the method
         * {@code setFormat} of {@code Chronometer mChronometer} with the string "Formatted time (%s)".
         *
         * @param v view that was clicked
         */
        @Override
        public void onClick(View v) {
            mChronometer.setFormat("Formatted time (%s)");
        }
    };

    /**
     * {@code OnClickListener} for the button R.id.clear_format "Clear format string"
     */
    View.OnClickListener mClearFormatListener = new OnClickListener() {
        /**
         * Called when the button R.id.clear_format "Clear format string" is clicked. We call the
         * method {@code setFormat} of {@code Chronometer mChronometer} with null as the argument.
         *
         * @param v view that was clicked
         */
        @Override
        public void onClick(View v) {
            mChronometer.setFormat(null);
        }
    };
}

/*
 * Copyright (C) 20013The Android Open Source Project
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
package com.example.android.apis.hardware

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.ConsumerIrManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R

/**
 * App that transmits an IR code
 *
 * This demonstrates the [ConsumerIrManager] class.
 *
 * Demo Hardware / Consumer IR
 *
 * Source files:
 *  * hardware/ConsumerIr.kt Consumer IR demo
 *  * res/layout/consumer_ir.xml Defines contents of the screen
 */
@Suppress("MemberVisibilityCanBePrivate")
class ConsumerIr : AppCompatActivity() {
    /**
     * Used to display the results of interacting with the [ConsumerIrManager]
     */
    var mFreqsText: TextView? = null

    /**
     * Our instance of the system level service [Context.CONSUMER_IR_SERVICE] (used for transmitting
     * infrared signals from the devices IR device).
     */
    var mCIR: ConsumerIrManager? = null

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`.
     * We initialize our [ConsumerIrManager] field [mCIR] with an instance of the system level service
     * [Context.CONSUMER_IR_SERVICE] (used for transmitting infrared signals from the deviced IR
     * device). Then we set our content view to our layout file R.layout.consumer_ir, and set the
     * [View.OnClickListener] of the button in our layout with id R.id.send_button to our
     * [View.OnClickListener] field [mSendClickListener], and set the [View.OnClickListener] of
     * the button in our layout with id R.id.get_freqs_button to our [View.OnClickListener] field
     * [mGetFreqsClickListener]. Finally we initialize our [TextView] field [mFreqsText] to the
     * [TextView] with id R.id.freqs_text (used to display the results of interacting with the
     * [ConsumerIrManager]).
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState)

        // Get a reference to the ConsumerIrManager
        mCIR = getSystemService(CONSUMER_IR_SERVICE) as ConsumerIrManager?

        // See /res/layout/consumer_ir.xml for this
        // view layout definition, which is being set here as
        // the content of our screen.
        setContentView(R.layout.consumer_ir)

        // Set the OnClickListener for the button so we see when it's pressed.
        findViewById<View>(R.id.send_button).setOnClickListener(mSendClickListener)
        findViewById<View>(R.id.get_freqs_button).setOnClickListener(mGetFreqsClickListener)
        mFreqsText = findViewById(R.id.freqs_text)
    }

    /**
     * [View.OnClickListener] for the [Button] with id R.id.send_button, it transmits an infrared
     * pattern if our device has an infrared emitter.
     */
    var mSendClickListener: View.OnClickListener = View.OnClickListener {
        if (mCIR == null ||!mCIR!!.hasIrEmitter()) {
            Log.e(TAG, "No IR Emitter found\n")
            return@OnClickListener
        }

        // A pattern of alternating series of carrier on and off periods measured in
        // microseconds.
        val pattern = intArrayOf(1901, 4453, 625, 1614, 625, 1588, 625, 1614, 625, 442, 625,
                442, 625, 468, 625, 442, 625, 494, 572, 1614, 625, 1588, 625, 1614, 625, 494, 572,
                442, 651, 442, 625, 442, 625, 442, 625, 1614, 625, 1588, 651, 1588, 625, 442, 625,
                494, 598, 442, 625, 442, 625, 520, 572, 442, 625, 442, 625, 442, 651, 1588, 625,
                1614, 625, 1588, 625, 1614, 625, 1588, 625, 48958)

        // transmit the pattern at 38.4KHz
        mCIR!!.transmit(38400, pattern)
    }

    /**
     * [View.OnClickListener] for the [Button] with id R.id.get_freqs_button, it queries the
     * [ConsumerIrManager] for the infrared transmitter's supported carrier frequencies and
     * displays them in [TextView] field [mFreqsText] if the query is successful.
     */
    @SuppressLint("SetTextI18n", "DefaultLocale")
    var mGetFreqsClickListener: View.OnClickListener = View.OnClickListener {
        val b = StringBuilder()
        if (mCIR == null || !mCIR!!.hasIrEmitter()) {
            mFreqsText!!.text = "No IR Emitter found!"
            Log.e(TAG, "No IR Emitter found!\n")
            return@OnClickListener
        }

        // Get the available carrier frequency ranges
        val freqs = mCIR!!.carrierFrequencies
        b.append("IR Carrier Frequencies:\n")
        for (range in freqs) {
            b.append(String.format("    %d - %d\n", range.minFrequency, range.maxFrequency))
        }
        mFreqsText!!.text = b.toString()
    }

    companion object {
        /**
         * TAG used for logging.
         */
        private const val TAG = "ConsumerIrTest"
    }
}
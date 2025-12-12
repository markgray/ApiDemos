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
@file:Suppress("unused", "ReplaceNotNullAssertionWithElvisReturn")

package com.example.android.apis.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Demonstrates how to use a seek bar. It implements [SeekBar.OnSeekBarChangeListener] which has
 * the three call backs: `onProgressChanged`, `onStartTrackingTouch`, and `onStopTrackingTouch`
 * displaying appropriate text based on the information passed it.
 */
class SeekBar1 : AppCompatActivity(), OnSeekBarChangeListener {
    /**
     * The [SeekBar] in our layout with id R.id.seek that we are `OnSeekBarChangeListener`
     * for in this demo
     */
    private var mSeekBar: SeekBar? = null

    /**
     * [TextView] we use to display the position of [SeekBar] field [mSeekBar]
     */
    private var mProgressText: TextView? = null

    /**
     * [TextView] we display whether we are tracking ("Tracking on" user has started a touch
     * gesture) or not ("Tracking off" user has finished a touch gesture).
     */
    private var mTrackingText: TextView? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.seekbar_1. We
     * initialize our [SeekBar] field [mSeekBar] by finding the view with id R.id.seek and set
     * its `OnSeekBarChangeListener` to this. We initialized our [TextView] field [mProgressText]
     * by finding the view with id R.id.progress, and [TextView] field [mTrackingText] by finding
     * the view with id R.id.tracking. We find the [CheckBox] with id R.id.enabled to initialize
     * our variable `val checkBox` and set its `OnCheckedChangeListener` to an a lambda which
     * enables or disables all three [SeekBar]'s in our layout based on whether the [CheckBox] is
     * checked or unchecked.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.seekbar_1)
        mSeekBar = findViewById(R.id.seek)
        mSeekBar!!.setOnSeekBarChangeListener(this)
        mProgressText = findViewById(R.id.progress)
        mTrackingText = findViewById(R.id.tracking)
        val checkBox = findViewById<View>(R.id.enabled) as CheckBox
        checkBox.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->

            /**
             * Called when the [CheckBox] with id R.id.enabled changes state. We find
             * the views with id R.id.seekMin and R.id.seekMax and set their enabled state
             * to the new state of the [CheckBox] given by our parameter [isChecked] and do
             * the same for our [SeekBar] field [mSeekBar].
             *
             * @param buttonView [CheckBox] which has changed state
             * @param isChecked true if the [CheckBox] is not checked, false otherwise
             */
            findViewById<View>(R.id.seekMin).isEnabled = isChecked
            findViewById<View>(R.id.seekMax).isEnabled = isChecked
            mSeekBar!!.isEnabled = isChecked
        }
    }

    /**
     * Notification that the progress level has changed. We set the text of [TextView] field
     * [mProgressText] to a string formed from the string value of our [Int] parameter [progress]
     * concatenated with a space followed by the string with the resource id R.string.seekbar_from_touch
     * ("from touch") followed by the string "=" followed by the string value of our parameter
     * [fromTouch] (true or false).
     *
     * @param seekBar   The [SeekBar] whose progress has changed
     * @param progress  The current progress level.
     * @param fromTouch True if the progress change was initiated by the user.
     */
    @SuppressLint("SetTextI18n")
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromTouch: Boolean) {
        mProgressText!!.text =
            progress.toString() + " " + getString(R.string.seekbar_from_touch) + "=" + fromTouch
    }

    /**
     * Notification that the user has started a touch gesture. We set the text of our [TextView]
     * field [mTrackingText] to the string with the resource id R.string.seekbar_tracking_on
     * ("Tracking on").
     *
     * @param seekBar The [SeekBar] in which the touch gesture began
     */
    override fun onStartTrackingTouch(seekBar: SeekBar) {
        mTrackingText!!.text = getString(R.string.seekbar_tracking_on)
    }

    /**
     * Notification that the user has finished a touch gesture. We set the text of our [TextView]
     * field [mTrackingText] to the string with the resource id R.string.seekbar_tracking_off
     * ("Tracking off").
     *
     * @param seekBar The [SeekBar] in which the touch gesture began
     */
    override fun onStopTrackingTouch(seekBar: SeekBar) {
        mTrackingText!!.text = getString(R.string.seekbar_tracking_off)
    }
}
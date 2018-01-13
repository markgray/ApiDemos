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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.android.apis.R;

/**
 * Demonstrates how to use a seek bar. It implements SeekBar.OnSeekBarChangeListener which
 * has the three call backs: onProgressChanged, onStartTrackingTouch, and onStopTrackingTouch
 * displaying appropriate text based on the information passed it.
 */
public class SeekBar1 extends Activity implements SeekBar.OnSeekBarChangeListener {
    /**
     * The {@code SeekBar} in our layout with id R.id.seek that we are {@code OnSeekBarChangeListener}
     * for in this demo
     */
    SeekBar mSeekBar;
    /**
     * {@code TextView} we use to display the position of {@code SeekBar mSeekBar}
     */
    TextView mProgressText;
    /**
     * {@code TextView} we display whether we are tracking ("Tracking on" user has started a touch
     * gesture) or not ("Tracking off" user has finished a touch gesture).
     */
    TextView mTrackingText;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.seekbar_1. We
     * initialize our field {@code SeekBar mSeekBar} by finding the view with id R.id.seek and set
     * its {@code OnSeekBarChangeListener} to this. We initialized our field {@code TextView mProgressText}
     * by finding the view with id R.id.progress, and {@code TextView mTrackingText} by finding the
     * view with id R.id.tracking. We find the {@code CheckBox} with id R.id.enabled and set its
     * {@code OnCheckedChangeListener} to an anonymous class which enables or disables all three
     * SeekBar's in our layout based on whether the {@code CheckBox} is checked or unchecked.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seekbar_1);

        mSeekBar = (SeekBar) findViewById(R.id.seek);
        mSeekBar.setOnSeekBarChangeListener(this);
        mProgressText = (TextView) findViewById(R.id.progress);
        mTrackingText = (TextView) findViewById(R.id.tracking);

        ((CheckBox) findViewById(R.id.enabled)).setOnCheckedChangeListener(
                new OnCheckedChangeListener() {
                    /**
                     * Called when the {@code CheckBox} with id R.id.enabled changes state. We find
                     * the views with id R.id.seekMin and R.id.seekMax and set their enabled state
                     * to the new state of the {@code CheckBox} given by our parameter {@code isChecked}
                     * and do the same for our field {@code SeekBar mSeekBar}.
                     *
                     * @param buttonView {@code CheckBox} which has changed state
                     * @param isChecked true if the {@code CheckBox} is not checked, false otherwise
                     */
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        findViewById(R.id.seekMin).setEnabled(isChecked);
                        findViewById(R.id.seekMax).setEnabled(isChecked);
                        mSeekBar.setEnabled(isChecked);
                    }
                });
    }

    /**
     * Notification that the progress level has changed. We set the text of {@code TextView mProgressText}
     * to a string formed from the string value of our parameter {@code progress} concatenated with a
     * space followed by the string with the resource id R.string.seekbar_from_touch ("from touch")
     * followed by the string "=" followed by the string value of our parameter {@code fromTouch}
     * (true or false).
     *
     * @param seekBar   The SeekBar whose progress has changed
     * @param progress  The current progress level.
     * @param fromTouch True if the progress change was initiated by the user.
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
        mProgressText.setText(progress + " " +
                getString(R.string.seekbar_from_touch) + "=" + fromTouch);
    }

    /**
     * Notification that the user has started a touch gesture. We set the text of our field
     * {@code TextView mTrackingText} to the string with the resource id R.string.seekbar_tracking_on
     * ("Tracking on").
     *
     * @param seekBar The SeekBar in which the touch gesture began
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mTrackingText.setText(getString(R.string.seekbar_tracking_on));
    }

    /**
     * Notification that the user has finished a touch gesture. We set the text of our field
     * {@code TextView mTrackingText} to the string with the resource id R.string.seekbar_tracking_off
     * ("Tracking off").
     *
     * @param seekBar The SeekBar in which the touch gesture began
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mTrackingText.setText(getString(R.string.seekbar_tracking_off));
    }
}

/*
 * Copyright (C) 2010 The Android Open Source Project
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

import com.example.android.apis.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;

/**
 * This application demonstrates the ability to transform views in 2D and 3D,
 * scaling them, translating them, and rotating them (in 2D and 3D). Use the
 * seek bars to set the various transform properties of the button. It sets the
 * SeekBar.OnSeekBarChangeListener of each SeekBar to morph the button using
 * setTranslationX, setTranslationY, setScaleX, setScaleY, setRotationX, setRotationY,
 * and setRotation. Good demo of SeekBar.
 */
public class RotatingButton extends Activity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.rotating_view.
     * We initialize our variable {@code Button rotatingButton} by finding the view with the ID
     * R.id.rotatingButton. We declare our variable {@code SeekBar seekBar} then set it by finding
     * the view with the ID R.id.translationX, set its upper range to 400, then set its
     * {@code OnSeekBarChangeListener} to an anonymous class which sets the horizontal location
     * {@code rotatingButton} relative to its left position based on the value set in the {@code SeekBar}
     * by the user. We again set {@code seekBar} by finding the view with the ID R.id.translationY,
     * set its upper range to 800, then set its {@code OnSeekBarChangeListener} to an anonymous class
     * which sets the vertical location {@code rotatingButton} relative to its top position based on
     * the value set in the {@code SeekBar} by the user. We again set {@code seekBar} by finding the
     * view with the ID R.id.scaleX, set its upper range to 50, set its current progress to 10 then
     * set its {@code OnSeekBarChangeListener} to an anonymous class which scales the X size of
     * {@code rotatingButton} as a proportion of the view's unscaled width based on the value set in
     * the {@code SeekBar} by the user divided by 10. We again set {@code seekBar} by finding the
     * view with the ID R.id.scaleY, set its upper range to 50, set its current progress to 10 then
     * set its {@code OnSeekBarChangeListener} to an anonymous class which scales the Y size of
     * {@code rotatingButton} as a proportion of the view's unscaled height based on the value set in
     * the {@code SeekBar} by the user divided by 10.
     * <p>
     * We again set {@code seekBar} by finding the view with the ID R.id.rotationX, set its upper
     * range to 360, then set its {@code OnSeekBarChangeListener} to an anonymous class which sets
     * the degrees that {@code rotatingButton} is rotated around the horizontal axis through the
     * pivot point to the value set in the {@code SeekBar} by the user.
     * <p>
     * We again set {@code seekBar} by finding the view with the ID R.id.rotationY, set its upper
     * range to 360, then set its {@code OnSeekBarChangeListener} to an anonymous class which sets
     * the degrees that {@code rotatingButton} is rotated around the vertical axis through the
     * pivot point to the value set in the {@code SeekBar} by the user.
     * <p>
     * We again set {@code seekBar} by finding the view with the ID R.id.rotationZ, set its upper
     * range to 360, then set its {@code OnSeekBarChangeListener} to an anonymous class which sets
     * the degrees that {@code rotatingButton} is around the pivot point to the value set in the
     * {@code SeekBar} by the user.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rotating_view);

        final Button rotatingButton = (Button) findViewById(R.id.rotatingButton);
        SeekBar seekBar;
        seekBar = (SeekBar) findViewById(R.id.translationX);
        seekBar.setMax(400);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rotatingButton.setTranslationX((float) progress);
            }
        });
        seekBar = (SeekBar) findViewById(R.id.translationY);
        seekBar.setMax(800);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rotatingButton.setTranslationY((float) progress);
            }
        });
        seekBar = (SeekBar) findViewById(R.id.scaleX);
        seekBar.setMax(50);
        seekBar.setProgress(10);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rotatingButton.setScaleX((float) progress / 10f);
            }
        });
        seekBar = (SeekBar) findViewById(R.id.scaleY);
        seekBar.setMax(50);
        seekBar.setProgress(10);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rotatingButton.setScaleY((float) progress / 10f);
            }
        });
        seekBar = (SeekBar) findViewById(R.id.rotationX);
        seekBar.setMax(360);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // prevent seeking on app creation
                rotatingButton.setRotationX((float) progress);
            }
        });
        seekBar = (SeekBar) findViewById(R.id.rotationY);
        seekBar.setMax(360);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // prevent seeking on app creation
                rotatingButton.setRotationY((float) progress);
            }
        });
        seekBar = (SeekBar) findViewById(R.id.rotationZ);
        seekBar.setMax(360);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // prevent seeking on app creation
                rotatingButton.setRotation((float) progress);
            }
        });
    }
}
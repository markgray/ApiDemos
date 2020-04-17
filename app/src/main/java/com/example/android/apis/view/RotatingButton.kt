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
package com.example.android.apis.view

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * This application demonstrates the ability to transform views in 2D and 3D,
 * scaling them, translating them, and rotating them (in 2D and 3D). Use the
 * seek bars to set the various transform properties of the button. It sets the
 * [SeekBar.OnSeekBarChangeListener] of each [SeekBar] to morph the button using
 * `setTranslationX`, `setTranslationY`, `setScaleX`, `setScaleY`, `setRotationX`,
 * `setRotationY`, and `setRotation`. Good demo of [SeekBar].
 */
class RotatingButton : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.rotating_view.
     * We initialize our [Button] variable `val rotatingButton` by finding the view with the ID
     * R.id.rotatingButton. We initialize our [SeekBar] variable `val seekBar` by finding
     * the view with the ID R.id.translationX, set its upper range to 400, then set its
     * `OnSeekBarChangeListener` to an anonymous class which sets the horizontal location of
     * `rotatingButton` relative to its left position based on the value set in the [SeekBar]
     * by the user. We again set `seekBar` by finding the view with the ID R.id.translationY,
     * set its upper range to 800, then set its `OnSeekBarChangeListener` to an anonymous class
     * which sets the vertical location of `rotatingButton` relative to its top position based on
     * the value set in the [SeekBar] by the user. We again set `seekBar` by finding the
     * view with the ID R.id.scaleX, set its upper range to 50, set its current progress to 10 then
     * set its `OnSeekBarChangeListener` to an anonymous class which scales the X size of
     * `rotatingButton` as a proportion of the view's unscaled width based on the value set in
     * the [SeekBar] by the user divided by 10. We again set `seekBar` by finding the view with
     * the ID R.id.scaleY, set its upper range to 50, set its current progress to 10 then set its
     * `OnSeekBarChangeListener` to an anonymous class which scales the Y size of `rotatingButton`
     * as a proportion of the view's unscaled height based on the value set in the [SeekBar] by the
     * user divided by 10.
     *
     * We again set `seekBar` by finding the view with the ID R.id.rotationX, set its upper
     * range to 360, then set its `OnSeekBarChangeListener` to an anonymous class which sets
     * the degrees that `rotatingButton` is rotated around the horizontal axis through the
     * pivot point to the value set in the [SeekBar] by the user.
     *
     * We again set `seekBar` by finding the view with the ID R.id.rotationY, set its upper
     * range to 360, then set its `OnSeekBarChangeListener` to an anonymous class which sets
     * the degrees that `rotatingButton` is rotated around the vertical axis through the
     * pivot point to the value set in the [SeekBar] by the user.
     *
     * We again set `seekBar` by finding the view with the ID R.id.rotationZ, set its upper
     * range to 360, then set its `OnSeekBarChangeListener` to an anonymous class which sets
     * the degrees that `rotatingButton` is rotated around the pivot point to the value set in
     * the [SeekBar] by the user.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState]` so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rotating_view)
        val rotatingButton = findViewById<Button>(R.id.rotatingButton)
        var seekBar: SeekBar = findViewById(R.id.translationX)
        seekBar.max = 400
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                rotatingButton.translationX = progress.toFloat()
            }
        })
        seekBar = findViewById(R.id.translationY)
        seekBar.max = 800
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                rotatingButton.translationY = progress.toFloat()
            }
        })
        seekBar = findViewById(R.id.scaleX)
        seekBar.max = 50
        seekBar.progress = 10
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                rotatingButton.scaleX = progress.toFloat() / 10f
            }
        })
        seekBar = findViewById(R.id.scaleY)
        seekBar.max = 50
        seekBar.progress = 10
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                rotatingButton.scaleY = progress.toFloat() / 10f
            }
        })
        seekBar = findViewById(R.id.rotationX)
        seekBar.max = 360
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // prevent seeking on app creation
                rotatingButton.rotationX = progress.toFloat()
            }
        })
        seekBar = findViewById(R.id.rotationY)
        seekBar.max = 360
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // prevent seeking on app creation
                rotatingButton.rotationY = progress.toFloat()
            }
        })
        seekBar = findViewById(R.id.rotationZ)
        seekBar.max = 360
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // prevent seeking on app creation
                rotatingButton.rotation = progress.toFloat()
            }
        })
    }
}
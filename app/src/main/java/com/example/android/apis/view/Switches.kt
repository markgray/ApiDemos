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

import android.os.Bundle
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.example.android.apis.R

/**
 * Demonstrates the use of the toggle switch widget. Customized text removed for Lollipop
 * unless attribute android:showText="true" is added
 */
class Switches : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.switches. We initialize
     * our [SwitchCompat] variable `val s` by finding the view with id R.id.monitored_switch. If `s`
     * is not null, we set its `OnCheckedChangeListener` to "this".
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.switches)
        val s = findViewById<SwitchCompat>(R.id.monitored_switch)
        s?.setOnCheckedChangeListener(this)
    }

    /**
     * Called when the checked state of a compound button has changed. We create and show a toast
     * displaying the string created by concatenating the string "Monitored switch is " with the
     * string "on" if our parameter [isChecked] is true, or "off" if it is false.
     *
     * @param buttonView The compound button view whose state has changed.
     * @param isChecked  The new checked state of buttonView.
     */
    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        Toast.makeText(
            /* context = */ this,
            /* text = */ "Monitored switch is ${if (isChecked) "on" else "off"}",
            /* duration = */ Toast.LENGTH_SHORT
        ).show()
    }
}
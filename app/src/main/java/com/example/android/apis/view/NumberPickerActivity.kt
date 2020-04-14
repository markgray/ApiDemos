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
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Demonstrates how to use a NumberPicker
 */
class NumberPickerActivity : AppCompatActivity() {

    /**
     * The [NumberPicker] widget in our layout file with ID R.id.number_picker
     */
    private lateinit var mNumberPicker: NumberPicker

    /**
     * Called when our activity is starting. First we call our super's implementation of `onCreate`.
     * We set our content view to our layout file R.layout.number_picker, initialize our [NumberPicker]
     * field [mNumberPicker] by finding the view in our layout file with ID R.id.number_picker, and
     * set its maximum value to 30. We find the [CheckBox] with id R.id.enabled and set its
     * `OnCheckedChangeListener` to a lambda which enables [mNumberPicker] if its `isChecked`
     * parameter is true or disables it if it is false.
     *
     * @param savedInstanceState We do not override [onSaveInstanceState] so do not use
     */
    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.number_picker)
        mNumberPicker = findViewById(R.id.number_picker)
        mNumberPicker.maxValue = 30
        (findViewById<View>(R.id.enabled) as CheckBox).setOnCheckedChangeListener {
            buttonView: CompoundButton?, isChecked: Boolean ->
            mNumberPicker.isEnabled = isChecked
        }
    }
}
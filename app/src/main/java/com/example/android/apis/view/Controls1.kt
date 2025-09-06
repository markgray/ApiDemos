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
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

import com.example.android.apis.R

/**
 * A gallery of basic controls: [Button], `EditText`, `RadioButton`, `Checkbox`, `Spinner` and
 * `Switch` if v14+. This example uses the light theme which is set using the activity attribute
 * android:theme="@style/Theme.AppCompat.Light" in AndroidManifest.xml
 */
open class Controls1 : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.controls_1. We set
     * [Button] `val disabledButton` by finding the view with ID R.id.button_disabled in our layout
     * and disable it (in order to show what a disabled button looks like). We set [Spinner] `val s1`
     * by finding the view in our layout with ID R.id.spinner1, create `ArrayAdapter<String>`
     * `val adapter` from our string array field [mStrings] using android.R.layout.simple_spinner_item
     * as the resource ID for a layout file containing a `TextView` to use when instantiating views,
     * and set its layout resource defining the drop down views to be the system layout file
     * android.R.layout.simple_spinner_dropdown_item. Finally we set `adapter` as the adapter for
     * `s1`.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.controls_1)
        val disabledButton = findViewById<Button>(R.id.button_disabled)
        disabledButton.isEnabled = false
        val s1 = findViewById<Spinner>(R.id.spinner1)
        val adapter = ArrayAdapter(
            /* context = */ this,
            /* resource = */ android.R.layout.simple_spinner_item,
            /* objects = */ mStrings
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        s1.adapter = adapter
    }

    companion object {
        /**
         * String array used to populate the `Spinner` with ID R.id.spinner1
         */
        private val mStrings = arrayOf(
            "Mercury", "Venus", "Earth", "Mars", "Jupiter", "Saturn", "Uranus", "Neptune"
        )
    }
}
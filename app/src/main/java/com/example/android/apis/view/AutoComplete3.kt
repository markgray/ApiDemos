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
import android.widget.AutoCompleteTextView
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Shows how to use an [AutoCompleteTextView] to provide suggestions as a user types. The entire very
 * long layout is inside a [ScrollView] that you have to scroll down to see the [AutoCompleteTextView]'s,
 * which control the [ScrollView] as they need to when presenting the suggestion list.
 */
class AutoComplete3 : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.autocomplete_3.
     * We create `ArrayAdapter<String>` `val adapter` using the array [AutoComplete1.COUNTRIES]
     * as the data and android.R.layout.simple_dropdown_item_1line as the resource ID for the layout
     * file which contains a TextView to use when instantiating views. We initialize our variable
     * [AutoCompleteTextView] `val textView` by finding the view with ID R.id.edit, and set its
     * adapter to `adapter`. Next we locate the view with ID R.id.edit2 to set `textView`,
     * and set its adapter to `adapter` as well.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.autocomplete_3)
        val adapter = ArrayAdapter(
            /* context = */ this,
            /* resource = */ android.R.layout.simple_dropdown_item_1line,
            /* objects = */ AutoComplete1.COUNTRIES
        )
        var textView = findViewById<AutoCompleteTextView>(R.id.edit)
        textView.setAdapter(adapter)
        textView = findViewById(R.id.edit2)
        textView.setAdapter(adapter)
    }
}
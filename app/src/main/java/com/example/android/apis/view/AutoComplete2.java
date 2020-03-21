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

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.apis.R;

/**
 * Shows how to use an AutoCompleteTextView to provide suggestions as a user  types. The
 * AutoCompleteTextView is located at the bottom of the screen, so the suggestions appear
 * in a pop-up list.
 */
public class AutoComplete2 extends AppCompatActivity {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.autocomplete_2.
     * We create {@code ArrayAdapter<String> adapter} using the array {@code AutoComplete1.COUNTRIES}
     * as the data and android.R.layout.simple_dropdown_item_1line as the resource ID for the layout
     * file which contains a TextView to use when instantiating views. We initialize our variable
     * {@code AutoCompleteTextView textView} by finding the view with ID R.id.edit, and set its
     * adapter to {@code adapter}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.autocomplete_2);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                AutoComplete1.COUNTRIES);
        AutoCompleteTextView textView = findViewById(R.id.edit);
        textView.setAdapter(adapter);
    }
}

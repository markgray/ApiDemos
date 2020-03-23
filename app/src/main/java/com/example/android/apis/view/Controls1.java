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

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.apis.R;


/**
 * A gallery of basic controls: Button, EditText, RadioButton, Checkbox, Spinner and switch if v14+.
 * This example uses the light theme which is set using android:theme="@style/Theme.AppCompat.Light"
 * in AndroidManifest.xml
 */
public class Controls1 extends AppCompatActivity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.controls_1. We set
     * {@code Button disabledButton} by finding the view with ID R.id.button_disabled in our layout
     * and disable it (in order to show what a disabled button looks like). We set {@code Spinner s1}
     * by finding the view in our layout with ID R.id.spinner1, create {@code ArrayAdapter<String> adapter}
     * from our string array {@code String[] mStrings} using android.R.layout.simple_spinner_item as
     * the resource ID for a layout file containing a TextView to use when instantiating views, and
     * set its layout resource defining the drop down views to be android.R.layout.simple_spinner_dropdown_item.
     * Finally we set {@code adapter} as the adapter for {@code s1}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.controls_1);

        Button disabledButton = findViewById(R.id.button_disabled);
        disabledButton.setEnabled(false);

        Spinner s1 = findViewById(R.id.spinner1);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mStrings);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s1.setAdapter(adapter);
    }

    /**
     * String array used to populate the spinner with ID R.id.spinner1
     */
    private static final String[] mStrings = {
            "Mercury", "Venus", "Earth", "Mars", "Jupiter", "Saturn", "Uranus", "Neptune"
    };
}

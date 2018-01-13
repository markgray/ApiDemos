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

import android.app.Activity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.android.apis.R;

/**
 * Demonstrates the use of the toggle switch widget. Customized text removed for Lollipop
 * unless attribute android:showText="true" is added
 */
public class Switches extends Activity implements CompoundButton.OnCheckedChangeListener {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.switches. We
     * initialize our variable {@code Switch s} by finding the view with id R.id.monitored_switch.
     * If {@code s} is not null, we set its {@code OnCheckedChangeListener} to "this".
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.switches);

        Switch s = (Switch) findViewById(R.id.monitored_switch);
        if (s != null) {
            s.setOnCheckedChangeListener(this);
        }
    }

    /**
     * Called when the checked state of a compound button has changed. We create and show a toast
     * displaying the string created by concatenating the string "Monitored switch is " with the
     * string "on" if our parameter {@code isChecked} is true, or "off" if it is false.
     *
     * @param buttonView The compound button view whose state has changed.
     * @param isChecked  The new checked state of buttonView.
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Toast.makeText(this, "Monitored switch is " + (isChecked ? "on" : "off"),
                Toast.LENGTH_SHORT).show();
    }
}

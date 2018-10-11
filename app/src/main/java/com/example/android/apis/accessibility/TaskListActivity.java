/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.example.android.apis.accessibility;

import com.example.android.apis.R;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;

/**
 * Starts up the task list that will interact with the AccessibilityService sample.
 */
public class TaskListActivity extends ListActivity {

    /**
     * An intent for launching the system settings.
     */
    private static final Intent sSettingsIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.tasklist_main. Then
     * we initialize {@code boolean[] checkboxes} with initial values for the {@code TaskAdapter} to
     * apply to its checkboxes, and {@code String[] labels} to the labels it should use. We then ask
     * the {@code TaskAdapter} constructor to construct an adapter from {@code labels} and {@code checkboxes}
     * to initialize our variable {@code TaskAdapter myAdapter} which we then set as our list adapter.
     * We initialize our variable {@code ImageButton button} by finding the view in our layout with
     * the id R.id.button and set its {@code OnClickListener} to an anonymous class which starts the
     * activity specified by our field {@code Intent sSettingsIntent} (an intent for launching the
     * system settings).
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tasklist_main);

        // Hard-coded hand-waving here.
        boolean[] checkboxes = {true, true, false, true, false, false, false};
        String[] labels = {"Take out Trash", "Do Laundry",
                "Conquer World", "Nap", "Do Taxes",
                "Abolish IRS", "Tea with Aunt Sharon"};

        TaskAdapter myAdapter = new TaskAdapter(this, labels, checkboxes);
        this.setListAdapter(myAdapter);

        // Add a shortcut to the accessibility settings.
        ImageButton button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            /**
             * When the button with the id R.id.button is clicked we start the activity specified by
             * our field {@code Intent sSettingsIntent} (an intent for launching the system settings).
             *
             * @param v v{@code View} that was clicked
             */
            @Override
            public void onClick(View v) {
                startActivity(sSettingsIntent);
            }
        });
    }
}

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

package com.example.android.apis.accessibility

import android.app.ListActivity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageButton
import com.example.android.apis.R

/**
 * Starts up the task list that will interact with the AccessibilityService sample.
 */
class TaskListActivity : ListActivity() {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.tasklist_main. Then
     * we initialize `boolean[] checkboxes` with initial values for the `TaskAdapter` to
     * apply to its checkboxes, and `String[] labels` to the labels it should use. We then ask
     * the `TaskAdapter` constructor to construct an adapter from `labels` and `checkboxes`
     * to initialize our variable `TaskAdapter myAdapter` which we then set as our list adapter.
     * We initialize our variable `ImageButton button` by finding the view in our layout with
     * the id R.id.button and set its `OnClickListener` to an anonymous class which starts the
     * activity specified by our field `Intent sSettingsIntent` (an intent for launching the
     * system settings).
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tasklist_main)

        // Hard-coded hand-waving here.
        val checkboxes = booleanArrayOf(true, true, false, true, false, false, false)
        val labels = arrayOf("Take out Trash", "Do Laundry", "Conquer World", "Nap",
                "Do Taxes", "Abolish IRS", "Tea with Aunt Sharon")

        val myAdapter = TaskAdapter(this, labels, checkboxes)
        this.listAdapter = myAdapter

        // Add a shortcut to the accessibility settings.
        val button = findViewById<ImageButton>(R.id.button)
        button.setOnClickListener {
            startActivity(sSettingsIntent)
        }
    }

    companion object {

        /**
         * An intent for launching the system settings.
         */
        private val sSettingsIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    }
}

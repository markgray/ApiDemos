/*
 * Copyright (C) 2008 The Android Open Source Project
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

@file:Suppress("DEPRECATION")

package com.example.android.apis.view

import android.app.TabActivity
import android.content.Intent
import android.os.Bundle

/**
 * An example of tab content that launches an activity via
 * android.widget.TabHost.TabSpec#setContent(android.content.Intent), the
 * three tabs launch .view.List1, .view.List8, and .view.Controls1
 * respectively, using Intent's. The last sets the Intent flag so that it is
 * recreated each time the tab is clicked using:
 * Intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
 * TODO: replace deprecated TabActivity with modern navigation idiom
 */
class Tabs3 : TabActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. Then we initialize our `TabHost` variable `val tabHost` by retrieving the
     * `TabHost` our activity is using to host its tabs. We add three tabs to `tabHost` using its
     * `addTab` method to add tab specs created with the tags "tab1", "tab2", and "tab3", use the
     * strings "list", "photo list", and "destroy" to set the indicator labels, and set the content
     * to an [Intent] to launch the activities `List1`, `List8` and `Controls1` respectively. The
     * [Intent] for the third tab has the flag FLAG_ACTIVITY_CLEAR_TOP set as well (If set, and the
     * activity being launched is already running in the current task, then instead of launching a
     * new instance of that activity, all of the other activities on top of it will be closed and
     * this Intent will be delivered to the (now on top) old activity as a new Intent). So while the
     * first two tabs recall their content, the third one starts from scratch every time it is selected.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tabHost = tabHost
        tabHost.addTab(tabHost.newTabSpec("tab1")
                .setIndicator("list")
                .setContent(Intent(this, List1::class.java)))
        tabHost.addTab(tabHost.newTabSpec("tab2")
                .setIndicator("photo list")
                .setContent(Intent(this, List8::class.java)))

        // This tab sets the intent flag so that it is recreated each time
        // the tab is clicked.
        tabHost.addTab(tabHost.newTabSpec("tab3")
                .setIndicator("destroy")
                .setContent(Intent(this, Controls1::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)))
    }
}
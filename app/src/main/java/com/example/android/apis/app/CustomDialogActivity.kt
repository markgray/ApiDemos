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

package com.example.android.apis.app

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R

import android.app.Activity
import android.os.Bundle

/**
 * Dialog Activity
 *
 * This demonstrates the how to write an activity that looks like
 * a pop-up dialog with a custom theme using a different text color.
 */
class CustomDialogActivity : Activity() {
    /**
     * Called when the activity is starting. First we call through to the super's implementation
     * of onCreate, then we set our content view to our layout file R.layout.custom_dialog_activity.
     * The demo is completely specified by xml files:
     *
     *  - AndroidManifest.xml sets the theme using android:theme="@style/Theme.CustomDialog"
     *  - style/Theme.CustomDialog defined in the file values/styles.xml is derived from
     * parent="android:style/Theme.Dialog", and sets the background with
     * name="android:windowBackground=@drawable/filled_box"
     *  - drawable/filled_box.xml draws a box using a `shape` element to define a colored box
     * surrounded by a border of a different color
     *  - layout/custom_dialog_activity Layout file containing a TextView
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState)

        // See assets/res/any/layout/dialog_activity.xml for an
        // Example using default theme. layout/custom_dialog_activity
        // is  being set here as the content of our screen and the
        // Theme.CustomDialog defined in values/styles.xml is used
        // by AndroidManifest.xml to configure the activity.
        setContentView(R.layout.custom_dialog_activity)
    }
}

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
@file:Suppress("DEPRECATION")
// TODO: replace with PreferenceFragmentCompat from androidx.preference:preference:1.1.0 or higher.
package com.example.android.apis.preference

import android.annotation.SuppressLint
import android.os.Bundle
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import com.example.android.apis.R

/**
 * Shows how to use android.preference.SwitchPreference
 */
@SuppressLint("ExportedPreferenceActivity")
class SwitchPreference : PreferenceActivity() {
    /**
     * Called when the [PreferenceActivity] is starting. First we call through to our super's
     * implementation of `onCreate`. Next we set the default values for our preference file
     * named "switch" by reading the default values from the XML preference file R.xml.default_values
     * using the values defined by each Preference item's android:defaultValue attribute (but only if
     * this is the first time we have been run). We set the name of the `SharedPreferences` file that
     * preferences we manage will use to "switch", and then inflate the XML resource
     * R.xml.preference_switch and add its preference hierarchy to the current preference hierarchy.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    @Deprecated("Deprecated in Java")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(
            /* context = */ this,
            /* sharedPreferencesName = */ "switch",
            /* sharedPreferencesMode = */ MODE_PRIVATE,
            /* resId = */ R.xml.default_values,
            /* readAgain = */ false
        )

        // Load the preferences from an XML resource
        preferenceManager.sharedPreferencesName = "switch"
        addPreferencesFromResource(R.xml.preference_switch)
    }
}
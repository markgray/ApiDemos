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
import com.example.android.apis.R

/**
 * Uses deprecated method PreferenceActivity.addPreferencesFromResource to load preferences.
 */
@SuppressLint("ExportedPreferenceActivity")
class PreferencesFromXml : PreferenceActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we call the method [PreferenceActivity.addPreferencesFromResource] to load
     * the preferences from the XML resource file R.xml.preferences.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    @Deprecated("Deprecated in Java")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences)
    }
}
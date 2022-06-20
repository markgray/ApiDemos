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
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import com.example.android.apis.R

/**
 * This activity is an example of a simple settings screen that has default values. In order for the
 * default values to be populated into the [SharedPreferences] (from the preferences XML file), the
 * client must call [PreferenceManager.setDefaultValues]. This should be called early, typically
 * when the application is first created. An easy way to do this is to have a common function for
 * retrieving the [SharedPreferences] that takes care of calling it.
 */
@SuppressLint("ExportedPreferenceActivity")
class DefaultValues : PreferenceActivity() {
    /**
     * Called when the [PreferenceActivity] is starting. First we call through to our super's
     * implementation of `onCreate`. Then we call our method [getPrefs] to set the default values for
     * the shared preferences file [PREFS_NAME] ("defaults"), with file creation mode MODE_PRIVATE,
     * the preference XML file R.xml.default_values, and specifying that the defaults not be read
     * again.
     *
     * Next we use the [PreferenceManager] used by this activity to set the name of the [SharedPreferences]
     * file that preferences we manage will use to [PREFS_NAME] ("defaults"). Finally we inflate the
     * XML resource R.xml.default_values and add its preference hierarchy to the current preference
     * hierarchy. The key to the whole example lies in the use of the attribute android:defaultValue
     * for each of the [Preference] elements in the R.xml.default_values file.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    @Deprecated("Deprecated in Java")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getPrefs(this)
        preferenceManager.sharedPreferencesName = PREFS_NAME
        addPreferencesFromResource(R.xml.default_values)
    }

    companion object {
        /**
         * This is the global (to the .apk) name under which we store these preferences.  We want this
         * to be unique from other preferences so that we do not have unexpected name conflicts, and the
         * framework can correctly determine whether these preferences' defaults have already been written.
         */
        const val PREFS_NAME = "defaults"

        /**
         * Called to set the default values for the shared preferences file [PREFS_NAME] ("defaults"),
         * with file creation mode MODE_PRIVATE, the preference XML file R.xml.default_values, and
         * specifying that the defaults not be read again.
         *
         * @param context [Context] to use to access resources
         * @return our [SharedPreferences] read from [PREFS_NAME] ("defaults") (Unused by our caller)
         */
        fun getPrefs(context: Context): SharedPreferences {
            PreferenceManager.setDefaultValues(context, PREFS_NAME, Context.MODE_PRIVATE, R.xml.default_values, false)
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }
}
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
package com.example.android.apis.preference

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.example.android.apis.R

/**
 * Demonstration of [PreferenceFragmentCompat], showing a single fragment in an activity.
 */
@SuppressLint("ObsoleteSdkInt")
@RequiresApi(Build.VERSION_CODES.HONEYCOMB)
class FragmentPreferences : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. Then we fetch the `FragmentManager` for interacting with fragments associated
     * with this activity and use it to begin a fragment transaction which will replace any fragments
     * occupying the root element of our view with a new instance of [PrefsFragment], and then
     * we schedule a commit of this transaction.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Display the fragment as the main content.
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, PrefsFragment())
            .commit()
    }

    /**
     * Our [PreferenceFragmentCompat], loads its preferences from an XML resource
     */
    class PrefsFragment : PreferenceFragmentCompat() {
        /**
         * Called to do the initial creation of our fragment. This is called after [onAttach] and
         * before [onCreateView].
         *
         * Note that this can be called while the fragment's activity is still in the process of
         * being created.  As such, you can not rely on things like the activity's content view
         * hierarchy being initialized at this point.  If you want to do work once the activity itself
         * is created, see [onActivityCreated].
         *
         * First we call through to our super's implementation of `onCreate`. Then we Inflate the
         * XML resource R.xml.preferences and add the preference hierarchy to the current preference
         * hierarchy.
         *
         * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
         */
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences)
        }

        /**
         * Called during [onCreate] to supply the preferences for this fragment. Subclasses are
         * expected to call `setPreferenceScreen(PreferenceScreen)` either directly or via helper
         * methods such as [addPreferencesFromResource].
         *
         * @param savedInstanceState If the fragment is being re-created from a previous saved state,
         * this is the state.
         * @param rootKey            If non-null, this preference fragment should be rooted at the
         * `PreferenceScreen` with this key.
         */
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {}
    }
}
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
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.os.Handler
import android.preference.CheckBoxPreference
import android.preference.PreferenceActivity
import android.widget.Toast
import com.example.android.apis.R

/**
 * Example that shows finding a preference from the hierarchy and a custom preference type.
 */
@SuppressLint("ExportedPreferenceActivity")
class AdvancedPreferences : PreferenceActivity(), OnSharedPreferenceChangeListener {
    /**
     * The "Haunted preference" [CheckBoxPreference] in our preferences resource file
     * R.xml.advanced_preferences ([Runnable] field [mForceCheckBoxRunnable] switches it on and off
     * once a second).
     */
    private var mCheckBoxPreference: CheckBoxPreference? = null

    /**
     * [Handler] for our [Runnable] field [mForceCheckBoxRunnable] (used by its `run` override to
     * schedule itself to run once a second).
     */
    private val mHandler = Handler()

    /**
     * This is a simple example of controlling a preference from code. It toggles the preference in
     * [CheckBoxPreference] field [mCheckBoxPreference] on and off once a second.
     */
    private val mForceCheckBoxRunnable: Runnable = object : Runnable {
        /**
         * When called we make sure that [CheckBoxPreference] field [mCheckBoxPreference] is not
         * null before toggling it. Then we use [Handler] field [mHandler] to schedule us to run
         * again after a 1000 millisecond delay.
         */
        override fun run() {
            if (mCheckBoxPreference != null) {
                mCheckBoxPreference!!.isChecked = !mCheckBoxPreference!!.isChecked
            }

            // Force toggle again in a second
            mHandler.postDelayed(this, 1000)
        }
    }

    /**
     * Called when the [PreferenceActivity] is starting. First we call through to our super's
     * implementation of `onCreate`. Then we inflate our preference screen from the XML resource
     * R.xml.advanced_preferences and add its preference hierarchy to the current preference
     * hierarchy. Finally we locate the [CheckBoxPreference] with key [KEY_ADVANCED_CHECKBOX_PREFERENCE]
     * in the root of the preference hierarchy that this activity is showing in order to initialize
     * our [CheckBoxPreference] field [mCheckBoxPreference] with it.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load the XML preferences file
        addPreferencesFromResource(R.xml.advanced_preferences)

        // Get a reference to the checkbox preference
        mCheckBoxPreference = preferenceScreen
                .findPreference(KEY_ADVANCED_CHECKBOX_PREFERENCE) as CheckBoxPreference
    }

    /**
     * Called after [onRestoreInstanceState], [onRestart], or [onPause], for our activity to start
     * interacting with the user. First we call through to our super's implementation of `onResume`,
     * then we start our [Runnable] field [mForceCheckBoxRunnable] running. Finally we register this
     * as a [OnSharedPreferenceChangeListener] for the [SharedPreferences] used by the root of the
     * preference hierarchy that this activity is showing.
     */
    override fun onResume() {
        super.onResume()

        // Start the force toggle
        mForceCheckBoxRunnable.run()

        // Set up a listener whenever a key changes
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. First we call through to our super's implementation of `onPause`.
     * Then we unregister "this" as an [OnSharedPreferenceChangeListener], and remove all callbacks
     * from [Handler] field [mHandler] for [Runnable] field [mForceCheckBoxRunnable].
     */
    override fun onPause() {
        super.onPause()

        // Unregister the listener whenever a key changes
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        mHandler.removeCallbacks(mForceCheckBoxRunnable)
    }

    /**
     * Called when a shared preference is changed, added, or removed. If the key is [KEY_MY_PREFERENCE],
     * we create and show a toast with the message "Thanks! You increased my count to" concatenated
     * with the [Int] value stored under our [String] parameter [key] in [SharedPreferences] parameter
     * [sharedPreferences].
     *
     * @param sharedPreferences The [SharedPreferences] that received the change.
     * @param key The key of the preference that was changed, added, or removed.
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        // Let's do something when my counter preference value changes
        if (key == KEY_MY_PREFERENCE) {
            Toast.makeText(this, "Thanks! You increased my count to "
                    + sharedPreferences.getInt(key, 0), Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        /**
         * Key used for the `MyPreference` preference, our override of `onSharedPreferenceChanged`
         * will toast a message reporting the new value when it changes.
         */
        const val KEY_MY_PREFERENCE = "my_preference"

        /**
         * Key in our shared preferences for the `CheckBoxPreference` field `mCheckBoxPreference`
         */
        const val KEY_ADVANCED_CHECKBOX_PREFERENCE = "advanced_checkbox_preference"
    }
}
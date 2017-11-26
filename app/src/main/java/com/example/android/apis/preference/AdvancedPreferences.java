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

package com.example.android.apis.preference;

import com.example.android.apis.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.preference.CheckBoxPreference;
import android.widget.Toast;

/**
 * Example that shows finding a preference from the hierarchy and a custom preference type.
 */
@SuppressWarnings("deprecation")
public class AdvancedPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    /**
     * Key used for the {@code MyPreference} preference, our override of {@code onSharedPreferenceChanged}
     * will toast a message reporting the new value when it changes.
     */
    public static final String KEY_MY_PREFERENCE = "my_preference";
    /**
     * Key in our shared preferences for the {@code CheckBoxPreference mCheckBoxPreference}
     */
    public static final String KEY_ADVANCED_CHECKBOX_PREFERENCE = "advanced_checkbox_preference";

    /**
     * The "Haunted preference" {@code CheckBoxPreference} in our preferences resource file
     * R.xml.advanced_preferences ({@code Runnable mForceCheckBoxRunnable} switches it on and off
     * once a second).
     */
    private CheckBoxPreference mCheckBoxPreference;
    /**
     * {@code Handler} for our {@code Runnable mForceCheckBoxRunnable} (used by its {@code run} override
     * to schedule itself to run once a second).
     */
    private Handler mHandler = new Handler();

    /**
     * This is a simple example of controlling a preference from code. It toggles the preference
     * {@code CheckBoxPreference mCheckBoxPreference} on and off once a second.
     */
    private Runnable mForceCheckBoxRunnable = new Runnable() {
        /**
         * When called we make sure that {@code CheckBoxPreference mCheckBoxPreference} is not null
         * before toggling it. Then we use {@code Handler mHandler} to schedule us to run again after
         * a 1000 millisecond delay.
         */
        @Override
        public void run() {
            if (mCheckBoxPreference != null) {
                mCheckBoxPreference.setChecked(!mCheckBoxPreference.isChecked());
            }

            // Force toggle again in a second
            mHandler.postDelayed(this, 1000);
        }
    };

    /**
     * Called when the {@code PreferenceActivity} is starting. First we call through to our super's
     * implementation of {@code onCreate}. Then we inflate our preference screen from the XML resource
     * R.xml.advanced_preferences and add its preference hierarchy to the current preference hierarchy.
     * Finally we locate the {@code CheckBoxPreference} with key KEY_ADVANCED_CHECKBOX_PREFERENCE in
     * the root of the preference hierarchy that this activity is showing in order to initialize our
     * field {@code CheckBoxPreference mCheckBoxPreference} with it.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the XML preferences file
        addPreferencesFromResource(R.xml.advanced_preferences);

        // Get a reference to the checkbox preference
        mCheckBoxPreference = (CheckBoxPreference)getPreferenceScreen()
                .findPreference(KEY_ADVANCED_CHECKBOX_PREFERENCE);
    }

    /**
     * Called after {@code onRestoreInstanceState}, {@code onRestart}, or {@code onPause}, for our
     * activity to start interacting with the user. First we call through to our super's implementation
     * of {@code onResume}, then we start our {@code Runnable mForceCheckBoxRunnable} running. Finally
     * we register this as a {@code OnSharedPreferenceChangeListener} for the {@code SharedPreference}
     * used by the root of the preference hierarchy that this activity is showing.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Start the force toggle
        mForceCheckBoxRunnable.run();

        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. First we call through to our super's implementation of {@code onPause}.
     * Then we unregister "this" as an {@code OnSharedPreferenceChangeListener}, and remove all callbacks
     * from {@code Handler mHandler} for {@code Runnable mForceCheckBoxRunnable}.
     */
    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

        mHandler.removeCallbacks(mForceCheckBoxRunnable);
    }

    /**
     * Called when a shared preference is changed, added, or removed. If the key is KEY_MY_PREFERENCE,
     * we create and show a toast with the message "Thanks! You increased my count to " concatenated
     * with the int value stored under {@code String key} in {@code SharedPreferences sharedPreferences}.
     *
     * @param sharedPreferences The {@code SharedPreferences} that received the change.
     * @param key The key of the preference that was changed, added, or removed.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Let's do something when my counter preference value changes
        if (key.equals(KEY_MY_PREFERENCE)) {
            Toast.makeText(this, "Thanks! You increased my count to "
                    + sharedPreferences.getInt(key, 0), Toast.LENGTH_SHORT).show();
        }
    }

}

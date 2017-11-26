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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * This activity is an example of a simple settings screen that has default
 * values.
 * <p>
 * In order for the default values to be populated into the
 * {@link SharedPreferences} (from the preferences XML file), the client must
 * call
 * {@link PreferenceManager#setDefaultValues(android.content.Context, int, boolean)}.
 * <p>
 * This should be called early, typically when the application is first created.
 * An easy way to do this is to have a common function for retrieving the
 * SharedPreferences that takes care of calling it.
 */
@SuppressLint("ExportedPreferenceActivity")
public class DefaultValues extends PreferenceActivity {
    /**
     * This is the global (to the .apk) name under which we store these preferences.  We want this
     * to be unique from other preferences so that we do not have unexpected name conflicts, and the
     * framework can correctly determine whether these preferences' defaults have already been written.
     */
    static final String PREFS_NAME = "defaults";

    /**
     * Called when the {@code PreferenceActivity} is starting. First we call through to our super's
     * implementation of {@code onCreate}. Then we call our method {@code getPrefs} to set the default
     * values for the shared preferences file PREFS_NAME ("defaults"), with file creation mode MODE_PRIVATE,
     * the preference XML file R.xml.default_values, and specifying that the defaults not be read again.
     *
     * Next we use the {@code PreferenceManager} used by this activity to set the name of the
     * SharedPreferences file that preferences we manage will use to PREFS_NAME ("defaults").
     * Finally we inflate the XML resource R.xml.default_values and add its preference hierarchy to
     * the current preference hierarchy. The key to the whole example lies in the use of the attribute
     * android:defaultValue for each of the *Preference elements in the R.xml.default_values file.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPrefs(this);
        //noinspection deprecation
        getPreferenceManager().setSharedPreferencesName(PREFS_NAME);
        //noinspection deprecation
        addPreferencesFromResource(R.xml.default_values);
    }

    /**
     * Called to set the default values for the shared preferences file PREFS_NAME ("defaults"), with
     * file creation mode MODE_PRIVATE, the preference XML file R.xml.default_values, and specifying
     * that the defaults not be read again.
     *
     * @param context {@code Context} to use to access resources
     * @return our {@code SharedPreferences} read from PREFS_NAME ("defaults") Unused by our caller
     */
    @SuppressWarnings("UnusedReturnValue")
    static SharedPreferences getPrefs(Context context) {
        PreferenceManager.setDefaultValues(context, PREFS_NAME, MODE_PRIVATE, R.xml.default_values, false);
        return context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }
}

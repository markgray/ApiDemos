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
import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * A primitive example showing how some preferences can depend on other preferences. Uses
 * android:dependency to point to key of another Preference that the Preference will depend on.
 * If the other Preference is not set or is off, the Preference will be disabled.
 */
@SuppressLint("ExportedPreferenceActivity")
public class PreferenceDependencies extends PreferenceActivity {
    /**
     * Called when the {@code PreferenceActivity} is starting. We just call through to our super's
     * implementation of {@code onCreate}, then we inflate the XML resource R.xml.preference_dependencies
     * and add its preference hierarchy to the current preference hierarchy. The entire example occurs
     * in the xml, see the android:dependency="wifi" attribute ("wifi" is the key of the CheckBoxPreference)
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //noinspection deprecation
        addPreferencesFromResource(R.xml.preference_dependencies);
    }

}

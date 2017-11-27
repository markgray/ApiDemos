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

package com.example.android.apis.preference;

import com.example.android.apis.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Demonstration of PreferenceFragment, showing a single fragment in an
 * activity.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FragmentPreferences extends Activity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}. Then we fetch the FragmentManager for interacting with fragments associated
     * with this activity and use it to begin a fragment transaction which will replace any fragments
     * occupying the root element of our view with a new instance of {@code PrefsFragment}, and then
     * we schedule a commit of this transaction.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PrefsFragment())
                .commit();
    }

    /**
     * Our {@code PreferenceFragment}, loads its preferences from an XML resource
     */
    public static class PrefsFragment extends PreferenceFragment {
        /**
         * Called to do the initial creation of our fragment. This is called after {@code onAttach(Activity)}
         * and before {@code onCreateView(LayoutInflater, ViewGroup, Bundle)}.
         * <p>
         * Note that this can be called while the fragment's activity is still in the process of
         * being created.  As such, you can not rely on things like the activity's content view
         * hierarchy being initialized at this point.  If you want to do work once the activity itself
         * is created, see {@code onActivityCreated(Bundle)}.
         * <p>
         * First we call through to our super's implementation of {@code onCreate}. Then we Inflate
         * the XML resource R.xml.preferences and add the preference hierarchy to the current preference
         * hierarchy.
         *
         * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }
    }

}

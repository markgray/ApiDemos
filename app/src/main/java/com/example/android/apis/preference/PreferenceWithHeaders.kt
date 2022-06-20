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
@file:Suppress("DEPRECATION")
// TODO: replace with PreferenceFragmentCompat from androidx.preference:preference:1.1.0 or higher.
package com.example.android.apis.preference

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Button
import com.example.android.apis.R

/**
 * After override of isValidFragment {return true;} you have a top-level preference panel with
 * headers which when clicked launch the PreferenceFragment's listed in the xml/preference_headers.xml
 * resource loaded using loadHeadersFromResource.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressLint("SetTextI18n")
class PreferenceWithHeaders : PreferenceActivity() {
    /**
     * Called when the [PreferenceActivity] is starting. First we call through to our super's
     * implementation of `onCreate`. If the method [PreferenceActivity.hasHeaders] returns true
     * (we are currently showing the header list) we create [Button] `val button`, set its text to
     * "Some action", and set if as a footer that should be shown at the bottom of the header list.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
     */
    @Deprecated("Deprecated in Java")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Add a button to the header list.
        if (hasHeaders()) {
            val button = Button(this)
            button.text = "Some action"
            setListFooter(button)
        }
    }

    /**
     * Subclasses should override this method and verify that the given fragment is a valid type
     * to be attached to this activity. The default implementation returns `true` for apps built
     * for android:targetSdkVersion older than android.os.Build.VERSION_CODES.KITKAT. For later
     * versions, it will throw an exception.
     *
     * @param fragmentName the class name of the Fragment about to be attached to this activity.
     * @return true if the fragment class name is valid for this Activity and false otherwise.
     */
    @Deprecated("Deprecated in Java")
    override fun isValidFragment(fragmentName: String): Boolean {
        return true
    }

    /**
     * Called when the activity needs its list of headers built. By implementing this and adding at
     * least one item to the list, you will cause the activity to run in its modern fragment mode.
     * Note that this function may not always be called; for example, if the activity has been asked
     * to display a particular fragment without the header list, there is no need to build the headers.
     *
     * We simply call the function [loadHeadersFromResource] to Parse the XML file
     * R.xml.preference_headers as a header description, adding each parsed Header into the
     * [target] list of `Header` objects.
     *
     * @param target The list in which to place the headers.
     */
    @Deprecated("Deprecated in Java")
    override fun onBuildHeaders(target: List<Header>) {
        loadHeadersFromResource(R.xml.preference_headers, target)
    }

    /**
     * This fragment shows the preferences for the first header.
     */
    class Prefs1Fragment : PreferenceFragment() {
        /**
         * Called to do initial creation of a fragment. This is called after [onAttach] and before
         * [onCreateView].
         *
         * Note that this can be called while the fragment's activity is still in the process of being
         * created. As such, you can not rely on things like the activity's content view hierarchy
         * being initialized at this point. If you want to do work once the activity itself is created,
         * see [onActivityCreated].
         *
         * First we call through to our super's implementation of `onCreate`. Then we load the
         * default values of our preferences from R.xml.advanced_preferences if we have not been run
         * before. Finally we inflate the XML resource R.xml.fragmented_preferences and add its
         * preference hierarchy to the current preference hierarchy.
         *
         * @param savedInstanceState If the fragment is being re-created from a previous saved state,
         * this is the state. We do not override [onSaveInstanceState] so do not use.
         */
        @Deprecated("Deprecated in Java")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // Make sure default values are applied.  In a real app, you would
            // want this in a shared function that is used to retrieve the
            // SharedPreferences wherever they are needed.
            PreferenceManager.setDefaultValues(activity, R.xml.advanced_preferences, false)

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragmented_preferences)
        }
    }

    /**
     * This fragment contains a second-level set of preferences that you
     * can get to by tapping an item in the first preferences fragment.
     */
    class Prefs1FragmentInner : PreferenceFragment() {
        /**
         * Called to do initial creation of a fragment. First we call through to our super's implementation
         * of `onCreate`. Then we inflate the XML resource R.xml.fragmented_preferences_inner and add
         * its preference hierarchy to the current preference hierarchy.
         *
         * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
         */
        @Deprecated("Deprecated in Java")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // Can retrieve arguments from preference XML.
            Log.i("args", "Arguments: $arguments")

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragmented_preferences_inner)
        }
    }

    /**
     * This fragment shows the preferences for the second header.
     */
    class Prefs2Fragment : PreferenceFragment() {
        /**
         * Called to do initial creation of a fragment. First we call through to our super's implementation
         * of `onCreate`. Then we inflate the XML resource R.xml.preference_dependencies and add
         * its preference hierarchy to the current preference hierarchy.
         *
         * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
         */
        @Deprecated("Deprecated in Java")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // Can retrieve arguments from headers XML.
            Log.i("args", "Arguments: $arguments")

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preference_dependencies)
        }
    }
}
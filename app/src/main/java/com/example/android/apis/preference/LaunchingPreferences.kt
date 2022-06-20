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
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Demonstrates launching a [PreferenceActivity] and grabbing a value it saved.
 */
class LaunchingPreferences : AppCompatActivity(), View.OnClickListener {
    /**
     * [TextView] that we display our counter in.
     */
    private var mCounterText: TextView? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. Then we set the default values from preference file R.xml.advanced_preferences
     * by reading the values defined by each Preference item's android:defaultValue attribute (We
     * pass false as the [Boolean] `readAgain` flag so this will only happen once).
     *
     * Next we create [LinearLayout] `val layout`, set its orientation to VERTICAL and set it as our
     * content view. We create [Button] `val launchPreferences`, set its text to the string
     * R.string.launch_preference_activity ("Launch PreferenceActivity"), set its `OnClickListener`
     * to "this" and add it to [LinearLayout] `layout` using the layout parameters MATCH_PARENT and
     * WRAP_CONTENT for the width and height respectively. Next we initialize our [TextView] field
     * [mCounterText] with a new instance and add it to [LinearLayout] `layout` using the layout
     * parameters MATCH_PARENT and WRAP_CONTENT for the width and height respectively.
     *
     * Finally we call our method [updateCounterText] to read the preference stored under the key
     * [AdvancedPreferences.KEY_MY_PREFERENCE] ("my_preference"), then format and display it in
     * [TextView] field [mCounterText].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * These preferences have defaults, so before using them go apply those
         * defaults. This will only execute once -- when the defaults are applied
         * a boolean preference is set so they will not be applied again.
         */
        PreferenceManager.setDefaultValues(this, R.xml.advanced_preferences, false)

        // Simple layout
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        setContentView(layout)

        // Create a simple button that will launch the preferences
        val launchPreferences = Button(this)
        launchPreferences.text = getString(R.string.launch_preference_activity)
        launchPreferences.setOnClickListener(this)
        layout.addView(
                launchPreferences,
                LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        )
        mCounterText = TextView(this)
        layout.addView(
                mCounterText,
                LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        )
        updateCounterText()
    }

    /**
     * Called when our "Launch PreferenceActivity" button is clicked. First we create an [Intent]
     * `val launchPreferencesIntent` to launch [AdvancedPreferences], then we use it to start that
     * activity for a result using the request code [REQUEST_CODE_PREFERENCES] (1). When that
     * activity is finished, the result will be passed to our [onActivityResult] override.
     *
     * @param v [View] that has been clicked, the button labeled "Launch PreferenceActivity"
     * in our case
     */
    override fun onClick(v: View) {

        // When the button is clicked, launch an activity through this intent
        val launchPreferencesIntent = Intent().setClass(this, AdvancedPreferences::class.java)

        // Make it a sub-activity so we know when it returns
        startActivityForResult(launchPreferencesIntent, REQUEST_CODE_PREFERENCES)
    }

    /**
     * Called when an activity we launched exits, giving us the [requestCode] we started it with,
     * the [resultCode] it returned, and any additional data from it. First we call our super's
     * implementation of `onActivityResult`, then if the [requestCode] parameter is ou
     * [REQUEST_CODE_PREFERENCES] (the code we started [AdvancedPreferences] with), we call our
     * method [updateCounterText] to read the preference stored under the key
     * [AdvancedPreferences.KEY_MY_PREFERENCE] ("my_preference"), then format and display it in
     * [TextView] field [mCounterText].
     *
     * @param requestCode The integer request code originally supplied to [startActivityForResult],
     * in our case [REQUEST_CODE_PREFERENCES]
     * @param resultCode  The integer result code returned by the child activity through its setResult().
     * @param data        An [Intent], which can return result data to the caller UNUSED
     */
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // The preferences returned if the request code is what we had given
        // earlier in startSubActivity
        if (requestCode == REQUEST_CODE_PREFERENCES) {
            // Read a sample value they have set
            updateCounterText()
        }
    }

    /**
     * Called to read the preference stored under the key [AdvancedPreferences.KEY_MY_PREFERENCE]
     * ("my_preference"), then format and display it in [TextView] field [mCounterText]. First we
     * fetch a [SharedPreferences] instance that points to the default file that is used by the
     * preference framework in the given context to initialize [SharedPreferences] `val sharedPref`.
     * We fetch the [Int] stored under the key [AdvancedPreferences.KEY_MY_PREFERENCE] in `sharedPref`
     * to initialize [Int] `val counter`. Then we concatenate the string R.string.counter_value_is
     * ("The counter value is") to a space followed by the string value of `counter` and set the
     * text of [TextView] field [mCounterText] to it.
     */
    @SuppressLint("SetTextI18n")
    private fun updateCounterText() {
        // Since we're in the same package, we can use this context to get
        // the default shared preferences
        val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val counter = sharedPref.getInt(AdvancedPreferences.KEY_MY_PREFERENCE, 0)
        mCounterText!!.text = getString(R.string.counter_value_is) + " " + counter
    }

    companion object {
        /**
         * Request code we use when starting the activity `AdvancedPreferences` for result.
         */
        private const val REQUEST_CODE_PREFERENCES = 1
    }
}
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
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

/**
 * Demonstrates launching a PreferenceActivity and grabbing a value it saved.
 */
public class LaunchingPreferences extends Activity implements OnClickListener {

    /**
     * Request code we use when starting the activity {@code AdvancedPreferences} for result.
     */
    private static final int REQUEST_CODE_PREFERENCES = 1;

    /**
     * {@code TextView} that we display our counter in.
     */
    private TextView mCounterText;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}. Then we set the default values from preference file R.xml.advanced_preferences
     * by reading the values defined by each Preference item's android:defaultValue attribute (We pass
     * false as the {@code boolean readAgain} flag so this will only happen once).
     * <p>
     * Next we create {@code LinearLayout layout}, set its orientation to VERTICAL and set it as our
     * content view. We create {@code Button launchPreferences}, set its text to the string
     * R.string.launch_preference_activity ("Launch PreferenceActivity"), set its {@code OnClickListener}
     * to "this" and add it to {@code LinearLayout layout} using the layout parameters MATCH_PARENT
     * and WRAP_CONTENT for the width and height. Next we initialize our field {@code TextView mCounterText}
     * with a new instance and add it to {@code LinearLayout layout} using the layout parameters MATCH_PARENT
     * and WRAP_CONTENT for the width and height.
     * <p>
     * Finally we call our method {@code updateCounterText} to read the preference stored under the
     * key {@code AdvancedPreferences.KEY_MY_PREFERENCE} ("my_preference"), then format and display
     * it in {@code TextView mCounterText}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * These preferences have defaults, so before using them go apply those
         * defaults.  This will only execute once -- when the defaults are applied
         * a boolean preference is set so they will not be applied again.
         */
        PreferenceManager.setDefaultValues(this, R.xml.advanced_preferences, false);

        // Simple layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        setContentView(layout);

        // Create a simple button that will launch the preferences
        Button launchPreferences = new Button(this);
        launchPreferences.setText(getString(R.string.launch_preference_activity));
        launchPreferences.setOnClickListener(this);
        layout.addView(launchPreferences, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        mCounterText = new TextView(this);
        layout.addView(mCounterText, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        updateCounterText();
    }

    /**
     * Called when our "Launch PreferenceActivity" button is clicked. First we create an intent
     * {@code Intent launchPreferencesIntent} to launch {@code AdvancedPreferences}, then we use
     * it to start that activity for a result using the request code REQUEST_CODE_PREFERENCES (1).
     * When that activity is finished, the result will be passed to our {@code onActivityResult}
     * override.
     *
     * @param v View that has been clicked, the button labeled "Launch PreferenceActivity" in our case
     */
    @Override
    public void onClick(View v) {

        // When the button is clicked, launch an activity through this intent
        Intent launchPreferencesIntent = new Intent().setClass(this, AdvancedPreferences.class);

        // Make it a sub-activity so we know when it returns
        startActivityForResult(launchPreferencesIntent, REQUEST_CODE_PREFERENCES);
    }

    /**
     * Called when an activity we launched exits, giving us the requestCode we started it with, the
     * resultCode it returned, and any additional data from it. First we call our super's implementation
     * of {@code onActivityResult}, then if the {@code requestCode} parameter is REQUEST_CODE_PREFERENCES
     * (the code we started {@code AdvancedPreferences} with), we call our method {@code updateCounterText}
     * to read the preference stored under the key {@code AdvancedPreferences.KEY_MY_PREFERENCE}
     * ("my_preference"), then format and display it in {@code TextView mCounterText}.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult(),
     *                    in our case REQUEST_CODE_PREFERENCES
     * @param resultCode  The integer result code returned by the child activity through its setResult().
     * @param data        An Intent, which can return result data to the caller UNUSED
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // The preferences returned if the request code is what we had given
        // earlier in startSubActivity
        if (requestCode == REQUEST_CODE_PREFERENCES) {
            // Read a sample value they have set
            updateCounterText();
        }
    }

    /**
     * Called to read the preference stored under the key {@code AdvancedPreferences.KEY_MY_PREFERENCE}
     * ("my_preference"), then format and display it in {@code TextView mCounterText}. First we fetch
     * a {@code SharedPreferences} instance that points to the default file that is used by the
     * preference framework in the given context to initialize {@code SharedPreferences sharedPref}.
     * We fetch the int stored under the key {@code AdvancedPreferences.KEY_MY_PREFERENCE} in
     * {@code sharedPref} to initialize {@code int counter}. Then we concatenate the string
     * R.string.counter_value_is ("The counter value is") to a space followed by the string value of
     * {@code counter} and set the text of {@code TextView mCounterText} to it.
     */
    @SuppressLint("SetTextI18n")
    private void updateCounterText() {
        // Since we're in the same package, we can use this context to get
        // the default shared preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        final int counter = sharedPref.getInt(AdvancedPreferences.KEY_MY_PREFERENCE, 0);
        mCounterText.setText(getString(R.string.counter_value_is) + " " + counter);
    }
}

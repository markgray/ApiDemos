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

package com.example.android.apis.app;

import com.example.android.apis.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Launched by a push of a Button in the Activity RedirectEnter we will immediately start
 * RedirectGetter if there is no data stored in "RedirectData" yet, if there is data we
 * will display it and give the user the options to either "Clear and Exit" back to
 * RedirectEnter, or "New Text" which restarts RedirectGetter. RedirectMain uses the
 * request codes INIT_TEXT_REQUEST, and NEW_TEXT_REQUEST that it sends in the Intent to
 * RedirectGetter to determine what to do if the result code was RESULT_CANCELED,
 * either finish() back to RedirectEnter, or just display the old text.
 */
public class RedirectMain extends Activity {
    static final int INIT_TEXT_REQUEST = 0; // Request code for initial run of Activity RedirectGetter
    static final int NEW_TEXT_REQUEST = 1;  // Request code when new text Button is clicked

    @SuppressWarnings("FieldCanBeLocal")
    private String mTextPref; // String stored in shared preferences file by RedirectGetter

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, the we set our content view to our layout file R.layout.redirect_main
     *
     * @param savedInstanceState always null since onSaveInstanceState is not called
     */
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.redirect_main);

        // Watch for button clicks.
        Button clearButton = (Button)findViewById(R.id.clear);
        clearButton.setOnClickListener(mClearListener);
        Button newButton = (Button)findViewById(R.id.newView);
        newButton.setOnClickListener(mNewListener);

        // Retrieve the current text preference.  If there is no text
        // preference set, we need to get it from the user by invoking the
        // activity that retrieves it.  To do this cleanly, we will
        // temporarily hide our own activity so it is not displayed until the
        // result is returned.
        if (!loadPrefs()) {
            Intent intent = new Intent(this, RedirectGetter.class);
            startActivityForResult(intent, INIT_TEXT_REQUEST);
        }
    }

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INIT_TEXT_REQUEST) {

            // If the request was cancelled, then we are cancelled as well.
            if (resultCode == RESULT_CANCELED) {
                finish();

            // Otherwise, there now should be text...  reload the prefs,
            // and show our UI.  (Optionally we could verify that the text
            // is now set and exit if it isn't.)
            } else {
                loadPrefs();
            }

        } else if (requestCode == NEW_TEXT_REQUEST) {

            // In this case we are just changing the text, so if it was
            // cancelled then we can leave things as-is.
            if (resultCode != RESULT_CANCELED) {
                loadPrefs();
            }

        }
    }

    @SuppressWarnings("FinalPrivateMethod")
    private final boolean loadPrefs() {
        // Retrieve the current redirect values.
        // NOTE: because this preference is shared between multiple
        // activities, you must be careful about when you read or write
        // it in order to keep from stepping on yourself.
        SharedPreferences preferences = getSharedPreferences("RedirectData", 0);

        mTextPref = preferences.getString("text", null);
        if (mTextPref != null) {
            TextView text = (TextView)findViewById(R.id.text);
            text.setText(mTextPref);
            return true;
        }

        return false;
    }

    private OnClickListener mClearListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // Erase the preferences and exit!
            SharedPreferences preferences = getSharedPreferences("RedirectData", 0);
            preferences.edit().remove("text").commit();
            finish();
        }
    };

    private OnClickListener mNewListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // Retrieve new text preferences.
            Intent intent = new Intent(RedirectMain.this, RedirectGetter.class);
            startActivityForResult(intent, NEW_TEXT_REQUEST);
        }
    };
}

/*
 * Copyright (C) 2012 The Android Open Source Project
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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.apis.R;

/**
 * Show how to end a deeply nested stack of activities, returning to the activity at the very top.
 * It does this by calling finishAffinity() which finishes this activity as well as all activities
 * immediately below it in the current task that have the same affinity.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
@SuppressLint("SetTextI18n")
public class FinishAffinity extends Activity {
    int mNesting;
    /**
     * OnClickListener for the R.id.nest ("Nest some more") Button, it starts another instance of
     * this Activity when the Button is clicked.
     */
    private OnClickListener mNestListener = new OnClickListener() {
        /**
         * We create Intent intent which will start another instance of this activity, then add one
         * more than the current value of our field mNesting as an extra to the Intent under the
         * name "nesting", and finally we start the new instance of this Activity.
         *
         * @param v Button which was clicked: R.id.nest ("Nest some more")
         */
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(FinishAffinity.this, FinishAffinity.class);
            intent.putExtra("nesting", mNesting + 1);
            startActivity(intent);
        }
    };
    /**
     * OnClickListener for the R.id.finish ("FINISH") Button, it simply calls the method
     * Activity.finishAffinity() which finishes this activity as well as all activities
     * immediately below it in the current task that have the same affinity.
     */
    private OnClickListener mFinishListener = new OnClickListener() {
        /**
         * Simply call Activity.finishAffinity() which finishes this activity as well as all
         * activities immediately below it in the current task that have the same affinity.
         *
         * @param v Button which was clicked: R.id.finish ("FINISH")
         */
        @Override
        public void onClick(View v) {
            finishAffinity();
        }
    };

    /**
     * Called when the activity is starting. First we call through to the super's implementation
     * of onCreate, then we set our content view to our layout R.layout.activity_finish_affinity.
     * Next we fetch the fetch the value of the int stored as an extra under the key "nesting" in
     * the Intent what launched us (defaulting to the value "1" if unset) and store it in our field
     * mNesting. Finally we locate the Button R.id.nest in our layout and set its OnClickListener to
     * mNestListener, and find the Button R.id.finish and set its OnClickListener to mFinishListener.
     *
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_finish_affinity);

        mNesting = getIntent().getIntExtra("nesting", 1);
        ((TextView) findViewById(R.id.seq)).setText("Current nesting: " + mNesting);

        // Watch for button clicks.
        Button button = (Button) findViewById(R.id.nest);
        button.setOnClickListener(mNestListener);
        button = (Button) findViewById(R.id.finish);
        button.setOnClickListener(mFinishListener);
    }
}

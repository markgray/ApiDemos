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

package com.example.android.apis.app;

import com.example.android.apis.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Uses setTheme(theme) followed by recreate() to cycle through three different Holo themes
 * (original version did not initialize the field that the current theme is kept in, causing
 * it to crash on Material Light default devices.)
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ActivityRecreate extends Activity {
    int mCurTheme; // Theme in use for this instance of the Activity

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate. Then if savedInstanceState is not null we retrieve the mCurTheme from that Bundle
     * (stored under the key "theme"), and switch on its current value changing the mCurTheme from
     * Theme_Holo_Light to Theme_Holo_Dialog to Theme_Holo and back to Theme_Holo_Light. If
     * savedInstanceState is null (first time run) we set mCurTheme to Theme_Holo_Light. Next we
     * set the base theme for this context to mCurTheme, then set the content view to our layout
     * file R.layout.activity_recreate. Finally we locate the "Recreate" Button (R.id.recreate) and
     * set its OnClickListener to OnClickListener mRecreateListener so that when the Button is
     * clicked the Activity will be recreated.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.
     *                           <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mCurTheme = savedInstanceState.getInt("theme");

            // Switch to a new theme different from last theme.
            switch (mCurTheme) {
                case android.R.style.Theme_Holo_Light:
                    mCurTheme = android.R.style.Theme_Holo_Dialog;
                    break;
                case android.R.style.Theme_Holo_Dialog:
                    mCurTheme = android.R.style.Theme_Holo;
                    break;
                default:
                    mCurTheme = android.R.style.Theme_Holo_Light;
                    break;
            }

        } else {
            mCurTheme = android.R.style.Theme_Holo_Light;
        }

        setTheme(mCurTheme);
        setContentView(R.layout.activity_recreate);

        // Watch for button clicks.
        Button button = (Button) findViewById(R.id.recreate);
        button.setOnClickListener(mRecreateListener);
    }

    /**
     * Called to retrieve per-instance state from an activity before being killed
     * so that the state can be restored in {@link #onCreate} or
     * {@link #onRestoreInstanceState} (the {@link Bundle} populated by this method
     * will be passed to both).
     *
     * First we call through to our super's implementation of onSaveInstanceState, then we insert
     * the current value of mCurTheme using the key "theme" in the Bundle savedInstanceState passed
     * us. It will be used in our onCreate method when the Activity is recreated.
     *
     * @param savedInstanceState Bundle in which to place your saved state.
     */
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("theme", mCurTheme);
    }

    /**
     * OnClickListener used the our "Recreate" Button (R.id.recreate) in our layout
     */
    private OnClickListener mRecreateListener = new OnClickListener() {
        /**
         * Called when a view has been clicked. We simply call Activity.recreate() to cause this
         * Activity to be recreated with a new instance. This results in essentially the same flow
         * as when the Activity is created due to a configuration change -- the current instance
         * will go through its lifecycle to onDestroy() and a new instance then created after it.
         *
         * @param v View of the Button that was clicked
         */
        @Override
        public void onClick(View v) {
            recreate();
        }
    };
}

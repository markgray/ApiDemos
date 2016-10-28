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

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import com.example.android.apis.R;

import android.app.Activity;
import android.os.Bundle;

/**
 * <h3>Translucent Activity</h3>
 * <p>
 * <p>This demonstrates the how to write an activity that is translucent,
 * allowing windows underneath to show through.</p>
 *
 * Sets the theme on the window in AndroidManifest to @style/Theme.Translucent
 * which is derived in styles.xml from android:style/Theme.Translucent, thereby
 * causing the window to be composited over whatever is behind it.
 */
public class TranslucentActivity extends Activity {
    /**
     * Initialization of the Activity after it is first created. First we call through
     * to our super's     * implementation of onCreate, then we set our content view to
     * our layout file R.layout.translucent_background.
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState);

        // See assets/res/any/layout/translucent_background.xml for this
        // view layout definition, which is being set here as
        // the content of our screen.
        setContentView(R.layout.translucent_background);
    }
}

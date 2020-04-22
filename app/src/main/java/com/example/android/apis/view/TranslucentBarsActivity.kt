/*
 * Copyright (C) 2013 The Android Open Source Project
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
package com.example.android.apis.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * This activity demonstrates a simple implementation of displaying content with translucent
 * system UI bars. Layout sets the attribute android:fitsSystemWindows="true" to adjust view
 * layout based on system windows such as the status bar. When true, it adjusts the padding
 * of the view to leave space for the system windows. Will only take effect if this view is
 * in a non-embedded activity. This has the effect of allowing the text displayed to be visible
 * below the system navigation bar and notification bar.
 *
 * android:theme="@style/Theme.NoActionBar.MyTranslucentDecor" is set in AndroidManifest
 * to make the bars translucent.
 */
class TranslucentBarsActivity : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.translucent_bars.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.translucent_bars)
    }
}
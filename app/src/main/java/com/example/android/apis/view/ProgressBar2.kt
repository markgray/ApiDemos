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
package com.example.android.apis.view

import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Demonstrates the use of indeterminate progress bars as widgets and in the
 * window's title bar. The widgets show the 3 different sizes of circular
 * progress bars that can be used. The window title bar does not work, it has
 * been dropped as of Android 5.0 appCompat library.
 */
class ProgressBar2 : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we request the window feature FEATURE_INDETERMINATE_PROGRESS (removed
     * since API 21), then we set our content view to our layout file R.layout.progressbar_2.
     * Finally we call the method [setProgressBarVisibility] to make sure the progress bar in the
     * title bar is visible (No longer supported starting in API 21).
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request for the progress bar to be shown in the title
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
        setContentView(R.layout.progressbar_2)

        // Make sure the progress bar is visible
        setProgressBarVisibility(/* visible = */ true)
    }
}
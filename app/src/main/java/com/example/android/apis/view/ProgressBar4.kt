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
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Demonstrates how to use an indeterminate progress indicator in the window's
 * title bar. This does not work for android starting with 5.0
 */
class ProgressBar4 : AppCompatActivity() {
    /**
     * Flag to indicate whether indeterminate progress indicator in the window's title bar is
     * visible or not.
     */
    private var mToggleIndeterminate = false

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, we request the window feature FEATURE_INDETERMINATE_PROGRESS (does nothing
     * since API 21), and then we set our content view to our layout file R.layout.progressbar_4.
     * We set the visibility of the indeterminate progress bar in the title to the value of our
     * field [mToggleIndeterminate] (starts out false for what it is worth, the call does nothing
     * since API 21). We initialize [Button] variable `val button` by finding the view with ID
     * R.id.toggle, and set its `OnClickListener` to an a lambda which toggles the value of
     * [mToggleIndeterminate] and sets the visibility of the indeterminate progress bar in the
     * title to the new value (does nothing since API 21).
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request progress bar
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
        setContentView(R.layout.progressbar_4)
        setProgressBarIndeterminateVisibility(mToggleIndeterminate)
        val button = findViewById<Button>(R.id.toggle)
        button.setOnClickListener {
            mToggleIndeterminate = !mToggleIndeterminate
            setProgressBarIndeterminateVisibility(mToggleIndeterminate)
        }
    }
}
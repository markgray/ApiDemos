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
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Demonstrates how to use progress bars as widgets and in the title bar.  The progress bar
 * in the title will be shown until the progress is complete, at which point it fades away.
 * Demonstrates how to use [ProgressBar] as a widget. The [ProgressBar] is defined in
 * the layout xml file to use style="?android:attr/progressBarStyleHorizontal",
 * android:layout_width="200dip", android:layout_height="wrap_content",
 * android:max="100". It uses android:progress="50" to initialize the state of
 * the default progress and android:secondaryProgress="75" to initialize the state
 * of the secondary progress. Buttons below the [ProgressBar] decrement or increment
 * the two progress states. In spite of the comments in the code, the progress bar
 * does not appear in the title bar, it appears at the top of the LinearLayout
 */
class ProgressBar1 : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we request the window feature FEATURE_PROGRESS (No longer supported
     * starting in API 21) then we set our content view to our layout file R.layout.progressbar_1.
     * We call the method [setProgressBarVisibility] to set the visibility of the progress bar
     * in the title to true (No longer supported starting in API 21). We initialize our [ProgressBar]
     * variable `val progressHorizontal` by finding the view with ID R.id.progress_horizontal,
     * set the progress of the progress bar in the title to 100 times the progress of the progress
     * bar `progressHorizontal` (starts at 50 of 100 in the xml), then we set the secondary progress
     * for the progress bar in the title to 100 times the progress of the secondary progress bar of
     * `progressHorizontal` (starts at 75 of 100 in the xml). We initialize our [Button] variable
     * `var button` by finding the view with the ID R.id.increase in our layout, then set its
     * `OnClickListener` to an a lambda which increments the progress of `progressHorizontal` by 1,
     * and sets the progress bar in the title to 100 times the progress of `progressHorizontal`. We
     * set `button` again by finding the view with the ID R.id.decrease then set its `OnClickListener`
     * to an a lambda which increments the progress of `progressHorizontal` by -1 (decrements), and
     * sets the progress bar in the title to 100 times the progress of `progressHorizontal`. We set
     * `button` again by finding the view with the ID R.id.increase_secondary then set its
     * `OnClickListener` to an a lambda which increments the progress of the secondary progress of
     * `progressHorizontal` by 1, and sets the secondary progress bar in the title to 100 times the
     * secondary progress of `progressHorizontal`. Finally we set `button` again by finding the view
     * with the ID R.id.decrease_secondary then set its `OnClickListener` to an a lambda which
     * increments the progress of the secondary progress of `progressHorizontal` by -1 (decrements),
     * and sets the secondary progress bar in the title to 100 times the secondary progress of
     * `progressHorizontal`.
     *
     * None of the title bar progress settings have any effect as of API 21.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    @Suppress("DEPRECATION", "UNUSED_ANONYMOUS_PARAMETER")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request the progress bar to be shown in the title
        requestWindowFeature(Window.FEATURE_PROGRESS)
        setContentView(R.layout.progressbar_1)
        setProgressBarVisibility(true)
        val progressHorizontal = findViewById<ProgressBar>(R.id.progress_horizontal)
        setProgress(progressHorizontal.progress * 100)
        setSecondaryProgress(progressHorizontal.secondaryProgress * 100)
        var button = findViewById<Button>(R.id.increase)
        button.setOnClickListener { v: View? ->
            progressHorizontal.incrementProgressBy(1)
            // Title progress is in range 0..10000
            setProgress(100 * progressHorizontal.progress)
        }
        button = findViewById(R.id.decrease)
        button.setOnClickListener { v: View? ->
            progressHorizontal.incrementProgressBy(-1)
            // Title progress is in range 0..10000
            setProgress(100 * progressHorizontal.progress)
        }
        button = findViewById(R.id.increase_secondary)
        button.setOnClickListener { v: View? ->
            progressHorizontal.incrementSecondaryProgressBy(1)
            // Title progress is in range 0..10000
            setSecondaryProgress(100 * progressHorizontal.secondaryProgress)
        }
        button = findViewById(R.id.decrease_secondary)
        button.setOnClickListener { v: View? ->
            progressHorizontal.incrementSecondaryProgressBy(-1)
            // Title progress is in range 0..10000
            setSecondaryProgress(100 * progressHorizontal.secondaryProgress)
        }
    }
}
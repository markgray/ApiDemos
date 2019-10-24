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

package com.example.android.apis.app

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import com.example.android.apis.R

/**
 * Show how to end a deeply nested stack of activities, returning to the activity at the very top.
 * It does this by calling finishAffinity() which finishes this activity as well as all activities
 * immediately below it in the current task that have the same affinity.
 */
@Suppress("MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
@SuppressLint("SetTextI18n")
class FinishAffinity : AppCompatActivity() {
    /**
     * Nesting level for this instance of `FinishAffinity`.
     */
    internal var mNesting: Int = 0
    /**
     * OnClickListener for the R.id.nest ("Nest some more") Button, it starts another instance of
     * this Activity when the Button is clicked. We create Intent intent which will start another
     * instance of this activity, then add one more than the current value of our field mNesting
     * as an extra to the Intent under the name "nesting", and finally we start the new instance
     * of this Activity.
     *
     * Parameter: v Button which was clicked: R.id.nest ("Nest some more")
     */
    private val mNestListener = OnClickListener {
        val intent = Intent(this@FinishAffinity, FinishAffinity::class.java)
        intent.putExtra("nesting", mNesting + 1)
        startActivity(intent)
    }
    /**
     * OnClickListener for the R.id.finish ("FINISH") Button, it simply calls the method
     * Activity.finishAffinity() which finishes this activity as well as all activities
     * immediately below it in the current task that have the same affinity.
     *
     * Parameter: v Button which was clicked: R.id.finish ("FINISH")
     */
    private val mFinishListener = OnClickListener {
        finishAffinity()
    }

    /**
     * Called when the activity is starting. First we call through to the super's implementation
     * of onCreate, then we set our content view to our layout R.layout.activity_finish_affinity.
     * Next we fetch the fetch the value of the int stored as an extra under the key "nesting" in
     * the Intent what launched us (defaulting to the value "1" if unset) and store it in our field
     * mNesting. Finally we locate the Button R.id.nest in our layout and set its OnClickListener to
     * mNestListener, and find the Button R.id.finish and set its OnClickListener to mFinishListener.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_finish_affinity)

        mNesting = intent.getIntExtra("nesting", 1)
        (findViewById<View>(R.id.seq) as TextView).text = "Current nesting: $mNesting"

        // Watch for button clicks.
        var button = findViewById<Button>(R.id.nest)
        button.setOnClickListener(mNestListener)
        button = findViewById(R.id.finish)
        button.setOnClickListener(mFinishListener)
    }
}

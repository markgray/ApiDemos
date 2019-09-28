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

package com.example.android.apis.app

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button


/**
 * Entry into our redirection example, describing what will happen.
 *
 * Consists of three activities: RedirectEnter, RedirectGetter, and RedirectMain.
 * RedirectGetter stores user input in the shared preference file "RedirectData"
 * RedirectEnter just describes what will happen, and when the "Go" button is
 * clicked starts up RedirectMain, which immediately starts RedirectGetter if
 * there is no data stored in "RedirectData" yet, if there is data it will display
 * it and give the user the options to either "Clear and Exit" back to RedirectEnter,
 * or "New Text" which restarts RedirectGetter. RedirectMain uses the request
 * codes INIT_TEXT_REQUEST, and NEW_TEXT_REQUEST that it sends in the Intent to
 * RedirectGetter to determine what to do if the result code was RESULT_CANCELED,
 * either finish() back to RedirectEnter, or just display the old text.
 */
class RedirectEnter : Activity() {

    /**
     * Called when a view has been clicked. We create an Intent intent intended to start the
     * Activity RedirectMain, and then we startActivity that intent which will launch the
     * Activity.
     *
     * Parameter: v View of Button that was clicked
     */
    private val mGoListener = OnClickListener {
        // Here we start up the main entry point of our redirection
        // example.
        val intent = Intent(this@RedirectEnter, RedirectMain::class.java)
        startActivity(intent)
    }

    /**
     * Called when the activity is starting. First we call through to our super's implementation
     * of onCreate, then we set our content view to our layout file R.layout.redirect_enter. Next
     * we locate the Button goButton (R.id.go) and set its OnClickListener to the OnClickListener
     * mGoListener.
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.redirect_enter)

        // Watch for button clicks.
        val goButton = findViewById<View>(R.id.go) as Button
        goButton.setOnClickListener(mGoListener)
    }
}


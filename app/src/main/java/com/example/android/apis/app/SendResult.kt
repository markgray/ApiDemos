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

import android.content.Intent
import android.os.Bundle
import android.view.View.OnClickListener
import android.widget.Button

import androidx.appcompat.app.AppCompatActivity

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R


/**
 * Example of sending a result back to another activity.
 */
class SendResult : AppCompatActivity() {

    /**
     * OnClickListener for the Button R.id.corky ("CORKY") When clicked we setResult to an Intent
     * with the Action "Corky!" and finish the Activity
     *
     * Parameter: v View of the Button that was clicked
     */
    private val mCorkyListener = OnClickListener {
        // To send a result, simply call setResult() before your
        // activity is finished.
        setResult(RESULT_OK, Intent().setAction("Corky!"))
        finish()
    }

    /**
     * OnClickListener for the Button R.id.violet ("VIOLET")
     * When clicked we setResult to an Intent with the Action "Violet!" and finish the Activity
     *
     * Parameter: v View of the Button that was clicked
     */
    private val mVioletListener = OnClickListener {
        // To send a result, simply call setResult() before your
        // activity is finished.
        setResult(RESULT_OK, Intent().setAction("Violet!"))
        finish()
    }

    /**
     * Initialization of the Activity after it is first created.  Must at least
     * call [setContentView()][androidx.appcompat.app.AppCompatActivity.setContentView] to
     * describe what is to be displayed in the screen.
     *
     * First we call through to our super's implementation of onCreate. Then we set our content
     * view to our layout file R.layout.send_result. We locate the Button R.id.corky ("CORKY")
     * and set its OnClickListener to OnClickListener mCorkyListener, and locate the Button
     * R.id.violet ("VIOLET") and set its OnClickListener to OnClickListener mVioletListener.
     *
     * @param savedInstanceState onSaveInstanceState is not overridden so we do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState)

        // See assets/res/any/layout/hello_world.xml for this
        // view layout definition, which is being set here as
        // the content of our screen.
        setContentView(R.layout.send_result)

        // Watch for button clicks.
        var button = findViewById<Button>(R.id.corky)
        button.setOnClickListener(mCorkyListener)
        button = findViewById(R.id.violet)
        button.setOnClickListener(mVioletListener)
    }
}

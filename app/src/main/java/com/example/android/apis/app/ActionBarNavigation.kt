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
package com.example.android.apis.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * This demonstrates implementing common navigation flows with the action bar.
 * It hows how to use "Up" button in Action Bar, new Document is created in a
 * separate activity, so you have to use "recent" to switch to it, and then
 * the "up" button works as "up", otherwise it works as "Back". Uses the
 * attribute android:taskAffinity=":bar_navigation" to associate the activities.
 * RequiresApi(Build.VERSION_CODES.LOLLIPOP)
 */
@SuppressLint("SetTextI18n")
class ActionBarNavigation : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation
     * of onCreate. Then we get a reference to this activity's ActionBar and set the display option
     * DISPLAY_HOME_AS_UP. We set the content view to our layout file R.layout.action_bar_navigation,
     * locate the TextView text (R.id.launchedfrom) for our message, and based on whether the
     * category Intent.CATEGORY_SAMPLE_CODE exists in the intent that launched our activity we
     * either set the text of "text" to "This was launched from ApiDemos" if it does or "This
     * was created from up navigation" if it does not.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Turn on the up affordance.
        val bar = supportActionBar

        bar!!.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP)

        setContentView(R.layout.action_bar_navigation)
        val text = findViewById<TextView>(R.id.launchedfrom)
        if (intent.hasCategory(Intent.CATEGORY_SAMPLE_CODE)) {
            text.text = "This was launched from ApiDemos"
        } else {
            text.text = "This was created from up navigation"
        }
    }

    /**
     * This is specified by the "New in-task activity" Button by the xml attribute
     * android:onClick="onNewActivity" and is called if the Button is clicked.
     * We create an Intent that will launch the Activity ActionBarNavigationTarget,
     * and then we start this activity.
     *
     * @param button "New in-task activity" Button
     */
    @Suppress("UNUSED_PARAMETER")
    fun onNewActivity(button: View) {
        val intent = Intent(this, ActionBarNavigationTarget::class.java)
        startActivity(intent)
    }

    /**
     * This is specified by the "New document" Button by the xml attribute
     * android:onClick="onNewDocument" and is called if the Button is clicked.
     * We create an Intent that will launch the Activity ActionBarNavigationTarget,
     * add the flag Intent.FLAG_ACTIVITY_NEW_DOCUMENT to the Intent and then we
     * start this activity.
     *
     * @param button "New document" Button
     */
    @Suppress("UNUSED_PARAMETER")
    fun onNewDocument(button: View) {
        val intent = Intent(this, ActionBarNavigationTarget::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        startActivity(intent)
    }
}

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

import android.annotation.TargetApi
import androidx.appcompat.app.ActionBar
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Target launch activity for demonstrations from [ActionBarNavigation].
 * It hows how to use "Up" button in Action Bar, new Document is created in a
 * separate activity, so you have to use "recent" to switch to it, and then
 * the "up" button works as "up", otherwise it works as "Back". Uses the
 * attribute android:taskAffinity=":bar_navigation" to associate the activities.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class ActionBarNavigationTarget : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation
     * of onCreate. The content view is set to our layout file R.layout.action_bar_navigation_target.
     * We fetch a reference to our ActionBar and set the display option DISPLAY_HOME_AS_UP.
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.action_bar_navigation_target)

        // Turn on the up affordance.
        val bar = supportActionBar

        bar!!.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP)
    }
}

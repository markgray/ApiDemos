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

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R

/**
 * Demonstrates required behavior of saving and restoring dynamic activity
 * state, so that an activity will restart with the correct state if it is
 * stopped by the system.
 *
 * In general, any activity that has been paused may be stopped by the system
 * at any time if it needs more resources for the currently running activity.
 * To handle this, before being paused the
 * [onSaveInstanceState()][android.app.Activity.onSaveInstanceState] method is
 * called before an activity is paused, allowing it to supply its current state.
 * If that activity then needs to be stopped, upon restarting it will receive its
 * last saved state in [androidx.appcompat.app.AppCompatActivity.onCreate].
 *
 * In this example we are currently saving and restoring the state of the
 * top text editor, but not of the bottom text editor.  You can see the difference
 * by editing the two text fields, then going to a couple different
 * applications while the demo is running and then returning back to it.  The
 * system takes care of saving a view's state as long as an id has been
 * assigned to the view, so we assign an ID to the view being saved but not
 * one to the view that isn't being saved.
 *
 * Demo  App/Activity/SaveRestoreState
 *
 * Source files:
 *  - app/SaveRestoreState.java The Save/Restore Screen implementation
 *  - /res/any/layout/save_restore_state.xml Defines contents of the screen
 */
class SaveRestoreState : AppCompatActivity() {

    /**
     * Retrieve the text that is currently in the "saved" editor, and
     * change the text that is currently in the "saved" editor.
     */
    @Suppress("unused")
    var savedText: CharSequence
        get() = (findViewById<View>(R.id.saved) as EditText).text
        set(text) = (findViewById<View>(R.id.saved) as EditText).setText(text)

    /**
     * Initialization of the Activity after it is first created.  Here we use
     * [setContentView()][androidx.appcompat.app.AppCompatActivity.setContentView] to set up
     * the Activity's content, and retrieve the EditText widget whose state we
     * will save/restore.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState)

        // See assets/res/any/layout/save_restore_state.xml for this
        // view layout definition, which is being set here as
        // the content of our screen.
        setContentView(R.layout.save_restore_state)

        // Set message to be appropriate for this screen.
        (findViewById<View>(R.id.msg) as TextView).setText(R.string.save_restore_msg)
    }
}


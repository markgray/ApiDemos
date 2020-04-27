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

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent

/**
 * This is an example implementation of the [android.app.Instrumentation]
 * class demonstrating instrumentation against one of this application's sample
 * activities.
 */
class LocalSampleInstrumentation : Instrumentation() {
    abstract class ActivityRunnable(val activity: Activity) : Runnable

    override fun onCreate(arguments: Bundle) {
        super.onCreate(arguments)

        // When this instrumentation is created, we simply want to start
        // its test code off in a separate thread, which will call back
        // to us in onStart().
        start()
    }

    override fun onStart() {
        super.onStart()
        // First start the activity we are instrumenting -- the save/restore
        // state sample, which has a nice edit text into which we can write
        // text.
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setClass(targetContext, SaveRestoreState::class.java)
        val activity = startActivitySync(intent) as SaveRestoreState

        // This is the Activity object that was started, to do with as we want.
        Log.i("LocalSampleInstr...",
                "Initial text: " + activity.savedText)

        // Clear the text so we start fresh.
        runOnMainSync(object : ActivityRunnable(activity) {
            override fun run() {
                activity.savedText = ""
            }
        })

        // Act like the user is typing some text.
        sendKeySync(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT))
        sendCharacterSync(KeyEvent.KEYCODE_H)
        sendKeySync(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT))
        sendCharacterSync(KeyEvent.KEYCODE_E)
        sendCharacterSync(KeyEvent.KEYCODE_L)
        sendCharacterSync(KeyEvent.KEYCODE_L)
        sendCharacterSync(KeyEvent.KEYCODE_O)

        // Wait for the activity to finish all of its processing.
        waitForIdleSync()

        // Retrieve the text we should have written...
        Log.i("LocalSampleInstr...",
                "Final text: " + activity.savedText)

        // And we are done!
        Log.i("ContactsFilterInstru...", "Done!")
        finish(Activity.RESULT_OK, null)
    }
}
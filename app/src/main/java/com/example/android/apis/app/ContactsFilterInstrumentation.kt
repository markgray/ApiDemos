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
 * class, allowing you to run tests against application code.  The
 * instrumentation implementation here is loaded into the application's
 * process, for controlling and monitoring what it does.
 */
class ContactsFilterInstrumentation : Instrumentation() {
    override fun onCreate(arguments: Bundle) {
        super.onCreate(arguments)

        // When this instrumentation is created, we simply want to start
        // its test code off in a separate thread, which will call back
        // to us in onStart().
        start()
    }

    override fun onStart() {
        super.onStart()
        // First start the activity we are instrumenting -- the contacts
        // list.
        val intent = Intent(Intent.ACTION_MAIN)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.setClassName(
            targetContext,
            "com.android.phone.Dialer"
        )
        val activity = startActivitySync(intent)

        // This is the Activity object that was started, to do with as we want.
        Log.i("ContactsFilterInstr...", "Started: $activity")

        // We are going to enqueue a couple key events to simulate the user
        // filtering the list.  This is the low-level API so we must send both
        // down and up events.
        sendKeySync(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_M))
        sendKeySync(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_M))
        sendKeySync(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_A))
        sendKeySync(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_A))

        // Wait for the activity to finish all of its processing.
        waitForIdleSync()

        // And we are done!
        Log.i("ContactsFilterInstr...", "Done!")
        finish(Activity.RESULT_OK, null)
    }
}
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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R

/**
 * This is an example of implement an [BroadcastReceiver] for an alarm that
 * should occur once.
 *
 * When the alarm goes off, we show a *Toast*, a quick message.
 *
 * This class is flagged as a BroadcastReceiver in AndroidManifest.xml using the element:
 *
 * <receiver android:name=".app.OneShotAlarm" android:process=":remote"></receiver>
 */
class OneShotAlarm : BroadcastReceiver() {
    /**
     * This method is called when the BroadcastReceiver is receiving an Intent
     * broadcast.  During this time you can use the other methods on
     * BroadcastReceiver to view/modify the current result values.  This method
     * is always called within the main thread of its process, unless you
     * explicitly asked for it to be scheduled on a different thread using
     * [android.content.Context.registerReceiver]. When it runs on the main
     * thread you should never perform long-running operations in it (there
     * is a timeout of 10 seconds that the system allows before considering
     * the receiver to be blocked and a candidate to be killed). You cannot
     * launch a popup dialog in your implementation of onReceive().
     *
     * **If this BroadcastReceiver was launched through a `<receiver>` tag,
     * then the object is no longer alive after returning from this
     * function.**  This means you should not perform any operations that
     * return a result to you asynchronously -- in particular, for interacting
     * with services, you should use [Context.startService] instead of
     * [Context.bindService]. If you wish to interact with a service that is
     * already running, you can use [peekService].
     *
     * The Intent filters used in [android.content.Context.registerReceiver]
     * and in application manifests are *not* guaranteed to be exclusive. They
     * are hints to the operating system about how to find suitable recipients. It is
     * possible for senders to force delivery to specific recipients, bypassing filter
     * resolution.  For this reason, `onReceive(Context, Intent) onReceive()`
     * implementations should respond only to known actions, ignoring any unexpected
     * Intents that they may receive.
     *
     * We simply make a Toast stating that we have received a one shot alarm broadcast.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context, R.string.one_shot_received, Toast.LENGTH_SHORT).show()
    }
}


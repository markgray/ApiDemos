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

@file:Suppress("ReplaceNotNullAssertionWithElvisReturn")

package com.example.android.apis.app

import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * This is an activity that provides an interstitial UI for the notification
 * that is posted by [IncomingMessage]. It allows the user to switch
 * to the app in its appropriate state if they want.
 */
@RequiresApi(Build.VERSION_CODES.O)
class IncomingMessageInterstitial : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.incoming_message_interstitial.
     * We then locate the [Button] with ID R.id.notify_app to initialize our variable `val button`
     * and set its `OnClickListener` to an a lambda which will call our method [switchToApp] when
     * the Button is clicked.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.incoming_message_interstitial)

        val button = findViewById<Button>(R.id.notify_app)
        /**
         * Called when the R.id.notify_app Button ("Switch to app") is clicked, we simply call
         * our method [switchToApp] to build an appropriate back stack and switch to
         * the Activity [IncomingMessageView].
         *
         * Parameter: View of the Button that was clicked
         */
        button.setOnClickListener {
            switchToApp()
        }
    }


    /**
     * Perform a switch to the app. A new activity stack is started, replacing whatever is currently
     * running, and this activity is finished. First we retrieve the [CharSequence] which was stored
     * as an extra in the Intent which launched us under the key KEY_FROM to initialize our variable
     * `val from`, and the [CharSequence] stored under the key KEY_MESSAGE to initialize our variable
     * `val msg`. We then use the method [IncomingMessage.makeMessageIntentStack] to build a back
     * stack which includes an `Intent` to launch [IncomingMessageView] to initialize our array of
     * `Intent`'s `val stack`, start the activities in `stack`, and close this Activity by calling
     * [finish].
     */
    @Suppress("MemberVisibilityCanBePrivate")
    internal fun switchToApp() {
        // We will launch the app showing what the user picked.  In this simple
        // example, it is just what the notification gave us.
        val from = intent.getCharSequenceExtra(IncomingMessageView.KEY_FROM)
        val msg = intent.getCharSequenceExtra(IncomingMessageView.KEY_MESSAGE)
        // Build the new activity stack, launch it, and finish this UI.

        val stack = IncomingMessage.makeMessageIntentStack(this, from!!, msg!!)
        startActivities(stack)
        finish()
    }

}

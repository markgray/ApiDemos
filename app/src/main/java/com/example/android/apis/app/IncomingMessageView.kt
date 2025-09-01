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

import android.app.NotificationManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * This activity is run as the click activity for [IncomingMessage], and also
 * [IncomingMessageInterstitial]. When it comes up, it also clears the notification, because
 * the "message" has been "read."
 */
class IncomingMessageView : AppCompatActivity() {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.incoming_message_view.
     * We locate the [TextView] in the layout with ID R.id.from and set its text to the String
     * stored as an extra in the Intent that launched us under the key KEY_FROM and the text of
     * the [TextView] with ID R.id.message to that stored under the key KEY_MESSAGE. We fetch a
     * handle to the system service NOTIFICATION_SERVICE [NotificationManager] to initialize our
     * variable `val nm` and use it to cancel the notification which was originally posted by
     * [IncomingMessage].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.incoming_message_view)

        // Fill in the message content.
        (findViewById<View>(R.id.from) as TextView).text =
            intent.getCharSequenceExtra(KEY_FROM)
        (findViewById<View>(R.id.message) as TextView).text =
            intent.getCharSequenceExtra(KEY_MESSAGE)

        // look up the notification manager service
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // cancel the notification that we started in IncomingMessage

        nm.cancel(R.string.imcoming_message_ticker_text)
    }

    /**
     * Our static constants.
     */
    companion object {
        /**
         * Extra that can be supplied to Intent: who the message is from.
         */
        const val KEY_FROM: String = "from"

        /**
         * Extra that can be supplied to Intent: the message that was sent.
         */
        const val KEY_MESSAGE: String = "message"
    }
}


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

package com.example.android.apis.app;

import com.example.android.apis.R;

import android.app.NotificationManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * This activity is run as the click activity for {@link IncomingMessage}, and also
 * {@link IncomingMessageInterstitial}. When it comes up, it also clears the notification, because
 * the "message" has been "read."
 */
public class IncomingMessageView extends AppCompatActivity {
    /**
     * Extra that can be supplied to Intent: who the message is from.
     */
    static final public String KEY_FROM = "from";
    /**
     * Extra that can be supplied to Intent: the message that was sent.
     */
    static final public String KEY_MESSAGE = "message";

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.incoming_message_view.
     * We locate the {@code TextView} in the layout with ID R.id.from and set its text to the String
     * stored as an extra in the Intent that launched us under the key KEY_FROM and the text of
     * R.id.message to that stored under the key KEY_MESSAGE. We fetch a handle to the system service
     * NOTIFICATION_SERVICE for {@code NotificationManager nm} and use it to cancel the notification
     * which was originally posted by {@code IncomingMessage}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.incoming_message_view);

        // Fill in the message content.
        ((TextView)findViewById(R.id.from)).setText(
                getIntent().getCharSequenceExtra(KEY_FROM));
        ((TextView)findViewById(R.id.message)).setText(
                getIntent().getCharSequenceExtra(KEY_MESSAGE));

        // look up the notification manager service
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // cancel the notification that we started in IncomingMessage
        //noinspection ConstantConditions
        nm.cancel(R.string.imcoming_message_ticker_text);
    }
}


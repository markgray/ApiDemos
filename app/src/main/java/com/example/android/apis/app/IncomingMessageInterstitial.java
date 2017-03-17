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

package com.example.android.apis.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.android.apis.R;

/**
 * This is an activity that provides an interstitial UI for the notification
 * that is posted by {@link IncomingMessage}.  It allows the user to switch
 * to the app in its appropriate state if they want.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class IncomingMessageInterstitial extends Activity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.incoming_message_interstitial.
     * We then locate {@code Button button} with ID R.id.notify_app in our layout and set its {@code OnClickListener}
     * to an anonymous class which will call our method {@code switchToApp()} when the Button is clicked.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.incoming_message_interstitial);

        Button button = (Button) findViewById(R.id.notify_app);
        button.setOnClickListener(new Button.OnClickListener() {
            /**
             * Called when the R.id.notify_app Button ("Switch to app") is clicked, we simply call
             * our method {@code switchToApp()} to build an appropriate back stack and switch to
             * the Activity {@code IncomingMessageView}.
             *
             * @param v View of the Button that was clicked
             */
            @Override
            public void onClick(View v) {
                switchToApp();
            }
        });
    }


    /**
     * Perform a switch to the app.  A new activity stack is started, replacing whatever is currently
     * running, and this activity is finished. First we retrieve {@code CharSequence from} which was
     * stored as an extra in the Intent which launched us under the key KEY_FROM, and {@code CharSequence msg}
     * stored under the key KEY_MESSAGE. We then use the method {@code IncomingMessage.makeMessageIntentStack}
     * to build a back stack for {@code Intent[] stack} which includes an Intent to launch
     * {@code IncomingMessageView}, start the activities in {@code stack}, and close this Activity by
     * calling {@code finish()}.
     */
    void switchToApp() {
        // We will launch the app showing what the user picked.  In this simple
        // example, it is just what the notification gave us.
        CharSequence from = getIntent().getCharSequenceExtra(IncomingMessageView.KEY_FROM);
        CharSequence msg = getIntent().getCharSequenceExtra(IncomingMessageView.KEY_MESSAGE);
        // Build the new activity stack, launch it, and finish this UI.
        Intent[] stack = IncomingMessage.makeMessageIntentStack(this, from, msg);
        startActivities(stack);
        finish();
    }

}

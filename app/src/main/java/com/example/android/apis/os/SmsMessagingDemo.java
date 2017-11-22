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

package com.example.android.apis.os;

import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.example.android.apis.R;

/**
 * Shows how to send and receive SMS messages. Nifty use of tts as well.
 */
public class SmsMessagingDemo extends Activity {
    /**
     * Tag string for our debug logs
     */
    private static final String TAG = "SmsMessagingDemo";

    /**
     * Intent Extra key for the originating address (sender) of this SMS message in String form,
     * set when we are started by the {@code SmsMessageReceiver} {@code BroadcastReceiver}.
     */
    public static final String SMS_RECIPIENT_EXTRA = "com.example.android.apis.os.SMS_RECIPIENT";

    /**
     * Used as the action for the "Sent {@code PendingIntent}" passed to {@code sendTextMessage}.
     * It sounds as if since 4.4 KitKat the default SMS app will take precedence for this (but the
     * documentation is a bit confusing).
     */
    public static final String ACTION_SMS_SENT = "com.example.android.apis.os.SMS_SENT_ACTION";

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.sms_demo.
     * <p>
     * Next we check to see if the {@code Intent} that launched us contains an extra with the key
     * SMS_RECIPIENT_EXTRA (this is set to the from data of a received SMS message when
     * {@code SmsReceivedDialog} launches us), and if it does we set the text of the {@code TextView}
     * in our layout which has the ID R.id.sms_recipient (labeled "Recipient #") to the string stored
     * in the extras of the {@code Intent} that launched us under that key. We then request focus for
     * the the view in our layout which has ID R.id.sms_content (labeled "Message Body").
     * <p>
     * We locate the view in our layout with ID R.id.sms_enable_receiver (labeled "Enable SMS broadcast
     * receiver") and save a reference to it in {@code CheckBox enableCheckBox}. We then set
     * {@code PackageManager pm} to a new instance, and create {@code ComponentName componentName} to
     * reference the {@code SmsMessageReceiver} class in our package. We set the checked state of
     * {@code enableCheckBox} to the enabled setting for {@code componentName} (it starts our false
     * in the AndroidManifest.xml file).
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sms_demo);

        if (getIntent().hasExtra(SMS_RECIPIENT_EXTRA)) {
            //noinspection ConstantConditions
            ((TextView) findViewById(R.id.sms_recipient))
                    .setText(getIntent().getExtras().getString(SMS_RECIPIENT_EXTRA));
            findViewById(R.id.sms_content).requestFocus();
        }

        // Enable or disable the broadcast receiver depending on the checked
        // state of the checkbox.
        CheckBox enableCheckBox = (CheckBox) findViewById(R.id.sms_enable_receiver);

        final PackageManager pm = this.getPackageManager();
        final ComponentName componentName = new ComponentName("com.example.android.apis",
                "com.example.android.apis.os.SmsMessageReceiver");

        enableCheckBox.setChecked(pm.getComponentEnabledSetting(componentName) ==
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED);

        enableCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, (isChecked ? "Enabling" : "Disabling") + " SMS receiver");

                pm.setComponentEnabledSetting(componentName,
                        isChecked ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                                : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
            }
        });

        final EditText recipientTextEdit = (EditText) SmsMessagingDemo.this.findViewById(R.id.sms_recipient);
        final EditText contentTextEdit = (EditText) SmsMessagingDemo.this.findViewById(R.id.sms_content);
        final TextView statusView = (TextView) SmsMessagingDemo.this.findViewById(R.id.sms_status);

        // Watch for send button clicks and send text messages.
        Button sendButton = (Button) findViewById(R.id.sms_send_message);
        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(recipientTextEdit.getText())) {
                    Toast.makeText(SmsMessagingDemo.this, "Please enter a message recipient.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(contentTextEdit.getText())) {
                    Toast.makeText(SmsMessagingDemo.this, "Please enter a message body.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                recipientTextEdit.setEnabled(false);
                contentTextEdit.setEnabled(false);

                SmsManager sms = SmsManager.getDefault();

                List<String> messages = sms.divideMessage(contentTextEdit.getText().toString());

                String recipient = recipientTextEdit.getText().toString();
                for (String message : messages) {
                    sms.sendTextMessage(recipient,
                            null,
                            message,
                            PendingIntent.getBroadcast(
                                    SmsMessagingDemo.this,
                                    0,
                                    new Intent(ACTION_SMS_SENT),
                                    0),
                            null);
                }
            }
        });

        // Register broadcast receivers for SMS sent and delivered intents
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = null;
                boolean error = true;
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        message = "Message sent!";
                        error = false;
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        message = "Error.";
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        message = "Error: No service.";
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        message = "Error: Null PDU.";
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        message = "Error: Radio off.";
                        break;
                }

                recipientTextEdit.setEnabled(true);
                contentTextEdit.setEnabled(true);
                contentTextEdit.setText("");

                statusView.setText(message);
                statusView.setTextColor(error ? Color.RED : Color.GREEN);
            }
        }, new IntentFilter(ACTION_SMS_SENT));
    }
}

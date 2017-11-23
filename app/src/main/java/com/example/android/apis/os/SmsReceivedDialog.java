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

import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

import com.example.android.apis.R;

/**
 * Part of the {@code SmsMessagingDemo} demonstration, we are launched by {@code SmsMessageReceiver}
 * to display an SMS message that it has received and to use the {@code TextToSpeech} engine to read
 * it aloud as well.
 */
@SuppressWarnings("FieldCanBeLocal")
public class SmsReceivedDialog extends Activity implements OnInitListener {
    /**
     * TAG used for logging.
     */
    private static final String TAG = "SmsReceivedDialog";

    /**
     * The id of the dialog that we display (there is only the one), it is the value we use when
     * calling {@code showDialog}, and is passed to our {@code onCreateDialog} override.
     */
    private static final int DIALOG_SHOW_MESSAGE = 1;

    /**
     * Key for the SMS "From Address" information which {@code SmsMessageReceiver} adds as an extra
     * to the {@code Intent} it uses to launch us.
     */
    public static final String SMS_FROM_ADDRESS_EXTRA = "com.example.android.apis.os.SMS_FROM_ADDRESS";
    /**
     * Key for the DISPLAY_NAME which {@code SmsMessageReceiver} adds as an extra to the {@code Intent}
     * it uses to launch us ({@code SmsMessageReceiver} retrieves it from the contacts database using
     * the phone number the SMS message appears to come from).
     */
    public static final String SMS_FROM_DISPLAY_NAME_EXTRA = "com.example.android.apis.os.SMS_FROM_DISPLAY_NAME";
    /**
     * Key for the message body of the SMS message which {@code SmsMessageReceiver} adds as an extra
     * to the {@code Intent} it uses to launch us
     */
    public static final String SMS_MESSAGE_EXTRA = "com.example.android.apis.os.SMS_MESSAGE";

    /**
     * {@code TextToSpeech} instance we use to read the message out loud.
     */
    private TextToSpeech mTts;

    /**
     * DISPLAY_NAME information retrieved from the {@code Intent} that {@code SmsMessageReceiver}
     * used to launch us.
     */
    private String mFromDisplayName;
    /**
     * "From Address" information retrieved from the {@code Intent} that {@code SmsMessageReceiver}
     * used to launch us.
     */
    private String mFromAddress;
    /**
     * The message body of the SMS message retrieved from the {@code Intent} that {@code SmsMessageReceiver}
     * used to launch us.
     */
    private String mMessage;
    /**
     * {@code String} with {@code mFromDisplayName} and {@code mMessage} formatted for the
     * {@code TextToSpeech} engine to read aloud.
     */
    private String mFullBodyString;

    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //noinspection ConstantConditions
        mFromAddress = getIntent().getExtras().getString(SMS_FROM_ADDRESS_EXTRA);
        mFromDisplayName = getIntent().getExtras().getString(SMS_FROM_DISPLAY_NAME_EXTRA);
        mMessage = getIntent().getExtras().getString(SMS_MESSAGE_EXTRA);

        mFullBodyString = String.format(
                getResources().getString(R.string.sms_speak_string_format),
                mFromDisplayName,
                mMessage);

        //noinspection deprecation
        showDialog(DIALOG_SHOW_MESSAGE);
        mTts = new TextToSpeech(this, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = mTts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "TTS language is not available.");
            } else {
                //noinspection deprecation
                mTts.speak(mFullBodyString, TextToSpeech.QUEUE_ADD, null);
            }
        } else {
            // Initialization failed.
            Log.e(TAG, "Could not initialize TTS.");
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_SHOW_MESSAGE:
            return new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_email)
                    .setTitle("Message Received")
                    .setMessage(mFullBodyString)
                    .setPositiveButton(R.string.reply, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Begin creating the reply with the SmsMessagingDemo activity
                            Intent i = new Intent();
                            i.setClass(SmsReceivedDialog.this, SmsMessagingDemo.class);
                            i.putExtra(SmsMessagingDemo.SMS_RECIPIENT_EXTRA, mFromAddress);
                            startActivity(i);

                            dialog.dismiss();
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                            finish();
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }
                    }).create();
        }
        return null;
    }
}

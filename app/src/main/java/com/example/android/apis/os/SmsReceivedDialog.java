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

import java.util.Locale;

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
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}. We initialize our field {@code String mFromAddress} by retrieving the extra
     * with key SMS_FROM_ADDRESS_EXTRA from the {@code Intent}, our field {@code String mFromDisplayName}
     * by retrieving the extra with key SMS_FROM_DISPLAY_NAME_EXTRA, and our field {@code String mMessage}
     * by retrieving the extra with key SMS_MESSAGE_EXTRA. Then we format {@code mFromDisplayName} and
     * {@code mMessage} into a string using the format contained in R.string.sms_speak_string_format
     * to initialize our field {@code String mFullBodyString}. We call the method {@code showDialog}
     * to display our dialog with ID DIALOG_SHOW_MESSAGE (this calls our override {@code onCreateDialog}).
     * Finally we initialize our field {@code TextToSpeech mTts} with a new instance using "this" as
     * the {@code TextToSpeech.OnInitListener} (this will also initialize the associated TextToSpeech
     * engine if it isn't already running).
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

    /**
     * Called to signal the completion of the TextToSpeech engine initialization. If our parameter is
     * TextToSpeech.SUCCESS we set the text-to-speech language to Locale.US and if the {@code result}
     * of the call is LANG_MISSING_DATA, we log an error "TTS language is not available", otherwise
     * we call the {@code speak} method of {@code mTts} to speak the string in {@code mFullBodyString}.
     *
     * @param status {@link TextToSpeech#SUCCESS} or {@link TextToSpeech#ERROR}.
     */
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

    /**
     * Callback for creating dialogs that are managed (saved and restored) for you by the activity.
     * We switch based on the {@code int id} parameter, returning null for everything except the
     * ID DIALOG_SHOW_MESSAGE. For DIALOG_SHOW_MESSAGE we create and return an {@code AlertDialog}
     * whose icon is android.R.drawable.ic_dialog_email, whose message is "Message Received". Its
     * positive button displays the text R.string.reply ("Reply"), and has an {@code OnClickListener}
     * that is an anonymous class which creates an {@code Intent} to launch {@code SmsMessagingDemo},
     * adds the from address contained in {@code String mFromAddress} as an extra with the key
     * {@code SmsMessagingDemo.SMS_RECIPIENT_EXTRA}, and launches the {@code Intent} then dismisses
     * our dialog and finishes the {@code SmsReceivedDialog} activity we are a part of. Its negative
     * button displays the text R.string.dismiss ("Dismiss") and has an {@code OnClickListener} which
     * simply dismisses our dialog and finishes the {@code SmsReceivedDialog} activity we are a part of.
     * Its {@code OnCancelListener} just finishes the {@code SmsReceivedDialog} activity we are a part
     * of.
     *
     * @param id The id of the dialog.
     * @return The dialog.
     */
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

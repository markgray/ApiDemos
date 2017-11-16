/*
 * Copyright (C) 2014 The Android Open Source Project
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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.apis.R;
import com.google.android.mms.ContentType;
import com.google.android.mms.InvalidHeaderValueException;
import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.GenericPdu;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.PduComposer;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduParser;
import com.google.android.mms.pdu.PduPart;
import com.google.android.mms.pdu.RetrieveConf;
import com.google.android.mms.pdu.SendConf;
import com.google.android.mms.pdu.SendReq;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MmsMessagingDemo extends Activity {
    /**
     * TAG used for logging
     */
    private static final String TAG = "MmsMessagingDemo";

    /**
     * {@code Intent} Extras key for the X-Mms-Content-Location value (See the method
     * {@code getContentLocation} in class com.google.android.mms.pdu.NotificationInd)
     */
    public static final String EXTRA_NOTIFICATION_URL = "notification_url";

    /**
     * The action for {@code IntentFilter mSentFilter}, registered to be listened for by
     * {@code BroadcastReceiver mSentReceiver}. It is the action used for the {@code PendingIntent}
     * argument to the method {@code sendMultimediaMessage} which is broadcast when the message is
     * successfully sent, or failed.
     */
    private static final String ACTION_MMS_SENT = "com.example.android.apis.os.MMS_SENT_ACTION";
    /**
     * The action for {@code IntentFilter mReceivedFilter}, registered to be listened for by
     * {@code BroadcastReceiver mReceivedReceiver}. It is the action used for the {@code PendingIntent}
     * argument to the method {@code downloadMultimediaMessage} which is broadcast when the message
     * is downloaded, or the download has failed.
     */
    private static final String ACTION_MMS_RECEIVED = "com.example.android.apis.os.MMS_RECEIVED_ACTION";

    /**
     * {@code EditText} with ID R.id.mms_recipients_input in our layout file, used by the user for
     * setting the recipients of a message to be sent, and by our method {@code handleReceivedResult}
     * to display the recipients of a received message.
     */
    private EditText mRecipientsInput;
    /**
     * {@code EditText} with ID R.id.mms_subject_input in our layout file, used by the user for
     * setting the subject of a message to be sent, and by our method {@code handleReceivedResult}
     * to display the subject of a received message.
     */
    private EditText mSubjectInput;
    /**
     * {@code EditText} with ID R.id.mms_text_input in our layout file, used by the user for
     * entering the text of a message to be sent, and by our method {@code handleReceivedResult}
     * to display the text of a received message.
     */
    private EditText mTextInput;
    /**
     * {@code TextView} with ID R.id.mms_send_status in our layout file, used to display the status
     * of our activity, one of: R.string.mms_status_sending ("Sending"), R.string.mms_status_downloading
     * ("Downloading"), R.string.mms_status_failed ("Failed"), R.string.mms_status_sent ("Sent OK"),
     * or R.string.mms_status_downloaded ("Downloaded") depending on the status we are reporting.
     */
    private TextView mSendStatusView;
    /**
     * {@code Button} with ID R.id.mms_send_button, when clicked its {@code OnClickListener} calls
     * our method {@code sendMessage} which sends the MMS message we have composed, titled, and
     * addressed using the {@code EditText} views in our layout.
     */
    private Button mSendButton;
    /**
     * File we write our message pdu to. The method {@code make} of a {@code PduComposer} created
     * from the {@code SendReq} (see com.google.android.mms.pdu.SendReq) we build from the message
     * the user wants to send returns a byte[] array which we write to {@code mSendFile}. We then
     * pass an {@code Uri} pointing to this file when we call {@code sendMultimediaMessage}.
     */
    private File mSendFile;
    /**
     * File we download an MMS message to. We pass an {@code Uri} pointing to this file when we call
     * {@code downloadMultimediaMessage}. Our method {@code handleReceivedResult} is called to read
     * and parse this file when our {@code BroadcastReceiver mReceivedReceiver} receives an
     * ACTION_MMS_RECEIVED broadcast in its {@code onReceive} method.
     */
    private File mDownloadFile;
    /**
     * Random number generator used to create random file names for both send and download.
     */
    private Random mRandom = new Random();

    /**
     * {@code BroadcastReceiver} used to receive broadcast intents for {@code IntentFilter mSentFilter}
     * whose action is ACTION_MMS_SENT. Just passes the result code and {@code Intent} to our method
     * {@code handleSentResult}
     */
    private BroadcastReceiver mSentReceiver = new BroadcastReceiver() {
        /**
         * This method is called when the BroadcastReceiver is receiving an Intent broadcast that
         * matches {@code IntentFilter mSentFilter} (whose action is ACTION_MMS_SENT). We simply
         * pass the current result code and the {@code Intent} that was sent to us to our method
         * {@code handleSentResult}.
         *
         * @param context The Context in which the receiver is running.
         * @param intent The Intent being received.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            handleSentResult(getResultCode(), intent);
        }
    };
    /**
     * {@code IntentFilter} used for {@code BroadcastReceiver mSentReceiver}, its action is ACTION_MMS_SENT
     * ("com.example.android.apis.os.MMS_SENT_ACTION").
     */
    private IntentFilter mSentFilter = new IntentFilter(ACTION_MMS_SENT);

    /**
     * {@code BroadcastReceiver} used to receive broadcast intents for {@code IntentFilter mReceivedFilter}
     * whose action is ACTION_MMS_RECEIVED. Just passes the context we are running in, the result code
     * and {@code Intent} to our method {@code handleReceivedResult}
     */
    private BroadcastReceiver mReceivedReceiver = new BroadcastReceiver() {
        /**
         * This method is called when the BroadcastReceiver is receiving an Intent broadcast that
         * matches {@code IntentFilter mReceivedFilter} (whose action is ACTION_MMS_RECEIVED). We
         * simply pass the context we are running in, the current result code, and the {@code Intent}
         * that was sent to us to our method {@code handleReceivedResult}
         *
         * @param context The Context in which the receiver is running.
         * @param intent The Intent being received.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            handleReceivedResult(context, getResultCode(), intent);
        }
    };
    /**
     * {@code IntentFilter} used for {@code BroadcastReceiver mReceivedReceiver}, its action is
     * ACTION_MMS_RECEIVED ("com.example.android.apis.os.MMS_RECEIVED_ACTION").
     */
    private IntentFilter mReceivedFilter = new IntentFilter(ACTION_MMS_RECEIVED);

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file. We initialize our field
     * {@code CheckBox enableCheckBox} by locating the {@code CheckBox} with ID R.id.mms_enable_receiver
     * in our layout. We set {@code PackageManager pm} to a {@code PackageManager} instance. We create
     * {@code ComponentName componentName} with the name of the package that the component exists in
     * as "com.example.android.apis", and The name of the class inside of that package that implements
     * the component set to "com.example.android.apis.os.MmsWapPushReceiver". We use {@code componentName}
     * to fetch the enabled setting for {@code MmsWapPushReceiver} to set {@code int componentEnabledSetting}
     * (it will be one of COMPONENT_ENABLED_STATE_ENABLED, COMPONENT_ENABLED_STATE_DISABLED, or
     * COMPONENT_ENABLED_STATE_DEFAULT, with the last implying that its enabled state is the same as
     * that originally specified in the AndroidManifest which is disabled). If {@code componentEnabledSetting}
     * is COMPONENT_ENABLED_STATE_ENABLED (1) we set the {@code CheckBox enableCheckBox} to checked,
     * otherwise unchecked. We next set the {@code OnCheckedChangeListener} of {@code enableCheckBox}
     * to an anonymous class which sets the enabled setting for {@code componentName} to the new state
     * of {@code enableCheckBox} (this setting will override any enabled state which may have been set
     * by the component in its manifest, and is persistent). We also include the flag DONT_KILL_APP
     * in our call to {@code setComponentEnabledSetting} to indicate that we don't want to kill the
     * app containing the component.
     * <p>
     * We initialize our field {@code EditText mRecipientsInput} by locating the view with ID
     * R.id.mms_recipients_input, our field {@code EditText mSubjectInput} by locating the view with
     * ID R.id.mms_subject_input, our field {@code EditText mTextInput} by locating the view with ID
     * R.id.mms_text_input, our field {@code TextView mSendStatusView} by locating the view with ID
     * R.id.mms_send_status, and our field {@code Button mSendButton} by locating the view with ID
     * R.id.mms_send_button. We set the {@code OnClickListener} of {@code mSendButton} to an anonymous
     * class which calls our method {@code sendMessage} with the text from {@code mRecipientsInput},
     * {@code mSubjectInput}, and {@code mTextInput}.
     * <p>
     * We not register {@code BroadcastReceiver mSentReceiver} to receive any broadcast intents matching
     * {@code IntentFilter mSentFilter}, and {@code BroadcastReceiver mReceivedReceiver} to receive any
     * broadcast intents matching {@code IntentFilter mReceivedFilter}.
     * <p>
     * We fetch the intent that launched us to {@code Intent intent}, and try to extract an extra from
     * it with key EXTRA_NOTIFICATION_URL ("notification_url") to {@code String notificationIndUrl}.
     * If {@code notificationIndUrl} is not the empty string, we call our method {@code downloadMessage}
     * with {@code notificationIndUrl} as the parameter (which will download the SMS message from the
     * carrier on a background thread, using {@code notificationIndUrl} as the location URL of the MMS
     * message to be downloaded, obtained from the MMS WAP push notification by {@code MmsWapPushReceiver}).
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState}, so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mms_demo);

        // Enable or disable the broadcast receiver depending on the checked
        // state of the checkbox.
        final CheckBox enableCheckBox = (CheckBox) findViewById(R.id.mms_enable_receiver);
        final PackageManager pm = this.getPackageManager();
        final ComponentName componentName = new ComponentName("com.example.android.apis",
                "com.example.android.apis.os.MmsWapPushReceiver");
        final int componentEnabledSetting = pm.getComponentEnabledSetting(componentName);
        enableCheckBox.setChecked(componentEnabledSetting ==
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
        enableCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, (isChecked ? "Enabling" : "Disabling") + " MMS receiver");
                pm.setComponentEnabledSetting(componentName,
                        isChecked ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                                : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
            }
        });

        mRecipientsInput = (EditText) findViewById(R.id.mms_recipients_input);
        mSubjectInput = (EditText) findViewById(R.id.mms_subject_input);
        mTextInput = (EditText) findViewById(R.id.mms_text_input);
        mSendStatusView = (TextView) findViewById(R.id.mms_send_status);
        mSendButton = (Button) findViewById(R.id.mms_send_button);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when our {@code Button mSendButton} ("Send") is clicked. We fetch the text in
             * {@code EditText mRecipientsInput}, {@code EditText mSubjectInput}, and
             * {@code EditText mTextInput} to use as arguments to our method {@code sendMessage}
             * (which we call).
             *
             * @param v View that was clicked
             */
            @Override
            public void onClick(View v) {
                sendMessage(
                        mRecipientsInput.getText().toString(),
                        mSubjectInput.getText().toString(),
                        mTextInput.getText().toString());
            }
        });

        registerReceiver(mSentReceiver, mSentFilter);
        registerReceiver(mReceivedReceiver, mReceivedFilter);

        final Intent intent = getIntent();
        final String notificationIndUrl = intent.getStringExtra(EXTRA_NOTIFICATION_URL);
        if (!TextUtils.isEmpty(notificationIndUrl)) {
            downloadMessage(notificationIndUrl);
        }
    }

    /**
     * This is called for activities that set launchMode to "singleTop" in
     * their package, or if a client used the {@link Intent#FLAG_ACTIVITY_SINGLE_TOP}
     * flag when calling {@link #startActivity}.  In either case, when the
     * activity is re-launched while at the top of the activity stack instead
     * of a new instance of the activity being started, onNewIntent() will be
     * called on the existing instance with the Intent that was used to
     * re-launch it.
     * <p>
     * First we call our super's implementation of {@code onNewIntent}, then we retrieve the string
     * stored under the key EXTRA_NOTIFICATION_URL ("notification_url") to {@code String notificationIndUrl}
     * and if the result is not empty we call our method {@code downloadMessage} to download
     * {@code notificationIndUrl}.
     *
     * @param intent The new intent that was started for the activity.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        final String notificationIndUrl = intent.getStringExtra(EXTRA_NOTIFICATION_URL);
        if (!TextUtils.isEmpty(notificationIndUrl)) {
            downloadMessage(notificationIndUrl);
        }
    }

    /**
     * Sends the MMS message in a background thread. First we set the text of {@code TextView mSendStatusView}
     * to the string R.string.mms_status_sending ("Sending"), and disable {@code Button mSendButton}
     * (the "Send" button). We create a random string to set {@code String fileName}, creating a
     * filename consisting of "send." appended to the string value of a random long, appended to the
     * extension ".dat", then create {@code File mSendFile} using the application specific cache directory
     * as the path and {@code filename} as the file name.
     *
     * Now we create an anonymous {@code Runnable} class to send our MMS message, and start it running
     * in a background thread. (See the comments of the {@code Run} method of this {@code Runnable}
     * class for the details.
     *
     * @param recipients the intended recipients of the MMS message, read from text entered in
     *                   {@code EditText mRecipientsInput}.
     * @param subject    subject of the MMS message, read from text entered in
     *                   {@code EditText mSubjectInput}
     * @param text       text of the MMS message, read from text entered in {@code EditText mTextInput}
     */
    private void sendMessage(final String recipients, final String subject, final String text) {
        Log.d(TAG, "Sending");
        mSendStatusView.setText(getResources().getString(R.string.mms_status_sending));
        mSendButton.setEnabled(false);

        final String fileName = "send." + String.valueOf(Math.abs(mRandom.nextLong())) + ".dat";
        mSendFile = new File(getCacheDir(), fileName);

        // Making RPC call in non-UI thread
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            /**
             * Writes a PDU containing the {@code recipients}, {@code subject}, and {@code text} passed
             * to {@code sendMessage} to the file {@code mSendFile}, and creates Uri's which will allow
             * the MMS system to access this file to send it out.
             */
            @Override
            public void run() {
                final byte[] pdu = buildPdu(MmsMessagingDemo.this, recipients, subject, text);
                Uri writerUri = (new Uri.Builder())
                        .authority("com.example.android.apis.os.MmsFileProvider")
                        .path(fileName)
                        .scheme(ContentResolver.SCHEME_CONTENT)
                        .build();
                final PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        MmsMessagingDemo.this, 0, new Intent(ACTION_MMS_SENT), 0);
                FileOutputStream writer = null;
                Uri contentUri = null;
                try {
                    writer = new FileOutputStream(mSendFile);
                    writer.write(pdu);
                    contentUri = writerUri;
                } catch (final IOException e) {
                    Log.e(TAG, "Error writing send file", e);
                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException e) {
                            Log.i(TAG, e.getLocalizedMessage());
                        }
                    }
                }

                if (contentUri != null) {
                    SmsManager.getDefault().sendMultimediaMessage(getApplicationContext(),
                            contentUri, null/*locationUrl*/, null/*configOverrides*/,
                            pendingIntent);
                } else {
                    Log.e(TAG, "Error writing sending Mms");
                    try {
                        pendingIntent.send(SmsManager.MMS_ERROR_IO_ERROR);
                    } catch (CanceledException ex) {
                        Log.e(TAG, "Mms pending intent cancelled?", ex);
                    }
                }
            }
        });
    }

    private void downloadMessage(final String locationUrl) {
        Log.d(TAG, "Downloading " + locationUrl);
        mSendStatusView.setText(getResources().getString(R.string.mms_status_downloading));
        mSendButton.setEnabled(false);
        mRecipientsInput.setText("");
        mSubjectInput.setText("");
        mTextInput.setText("");
        final String fileName = "download." + String.valueOf(Math.abs(mRandom.nextLong())) + ".dat";
        mDownloadFile = new File(getCacheDir(), fileName);
        // Making RPC call in non-UI thread
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                Uri contentUri = (new Uri.Builder())
                        .authority("com.example.android.apis.os.MmsFileProvider")
                        .path(fileName)
                        .scheme(ContentResolver.SCHEME_CONTENT)
                        .build();
                final PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        MmsMessagingDemo.this, 0, new Intent(ACTION_MMS_RECEIVED), 0);
                SmsManager.getDefault().downloadMultimediaMessage(getApplicationContext(),
                        locationUrl, contentUri, null/*configOverrides*/, pendingIntent);
            }
        });
    }

    private void handleSentResult(int code, Intent intent) {
        //noinspection ResultOfMethodCallIgnored
        mSendFile.delete();
        int status = R.string.mms_status_failed;
        if (code == Activity.RESULT_OK) {
            final byte[] response = intent.getByteArrayExtra(SmsManager.EXTRA_MMS_DATA);
            if (response != null) {
                final GenericPdu pdu = new PduParser(
                        response, PduParserUtil.shouldParseContentDisposition()).parse();
                if (pdu instanceof SendConf) {
                    final SendConf sendConf = (SendConf) pdu;
                    if (sendConf.getResponseStatus() == PduHeaders.RESPONSE_STATUS_OK) {
                        status = R.string.mms_status_sent;
                    } else {
                        Log.e(TAG, "MMS sent, error=" + sendConf.getResponseStatus());
                    }
                } else {
                    Log.e(TAG, "MMS sent, invalid response");
                }
            } else {
                Log.e(TAG, "MMS sent, empty response");
            }
        } else {
            Log.e(TAG, "MMS not sent, error=" + code);
        }

        mSendFile = null;
        mSendStatusView.setText(status);
        mSendButton.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSentReceiver != null) {
            unregisterReceiver(mSentReceiver);
        }
        if (mReceivedReceiver != null) {
            unregisterReceiver(mReceivedReceiver);
        }
    }

    @SuppressWarnings("UnusedParameters")
    private void handleReceivedResult(Context context, int code, Intent intent) {
        int status = R.string.mms_status_failed;
        if (code == Activity.RESULT_OK) {
            try {
                final int nBytes = (int) mDownloadFile.length();
                FileInputStream reader = new FileInputStream(mDownloadFile);
                final byte[] response = new byte[nBytes];
                final int read = reader.read(response, 0, nBytes);
                if (read == nBytes) {
                    final GenericPdu pdu = new PduParser(
                            response, PduParserUtil.shouldParseContentDisposition()).parse();
                    if (pdu instanceof RetrieveConf) {
                        final RetrieveConf retrieveConf = (RetrieveConf) pdu;
                        mRecipientsInput.setText(getRecipients(context, retrieveConf));
                        mSubjectInput.setText(getSubject(retrieveConf));
                        mTextInput.setText(getMessageText(retrieveConf));
                        status = R.string.mms_status_downloaded;
                    } else {
                        Log.e(TAG, "MMS received, invalid response");
                    }
                } else {
                    Log.e(TAG, "MMS received, empty response");
                }
            } catch (FileNotFoundException e) {
                Log.e(TAG, "MMS received, file not found exception", e);
            } catch (IOException e) {
                Log.e(TAG, "MMS received, io exception", e);
            } finally {
                //noinspection ResultOfMethodCallIgnored
                mDownloadFile.delete();
            }
        } else {
            Log.e(TAG, "MMS not received, error=" + code);
        }
        mDownloadFile = null;
        mSendStatusView.setText(status);
        mSendButton.setEnabled(true);
    }

    public static final long DEFAULT_EXPIRY_TIME = 7 * 24 * 60 * 60;
    public static final int DEFAULT_PRIORITY = PduHeaders.PRIORITY_NORMAL;

    private static final String TEXT_PART_FILENAME = "text_0.txt";
    private static final String sSmilText =
            "<smil>" +
                    "<head>" +
                    "<layout>" +
                    "<root-layout/>" +
                    "<region height=\"100%%\" id=\"Text\" left=\"0%%\" top=\"0%%\" width=\"100%%\"/>" +
                    "</layout>" +
                    "</head>" +
                    "<body>" +
                    "<par dur=\"8000ms\">" +
                    "<text src=\"%s\" region=\"Text\"/>" +
                    "</par>" +
                    "</body>" +
                    "</smil>";

    private static byte[] buildPdu(Context context, String recipients, String subject, String text) {
        final SendReq req = new SendReq();
        // From, per spec
        final String lineNumber = getSimNumber(context);
        if (!TextUtils.isEmpty(lineNumber)) {
            req.setFrom(new EncodedStringValue(lineNumber));
        }
        // To
        EncodedStringValue[] encodedNumbers =
                EncodedStringValue.encodeStrings(recipients.split(" "));
        if (encodedNumbers != null) {
            req.setTo(encodedNumbers);
        }
        // Subject
        if (!TextUtils.isEmpty(subject)) {
            req.setSubject(new EncodedStringValue(subject));
        }
        // Date
        req.setDate(System.currentTimeMillis() / 1000);
        // Body
        PduBody body = new PduBody();
        // Add text part. Always add a smil part for compatibility, without it there
        // may be issues on some carriers/client apps
        final int size = addTextPart(body, text, true/* add text smil */);
        req.setBody(body);
        // Message size
        req.setMessageSize(size);
        // Message class
        req.setMessageClass(PduHeaders.MESSAGE_CLASS_PERSONAL_STR.getBytes());
        // Expiry
        req.setExpiry(DEFAULT_EXPIRY_TIME);
        try {
            // Priority
            req.setPriority(DEFAULT_PRIORITY);
            // Delivery report
            req.setDeliveryReport(PduHeaders.VALUE_NO);
            // Read report
            req.setReadReport(PduHeaders.VALUE_NO);
        } catch (InvalidHeaderValueException e) {
            Log.i(TAG, e.getLocalizedMessage());
        }

        return new PduComposer(context, req).make();
    }

    @SuppressWarnings("SameParameterValue")
    private static int addTextPart(PduBody pb, String message, boolean addTextSmil) {
        final PduPart part = new PduPart();
        // Set Charset if it's a text media.
        part.setCharset(CharacterSets.UTF_8);
        // Set Content-Type.
        part.setContentType(ContentType.TEXT_PLAIN.getBytes());
        // Set Content-Location.
        part.setContentLocation(TEXT_PART_FILENAME.getBytes());
        int index = TEXT_PART_FILENAME.lastIndexOf(".");
        String contentId = (index == -1) ? TEXT_PART_FILENAME
                : TEXT_PART_FILENAME.substring(0, index);
        part.setContentId(contentId.getBytes());
        part.setData(message.getBytes());
        pb.addPart(part);
        if (addTextSmil) {
            final String smil = String.format(sSmilText, TEXT_PART_FILENAME);
            addSmilPart(pb, smil);
        }
        return part.getData().length;
    }

    private static void addSmilPart(PduBody pb, String smil) {
        final PduPart smilPart = new PduPart();
        smilPart.setContentId("smil".getBytes());
        smilPart.setContentLocation("smil.xml".getBytes());
        smilPart.setContentType(ContentType.APP_SMIL.getBytes());
        smilPart.setData(smil.getBytes());
        pb.addPart(0, smilPart);
    }

    private static String getRecipients(Context context, RetrieveConf retrieveConf) {
        final String self = getSimNumber(context);
        final StringBuilder sb = new StringBuilder();
        if (retrieveConf.getFrom() != null) {
            sb.append(retrieveConf.getFrom().getString());
        }
        if (retrieveConf.getTo() != null) {
            for (EncodedStringValue to : retrieveConf.getTo()) {
                final String number = to.getString();
                if (!PhoneNumberUtils.compare(number, self)) {
                    sb.append(" ").append(to.getString());
                }
            }
        }
        if (retrieveConf.getCc() != null) {
            for (EncodedStringValue cc : retrieveConf.getCc()) {
                final String number = cc.getString();
                if (!PhoneNumberUtils.compare(number, self)) {
                    sb.append(" ").append(cc.getString());
                }
            }
        }
        return sb.toString();
    }

    private static String getSubject(RetrieveConf retrieveConf) {
        final EncodedStringValue subject = retrieveConf.getSubject();
        return subject != null ? subject.getString() : "";
    }

    private static String getMessageText(RetrieveConf retrieveConf) {
        final StringBuilder sb = new StringBuilder();
        final PduBody body = retrieveConf.getBody();
        if (body != null) {
            for (int i = 0; i < body.getPartsNum(); i++) {
                final PduPart part = body.getPart(i);
                if (part != null
                        && part.getContentType() != null
                        && ContentType.isTextType(new String(part.getContentType()))) {
                    sb.append(new String(part.getData()));
                }
            }
        }
        return sb.toString();
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    private static String getSimNumber(Context context) {
        final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            return telephonyManager.getLine1Number();
        }
        return null;
    }
}

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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.util.Log;

import com.google.android.mms.ContentType;
import com.google.android.mms.pdu.GenericPdu;
import com.google.android.mms.pdu.NotificationInd;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduParser;

/**
 * Receiver for MMS WAP push, used by {@code MmsMessagingDemo}
 */
public class MmsWapPushReceiver extends BroadcastReceiver {
    /**
     * TAG used for logging
     */
    private static final String TAG = "MmsMessagingDemo";

    /**
     * This method is called when the BroadcastReceiver is receiving an Intent broadcast for either
     * of the actions WAP_PUSH_RECEIVED, or WAP_PUSH_DELIVER (we only handle WAP_PUSH_RECEIVED).
     * <p>
     * First we make sure that the action of the {@code Intent intent} is WAP_PUSH_RECEIVED_ACTION,
     * and that the {@code ContentType} is MMS_MESSAGE ("application/vnd.wap.mms-message") and ignore
     * it if it not. Otherwise we retrieve {@code byte[] data} from the {@code intent} extra stored
     * under the key "data". We create {@code PduParser parser} from {@code data}, and set our variable
     * {@code GenericPdu pdu} to null. Then wrapped in a try block intended to catch RuntimeException
     * we set {@code pdu} to the pdu structure parsed by {@code parser}. If {@code pdu} is null we
     * log the error and return having done nothing.
     * <p>
     * Now we switch based on the X-Mms-Message-Type field value of {@code pdu}:
     * <ul>
     * <li>
     * MESSAGE_TYPE_NOTIFICATION_IND - we cast {@code pdu} to {@code NotificationInd nInd},
     * fetch its X-Mms-Content-Location value to {@code String location}. We create a new
     * instance for {@code Intent di}, set its class to {@code MmsMessagingDemo.class}, add
     * the flags FLAG_ACTIVITY_NEW_TASK, and FLAG_ACTIVITY_SINGLE_TOP, add the extra
     * {@code location} under the key EXTRA_NOTIFICATION_URL ("notification_url") and start
     * the activity specified by {@code di} running.
     * </li>
     * <li>
     * MESSAGE_TYPE_DELIVERY_IND - we simply log the message "Received delivery report"
     * </li>
     * <li>
     * MESSAGE_TYPE_READ_ORIG_IND - we simply log the message "Received read report"
     * </li>
     * </ul>
     *
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION.equals(intent.getAction())
                && ContentType.MMS_MESSAGE.equals(intent.getType())) {
            final byte[] data = intent.getByteArrayExtra("data");
            final PduParser parser = new PduParser(
                    data, PduParserUtil.shouldParseContentDisposition());
            GenericPdu pdu = null;
            try {
                pdu = parser.parse();
            } catch (final RuntimeException e) {
                Log.e(TAG, "Invalid MMS WAP push", e);
            }
            if (pdu == null) {
                Log.e(TAG, "Invalid WAP push data");
                return;
            }
            switch (pdu.getMessageType()) {
                case PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND: {
                    final NotificationInd nInd = (NotificationInd) pdu;
                    final String location = new String(nInd.getContentLocation());
                    Log.v(TAG, "Received MMS notification: " + location);
                    final Intent di = new Intent();
                    di.setClass(context, MmsMessagingDemo.class);
                    di.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    di.putExtra(MmsMessagingDemo.EXTRA_NOTIFICATION_URL, location);
                    context.startActivity(di);
                    break;
                }
                // TODO: impl. handling of the following push
                case PduHeaders.MESSAGE_TYPE_DELIVERY_IND: {
                    Log.v(TAG, "Received delivery report");
                    break;
                }
                case PduHeaders.MESSAGE_TYPE_READ_ORIG_IND: {
                    Log.v(TAG, "Received read report");
                    break;
                }
            }
        }
    }
}

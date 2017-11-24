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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;

/**
 * {@code BroadcastReceiver} for the SMS actions android.provider.Telephony.SMS_RECEIVED and
 * android.provider.Telephony.SMS_DELIVER, a part of the {@code SmsMessagingDemo} sample code.
 */
public class SmsMessageReceiver extends BroadcastReceiver {
    /**
     * This method is called when the BroadcastReceiver is receiving an Intent broadcast. First we
     * retrieve all the extras from the {@code Intent} that launched us into {@code Bundle extras},
     * and if it is null we return having done nothing. We retrieve the object stored in {@code extras}
     * under the key "pdus" to {@code Object[] pdus}. Then for all the objects in {@code Object[] pdus}
     * we retrieve the {@code SmsMessage message} from the current {@code Object[] pdus}. We fetch
     * the originating address from {@code SmsMessage message} to {@code String fromAddress} and set
     * {@code String fromDisplayName} to {@code fromAddress}.
     * <p>
     * We declare {@code Uri uri}, and {@code String[] projection}. Then set {@code uri} to an {@code Uri}
     * whose path is ContactsContract.PhoneLookup.CONTENT_FILTER_URI (the content:// style URI for
     * the the {@code PhoneLookup} table), with the encoded phone number in {@code fromAddress} appended
     * to it (the phone number to lookup in the Contacts database). We set {@code projection} to an
     * array of {@code String[]} whose only entry is for the DISPLAY_NAME column of the database.
     * <p>
     * We create {@code Cursor cursor} by querying {@code uri} with the projection {@code projection}
     * and it is not null, we move {@code cursor} to the first row and set {@code fromDisplayName}
     * to the string in column index 0, and then close {@code cursor}.
     * <p>
     * We now create a new instance for {@code Intent di}, set its class to {@code SmsReceivedDialog},
     * add the flags FLAG_ACTIVITY_NEW_TASK and FLAG_ACTIVITY_SINGLE_TOP, add {@code fromAddress} as
     * an extra under the key SmsReceivedDialog.SMS_FROM_ADDRESS_EXTRA, {@code fromDisplayName} under
     * the key SmsReceivedDialog.SMS_FROM_DISPLAY_NAME_EXTRA, and the body of the {@code message} pdu
     * as a string under the key SmsReceivedDialog.SMS_MESSAGE_EXTRA. We then start the activity that
     * {@code di} is an intent for ({@code SmsReceivedDialog}).
     * <p>
     * Finally we break out of the loop without processing the rest of the pdu objects in {@code pdus}.
     *
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    @SuppressWarnings({"UnusedAssignment", "SpellCheckingInspection"})
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return;
        }

        Object[] pdus = (Object[]) extras.get("pdus");

        //noinspection ConstantConditions,ForLoopReplaceableByForEach,LoopStatementThatDoesntLoop
        for (int i = 0; i < pdus.length; i++) {
            @SuppressWarnings("deprecation")
            SmsMessage message = SmsMessage.createFromPdu((byte[]) pdus[i]);
            String fromAddress = message.getOriginatingAddress();
            String fromDisplayName = fromAddress;

            Uri uri;
            String[] projection;

            // If targeting Donut or below, use
            // Contacts.Phones.CONTENT_FILTER_URL and
            // Contacts.Phones.DISPLAY_NAME
            uri = Uri.withAppendedPath(
                    ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(fromAddress));
            projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

            // Query the filter URI
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst())
                    fromDisplayName = cursor.getString(0);

                cursor.close();
            }

            // Trigger the main activity to fire up a dialog that shows/reads the received messages
            Intent di = new Intent();
            di.setClass(context, SmsReceivedDialog.class);
            di.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            di.putExtra(SmsReceivedDialog.SMS_FROM_ADDRESS_EXTRA, fromAddress);
            di.putExtra(SmsReceivedDialog.SMS_FROM_DISPLAY_NAME_EXTRA, fromDisplayName);
            //noinspection RedundantStringToString
            di.putExtra(SmsReceivedDialog.SMS_MESSAGE_EXTRA, message.getMessageBody().toString());
            context.startActivity(di);

            // For the purposes of this demo, we'll only handle the first received message.
            break;
        }
    }
}

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

package com.example.android.apis.content;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.example.android.apis.R;

/**
 * Demonstrates launching the contacts app to pick a contact.  Does not
 * require permission to read contacts, as that permission will be granted
 * when the selected contact is returned.
 */
public class PickContact extends Activity {
    /**
     * The MIME type of a CONTENT_URI subdirectory of a single person.
     * Constant Value: "vnd.android.cursor.item/contact"
     */
    final String CONTACT = ContactsContract.Contacts.CONTENT_ITEM_TYPE;
    /**
     * The MIME type of a CONTENT_URI subdirectory of a single person.
     * Constant Value: "vnd.android.cursor.item/person"
     */
    final String PERSON = "vnd.android.cursor.item/person";
    /**
     * A data kind representing a telephone number. MIME type used when storing this in data table.
     * Constant Value: "vnd.android.cursor.item/phone_v2"
     */
    final String PHONE = ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE;
    /**
     * A data kind representing a postal addresses and MIME type used when storing this in data table.
     * Constant Value: "vnd.android.cursor.item/postal-address_v2"
     */
    final String POSTAL = ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE;
    /**
     * Toast we will toast the contact data base query result in
     */
    Toast mToast;
    /**
     * {@code ResultDisplayer} instance that launched contact data base query, it is set to "this"
     * in the {@code onClick} override when the Button using "this" {@code ResultDisplayer} as its
     * {@code OnClickListener} is clicked, and used in our {@code onActivityResult} when the
     * {@code Intent} launched returns a result in order to access the field {@code ResultDisplayer.mMsg}
     * to use in the toast.
     */
    ResultDisplayer mPendingResult;

    @SuppressWarnings("WeakerAccess")
    class ResultDisplayer implements OnClickListener {
        String mMsg;
        String mMimeType;

        ResultDisplayer(String msg, String mimeType) {
            mMsg = msg;
            mMimeType = mimeType;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType(mMimeType);
            mPendingResult = this;
            startActivityForResult(intent, 1);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.pick_contact);

        // Watch for button clicks.
        findViewById(R.id.pick_contact).setOnClickListener(new ResultDisplayer("Selected contact", CONTACT));
        findViewById(R.id.pick_person).setOnClickListener(new ResultDisplayer("Selected person", PERSON));
        findViewById(R.id.pick_phone).setOnClickListener(new ResultDisplayer("Selected phone", PHONE));
        findViewById(R.id.pick_address).setOnClickListener(new ResultDisplayer("Selected address", POSTAL));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                Cursor c = null;
                try {
                    c = getContentResolver().query(uri, new String[]{BaseColumns._ID}, null, null, null);
                    if (c != null && c.moveToFirst()) {
                        int id = c.getInt(0);
                        if (mToast != null) {
                            mToast.cancel();
                        }
                        String txt = mPendingResult.mMsg + ":\n" + uri + "\nid: " + id;
                        mToast = Toast.makeText(this, txt, Toast.LENGTH_LONG);
                        mToast.show();
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        }
    }
}


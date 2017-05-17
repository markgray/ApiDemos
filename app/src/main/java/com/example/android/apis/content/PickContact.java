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
     * in the {@code onClick} override of {@code ResultDisplayer} when the Button using "this"
     * {@code ResultDisplayer} as its {@code OnClickListener} is clicked, and used in our
     * {@code onActivityResult} when the {@code Intent} launched returns a result in order to access
     * the field {@code ResultDisplayer.mMsg} to use in the toast.
     */
    ResultDisplayer mPendingResult;

    /**
     * This is a custom {@code OnClickListener} which is used for each of the {@code Button}'s in our
     * UI. It uses its constructor to specify a string to be used as part of the toast which will be
     * shown by {@code onActivityResult}, as well as the MIME data type used in the {@code Intent}
     * which launches the contact data base query.
     */
    @SuppressWarnings("WeakerAccess")
    class ResultDisplayer implements OnClickListener {
        /**
         * String to use as part of the toast displayed when the result of the contact database query
         * is returned to {@code onActivityResult}.
         */
        String mMsg;
        /**
         * MIME data type to use for the {@code Intent} launching the contact database query.
         */
        String mMimeType;

        /**
         * Constructor used for each Button in our UI, we save the parameter {@code String msg} in our
         * field {@code String mMsg} and {@code String mimeType} in {@code String mMimeType}
         *
         * @param msg      String to use as part of the toast displayed when the result of the contact
         *                 database query is returned to {@code onActivityResult}.
         * @param mimeType MIME data type to use for the {@code Intent} launching the contact
         *                 database query.
         */
        ResultDisplayer(String msg, String mimeType) {
            mMsg = msg;
            mMimeType = mimeType;
        }

        /**
         * Called when the Button is clicked that uses this instance of {@code ResultDisplayer} as
         * its {@code OnClickListener}. First we create {@code Intent intent} with the action
         * ACTION_GET_CONTENT (Allows the user to select a particular kind of data and return it).
         * We set an explicit MIME data type for {@code intent} to be the contents of our field
         * {@code String mMimeType} (which is set for this instance in our constructor), set our
         * Activity's field {@code ResultDisplayer mPendingResult} to this so that we can be found
         * by {@code onActivityResult} when a result for a query is returned, and finally we use
         * {@code intent} to launch an activity for which we would like a result when it finishes.
         * (Said result will be returned to {@code onActivityResult} in the sweet by-and-by).
         *
         * @param v View of Button that was clicked
         */
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType(mMimeType);
            mPendingResult = this;
            startActivityForResult(intent, 1);
        }
    }

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.pick_contact. We
     * locate each of the four Buttons in our UI and set their {@code OnClickListener} to an
     * instance of {@code ResultDisplayer} using the parameters of the constructor to customize the
     * behavior when a particular Button is clicked as follows:
     * <ul>
     * <li>R.id.pick_contact "Selected contact" "vnd.android.cursor.item/contact"</li>
     * <li>R.id.pick_person "Selected person" "vnd.android.cursor.item/person"</li>
     * <li>R.id.pick_phone "Selected phone" "vnd.android.cursor.item/phone_v2"</li>
     * <li>R.id.pick_address "Selected address" "vnd.android.cursor.item/postal-address_v2"</li>
     * </ul>
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
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

    /**
     * Called when an activity you launched exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * <p>
     * If our parameter {@code Intent data} is non-null, we retrieve the {@code Uri uri} data this
     * intent is operating on. If {@code uri} is non-null we allocate {@code Cursor c}, setting it
     * to null, then wrapped in a try block we use a ContentResolver instance for our application's
     * package to query {@code uri} for the column containing the unique ID for a row, saving the
     * {@code Cursor} returned in {@code Cursor c}. If {@code c} is now non-null and we can
     * successfully move to the first row of {@code c} we retrieve the {@code int} value of column
     * 0 of that row to {@code int id}. If there has been a toast already toasted using our field
     * {@code Toast mToast} we cancel it.
     * <p>
     * We now create {@code String txt} using our field {@code ResultDisplayer mPendingResult} to
     * retrieve the value of the field {@code String mMsg} of the {@code ResultDisplayer} instance
     * whose result we are receiving after its Button was clicked, concatenated with the string value
     * of {@code uri} and the value of {@code id}. We set {@code Toast mToast} to a toast that uses
     * {@code txt} as its text and show it.
     * <p>
     * The finally block of our try then closes the {@code Cursor c} if it is not null.
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached as Intent "extras").
     */
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


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

package com.example.android.apis.view;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.apis.R;

/**
 * Shows how to use the ContentResolver for the contacts database as the source
 * of data for an auto complete lookup of a contact. It uses android:completionHint
 * to show the hint "Typing * will show all of your contacts." in the AutoCompleteTextView.
 */
public class AutoComplete5 extends AppCompatActivity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.autocomplete_5.
     * We initialize {@code ContentResolver content} with a ContentResolver instance for our
     * application's package, create {@code Cursor cursor} by querying the URI Contacts.CONTENT_URI
     * (content://com.android.contacts/contacts), with the projection {@code AutoComplete4.CONTACT_PROJECTION}
     * returning a Cursor over the result set. We create {@code AutoComplete4.ContactListAdapter adapter} from
     * {@code cursor}, initialize {@code AutoCompleteTextView textView} by finding the view with ID
     * R.id.edit, and set its adapter to {@code adapter}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.autocomplete_5);

        ContentResolver content = getContentResolver();
        @SuppressLint("Recycle")
        Cursor cursor = content.query(Contacts.CONTENT_URI, AutoComplete4.CONTACT_PROJECTION, null, null, null);
        AutoComplete4.ContactListAdapter adapter = new AutoComplete4.ContactListAdapter(this, cursor);

        AutoCompleteTextView textView = findViewById(R.id.edit);
        textView.setAdapter(adapter);
    }
}
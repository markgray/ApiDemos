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
package com.example.android.apis.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R
import com.example.android.apis.view.AutoComplete4.ContactListAdapter

/**
 * Shows how to use the ContentResolver for the contacts database as the source
 * of data for an auto complete lookup of a contact. It uses android:completionHint
 * to show the hint "Typing * will show all of your contacts." in the AutoCompleteTextView.
 */
class AutoComplete5 : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.autocomplete_5.
     * We initialize `ContentResolver content` with a ContentResolver instance for our
     * application's package, create `Cursor cursor` by querying the URI Contacts.CONTENT_URI
     * (content://com.android.contacts/contacts), with the projection `AutoComplete4.CONTACT_PROJECTION`
     * returning a Cursor over the result set. We create `AutoComplete4.ContactListAdapter adapter` from
     * `cursor`, initialize `AutoCompleteTextView textView` by finding the view with ID
     * R.id.edit, and set its adapter to `adapter`.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.autocomplete_5)
        val content = contentResolver
        @SuppressLint("Recycle")
        val cursor = content.query(
                ContactsContract.Contacts.CONTENT_URI,
                AutoComplete4.CONTACT_PROJECTION,
                null,
                null,
                null
        )
        val adapter = ContactListAdapter(this, cursor)
        val textView = findViewById<AutoCompleteTextView>(R.id.edit)
        textView.setAdapter(adapter)
    }
}
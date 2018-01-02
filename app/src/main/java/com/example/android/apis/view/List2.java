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

import android.app.ListActivity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;

/**
 * A list view example where the data comes from a cursor. Builds a cursor using
 * getContentResolver().query(...) of the Contacts provider for a projection of
 * Contacts._ID, and Contacts.DISPLAY_NAME and sets it as the ListAdapter for the
 * ListActivity's list
 */
@SuppressWarnings("deprecation")
public class List2 extends ListActivity {
    /**
     * Projection for the data we want from the contacts data base.
     */
    private static final String[] CONTACT_PROJECTION = new String[]{
            Contacts._ID,
            Contacts.DISPLAY_NAME
    };

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}. Next we retrieve {@code Cursor c} by querying the uri Contacts.CONTENT_URI
     * ("content://com.android.contacts/contacts"), for the projection CONTACT_PROJECTION, and null
     * for the rest of the arguments to {@code query}. We call the method {@code startManagingCursor}
     * so the {@code Activity} will take care of managing {@code Cursor c}. Then we create a new
     * instance of {@code SimpleCursorAdapter} for our variable {@code ListAdapter adapter} from
     * {@code Cursor c} using the layout android.R.layout.simple_list_item_1 to display the DISPLAY_NAME
     * field of our data in the {@code TextView} with ID android.R.id.text1. Finally we set our list
     * adapter to {@code adapter}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get a cursor with all people
        Cursor c = getContentResolver().query(Contacts.CONTENT_URI, CONTACT_PROJECTION,
                null, null, null);
        startManagingCursor(c);

        ListAdapter adapter = new SimpleCursorAdapter(this,
                // Use a template that displays a text view
                android.R.layout.simple_list_item_1,
                // Give the cursor to the list adapter
                c,
                // Map the NAME column in the people database to...
                new String[]{Contacts.DISPLAY_NAME},
                // The "text1" view defined in the XML template
                new int[]{android.R.id.text1});
        setListAdapter(adapter);
    }
}
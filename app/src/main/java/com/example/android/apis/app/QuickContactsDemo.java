/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.example.android.apis.app;

import com.example.android.apis.R;

import android.app.ListActivity;
import android.content.Context;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.view.View;
import android.view.ViewGroup;
import android.widget.QuickContactBadge;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

/**
 * Shows how to query the contacts database using a cursor, and display the results in a
 * ListActivity hosted ListView. It uses a deprecated api which queries the database on
 * the UI thread -- it is better to use LoaderManager with a CursorLoader. See the
 * documentation for LoaderManager for an example of how to do this.
 * TODO: write LoaderManager version.
 */
@SuppressWarnings({"unused"})
public class QuickContactsDemo extends ListActivity {
    /**
     * A list of which columns to return.
     */
    static final String[] CONTACTS_SUMMARY_PROJECTION = new String[] {
            Contacts._ID, // 0
            Contacts.DISPLAY_NAME, // 1
            Contacts.STARRED, // 2
            Contacts.TIMES_CONTACTED, // 3
            Contacts.CONTACT_PRESENCE, // 4
            Contacts.PHOTO_ID, // 5
            Contacts.LOOKUP_KEY, // 6
            Contacts.HAS_PHONE_NUMBER, // 7
    };

    /**
     * Indexes to column in cursor returned. See CONTACTS_SUMMARY_PROJECTION above
     */
    static final int SUMMARY_ID_COLUMN_INDEX = 0;
    static final int SUMMARY_NAME_COLUMN_INDEX = 1;
    static final int SUMMARY_STARRED_COLUMN_INDEX = 2;
    static final int SUMMARY_TIMES_CONTACTED_COLUMN_INDEX = 3;
    static final int SUMMARY_PRESENCE_STATUS_COLUMN_INDEX = 4;
    static final int SUMMARY_PHOTO_ID_COLUMN_INDEX = 5;
    static final int SUMMARY_LOOKUP_KEY = 6;
    static final int SUMMARY_HAS_PHONE_COLUMN_INDEX = 7;

    /**
     * Called when the activity is starting. First we call through to our super's implementation
     * of onCreate. Then we create an SQL selection String select to use for our contacts table
     * query specifying that the DISPLAY_NAME is not null, the contact HAS_PHONE_NUMBER is 1 and
     * the DISPLAY_NAME is not the empty String. Next we query the contacts table requesting the
     * CONTACTS_SUMMARY_PROJECTION columns, using our "select" selection to filter which rows to
     * retrieve, a null for the selection arguments, and sorted by the DISPLAY_NAME. We use the
     * Cursor c returned for that query to create ContactListItemAdapter adapter using our items
     * layout R.layout.quick_contacts, and set the list adapter for this ListActivity to "adapter".
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String select = "((" + Contacts.DISPLAY_NAME + " NOTNULL) AND ("
                + Contacts.HAS_PHONE_NUMBER + "=1) AND ("
                + Contacts.DISPLAY_NAME + " != '' ))";
        Cursor c =
                getContentResolver().query(Contacts.CONTENT_URI, CONTACTS_SUMMARY_PROJECTION, select,
                null, Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
        //noinspection deprecation
        startManagingCursor(c);
        ContactListItemAdapter adapter = new ContactListItemAdapter(this, R.layout.quick_contacts, c);
        setListAdapter(adapter);

    }

    /**
     * This is the class we use to display contact items in our ListActivity's List.
     */
    private final class ContactListItemAdapter extends ResourceCursorAdapter {
        /**
         * Creates a ContactListItemAdapter.
         *
         * @param context The context where the ListView associated with this adapter is running
         * @param layout resource identifier of a layout file that defines the views for this list
         *               item. Unless you override them later, this will define both the item views
         *               and the drop down views
         * @param c Cursor
         */
        @SuppressWarnings("WeakerAccess")
        public ContactListItemAdapter(Context context, int layout, Cursor c) {
            //noinspection deprecation
            super(context, layout, c);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final ContactListItemCache cache = (ContactListItemCache) view.getTag();
            // Set the name
            cursor.copyStringToBuffer(SUMMARY_NAME_COLUMN_INDEX, cache.nameBuffer);
            int size = cache.nameBuffer.sizeCopied;
            cache.nameView.setText(cache.nameBuffer.data, 0, size);
            final long contactId = cursor.getLong(SUMMARY_ID_COLUMN_INDEX);
            final String lookupKey = cursor.getString(SUMMARY_LOOKUP_KEY);
            cache.photoView.assignContactUri(Contacts.getLookupUri(contactId, lookupKey));
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = super.newView(context, cursor, parent);
            ContactListItemCache cache = new ContactListItemCache();
            cache.nameView = (TextView) view.findViewById(R.id.name);
            cache.photoView = (QuickContactBadge) view.findViewById(R.id.badge);
            view.setTag(cache);

            return view;
        }
    }

    final static class ContactListItemCache {
        @SuppressWarnings("WeakerAccess")
        public TextView nameView;
        @SuppressWarnings("WeakerAccess")
        public QuickContactBadge photoView;
        @SuppressWarnings("WeakerAccess")
        public CharArrayBuffer nameBuffer = new CharArrayBuffer(128);
    }
}

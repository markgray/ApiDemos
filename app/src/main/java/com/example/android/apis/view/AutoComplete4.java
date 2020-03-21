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
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.FilterQueryProvider;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.apis.R;

/**
 * Shows how to use the ContentResolver for the contacts database as the source
 * of data for an auto complete lookup of a contract.
 */
public class AutoComplete4 extends AppCompatActivity {
    /**
     * Projection containing a list of which columns to return from the contacts database.
     */
    public static final String[] CONTACT_PROJECTION = new String[]{
            Contacts._ID,
            Contacts.DISPLAY_NAME
    };
    /**
     * Index of the column in the cursor containing Contacts.DISPLAY_NAME
     */
    private static final int COLUMN_DISPLAY_NAME = 1;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.autocomplete_4.
     * We initialize {@code ContentResolver content} with a ContentResolver instance for our
     * application's package, create {@code Cursor cursor} by querying the URI Contacts.CONTENT_URI
     * (content://com.android.contacts/contacts), with the projection {@code CONTACT_PROJECTION}
     * returning a Cursor over the result set. We create {@code ContactListAdapter adapter} from
     * {@code cursor}, initialize {@code AutoCompleteTextView textView} by finding the view with ID
     * R.id.edit, and set its adapter to {@code adapter}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.autocomplete_4);

        ContentResolver content = getContentResolver();
        @SuppressLint("Recycle")
        Cursor cursor = content.query(Contacts.CONTENT_URI, CONTACT_PROJECTION,
                null, null, null);

        ContactListAdapter adapter = new ContactListAdapter(this, cursor);

        AutoCompleteTextView textView = findViewById(R.id.edit);
        textView.setAdapter(adapter);
    }

    /**
     * {@code CursorAdapter} subclass we use to populate the suggestions of our
     * {@code AutoCompleteTextView} with data from the contacts database.
     */
    @SuppressWarnings("WeakerAccess")
    public static class ContactListAdapter extends CursorAdapter implements Filterable {
        /**
         * ContentResolver instance for our application's package.
         */
        private ContentResolver mContent;

        /**
         * Our constructor. First we call our super's constructor, then we initialize our field
         * {@code ContentResolver mContent} with a ContentResolver instance for our {@code Context}.
         *
         * @param context The context, "this" when called from {@code onCreate} in {@code AutoComplete4}
         * @param c       The cursor from which to get the data.
         */
        public ContactListAdapter(Context context, Cursor c) {
            //noinspection deprecation
            super(context, c);
            mContent = context.getContentResolver();
        }

        /**
         * Makes a new view to hold the data pointed to by {@code Cursor cursor}. First we initialize
         * {@code LayoutInflater inflater} with a LayoutInflater for {@code Context context}, and use
         * it to inflate the XML layout resource file android.R.layout.simple_dropdown_item_1line,
         * into {@code TextView view}, using {@code ViewGroup parent} to provide a set of LayoutParams
         * values for root of the returned hierarchy. We then set the text of {@code view} to the
         * string of the column COLUMN_DISPLAY_NAME (1) of {@code Cursor cursor}, and return {@code view}
         * to the caller.
         *
         * @param context Interface to application's global information
         * @param cursor  The cursor from which to get the data. The cursor is already moved to the correct position.
         * @param parent  The parent to which the new view is attached to
         * @return the newly created view.
         */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final LayoutInflater inflater = LayoutInflater.from(context);
            final TextView view = (TextView) inflater.inflate(
                    android.R.layout.simple_dropdown_item_1line, parent, false);
            view.setText(cursor.getString(COLUMN_DISPLAY_NAME));
            return view;
        }

        /**
         * Bind an existing view to the data pointed to by {@code Cursor cursor}. We simply set the
         * text of {@code View view} to the string of the column COLUMN_DISPLAY_NAME (1) of
         * {@code Cursor cursor}
         *
         * @param view    Existing view, returned earlier by newView
         * @param context Interface to application's global information
         * @param cursor  The cursor from which to get the data. The cursor is already
         *                moved to the correct position.
         */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ((TextView) view).setText(cursor.getString(COLUMN_DISPLAY_NAME));
        }

        /**
         * Converts the cursor into a {@code String}. We simply return the string of the column
         * COLUMN_DISPLAY_NAME (1) of {@code Cursor cursor}
         *
         * @param cursor the cursor to convert to a String
         * @return a String representing the value
         */
        @Override
        public String convertToString(Cursor cursor) {
            return cursor.getString(COLUMN_DISPLAY_NAME);
        }

        /**
         * Runs a query with the specified constraint. This query is requested by the filter attached
         * to this adapter. We initialize {@code FilterQueryProvider filter} the current filter query
         * provider or null if it does not exist, and if it is not null we simply use it to run a
         * query using {@code constraint} as the constraint with which the query must be filtered.
         * Otherwise we create {@code Uri uri} using Contacts.CONTENT_FILTER_URI as the base Uri with
         * the string value of {@code constraint} appended to it as the path. Then we return the
         * {@code Cursor} that results when the {@code ContentResolver mContent} queries {@code uri}
         * using {@code CONTACT_PROJECTION} as the projection.
         *
         * @param constraint the constraint with which the query must be filtered
         * @return a Cursor representing the results of the new query
         */
        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            FilterQueryProvider filter = getFilterQueryProvider();
            if (filter != null) {
                return filter.runQuery(constraint);
            }

            Uri uri = Uri.withAppendedPath(
                    Contacts.CONTENT_FILTER_URI,
                    Uri.encode(constraint.toString()));
            return mContent.query(uri, CONTACT_PROJECTION, null, null, null);
        }
    }
}
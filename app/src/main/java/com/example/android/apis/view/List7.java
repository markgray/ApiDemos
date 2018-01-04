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

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import com.example.android.apis.R;


import android.app.ListActivity;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * A list view example where the data comes from a cursor. Original used the "selected"
 * item in list to display the details of an item at the bottom of the string by implementing
 * OnItemSelectedListener, I modified to do the same when onListItemClick is called since
 * a touch interface cannot select.
 */
@SuppressWarnings("deprecation")
public class List7 extends ListActivity implements OnItemSelectedListener {
    /**
     * {@code TextView} we use to display the phone number of the selected contact.
     */
    private TextView mPhone;

    /**
     * Projection we use to query the contact database.
     */
    private static final String[] PHONE_PROJECTION = new String[]{
            Phone._ID,
            Phone.TYPE,
            Phone.LABEL,
            Phone.NUMBER,
            Phone.DISPLAY_NAME
    };

    /**
     * Column number of the phone TYPE field in our cursor
     */
    private static final int COLUMN_PHONE_TYPE = 1;
    /**
     * Column number of the phone LABEL field in our cursor
     */
    private static final int COLUMN_PHONE_LABEL = 2;
    /**
     * Column number of the phone NUMBER field in our cursor
     */
    private static final int COLUMN_PHONE_NUMBER = 3;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.list_7. We initialize
     * our field {@code TextView mPhone} by finding the view with ID R.id.phone, and we set the
     * {@code OnItemSelectedListener} of our {@code ListView} to this. Next we retrieve {@code Cursor c}
     * by querying the uri Phone.CONTENT_URI ("content://com.android.contacts/data/phones"), for the
     * projection PHONE_PROJECTION, with the selection Phone.NUMBER concatenated to the string
     * " NOT NULL", null for the {@code selectionArgs} and null for the {@code sortOrder} arguments
     * to {@code query}. We call the method {@code startManagingCursor} so the {@code Activity} will
     * take care of managing {@code Cursor c}. We create a new instance of {@code SimpleCursorAdapter}
     * for {@code ListAdapter adapter} from {@code Cursor c} using the system layout
     * android.R.layout.simple_list_item_1 to display the Phone.DISPLAY_NAME in the TextView with
     * ID android.R.id.text1. Finally we set our list adapter to {@code adapter}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_7);

        mPhone = (TextView) findViewById(R.id.phone);
        getListView().setOnItemSelectedListener(this);

        // Get a cursor with all numbers.
        // This query will only return contacts with phone numbers
        Cursor c = getContentResolver().query(Phone.CONTENT_URI,
                PHONE_PROJECTION, Phone.NUMBER + " NOT NULL", null, null);
        startManagingCursor(c);

        ListAdapter adapter = new SimpleCursorAdapter(this,
                // Use a template that displays a text view
                android.R.layout.simple_list_item_1,
                // Give the cursor to the list adapter
                c,
                // Map the DISPLAY_NAME column to...
                new String[]{Phone.DISPLAY_NAME},
                // The "text1" view defined in the XML template
                new int[]{android.R.id.text1});
        setListAdapter(adapter);
    }

    /**
     * This method will be called when an item in the list is clicked. First we call the method
     * {@code setSelection} to set our parameter {@code position} to be the currently selected list
     * item.
     *
     * @param l        The ListView where the click happened
     * @param v        The view that was clicked within the ListView
     * @param position The position of the view in the list
     * @param id       The row id of the item that was clicked
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        setSelection(position);
        Cursor c = (Cursor) getListView().getItemAtPosition(position);
        int type = c.getInt(COLUMN_PHONE_TYPE);
        String phone = c.getString(COLUMN_PHONE_NUMBER);
        String label = null;
        //Custom type? Then get the custom label
        if (type == Phone.TYPE_CUSTOM) {
            label = c.getString(COLUMN_PHONE_LABEL);
        }
        //Get the readable string
        String numberType = (String) Phone.getTypeLabel(getResources(), type, label);
        String text = numberType + ": " + phone;
        mPhone.setText(text);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        if (position >= 0) {
            //Get current cursor
            Cursor c = (Cursor) parent.getItemAtPosition(position);
            int type = c.getInt(COLUMN_PHONE_TYPE);
            String phone = c.getString(COLUMN_PHONE_NUMBER);
            String label = null;
            //Custom type? Then get the custom label
            if (type == Phone.TYPE_CUSTOM) {
                label = c.getString(COLUMN_PHONE_LABEL);
            }
            //Get the readable string
            String numberType = (String) Phone.getTypeLabel(getResources(), type, label);
            String text = numberType + ": " + phone;
            mPhone.setText(text);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}

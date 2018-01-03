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
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * A list view example where the data comes from a cursor, and a
 * SimpleCursorListAdapter is used to map each item to a two-line
 * display.
 */
@SuppressWarnings("deprecation")
public class List3 extends ListActivity {
    /**
     * Projection of the data fields we want from the phone database.
     */
    private static final String[] PHONE_PROJECTION = new String[]{
            Phone._ID,
            Phone.TYPE,
            Phone.LABEL,
            Phone.NUMBER
    };

    /**
     * Column number in our projection for the Phone.TYPE field
     */
    private static final int COLUMN_TYPE = 1;
    /**
     * Column number in our projection for the Phone.LABEL field
     */
    private static final int COLUMN_LABEL = 2;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}. Next we retrieve {@code Cursor c} by querying the uri Phone.CONTENT_URI
     * ("content://com.android.contacts/data/phones"), for the projection PHONE_PROJECTION, and null
     * for the rest of the arguments to {@code query}. We call the method {@code startManagingCursor}
     * so the {@code Activity} will take care of managing {@code Cursor c}. We create a new instance
     * for {@code SimpleCursorAdapter adapter} from {@code Cursor c} using the system layout
     * android.R.layout.simple_list_item_2 to display the Phone.TYPE, and Phone.NUMBER fields of our
     * data in the {@code TextView} with ID android.R.id.text1, and android.R.id.text2 respectively.
     * We set the view binder of {@code adapter} to an anonymous class whose {@code setViewValue}
     * override intercepts only the COLUMN_TYPE columnIndex and does some special handling in order
     * to properly handle the Phone.TYPE_CUSTOM COLUMN_TYPE (if the COLUMN_TYPE is Phone.TYPE_CUSTOM
     * it fetches the {@code label} to use for the phone from the COLUMN_LABEL field of the cursor).
     * Finally we set our list adapter to {@code adapter}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get a cursor with all phones
        Cursor c = getContentResolver().query(Phone.CONTENT_URI, PHONE_PROJECTION,
                null, null, null);
        startManagingCursor(c);

        // Map Cursor columns to views defined in simple_list_item_2.xml
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, c,
                new String[]{
                        Phone.TYPE,
                        Phone.NUMBER
                },
                new int[]{android.R.id.text1, android.R.id.text2});
        //Used to display a readable string for the phone type
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            /**
             * Binds the Cursor column defined by the specified index to the specified view. If the
             * {@code columnIndex} is not our COLUMN_TYPE we return false so that the adapter will
             * handle the binding itself. Otherwise we initialize {@code int type} with the value
             * in column {@code COLUMN_TYPE} of the {@code cursor}, and initialize {@code String label}
             * to null. If {@code type} is Phone.TYPE_CUSTOM we set {@code label} to the value of
             * column COLUMN_LABEL of the cursor. We set {@code String text} to the CharSequence that
             * best describes {@code type}, substituting {@code label} if {@code type} is TYPE_CUSTOM.
             * We set the text of {@code view} to {@code text} and return true to the caller.
             *
             * @param view the view to bind the data to
             * @param cursor the cursor to get the data from
             * @param columnIndex the column at which the data can be found in the cursor
             * @return true if the data was bound to the view, false otherwise
             */
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                //Let the adapter handle the binding if the column is not TYPE
                if (columnIndex != COLUMN_TYPE) {
                    return false;
                }
                int type = cursor.getInt(COLUMN_TYPE);
                String label = null;
                //Custom type? Then get the custom label
                if (type == Phone.TYPE_CUSTOM) {
                    label = cursor.getString(COLUMN_LABEL);
                }
                //Get the readable string
                String text = (String) Phone.getTypeLabel(getResources(), type, label);
                //Set text
                ((TextView) view).setText(text);
                return true;
            }
        });
        setListAdapter(adapter);
    }
}
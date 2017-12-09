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

import android.app.ExpandableListActivity;
import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.widget.CursorTreeAdapter;
import android.widget.SimpleCursorTreeAdapter;

/**
 * Demonstrates expandable lists backed by Cursors-- uses Contacts data base to retrieve names and
 * when a name is clicked expands to show the phone numbers for that name.
 */
public class ExpandableList2 extends ExpandableListActivity {
    /**
     * Our handler for making asynchronous ContentResolver queries of the contacts database.
     */
    private QueryHandler mQueryHandler;
    /**
     * In our case it is an {@code MyExpandableListAdapter} which extends {@code SimpleCursorTreeAdapter}.
     * It is an adapter that exposes data from a series of Cursors to an ExpandableListView widget. The
     * top-level Cursor (that is given in the constructor) exposes the groups, while subsequent
     * Cursors returned from getChildrenCursor(Cursor) expose children within a particular group.
     */
    private CursorTreeAdapter mAdapter;

    /**
     * Projection used to query the contacts data base, consists of a list of the columns to return.
     */
    private static final String[] CONTACTS_PROJECTION = new String[]{
            Contacts._ID,
            Contacts.DISPLAY_NAME
    };
    /**
     * Column number of the unique _ID entry for a row in our query of the contacts database
     */
    private static final int GROUP_ID_COLUMN_INDEX = 0;

    /**
     * Projection used to query a contact for its phone numbers
     */
    private static final String[] PHONE_NUMBER_PROJECTION = new String[]{
            Phone._ID,
            Phone.NUMBER
    };

    /**
     * Token used when calling {@code startQuery} when querying for people, it will be passed into
     * {@code onQueryComplete} to identify the query.
     */
    private static final int TOKEN_GROUP = 0;
    /**
     * Token used when calling {@code startQuery} when querying for a contact's phone numbers, it
     * will be passed into {@code onQueryComplete} to identify the query.
     */
    private static final int TOKEN_CHILD = 1;


    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}. Next we initialize {@code CursorTreeAdapter mAdapter} with a new instance
     * of {@code MyExpandableListAdapter} configured to use android.R.layout.simple_expandable_list_item_1
     * for both the group and the child layout, Contacts.DISPLAY_NAME as the list of column names that
     * will be used to display the data for a group using android.R.id.text1 in the group layout to
     * display them, and Phone.NUMBER as the list of column names that will be used to display the data
     * for a child using android.R.id.text1 in the child layout to display them. We then set our list
     * adapter to {@code mAdapter}.
     * <p>
     * Next we initialize our field {@code QueryHandler mQueryHandler} with a new instance using
     * {@code mAdapter} as the {@code CursorTreeAdapter} that {@code onQueryComplete} will set the
     * group Cursor or child Cursor to (depending on whether it was TOKEN_GROUP or TOKEN_CHILD
     * query that completed).
     * <p>
     * Finally we call the {@code startQuery} method of {@code QueryHandler mQueryHandler} to read
     * the list of people from the contacts database, using TOKEN_GROUP as the token that will be
     * passed into onQueryComplete to identify the query, Contacts.CONTENT_URI as the Uri that will
     * be queried ("content://com.android.contacts/contacts"), CONTACTS_PROJECTION as the projection,
     * the string formed by concatenating Contacts.HAS_PHONE_NUMBER with "=1" ("has_phone_number=1",
     * as the selection, which selects all contacts which have at least one phone number.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up our adapter
        mAdapter = new MyExpandableListAdapter(
                this,
                android.R.layout.simple_expandable_list_item_1,
                android.R.layout.simple_expandable_list_item_1,
                new String[]{Contacts.DISPLAY_NAME}, // Name for group layouts
                new int[]{android.R.id.text1},
                new String[]{Phone.NUMBER}, // Number for child layouts
                new int[]{android.R.id.text1});

        setListAdapter(mAdapter);

        mQueryHandler = new QueryHandler(this, mAdapter);

        // Query for people
        mQueryHandler.startQuery(TOKEN_GROUP, null, Contacts.CONTENT_URI, CONTACTS_PROJECTION,
                Contacts.HAS_PHONE_NUMBER + "=1", null, null);
    }

    /**
     * Called so we can perform any final cleanup before our activity is destroyed. We change the
     * cursor of {@code CursorTreeAdapter mAdapter} to null and set {@code mAdapter} to null (which
     * will cause the group cursor and all of the child cursors to be closed).
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Null out the group cursor. This will cause the group cursor and all of the child cursors
        // to be closed.
        mAdapter.changeCursor(null);
        mAdapter = null;
    }

    /**
     * Custom {@code AsyncQueryHandler} we use to query the contacts database.
     */
    @SuppressWarnings("WeakerAccess")
    private static final class QueryHandler extends AsyncQueryHandler {
        /**
         * The {@code CursorTreeAdapter} whose group or child cursor is set to the {@code Cursor}
         * returned as the results of a query, initialized by an argument to our constructor.
         */
        private CursorTreeAdapter mAdapter;

        /**
         * Our constructor. First we call our super's constructor with a ContentResolver instance for
         * our application's package, then we save our parameter {@code CursorTreeAdapter adapter} in
         * our field {@code CursorTreeAdapter mAdapter}.
         *
         * @param context {@code Context} to use to get a ContentResolver instance for our application's
         *                package, this in the {@code onCreate} method of {@code ExpandableList2}
         * @param adapter {@code CursorTreeAdapter} whose cursors (group and child) we are to change.
         */
        public QueryHandler(Context context, CursorTreeAdapter adapter) {
            super(context.getContentResolver());
            this.mAdapter = adapter;
        }

        /**
         * Called when an asynchronous query is completed. We switch based on the value of our parameter
         * {@code int token}:
         * <ul>
         * <li>
         * TOKEN_GROUP - we set the group Cursor of {@code CursorTreeAdapter mAdapter} to our
         * parameter {@code Cursor cursor}, and break.
         * </li>
         * <li>
         * TOKEN_CHILD - we initialize our variable {@code int groupPosition} by casting our
         * parameter {@code Object cookie} to Integer, and then set the children Cursor for
         * group {@code groupPosition} to our parameter {@code Cursor cursor}, and break.
         * </li>
         * </ul>
         *
         * @param token  the token to identify the query, passed in from
         *               {@code startQuery}.
         * @param cookie the cookie object passed in from {@code startQuery}.
         * @param cursor The cursor holding the results from the query.
         */
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            switch (token) {
                case TOKEN_GROUP:
                    mAdapter.setGroupCursor(cursor);
                    break;

                case TOKEN_CHILD:
                    int groupPosition = (Integer) cookie;
                    mAdapter.setChildrenCursor(groupPosition, cursor);
                    break;
            }
        }
    }

    /**
     * Custom {@code SimpleCursorTreeAdapter} we use for {@code CursorTreeAdapter mAdapter}, its
     * group and children cursors are set by a query to the contacts database.
     */
    @SuppressWarnings("WeakerAccess")
    public class MyExpandableListAdapter extends SimpleCursorTreeAdapter {
        /**
         * Our constructor. We simply call our super's constructor (after shuffling our parameters a
         * bit). Note that the constructor does not take a Cursor. This is done to avoid querying the
         * database on the main thread.
         *
         * @param context      The context where the ExpandableListView associated with this
         *                     SimpleCursorTreeAdapter is running, this when called from the
         *                     {@code onCreate} method of {@code ExpandableList2}
         * @param groupLayout  The resource identifier of a layout file that defines the views for a group.
         * @param childLayout  The resource identifier of a layout file that defines the views for a child.
         * @param groupFrom    A list of column names that will be used to display the data for a group.
         * @param groupTo      The resource identifiers of the group views in the group layout that should
         *                     display the columns in the groupFrom parameter.
         * @param childrenFrom A list of column names that will be used to display the data for a child.
         * @param childrenTo   The resource identifiers of the child views from the child layout that should
         *                     display columns in the childrenFrom parameter.
         */
        public MyExpandableListAdapter(Context context, int groupLayout, int childLayout,
                                       String[] groupFrom, int[] groupTo,
                                       String[] childrenFrom, int[] childrenTo) {

            super(context, null,
                    groupLayout, groupFrom, groupTo,
                    childLayout, childrenFrom, childrenTo);
        }

        /**
         * Gets the Cursor for the children at the given group. We construct {@code Uri.Builder builder}
         * by copying the attributes from Uri Contacts.CONTENT_URI ("content://com.android.contacts/contacts").
         * We append the ID GROUP_ID_COLUMN_INDEX to {@code builder} (the column number of the unique _ID
         * entry for a row in our query of the contacts database), we then append the encoded path of
         * Contacts.Data.CONTENT_DIRECTORY (The directory twig for this sub-table "data") to {@code builder}.
         * We then build {@code builder} to create {@code Uri phoneNumbersUri}.
         * <p>
         * We then call the {@code startQuery} method of {@code QueryHandler mQueryHandler} to read
         * the phone numbers of the contact that our parameter {@code Cursor groupCursor} is pointing
         * to from the contacts database, using TOKEN_GROUP as the token that will be passed into
         * onQueryComplete to identify the query, the current position of {@code groupCursor} as the
         * "cookie" object that gets passed into {@code onQueryComplete} (it is used to select which
         * group will have its child cursor set), {@code phoneNumbersUri} as the Uri that will
         * be queried ("content://com.android.contacts/contacts/??/data", where ?? is replaced by the
         * unique _ID entry for the currently selected person), PHONE_NUMBER_PROJECTION as the projection,
         * the string formed by concatenating Phone.MIMETYPE with "=?" as the selection ("mimetype=?"),
         * and Phone.CONTENT_ITEM_TYPE as the selection arguments ("vnd.android.cursor.item/phone_v2").
         * <p>
         * We then return null to the caller, since the query will be done in the background.
         *
         * @param groupCursor The cursor pointing to the group whose children cursor
         *                    should be returned
         * @return The cursor for the children of a particular group, or null.
         */
        @Override
        protected Cursor getChildrenCursor(Cursor groupCursor) {
            // Given the group, we return a cursor for all the children within that group

            // Return a cursor that points to this contact's phone numbers
            Uri.Builder builder = Contacts.CONTENT_URI.buildUpon();
            ContentUris.appendId(builder, groupCursor.getLong(GROUP_ID_COLUMN_INDEX));
            builder.appendEncodedPath(Contacts.Data.CONTENT_DIRECTORY);
            Uri phoneNumbersUri = builder.build();

            mQueryHandler.startQuery(TOKEN_CHILD, groupCursor.getPosition(), phoneNumbersUri,
                    PHONE_NUMBER_PROJECTION, Phone.MIMETYPE + "=?",
                    new String[]{Phone.CONTENT_ITEM_TYPE}, null);

            return null;
        }
    }
}

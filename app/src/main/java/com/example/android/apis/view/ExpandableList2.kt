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

import android.content.AsyncQueryHandler
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.widget.CursorTreeAdapter
import android.widget.ExpandableListView
import android.widget.SimpleCursorTreeAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R
import com.example.android.apis.view.ExpandableList2.Companion.CONTACTS_PROJECTION

/**
 * Demonstrates expandable lists backed by Cursors-- uses Contacts data base to retrieve names and
 * when a name is clicked expands to show the phone numbers for that name.
 */
class ExpandableList2 : AppCompatActivity() {
    /**
     * Our handler for making asynchronous ContentResolver queries of the contacts database.
     */
    private var mQueryHandler: QueryHandler? = null

    /**
     * The [ExpandableListView] in our layout file with ID `R.id.list`
     */
    private lateinit var expandableList: ExpandableListView

    /**
     * The [CursorTreeAdapter] for our [ExpandableListView] In our case it is an instance of
     * [MyExpandableListAdapter] which extends [SimpleCursorTreeAdapter]. It is an adapter that
     * exposes data from a series of Cursors to an `ExpandableListView` widget. The top-level
     * [Cursor] (that is given in the constructor) exposes the groups, while subsequent Cursors
     * returned from the [MyExpandableListAdapter.getChildrenCursor] method called with the parent
     * [Cursor] expose children within a particular group.
     */
    private var mAdapter: CursorTreeAdapter? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. Next we initialize [CursorTreeAdapter] field [mAdapter] with a new instance of
     * [MyExpandableListAdapter] configured to use android.R.layout.simple_expandable_list_item_1
     * for both the group and the child layout, Contacts.DISPLAY_NAME as the list of column names
     * that will be used to display the data for a group using android.R.id.text1 in the group
     * layout to display them, and Phone.NUMBER as the list of column names that will be used to
     * display the data for a child using android.R.id.text1 in the child layout to display them.
     * We then set our list adapter to [mAdapter].
     *
     * Next we initialize our [QueryHandler] field [mQueryHandler] with a new instance using
     * [mAdapter] as the [CursorTreeAdapter] that [QueryHandler.onQueryComplete] will set the
     * group Cursor or child Cursor to (depending on whether it was TOKEN_GROUP or TOKEN_CHILD
     * query that completed).
     *
     * Finally we call the `startQuery` method of [QueryHandler] field [mQueryHandler] to read the
     * list of people from the contacts database, using TOKEN_GROUP as the token that will be passed
     * into [QueryHandler.onQueryComplete] to identify the query, Contacts.CONTENT_URI as the Uri
     * that will be queried ("content://com.android.contacts/contacts"), [CONTACTS_PROJECTION] as
     * the projection, the string formed by concatenating `Contacts.HAS_PHONE_NUMBER` with "=1"
     * ("has_phone_number=1", as the selection, which selects all contacts which have at least one
     * phone number.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.expandable_list1)
        expandableList = findViewById(R.id.list)

        // Set up our adapter
        mAdapter = MyExpandableListAdapter(
            context = this,
            groupLayout = android.R.layout.simple_expandable_list_item_1,
            childLayout = android.R.layout.simple_expandable_list_item_1,
            groupFrom = arrayOf(ContactsContract.Contacts.DISPLAY_NAME),
            groupTo = intArrayOf(android.R.id.text1),
            childrenFrom = arrayOf(Phone.NUMBER),
            childrenTo = intArrayOf(android.R.id.text1)
        )
        expandableList.setAdapter(mAdapter)
        mQueryHandler = QueryHandler(this, mAdapter)

        // Query for people
        mQueryHandler!!.startQuery(
            /* token = */ TOKEN_GROUP,
            /* cookie = */ null,
            /* uri = */ ContactsContract.Contacts.CONTENT_URI,
            /* projection = */ CONTACTS_PROJECTION,
            /* selection = */ ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1",
            /* selectionArgs = */ null,
            /* orderBy = */ null
        )
    }

    /**
     * Called so we can perform any final cleanup before our activity is destroyed. We change the
     * cursor of [CursorTreeAdapter] field [mAdapter] to null and set [mAdapter] to null (which
     * will cause the group cursor and all of the child cursors to be closed).
     */
    override fun onDestroy() {
        super.onDestroy()

        // Null out the group cursor. This will cause the group cursor and all of the child cursors
        // to be closed.
        mAdapter!!.changeCursor(/* cursor = */ null)
        mAdapter = null
    }

    /**
     * Custom [AsyncQueryHandler] we use to query the contacts database.
     * Our constructor. First we call our super's constructor with a `ContentResolver` instance for
     * our application's package, then we save our [CursorTreeAdapter] parameter  in our field
     * [CursorTreeAdapter] field [mAdapter].
     *
     * @param context [Context] to use to get a `ContentResolver` instance for our application's
     * package, this in the `onCreate` method of [ExpandableList2]
     * @property mAdapter [CursorTreeAdapter] whose cursors (group and child) we are to change,
     * returned as the results of a query, initialized by an argument to our constructor.
     */
    private class QueryHandler(
        context: Context,
        private val mAdapter: CursorTreeAdapter?
    ) : AsyncQueryHandler(/* cr = */ context.contentResolver) {

        /**
         * Called when an asynchronous query is completed. We switch based on the value of our [Int]
         * parameter [token]:
         *
         *  * TOKEN_GROUP - we set the group Cursor of [CursorTreeAdapter] field [mAdapter] to our
         *  [Cursor] parameter [cursor], and break.
         *
         *  * TOKEN_CHILD - we initialize our [Int] variable `val groupPosition` by casting our
         *  [Any] parameter [cookie] to [Int], and then set the children [Cursor] for group
         *  `groupPosition` to our [Cursor] parameter [cursor], and break.
         *
         * @param token  the token to identify the query, passed in from `startQuery`.
         * @param cookie the cookie object passed in from `startQuery`.
         * @param cursor The cursor holding the results from the query.
         */
        override fun onQueryComplete(token: Int, cookie: Any?, cursor: Cursor) {
            when (token) {
                TOKEN_GROUP -> mAdapter!!.setGroupCursor(cursor)
                TOKEN_CHILD -> {
                    val groupPosition = cookie as Int
                    mAdapter!!.setChildrenCursor(groupPosition, cursor)
                }
            }
        }

    }

    /**
     * Custom [SimpleCursorTreeAdapter] we use for [CursorTreeAdapter] field [mAdapter], its
     * group and children cursors are set by a query to the contacts database.
     * Our constructor. We simply call our super's constructor (after shuffling our parameters a
     * bit). Note that the constructor does not take a Cursor. This is done to avoid querying the
     * database on the main thread.
     *
     * @param context      The context where the ExpandableListView associated with this
     *                     SimpleCursorTreeAdapter is running, this when called from the
     *                     `onCreate` method of `ExpandableList2`
     * @param groupLayout  The resource identifier of a layout file that defines the views for a group.
     * @param childLayout  The resource identifier of a layout file that defines the views for a child.
     * @param groupFrom    A list of column names that will be used to display the data for a group.
     * @param groupTo      The resource identifiers of the group views in the group layout that should
     *                     display the columns in the groupFrom parameter.
     * @param childrenFrom A list of column names that will be used to display the data for a child.
     * @param childrenTo   The resource identifiers of the child views from the child layout that should
     *                     display columns in the childrenFrom parameter.
     */
    inner class MyExpandableListAdapter(
        context: Context?,
        groupLayout: Int,
        childLayout: Int,
        groupFrom: Array<String?>?,
        groupTo: IntArray?,
        childrenFrom: Array<String?>?,
        childrenTo: IntArray?
    ) : SimpleCursorTreeAdapter(
        /* context = */ context,
        /* cursor = */ null,
        /* groupLayout = */ groupLayout,
        /* groupFrom = */ groupFrom,
        /* groupTo = */ groupTo,
        /* childLayout = */ childLayout,
        /* childFrom = */ childrenFrom,
        /* childTo = */ childrenTo
    ) {
        /**
         * Gets the Cursor for the children at the given group. We construct [Uri.Builder]
         * `val builder` by copying the attributes from Uri Contacts.CONTENT_URI
         * ("content://com.android.contacts/contacts"). We append the ID GROUP_ID_COLUMN_INDEX to
         * `builder` (the column number of the unique _ID entry for a row in our query of the
         * contacts database), we then append the encoded path of Contacts.Data.CONTENT_DIRECTORY
         * (The directory twig for this sub-table "data") to `builder`. We then build `builder` to
         * create [Uri] `val phoneNumbersUri`.
         *
         * We then call the `startQuery` method of [QueryHandler] field [mQueryHandler] to read
         * the phone numbers of the contact that our [Cursor] parameter [groupCursor] is pointing
         * to from the contacts database, using TOKEN_GROUP as the token that will be passed into
         * `onQueryComplete` to identify the query, the current position of [groupCursor] as the
         * "cookie" object that gets passed into `onQueryComplete` (it is used to select which
         * group will have its child cursor set), `phoneNumbersUri` as the Uri that will be queried
         * ("content://com.android.contacts/contacts/??/data", where ?? is replaced by the unique
         * _ID entry for the currently selected person), PHONE_NUMBER_PROJECTION as the projection,
         * the string formed by concatenating Phone.MIMETYPE with "=?" as the selection ("mimetype=?"),
         * and Phone.CONTENT_ITEM_TYPE as the selection arguments ("vnd.android.cursor.item/phone_v2").
         *
         * We then return null to the caller, since the query will be done in the background.
         *
         * @param groupCursor The cursor pointing to the group whose children cursor
         *                    should be returned
         * @return The cursor for the children of a particular group, or null.
         */
        override fun getChildrenCursor(groupCursor: Cursor): Cursor? {
            // Given the group, we return a cursor for all the children within that group

            // Return a cursor that points to this contact's phone numbers
            val builder: Uri.Builder = ContactsContract.Contacts.CONTENT_URI.buildUpon()
            ContentUris.appendId(builder, groupCursor.getLong(GROUP_ID_COLUMN_INDEX))
            builder.appendEncodedPath(ContactsContract.Contacts.Data.CONTENT_DIRECTORY)
            val phoneNumbersUri: Uri? = builder.build()
            mQueryHandler!!.startQuery(
                /* token = */ TOKEN_CHILD,
                /* cookie = */ groupCursor.position,
                /* uri = */ phoneNumbersUri,
                /* projection = */ PHONE_NUMBER_PROJECTION,
                /* selection = */ Phone.MIMETYPE + "=?",
                /* selectionArgs = */ arrayOf(Phone.CONTENT_ITEM_TYPE),
                /* orderBy = */ null
            )
            return null
        }
    }

    companion object {
        /**
         * Projection used to query the contacts data base, consists of a list of the columns to return.
         */
        private val CONTACTS_PROJECTION: Array<String> = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME
        )

        /**
         * Column number of the unique _ID entry for a row in our query of the contacts database
         */
        private const val GROUP_ID_COLUMN_INDEX = 0

        /**
         * Projection used to query a contact for its phone numbers
         */
        private val PHONE_NUMBER_PROJECTION: Array<String> = arrayOf(
            Phone._ID,
            Phone.NUMBER
        )

        /**
         * Token used when calling `startQuery` when querying for people, it will be passed into
         * `onQueryComplete` to identify the query.
         */
        private const val TOKEN_GROUP = 0

        /**
         * Token used when calling `startQuery` when querying for a contact's phone numbers, it
         * will be passed into `onQueryComplete` to identify the query.
         */
        private const val TOKEN_CHILD = 1
    }
}

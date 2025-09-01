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

@file:Suppress("DEPRECATION")
// TODO: Replace deprecated API usage

package com.example.android.apis.app

import android.annotation.SuppressLint
import android.app.ListActivity
import android.content.Context
import android.database.CharArrayBuffer
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract.Contacts
import android.view.View
import android.view.ViewGroup
import android.widget.QuickContactBadge
import android.widget.ResourceCursorAdapter
import android.widget.TextView
import com.example.android.apis.R

/**
 * Shows how to query the contacts database using a cursor, and display the results in a
 * ListActivity hosted ListView. It uses a deprecated api which queries the database on
 * the UI thread -- it is better to use LoaderManager with a CursorLoader. See the
 * documentation for LoaderManager for an example of how to do this.
 * TODO: write LoaderManager version.
 */
class QuickContactsDemo : ListActivity() {

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
    @SuppressLint("Recycle")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val select = ("((" + Contacts.DISPLAY_NAME + " NOTNULL) AND ("
            + Contacts.HAS_PHONE_NUMBER + "=1) AND ("
            + Contacts.DISPLAY_NAME + " != '' ))")
        val c = contentResolver.query(
            Contacts.CONTENT_URI,
            CONTACTS_SUMMARY_PROJECTION,
            select,
            null,
            Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC"
        )

        @Suppress("DEPRECATION")
        startManagingCursor(c)
        val adapter = ContactListItemAdapter(this, R.layout.quick_contacts, c!!)
        listAdapter = adapter

    }

    /**
     * This is the class we use to display contact items in our ListActivity's List.
     *
     * @param context The context where the ListView associated with this adapter is running
     * @param layout resource identifier of a layout file that defines the views for this list
     * item. Unless you override them later, this will define both the item views
     * and the drop down views
     * @param c Cursor
     */
    private inner class ContactListItemAdapter(
        context: Context,
        layout: Int,
        c: Cursor
    ) : ResourceCursorAdapter(context, layout, c) {

        /**
         * Bind an existing view to the data pointed to by cursor. First we retrieve the Tag from
         * the view passed us to the variable ContactListItemCache cache. Then we copy the name from
         * the cursor (SUMMARY_NAME_COLUMN_INDEX) to the CharArrayBuffer nameBuffer in "cache". We
         * set int size to the size of the copied name data, and set the text of the TextView
         * nameView in "cache" to the characters copied to the char[] nameBuffer.data array. Finally
         * we assign the contact uri that the QuickContactBadge photoView of the "cache" should be
         * associated with based on the cursor values for SUMMARY_ID_COLUMN_INDEX (Contacts._ID)
         * and SUMMARY_LOOKUP_KEY (Contacts.LOOKUP_KEY).
         *
         * @param view    Existing view, returned earlier by newView
         * @param context Interface to application's global information
         * @param cursor  The cursor from which to get the data. The cursor is already
         * moved to the correct position.
         */
        override fun bindView(view: View, context: Context, cursor: Cursor) {
            val cache = view.tag as ContactListItemCache
            // Set the name
            cursor.copyStringToBuffer(SUMMARY_NAME_COLUMN_INDEX, cache.nameBuffer)
            val size = cache.nameBuffer.sizeCopied
            cache.nameView!!.setText(cache.nameBuffer.data, 0, size)
            val contactId = cursor.getLong(SUMMARY_ID_COLUMN_INDEX)
            val lookupKey = cursor.getString(SUMMARY_LOOKUP_KEY)
            cache.photoView!!.assignContactUri(Contacts.getLookupUri(contactId, lookupKey))
        }

        /**
         * Makes a new view to hold the data pointed to by cursor. First we call through to our
         * super's implementation of newView, saving the returned view in our variable View view.
         * Then we create a ContactListItemCache cache, locate the TextView R.id.name in "view"
         * and set the nameView of "cache" to it, and locate the QuickContactBadge R.id.badge in
         * "view" and set the photoView of "cache" to it. We then set the Tag of "view" to "cache"
         * and return "view".
         *
         * @param context Interface to application's global information
         * @param cursor The cursor from which to get the data. The cursor is already
         * moved to the correct position.
         * @param parent The parent to which the new view is attached to
         * @return the newly created view.
         */
        override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
            val view = super.newView(context, cursor, parent)
            val cache = ContactListItemCache()
            cache.nameView = view.findViewById(R.id.name)
            cache.photoView = view.findViewById(R.id.badge)
            view.tag = cache

            return view
        }
    }

    /**
     * Class used to hold contact information, set as the Tag of the View for the contact item.
     */
    internal class ContactListItemCache {
        var nameView: TextView? = null
        var photoView: QuickContactBadge? = null
        var nameBuffer = CharArrayBuffer(128)
    }

    companion object {
        /**
         * A list of which columns to return.
         */
        internal val CONTACTS_SUMMARY_PROJECTION = arrayOf(
            Contacts._ID, // 0
            Contacts.DISPLAY_NAME, // 1
            Contacts.STARRED, // 2
            Contacts.TIMES_CONTACTED, // 3
            Contacts.CONTACT_PRESENCE, // 4
            Contacts.PHOTO_ID, // 5
            Contacts.LOOKUP_KEY, // 6
            Contacts.HAS_PHONE_NUMBER
        )// 7

        /**
         * Indexes to column in cursor returned. See CONTACTS_SUMMARY_PROJECTION above
         */
        internal const val SUMMARY_ID_COLUMN_INDEX = 0
        internal const val SUMMARY_NAME_COLUMN_INDEX = 1

        @Suppress("unused")
        internal const val SUMMARY_STARRED_COLUMN_INDEX = 2

        @Suppress("unused")
        internal const val SUMMARY_TIMES_CONTACTED_COLUMN_INDEX = 3

        @Suppress("unused")
        internal const val SUMMARY_PRESENCE_STATUS_COLUMN_INDEX = 4

        @Suppress("unused")
        internal const val SUMMARY_PHOTO_ID_COLUMN_INDEX = 5
        internal const val SUMMARY_LOOKUP_KEY = 6

        @Suppress("unused")
        internal const val SUMMARY_HAS_PHONE_COLUMN_INDEX = 7
    }
}

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
@file:Suppress("DEPRECATION")

package com.example.android.apis.view

import android.app.ListActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.widget.SimpleCursorAdapter
import android.widget.TextView

/**
 * A list view example where the data comes from a cursor, and a
 * [SimpleCursorAdapter] is used to map each item to a two-line display.
 * TODO: Switch to the new CursorLoader class with LoaderManager instead
 * TODO: Use ListFragment or RecyclerView instead of ListActivity
 */
class List3 : ListActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. Next we retrieve `Cursor` variable `val c` by querying the uri Phone.CONTENT_URI
     * ("content://com.android.contacts/data/phones"), for the projection PHONE_PROJECTION, and null
     * for the rest of the arguments to `query`. We call the method [startManagingCursor] so the
     * `Activity` will take care of managing `Cursor` variable `c`. We create a new instance for
     * [SimpleCursorAdapter] variable `val adapter` from `Cursor` variable `c` using the system
     * layout android.R.layout.simple_list_item_2 to display the Phone.TYPE, and Phone.NUMBER fields
     * of our data in the `TextView` with ID android.R.id.text1, and android.R.id.text2 respectively.
     * We set the view binder of `adapter` to a lambda whose `setViewValue` override intercepts only
     * the COLUMN_TYPE columnIndex and does some special handling in order to properly handle the
     * Phone.TYPE_CUSTOM COLUMN_TYPE (if the COLUMN_TYPE is Phone.TYPE_CUSTOM it fetches the `label`
     * to use for the phone from the COLUMN_LABEL field of the cursor). Finally we set our list
     * adapter to `adapter`.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get a cursor with all phones
        val c = contentResolver.query(Phone.CONTENT_URI, PHONE_PROJECTION,
                null, null, null)
        @Suppress("DEPRECATION")
        startManagingCursor(c)

        // Map Cursor columns to views defined in simple_list_item_2.xml
        @Suppress("DEPRECATION")
        val adapter = SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, c, arrayOf(
                Phone.TYPE,
                Phone.NUMBER
        ), intArrayOf(android.R.id.text1, android.R.id.text2))
        //Used to display a readable string for the phone type
        adapter.viewBinder = SimpleCursorAdapter.ViewBinder {
            view,
            cursor,
            columnIndex ->
            /**
             * Binds the Cursor column defined by the specified index to the specified view. If the
             * [columnIndex] is not our COLUMN_TYPE we return false so that the adapter will
             * handle the binding itself. Otherwise we initialize `val type` with the value
             * in column `COLUMN_TYPE` of the [cursor], and initialize [String] variable `var label`
             * to null. If `type` is Phone.TYPE_CUSTOM we set [String] variable `var label` to the
             * value of column COLUMN_LABEL of the cursor. We set `val text` to the [CharSequence]
             * that best describes `type`, substituting `label` if `type` is TYPE_CUSTOM. We set the
             * text of [view] to `text` and return true to the caller.
             *
             * @param view the view to bind the data to
             * @param cursor the cursor to get the data from
             * @param columnIndex the column at which the data can be found in the cursor
             * @return true if the data was bound to the view, false otherwise
             */
            //Let the adapter handle the binding if the column is not TYPE
            if (columnIndex != COLUMN_TYPE) {
                return@ViewBinder false
            }
            val type = cursor.getInt(COLUMN_TYPE)
            var label: String? = null
            //Custom type? Then get the custom label
            if (type == Phone.TYPE_CUSTOM) {
                label = cursor.getString(COLUMN_LABEL)
            }
            //Get the readable string
            val text = Phone.getTypeLabel(resources, type, label) as String
            //Set text
            (view as TextView).text = text
            true
        }
        listAdapter = adapter
    }

    companion object {
        /**
         * Projection of the data fields we want from the phone database.
         */
        private val PHONE_PROJECTION = arrayOf(
                Phone._ID,
                Phone.TYPE,
                Phone.LABEL,
                Phone.NUMBER
        )

        /**
         * Column number in our projection for the Phone.TYPE field
         */
        private const val COLUMN_TYPE = 1

        /**
         * Column number in our projection for the Phone.LABEL field
         */
        private const val COLUMN_LABEL = 2
    }
}
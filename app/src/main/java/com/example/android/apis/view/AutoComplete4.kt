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
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Shows how to use the [ContentResolver] for the contacts database as the source
 * of data for an auto complete lookup of a contract.
 */
class AutoComplete4 : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.autocomplete_4.
     * We initialize [ContentResolver] `val content` with a [ContentResolver] instance for our
     * application's package, create [Cursor] `val cursor` by querying the URI Contacts.CONTENT_URI
     * (content://com.android.contacts/contacts), with the projection [CONTACT_PROJECTION]
     * returning a [Cursor] over the result set. We create [ContactListAdapter] `val adapter` from
     * `cursor`, initialize [AutoCompleteTextView] `val textView` by finding the view with ID
     * R.id.edit, and set its adapter to `adapter`.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.autocomplete_4)
        val content = contentResolver
        @SuppressLint("Recycle")
        val cursor = content.query(
                ContactsContract.Contacts.CONTENT_URI,
                CONTACT_PROJECTION,
                null,
                null,
                null
        )
        val adapter = ContactListAdapter(this, cursor)
        val textView = findViewById<AutoCompleteTextView>(R.id.edit)
        textView.setAdapter(adapter)
    }

    /**
     * [CursorAdapter] subclass we use to populate the suggestions of our
     * [AutoCompleteTextView] with data from the contacts database.
     */
    @Suppress("DEPRECATION")
    class ContactListAdapter(
            context: Context,
            c: Cursor?
    ) : CursorAdapter(context, c), Filterable {

        /**
         * [ContentResolver] instance for our application's package.
         */
        private val mContent: ContentResolver = context.contentResolver

        /**
         * Makes a new view to hold the data pointed to by [Cursor] parameter [cursor]. First we
         * initialize [LayoutInflater] `val inflater` with a [LayoutInflater] for [Context] parameter
         * [context], and use it to inflate the system XML layout resource file
         * android.R.layout.simple_dropdown_item_1line, into [TextView] `val view`, using [ViewGroup]
         * parameter [parent] to provide a set of LayoutParams values for root of the returned
         * hierarchy. We then set the text of `view` to the string of the column [COLUMN_DISPLAY_NAME]
         * (1) of [Cursor] `cursor`, and return `view` to the caller.
         *
         * @param context Interface to application's global information
         * @param cursor  The [Cursor] from which to get the data. The [Cursor] is already moved
         *                to the correct position.
         * @param parent  The parent to which the new view is attached to
         * @return the newly created view.
         */
        override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(
                    android.R.layout.simple_dropdown_item_1line,
                    parent,
                    false
            ) as TextView
            view.text = cursor.getString(COLUMN_DISPLAY_NAME)
            return view
        }

        /**
         * Bind an existing view to the data pointed to by [Cursor] parameter [cursor]. We simply
         * set the text of [View] parameter [view] to the string of the column [COLUMN_DISPLAY_NAME]
         * (1) of [cursor]
         *
         * @param view    Existing [View], returned earlier by [newView]
         * @param context Interface to application's global information
         * @param cursor  The [Cursor] from which to get the data. The [Cursor] is already
         *                moved to the correct position.
         */
        override fun bindView(view: View, context: Context, cursor: Cursor) {
            (view as TextView).text = cursor.getString(COLUMN_DISPLAY_NAME)
        }

        /**
         * Converts the [Cursor] parameter [cursor] into a [String]. We simply return the string of
         * the column [COLUMN_DISPLAY_NAME] (1) of [cursor]
         *
         * @param cursor the cursor to convert to a String
         * @return a String representing the value
         */
        override fun convertToString(cursor: Cursor): String {
            return cursor.getString(COLUMN_DISPLAY_NAME)
        }

        /**
         * Runs a query with the specified constraint. This query is requested by the filter attached
         * to this adapter. We initialize [FilterQueryProvider] `val filter` the current filter query
         * provider or null if it does not exist, and if it is not null we simply use it to run a
         * query using [CharSequence] parameter [constraint] as the constraint with which the query
         * must be filtered. Otherwise we create [Uri] `val uri` using `Contacts.CONTENT_FILTER_URI`
         * as the base Uri with the string value of [constraint] appended to it as the path. Then we
         * return the [Cursor] that results when the [ContentResolver] field [mContent] queries `uri`
         * using [CONTACT_PROJECTION] as the projection.
         *
         * @param constraint the constraint with which the query must be filtered
         * @return a [Cursor] representing the results of the new query
         */
        @SuppressLint("Recycle")
        override fun runQueryOnBackgroundThread(constraint: CharSequence): Cursor {
            val filter: FilterQueryProvider = filterQueryProvider
            @Suppress("SENSELESS_COMPARISON")
            if (filter != null) {
                return filter.runQuery(constraint)
            }
            val uri = Uri.withAppendedPath(
                    ContactsContract.Contacts.CONTENT_FILTER_URI,
                    Uri.encode(constraint.toString()))
            return mContent.query(
                    uri,
                    CONTACT_PROJECTION,
                    null,
                    null,
                    null
            )!!
        }

    }

    companion object {
        /**
         * Projection containing a list of which columns to return from the contacts database.
         */
        @JvmField
        val CONTACT_PROJECTION = arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
        )

        /**
         * Index of the column in the cursor containing Contacts.DISPLAY_NAME
         */
        const val COLUMN_DISPLAY_NAME = 1
    }
}
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
//TODO: Switch to the new androidx.loader.content.CursorLoader class with androidx.loader.app.LoaderManager
package com.example.android.apis.view

import android.os.Bundle
import android.provider.ContactsContract
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * A list view example where the data comes from a cursor. Builds a cursor using
 * getContentResolver().query(...) of the Contacts provider for a projection of
 * Contacts._ID, and Contacts.DISPLAY_NAME and sets it as the [ListAdapter] for the
 * [ListView]'s list instead of SimpleCursorAdapter
 */
class List2 : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. Next we retrieve `Cursor` variable `val c` by querying the uri Contacts.CONTENT_URI
     * ("content://com.android.contacts/contacts"), for the projection CONTACT_PROJECTION, and null
     * for the rest of the arguments to `query`. We call the method [startManagingCursor] so the
     * `Activity` will take care of managing `Cursor` variable `c`. Then we create a new instance of
     * [SimpleCursorAdapter] for our [ListAdapter] variable `val adapter` from `Cursor` variable `c`
     * using the layout android.R.layout.simple_list_item_1 to display the DISPLAY_NAME field of our
     * data in the `TextView` with ID android.R.id.text1. Finally we set our list adapter to `adapter`.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_2)
        val list = findViewById<ListView>(R.id.list)

        // Get a cursor with all people
        val c = contentResolver.query(
            /* uri = */ ContactsContract.Contacts.CONTENT_URI,
            /* projection = */ CONTACT_PROJECTION,
            /* selection = */ null,
            /* selectionArgs = */ null,
            /* sortOrder = */ null
        )
        startManagingCursor(c)
        /**
         * Use a template that displays a text view, and give the cursor to the list adapter
         */
        @Suppress("DEPRECATION")
        val adapter: ListAdapter = SimpleCursorAdapter(
            /* context = */ this,
            /* layout = */ android.R.layout.simple_list_item_1,
            /* c = */ c,
            /* from = */ arrayOf(ContactsContract.Contacts.DISPLAY_NAME),
            /* to = */ intArrayOf(android.R.id.text1)
        )
        list.adapter = adapter
    }

    companion object {
        /**
         * Projection for the data we want from the contacts data base.
         */
        private val CONTACT_PROJECTION = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME
        )
    }
}
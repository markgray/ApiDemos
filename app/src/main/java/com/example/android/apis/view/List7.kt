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
// TODO: correct comments, replace deprecated startManagingCursor and SimpleCursorAdapter
package com.example.android.apis.view

import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * A list view example where the data comes from a cursor. Original used the "selected"
 * item in list to display the details of an item at the bottom of the string by implementing
 * OnItemSelectedListener, I modified to do the same when onListItemClick is called since
 * a touch interface cannot select.
 */
class List7 : AppCompatActivity(), OnItemSelectedListener {
    /**
     * [TextView] we use to display the phone number of the selected contact.
     */
    private var mPhone: TextView? = null

    /**
     * The [ListView] we use to display our list of contacts.
     */
    private lateinit var listView: ListView

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.list_7. We initialize
     * our [TextView] field [mPhone] by finding the view with ID R.id.phone, and we set the
     * `OnItemSelectedListener` of our [ListView] to this. Next we retrieve [Cursor] variable
     * `val c` by querying the uri Phone.CONTENT_URI ("content://com.android.contacts/data/phones"),
     * for the projection PHONE_PROJECTION, with the selection Phone.NUMBER concatenated to the
     * string " NOT NULL", null for the `selectionArgs` and null for the `sortOrder` arguments
     * to `query`. We call the method `startManagingCursor` so the `Activity` will take care of
     * managing [Cursor] variable `c`. We create a new instance of [SimpleCursorAdapter] for
     * [ListAdapter] `val adapter` from `c` using the system layout android.R.layout.simple_list_item_1
     * to display the Phone.DISPLAY_NAME in the [TextView] with ID android.R.id.text1. Finally we set
     * our list adapter to `adapter`.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_7)
        mPhone = findViewById(R.id.phone)
        listView = findViewById(R.id.list)
        listView.onItemSelectedListener = this

        // Get a cursor with all numbers.
        // This query will only return contacts with phone numbers
        val c: Cursor? = contentResolver.query(
            /* uri = */ Phone.CONTENT_URI,
            /* projection = */ PHONE_PROJECTION,
            /* selection = */ Phone.NUMBER + " NOT NULL",
            /* selectionArgs = */ null,
            /* sortOrder = */ null
        )
        startManagingCursor(c)
        val adapter: ListAdapter = SimpleCursorAdapter(
            /* context = */ this,
            /* layout = */ android.R.layout.simple_list_item_1,
            /* c = */ c, // Give the cursor to the list adapter
            /* from = */ arrayOf(Phone.DISPLAY_NAME),
            /* to = */ intArrayOf(android.R.id.text1)
        )
        listView.adapter = adapter
        listView.setOnItemClickListener { parent, view, position, id ->
            onListItemClick(l = parent as ListView, v = view, position = position, id = id)
        }
    }

    /**
     * This method will be called when an item in the list is clicked. First we call the method
     * `setSelection` to set our parameter [position] to be the currently selected list item. Then
     * we set [Cursor] variable `val c` to the object at [position] in our [ListView]. We set our
     * [Int] variable `val type` to the data in the Phone.TYPE column of `c`, and [String] varible
     * `val phone` to the data in the Phone.NUMBER column. We initialize [String] variable
     * `val label` to null, and if the `type` is Phone.TYPE_CUSTOM we set `label` to the data in
     * the Phone.LABEL column of `c`. We initialize [String] variable `val numberType` to the type
     * label returned by `getTypeLabel` for `type` (defaulting to `label` if it is a Phone.TYPE_CUSTOM
     * type), and create [String] variable `val text` by concatenating `numberType` with the string
     * ": " and `phone`. We then set the text of [TextView] field [mPhone] to `text`.
     *
     * @param l        The [ListView] where the click happened
     * @param v        The [View] that was clicked within the [ListView]
     * @param position The position of the [View] in the list
     * @param id       The row id of the item that was clicked
     */
    @Suppress("UNUSED_PARAMETER")
    fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        l.setSelection(position)
        val c = listView.getItemAtPosition(position) as Cursor
        val type: Int = c.getInt(COLUMN_PHONE_TYPE)
        val phone: String? = c.getString(COLUMN_PHONE_NUMBER)
        var label: String? = null
        //Custom type? Then get the custom label
        if (type == Phone.TYPE_CUSTOM) {
            label = c.getString(COLUMN_PHONE_LABEL)
        }
        //Get the readable string
        val numberType: String = Phone.getTypeLabel(resources, type, label) as String
        val text = "$numberType: $phone"
        mPhone!!.text = text
    }

    /**
     * Callback method to be invoked when an item in this view has been selected. If our parameter
     * [position] is greater than or equal to 0, we set [Cursor] variable `val c` to the object at
     * [position] in our `AdapterView<?>` parameter [parent]. We set our [Int] variable `val type`
     * to the data in the Phone.TYPE column of `c`, and [String] variable `val phone` to the data in
     * the Phone.NUMBER column. We initialize [String] variable `val label` to null, and if the `type`
     * is Phone.TYPE_CUSTOM we set `label` to the data in the Phone.LABEL column of `c`. We initialize
     * [String] variable `val numberType` to the type label returned by `getTypeLabel` for `type`
     * (defaulting to `label` if it is a Phone.TYPE_CUSTOM type), and create [String] variable
     * `val text` by concatenating `numberType` with the string ": " and `phone`. We then set the
     * text of [TextView] field [mPhone] to `text`.
     *
     * @param parent   The AdapterView where the selection happened
     * @param v        The view within the AdapterView that was clicked
     * @param position The position of the view in the adapter
     * @param id       The row id of the item that is selected
     */
    override fun onItemSelected(parent: AdapterView<*>, v: View, position: Int, id: Long) {
        if (position >= 0) {
            //Get current cursor
            val c = parent.getItemAtPosition(position) as Cursor
            val type: Int = c.getInt(COLUMN_PHONE_TYPE)
            val phone: String? = c.getString(COLUMN_PHONE_NUMBER)
            var label: String? = null
            //Custom type? Then get the custom label
            if (type == Phone.TYPE_CUSTOM) {
                label = c.getString(COLUMN_PHONE_LABEL)
            }
            //Get the readable string
            val numberType: String = Phone.getTypeLabel(resources, type, label) as String
            val text = "$numberType: $phone"
            mPhone!!.text = text
        }
    }

    /**
     * Callback method to be invoked when the selection disappears from this view. We ignore this.
     *
     * @param parent The AdapterView that now contains no selected item.
     */
    override fun onNothingSelected(parent: AdapterView<*>?) {}

    companion object {
        /**
         * Projection we use to query the contact database.
         */
        private val PHONE_PROJECTION = arrayOf(
            Phone._ID,
            Phone.TYPE,
            Phone.LABEL,
            Phone.NUMBER,
            Phone.DISPLAY_NAME
        )

        /**
         * Column number of the phone TYPE field in our cursor
         */
        private const val COLUMN_PHONE_TYPE = 1

        /**
         * Column number of the phone LABEL field in our cursor
         */
        private const val COLUMN_PHONE_LABEL = 2

        /**
         * Column number of the phone NUMBER field in our cursor
         */
        private const val COLUMN_PHONE_NUMBER = 3
    }
}
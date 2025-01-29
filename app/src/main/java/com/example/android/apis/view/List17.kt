/*
 * Copyright (C) 2010 The Android Open Source Project
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

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * A list view where the last item the user clicked is placed in the "activated" state, causing its
 * background to highlight. Uses the built-in layout android.R.layout.simple_list_item_activated_1
 * for showing a list item with a single line of text whose background changes when activated. Uses
 * getListView().setItemChecked in onCreate to start with first item activated.
 */
class List17 : AppCompatActivity() {
    /**
     * The [ListView] in our layout file
     */
    private lateinit var list: ListView

    /**
     * Reference to the array that we use as our database.
     */
    private val mStrings = Cheeses.sCheeseStrings

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file `R.layout.list_17` and initialize
     * our [ListView] field [list] by finding the view with ID `R.id.list`. Then we set the adapter
     * of [list] to a new instance of [ArrayAdapter] constructed to display our array [mStrings]
     * using the layout android.R.layout.simple_list_item_activated_1. We enable the text filter for
     * our [ListView] (needs keyboard to use), set its choice mode to CHOICE_MODE_SINGLE, and set
     * the item at position 0 to be checked. Finally we set the [OnItemClickListener] of [list] to
     * a lambda which calls our method [onListItemClick] (which is what was called when an item in
     * the [ListView] was clicked when this was a `ListActivity`).
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_17)
        list = findViewById(R.id.list)

        // Use the built-in layout for showing a list item with a single
        // line of text whose background changes when activated.
        list.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_activated_1,
            mStrings
        )
        list.isTextFilterEnabled = true

        // Tell the list view to show one checked/activated item at a time.
        list.choiceMode = ListView.CHOICE_MODE_SINGLE

        // Start with first item activated.
        // Make the newly clicked item the currently selected one.
        list.setItemChecked(0, true)
        list.setOnItemClickListener { parent, view, position, id ->
            onListItemClick(parent as ListView, view, position, id)
        }
    }

    /**
     * This method will be called when an item in the list is selected. We call the `setItemChecked`
     * method of our [ListView] to set the item at [position] to checked state (automatically
     * un-checking the previously selected item).
     *
     * @param l        The ListView where the click happened
     * @param v        The view that was clicked within the ListView
     * @param position The position of the view in the list
     * @param id       The row id of the item that was clicked
     */
    @Suppress("UNUSED_PARAMETER")
    fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        // Make the newly clicked item the currently selected one.
        list.setItemChecked(position, true)
    }
}

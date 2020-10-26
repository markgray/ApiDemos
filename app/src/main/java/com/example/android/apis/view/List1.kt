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
import android.widget.ArrayAdapter

/**
 * A list view example where the data for the list comes from an array of strings.
 * Uses an `ArrayAdapter<String>` to fill a ListActivity's list
 * TODO: Use ListFragment or RecyclerView instead of ListActivity
 */
class List1 : ListActivity() {
    /**
     * Reference to the array of strings we want to display in our ListView.
     */
    private val mStrings = Cheeses.sCheeseStrings

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. Then we set our list adapter to an instance of [ArrayAdapter] created to display
     * `String[]` field[mStrings] using the layout android.R.layout.simple_list_item_1. Finally we
     * enable the text filter of our ListView (typing when the view has focus will filter the
     * children to match the users input).
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use an existing ListAdapter that will map an array
        // of strings to TextViews
        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mStrings)
        listView.isTextFilterEnabled = true
    }
}
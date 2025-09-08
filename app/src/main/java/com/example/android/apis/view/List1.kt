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

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * A list view example where the data for the list comes from an array of strings. It uses an
 * `ArrayAdapter<String>` to fill a [ListView]'s list. It sets the isTextFilterEnabled` property
 * of the [ListView] to `true` so that the list is filtered when the user types, but as there is
 * no way for the keyboard to appear on a phone this nifty feature does not happen.
 */
class List1 : AppCompatActivity() {
    /**
     * Reference to the array of strings we want to display in our ListView.
     */
    private val mStrings = Cheeses.sCheeseStrings

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file `R.layout.list_1`, and set the
     * adapter of `list` to an instance of [ArrayAdapter] created to display our `String[]` field
     * [mStrings] using the layout android.R.layout.simple_list_item_1. Finally we enable the text
     * filter of our ListView (typing when the view has focus will filter the children to match the
     * users input).
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_1)
        val list = findViewById<ListView>(R.id.list)

        // Use an existing ListAdapter that will map an array
        // of strings to TextViews
        list.adapter = ArrayAdapter(
            /* context = */ this,
            /* resource = */ android.R.layout.simple_list_item_1,
            /* objects = */ mStrings
        )
        list.isTextFilterEnabled = true
    }
}

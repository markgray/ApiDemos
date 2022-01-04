/*
 * Copyright (C) 2008 The Android Open Source Project
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
 * This example shows how to use choice mode on a list. This list is
 * in CHOICE_MODE_MULTIPLE mode, which means the items behave like
 * checkboxes.
 */
class List11 : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file [R.layout.list_11] and initialize
     * our [ListView] variable `val list` by finding the view with ID [R.id.list]. Next We set the
     * adapter of `list` to a new instance of [ArrayAdapter] constructed to display our [String] array
     * field [GENRES] using the system layout file android.R.layout.simple_list_item_single_choice as
     * the layout file for each item in the list. We disable focus for the items of `list`, and set
     * its choice mode to CHOICE_MODE_MULTIPLE.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_11)
        val list: ListView = findViewById(R.id.list)
        list.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_multiple_choice,
            GENRES
        )
        list.itemsCanFocus = false
        list.choiceMode = ListView.CHOICE_MODE_MULTIPLE
    }

    companion object {
        private val GENRES = arrayOf(
            "Action", "Adventure", "Animation", "Children", "Comedy", "Documentary", "Drama",
            "Foreign", "History", "Independent", "Romance", "Sci-Fi", "Television", "Thriller"
        )
    }
}
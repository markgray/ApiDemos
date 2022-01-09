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

import android.os.Bundle
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * This activity demonstrates splitting touch events across multiple views within a view group. Here
 * we have two [ListView]'s within a `LinearLayout` that has the attribute android:splitMotionEvents
 * set to "true". Try scrolling both lists simultaneously using multiple fingers.
 */
class SplitTouchView : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.split_touch_view. We
     * initialize our [ListView] variable `val list1` by finding the view with the resources id
     * R.id.list1 and [ListView] variable `val list2` by finding the view with the resources id
     * R.id.list2. We initialize our [ListAdapter] variable `val adapter` with an [ArrayAdapter]
     * constructed to display the [String] array `[Cheeses.sCheeseStrings] using the layout file
     * android.R.layout.simple_list_item_1 and set it as the adapter for both `list1` and `list2`.
     * Finally we set the `OnItemClickListener` of both `list1` and `list2` to our [OnItemClickListener]
     * field [itemClickListener] whose `onItemClick` override displays a humorous Pythonesque toast
     * formed from the cheese selected and a string from the string-array with the resource id
     * R.array.cheese_responses.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.split_touch_view)
        val list1 = findViewById<ListView>(R.id.list1)
        val list2 = findViewById<ListView>(R.id.list2)
        val adapter: ListAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            Cheeses.sCheeseStrings
        )
        list1.adapter = adapter
        list2.adapter = adapter
        list1.onItemClickListener = itemClickListener
        list2.onItemClickListener = itemClickListener
    }

    /**
     * Index of the next string to use from the string-array with the resource id
     * R.array.cheese_responses.
     */
    private var responseIndex = 0

    /**
     * [OnItemClickListener] for both of our [ListView] views, its `onItemClick` override displays
     * a humorous Pythonesque toast formed from the cheese selected and a string from the
     * string-array with the resource id R.array.cheese_responses.
     */
    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    private val itemClickListener = OnItemClickListener { parent, // The AdapterView where the click happened.
                                                          view,           // The view within the AdapterView that was clicked
                                                          position,         // The position of the view in the adapter.
                                                          id              // The row id of the item that was clicked.
        ->

        /**
         * Callback method to be invoked when an item in this `AdapterView` has been clicked. First
         * we initialize our `String` array variable `val responses` by fetching the string-array
         * with id R.array.cheese_responses. We initialize `String` variable `val response` with the
         * string in `responses` at index `responseIndex` modulo the length of `responses` (post
         * incrementing `responseIndex` while we are at it). We then use the string with resource
         * id R.string.split_touch_view_cheese_toast ("`Do you have any %1$s? %2$s`") to format the
         * cheese name found at index `position` in `sCheeseStrings` and `response` to initialize
         * our `String` variable `val message`. We create `Toast` variable `val toast` from `message`
         * and then show it.
         */
        val responses = resources.getStringArray(R.array.cheese_responses)
        val response = responses[responseIndex++ % responses.size]
        val message = resources.getString(R.string.split_touch_view_cheese_toast,
            Cheeses.sCheeseStrings[position], response)
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG)
        toast.show()
    }
}
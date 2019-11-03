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

package com.example.android.apis.app

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.ListFragment

import com.example.android.apis.Shakespeare

/**
 * Demonstration of using [ListFragment] to show a list of items from a canned array. Uses a
 * [ListFragment] as the sole content of the activities window, using `setListAdapter` to configure
 * the list to display an array by constructing an `ArrayAdapter<String>` using that array
 * ([Shakespeare.TITLES]) as its data. It overrides `onListItemClick` simply to log the id of the
 * item clicked.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class FragmentListArray : FragmentActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. Then we use the support `FragmentManager` to see if there is not already a
     * `Fragment` using the id we use (android.R.id.content), and if there is, we do nothing. If
     * there is not (`findFragmentById` returns null) it is the first time we are being created so
     * we need to create a new instance of [ArrayListFragment] to initialize our variable `val list`
     * and then use the `FragmentManager`] used for interacting with fragments associated with this
     * activity to begin a new `FragmentTransaction` which we use to add `list` to the Activity state
     * as the contents of the container with ID android.R.id.content (the root element of our content
     * view). We then commit the `FragmentTransaction`.
     *
     * @param savedInstanceState we do not override onSaveInstanceState so do not use this
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create the list fragment and add it as our sole content.
        if (supportFragmentManager.findFragmentById(android.R.id.content) == null) {
            val list = ArrayListFragment()
            supportFragmentManager.beginTransaction().add(android.R.id.content, list).commit()
        }
    }

    /**
     * This [ListFragment] uses the `String[]` array [Shakespeare.TITLES] as the contents of its
     * List, and Log's the ID of any item that is clicked.
     */
    class ArrayListFragment : ListFragment() {

        /**
         * Called when the fragment's activity has been created and this fragment's view hierarchy
         * instantiated. First we call through to our super's implementation of `onActivityCreated`.
         * Then we set the cursor for our list view to an instance of `ArrayAdapter<String>`
         * which uses the system layout file android.R.layout.simple_list_item_1 (a `TextView`
         * with the id "@android:id/text1" as the per item layout) and [Shakespeare.TITLES] as the
         * Object's with which to populate the List.
         *
         * @param savedInstanceState we do not override onSaveInstanceState so do not use
         */
        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)

            listAdapter = ArrayAdapter(
                    activity!!,
                    android.R.layout.simple_list_item_1,
                    Shakespeare.TITLES
            )
        }

        /**
         * This method will be called when an item in the list is selected. We simply write the id
         * that was clicked to the Log.
         *
         * @param l The [ListView] where the click happened
         * @param v The view that was clicked within the [ListView]
         * @param position The position of the view in the list
         * @param id The row id of the item that was clicked
         */
        override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
            Log.i("FragmentList", "Item clicked: $id")
        }
    }
}

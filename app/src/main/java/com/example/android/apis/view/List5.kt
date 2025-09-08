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

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * A list view example with separators. Separators are implemented by overriding the methods
 * areAllItemsEnabled, and isEnabled
 */
class List5 : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. Then we set our list adapter to a new instance of [MyListAdapter].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_5)
        val list = findViewById<ListView>(R.id.list)
        list.adapter = MyListAdapter(this)
    }

    /**
     * Our constructor, we simply extend [BaseAdapter]
     *
     * @param mContext [Context] to use to inflate views.
     */
    private inner class MyListAdapter(
        private val mContext: Context
    ) : BaseAdapter() {

        /**
         * How many items are in the data set represented by this Adapter. We return the length of
         * our [String] array field [mStrings].
         *
         * @return Count of items.
         */
        override fun getCount(): Int {
            return mStrings.size
        }

        /**
         * Indicates whether all the items in this adapter are enabled. If true, it means all items
         * are selectable and clickable (there is no separator.) We return false in order to have
         * separators.
         *
         * @return True if all items are enabled, false otherwise.
         */
        override fun areAllItemsEnabled(): Boolean {
            return false
        }

        /**
         * Returns true if the item at the specified position is not a separator. (A separator is a
         * non-selectable, non-clickable item). Our separators are all "-" characters so we return
         * false if the string at [position] in [mStrings] starts with "-", and true if it does
         * not.
         *
         * @param position Index of the item
         * @return True if the item is not a separator
         */
        override fun isEnabled(position: Int): Boolean {
            return !mStrings[position].startsWith("-")
        }

        /**
         * Get the data item associated with the specified position in the data set. Since the data
         * comes from an array, just returning the index is sufficient to get at the data. If we
         * were using a more complex data structure, we would return whatever object represents one
         * row in the list.
         *
         * @param position Position of the item whose data we want within the adapter's data set.
         * @return The data at the specified position.
         */
        override fun getItem(position: Int): Any {
            return position
        }

        /**
         * Get the row id associated with the specified position in the list. We use the array index
         * as a unique id, so we just return our parameter [position] to the caller.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        /**
         * Get a View that displays the data at the specified position in the data set. If our [View]
         * parameter [convertView] is null we create a new instance of [TextView] for `val tv` by
         * inflating android.R.layout.simple_expandable_list_item_1, using [parent] to supply layout
         * parameters. If [convertView] is not null, we cast it to a [TextView] to set `tv`. In
         * either case we set the text of `tv` to the string at index [position] in [mStrings] and
         * return `tv` to our caller.
         *
         * @param position    The position of the item within the adapter's data set whose view we want.
         * @param convertView The old [View] to reuse, if possible.
         * @param parent      The parent that this [View] will eventually be attached to
         * @return A [View] corresponding to the data at the specified position.
         */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val tv: TextView = if (convertView == null) {
                LayoutInflater.from(mContext).inflate(
                    /* resource = */ android.R.layout.simple_expandable_list_item_1,
                    /* root = */ parent,
                    /* attachToRoot = */ false
                ) as TextView
            } else {
                convertView as TextView
            }
            tv.text = mStrings[position]
            return tv
        }

    }

    /**
     * Array of cheeses that we use for our `ListView`.
     */
    private val mStrings = arrayOf(
        "----------",
        "----------",
        "Abbaye de Belloc",
        "Abbaye du Mont des Cats",
        "Abertam",
        "----------",
        "Abondance",
        "----------",
        "Ackawi",
        "Acorn",
        "Adelost",
        "Affidelice au Chablis",
        "Afuega'l Pitu",
        "Airag",
        "----------",
        "Airedale",
        "Aisy Cendre",
        "----------",
        "Allgauer Emmentaler",
        "Alverca",
        "Ambert",
        "American Cheese",
        "Ami du Chambertin",
        "----------",
        "----------",
        "Anejo Enchilado",
        "Anneau du Vic-Bilh",
        "Anthoriro",
        "----------",
        "----------"
    )
}
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

package com.example.android.apis.view;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * A list view example with separators. Separators are implemented by overriding the methods
 * areAllItemsEnabled, and isEnabled
 */
public class List5 extends ListActivity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}. Then we set our list adapter to a new instance of {@code MyListAdapter}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setListAdapter(new MyListAdapter(this));
    }

    @SuppressWarnings("WeakerAccess")
    private class MyListAdapter extends BaseAdapter {
        /**
         * {@code Context} to use to access resources.
         */
        private Context mContext;

        /**
         * Our constructor, we simply save our parameter {@code context} in our field
         * {@code Context mContext}.
         *
         * @param context {@code Context} to use to access resources.
         */
        public MyListAdapter(Context context) {
            mContext = context;
        }

        /**
         * How many items are in the data set represented by this Adapter. We return the length of
         * our array {@code String[] mStrings}.
         *
         * @return Count of items.
         */
        @Override
        public int getCount() {
            return mStrings.length;
        }

        /**
         * Indicates whether all the items in this adapter are enabled. If true, it means all items
         * are selectable and clickable (there is no separator.) We return false in order to have
         * separators.
         *
         * @return True if all items are enabled, false otherwise.
         */
        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        /**
         * Returns true if the item at the specified position is not a separator. (A separator is a
         * non-selectable, non-clickable item). Our separators are all "-" characters so we return
         * false if the string at {@code mStrings[position]} starts with "-", and true if it does
         * not.
         *
         * @param position Index of the item
         * @return True if the item is not a separator
         */
        @Override
        public boolean isEnabled(int position) {
            return !mStrings[position].startsWith("-");
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
        @Override
        public Object getItem(int position) {
            return position;
        }

        /**
         * Get the row id associated with the specified position in the list. We use the array index
         * as a unique id, so we just return our parameter {@code position} to the caller.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * Get a View that displays the data at the specified position in the data set. First we
         * declare {@code TextView tv}. Then if our parameter {@code convertView} is null we create
         * a new instance for {@code TextView tv} by inflating android.R.layout.simple_expandable_list_item_1,
         * using {@code parent} to supply layout parameters. If {@code convertView} is not null, we
         * cast it to a {@code TextView} to set {@code tv}. In either case we set the text of {@code tv}
         * to the string at {@code mStrings[position]} and return {@code tv} to our caller.
         *
         * @param position    The position of the item within the adapter's data set whose view we want.
         * @param convertView The old view to reuse, if possible.
         * @param parent      The parent that this view will eventually be attached to
         * @return A View corresponding to the data at the specified position.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv;
            if (convertView == null) {
                tv = (TextView) LayoutInflater.from(mContext).inflate(
                        android.R.layout.simple_expandable_list_item_1, parent, false);
            } else {
                tv = (TextView) convertView;
            }
            tv.setText(mStrings[position]);
            return tv;
        }
    }

    /**
     * Array of cheeses that we use for our {@code ListView}.
     */
    private String[] mStrings = {
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
    };

}

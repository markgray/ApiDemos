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

package com.example.android.apis.app;

import com.example.android.apis.Shakespeare;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ListFragment;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Demonstration of using ListFragment to show a list of items from a canned array.
 * <p>
 * Uses a ListFragment as the sole content of the activities window, using setListAdapter to configure
 * the list to display an array by constructing an ArrayAdapter<String> using that array (Shakespeare.TITLES)
 * as its data:
 * <p>
 * ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, Shakespeare.TITLES));
 * <p>
 * It overrides onListItemClick simply to log the id of the item clicked.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FragmentListArray extends Activity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate. Then we use the FragmentManager to see if there is not already a Fragment using the
     * id we use (android.R.id.content), and if there is, we do nothing. If there is not (findFragmentById
     * returns null) it is the first time we are being created so we need to create a new instance
     * of <b>ArrayListFragment list</b> and then use the FragmentManager used for interacting with
     * fragments associated with this activity to begin a new <b>FragmentTransaction</b> which we
     * use to add <b>list</b> to the Activity state. We then commit the <b>FragmentTransaction</b>.
     *
     * @param savedInstanceState we do not override onSaveInstanceState so do not use this
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the list fragment and add it as our sole content.
        if (getFragmentManager().findFragmentById(android.R.id.content) == null) {
            ArrayListFragment list = new ArrayListFragment();
            getFragmentManager().beginTransaction().add(android.R.id.content, list).commit();
        }
    }

    /**
     * This ListFragment uses <b>String[] Shakespeare.TITLES</b> as the contents of its List, and
     * Log's the <b>long id</b> of any item that is clicked.
     */
    public static class ArrayListFragment extends ListFragment {

        /**
         * Called when the fragment's activity has been created and this fragment's view hierarchy
         * instantiated. First we call through to our super's implementation of onActivityCreated.
         * Then we set the cursor for our list view to an instance of <b>ArrayAdapter<String></b>
         * which uses the system layout file android.R.layout.simple_list_item_1 (a <b>TextView</b>
         * with the id "@android:id/text1" as the per item layout and <b>String[] Shakespeare.TITLES</b>
         * as the Object's with which to populate the List.
         *
         * @param savedInstanceState we do not override onSaveInstanceState so do not use
         */
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setListAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, Shakespeare.TITLES));
        }

        /**
         * This method will be called when an item in the list is selected. We simply write the id
         * that was clicked to the Log.
         *
         * @param l The ListView where the click happened
         * @param v The view that was clicked within the ListView
         * @param position The position of the view in the list
         * @param id The row id of the item that was clicked
         */
        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            Log.i("FragmentList", "Item clicked: " + id);
        }
    }
}

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
 * Uses a ListFragment as the sole content of the activities window, using setListAdapter to set the
 * list to display an array created using:
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
     * id we use (android.R.id.content), and if there is we do nothing. If there is not (findFragmentById
     * returns null) it is the first time we are
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

    public static class ArrayListFragment extends ListFragment {

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setListAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, Shakespeare.TITLES));
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            Log.i("FragmentList", "Item clicked: " + id);
        }
    }
}

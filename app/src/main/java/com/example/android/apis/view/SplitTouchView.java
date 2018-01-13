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

package com.example.android.apis.view;

import com.example.android.apis.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


/**
 * This activity demonstrates splitting touch events across multiple views within a
 * view group.  Here we have two ListViews within a LinearLayout that has the attribute
 * android:splitMotionEvents set to "true". Try scrolling both lists simultaneously
 * using multiple fingers.
 */
public class SplitTouchView extends Activity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.split_touch_view.
     * We initialize our variable {@code ListView list1} by finding the view with the resources id
     * R.id.list1 and {@code ListView list2} by finding the view with the resources id R.id.list2.
     * We initialize our variable {@code ListAdapter adapter} with an {@code ArrayAdapter} constructed
     * to display the array {@code String[] sCheeseStrings} using the layout file
     * android.R.layout.simple_list_item_1 and set it as the adapter for both {@code list1} and
     * {@code list2}. Finally we set the {@code OnItemClickListener} of both {@code list1} and
     * {@code list2} to our field {@code OnItemClickListener itemClickListener} whose {@code onItemClick}
     * override displays a humorous Pythonesque toast formed from the cheese selected and a string
     * from the string-array with the resource id R.array.cheese_responses.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.split_touch_view);

        ListView list1 = (ListView) findViewById(R.id.list1);
        ListView list2 = (ListView) findViewById(R.id.list2);
        ListAdapter adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, Cheeses.sCheeseStrings);
        list1.setAdapter(adapter);
        list2.setAdapter(adapter);

        list1.setOnItemClickListener(itemClickListener);
        list2.setOnItemClickListener(itemClickListener);
    }

    /**
     * Index of the next string to use from the string-array with the resource id R.array.cheese_responses.
     */
    private int responseIndex = 0;

    /**
     * {@code OnItemClickListener} for both of our {@code ListView} views, its {@code onItemClick}
     * override displays a humorous Pythonesque toast formed from the cheese selected and a string
     * from the string-array with the resource id R.array.cheese_responses.
     */
    private final OnItemClickListener itemClickListener = new OnItemClickListener() {
        /**
         * Callback method to be invoked when an item in this AdapterView has been clicked. First we
         * initialize our variable {@code String[] responses} by fetching the string-array with id
         * R.array.cheese_responses. We initialize {@code String response} with the string in
         * {@code String[] responses} at index {@code responseIndex} modulo the length of
         * {@code responses} (post incrementing {@code responseIndex} while we are at it). We then use
         * the string with resource id R.string.split_touch_view_cheese_toast ("{@code Do you have any %1$s? %2$s}")
         * to format the cheese name found at index {@code position} in {@code String[] sCheeseStrings}
         * and {@code response} to initialize our variable {@code String message}. We create
         * {@code Toast toast} from {@code message} and then show it.
         *
         * @param parent The AdapterView where the click happened.
         * @param view The view within the AdapterView that was clicked (this
         *            will be a view provided by the adapter)
         * @param position The position of the view in the adapter.
         * @param id The row id of the item that was clicked.
         */
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String[] responses = getResources().getStringArray(R.array.cheese_responses);
            String response = responses[responseIndex++ % responses.length];

            String message = getResources().getString(R.string.split_touch_view_cheese_toast,
                    Cheeses.sCheeseStrings[position], response);

            Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
            toast.show();
        }
    };
}

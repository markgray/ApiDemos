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

import com.example.android.apis.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.ArrayAdapter;

/**
 * Demonstrates how the layout_weight attribute can shrink an element too big
 * to fit on screen. The layout fills the screen, with the children stacked from
 * the top. The ListView receives all the extra space due to its attribute
 * android:layout_weight="1.0", but because it is still larger than the screen
 * it is forced to enter scroll mode.
 */
public class LinearLayout9 extends Activity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.linear_layout_9.
     * Then we initialize our varaible {@code ListView list} by finding the view with the ID
     * R.id.list, and set its adapter to a new instance of {@code ArrayAdapter} constructed to
     * display the array {@code String[] COUNTRIES} in the activity {@code AutoComplete1} using
     * the system layout android.R.layout.simple_list_item_1.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.linear_layout_9);
        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                AutoComplete1.COUNTRIES));
    }

}

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
 * Demonstrates how the layout_weight attribute can shrink an element too big
 * to fit on screen. The layout fills the screen, with the children stacked from
 * the top. The ListView receives all the extra space due to its attribute
 * android:layout_weight="1.0", but because it is still larger than the screen
 * it is forced to enter scroll mode.
 */
class LinearLayout9 : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.linear_layout_9.
     * Then we initialize our [ListView] varaible `val list` by finding the view with the ID
     * R.id.list, and set its adapter to a new instance of [ArrayAdapter] constructed to display
     * the [String] array [AutoComplete1.COUNTRIES] from the activity [AutoComplete1] using the
     * system layout android.R.layout.simple_list_item_1.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.linear_layout_9)
        val list = findViewById<ListView>(R.id.list)
        list.adapter = ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                AutoComplete1.COUNTRIES)
    }
}
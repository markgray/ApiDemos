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

@file:Suppress("DEPRECATION")

package com.example.android.apis.view

import android.annotation.SuppressLint
import android.app.TabActivity
import android.os.Bundle
import android.view.View
import android.widget.TabHost.TabContentFactory
import android.widget.TextView
import com.example.android.apis.R

/**
 * Demonstrates the Tab scrolling when too many tabs are displayed to fit
 * in the screen. In the layout a `HorizontalScrollView` contains the `TabWidget`
 * and 30 tabs are added to it using TabHost.addTab(TabHost.TabSpec tabSpec)
 * the callback [createTabContent] creates the content as each tab is selected.
 */
class Tabs5 : TabActivity(), TabContentFactory {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.tabs_scroll. Next we
     * initialize our `TabHost` variable `val tabHost` by retrieving the `TabHost` our activity is
     * using to host its tabs. We loop over `i` from 1 to 30 creating [String] variable `val name`
     * by concatenating the string "Tab " with the string value of `i`, then using the `tabHost`
     * method `addTab` to add a `TabSpec` created using `name` as the tag and the indicator label,
     * and setting the `TabContentFactory` to "this".
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    @Deprecated("Deprecated in Java")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tabs_scroll)
        val tabHost = tabHost
        for (i in 1..30) {
            val name = "Tab $i"
            tabHost.addTab(tabHost.newTabSpec(name)
                    .setIndicator(name)
                    .setContent(this))
        }
    }

    /**
     * Callback to make the tab contents. We initialize our [TextView] variable `val tv` with a new
     * instance, set its text to a string formed by concatenating the string "Content for tab with tag "
     * with our [String] parameter [tag], and return `tv` to the caller.
     *
     * @param tag Which tab was selected.
     * @return The view to display the contents of the selected tab.
     */
    @Deprecated("Deprecated in Java")
    @SuppressLint("SetTextI18n")
    override fun createTabContent(tag: String): View {
        val tv = TextView(this)
        tv.text = "Content for tab with tag $tag"
        return tv
    }
}
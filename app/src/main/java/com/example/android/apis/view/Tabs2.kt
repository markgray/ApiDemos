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
 * Example of using a tab content factory for the content via
 * TabHost.TabSpec#setContent(android.widget.TabHost.TabContentFactory)
 * It also demonstrates using an icon on one of the tabs via
 * TabHost.TabSpec#setIndicator(CharSequence, android.graphics.drawable.Drawable)
 * but this does not work using the default Theme as of Ice Cream Sandwich.
 */
open class Tabs2 : TabActivity(), TabContentFactory {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. Then we initialize our `TabHost` variable `val tabHost` by retrieving the
     * `TabHost` our activity is using to host its tabs. We add three tabs to `tabHost` using its
     * `addTab` method to add tab specs created with the tags "tab1", "tab2", and "tab3", using the
     * same string to set the indicator label, and setting the content of the tabs to use "this" as
     * the `TabHost.TabContentFactory` to use to create the content of all tabs. Note that the
     * "tab1" call to the method `setIndicator` also specifies a drawable to use but this stopped
     * working for the default Theme as of Ice Cream Sandwich.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tabHost = tabHost
        tabHost.addTab(tabHost.newTabSpec("tab1")
                .setIndicator("tab1", resources.getDrawable(R.drawable.star_big_on))
                .setContent(this))
        tabHost.addTab(tabHost.newTabSpec("tab2")
                .setIndicator("tab2")
                .setContent(this))
        tabHost.addTab(tabHost.newTabSpec("tab3")
                .setIndicator("tab3")
                .setContent(this))
    }

    /**
     * Callback to make the tab contents. We initialize our [TextView] variable `val tv` with a new
     * instance, set its text to a string formed by concatenating the string "Content for tab with
     * tag "  with our [String] parameter [tag], then return `tv` to the caller.
     *
     * @param tag Which tab was selected.
     * @return The view to display the contents of the selected tab.
     */
    @SuppressLint("SetTextI18n")
    override fun createTabContent(tag: String): View {
        val tv = TextView(this)
        tv.text = "Content for tab with tag $tag"
        return tv
    }
}
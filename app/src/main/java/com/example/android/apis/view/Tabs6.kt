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
import androidx.core.content.res.ResourcesCompat
import com.example.android.apis.R

/**
 * Uses a right gravity for the TabWidget. Does not seem to have any effect even
 * after changing AndroidManifest to android:theme="@android:style/Theme"
 */
class Tabs6 : TabActivity(), TabContentFactory {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.tabs_right_gravity. Next
     * we initialize our `TabHost` variable `val tabHost` by retrieving the TabHost our activity is
     * using to host its tabs. We add three tabs to `tabHost` using its `addTab` method to add tab
     * specs created with the tags "tab1", "tab2", and "tab3", using the same string to set the
     * indicator label, and setting the content of the tabs to use "this" as the
     * TabHost.TabContentFactory to use to create the content of all tabs.
     *
     * Note that the "tab1" call to the method `setIndicator` also specifies a drawable to
     * use but this stopped working for the default Theme as of Ice Cream Sandwich, changing the
     * theme used in the manifest file to android:theme="@android:style/Theme" fixed this.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tabs_right_gravity)
        val tabHost = tabHost
        tabHost.addTab(tabHost.newTabSpec("tab1")
            .setIndicator(
                "tab1",
                ResourcesCompat.getDrawable(resources, R.drawable.star_big_on, null)
            )
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
     * instance, set its text to a string formed by concatenating the string "Content for tab with tag "
     * with our [String] parameter [tag], then return `tv` to the caller.
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
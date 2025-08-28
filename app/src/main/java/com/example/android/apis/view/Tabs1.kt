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

import android.app.TabActivity
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.widget.TabHost
import com.example.android.apis.R

/**
 * An example of tabs that uses labels (`TabSpec.setIndicator`)
 * for its indicators and views by id from a layout file (`TabSpec.setContent`).
 * TODO: replace deprecated TabActivity with modern navigation idiom
 */
class Tabs1 : TabActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. Then we initialize our `TabHost` variable `val tabHost` by retrieving the
     * `TabHost` our activity is using to host its tabs. We obtain the [LayoutInflater] for the
     * context "this", and use it to inflate our layout file R.layout.tabs1 using the tab content
     * view of `tabHost` as its root, and attaching our inflated view to this root. We add three
     * tabs to `tabHost` using its `addTab` method to add tab specs created with the tags "tab1",
     * "tab2", and "tab3", using the same string to set the indicator label, and setting the
     * content of the tabs to use the views with resource id's R.id.view1, R.id.view2, and
     * R.id.view3 respectively.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    @Deprecated("Deprecated in Java")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tabHost: TabHost = tabHost
        tabHost.setPadding(
            dpToPixel(8, this),
            dpToPixel(60, this),
            dpToPixel(8, this),
            dpToPixel(240, this)
        )
        LayoutInflater.from(this).inflate(
            R.layout.tabs1,
            tabHost.tabContentView,
            true
        )
        tabHost.addTab(
            tabHost.newTabSpec("tab1")
                .setIndicator("tab1")
                .setContent(R.id.view1)
        )
        tabHost.addTab(
            tabHost.newTabSpec("tab2")
                .setIndicator("tab2")
                .setContent(R.id.view2)
        )
        tabHost.addTab(
            tabHost.newTabSpec("tab3")
                .setIndicator("tab3")
                .setContent(R.id.view3)
        )
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density. First we
     * fetch a [Resources] instance for `val resources`, then we fetch the current display
     * metrics that are in effect for this resource object to [DisplayMetrics] `val metrics`.
     * Finally we return our [dp] parameter multiplied by the the screen density expressed as
     * dots-per-inch, divided by the reference density used throughout the system.
     *
     * @param dp      A value in dp (density independent pixels) unit which we need to convert
     *                into pixels
     * @param context [Context] to get resources and device specific display metrics
     * @return An [Int] value to represent px equivalent to dp depending on device density
     */
    fun dpToPixel(dp: Int, context: Context): Int {
        val resources: Resources = context.resources
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
    }
}
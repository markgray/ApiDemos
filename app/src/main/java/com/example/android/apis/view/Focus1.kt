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
import android.webkit.WebView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Demonstrates the use of non-focusable views: caused by android:focusable="false"
 * in layout file layout/focus_1.xml
 */
class Focus1 : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.focus_1. We initialize
     * our [WebView] variable `val webView` by finding the view with ID R.id.rssWebView, then load
     * an html string into it. We initialize our [ListView] variable `val listView` by finding the
     * view with ID R.id.rssListView, and set its adapter to a new instance of [ArrayAdapter]
     * created using "this" as its `Context`, android.R.layout.simple_list_item_1 as the resource
     * ID for the layout file containing a `TextView` to use when instantiating views, and the
     * string array `String[]{"Ars Technica", "Slashdot", "GameKult"}` as the objects to represent
     * in the [ListView].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.focus_1)
        val webView = findViewById<WebView>(R.id.rssWebView)
        webView.loadData(
                "<html><body>Can I focus?<br /><a href=\"#\">No I cannot!</a>.</body></html>",
                "text/html",
                null
        )
        val listView = findViewById<ListView>(R.id.rssListView)
        listView.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                arrayOf("Ars Technica", "Slashdot", "GameKult")
        )
    }
}
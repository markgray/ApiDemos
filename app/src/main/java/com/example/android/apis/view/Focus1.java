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
import android.webkit.WebView;
import android.widget.ListView;
import android.widget.ArrayAdapter;

/**
 * Demonstrates the use of non-focusable views: caused by android:focusable="false"
 * in layout file layout/focus_1.xml
 */
public class Focus1 extends Activity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.focus_1. We initialize
     * our variable {@code WebView webView} by finding the view with ID R.id.rssWebView, then load an
     * html string into it. We initialize our variable {@code ListView listView} by finding the view
     * with ID R.id.rssListView, and set its adapter to a new instance of {@code ArrayAdapter} created
     * using "this" as its {@code Context}, android.R.layout.simple_list_item_1 as the resource ID for
     * the layout file containing a TextView to use when instantiating views, and the string array
     * {@code String[]{"Ars Technica", "Slashdot", "GameKult"}} as the objects to represent in the ListView.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.focus_1);

        WebView webView = (WebView) findViewById(R.id.rssWebView);
        webView.loadData(
                "<html><body>Can I focus?<br /><a href=\"#\">No I cannot!</a>.</body></html>",
                "text/html", null);

        ListView listView = (ListView) findViewById(R.id.rssListView);
        listView.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                new String[]{"Ars Technica", "Slashdot", "GameKult"}));
    }
}

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
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Sample creating 1 WebView. Html data is loaded into the layout's WebView using WebView.loadData
 */
class WebView1 : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.webview_1. We initialize
     * our [String] variable `val mimeType` to the string "text/html", initialize our [WebView]
     * variable `val wv` by finding the view with id R.id.wv1 in our layout. Finally we call the
     * `loadData` method of `wv` to load the string:
     *
     *  `"<a href='http://www.google.com/'>Hello World! - 1</a>"`
     *
     *  with the mime type `mimeType`,
     *
     *  and null for the encoding (the data uses ASCII encoding for octets inside the range of safe
     *  URL characters and uses the standard %xx hex encoding of URLs for octets outside that range.
     *  For example, '#', '%', '\', '?' should be replaced by %23, %25, %27, %3f respectively).
     *
     * @param icicle we do not override [onSaveInstanceState] so do not use.
     */
    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.webview_1)
        val mimeType = "text/html"
        val wv: WebView = findViewById(R.id.wv1)
        wv.loadData(
            /* data = */ "<a href='http://www.google.com/'>Hello World! - 1</a>",
            /* mimeType = */ mimeType,
            /* encoding = */ null
        )
    }
}
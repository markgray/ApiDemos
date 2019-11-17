/*
 * Copyright (C) 2013 The Android Open Source Project
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
package com.example.android.apis.app

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.print.PrintManager
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * This class demonstrates how to implement HTML content printing
 * from a [WebView] which is shown on the screen.
 *
 *
 * This activity shows a simple HTML content in a [WebView]
 * and allows the user to print that content via an action in the
 * action bar. The shown [WebView] is doing the printing.
 *
 *
 * @see PrintManager
 *
 * @see WebView
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class PrintHtmlFromScreen : AppCompatActivity() {
    private var mWebView // WebView in our layout file
            : WebView? = null
    private var mDataLoaded: Boolean = false // Flag used to enable print option in options menu (set after WebView finishes loading) = false

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, we set our content view to our layout file R.layout.print_html_from_screen
     * and set `WebView mWebView` to the `WebView` we find in our layout R.id.web_view.
     * We set the `WebViewClient` of `mWebView` to an anonymous class which will set our
     * `mDataLoaded` flag to true and invalidate the options menu when `mWebView` finishes
     * loading our Html page (causing the "print" option to be visible). Then we instruct `mWebView`
     * to load our HTML page "file:///android_res/raw/motogp_stats.html".
     *
     * @param savedInstanceState we do not override onSaveInstanceState so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.print_html_from_screen)
        mWebView = findViewById(R.id.web_view)
        // Important: Only enable the print option after the page is loaded.
        mWebView!!.webViewClient = object : WebViewClient() {
            /**
             * Notifies the host application that a page has finished loading. We set our flag
             * `mDataLoaded` to true, and declare that the options menu has changed, so
             * should be recreated.
             *
             * @param view The WebView that is initiating the callback.
             * @param url The url of the page.
             */
            override fun onPageFinished(view: WebView, url: String) { // Data loaded, so now we want to show the print option.
                mDataLoaded = true
                invalidateOptionsMenu()
            }
        }
        // Load an HTML page.
        mWebView!!.loadUrl("file:///android_res/raw/motogp_stats.html")
    }

    /**
     * Initialize the contents of the Activity's standard options menu. First we call through to our
     * super's implementation of `onCreateOptionsMenu`, then if `mDataLoaded` is true
     * we inflate our menu R.menu.print_custom_content in to `Menu menu`. We then return true
     * so that the menu will be displayed.
     *
     * @param menu The options menu in which we place our items.
     *
     * @return We return true so the menu will be displayed
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        if (mDataLoaded) {
            menuInflater.inflate(R.menu.print_custom_content, menu)
        }
        return true
    }

    /**
     * This hook is called whenever an item in our options menu is selected. If the ID of the
     * `MenuItem item` selected is R.id.menu_print, we call our method `print` to print
     * our `WebView mWebView` and return true to the caller. Otherwise we return the result of
     * calling our super's implementation of `onOptionsItemSelected`.
     *
     * @param item The menu item that was selected.
     *
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_print) {
            print()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Causes the print of the `WebView mWebView`. First we fetch a handle to the system
     * wide PRINT_SERVICE service to `PrintManager printManager`, then we ask it to print
     * the `PrintDocumentAdapter` created by `mWebView` using the default printer
     * attributes and the print job name "MotoGP stats".
     */
    private fun print() { // Get the print manager.
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
        // Pass in the ViewView's document adapter.
        printManager.print("MotoGP stats", mWebView!!.createPrintDocumentAdapter("MotoGP stats"), null)
    }
}
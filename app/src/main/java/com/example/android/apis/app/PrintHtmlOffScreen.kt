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

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * This class demonstrates how to implement HTML content printing
 * from a [WebView] which is not shown on the screen.
 *
 * This activity shows a text prompt and when the user chooses the
 * print option from the overflow menu an HTML page with content that
 * is not on the screen is printed via an off-screen [WebView].
 *
 * @see PrintManager
 * @see WebView
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class PrintHtmlOffScreen : AppCompatActivity() {
    /**
     * WebView we load our HTML file into and print
     */
    private var mWebView: WebView? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.print_html_off_screen.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.print_html_off_screen)
    }

    /**
     * Initialize the contents of the Activity's standard options menu. First we call through to our
     * super's implementation of `onCreateOptionsMenu`, then we fetch a `MenuInflater` and use it
     * to inflate our menu resource file R.menu.print_custom_content into our [Menu] parameter
     * [menu]. We return *true* so that the menu will be displayed.
     *
     * @param menu [Menu] we should inflate our menu xml file into.
     * @return true to display the menu.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.print_custom_content, menu)
        return true
    }

    /**
     * This hook is called whenever an item in our options menu is selected. First we check to see
     * if the ID of our [MenuItem] parameter [item] is the same as our R.id.menu_print and if it is
     * we call our method [print] and return *true* to the caller (to indicate that we consumed the
     * selection here). If it is not our [MenuItem] we return the value returned by our super's
     * implementation of `onOptionsItemSelected`.
     *
     * @param item The menu item that was selected.
     * @return [Boolean] we return *true* to consume the menu item selection here if it is our
     * "Print" menu item, otherwise we return the value returned by our super's implementation of
     * `onOptionsItemSelected`.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_print) {
            print()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Called when our "Print" menu item is clicked, it causes our HTML file to be loaded and
     * printed. First we create a [WebView] for our field [mWebView]. Then we set the
     * [WebViewClient] of [mWebView] to an anonymous class which calls our [doPrint] method only
     * when the load of the HTML file completes. Finally we instruct `mWebView` to load our HTML
     * file "file:///android_res/raw/motogp_stats.html"
     */
    private fun print() {
        /**
         * Create a [WebView] and hold on to it as the printing will start when
         * load completes and we do not want the [WebView] to be garbage collected.
         */
        mWebView = WebView(this)
        /**
         * Important: Only after the page is loaded we will do the print.
         */
        mWebView!!.webViewClient = object : WebViewClient() {
            /**
             * Notifies us that a page has finished loading. We simply call our method `doPrint`
             * when the page has loaded.
             *
             * @param view The [WebView] that is initiating the callback.
             * @param url The url of the page.
             */
            override fun onPageFinished(view: WebView, url: String) {
                doPrint()
            }
        }
        /**
         * Load an HTML page.
         */
        mWebView!!.loadUrl("file:///android_res/raw/motogp_stats.html")
    }

    /**
     * Causes our [WebView] field [mWebView] to print its contents. First we get a handle to the
     * system level PRINT_SERVICE for our [PrintManager] variable `val printManager`, then we create
     * an instance of [PrintDocumentAdapter] for our variable `val adapter` which "wraps" around the
     * [PrintDocumentAdapter] created by our [mWebView]. The wrapping allows us to add code to the
     * `onFinish` callback to destroy [mWebView], and set it to *null*. All other callbacks simply
     * call through to the [PrintDocumentAdapter] in `mWrappedInstance` created by [mWebView].
     * Finally we pass the wrapper `adapter` to `printManager` to print the HTML in [mWebView].
     */
    private fun doPrint() {
        /**
         * Get the print manager.
         */
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager

        /**
         * Create a wrapper [PrintDocumentAdapter] to clean up when done.
         */
        val adapter: PrintDocumentAdapter = object : PrintDocumentAdapter() {
            private val mWrappedInstance = mWebView!!.createPrintDocumentAdapter("MotoGP stats")

            /**
             * Called when printing starts. This method is invoked on the main thread. We simply pass
             * the call on through to `mWrappedInstance`.
             */
            override fun onStart() {
                mWrappedInstance.onStart()
            }

            /**
             * Called when the print attributes (page size, density, etc) changed giving you a
             * chance to layout the content such that it matches the new constraints. This method
             * is invoked on the main thread. We simply pass the call on through to
             * `mWrappedInstance`.
             *
             * @param oldAttributes The old print attributes.
             * @param newAttributes The new print attributes.
             * @param cancellationSignal Signal for observing cancel layout requests.
             * @param callback Callback to inform the system for the layout result.
             * @param extras Additional information about how to layout the content.
             */
            override fun onLayout(
                oldAttributes: PrintAttributes,
                newAttributes: PrintAttributes,
                cancellationSignal: CancellationSignal,
                callback: LayoutResultCallback,
                extras: Bundle
            ) {
                mWrappedInstance.onLayout(
                    oldAttributes,
                    newAttributes,
                    cancellationSignal,
                    callback,
                    extras
                )
            }

            /**
             * Called when specific pages of the content should be written in the
             * form of a PDF file to the given file descriptor. This method is invoked
             * on the main thread. We simply pass the call on through to `mWrappedInstance`.
             *
             * @param pages The pages whose content to print - non-overlapping in ascending order.
             * @param destination The destination file descriptor to which to write.
             * @param cancellationSignal Signal for observing cancel writing requests.
             * @param callback Callback to inform the system for the write result.
             */
            override fun onWrite(
                pages: Array<PageRange>,
                destination: ParcelFileDescriptor,
                cancellationSignal: CancellationSignal,
                callback: WriteResultCallback
            ) {
                mWrappedInstance.onWrite(pages, destination, cancellationSignal, callback)
            }

            /**
             * Called when printing finishes. You can use this callback to release resources
             * acquired in [onStart]. This method is invoked on the main thread.
             * We call through to `mWrappedInstance`, destroy the internal state of our
             * [WebView] field [mWebView], and set it to *null*.
             */
            override fun onFinish() {
                mWrappedInstance.onFinish()
                /**
                 * Intercept the finish call to know when printing is done
                 * and destroy the WebView as it is expensive to keep around.
                 */
                mWebView!!.destroy()
                mWebView = null
            }
        }
        /**
         * Pass in the [WebView]'s document adapter.
         */
        printManager.print("MotoGP stats", adapter, null)
    }
}
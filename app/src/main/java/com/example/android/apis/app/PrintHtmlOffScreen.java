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

package com.example.android.apis.app;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.apis.R;

/**
 * This class demonstrates how to implement HTML content printing
 * from a {@link WebView} which is not shown on the screen.
 * <p>
 * This activity shows a text prompt and when the user chooses the
 * print option from the overflow menu an HTML page with content that
 * is not on the screen is printed via an off-screen {@link WebView}.
 * </p>
 *
 * @see PrintManager
 * @see WebView
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class PrintHtmlOffScreen extends AppCompatActivity {

    private WebView mWebView; // WebView we load our HTML file into and print

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.print_html_off_screen.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.print_html_off_screen);
    }

    /**
     * Initialize the contents of the Activity's standard options menu. First we call through to our
     * super's implementation of {@code onCreateOptionsMenu}, then we fetch a {@code MenuInflater} and
     * use it to inflate our menu resource file R.menu.print_custom_content into {@code menu}. We
     * return true so that the menu will be displayed.
     *
     * @param menu Menu to inflate our menu xml file into.
     * @return true to display the menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.print_custom_content, menu);
        return true;
    }

    /**
     * This hook is called whenever an item in our options menu is selected. First we check to see
     * if the ID of {@code MenuItem item} is the same as our R.id.menu_print and if it is we call
     * our method {@code print()} and return true to the caller (to indicate that we consumed the
     * selection here). If it is not our {@code MenuItem} we return the value returned by our super's
     * implementation of onOptionsItemSelected.
     *
     * @param item The menu item that was selected.
     * @return boolean we return true to consume the menu item selection here if it is our "Print"
     * menu item, otherwise we return the value returned by our super's implementation of
     * onOptionsItemSelected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_print) {
            print();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when our "Print" menu item is clicked, it causes our HTML file to be loaded and printed.
     * First we create a {@code WebView} for our field {@code WebView mWebView}. Then we set the
     * {@code WebViewClient} of {@code mWebView} to an anonymous class which calls our {@code doPrint}
     * method only when the load of the HTML file completes. Finally we instruct {@code mWebView} to
     * load our HTML file "file:///android_res/raw/motogp_stats.html"
     */
    private void print() {
        // Create a WebView and hold on to it as the printing will start when
        // load completes and we do not want the WbeView to be garbage collected.
        mWebView = new WebView(this);

        // Important: Only after the page is loaded we will do the print.
        mWebView.setWebViewClient(new WebViewClient() {
            /**
             * Notifies us that a page has finished loading. We simply call our method {@code doPrint}
             * when the page has loaded.
             *
             * @param view The WebView that is initiating the callback.
             * @param url The url of the page.
             */
            @Override
            public void onPageFinished(WebView view, String url) {
                doPrint();
            }
        });

        // Load an HTML page.
        mWebView.loadUrl("file:///android_res/raw/motogp_stats.html");
    }

    /**
     * Causes our field {@code WebView mWebView} to print its contents. First we get a handle to the
     * system level PRINT_SERVICE for {@code PrintManager printManager}, then we create an instance
     * of {@code PrintDocumentAdapter adapter} which "wraps" around the {@code PrintDocumentAdapter}
     * created by our {@code WebView mWebView}. The wrapping allows us to add code to the {@code onFinish}
     * callback to destroy {@code mWebView}, and set it to null. All other callbacks simply call through
     * to the {@code PrintDocumentAdapter mWrappedInstance} created by {@code mWebView}. Finally we
     * pass the wrapper {@code adapter} to {@code printManager} to print the HTML in {@code mWebView}.
     */
    private void doPrint() {
        // Get the print manager.
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);

        // Create a wrapper PrintDocumentAdapter to clean up when done.
        PrintDocumentAdapter adapter = new PrintDocumentAdapter() {
            private final PrintDocumentAdapter mWrappedInstance = mWebView.createPrintDocumentAdapter();

            /**
             * Called when printing starts. This method is invoked on the main thread. We simply pass
             * the call on through to {@code mWrappedInstance}.
             */
            @Override
            public void onStart() {
                mWrappedInstance.onStart();
            }

            /**
             * Called when the print attributes (page size, density, etc) changed giving you a
             * chance to layout the content such that it matches the new constraints. This method
             * is invoked on the main thread. We simply pass the call on through to
             * {@code mWrappedInstance}.
             *
             * @param oldAttributes The old print attributes.
             * @param newAttributes The new print attributes.
             * @param cancellationSignal Signal for observing cancel layout requests.
             * @param callback Callback to inform the system for the layout result.
             * @param extras Additional information about how to layout the content.
             */
            @Override
            public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                                 CancellationSignal cancellationSignal, LayoutResultCallback callback,
                                 Bundle extras) {
                mWrappedInstance.onLayout(oldAttributes, newAttributes, cancellationSignal, callback, extras);
            }

            /**
             * Called when specific pages of the content should be written in the
             * form of a PDF file to the given file descriptor. This method is invoked
             * on the main thread. We simply pass the call on through to {@code mWrappedInstance}.
             *
             * @param pages The pages whose content to print - non-overlapping in ascending order.
             * @param destination The destination file descriptor to which to write.
             * @param cancellationSignal Signal for observing cancel writing requests.
             * @param callback Callback to inform the system for the write result.
             */
            @Override
            public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                                CancellationSignal cancellationSignal, WriteResultCallback callback) {
                mWrappedInstance.onWrite(pages, destination, cancellationSignal, callback);
            }

            /**
             * Called when printing finishes. You can use this callback to release resources
             * acquired in {@link #onStart()}. This method is invoked on the main thread.
             * We call through to {@code mWrappedInstance}, destroy the internal state of our
             * {@code WdbView mWebView}, and set it to null.
             */
            @Override
            public void onFinish() {
                mWrappedInstance.onFinish();
                // Intercept the finish call to know when printing is done
                // and destroy the WebView as it is expensive to keep around.
                mWebView.destroy();
                mWebView = null;
            }
        };

        // Pass in the ViewView's document adapter.
        //noinspection ConstantConditions
        printManager.print("MotoGP stats", adapter, null);
    }
}

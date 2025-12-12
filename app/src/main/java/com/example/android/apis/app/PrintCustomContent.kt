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
@file:Suppress("DEPRECATION", "ReplaceNotNullAssertionWithElvisReturn")
// TODO: Fix the many deprecated method usages
package com.example.android.apis.app

import android.annotation.SuppressLint
import android.app.ListActivity
import android.content.Context
import android.content.res.Configuration
import android.graphics.pdf.PdfDocument
import android.os.AsyncTask
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.print.pdf.PrintedPdfDocument
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.util.size
import com.example.android.apis.R
import java.io.FileOutputStream
import java.io.IOException

/**
 * This class demonstrates how to implement custom printing support.
 *
 * This activity shows the list of the MotoGp champions by year and
 * brand. The print option in the overflow menu allows the user to
 * print the content. The list of items is laid out so that it fits
 * the options selected by the user from the UI such as page size.
 * Hence, for different page sizes the printed content will have
 * different page count.
 *
 * This sample demonstrates how to completely implement a [PrintDocumentAdapter]
 * in which:
 *
 *  * Layout based on the selected print options is performed.
 *  * Layout work is performed only if print options change would change the content.
 *  * Layout result is properly reported.
 *  * Only requested pages are written.
 *  * Write result is properly reported.
 *  * Both Layout and write respond to cancellation.
 *  * Layout and render of views is demonstrated.
 *
 * @see PrintManager
 * @see PrintDocumentAdapter
 * RequiresApi(Build.VERSION_CODES.KITKAT)
 */
class PrintCustomContent : ListActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set the cursor for our `ListView` to an instance of
     * `MotoGpStatAdapter`, which extends `BaseAdapter` and is a very simple adapter
     * that feeds items from the `List<PrintCustomContent.MotoGpStatItem>`'s returned by the
     * method `loadMotoGpStats()` used in its constructor.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listAdapter = MotoGpStatAdapter(loadMotoGpStats(), layoutInflater)
    }

    /**
     * Initialize the contents of the Activity's standard options menu by inflating a menu resource
     * xml file into our [Menu] parameter [menu] using a `MenuInflater`. First we call through to
     * our super's implementation of `onCreateOptionsMenu`, then we use a `MenuInflater` for our
     * [Context] to inflate our menu xml file R.menu.print_custom_content into our [Menu] parameter
     * [menu]. Finally we return *true* so that the menu will be displayed.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return *true* for the menu to be displayed.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.print_custom_content, menu)
        return true
    }

    /**
     * This hook is called whenever an item in your options menu is selected. If the item ID is
     * R.id.menu_print we call our method `print()` and return *true* to consume the item click
     * here. Otherwise we return the return value of our super's implementation of
     * `onOptionsItemSelected`.
     *
     * @param item The menu item that was selected.
     * @return boolean Return true to consume item selection here.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_print) {
            print()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Prints the contents of our `ListView`. First we initialize our variable `val printManager`
     * with a [PrintManager] handle to the PRINT_SERVICE system wide service. Then we request it
     * to create a print job with the job name "MotoGp stats", using a [PrintMotoGpAdapter] subclass
     * of [PrintDocumentAdapter] instance to emit the document to print, and *null* for the
     * `PrintAttributes`.
     */
    private fun print() {
        val printManager = getSystemService(PRINT_SERVICE) as PrintManager
        printManager.print("MotoGp stats", PrintMotoGpAdapter(), null)
    }

    /**
     * Reads in string-array resources containing the years, champions, and constructors for the
     * MotoGp winners then creates a list of [MotoGpStatItem]'s from the three. First we read in
     * the `String[]` array `val years` from the string-array R.array.motogp_years, `String[]` array
     * `val champions` from the string-array R.array.motogp_champions and  `String[]` array
     * `val constructors` from the string-array R.array.motogp_constructors. We allocate an
     * `ArrayList<>` for the `MutableList<MotoGpStatItem>` variable `val items`. Then for every
     * entry in our three string-array's we allocate a `MotoGpStatItem` for `val item`, set the
     * fields of `item` to the respective entry in the arrays `years`, `champions`, and `constructors`.
     * We then add the `MotoGpStatItem` in `item` to the `List<MotoGpStatItem>` in `items`. Finally
     * we return `items` to the caller.
     *
     * @return list of `MotoGpStatItem`'s as read from our string-array resources
     */
    private fun loadMotoGpStats(): List<MotoGpStatItem> {
        val years = resources.getStringArray(R.array.motogp_years)
        val champions = resources.getStringArray(R.array.motogp_champions)
        val constructors = resources.getStringArray(R.array.motogp_constructors)
        val items: MutableList<MotoGpStatItem> = ArrayList()
        val itemCount = years.size
        for (i in 0 until itemCount) {
            val item = MotoGpStatItem()
            item.year = years[i]
            item.champion = champions[i]
            item.constructor = constructors[i]
            items.add(item)
        }
        return items
    }

    /**
     * Class that contains String fields for each MotoGp winner: year, champion, and constructor.
     */
    private class MotoGpStatItem {
        var year: String? = null
        var champion: String? = null
        var constructor: String? = null
    }

    /**
     * `ListAdapter` used to hold the List of `MotoGpStatItem`'s for display in our
     * `ListView` and for `PrintMotoGpAdapter` to use to supply information when it
     * is acting as a `PrintDocumentAdapter`
     *
     * @property mItems `List` of `MotoGpStatItem` data items for MotoGp winners
     * @property mInflater `LayoutInflater` to use in `getView` override to inflate our xml layout file
     * R.layout.motogp_stat_item
     */
    private class MotoGpStatAdapter(
        private val mItems: List<MotoGpStatItem>,
        private val mInflater: LayoutInflater
    ) : BaseAdapter() {

        /**
         * Returns a clone of the MotoGp winner data items in our field `List<MotoGpStatItem> mItems`.
         *
         * @return list containing the elements of our field `List<MotoGpStatItem> mItems`
         */
        fun cloneItems(): List<MotoGpStatItem> {
            return ArrayList(mItems)
        }

        /**
         * How many items are in the data set represented by this Adapter.
         *
         * @return Count of items.
         */
        override fun getCount(): Int {
            return mItems.size
        }

        /**
         * Get the data item associated with the specified position in the data set.
         *
         * @param position Position of the item whose data we want within the adapter's data set.
         * @return The data at the specified position.
         */
        override fun getItem(position: Int): Any {
            return mItems[position]
        }

        /**
         * Get the row id associated with the specified position in the list. Our row id is the same
         * as the position.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        /**
         * Get a View that displays the data at the specified position in the data set. You can
         * either create a View manually or inflate it from an XML layout file. When the View is
         * inflated, the parent View (GridView, ListView...) will apply default layout parameters
         * unless you use [android.view.LayoutInflater.inflate] to specify a root view and to
         * prevent attachment to the root.
         *
         * First we copy our [View] parameter [convertView] into our variable `var convertViewLocal`,
         * then we check to see if `convertViewLocal` can be reused, and if not we use our
         * [LayoutInflater] field [mInflater] to set `convertViewLocal` to our inflated layout file
         * R.layout.motogp_stat_item, using our [ViewGroup] parameter [parent] for layout parameters
         * but not attaching it to that root. Then we fetch the `MotoGpStatItem` at the position
         * [position] to initialize our variable `val item`. We locate the [TextView] at ID R.id.year
         * in our `convertViewLocal` to initialize our variable `val yearView` and set its text to the
         * `year` field of `item`, locate the [TextView] at ID R.id.champion in our `convertViewLocal`
         * to initialize our variable `val championView` and set its text to the `champion` field
         * of `item`, and locate the [TextView] at ID R.id.constructor in our `convertViewLocal` to
         * initialize our variable `val constructorView` and set its text to the `constructor` field
         * of `item`. Finally we return `convertViewLocal` to the caller.
         *
         * @param position    The position of the item within the adapter's data set of the item whose view
         * we want.
         * @param convertView The old view to reuse, if possible. Note: You should check that this view
         * is non-null and of an appropriate type before using. If it is not possible to convert
         * this view to display the correct data, this method can create a new view.
         * Heterogeneous lists can specify their number of view types, so that this View is
         * always of the right type (see [.getViewTypeCount] and
         * [.getItemViewType]).
         * @param parent      The parent that this view will eventually be attached to
         * @return A View corresponding to the data at the specified position.
         */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertViewLocal = convertView
            if (convertViewLocal == null) {
                convertViewLocal = mInflater.inflate(R.layout.motogp_stat_item, parent, false)
            }
            val item = getItem(position) as MotoGpStatItem
            val yearView = convertViewLocal!!.findViewById<TextView>(R.id.year)
            yearView.text = item.year
            val championView = convertViewLocal.findViewById<TextView>(R.id.champion)
            championView.text = item.champion
            val constructorView = convertViewLocal.findViewById<TextView>(R.id.constructor)
            constructorView.text = item.constructor
            return convertViewLocal
        }

    }

    /**
     * This is an implementation of a `PrintDocumentAdapter` which will print the contents of
     * the `MotoGpStatAdapter` Adapter populating our `ListView`.
     */
    private inner class PrintMotoGpAdapter : PrintDocumentAdapter() {
        /**
         * Width of page to be printed, it is the maximum width the printer will print
         */
        private var mRenderPageWidth = 0

        /**
         * Height of page to be printed, it is the maximum height the printer will print
         */
        private var mRenderPageHeight = 0

        /**
         * Copy of `PrintAttributes newAttributes` parameter to `onLayout` which we stash
         * for later use in background thread which does the actual printing.
         */
        private var mPrintAttributes: PrintAttributes? = null

        /**
         * `PrintDocumentInfo info` returned from AsyncTask launched by `onLayout`, used
         * in call to `LayoutResultCallback callback.onLayoutFinished`
         */
        private var mDocumentInfo: PrintDocumentInfo? = null

        /**
         * `Context` for resources at printer density created in `onLayout` using a
         * `Configuration` whose field `densityDpi` is set to a density calculated
         * for the maximum size for printer layout, and whose theme is set to the
         * system theme android.R.style.Theme_Holo_Light
         */
        private var mPrintContext: Context? = null

        /**
         * Called when the print attributes (page size, density, etc) changed
         * giving you a chance to layout the content such that it matches the
         * new constraints. This method is invoked on the main thread.
         *
         * After you are done laying out, you **must** invoke: `LayoutResultCallback.onLayoutFinished`
         * with the last argument *true* or *false* depending on whether the layout changed the
         * content or not, respectively; or
         * `LayoutResultCallback.onLayoutFailed`, if an error occurred;
         * or `LayoutResultCallback.onLayoutCancelled` if layout was
         * cancelled in a response to a cancellation request via the passed in
         * [CancellationSignal]. Note that you **must** call one of
         * the methods of the given callback for this method to be considered complete
         * which is you will not receive any calls to this adapter until the current
         * layout operation is complete by invoking a method on the callback instance.
         * The callback methods can be invoked from an arbitrary thread.
         *
         * One of the arguments passed to this method is a [CancellationSignal]
         * which is used to propagate requests from the system to your application for
         * canceling the current layout operation. For example, a cancellation may be
         * requested if the user changes a print option that may affect layout while
         * you are performing a layout operation. In such a case the system will make
         * an attempt to cancel the current layout as another one will have to be performed.
         * Typically, you should register a cancellation callback in the cancellation
         * signal. The cancellation callback **will not** be made on the
         * main thread and can be registered as follows:
         *
         *     cancellationSignal.setOnCancelListener(new OnCancelListener() {
         *         @Override
         *         public void onCancel() {
         *            // Cancel layout
         *         }
         *     });
         *
         * **Note:** If the content is large and a layout will be performed, it is a good practice
         * to schedule the work on a dedicated thread and register an observer in the provided
         * [CancellationSignal] upon invocation of which you should stop the layout.
         *
         * First we check whether we have already been canceled by calling the `isCanceled()`
         * method of our [CancellationSignal] parameter [cancellationSignal], and if so we just
         * call the `onLayoutCancelled` callback of our `LayoutResultCallback` parameter [callback]
         * and return having done nothing.
         *
         * Next we set a [Boolean] flag `var layoutNeeded` to *false*, assuming that we will not
         * need to do a layout after all. Next we compute the [Int] value `val density` to be
         * the max of the Horizontal DPI and Vertical DPI of our parameter [PrintAttributes]
         * parameter [newAttributes]. We use this to calculate the `marginLeft`, `marginRight` and
         * `contentWidth` we are to use based on the Media size specified in our [PrintAttributes]
         * parameter [newAttributes]. If the value stashed in our [Int] field [mRenderPageWidth]
         * is not equal to `contextWidth`, we set it to `contextWidth` and set our `layoutNeeded`
         * flag to *true*. In a similar way we calculate the Content height given [newAttributes]
         * for our [Int] `val contentHeight`, and if the value stashed in our [Int] field
         * [mRenderPageHeight] is not the same we set [mRenderPageHeight] to `contentHeight` and
         * set our `layoutNeeded` flag to *true*.
         *
         * Next if we have yet to set our [Context] field [mPrintContext], or the DPI it uses is
         * different from the `density` we calculated above we create a [Configuration] for
         * `val configuration`, set its field `densityDpi` to `density`, set [mPrintContext] to a
         * [Context] created from `configuration` and set its theme to the system theme
         * android.R.style.Theme_Holo_Light.
         *
         * Now if our flag `layoutNeeded` is still *false*, we call the callback
         * `callback.onLayoutFinished(mDocumentInfo, false)` and return.
         *
         * Otherwise we have work to do. We clone the contents of our `ListAdapter` into
         * `List<MotoGpStatItem>` variable `val items` so that a background thread can access it,
         * then launch an anonymous [MotoGpOnLayoutAsyncTask] constructed using our parameters
         * [cancellationSignal], and [newAttributes], our `List` of [MotoGpStatItem]'s in `items`
         * and our parameter [callback] to do all the work for us.
         *
         * @param oldAttributes      The old print attributes.
         * @param newAttributes      The new print attributes.
         * @param cancellationSignal Signal for observing cancel layout requests.
         * @param callback           Callback to inform the system for the layout result.
         * @param metadata           Additional information about how to layout the content. Unused
         *
         * @see CancellationSignal
         *
         * @see .EXTRA_PRINT_PREVIEW
         */
        override fun onLayout(
            oldAttributes: PrintAttributes,
            newAttributes: PrintAttributes,
            cancellationSignal: CancellationSignal,
            callback: LayoutResultCallback,
            metadata: Bundle
        ) { // If we are already cancelled, don't do any work.
            if (cancellationSignal.isCanceled) {
                callback.onLayoutCancelled()
                return
            }
            /**
             * Now we determined if the print attributes changed in a way that
             * would change the layout and if so we will do a layout pass.
             */
            var layoutNeeded = false
            val density = newAttributes.resolution!!.horizontalDpi
                .coerceAtLeast(newAttributes.resolution!!.verticalDpi)

            /**
             * Note that we are using the PrintedPdfDocument class which creates
             * a PDF generating canvas whose size is in points (1/72") not screen
             * pixels. Hence, this canvas is pretty small compared to the screen.
             * The recommended way is to layout the content in the desired size,
             * in this case as large as the printer can do, and set a translation
             * to the PDF canvas to shrink in. Note that PDF is a vector format
             * and you will not lose data during the transformation.
             * The content width is equal to the page width minus the margins times
             * the horizontal printer density. This way we get the maximal number
             * of pixels the printer can put horizontally.
             */
            val marginLeft = (density * newAttributes.minMargins!!
                .leftMils.toFloat() / MILS_IN_INCH).toInt()
            val marginRight = (density * newAttributes.minMargins!!
                .rightMils.toFloat() / MILS_IN_INCH).toInt()
            val contentWidth = (density * newAttributes.mediaSize!!
                .widthMils.toFloat() / MILS_IN_INCH).toInt() - marginLeft - marginRight
            if (mRenderPageWidth != contentWidth) {
                mRenderPageWidth = contentWidth
                layoutNeeded = true
            }
            /**
             * The content height is equal to the page height minus the margins times
             * the vertical printer resolution. This way we get the maximal number
             * of pixels the printer can put vertically.
             */
            val marginTop = (density * newAttributes.minMargins!!
                .topMils.toFloat() / MILS_IN_INCH).toInt()
            val marginBottom = (density * newAttributes.minMargins!!
                .bottomMils.toFloat() / MILS_IN_INCH).toInt()
            val contentHeight = (density * newAttributes.mediaSize!!
                .heightMils.toFloat() / MILS_IN_INCH).toInt() - marginTop - marginBottom
            if (mRenderPageHeight != contentHeight) {
                mRenderPageHeight = contentHeight
                layoutNeeded = true
            }
            /**
             * Create a context for resources at printer density. We will
             * be inflating views to render them and would like them to use
             * resources for a density the printer supports.
             */
            if (mPrintContext == null || mPrintContext!!.resources
                    .configuration.densityDpi != density
            ) {
                val configuration = Configuration()
                configuration.densityDpi = density
                mPrintContext = createConfigurationContext(
                    configuration
                )
                @Suppress("DEPRECATION")
                (mPrintContext as Context).setTheme(android.R.style.Theme_Holo_Light)
            }
            /**
             * If no layout is needed that we did a layout at least once and
             * the document info is not null, also the second argument is false
             * to notify the system that the content did not change. This is
             * important as if the system has some pages and the content didn't
             * change the system will ask, the application to write them again.
             */
            if (!layoutNeeded) {
                callback.onLayoutFinished(mDocumentInfo, false)
                return
            }
            /**
             * For demonstration purposes we will do the layout off the main thread
             * but for small content sizes like this one it is OK to do that on the
             * main thread. Store the data as we will layout off the main thread.
             */
            val items = (listAdapter as MotoGpStatAdapter).cloneItems()
            MotoGpOnLayoutAsyncTask(cancellationSignal, newAttributes, items, callback)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null, null)
        }

        /**
         * Called when specific pages of the content should be written in the form of a PDF file
         * to the given file descriptor. This method is invoked on the main thread.
         *
         * After you are done writing, you should close the file descriptor and invoke
         * the `onWriteFinished` method of the `WriteResultCallback` parameter [callback]
         * if writing completed successfully; or the `onWriteFailed` method if an error occurred;
         * or the `onWriteCancelled` method if writing was cancelled in a response to a cancellation
         * request via the passed in [CancellationSignal] parameter [cancellationSignal].
         * Note that you **must** call one of the methods of the given [callback] for this method
         * to be considered complete which is you will not receive any calls to this adapter until
         * the current write operation is complete by invoking a method on the callback instance.
         * The callback methods can be invoked from an arbitrary thread.
         *
         * One of the arguments passed to this method is a [CancellationSignal]
         * which is used to propagate requests from the system to your application for
         * canceling the current write operation. For example, a cancellation may be
         * requested if the user changes a print option that may affect layout while
         * you are performing a write operation. In such a case the system will make
         * an attempt to cancel the current write as a layout will have to be performed
         * which then may be followed by a write. Typically, you should register a
         * cancellation callback in the cancellation signal. The cancellation callback
         * **will not** be made on the main thread and can be registered
         * as follows:
         *
         *      cancellationSignal.setOnCancelListener(new OnCancelListener() {
         *          @Override
         *          public void onCancel() {
         *              // Cancel write
         *          }
         *      });
         *
         * **Note:** If the printed content is large, it is a good practice to schedule writing it
         * on a dedicated thread and register an observer in the provided [CancellationSignal] upon
         * invocation of which you should stop writing.
         *
         * First we check whether we have already been canceled by calling the `isCanceled` method
         * of our [CancellationSignal] parameter [cancellationSignal], and if so we just call the
         * `onWriteCancelled` callback of our `LayoutResultCallback` parameter [callback] and
         * return having done nothing.
         *
         * Otherwise we have work to do. We clone the contents of our `ListAdapter` into a
         * `List<MotoGpStatItem>` variable `val items` so that a background thread can access it,
         * then launch an anonymous [MotoGpOnWriteAsyncTask] constructed using our parameter
         * [cancellationSignal], our `List` of [MotoGpStatItem]'s in `items`, our parameter [pages],
         * and our parameter [callback] to do all the work for us.
         *
         * @param pages              The pages whose content to print - non-overlapping in ascending order.
         * @param destination        The destination file descriptor to which to write.
         * @param cancellationSignal Signal for observing cancel writing requests.
         * @param callback           Callback to inform the system for the write result.
         *
         * @see CancellationSignal
         */
        override fun onWrite(
            pages: Array<PageRange>,
            destination: ParcelFileDescriptor,
            cancellationSignal: CancellationSignal,
            callback: WriteResultCallback
        ) { // If we are already cancelled, don't do any work.
            if (cancellationSignal.isCanceled) {
                callback.onWriteCancelled()
                return
            }
            // Store the data as we will layout off the main thread.
            val items = (listAdapter as MotoGpStatAdapter).cloneItems()
            MotoGpOnWriteAsyncTask(cancellationSignal, items, pages, destination, callback)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null, null)
        }

        /**
         * Provides the printer page dimensions contained in our fields [mRenderPageWidth] and
         * [mRenderPageHeight] to our [View] parameter [view] as constraint information for its
         * width and height dimensions. First we create an [Int] `val widthMeasureSpec`
         * [MeasureSpec] integer, specifying the requirements for this view to be EXACTLY
         * [mRenderPageWidth], with 0 padding, and however big the child wants to be in the width
         * dimension. Then we create an [Int] `val heightMeasureSpec` [MeasureSpec] integer,
         * specifying the  requirements for this view to be EXACTLY [mRenderPageHeight], with 0
         * padding, and however big the child wants to be in the height dimension. Finally we pass
         * these two values as constraints to our [View] parameter [view]'s measure method. This
         * configures the [View] for later calls to `getMeasuredHeight` by `onLayout`, and to
         * render itself to the canvas it is given by `onWrite` when the time comes. Note that
         * we only use this to measure all the item views fetched from [MotoGpStatAdapter].
         *
         * @param view [View] that needs to determine its measurement based on the printer
         * sizes in our fields [mRenderPageWidth] and [mRenderPageHeight]
         */
        private fun measureView(view: View?) {
            val widthMeasureSpec = ViewGroup.getChildMeasureSpec(
                MeasureSpec.makeMeasureSpec(mRenderPageWidth, MeasureSpec.EXACTLY),
                0,
                view!!.layoutParams.width
            )
            val heightMeasureSpec = ViewGroup.getChildMeasureSpec(
                MeasureSpec.makeMeasureSpec(mRenderPageHeight, MeasureSpec.EXACTLY),
                0,
                view.layoutParams.height
            )
            view.measure(widthMeasureSpec, heightMeasureSpec)
        }

        /**
         * Used to calculate the `PageRange[]` array returned to `onWriteFinished`, it
         * parses all the page numbers printed that are adjacent in its [SparseIntArray]
         * parameter [writtenPages] into different [PageRange] structures which it
         * stores in `List<PageRange>` [MutableList] `val pageRanges` one by one. When done
         * with its input it converts `pageRanges` to a [PageRange] array `val pageRangesArray`
         * which it returns to the caller.
         *
         * To do this we first allocate an [ArrayList] for our `MutableList<PageRange>` variable
         * `val pageRanges`, declare the `var start` of [PageRange] variable, declare the `var end`
         * of [PageRange] variable, and initialize the [Int] `var writtenPageCount` to the number
         * of pages contained in our [SparseIntArray] parameter [writtenPages].
         *
         * Now we loop through all the pages in [writtenPages] one by one using the index `i`. We
         * fetch the page number contained in the `i`'th entry in [writtenPages] and use it to set
         * `start`, `oldEnd` and `end`. Then while the next page number fetched from [writtenPages]
         * is less than 1 greater than the previous page (`(end - oldEnd) <= 1`), we set `oldEnd`
         * to `end`, fetch the next page number from [writtenPages] to set `end`, and increment the
         * index `i`. We continue the while loop as long as the pages are adjacent, when there is a
         * skip of a page (or more) or we run out of pages (`i >= writtenPageCount`), we exit the
         * while loop, construct a [PageRange] `val pageRange` that runs from `start` to `end`
         * and add it to our `MutableList<PageRange>` variable `pageRanges`.
         *
         * Once all pages in [writtenPages] have been processed in this manner, we allocate
         * a [PageRange] array for `val pageRangesArray` to be the size of `pageRanges`, and load
         * the contents of `pageRanges` into `pageRangesArray` then return `pageRangesArray` to
         * the caller.
         *
         * @param writtenPages pages that were printed stored in a [SparseIntArray]
         * @return Range of pages printed
         */
        private fun computeWrittenPageRanges(writtenPages: SparseIntArray): Array<PageRange?> {
            /**
             * List of [PageRange] structures we parse the [writtenPages] parameter into.
             */
            val pageRanges: MutableList<PageRange> = ArrayList()

            /**
             * Current start of the range of pages
             */
            var start: Int

            /**
             * Current end of the range of pages
             */
            var end: Int

            /**
             * Size of the [writtenPages] array, number of pages stored in it.
             */
            val writtenPageCount = writtenPages.size
            var i = 0
            while (i < writtenPageCount) {
                start = writtenPages.valueAt(i)
                end = start
                var oldEnd = end
                while (i < writtenPageCount && (end - oldEnd) <= 1) {
                    oldEnd = end
                    end = writtenPages.valueAt(i)
                    i++
                }
                val pageRange = PageRange(start, end)
                pageRanges.add(pageRange)
                i++
            }
            val pageRangesArray = arrayOfNulls<PageRange>(pageRanges.size)
            i = 0
            for (pageRange in pageRanges) {
                pageRangesArray[i++] = pageRange
            }
            return pageRangesArray
        }

        /**
         * Looks through [PageRange] array parameter [pageRanges] to see if the page [page] is
         * included somewhere in the page ranges. For each [PageRange] `pageRange` in [pageRanges]
         * we check to see if `pageRange` "contains" [page] between its start and end,
         * and if so we return *true*. If none of the [PageRange]'s in [pageRanges] contain [page]
         * we return *false*.
         *
         * @param pageRanges array of [PageRange] objects to search
         * @param page       page number to search for
         * @return *true* if page is found, *false* if not
         */
        private fun containsPage(pageRanges: Array<PageRange>, page: Int): Boolean {
            for (pageRange: PageRange in pageRanges) {
                if ((pageRange.start <= page
                        && pageRange.end >= page)
                ) {
                    return true
                }
            }
            return false
        }

        /**
         * Background task to perform all the layouts required by the `onLayout` callback in
         * order to calculate a [PrintDocumentInfo] for the [PrintDocumentAdapter].
         *
         * @param cancellationSignal Signal for observing cancel layout requests passed to `onLayout`
         * which allows us to be canceled by setting a `OnCancelListener` on it.
         * @param newAttributes The new print attributes passed to `onLayout`
         * This is a copy of the [PrintAttributes] passed to `onLayout` which represents the
         * attributes of the print job. These attributes describe how the printed content should be
         * laid out.
         * @param items List of [MotoGpStatItem]'s which we display in our ListView and want to print.
         * @param callback Callback to inform the system for the layout result passed to `onLayout`
         * Provides access to the three callbacks we need to return our result when we are done:
         *  * `onLayoutFinished` - layout was successful, returns a `PrintDocumentInfo` which
         *  contains the information our layout determined.
         *  * `onLayoutCancelled` - Notifies that layout was cancelled as a result of a
         *  cancellation request.
         *  * `onLayoutFailed` - Notifies that an error occurred while laying out the
         *  document, the argument is a `CharSequence` describing the error.
         */
        @SuppressLint("StaticFieldLeak")
        private inner class MotoGpOnLayoutAsyncTask(
            private val cancellationSignal: CancellationSignal,
            private val newAttributes: PrintAttributes,
            private val items: List<MotoGpStatItem>,
            private val callback: LayoutResultCallback

        ) : AsyncTask<Void?, Void?, PrintDocumentInfo?>() {

            /**
             * Runs on the UI thread before [doInBackground]. First we set the `OnCancelListener`
             * of our [CancellationSignal] field [cancellationSignal] to an anonymous class which
             * calls `AsyncTask.cancel(true)` to cancel this [MotoGpOnLayoutAsyncTask] when the
             * print job signals that it has been canceled, and then we stash a copy of our field
             * `newAttributes` in our [PrintAttributes] field [mPrintAttributes].
             * TODO: figure out if this stashing is necessary after our refactoring
             */
            @Deprecated("Deprecated in Java")
            override fun onPreExecute() { // First register for cancellation requests.
                cancellationSignal.setOnCancelListener { cancel(true) }
                // Stash the attributes as we will need them for rendering.
                mPrintAttributes = newAttributes
            }

            /**
             * This background thread does a trial layout of our document in order to determine how
             * many pages the document will be, which it returns encoded in a [PrintDocumentInfo]
             * object. Wrapped in a try block, we initialize our variables and loop through every
             * items [View] that our [MotoGpStatAdapter] contains, inflate it, measure it, and
             * add that measurement to our running count of the size of the current page kept in our
             * variable `var pageContentHeight`. When `pageContentHeight` exceeds `mRenderPageHeight`
             * (the height of a printed page), we advance our page count `currentPage` and "place"
             * the last [View] laid out on a new page. When we are done measuring all the Views we
             * build a [PrintDocumentInfo] `val info` containing the page count, we call the callback
             * `LayoutResultCallback.onLayoutFinished` with `info` and the flag for reporting
             * a layout change set to *true*. Finally we return `info` to the caller. If our try
             * block encounters an exception, we call the callback `LayoutResultCallback.onLayoutFailed`
             * and throw a runtime exception.
             *
             * @param params we do not use any parameters
             * @return information about our document for printing purposes contains the document
             * name "MotoGP_stats.pdf", the content type CONTENT_TYPE_DOCUMENT, and the total number
             * of pages in our document.
             */
            @Deprecated("Deprecated in Java")
            @SuppressLint("WrongThread")
            override fun doInBackground(vararg params: Void?): PrintDocumentInfo? {
                try {
                    /**
                     * Create an adapter with the stats and an inflater
                     * to load resources for the printer density.
                     */
                    val inflater = mPrintContext!!
                        .getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val adapter = MotoGpStatAdapter(items, inflater)

                    /**
                     * Page count, which is advanced every time pageContentHeight > [mRenderPageHeight]
                     */
                    var currentPage = 0

                    /**
                     * Height of the current page being laid out
                     */
                    var pageContentHeight = 0

                    /**
                     * view type of the last View processed
                     */
                    var viewType = -1

                    /**
                     * View containing current item that is being processed
                     * This is used to provide layout parameters when calling
                     * `MotoGpStatAdapter.getView`
                     */
                    var view: View? = null
                    val dummyParent = LinearLayout(mPrintContext)
                    dummyParent.orientation = LinearLayout.VERTICAL
                    /**
                     *
                     */
                    val itemCount = adapter.count
                    for (i in 0 until itemCount) {
                        /**
                         * Be nice and respond to cancellation.
                         */
                        if (isCancelled) {
                            return null
                        }
                        /**
                         * Get the next view.
                         */
                        val nextViewType = adapter.getItemViewType(i)
                        view = if (viewType == nextViewType) {
                            adapter.getView(i, (view)!!, dummyParent)
                        } else {
                            adapter.getView(i, null, dummyParent)
                        }
                        viewType = nextViewType
                        /**
                         * Measure the next view
                         */
                        measureView(view)
                        /**
                         * Add the height but if the view crosses the page
                         * boundary we will put it to the next page.
                         */
                        pageContentHeight += view.measuredHeight
                        if (pageContentHeight > mRenderPageHeight) {
                            pageContentHeight = view.measuredHeight
                            currentPage++
                        }
                    }
                    /**
                     * Create a document info describing the result.
                     */
                    val info = PrintDocumentInfo.Builder("MotoGP_stats.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(currentPage + 1)
                        .build()
                    /**
                     * We completed the layout as a result of print attributes
                     * change. Hence, if we are here the content changed for
                     * sure which is why we pass true as the second argument.
                     */
                    callback.onLayoutFinished(info, true)
                    return info
                } catch (e: Exception) {
                    /**
                     * An unexpected error, report that we failed and
                     * one may pass in a human readable localized text
                     * for what the error is if known.
                     */
                    callback.onLayoutFailed(null)
                    throw RuntimeException(e)
                }
            }

            /**
             * Runs on the UI thread after [doInBackground]. The specified result is the value
             * returned by [doInBackground]. We simply cache the [PrintDocumentInfo] parameter
             * [result] in our [PrintDocumentInfo] field [mDocumentInfo] in order to send it over
             * if the next layout pass does not result in a content change.
             *
             * @param result the [PrintDocumentInfo] calculated by our override of [doInBackground]
             */
            @Deprecated("Deprecated in Java")
            override fun onPostExecute(result: PrintDocumentInfo?) {
                /**
                 * Update the cached info to send it over if the next
                 * layout pass does not result in a content change.
                 */
                mDocumentInfo = result
            }

            /**
             * Runs on the UI thread after [cancel] is invoked and
             * `doInBackground(Object[])` has finished. We simply call the callback
             * `LayoutResultCallback.onLayoutCancelled`.
             *
             * @param result ignored
             */
            @Deprecated("Deprecated in Java")
            override fun onCancelled(result: PrintDocumentInfo?) {
                /**
                 * Task was cancelled, report that.
                 */
                callback.onLayoutCancelled()
            }

        }

        /**
         * Background task launched from `PrintDocumentAdapter.onWrite` callback which is
         * called when specific pages of the content should be written in the form of a PDF file to
         * the given [ParcelFileDescriptor] file descriptor [destination].
         *
         * @property cancellationSignal Signal for observing cancel writing requests. It is used to
         * propagate requests from the system to your application for canceling the current write
         * operation.
         * @property items List of MotoGp winners data Objects for our [MotoGpStatAdapter] to hold
         * for this [MotoGpOnWriteAsyncTask] to format and write.
         * @property pages The pages that we should print.
         * @property destination The destination file descriptor to write to.
         * @property callback Callback to inform the system of the write result.
         */
        @SuppressLint("StaticFieldLeak")
        private inner class MotoGpOnWriteAsyncTask(
            private val cancellationSignal: CancellationSignal,
            private val items: List<MotoGpStatItem>,
            private val pages: Array<PageRange>,
            private val destination: ParcelFileDescriptor,
            private val callback: WriteResultCallback
        ) : AsyncTask<Void?, Void?, Void?>() {
            /**
             * [SparseIntArray] holding the page numbers of the pages we have printed.
             */
            private val mWrittenPages: SparseIntArray = SparseIntArray()

            /**
             * [PrintedPdfDocument] we use to hold the canvases we draw our views on.
             */
            private val mPdfDocument: PrintedPdfDocument =
                PrintedPdfDocument(this@PrintCustomContent, (mPrintAttributes)!!)

            /**
             * Runs on the UI thread before [doInBackground]. We simply set the
             * `OnCancelListener` of our [CancellationSignal] field [cancellationSignal]
             * to a lambda which cancels our [AsyncTask], interrupting our thread if needed.
             */
            @Deprecated("Deprecated in Java")
            override fun onPreExecute() {
                /**
                 * First register for cancellation requests.
                 */
                cancellationSignal.setOnCancelListener { cancel(true) }
            }

            /**
             * Started by our `onWrite` callback to render, draw and write pdf using our
             * [PrintedPdfDocument] field [mPdfDocument] on a background thread. First we create
             * an [MotoGpStatAdapter] for `val adapter` using our clone of the UI's content data
             * list in our `List<MotoGpStatItem>` field [items], and a [LayoutInflater] retrieved
             * from the system. We set the current page number: `var currentPage` to -1, and the
             * height of the page we are working on: `var pageContentHeight` to 0. We set our
             * item view type `var viewType` to -1 to indicate that we do not currently have a
             * `View view` of the correct type for our `adapter` to reuse and need to request one
             * by passing *null* for the `convertView` when calling `getView`. We initialize the
             * data item [View] that we will be using for each of the data items ([View] `var view`)
             * to null, and the [PdfDocument.Page] `var page` whose canvas we are currently
             * drawing to for the current page to *null*. We construct a [LinearLayout] for our
             * `val dummyParent` in order to use it for `LayoutParams` when calling `adapter.getView`,
             * and set its orientation to VERTICAL. We compute a [Float] scaling factor `val scale`
             * in order to convert our layout and rendering which is done in pixels to points (1/72")
             * which is used by the PDF canvas. We set [Int] `val itemCount` to the total number of
             * data items contained in our [MotoGpStatAdapter] `adapter` and then loop for each of
             * these using `i` as the index.
             *
             * After making sure we have not been canceled first, for each item `i` contained in
             * `adapter` we fetch the type of the view to `val nextViewType`, and if it is the same
             * as `viewType` (the type of the current [View] in `view`) we pass `view` to
             * `adapter.getView` to be recycled when requesting it to render the item `i` into a
             * [View] we can set `view` to, otherwise we pass *null* so that it will allocate and
             * layout a new [View] before rendering item `i`. We set `viewType` to `newViewType` to
             * cause the [View] to be recycled on the next pass. We then call our method `measureView`
             * to instruct `view` to measure itself based on the printer page dimensions, and add the
             * measured height of the [View] in `view` to `pageContentHeight`.
             *
             * Then if this is the first time through the loop (`currentPage` < 0) or the size of
             * the current page is greater than the size of the printer page ( that is:
             * `pageContentHeight > mRenderPageHeight`) we set `pageContentHeight` to the height
             * of the [View] `view`, increment `currentPage`, and if we have a page ready to print
             * (not the first time through the loop, and the last page was among those requested to
             * be printed) we finish the [PdfDocument.Page] `page` we have been working on in our
             * [PrintedPdfDocument] field [mPdfDocument].
             *
             * We look through the `PageRanges[]` parmater [pages] for the `currentPage` using our
             * method [containsPage], and if it is a wanted page number we start a new
             * [PdfDocument.Page] for `page`, fetch its `Canvas` in order to scale it using our
             * pixels to points scaling factor `scale` and append `currentPage` to our list of
             * written pages in the [SparseIntArray] field [mWrittenPages]. If it is not a wanted
             * page we set `page` to *null*.
             *
             * Then if `page` is not *null*, we layout [View] `view` and instruct it to draw
             * itself into the `Canvas` of `page`. Then we translate the `Canvas` of `page` to
             * get ready for the next view.
             *
             * Once done with all the items in our `adapter`, if `page` is not null we
             * finish that page.
             *
             * Once done rendering the [PrintedPdfDocument] field [mPdfDocument], wrapped in a
             * try block intended to catch [IOException], we instruct [mPdfDocument] to write
             * itself to a [FileOutputStream] created from the `FileDescriptor` of our
             * [ParcelFileDescriptor] field [destination]. If we catch an [IOException] we call
             * `callback.onWriteFailed`. In any case we close [mPdfDocument] in a finally block and
             * return *null* to our caller.
             *
             * TODO: fix "WrongThread" warning
             *
             * @param params we have no parameters
             * @return we return *null*
             */
            @Deprecated("Deprecated in Java")
            @SuppressLint("WrongThread")
            override fun doInBackground(vararg params: Void?): Void? {
                /**
                 * Go over all the pages and write only the requested ones.
                 * Create an adapter with the stats and an inflater
                 * to load resources for the printer density.
                 */
                val adapter = MotoGpStatAdapter(
                    items,
                    mPrintContext!!
                        .getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                )

                /**
                 * Current page number that we are working on
                 */
                var currentPage = -1

                /**
                 * Height of the current page so far
                 */
                var pageContentHeight = 0

                /**
                 * Set to -1 so we only request a new [View] the first time
                 * (`getItemViewType` returns > 0)
                 */
                var viewType = -1

                /**
                 * View for item that we are laying out, then drawing to the pdf `Canvas`
                 */
                var view: View? = null

                /**
                 * [PdfDocument.Page] page of the pdf that we are drawing to.
                 */
                var page: PdfDocument.Page? = null

                /**
                 * This dummy [LinearLayout] is used to provide `LayoutParams` for rendering the
                 * data item [View].
                 */
                val dummyParent = LinearLayout(mPrintContext)
                dummyParent.orientation = LinearLayout.VERTICAL
                /**
                 * The content is laid out and rendered in screen pixels with
                 * the width and height of the paper size times the print
                 * density but the PDF canvas size is in points which are 1/72",
                 * so we will scale down the content.
                 */
                val scale = (
                    mPdfDocument.pageContentRect.width().toFloat()
                        / mRenderPageWidth).coerceAtMost(
                    (
                        mPdfDocument.pageContentRect.height().toFloat()
                            / mRenderPageHeight)
                )
                val itemCount = adapter.count
                for (i in 0 until itemCount) {
                    /**
                     * Be nice and respond to cancellation.
                     */
                    if (isCancelled) {
                        return null
                    }
                    /**
                     * Get the next view.
                     */
                    val nextViewType = adapter.getItemViewType(i)
                    view = if (viewType == nextViewType) {
                        adapter.getView(i, (view)!!, dummyParent)
                    } else {
                        adapter.getView(i, null, dummyParent)
                    }
                    viewType = nextViewType
                    /**
                     * Measure the next view
                     */
                    measureView(view)
                    /**
                     * Add the height but if the view crosses the page
                     * boundary we will put it to the next one.
                     */
                    pageContentHeight += view.measuredHeight
                    if (currentPage < 0 || pageContentHeight > mRenderPageHeight) {
                        pageContentHeight = view.measuredHeight
                        currentPage++
                        /**
                         * Done with the current page - finish it.
                         */
                        if (page != null) {
                            mPdfDocument.finishPage(page)
                        }
                        /**
                         * If the page is requested, render it.
                         */
                        if (containsPage(pages, currentPage)) {
                            page = mPdfDocument.startPage(currentPage)
                            page.canvas.scale(scale, scale)
                            /**
                             * Keep track which pages are written.
                             */
                            mWrittenPages.append(mWrittenPages.size, currentPage)
                        } else {
                            page = null
                        }
                    }
                    /**
                     * If the current view is on a requested page, render it.
                     */
                    if (page != null) {
                        /**
                         * Layout and render the content.
                         */
                        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
                        view.draw(page.canvas)
                        /**
                         * Move the canvas for the next view.
                         */
                        page.canvas.translate(0f, view.height.toFloat())
                    }
                }
                /**
                 * Done with the last page.
                 */
                if (page != null) {
                    mPdfDocument.finishPage(page)
                }
                /**
                 * Write the data and return success or failure.
                 */
                try {
                    mPdfDocument.writeTo(FileOutputStream(destination.fileDescriptor))
                    /**
                     * Compute which page ranges were written based on
                     * the bookkeeping we maintained.
                     */
                    val pageRanges = computeWrittenPageRanges(mWrittenPages)
                    callback.onWriteFinished(pageRanges)
                } catch (_: IOException) {
                    callback.onWriteFailed(null)
                } finally {
                    mPdfDocument.close()
                }
                return null
            }

            /**
             * Runs on the UI thread after [cancel] is invoked and
             * `doInBackground(Object[])` has finished. We call `callback.onWriteCancelled`
             * and close the [PrintedPdfDocument] field [mPdfDocument]
             *
             * @param result no result used
             */
            @Deprecated("Deprecated in Java")
            override fun onCancelled(result: Void?) { // Task was cancelled, report that.
                callback.onWriteCancelled()
                mPdfDocument.close()
            }

        }
    }

    /**
     * Our static constants
     */
    companion object {
        /**
         * How many mils in an inch
         */
        private const val MILS_IN_INCH = 1000
    }
}
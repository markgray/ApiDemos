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
import android.app.ListActivity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.pdf.PdfDocument.Page;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.CancellationSignal.OnCancelListener;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.print.pdf.PrintedPdfDocument;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.apis.R;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class demonstrates how to implement custom printing support.
 * <p>
 * This activity shows the list of the MotoGp champions by year and
 * brand. The print option in the overflow menu allows the user to
 * print the content. The list of items is laid out to that it fits
 * the options selected by the user from the UI such as page size.
 * Hence, for different page sizes the printed content will have
 * different page count.
 * <p>
 * This sample demonstrates how to completely implement a {@link PrintDocumentAdapter}
 * in which:
 * <ul>
 * <li>Layout based on the selected print options is performed.</li>
 * <li>Layout work is performed only if print options change would change the content.</li>
 * <li>Layout result is properly reported.</li>
 * <li>Only requested pages are written.</li>
 * <li>Write result is properly reported.</li>
 * <li>Both Layout and write respond to cancellation.</li>
 * <li>Layout and render of views is demonstrated.</li>
 * </ul>
 *
 * @see PrintManager
 * @see PrintDocumentAdapter
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class PrintCustomContent extends ListActivity {

    /**
     * How many mils in an inch
     */
    private static final int MILS_IN_INCH = 1000;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set the cursor for our {@code ListView} to an instance of
     * {@code MotoGpStatAdapter}, which extends {@code BaseAdapter} and is a very simple adapter
     * that feeds items from the {@code List<PrintCustomContent.MotoGpStatItem>}'s returned by the
     * method {@code loadMotoGpStats()} used in its constructor.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new MotoGpStatAdapter(loadMotoGpStats(), getLayoutInflater()));
    }

    /**
     * Initialize the contents of the Activity's standard options menu by inflating a menu resource
     * xml file into <var>menu</var> using a {@code MenuInflater}. First we call through to our
     * super's implementation of {@code onCreateOptionsMenu}, then we use a {@code MenuInflater}
     * to inflate our menu xml file R.menu.print_custom_content into {@code Menu menu}. Finally we
     * return true so that the menu will be displayed.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.print_custom_content, menu);
        return true;
    }

    /**
     * This hook is called whenever an item in your options menu is selected. If the item ID is
     * R.id.menu_print we call our method {@code print()} and return true to consume the item click
     * here. Otherwise we return the return value of our super's implementation of
     * {@code onOptionsItemSelected}.
     *
     * @param item The menu item that was selected.
     * @return boolean Return true to consume item selection here.
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
     * Prints the contents of our {@code ListView}. First we initialize {@code PrintManager printManager}
     * with a handle to the PRINT_SERVICE system wide service. Then we request it to create a print job
     * with the job name "MotoGp stats", an {@code PrintMotoGpAdapter} {@code PrintDocumentAdapter}
     * class instance to emit the document to print, and null for the {@code PrintAttributes}.
     */
    private void print() {
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);

        printManager.print("MotoGp stats", new PrintMotoGpAdapter(), null);
    }

    /**
     * Reads in string-array resources containing the years, champions, and constructors for the MotoGp
     * winners then creates a list of {@code MotoGpStatItem}'s from the three. First we read in
     * {@code String[] years} from the string-array R.array.motogp_years, {@code String[] champions}
     * from the string-array R.array.motogp_champions and  {@code String[] years} from the string-array
     * R.array.motogp_constructors. We allocate an {@code ArrayList<>} for {@code List<MotoGpStatItem> items}.
     * Then for every entry in our three string-array's we allocate a {@code MotoGpStatItem item}  set the
     * fields of {@code item} to the respective entry in the arrays {@code years, champions, and constructors}.
     * We then add the {@code MotoGpStatItem item} to {@code List<MotoGpStatItem> items}. Finally we
     * return {@code items} to the caller.
     *
     * @return list of {@code MotoGpStatItem}'s as read from our string-array resources
     */
    private List<MotoGpStatItem> loadMotoGpStats() {
        String[] years = getResources().getStringArray(R.array.motogp_years);
        String[] champions = getResources().getStringArray(R.array.motogp_champions);
        String[] constructors = getResources().getStringArray(R.array.motogp_constructors);

        List<MotoGpStatItem> items = new ArrayList<>();

        final int itemCount = years.length;
        for (int i = 0; i < itemCount; i++) {
            MotoGpStatItem item = new MotoGpStatItem();
            item.year = years[i];
            item.champion = champions[i];
            item.constructor = constructors[i];
            items.add(item);
        }

        return items;
    }

    /**
     * Class that contains String fields for each MotoGp winner: year, champion, and constructor.
     */
    private static final class MotoGpStatItem {
        String year;
        String champion;
        String constructor;
    }

    /**
     * {@code ListAdapter} used to hold the List of {@code MotoGpStatItem}'s for display in our
     * {@code ListView} and to for {@code PrintMotoGpAdapter} to use to supply information when it
     * is acting as a {@code PrintDocumentAdapter}
     */
    private class MotoGpStatAdapter extends BaseAdapter {
        /**
         * {@code List} of data Objects for MotoGp winners initialized in constructor
         */
        private final List<MotoGpStatItem> mItems;
        /**
         * {@code LayoutInflater} to use in {@code getView} override to inflate our xml layout file
         * R.layout.motogp_stat_item
         */
        private final LayoutInflater mInflater;

        /**
         * Constructor that initializes our fields {@code List<MotoGpStatItem> mItems}, and
         * {@code LayoutInflater mInflater} to our parameters {@code items} and {@code inflater}
         * respectively.
         *
         * @param items    {@code List} of {@code MotoGpStatItem} data items for MotoGp winners
         * @param inflater {@code LayoutInflater} to use in {@code getView} override
         */
        @SuppressWarnings("WeakerAccess")
        public MotoGpStatAdapter(List<MotoGpStatItem> items, LayoutInflater inflater) {
            mItems = items;
            mInflater = inflater;
        }

        /**
         * Returns a clone of the MotoGp winner data items in our field {@code List<MotoGpStatItem> mItems}.
         *
         * @return list containing the elements of our field {@code List<MotoGpStatItem> mItems}
         */
        @SuppressWarnings("WeakerAccess")
        public List<MotoGpStatItem> cloneItems() {
            return new ArrayList<>(mItems);
        }

        /**
         * How many items are in the data set represented by this Adapter.
         *
         * @return Count of items.
         */
        @Override
        public int getCount() {
            return mItems.size();
        }

        /**
         * Get the data item associated with the specified position in the data set.
         *
         * @param position Position of the item whose data we want within the adapter's data set.
         * @return The data at the specified position.
         */
        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        /**
         * Get the row id associated with the specified position in the list. Our row id is the same
         * as th position.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * Get a View that displays the data at the specified position in the data set. You can either
         * create a View manually or inflate it from an XML layout file. When the View is inflated, the
         * parent View (GridView, ListView...) will apply default layout parameters unless you use
         * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)}
         * to specify a root view and to prevent attachment to the root.
         * <p>
         * First we check to see if there is a {@code View convertView} we can reuse, and if not we
         * use our field {@code LayoutInflater mInflater} to set {@code convertView} to our inflated
         * layout file R.layout.motogp_stat_item, using {@code ViewGroup parent} for layout parameters
         * but not attaching it to that root. Then we fetch the {@code MotoGpStatItem item} at the
         * position {@code position}. We locate {@code TextView yearView} at ID R.id.year in our
         * layout {@code convertView} and set its text to the {@code year} field of {@code item},
         * locate {@code TextView championView} at ID R.id.champion in our layout {@code convertView}
         * and set its text to the {@code champion} field of {@code item}, and locate
         * {@code TextView constructorView} at ID R.id.constructor in our layout {@code convertView}
         * and set its text to the {@code year} field of {@code constructor}. Finally we return
         * {@code convertView} to the caller.
         *
         * @param position    The position of the item within the adapter's data set of the item whose view
         *                    we want.
         * @param convertView The old view to reuse, if possible. Note: You should check that this view
         *                    is non-null and of an appropriate type before using. If it is not possible to convert
         *                    this view to display the correct data, this method can create a new view.
         *                    Heterogeneous lists can specify their number of view types, so that this View is
         *                    always of the right type (see {@link #getViewTypeCount()} and
         *                    {@link #getItemViewType(int)}).
         * @param parent      The parent that this view will eventually be attached to
         * @return A View corresponding to the data at the specified position.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.motogp_stat_item, parent, false);
            }

            MotoGpStatItem item = (MotoGpStatItem) getItem(position);

            TextView yearView = (TextView) convertView.findViewById(R.id.year);
            yearView.setText(item.year);

            TextView championView = (TextView) convertView.findViewById(R.id.champion);
            championView.setText(item.champion);

            TextView constructorView = (TextView) convertView.findViewById(R.id.constructor);
            constructorView.setText(item.constructor);

            return convertView;
        }
    }

    /**
     * This is an implementation of a {@code PrintDocumentAdapter} which will print the contents of
     * the {@code MotoGpStatAdapter} Adapter populating our {@code ListView}.
     */
    private class PrintMotoGpAdapter extends PrintDocumentAdapter {
        /**
         * Width of page to be printed, it is the maximum width the printer will print
         */
        private int mRenderPageWidth;
        /**
         * Height of page to be printed, it is the maximum height the printer will print
         */
        private int mRenderPageHeight;

        /**
         * Copy of {@code PrintAttributes newAttributes} parameter to {@code onLayout} which we stash
         * for later use in background thread which does the actual printing.
         */
        private PrintAttributes mPrintAttributes;
        /**
         * {@code PrintDocumentInfo info} returned from AsyncTask launched by {@code onLayout}, used
         * in call to {@code LayoutResultCallback callback.onLayoutFinished}
         */
        private PrintDocumentInfo mDocumentInfo;
        /**
         * {@code Context} for resources at printer density created in {@code onLayout} using a
         * {@code Configuration} whose field {@code densityDpi} is set to a density calculated
         * for the maximum size for printer layout, and whose theme is set to the system theme
         * android.R.style.Theme_Holo_Light
         */
        private Context mPrintContext;

        /**
         * Called when the print attributes (page size, density, etc) changed
         * giving you a chance to layout the content such that it matches the
         * new constraints. This method is invoked on the main thread.
         * <p>
         * After you are done laying out, you <strong>must</strong> invoke: {@link
         * LayoutResultCallback#onLayoutFinished(PrintDocumentInfo, boolean)} with
         * the last argument <code>true</code> or <code>false</code> depending on
         * whether the layout changed the content or not, respectively; or {@link
         * LayoutResultCallback#onLayoutFailed(CharSequence)}, if an error occurred;
         * or {@link LayoutResultCallback#onLayoutCancelled()} if layout was
         * cancelled in a response to a cancellation request via the passed in
         * {@link CancellationSignal}. Note that you <strong>must</strong> call one of
         * the methods of the given callback for this method to be considered complete
         * which is you will not receive any calls to this adapter until the current
         * layout operation is complete by invoking a method on the callback instance.
         * The callback methods can be invoked from an arbitrary thread.
         * <p>
         * One of the arguments passed to this method is a {@link CancellationSignal}
         * which is used to propagate requests from the system to your application for
         * canceling the current layout operation. For example, a cancellation may be
         * requested if the user changes a print option that may affect layout while
         * you are performing a layout operation. In such a case the system will make
         * an attempt to cancel the current layout as another one will have to be performed.
         * Typically, you should register a cancellation callback in the cancellation
         * signal. The cancellation callback <strong>will not</strong> be made on the
         * main thread and can be registered as follows:
         * <p>
         * <pre>
         * cancellationSignal.setOnCancelListener(new OnCancelListener() {
         *     &#064;Override
         *     public void onCancel() {
         *         // Cancel layout
         *     }
         * });
         * </pre>
         * <p>
         * <strong>Note:</strong> If the content is large and a layout will be
         * performed, it is a good practice to schedule the work on a dedicated
         * thread and register an observer in the provided {@link
         * CancellationSignal} upon invocation of which you should stop the
         * layout.
         * <p>
         * First we check whether we have already been canceled by calling
         * {@code CancellationSignal cancellationSignal.isCanceled()}, and if so we just call the
         * callback {@code LayoutResultCallback callback.onLayoutCancelled()} and return having
         * done nothing.
         * <p>
         * Next we set a flag {@code boolean layoutNeeded} to false, assuming that we will not need
         * to do a layout after all. Next we compute the value {@code int density} based on the max
         * of the Horizontal DPI and Vertical DPI of our parameter {@code PrintAttributes newAttributes}.
         * We use this to calculate the {@code marginLeft}, {@code marginRight} and {@code contentWidth}
         * we are to use based on the Media size specified in {@code newAttributes}. If the value
         * stashed in our field {@code int mRenderPageWidth} is not equal to {@code contextWidth},
         * we set it to {@code contextWidth} and set our {@code layoutNeeded} flag to true. In a similar
         * way we calculate the Content height given {@code newAttributes} for {@code int contentHeight},
         * and if the value stashed in our field {@code int mRenderPageHeight} is not the same we set
         * {@code mRenderPageHeight} to {@code contentHeight} and set our {@code layoutNeeded} flag to
         * true.
         * <p>
         * Next if we have yet to set {@code Context mPrintContext}, or the DPI it uses is different
         * from the {@code density} we calculated above we create {@code Configuration configuration},
         * set its field {@code densityDpi} to {@code density}, set {@code mPrintContext} to a
         * {@code Context} created from {@code configuration} and set its theme to the system theme
         * android.R.style.Theme_Holo_Light.
         * <p>
         * Now if our flag {@code layoutNeeded} is still false, we call the callback
         * {@code callback.onLayoutFinished(mDocumentInfo, false)} and return.
         * <p>
         * Otherwise we have work to do, we clone the contents of our {@code ListAdapter} into
         * {@code List<MotoGpStatItem> items} so that a background thread can access it,
         * then launch an anonymous {@code MotoGpOnLayoutAsyncTask} which is an
         * {@code AsyncTask<Void, Void, PrintDocumentInfo>} to do all the work for us.
         *
         * @param oldAttributes      The old print attributes.
         * @param newAttributes      The new print attributes.
         * @param cancellationSignal Signal for observing cancel layout requests.
         * @param callback           Callback to inform the system for the layout result.
         * @param metadata           Additional information about how to layout the content. Unused
         * @see LayoutResultCallback
         * @see CancellationSignal
         * @see #EXTRA_PRINT_PREVIEW
         */
        @Override
        public void onLayout(final PrintAttributes oldAttributes,
                             final PrintAttributes newAttributes,
                             final CancellationSignal cancellationSignal,
                             final LayoutResultCallback callback,
                             final Bundle metadata) {

            // If we are already cancelled, don't do any work.
            if (cancellationSignal.isCanceled()) {
                callback.onLayoutCancelled();
                return;
            }

            // Now we determined if the print attributes changed in a way that
            // would change the layout and if so we will do a layout pass.
            boolean layoutNeeded = false;

            @SuppressWarnings("ConstantConditions")
            final int density = Math.max(newAttributes.getResolution().getHorizontalDpi(),
                    newAttributes.getResolution().getVerticalDpi());

            // Note that we are using the PrintedPdfDocument class which creates
            // a PDF generating canvas whose size is in points (1/72") not screen
            // pixels. Hence, this canvas is pretty small compared to the screen.
            // The recommended way is to layout the content in the desired size,
            // in this case as large as the printer can do, and set a translation
            // to the PDF canvas to shrink in. Note that PDF is a vector format
            // and you will not lose data during the transformation.

            // The content width is equal to the page width minus the margins times
            // the horizontal printer density. This way we get the maximal number
            // of pixels the printer can put horizontally.
            @SuppressWarnings("ConstantConditions")
            final int marginLeft = (int) (density * (float) newAttributes.getMinMargins()
                    .getLeftMils() / MILS_IN_INCH);
            final int marginRight = (int) (density * (float) newAttributes.getMinMargins()
                    .getRightMils() / MILS_IN_INCH);
            @SuppressWarnings("ConstantConditions")
            final int contentWidth = (int) (density * (float) newAttributes.getMediaSize()
                    .getWidthMils() / MILS_IN_INCH) - marginLeft - marginRight;
            if (mRenderPageWidth != contentWidth) {
                mRenderPageWidth = contentWidth;
                layoutNeeded = true;
            }

            // The content height is equal to the page height minus the margins times
            // the vertical printer resolution. This way we get the maximal number
            // of pixels the printer can put vertically.
            final int marginTop = (int) (density * (float) newAttributes.getMinMargins()
                    .getTopMils() / MILS_IN_INCH);
            final int marginBottom = (int) (density * (float) newAttributes.getMinMargins()
                    .getBottomMils() / MILS_IN_INCH);
            final int contentHeight = (int) (density * (float) newAttributes.getMediaSize()
                    .getHeightMils() / MILS_IN_INCH) - marginTop - marginBottom;
            if (mRenderPageHeight != contentHeight) {
                mRenderPageHeight = contentHeight;
                layoutNeeded = true;
            }

            // Create a context for resources at printer density. We will
            // be inflating views to render them and would like them to use
            // resources for a density the printer supports.
            if (mPrintContext == null || mPrintContext.getResources()
                    .getConfiguration().densityDpi != density) {
                Configuration configuration = new Configuration();
                configuration.densityDpi = density;
                mPrintContext = createConfigurationContext(
                        configuration);
                mPrintContext.setTheme(android.R.style.Theme_Holo_Light);
            }

            // If no layout is needed that we did a layout at least once and
            // the document info is not null, also the second argument is false
            // to notify the system that the content did not change. This is
            // important as if the system has some pages and the content didn't
            // change the system will ask, the application to write them again.
            if (!layoutNeeded) {
                callback.onLayoutFinished(mDocumentInfo, false);
                return;
            }

            // For demonstration purposes we will do the layout off the main
            // thread but for small content sizes like this one it is OK to do
            // that on the main thread.

            // Store the data as we will layout off the main thread.
            final List<MotoGpStatItem> items = ((MotoGpStatAdapter)
                    getListAdapter()).cloneItems();

            new MotoGpOnLayoutAsyncTask(cancellationSignal, newAttributes, items, callback)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
        }

        /**
         * Called when specific pages of the content should be written in the
         * form of a PDF file to the given file descriptor. This method is invoked
         * on the main thread.
         * <p>
         * After you are done writing, you should close the file descriptor and
         * invoke {@link WriteResultCallback#onWriteFinished(PageRange[])}, if writing
         * completed successfully; or {@link WriteResultCallback#onWriteFailed(
         *CharSequence)}, if an error occurred; or {@link WriteResultCallback#onWriteCancelled()},
         * if writing was cancelled in a response to a cancellation request via the passed
         * in {@link CancellationSignal}. Note that you <strong>must</strong> call one of
         * the methods of the given callback for this method to be considered complete which
         * is you will not receive any calls to this adapter until the current write
         * operation is complete by invoking a method on the callback instance. The callback
         * methods can be invoked from an arbitrary thread.
         * <p>
         * One of the arguments passed to this method is a {@link CancellationSignal}
         * which is used to propagate requests from the system to your application for
         * canceling the current write operation. For example, a cancellation may be
         * requested if the user changes a print option that may affect layout while
         * you are performing a write operation. In such a case the system will make
         * an attempt to cancel the current write as a layout will have to be performed
         * which then may be followed by a write. Typically, you should register a
         * cancellation callback in the cancellation signal. The cancellation callback
         * <strong>will not</strong> be made on the main thread and can be registered
         * as follows:
         * <p>
         * <pre>
         * cancellationSignal.setOnCancelListener(new OnCancelListener() {
         *     &#064;Override
         *     public void onCancel() {
         *         // Cancel write
         *     }
         * });
         * </pre>
         * <p>
         * <strong>Note:</strong> If the printed content is large, it is a good
         * practice to schedule writing it on a dedicated thread and register an
         * observer in the provided {@link CancellationSignal} upon invocation of
         * which you should stop writing.
         * <p>
         * First we check whether we have already been canceled by calling
         * {@code CancellationSignal cancellationSignal.isCanceled()}, and if so we just call the
         * callback {@code LayoutResultCallback callback.onWriteCancelled()} and return having
         * done nothing.
         * <p>
         * Otherwise we have work to do, we clone the contents of our {@code ListAdapter} into
         * {@code List<MotoGpStatItem> items} so that a background thread can access it,
         * then launch an anonymous {@code MotoGpOnWriteAsyncTask} which is an
         * {@code AsyncTask<Void, Void, Void>} to do all the work for us.
         *
         * @param pages              The pages whose content to print - non-overlapping in ascending order.
         * @param destination        The destination file descriptor to which to write.
         * @param cancellationSignal Signal for observing cancel writing requests.
         * @param callback           Callback to inform the system for the write result.
         * @see WriteResultCallback
         * @see CancellationSignal
         */
        @Override
        public void onWrite(final PageRange[] pages,
                            final ParcelFileDescriptor destination,
                            final CancellationSignal cancellationSignal,
                            final WriteResultCallback callback) {

            // If we are already cancelled, don't do any work.
            if (cancellationSignal.isCanceled()) {
                callback.onWriteCancelled();
                return;
            }

            // Store the data as we will layout off the main thread.
            final List<MotoGpStatItem> items = ((MotoGpStatAdapter)
                    getListAdapter()).cloneItems();

            new MotoGpOnWriteAsyncTask(cancellationSignal, items, pages, destination, callback)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
        }

        /**
         * Provides the printer page dimensions {@code mRenderPageWidth} and {@code mRenderPageHeight}
         * to the {@code View view} as constraint information for its width and height dimensions.
         * First we create an {@code int widthMeasureSpec} MeasureSpec integer, specifying the
         * requirements for this view to be EXACTLY {@code mRenderPageWidth}, with 0 padding, and
         * however big the child wants to be in the width dimension. Then we create an
         * {@code int heightMeasureSpec} MeasureSpec integer, specifying the  requirements for this
         * view to be EXACTLY {@code mRenderPageHeight}, with 0 padding, and however big the child
         * wants to be in the height dimension. Finally we pass these two values as constraints to
         * the {@code View view}'s measure method. This configures the {@code View} for later calls
         * to {@code getMeasuredHeight} by {@code onLayout}, and to render itself to the canvas it
         * is given by {@code onWrite} when the time comes. Note that we only use this to measure
         * all the item views fetched from {@code MotoGpStatAdapter}.
         *
         * @param view {@code View} that needs to determine its measurement based on the printer
         *             sizes {@code mRenderPageWidth} and {@code mRenderPageHeight}
         */
        private void measureView(View view) {
            final int widthMeasureSpec = ViewGroup.getChildMeasureSpec(
                    MeasureSpec.makeMeasureSpec(mRenderPageWidth, MeasureSpec.EXACTLY),
                    0,
                    view.getLayoutParams().width);
            final int heightMeasureSpec = ViewGroup.getChildMeasureSpec(
                    MeasureSpec.makeMeasureSpec(mRenderPageHeight, MeasureSpec.EXACTLY),
                    0,
                    view.getLayoutParams().height);
            view.measure(widthMeasureSpec, heightMeasureSpec);
        }

        /**
         * Used to calculate the {@code PageRange[]} array returned to {@code onWriteFinished}, it
         * parses all the page numbers printed that are adjacent in its parameter
         * {@code SparseIntArray writtenPages} into different {@code PageRange} structures which it
         * stores in {@code List<PageRange> pageRanges} one by one. When done with its input it
         * converts {@code List<PageRange> pageRanges} to {@code PageRange[] pageRangesArray} which
         * it returns to the caller.
         * <p>
         * To do this we first allocate an {@code ArrayList} for {@code List<PageRange> pageRanges},
         * allocate the {@code start} of PageRange variable, allocate the {@code end} of PageRange
         * variable, and initialize {@code int writtenPageCount} to the number of pages contained in
         * our parameter {@code SparseIntArray writtenPages}.
         * <p>
         * Now we loop through all the pages in {@code writtenPages} one by one using the index
         * {@code i}. We fetch the page number contained in the {@code i}'th entry in {@code writtenPages}
         * and use it to set {@code start}, {@code oldEnd} and {@code end}. Then while the next page
         * number fetched from {@code writtenPages} is less than 1 greater than the previous page
         * ({@code (end - oldEnd) <= 1}), we set {@code oldEnd} to {@code end}, fetch the next page
         * number from {@code writtenPages} to set {@code end}, and increment the index {@code i}.
         * We continue the while loop as long as the pages are adjacent, when there is a skip of a
         * page (or more) or we run out of pages ({@code i >= writtenPageCount}), we exit the while
         * loop, construct a {@code PageRange pageRange} that runs from {@code start} to {@code end}
         * and add it to {@code List<PageRange> pageRanges}.
         * <p>
         * Once all pages in {@code writtenPages} have been processed in this manner, we allocate
         * {@code PageRange[] pageRangesArray} to be the size of {@code pageRanges}, load the contents
         * of {@code pageRanges} into {@code pageRangesArray} and return {@code pageRangesArray} to
         * the caller.
         *
         * @param writtenPages pages that were printed stored in a {@code SparseIntArray}
         * @return Range of pages printed
         */
        private PageRange[] computeWrittenPageRanges(SparseIntArray writtenPages) {

            // List of {@code PageRange} structures we parse the {@code writtenPages} into.
            List<PageRange> pageRanges = new ArrayList<>();

            // Current start of the range of pages
            int start;

            // Current end of the range of pages
            int end;

            // Size of the {@code writtenPages} array, number of pages stored in it.
            final int writtenPageCount = writtenPages.size();

            for (int i = 0; i < writtenPageCount; i++) {
                start = writtenPages.valueAt(i);
                int oldEnd = end = start;
                while (i < writtenPageCount && (end - oldEnd) <= 1) {
                    oldEnd = end;
                    end = writtenPages.valueAt(i);
                    i++;
                }
                PageRange pageRange = new PageRange(start, end);
                pageRanges.add(pageRange);
            }

            PageRange[] pageRangesArray = new PageRange[pageRanges.size()];
            pageRanges.toArray(pageRangesArray);
            return pageRangesArray;
        }

        /**
         * Looks through {@code PageRange[] pageRanges} to see if the page {@code page} is included
         * somewhere in the page ranges. For each {@code PageRange pageRange} in {@code pageRanges}
         * we check to see if {@code pageRange} "contains" {@code page} between its start and end,
         * and if so we return true. If none of the {@code pageRanges} contain {@code page} we return
         * false.
         *
         * @param pageRanges array of {@code PageRange} objects to search
         * @param page       page number to search for
         * @return true if page is found, false if not
         */
        private boolean containsPage(PageRange[] pageRanges, int page) {
            for (PageRange pageRange : pageRanges) {
                if (pageRange.getStart() <= page
                        && pageRange.getEnd() >= page) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Background task to perform all the layouts required by the {@code onLayout} callback in
         * order to calculate a {@code PrintDocumentInfo} for the {@code PrintDocumentAdapter}.
         */
        private class MotoGpOnLayoutAsyncTask extends AsyncTask<Void, Void, PrintDocumentInfo> {
            /**
             * This is a copy of the {@code CancellationSignal} passed to {@code onLayout} which allows
             * us to be canceled by setting a {@code OnCancelListener} on it.
             */
            private final CancellationSignal cancellationSignal;
            /**
             * This is a copy of the {@code PrintAttributes} passed to {@code onLayout} which
             * represents the attributes of the print job. These attributes describe how the printed
             * content should be laid out.
             */
            private final PrintAttributes newAttributes;
            /**
             * List of {@code MotoGpStatItem}'s which we display in our ListView and want to print.
             */
            private final List<MotoGpStatItem> items;
            /**
             * Provides access to the three callbacks we need to return our result when we are done:
             * <ul>
             * <li>
             * {@code onLayoutFinished} - layout was successful, returns a {@code PrintDocumentInfo}
             * which contains the information our layout determined.
             * </li>
             * <li>
             * {@code onLayoutCancelled} - Notifies that layout was cancelled as a result of a
             * cancellation request.
             * </li>
             * <li>
             * {@code onLayoutFailed} - Notifies that an error occurred while laying out the
             * document, the argument is a {@code CharSequence} describing the error.
             * </li>
             * </ul>
             */
            private final LayoutResultCallback callback;

            /**
             * Constructor which initializes our fields with its parameters of the same name.
             *
             * @param cancellationSignal Signal for observing cancel layout requests passed to {@code onLayout}
             * @param newAttributes      The new print attributes passed to {@code onLayout}
             * @param items              A clone of the List of {@code MotoGpStatItem}'s displayed by our UI
             * @param callback           Callback to inform the system for the layout result passed to {@code onLayout}
             */
            MotoGpOnLayoutAsyncTask(CancellationSignal cancellationSignal,
                                    PrintAttributes newAttributes,
                                    List<MotoGpStatItem> items,
                                    LayoutResultCallback callback) {
                this.cancellationSignal = cancellationSignal;
                this.newAttributes = newAttributes;
                this.items = items;
                this.callback = callback;
            }

            /**
             * Runs on the UI thread before {@link #doInBackground}. First we set the {@code OnCancelListener}
             * of our field {@code CancellationSignal cancellationSignal} to an anonymous class which
             * calls {@code AsyncTask.cancel(true)} to cancel this {@code MotoGpOnLayoutAsyncTask}
             * when the print job signals that it has been canceled, and then we stash a copy of
             * our field {@code newAttributes} in our field {@code PrintAttributes mPrintAttributes}.
             * TODO: figure out if this stashing is necessary after our refactoring
             */
            @Override
            protected void onPreExecute() {
                // First register for cancellation requests.
                cancellationSignal.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {
                        cancel(true);
                    }
                });
                // Stash the attributes as we will need them for rendering.
                mPrintAttributes = newAttributes;
            }

            /**
             * This background thread does a trial layout of our document in order to determine how
             * many pages the document will be, which it returns wrapped in a {@code PrintDocumentInfo}
             * object. Wrapped in a try block, we initialize our variables and loop through every
             * items View that our {@code MotoGpStatAdapter} contains, inflate it, measure it, and
             * add that measurement to our running count of the size of the current page:
             * {@code pageContentHeight}. When {@code pageContentHeight} exceeds {@code mRenderPageHeight}
             * (the height of a printed page), we advance our page count {@code currentPage} and "place"
             * the last View laid out on a new page. When we are done measuring all the Views we build
             * a {@code PrintDocumentInfo info} containing the page count, we call the callback
             * {@code LayoutResultCallback.onLayoutFinished} with {@code info} and the flag for reporting
             * a layout change set to true. Finally we return {@code info} to the caller. If our try
             * block encounters an exception, we call the callback {@code LayoutResultCallback.onLayoutFailed}
             * and throw a runtime exception.
             *
             * @param params we do not use any parameters
             * @return information about our document for printing purposes contains the document name
             * "MotoGP_stats.pdf", the content type CONTENT_TYPE_DOCUMENT, and the total number of
             * pages in our document.
             */
            @SuppressWarnings("WrongThread")
            @Override
            protected PrintDocumentInfo doInBackground(Void... params) {
                try {
                    // Create an adapter with the stats and an inflater
                    // to load resources for the printer density.
                    LayoutInflater inflater = (LayoutInflater) mPrintContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    MotoGpStatAdapter adapter = new MotoGpStatAdapter(items, inflater);

                    int currentPage = 0; // Page count, which is advanced every time pageContentHeight > mRenderPageHeight
                    int pageContentHeight = 0; // Height of the current page being laid out
                    int viewType = -1; // view type of the last View processed
                    View view = null; // View containing current item that is being processed
                    // This is used to provide layout parameters when calling MotoGpStatAdapter.getView
                    LinearLayout dummyParent = new LinearLayout(mPrintContext);
                    dummyParent.setOrientation(LinearLayout.VERTICAL);

                    final int itemCount = adapter.getCount(); // Total number of items displayed by our MotoGpStatAdapter
                    for (int i = 0; i < itemCount; i++) {
                        // Be nice and respond to cancellation.
                        if (isCancelled()) {
                            return null;
                        }

                        // Get the next view.
                        final int nextViewType = adapter.getItemViewType(i);
                        if (viewType == nextViewType) {
                            view = adapter.getView(i, view, dummyParent);
                        } else {
                            view = adapter.getView(i, null, dummyParent);
                        }
                        viewType = nextViewType;

                        // Measure the next view
                        measureView(view);

                        // Add the height but if the view crosses the page
                        // boundary we will put it to the next page.
                        pageContentHeight += view.getMeasuredHeight();
                        if (pageContentHeight > mRenderPageHeight) {
                            pageContentHeight = view.getMeasuredHeight();
                            currentPage++;
                        }
                    }

                    // Create a document info describing the result.
                    PrintDocumentInfo info = new PrintDocumentInfo
                            .Builder("MotoGP_stats.pdf")
                            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                            .setPageCount(currentPage + 1)
                            .build();

                    // We completed the layout as a result of print attributes
                    // change. Hence, if we are here the content changed for
                    // sure which is why we pass true as the second argument.
                    callback.onLayoutFinished(info, true);
                    return info;
                } catch (Exception e) {
                    // An unexpected error, report that we failed and
                    // one may pass in a human readable localized text
                    // for what the error is if known.
                    callback.onLayoutFailed(null);
                    throw new RuntimeException(e);
                }
            }

            /**
             * Runs on the UI thread after {@link #doInBackground}. The specified result is the value
             * returned by {@link #doInBackground}. We simply cache the {@code PrintDocumentInfo} in
             * our field {@code PrintDocumentInfo mDocumentInfo} in order to send it over if the next
             * layout pass does not result in a content change.
             *
             * @param result the {@code PrintDocumentInfo} calculated by our override of {@code doInBackground}
             */
            @Override
            protected void onPostExecute(PrintDocumentInfo result) {
                // Update the cached info to send it over if the next
                // layout pass does not result in a content change.
                mDocumentInfo = result;
            }

            /**
             * Runs on the UI thread after {@link #cancel(boolean)} is invoked and
             * {@link #doInBackground(Object[])} has finished. We simply call the callback
             * {@code LayoutResultCallback.onLayoutCancelled}.
             *
             * @param result ignored
             */
            @Override
            protected void onCancelled(PrintDocumentInfo result) {
                // Task was cancelled, report that.
                callback.onLayoutCancelled();
            }
        }

        private class MotoGpOnWriteAsyncTask extends AsyncTask<Void, Void, Void> {
            private final SparseIntArray mWrittenPages;
            private final PrintedPdfDocument mPdfDocument;
            private final CancellationSignal cancellationSignal;
            private final List<MotoGpStatItem> items;
            private final PageRange[] pages;
            private final ParcelFileDescriptor destination;
            private final WriteResultCallback callback;

            @SuppressWarnings("WeakerAccess")
            public MotoGpOnWriteAsyncTask(CancellationSignal cancellationSignal,
                                          List<MotoGpStatItem> items,
                                          PageRange[] pages,
                                          ParcelFileDescriptor destination,
                                          WriteResultCallback callback) {
                this.cancellationSignal = cancellationSignal;
                this.items = items;
                this.pages = pages;
                this.destination = destination;
                this.callback = callback;
                mWrittenPages = new SparseIntArray();
                mPdfDocument = new PrintedPdfDocument(
                        PrintCustomContent.this, mPrintAttributes);
            }

            @Override
            protected void onPreExecute() {
                // First register for cancellation requests.
                cancellationSignal.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {
                        cancel(true);
                    }
                });
            }

            @SuppressWarnings("WrongThread")
            @Override
            protected Void doInBackground(Void... params) {
                // Go over all the pages and write only the requested ones.
                // Create an adapter with the stats and an inflater
                // to load resources for the printer density.
                MotoGpStatAdapter adapter = new MotoGpStatAdapter(items,
                        (LayoutInflater) mPrintContext.getSystemService(
                                Context.LAYOUT_INFLATER_SERVICE));

                int currentPage = -1;
                int pageContentHeight = 0;
                int viewType = -1;
                View view = null;
                Page page = null;
                LinearLayout dummyParent = new LinearLayout(mPrintContext);
                dummyParent.setOrientation(LinearLayout.VERTICAL);

                // The content is laid out and rendered in screen pixels with
                // the width and height of the paper size times the print
                // density but the PDF canvas size is in points which are 1/72",
                // so we will scale down the content.
                final float scale = Math.min(
                        (float) mPdfDocument.getPageContentRect().width()
                                / mRenderPageWidth,
                        (float) mPdfDocument.getPageContentRect().height()
                                / mRenderPageHeight);

                final int itemCount = adapter.getCount();
                for (int i = 0; i < itemCount; i++) {
                    // Be nice and respond to cancellation.
                    if (isCancelled()) {
                        return null;
                    }

                    // Get the next view.
                    final int nextViewType = adapter.getItemViewType(i);
                    if (viewType == nextViewType) {
                        view = adapter.getView(i, view, dummyParent);
                    } else {
                        view = adapter.getView(i, null, dummyParent);
                    }
                    viewType = nextViewType;

                    // Measure the next view
                    measureView(view);

                    // Add the height but if the view crosses the page
                    // boundary we will put it to the next one.
                    pageContentHeight += view.getMeasuredHeight();
                    if (currentPage < 0 || pageContentHeight > mRenderPageHeight) {
                        pageContentHeight = view.getMeasuredHeight();
                        currentPage++;
                        // Done with the current page - finish it.
                        if (page != null) {
                            mPdfDocument.finishPage(page);
                        }
                        // If the page is requested, render it.
                        if (containsPage(pages, currentPage)) {
                            //noinspection Range
                            page = mPdfDocument.startPage(currentPage);
                            page.getCanvas().scale(scale, scale);
                            // Keep track which pages are written.
                            mWrittenPages.append(mWrittenPages.size(), currentPage);
                        } else {
                            page = null;
                        }
                    }

                    // If the current view is on a requested page, render it.
                    if (page != null) {
                        // Layout an render the content.
                        view.layout(0, 0, view.getMeasuredWidth(),
                                view.getMeasuredHeight());
                        view.draw(page.getCanvas());
                        // Move the canvas for the next view.
                        page.getCanvas().translate(0, view.getHeight());
                    }
                }

                // Done with the last page.
                if (page != null) {
                    mPdfDocument.finishPage(page);
                }

                // Write the data and return success or failure.
                try {
                    mPdfDocument.writeTo(new FileOutputStream(
                            destination.getFileDescriptor()));
                    // Compute which page ranges were written based on
                    // the bookkeeping we maintained.
                    PageRange[] pageRanges = computeWrittenPageRanges(mWrittenPages);
                    callback.onWriteFinished(pageRanges);
                } catch (IOException ioe) {
                    callback.onWriteFailed(null);
                } finally {
                    mPdfDocument.close();
                }

                return null;
            }

            @Override
            protected void onCancelled(Void result) {
                // Task was cancelled, report that.
                callback.onWriteCancelled();
                mPdfDocument.close();
            }
        }
    }
}

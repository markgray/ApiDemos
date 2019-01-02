package com.example.android.apis.app;
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

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.print.PrintManager;
import androidx.print.PrintHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.ImageView;

import com.example.android.apis.R;

/**
 * This class demonstrates how to implement bitmap printing.
 * <p>
 * This activity shows an image and offers a print option in the overflow
 * menu. When the user chooses to print a helper class from the support
 * library is used to print the image.
 * </p>
 * Shows how to print a Bitmap. Uses ImageView.getDrawable()).getBitmap(); to
 * retrieve a bitmap of the drawable used in the screen, then to print it uses:
 * PrintHelper.printBitmap("Print Bitmap", bitmap);
 *
 * @see PrintManager
 * @see WebView
 */
public class PrintBitmap extends Activity {

    private ImageView mImageView;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.print_bitmap.
     * Finally we initialize our field {@code ImageView mImageView} by locating the view R.id.image
     * in our layout.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.print_bitmap);
        mImageView = (ImageView) findViewById(R.id.image);
    }

    /**
     * Initialize the contents of the Activity's standard options menu. First we call through to our
     * super's implementation of {@code onCreateOptionsMenu}, then we retrieve a {@code MenuInflater}
     * with this context and use it to inflate the menu hierarchy from our menu resource
     * R.menu.print_custom_content into our parameter <var>menu</var>. We return true so that the
     * menu will be shown.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.print_custom_content, menu);
        return true;
    }

    /**
     * This hook is called whenever an item in your options menu is selected. First we check to see
     * if the <var>item</var> selected has the ID R.id.menu_print and if so we call our method
     * {@code print()} then return true to consume the click here. Otherwise we return the result
     * returned by calling through to our super's implementation of {@code onOptionsItemSelected}.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
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
     * Prints the image displayed in our layout. First we retrieve a handle for the PrintHelper that
     * can be used to print images to {@code PrintHelper printHelper}. Then we select SCALE_MODE_FIT
     * for {@code printHelper} (image will be scaled but leave white space). We retrieve the drawable
     * from our {@code ImageView mImageView} and use the bitmap used by this drawable to set the variable
     * {@code Bitmap bitmap}. Finally we use {@code printHelper} to print {@code bitmap} using the
     * job name "Print Bitmap".
     */
    private void print() {
        // Get the print manager.
        PrintHelper printHelper = new PrintHelper(this);

        // Set the desired scale mode.
        printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);

        // Get the bitmap for the ImageView's drawable.
        Bitmap bitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();

        // Print the bitmap.
        printHelper.printBitmap("Print Bitmap", bitmap);
    }
}

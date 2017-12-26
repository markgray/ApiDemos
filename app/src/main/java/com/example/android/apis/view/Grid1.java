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

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.List;

//Need the following import to get access to the app resources, since this
//class is in a sub-package.
import com.example.android.apis.R;

/**
 * Shows how to use a GridView to display a grid of ImageView's created
 * from the app icons retrieved from the PackageManager
 */
public class Grid1 extends Activity {
    /**
     * The {@code GridView} in our layout with ID R.id.myGrid
     */
    GridView mGrid;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.grid_1. Next we
     * call our method {@code loadApps} to load {@code List<ResolveInfo> mApps} with data from the
     * {@code PackageManager} (all activities that can be performed for an intent with the action
     * MAIN, and category LAUNCHER). We initialize our field {@code GridView mGrid} by finding the
     * view with the ID R.id.myGrid, and set its adapter to a new instance of {@code AppsAdapter}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid_1);

        loadApps(); // do this in onResume?

        mGrid = (GridView) findViewById(R.id.myGrid);
        mGrid.setAdapter(new AppsAdapter());
    }

    /**
     * List of {@code ResolveInfo} objects for all activities that can be executed for an intent
     * with the action MAIN, and category LAUNCHER loaded from the {@code PackageManager} by our
     * method {@code loadApps}.
     */
    private List<ResolveInfo> mApps;

    /**
     * Loads {@code List<ResolveInfo> mApps} with a list of all activities that can be performed for
     * an intent with the action MAIN, and category LAUNCHER loaded using the {@code PackageManager}.
     * First we create {@code Intent mainIntent} with the action ACTION_MAIN, and add the category
     * CATEGORY_LAUNCHER. Then we retrieve a {@code PackageManager} instance and use it to retrieve
     * all activities that can be performed for intent {@code mainIntent} to initialize {@code mApps}.
     */
    private void loadApps() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        mApps = getPackageManager().queryIntentActivities(mainIntent, 0);
    }

    /**
     * Adapter that displays the icons in {@code List<ResolveInfo> mApps}.
     */
    @SuppressWarnings("WeakerAccess")
    public class AppsAdapter extends BaseAdapter {
        /**
         * Our constructor.
         */
        public AppsAdapter() {
        }

        /**
         * Get a View that displays the data at the specified position in the data set. First we
         * declare {@code ImageView i}, then if our parameter {@code convertView} is null we create
         * a new instance of {@code ImageView} for {@code i}, set its scale type to FIT_CENTER and
         * set its layout parameters to 50 pixels wide by 50 pixels high. If {@code convertView} is
         * not null we set {@code i} to it after casting it to an {@code ImageView}. We initialize
         * {@code ResolveInfo info} with the data in {@code mApps} at position {@code position} and
         * set {@code i} to a drawable of the icon associated with {@code info} that we retrieve by
         * using that {@code ResolveInfo} to call back a {@code PackageManager} instance to load the
         * icon from the application. Finally we return {@code i} to the caller.
         *
         * @param position    The position of the item within the adapter's data set whose view we want.
         * @param convertView The old view to reuse, if possible.
         * @param parent      The parent that this view will eventually be attached to
         * @return A View corresponding to the data at the specified position.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView i;

            if (convertView == null) {
                i = new ImageView(Grid1.this);
                i.setScaleType(ImageView.ScaleType.FIT_CENTER);
                i.setLayoutParams(new GridView.LayoutParams(50, 50));
            } else {
                i = (ImageView) convertView;
            }

            ResolveInfo info = mApps.get(position);
            i.setImageDrawable(info.activityInfo.loadIcon(getPackageManager()));

            return i;
        }

        /**
         * How many items are in the data set represented by this Adapter. We return the size of our
         * {@code List<ResolveInfo> mApps} field.
         *
         * @return Count of items.
         */
        @Override
        public final int getCount() {
            return mApps.size();
        }

        /**
         * Get the data item associated with the specified position in the data set. We return the
         * {@code ResolveInfo} at position {@code position} in {@code List<ResolveInfo> mApps}.
         *
         * @param position Position of the item whose data we want within the adapter's data set.
         * @return The data at the specified position.
         */
        @Override
        public final Object getItem(int position) {
            return mApps.get(position);
        }

        /**
         * Gets the row id associated with the specified position in the list, in our case the row
         * id is the same of our parameter {@code position} so we return it.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        @Override
        public final long getItemId(int position) {
            return position;
        }
    }

}

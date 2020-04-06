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

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.apis.R;

import java.util.List;

/**
 * Its layout file R.layout.layout_animation_5 uses an android:layoutAnimation attribute
 * which uses "@anim/layout_grid_inverse_fade" which in turn uses the attributes
 * android:directionPriority="row", and android:direction="right_to_left|bottom_to_top"
 * along with anim/fade.xml to fade in app icons in a grid from bottom right to top left
 * instead of top down order.
 */
public class LayoutAnimation5 extends AppCompatActivity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.layout_animation_5.
     * Next we call our method {@code loadApps} to load {@code List<ResolveInfo> mApps} with data
     * from the {@code PackageManager} (all activities that can be performed for an intent with the
     * action MAIN, and category LAUNCHER). We initialize our field {@code GridView mGrid} by finding the
     * view with the ID R.id.grid, and set its adapter to a new instance of {@code AppsAdapter}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_animation_5);

        loadApps();

        GridView grid = findViewById(R.id.grid);
        grid.setAdapter(new AppsAdapter());
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
    public class AppsAdapter extends BaseAdapter {
        /**
         * Get a View that displays the data at the specified position in the data set. First we
         * create a new instance for our variable {@code ImageView i}. We initialize our variable
         * {@code ResolveInfo info} with the data in {@code mApps} at position {@code position}
         * modulo the size of {@code mApps}, and set the content of {@code i} to a drawable of the
         * icon associated with {@code info} that we retrieve by using that {@code ResolveInfo} to
         * call back a {@code PackageManager} instance to load the icon from the application. We set
         * the scale type of {@code i} to FIT_CENTER. We set our variable {@code int w} to 36 times
         * the logical display density plus 0.5, and use it to set the layout parameters of {@code i}
         * to {@code w} by {@code w}. Finally we return {@code i} to the caller.
         *
         * @param position    The position of the item within the adapter's data set whose view we want.
         * @param convertView The old view to reuse, if possible.
         * @param parent      The parent that this view will eventually be attached to
         * @return A View corresponding to the data at the specified position.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView i = new ImageView(LayoutAnimation5.this);

            ResolveInfo info = mApps.get(position % mApps.size());

            i.setImageDrawable(info.activityInfo.loadIcon(getPackageManager()));
            i.setScaleType(ImageView.ScaleType.FIT_CENTER);
            final int w = (int) (36 * getResources().getDisplayMetrics().density + 0.5f);
            i.setLayoutParams(new GridView.LayoutParams(w, w));
            return i;
        }

        /**
         * How many items are in the data set represented by this Adapter. We return the minimum of
         * the size of {@code mApps} and 32.
         *
         * @return Count of items.
         */
        @Override
        public final int getCount() {
            return Math.min(32, mApps.size());
        }

        /**
         * Get the data item associated with the specified position in the data set. We return the
         * data at position {@code position} modulo the size of {@code mApps}
         *
         * @param position Position of the item within the adapter's data set that we want.
         * @return The data at the specified position.
         */
        @Override
        public final Object getItem(int position) {
            return mApps.get(position % mApps.size());
        }

        /**
         * Get the row id associated with the specified position in the list. Our row id is the same
         * as our parameter {@code position} so we just return that to the caller.
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

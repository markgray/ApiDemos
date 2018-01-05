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

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.AbsListView;

import java.util.ArrayList;

/**
 * A list view that demonstrates the use of setEmptyView. This example also uses
 * a custom layout file that adds some extra buttons to the screen.
 */
public class List8 extends ListActivity {
    /**
     * The custom {@code BaseAdapter} for our {@code ListView}
     */
    PhotoAdapter mAdapter;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.list_8. Then we
     * set the empty view of our {@code ListView} to the {@code TextView} in our layout with the
     * ID R.id.empty ("No photos"). We initialize our field {@code PhotoAdapter mAdapter} with a
     * new instance and set it as the list adapter of our {@code ListView}. We initialize our
     * variable {@code Button clear} by finding the view with ID R.id.clear, and set its
     * {@code OnClickListener} to an anonymous class which calls the {@code clearPhotos} method of
     * {@code mAdapter}. Finally we initialize our variable {@code Button add} by finding the view
     * with ID R.id.clear, and set its {@code OnClickListener} to an anonymous class which calls
     * the {@code addPhotos} method of {@code mAdapter}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Use a custom layout file
        setContentView(R.layout.list_8);

        // Tell the list view which view to display when the list is empty
        getListView().setEmptyView(findViewById(R.id.empty));

        // Set up our adapter
        mAdapter = new PhotoAdapter(this);
        setListAdapter(mAdapter);

        // Wire up the clear button to remove all photos
        Button clear = (Button) findViewById(R.id.clear);
        clear.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when the R.id.clear ("Clear photos") button is clicked, we just call the
             * {@code clearPhotos} method of {@code mAdapter}.
             *
             * @param v View that was clicked
             */
            @Override
            public void onClick(View v) {
                mAdapter.clearPhotos();
            }
        });

        // Wire up the add button to add a new photo
        Button add = (Button) findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when the R.id.add ("New photo") button is clicked, we just call the
             * {@code addPhotos} method of {@code mAdapter}.
             *
             * @param v View that was clicked
             */
            @Override
            public void onClick(View v) {
                mAdapter.addPhotos();
            }
        });
    }

    /**
     * A simple adapter which maintains an ArrayList of photo resource Ids.
     * Each photo is displayed as an image. This adapter supports clearing the
     * list of photos and adding a new photo.
     */
    @SuppressWarnings("WeakerAccess")
    public class PhotoAdapter extends BaseAdapter {
        /**
         * Resource IDs of the jpg photos we can add to our {@code ListView}
         */
        private Integer[] mPhotoPool = {
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1, R.drawable.sample_thumb_2,
                R.drawable.sample_thumb_3, R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7};

        /**
         * List of photos we are currently displaying in our {@code ListView}
         */
        private ArrayList<Integer> mPhotos = new ArrayList<>();

        /**
         * {@code Context} to use to construct views (set to its parameter by our constructor).
         */
        private Context mContext;

        /**
         * Our constructor, we just store our parameter {@code Context c} in our field
         * {@code Context mContext}.
         *
         * @param c {@code Context} to use to construct views (this in the {@code onCreate} method
         *          of {@code List8}
         */
        public PhotoAdapter(Context c) {
            mContext = c;
        }

        /**
         * How many items are in the data set represented by this Adapter. We just return the current
         * size of our field {@code ArrayList<Integer> mPhotos}.
         *
         * @return Count of items.
         */
        @Override
        public int getCount() {
            return mPhotos.size();
        }

        /**
         * Get the data item associated with the specified position in the data set. Since the data
         * comes from an {@code ArrayList<Integer>}, just returning the index is sufficient to get
         * at the data. If we were using a more complex data structure, we would return whatever
         * object represents one row in the list.
         *
         * @param position Position of the item whose data we want within the adapter's data set.
         * @return The data at the specified position.
         */
        @Override
        public Object getItem(int position) {
            return position;
        }

        /**
         * Get the row id associated with the specified position in the list. Use the
         * {@code ArrayList<Integer>} index as a unique id.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * Get a View that displays the data at the specified position in the data set. First we
         * initialize our variable {@code ImageView i} with a new instance. We decode an load
         * {@code i} with the jpg whose resource ID is at {@code position} within our field
         * {@code ArrayList<Integer> mPhotos}, enable {@code i} to adjust its bounds to preserve
         * the aspect ratio of its drawable, set its layout parameters to a new instance of
         * {@code LayoutParams} specifying WRAP_CONTENT for both width and height, and set its
         * background to the png with resource ID R.drawable.picture_frame. Finally we return
         * {@code i} to the caller.
         *
         * @param position    The position of the item within the adapter's data set whose view we want.
         * @param convertView The old view to reuse, if possible. We do not bother.
         * @param parent      The parent that this view will eventually be attached to
         * @return A View corresponding to the data at the specified position.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Make an ImageView to show a photo
            ImageView i = new ImageView(mContext);

            i.setImageResource(mPhotos.get(position));
            i.setAdjustViewBounds(true);
            i.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
            // Give it a nice background
            i.setBackgroundResource(R.drawable.picture_frame);
            return i;
        }

        /**
         * Clears all photos from our field {@code ArrayList<Integer> mPhotos}, called from the
         * {@code OnClickListener} of the button with ID R.id.clear ("Clear photos"). We call the
         * {@code clear} method of {@code mPhotos} to clear it, and notify the observers  attached
         * to our custom {@code BaseAdapter} that the underlying data has been changed and any View
         * reflecting the data set should refresh itself.
         */
        public void clearPhotos() {
            mPhotos.clear();
            notifyDataSetChanged();
        }

        /**
         * Adds a new photo to our field {@code ArrayList<Integer> mPhotos}, called from the
         * {@code OnClickListener} of the button with ID R.id.add ("New photo"). We generate a
         * random index into our pool our jpg resource IDs {@code Integer[] mPhotoPool} for
         * {@code int whichPhoto}, initialize {@code int newPhoto} with the resource ID we find
         * at {@code mPhotoPool[whichPhoto]}, add it to our field {@code ArrayList<Integer> mPhotos}
         * and notify the observers  attached to our custom {@code BaseAdapter} that the underlying
         * data has been changed and any View reflecting the data set should refresh itself.
         */
        public void addPhotos() {
            int whichPhoto = (int) Math.round(Math.random() * (mPhotoPool.length - 1));
            int newPhoto = mPhotoPool[whichPhoto];
            mPhotos.add(newPhoto);
            notifyDataSetChanged();
        }

    }
}

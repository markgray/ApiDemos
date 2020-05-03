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

import android.app.ListActivity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import com.example.android.apis.R
import java.util.ArrayList
import kotlin.math.roundToInt

/**
 * A list view that demonstrates the use of `setEmptyView`. This example also uses
 * a custom layout file that adds some extra buttons to the screen.
 */
class List8 : ListActivity() {
    /**
     * The custom [BaseAdapter] for our `ListView`
     */
    var mAdapter: PhotoAdapter? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.list_8. Then we
     * set the empty view of our `ListView` to the `TextView` in our layout with the ID R.id.empty
     * ("No photos"). We initialize our [PhotoAdapter] field [mAdapter] with a new instance and set
     * it as the list adapter of our `ListView`. We initialize our [Button] variable `val clear` by
     * finding the view with ID R.id.clear, and set its `OnClickListener` to an a lambda which calls
     * the `clearPhotos` method of [mAdapter]. Finally we initialize our [Button] variable `val add`
     * by finding the view with ID R.id.clear, and set its `OnClickListener` to an a lambda which
     * calls the `addPhotos` method of [mAdapter].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use a custom layout file
        setContentView(R.layout.list_8)

        // Tell the list view which view to display when the list is empty
        listView.emptyView = findViewById(R.id.empty)

        // Set up our adapter
        mAdapter = PhotoAdapter(this)
        listAdapter = mAdapter

        // Wire up the clear button to remove all photos
        val clear = findViewById<Button>(R.id.clear)
        clear.setOnClickListener { mAdapter!!.clearPhotos() }

        // Wire up the add button to add a new photo
        val add = findViewById<Button>(R.id.add)
        add.setOnClickListener { mAdapter!!.addPhotos() }
    }

    /**
     * A simple adapter which maintains an ArrayList of photo resource Ids.
     * Each photo is displayed as an image. This adapter supports clearing the
     * list of photos and adding a new photo.
     */
    inner class PhotoAdapter
    /**
     * Our constructor, we just store our parameter in our [Context] field [mContext].
     *
     * @param mContext [Context] to use to construct views (this in the `onCreate` method
     * of [List8]
     */
    (
            /**
             * [Context] to use to construct views (set to its parameter by our constructor).
             */
            private val mContext: Context

    ) : BaseAdapter() {

        /**
         * Resource IDs of the jpg photos we can add to our `ListView`
         */
        private val mPhotoPool = arrayOf(
                R.drawable.sample_thumb_0, R.drawable.sample_thumb_1, R.drawable.sample_thumb_2,
                R.drawable.sample_thumb_3, R.drawable.sample_thumb_4, R.drawable.sample_thumb_5,
                R.drawable.sample_thumb_6, R.drawable.sample_thumb_7)

        /**
         * List of photos we are currently displaying in our `ListView`
         */
        private val mPhotos = ArrayList<Int>()

        /**
         * How many items are in the data set represented by this Adapter. We just return the
         * current size of our `ArrayList<Integer>` field [mPhotos].
         *
         * @return Count of items.
         */
        override fun getCount(): Int {
            return mPhotos.size
        }

        /**
         * Get the data item associated with the specified position in the data set. Since the data
         * comes from an `ArrayList<Integer>`, just returning the index is sufficient to get
         * at the data. If we were using a more complex data structure, we would return whatever
         * object represents one row in the list.
         *
         * @param position Position of the item whose data we want within the adapter's data set.
         * @return The data at the specified position.
         */
        override fun getItem(position: Int): Any {
            return position
        }

        /**
         * Get the row id associated with the specified position in the list. We use the
         * `ArrayList<Integer>` index as a unique id.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        /**
         * Get a View that displays the data at the specified position in the data set. First we
         * initialize our [ImageView] variable `val i` with a new instance. We decode and load
         * `i` with the jpg whose resource ID is at [position] within our `ArrayList<Integer>`
         * field [mPhotos], enable `i` to adjust its bounds to preserve the aspect ratio of its
         * drawable, set its layout parameters to a new instance of [AbsListView.LayoutParams]
         * specifying WRAP_CONTENT for both width and height, and set its background to the png
         * with resource ID R.drawable.picture_frame. Finally we return `i` to the caller.
         *
         * @param position    The position of the item within the adapter's data set whose view we want.
         * @param convertView The old view to reuse, if possible. We do not bother.
         * @param parent      The parent that this view will eventually be attached to
         * @return A View corresponding to the data at the specified position.
         */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            // Make an ImageView to show a photo
            val i = ImageView(mContext)
            i.setImageResource(mPhotos[position])
            i.adjustViewBounds = true
            i.layoutParams = AbsListView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            // Give it a nice background
            i.setBackgroundResource(R.drawable.picture_frame)
            return i
        }

        /**
         * Clears all photos from our `ArrayList<Integer>` field [mPhotos], called from the
         * `OnClickListener` of the button with ID R.id.clear ("Clear photos"). We call the
         * `clear` method of [mPhotos] to clear it, and notify the observers  attached to our
         * custom [BaseAdapter] that the underlying data has been changed and any [View]
         * reflecting the data set should refresh itself.
         */
        fun clearPhotos() {
            mPhotos.clear()
            notifyDataSetChanged()
        }

        /**
         * Adds a new photo to our `ArrayList<Integer>` field [mPhotos], called from the
         * `OnClickListener` of the button with ID R.id.add ("New photo"). We generate a random
         * index into our pool of jpg resource IDs in the [Int] array field [mPhotoPool] for
         * `val whichPhoto`, initialize `val newPhoto` with the resource ID we find at the
         * `whichPhoto` index in [mPhotoPool], add it to our `ArrayList<Integer>` field [mPhotos]
         * and notify the observers  attached to our custom [BaseAdapter] that the underlying data
         * has been changed and any [View] reflecting the data set should refresh itself.
         */
        fun addPhotos() {
            val whichPhoto = (Math.random() * (mPhotoPool.size - 1)).roundToInt()
            val newPhoto = mPhotoPool[whichPhoto]
            mPhotos.add(newPhoto)
            notifyDataSetChanged()
        }

    }
}
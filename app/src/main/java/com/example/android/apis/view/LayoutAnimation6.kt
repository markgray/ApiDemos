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

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Its layout file R.layout.layout_animation_6 uses an android:layoutAnimation attribute
 * specifying "@anim/layout_wave_scale" as the animation, which in turn uses an
 * android:animation attribute to specify @anim/wave_scale as its animation. This
 * animation animates alpha from 0.0 to 1.0 with a duration of 100, and two fancy scaling
 * animations. The results are: 1. Fade in app icons, 2. Scale them in from .5 size, to
 * 1.5 size, 3. Scale back from 1.5 to 1.0 size.
 */
class LayoutAnimation6 : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.layout_animation_6.
     * Next we call our method `loadApps` to load `List<ResolveInfo> mApps` with data
     * from the `PackageManager` (all activities that can be performed for an intent with the
     * action MAIN, and category LAUNCHER). We initialize our field `GridView mGrid` by finding the
     * view with the ID R.id.grid, and set its adapter to a new instance of `AppsAdapter`.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_animation_6)
        loadApps()
        val grid = findViewById<GridView>(R.id.grid)
        grid.adapter = AppsAdapter()
    }

    /**
     * List of `ResolveInfo` objects for all activities that can be executed for an intent
     * with the action MAIN, and category LAUNCHER loaded from the `PackageManager` by our
     * method `loadApps`.
     */
    private var mApps: List<ResolveInfo>? = null

    /**
     * Loads `List<ResolveInfo> mApps` with a list of all activities that can be performed for
     * an intent with the action MAIN, and category LAUNCHER loaded using the `PackageManager`.
     * First we create `Intent mainIntent` with the action ACTION_MAIN, and add the category
     * CATEGORY_LAUNCHER. Then we retrieve a `PackageManager` instance and use it to retrieve
     * all activities that can be performed for intent `mainIntent` to initialize `mApps`.
     */
    private fun loadApps() {
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        mApps = packageManager.queryIntentActivities(mainIntent, 0)
    }

    /**
     * Adapter that displays the icons in `List<ResolveInfo> mApps`.
     */
    inner class AppsAdapter : BaseAdapter() {
        /**
         * Get a View that displays the data at the specified position in the data set. First we
         * create a new instance for our variable `ImageView i`. We initialize our variable
         * `ResolveInfo info` with the data in `mApps` at position `position`
         * modulo the size of `mApps`, and set the content of `i` to a drawable of the
         * icon associated with `info` that we retrieve by using that `ResolveInfo` to
         * call back a `PackageManager` instance to load the icon from the application. We set
         * the scale type of `i` to FIT_CENTER. We set our variable `int w` to 36 times
         * the logical display density plus 0.5, and use it to set the layout parameters of `i`
         * to `w` by `w`. Finally we return `i` to the caller.
         *
         * @param position    The position of the item within the adapter's data set whose view we want.
         * @param convertView The old view to reuse, if possible.
         * @param parent      The parent that this view will eventually be attached to
         * @return A View corresponding to the data at the specified position.
         */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val i = ImageView(this@LayoutAnimation6)
            val info = mApps!![position % mApps!!.size]
            i.setImageDrawable(info.activityInfo.loadIcon(packageManager))
            i.scaleType = ImageView.ScaleType.FIT_CENTER
            val w = (36 * resources.displayMetrics.density + 0.5f).toInt()
            i.layoutParams = AbsListView.LayoutParams(w, w)
            return i
        }

        /**
         * How many items are in the data set represented by this Adapter. We return the minimum of
         * the size of `mApps` and 32.
         *
         * @return Count of items.
         */
        override fun getCount(): Int {
            return mApps!!.size.coerceAtMost(32)
        }

        /**
         * Get the data item associated with the specified position in the data set. We return the
         * data at position `position` modulo the size of `mApps`
         *
         * @param position Position of the item within the adapter's data set that we want.
         * @return The data at the specified position.
         */
        override fun getItem(position: Int): Any {
            return mApps!![position % mApps!!.size]
        }

        /**
         * Get the row id associated with the specified position in the list. Our row id is the same
         * as our parameter `position` so we just return that to the caller.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }
    }
}
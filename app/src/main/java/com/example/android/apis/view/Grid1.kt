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

/**
 * Need the following import to get access to the app resources,
 * since this class is in a sub-package.
 */
import com.example.android.apis.R

/**
 * Shows how to use a [GridView] to display a grid of [ImageView]'s created
 * from the app icons retrieved from the `PackageManager`
 */
@Suppress("MemberVisibilityCanBePrivate")
class Grid1 : AppCompatActivity() {
    /**
     * The [GridView] in our layout with ID R.id.myGrid
     */
    var mGrid: GridView? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.grid_1. Next we
     * call our method [loadApps] to load `List<ResolveInfo>` field [mApps] with data from the
     * `PackageManager` (all activities that can be performed for an intent with the action
     * MAIN, and category LAUNCHER). We initialize our [GridView] field [mGrid] by finding the
     * view with the ID R.id.myGrid, and set its adapter to a new instance of [AppsAdapter].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.grid_1)
        loadApps() // do this in onResume?
        mGrid = findViewById(R.id.myGrid)
        mGrid!!.adapter = AppsAdapter()
    }

    /**
     * List of [ResolveInfo] objects for all activities that can be executed for an intent
     * with the action MAIN, and category LAUNCHER loaded from the `PackageManager` by our
     * method [loadApps].
     */
    private var mApps: List<ResolveInfo>? = null

    /**
     * Loads `List<ResolveInfo>` field [mApps] with a list of all activities that can be performed
     * for an intent with the action MAIN, and category LAUNCHER loaded using the `PackageManager`.
     * First we create [Intent] variable `val mainIntent` with the action ACTION_MAIN, and add the
     * category CATEGORY_LAUNCHER. Then we retrieve a `PackageManager` instance and use it to
     * retrieve all activities that can be performed for intent `mainIntent` to initialize [mApps].
     */
    private fun loadApps() {
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        mApps = packageManager.queryIntentActivities(mainIntent, 0)
    }

    /**
     * Adapter that displays the icons in `List<ResolveInfo>` field [mApps].
     */
    inner class AppsAdapter
    /**
     * Our constructor.
     */
        : BaseAdapter() {

        /**
         * Logical density of the display
         */
        private val dp2px: Float = resources.displayMetrics.density

        /**
         * Get a View that displays the data at the specified position in the data set. First we
         * declare [ImageView] `val i`, then if our [View] parameter [convertView] is null we
         * create a new instance of [ImageView] for `i`, set its scale type to FIT_CENTER and
         * set its layout parameters to 50 pixels wide by 50 pixels high. If [convertView] is
         * not null we set `i` to it after casting it to an [ImageView]. We initialize
         * [ResolveInfo] `val info` with the data in [mApps] at position [position] and
         * set `i` to a drawable of the icon associated with `info` that we retrieve by
         * using that [ResolveInfo] to call back a `PackageManager` instance to load the
         * icon from the application. Finally we return `i` to the caller.
         *
         * @param position    The position of the item within the adapter's data set whose view we want.
         * @param convertView The old [View] to reuse, if possible.
         * @param parent      The parent that this view will eventually be attached to
         * @return A [View] corresponding to the data at the specified position.
         */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val i: ImageView
            if (convertView == null) {
                i = ImageView(this@Grid1)
                i.scaleType = ImageView.ScaleType.FIT_CENTER
                val scaled50 = (50f*dp2px).toInt()
                i.layoutParams = AbsListView.LayoutParams(scaled50, scaled50)
            } else {
                i = convertView as ImageView
            }
            val info = mApps!![position]
            i.setImageDrawable(info.activityInfo.loadIcon(packageManager))
            return i
        }

        /**
         * How many items are in the data set represented by this Adapter. We return the size of our
         * `List<ResolveInfo>` field [mApps].
         *
         * @return Count of items.
         */
        override fun getCount(): Int {
            return mApps!!.size
        }

        /**
         * Get the data item associated with the specified position in the data set. We return the
         * [ResolveInfo] at position [position] in `List<ResolveInfo>` field [mApps].
         *
         * @param position Position of the item whose data we want within the adapter's data set.
         * @return The data at the specified position.
         */
        override fun getItem(position: Int): Any {
            return mApps!![position]
        }

        /**
         * Gets the row id associated with the specified position in the list, in our case the row
         * id is the same of our parameter [position] so we return it.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }
    }
}
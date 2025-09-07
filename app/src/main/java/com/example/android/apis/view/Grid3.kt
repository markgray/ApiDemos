/*
 * Copyright (C) 2010 The Android Open Source Project
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

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Build
import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.Checkable
import android.widget.FrameLayout
import android.widget.GridView
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.android.apis.R

/**
 * This demo illustrates the use of CHOICE_MODE_MULTIPLE_MODAL, a.k.a. selection mode on [GridView].
 * Implements multi-selection mode on [GridView]
 */
@SuppressLint("ObsoleteSdkInt")
@RequiresApi(Build.VERSION_CODES.HONEYCOMB)
class Grid3 : AppCompatActivity() {
    /**
     * Our layout's [GridView], with ID R.id.myGrid.
     */
    var mGrid: GridView? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.grid_1. Next we call our
     * method [loadApps] to load our `List<ResolveInfo>` field  [mApps] with [ResolveInfo] objects
     * for accessing information about all of the apps that the `PackageManager` knows about. We
     * initialize [GridView] field [mGrid] by finding the view with ID R.id.myGrid, set its adapter
     * to a new instance of [AppsAdapter], set its choice mode to CHOICE_MODE_MULTIPLE_MODAL, and
     * set its [MultiChoiceModeListener] to a new instance of our class [MultiChoiceModeListener].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.grid_1)
        loadApps()
        mGrid = findViewById(R.id.myGrid)
        mGrid!!.adapter = AppsAdapter()
        mGrid!!.choiceMode = GridView.CHOICE_MODE_MULTIPLE_MODAL
        mGrid!!.setMultiChoiceModeListener(MultiChoiceModeListener())
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
     * First we create [Intent] `val mainIntent` with the action ACTION_MAIN, and add the category
     * CATEGORY_LAUNCHER. Then we retrieve a `PackageManager` instance and use it to retrieve
     * all activities that can be performed for intent `mainIntent` to initialize [mApps].
     */
    private fun loadApps() {
        val mainIntent = Intent(/* action = */ Intent.ACTION_MAIN, /* uri = */ null)
        mainIntent.addCategory(/* category = */ Intent.CATEGORY_LAUNCHER)
        mApps = packageManager.queryIntentActivities(/* intent = */ mainIntent, /* flags = */ 0)
    }

    /**
     * Our adapter which fetches app icons from the [ResolveInfo] for each of the applications
     * in our `List<ResolveInfo>` field [mApps].
     */
    inner class AppsAdapter : BaseAdapter() {
        /**
         * Logical density of the display
         */
        private val dp2px: Float = resources.displayMetrics.density

        /**
         * Width of an icon in pixels (50*dp2px)
         */
        private val w: Int = (50 * dp2px).toInt()

        /**
         * Height of an icon in pixels (50*dp2px)
         */
        private val h: Int = (50 * dp2px).toInt()

        /**
         * Get a View that displays the data at the specified position in the data set. First we
         * declare [CheckableLayout] variable `val l` and [ImageView] variable `val i`. Then if our
         * parameter [convertView] is null we create a new instance of [ImageView] for `i`, set its
         * scale type to FIT_CENTER and set its layout parameters to `w` pixels wide by `h` pixels
         * high (these are both 50dp scaled by the logical density of our display). We create a new
         * instance of [CheckableLayout] for `l`, and set its layout parameters to WRAP_CONTENT for
         * both width and height. We then add the view `i` to `l`. If [convertView] is not null we
         * set `l` to it after casting it to a [CheckableLayout], and set `i` to the child of `l` at
         * position 0. We initialize [ResolveInfo] variable `val info` with the data in [mApps] at
         * position [position] and set `i` to a drawable of the icon associated with `info` that we
         * retrieve by using that [ResolveInfo] to call back a `PackageManager` instance to load the
         * icon from the application. Finally we return `l` to the caller.
         *
         * @param position    The position of the item within the adapter's data set whose view we want.
         * @param convertView The old [View] to reuse, if possible.
         * @param parent      The parent that this view will eventually be attached to
         * @return A [View] corresponding to the data at the specified position.
         */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val l: CheckableLayout
            val i: ImageView
            if (convertView == null) {
                i = ImageView(this@Grid3)
                i.scaleType = ImageView.ScaleType.FIT_CENTER
                i.layoutParams = ViewGroup.LayoutParams(/* width = */ w, /* height = */ h)
                l = CheckableLayout(context = this@Grid3)
                l.layoutParams = AbsListView.LayoutParams(
                    /* w = */ AbsListView.LayoutParams.WRAP_CONTENT,
                    /* h = */ AbsListView.LayoutParams.WRAP_CONTENT
                )
                l.addView(/* child = */ i)
            } else {
                l = convertView as CheckableLayout
                i = l.getChildAt(/* index = */ 0) as ImageView
            }
            val info: ResolveInfo = mApps!![position]
            i.setImageDrawable(info.activityInfo.loadIcon(/* pm = */ packageManager))
            return l
        }

        /**
         * How many items are in the data set represented by this Adapter. We return the size of
         * `List<ResolveInfo>` field [mApps],
         *
         * @return Count of items.
         */
        override fun getCount(): Int {
            return mApps!!.size
        }

        /**
         * Get the data item associated with the specified position in the data set. We return the
         * data at position [position] in `List<ResolveInfo>` field [mApps].
         *
         * @param position Position of the item whose data we want within the adapter's data set.
         * @return The data at the specified position.
         */
        override fun getItem(position: Int): Any {
            return mApps!![position]
        }

        /**
         * Get the row id associated with the specified position in the list. Our row id is the same
         * as our parameter [position], so we just return that.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }
    }

    /**
     * View group which holds our [ImageView], and allows it to be checkable.
     *
     * @param context [Context] to use to access resources
     */
    inner class CheckableLayout(context: Context?) : FrameLayout(context!!), Checkable {
        /**
         * Flag to indicate whether we are checked or not.
         */
        private var mChecked = false

        /**
         * Change the checked state of the view. We save our parameter [checked] in our field
         * [mChecked], and set our background to a slightly translucent blue (0x770000ff) if
         * we are checked, or to null if we are not checked.
         *
         * @param checked The new checked state
         */
        override fun setChecked(checked: Boolean) {
            mChecked = checked
            background = if (checked) {
                ResourcesCompat.getDrawable(
                    /* res = */ resources,
                    /* id = */ R.drawable.blue,
                    /* theme = */ null
                )
            } else {
                null
            }
        }

        /**
         * Returns the current checked state of the view, which is the value of our field [mChecked].
         *
         * @return The current checked state of the view
         */
        override fun isChecked(): Boolean {
            return mChecked
        }

        /**
         * Change the checked state of the view to the inverse of its current state. We just call
         * our method [setChecked] with the negated value of our field [mChecked] (kotlin prefers
         * to call our method a property with the name [isChecked] - go figure).
         */
        override fun toggle() {
            isChecked = !mChecked
        }
    }

    /**
     * Our custom [AbsListView.MultiChoiceModeListener], customized to just display the number
     * of items selected in the action mode.
     */
    inner class MultiChoiceModeListener : AbsListView.MultiChoiceModeListener {
        /**
         * Called when action mode is first created. We set the title of our [ActionMode] parameter
         * [mode] to the string "Select Items", and the subtitle to the string "One item selected",
         * then return true to the caller.
         *
         * @param mode [ActionMode] being created
         * @param menu [Menu] used to populate action buttons
         * @return true if the action mode should be created, false if entering this mode should
         * be aborted.
         */
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.title = "Select Items"
            mode.subtitle = "One item selected"
            return true
        }

        /**
         * Called to refresh an action mode's action menu whenever it is invalidated. We just return
         * true to the caller.
         *
         * @param mode [ActionMode] being prepared
         * @param menu [Menu] used to populate action buttons
         * @return true if the menu or action mode was updated, false otherwise.
         */
        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return true
        }

        /**
         * Called to report a user click on an action button. We just return true to the caller.
         *
         * @param mode The current [ActionMode]
         * @param item The [MenuItem] that was clicked
         * @return true if this callback handled the event, false if the standard [MenuItem]
         * invocation should continue.
         */
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return true
        }

        /**
         * Called when an action mode is about to be exited and destroyed. We ignore it.
         *
         * @param mode The current [ActionMode] being destroyed
         */
        override fun onDestroyActionMode(mode: ActionMode) {}

        /**
         * Called when an item is checked or unchecked during selection mode. We initialize our
         * variable `val selectCount` with the number of items currently selected in our field
         * [GridView] field [mGrid], then switch on it:
         *
         *  * 1: we set the subtitle of [ActionMode] parameter [mode] to the string "One item
         *  selected" then break
         *
         *  * default: we set the subtitle of [ActionMode] parameter [mode] to the string formed by
         *  prepending the string " items selected" with the string value of `selectCount`,
         *  then break.
         *
         * @param mode     The `ActionMode` providing the selection mode
         * @param position Adapter position of the item that was checked or unchecked
         * @param id       Adapter ID of the item that was checked or unchecked
         * @param checked  true if the item is now checked, false if the item is now unchecked.
         */
        override fun onItemCheckedStateChanged(
            mode: ActionMode,
            position: Int,
            id: Long,
            checked: Boolean
        ) {
            when (val selectCount: Int = mGrid!!.checkedItemCount) {
                1 -> mode.subtitle = "One item selected"
                else -> mode.subtitle = "$selectCount items selected"
            }
        }
    }
}
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
package com.example.android.apis.app

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import com.example.android.apis.R

/**
 * This demonstrates idiomatic usage of the Action Bar. The default Honeycomb theme
 * includes the action bar by default and a menu resource is used to populate the
 * menu data itself. If you'd like to see how these things work under the hood, see
 * [ActionBarMechanics].
 * RequiresApi(Build.VERSION_CODES.HONEYCOMB)
 */
@Suppress("MemberVisibilityCanBePrivate")
class ActionBarUsage : AppCompatActivity(), OnQueryTextListener {
    /**
     * The [TextView] we use as our content view, we set its text in our [onQueryTextChange]
     * to the new content of the query text, which happens for every key stroke.
     */
    internal lateinit var mSearchText: TextView

    /**
     * Menu item id of the last sort submenu item selected, -1 means none have been selected yet,
     * R.id.action_sort_size for "By size", or R.id.ic_menu_sort_alphabetically for "Alphabetically".
     */
    internal var mSortMode = -1

    /**
     * A reference to our activity's [ActionBar] for setting its display options.
     */
    internal var mActionBar: ActionBar? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation
     * of `onCreate`, then we set our [TextView] field [mSearchText] to a new instance of
     * [TextView] and set our content view to this [TextView]. Then we initialize our
     * [ActionBar] field [mActionBar] to a reference to the [ActionBar] and use it to disable the
     * [ActionBar.DISPLAY_SHOW_TITLE] display option.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSearchText = TextView(this)
        setContentView(mSearchText)
        mActionBar = supportActionBar
        mActionBar!!.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE)
    }

    /**
     * Initialize the contents of the Activity's standard options menu. You should place your
     * menu items in the menu passed as a parameter. First we fetch a `MenuInflater` for this
     * context to initialize our variable `val inflater`, and use it to inflate our menu
     * (R.menu.actions) into our [Menu] parameter [menu]. We initialize our [MenuItem] variable
     * `val item` by finding the menu item with the id R.id.action_search, and fetch the currently
     * set action view for this menu item to initialize our [SearchView] variable `val searchView`.
     * We then set the [OnQueryTextListener] of `searchView` to *this* (our Activity implements the
     * [OnQueryTextListener] interface). Finally we return *true* so that the menu is displayed.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.i(TAG, "onCreateOptionsMenu has been called")
        val inflater = menuInflater
        inflater.inflate(R.menu.actions, menu)
        val item = menu.findItem(R.id.action_search)
        val searchView = item.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        return true
    }

    /**
     * Prepare the Screen's standard options menu to be displayed. This is called right before
     * the menu is shown, every time it is shown. You can use this method to efficiently
     * enable/disable items or otherwise dynamically modify the contents. Our [Int] field [mSortMode]
     * starts out as -1 and has its value set differently only when one of the R.id.action_sort
     * submenu items have been clicked -- which causes our method [onSort] to be called due to
     * their use of the android:onClick="onSort" attribute. [onSort] will set [mSortMode]
     * according to which item in the submenu is selected (defaulting to R.id.action_sort_size
     * until one is selected), and finally [onSort] calls [invalidateOptionsMenu] which causes this
     * callback to be called in order to change the icon of R.id.action_sort to whichever sort mode
     * has been selected (the icon is actually displayed only when there is room in the [ActionBar]).
     * Finally this callback returns the return value from our super's implementation of
     * `onPrepareOptionsMenu` (which is assumed to be true).
     *
     * @param menu The options menu as last shown or first initialized by onCreateOptionsMenu().
     * @return You must return true for the menu to be displayed; if you return false it will not be shown.
     */
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (mSortMode != -1) {
            Log.i(TAG, "mSortMode =$mSortMode")
            val icon = menu.findItem(mSortMode).icon
            menu.findItem(R.id.action_sort).icon = icon
        }
        return super.onPrepareOptionsMenu(menu)
    }

    /**
     * This hook is called whenever an item in your options menu is selected. We simply toast a
     * message showing the current title of the item which was selected, then return true so that
     * the item selection is considered to have been consumed.
     *
     * @param item The menu item that was selected
     * @return boolean Return false to allow normal menu processing to proceed, true to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Toast.makeText(this, "Selected Item: " + item.title, Toast.LENGTH_SHORT).show()
        return true
    }

    // This method is specified as an onClick handler in the menu xml and will
    // take precedence over the Activity's onOptionsItemSelected method.
    // See res/menu/actions.xml for more info.
    /**
     * The R.id.action_sort menu item has been clicked showing the submenu for the sort item, and
     * one of the items (R.id.action_sort_size or R.id.action_sort_alpha) has been chosen. If the
     * submenu was dismissed without choosing this method is not called. We set the [Int] field
     * [mSortMode] to the id of the menu item selected, and invalidate the options menu so that
     * [onCreateOptionsMenu] and [onPrepareOptionsMenu] will be called in order to update the menu
     * accordingly.
     *
     * @param item The menu item that was selected.
     */
    fun onSort(item: MenuItem) {
        mSortMode = item.itemId
        // Request a call to onPrepareOptionsMenu so we can change the sort icon
        invalidateOptionsMenu()
    }

    // The following two callbacks are called for the SearchView.OnQueryChangeListener
    // For more about using SearchView, see src/.../view/SearchView1.java and SearchView2.java

    /**
     * Called when the query text is changed by the user. We produce a String containing the text
     * the user has entered and set the text of our [TextView] field [mSearchText] (the content View
     * of the Activity which was created in [onCreate]) to this String, or the empty String if no
     * search String has been entered. We are called with the empty String when the menu is created
     * and when the search string has been cleared as well as each time a character is added.
     *
     * @param newText the new content of the query text field.
     * @return *false* if the [SearchView] should perform the default action of showing any
     * suggestions if available, *true* if the action was handled by the listener.
     */
    override fun onQueryTextChange(newText: String): Boolean {
        var myNewText = newText
        Log.i(TAG, "onQueryTextChange has been called")
        myNewText = if (myNewText.isEmpty()) "" else "Query so far: $myNewText"
        mSearchText.text = myNewText
        return true
    }

    /**
     * Called when the user submits the query. This could be due to a key press on the
     * keyboard or due to pressing a submit button. We simply toast the contents of query
     * submitted and return true to indicate we have handled it.
     *
     * @param query the query text that is to be submitted
     * @return *true* if the query has been handled by the listener, *false* to let the
     * [SearchView] perform the default action.
     */
    override fun onQueryTextSubmit(query: String): Boolean {
        Toast.makeText(this, "Searching for: $query...", Toast.LENGTH_SHORT).show()
        return true
    }

    companion object {
        /**
         * TAG used for logging.
         */
        private const val TAG = "ActionBarUsage"
    }
}

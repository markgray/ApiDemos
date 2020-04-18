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
import android.annotation.TargetApi
import android.app.SearchManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.example.android.apis.R

/**
 * This demonstrates the usage of [SearchView] in an ActionBar as a menu item. It sets
 * a `SearchableInfo` on the [SearchView] for suggestions and submitting queries to. In
 * AndroidManifest.xml a `<meta-data>` element android:name="android.app.default_searchable"
 * android:value=".app.SearchQueryResults" sets the Activity to handle search requests.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
open class SearchViewActionBar : AppCompatActivity(), SearchView.OnQueryTextListener {
    /**
     * [SearchView] in our options menu with ID R.id.action_search, and with the action view
     * set by android:actionViewClass="android.widget.SearchView"
     */
    private var mSearchView: SearchView? = null

    /**
     * [TextView] in our layout with ID R.id.status_text, used to display query text and query
     * results.
     */
    private var mStatusView: TextView? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we request the window feature FEATURE_ACTION_BAR (enabling the Action
     * Bar), and then we set our content view to our layout file R.layout.searchview_actionbar.
     * Finally we initialize our [TextView] field [mStatusView] by finding the view with the ID
     * R.id.status_text.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_ACTION_BAR)
        setContentView(R.layout.searchview_actionbar)
        mStatusView = findViewById(R.id.status_text)
    }

    /**
     * Initialize the contents of the Activity's standard options menu. First we call through to our
     * super's implementation of `onCreateOptionsMenu`, then we initialize our `MenuInflater` variable
     * `val inflater` by fetching a `MenuInflater` for this context. We use `inflater` to inflate our
     * menu layout R.menu.searchview_in_menu into our [Menu] parameter [menu], initialize `MenuItem`
     * variable `val searchItem` by finding the item with the ID R.id.action_search, and then
     * initialize our [SearchView] field [mSearchView] by retrieving the currently set action view
     * for `MenuItem` variable `val searchItem`. Then we call our [setupSearchView] method with
     * `searchItem` to configure `searchItem` to behave as we wish it to. Finally we return true to
     * the caller so that the menu will be displayed.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        val inflater = menuInflater
        inflater.inflate(R.menu.searchview_in_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        mSearchView = searchItem.actionView as SearchView
        setupSearchView(searchItem)
        return true
    }

    /**
     * Configures its [MenuItem] parameter [searchItem] as well as configuring our [SearchView] field
     * [mSearchView] to act as a [SearchView]. First we call our method [isAlwaysExpanded] (which
     * always returns false) and based on its return value (false) we set the show as action flags
     * SHOW_AS_ACTION_IF_ROOM and SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW of [MenuItem] parameter
     * [searchItem] (these flags request that this item be shown as a button in the Action Bar if
     * the system decides there is room for it, and has the item's action view collapse to a normal
     * menu item respectively). We initialize our [SearchManager] variable `val searchManager` with
     * a handle to a system level service SEARCH_SERVICE instance (provides access to the system
     * search services). If `searchManager` is not null we initialize our `List<SearchableInfo>`
     * variable `val searchables` with a list of the searchable activities that can be included in
     * global search. Then we initialize `SearchableInfo` variable `var info` with the information
     * about a searchable activity with the complete component name of this application.
     *
     * Next we loop through all the `SearchableInfo` ``inf` in the list `searchables` and if the
     * search suggestions authority of `inf` is not null, and its search suggestion content
     * provider authority starts with the string "applications" (there are none on my pixel) we set
     * `info` to `inf` (does not happen!). After looping through all the global `SearchableInfo`
     * objects we set the `SearchableInfo` for `mSearchView` to `info` (properties in the
     * `SearchableInfo` are used to display labels, hints, suggestions, create intents for launching
     * search results screens and controlling other affordances such as a voice button).
     *
     * Finally we set the `OnQueryTextListener` of [mSearchView] to "this".
     *
     * @param searchItem `MenuItem` in our action bar menu that is to be configured.
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private fun setupSearchView(searchItem: MenuItem) {
        if (isAlwaysExpanded()) {
            mSearchView!!.setIconifiedByDefault(false)
            searchItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        } else {
            searchItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM
                    or MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
        }
        val searchManager: SearchManager? = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        if (searchManager != null) {
            val searchables = searchManager.searchablesInGlobalSearch

            // Try to use the "applications" global search provider
            val componentName = componentName
            var info = searchManager.getSearchableInfo(componentName)
            for (inf in searchables) {
                val suggestAuthority = inf.suggestAuthority
                if (inf.suggestAuthority != null
                        && suggestAuthority.startsWith("applications")) {
                    info = inf
                }
            }
            mSearchView!!.setSearchableInfo(info)
        }
        mSearchView!!.setOnQueryTextListener(this)
    }

    /**
     * Called when the query text is changed by the user. We set the text of our [TextView] field
     * [mStatusView] to a string formed by concatenating the string "Query = " with our [String]
     * parameter [newText], and return false to the caller.
     *
     * @param newText the new content of the query text field.
     * @return false if the SearchView should perform the default action of showing any
     * suggestions if available, true if the action was handled by the listener.
     */
    @SuppressLint("SetTextI18n")
    override fun onQueryTextChange(newText: String): Boolean {
        mStatusView!!.text = "Query = $newText"
        return false
    }

    /**
     * Called when the user submits the query. We set the text of our [TextView] field [mStatusView]
     * to a string formed by concatenating the string "Query = " with our [String] parameter [query]
     * followed by the string " : submitted", and return false to the caller.
     *
     * @param query the query text that is to be submitted
     * @return true if the query has been handled by the listener, false to let the
     * SearchView perform the default action.
     */
    @SuppressLint("SetTextI18n")
    override fun onQueryTextSubmit(query: String): Boolean {
        mStatusView!!.text = "Query = $query : submitted"
        return false
    }

    /**
     * Override this to have your subclass determine whether our search view is always expanded (by
     * returning true instead of false like we do).
     *
     * @return true to have the search menu item always expanded, false to have iconified.
     */
    open fun isAlwaysExpanded(): Boolean {
        return false
    }
}
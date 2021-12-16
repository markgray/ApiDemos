/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.app.Activity
import android.app.AlertDialog
import android.app.SearchManager
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * This activity shows a few different ways to invoke search, and inserts context-specific data for
 * use by the search activity. The search activity is defined in AndroidManifest.xml using a meta-data
 * element added to our `<activity>` parent component which has the attributes:
 *
 *  * android:name="android.app.default_searchable"
 *  * android:value=".app.SearchQueryResults"
 *
 * This specifies `.app.SearchQueryResults` to be our searchable activity (it performs searches).
 * The definition of android.app.default_searchable is more typically handled at the application
 * level, where it can serve as a default for all of your activities.
 */
class SearchInvoke : AppCompatActivity() {
    // UI elements
    /**
     * Button used to start search - Without a keyboard, you need to press
     * this to get a soft keyboard to use.
     */
    private var mStartSearch: Button? = null

    /**
     * Spinner used to select between "Search Key", "Menu Item", "Type-To-Search" or "Disabled"
     */
    private var mMenuMode: Spinner? = null

    /**
     * Used to enter text to prefill the search
     */
    private var mQueryPrefill: EditText? = null

    /**
     * context specific data to include in a Bundle under the key "demo_key"
     * it will be returned in search Intents
     */
    private var mQueryAppData: EditText? = null

    /**
     * Called when the activity is created. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.search_invoke.
     * We locate the [Button] with ID R.id.btn_start_search to initialize our field [mStartSearch],
     * the [Spinner] with ID R.id.spinner_menu_mode to initialize our field [mMenuMode], the
     * [EditText] with ID R.id.txt_query_prefill to initialize our field [mQueryPrefill], and the
     * [EditText] with ID R.id.txt_query_appdata to initialize our field [mQueryAppData]. Then we
     * create from the "string-array" R.array.search_menuModes an `ArrayAdapter<CharSequence>` for
     * `val adapter` using the system layout android.R.layout.simple_spinner_dropdown_item, set the
     * layout resource to create the drop down views to android.R.layout.simple_spinner_dropdown_item
     * and set the `SpinnerAdapter` of our [Spinner] field [mMenuMode] to `adapter`. We set the
     * `OnItemSelectedListener` of [mMenuMode] to an anonymous class which selects the default key
     * handling for this activity based on the Spinner item selected to DEFAULT_KEYS_SEARCH_LOCAL
     * if MENUMODE_TYPE_TO_SEARCH is selected, and DEFAULT_KEYS_DISABLE otherwise (keyboard required
     * to tell the difference). Finally we set the `OnClickListener` of our [Button] field
     * [mStartSearch] to a lambda which calls our override of the method `onSearchRequested` when the
     * [Button] is clicked.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /**
         * Inflate our UI from its XML layout description.
         */
        setContentView(R.layout.search_invoke)
        /**
         * Get display items for later interaction
         */
        mStartSearch = findViewById(R.id.btn_start_search)
        mMenuMode = findViewById(R.id.spinner_menu_mode)
        mQueryPrefill = findViewById(R.id.txt_query_prefill)
        mQueryAppData = findViewById(R.id.txt_query_appdata)
        /**
         * Populate items
         */
        val adapter = ArrayAdapter.createFromResource(
            this, R.array.search_menuModes, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mMenuMode!!.adapter = adapter
        /**
         * Create listener for the menu mode dropdown. We use this to demonstrate control
         * of the default keys handler in every Activity. More typically, you will simply set
         * the default key mode in your activity's onCreate() handler.
         */
        mMenuMode!!.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == MENUMODE_TYPE_TO_SEARCH) {
                    setDefaultKeyMode(Activity.DEFAULT_KEYS_SEARCH_LOCAL)
                } else {
                    setDefaultKeyMode(Activity.DEFAULT_KEYS_DISABLE)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                setDefaultKeyMode(Activity.DEFAULT_KEYS_DISABLE)
            }
        }
        /**
         * Attach actions to buttons
         */
        mStartSearch!!.setOnClickListener { onSearchRequested() }
    }

    /**
     * Called when your activity's options menu needs to be updated. First we call through to our
     * super's implementation of `onPrepareOptionsMenu`. Then we declare a [MenuItem] `var item`.
     * Then we remove the two [MenuItem]'s we may have added in a previous call. Next we
     * switch based on the position of the currently selected item of our `Spinner mMenuMode`:
     *
     *  * MENUMODE_SEARCH_KEY - we add the [MenuItem] "(Search Key)" as item ID 0
     *  * MENUMODE_MENU_ITEM - we add the [MenuItem] "Search" as item ID 0, and set its alphabetic
     *  shortcut to MENU_KEY
     *  * MENUMODE_TYPE_TO_SEARCH - we add the [MenuItem] "(Type-To-Search)" as item ID 0
     *  * MENUMODE_DISABLED - we add the [MenuItem] "(Disabled)" as item ID 0
     *
     * In all cases we then add the [MenuItem] "Clear History" as item ID 1 to `Menu menu` and
     * return true to the caller.
     *
     * @param menu The options menu as last shown or first initialized by [onCreateOptionsMenu].
     * @return *true* so the menu will be displayed
     */
    @Suppress("UNUSED_VALUE")
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        var item: MenuItem
        /**
         * first, get rid of our menus (if any)
         */
        menu.removeItem(0)
        menu.removeItem(1)
        when (mMenuMode!!.selectedItemPosition) {
            MENUMODE_SEARCH_KEY -> item = menu.add(Menu.NONE, 0, Menu.NONE, "(Search Key)")
            MENUMODE_MENU_ITEM -> {
                item = menu.add(Menu.NONE, 0, Menu.NONE, "Search")
                item.alphabeticShortcut = SearchManager.MENU_KEY
            }
            MENUMODE_TYPE_TO_SEARCH -> item = menu.add(Menu.NONE, 0, Menu.NONE, "(Type-To-Search)")
            MENUMODE_DISABLED -> item = menu.add(Menu.NONE, 0, Menu.NONE, "(Disabled)")
        }
        item = menu.add(Menu.NONE, 1, Menu.NONE, "Clear History")
        return true
    }

    /**
     * This hook is called whenever an item in our options menu is selected. First we switch based on
     * the item ID that was selected, if it is item 1 we then call our method [clearSearchHistory]
     * to clear the suggestions that are provided by our provider [SearchSuggestionSampleProvider]
     * via the [SearchQueryResults] search activity. If it is item 0, we then switch based on
     * the selection of [Spinner] field [mMenuMode]:
     *
     *  * MENUMODE_SEARCH_KEY - we pop up a dialog instructing the user to dismiss the dialog and
     *  use the search key
     *  * MENUMODE_MENU_ITEM - immediately calls our override of `onSearchRequested`
     *  * MENUMODE_TYPE_TO_SEARCH - we pop up a dialog instructing the user to dismiss the dialog
     *  and start typing
     *  * MENUMODE_DISABLED - we pop up a dialog instructing the user that they have disabled search
     *
     * After either of these menu items we return the value returned by our super's implementation
     * of `onOptionsItemSelected` to the caller.
     *
     * @param item The menu item that was selected.
     * @return *true* to consume the item here.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> when (mMenuMode!!.selectedItemPosition) {
                MENUMODE_SEARCH_KEY -> AlertDialog.Builder(this)
                    .setMessage("To invoke search, dismiss this dialog and press the search key" +
                        " (F5 on the simulator).")
                    .setPositiveButton("OK", null)
                    .show()
                MENUMODE_MENU_ITEM -> onSearchRequested()
                MENUMODE_TYPE_TO_SEARCH -> AlertDialog.Builder(this)
                    .setMessage("To invoke search, dismiss this dialog and start typing.")
                    .setPositiveButton("OK", null)
                    .show()
                MENUMODE_DISABLED -> AlertDialog.Builder(this)
                    .setMessage("You have disabled search.")
                    .setPositiveButton("OK", null)
                    .show()
            }
            1 -> clearSearchHistory()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * This hook is called when the user signals the desire to start a search. By overriding this
     * hook we can insert local or context-specific data. First we check to see if MENUMODE_DISABLED
     * is selected in our `Spinner mMenuMode`, and if so we return *false* having done nothing.
     * (*false* is an indication to the caller that our activity blocks search). Next we fetch the
     * contents of our [EditText] field [mQueryPrefill] to initialize our [String] variable
     * `val queryPrefill` (the String the user has typed in to prefill the search box). Next we
     * declare a [Bundle] `var appDataBundle` setting it to *null*, fetch the contents of [EditText]
     * field [mQueryAppData] to initialize the [String] `val queryAppDataString` (the [String] the
     * user has entered to be used for context-specific search data), and if it is not *null* we
     * allocate a new [Bundle] for `appDataBundle` and store `queryAppDataString` in it using the
     * key "demo_key". Next we call the [Activity] member function [startSearch] using `queryPrefill`
     * as the initial query, the flag *false* so that the query is not preselected, `appDataBundle`
     * as the application-specific context, and *false* to flag that it should only launch the
     * search that has been specifically defined by the application. Finally we return *true* to
     * indicate to the caller that a search has been launched.
     *
     * @return Returns *true* if search launched, *false* if activity blocks it
     */
    override fun onSearchRequested(): Boolean {
        /**
         * If your application absolutely must disable search, do it here.
         */
        if (mMenuMode!!.selectedItemPosition == MENUMODE_DISABLED) {
            return false
        }
        /**
         * It's possible to pre-fill the query string before launching the search
         * UI.  For this demo, we simply copy it from the user input field.
         * For most applications, you can simply pass null to [startSearch] to
         * open the UI with an empty query string.
         */
        val queryPrefill = mQueryPrefill!!.text.toString()

        /**
         * Next, set up a bundle to send context-specific search data (if any)
         * The bundle can contain any number of elements, using any number of keys;
         * For this Api Demo we copy a string from the user input field, and store
         * it in the bundle as a string with the key "demo_key".
         * For most applications, you can simply pass *null* to [startSearch].
         */
        var appDataBundle: Bundle? = null
        val queryAppDataString = mQueryAppData!!.text.toString()
        @Suppress("SENSELESS_COMPARISON")
        if (queryAppDataString != null) {
            appDataBundle = Bundle()
            appDataBundle.putString("demo_key", queryAppDataString)
        }
        /**
         * Now call the Activity member function that invokes the Search Manager UI.
         */
        startSearch(queryPrefill, false, appDataBundle, false)
        /**
         * Returning *true* indicates that we did launch the search, instead of blocking it.
         */
        return true
    }

    /**
     * Any application that implements search suggestions based on previous actions (such as
     * recent queries, page/items viewed, etc.) should provide a way for the user to clear the
     * history.  This gives the user a measure of privacy, if they do not wish for their recent
     * searches to be replayed by other users of the device (via suggestions).
     *
     * This example shows how to clear the search history for apps that use
     * android.provider.SearchRecentSuggestions.  If you have developed a custom suggestions
     * provider, you'll need to provide a similar API for clearing history.
     *
     * In this sample app we call this method from a "Clear History" menu item.  You could also
     * implement the UI in your preferences, or any other logical place in your UI.
     *
     * First we create an instance of [SearchRecentSuggestions] for our variable `val suggestions`
     * configured to use [SearchSuggestionSampleProvider.AUTHORITY]
     * ("com.example.android.apis.SuggestionProvider"),
     * and [SearchSuggestionSampleProvider.MODE] (DATABASE_MODE_QUERIES - configures the database
     * to record recent queries - required). Then we instruct `suggestions` to perform a clear history
     * operation.
     */
    private fun clearSearchHistory() {
        val suggestions = SearchRecentSuggestions(this,
            SearchSuggestionSampleProvider.AUTHORITY, SearchSuggestionSampleProvider.MODE)
        suggestions.clearHistory()
    }

    /**
     * Our static constants
     */
    companion object {
        /**
         * Menu mode spinner choices. This list must match the "search_menuModes" list found in
         * ApiDemos/res/values/arrays.xml
         */
        /**
         * "Search Key" Uses the search key to launch searches (Needs keyboard)
         */
        const val MENUMODE_SEARCH_KEY = 0

        /**
         * "Menu Item" Uses the menu item itself to launch searches
         */
        const val MENUMODE_MENU_ITEM = 1

        /**
         * "Type-To-Search" unhandled keystrokes will start an application-defined search
         * (needs keyboard)
         */
        const val MENUMODE_TYPE_TO_SEARCH = 2

        /**
         * "Disabled" Search is disabled
         */
        const val MENUMODE_DISABLED = 3
    }
}
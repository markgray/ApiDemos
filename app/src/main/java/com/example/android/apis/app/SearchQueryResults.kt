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

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * This activity represents the "search" activity in your application, in which search results are
 * gathered and displayed. It is specified as so in AndroidManifest.xml using an intent-filter
 * element containing the sub-elements `<action>` android:name="android.intent.action.SEARCH",
 * and `<category>` android:name="android.intent.category.DEFAULT", as well as a meta-data
 * element with attributes android:name="android.app.searchable" and android:resource="@xml/searchable".
 * xml/searchable is a search configuration file with a `<searchable>` root element.
 * Note: that although this is intended to only be called by `SearchInvoke` it also can be
 * launched using the "App/Search/Query Search Results" path of the `ApiDemos` app.
 */
@Suppress("MemberVisibilityCanBePrivate")
class SearchQueryResults : AppCompatActivity() {
    // UI elements
    /**
     * [TextView] we will display our query text in
     */
    var mQueryText: TextView? = null
    /**
     * [TextView] we will display our application-specific context (if any)
     */
    var mAppDataText: TextView? = null
    /**
     * [TextView] we will use to inform user whether we were started by a search or by launching.
     */
    var mDeliveredByText: TextView? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.search_query_results.
     * We initialize our [TextView] field [mQueryText] to the `View` with ID R.id.txt_query in our
     * layout file, our [TextView] field [mAppDataText] to the `View` with ID R.id.txt_appdata, and
     * our [TextView] field [mDeliveredByText] to the `View` with ID R.id.txt_deliveredby. Then we
     * fetch the [Intent] that started this activity to initialize our [Intent] varible
     * `val queryIntent`, retrieve from `queryIntent` the general action to be performed to set our
     * [String] variable `val queryAction` and we check to see if `queryAction` is ACTION_SEARCH,
     * and if so, we handle it by calling our method [doSearchQuery], otherwise we just set the text
     * of [mDeliveredByText] to "onCreate(), but no ACTION_SEARCH intent".
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    @SuppressLint("SetTextI18n")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate our UI from its XML layout description.
        setContentView(R.layout.search_query_results)
        // Get active display items for later updates
        mQueryText = findViewById(R.id.txt_query)
        mAppDataText = findViewById(R.id.txt_appdata)
        mDeliveredByText = findViewById(R.id.txt_deliveredby)
        // get and process search query here
        val queryIntent = intent
        val queryAction = queryIntent.action
        if (Intent.ACTION_SEARCH == queryAction) {
            doSearchQuery(queryIntent, "onCreate()")
        } else {
            mDeliveredByText!!.text = "onCreate(), but no ACTION_SEARCH intent"
        }
    }

    /**
     * Called when new [Intent] is delivered. First we call through to our super's implementation of
     * `onNewIntent`. Then we fetch the [Intent] that started this activity to set our [Intent]
     * variable `val queryIntent`, retrieve the general action to be performed to set our [String]
     * variable `val queryAction` and we check to see if `queryAction` is ACTION_SEARCH, and if so,
     * we handle it by calling our method [doSearchQuery], otherwise we just set the text of
     * [mDeliveredByText] to "onNewIntent(), but no ACTION_SEARCH intent".
     *
     * @param newIntent The [Intent] used to restart this activity
     */
    @SuppressLint("SetTextI18n")
    public override fun onNewIntent(newIntent: Intent) {
        super.onNewIntent(newIntent)
        // get and process search query here
        val queryIntent = intent
        val queryAction = queryIntent.action
        if (Intent.ACTION_SEARCH == queryAction) {
            doSearchQuery(queryIntent, "onNewIntent()")
        } else {
            mDeliveredByText!!.text = "onNewIntent(), but no ACTION_SEARCH intent"
        }
    }

    /**
     * Generic search handler. In a "real" application, you would use the query string to select
     * results from your data source, and present a list of those results to the user. First we
     * retrieve the [String] which is stored in our [Intent] parameter [queryIntent] as extended
     * data using the key SearchManager.QUERY to initialize our [String] variable  `val queryString`,
     * and then use `queryString` to set the text of our [TextView] field [mQueryText]. We create
     * an instance of [SearchRecentSuggestions] to initialize our variable `val suggestions` which
     * is constructed using the authority [SearchSuggestionSampleProvider.AUTHORITY], and the mode
     * [SearchSuggestionSampleProvider.MODE] and then use `suggestions` to add `queryString` to the
     * recent queries list. We retrieve the [Bundle] from `queryIntent` that might have been stored
     * as extended data under the key [SearchManager.APP_DATA] to initialize our variable
     * `val appData`. If `appData` is *null* we set the text of our [TextView] field [mAppDataText]
     * to "<no app data bundle>", and if it is not *null* we retrieve the [String] from `appData`
     * using the key "demo_key" to initialize our variable `val testStr` and if `testStr` is *null*
     * set the text of our [TextView] field [mAppDataText] to "<no app data>", otherwise we set it
     * to the contents of `testStr`. Finally we set the text of our [TextView] field [mDeliveredByText]
     * to the contents of our [String] parameter [entryPoint].
     *
     * @param queryIntent [Intent] used to launch our Activity
     * @param entryPoint [String] identifying how our Activity was launched to display to user in
     * our [TextView] field [mDeliveredByText]
     */
    @SuppressLint("SetTextI18n")
    private fun doSearchQuery(queryIntent: Intent, entryPoint: String) { // The search query is provided as an "extra" string in the query intent
        val queryString = queryIntent.getStringExtra(SearchManager.QUERY)
        mQueryText!!.text = queryString
        /**
         * Record the query string in the recent queries suggestions provider.
         */
        val suggestions = SearchRecentSuggestions(this,
                SearchSuggestionSampleProvider.AUTHORITY, SearchSuggestionSampleProvider.MODE)
        suggestions.saveRecentQuery(queryString, null)
        /**
         * If your application provides context data for its searches,
         * you will receive it as an "extra" bundle in the query intent.
         * The bundle can contain any number of elements, using any number of keys;
         * For this Api Demo we're just using a single string, stored using "demo key".
         */
        val appData = queryIntent.getBundleExtra(SearchManager.APP_DATA)
        if (appData == null) {
            mAppDataText!!.text = "<no app data bundle>"
        }
        if (appData != null) {
            val testStr = appData.getString("demo_key")
            mAppDataText!!.text = testStr ?: "<no app data>"
        }
        /**
         * Report the method by which we were called.
         */
        mDeliveredByText!!.text = entryPoint
    }
}
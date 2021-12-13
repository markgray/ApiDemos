/*
 * Copyright (C) 2012 The Android Open Source Project
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

import android.annotation.TargetApi
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract.Contacts
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ListView

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnCloseListener
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.fragment.app.ListFragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader

/**
 * Demonstration of the use of a [CursorLoader] to load and display contacts
 * data in a fragment. Shows how to retain a [ListFragment] by calling
 * `setRetainInstance(true)` in the `onActivityCreated` callback.
 */
@Suppress("MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class LoaderRetained : AppCompatActivity() {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our `FragmentManager` variable `val fm` to the support `FragmentManager`
     * used for interacting with fragments associated with this activity. We use `fm` to search for
     * the `Fragment` with ID android.R.id.content (the root view) and if it has not been added yet
     * we create a [CursorLoaderListFragment] instance to initialize our variable `val list` and use
     * `fm` to begin a `FragmentTransaction` which we use to add `list` to the activity state to
     * the view with ID android.R.id.content and then commit the `FragmentTransaction`.
     *
     * @param savedInstanceState we do not override onSaveInstanceState so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fm = supportFragmentManager

        // Create the list fragment and add it as our sole content.
        if (fm.findFragmentById(android.R.id.content) == null) {
            val list = CursorLoaderListFragment()
            fm.beginTransaction().add(android.R.id.content, list).commit()
        }
    }


    /**
     * A simple [ListFragment] for displaying the contacts database.
     */
    class CursorLoaderListFragment : ListFragment(),
            OnQueryTextListener,
            OnCloseListener,
            LoaderManager.LoaderCallbacks<Cursor>
    {

        /**
         * This is the Adapter being used to display the list's data.
         */
        internal lateinit var mAdapter: SimpleCursorAdapter

        /**
         * The [SearchView] for doing filtering.
         */
        internal lateinit var mSearchView: SearchView

        /**
         * If non-null, this is the current filter the user has provided.
         */
        internal var mCurFilter: String? = null

        /**
         * Called when all saved state has been restored into the view hierarchy of the fragment.
         * This is called after [onViewCreated] and before [onStart]. First we call through to our
         * super's implementation of `onViewStateRestored`, and then we set the retain instance
         * state flag of our Fragment to `true`. We set the empty text of our List that will be
         * shown if there is no data to "No phone numbers". We report that this fragment would like
         * to participate in populating the options menu (system will now call [onCreateOptionsMenu]
         * and related methods). We initialize our [SimpleCursorAdapter] field [mAdapter] with an
         * empty [SimpleCursorAdapter], and set our list's adapter to [mAdapter]. We set the list to
         * not be displayed while the data is being loaded so the indefinite progress bar will be
         * displayed to start with. Then we make sure a loader is initialized and connected to us.
         *
         * @param savedInstanceState We do not override [onSaveInstanceState] so do not use.
         */
        override fun onViewStateRestored(savedInstanceState: Bundle?) {
            super.onViewStateRestored(savedInstanceState)

            // In this sample we are going to use a retained fragment.
            @Suppress("DEPRECATION")
            retainInstance = true // TODO: replace this with ViewModel to retain data

            // Give some text to display if there is no data.  In a real
            // application this would come from a resource.
            setEmptyText("No phone numbers")

            // We have a menu item to show in action bar.
            setHasOptionsMenu(true)

            // Create an empty adapter we will use to display the loaded data.
            mAdapter = SimpleCursorAdapter(activity,
                android.R.layout.simple_list_item_2, null,
                arrayOf(Contacts.DISPLAY_NAME, Contacts.CONTACT_STATUS),
                intArrayOf(android.R.id.text1, android.R.id.text2), 0)
            listAdapter = mAdapter

            // Start out with a progress indicator.
            setListShown(false)

            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            LoaderManager.getInstance(this).initLoader(0, null, this)
        }

        /**
         * Customized [SearchView] which clears the search text when the [SearchView] is
         * collapsed.
         */
        class MySearchView
        /**
         * Constructor which simply calls through to our super's constructor.
         *
         * @param context in our case the `Activity` returned by `getActivity()`.
         */
        (context: Context) : SearchView(context) {

            // The normal SearchView doesn't clear its search text when
            // collapsed, so we will do this for it.

            /**
             * Called when this view is collapsed as an action view. We set the query to the empty
             * [String], without performing the search, then call through to our super's implementation
             * of `onActionViewCollapsed`.
             */
            override fun onActionViewCollapsed() {
                setQuery("", false)
                super.onActionViewCollapsed()
            }
        }

        /**
         * Initialize the contents of the Activity's standard options menu. First we `add` a
         * [MenuItem] to [menu] with the title "Search" saving a reference to it in our [MenuItem]
         * variable `val item`. We set the icon for `item` to the system icon ic_menu_search, set
         * its flags SHOW_AS_ACTION_IF_ROOM and SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW. We initialize
         * our [SearchView] field [mSearchView] with a new instance of [MySearchView] using the
         * `Activity` this fragment is currently associated with as the [Context], set the
         * [OnQueryTextListener] of [mSearchView] to *this*, and also set its [OnCloseListener] to
         * *this*. We set [mSearchView] to be iconified by default, and finally set the action view
         * of `item` to be [mSearchView].
         *
         * @param menu     The options menu in which you place your items.
         * @param inflater inflater you can use to inflate compiled xml files into `menu`
         */
        override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
            // Place an action bar item for searching.
            val item = menu.add("Search")
            item.setIcon(android.R.drawable.ic_menu_search)
            item.setShowAsAction(
                    MenuItem.SHOW_AS_ACTION_IF_ROOM or MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW
            )
            mSearchView = MySearchView(activity as Context)
            mSearchView.setOnQueryTextListener(this)
            mSearchView.setOnCloseListener(this)
            mSearchView.setIconifiedByDefault(true)
            item.actionView = mSearchView
        }

        /**
         * Called when the query text is changed by the user. If our [newText] parameter is not the
         * empty [String] we set our [String] variable `val newFilter` to [newText], otherwise we
         * set it to *null*. If the current filter in our [String] field [mCurFilter] and `newText`
         * are both *null* we immediately return *true* to the caller. If [mCurFilter] is not *null*
         * and it is equal to `newFilter` we return *true* to the caller. Otherwise we set
         * [mCurFilter] to `newFilter`, instruct the [LoaderManager] to restart the loader, and
         * then return *true* to the caller.
         *
         * @param newText the new content of the query text field.
         * @return *true* since the action is completely handled by this listener.
         */
        override fun onQueryTextChange(newText: String): Boolean {
            // Called when the action bar search text has changed.  Update
            // the search filter, and restart the loader to do a new query
            // with this filter.
            val newFilter = if (!TextUtils.isEmpty(newText)) newText else null
            // Don't do anything if the filter hasn't actually changed.
            // Prevents restarting the loader when restoring state.
            if (mCurFilter == null && newFilter == null) {
                return true
            }
            if (mCurFilter != null && mCurFilter == newFilter) {
                return true
            }
            mCurFilter = newFilter
            LoaderManager.getInstance(this).restartLoader(0, null, this)
            return true
        }

        /**
         * Called when the user submits the query. We ignore this and just return *true* to the caller.
         *
         * @param query the query text that is to be submitted
         * @return *true* since the query has been handled by the listener
         */
        override fun onQueryTextSubmit(query: String): Boolean {
            // Don't care about this.
            return true
        }

        /**
         * The user is attempting to close the [SearchView]. If the [SearchView] field [mSearchView]
         * query has text in it, we set it to *null* and instruct it to submit the search. We then
         * return *true* to the caller indicating that we do not require it to do anything more.
         *
         * @return *true* since the listener wants to override the default behavior of clearing the
         * text field and dismissing it.
         */
        override fun onClose(): Boolean {
            if (!TextUtils.isEmpty(mSearchView.query)) {
                mSearchView.setQuery(null, true)
            }
            return true
        }

        /**
         * This method will be called when an item in the list is selected. We simply log the id of
         * the item clicked.
         *
         * @param l The [ListView] where the click happened
         * @param v The [View] that was clicked within the [ListView]
         * @param position The position of the view in the list
         * @param id The row id of the item that was clicked
         */
        override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
            // Insert desired behavior here.
            Log.i("FragmentComplexList", "Item clicked: $id")
        }

        /**
         * Instantiate and return a new [Loader] for the given ID. First we create a [Uri] to
         * initialize our variable `val baseUri` using just `Contacts.CONTENT_URI` as the [Uri] if
         * there is no filter in our field [mCurFilter], or creating a [Uri] by encoding the special
         * characters and then appending the filter in our [String] field [mCurFilter] to the base
         * [Uri] `Contacts.CONTENT_FILTER_URI`. We then create a selection filter to initialize our
         * [String] variable `val select` declaring which rows to return, formatted as an SQL WHERE
         * clause (excluding the WHERE itself). The `select` selection specifies that DISPLAY_NAME
         * is not null, HAS_PHONE_NUMBER is equal to 1 (contact has at least one phone number), and
         * the DISPLAY_NAME is not the empty string. Finally we create and return a [CursorLoader]
         * constructed using `baseUri`, specifying the columns listed in our `String[]` constant
         * `CONTACTS_SUMMARY_PROJECTION`, the rows selected by `select`, with *null* selection
         * arguments, and specifying that the results be sorted using the SQL "ORDER BY" clause:
         * Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC".
         *
         * @param id The ID whose loader is to be created.
         * @param args Any arguments supplied by the caller.
         * @return Return a new Loader instance that is ready to start loading.
         */
        override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
            // This is called when a new Loader needs to be created.  This
            // sample only has one Loader, so we don't care about the ID.
            // First, pick the base URI to use depending on whether we are
            // currently filtering.
            val baseUri: Uri = if (mCurFilter != null) {
                Uri.withAppendedPath(Contacts.CONTENT_FILTER_URI,
                        Uri.encode(mCurFilter))
            } else {
                Contacts.CONTENT_URI
            }

            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            val select = ("((" + Contacts.DISPLAY_NAME + " NOTNULL) AND ("
                    + Contacts.HAS_PHONE_NUMBER + "=1) AND ("
                    + Contacts.DISPLAY_NAME + " != '' ))")

            return CursorLoader(activity!!, baseUri,
                    CONTACTS_SUMMARY_PROJECTION, select, null,
                    Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC")
        }

        /**
         * Called when a previously created loader has finished its load. First we swap the new cursor
         * in and then if the `Fragment` is in the resumed state (first time running) we cause
         * the list to be shown (replacing the indeterminate progress indicator), otherwise (after an
         * orientation change) we cause it to be shown without animation from the previous state.
         *
         * @param loader The [Loader] that has finished.
         * @param data The data generated by the [Loader].
         */
        override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
            // Swap the new cursor in.  (The framework will take care of closing the
            // old cursor once we return.)
            val oldCursor = mAdapter.swapCursor(data)
            if (oldCursor == null) {
                Log.i(TAG, "onLoadFinished swapCursor returns null")
            } else {
                Log.i(TAG, "onLoadFinished swapCursor returns not null")
            }

            // The list should now be shown.
            if (isResumed) {
                setListShown(true)
            } else {
                setListShownNoAnimation(true)
            }
        }

        /**
         * Called when a previously created loader is being reset, and thus making its data
         * unavailable. We swap in a *null* [Cursor], causing the old [Cursor] to be closed. This
         * is not called after an orientation change, but might be if the contacts database changes
         * behind our back(?)
         *
         * @param loader The [Loader] that is being reset.
         */
        override fun onLoaderReset(loader: Loader<Cursor>) {
            // This is called when the last Cursor provided to onLoadFinished()
            // above is about to be closed.  We need to make sure we are no
            // longer using it.
            mAdapter.swapCursor(null)
        }

        /**
         * Our static constant
         */
        companion object {

            /**
             * These are the Contacts columns that we will retrieve.
             */
            internal val CONTACTS_SUMMARY_PROJECTION = arrayOf(Contacts._ID, Contacts.DISPLAY_NAME, Contacts.CONTACT_STATUS, Contacts.CONTACT_PRESENCE, Contacts.PHOTO_ID, Contacts.LOOKUP_KEY)
        }
    }

    /**
     * Our static constant
     */
    companion object {

        /**
         * TAG used for logging.
         */
        internal const val TAG = "LoaderRetained"
    }

}

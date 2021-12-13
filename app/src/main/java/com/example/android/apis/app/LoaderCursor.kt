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

import android.annotation.TargetApi
import android.app.Activity
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
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.ListFragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.example.android.apis.app.LoaderCursor.CursorLoaderListFragment

/**
 * Demonstration of the use of a CursorLoader to load and display contacts data in a fragment.
 * Creates a custom class [CursorLoaderListFragment] which extends [ListFragment], with the
 * necessary callbacks to serve as a [CursorLoader] to load and display contacts data in the
 * [ListFragment]. Includes the use of a [SearchView] which might come in handy for MarkovChain
 */
@Suppress("MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class LoaderCursor : AppCompatActivity() {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we initialize our [FragmentManager] variable `val fm` with a reference to
     * the support [FragmentManager] for interacting with fragments associated with this activity.
     * We check whether this is the first time we are being created by using `fm` to search for a
     * `Fragment` with the container view ID android.R.id.content (our root view) and if it does not
     * find one we create an instance of [CursorLoaderListFragment] to initialize our variable
     * `val list` and use `fm` begin a `FragmentTransaction` to which we chain an add of the fragment
     * `list` to the Activity's state using the container view android.R.id.content, followed by a
     * commit of the `FragmentTransaction`. On the other hand if there was already a Fragment
     * occupying android.R.id.content we are being recreated after an orientation change and do not
     * need to do anything.
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
        } else {
            Log.i(TAG, "There is already a Fragment occupying our root view")
        }
    }

    /**
     * This [ListFragment] fills its `List` with data returned from a [SimpleCursorAdapter]
     */
    class CursorLoaderListFragment
        : ListFragment(),
            OnQueryTextListener,
            OnCloseListener, LoaderManager.LoaderCallbacks<Cursor>
    {

        /**
         * This is the Adapter being used to display the list's data.
         */
        internal lateinit var mAdapter: SimpleCursorAdapter

        /**
         * The SearchView for doing filtering.
         */
        internal lateinit var mSearchView: SearchView

        /**
         * If non-null, this is the current filter the user has provided.
         */
        internal var mCurFilter: String? = null

        /**
         * Called when all saved state has been restored into the view hierarchy of the fragment.
         * This is called after [onViewCreated] and before [onStart]. First we call through to our
         * super's implementaton of `onViewStateRestored`, then we set the text for the `TextView`
         * of the default content for our `ListFragment` to the String "No phone numbers". Then we
         * report that this fragment would like to participate in populating the options menu by
         * receiving a call to [onCreateOptionsMenu] and related methods. Next we create an instance
         * of [SimpleCursorAdapter] with a *null* cursor (we will set the cursor for the adapter
         * later) and save a reference to it in our [SimpleCursorAdapter] field [mAdapter], which we
         * then use to set our ListView's adapter. We call the [ListFragment] method `setListShown`
         * with `false` so that a indeterminate progress bar will be displayed while we wait for our
         * Adapter to have data available for the List. Finally we start a loader (or reconnect)
         * specifying *this* as the interface provider for [LoaderManager.LoaderCallbacks].
         *
         * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
         */
        override fun onViewStateRestored(savedInstanceState: Bundle?) {
            super.onViewStateRestored(savedInstanceState)

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
         * Custom [SearchView] which clears the search text when the [SearchView] is
         * collapsed by the user.
         */
        class MySearchView
        /**
         * Constructor which simply calls our super's constructor.
         *
         * @param context Context used by super (`getActivity()` called from menu of our
         * `CursorLoaderListFragment` would return `LoaderCurstor` as the Activity Context
         * in our case)
         */
        (context: Context) : SearchView(context) {

            /**
             * Called when this view is collapsed as an action view.
             * See [MenuItem.collapseActionView].
             *
             * The normal SearchView doesn't clear its search text when collapsed, so we will do
             * this for it. We simply call `setQuery` with an empty String, and false to
             * prevent it being looked up. Finally we call through to our super's implementation
             * of `onActionViewCollapsed`.
             */
            override fun onActionViewCollapsed() {
                setQuery("", false)
                super.onActionViewCollapsed()
            }
        }

        /**
         * Initialize the contents of the Activity's standard options menu. You should place your
         * menu items in to the [Menu] parameter [menu]. For this method to be called, you must have
         * first called [setHasOptionsMenu]. See [Activity.onCreateOptionsMenu] for more information.
         *
         * First we add a new [MenuItem] to our [Menu] parameter [menu] with the title "Search",
         * saving a reference to it in our variable `val item`. We set the icon for `item` to the
         * android system drawable ic_menu_search, set the options for `item` to SHOW_AS_ACTION_IF_ROOM
         * and SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW (the items action view will collapse to a normal
         * menu item). Next we initialize our [SearchView] field [mSearchView] with a new instance
         * of [MySearchView], set its [SearchView.OnQueryTextListener] to *this*, set its
         * [SearchView.OnCloseListener] to *this*, and set the default or resting state of the
         * search field to iconified (a single search icon is shown by default and expands to show
         * the text field and other buttons when pressed). Also, if the default state is iconified,
         * then it collapses to that state when the close button is pressed. Finally we set the
         * action view of `item` to [mSearchView].
         *
         * @param menu     The options menu in which you place your items.
         * @param inflater could be used to instantiate menu XML files into Menu objects, but we do
         * not use
         * @see .setHasOptionsMenu
         *
         * @see .onPrepareOptionsMenu
         *
         * @see .onOptionsItemSelected
         */
        override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
            // Place an action bar item for searching.
            val item = menu.add("Search")
            item.setIcon(android.R.drawable.ic_menu_search)
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM or MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
            mSearchView = MySearchView(activity as Context)
            mSearchView.setOnQueryTextListener(this)
            mSearchView.setOnCloseListener(this)
            mSearchView.setIconifiedByDefault(true)
            item.actionView = mSearchView
        }

        /**
         * Called when the query text is changed by the user. If the new text is not empty we set
         * [String] variable `val newFilter` to it, otherwise we set it to null. If both `newFilter`
         * and our current [String] filter in field [mCurFilter] are null we do nothing and return
         * *true* to the caller. If [mCurFilter] is not *null* and `newFilter` is equal to it we
         * also do nothing and return *true* to the caller.
         *
         * If we get this far, the user has changed the filter that we should be using, so we set
         * [mCurFilter] to `newFilter`, and use the [LoaderManager] for this `Fragment` to restart
         * our loader (which eventually will result in our override of [onCreateLoader] being called).
         * Finally we return *true* to the caller.
         *
         * @param newText the new content of the query text field.
         * @return *false* if the [SearchView] should perform the default action of showing any
         * suggestions if available, *true* if the action was handled by the listener. We always
         * return *true*.
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
         * Called when the user submits the query. We do not use this feature so we just return *true*
         * to the caller to signify that we have "handled" the query.
         *
         * @param query the query text that is to be submitted
         * @return *true* if the query has been handled by the listener, *false* to let the
         * [SearchView] perform the default action.
         */
        override fun onQueryTextSubmit(query: String): Boolean {
            // Don't care about this.
            return true
        }

        /**
         * The user is attempting to close the [SearchView]. We check to see if there are currently
         * any characters in the text field of our [SearchView] field [mSearchView] and if there are
         * we set the query of [mSearchView] to *null* and submit the query. Finally we return *true*
         * to the caller (since we wanted to override the default behavior).
         *
         * @return *true* if the listener wants to override the default behavior of clearing the
         * text field and dismissing it, *false* otherwise.
         */
        override fun onClose(): Boolean {
            if (!TextUtils.isEmpty(mSearchView.query)) {
                mSearchView.setQuery(null, true)
            }
            return true
        }

        /**
         * This method will be called when an item in the list is selected. Subclasses should
         * override. Subclasses can call getListView().getItemAtPosition(position) if they need
         * to access the data associated with the selected item.
         *
         * Since we are not an actual application, we do not do anything when a list item is clicked.
         *
         * @param l        The ListView where the click happened
         * @param v        The view that was clicked within the ListView
         * @param position The position of the view in the list
         * @param id       The row id of the item that was clicked
         */
        override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
            // Insert desired behavior here.
            Log.i("FragmentComplexList", "Item clicked: $id")
        }

        /**
         * Instantiate and return a new Loader for the given ID. Part of the `LoaderCallbacks`
         * interface. First we construct the [Uri] variable `val baseUri` that will be used to
         * query the contacts database: if we have a filter defined by user use of our [SearchView]
         * we create a `baseUri` that uses an appended path of an encoding of our filter [String]
         * field [mCurFilter].
         *
         * (ala content://com.android.contacts/contacts/filter/value of mCurFilter)
         *
         * If there is no filter at present `baseUri` is
         *
         * content://com.android.contacts/contacts
         *
         * Then we construct [String] variable `val select`, the filter declaring which rows to
         * return, formatted as an SQL WHERE clause (excluding the WHERE itself) which consists of:
         *
         * ((display_name NOTNULL) AND (has_phone_number=1) AND (display_name != '' ))
         *
         * Finally we return an instance of [CursorLoader] constructed using the [Uri] `baseUri`
         * we calculated for the content to retrieve, our `String[] CONTACTS_SUMMARY_PROJECTION`
         * as the projection (list of columns to return), [String] `select` as the selection (rows
         * which match the selection will be returned), *null* for the selection arguments, and the
         * [String] "display_name COLLATE LOCALIZED ASC" as the sort order for the rows.
         *
         * @param id   The ID whose loader is to be created.
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

            return CursorLoader(requireActivity(), baseUri,
                    CONTACTS_SUMMARY_PROJECTION, select, null,
                    Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC")
        }

        /**
         * Called when a previously created loader has finished its load. Note that normally an
         * application is not allowed to commit fragment transactions while in this call, since
         * it can happen after an activity's state is saved.
         * See [FragmentManager.openTransaction()][FragmentManager.beginTransaction] for further
         * discussion on this.
         *
         * This function is guaranteed to be called prior to the release of
         * the last data that was supplied for this Loader. At this point
         * you should remove all use of the old data (since it will be released
         * soon), but should not do your own release of the data since its Loader
         * owns it and will take care of that.  The Loader will take care of
         * management of its data so you don't have to.  In particular:
         *
         * The Loader will monitor for changes to the data, and report
         * them to you through new calls here.  You should not monitor the
         * data yourself.  For example, if the data is a [android.database.Cursor]
         * and you place it in a [android.widget.CursorAdapter], use
         * the [android.widget.CursorAdapter] constructor without passing
         * in either [android.widget.CursorAdapter.FLAG_AUTO_REQUERY]
         * or [android.widget.CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER]
         * (that is, use 0 for the flags argument). This prevents the CursorAdapter
         * from doing its own observing of the Cursor, which is not needed since
         * when a change happens you will get a new Cursor throw another call
         * here.
         *
         * The Loader will release the data once it knows the application is no longer
         * using it. For example, if the data is a [android.database.Cursor] from a
         * [android.content.CursorLoader], you should not call close() on it yourself.
         * If the Cursor is being placed in a [android.widget.CursorAdapter], you should
         * use the [android.widget.CursorAdapter.swapCursor] method so that the old Cursor
         * is not closed.
         *
         * First we instruct our [SimpleCursorAdapter] field [mAdapter] to swap in our newly
         * loaded [Cursor] parameter [data], and then if our `Fragment` is in the resumed state
         * we toggle our `List` to be shown now. If we are not in the resumed state we toggle our
         * `List` to be shown ommitting the animation.
         *
         * @param loader The Loader that has finished.
         * @param data   The data generated by the Loader.
         */
        override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
            // Swap the new cursor in.  (The framework will take care of closing the
            // old cursor once we return.)
            mAdapter.swapCursor(data)

            // The list should now be shown.
            if (isResumed) {
                setListShown(true)
            } else {
                setListShownNoAnimation(true)
            }
        }

        /**
         * Called when a previously created loader is being reset, and thus
         * making its data unavailable. The application should at this point
         * remove any references it has to the Loader's data.
         *
         * We simply instruct our [SimpleCursorAdapter] field [mAdapter] to swap in a null [Cursor].
         *
         * @param loader The Loader that is being reset.
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
             * These are the Contacts rows that we will retrieve.
             */
            internal val CONTACTS_SUMMARY_PROJECTION = arrayOf(Contacts._ID, // The unique ID for a row
                    Contacts.DISPLAY_NAME, // The display name for the contact
                    Contacts.CONTACT_STATUS, // Contact's latest status updat
                    Contacts.CONTACT_PRESENCE, // Contact presence status
                    Contacts.PHOTO_ID, // Reference to the row in the data table holding the photo
                    Contacts.LOOKUP_KEY)// An opaque value that contains hints on how to find the contact
        }
    }

    /**
     * Our static constant
     */
    companion object {
        /**
         * TAG used for logging.
         */
        internal const val TAG = "LoaderCursor"
    }

}

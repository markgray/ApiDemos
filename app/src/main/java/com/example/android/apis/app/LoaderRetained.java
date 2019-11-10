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

package com.example.android.apis.app;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.ListFragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SearchView.OnCloseListener;
import androidx.appcompat.widget.SearchView.OnQueryTextListener;

import org.jetbrains.annotations.NotNull;

/**
 * Demonstration of the use of a CursorLoader to load and display contacts
 * data in a fragment. Shows how to retain a ListFragment by calling
 * setRetainInstance(true) in the onActivityCreated callback.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class LoaderRetained extends AppCompatActivity {

    final static String TAG = "LoaderRetained";

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set {@code FragmentManager fm} to the FragmentManager used for
     * interacting with fragments associated with this activity. We use {@code fm} to search for
     * the Fragment with ID android.R.id.content and if it has not been added yet we create
     * {@code CursorLoaderListFragment list} and use {@code fm} to begin a {@code FragmentTransaction}
     * which we use to add {@code list} to the activity state using ID android.R.id.content and then
     * commit the {@code FragmentTransaction}.
     *
     * @param savedInstanceState we do not override onSaveInstanceState so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fm = getSupportFragmentManager();

        // Create the list fragment and add it as our sole content.
        if (fm.findFragmentById(android.R.id.content) == null) {
            CursorLoaderListFragment list = new CursorLoaderListFragment();
            fm.beginTransaction().add(android.R.id.content, list).commit();
        }
    }


    /**
     * A simple {@code ListFragment} for displaying the contacts database.
     */
    @SuppressWarnings("deprecation")
    public static class CursorLoaderListFragment extends ListFragment
            implements OnQueryTextListener, OnCloseListener,
            LoaderManager.LoaderCallbacks<Cursor> {

        /**
         * This is the Adapter being used to display the list's data.
         */
        SimpleCursorAdapter mAdapter;

        /**
         * The SearchView for doing filtering.
         */
        SearchView mSearchView;

        /**
         * If non-null, this is the current filter the user has provided.
         */
        String mCurFilter;

        /**
         * Called when the fragment's activity has been created and this fragment's view hierarchy
         * instantiated. First we call through to our super's implementation of onActivityCreated,
         * and then we set the retain instance state flag of our Fragment to true. We set the empty
         * text of our List that will be shown if there is no data to "No phone numbers". We report
         * that this fragment would like to participate in populating the options menu (system will
         * now call {@code onCreateOptionsMenu(Menu, MenuInflater)} and related methods). We initialize
         * our field {@code SimpleCursorAdapter mAdapter} with an empty {@code SimpleCursorAdapter},
         * and set our list's adapter to {@code mAdapter}. We set the list to not be displayed while
         * the data is being loaded so the indefinite progress bar will be displayed to start with.
         * Then we make sure a loader is initialized and connected to us.
         *
         * @param savedInstanceState We do not override {@code onSaveInstanceState} so do not use.
         */
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            // In this sample we are going to use a retained fragment.
            setRetainInstance(true);

            // Give some text to display if there is no data.  In a real
            // application this would come from a resource.
            setEmptyText("No phone numbers");

            // We have a menu item to show in action bar.
            setHasOptionsMenu(true);

            // Create an empty adapter we will use to display the loaded data.
            mAdapter = new SimpleCursorAdapter(getActivity(),
                    android.R.layout.simple_list_item_2, null,
                    new String[]{Contacts.DISPLAY_NAME, Contacts.CONTACT_STATUS},
                    new int[]{android.R.id.text1, android.R.id.text2}, 0);
            setListAdapter(mAdapter);

            // Start out with a progress indicator.
            setListShown(false);

            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            getLoaderManager().initLoader(0, null, this);
        }

        /**
         * Customized {@code SearchView} which clears the search text when the {@code SearchView} is
         * collapsed.
         */
        public static class MySearchView extends SearchView {

            /**
             * Constructor which simply calls through to our super's constructor.
             *
             * @param context in our case the {@code Activity} returned by {@code getActivity()}.
             */
            public MySearchView(Context context) {
                super(context);
            }

            // The normal SearchView doesn't clear its search text when
            // collapsed, so we will do this for it.

            /**
             * Called when this view is collapsed as an action view. We set the query to the empty
             * String, without performing the search, then call through to our super's implementation
             * of {@code onActionViewCollapsed}.
             */
            @Override
            public void onActionViewCollapsed() {
                setQuery("", false);
                super.onActionViewCollapsed();
            }
        }

        /**
         * Initialize the contents of the Activity's standard options menu. First we {@code add} a
         * {@code MenuItem item} to {@code menu} with the title "Search". We set the icon for
         * {@code item} to the system icon android.R.drawable.ic_menu_search, set its flags
         * SHOW_AS_ACTION_IF_ROOM and SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW. We initialize our field
         * {@code SearchView mSearchView} with a new instance of {@code MySearchView} using the
         * {@code Activity} this fragment is currently associated with as the {@code Context}, set
         * the {@code OnQueryTextListener} of {@code mSearchView} to "this", and also set its
         * {@code OnCloseListener} to "this". We set {@code mSearchView} to be iconified by default,
         * and finally set the action view of {@code item} to be {@code mSearchView}.
         *
         * @param menu     The options menu in which you place your items.
         * @param inflater inflater you can use to inflate compiled xml files into {@code menu}
         */
        @Override
        public void onCreateOptionsMenu(Menu menu, @NotNull MenuInflater inflater) {
            // Place an action bar item for searching.
            MenuItem item = menu.add("Search");
            item.setIcon(android.R.drawable.ic_menu_search);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                    | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
            mSearchView = new MySearchView(getActivity());
            mSearchView.setOnQueryTextListener(this);
            mSearchView.setOnCloseListener(this);
            mSearchView.setIconifiedByDefault(true);
            item.setActionView(mSearchView);
        }

        /**
         * Called when the query text is changed by the user. If {@code newText} is not the empty
         * String we set {@code String newFilter} to {@code newText}, otherwise we set it to null.
         * If the current filter in our field {@code String mCurFilter} and {@code newText} are both
         * null we immediately return true to the caller. If {@code mCurFilter} is not null and it
         * is equal to {@code newFilter} we return true to the caller. Otherwise we set
         * {@code mCurFilter} to {@code newText}, instruct the {@code LoaderManager} to restart the
         * loader, and then return true to the caller.
         *
         * @param newText the new content of the query text field.
         * @return true since the action is completely handled by this listener.
         */
        @Override
        public boolean onQueryTextChange(String newText) {
            // Called when the action bar search text has changed.  Update
            // the search filter, and restart the loader to do a new query
            // with this filter.
            String newFilter = !TextUtils.isEmpty(newText) ? newText : null;
            // Don't do anything if the filter hasn't actually changed.
            // Prevents restarting the loader when restoring state.
            if (mCurFilter == null && newFilter == null) {
                return true;
            }
            if (mCurFilter != null && mCurFilter.equals(newFilter)) {
                return true;
            }
            mCurFilter = newFilter;
            getLoaderManager().restartLoader(0, null, this);
            return true;
        }

        /**
         * Called when the user submits the query. We ignore this and just return true to the caller.
         *
         * @param query the query text that is to be submitted
         * @return true since the query has been handled by the listener
         */
        @Override
        public boolean onQueryTextSubmit(String query) {
            // Don't care about this.
            return true;
        }

        /**
         * The user is attempting to close the SearchView. If the {@code SearchView mSearchView}
         * query has text in it, we set it to null and instruct it to submit the search. We then
         * return true to the caller indicating that we do not require it to do anything more.
         *
         * @return true since the listener wants to override the default behavior of clearing the
         * text field and dismissing it.
         */
        @Override
        public boolean onClose() {
            if (!TextUtils.isEmpty(mSearchView.getQuery())) {
                mSearchView.setQuery(null, true);
            }
            return true;
        }

        /**
         * This method will be called when an item in the list is selected. We simply log the id of
         * the item clicked.
         *
         * @param l The ListView where the click happened
         * @param v The view that was clicked within the ListView
         * @param position The position of the view in the list
         * @param id The row id of the item that was clicked
         */
        @Override
        public void onListItemClick(@NotNull ListView l, @NotNull View v, int position, long id) {
            // Insert desired behavior here.
            Log.i("FragmentComplexList", "Item clicked: " + id);
        }

        /**
         * These are the Contacts columns that we will retrieve.
         */
        static final String[] CONTACTS_SUMMARY_PROJECTION = new String[]{
                Contacts._ID,
                Contacts.DISPLAY_NAME,
                Contacts.CONTACT_STATUS,
                Contacts.CONTACT_PRESENCE,
                Contacts.PHOTO_ID,
                Contacts.LOOKUP_KEY,
        };

        /**
         * Instantiate and return a new Loader for the given ID. First we create {@code Uri baseUri}
         * using just {@code Contacts.CONTENT_URI} as the Uri if there is no filter specified by our
         * {@code SearchView}, or creating Uri by encoding the special characters and then appending
         * the filter in {@code String mCurFilter} to the base Uri {@code Contacts.CONTENT_FILTER_URI}.
         * We then create a {@code String selection} filter declaring which rows to return, formatted
         * as an SQL WHERE clause (excluding the WHERE itself). The {@code selection} specifies that
         * DISPLAY_NAME is not null, HAS_PHONE_NUMBER is equal to 1 (contact has at least one phone
         * number), and the DISPLAY_NAME is not the empty string. Finally we create and return a
         * {@code CursorLoader} constructed using {@code baseUri}. specifying the columns listed in
         * {@code String[] CONTACTS_SUMMARY_PROJECTION}, the rows selected by {@code String select},
         * with null selection arguments, and specifying that the results be sorted using the SQL
         * "ORDER BY" clause: Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC".
         *
         * @param id The ID whose loader is to be created.
         * @param args Any arguments supplied by the caller.
         * @return Return a new Loader instance that is ready to start loading.
         */
        @NotNull
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            // This is called when a new Loader needs to be created.  This
            // sample only has one Loader, so we don't care about the ID.
            // First, pick the base URI to use depending on whether we are
            // currently filtering.
            Uri baseUri;
            if (mCurFilter != null) {
                baseUri = Uri.withAppendedPath(Contacts.CONTENT_FILTER_URI,
                        Uri.encode(mCurFilter));
            } else {
                baseUri = Contacts.CONTENT_URI;
            }

            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            String select = "((" + Contacts.DISPLAY_NAME + " NOTNULL) AND ("
                    + Contacts.HAS_PHONE_NUMBER + "=1) AND ("
                    + Contacts.DISPLAY_NAME + " != '' ))";
            //noinspection ConstantConditions
            return new CursorLoader(getActivity(), baseUri,
                    CONTACTS_SUMMARY_PROJECTION, select, null,
                    Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
        }

        /**
         * Called when a previously created loader has finished its load. First we swap the new cursor
         * in and then if the {@code Fragment} is in the resumed state (first time running) we cause
         * the list to be shown (replacing the indeterminate progress indicator), otherwise (after an
         * orientation change) we cause it to be shown without animation from the previous state.
         *
         * @param loader The Loader that has finished.
         * @param data The data generated by the Loader.
         */
        @Override
        public void onLoadFinished(@NotNull Loader<Cursor> loader, Cursor data) {
            // Swap the new cursor in.  (The framework will take care of closing the
            // old cursor once we return.)
            Cursor oldCursor = mAdapter.swapCursor(data);
            if (oldCursor == null) {
                Log.i(TAG, "onLoadFinished swapCursor returns null");
            } else {
                Log.i(TAG, "onLoadFinished swapCursor returns not null");
            }

            // The list should now be shown.
            if (isResumed()) {
                setListShown(true);
            } else {
                setListShownNoAnimation(true);
            }
        }

        /**
         * Called when a previously created loader is being reset, and thus making its data
         * unavailable. We swap in a null cursor, causing the old cursor to be closed. This is
         * not called after an orientation change, but might be if the contacts database changes
         * behind our back(?)
         *
         * @param loader The Loader that is being reset.
         */
        @Override
        public void onLoaderReset(@NotNull Loader<Cursor> loader) {
            // This is called when the last Cursor provided to onLoadFinished()
            // above is about to be closed.  We need to make sure we are no
            // longer using it.
            mAdapter.swapCursor(null);
        }
    }

}

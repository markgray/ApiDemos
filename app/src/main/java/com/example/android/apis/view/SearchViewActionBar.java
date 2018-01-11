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

package com.example.android.apis.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.android.apis.R;

import java.util.List;

/**
 * This demonstrates the usage of SearchView in an ActionBar as a menu item. It sets
 * a SearchableInfo on the SearchView for suggestions and submitting queries to. In
 * AndroidManifest.xml a {@code <meta-data>} element android:name="android.app.default_searchable"
 * android:value=".app.SearchQueryResults" sets the Activity to handle search requests.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SearchViewActionBar extends Activity implements SearchView.OnQueryTextListener {
    /**
     * {@code SearchView} in our options menu with ID R.id.action_search, and with the action view
     * set by android:actionViewClass="android.widget.SearchView"
     */
    private SearchView mSearchView;
    /**
     * {@code TextView} in our layout with ID R.id.status_text, used to display query text and query
     * results.
     */
    private TextView mStatusView;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we request the window feature FEATURE_ACTION_BAR (enabling the Action
     * Bar), and then we set our content view to our layout file R.layout.searchview_actionbar.
     * Finally we initialize our field {@code TextView mStatusView} by finding the view with the ID
     * R.id.status_text.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.searchview_actionbar);

        mStatusView = (TextView) findViewById(R.id.status_text);
    }

    /**
     * Initialize the contents of the Activity's standard options menu. First we call through to our
     * super's implementation of {@code onCreateOptionsMenu}, then we initialize our variable
     * {@code MenuInflater inflater} by fetching a {@code MenuInflater} for this context. We use
     * {@code inflater} to inflate our menu layout R.menu.searchview_in_menu into our parameter
     * {@code Menu menu}, initialize {@code MenuItem searchItem} by finding the item with the ID
     * R.id.action_search, and then initialize our field {@code SearchView mSearchView} by retrieving
     * the currently set action view for {@code MenuItem searchItem}. Then we call our method
     * {@code setupSearchView(searchItem)} to configure {@code searchItem} to behave as we wish it to.
     * Finally we return true to the caller so that the menu will be displayed.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.searchview_in_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) searchItem.getActionView();
        setupSearchView(searchItem);

        return true;
    }

    /**
     * Configures its parameter {@code MenuItem searchItem} as well as configuring our field
     * {@code SearchView mSearchView} to act as a {@code SearchView}. First we call our method
     * {@code isAlwaysExpanded} (which always returns false) and based on its return value (false)
     * we set the show as action flags SHOW_AS_ACTION_IF_ROOM and SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW
     * of {@code MenuItem searchItem} (these flags request that this item be shown as a button in the
     * Action Bar if the system decides there is room for it, and has the item's action view collapse
     * to a normal menu item respectively). We initialize our variable {@code SearchManager searchManager}
     * with a handle to a system level service SEARCH_SERVICE instance (provides access to the system
     * search services). If {@code searchManager} is not null we initialize our variable
     * {@code List<SearchableInfo> searchables} with a list of the searchable activities that can be
     * included in global search. Then we initialize {@code SearchableInfo info} with the information
     * about a searchable activity with the complete component name of this activity.
     * <p>
     * Next we loop through the {@code SearchableInfo inf} in the list {@code searchables} and if the
     * search suggestions authority of {@code inf} is not null, and its search suggestion content
     * provider authority starts with the string "applications" (there are none on my pixel) we set
     * {@code info} to {@code inf} (does not happen!). After looping through all the global
     * {@code SearchableInfo} object we set the {@code SearchableInfo} for {@code mSearchView} to
     * {@code info} (properties in the {@code SearchableInfo} are used to display labels, hints,
     * suggestions, create intents for launching search results screens and controlling other
     * affordances such as a voice button).
     * <p>
     * Finally we set the {@code OnQueryTextListener} of {@code mSearchView} to "this".
     *
     * @param searchItem {@code MenuItem} in our action bar menu that is to be configured.
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setupSearchView(MenuItem searchItem) {

        if (isAlwaysExpanded()) {
            mSearchView.setIconifiedByDefault(false);
        } else {
            searchItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM
                    | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        }

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if (searchManager != null) {
            List<SearchableInfo> searchables = searchManager.getSearchablesInGlobalSearch();

            // Try to use the "applications" global search provider
            final ComponentName componentName = getComponentName();
            SearchableInfo info = searchManager.getSearchableInfo(componentName);
            for (SearchableInfo inf : searchables) {
                final String suggestAuthority = inf.getSuggestAuthority();
                if (inf.getSuggestAuthority() != null
                        && suggestAuthority.startsWith("applications")) {
                    info = inf;
                }
            }
            mSearchView.setSearchableInfo(info);
        }

        mSearchView.setOnQueryTextListener(this);
    }

    /**
     * Called when the query text is changed by the user. We set the text of our field {@code TextView mStatusView}
     * to a string formed by concatenating the string "Query = " with our parameter {@code String newText},
     * and return false to the caller.
     *
     * @param newText the new content of the query text field.
     * @return false if the SearchView should perform the default action of showing any
     * suggestions if available, true if the action was handled by the listener.
     */
    @SuppressLint("SetTextI18n")
    public boolean onQueryTextChange(String newText) {
        mStatusView.setText("Query = " + newText);
        return false;
    }

    /**
     * Called when the user submits the query. We set the text of our field {@code TextView mStatusView}
     * to a string formed by concatenating the string "Query = " with our parameter {@code String newText}
     * followed by the string " : submitted", and return false to the caller.
     *
     * @param query the query text that is to be submitted
     * @return true if the query has been handled by the listener, false to let the
     * SearchView perform the default action.
     */
    @SuppressLint("SetTextI18n")
    public boolean onQueryTextSubmit(String query) {
        mStatusView.setText("Query = " + query + " : submitted");
        return false;
    }

    /**
     * Override this to have your subclass determine whether our search view is always expanded (by
     * returning true instead of false like we do).
     *
     * @return true to have the search menu item always expanded, false to have iconified.
     */
    protected boolean isAlwaysExpanded() {
        return false;
    }
}

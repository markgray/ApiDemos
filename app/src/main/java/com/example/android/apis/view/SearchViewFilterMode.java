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

import com.example.android.apis.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;


/**
 * Shows a list that can be filtered in-place with a SearchView in non-iconified mode.
 * Calls setTextFilterEnabled(true) on the layout's ListView to filter the children
 * displayed in the ListView and the value it gets in the onQueryTextChange callback
 * it implements as a SearchView.OnQueryTextListener to setFilterText on the ListView.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SearchViewFilterMode extends Activity implements SearchView.OnQueryTextListener {

    /**
     * TAG to use for logging UNUSED
     */
    @SuppressWarnings("unused")
    private static final String TAG = "SearchViewFilterMode";

    /**
     * {@code SearchView} with ID R.id.search_view in our layout
     */
    private SearchView mSearchView;
    /**
     * {@code ListView} with ID R.id.list_view in our layout used to display our filtered list of
     * cheeses.
     */
    private ListView mListView;
    /**
     * UNUSED
     */
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private ArrayAdapter<String> mAdapter;

    /**
     * Reference to our database of cheeses.
     */
    private final String[] mStrings = Cheeses.sCheeseStrings;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we request the window feature FEATURE_ACTION_BAR (for no good reason),
     * and then we set our content view to our layout file R.layout.searchview_filter.
     * <p>
     * We initialize our field {@code SearchView mSearchView} by finding the view with the ID
     * R.id.search_view, and {@code ListView mListView} by finding the view with the ID R.id.list_view.
     * We set the adapter of {@code mListView} to an {@code ArrayAdapter} constructed to display our
     * database {@code mStrings} using the layout android.R.layout.simple_list_item_1, enable its
     * type filter window capability (typing when this view has focus will filter the children to
     * match the users input). Finally we call our method {@code setupSearchView} to set up
     * {@code mSearchView} as we want it.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.searchview_filter);

        mSearchView = (SearchView) findViewById(R.id.search_view);
        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setAdapter(mAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                mStrings));
        mListView.setTextFilterEnabled(true);
        setupSearchView();
    }

    /**
     * Called to set up {@code mSearchView} as we want it. We set the iconified by default resting
     * state of {@code mSearchView} to false, set its {@code OnQueryTextListener} to this, disable
     * its submit button, and set its query hint to the string with ID R.string.cheese_hunt_hint
     * ("Cheese hunt").
     */
    private void setupSearchView() {
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.setQueryHint(getString(R.string.cheese_hunt_hint));
    }

    /**
     * Called when the query text is changed by the user. If our parameter {@code newText} is empty
     * (no text or null) we call the {@code clearTextFilter} method of {@code mListView} to clear
     * its text filter, otherwise we call its {@code setFilterText} to set its filter text to
     * {@code newText}. In either case we return true to the caller to indicate that we handled the
     * action.
     *
     * @param newText the new content of the query text field.
     * @return true if the action was handled by the listener.
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            mListView.clearTextFilter();
        } else {
            mListView.setFilterText(newText);
        }
        return true;
    }

    /**
     * Called when the user submits the query. We just ignore and return false to the caller so that
     * the default action can be taken.
     *
     * @param query the query text that is to be submitted
     * @return false to let the SearchView perform the default action.
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }
}

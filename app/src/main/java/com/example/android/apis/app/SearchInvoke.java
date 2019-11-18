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

package com.example.android.apis.app;

import com.example.android.apis.R;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

import androidx.appcompat.app.AppCompatActivity;

/**
 * This activity shows a few different ways to invoke search, and inserts context-specific data for
 * use by the search activity. The search activity is defined in AndroidManifest.xml using a meta-data
 * element added to our {@code <activity>} parent component which has the attributes:
 * <ul>
 * <li>android:name="android.app.default_searchable"</li>
 * <li>android:value=".app.SearchQueryResults"</li>
 * </ul>
 * This specifies .app.SearchQueryResults to be our searchable activity (it performs searches).
 * The definition of android.app.default_searchable is more typically handled at the application
 * level, where it can serve as a default for all of your activities.
 */
public class SearchInvoke extends AppCompatActivity {
    // UI elements
    Button mStartSearch; // Button used to start search - Without a keyboard, you need to press this to get a soft keyboard to use.
    Spinner mMenuMode; // Spinner used to select between "Search Key", "Menu Item", "Type-To-Search" or "Disabled"
    EditText mQueryPrefill; // Used to enter text to prefill the search
    EditText mQueryAppData; // context specific data to include in a Bundle under the key "demo_key" it will be returned in search Intents

    // Menu mode spinner choices
    // This list must match the list found in samples/ApiDemos/res/values/arrays.xml
    final static int MENUMODE_SEARCH_KEY = 0; // "Search Key" Uses the search key to launch searches (Needs keyboard)
    final static int MENUMODE_MENU_ITEM = 1;  // "Menu Item" Uses the menu item itself to launch searches
    final static int MENUMODE_TYPE_TO_SEARCH = 2; // "Type-To-Search" unhandled keystrokes will start an application-defined search (needs keyboard)
    final static int MENUMODE_DISABLED = 3; // "Disabled" Search is disabled

    /**
     * Called when the activity is created. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.search_invoke.
     * We locate the {@code Button mStartSearch} R.id.btn_start_search, the {@code Spinner mMenuMode}
     * R.id.spinner_menu_mode, the {@code EditText mQueryPrefill} R.id.txt_query_prefill, and the
     * {@code EditText mQueryAppData} R.id.txt_query_appdata. Then we create from R.array.search_menuModes
     * an ArrayAdapter<CharSequence> adapter using the system layout android.R.layout.simple_spinner_dropdown_item,
     * set the layout resource to create the drop down views to android.R.layout.simple_spinner_dropdown_item
     * and set the {@code SpinnerAdapter} of {@code Spinner mMenuMode} to {@code adapter}. We set the
     * {@code OnItemSelectedListener} of {@code mMenuMode} to an anonymous class which selects the
     * default key handling for this activity based on the Spinner item selected to DEFAULT_KEYS_SEARCH_LOCAL
     * if MENUMODE_TYPE_TO_SEARCH is selected, and DEFAULT_KEYS_DISABLE otherwise (keyboard required
     * to tell the difference). Finally we set the {@code OnClickListener} of {@code Button mStartSearch}
     * to an anonymous class which calls our override of the method {@code onSearchRequested} when the
     * Button is clicked.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate our UI from its XML layout description.
        setContentView(R.layout.search_invoke);

        // Get display items for later interaction
        mStartSearch = findViewById(R.id.btn_start_search);
        mMenuMode = findViewById(R.id.spinner_menu_mode);
        mQueryPrefill = findViewById(R.id.txt_query_prefill);
        mQueryAppData = findViewById(R.id.txt_query_appdata);

        // Populate items
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.search_menuModes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mMenuMode.setAdapter(adapter);

        // Create listener for the menu mode dropdown.  We use this to demonstrate control
        // of the default keys handler in every Activity.  More typically, you will simply set
        // the default key mode in your activity's onCreate() handler.
        mMenuMode.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position == MENUMODE_TYPE_TO_SEARCH) {
                            setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
                        } else {
                            setDefaultKeyMode(DEFAULT_KEYS_DISABLE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        setDefaultKeyMode(DEFAULT_KEYS_DISABLE);
                    }
                });

        // Attach actions to buttons
        mStartSearch.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onSearchRequested();
                    }
                });
    }

    /**
     * Called when your activity's options menu needs to be updated. First we call through to our
     * super's implementation of {@code onPrepareOptionsMenu}. Then we declare a {@code MenuItem item}.
     * Then we remove the two {@code MenuItem}'s we may have added in a previous call. Next we
     * switch based on the position of the currently selected item of our {@code Spinner mMenuMode}:
     * <ul>
     * <li>MENUMODE_SEARCH_KEY - we add the MenuItem "(Search Key)" as item ID 0</li>
     * <li>MENUMODE_MENU_ITEM - we add the MenuItem "Search" as item ID 0, and set its alphabetic shortcut to MENU_KEY</li>
     * <li>MENUMODE_TYPE_TO_SEARCH - we add the MenuItem "(Type-To-Search)" as item ID 0</li>
     * <li>MENUMODE_DISABLED - we add the MenuItem "(Disabled)" as item ID 0</li>
     * </ul>
     * In all cases we then add the MenuItem "Clear History" as item ID 1 to {@code Menu menu} and
     * return true to the caller.
     *
     * @param menu The options menu as last shown or first initialized by onCreateOptionsMenu().
     * @return true so the menu will be displayed
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item;

        // first, get rid of our menus (if any)
        menu.removeItem(0);
        menu.removeItem(1);

        // next, add back item(s) based on current menu mode
        switch (mMenuMode.getSelectedItemPosition()) {
            case MENUMODE_SEARCH_KEY:
                //noinspection UnusedAssignment
                item = menu.add(Menu.NONE, 0, Menu.NONE, "(Search Key)");
                break;

            case MENUMODE_MENU_ITEM:
                item = menu.add(Menu.NONE, 0, Menu.NONE, "Search");
                item.setAlphabeticShortcut(SearchManager.MENU_KEY);
                break;

            case MENUMODE_TYPE_TO_SEARCH:
                //noinspection UnusedAssignment
                item = menu.add(Menu.NONE, 0, Menu.NONE, "(Type-To-Search)");
                break;

            case MENUMODE_DISABLED:
                //noinspection UnusedAssignment
                item = menu.add(Menu.NONE, 0, Menu.NONE, "(Disabled)");
                break;
        }

        //noinspection UnusedAssignment
        item = menu.add(Menu.NONE, 1, Menu.NONE, "Clear History");
        return true;
    }

    /**
     * This hook is called whenever an item in our options menu is selected. First we switch based on
     * the item ID that was selected, if it is item 1 we then call our method {@code clearSearchHistory}
     * to clear the suggestions that are provided by our provider {@code SearchSuggestionSampleProvider}
     * via the {@code SearchQueryResults} search activity. If it is item 0, we then switch based on
     * the selection of {@code Spinner mMenuMode}:
     * <ul>
     *     <li>MENUMODE_SEARCH_KEY - we pop up a dialog instructing the user to dismiss the dialog and use the search key</li>
     *     <li>MENUMODE_MENU_ITEM - immediately calls our override of {@code onSearchRequested}</li>
     *     <li>MENUMODE_TYPE_TO_SEARCH - we pop up a dialog instructing the user to dismiss the dialog and start typing</li>
     *     <li>MENUMODE_DISABLED - we pop up a dialog instructing the user that they have disabled search</li>
     * </ul>
     * After either of these menu items we return the super's implementation of {@code onOptionsItemSelected} to the caller.
     *
     * @param item The menu item that was selected.
     * @return true to consume the item here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                switch (mMenuMode.getSelectedItemPosition()) {
                    case MENUMODE_SEARCH_KEY:
                        new AlertDialog.Builder(this)
                                .setMessage("To invoke search, dismiss this dialog and press the search key" +
                                        " (F5 on the simulator).")
                                .setPositiveButton("OK", null)
                                .show();
                        break;

                    case MENUMODE_MENU_ITEM:
                        onSearchRequested();
                        break;

                    case MENUMODE_TYPE_TO_SEARCH:
                        new AlertDialog.Builder(this)
                                .setMessage("To invoke search, dismiss this dialog and start typing.")
                                .setPositiveButton("OK", null)
                                .show();
                        break;

                    case MENUMODE_DISABLED:
                        new AlertDialog.Builder(this)
                                .setMessage("You have disabled search.")
                                .setPositiveButton("OK", null)
                                .show();
                        break;
                }
                break;
            case 1:
                clearSearchHistory();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * This hook is called when the user signals the desire to start a search. By overriding this
     * hook we can insert local or context-specific data. First we check to see if MENUMODE_DISABLED
     * is selected in our {@code Spinner mMenuMode}, and if so we return false having done nothing.
     * (false is an indication to the caller that our activity blocks search). Next we fetch the
     * contents of {@code EditText mQueryPrefill} to {@code String queryPrefill} (the String the
     * user has typed in to prefill the search box). Next we declare {@code Bundle appDataBundle},
     * fetch the contents of {@code EditText mQueryAppData} to {@code String queryAppDataString} (the
     * String the user has entered to be used for context-specific search data), and if it is not
     * null we allocate a new {@code Bundle} for {@code appDataBundle} and store {@code queryAppDataString}
     * in it using the key "demo_key". Next we call the {@code Activity} member function {@code startSearch}
     * using {@code queryPrefill} as the initial query, the flag false so that the query is not preselected,
     * {@code appDataBundle} as the application-specific context, and false to flag that it should
     * only launch the search that has been specifically defined by the application. Finally we return
     * true to indicate to the caller that a search has been launched.
     *
     * @return Returns true if search launched, false if activity blocks it
     */
    @Override
    public boolean onSearchRequested() {
        // If your application absolutely must disable search, do it here.
        if (mMenuMode.getSelectedItemPosition() == MENUMODE_DISABLED) {
            return false;
        }

        // It's possible to pre-fill the query string before launching the search
        // UI.  For this demo, we simply copy it from the user input field.
        // For most applications, you can simply pass null to startSearch() to
        // open the UI with an empty query string.
        final String queryPrefill = mQueryPrefill.getText().toString();

        // Next, set up a bundle to send context-specific search data (if any)
        // The bundle can contain any number of elements, using any number of keys;
        // For this Api Demo we copy a string from the user input field, and store
        // it in the bundle as a string with the key "demo_key".
        // For most applications, you can simply pass null to startSearch().
        Bundle appDataBundle = null;
        final String queryAppDataString = mQueryAppData.getText().toString();
        //noinspection ConstantConditions
        if (queryAppDataString != null) {
            appDataBundle = new Bundle();
            appDataBundle.putString("demo_key", queryAppDataString);
        }

        // Now call the Activity member function that invokes the Search Manager UI.
        startSearch(queryPrefill, false, appDataBundle, false);

        // Returning true indicates that we did launch the search, instead of blocking it.
        return true;
    }

    /**
     * Any application that implements search suggestions based on previous actions (such as
     * recent queries, page/items viewed, etc.) should provide a way for the user to clear the
     * history.  This gives the user a measure of privacy, if they do not wish for their recent
     * searches to be replayed by other users of the device (via suggestions).
     * <p>
     * This example shows how to clear the search history for apps that use
     * android.provider.SearchRecentSuggestions.  If you have developed a custom suggestions
     * provider, you'll need to provide a similar API for clearing history.
     * <p>
     * In this sample app we call this method from a "Clear History" menu item.  You could also
     * implement the UI in your preferences, or any other logical place in your UI.
     *
     * First we create an instance of {@code SearchRecentSuggestions suggestions} configured to
     * use {@code SearchSuggestionSampleProvider.AUTHORITY} ("com.example.android.apis.SuggestionProvider"),
     * and SearchSuggestionSampleProvider.MODE (DATABASE_MODE_QUERIES - configures the database to record
     * recent queries - required). Then we instruct {@code suggestions} to perform a clear history
     * operation.
     */
    private void clearSearchHistory() {
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                SearchSuggestionSampleProvider.AUTHORITY, SearchSuggestionSampleProvider.MODE);
        suggestions.clearHistory();
    }

}

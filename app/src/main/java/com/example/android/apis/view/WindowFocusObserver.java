/*
 * Copyright (C) 2011 The Android Open Source Project
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
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowId;
import android.view.WindowId.FocusObserver;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.view.MenuItemCompat;

import com.example.android.apis.R;

/**
 * Implements a WindowId.FocusObserver() whose onFocusGained(WindowId) merely prints "Gained focus",
 * and whose onFocusLost(WindowId) prints "Lost focus". Nothing happens on a touch only screen of course.
 */
@SuppressLint("SetTextI18n")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class WindowFocusObserver extends AppCompatActivity implements SearchView.OnQueryTextListener {
    /**
     * {@code TextView} in our layout file that we use to write focus change status to in our
     * {@code FocusObserver mObserver}: either "Gained focus" when our {@code onFocusGained}
     * override is called, or "Lost focus" when our {@code onFocusLost} override is called.
     */
    TextView mState;

    /**
     * Our {@code FocusObserver}, our {@code onAttachedToWindow} override registers it to listen for
     * focus changes on our window by calling the {@code registerFocusObserver} method of the
     * {@code WindowId} of our main content view.
     */
    final FocusObserver mObserver = new WindowId.FocusObserver() {
        /**
         * Called when one of the monitored windows gains input focus. We simply set the text of
         * {@code TextView mState} to "Gained focus".
         *
         * @param token {@code WindowId} of the window which has gained focus.
         */
        @Override
        public void onFocusGained(WindowId token) {
            mState.setText("Gained focus");
        }

        /**
         * Called when one of the monitored windows loses input focus. We simply set the text of
         * {@code TextView mState} to "Lost focus".
         *
         * @param token {@code WindowId} of the window which has lost focus.
         */
        @Override
        public void onFocusLost(WindowId token) {
            mState.setText("Lost focus");
        }
    };

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.window_focus_observer.
     * Finally we initialize our field {@code TextView mState} by finding the view with ID R.id.focus_state.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.window_focus_observer);

        mState = findViewById(R.id.focus_state);
    }

    /**
     * Initialize the contents of the Activity's standard options menu. First we initialize
     * {@code MenuInflater inflater} with a new instance for this context and use it to inflate the
     * menu file R.menu.content_actions into {@code Menu menu}. We initialize {@code SearchView searchView}
     * by finding the menu item with ID R.id.action_search and then retrieving the currently set action
     * view for this menu item. We then set the {@code OnQueryTextListener} of {@code searchView} to "this".
     * <p>
     * We initialize {@code MenuItem actionItem} by finding the menu item with the ID
     * R.id.menu_item_share_action_provider_action_bar, and initialize {@code ShareActionProvider actionProvider}
     * by using {@code actionItem} to get its action provider. We set the file name of a file for persisting
     * the share history of {@code actionProvider} to DEFAULT_SHARE_HISTORY_FILE_NAME. We create
     * {@code Intent shareIntent} with action ACTION_SEND, and set its type to "image&#8260;&#42;".
     * We create {@code Uri uri} from the path for the file with the name "shared.png" in our file
     * system (file:///data/user/0/com.example.android.apis/files/shared.png). We include {@code uri}
     * as an extra in {@code shareIntent} using the key EXTRA_STREAM ( "android.intent.extra.STREAM").
     * We then set the share intent of {@code actionProvider} to {@code shareIntent} and return true
     * to the caller.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.content_actions, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(this);

        // Set file with share history to the provider and set the share intent.
        MenuItem actionItem = menu.findItem(R.id.menu_item_share_action_provider_action_bar);
        ShareActionProvider actionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(actionItem);
        actionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        // Note that you can set/change the intent any time,
        // say when the user has selected an image.
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        Uri uri = Uri.fromFile(getFileStreamPath("shared.png"));
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        actionProvider.setShareIntent(shareIntent);
        return true;
    }

    /**
     * This is referenced using the attribute android:onClick="onSort" in our menu xml file:
     * menu/content_actions.xml, but does absolutely nothing.
     *
     * @param item {@code MenuItem} which has been selected
     */
    public void onSort(MenuItem item) {
    }

    /**
     * Called when the query text is changed by the user. We return true having done nothing.
     *
     * @param newText the new content of the query text field.
     * @return false if the SearchView should perform the default action of showing any
     * suggestions if available, true if the action was handled by the listener.
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        return true;
    }

    /**
     * Called when the user submits the query. We simply toast the contents of the query string and
     * return true to the caller.
     *
     * @param query the query text that is to be submitted
     * @return true if the query has been handled by the listener, false to let the
     * SearchView perform the default action.
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        Toast.makeText(this, "Searching for: " + query + "...", Toast.LENGTH_SHORT).show();
        return true;
    }

    /**
     * Called when the main window associated with the activity has been attached to the window
     * manager. First we call through to our super's implementation of {@code onAttachedToWindow}.
     * Then we initialize {@code WindowId token} with the {@code WindowId} of the the top-level window
     * decor view of the current window of this activity. We then register {@code FocusObserver mObserver}
     * to start monitoring for changes in the focus state of {@code token}. If {@code token} is currently
     * focused we set the text of {@code TextView mState} to the string "Focused", otherwise we set it
     * to "Not focused".
     */
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        WindowId token = getWindow().getDecorView().getWindowId();
        token.registerFocusObserver(mObserver);
        mState.setText(token.isFocused() ? "Focused" : "Not focused");
    }

    /**
     * Called when the main window associated with the activity has been detached from the window
     * manager. First we call through to our super's implementation of {@code onDetachedFromWindow}.
     * Then we retrieve the current Window for the activity, use it to fetch its decor view, fetch
     * the {@code WindowId} of the decor view, and unregister {@code FocusObserver mObserver} as a
     * {@code FocusObserver} on that window.
     */
    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getWindow().getDecorView().getWindowId().unregisterFocusObserver(mObserver);
    }
}

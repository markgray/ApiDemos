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
package com.example.android.apis.view

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.WindowId
import android.view.WindowId.FocusObserver
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.ShareActionProvider
import androidx.core.view.MenuItemCompat
import com.example.android.apis.R

/**
 * Implements a WindowId.FocusObserver() whose onFocusGained(WindowId) merely prints "Gained focus",
 * and whose onFocusLost(WindowId) prints "Lost focus". When menu items are clicked or the app is
 * put in the background the window loses focus even on a touch only device.
 */
@SuppressLint("SetTextI18n")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class WindowFocusObserver : AppCompatActivity(), SearchView.OnQueryTextListener {
    /**
     * [TextView] in our layout file that we use to write focus status to from our [FocusObserver]
     * field [mObserver]: either "Gained focus" when our [FocusObserver.onFocusGained] override is
     * called, or "Lost focus" when our [FocusObserver.onFocusLost] override is called.
     */
    var mState: TextView? = null

    /**
     * Our [FocusObserver], our [onAttachedToWindow] override registers it to listen for
     * focus changes on our window by calling the `registerFocusObserver` method of the
     * [WindowId] of our main content view.
     */
    private val mObserver: FocusObserver = object : FocusObserver() {
        /**
         * Called when one of the monitored windows gains input focus. We simply set the text of
         * [TextView] field [mState] to "Gained focus".
         *
         * @param token [WindowId] of the window which has gained focus.
         */
        override fun onFocusGained(token: WindowId) {
            mState!!.text = "Gained focus"
        }

        /**
         * Called when one of the monitored windows loses input focus. We simply set the text of
         * [TextView] field [mState] to "Lost focus".
         *
         * @param token [WindowId] of the window which has lost focus.
         */
        override fun onFocusLost(token: WindowId) {
            mState!!.text = "Lost focus"
        }
    }

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.window_focus_observer.
     * Finally we initialize our [TextView] field [mState] by finding the view with ID R.id.focus_state.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.window_focus_observer)
        mState = findViewById(R.id.focus_state)
    }

    /**
     * Initialize the contents of the Activity's standard options menu. First we initialize
     * [MenuInflater] `val inflater` with a new instance for this context and use it to inflate the
     * menu file R.menu.content_actions into [Menu] parameter [menu]. We initialize [SearchView]
     * `val searchView` by finding the menu item with ID R.id.action_search and then retrieving the
     * currently set action view for this menu item. We then set the [SearchView.OnQueryTextListener]
     * of `searchView` to "this"
     *
     * We initialize [MenuItem] `val actionItem` by finding the menu item with the ID
     * R.id.menu_item_share_action_provider_action_bar, and initialize [ShareActionProvider]
     * `val actionProvider` by using `actionItem` to get its action provider. We set the file name
     * of a file for persisting the share history of `actionProvider` to DEFAULT_SHARE_HISTORY_FILE_NAME.
     * We create [Intent] `val shareIntent` with action ACTION_SEND, and set its type to
     * "image&#8260;&#42;". We create [Uri] `val uri` from the path for the file with the name
     * "shared.png" in our file system (file:///data/user/0/com.example.android.apis/files/shared.png).
     * We include `uri` as an extra in `shareIntent` using the key EXTRA_STREAM
     * ("android.intent.extra.STREAM"). We then set the share intent of `actionProvider` to
     * `shareIntent` and return true to the caller.
     *
     * @param menu The options [Menu] in which you place your items.
     * @return You must return true for the menu to be displayed;
     *         if you return false it will not be shown.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.content_actions, menu)
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.setOnQueryTextListener(this)

        // Set file with share history to the provider and set the share intent.
        val actionItem = menu.findItem(R.id.menu_item_share_action_provider_action_bar)
        val actionProvider = MenuItemCompat.getActionProvider(actionItem) as ShareActionProvider
        actionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME)
        // Note that you can set/change the intent any time,
        // say when the user has selected an image.
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/*"
        val uri = Uri.fromFile(getFileStreamPath("shared.png"))
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        actionProvider.setShareIntent(shareIntent)
        return true
    }

    /**
     * This is referenced using the attribute android:onClick="onSort" in our menu xml file:
     * menu/content_actions.xml, but does absolutely nothing.
     *
     * @param item `MenuItem` which has been selected
     */
    @Suppress("UNUSED_PARAMETER")
    fun onSort(item: MenuItem?) {
    }

    /**
     * Called when the query text is changed by the user. We return true having done nothing.
     *
     * @param newText the new content of the query text field.
     * @return false if the SearchView should perform the default action of showing any
     *         suggestions if available, true if the action was handled by the listener.
     */
    override fun onQueryTextChange(newText: String): Boolean {
        return true
    }

    /**
     * Called when the user submits the query. We simply toast the contents of the query string and
     * return true to the caller.
     *
     * @param query the query text that is to be submitted
     * @return true if the query has been handled by the listener, false to let the
     *         SearchView perform the default action.
     */
    override fun onQueryTextSubmit(query: String): Boolean {
        Toast.makeText(this, "Searching for: $query...", Toast.LENGTH_SHORT).show()
        return true
    }

    /**
     * Called when the main window associated with the activity has been attached to the window
     * manager. First we call through to our super's implementation of `onAttachedToWindow`.
     * Then we initialize [WindowId] `val token` with the [WindowId] of the the top-level window
     * decor view of the current window of this activity. We then register [FocusObserver] field
     * [mObserver] to start monitoring for changes in the focus state of `token`. If `token` is
     * currently focused we set the text of [TextView] field [mState] to the string "Focused",
     * otherwise we set it to "Not focused".
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val token = window.decorView.windowId
        token.registerFocusObserver(mObserver)
        mState!!.text = if (token.isFocused) "Focused" else "Not focused"
    }

    /**
     * Called when the main window associated with the activity has been detached from the window
     * manager. First we call through to our super's implementation of `onDetachedFromWindow`.
     * Then we retrieve the current Window for the activity, use it to fetch its decor view, fetch
     * the [WindowId] of the decor view, and unregister [FocusObserver] field [mObserver] as a
     * [FocusObserver] on that window.
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        window.decorView.windowId.unregisterFocusObserver(mObserver)
    }
}
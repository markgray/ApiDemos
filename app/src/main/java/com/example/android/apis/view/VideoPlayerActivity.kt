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
@file:Suppress("DEPRECATION", "UNUSED_PARAMETER", "MemberVisibilityCanBePrivate", "RedundantOverride")

package com.example.android.apis.view

import android.annotation.TargetApi
import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnSystemUiVisibilityChangeListener
import android.view.Window
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.ShareActionProvider
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.FragmentTransaction
import androidx.appcompat.widget.AppCompatImageView
import com.example.android.apis.R

/**
 * This activity demonstrates how to use system UI flags to implement
 * a video player style of UI (where the navigation bar should be hidden
 * when the user isn't interacting with the screen to achieve full screen
 * video playback). Uses system UI flags to transition in and out of modes
 * where the entire screen can be filled with content (at the expense of
 * no user interaction).
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class VideoPlayerActivity :
        AppCompatActivity(),
        SearchView.OnQueryTextListener,
        ActionBar.TabListener
{
    /**
     * Implementation of a view for displaying full-screen video playback,
     * using system UI flags to transition in and out of modes where the entire
     * screen can be filled with content (at the expense of no user interaction).
     */
    class Content(context: Context?, attrs: AttributeSet?) :
            AppCompatImageView(context, attrs),
            OnSystemUiVisibilityChangeListener,
            View.OnClickListener,
            ActionBar.OnMenuVisibilityListener
    {
        /**
         * [Activity] of the containing activity that is passed to our [init] method.
         * ("this" when called from the `onCreate` method of [VideoPlayerActivity].
         */
        var mActivity: Activity? = null

        /**
         * [TextView] in the [VideoPlayerActivity] layout file R.layout.video_player with the id
         * R.layout.video_player (the text reads "A title goes here"), we use it just to switch
         * its visibility to VISIBLE or INVISIBLE in our [setNavVisibility] method.
         */
        var mTitleView: TextView? = null

        /**
         * [Button] in the [VideoPlayerActivity] layout file R.layout.video_player with the id
         * R.id.play we use it to switch its text to "Play" of "Pause" in our [setPlayPaused]
         * method, and to make it VISIBLE or INVISIBLE in our [setNavVisibility] method.
         */
        var mPlayButton: Button? = null

        /**
         * [SeekBar] in the [VideoPlayerActivity] layout file R.layout.video_player with the id
         * R.id.seekbar, we use it just to switch its visibility to VISIBLE or INVISIBLE in our
         * [setNavVisibility] method.
         */
        var mSeekView: SeekBar? = null

        /**
         * Flag to indicate whether we have added "this" as an `OnMenuVisibilityListener` to the
         * action bar (which we do in our [onAttachedToWindow] override). If it is true we call
         * `removeOnMenuVisibilityListener` in our [onDetachedFromWindow] override to remove us.
         */
        var mAddedMenuListener = false

        /**
         * Flag to indicate that the menus are currently open, it is set in [onMenuVisibilityChanged]
         * to its parameter, and if it is true our [setNavVisibility] will not schedule [Runnable]
         * field [mNavHider] to auto hide our navigation UI.
         */
        var mMenusOpen = false

        /**
         * Paused flag, when true navigation UI is displayed.
         */
        var mPaused = false

        /**
         * Unused
         */
        @Suppress("unused")
        var mNavVisible = false

        /**
         * Last system UI visibility mask, received by [onSystemUiVisibilityChange] override.
         */
        var mLastSystemUiVis = 0

        /**
         * [Runnable] which makes system UI visibility go away after 3000ms when play is
         * resumed, its running is scheduled in our [setNavVisibility] method
         */
        var mNavHider = Runnable { setNavVisibility(false) }

        /**
         * Called by our containing [Activity] to initialize our fields with information about
         * the state of the video player that we will interact with. We save our [Activity]
         * parameter [activity] in our [Activity] field [mActivity], our [TextView] parameter
         * [title] in our [TextView] field [mTitleView], our [Button] parameter [playButton] in
         * our [Button] field [mPlayButton], and our [SeekBar] parameter [seek] in our [SeekBar]
         * field [mSeekView]. We set the `OnClickListener` of [mPlayButton] to "this", and call
         * our [setPlayPaused] method with the argument true to initialize our UI to the paused
         * state.
         *
         * @param activity   [Activity] we use to fetch the action bar
         * @param title      [TextView] containing the title we are playing
         * @param playButton [Button] that toggles between play and paused states
         * @param seek       [SeekBar] in the layout file we are contained in
         */
        fun init(activity: Activity?, title: TextView?, playButton: Button?, seek: SeekBar?) {
            // This called by the containing activity to supply the surrounding
            // state of the video player that it will interact with.
            mActivity = activity
            mTitleView = title
            mPlayButton = playButton
            mSeekView = seek
            mPlayButton!!.setOnClickListener(this)
            setPlayPaused(true)
        }

        /**
         * This is called when the view is attached to a window. First we call our super's
         * implementation of `onAttachedToWindow`. Then if our [Activity] field [mActivity]
         * is not null we set our flag [mAddedMenuListener] to true, use [mActivity] to get a
         * reference to the action bar (we have to cast it first to [AppCompatActivity] to use
         * the `getSupportActionBar` method) and use this reference to register "this" as an
         * `OnMenuVisibilityListener`.
         */
        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            if (mActivity != null) {
                mAddedMenuListener = true
                (mActivity as AppCompatActivity).supportActionBar!!.addOnMenuVisibilityListener(this)
            }
        }

        /**
         * This is called when the view is detached from a window. First we call our super's
         * implementation of `onDetachedFromWindow`. Then if our flag [mAddedMenuListener] is
         * true, we use [mActivity] to get a reference to the action bar (we have to cast it
         * first to [AppCompatActivity] to use the `getSupportActionBar` method) and use this
         * reference to remove "this" as an `OnMenuVisibilityListener`.
         */
        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            if (mAddedMenuListener) {
                (mActivity as AppCompatActivity).supportActionBar!!.removeOnMenuVisibilityListener(this)
            }
        }

        /**
         * Called when the status bar changes visibility. We initialize [Int] variable `val diff`
         * to the bits that have changed by bitwise exclusive or'ing our [Int] parameter [visibility]
         * with our field [mLastSystemUiVis], then set [mLastSystemUiVis] to [visibility]. If the bit
         * that changed is SYSTEM_UI_FLAG_HIDE_NAVIGATION, and the new value of the bit in [visibility]
         * is equal to 0, we call our method [setNavVisibility] with the argument true in order to
         * make our navigation UI visible, and to schedule [Runnable] field [mNavHider] to run in
         * 3000ms to make it invisible.
         *
         * @param visibility current system UI visibility mask, Bitwise-or of the bit flags
         * SYSTEM_UI_FLAG_LOW_PROFILE, SYSTEM_UI_FLAG_HIDE_NAVIGATION, and
         * SYSTEM_UI_FLAG_FULLSCREEN.
         */
        override fun onSystemUiVisibilityChange(visibility: Int) {
            // Detect when we go out of nav-hidden mode, to clear our state
            // back to having the full UI chrome up.  Only do this when
            // the state is changing and nav is no longer hidden.
            val diff = mLastSystemUiVis xor visibility
            mLastSystemUiVis = visibility
            if (diff and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION != 0
                    && visibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION == 0) {
                setNavVisibility(true)
            }
        }

        /**
         * Called when the window containing has changed its visibility (between GONE, INVISIBLE,
         * and VISIBLE). First we call our super's implementation of `onWindowVisibilityChanged`
         * then we call our method [setPlayPaused] with true as the argument in order to pause
         * play (when we become visible or invisible, play is paused).
         *
         * @param visibility The new visibility of the window.
         */
        override fun onWindowVisibilityChanged(visibility: Int) {
            super.onWindowVisibilityChanged(visibility)

            // When we become visible or invisible, play is paused.
            setPlayPaused(true)
        }

        /**
         * Called when a [View] we are registered as an `OnClickListener` for is clicked.
         * If the [View] parameter [v] that was clicked is [Button] field [mPlayButton] we
         * call our method [setPlayPaused] with the inverse of [Boolean] field [mPaused] to
         * toggle our play/pause state, otherwise we call our method [setNavVisibility] with
         * true as the argument to make the navigation visible.
         *
         * @param v [View] that was clicked
         */
        override fun onClick(v: View) {
            if (v === mPlayButton) {
                // Clicking on the play/pause button toggles its state.
                setPlayPaused(!mPaused)
            } else {
                // Clicking elsewhere makes the navigation visible.
                setNavVisibility(true)
            }
        }

        /**
         * Called when an action bar menu is shown or hidden. We save our [Boolean] parameter
         * [isVisible] in our field [mMenusOpen], then call our method [setNavVisibility] with
         * the argument true to make the navigation visible.
         *
         * @param isVisible True if an action bar menu is now visible, false if no action bar
         * menus are visible.
         */
        override fun onMenuVisibilityChanged(isVisible: Boolean) {
            mMenusOpen = isVisible
            setNavVisibility(true)
        }

        /**
         * Called to change state to paused if its [Boolean] parameter [paused] is true, or to play
         * if it is false. First we save [paused] in our field [mPaused]. If [mPaused] is true we
         * set the text of our [Button] field [mPlayButton] to the string with the resource id
         * R.string.play ("Play"), if false we set it to the string with the resource id
         * R.string.pause ("Pause"). We call our method [setKeepScreenOn] with the inverse of
         * [paused] to keep our screen on if we are now in play state, or to allow it to go off
         * if we are now in paused state. Finally we call our method [setNavVisibility] true to
         * make the navigation visible.
         *
         * @param paused if true move to the paused state, if false move to the play state
         */
        fun setPlayPaused(paused: Boolean) {
            mPaused = paused
            mPlayButton!!.setText(if (paused) R.string.play else R.string.pause)
            keepScreenOn = !paused
            setNavVisibility(true)
        }

        /**
         * Called to make our navigation visible if its [Boolean] parameter [visible] is true, or
         * to hide it if it is false. We initialize our [Int] variable `var newVis` by or'ing
         * together the following bit flags:
         *
         *  * SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN - View would like its window to be laid out as if it
         *  has requested SYSTEM_UI_FLAG_FULLSCREEN, even if it currently hasn't. This allows it
         *  to avoid artifacts when switching in and out of that mode, at the expense that some
         *  of its user interface may be covered by screen decorations when they are shown.
         *
         *  * SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION - View would like its window to be laid out
         *  as if it has requested SYSTEM_UI_FLAG_HIDE_NAVIGATION, even if it currently hasn't.
         *  This allows it to avoid artifacts when switching in and out of that mode, at the
         *  expense that some of its user interface may be covered by screen decorations when
         *  they are shown.
         *
         *  * SYSTEM_UI_FLAG_LAYOUT_STABLE - When using other layout flags, we would like a
         *  stable view of the content insets given to fitSystemWindows(Rect). This means that
         *  the insets seen there will always represent the worst case that the application
         *  can expect as a continuous state.
         *
         * Then [visible] is false (we want to hide the navigation) we or `newVis` with the
         * following bit flags:
         *
         *  * SYSTEM_UI_FLAG_LOW_PROFILE - View requests the system UI to enter an unobtrusive
         *  "low profile" mode. In low profile mode, the status bar and/or navigation icons
         *  may dim.
         *
         *  * SYSTEM_UI_FLAG_FULLSCREEN - View requests to go into the normal fullscreen mode
         *  so that its content can take over the screen while still allowing the user to
         *  interact with the application.
         *
         *  * SYSTEM_UI_FLAG_HIDE_NAVIGATION - View requests that the system navigation be
         *  temporarily hidden.
         *
         * Then if [visible] is true, we initialize `Handler` variable `val h` with a handler
         * associated with the thread running the [View]. If `h` is not null, we remove all
         * scheduled runs of [Runnable] field [mNavHider] from its queue. If [mMenusOpen] is
         * false (no menus are open), and [mPaused] is false (we are in play state) we use `h`
         * to schedule [Runnable] field [mNavHider] to run in 3000ms to hide the navigation again.
         *
         * We now set the system UI visibility to `newVis`, and set the visibility of the views
         * [mTitleView], [mPlayButton], and [mSeekView] to VISIBLE if our parameter [visible] is
         * true, or to INVISIBLE if it is false.
         *
         * @param visible if true we make our navigation visible, if false we hide the navigation.
         */
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        fun setNavVisibility(visible: Boolean) {
            var newVis = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            if (!visible) {
                newVis = newVis or (View.SYSTEM_UI_FLAG_LOW_PROFILE
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
            }

            // If we are now visible, schedule a timer for us to go invisible.
            if (visible) {
                val h = handler
                if (h != null) {
                    h.removeCallbacks(mNavHider)
                    if (!mMenusOpen && !mPaused) {
                        // If the menus are open or play is paused, we will not auto-hide.
                        h.postDelayed(mNavHider, 3000)
                    }
                }
            }

            // Set the new desired visibility.
            systemUiVisibility = newVis
            mTitleView!!.visibility = if (visible) View.VISIBLE else View.INVISIBLE
            mPlayButton!!.visibility = if (visible) View.VISIBLE else View.INVISIBLE
            mSeekView!!.visibility = if (visible) View.VISIBLE else View.INVISIBLE
        }

        /**
         * Init block of our constructor called when we are inflated from XML. We register this
         * as a `OnSystemUiVisibilityChangeListener`, and a `OnClickListener`.
         */
        init {
            setOnSystemUiVisibilityChangeListener(this)
            setOnClickListener(this)
        }
    }

    /**
     * Our [Content] instance.
     */
    var mContent: Content? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we request the window feature FEATURE_ACTION_BAR_OVERLAY (requests an
     * Action Bar that overlays window content), and then we set our content view to our layout file
     * R.layout.video_player. We initialize our [Content] field [mContent] by finding the view
     * with the id R.id.content, then call its `init` method with "this" as the activity, the
     * [TextView] with id R.id.title, the [Button] with id R.id.play, and the [SeekBar] with id
     * R.id.seekbar. We initialize [ActionBar] variable `val bar` by retrieving a reference to this
     * activity's support [ActionBar], then add 3 tabs to it with the text "Tab 1", "Tab 2", and
     * "Tab 3" and setting their `TabListener` each to this.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY)
        setContentView(R.layout.video_player)
        mContent = findViewById(R.id.content)
        mContent!!.init(this, findViewById(R.id.title),
                findViewById(R.id.play),
                findViewById(R.id.seekbar))
        val bar = supportActionBar
        bar!!.addTab(bar.newTab().setText("Tab 1").setTabListener(this))
        bar.addTab(bar.newTab().setText("Tab 2").setTabListener(this))
        bar.addTab(bar.newTab().setText("Tab 3").setTabListener(this))
    }

    /**
     * Initialize the contents of the Activity's standard options menu. We initialize our `MenuInflater`
     * varible `val inflater` with a `MenuInflater` for this context, then use it to inflate the menu
     * layout file R.menu.content_actions into our [Menu] parameter [menu]. We initialize our
     * [SearchView] variable `val searchView` by finding the menu item with the id R.id.action_search
     * in [menu] and fetching the currently set action view for this menu item. We then set the
     * `OnQueryTextListener` of `searchView` to "this". We initialize our [MenuItem] variable
     * `val actionItem` by finding the menu item in [menu] with the id
     * R.id.menu_item_share_action_provider_action_bar, and use it to initialize our
     * [ShareActionProvider] variable `val actionProvider` by fetching its action provider. We set
     * the file name of the file for persisting the share history of `actionProvider` to the string
     * DEFAULT_SHARE_HISTORY_FILE_NAME ("share_history.xml"). We initialize our [Intent] variable
     * `val shareIntent` to the [Intent] returned by our [createShareIntent] method, and then set
     * the share intent of `actionProvider` to  `shareIntent` and return true to the caller.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed.
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.content_actions, menu)
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.setOnQueryTextListener(this)

        // Set file with share history to the provider and set the share intent.
        val actionItem = menu.findItem(R.id.menu_item_share_action_provider_action_bar)
        val actionProvider = MenuItemCompat.getActionProvider(actionItem) as ShareActionProvider
        actionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME)
        // Note that you can set/change the intent any time,
        // say when the user has selected an image.
        val shareIntent = createShareIntent()
        actionProvider.setShareIntent(shareIntent)
        return true
    }

    /**
     * Creates a sharing [Intent]. We first initalize our [Intent] variable `val shareIntent` with
     * a new instance with the action ACTION_SEND, and add the flag [Intent.FLAG_GRANT_READ_URI_PERMISSION]
     * to it. We initialize our `val b` to a new instance of [Uri.Builder], set the scheme of `b` to
     * "content", and set its authority to our "com.example.android.apis.content.FileProvider"
     * (our content/FileProvider `ContentProvider` which provides access to resources in our
     * apk to other apps). We initialize our `val tv` to a new instance of [TypedValue], and use
     * it to hold the resource data for our raw asset png file with id R.raw.robot. We then append
     * to our [Uri.Builder] `b` the encoded path of the asset cookie of `tv` for asset R.raw.robot
     * followed by the encoded path of the string value for R.raw.robot. We then initialize our
     * `val uri` to the [Uri] that results from building `b`. We next set the mime type of `shareIntent`
     * to "image/png", add `uri` as an extra under the key [Intent.EXTRA_STREAM], and set the clip
     * data of `shareIntent` to a new instance of [ClipData] holding a [Uri] whose `ContentResolver`
     * is a `ContentResolver` instance for our application's package, whose user-visible label for
     * the clip data is "image", and whose [Uri] is our `uri`. Finally we return `shareIntent` to
     * the caller.
     *
     * @return The sharing [Intent].
     */
    private fun createShareIntent(): Intent {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val b: Uri.Builder = Uri.Builder()
        b.scheme("content")
        b.authority("com.example.android.apis.content.FileProvider")
        val tv = TypedValue()
        resources.getValue(R.raw.robot, tv, true)
        b.appendEncodedPath(tv.assetCookie.toString())
        b.appendEncodedPath(tv.string.toString())
        val uri: Uri = b.build()
        shareIntent.type = "image/png"
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.clipData = ClipData.newUri(contentResolver, "image", uri)
        return shareIntent
    }

    /**
     * Called when the main window associated with the activity has been attached to the window
     * manager. We just call our super's implementation of `onAttachedToWindow`.
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    /**
     * Called after [onRestoreInstanceState], [onRestart], or [onPause], for our activity to start
     * interacting with the user. We just call our super's implementation of `onResume`.
     */
    override fun onResume() {
        super.onResume()
    }

    /**
     * This method is referenced in the menu in the attribute android:onClick="onSort".
     */
    fun onSort(item: MenuItem?) {}

    /**
     * This hook is called whenever an item in your options menu is selected. We switch on the item
     * id of our parameter `MenuItem item`:
     *
     *  * R.id.show_tabs "Show Tabs" - We retrieve the action bar for our activity and set its
     *  navigation mode to NAVIGATION_MODE_TABS (Tab navigation mode. Instead of static title
     *  text this mode presents a series of tabs for navigation within the activity). We then
     *  set the item to checked, and return true to the caller to indicate we have consumed
     *  the event.
     *
     *  * R.id.hide_tabs "Hide Tabs" - We retrieve the action bar for our activity and set its
     *  navigation mode to NAVIGATION_MODE_STANDARD (Standard navigation mode. Consists of
     *  either a logo or icon and title text with an optional subtitle. Clicking any of these
     *  elements will dispatch onOptionsItemSelected to the host Activity with a MenuItem
     *  with item ID android.R.id.home). We then set the item to checked, and return true to
     *  the caller to indicate we have consumed the event.
     *
     * If the item id is not one of two above, we return false to the caller to allow normal menu
     * processing to proceed.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.show_tabs -> {
                // noinspection ConstantConditions
                supportActionBar!!.navigationMode = ActionBar.NAVIGATION_MODE_TABS
                item.isChecked = true
                return true
            }
            R.id.hide_tabs -> {
                // noinspection ConstantConditions
                supportActionBar!!.navigationMode = ActionBar.NAVIGATION_MODE_STANDARD
                item.isChecked = true
                return true
            }
        }
        return false
    }

    /**
     * Called when the query text is changed by the user. We just return true to the caller to indicate
     * that we have handled the event.
     *
     * @param newText the new content of the query text field.
     * @return false if the SearchView should perform the default action of showing any
     * suggestions if available, true if the action was handled by the listener.
     */
    override fun onQueryTextChange(newText: String): Boolean {
        return true
    }

    /**
     * Called when the user submits the query. We toast a string formed by concatenating the string
     * "Searching for: " with our [String] parameter [query] followed by the string "...", then
     * return true to the caller to indicate that we have handled the event.
     *
     * @param query the query text that is to be submitted
     * @return true if the query has been handled by the listener, false to let the
     * SearchView perform the default action.
     */
    override fun onQueryTextSubmit(query: String): Boolean {
        Toast.makeText(this, "Searching for: $query...", Toast.LENGTH_SHORT).show()
        return true
    }

    /**
     * Called when a tab enters the selected state. We ignore.
     *
     * @param tab The tab that was selected
     * @param ft  A [FragmentTransaction] for queuing fragment operations to execute
     * during a tab switch. The previous tab's unselect and this tab's select will be
     * executed in a single transaction. This [FragmentTransaction] does not support
     * being added to the back stack.
     */
    override fun onTabSelected(tab: ActionBar.Tab, ft: FragmentTransaction) {}

    /**
     * Called when a tab exits the selected state. We ignore.
     *
     * @param tab The tab that was unselected
     * @param ft  A [FragmentTransaction] for queuing fragment operations to execute
     * during a tab switch. This tab's unselect and the newly selected tab's select
     * will be executed in a single transaction. This [FragmentTransaction] does not
     * support being added to the back stack.
     */
    override fun onTabUnselected(tab: ActionBar.Tab, ft: FragmentTransaction) {}

    /**
     * Called when a tab that is already selected is chosen again by the user. We ignore.
     *
     * @param tab The tab that was reselected.
     * @param ft  A [FragmentTransaction] for queuing fragment operations to execute
     * once this method returns. This [FragmentTransaction] does not support
     * being added to the back stack.
     */
    override fun onTabReselected(tab: ActionBar.Tab, ft: FragmentTransaction) {}
}
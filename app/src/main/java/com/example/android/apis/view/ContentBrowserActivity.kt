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
@file:Suppress("DEPRECATION")
// TODO: replace all deprecated apis

package com.example.android.apis.view

import android.annotation.TargetApi
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
import android.view.ViewGroup
import android.view.Window
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.ShareActionProvider
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.FragmentTransaction
import com.example.android.apis.R

/**
 * This activity demonstrates how to use system UI flags to implement
 * a content browser style of UI (such as a book reader). Includes "Content",
 * and implementation of a view for displaying immersive content, using system
 * UI flags to transition in and out of modes where the user is focused on
 * that content. When the user clicks, it toggles the visibility of navigation
 * elements.
 * TODO: replace deprecated OnSystemUiVisibilityChangeListener with OnApplyWindowInsetsListener
 */
@Suppress("DEPRECATION", "MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class ContentBrowserActivity : AppCompatActivity(),
    SearchView.OnQueryTextListener,
    ActionBar.TabListener {
    /**
     * Implementation of a view for displaying immersive content, using system UI
     * flags to transition in and out of modes where the user is focused on that
     * content.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    class Content(context: Context, attrs: AttributeSet?) : ScrollView(context, attrs),
        OnSystemUiVisibilityChangeListener,
        View.OnClickListener {
        /**
         * [TextView] we use to display our "content" in (the string with the resource id
         * R.string.alert_dialog_two_buttons2ultra_msg)
         */
        var mText: TextView = TextView(context)

        /**
         * [TextView] that our containing activity uses to display a title, it is toggled between
         * VISIBLE and INVISIBLE to increase usable window space.
         */
        var mTitleView: TextView? = null

        /**
         * [SeekBar] that our containing activity uses to display and control the position of
         * our [ScrollView], it is toggled between VISIBLE and INVISIBLE to increase usable
         * window space.
         */
        var mSeekView: SeekBar? = null

        /**
         * UNUSED
         */
        @Suppress("unused")
        var mNavVisible = false

        /**
         * These are the visibility flags to be given to [setSystemUiVisibility], they are modified
         * by user choice in the menu. It starts out:
         *
         * SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN:
         * View would like its window to be laid out as if it has requested SYSTEM_UI_FLAG_FULLSCREEN,
         * even if it currently hasn't. This allows it to avoid artifacts when switching in and out of
         * that mode, at the expense that some of its user interface may be covered by screen decorations
         * when they are shown.
         *
         * SYSTEM_UI_FLAG_LAYOUT_STABLE:
         * When using other layout flags, we would like a stable view of the content insets given to
         * fitSystemWindows(Rect). This means that the insets seen there will always represent the
         * worst case that the application can expect as a continuous state. In the stock Android UI
         * this is the space for the system bar, nav bar, and status bar, but not more transient
         * elements such as an input method. The stable layout your UI sees is based on the system
         * UI modes you can switch to. That is, if you specify SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN then
         * you will get a stable layout for changes of the SYSTEM_UI_FLAG_FULLSCREEN mode
         */
        var mBaseSystemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        /**
         * Current global UI visibility flags received by our [onSystemUiVisibilityChange] callback.
         */
        var mLastSystemUiVis = 0

        /**
         * [Runnable] that makes the navigation invisible after a delay of 2000ms. Used by our
         * [onWindowVisibilityChanged] callback in order to show our navigation elements briefly
         * before hiding them. Calls our method [setNavVisibility] with the argument `false` to
         * make the navigation views invisible, this includes [TextView] field [mTitleView], and
         * [SeekBar] field [mSeekView] as well as calling [setSystemUiVisibility] to set the system
         * UI visibility to the appropriate state.
         */
        var mNavHider: Runnable = Runnable {
            setNavVisibility(false)
        }

        /**
         * Called by the containing activity to supply the surrounding state of the content browser
         * that it will interact with. We save our [TextView] parameter [title] in our [TextView]
         * field [mTitleView], and [SeekBar] parameter [seek] in [SeekBar] field [mSeekView] then
         * call our method [setNavVisibility] with the argument `true` to make our navigation UI
         * appropriately visible.
         *
         * @param title [TextView] to use for our title in `TextView mTitleView`.
         * @param seek  [SeekBar] to use for our seekbar in `SeekBar mSeekView`.
         */
        fun init(title: TextView?, seek: SeekBar?) {
            // This called by the containing activity to supply the surrounding
            // state of the content browser that it will interact with.
            mTitleView = title
            mSeekView = seek
            setNavVisibility(true)
        }

        /**
         * Called when the status bar changes visibility because of a call to [setSystemUiVisibility].
         * We initialize our [Int] variable `val diff` by xor'ing [mLastSystemUiVis] (the previous
         * visibility mask) with our [Int] parameter [visibility] (the new visibility mask) isolating
         * the bits that have changed state. We then set [mLastSystemUiVis] to [visibility]. If the
         * bit that changed was SYSTEM_UI_FLAG_LOW_PROFILE, and the new value in [visibility] is 0
         * (we have left low profile mode), we call our method [setNavVisibility] with the argument
         * true to make our navigation UI visible.
         *
         * @param visibility Bitwise-or of flags SYSTEM_UI_FLAG_LOW_PROFILE,
         * SYSTEM_UI_FLAG_HIDE_NAVIGATION, and SYSTEM_UI_FLAG_FULLSCREEN.
         */
        @Deprecated("Deprecated in Java")
        override fun onSystemUiVisibilityChange(visibility: Int) {
            // Detect when we go out of low-profile mode, to also go out
            // of full screen.  We only do this when the low profile mode
            // is changing from its last state, and turning off.
            val diff = mLastSystemUiVis xor visibility
            mLastSystemUiVis = visibility
            if (((diff and View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0
                    && (visibility and View.SYSTEM_UI_FLAG_LOW_PROFILE) == 0)) {
                setNavVisibility(true)
            }
        }

        /**
         * Called when the containing window has changed its visibility (between GONE, INVISIBLE,
         * and VISIBLE). First we call our super's implementation of `onWindowVisibilityChanged`,
         * then we call our method [setNavVisibility] with true as the argument to make our
         * navigation UI visible. Finally we get a handler associated with the thread running our
         * view and schedule the [Runnable] field [mNavHider] to run in 2000ms to make the
         * navigation UI invisible.
         *
         * @param visibility The new visibility of the window.
         */
        override fun onWindowVisibilityChanged(visibility: Int) {
            super.onWindowVisibilityChanged(visibility)

            // When we become visible, we show our navigation elements briefly
            // before hiding them.
            setNavVisibility(true)
            handler.postDelayed(mNavHider, 2000)
        }

        /**
         * This is called in response to an internal scroll in this view. We first call through to
         * our super's implementation of `onScrollChanged`, then we call our method [setNavVisibility]
         * with false as the argument to hide the navigation elements.
         *
         * @param l    Current horizontal scroll origin.
         * @param t    Current vertical scroll origin.
         * @param oldl Previous horizontal scroll origin.
         * @param oldt Previous vertical scroll origin.
         */
        override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
            super.onScrollChanged(l, t, oldl, oldt)

            // When the user scrolls, we hide navigation elements.
            setNavVisibility(false)
        }

        /**
         * Called when our view has been clicked. When the user clicks, we toggle the visibility of
         * the navigation elements. We fetch the current system visibility flags to initialize our
         * [Int] variable `val curVis`. Then we call our method [setNavVisibility] with false if the
         * SYSTEM_UI_FLAG_LOW_PROFILE bit in `curVis` is not set and true if it is set (thereby
         * toggling the visibility of the navigation elements).
         *
         * @param v The view that was clicked.
         */
        override fun onClick(v: View) {
            // When the user clicks, we toggle the visibility of navigation elements.
            val curVis = systemUiVisibility
            setNavVisibility((curVis and View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0)
        }

        /**
         * Convenience setter method for our field [mBaseSystemUiVisibility], we just set
         * [mBaseSystemUiVisibility] to our [Int] parameter [visibility].
         *
         * @param visibility new value for `mBaseSystemUiVisibility`
         */
        fun setBaseSystemUiVisibility(visibility: Int) {
            mBaseSystemUiVisibility = visibility
        }

        /**
         * Set our navigation elements visible if our parameter [visible] is true, or invisible
         * if it is false. First we initialize our variable `var newVis` to our field
         * [mBaseSystemUiVisibility]. If our parameter [visible] is false we set the flags
         * SYSTEM_UI_FLAG_LOW_PROFILE and SYSTEM_UI_FLAG_FULLSCREEN in `newVis`. We initialize
         * [Boolean] variable `val changed` to true if `newVis` is the same as the last system
         * UI that was requested using [setSystemUiVisibility] (is this logic inverted?). If
         * `changed` or `visible` is true, we initialize `Handler` variable `val h` with a handler
         * associated with the thread running our View, and if the result is not null we remove any
         * scheduled [Runnable] field [mNavHider] from the queue. We then call [setSystemUiVisibility]
         * to set the system UI visibility to `newVis`, and if `visible` is true we set the visibility
         * of both [TextView] field [mTitleView] and [SeekBar] field [mSeekView] to VISIBLE, or to
         * INVISIBLE if `visible` is false.
         *
         * @param visible true makes our navigation elements visible, false makes them invisible.
         */
        fun setNavVisibility(visible: Boolean) {
            var newVis = mBaseSystemUiVisibility
            if (!visible) {
                newVis = newVis or (View.SYSTEM_UI_FLAG_LOW_PROFILE or View.SYSTEM_UI_FLAG_FULLSCREEN)
            }
            val changed = newVis == systemUiVisibility

            // Un-schedule any pending event to hide navigation if we are
            // changing the visibility, or making the UI visible.
            if (changed || visible) {
                val h = handler
                h?.removeCallbacks(mNavHider)
            }

            // Set the new desired visibility.
            systemUiVisibility = newVis
            mTitleView!!.visibility = if (visible) View.VISIBLE else View.INVISIBLE
            mSeekView!!.visibility = if (visible) View.VISIBLE else View.INVISIBLE
        }

        /**
         * The init block of our constructor that is called when we are inflated from an xml layout
         * file. We initialize our `TextView` field `mText` with a new instance, set its text size
         * to 16dp, set its text to the string with resource id R.string.alert_dialog_two_buttons2ultra_msg
         * (a very long bit of nonsense text), disable its clickable state, set its `OnClickListener`
         * to "this", make its text selectable by the user, then add it our view using `LayoutParams`
         * which specify a width of MATCH_PARENT and a height of WRAP_CONTENT. Finally we register
         * "this" as an `OnSystemUiVisibilityChangeListener`.
         */
        init {
            mText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
            mText.text = context.getString(R.string.alert_dialog_two_buttons2ultra_msg)
            mText.isClickable = false
            mText.setOnClickListener(this)
            mText.setTextIsSelectable(true)
            addView(mText, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            setOnSystemUiVisibilityChangeListener(this)
        }
    }

    /**
     * [Content] instance we use.
     */
    var mContent: Content? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we request the window feature FEATURE_ACTION_BAR_OVERLAY (requests an
     * Action Bar that overlays window content), then we set our content view to our layout file
     * R.layout.content_browser. We initialize our [Content] field [mContent] by finding the view
     * with id R.id.content, and call its `init` method passing it the view with the id R.id.title
     * for its title [TextView] and the view with the id R.id.seekbar for its [SeekBar]. We initialize
     * our [ActionBar] variable `val bar` by retrieving a reference to our activity's support
     * ActionBar, then create and add three tabs to it whose text we set to "Tab 1", "Tab 2", and
     * "Tab 3" and whose `TabListener` we set to "this".
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY)
        setContentView(R.layout.content_browser)
        mContent = findViewById(R.id.content)
        mContent!!.init(findViewById(R.id.title),
            findViewById(R.id.seekbar))
        val bar = supportActionBar
        bar!!.addTab(bar.newTab().setText("Tab 1").setTabListener(this))
        bar.addTab(bar.newTab().setText("Tab 2").setTabListener(this))
        bar.addTab(bar.newTab().setText("Tab 3").setTabListener(this))
    }

    /**
     * Initialize the contents of the Activity's standard options menu. We initialize our `MenuInflater`
     * variable `val inflater` with a `MenuInflater` for this context, then use it to inflate the menu
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
     * @param menu The options [Menu] in which you place your items.
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
    @Suppress("RedundantOverride")
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    /**
     * Called after [onRestoreInstanceState], [onRestart], or [onPause], for ou activity to start
     * interacting with the user. We just call our super's implementation of `onResume`.
     */
    @Suppress("RedundantOverride")
    override fun onResume() {
        super.onResume()
    }

    /**
     * This method is used for the android:onClick method by two of the menu items in the menu. We
     * do nothing.
     *
     * @param item [MenuItem] that has been selected.
     */
    @Suppress("UNUSED_PARAMETER")
    fun onSort(item: MenuItem?) {
    }

    /**
     * This hook is called whenever an item in your options menu is selected. We switch on the item
     * id of our [MenuItem] parameter [item]:
     *
     *  * R.id.show_tabs - we fetch a reference to our action bar and call its `setNavigationMode`
     *  method with the argument NAVIGATION_MODE_TABS to have it display tabs. We then call the
     *  `setChecked` method of [item] to make sure the `CheckBox` is checked and return true to the
     *  caller to signal that we have consumed the event.
     *
     *  * R.id.hide_tabs - we fetch a reference to our action bar and call its `setNavigationMode`
     *  method with the argument NAVIGATION_MODE_STANDARD to have it hide the tabs. We then call the
     *  `setChecked` method of [item] to make sure the `CheckBox` is checked and  return true to the
     *  caller to signal that we have consumed the event.
     *
     *  * R.id.stable_layout - we toggle the checked state of [item], then we call the
     *  `setBaseSystemUiVisibility` of our [Content] field [mContent] with the bitmask
     *  formed by or'ing SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN with SYSTEM_UI_FLAG_LAYOUT_STABLE if
     *  the [item] is now checked, or the bit flag SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN if it
     *  is not. Then we return true to the caller to signal that we have consumed the event.
     *
     * If the item selected is not one of the three above, we return false to the caller to allow
     * normal menu processing to proceed.
     *
     * @param item The menu item that was selected.
     * @return Return false to allow normal menu processing to proceed, true to consume it here.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
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
            R.id.stable_layout -> {
                item.isChecked = !item.isChecked
                mContent!!.setBaseSystemUiVisibility(if (item.isChecked) (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE) else View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
                return true
            }
        }
        return false
    }

    /**
     * Called when the query text is changed by the user. We just return true signaling that we have
     * consumed the event.
     *
     * @param newText the new content of the query text field.
     * @return false if the [SearchView] should perform the default action of showing any
     * suggestions if available, true if the action was handled by the listener.
     */
    override fun onQueryTextChange(newText: String): Boolean {
        return true
    }

    /**
     * Called when the user submits the query. We construct and show a toast displaying the string
     * formed by concatenating the string "Searching for: " with our [String] parameter [query]
     * followed by the string "...", then we return true to the caller to signal that we have handled
     * the query.
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
     * during a tab switch. The previous tab's un-select and this tab's select will be
     * executed in a single transaction. This [FragmentTransaction] does not support
     * being added to the back stack.
     */
    override fun onTabSelected(tab: ActionBar.Tab, ft: FragmentTransaction) {}

    /**
     * Called when a tab exits the selected state. We ignore.
     *
     * @param tab The tab that was unselected
     * @param ft  A [FragmentTransaction] for queuing fragment operations to execute
     * during a tab switch. This tab's un-select and the newly selected tab's select
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
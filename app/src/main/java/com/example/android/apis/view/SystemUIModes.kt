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

package com.example.android.apis.view

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnSystemUiVisibilityChangeListener
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.ShareActionProvider
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.FragmentTransaction
import com.example.android.apis.R

/**
 * This activity demonstrates some of the available ways to reduce the size or visual contrast of
 * the system decor, in order to better focus the user's attention or use available screen real
 * estate on the task at hand. Uses CheckBox'es to set or unset the various flags passed to
 * View.setSystemUiVisibility for the IV extends ImageView which serves as the background in the
 * FrameLayout holding it and the CheckBox'es which overlay it.
 * TODO: replace deprecated OnSystemUiVisibilityChangeListener with OnApplyWindowInsetsListener
 * TODO: replace deprecated Action bar navigation using tabs
 * TODO: replace SYSTEM_UI_FLAG_* with WindowInsetsController
 */
@Suppress("UNUSED_PARAMETER", "MemberVisibilityCanBePrivate", "UNUSED_ANONYMOUS_PARAMETER", "RedundantOverride")
@TargetApi(Build.VERSION_CODES.KITKAT)
open class SystemUIModes :
    AppCompatActivity(),
    SearchView.OnQueryTextListener,
    ActionBar.TabListener
{
    /**
     * `ImageView` which is used as the background of our window.
     */
    class IV : AppCompatImageView, OnSystemUiVisibilityChangeListener {
        /**
         * [SystemUIModes] activity containing us. We use it to access its methods in
         * several places.
         */
        private var mActivity: SystemUIModes? = null

        /**
         * [ActionMode] which the user can select to be displayed using a checkbox.
         */
        private var mActionMode: ActionMode? = null

        /**
         * Our constructor. We just call our super's constructor. UNUSED
         *
         * @param context The [Context] the view is running in, through which it can access the
         * current theme, resources, etc.
         */
        constructor(context: Context?) : super(context!!)

        /**
         * Constructor which is called when our view is being inflated from an xml file. We just call
         * our super's constructor.
         *
         * @param context The [Context] the view is running in, through which it can access the
         *                current theme, resources, etc.
         * @param attrs   The attributes of the XML tag that is inflating the view.
         */
        constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)

        /**
         * Setter for our [SystemUIModes] field [mActivity]. We register "this" as a
         * [OnSystemUiVisibilityChangeListener], then save our [SystemUIModes] parameter
         * [act] in our field [mActivity].
         *
         * @param act [SystemUIModes] instance which is containing us.
         */
        fun setActivity(act: SystemUIModes?) {
            setOnSystemUiVisibilityChangeListener(this)
            mActivity = act
        }

        /**
         * This is called during layout when the size of this view has changed. We just call the
         * `refreshSizes` method of our [SystemUIModes] containing activity [mActivity] which
         * displays the new display metrics in a [TextView].
         *
         * @param w    Current width of this view.
         * @param h    Current height of this view.
         * @param oldw Old width of this view.
         * @param oldh Old height of this view.
         */
        public override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            mActivity!!.refreshSizes()
        }

        /**
         * Called when the status bar changes visibility. We call the `updateCheckControls` method
         * of our containing [SystemUIModes] activity [mActivity] which updates the checked state
         * of its CheckBoxes and the `refreshSizes` methods of our [mActivity] which displays the
         * new display metrics in a [TextView].
         *
         * @param visibility Bitwise-or of flags SYSTEM_UI_FLAG_LOW_PROFILE,
         * SYSTEM_UI_FLAG_HIDE_NAVIGATION and SYSTEM_UI_FLAG_FULLSCREEN.
         */
        @Deprecated("Deprecated in Java")
        override fun onSystemUiVisibilityChange(visibility: Int) {
            mActivity!!.updateCheckControls()
            mActivity!!.refreshSizes()
        }

        /**
         * [ActionMode.Callback] for the [ActionMode] which the user can choose to
         * display using a checkbox.
         */
        private inner class MyActionModeCallback : ActionMode.Callback {
            /**
             * Called when action mode is first created. The menu supplied will be used to generate
             * action buttons for the action mode. First we set the title of our [ActionMode]
             * parameter [mode] to the string "My Action Mode!", set its subtitle to null, and
             * "hint" that the title is not optional. Then we add to our [Menu] parameter [menu] a
             * menu item with the title "Sort By Size", whose icon we set to
             * android.R.drawable.ic_menu_sort_by_size, and a menu item with the title "Sort By Alpha",
             * whose icon we set to android.R.drawable.ic_menu_sort_alphabetically. Finally we return
             * true to the caller to indicate that the action mode should be created.
             *
             * @param mode [ActionMode] being created
             * @param menu [Menu] used to populate action buttons
             * @return true if the action mode should be created, false if entering this
             * mode should be aborted.
             */
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                mode.title = "My Action Mode!"
                mode.subtitle = null
                mode.titleOptionalHint = false
                menu.add("Sort By Size").setIcon(android.R.drawable.ic_menu_sort_by_size)
                menu.add("Sort By Alpha").setIcon(android.R.drawable.ic_menu_sort_alphabetically)
                return true
            }

            /**
             * Called to refresh an action mode's action menu whenever it is invalidated. We ignore.
             *
             * @param mode ActionMode being prepared
             * @param menu Menu used to populate action buttons
             * @return true if the menu or action mode was updated, false otherwise.
             */
            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                return true
            }

            /**
             * Called to report a user click on an action button. We return true to indicate that we
             * consumed the event.
             *
             * @param mode The current [ActionMode]
             * @param item The item that was clicked
             * @return true if this callback handled the event, false if the standard [MenuItem]
             * invocation should continue.
             */
            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                return true
            }

            /**
             * Called when an action mode is about to be exited and destroyed. We set our [ActionMode]
             * field [mActionMode] to null, and call the `clearActionMode` method of our [SystemUIModes]
             * field [mActivity] to un-check the checkbox with the id R.id.windowActionMode.
             *
             * @param mode The current ActionMode being destroyed
             */
            override fun onDestroyActionMode(mode: ActionMode) {
                mActionMode = null
                mActivity!!.clearActionMode()
            }
        }

        /**
         * Called when the checkbox with the id R.id.windowActionMode is checked to create a new
         * [MyActionModeCallback] and use it to start an action mode using that [ActionMode.Callback]
         * to control the lifecycle of the action mode.
         */
        fun startActionMode() {
            if (mActionMode == null) {
                val cb: ActionMode.Callback = MyActionModeCallback()
                mActionMode = startActionMode(cb)
            }
        }

        /**
         * Called when the checkbox with the id R.id.windowActionMode is un-checked. If our [ActionMode]
         * field [mActionMode] is not null, we call its `finish` method to finish and close this action
         * mode. The action mode's [ActionMode.Callback] will have its `onDestroyActionMode` method
         * called.
         */
        fun stopActionMode() {
            if (mActionMode != null) {
                mActionMode!!.finish()
            }
        }
    }

    /**
     * Called when the [CheckBox] with the id R.id.windowFullscreen is checked or unchecked.
     * We initialize our `Window` variable `val win` with the current Window for our activity,
     * initialize `WindowManager.LayoutParams` variable `val winParams` with the current window
     * attributes associated with `win`, and initialize `val bits` with the Window flag FLAG_FULLSCREEN
     * (hide all screen decorations (such as the status bar) while this window is displayed). If our
     * [Boolean] parameter [on] is true we set the FLAG_FULLSCREEN bit in the `flags` field of
     * `winParams`, if it is false we clear that bit. Finally we use `winParams` to set the window
     * attributes of `win`.
     *
     * @param on if true go to full screen mode, if false leave full screen mode.
     */
    private fun setFullscreen(on: Boolean) {
        val win = window
        val winParams = win.attributes
        val bits = WindowManager.LayoutParams.FLAG_FULLSCREEN
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    /**
     * Called when the [CheckBox] with the id R.id.windowOverscan is checked or unchecked to
     * switch in or out of overscan mode. We initialize our `Window` variable `val win` with the
     * current Window for our activity, initialize `WindowManager.LayoutParams` variable
     * `val winParams` with the current window attributes associated with `win`, and initialize
     * `val bits` with the Window flag FLAG_LAYOUT_IN_OVERSCAN (allow window contents to extend into
     * the screen's overscan area, if there is one) If our [Boolean] parameter [on] is true we set
     * the FLAG_LAYOUT_IN_OVERSCAN bit in the `flags` field of `winParams`, if it is false
     * we clear that bit. Finally we use `winParams` to set the window attributes of `win`.
     *
     * @param on true to allow window contents to extend in to the screen's overscan area, false to
     * disable this.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun setOverscan(on: Boolean) {
        val win = window
        val winParams = win.attributes
        val bits = WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    /**
     * Called when the [CheckBox] with the id R.id.windowTranslucentStatus is checked or unchecked
     * to enable or disable translucent status bar. We initialize our `Window` variable `val win`
     * with the current Window for our activity, initialize `WindowManager.LayoutParams` variable
     * `val winParams` with the current window attributes associated with `win`, and initialize
     * `val bits` with the Window flag FLAG_TRANSLUCENT_STATUS (request a translucent status bar
     * with minimal system-provided background protection) If our [Boolean] parameter [on] is true
     * we set the FLAG_TRANSLUCENT_STATUS bit in the `flags` field of `winParams`, if it is false
     * we clear that bit. Finally we use `winParams` to set the window attributes of `win`.
     *
     * @param on true to request a translucent status bar with minimal system-provided background
     * protection, false to disable this.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun setTranslucentStatus(on: Boolean) {
        val win = window
        val winParams = win.attributes
        val bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    /**
     * Called when the [CheckBox] with the id R.id.windowTranslucentNav is checked or unchecked
     * to enable or disable translucent navigation bar. We initialize our `Window` variable
     * `val win` with the current Window for our activity, initialize `WindowManager.LayoutParams`
     * variable `val winParams` with the current window attributes associated with `win`, and
     * initialize `val bits` with the Window flag FLAG_TRANSLUCENT_NAVIGATION (request a translucent
     * navigation bar with minimal system-provided background protection) If our [Boolean] parameter
     * [on] is true we set the FLAG_TRANSLUCENT_NAVIGATION bit in the `flags` field of `winParams`,
     * if it is false we clear that bit. Finally we use `winParams` to set the window attributes of
     * `win`.
     *
     * @param on true to request a translucent navigation bar with minimal system-provided background
     * protection, false to disable this.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun setTranslucentNavigation(on: Boolean) {
        val win = window
        val winParams = win.attributes
        val bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    /**
     * Fetches the width and height of the display in pixels and returns a string describing them.
     * We initialize our `DisplayMetrics` variable `val dm` with the current display metrics that
     * are in effect. Then we return a formatted string of the `widthPixels` field of `dm`
     * (absolute width of the available display size in pixels) and the `heightPixels` field
     * of `dm` (absolute height of the available display size in pixels), formatted using the
     * format "DisplayMetrics = (%d x %d)".
     *
     * @return String describing the width and height of the display in pixels.
     */
    @get:SuppressLint("DefaultLocale")
    private val displaySize: String
        get() {
            val dm = resources.displayMetrics
            return String.format("DisplayMetrics = (%d x %d)", dm.widthPixels, dm.heightPixels)
        }

    /**
     * Retrieves the left, top, right, and bottom positions of [IV] field [mImage] and returns a
     * string formatted using the format "View = (%d,%d - %d,%d)".
     *
     * @return string displaying the top left and bottom right coordinates of our [IV] field
     * [mImage] view.
     */
    @get:SuppressLint("DefaultLocale")
    private val viewSize: String
        get() = String.format("View = (%d,%d - %d,%d)",
            mImage!!.left, mImage!!.top,
            mImage!!.right, mImage!!.bottom)

    /**
     * Called from the `onSizeChanged` and `onSystemUiVisibilityChange` callbacks of our
     * embedded [IV] field [mImage] in order to display the new display metrics values in our
     * [TextView] field [mMetricsText]. We just set the text of [mMetricsText] to the string
     * formed by concatenating the value returned by our method `getDisplaySize` (kotlin calls
     * the the property [displaySize]) " " followed by the value returned by our method
     * `getViewSize` (kotlin calls this the property [viewSize]).
     */
    @SuppressLint("SetTextI18n")
    fun refreshSizes() {
        mMetricsText!!.text = "$displaySize $viewSize"
    }

    /**
     * Our embedded [IV] instance.
     */
    var mImage: IV? = null

    /**
     * References to the 8 [CheckBox] at the top of the view, they are all managed by the
     * `OnCheckedChangeListener` variable `checkChangeListener` created in [onCreate] (the
     * state of the checkboxes are read by the method [updateSystemUi] and the related flag
     * in the [Int] array field [mCheckFlags] is set if the [CheckBox] is checked, and the
     * resulting mask is used to update the system UI using the method `setSystemUiVisibility`).
     */
    var mCheckControls = arrayOfNulls<CheckBox>(8)

    /**
     * System UI Flags controlled by the 8 checkboxes in [CheckBox] array field [mCheckControls]
     */
    var mCheckFlags = intArrayOf(View.SYSTEM_UI_FLAG_LOW_PROFILE,
        View.SYSTEM_UI_FLAG_FULLSCREEN, View.SYSTEM_UI_FLAG_HIDE_NAVIGATION,
        View.SYSTEM_UI_FLAG_IMMERSIVE, View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY,
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE, View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN,
        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    )

    /**
     * [TextView] with id R.id.metricsText which our method [refreshSizes] uses to display
     * the display and view metrics whenever they have changed.
     */
    var mMetricsText: TextView? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.system_ui_modes.
     * We initialize our [IV] field [mImage] by finding the view with id R.id.image, and call its
     * `setActivity` method to have it set its [SystemUIModes] field `mActivity` field to "this".
     * We initialize [CompoundButton.OnCheckedChangeListener] variable `val checkChangeListener`
     * with an a lambda which calls our method [updateSystemUi] whenever one of the checkboxes it
     * listens to changes state. We initialize the 8 entries in the [CheckBox] array field
     * [mCheckControls] by finding the views with the following ids:
     *
     *  * R.id.modeLowProfile - "LOW_PROFILE" controls flag SYSTEM_UI_FLAG_LOW_PROFILE
     *  View requests the system UI to enter an unobtrusive "low profile" mode.
     *
     *  * R.id.modeFullscreen - "FULLSCRN" controls flag SYSTEM_UI_FLAG_FULLSCREEN View requests
     *  to go into the normal fullscreen mode so that its content can take over the screen.
     *
     *  * R.id.modeHideNavigation - "HIDE_NAV" controls flag SYSTEM_UI_FLAG_HIDE_NAVIGATION View
     *  request that the system navigation be temporarily hidden.
     *
     *  * R.id.modeImmersive - "IMMERSIVE" controls flag SYSTEM_UI_FLAG_IMMERSIVE View would like
     *  to remain interactive when hiding the navigation bar with SYSTEM_UI_FLAG_HIDE_NAVIGATION.
     *  If this flag is not set, SYSTEM_UI_FLAG_HIDE_NAVIGATION will be force cleared by the system
     *  on any user interaction.
     *
     *  * R.id.modeImmersiveSticky - "IMM_STICKY" controls flag SYSTEM_UI_FLAG_IMMERSIVE_STICKY
     *  View would like to remain interactive when hiding the status bar with SYSTEM_UI_FLAG_FULLSCREEN
     *  and/or hiding the navigation bar with SYSTEM_UI_FLAG_HIDE_NAVIGATION. Use this flag to
     *  create an immersive experience while also hiding the system bars. If this flag is not set,
     *  SYSTEM_UI_FLAG_HIDE_NAVIGATION will be force cleared by the system on any user interaction,
     *  and SYSTEM_UI_FLAG_FULLSCREEN will be force-cleared by the system if the user swipes from
     *  the top of the screen.
     *
     *  * R.id.layoutStable - "STABLE" controls flag SYSTEM_UI_FLAG_LAYOUT_STABLE When using other
     *  layout flags, we would like a stable view of the content insets given  to fitSystemWindows(Rect).
     *  This means that the insets seen there will always represent the worst case that the application
     *  can expect as a continuous state.
     *
     *  * R.id.layoutFullscreen - "FULLSCRN" controls flag SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
     *  View would like its window to be laid out as if it has requested SYSTEM_UI_FLAG_FULLSCREEN,
     *  even if it currently hasn't. This allows it to avoid artifacts when switching in and out
     *  of that mode, at the expense that some of its user interface may be covered by screen
     *  decorations when they are shown.
     *
     *  * R.id.layoutHideNavigation - "HIDE_NAV" controls flag SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
     *  View would like its window to be laid out as if it has requested SYSTEM_UI_FLAG_HIDE_NAVIGATION,
     *  even if it currently hasn't. This allows it to avoid artifacts when switching in and out
     *  of that mode, at the expense that some of its user interface may be covered by screen
     *  decorations when they are shown.
     *
     *
     * Next we loop through all 8 of the checkboxes in [mCheckControls] and set their
     * `OnCheckedChangeListener` to `checkChangeListener`.
     *
     * We now find the 6 remaining checkboxes in our layout under the "Window" label (grouped this
     * way because they each set or clear their flags using the layout parameters of the current
     * Window of the activity instead of using `setSystemUiVisibility`) and set their
     * `OnCheckedChangeListener` to anonymous classes as follows:
     *
     *  * R.id.windowFullscreen - "FULLSCRN" calls our method `setFullscreen` with the
     *  value of its parameter `isChecked` which sets or clears the
     *  WindowManager.LayoutParams.FLAG_FULLSCREEN bit of our activities window.
     *
     *  * R.id.windowOverscan - "OVERSCAN" calls our method `setOverscan` with the
     *  value of its parameter `isChecked` which sets or clears the
     *  WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN bit of our activities window.
     *
     *  * R.id.windowTranslucentStatus - "TRANS_STATUS" calls our method `setTranslucentStatus`
     *  with the value of its parameter `isChecked` which sets or clears the
     *  WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS bit of our activities window.
     *
     *  * R.id.windowTranslucentNav - "TRANS_NAV" calls our method `setTranslucentNavigation`
     *  with the value of its parameter `isChecked` which sets or clears the
     *  WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION bit of our activities window.
     *
     *  * R.id.windowHideActionBar - "No ActionBar" if its parameter `isChecked` is true
     *  it retrieves a reference to this activity's ActionBar and calls its `hide`
     *  method, if false it retrieves a reference to this activity's ActionBar and calls its
     *  `show` method.
     *
     *  * R.id.windowActionMode - "Action Mode" if its parameter `isChecked` is true it calls the
     *  `startActionMode` method of `IV mImage`, if it is false it calls the `stopActionMode` method.
     *
     * Finally we initialize our [TextView] field [mMetricsText] by finding the view with id
     * R.id.metricsText.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.system_ui_modes)
        mImage = findViewById(R.id.image)
        mImage!!.setActivity(this)
        val checkChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            updateSystemUi()
        }
        mCheckControls[0] = findViewById(R.id.modeLowProfile)
        mCheckControls[1] = findViewById(R.id.modeFullscreen)
        mCheckControls[2] = findViewById(R.id.modeHideNavigation)
        mCheckControls[3] = findViewById(R.id.modeImmersive)
        mCheckControls[4] = findViewById(R.id.modeImmersiveSticky)
        mCheckControls[5] = findViewById(R.id.layoutStable)
        mCheckControls[6] = findViewById(R.id.layoutFullscreen)
        mCheckControls[7] = findViewById(R.id.layoutHideNavigation)
        for (i in mCheckControls.indices) {
            mCheckControls[i]!!.setOnCheckedChangeListener(checkChangeListener)
        }
        (findViewById<View>(R.id.windowFullscreen) as CheckBox).setOnCheckedChangeListener { buttonView, isChecked -> setFullscreen(isChecked) }
        (findViewById<View>(R.id.windowOverscan) as CheckBox).setOnCheckedChangeListener { buttonView, isChecked -> setOverscan(isChecked) }
        (findViewById<View>(R.id.windowTranslucentStatus) as CheckBox).setOnCheckedChangeListener { buttonView, isChecked -> setTranslucentStatus(isChecked) }
        (findViewById<View>(R.id.windowTranslucentNav) as CheckBox).setOnCheckedChangeListener { buttonView, isChecked -> setTranslucentNavigation(isChecked) }
        (findViewById<View>(R.id.windowHideActionBar) as CheckBox).setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                supportActionBar!!.hide()
            } else {
                supportActionBar!!.hide()
            }
        }
        (findViewById<View>(R.id.windowActionMode) as CheckBox).setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                mImage!!.startActionMode()
            } else {
                mImage!!.stopActionMode()
            }
        }
        mMetricsText = findViewById(R.id.metricsText)
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
     * manager. We call our method [updateCheckControls] to read the System Ui Visibility settings
     * and update the state of the 8 checkboxes in [CheckBox] array field [mCheckControls] based
     * on the current settings.
     */
    override fun onAttachedToWindow() {
        updateCheckControls()
    }

    /**
     * Called after [onRestoreInstanceState], [onRestart], or [onPause], for our activity to start
     * interacting with the user. We just call our super's implementation of `onResume`.
     */
    override fun onResume() {
        super.onResume()
    }

    /**
     * Called using an android:onClick="onSort" attribute in the menu file R.menu.content_actions
     * from the two menu items @+id/action_sort_alpha ("Alphabetically") and @+id/action_sort_size
     * ("By size"). We ignore.
     *
     * @param item [MenuItem] that was selected
     */
    fun onSort(item: MenuItem?) {}

    /**
     * Called when the query text is changed by the user. We return true to indicate that we have
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
     * Called when the user submits the query. We toast a string formed by concatenating the string
     * "Searching for: " with our [String] parameter [query] followed by the string "...", then
     * return true to indicate that we have handled the query.
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
     * This hook is called whenever an item in your options menu is selected. We switch on the item
     * id of our [MenuItem] parameter [item]:
     *
     *  * R.id.show_tabs ("Show Tabs") - we fetch a reference to the action bar and set its
     *  navigation mode to NAVIGATION_MODE_TABS (Instead of static title text this mode
     *  presents a series of tabs for navigation within the activity), set [item] to
     *  the checked state, and return true to the caller to consume the event here.
     *
     *  * R.id.hide_tabs ("Hide Tabs") - we fetch a reference to the action bar and set its
     *  navigation mode to NAVIGATION_MODE_STANDARD (Standard navigation mode. Consists of
     *  either a logo or icon and title text with an optional subtitle. Clicking any of these
     *  elements will dispatch onOptionsItemSelected to the host Activity with a [MenuItem]
     *  with item ID android.R.id.home), set [item] to the unchecked state, and return
     *  true to the caller to consume the event here.
     *
     * If the item id of [MenuItem] parameter [item] is neither of the above we return false to the
     * caller to allow normal menu processing to proceed.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.show_tabs -> {
                supportActionBar!!.navigationMode = ActionBar.NAVIGATION_MODE_TABS
                item.isChecked = true
                return true
            }
            R.id.hide_tabs -> {
                supportActionBar!!.navigationMode = ActionBar.NAVIGATION_MODE_STANDARD
                item.isChecked = true
                return true
            }
        }
        return false
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

    /**
     * Called to update the checked/unchecked state of the 8 checkboxes in [CheckBox] array field
     * [mCheckControls] based on the current value of the system UI visibility mask. First we
     * initialize our variable `val visibility` with the current system UI visibility. Then we loop
     * over `i` through the 8 checkboxes in [mCheckControls] setting each checkbox to checked if the
     * bit in `visibility` for the bit given in the `i`'th entry in [mCheckFlags] is 1, or to
     * unchecked if it is zero.
     */
    fun updateCheckControls() {
        val visibility = mImage!!.systemUiVisibility
        for (i in mCheckControls.indices) {
            mCheckControls[i]!!.isChecked = visibility and mCheckFlags[i] != 0
        }
    }

    /**
     * Called to update the system UI visibility based on the current state of the 8 checkboxes in
     * [CheckBox] array field [mCheckControls]. First we initialize `var visibility` to zero, then
     * we loop over `i` through the 8 checkboxes in [mCheckControls] or'ing in the bit in the
     * `i`th entry in [mCheckFlags] if the `i`th entry in [mCheckControls] is checked. Finally we
     * set the system UI visibility to `visibility`.
     */
    fun updateSystemUi() {
        var visibility = 0
        for (i in mCheckControls.indices) {
            if (mCheckControls[i]!!.isChecked) {
                visibility = visibility or mCheckFlags[i]
            }
        }
        mImage!!.systemUiVisibility = visibility
    }

    /**
     * Called from the `onDestroyActionMode` override of our [IV] objects action mode
     * callback `MyActionModeCallback`, we just locate the [CheckBox] with the id
     * R.id.windowActionMode and set it to the unchecked state.
     */
    fun clearActionMode() {
        (findViewById<View>(R.id.windowActionMode) as CheckBox).isChecked = false
    }

    companion object {
        /**
         * UNUSED
         */
        @Suppress("unused")
        var TOAST_LENGTH = 500
    }
}
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
@file:Suppress("DEPRECATION")

package com.example.android.apis.app

import com.example.android.apis.R

import android.annotation.TargetApi
import android.app.ActionBar
import android.app.ActionBar.Tab
import android.app.Activity
import android.app.FragmentTransaction
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner

/**
 * This demo shows how various action bar display option flags can be combined
 * and their effects.
 */
@Suppress("DEPRECATION")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
class ActionBarDisplayOptions : Activity(), View.OnClickListener, ActionBar.TabListener,
        AdapterView.OnItemSelectedListener, ActionBar.OnNavigationListener {
    private var mCustomView: View? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, then we set our content view to our layout file R.layout.action_bar_display_options.
     * Then we locate all the buttons in our layout file and set their OnClickListener to "this",
     * and we locate the Spinner (R.id.toggle_navigation) and set its OnItemSelectedListener to
     * "this". We inflate into View mCustomView (our custom View) which consists of simply a Button
     * from its layout file R.layout.action_bar_display_options_custom. Then we retrieve a reference
     * to this activity's ActionBar into ActionBar bar. We use "bar" to set the custom view for the
     * ActionBar to View mCustomView with layout parameters WRAP_CONTENT for width and WRAP_CONTENT
     * for height. Then we add three tabs to the ActionBar, setting their ActionBar.TabListener to
     * "this". We create an `ArrayAdapter<String>` adapter using "this" as its context and the system
     * layout android.R.layout.simple_list_item_1 as its layout file which contains a TextView to
     * use when instantiating views. We add three items ("Item 1", "Item 2", and "Item 3") to
     * "adapter" then we the set adapter and navigation callback for list navigation mode to
     * "adapter" and "this" respectively.
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.action_bar_display_options)

        findViewById<View>(R.id.toggle_home_as_up).setOnClickListener(this)
        findViewById<View>(R.id.toggle_show_home).setOnClickListener(this)
        findViewById<View>(R.id.toggle_use_logo).setOnClickListener(this)
        findViewById<View>(R.id.toggle_show_title).setOnClickListener(this)
        findViewById<View>(R.id.toggle_show_custom).setOnClickListener(this)
        findViewById<View>(R.id.cycle_custom_gravity).setOnClickListener(this)
        findViewById<View>(R.id.toggle_visibility).setOnClickListener(this)
        findViewById<View>(R.id.toggle_system_ui).setOnClickListener(this)

        (findViewById<View>(R.id.toggle_navigation) as Spinner).onItemSelectedListener = this

        mCustomView = layoutInflater.inflate(R.layout.action_bar_display_options_custom,
                findViewById<View>(android.R.id.content) as ViewGroup, false)
        // Configure several action bar elements that will be toggled by display options.
        val bar = actionBar

        bar!!.setCustomView(mCustomView,
                ActionBar.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))

        bar.addTab(bar.newTab().setText("Tab 1").setTabListener(this))
        bar.addTab(bar.newTab().setText("Tab 2").setTabListener(this))
        bar.addTab(bar.newTab().setText("Tab 3").setTabListener(this))

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)
        adapter.add("Item 1")
        adapter.add("Item 2")
        adapter.add("Item 3")
        bar.setListNavigationCallbacks(adapter, this)
    }

    /**
     * Initialize the contents of the Activity's standard options menu. We simply fetch a MenuInflater
     * with this context and use it to inflate our menu R.menu.display_options_actions into the
     * options menu "Menu menu" passed to us.
     *
     * @param menu The options menu in which you place your items.
     *
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.display_options_actions, menu)
        return true
    }

    /**
     * Called when a view has been clicked. This is called by all the Button's in our UI which have
     * had their OnClickListener set to "this". First we fetch a reference to the ActionBar into
     * ActionBar bar, and initialize int flags to zero. Then we switch on the id of the View v which
     * has been clicked.
     *
     * The first five Button's set "int flags" to toggle the appropriate flag at the end of the
     * switch statement:
     *
     *  - R.id.toggle_home_as_up ("DISPLAY_HOME_AS_UP") flag: ActionBar.DISPLAY_HOME_AS_UP
     * Display the 'home' element such that it appears as an 'up' affordance. e.g. show an
     * arrow to the left indicating the action that will be taken. Set this flag if selecting
     * the 'home' button in the action bar to return up by a single level in your UI rather
     * than back to the top level or front page.
     *  - R.id.toggle_show_home ("DISPLAY_SHOW_HOME") flag: ActionBar.DISPLAY_SHOW_HOME
     * Show 'home' elements in this action bar, leaving more space for other navigation
     * elements. This includes logo and icon.
     *  - R.id.toggle_use_logo ("DISPLAY_USE_LOGO") flag: ActionBar.DISPLAY_USE_LOGO
     * Show the logo defined as the attribute android:logo="@drawable/apidemo_androidlogo"
     * in AndroidManifest.xml for the Activity (Shown only if ActionBar.DISPLAY_SHOW_HOME
     * is also set.)
     *  - R.id.toggle_show_title ("DISPLAY_SHOW_TITLE") flag: ActionBar.DISPLAY_SHOW_TITLE
     * Show the activity title and subtitle, if present.
     * R.id.toggle_show_custom ("DISPLAY_SHOW_CUSTOM") flag: ActionBar.DISPLAY_SHOW_CUSTOM
     * Show the custom view if one has been set.
     *
     * The other Button's perform more complex operations than toggling using int flags and return
     * after doing them rather than fall through to end of the outer switch:
     *
     *  - R.id.cycle_custom_gravity ("Cycle Custom View Gravity") first fetches the current layout
     * parameters for the View mCustomView to the variable ActionBar.LayoutParams lp, then
     * it switches based on the current value of the field lp.gravity masked with
     *  - Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK (the Binary mask for horizontal gravity and
     * script specific direction bit), and changes newGravity if the current value is:
     *  - Gravity.START (Push object to x-axis position at the start of its container, not
     * changing its size) changes to Gravity.CENTER_HORIZONTAL
     *  - Gravity.CENTER_HORIZONTAL (Place object in the horizontal center of its container,
     * not changing its size) changes to Gravity.END
     *  - Gravity.END (Push object to x-axis position at the end of its container, not
     * changing its size) changes to Gravity.START
     *
     * After deciding what newGravity is to be in the switch statement we set the appropriate
     * bit in lp.gravity, then call bar.setCustomView(mCustomView, lp) to install our custom
     * View (mCustomView) with our modified layout parameters "ActionBar.LayoutParams lp" as
     * the the ActionBar's custom View. (The display option DISPLAY_SHOW_CUSTOM must be set
     * for the custom view to be displayed). We then return.
     *
     *  - R.id.toggle_visibility ("Toggle Visibility") We check if the ActionBar is showing and if
     * it is we call bar.hide() to hide the ActionBar, and if it is not we call bar.show()
     * to show the ActionBar. In either case we then return.
     *  - R.id.toggle_system_ui ("Toggle System UI") If the View.SYSTEM_UI_FLAG_FULLSCREEN bit of the
     * last setSystemUiVisibility(int) that this view has requested is set we clear all bits
     * of the system ui visibility, if it is not set we set the View.SYSTEM_UI_FLAG_FULLSCREEN
     * bit. In either case we then return
     *
     * For the five Button's which set "flags" to the flag they wish to toggle and then "break",
     * we retrieve the current set of display options to "int change", toggle the flag in "change"
     * that needs to be toggled, then using flags as the mask to specify which flags to change
     * we set or clear only that bit in the ActionBar's display options by calling
     * bar.setDisplayOptions(change, flags)
     *
     * @param v Button which was clicked
     */
    override fun onClick(v: View) {
        val bar = actionBar
        var flags = 0
        when (v.id) {
            R.id.toggle_home_as_up -> flags = ActionBar.DISPLAY_HOME_AS_UP
            R.id.toggle_show_home -> flags = ActionBar.DISPLAY_SHOW_HOME
            R.id.toggle_use_logo -> flags = ActionBar.DISPLAY_USE_LOGO
            R.id.toggle_show_title -> flags = ActionBar.DISPLAY_SHOW_TITLE
            R.id.toggle_show_custom -> flags = ActionBar.DISPLAY_SHOW_CUSTOM
            R.id.cycle_custom_gravity -> {
                val lp = mCustomView!!.layoutParams as ActionBar.LayoutParams
                var newGravity = 0
                when (lp.gravity and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
                    Gravity.START -> newGravity = Gravity.CENTER_HORIZONTAL
                    Gravity.CENTER_HORIZONTAL -> newGravity = Gravity.END
                    Gravity.END -> newGravity = Gravity.START
                }
                lp.gravity = lp.gravity and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK.inv() or newGravity

                bar!!.setCustomView(mCustomView, lp)
                return
            }
            R.id.toggle_visibility -> {

                if (bar!!.isShowing) {
                    bar.hide()
                } else {
                    bar.show()
                }
                return
            }
            R.id.toggle_system_ui -> {
                if (window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_FULLSCREEN != 0) {
                    window.decorView.systemUiVisibility = 0
                } else {
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
                }
                return
            }
        }


        val change = bar!!.displayOptions xor flags

        bar.setDisplayOptions(change, flags)
    }

    /**
     * Callback interface invoked when a tab is focused, unfocused, added, or removed. Part of the
     * ActionBar.TabListener interface we must implement to add a tab to the ActionBar.
     *
     * @param tab The tab that was selected
     * @param ft A [FragmentTransaction] for queuing fragment operations to execute
     * during a tab switch. The previous tab's unselect and this tab's select will be
     * executed in a single transaction. This FragmentTransaction does not support
     * being added to the back stack.
     */
    override fun onTabSelected(tab: Tab, ft: FragmentTransaction) {}

    /**
     * Called when a tab exits the selected state. Part of the ActionBar.TabListener interface
     * we must implement to add a tab to the ActionBar.
     *
     * @param tab The tab that was unselected
     * @param ft A [FragmentTransaction] for queuing fragment operations to execute
     * during a tab switch. This tab's unselect and the newly selected tab's select
     * will be executed in a single transaction. This FragmentTransaction does not
     * support being added to the back stack.
     */
    override fun onTabUnselected(tab: Tab, ft: FragmentTransaction) {}

    /**
     * Called when a tab that is already selected is chosen again by the user.
     * Some applications may use this action to return to the top level of a category.
     * Part of the ActionBar.TabListener interface  we must implement to add a tab to the ActionBar.
     *
     * @param tab The tab that was reselected.
     * @param ft A [FragmentTransaction] for queuing fragment operations to execute
     * once this method returns. This FragmentTransaction does not support
     * being added to the back stack.
     */
    override fun onTabReselected(tab: Tab, ft: FragmentTransaction) {}

    /**
     * Callback method to be invoked when an item in this view has been
     * selected. This callback is invoked only when the newly selected
     * position is different from the previously selected position or if
     * there was no selected item.
     *
     * Part of the Spinner.OnItemSelectedListener interface which is inherited from AbsSpinner,
     * which is inherited from `AdapterView<SpinnerAdapter>`, we set the OnItemSelectedListener
     * of the R.id.toggle_navigation Spinner to "this" and get called when an item in the Spinner
     * list has been selected. First fetch a reference to the ActionBar into ActionBar bar, then
     * we perform a switch (which might might as well be an "if") based on the id of the AdapterView
     * which has had an item selected, and if the id is R.id.toggle_navigation, we switch on the
     * position of the item in the adapter and set "int mode" to NAVIGATION_MODE_TABS,
     * NAVIGATION_MODE_LIST, or NAVIGATION_MODE_STANDARD. Before returning we call
     * ActionBar.setNavigationMode(mode) to set the current navigation mode to the mode selected.
     *
     * @param parent The AdapterView where the selection happened
     * @param view The view within the AdapterView that was clicked
     * @param position The position of the view in the adapter
     * @param id The row id of the item that is selected
     */
    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val bar = actionBar

        when (parent.id) {
            R.id.toggle_navigation -> {
                val mode: Int = when (position) {
                    1 -> ActionBar.NAVIGATION_MODE_TABS
                    2 -> ActionBar.NAVIGATION_MODE_LIST
                    else -> ActionBar.NAVIGATION_MODE_STANDARD
                }

                bar!!.navigationMode = mode
            }
        }
    }

    /**
     * Callback method to be invoked when the selection disappears from this
     * view. The selection can disappear for instance when touch is activated
     * or when the adapter becomes empty.
     *
     * Part of the Spinner.OnItemSelectedListener interface which is inherited from AbsSpinner,
     * which is inherited from `AdapterView<SpinnerAdapter>`, there is nothing we need do.
     *
     * @param parent The AdapterView that now contains no selected item.
     */
    override fun onNothingSelected(parent: AdapterView<*>) {}

    /**
     * This method is called whenever a navigation item in your action bar
     * is selected. Part of the ActionBar.OnNavigationListener interface which
     * we implement, there is nothing we want to do, so we return false.
     *
     * @param itemPosition Position of the item clicked.
     * @param itemId ID of the item clicked.
     *
     * @return True if the event was handled, false otherwise.
     */
    override fun onNavigationItemSelected(itemPosition: Int, itemId: Long): Boolean {
        return false
    }
}

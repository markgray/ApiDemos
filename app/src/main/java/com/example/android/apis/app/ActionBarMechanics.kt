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
package com.example.android.apis.app

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * This demonstrates the basics of the Action Bar and how it inter-operates with the
 * standard options menu. This demo is for informative purposes only; see ActionBarUsage for
 * an example of using the Action Bar in a more idiomatic manner.
 * RequiresApi(Build.VERSION_CODES.HONEYCOMB)
 */
class ActionBarMechanics : AppCompatActivity() {
    /**
     * Called when the activity is starting. We first call through to our super's implementation
     * of onCreate, then we enable the extended screen feature Window.FEATURE_ACTION_BAR which
     * is the flag for enabling the Action Bar. This is enabled by default for some devices.
     * The Action Bar replaces the title bar and provides an alternate location for an on-screen
     * menu button on some devices.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // The Action Bar is a window feature. The feature must be requested
        // before setting a content view. Normally this is set automatically
        // by your Activity's theme in your manifest. The provided system
        // theme Theme.WithActionBar enables this for you. Use it as you would
        // use Theme.NoTitleBar. You can add an Action Bar to your own themes
        // by adding the element <item name="android:windowActionBar">true</item>
        // to your style definition.
        window.requestFeature(Window.FEATURE_ACTION_BAR)
    }

    /**
     * Initialize the contents of the Activity's standard options menu. We first add a "Normal Item"
     * to our menu using the default to never show in the action bar (it will instead appear in the
     * Action overflow menu in a cascading menu). Next we add a "Action Button" to the menu and save
     * the newly added menu item in the MenuItem actionItem returned for later use. We call
     * setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM) on MenuItem actionItem to set how this
     * item should display in the presence of an Action Bar (Shows this item as a button in an
     * Action Bar if the system decides there is room for it). actionItem.setIcon is called to
     * set the icon associated with this item to android.R.drawable.ic_menu_share (the standard
     * Android "share" icon.) Finally we return true so that the menu will be shown.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Menu items default to never show in the action bar. On most devices this means
        // they will show in the standard options menu panel when the menu button is pressed.
        // On xlarge-screen devices a "More" button will appear in the far right of the
        // Action Bar that will display remaining items in a cascading menu.
        menu.add("Normal item")

        val actionItem = menu.add("Action Button")

        // Items that show as actions should favor the "if room" setting, which will
        // prevent too many buttons from crowding the bar. Extra items will show in the
        // overflow area.
        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)

        // Items that show as actions are strongly encouraged to use an icon.
        // These icons are shown without a text description, and therefore should
        // be sufficiently descriptive on their own.
        actionItem.setIcon(android.R.drawable.ic_menu_share)

        return true
    }

    /**
     * This hook is called whenever an item in your options menu is selected. Here we simply toast
     * current title of the item, then return true to indicate that we consumed the click.
     *
     * @param item The menu item that was selected.
     * @return Return false to allow normal menu processing to proceed, true to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Toast.makeText(this, "Selected Item: " + item.title, Toast.LENGTH_SHORT).show()
        return true
    }
}

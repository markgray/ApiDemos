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

package com.example.android.apis.app

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBar
import androidx.core.view.ActionProvider

import com.example.android.apis.R

/**
 * This activity demonstrates how to implement an [androidx.core.view.ActionProvider]
 * for adding functionality to the Action Bar. In particular this demo creates an
 * ActionProvider for launching the system settings and adds a menu item with that
 * provider.
 */
@Suppress("MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class ActionBarSettingsActionProviderActivity : AppCompatActivity() {

    /**
     * A reference to our activity's [ActionBar] for setting its display options.
     */
    internal var mActionBar: ActionBar? = null

    /**
     * Initialize the contents of the Activity's standard options menu. First we call through
     * to our super's implementation of `onCreateOptionsMenu`. Then we fetch a `MenuInflater`
     * and use it to inflate our menu from R.menu.action_bar_settings_action_provider into the
     * `Menu menu` given us. Finally we return true so the menu will be displayed.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed; if you return false it will not be shown.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.action_bar_settings_action_provider, menu)
        mActionBar = supportActionBar
        mActionBar!!.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE)
        return true
    }

    /**
     * This hook is called whenever an item in your options menu is selected. We simply toast the
     * message "Handling in onOptionsItemSelected avoided" and return false and the class we specify
     * by the android:actionProviderClass in the menu xml is used to call that class's implementation
     * of onPerformDefaultAction. This callback is only called from the "Settings" item in the
     * overflow menu, NOT from the icon shown (ifRoom) in the ActionBar, onPerformDefaultAction is
     * just called.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to proceed, true to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // If this callback does not handle the item click, onPerformDefaultAction
        // of the ActionProvider is invoked. Hence, the provider encapsulates the
        // complete functionality of the menu item.
        Log.i(TAG, "onOptionsItemSelected has been called")
        Toast.makeText(this, R.string.action_bar_settings_action_provider_no_handling,
                Toast.LENGTH_SHORT).show()
        return false
    }

    /**
     * This class is specified using the xml attribute android:actionProviderClass in our menu
     * xml file. It extends the abstract class ActionProvider, implementing the abstract callbacks
     * onCreateActionView and onPerformDefaultAction
     */
    class SettingsActionProvider
    /**
     * Creates a new instance. We first call through to our super's constructor, then save the
     * Context passed us for use in onCreateActionView.
     */
    (
            /** Context for accessing resources.  */
            private val mContext: Context) : ActionProvider(mContext) {

        /**
         * Factory method called by the Android framework to create new action views. First we fetch
         * a LayoutInflater layoutInflater using the Context passed when creating this instance of
         * the SettingsActionProvider Class and use this LayoutInflater to inflate View view from
         * from our layout file R.layout.action_bar_settings_action_provider. We find in this view
         * our ImageButton button (R.id.button) and set the OnClickListener of "button" to a callback
         * which uses mContext to start the activity in the Intent sSettingsIntent (the system
         * settings Activity specified using Settings.ACTION_SETTINGS). Finally we return our View.
         *
         * @return A new action view.
         */
        override fun onCreateActionView(): View {
            // Inflate the action view to be shown on the action bar.
            val layoutInflater = LayoutInflater.from(mContext)
            @SuppressLint("InflateParams")
            val view = layoutInflater.inflate(R.layout.action_bar_settings_action_provider, null)
            val button = view.findViewById<ImageButton>(R.id.button)
            // Attach a click listener for launching the system settings.
            button.setOnClickListener {
                mContext.startActivity(sSettingsIntent)
            }
            return view
        }

        /**
         * Called only when the Action is in the overflow menu, not when it is in the ActionBar.
         * We simply use our saved Context mContext to start the system settings Activity, then
         * return true to denote that the action has been handled.
         *
         * @return true if the Action has been handled.
         */
        override fun onPerformDefaultAction(): Boolean {
            // This is called if the host menu item placed in the overflow menu of the
            // action bar is clicked and the host activity did not handle the click.
            Log.i(TAG, "onPerformDefaultAction has been called")
            mContext.startActivity(sSettingsIntent)
            return true
        }

        companion object {

            /** An intent for launching the system settings.  */
            private val sSettingsIntent = Intent(Settings.ACTION_SETTINGS)
        }
    }

    companion object {

        /**
         * TAG used for logging.
         */
        private const val TAG = "ActionBarSettings"
    }
}

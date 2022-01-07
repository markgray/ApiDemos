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

package com.example.android.apis.view

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * This demo illustrates the use of CHOICE_MODE_MULTIPLE_MODAL, a.k.a. selection mode on ListView
 * coupled with the new simple_list_item_activated_1 which uses a highlighted border for selected
 * items.
 */
class List16 : AppCompatActivity() {
    /**
     * The [ListView] in our layout file
     */
    private lateinit var list: ListView
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file [R.layout.list_16] and initialize
     * our [ListView] field [list] by finding the view with ID [R.id.list]. We set the choice mode
     * of [list] to `CHOICE_MODE_MULTIPLE_MODAL`, and set its `MultiChoiceModeListener` to a new
     * instance of [ModeCallback]. Finally we set the list adapter of [list] to a new instance of
     * [ArrayAdapter] constructed to display the array [Cheeses.sCheeseStrings] using the layout
     * android.R.layout.simple_list_item_activated_1.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_16)
        list = findViewById(R.id.list)
        list.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
        list.setMultiChoiceModeListener(ModeCallback())
        list.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_activated_1,
            Cheeses.sCheeseStrings
        )
    }

    /**
     * Called when activity start-up is complete (after [onStart] and [onRestoreInstanceState]
     * have been called). First we call through to our super's implementation of `onPostCreate`,
     * then we fetch a reference to this activity's ActionBar and set its subtitle to the string:
     * "Long press to start selection".
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        supportActionBar!!.subtitle = "Long press to start selection"
    }

    /**
     * Our custom [AbsListView.MultiChoiceModeListener], becomes active when the [ListView] is long
     * clicked. It then inflates an action mode R.menu.list_select_menu which remains up as long as
     * at least one item is selected, or until the selected items are "shared"
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private inner class ModeCallback : AbsListView.MultiChoiceModeListener {
        /**
         * Called when action mode is first created. The menu supplied will be used to generate
         * action buttons for the action mode. First we initialize `MenuInflater inflater` by
         * setting it to the value returned by `getMenuInflater`, then we use it to inflate
         * our menu layout file R.menu.list_select_menu into our parameter `Menu menu`. We set
         * the title of our parameter `ActionMode mode` to the string "Select Items", and then
         * we return true to the caller so that the action mode will be created.
         *
         * @param mode [ActionMode] being created
         * @param menu [Menu] used to populate action buttons
         * @return true if the action mode should be created
         */
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            val inflater = menuInflater
            inflater.inflate(R.menu.list_select_menu, menu)
            mode.title = "Select Items"
            return true
        }

        /**
         * Called to refresh an action mode's action menu whenever it is invalidated. We always
         * return true to the caller.
         *
         * @param mode ActionMode being prepared
         * @param menu Menu used to populate action buttons
         * @return true if the menu or action mode was updated, false otherwise.
         */
        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return true
        }

        /**
         * Called to report a user click on an action button. We switch on the item ID of our
         * [MenuItem] parameter [item]:
         *
         *  * R.id.share - we toast a message saying that the number of checked items have
         *  been "shared", then call the `finish` method of our [ActionMode] parameter [mode]
         *  to finish and close the action mode. Finally we break.
         *
         *  * default - we toast a message stating that the item with the title of our [MenuItem]
         *  parameter [item] has been clicked, then we break
         *
         * In all cases we return true to the caller signifying that we have handled the event.
         *
         * @param mode The current [ActionMode]
         * @param item The item that was clicked
         * @return true if this callback handled the event, false if the standard MenuItem
         * invocation should continue.
         */
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.share -> {
                    Toast.makeText(this@List16, "Shared " + list.checkedItemCount +
                        " items", Toast.LENGTH_SHORT).show()
                    mode.finish()
                }
                else -> Toast.makeText(this@List16, "Clicked " + item.title,
                    Toast.LENGTH_SHORT).show()
            }
            return true
        }

        /**
         * Called when an action mode is about to be exited and destroyed. We ignore it.
         *
         * @param mode The current [ActionMode] being destroyed
         */
        override fun onDestroyActionMode(mode: ActionMode) {}

        /**
         * Called when an item is checked or unchecked during selection mode. We initialize
         * our variable `int checkedCount` with the number of checked items in our
         * `ListView`, then switch on it:
         *
         *  * 0 - we set the subtitle of [ActionMode] parameter [mode] to null and break.
         *
         *  * 1 - we set the subtitle of [ActionMode] parameter [mode] to the string
         *  "One item selected" and break.
         *
         *  * default - we set the subtitle of [ActionMode] parameter [mode] to the string value
         *  of `checkedCount` concatenated to the string " items selected" and break.
         *
         * @param mode     The [ActionMode] providing the selection mode
         * @param position Adapter position of the item that was checked or unchecked
         * @param id       Adapter ID of the item that was checked or unchecked
         * @param checked  true if the item is now checked, false if the item is now unchecked.
         */
        override fun onItemCheckedStateChanged(mode: ActionMode, position: Int, id: Long, checked: Boolean) {
            when (val checkedCount = list.checkedItemCount) {
                0 -> mode.subtitle = null
                1 -> mode.subtitle = "One item selected"
                else -> mode.subtitle = "$checkedCount items selected"
            }
        }
    }
}
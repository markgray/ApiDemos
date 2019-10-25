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

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.Fragment

import com.example.android.apis.R

/**
 * Demonstration of displaying a context menu from a fragment.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class FragmentContextMenu : FragmentActivity() {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, Then we initialize our [ContextMenuFragment] variable `val content` with a new
     * instance, and use the support `FragmentManager` for interacting with fragments associated
     * with this activity to start a series of edit operations on the Fragments associated with this
     * `FragmentManager` and chaining on the `FragmentTransaction` returned by the `beginTransaction`
     * method we add `content` to the Activity state, and finally, again chaining on the same
     * `FragmentTransaction` we commit that transaction.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use this
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create the list fragment and add it as our sole content.
        val content = ContextMenuFragment()
        supportFragmentManager.beginTransaction().add(android.R.id.content, content).commit()
    }

    /**
     * Fragment whose purpose is to populate and react to a context menu which is opened when the
     * Button R.id.long_press ("Long press me") in the layout is long  pressed.
     */
    class ContextMenuFragment : Fragment() {

        /**
         * Called to have the fragment instantiate its user interface view. First we use our
         * [LayoutInflater] parameter [inflater] to inflate our layout file R.layout.fragment_context_menu
         * into our [View] variable `view`, then we register the `View` of the `Button` in `view`
         * with the id R.id.long_press ("Long press me") for a context menu to be shown. This will
         * set the [View.OnCreateContextMenuListener] on `view` to this fragment, so that the callback
         * `onCreateContextMenu(ContextMenu, View, ContextMenuInfo)` will be called when it is time
         * to show the context menu. Finally we return `view` to the caller.
         *
         * @param inflater The [LayoutInflater] object that can be used to inflate any views in the
         * fragment.
         * @param container If non-null, this is the parent view that the fragment's UI should be
         * attached to. Used to generate the LayoutParams of the view.
         * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
         *
         * @return View for the fragment's UI
         */
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val view = inflater.inflate(R.layout.fragment_context_menu, container, false)
            registerForContextMenu(view.findViewById(R.id.long_press))
            return view
        }

        /**
         * Called when a context menu for the `view` is about to be shown. Unlike
         * [onCreateOptionsMenu], this will be called every time the context menu
         * is about to be shown and should be populated for the view.
         *
         * First we call through to our super's implementation of `onCreateContextMenu`, then we add
         * two items to the menu: "Menu A" with id R.id.a_item, and "Menu B" with id R.id.b_item,
         * specifying no group for the items, and no order.
         *
         * @param menu The context menu that is being built
         * @param v The view for which the context menu is being built
         * @param menuInfo Extra information about the item for which the
         * context menu should be shown. This information will vary
         * depending on the class of v.
         */
        override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
            super.onCreateContextMenu(menu, v, menuInfo)
            menu.add(Menu.NONE, R.id.a_item, Menu.NONE, "Menu A")
            menu.add(Menu.NONE, R.id.b_item, Menu.NONE, "Menu B")
        }

        /**
         * This hook is called whenever an item in a context menu is selected. The
         * default implementation simply returns false to have the normal processing
         * happen (calling the item's Runnable or sending a message to its Handler
         * as appropriate). You can use this method for any items for which you
         * would like to do processing without those other facilities.
         *
         * Use [MenuItem.getMenuInfo] to get extra information set by the
         * View that added this menu item.
         *
         * Derived classes should call through to the base class for it to perform
         * the default menu handling.
         *
         * We fetch the identifier for our [MenuItem] parameter [item] and switch on the id
         * displaying a Toast depending on the id: R.id.a_item "Item 1a was chosen", or
         * R.id.b_item "Item 1b was chosen" then return true to indicate we have consumed the
         * context menu selection. If the identifier of the [item] matches neither of our id's
         * then we return the value returned by our super's implementation of `onContextItemSelected`
         * (which returns false).
         *
         * @param item The context menu item that was selected.
         * @return boolean Return *false* to allow normal context menu processing to
         * proceed, *true* to consume it here.
         */
        override fun onContextItemSelected(item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.a_item -> {
                    Toast.makeText(activity, "Item 1a was chosen", Toast.LENGTH_SHORT).show()
                    return true
                }
                R.id.b_item -> {
                    Toast.makeText(activity, "Item 1b was chosen", Toast.LENGTH_SHORT).show()
                    return true
                }
            }
            return super.onContextItemSelected(item)
        }
    }
}

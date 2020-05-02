/*
 * Copyright (C) 2007 The Android Open Source Project
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
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity

import com.example.android.apis.R

/**
 * Demonstrates inflating menus from XML. There are 6 different menu XML resources that the user can
 * choose to inflate: R.menu.title_only, R.menu.title_icon, R.menu.submenu, R.menu.groups,
 * R.menu.checkable, R.menu.shortcuts, R.menu.order, R.menu.category_order, R.menu.visible, and
 * R.menu.disabled and this Activity will use `MenuInflater.inflate` to inflate them.
 * R.menu.title_icon does not show the icon (boo hoo!), but oddly enough the submenu does?
 * First, select an example resource from the spinner, and then hit the menu button. To choose
 * another, back out of the activity and start over.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class MenuInflateFromXml : AppCompatActivity() {

    /**
     * Lets the user choose a menu resource.
     */
    private var mSpinner: Spinner? = null

    /**
     * Shown as instructions.
     */
    private var mInstructionsText: TextView? = null

    /**
     * This is the [Menu] passed us in our override of [onCreateOptionsMenu].
     * It is safe to hold on to this.
     */
    private var mMenu: Menu? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we create a [LinearLayout] instance to initialize our variable `val layout`,
     * and set its orientation to VERTICAL.
     *
     * In order to create the [Spinner] for our [mSpinner] field we create an `ArrayAdapter<String>`
     * to initialize our variable `val adapter` using the system layout file
     * android.R.layout.simple_spinner_item as the layout file for each item, and the [String] array
     * static field [sMenuExampleNames] for the `Object`'s to represent in the `ListView` of the
     * [Spinner]. We set the layout resource to create the drop down views of the `adapter` to the
     * `CheckedTextView` contained in the system layout file android.R.layout.simple_spinner_dropdown_item.
     * Next we initialize our [Spinner] field [mSpinner] with a new instance of [Spinner], set its
     * ID to R.id.spinner (so the system will automatically save its instance state), set the Adapter
     * for [mSpinner] to `adapter`, and set its [AdapterView.OnItemSelectedListener] to an anonymous
     * object whose `onItemSelected` override simply invalidates our options menu whenever it is
     * called. Having completely configured our [Spinner] filed [mSpinner] we add it to our
     * [LinearLayout] `layout` using the `LayoutParams` MATCH_PARENT and WRAP_CONTENT.
     *
     * Next we create the help text for our [TextView] field [mInstructionsText] by creating a new
     * instance of [TextView], setting its text to our resource string
     * R.string.menu_from_xml_instructions_press_menu ("Select a menu resource and press the menu
     * key"). We create a [LinearLayout.LayoutParams] instance to initialize our variable `val lp`
     * with the `LayoutParams` MATCH_PARENT and WRAP_CONTENT, set its left, top, bottom and right
     * margins to 10 pixels, and then add [mInstructionsText] to our [LinearLayout] `layout` using
     * `lp` as its `LayoutParams`. Finally we set the content view for our activity to `layout`.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use this
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create a simple layout
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        // Create the spinner to allow the user to choose a menu XML
        val adapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, sMenuExampleNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mSpinner = Spinner(this)
        // When programmatically creating views, make sure to set an ID
        // so it will automatically save its instance state
        mSpinner!!.id = R.id.spinner
        mSpinner!!.adapter = adapter
        mSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            /**
             * Callback method to be invoked when an item in this view has been selected. We simply
             * invalidate the current options menu by declaring that the options menu has changed,
             * so should be recreated. The `onCreateOptionsMenu(Menu)` method will be called
             * the next time it needs to be displayed.
             *
             * @param parent The [AdapterView] where the selection happened (Unused)
             * @param view The [View] within the [AdapterView] that was clicked (Unused)
             * @param position The position of the view in the adapter (Unused)
             * @param id The row id of the item that is selected (Unused)
             */
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                invalidateOptionsMenu()
            }

            /**
             * Callback method to be invoked when the selection disappears from this
             * view. The selection can disappear for instance when touch is activated
             * or when the adapter becomes empty. We do nothing.
             *
             * @param parent The AdapterView that now contains no selected item. (Unused)
             */
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Add the spinner
        layout.addView(mSpinner,
                LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT))

        // Create help text
        mInstructionsText = TextView(this)
        mInstructionsText!!.text = resources.getString(
                R.string.menu_from_xml_instructions_press_menu)

        // Add the help, make it look decent
        val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(10, 10, 10, 10)
        layout.addView(mInstructionsText, lp)

        // Set the layout as our content view
        setContentView(layout)
    }

    /**
     * Initialize the contents of the Activity's standard options menu. First we save a reference to
     * the [Menu] parameter [menu] that we will be filling in our [Menu] field [mMenu] (we will need
     * it in our [onOptionsItemSelected] override). Next we get a `MenuInflater` for this context
     * to initialize our variable `val inflater` and use it to inflate one of the different example
     * menu resource ID's in the [Int] array [sMenuExampleResources] based on which menu type is
     * currently selected by the [Spinner] field [mSpinner] into our [Menu] parameter [menu]. We
     * change the instructions in our [TextView] field [mInstructionsText] to read: "If you want to
     * choose another menu resource, go back and re-run this activity." (Rerunning the activity is
     * not really necessary because of the use of an `invalidateOptionsMenu()` call in the [Spinner]'s
     * `onItemSelected` override - simply choosing a different menu resource will change the menu
     * correctly.) Finally we return *true* so that the menu will be displayed.
     *
     * @param menu The options [Menu] in which we place our items.
     * @return You must return *true* for the menu to be displayed;
     * if you return *false* it will not be shown.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Hold on to this
        mMenu = menu

        // Inflate the currently selected menu XML resource.
        val inflater = menuInflater
        inflater.inflate(sMenuExampleResources[mSpinner!!.selectedItemPosition], menu)

        // Change instructions
        mInstructionsText!!.text = resources.getString(R.string.menu_from_xml_instructions_go_back)

        return true
    }

    /**
     * This hook is called whenever an item in your options menu is selected. We switch based on the
     * item ID of the `MenuItem item` that was selected:
     *  * R.id.jump - we toast the message "Jump up in the air!", invalidate the options menu (for
     *  some unknown reason) and return *true* to indicate that we consumed the event
     *  * R.id.dive - we toast the message "Dive into the water!" and return *true* to indicate that
     *  we consumed the event
     *  * R.id.browser_visibility - we toggle the visibility of the R.id.browser menu item group
     *  (contained in the menu/groups.xml menu resource, which is selected by the "Groups" item in
     *  the menu type selection `Spinner`.
     *  * R.id.email_visibility - we toggle the visibility of the R.id.email menu item group
     *  (contained in the menu/groups.xml menu resource, which is selected by the "Groups" item in
     *  the menu type selection `Spinner`.
     *  * default - catch all for all other menu item selections - if the [MenuItem] parameter [item]
     *  is a sub-menu we do nothing and return *false* to allow normal menu processing to proceed,
     *  otherwise we retrieve the title for the [MenuItem] parameter [item] and toast it, then return
     *  *true* to consume the event.
     *
     * If the [item] ID processing block selected did not have a return in it, we exit the *when*
     * block and return *false* to allow normal menu processing to proceed.
     *
     * @param item The menu item that was selected.
     * @return boolean Return *false* to allow normal menu processing to
     * proceed, *true* to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // For "Title only": Examples of matching an ID with one assigned in
            //                   the XML
            R.id.jump -> {
                Toast.makeText(this, "Jump up in the air!", Toast.LENGTH_SHORT).show()
                invalidateOptionsMenu()
                return true
            }

            R.id.dive -> {
                Toast.makeText(this, "Dive into the water!", Toast.LENGTH_SHORT).show()
                return true
            }

            // For "Groups": Toggle visibility of grouped menu items with
            //               non-grouped menu items
            R.id.browser_visibility -> {
                // The refresh item is part of the browser group
                val shouldShowBrowser = !mMenu!!.findItem(R.id.refresh).isVisible
                mMenu!!.setGroupVisible(R.id.browser, shouldShowBrowser)
            }

            R.id.email_visibility -> {
                // The reply item is part of the email group
                val shouldShowEmail = !mMenu!!.findItem(R.id.reply).isVisible
                mMenu!!.setGroupVisible(R.id.email, shouldShowEmail)
            }

            // Generic catch all for all the other menu resources
            else ->
                // Don't toast text when a submenu is clicked
                if (!item.hasSubMenu()) {
                    Toast.makeText(this, item.title, Toast.LENGTH_SHORT).show()
                    return true
                }
        }

        return false
    }

    /**
     * Our static constants.
     */
    companion object {
        /**
         * Different example menu resources.
         */
        private val sMenuExampleResources = intArrayOf(
                R.menu.title_icon,
                R.menu.title_only,
                R.menu.submenu,
                R.menu.groups,
                R.menu.checkable,
                R.menu.shortcuts,
                R.menu.order,
                R.menu.category_order,
                R.menu.visible,
                R.menu.disabled
        )

        /**
         * Names corresponding to the different example menu resources.
         */
        private val sMenuExampleNames = arrayOf(
                "Title and Icon",
                "Title only",
                "Submenu",
                "Groups",
                "Checkable",
                "Shortcuts",
                "Order",
                "Category and Order",
                "Visible",
                "Disabled"
        )
    }


}

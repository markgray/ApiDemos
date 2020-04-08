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
package com.example.android.apis.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Demonstrates horizontal and vertical gravity. Demonstrates changing gravity of a LinearLayout
 * programmatically using setOrientation(LinearLayout.*) as dictated by options menu choice between
 * horizontal and vertical gravity.
 */
class LinearLayout8 : AppCompatActivity() {
    /**
     * [LinearLayout] in our layout with ID R.id.layout
     */
    private var mLinearLayout: LinearLayout? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.custom_view_1.
     * Finally we initialize our [LinearLayout] field [mLinearLayout] by finding the view in our
     * layout with ID R.id.layout.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.linear_layout_8)
        mLinearLayout = findViewById(R.id.layout)
    }

    /**
     * Initialize the contents of the Activity's standard options menu. First we call through to our
     * super's implementation of `onCreateOptionsMenu`, then we add the following menu items
     * to our [Menu] parameter [menu]:
     *
     *  * VERTICAL_ID (1) - "Vertical"
     *
     *  * HORIZONTAL_ID (2) - "Horizontal"
     *
     *  * TOP_ID (3) - "Top"
     *
     *  * MIDDLE_ID (4) - "Middle"
     *
     *  * BOTTOM_ID (5) - "Bottom"
     *
     *  * LEFT_ID (6) - "Left"
     *
     *  * CENTER_ID (7) - "Center"
     *
     *  * RIGHT_ID (8) - "Right"
     *
     * We then return true to the caller so that the menu will be displayed.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menu.add(0, VERTICAL_ID, 0, R.string.linear_layout_8_vertical)
        menu.add(0, HORIZONTAL_ID, 0, R.string.linear_layout_8_horizontal)
        menu.add(0, TOP_ID, 0, R.string.linear_layout_8_top)
        menu.add(0, MIDDLE_ID, 0, R.string.linear_layout_8_middle)
        menu.add(0, BOTTOM_ID, 0, R.string.linear_layout_8_bottom)
        menu.add(0, LEFT_ID, 0, R.string.linear_layout_8_left)
        menu.add(0, CENTER_ID, 0, R.string.linear_layout_8_center)
        menu.add(0, RIGHT_ID, 0, R.string.linear_layout_8_right)
        return true
    }

    /**
     * This hook is called whenever an item in your options menu is selected. We switch based on the
     * item ID of our [MenuItem] parameter [item]:
     *
     *  * VERTICAL_ID - we set the orientation of [mLinearLayout] to VERTICAL and return
     *  true to the caller.
     *
     *  * HORIZONTAL_ID - we set the orientation of [mLinearLayout] to HORIZONTAL and return
     *  true to the caller.
     *
     *  * TOP_ID - we set the vertical gravity of [mLinearLayout] to TOP and return
     *  true to the caller.
     *
     *  * MIDDLE_ID - we set the vertical gravity of [mLinearLayout] to CENTER_VERTICAL
     *  and return true to the caller.
     *
     *  * BOTTOM_ID - we set the vertical gravity of [mLinearLayout] to BOTTOM and return
     *  true to the caller.
     *
     *  * LEFT_ID - we set the horizontal gravity of [mLinearLayout] to LEFT and return
     *  true to the caller.
     *
     *  * CENTER_ID - we set the horizontal gravity of [mLinearLayout] to CENTER_HORIZONTAL
     *  and return true to the caller.
     *
     *  * RIGHT_ID - we set the horizontal gravity of [mLinearLayout] to RIGHT and return
     *  true to the caller.
     *
     * If it is not one of our menu item IDs, we return the value returned by our super's
     * implementation of `onOptionsItemSelected`.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     */
    @SuppressLint("RtlHardcoded")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            VERTICAL_ID -> {
                mLinearLayout!!.orientation = LinearLayout.VERTICAL
                return true
            }
            HORIZONTAL_ID -> {
                mLinearLayout!!.orientation = LinearLayout.HORIZONTAL
                return true
            }
            TOP_ID -> {
                mLinearLayout!!.setVerticalGravity(Gravity.TOP)
                return true
            }
            MIDDLE_ID -> {
                mLinearLayout!!.setVerticalGravity(Gravity.CENTER_VERTICAL)
                return true
            }
            BOTTOM_ID -> {
                mLinearLayout!!.setVerticalGravity(Gravity.BOTTOM)
                return true
            }
            LEFT_ID -> {
                mLinearLayout!!.setHorizontalGravity(Gravity.LEFT)
                return true
            }
            CENTER_ID -> {
                mLinearLayout!!.setHorizontalGravity(Gravity.CENTER_HORIZONTAL)
                return true
            }
            RIGHT_ID -> {
                mLinearLayout!!.setHorizontalGravity(Gravity.RIGHT)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        // Menu item Ids
        const val VERTICAL_ID = Menu.FIRST
        const val HORIZONTAL_ID = Menu.FIRST + 1
        const val TOP_ID = Menu.FIRST + 2
        const val MIDDLE_ID = Menu.FIRST + 3
        const val BOTTOM_ID = Menu.FIRST + 4
        const val LEFT_ID = Menu.FIRST + 5
        const val CENTER_ID = Menu.FIRST + 6
        const val RIGHT_ID = Menu.FIRST + 7
    }
}
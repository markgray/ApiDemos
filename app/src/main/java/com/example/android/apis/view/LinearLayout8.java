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

package com.example.android.apis.view;

//Need the following import to get access to the app resources, since this
//class is in a sub-package.

import com.example.android.apis.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.LinearLayout;

/**
 * Demonstrates horizontal and vertical gravity. Demonstrates changing gravity of a LinearLayout
 * programmatically using setOrientation(LinearLayout.*) as dictated by options menu choice between
 * horizontal and vertical gravity.
 */
public class LinearLayout8 extends Activity {
    /**
     * {@code LinearLayout} in our layout with ID R.id.layout
     */
    private LinearLayout mLinearLayout;

    // Menu item Ids
    public static final int VERTICAL_ID = Menu.FIRST;
    public static final int HORIZONTAL_ID = Menu.FIRST + 1;

    public static final int TOP_ID = Menu.FIRST + 2;
    public static final int MIDDLE_ID = Menu.FIRST + 3;
    public static final int BOTTOM_ID = Menu.FIRST + 4;

    public static final int LEFT_ID = Menu.FIRST + 5;
    public static final int CENTER_ID = Menu.FIRST + 6;
    public static final int RIGHT_ID = Menu.FIRST + 7;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.custom_view_1.
     * Finally we initialize our field {@code LinearLayout mLinearLayout} by finding the view in our
     * layout with ID R.id.layout.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.linear_layout_8);
        mLinearLayout = (LinearLayout) findViewById(R.id.layout);
    }

    /**
     * Initialize the contents of the Activity's standard options menu. First we call through to our
     * super's implementation of {@code onCreateOptionsMenu}, then we add the following menu items
     * to our parameter {@code Menu menu}:
     * <ul>
     * <li>
     * VERTICAL_ID (1) - "Vertical"
     * </li>
     * <li>
     * HORIZONTAL_ID (2) - "Horizontal"
     * </li>
     * <li>
     * TOP_ID (3) - "Top"
     * </li>
     * <li>
     * MIDDLE_ID (4) - "Middle"
     * </li>
     * <li>
     * BOTTOM_ID (5) - "Bottom"
     * </li>
     * <li>
     * LEFT_ID (6) - "Left"
     * </li>
     * <li>
     * CENTER_ID (7) - "Center"
     * </li>
     * <li>
     * RIGHT_ID (8) - "Right"
     * </li>
     * </ul>
     * We then return true to the caller so that the menu will be displayed.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, VERTICAL_ID, 0, R.string.linear_layout_8_vertical);
        menu.add(0, HORIZONTAL_ID, 0, R.string.linear_layout_8_horizontal);
        menu.add(0, TOP_ID, 0, R.string.linear_layout_8_top);
        menu.add(0, MIDDLE_ID, 0, R.string.linear_layout_8_middle);
        menu.add(0, BOTTOM_ID, 0, R.string.linear_layout_8_bottom);
        menu.add(0, LEFT_ID, 0, R.string.linear_layout_8_left);
        menu.add(0, CENTER_ID, 0, R.string.linear_layout_8_center);
        menu.add(0, RIGHT_ID, 0, R.string.linear_layout_8_right);

        return true;
    }

    /**
     * This hook is called whenever an item in your options menu is selected. We switch based on the
     * item ID of our parameter {@code MenuItem item}:
     * <ul>
     * <li>
     * VERTICAL_ID - we set the orientation of {@code mLinearLayout} to VERTICAL and return
     * true to the caller.
     * </li>
     * <li>
     * HORIZONTAL_ID - we set the orientation of {@code mLinearLayout} to HORIZONTAL and return
     * true to the caller.
     * </li>
     * <li>
     * TOP_ID - we set the vertical gravity of {@code mLinearLayout} to TOP and return
     * true to the caller.
     * </li>
     * <li>
     * MIDDLE_ID - we set the vertical gravity of {@code mLinearLayout} to CENTER_VERTICAL
     * and return true to the caller.
     * </li>
     * <li>
     * BOTTOM_ID - we set the vertical gravity of {@code mLinearLayout} to BOTTOM and return
     * true to the caller.
     * </li>
     * <li>
     * LEFT_ID - we set the horizontal gravity of {@code mLinearLayout} to LEFT and return
     * true to the caller.
     * </li>
     * <li>
     * CENTER_ID - we set the horizontal gravity of {@code mLinearLayout} to CENTER_HORIZONTAL
     * and return true to the caller.
     * </li>
     * <li>
     * RIGHT_ID - we set the horizontal gravity of {@code mLinearLayout} to RIGHT and return
     * true to the caller.
     * </li>
     * </ul>
     * If it is not one of our menu item IDs, we return the value returned by our super's implementation
     * of {@code onOptionsItemSelected}.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     */
    @SuppressLint("RtlHardcoded")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case VERTICAL_ID:
                mLinearLayout.setOrientation(LinearLayout.VERTICAL);
                return true;
            case HORIZONTAL_ID:
                mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                return true;

            case TOP_ID:
                mLinearLayout.setVerticalGravity(Gravity.TOP);
                return true;
            case MIDDLE_ID:
                mLinearLayout.setVerticalGravity(Gravity.CENTER_VERTICAL);
                return true;
            case BOTTOM_ID:
                mLinearLayout.setVerticalGravity(Gravity.BOTTOM);
                return true;

            case LEFT_ID:
                mLinearLayout.setHorizontalGravity(Gravity.LEFT);
                return true;
            case CENTER_ID:
                mLinearLayout.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
                return true;
            case RIGHT_ID:
                mLinearLayout.setHorizontalGravity(Gravity.RIGHT);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }
}

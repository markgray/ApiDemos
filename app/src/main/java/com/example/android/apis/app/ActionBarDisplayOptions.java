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
package com.example.android.apis.app;

import com.example.android.apis.R;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * This demo shows how various action bar display option flags can be combined
 * and their effects.
 */
@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ActionBarDisplayOptions extends Activity implements View.OnClickListener,
        ActionBar.TabListener, Spinner.OnItemSelectedListener, ActionBar.OnNavigationListener {
    private View mCustomView;

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
     * "this". We create an ArrayAdapter<String> adapter using "this" as its context and the system
     * layout android.R.layout.simple_list_item_1 as its layout file which contains a TextView to
     * use when instantiating views. We add three items ("Item 1", "Item 2", and "Item 3") to
     * "adapter" then we the set adapter and navigation callback for list navigation mode to
     * "adapter" and "this" respectively.
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.action_bar_display_options);

        findViewById(R.id.toggle_home_as_up).setOnClickListener(this);
        findViewById(R.id.toggle_show_home).setOnClickListener(this);
        findViewById(R.id.toggle_use_logo).setOnClickListener(this);
        findViewById(R.id.toggle_show_title).setOnClickListener(this);
        findViewById(R.id.toggle_show_custom).setOnClickListener(this);
        findViewById(R.id.cycle_custom_gravity).setOnClickListener(this);
        findViewById(R.id.toggle_visibility).setOnClickListener(this);
        findViewById(R.id.toggle_system_ui).setOnClickListener(this);

        ((Spinner) findViewById(R.id.toggle_navigation)).setOnItemSelectedListener(this);

        mCustomView = getLayoutInflater().inflate(R.layout.action_bar_display_options_custom, null);
        // Configure several action bar elements that will be toggled by display options.
        final ActionBar bar = getActionBar();
        //noinspection ConstantConditions
        bar.setCustomView(mCustomView,
                new ActionBar.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        bar.addTab(bar.newTab().setText("Tab 1").setTabListener(this));
        bar.addTab(bar.newTab().setText("Tab 2").setTabListener(this));
        bar.addTab(bar.newTab().setText("Tab 3").setTabListener(this));

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1);
        adapter.add("Item 1");
        adapter.add("Item 2");
        adapter.add("Item 3");
        bar.setListNavigationCallbacks(adapter, this);
    }

    /**
     * Initialize the contents of the Activity's standard options menu. We simply fetch a  MenuInflater
     * with this context and use it to inflate our menu R.menu.display_options_actions into the
     * options menu "Menu menu" passed to us.
     *
     * @param menu The options menu in which you place your items.
     *
     * @return You must return true for the menu to be displayed;
     *         if you return false it will not be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.display_options_actions, menu);
        return true;
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
     *   R.id.toggle_home_as_up ("DISPLAY_HOME_AS_UP") flag: ActionBar.DISPLAY_HOME_AS_UP
     *       Display the 'home' element such that it appears as an 'up' affordance. e.g. show an
     *       arrow to the left indicating the action that will be taken. Set this flag if selecting
     *       the 'home' button in the action bar to return up by a single level in your UI rather
     *       than back to the top level or front page.
     *   R.id.toggle_show_home ("DISPLAY_SHOW_HOME") flag: ActionBar.DISPLAY_SHOW_HOME
     *       Show 'home' elements in this action bar, leaving more space for other navigation
     *       elements. This includes logo and icon.
     *   R.id.toggle_use_logo ("DISPLAY_USE_LOGO") flag: ActionBar.DISPLAY_USE_LOGO
     *       Show the logo defined as the attribute android:logo="@drawable/apidemo_androidlogo"
     *       in AndroidManifest.xml for the Activity (Shown only if ActionBar.DISPLAY_SHOW_HOME
     *       is also set.)
     *   R.id.toggle_show_title ("DISPLAY_SHOW_TITLE") flag: ActionBar.DISPLAY_SHOW_TITLE
     *       Show the activity title and subtitle, if present.
     *   R.id.toggle_show_custom ("DISPLAY_SHOW_CUSTOM") flag: ActionBar.DISPLAY_SHOW_CUSTOM
     *       Show the custom view if one has been set.
     *
     * The other Button's perform more complex operations than toggling using int flags and return
     * after doing them rather than fall through to end of the outer switch:
     *
     *   R.id.cycle_custom_gravity ("Cycle Custom View Gravity") first fetches the current layout
     *       parameters for the View mCustomView to the variable ActionBar.LayoutParams lp, then
     *       it switches based on the current value of the field lp.gravity masked with
     *       Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK (the Binary mask for horizontal gravity and
     *       script specific direction bit), and changes newGravity if the current value is:
     *         Gravity.START (Push object to x-axis position at the start of its container, not
     *           changing its size) changes to Gravity.CENTER_HORIZONTAL
     *         Gravity.CENTER_HORIZONTAL (Place object in the horizontal center of its container,
     *           not changing its size) changes to Gravity.END
     *         Gravity.END (Push object to x-axis position at the end of its container, not
     *           changing its size) changes to Gravity.START
     *       After deciding what newGravity is to be in the switch statement we set the appropriate
     *       bit in lp.gravity, then call bar.setCustomView(mCustomView, lp) to install our custom
     *       View (mCustomView) with our modified layout parameters "ActionBar.LayoutParams lp" as
     *       the the ActionBar's custom View. (The display option DISPLAY_SHOW_CUSTOM must be set
     *       for the custom view to be displayed). We then return.
     *   R.id.toggle_visibility ("Toggle Visibility") We check if the ActionBar is showing and if
     *       it is we call bar.hide() to hide the ActionBar, and if it is not we call bar.show()
     *       to show the ActionBar. In either case we then return.
     *   R.id.toggle_system_ui ("Toggle System UI") If the View.SYSTEM_UI_FLAG_FULLSCREEN bit of the
     *       last setSystemUiVisibility(int) that this view has requested is set we clear all bits
     *       of the system ui visibility, if it is not set we set the View.SYSTEM_UI_FLAG_FULLSCREEN
     *       bit. In either case we then return
     *
     * For the five Button's which set "flags" to the flag they wish to toggle and then "break",
     * we retrieve the current set of display options to "int change", toggle the flag in "change"
     * that needs to be toggled, then using flags as the mask to specify which flags to change
     * we set or clear only that bit in the ActionBar's display options by calling
     * bar.setDisplayOptions(change, flags)
     *
     * @param v Button which was clicked
     */
    @Override
    public void onClick(View v) {
        final ActionBar bar = getActionBar();
        int flags = 0;
        switch (v.getId()) {
            case R.id.toggle_home_as_up:
                flags = ActionBar.DISPLAY_HOME_AS_UP;
                break;
            case R.id.toggle_show_home:
                flags = ActionBar.DISPLAY_SHOW_HOME;
                break;
            case R.id.toggle_use_logo:
                flags = ActionBar.DISPLAY_USE_LOGO;
                break;
            case R.id.toggle_show_title:
                flags = ActionBar.DISPLAY_SHOW_TITLE;
                break;
            case R.id.toggle_show_custom:
                flags = ActionBar.DISPLAY_SHOW_CUSTOM;
                break;
            case R.id.cycle_custom_gravity:
                ActionBar.LayoutParams lp = (ActionBar.LayoutParams) mCustomView.getLayoutParams();
                int newGravity = 0;
                switch (lp.gravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
                    case Gravity.START:
                        newGravity = Gravity.CENTER_HORIZONTAL;
                        break;
                    case Gravity.CENTER_HORIZONTAL:
                        newGravity = Gravity.END;
                        break;
                    case Gravity.END:
                        newGravity = Gravity.START;
                        break;
                }
                lp.gravity = lp.gravity & ~Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK | newGravity;
                //noinspection ConstantConditions
                bar.setCustomView(mCustomView, lp);
                return;
            case R.id.toggle_visibility:
                //noinspection ConstantConditions
                if (bar.isShowing()) {
                    bar.hide();
                } else {
                    bar.show();
                }
                return;
            case R.id.toggle_system_ui:
                if ((getWindow().getDecorView().getSystemUiVisibility()
                        & View.SYSTEM_UI_FLAG_FULLSCREEN) != 0) {
                    getWindow().getDecorView().setSystemUiVisibility(0);
                } else {
                    getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_FULLSCREEN);
                }
                return;
        }

        //noinspection ConstantConditions
        int change = bar.getDisplayOptions() ^ flags;
        //noinspection WrongConstant
        bar.setDisplayOptions(change, flags);
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        final ActionBar bar = getActionBar();
        switch (parent.getId()) {
            case R.id.toggle_navigation:
                final int mode;
                switch (position) {
                    case 1:
                        mode = ActionBar.NAVIGATION_MODE_TABS;
                        break;
                    case 2:
                        mode = ActionBar.NAVIGATION_MODE_LIST;
                        break;
                    default:
                        mode = ActionBar.NAVIGATION_MODE_STANDARD;
                }
                //noinspection ConstantConditions
                bar.setNavigationMode(mode);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        return false;
    }
}

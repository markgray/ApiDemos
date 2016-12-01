/*
 * Copyright (C) 2012 The Android Open Source Project
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


import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

/**
 * This demonstrates the use of action bar tabs and how they interact
 * with other action bar features.
 */
@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
public class FragmentNestingTabs extends Activity {

    /**
     * Called when the activity is starting. First we turn on the framework's internal fragment manager
     * debugging logs, then we call through to our super's implementation of onCreate. Next we set
     * our local variable <b>ActionBar bar</b> to a reference to this activity's ActionBar. Because
     * <b>getActionBar()</b> will return null if there is no ActionBar we are careful to wrap each
     * of the following uses of <b>bar</b> in an "if" test to avoid using a null value:
     * <p>
     * 1. We set the current navigation mode of the ActionBar to NAVIGATION_MODE_TABS
     * <p>
     * 2. We disable the DISPLAY_SHOW_TITLE display option
     * <p>
     * 3. We add a Tab with title "Menus" and a TabListener which will load the Fragment
     * FragmentMenuFragment when the tab is selected.
     * <p>
     * 4. We add a Tab with title "Args" and a TabListener which will load the Fragment
     * FragmentArgumentsFragment when the tab is selected.
     * <p>
     * 5. We add a Tab with title "Stack" and a TabListener which will load the Fragment
     * FragmentStackFragment when the tab is selected.
     * <p>
     * 6. We add a Tab with title "tabs" and a TabListener which will load the Fragment
     * FragmentTabsFragment when the tab is selected.
     * <p>
     * Finally if <b>savedInstanceState</b> is not null (we are being recreated after an orientation
     * change) we set the selected navigation item to the value that was saved by our override of
     * onSaveInstanceState under the key "tab".
     *
     * @param savedInstanceState if we are being recreated after an orientation change this will
     *                           include the selected navigation item which was saved by our override
     *                           of onSaveInstanceState under the key "tab".
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FragmentManager.enableDebugLogging(true);
        super.onCreate(savedInstanceState);

        final ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }
        if (bar != null) {
            bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        }

        if (bar != null) {
            bar.addTab(bar.newTab()
                    .setText("Menus")
                    .setTabListener(new TabListener<>(
                            this, "menus", FragmentMenuFragment.class)));
        }
        if (bar != null) {
            bar.addTab(bar.newTab()
                    .setText("Args")
                    .setTabListener(new TabListener<>(
                            this, "args", FragmentArgumentsFragment.class)));
        }
        if (bar != null) {
            bar.addTab(bar.newTab()
                    .setText("Stack")
                    .setTabListener(new TabListener<>(
                            this, "stack", FragmentStackFragment.class)));
        }
        if (bar != null) {
            bar.addTab(bar.newTab()
                    .setText("Tabs")
                    .setTabListener(new TabListener<>(
                            this, "tabs", FragmentTabsFragment.class)));
        }

        if (savedInstanceState != null) {
            if (bar != null) {
                bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //noinspection ConstantConditions
        outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
    }

    public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
        private final Activity mActivity;
        private final String mTag;
        private final Class<T> mClass;
        private final Bundle mArgs;
        private Fragment mFragment;

        TabListener(Activity activity, String tag, Class<T> clz) {
            this(activity, tag, clz, null);
        }

        TabListener(Activity activity, String tag, Class<T> clz, Bundle args) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
            mArgs = args;

            // Check to see if we already have a fragment for this tab, probably
            // from a previously saved state.  If so, deactivate it, because our
            // initial state is that a tab isn't shown.
            mFragment = mActivity.getFragmentManager().findFragmentByTag(mTag);
            if (mFragment != null && !mFragment.isDetached()) {
                FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
                ft.detach(mFragment);
                ft.commit();
            }
        }

        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            if (mFragment == null) {
                mFragment = Fragment.instantiate(mActivity, mClass.getName(), mArgs);
                ft.add(android.R.id.content, mFragment, mTag);
            } else {
                ft.attach(mFragment);
            }
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                ft.detach(mFragment);
            }
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
            Toast.makeText(mActivity, "Reselected!", Toast.LENGTH_SHORT).show();
        }
    }
}


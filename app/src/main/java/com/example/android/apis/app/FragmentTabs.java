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
package com.example.android.apis.app;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

/**
 * This demonstrates the use of action bar tabs and how they interact with other action bar features.
 * The Activities chosen to populate the tabs all have their own uses for the action bar, and they
 * are switched in and out by a class which implements the interface {@code ActionBar.TabListener}
 * <p>
 * {@code TabListener<T extends Fragment> implements ActionBar.TabListener}
 */
@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
public class FragmentTabs extends Activity {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, then we retrieve a reference to this activity's ActionBar to initialize our variable
     * {@code ActionBar bar}. We set the navigation mode of {@code bar} to NAVIGATION_MODE_TABS, and
     * clear the DISPLAY_SHOW_TITLE display option of the {@code ActionBar} to make more room for our
     * UI. Next we add 4 tabs configured to launch 4 different {@code Fragment} classes. In each case
     * our procedure consists of using {@code bar.newTab()} to create a new ActionBar.Tab, set the
     * text of the new tab to a title appropriate for the {@code Fragment} the tab will control, set
     * the {@code TabListener} of the tab to a new instance of our class {@code TabListener<>} with
     * the arguments to the constructor of the {@code TabListener<>} chosen to give a unique tag
     * String and {@code Fragment} class, and then we add the tab to the {@code ActionBar bar} (the
     * tab is not included in the action bar until it is added). (The code for adding the 4 tabs is
     * identical except for the arguments passed to the methods used so I will spare you the details.)
     * <p>
     * Finally, if our parameter {@code Bundle savedInstanceState} is not null, we are being recreated
     * from a previous run so we retrieve the {@code int} that our override of {@code onSaveInstanceState}
     * saved in the {@code Bundle} under the key "tab" and use it to set the selected navigation item
     * of our tabbed {@code ActionBar}.
     *
     * @param savedInstanceState If not null we are being recreated so we retrieve the selected
     *                           navigation item number which was stored in our override of
     *                           onSaveInstanceState under the key "tab".
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActionBar bar = getActionBar();
        //noinspection ConstantConditions
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

        bar.addTab(bar.newTab()
                .setText("Simple")
                .setTabListener(new TabListener<>(
                        this, "simple", FragmentStack.CountingFragment.class)));
        bar.addTab(bar.newTab()
                .setText("Contacts")
                .setTabListener(new TabListener<>(
                        this, "contacts", LoaderCursor.CursorLoaderListFragment.class)));
        bar.addTab(bar.newTab()
                .setText("Apps")
                .setTabListener(new TabListener<>(
                        this, "apps", LoaderCustom.AppListFragment.class)));
        bar.addTab(bar.newTab()
                .setText("Throttle")
                .setTabListener(new TabListener<>(
                        this, "throttle", LoaderThrottle.ThrottledLoaderListFragment.class)));

        if (savedInstanceState != null) {
            bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
        }
    }

    /**
     * Called to retrieve per-instance state from an activity before being killed
     * so that the state can be restored in {@link #onCreate} or
     * {@link #onRestoreInstanceState} (the {@link Bundle} populated by this method
     * will be passed to both).
     * <p>
     * First we call through to our super's implementation of onCreate, then we retrieve a reference
     * to this activity's ActionBar, and use it to get the position of the selected navigation item
     * in the ActionBar, which we save in our parameter {@code Bundle outState} using the key "tab".
     *
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //noinspection ConstantConditions
        outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
    }

    /**
     *
     * @param <T> Class type we create when our tab is selected.
     */
    @SuppressWarnings("WeakerAccess")
    public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
        private final Activity mActivity;
        private final String mTag;
        private final Class<T> mClass;
        private final Bundle mArgs;
        private Fragment mFragment;

        public TabListener(Activity activity, String tag, Class<T> clz) {
            this(activity, tag, clz, null);
        }

        public TabListener(Activity activity, String tag, Class<T> clz, Bundle args) {
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

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            if (mFragment == null) {
                mFragment = Fragment.instantiate(mActivity, mClass.getName(), mArgs);
                ft.add(android.R.id.content, mFragment, mTag);
            } else {
                ft.attach(mFragment);
            }
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                ft.detach(mFragment);
            }
        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
            Toast.makeText(mActivity, "Reselected!", Toast.LENGTH_SHORT).show();
        }
    }
}


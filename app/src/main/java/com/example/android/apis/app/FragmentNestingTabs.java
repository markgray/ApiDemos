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
import android.util.Log;
import android.widget.Toast;

/**
 * This demonstrates the use of action bar tabs and how they interact
 * with other action bar features.
 */
@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
public class FragmentNestingTabs extends Activity {
    final static String TAG = "FragmentNestingTabs";

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

    /**
     * Called to retrieve per-instance state from an activity before being killed
     * so that the state can be restored in {@link #onCreate} or
     * {@link #onRestoreInstanceState} (the {@link Bundle} populated by this method
     * will be passed to both).
     * <p>
     * First we call through to our super's implementation of onSaveInstanceState, then we save the
     * position of the selected navigation item of our ActionBar's tabs in <b>Bundle outState</b>
     * using the key "tab".
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
     * This subclass of <b>ActionBar.TabListener</b> will create (or re-attach) a <b>Fragment</b>
     * specified by the arguments passed to its constructors.
     *
     * @param <T> A subclass of <b>Fragment</b> which we will create when the tab we are "listening"
     *            to is selected.
     */
    public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
        private final Activity mActivity; // FragmentNestingTabs Activity passed as this to constructors
        private final String mTag; // tag to use as name of fragment for later use by findFragmentByTag
        private final Class<T> mClass; // Class<T> clz from constructor (java Fragment subclass we create)
        private final Bundle mArgs; // Bundle of arguments to be passed to Fragment we create
        private Fragment mFragment; // Fragment instance we create (or find if already created)

        /**
         * This constructor merely hands off to the TabListener(Activity, String, Class, Bundle)
         * constructor using null as the Bundle that will be used to initialize our field
         * <b>Bundle mArgs</b>.
         *
         * @param activity the Activity FragmentNestingTabs onCreate override calls using "this"
         * @param tag      Tag we wish to have the FragmentManager to use for this Fragment
         * @param clz      Class of the Fragment to instantiate in our tab
         */
        TabListener(Activity activity, String tag, Class<T> clz) {
            this(activity, tag, clz, null);
        }

        /**
         * Constructor for a new instance of TabListener. First we use our parameters to initialize
         * our fields <b>Activity mActivity</b>, <b>String mTag</b>, <b>Class mClass</b>, and
         * <b>Bundle mArgs</b>. Then we search for a pre-existing Fragment for the tab by using the
         * FragmentManager associated with <b>Activity mActivity</b> to look for a Fragment that
         * used <b>tag</b> as its tag when it was added. If there is already a Fragment with that
         * tag, and it is not currently detached, we begin a <b>FragmentTransaction ft</b> which
         * we use to detach that Fragment, and commit the transaction.
         *
         * @param activity the FragmentNestingTabs Activity in our case
         * @param tag      name for this Fragment to later find the Fragment using <b>findFragmentByTag</b>
         * @param clz      Fragment subclass class that we want to instantiate
         * @param args     argument Bundle to pass to the new Fragment when it is instantiated.
         */
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

        /**
         * Called when a tab enters the selected state. If this is the first time this tab has been
         * selected (our field <b>mFragment</b> is null), we instantiate a Fragment using the class
         * specified in our field <b>Class mClass</b> and using the <b>Bundle mArgs</b> field to
         * supply the argument <b>Bundle</b> for the Fragment (if any), and  we use our argument
         * <b>FragmentTransaction ft</b> to add the Fragment to the Activity state. On the other
         * hand if our field <b>mFragment</b> is not null, then we already have an existing Fragment
         * so all have to do is use our argument <b>FragmentTransaction ft</b> to re-attach that
         * <b>Fragment mFragment</b> which had been detached from the UI with detach(Fragment). T
         * his causes its view hierarchy to be re-created, attached to the UI, and displayed when
         * the <b>FragmentTransaction ft</b> is eventually "committed.
         *
         * @param tab The tab that was selected
         * @param ft  A {@link FragmentTransaction} for queuing fragment operations to execute
         *            during a tab switch. The previous tab's un-select and this tab's select will be
         *            executed in a single transaction. This FragmentTransaction does not support
         *            being added to the back stack.
         */
        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            if (mFragment == null) {
                mFragment = Fragment.instantiate(mActivity, mClass.getName(), mArgs);
                ft.add(android.R.id.content, mFragment, mTag);
            } else {
                ft.attach(mFragment);
            }
        }

        /**
         * Called when a tab exits the selected state. If our field <b>Fragment mFragment</b> is not
         * null, we use our parameter <b>FragmentTransaction ft</b> to detach it. If <b>mFragment</b>
         * is null we do nothing, although I cannot think of a reason why it would be null.
         *
         * @param tab The tab that was unselected
         * @param ft  A {@link FragmentTransaction} for queuing fragment operations to execute
         *            during a tab switch. This tab's un-select and the newly selected tab's select
         *            will be executed in a single transaction. This FragmentTransaction does not
         *            support being added to the back stack.
         */
        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                ft.detach(mFragment);
            } else {
                Log.i(TAG, "un-select occurred with a null mFragment");
            }
        }

        /**
         * Called when a tab that is already selected is chosen again by the user.
         * Some applications may use this action to return to the top level of a category.
         * <p>
         * We do nothing but "Toast" about this event.
         *
         * @param tab The tab that was reselected.
         * @param ft  A {@link FragmentTransaction} for queuing fragment operations to execute
         *            once this method returns. This FragmentTransaction does not support
         *            being added to the back stack.
         */
        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
            Toast.makeText(mActivity, "Reselected!", Toast.LENGTH_SHORT).show();
        }
    }
}


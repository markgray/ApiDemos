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
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBar.Tab;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import org.jetbrains.annotations.NotNull;

/**
 * This demonstrates the use of action bar tabs and how they interact with other action bar features.
 * The Activities chosen to populate the tabs all have their own uses for the action bar, and they
 * are switched in and out by a class which implements the interface {@code ActionBar.TabListener}
 * <p>
 * {@code TabListener<T extends Fragment> implements ActionBar.TabListener}
 */
@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
public class FragmentTabs extends AppCompatActivity {
    final static String TAG = "FragmentTabs"; // TAG for logging

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

        final ActionBar bar = getSupportActionBar();
        //noinspection ConstantConditions
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

        bar.addTab(bar.newTab()
                .setText("Simple 1")
                .setTabListener(new TabListener<>(
                        this, "simple1", FragmentStack.CountingFragment.class)));
        bar.addTab(bar.newTab()
                .setText("Simple 2")
                .setTabListener(new TabListener<>(
                        this, "simple2", FragmentStack.CountingFragment.class)));
        bar.addTab(bar.newTab()
                .setText("Simple 3")
                .setTabListener(new TabListener<>(
                        this, "simple3", FragmentStack.CountingFragment.class)));
        bar.addTab(bar.newTab()
                .setText("Simple 4")
                .setTabListener(new TabListener<>(
                        this, "simple4", FragmentStack.CountingFragment.class)));

//        bar.addTab(bar.newTab()
//                .setText("Contacts")
//                .setTabListener(new TabListener<>(
//                        this, "contacts", LoaderCursor.CursorLoaderListFragment.class)));
//        bar.addTab(bar.newTab()
//                .setText("Apps")
//                .setTabListener(new TabListener<>(
//                        this, "apps", LoaderCustom.AppListFragment.class)));
//        bar.addTab(bar.newTab()
//                .setText("Throttle")
//                .setTabListener(new TabListener<>(
//                        this, "throttle", LoaderThrottle.ThrottledLoaderListFragment.class)));

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
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //noinspection ConstantConditions
        outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
    }

    /**
     * A {@code TabListener} instance is created for each tab in the {@code ActionBar} in order to
     * handle switching to and from the tab. When its {@code onTabSelected} override is called it will
     * instantiate or reattach the {@code Fragment} class it was constructed for, when its
     * {@code onTabUnselected} override is called it will detach its {@code Fragment}.
     *
     * @param <T> Class type we create when our tab is selected.
     */
    @SuppressWarnings("WeakerAccess")
    public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
        private final Activity mActivity; // Activity passed to our constructor used when Context is needed, "this" from FragmentTabs onCreate
        private final String mTag; // tag passed to our constructor used as tag name for the Fragment we add
        private final Class<T> mClass; // Class of the Fragment we control
        private final Bundle mArgs; // Arguments Bundle for the Fragment we instantiate (we do not use this feature, all Fragment's are created without arguments
        private Fragment mFragment; // Reference to Fragment instance we have created

        /**
         * Constructor for a Fragment which does not require an argument Bundle. We simply call the
         * constructor for a Fragment which requires an argument Bundle using null as that Bundle.
         *
         * @param activity used for Context in various places
         * @param tag      tag name to use when adding our Fragment
         * @param clz      Class name of the Fragment instance we create and control
         */
        public TabListener(FragmentActivity activity, String tag, Class<T> clz) {
            this(activity, tag, clz, null);
        }

        /**
         * Constructor for a Fragment which requires an argument Bundle. We first use our parameters
         * to initialize our fields {@code Activity mActivity}, {@code String mTag},
         * {@code Class<T> mClass}, and {@code Bundle mArgs} respectively. Then we use the
         * FragmentManager for interacting with fragments associated with this activity to search for
         * a {@code Fragment mFragment} identified with our tag {@code String mTag}. If one is found
         * and it is not already explicitly detached from the UI we need to detach it from the UI (
         * This only happens when an orientation change occurs while our Fragment has control of the
         * UI, and it needs to be recreated for the new orientation.) To do this we use the
         * FragmentManager for interacting with fragments associated with thisactivity to being a
         * new {@code FragmentTransaction ft}, use {@code ft} to detach {@code Fragment mFragment},
         * and commit the transaction.
         *
         * @param activity used for Context in various places
         * @param tag      tag name to use when adding our Fragment
         * @param clz      Class name of the Fragment instance we create and control
         * @param args     Bundle of arguments which will be passed to our Fragment when we instantiate it
         */
        public TabListener(Activity activity, String tag, Class<T> clz, Bundle args) {
            mActivity = activity;

            mTag = tag;
            mClass = clz;
            mArgs = args;

            // Check to see if we already have a fragment for this tab, probably
            // from a previously saved state.  If so, deactivate it, because our
            // initial state is that a tab isn't shown.
            mFragment = ((FragmentActivity) mActivity).getSupportFragmentManager().findFragmentByTag(mTag);
            if (mFragment != null && !mFragment.isDetached()) {
                FragmentTransaction ft = ((FragmentActivity) mActivity).getSupportFragmentManager().beginTransaction();
                ft.detach(mFragment);
                ft.commit();
            }
        }

        /**
         * Called when our tab enters the selected state. If this is the first time our tab has been
         * selected (mFragment == null) we initialize {@code Fragment mFragment} with a new instance
         * of the {@code Fragment} class our tab contains, and use our parameter {@code FragmentTransaction ft}
         * to add this fragment to the activity state into the Activity's content view using the tag
         * {@code String mTag}. If we have previously been selected our {@code Fragment mFragment} is
         * intact but detached, so we use our parameter {@code FragmentTransaction ft} to re-attach
         * our {@code Fragment mFragment}.
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
         * Called when the tab exits the selected state. To be safe we first test that we do have
         * an existing {@code Fragment mFragment}, and if so we proceed to use our parameter
         * {@code FragmentTransaction ft} to detach our {@code Fragment} from the UI.
         *
         * @param tab The tab that was unselected
         * @param ft  A {@link FragmentTransaction} for queuing fragment operations to execute
         *            during a tab switch. This tab's unselect and the newly selected tab's select
         *            will be executed in a single transaction. This FragmentTransaction does not
         *            support being added to the back stack.
         */
        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                ft.detach(mFragment);
            } else {
                Log.i(TAG, "mFragment was null when unselected -- how odd!");
            }
        }

        /**
         * Called when a tab that is already selected is chosen again by the user.
         * Some applications may use this action to return to the top level of a category.
         * <p>
         * We simply Toast the fact that we were reselected.
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


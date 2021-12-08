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
@file:Suppress("DEPRECATION")
// TODO: Replace Tab use with modern navigation UI
package com.example.android.apis.app

import android.annotation.TargetApi
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBar.Tab
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction

/**
 * This demonstrates the use of action bar tabs and how they interact with other action bar features.
 * The Activities chosen to populate the tabs all have their own uses for the action bar, and they
 * are switched in and out by a class which implements the interface [ActionBar.TabListener]
 *
 * `TabListener<T extends Fragment> implements ActionBar.TabListener`
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
class FragmentTabs : AppCompatActivity() {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we retrieve a reference to this activity's support [ActionBar] to initialize
     * our [ActionBar] variable `val bar`. We set the navigation mode of `bar` to NAVIGATION_MODE_TABS,
     * and clear the DISPLAY_SHOW_TITLE display option of the [ActionBar] to make more room for our
     * UI. Next we add 4 tabs configured to launch 4 different (eventually) [Fragment] classes. In
     * each case our procedure consists of using `bar.newTab()` to create a new [ActionBar.Tab], set
     * the text of the new tab to a title appropriate for the [Fragment] the tab will control, set
     * the [TabListener] of the tab to a new instance of our class `TabListener<>` with the arguments
     * to the constructor of the `TabListener<>` chosen to give a unique tag String and [Fragment]
     * class, and then we add the tab to the `bar` (the tab is not included in the action bar until
     * it is added). (The code for adding the 4 tabs is identical except for the arguments passed to
     * the methods used so I will spare you the details.)
     *
     * Finally, if our [Bundle] parameter [savedInstanceState] is not null, we are being recreated
     * from a previous run so we retrieve the [Int] that our override of [onSaveInstanceState]
     * saved in the [Bundle] under the key "tab" and use it to set the selected navigation item
     * of our tabbed [ActionBar].
     *
     * @param savedInstanceState If not *null* we are being recreated so we retrieve the selected
     * navigation item number which was stored in the [Bundle] by our override of [onSaveInstanceState]
     * under the key "tab".
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bar = supportActionBar

        bar!!.navigationMode = ActionBar.NAVIGATION_MODE_TABS
        bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE)

        bar.addTab(bar.newTab()
                .setText("Simple")
                .setTabListener(TabListener(
                        this,
                        "simple1",
                        FragmentStack.CountingFragment::class.java)))

        bar.addTab(bar.newTab()
                .setText("Contacts")
                .setTabListener(TabListener(
                        this,
                        "contacts",
                        LoaderCursor.CursorLoaderListFragment::class.java)))

        bar.addTab(bar.newTab()
                .setText("Apps")
                .setTabListener(TabListener(
                        this,
                        "apps",
                        LoaderCustom.AppListFragment::class.java)))

        bar.addTab(bar.newTab()
                .setText("Throttle")
                .setTabListener(TabListener(
                        this,
                        "throttle",
                        LoaderThrottle.ThrottledLoaderListFragment::class.java)))

        if (savedInstanceState != null) {
            bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0))
        }
    }

    /**
     * Called to retrieve per-instance state from an activity before being killed so that the state
     * can be restored in [onCreate] or [onRestoreInstanceState] (the [Bundle] populated by this
     * method will be passed to both).
     *
     * First we call through to our super's implementation of `onCreate`, then we retrieve a
     * reference to this activity's [ActionBar], and use it to get the position of the selected
     * navigation item in the [ActionBar], which we save in our [Bundle] parameter [outState] under
     * the key "tab".
     *
     * @param outState Bundle in which to place your saved state.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt("tab", supportActionBar!!.selectedNavigationIndex)
    }

    /**
     * A [TabListener] instance is created for each tab in the [ActionBar] in order to handle
     * switching to and from the tab. When its `onTabSelected` override is called it will
     * instantiate or reattach the [Fragment] class it was constructed for, when its
     * `onTabUnselected` override is called it will detach its `Fragment`.
     *
     * @param T Class type we create when our tab is selected.
     */
    class TabListener<T : Fragment>
    /**
     * Constructor for a [Fragment] which requires an argument [Bundle]. We first use our parameters
     * to initialize our [Activity] field [mActivity], [String] field [mTag], `Class<T>` field
     * [mClass], and [Bundle] field [mArgs] respectively. Then we use the support `FragmentManager`
     * for interacting with fragments associated with this activity to search for a [Fragment]
     * identified with our [String] tag [mTag] to initialize our field [mFragment]. If one is found
     * and it is not already explicitly detached from the UI we need to detach it from the UI (this
     * only happens when an orientation change occurs while our Fragment has control of the UI, and
     * it needs to be recreated for the new orientation.) To do this we use the `FragmentManager`
     * to begin a new [FragmentTransaction] to initialize our variable `val ft`, use `ft` to detach
     * [mFragment], and commit the transaction.
     *
     * @param mActivity used for Context in various places
     * @param mTag      tag name to use when adding our Fragment
     * @param mClass    Class name of the Fragment instance we create and control
     * @param mArgs     Bundle of arguments which will be passed to our Fragment when we instantiate it
     */
    (private val mActivity: Activity // Activity passed to our constructor used when Context is needed, "this" from FragmentTabs onCreate
     , private val mTag: String // tag passed to our constructor used as tag name for the Fragment we add
     , private val mClass: Class<T> // Class of the Fragment we control
     , private val mArgs: Bundle? // Arguments Bundle for the Fragment we instantiate (we do not use this feature, all Fragment's are created without arguments
    ) : ActionBar.TabListener {
        /**
         * Reference to [Fragment] instance we have created
         */
        private var mFragment: Fragment? = null

        /**
         * Constructor for a [Fragment] which does not require an argument [Bundle]. We simply call
         * the constructor for a [Fragment] which requires an argument [Bundle] using *null* as that
         * [Bundle].
         *
         * @param activity used for `Context` in various places
         * @param tag      tag name to use when adding our [Fragment]
         * @param clz      [Class] name of the [Fragment] instance we create and control
         */
        constructor(
                activity: AppCompatActivity,
                tag: String,
                clz: Class<T>
        ) : this(activity, tag, clz, null)

        init {

            // Check to see if we already have a fragment for this tab, probably
            // from a previously saved state.  If so, deactivate it, because our
            // initial state is that a tab isn't shown.
            mFragment = (mActivity as AppCompatActivity).supportFragmentManager.findFragmentByTag(mTag)
            if (mFragment != null && !mFragment!!.isDetached) {
                val ft = mActivity.supportFragmentManager.beginTransaction()
                ft.detach(mFragment!!)
                ft.commit()
            }
        }

        /**
         * Called when our tab enters the selected state. If this is the first time our tab has been
         * selected ([mFragment] == *null*) we initialize our [Fragment] field [mFragment] with a
         * new instance of the [Fragment] class our tab contains, and use our [FragmentTransaction]
         * parameter [ft] to add this fragment to the activity state as the content of the
         * Activity's content view using the [String] tag in our field [mTag]. If we have previously
         * been selected our [Fragment] field [mFragment] is intact but detached, so we use our
         * parameter [ft] to re-attach [mFragment].
         *
         * @param tab The tab that was selected
         * @param ft  A [FragmentTransaction] for queuing fragment operations to execute
         * during a tab switch. The previous tab's un-select and this tab's select will be
         * executed in a single transaction. This [FragmentTransaction] does not support
         * being added to the back stack.
         */
        override fun onTabSelected(tab: Tab, ft: FragmentTransaction) {
            if (mFragment == null) {
                mFragment = Fragment.instantiate(mActivity, mClass.name, mArgs)
                ft.add(android.R.id.content, mFragment!!, mTag)
            } else {
                ft.attach(mFragment!!)
            }
        }

        /**
         * Called when the tab exits the selected state. To be safe we first test that we do have
         * an existing [Fragment] in our field [mFragment], and if so we proceed to use our
         * [FragmentTransaction] parameter [ft] to detach [mFragment] from the UI.
         *
         * @param tab The tab that was unselected
         * @param ft  A [FragmentTransaction] for queuing fragment operations to execute
         * during a tab switch. This tab's unselect and the newly selected tab's select
         * will be executed in a single transaction. This [FragmentTransaction] does not
         * support being added to the back stack.
         */
        override fun onTabUnselected(tab: Tab, ft: FragmentTransaction) {
            if (mFragment != null) {
                ft.detach(mFragment!!)
            } else {
                Log.i(TAG, "mFragment was null when unselected -- how odd!")
            }
        }

        /**
         * Called when a tab that is already selected is chosen again by the user. Some applications
         * may use this action to return to the top level of a category. We simply Toast the fact
         * that we were reselected.
         *
         * @param tab The tab that was reselected.
         * @param ft  A [FragmentTransaction] for queuing fragment operations to execute
         * once this method returns. This [FragmentTransaction] does not support
         * being added to the back stack.
         */
        override fun onTabReselected(tab: Tab, ft: FragmentTransaction) {
            Toast.makeText(mActivity, "Reselected!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Our static constant.
     */
    companion object {
        /**
         * TAG for logging
         */
        internal const val TAG = "FragmentTabs"
    }
}


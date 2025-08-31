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
package com.example.android.apis.app

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

/**
 * This demonstrates the use of action bar tabs and how they interact
 * with other action bar features.
 */
@SuppressLint("ObsoleteSdkInt")
@Suppress("DEPRECATION") // TODO: replace tabs with modern navigation UI
@RequiresApi(Build.VERSION_CODES.HONEYCOMB_MR2)
class FragmentNestingTabs : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we turn on the framework's internal fragment
     * manager debugging logs, then we call through to our super's implementation of `onCreate`.
     * Next we set our [ActionBar] variable `val bar` to a reference to this activity's support
     * [ActionBar]. Because `supportActionBar` will return null if there is no [ActionBar] we are
     * careful to wrap each of the following uses of `bar`` in an "if" test to avoid using a null
     * value:
     *
     *  1. We set the current navigation mode of the [ActionBar] to NAVIGATION_MODE_TABS
     *
     *  2. We disable the DISPLAY_SHOW_TITLE display option
     *
     *  3. We add a Tab with title "Menus" and a [TabListener] which will load the [Fragment]
     *  [FragmentMenuFragment] when the tab is selected.
     *
     *  4. We add a Tab with title "Args" and a [TabListener] which will load the [Fragment]
     *  [FragmentArgumentsFragment] when the tab is selected.
     *
     *  5. We add a Tab with title "Stack" and a [TabListener] which will load the [Fragment]
     *  [FragmentStackFragment] when the tab is selected.
     *
     *  6. We add a Tab with title "tabs" and a [TabListener] which will load the [Fragment]
     *  [FragmentTabsFragment] when the tab is selected.
     *
     * Finally if [savedInstanceState] is not null (we are being recreated after an orientation
     * change) we set the selected navigation item to the value that was saved by our override of
     * [onSaveInstanceState] under the key "tab".
     *
     * @param savedInstanceState if we are being recreated after an orientation change this will
     * include the selected navigation item which was saved by our override of [onSaveInstanceState]
     * under the key "tab".
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        FragmentManager.enableDebugLogging(true)
        super.onCreate(savedInstanceState)
        val bar = supportActionBar
        if (bar != null) {
            bar.navigationMode = ActionBar.NAVIGATION_MODE_TABS
        }
        bar?.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE)
        bar?.addTab(
            bar.newTab()
                .setText("Menus")
                .setTabListener(
                    TabListener(
                        this, "menus", FragmentMenuFragment::class.java
                    )
                )
        )
        bar?.addTab(
            bar.newTab()
                .setText("Args")
                .setTabListener(
                    TabListener(
                        this, "args", FragmentArgumentsFragment::class.java
                    )
                )
        )
        bar?.addTab(
            bar.newTab()
                .setText("Stack")
                .setTabListener(
                    TabListener(
                        this, "stack", FragmentStackFragment::class.java
                    )
                )
        )
        bar?.addTab(
            bar.newTab()
                .setText("Tabs")
                .setTabListener(
                    TabListener(
                        this, "tabs", FragmentTabsFragment::class.java
                    )
                )
        )
        if (savedInstanceState != null) {
            bar?.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0))
        }
    }

    /**
     * Called to retrieve per-instance state from an activity before being killed so that the state
     * can be restored in [onCreate] or [onRestoreInstanceState] (the [Bundle] populated by this
     * method will be passed to both).
     *
     * First we call through to our super's implementation of `onSaveInstanceState`, then we save
     * the position of the selected navigation item of our [ActionBar]'s tabs in [Bundle] parameter
     * [outState] using the key "tab".
     *
     * @param outState [Bundle] in which to place your saved state.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("tab", actionBar!!.selectedNavigationIndex)
    }

    /**
     * This subclass of [ActionBar.TabListener] will create (or re-attach) a [Fragment] specified
     * by the arguments passed to its constructors.
     *
     * @param <T> A subclass of [Fragment] which we will create when the tab we are "listening"
     * to is selected.
     */
    class TabListener<T : Fragment?> @JvmOverloads internal constructor(
        /**
         * FragmentNestingTabs Activity passed as this to constructors
         */
        private val mActivity: Activity,
        /**
         * tag to use as name of fragment for later use by findFragmentByTag
         */
        private val mTag: String,
        /**
         * `Class<T>` clz from constructor (java [Fragment] subclass we create)
         */
        private val mClass: Class<T>,
        /**
         * [Bundle] of arguments to be passed to [Fragment] we create
         */
        private val mArgs: Bundle? = null
    ) : ActionBar.TabListener {

        /**
         * [Fragment] instance we create (or find if already created)
         */
        private var mFragment: Fragment?

        /**
         * Called when a tab enters the selected state. If this is the first time this tab has been
         * selected (our field [mFragment] is null), we instantiate a [Fragment] using the class
         * specified in our [Class] field [mClass] and using the [Bundle] field [mArgs] to supply
         * the argument [Bundle] for the [Fragment] (if any), and  we use our [FragmentTransaction]
         * argument [ft] to add the [Fragment] to the Activity state. On the other hand if our field
         * [mFragment] is not null, then we already have an existing [Fragment] so all have to do is
         * use our [FragmentTransaction] argument [ft] to re-attach that [Fragment] field [mFragment]
         * which had been detached from the UI with `detach(Fragment)`. This causes its view hierarchy
         * to be re-created, attached to the UI, and displayed when the [FragmentTransaction] argument
         * [ft] is eventually committed.
         *
         * @param tab The tab that was selected
         * @param ft  A [FragmentTransaction] for queuing fragment operations to execute
         * during a tab switch. The previous tab's un-select and this tab's select will be
         * executed in a single transaction. This [FragmentTransaction] does not support
         * being added to the back stack.
         */
        override fun onTabSelected(tab: ActionBar.Tab, ft: FragmentTransaction) {
            if (mFragment == null) {
                mFragment = Fragment.instantiate(mActivity, mClass.name, mArgs)
                ft.add(android.R.id.content, mFragment!!, mTag)
            } else {
                ft.attach(mFragment!!)
            }
        }

        /**
         * Called when a tab exits the selected state. If our [Fragment] field [mFragment] is not
         * null, we use our [FragmentTransaction] parameter [ft] to detach it. If [mFragment] is
         * null we do nothing, although I cannot think of a reason why it would be null.
         *
         * @param tab The tab that was unselected
         * @param ft  A [FragmentTransaction] for queuing fragment operations to execute
         * during a tab switch. This tab's un-select and the newly selected tab's select
         * will be executed in a single transaction. This [FragmentTransaction] does not
         * support being added to the back stack.
         */
        override fun onTabUnselected(tab: ActionBar.Tab, ft: FragmentTransaction) {
            if (mFragment != null) {
                ft.detach(mFragment!!)
            } else {
                Log.i(TAG, "un-select occurred with a null mFragment")
            }
        }

        /**
         * Called when a tab that is already selected is chosen again by the user. Some applications
         * may use this action to return to the top level of a category. We do nothing but "Toast"
         * about this event.
         *
         * @param tab The tab that was reselected.
         * @param ft  A [FragmentTransaction] for queuing fragment operations to execute
         * once this method returns. This [FragmentTransaction] does not support
         * being added to the back stack.
         */
        override fun onTabReselected(tab: ActionBar.Tab, ft: FragmentTransaction) {
            Toast.makeText(mActivity, "Reselected!", Toast.LENGTH_SHORT).show()
        }

        /**
         * Init block for our constructor. First we search for a pre-existing `Fragment` for the
         * tab by using the `FragmentManager` associated with `Activity` field `mActivity` to look
         * for a `Fragment` that used `mTag` as its tag when it was added. If there is already a
         * `Fragment` with that tag, and it is not currently detached, we begin a `FragmentTransaction`
         * variable `val ft` which we use to detach that `Fragment`, and commit the transaction.
         */
        init {
            // Check to see if we already have a fragment for this tab, probably
            // from a previously saved state.  If so, deactivate it, because our
            // initial state is that a tab isn't shown.
            mFragment =
                (mActivity as AppCompatActivity).supportFragmentManager.findFragmentByTag(mTag)
            if (mFragment != null && !mFragment!!.isDetached) {
                val ft = mActivity.supportFragmentManager.beginTransaction()
                ft.detach(mFragment!!)
                ft.commit()
            }
        }
    }

    companion object {
        /**
         * TAG used for logging
         */
        const val TAG: String = "FragmentNestingTabs"
    }
}
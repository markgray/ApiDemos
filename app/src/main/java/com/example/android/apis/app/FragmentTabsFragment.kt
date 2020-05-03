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
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TabHost
import android.widget.TabHost.OnTabChangeListener
import android.widget.TabHost.TabContentFactory
import android.widget.TabHost.TabSpec

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

import com.example.android.apis.R

import com.example.android.apis.app.FragmentReceiveResult.ReceiveResultFragment
import com.example.android.apis.app.LoaderCustom.AppListFragment
import com.example.android.apis.app.LoaderThrottle.ThrottledLoaderListFragment

import java.util.ArrayList

/**
 * Sample fragment that contains tabs of other fragments.
 */
@Suppress("MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class FragmentTabsFragment : Fragment() {
    /**
     * Our custom [OnTabChangeListener] (Should use `FragmentTabHost` instead) Its `onTabChanged`
     * override switches fragments when a new tab is selected.
     */
    var mTabManager: TabManager? = null

    /**
     * Called to do initial creation of a fragment. First we call our super's implementation of
     * `onCreate`, then we initialize our [TabManager] field [mTabManager] with a new instance
     * constructed to use the `FrameLayout` with ID R.id.realtabcontent in our layout file to hold
     * the [Fragment] selected by the currently selected tab.
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mTabManager = TabManager(
                (activity as Context),
                childFragmentManager,
                R.id.realtabcontent
        )
    }

    /**
     * Called to have the fragment instantiate its user interface view. We initialize our [View]
     * variable `val v` by using our [LayoutInflater] parameter [inflater] to inflate the layout
     * file with ID R.layout.fragment_tabs_fragment using our [ViewGroup] parameter [container] for
     * LayoutParams without attaching to it. We initialize our [TabHost] variable `val host` to the
     * [TabHost] returned by the `handleCreateView` of our [TabManager] field [mTabManager]. We then
     * use the `addTab` method of [mTabManager] to add four tabs:
     *
     *  - The [ReceiveResultFragment] fragment tab with the tag "result" and the label "Result"
     *
     *  - The [LoaderCursor.CursorLoaderListFragment] fragment tab with the tag "contacts" and the
     *  label "Contacts"
     *
     *  - The [AppListFragment] fragment tab with the tag "apps" and the label "Apps"
     *
     *  - The [ThrottledLoaderListFragment] fragment tab with the tag "throttle" and the label
     *  "Throttle"
     *
     * Finally we return `v` to the caller.
     *
     * @param inflater The [LayoutInflater] object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return Return the View for the fragment's UI
     */
    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(
                R.layout.fragment_tabs_fragment,
                container,
                false
        )
        val host = mTabManager!!.handleCreateView(v)
        mTabManager!!.addTab(
                host!!.newTabSpec("result").setIndicator("Result"),
                ReceiveResultFragment::class.java,
                null
        )
        mTabManager!!.addTab(
                host.newTabSpec("contacts").setIndicator("Contacts"),
                LoaderCursor.CursorLoaderListFragment::class.java,
                null
        )
        mTabManager!!.addTab(
                host.newTabSpec("apps").setIndicator("Apps"),
                AppListFragment::class.java,
                null
        )
        mTabManager!!.addTab(
                host.newTabSpec("throttle").setIndicator("Throttle"),
                ThrottledLoaderListFragment::class.java,
                null
        )
        return v
    }

    /**
     * Called when all saved state has been restored into the view hierarchy of the fragment. This
     * can be used to do initialization based on saved state that you are letting the view hierarchy
     * track itself, such as whether check box widgets are currently checked. This is called after
     * [onActivityCreated] and before [onStart]. First we call our super's implementation of
     * [onViewStateRestored], then we call the `handleViewStateRestored` method of our [TabManager]
     * field [mTabManager] to have it restore the selected tab to the one selected before we were
     * restarted (if we are being re-created).
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state
     */
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        mTabManager!!.handleViewStateRestored(savedInstanceState)
    }

    /**
     * Called when the view previously created by [onCreateView] has been detached from the fragment.
     * to be displayed, a new view will be created. This is called after [onStop] and before
     * [onDestroy]  It is called regardless of whether [onCreateView] returned a non-null view.
     * Internally it is called after the view's state has been saved but before it has been removed
     * from its parent. First we call our super's implementation of `onDestroyView` then we call the
     * `handleDestroyView` method of our [TabManager] field [mTabManager].
     */
    override fun onDestroyView() {
        super.onDestroyView()
        mTabManager!!.handleDestroyView()
    }

    /**
     * Called to ask the fragment to save its current dynamic state, so it can later be reconstructed
     * in a new instance if its process is restarted. First we call our super's implementation of
     * `onSaveInstanceState` then we call the `handleSaveInstanceState` method of our [TabManager]
     * field [mTabManager].
     *
     * @param outState [Bundle] in which to place your saved state.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mTabManager!!.handleSaveInstanceState(outState)
    }

    /**
     * This is a helper class that implements a generic mechanism for
     * associating fragments with the tabs in a tab host.  DO NOT USE THIS.
     * If you want tabs in a fragment, use the support v13 library's
     * FragmentTabHost class, which takes care of all of this for you (in
     * a simpler way even).
     */
    class TabManager(
            /**
             * [Context] to use to access resources.
             */
            private val mContext: Context,
            /**
             * Child fragment manager for placing and managing Fragments inside of our Fragment.
             */
            private val mManager: FragmentManager,
            /**
             * The resource ID for the [TabHost] widget in our parent's layout file.
             */
            private val mContainerId: Int
    ) : OnTabChangeListener {

        /**
         * The list of [TabInfo] objects for the tabs we manage.
         */
        private var mTabs = ArrayList<TabInfo>()

        /**
         * The [TabHost] widget in our parent's layout file.
         */
        private var mTabHost: TabHost? = null

        /**
         * The [TabInfo] of the previously selected tab
         */
        private var mLastTab: TabInfo? = null

        /**
         * Flag indicating whether we have initialized the fragments of our tabs state in our
         * [handleViewStateRestored] method.
         */
        private var mInitialized = false

        /**
         * The tag of the currently selected tab.
         */
        private var mCurrentTabTag: String? = null

        /**
         * Class which holds all the info we need to manage the fragments and associated tabs which
         * we manage.
         */
        internal class TabInfo(
                /**
                 * The tag name for the tab
                 */
                val tag: String,
                /**
                 * The fragment loaded when the tab is selected.
                 */
                val clss: Class<*>,
                /**
                 * Optional argument [Bundle] for the fragment.
                 */
                val args: Bundle?
        ) {
            var fragment: Fragment? = null
        }

        /**
         * Our custom [TabContentFactory]
         */
        internal class DummyTabFactory(private val mContext: Context) : TabContentFactory {
            /**
             * Callback to make the tab contents. We initialize our [View] variable `val v` with a
             * new instance, set its minimum width and height to zero and return `v` to the caller.
             *
             * @param tag Which tab was selected.
             * @return The [View] to display the contents of the selected tab.
             */
            override fun createTabContent(tag: String): View {
                val v = View(mContext)
                v.minimumWidth = 0
                v.minimumHeight = 0
                return v
            }

        }

        /**
         * Called from the `onCreateView` override of our parent [FragmentTabsFragment] fragment to
         * allow us to locate the [TabHost] widget in the [View] parameter [root] and initialize it
         * for our use. First we call the [check] method to make sure our [TabHost] field [mTabHost]
         * is currently null ([check] throws an [IllegalStateException] if it is not null). Then we
         * initialize [mTabHost] by finding the [TabHost] in our [View] parameter [root] with the ID
         * android.R.id.tabhost. We call the [TabHost.setup] method of [mTabHost] to have it get
         * ready to have tabs to it, and then set the `OnTabChangedListener` of [mTabHost] to *this*.
         * finally we return [mTabHost] to the caller.
         *
         * @param root the [View] that contains the [TabHost] widget we are to use.
         * @return the [TabHost] we are using.
         */
        fun handleCreateView(root: View): TabHost? {
            check(mTabHost == null) { "TabHost already set" }
            mTabHost = root.findViewById<View>(android.R.id.tabhost) as TabHost
            mTabHost!!.setup()
            mTabHost!!.setOnTabChangedListener(this)
            return mTabHost
        }

        /**
         * Adds a new [Fragment] and a tab to select it. First we specify a [DummyTabFactory]
         * as the [TabHost.TabContentFactory] to use to create the content of the [TabSpec]
         * parameter [tabSpec]. We initialize our [String] variable `val tag` with the `tag`
         * field of [tabSpec], and initialize our [TabInfo] variable `val info` to a new instance
         * constructed to hold `tag` as the tag name for the tab, [clss] as the fragment loaded
         * when the tab is selected, and [args] as the argument [Bundle] for the fragment. We
         * add `info` to the list of [TabInfo] objects for the tabs we manage in our field [mTabs].
         * Finally we call the `addTab` method of our [TabHost] field [mTabHost] to have it add the
         * tab described by [tabSpec] to its [TabHost] widget.
         *
         * @param tabSpec the [TabSpec] for the tab we are adding.
         * @param clss    the [Class] of the [Fragment] selected by the tab we add.
         * @param args    the optional argument [Bundle] for the [Fragment].
         */
        fun addTab(tabSpec: TabSpec, clss: Class<*>, args: Bundle?) {
            tabSpec.setContent(DummyTabFactory(mContext))
            val tag = tabSpec.tag
            val info = TabInfo(tag, clss, args)
            mTabs.add(info)
            mTabHost!!.addTab(tabSpec)
        }

        /**
         * Called from our parent fragment's `onViewStateRestored` override in order to perform
         * all the initialization required to put our UI into its proper state. If our [Bundle]
         * parameter [savedInstanceState] is not null we set our [String] field [mCurrentTabTag]
         * to the [String] stored in [savedInstanceState] under the key "tab". In any case we
         * use the `setCurrentTabByTag` method of our [TabHost] field [mTabHost] to have it set
         * the selected tag to the tab with tag [mCurrentTabTag]. We then initialize our [String]
         * variable `val currentTab` to the current tab's tag returned by the `getCurrentTabTag`
         * method of [TabHost] field [mTabHost] (kotlin prefers to call this the `currentTabTag`
         * property) (Note: `currentTab` will be null if no tab is selected).
         *
         * Now we declare our [FragmentTransaction] variable `var ft` to start our as null. We
         * then loop over `i` for all the idices in our list of [TabInfo] objects field [mTabs]
         * initializing `val tab` variable to the `i`'th [TabInfo]. We use our [FragmentManager]
         * field [mManager] to search for a fragement with the same tag as the `tag` field of `tab`
         * and saving the [Fragment] returned in the `fragment` field of `tab`. If this is null
         * and that [Fragment] is not detached we branch on whether the `tag` is equal to `currentTab`
         * and if so we set our [TabInfo] field [mLastTab] to `tab`. If the tag is not for the
         * `currentTab` we need to detach it so if `ft` is still null we use our [FragmentManager]
         * field [mManager] to set `ft` to a new [FragmentTransaction], then we add a command to
         * detach the [Fragment] of `tab` to `ft`.
         *
         * When done looping through all the tabs we set our [mInitialized] field to true, set
         * `ft` to the [FragmentTransaction] returned by our [doTabChanged] method when it changes
         * to `currentTab`.
         *
         * If after all this `ft` is not null, we schedule a commit of `ft` and call the
         * `executePendingTransactions` method of our [FragmentManager] field [mManager]
         * to have it immediately execute any pending operations.
         *
         * @param savedInstanceState the previous saved state passed to the `onViewStateRestored`
         * override of our parent fragment.
         */
        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
        fun handleViewStateRestored(savedInstanceState: Bundle?) {
            if (savedInstanceState != null) {
                mCurrentTabTag = savedInstanceState.getString("tab")
            }
            mTabHost!!.setCurrentTabByTag(mCurrentTabTag)
            val currentTab = mTabHost!!.currentTabTag

            // Go through all tabs and make sure their fragments match
            // the correct state.
            var ft: FragmentTransaction? = null
            for (i in mTabs.indices) {
                val tab = mTabs[i]
                tab.fragment = mManager.findFragmentByTag(tab.tag)
                if (tab.fragment != null && !tab.fragment!!.isDetached) {
                    if (tab.tag == currentTab) {
                        // The fragment for this tab is already there and
                        // active, and it is what we really want to have
                        // as the current tab.  Nothing to do.
                        mLastTab = tab
                    } else {
                        // This fragment was restored in the active state,
                        // but is not the current tab.  Deactivate it.
                        if (ft == null) {
                            ft = mManager.beginTransaction()
                        }
                        ft.detach(tab.fragment!!)
                    }
                }
            }

            // We are now ready to go.  Make sure we are switched to the
            // correct tab.
            mInitialized = true
            ft = doTabChanged(currentTab, ft)
            if (ft != null) {
                ft.commit()
                mManager.executePendingTransactions()
            }
        }

        /**
         * Called from the `onDestroyView` override of our parent's fragment to do what we need to
         * do when its view has been detached from the fragment. We set the our [mCurrentTabTag]
         * field to the `currentTabTag` property of our [TabHost] field [mTabHost], set [mTabHost]
         * to null, clear all entries from our list of [TabInfo] objects field [mTabs] and set our
         * [mInitialized] field to false.
         */
        fun handleDestroyView() {
            mCurrentTabTag = mTabHost!!.currentTabTag
            mTabHost = null
            mTabs.clear()
            mInitialized = false
        }

        /**
         * Called from the `onSaveInstanceState` override of our parent's fragment to do what we
         * need to do to save our current dynamic state, so it can later be reconstructed in a new
         * instance if our process is restarted. We store the currently selected tab in [Bundle]
         * parameter [outState] under the key "tab", retrieving the [String] to store from the
         * `currentTabTag` property of our [TabHost] field [mTabHost] if [mTabHost] is not null or
         * using the [String] in our [String] field [mCurrentTabTag] if [mTabHost] is null.
         *
         * @param outState [Bundle] in which to place your saved state.
         */
        fun handleSaveInstanceState(outState: Bundle) {
            outState.putString("tab", if (mTabHost != null) mTabHost!!.currentTabTag else mCurrentTabTag)
        }

        /**
         * Part of the `OnTabChangeListener` interface which we implement, it is called when the
         * user has selected a new tab. If our [mInitialized] field is false we just return having
         * done nothing. Otherwise we initialize our [FragmentTransaction] variable `val ft` with
         * the [FragmentTransaction] that our [doTabChanged] method returns which is intended to
         * switch to the [Fragment] whose tab ID is [tabId]. Then we schedule `ft` to be commited.
         *
         * @param tabId the tab id of the tab that has been selected.
         */
        override fun onTabChanged(tabId: String) {
            if (!mInitialized) {
                return
            }
            val ft = doTabChanged(tabId, null)
            ft?.commit()
        }

        /**
         * Called to construct a [FragmentTransaction] which will switch to the tab with the tab ID
         * [tabId] and its associated [Fragment]. We initialize our [FragmentTransaction] variable
         * `var ftVar` to our parameter [ft], and initialize our [TabInfo] variable `var newTab`
         * to null. Then we loop over `i` for all of the [TabInfo] in our list field [mTabs] setting
         * our `val tab` variable to the `i`'th entry in [mTabs] and if the `tag` property of `tab`
         * is equal to [tabId] we set `newTab` to `tab`. When done searching [mTabs] for [tabId] we
         * check to make sure `newTab` is not null throwing an [IllegalStateException] complaining
         * "No tab known for tag [tabId]".
         *
         * Then we check if `newTab` is equal to our [mLastTab] field and do nothing more if it is.
         * Otherwise we check whether `ftVar` is null and initialize it by using our [FragmentManager]
         * field [mManager] to being a new [FragmentTransaction]. Then is our [mLastTab] field is
         * not null, and the `fragment` property of [mLastTab] is not null we instruct `ftVar` to
         * detach that fragment from the UI.
         *
         * We now branch on whether the `fragment` property of `newTab` is null:
         *
         *  - `fragment` property is null: we set it to the [Fragment] returned by the `instantiate`
         *  method when asked to instantiate a fragment with the class given by the `name` property
         *  of the `clss` property of `newTab` using the `args` property of `newTab` as the fragment
         *  argument [Bundle], and then use `ftVar` to add that fragment into the view with the
         *  ID of our [mContainerId] field using the `tag` property of `newTab` as the tag name.
         *
         *  - `fragment` property is not null: we use `ftVar` to re-attach the [Fragment] given by
         *  the `fragment` property of `newTab`.
         *
         * Finally we return `ftVar` to the caller.
         *
         * @param tabId tab ID of the tab and associated [Fragment] we want to switch to.
         * @param ft    if non-null this is a [FragmentTransaction] we should append commands to.
         *
         * @return a [FragmentTransaction] which includes the commands necessary to switch tabs.
         */
        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
        private fun doTabChanged(tabId: String?, ft: FragmentTransaction?): FragmentTransaction? {
            var ftVar = ft
            var newTab: TabInfo? = null
            for (i in mTabs.indices) {
                val tab = mTabs[i]
                if (tab.tag == tabId) {
                    newTab = tab
                }
            }
            checkNotNull(newTab) { "No tab known for tag $tabId" }
            if (mLastTab != newTab) {
                if (ftVar == null) {
                    ftVar = mManager.beginTransaction()
                }
                if (mLastTab != null) {
                    if (mLastTab!!.fragment != null) {
                        ftVar.detach(mLastTab!!.fragment!!)
                    }
                }
                if (newTab.fragment == null) {
                    @Suppress("DEPRECATION")
                    newTab.fragment = instantiate(
                            mContext,
                            newTab.clss.name,
                            newTab.args
                    )
                    ftVar.add(mContainerId, newTab.fragment!!, newTab.tag)
                } else {
                    ftVar.attach(newTab.fragment!!)
                }
                mLastTab = newTab
            }
            return ftVar
        }

    }
}
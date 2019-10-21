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
@file:Suppress("DEPRECATION")

package com.example.android.apis.app

import android.annotation.SuppressLint
import android.annotation.TargetApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBar.Tab
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * This demonstrates the use of action bar tabs and how they interact
 * with other action bar features.
 * You need to toggle tab mode before tabs show in action bar. Crashes when rotated
 * because it needs a zero argument constructor. The text should be passed in a bundle.
 * Working on doing this Right. Did so, but need to use retained fragments in order
 * to keep tabs (TODO: add retained fragments to this)
 */
@Suppress("MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class ActionBarTabs : AppCompatActivity() {

    var tabsMode = false
    /**
     * Called when the activity is starting or restarting after being killed.
     * First we call through to our super's implementation of onCreate. Next
     * we set our content view to our layout file R.layout.action_bar_tabs.
     * Then we check whether savedInstanceState is not null, and if so restore
     * tabsMode to the value it had when onSaveInstanceState was called.
     * TODO: Restore contents of tabs, number of tabs and which is selected.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down (as happens when rotated) then this Bundle
     * contains the data it most recently supplied in onSaveInstanceState.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.action_bar_tabs)
        if (savedInstanceState != null) {
            tabsMode = savedInstanceState.getBoolean("tabsMode")
        }
        if (tabsMode) {

            supportActionBar!!.navigationMode = ActionBar.NAVIGATION_MODE_TABS
            supportActionBar!!.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE)
        } else {

            supportActionBar!!.navigationMode = ActionBar.NAVIGATION_MODE_STANDARD
            supportActionBar!!.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_TITLE)
        }
    }

    /**
     * Called to retrieve per-instance state from an activity before being killed
     * so that the state can be restored in [onCreate] or [onRestoreInstanceState]
     * (the [Bundle] populated by this method will be passed to both).
     *
     * We just store the value of the Boolean tabsMode under the key "tabsMode", then
     * call through to our super's implementation of onSaveInstanceState.
     *
     * @param outState Bundle in which to place your saved state.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("tabsMode", tabsMode)
        super.onSaveInstanceState(outState)
    }

    /**
     * This is specified as the onClickListener for the R.id.btn_add_tab Button ("ADD NEW TAB")
     * Button using android:onClick="onAddTab". First we set ActionBar bar to a reference to our
     * activity's ActionBar. Then we set int tabCount to the number of tabs currently registered
     * with the action bar. We create String text by appending tabCount to the String "Tab ". We
     * create an instance of TabContentFragment fragment, addTab this to "bar", setText of the tab
     * to "text", set the TabListener of the tab to the TabContentFragment fragment just addTab'ed,
     * and finally set the text in the TabContentFragment fragment to "text".
     *
     * @param v Button "ADD NEW TAB" which was clicked
     */
    @Suppress("UNUSED_PARAMETER")
    fun onAddTab(v: View) {
        val bar = supportActionBar

        val tabCount = bar!!.tabCount
        val text = "Tab $tabCount"
        val fragment = TabContentFragment()
        bar.addTab(bar.newTab()
                .setText(text)
                .setTabListener(TabListener(fragment)))
        fragment.putText(text)
    }

    /**
     * This is specified as the onClickListener for the R.id.btn_remove_tab ("REMOVE LAST TAB")
     * Button using android:onClick="onRemoveTab". First we set ActionBar bar to a reference to
     * our activity's ActionBar. Then if the number of tabs currently registered with the action
     * bar is greater than 0 we removeTabAt the last tab position.
     *
     * @param v Button "REMOVE LAST TAB" which was clicked
     */
    @Suppress("UNUSED_PARAMETER")
    fun onRemoveTab(v: View) {
        val bar = supportActionBar

        if (bar!!.tabCount > 0) {
            bar.removeTabAt(bar.tabCount - 1)
        }
    }

    /**
     * This is specified as the onClickListener for the R.id.btn_toggle_tabs ("TOGGLE TAB MODE")
     * Button using android:onClick="onToggleTabs". First we set ActionBar bar to a reference to
     * our activity's ActionBar. Then if bar.getNavigationMode says that the ActionBar is in tabs
     * mode, we toggle it by first setting our field tabsMode to false, then setNavigationMode to
     * NAVIGATION_MODE_STANDARD, and we set setDisplayOptions to DISPLAY_SHOW_TITLE. If it is not
     * in tabs mode we set our field tabsMode to true, setNavigationMode to NAVIGATION_MODE_TABS,
     * and setDisplayOptions is used to clear DISPLAY_SHOW_TITLE.
     *
     * @param v Button "TOGGLE TAB MODE" which was clicked
     */
    @Suppress("UNUSED_PARAMETER")
    fun onToggleTabs(v: View) {
        val bar = supportActionBar


        if (bar!!.navigationMode == ActionBar.NAVIGATION_MODE_TABS) {
            tabsMode = false
            bar.navigationMode = ActionBar.NAVIGATION_MODE_STANDARD
            bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_TITLE)
        } else {
            tabsMode = true
            bar.navigationMode = ActionBar.NAVIGATION_MODE_TABS
            bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE)
        }
    }

    /**
     * This is specified as the onClickListener for the R.id.btn_remove_all_tabs ("REMOVE ALL TABS")
     * Button using android:onClick="onRemoveAllTabs". We simply retrieve a reference to this
     * activity's ActionBar and use it to invoke the method ActionBar,removeAllTabs()
     *
     * @param v Button "REMOVE ALL TABS" which was clicked
     */
    @Suppress("UNUSED_PARAMETER")
    fun onRemoveAllTabs(v: View) {

        supportActionBar!!.removeAllTabs()
    }

    /**
     * A TabListener receives event callbacks from the action bar as tabs
     * are deselected, selected, and reselected. A FragmentTransaction
     * is provided to each of these callbacks; if any operations are added
     * to it, it will be committed at the end of the full tab switch operation.
     * This lets tab switches be atomic without the app needing to track
     * the interactions between different tabs.
     *
     * NOTE: This is a very simple implementation that does not retain
     * fragment state of the non-visible tabs across activity instances.
     * Look at the FragmentTabs example for how to do a more complete
     * implementation.
     */
    private inner class TabListener
    /**
     * Initializes our TabContentFragment mFragment which we use when FragmentTransaction.add'ing
     * and FragmentTransaction.remove'ing when our tab is selected and unselected.
     *
     * @param mFragment TabContentFragment which this tab will contain
     */
    (private val mFragment: TabContentFragment) : ActionBar.TabListener {

        /**
         * Our tab has been selected, so FragmentTransaction.add our TabContentFragment mFragment
         * so it will be displayed.
         *
         * @param tab The tab that was selected
         * @param ft A [FragmentTransaction] for queuing fragment operations to execute
         * during a tab switch. The previous tab's unselected and this tab's selected will be
         * executed in a single transaction. This FragmentTransaction does not support
         * being added to the back stack.
         */
        override fun onTabSelected(tab: Tab?, ft: FragmentTransaction?) {
            ft!!.add(R.id.fragment_content, mFragment as Fragment, mFragment.text)
        }

        /**
         * Our tab has been unselected, so FragmentTransaction.remove our TabContentFragment mFragment
         *
         * @param tab The tab that was unselected
         * @param ft A [FragmentTransaction] for queuing fragment operations to execute
         * during a tab switch. This tab's unselected and the newly selected tab's select
         * will be executed in a single transaction. This FragmentTransaction does not
         * support being added to the back stack.
         */
        override fun onTabUnselected(tab: Tab?, ft: FragmentTransaction?) {
            ft!!.remove(mFragment as Fragment)
        }

        /**
         * Our tab has been reselected, we simply toast a message stating this.
         *
         * @param tab The tab that was reselected.
         * @param ft A [FragmentTransaction] for queuing fragment operations to execute
         * once this method returns. This FragmentTransaction does not support
         * being added to the back stack.
         */
        override fun onTabReselected(tab: Tab?, ft: FragmentTransaction?) {
            Toast.makeText(this@ActionBarTabs, "Reselected!", Toast.LENGTH_SHORT).show()
        }

    }

    /**
     * The Fragment we add to a tab
     */
    @SuppressLint("ValidFragment")
    class TabContentFragment : Fragment() {

        /**
         * Get function for our text content.
         *
         * @return Text this TabContentFragment is displaying
         */
        var text: String? = "new tab"
            private set
        private var textView: TextView? = null

        /**
         * Sets the text we display.
         *
         * @param text String to set our text content to
         */
        fun putText(text: String) {
            this.text = text
        }

        /**
         * Called to have the fragment instantiate its user interface view. First we use the
         * parameter LayoutInflater inflater to inflate into View fragView our layout file
         * R.layout.action_bar_tab_content. Then we determine if this fragment is being re-constructed
         * from a previous saved state by checking if our parameter Bundle savedInstanceState is not
         * null and if so we retrieve from savedInstanceState the text we stored under the key "mText"
         * when our implementation of onSaveInstanceState was called. Next we find the TextView in
         * our layout file which we use to display mText (R.id.text) and set the text to the contents
         * of our field mText. Finally we return the View fragView which contains our UI.
         *
         * @param inflater The LayoutInflater object that can be used to inflate
         * any views in the fragment,
         * @param container If non-null, this is the parent view that the fragment's
         * UI should be attached to.  The fragment should not add the view itself,
         * but this can be used to generate the LayoutParams of the view.
         * @param savedInstanceState If non-null, this fragment is being re-constructed
         * from a previous saved state as given here.
         *
         * @return Return the View for the fragment's UI, or null.
         */
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val fragView = inflater.inflate(R.layout.action_bar_tab_content, container, false)

            if (savedInstanceState != null) {
                text = savedInstanceState.getString("mText")
            }
            textView = fragView.findViewById(R.id.text)
            textView!!.text = text

            return fragView
        }

        /**
         * Called to ask the fragment to save its current dynamic state, so it can later be
         * reconstructed when a new instance of its process is restarted. Our only state consists
         * of our field String mText which we store in the parameter Bundle outState under the key
         * "mText". Then we call through to our super's implementation of onSaveInstanceState.
         *
         * @param outState Bundle in which to place your saved state.
         */
        override fun onSaveInstanceState(outState: Bundle) {
            outState.putString("mText", text)
            super.onSaveInstanceState(outState)
        }
    }
}

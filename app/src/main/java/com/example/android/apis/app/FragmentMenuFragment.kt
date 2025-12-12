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
@file:Suppress("unused", "ReplaceNotNullAssertionWithElvisReturn")

package com.example.android.apis.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import com.example.android.apis.R
import com.example.android.apis.app.FragmentMenu.Menu2Fragment
import com.example.android.apis.app.FragmentMenu.MenuFragment

/**
 * Demonstrates how fragments can participate in the options menu.
 * It is used by FragmentNestingTabs.
 * RequiresApi(Build.VERSION_CODES.HONEYCOMB)
 */
@Suppress("MemberVisibilityCanBePrivate", "UNUSED_ANONYMOUS_PARAMETER")
class FragmentMenuFragment : Fragment() {
    /**
     * Our [MenuFragment] instance.
     */
    var mFragment1: Fragment? = null

    /**
     * Our [Menu2Fragment] instance.
     */
    var mFragment2: Fragment? = null

    /**
     * The [CheckBox] in our layout with id R.id.menu1 it toggles the show/hide status of [mFragment1]
     */
    var mCheckBox1: CheckBox? = null

    /**
     * The [CheckBox] in our layout with id R.id.menu2  it toggles the show/hide status of [mFragment2]
     */
    var mCheckBox2: CheckBox? = null

    /**
     * Update fragment visibility when check boxes [mCheckBox1] and [mCheckBox2] are changed by
     * calling our method [updateFragmentVisibility]
     */
    val mClickListener: View.OnClickListener =
        View.OnClickListener { v: View? -> updateFragmentVisibility() }

    /**
     * Called to have the fragment instantiate its user interface view. We use our [LayoutInflater]
     * parameter [inflater] to inflate our layout file R.layout.fragment_menu into our [View]
     * variable `val v` using our [ViewGroup] parameter [container] for the `LayoutParams` without
     * attaching to it. We initialize our variable `val fm` with a private `FragmentManager` for
     * placing and managing Fragments inside of this `Fragment`, and initialize our variable `val ft`
     * by using `fm` to start a `FragmentTransaction`. We initialize our [Fragment] field [mFragment1]
     * by using `fm` to find the [Fragment] with the tag "f1" and if [mFragment1] is null we set it
     * to a new instance of [MenuFragment] and use `ft` to add [mFragment1] to the activity state
     * under the tag "f1". We initialize our [Fragment] field [mFragment2] by using `fm` to find the
     * [Fragment] with the tag "f2" and if [mFragment2] is null we set it to a new instance of
     * [Menu2Fragment] and use `ft` to add [mFragment2] to the activity state under the tag "f2".
     * We then schedule a commit of the `FragmentTransaction` we composed in `ft`.
     *
     * We initialize our [CheckBox] field [mCheckBox1] by finding the view in our layout with the
     * id R.id.menu1 and set its `OnClickListener` to our field [mClickListener], and initialize our
     * [CheckBox] field [mCheckBox2] by finding the view in our layout with the id R.id.menu2 and
     * also set its `OnClickListener` to our field [mClickListener]. We then call our method
     * [updateFragmentVisibility] to make sure fragments start out with correct visibility.
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
     * RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_menu, container, false)

        // Make sure the two menu fragments are created.
        val fm = childFragmentManager
        val ft = fm.beginTransaction()
        mFragment1 = fm.findFragmentByTag("f1")
        if (mFragment1 == null) {
            mFragment1 = MenuFragment()
            ft.add(mFragment1!!, "f1")
        }
        mFragment2 = fm.findFragmentByTag("f2")
        if (mFragment2 == null) {
            mFragment2 = Menu2Fragment()
            ft.add(mFragment2!!, "f2")
        }
        ft.commit()

        // Watch check box clicks.
        mCheckBox1 = v.findViewById(R.id.menu1)
        mCheckBox1!!.setOnClickListener(mClickListener)
        mCheckBox2 = v.findViewById(R.id.menu2)
        mCheckBox2!!.setOnClickListener(mClickListener)

        // Make sure fragments start out with correct visibility.
        updateFragmentVisibility()
        return v
    }

    /**
     * Called when all saved state has been restored into the view hierarchy of the fragment. We
     * call our super's implementation of `onViewStateRestored` then call our method
     * [updateFragmentVisibility] to update fragment visibility based on current check box state.
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        // Make sure fragments are updated after check box view state is restored.
        updateFragmentVisibility()
    }

    /**
     * Update fragment visibility based on current check box state. We initialize our variable
     * `val ft` by using a private `FragmentManager` for placing and managing Fragments inside
     * of this `Fragment` to start a `FragmentTransaction`. If our [CheckBox] field [mCheckBox1]
     * is checked we use `ft` to show our [Fragment] field [mFragment1], and if unchecked we use
     * `ft` to hide it. If our [CheckBox] field [mCheckBox2] is checked we use `ft` to show our
     * [Fragment] field [mFragment2], and if unchecked we use `ft` to hide it. We then schedule a
     * commit of the `FragmentTransaction` we composed in `ft`.
     * RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
     */
    fun updateFragmentVisibility() {
        val ft = childFragmentManager.beginTransaction()
        if (mCheckBox1!!.isChecked) ft.show(mFragment1!!) else ft.hide(mFragment1!!)
        if (mCheckBox2!!.isChecked) ft.show(mFragment2!!) else ft.hide(mFragment2!!)
        ft.commit()
    }
}
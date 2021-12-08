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

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.android.apis.R
import com.example.android.apis.app.FragmentStack.CountingFragment.Companion.newInstance

@Suppress("MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
/**
 * Used by the FragmentNestingTabs demo. It uses the CountingFragment code from the FragmentStack
 * demo to create a stack of `TextView`s displaying the fragment's depth in the stack.
 */
class FragmentStackFragment : Fragment() {
    /**
     * stack level of next `CountingFragment` to add to back stack
     */
    var mStackLevel = 1

    /**
     * Called to do initial creation of our fragment. First we call our super's implementation of
     * `onCreate`. Then if our [Bundle] argument [savedInstanceState] is null this is the first time
     * we are being called so we need to do first time initialization which consists of adding the
     * our initial `CountingFragment`, so we initialize our [Fragment] variable `val newFragment`
     * with a new instance of `CountingFragment`, initialize our [FragmentTransaction] variable
     * `val ft` by using a private `FragmentManager` for placing and managing Fragments inside of
     * this `Fragment` to begin a [FragmentTransaction] which we then use to add `newFragment` to
     * the activity state using the `FrameLayout` with ID R.id.simple_fragment in our layout for its
     * UI, the return from the `add` command is chained to a `commit` command to commit the
     * transaction. If [savedInstanceState] is not null however we are being recreated, so we set
     * our [mStackLevel] field to the [Int] stored in [savedInstanceState] under the key "level".
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            // Do first time initialization -- add initial fragment.
            val newFragment: Fragment = newInstance(mStackLevel)
            val ft = childFragmentManager.beginTransaction()
            ft.add(R.id.simple_fragment, newFragment).commit()
        } else {
            mStackLevel = savedInstanceState.getInt("level")
        }
    }

    /**
     * Called to have our fragment instantiate its user interface view. We use our [LayoutInflater]
     * parameter [inflater] to inflate our ID R.layout.fragment_stack layout file into our [View]
     * variable `val v` with our [ViewGroup] parameter [container] supplying the LayoutParams
     * without attaching `v` to it. We initialize our [Button] variable `var button` by finding
     * the view in `v` with ID R.id.new_fragment ("PUSH") and set its `OnClickListener` to a lambda
     * which calls our [addFragmentToStack] method to push a new fragment onto the stack, then set
     * `button` by finding the view with id R.id.delete_fragment ("POP") and set its `OnClickListener`
     * to a lambda which calls the `popBackStack` method of our private `FragmentManager` for placing
     * and managing Fragments inside of this `Fragment` in order to Pop the top state off the back
     * stack. Finally we return `v` to the caller.
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
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_stack, container, false)

        // Watch for button clicks.
        var button = v.findViewById<Button>(R.id.new_fragment)
        button.setOnClickListener { addFragmentToStack() }
        button = v.findViewById(R.id.delete_fragment)
        button.setOnClickListener { childFragmentManager.popBackStack() }
        return v
    }

    /**
     * Called to ask the fragment to save its current dynamic state, so it can later be reconstructed
     * in a new instance if its process is restarted. First we call our super's implementation of
     * `onSaveInstanceState` then we store our [Int] field [mStackLevel] in our [Bundle] parameter
     * [outState] under the key "level".
     *
     * @param outState [Bundle] in which to place your saved state.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("level", mStackLevel)
    }

    /**
     * Adds a new instance of `CountingFragment` to the back stack. First we increment our [Int]
     * field [mStackLevel]. Then we initialize our [Fragment] variable `val newFragment` with a
     * new instance of `CountingFragment` constructed to display the number [mStackLevel]. We
     * initialize our [FragmentTransaction] variable `val ft` by using a private `FragmentManager`
     * for placing and managing Fragments inside of this `Fragment` to begin a [FragmentTransaction].
     * We then use `ft` to replace the fragment using the `FrameLayout` with ID R.id.simple_fragment
     * in our layout for its UI. We set the transition animation for this transaction to be a
     * [FragmentTransaction.TRANSIT_FRAGMENT_OPEN] (the [Fragment] is being added onto the stack),
     * and have `ft` add the transaction to the back stack with null as the name. Finally we call
     * the `commit` method of `ft` to schedule a commit of the transaction.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun addFragmentToStack() {
        mStackLevel++

        // Instantiate a new fragment.
        val newFragment: Fragment = newInstance(mStackLevel)

        // Add the fragment to the activity, pushing this transaction
        // on to the back stack.
        val ft = childFragmentManager.beginTransaction()
        ft.replace(R.id.simple_fragment, newFragment)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        ft.addToBackStack(null)
        ft.commit()
    }
}
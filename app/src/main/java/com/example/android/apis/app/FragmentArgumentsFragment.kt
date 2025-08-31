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
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.android.apis.R

/**
 * Demonstrates a fragment that can be configured through both Bundle arguments
 * and layout attributes. Currently only used in FragmentNestingTabs.kt
 */
@SuppressLint("ObsoleteSdkInt")
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
class FragmentArgumentsFragment : Fragment() {
    /**
     * Called to do initial creation of a fragment. This is called after [onAttach] and before
     * [onCreateView]. Note that this can be called while the fragment's activity is still in the
     * process of being created. As such, you can not rely on things like the activity's content
     * view hierarchy being initialized at this point. If you want to do work once the activity
     * itself is created, see [onActivityCreated]. Any restored child fragments will be created
     * before the base [Fragment.onCreate] method returns.
     *
     * First we call our super's implementation of `onCreate`, then if our [Bundle] parameter
     * [savedInstanceState] is *null* this is the first-time init, so we need to create our child
     * fragment to embed in the activity. To do this we fetch a private `FragmentManager` for
     * placing and managing `Fragments` inside of this Fragment by calling the method
     * `getChildFragmentManager` and use it to begin a `FragmentTransaction` in order to initialize
     * our `FragmentTransaction` variable `val ft`. We then initialize our [Fragment] variable
     * `var newFragment` with a new instance of [FragmentArguments.MyFragment] whose label is
     * "From Arguments 1", and use `ft` to add it to the container with the id R.id.created1.
     * We then set `newFragment` to a new instance of [FragmentArguments.MyFragment] whose label is
     * "From Arguments 2", and use `ft` to add it to the container with the id R.id.created2.
     * Finally we commit the `FragmentTransaction`.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     * this is the state. We do no override [onSaveInstanceState] but use the fact that it is
     * non-null to indicated we have been restarted and do not need to recreate our child fragment.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            // First-time init; create fragment to embed in activity.
            val ft = childFragmentManager.beginTransaction()
            var newFragment: Fragment = FragmentArguments.MyFragment.newInstance("From Arguments 1")
            ft.add(R.id.created1, newFragment)
            newFragment = FragmentArguments.MyFragment.newInstance("From Arguments 2")
            ft.add(R.id.created2, newFragment)
            ft.commit()
        }
    }

    /**
     * Called to have the fragment instantiate its user interface view. This will be called between
     * [onCreate] and [onActivityCreated]. A default [View] can be returned by calling [Fragment] in
     * your constructor. We just return the [View] that our [LayoutInflater] parameter [inflater]
     * inflates from our layout file R.layout.fragment_arguments_fragment using our [ViewGroup]
     * parameter [container] for the layout parameters without attaching to it.
     *
     * @param inflater The [LayoutInflater] object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to. The fragment should not add the view itself,
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
        return inflater.inflate(
            R.layout.fragment_arguments_fragment,
            container,
            false
        )
    }

}

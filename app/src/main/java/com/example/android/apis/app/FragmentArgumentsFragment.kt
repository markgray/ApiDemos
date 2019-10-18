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
import androidx.fragment.app.Fragment
import com.example.android.apis.R

/**
 * Demonstrates a fragment that can be configured through both Bundle arguments
 * and layout attributes.
 */
@Suppress("unused") // It really is used!
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class FragmentArgumentsFragment : Fragment() {
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_arguments_fragment, container, false)
    }

}

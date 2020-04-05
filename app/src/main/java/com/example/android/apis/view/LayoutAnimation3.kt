/*
 * Copyright (C) 2007 The Android Open Source Project
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
package com.example.android.apis.view

import android.app.ListActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.example.android.apis.R

/**
 * Same as List Cascade above, but an android:layoutAnimation attribute in the layout file
 * R.layout.layout_animation_3 uses anim/layout_bottom_to_top_slide.xml which uses anim/slide_right
 * instead of implementing the animation in code
 */
class LayoutAnimation3 : ListActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.layout_animation_3.
     * Then we set our list adapter to a new instance of `ArrayAdapter` which is constructed
     * using our array `String[] mStrings` as the data, and android.R.layout.simple_list_item_1
     * as the layout file containing a TextView to use when instantiating views.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_animation_3)
        listAdapter = ArrayAdapter(this,
                android.R.layout.simple_list_item_1, mStrings)
    }

    /**
     * The data that our `ArrayAdapter` uses to fill our `ListView`.
     */
    private val mStrings = arrayOf(
            "Bordeaux",
            "Lyon",
            "Marseille",
            "Nancy",
            "Paris",
            "Toulouse",
            "Strasbourg"
    )
}
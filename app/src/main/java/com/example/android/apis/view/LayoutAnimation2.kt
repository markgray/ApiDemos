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
import android.view.animation.*
import android.widget.ArrayAdapter

/**
 * Shows how to use a LayoutAnimationController to animate the layout of a list in a ListActivity
 * Too fast on Nexus 6 to see effect
 */
class LayoutAnimation2 : ListActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. The we set our list adapter to a new instance of `ArrayAdapter` which
     * is constructed using our array `String[] mStrings` as the data, and the system layout
     * android.R.layout.simple_list_item_1 as the layout file containing a TextView to use when
     * instantiating views.
     *
     *
     * We create a new instance for `AnimationSet set` with the value true as the parameter so
     * that all of the animations in the set should use the interpolator associated with `set`.
     * We create `Animation animation` as an `AlphaAnimation` that goes from 0.0 to 1.0f,
     * set its duration to 50 milliseconds, and add it to `set`. We now set `animation`
     * to a new instance of `TranslateAnimation` with a from X value of 0.0 RELATIVE_TO_SELF,
     * a to X value of 1.0f RELATIVE_TO_SELF, a from Y value of -1.0 RELATIVE_TO_SELF, and a to Y
     * value of 0.0 RELATIVE_TO_SELF, set its duration to 100 milliseconds and add it to `set`.
     *
     *
     * We create `LayoutAnimationController controller` to use `set` with a delay of 0.5.
     * Finally we fetch our `ListView` to `ListView listView` and set ist layout animation
     * controller to `controller`.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mStrings)
        val set = AnimationSet(true)
        var animation: Animation = AlphaAnimation(0.0f, 1.0f)
        animation.duration = 50
        set.addAnimation(animation)
        animation = TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f
        )
        animation.setDuration(100)
        set.addAnimation(animation)
        val controller = LayoutAnimationController(set, 0.5f)
        val listView = listView
        listView.layoutAnimation = controller
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
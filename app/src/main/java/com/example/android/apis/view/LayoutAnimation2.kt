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

import android.os.Bundle
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.LayoutAnimationController
import android.view.animation.TranslateAnimation
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Shows how to use a [LayoutAnimationController] to animate the layout of a list in a [ListView]
 * Too fast on Nexus 6 to see effect
 */
class LayoutAnimation2 : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file `R.layout.layout_animation_2` and
     * initialize our [ListView] variable `val list` by finding the view with ID `R.id.list`.
     *
     * Next we set the list adapter of `list` to a new instance of [ArrayAdapter] which is
     * constructed using our `String[]` array [mStrings] as the data, and the system layout
     * android.R.layout.simple_list_item_1 as the layout file containing a `TextView` to use when
     * instantiating views.
     *
     * We create a new instance for [AnimationSet] variable `val set` with the value true as the
     * parameter so that all of the animations in the set should use the interpolator associated
     * with `set`. We create [Animation] variable `val animation` as an [AlphaAnimation] that goes
     * from 0.0 to 1.0f, set its duration to 50 milliseconds, and add it to `set`. We now set
     * `animation` to a new instance of [TranslateAnimation] with a "from X" value of 0.0
     * RELATIVE_TO_SELF, a "to X" value of 1.0f RELATIVE_TO_SELF, a "from Y" value of -1.0
     * RELATIVE_TO_SELF, and a "to Y" value of 0.0 RELATIVE_TO_SELF, set its duration to 100
     * milliseconds and add it to `set`.
     *
     * We create [LayoutAnimationController] variable `val controller` to use `set` with a delay
     * of 0.5. Finally we set the layout animation controller of our [ListView] variable `list`
     * to `controller`.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_animation_2)
        val list = findViewById<ListView>(R.id.list)
        list.adapter = ArrayAdapter(
            /* context = */ this,
            /* resource = */ android.R.layout.simple_list_item_1,
            /* objects = */ mStrings
        )
        val set = AnimationSet(/* shareInterpolator = */ true)
        var animation: Animation = AlphaAnimation(/* fromAlpha = */ 0.0f, /* toAlpha = */ 1.0f)
        animation.duration = 50
        set.addAnimation(/* a = */ animation)
        animation = TranslateAnimation(
            /* fromXType = */ Animation.RELATIVE_TO_SELF,
            /* fromXValue = */ 0.0f,
            /* toXType = */ Animation.RELATIVE_TO_SELF,
            /* toXValue = */ 0.0f,
            /* fromYType = */ Animation.RELATIVE_TO_SELF,
            /* fromYValue = */ -1.0f,
            /* toYType = */ Animation.RELATIVE_TO_SELF,
            /* toYValue = */ 0.0f
        )
        animation.setDuration(/* durationMillis = */ 100)
        set.addAnimation(/* a = */ animation)
        val controller = LayoutAnimationController(/* animation = */ set, /* delay = */ 0.5f)
        list.layoutAnimation = controller
    }

    /**
     * The data that our [ArrayAdapter] uses to fill our `ListView`.
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

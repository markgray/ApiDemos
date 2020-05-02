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
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity

import com.example.android.apis.R

/**
 * Shows how to use the four different animations available for a [ViewFlipper]: "Push up", "Push
 * left", "Cross fade", and "Hyperspace". A [ViewFlipper] is a simple `ViewAnimator` that will
 * animate between two or more views that have been added to it. Only one child is shown at a time.
 * If requested, it can automatically flip between each child at a regular interval.
 */
class Animation2 : AppCompatActivity(), OnItemSelectedListener {
    /**
     * The `ViewFlipper` in our layout with ID R.id.flipper
     */
    private var mFlipper: ViewFlipper? = null

    /**
     * Strings used for the `Adapter` used by the [Spinner] with ID R.id.spinner
     */
    private val mStrings = arrayOf(
            "Push up", "Push left", "Cross fade", "Hyperspace"
    )

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.animation_2. We
     * initialize our [ViewFlipper] field [mFlipper] by locating the [ViewFlipper] with ID
     * R.id.flipper in our layout file, and instruct it to "start flipping" (starts a timer for
     * it to cycle through its child views).
     *
     * Next we set [Spinner] `val s` by locating the view with ID R.id.spinner, and we create
     * `ArrayAdapter<String>` `val adapter` from our array of [String]'s field [mStrings] using the
     * android.R.layout.simple_spinner_item as the resource ID for a layout file containing a
     * `TextView` to use when instantiating views, and set android.R.layout.simple_spinner_dropdown_item
     * as the layout resource defining the drop down views.
     *
     * We then set `adapter` as the `Adapter` for [Spinner] `s`, and set "this" as the
     * [OnItemSelectedListener] for `s`.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.animation_2)
        mFlipper = findViewById(R.id.flipper)
        mFlipper!!.startFlipping()
        val s = findViewById<Spinner>(R.id.spinner)
        val adapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, mStrings)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        s.adapter = adapter
        s.onItemSelectedListener = this
    }

    /**
     * Callback method to be invoked when an item in the [Spinner] with ID R.id.spinner has been
     * selected. We switch base on the value of our parameter [position]:
     *
     *  * 0 - "Push up" we set the "in" animation of [ViewFlipper] field [mFlipper] to the [Animation]
     *  loaded from R.anim.push_left_in, and its "out" animation to one loaded from R.anim.push_left_out
     *
     *  * 1 - "Push left" we set the "in" animation of [ViewFlipper] field [mFlipper] to the [Animation]
     *  loaded from R.anim.push_up_in, and its "out"" animation to one loaded from R.anim.push_up_out
     *
     *  * 2 - "Cross fade" we set the "in"" animation of [ViewFlipper] field [mFlipper] to the [Animation]
     *  loaded from android.R.anim.fade_in, and its "out" animation to one loaded from
     *  android.R.anim.fade_out
     *
     *  * default - "Hyperspace" we set the "in" animation of [ViewFlipper] field [mFlipper] to the
     *  [Animation] loaded from R.anim.hyperspace_in, and its "out" animation to one loaded from
     *  R.anim.hyperspace_out
     *
     *
     * The eight different animation resource files contain XML elements for performing the animations:
     *
     *  * R.anim.push_up_in - contains a `<translate>` for y from 100p to 0p with a duration
     *  of 300 milliseconds, and an `<alpha>` from 0.0 to 1.0 also with a duration of 300
     *  milliseconds.
     *
     *  * R.anim.push_up_out - contains a `<translate>` for y from 0p to -100p with a duration
     *  of 300 milliseconds, and an `<alpha>` from 1.0 to 0.0 also with a duration of 300
     *  milliseconds.
     *
     *  * R.anim.push_left_in - contains a `<translate>` for x from 100p to 0p with a duration
     *  of 300 milliseconds, and an `<alpha>` from 0.0 to 1.0 also with a duration of 300
     *  milliseconds.
     *
     *  * R.anim.push_left_out - contains a `<translate>` for x from 0p to -100p with a duration
     *  of 300 milliseconds, and an `<alpha>` from 1.0 to 0.0 also with a duration of 300
     *  milliseconds.
     *
     *  * android.R.anim.fade_in - contains an `<alpha>` from 0.0 to 1.0 using an interpolator
     *  of android:interpolator="@interpolator/decelerate_quad", and a duration of config_longAnimTime
     *  (500 milliseconds).
     *
     *  * android.R.anim.fade_out - contains an `<alpha>` from 1.0 to 0.0 using an interpolator
     *  of android:interpolator="@interpolator/accelerate_quad", and a duration of config_mediumAnimTime
     *  (400 milliseconds).
     *
     *  * R.anim.hyperspace_in - contains an `<alpha>` from 0.0 to 1.0, with a duration of 300
     *  milliseconds and an start offset of 1200 milliseconds
     *
     *  * R.anim.hyperspace_out - contains a `<scale>` that scales x from 1.0 to 1.4, y from
     *  1.0 to 0.6, android:pivotX="50%", android:pivotY="50%", android:fillEnabled="true",
     *  android:fillAfter="false" and a duration of 700 milliseconds. This is followed by a
     *  `<set>` which contains a `<scale>` and a `<rotate>`, both with a start
     *  offset of 700 milliseconds. The `<set>` uses accelerate_interpolator as its interpolator,
     *  the `<scale>` scales x from 1.4 to 0.0, y from 0.6 to 0, android:pivotX="50%",
     *  android:pivotY="50%", android:fillEnabled="true", android:fillBefore="false",
     *  android:fillAfter="true" and a duration of 400 milliseconds, the `<rotate>` rotates
     *  from 0 degrees to -45 degrees, with android:toYScale="0.0", android:pivotX="50%",
     *  android:pivotY="50%", android:fillEnabled="true", android:fillBefore="false",
     *  android:fillAfter="true" and a duration of 400 milliseconds.
     *
     * @param parent   The [AdapterView] where the selection happened
     * @param v        The [View] within the AdapterView that was clicked
     * @param position The position of the view in the adapter
     * @param id       The row id of the item that is selected
     */
    override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
        when (position) {
            0 -> {
                mFlipper!!.inAnimation = AnimationUtils.loadAnimation(this, R.anim.push_up_in)
                mFlipper!!.outAnimation = AnimationUtils.loadAnimation(this, R.anim.push_up_out)
            }
            1 -> {
                mFlipper!!.inAnimation = AnimationUtils.loadAnimation(this, R.anim.push_left_in)
                mFlipper!!.outAnimation = AnimationUtils.loadAnimation(this, R.anim.push_left_out)
            }
            2 -> {
                mFlipper!!.inAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
                mFlipper!!.outAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out)
            }
            else -> {
                mFlipper!!.inAnimation = AnimationUtils.loadAnimation(this, R.anim.hyperspace_in)
                mFlipper!!.outAnimation = AnimationUtils.loadAnimation(this, R.anim.hyperspace_out)
            }
        }
    }

    /**
     * Callback method to be invoked when the selection disappears from this [Spinner]. We ignore.
     *
     * @param parent The AdapterView that now contains no selected item.
     */
    override fun onNothingSelected(parent: AdapterView<*>?) {}
}
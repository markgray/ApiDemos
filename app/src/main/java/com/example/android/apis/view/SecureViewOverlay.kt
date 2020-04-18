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
package com.example.android.apis.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.example.android.apis.R

/**
 * This view is part of the [SecureView] demonstration activity.
 *
 * This view is constructed in such a way as to obscure the buttons and descriptive
 * text of the activity in a poor attempt to fool the user into clicking on the buttons
 * despite the activity telling the user that they may be harmful.
 */
class SecureViewOverlay
/**
 * Constructor called to perform inflation from XML. We just call our super's constructor.
 *
 * @param context The [Context] the view is running in, through which it can access the current
 *                theme, resources, etc.
 * @param attrs   The attributes of the XML tag that is inflating the view.
 */
(context: Context?, attrs: AttributeSet?) : ViewGroup(context, attrs) {
    /**
     * [SecureView] `AppCompatActivity` to use to find views we need to obscure.
     */
    private var mActivity: SecureView? = null

    /**
     * Saves its [SecureView] parameter [activity] in our [SecureView] field [mActivity] to
     * later use to find the views we need to obscure.
     *
     * @param activity "this" of the [SecureView] `AppCompatActivity` we are to spoof.
     */
    fun setActivityToSpoof(activity: SecureView?) {
        mActivity = activity
    }

    /**
     * Measure the view and its content to determine the measured width and the measured height. We
     * call the method `measureChildren` to ask all of the children of our [ViewGroup] to measure
     * themselves, then we call our super's implementation of `onMeasure`.
     *
     * @param widthMeasureSpec  horizontal space requirements as imposed by the parent.
     * @param heightMeasureSpec vertical space requirements as imposed by the parent.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    /**
     * Called from layout when this view should assign a size and position to each of its children.
     *
     * We call our method [spoofLayout] with the view in our the layout file that inflated to
     * us that has the ID R.id.secure_view_overlay_description, and the view in the activity we
     * are spoofing ([SecureView] field [mActivity]) with ID R.id.secure_view_description.
     *
     * We call our method [spoofLayout] with the view in our the layout file that inflated to
     * us that has the ID R.id.secure_view_overlay_button1, and the view in the activity we
     * are spoofing ([SecureView] field [mActivity]) with ID R.id.secure_view_unsecure_button.
     *
     * We call our method `spoofLayout` with the view in our the layout file that inflated to
     * us that has the ID R.id.secure_view_overlay_button2, and the view in the activity we
     * are spoofing (`SecureView mActivity`) with ID R.id.secure_view_builtin_secure_button.
     *
     *
     * We call our method [spoofLayout] with the view in our the layout file that inflated to
     * us that has the ID R.id.secure_view_overlay_button3, and the view in the activity we
     * are spoofing ([SecureView] field [mActivity]) with ID R.id.secure_view_custom_secure_button.
     *
     * In each of these calls to [spoofLayout] the position of the view of the activity we are
     * spoofing is used to position the view in our own layout so that the original is obscured by
     * our layout.
     *
     * @param changed This is a new size or position for this view
     * @param l       Left position, relative to parent
     * @param t       Top position, relative to parent
     * @param r       Right position, relative to parent
     * @param b       Bottom position, relative to parent
     */
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        spoofLayout(findViewById(R.id.secure_view_overlay_description),
                mActivity!!.findViewById(R.id.secure_view_description))
        spoofLayout(findViewById(R.id.secure_view_overlay_button1),
                mActivity!!.findViewById(R.id.secure_view_unsecure_button))
        spoofLayout(findViewById(R.id.secure_view_overlay_button2),
                mActivity!!.findViewById(R.id.secure_view_builtin_secure_button))
        spoofLayout(findViewById(R.id.secure_view_overlay_button3),
                mActivity!!.findViewById(R.id.secure_view_custom_secure_button))
    }

    /**
     * Causes the [View] parameter [spoof] to be laid out over the [View] parameter [original]
     * to obscure it. We allocate an two entry [Int] array to initialize our variable `val globalPos`,
     * and call the method `getLocationOnScreen` to load it with the coordinates of this `ViewGroup`
     * on the screen. We then initialize `int x` with `globalPos[0]`, and `int y` with
     * `globalPos[1]`. We call the `getLocationOnScreen` method of our [View] parameter [original]
     * to load `globalPos` with the coordinates of the view we want to obscure, then set `x`
     * to `globalPos[0] - x` and `y` to `globalPos[1] - y`. Finally we call the `layout`
     * method of [spoof] to assign the size and position of [spoof] to be top left at `(x,y)`,
     * and bottom right at (`x` plus the width of `original`, `y` plus the height of `original`).
     *
     * @param spoof    view within our layout we need to position
     * @param original the view in the activity we are spoofing that we want to obscure.
     */
    private fun spoofLayout(spoof: View, original: View) {
        val globalPos = IntArray(2)
        getLocationOnScreen(globalPos)
        var x = globalPos[0]
        var y = globalPos[1]
        original.getLocationOnScreen(globalPos)
        x = globalPos[0] - x
        y = globalPos[1] - y
        spoof.layout(x, y, x + original.width, y + original.height)
    }
}
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
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextSwitcher
import android.widget.TextView
import android.widget.ViewSwitcher
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Uses a [TextSwitcher] to animate changes in the View controlled by the [TextSwitcher].
 * Two TextView's are requested from the app's overriding of [ViewSwitcher.ViewFactory]
 * method `makeView`, and when the [View] needs to be updated [TextSwitcher.setText] is
 * called to change the text and animate to it.
 */
class TextSwitcher1 : AppCompatActivity(), ViewSwitcher.ViewFactory, View.OnClickListener {
    /**
     * [TextSwitcher] in our layout file with id R.id.switcher
     */
    private var mSwitcher: TextSwitcher? = null

    /**
     * Counter used as the text in the [TextView] that the `setText` method of our [TextSwitcher]
     * field [mSwitcher] switches to.
     */
    private var mCounter = 0

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.text_switcher_1.
     * We initialize our [TextSwitcher] field [mSwitcher] by finding the view in our layout with
     * the id R.id.switcher and set its `ViewFactory` to "this". We create a fade in animation
     * from android.R.anim.fade_in to initialize `Animation` variable `val inAnim`, and a fade
     * out animation from android.R.anim.fade_out to initialize `val outAnim`, then set the in
     * animation of [mSwitcher] to `inAnim` and the out animation to `outAnim`.
     *
     * We initialize our [Button] variable `val nextButton` by finding the view with id R.id.next
     * ("Next") and set its `OnClickListener` to "this". Finally we call our method [updateCounter]
     * to have [mSwitcher] set its text to the current string value of our [Int] field [mCounter].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.text_switcher_1)
        mSwitcher = findViewById(R.id.switcher)
        mSwitcher!!.setFactory(this)
        val inAnim = AnimationUtils.loadAnimation(
                this,
                android.R.anim.fade_in
        )
        val outAnim = AnimationUtils.loadAnimation(
                this,
                android.R.anim.fade_out
        )
        mSwitcher!!.inAnimation = inAnim
        mSwitcher!!.outAnimation = outAnim
        val nextButton = findViewById<Button>(R.id.next)
        nextButton.setOnClickListener(this)
        updateCounter()
    }

    /**
     * Called when the button with id R.id.next ("Next") is clicked. We increment our [Int] field
     * [mCounter], then call our method [updateCounter] to have [mSwitcher] set its text to the new
     * string value of [mCounter].
     *
     * @param v View that was clicked
     */
    override fun onClick(v: View) {
        mCounter++
        updateCounter()
    }

    /**
     * Called to switch the text that [TextSwitcher] field [mSwitcher] is displaying in its
     * [TextView] to the string value of [Int] field [mCounter]. The `setText` method of
     * [mSwitcher] does this for us.
     */
    private fun updateCounter() {
        mSwitcher!!.setText(mCounter.toString())
    }

    /**
     * Creates a new [View] to be added in a [ViewSwitcher]. We create a new instance to
     * initialize our [TextView] variable `val t`, set its gravity to TOP and CENTER_HORIZONTAL,
     * and set its text size to 36. We log the fact that we were asked to create a new [TextView]
     * then return `t` to the caller.
     *
     * @return a [View]
     */
    override fun makeView(): View {
        val t = TextView(this)
        t.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        t.textSize = 36f
        Log.i("makeView", "New View requested")
        return t
    }
}
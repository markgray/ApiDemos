/*
 * Copyright (C) 2008 The Android Open Source Project
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
package com.example.android.apis.graphics

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import com.example.android.apis.R

/**
 * Shows how to use the [Animation] api (in this case [TranslateAnimation]) in order to move a jpg
 * around a [Canvas]. Uses [AnimateDrawable] which extends [ProxyDrawable] (A neat way to package
 * the methods required when extending `Drawable`, overriding only `draw` in [AnimateDrawable])
 */
class AnimateDrawables : GraphicsActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to a new instance of [SampleView].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(SampleView(this))
    }

    /**
     * Custom View which contains an instance of [AnimateDrawable] which it creates from a
     * resource drawable (R.drawable.beach) and an [TranslateAnimation] which will move the
     * drawable from (0,0) to (100,200)
     */
    private class SampleView(context: Context) : View(context) {
        /**
         * The instance of [AnimateDrawable] that does all the work of moving our drawable around.
         */
        private val mDrawable: AnimateDrawable

        /**
         * Called when the system needs us to draw our view. First we fill the entire bitmap of the
         * [Canvas] parameter [canvas] with the color white, then we instruct our [AnimateDrawable]
         * field [mDrawable] to draw itself. Finally we call [invalidate] to invalidate our entire
         * view, causing this method to be called again in the sweet by and by.
         *
         * @param canvas [Canvas] we are to draw our view to
         */
        override fun onDraw(canvas: Canvas) {
            canvas.drawColor(Color.WHITE)
            mDrawable.draw(canvas)
            invalidate()
        }

        /**
         * Configures our `View`, and creates and configures an instance of `AnimateDrawable` which
         * will move around our view's canvas. First we call through to our super's constructor,
         * then we enable this view to receive focus, and to be focusable in touch mode. Next we
         * retrieve our resource `Drawable` R.drawable.beach to `val dr`, and set the bounding
         * rectangle of `dr` to the intrinsic size of `dr` (the size that the drawable would like
         * to be laid out, including any inherent padding). We create `Animation` variable `val an`
         * to be a `TranslateAnimation` that moves from (0,0) to (100,200), set its duration to 2000
         * milliseconds, set its repeat count to INFINITE, and call its `initialize` method to set
         * the size of the object being animated and its parent both to 10 x 10.
         *
         * Now we initialize our `AnimateDrawable` field `mDrawable` with a new instance of
         * `AnimateDrawable` created using `Drawable` variable `dr` and `Animation` variable `an`.
         * Finally we start the animation `an` at the current time in milliseconds.
         *
         * Parameter: context `Context` to use to fetch resources
         */
        init {
            isFocusable = true
            isFocusableInTouchMode = true
            @Suppress("DEPRECATION")
            val dr = context.resources.getDrawable(R.drawable.beach)
            dr.setBounds(0, 0, dr.intrinsicWidth, dr.intrinsicHeight)
            val an: Animation = TranslateAnimation(0f, 100f, 0f, 200f)
            an.duration = 2000
            an.repeatCount = Animation.INFINITE
            an.initialize(10, 10, 10, 10)
            mDrawable = AnimateDrawable(dr, an)
            an.startNow()
        }
    }
}
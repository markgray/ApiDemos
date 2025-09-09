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
package com.example.android.apis.graphics

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.core.graphics.withTranslation
import com.example.android.apis.graphics.RoundRects.SampleView.Companion.setCornerRadii
import com.example.android.apis.graphics.Utilities.d2p
import kotlin.math.sqrt

/**
 * Shows how to use a [GradientDrawable] to draw rectangles with rounded corners while using three
 * different types of color gradient:
 *
 *  * [GradientDrawable.LINEAR_GRADIENT],
 *  * [GradientDrawable.RADIAL_GRADIENT],
 *  * [GradientDrawable.SWEEP_GRADIENT].
 */
class RoundRects : GraphicsActivity() {
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
     * The View that does all the work
     *
     * @param context the [Context] of the activity using us.
     * (See our `init` block for the details of our constructor)
     */
    private class SampleView(context: Context?) : View(context) {
        /**
         * [Rect] we use to set the bounds of the [GradientDrawable] field [mDrawable] we draw
         */
        private val mRect: Rect

        /**
         * [GradientDrawable] that we draw using different gradient types.
         */
        private val mDrawable: GradientDrawable

        /**
         * We implement this to do our drawing. First we set the bounds (Specify a bounding rectangle)
         * for our [GradientDrawable] field [mDrawable] to be our [Rect] field [mRect]. Then we
         * define a [Float] constant `val r=16` to use for the radii of our [mDrawable]. We then
         * proceed to demonstrate 6 different types of gradients, each wrapped between matching
         * saves of the [Canvas] parameter [canvas] current matrix, and restores of that state:
         *
         *  * LINEAR_GRADIENT - we move [Canvas] parameter [canvas] to (10,10), set the gradient
         *  type of [mDrawable] to LINEAR_GRADIENT, set the corner radii of the top-left and
         *  top-right to `r`, the other radii to 0 and draw it. This causes a Linear  color
         *  change from red at the top-left corner, to blue at the bottom-right.
         *
         *  * RADIAL_GRADIENT - we move [Canvas] parameter [canvas] to a point 20 pixels to the
         *  right of our LINEAR_GRADIENT rectangle, set the gradient type of [mDrawable] to
         *  RADIAL_GRADIENT, set the corner radii of the bottom-right and bottom-left to
         *  `r`, the other radii to 0 and draw it. This causes a Linear color change
         *  from red at the center of the rectangle to blue at the corners. We interpolate
         *  a move of the canvas to 10 pixels below the first row of rectangles now to get
         *  ready for the second row.
         *
         *  * SWEEP_GRADIENT - we move [Canvas] parameter [canvas] to (10,10), set the gradient
         *  type of [mDrawable] to SWEEP_GRADIENT, set the corner radii of the top-right and
         *  bottom-right to `r`, the other radii to 0 and draw it. This creates a circular sweep
         *  of blended colour around the rectangle, starting with red at the  0 degree location,
         *  transitioning to green, then to blue as it comes back to the  0 degree location.
         *
         *  * LINEAR_GRADIENT - same as the first LINEAR_GRADIENT except for its location on the
         *  second row to the right of the SWEEP_GRADIENT example and the use of rounded corners
         *  for the top-left and bottom-left corners. We interpolate a move of the canvas to
         *  10 pixels below the second row of rectangles now to get ready for the third row.
         *
         *  * RADIAL_GRADIENT - same as the first RADIAL_GRADIENT except for its location on the
         *  third row first column, and the use of rounded corners for the top-left and bottom-right
         *  corners.
         *
         *  * SWEEP_GRADIENT - same as the first SWEEP_GRADIENT except for its location on the
         *  third row second column, and the use of rounded corners for the top-right and bottom-left
         *  corners.
         *
         * @param canvas the [Canvas] on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            canvas.translate(0f, d2p(160f))
            mDrawable.bounds = mRect
            val r = 16f
            canvas.withTranslation(x = 10f, y = 10f) {
                mDrawable.gradientType = GradientDrawable.LINEAR_GRADIENT
                setCornerRadii(mDrawable, r, r, 0f, 0f)
                mDrawable.draw(this)
            }
            canvas.withTranslation(x = 10 + mRect.width() + 10.toFloat(), y = 10f) {
                mDrawable.gradientType = GradientDrawable.RADIAL_GRADIENT
                setCornerRadii(mDrawable, 0f, 0f, r, r)
                mDrawable.draw(this)
            }
            canvas.translate(0f, mRect.height() + 10.toFloat())
            canvas.withTranslation(x = 10f, y = 10f) {
                mDrawable.gradientType = GradientDrawable.SWEEP_GRADIENT
                setCornerRadii(mDrawable, 0f, r, r, 0f)
                mDrawable.draw(this)
            }
            canvas.withTranslation(x = 10 + mRect.width() + 10.toFloat(), y = 10f) {
                mDrawable.gradientType = GradientDrawable.LINEAR_GRADIENT
                setCornerRadii(mDrawable, r, 0f, 0f, r)
                mDrawable.draw(this)
            }
            canvas.translate(0f, mRect.height() + 10.toFloat())
            canvas.withTranslation(x = 10f, y = 10f) {
                mDrawable.gradientType = GradientDrawable.RADIAL_GRADIENT
                setCornerRadii(mDrawable, r, 0f, r, 0f)
                mDrawable.draw(this)
            }
            canvas.withTranslation(x = 10 + mRect.width() + 10.toFloat(), y = 10f) {
                mDrawable.gradientType = GradientDrawable.SWEEP_GRADIENT
                setCornerRadii(mDrawable, 0f, r, 0f, r)
                mDrawable.draw(this)
            }
        }

        companion object {
            /**
             * Convenience function for calling [setCornerRadii] `(radii: Float)`. We simply stuff
             * the radius arguments into an anonymous [FloatArray], repeating the single radius
             * value for both the x and y radius, then call the [setCornerRadii] method of our
             * [GradientDrawable] parameter [drawable].
             *
             * @param drawable [GradientDrawable] we are to set the corner radii on
             * @param r0       top-left radius (for both x and y)
             * @param r1       top-right radius (for both x and y)
             * @param r2       bottom-right radius (for both x and y)
             * @param r3       bottom-left radius (for both x and y)
             */
            fun setCornerRadii(
                drawable: GradientDrawable,
                r0: Float,
                r1: Float,
                r2: Float,
                r3: Float
            ) {
                drawable.cornerRadii = floatArrayOf(r0, r0, r1, r1, r2, r2, r3, r3)
            }
        }

        /**
         * The init block of our constructor. First we enable our view to receive focus. We initialize
         * our `Rect` field `mRect` with a new 120x120 pixel rectangle. We initialize our `GradientDrawable`
         * field `mDrawable` with a new instance whose orientation is TL_BR (draws the gradient from
         * the top-left to the bottom-right) and whose colors array is red, green and blue. We set
         * the shape of `mDrawable` to RECTANGLE and set the gradient radius to 60 times the square
         * root of 2.
         */
        init {
            isFocusable = true
            mRect = Rect(0, 0, 120, 120)
            mDrawable = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(-0x10000, -0xff0100, -0xffff01)
            )
            mDrawable.shape = GradientDrawable.RECTANGLE
            mDrawable.gradientRadius = (sqrt(2.0) * 60).toFloat()
        }
    }
}
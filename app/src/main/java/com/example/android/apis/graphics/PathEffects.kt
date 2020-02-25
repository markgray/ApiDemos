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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.view.KeyEvent
import android.view.View

/**
 * Shows how to use the PathEffect classes to make animated dashed lines, and smoothed, rounded lines.
 */
class PathEffects : GraphicsActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to a new instance of `SampleView`.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scale = resources.displayMetrics.density
        DISTANCE_BETWEEN_LINES = (28 * scale).toInt()
        X_INCREMENT = (20 * scale).toInt()
        setContentView(SampleView(this))
    }

    /**
     * Class which displays several animated dashed lines, and smoothed, rounded lines.
     */
    private class SampleView(context: Context?) : View(context) {
        /**
         * `Paint` instance that we use to draw our lines.
         */
        private val mPaint: Paint
        /**
         * Random zigzag `Path` we use to draw each line
         */
        private var mPath: Path
        /**
         * Our 6 kinds of `PathEffect` examples (including null - no effect).
         */
        private val mEffects: Array<PathEffect?>
        /**
         * The 6 colors we use for our lines: Color.BLACK, Color.RED, Color.BLUE, Color.GREEN,
         * Color.MAGENTA, and Color.BLACK
         */
        private val mColors: IntArray
        /**
         * Offset into the intervals array (mod the sum of all of the intervals) used when creating
         * dash effect lines. By incrementing it in the `onDraw` method the lines appear to
         * move.
         */
        private var mPhase = 0f

        /**
         * We implement this to do our drawing. First we fill the `Canvas canvas` with the color
         * white. Then we allocate a new instance of `RectF` for `RectF bounds` and load
         * it with the bounds of the control points of the path `mPath`. We translate our
         * `Canvas canvas` in the x direction by 10 - `bounds.left` (`bounds.left`
         * is always 0) and in the y direction by 10 - `bounds.top` `bounds.top` is always
         * 0 as well).
         *
         *
         * Next we call our method `makeEffects` to generate new versions of `PathEffects`
         * for `mEffects` using the present value of `mPhase`, then increment `mPhase`
         * and call `invalidate` to request that a new call to this method `onDraw` in the
         * future.
         *
         *
         * Now we are ready to loop through each `PathEffect` in `mEffects`, setting the
         * path effect object for `Paint mPaint` to each in turn, setting the color of `mPaint`
         * to the next color, then instructing the `Canvas canvas` to draw the path `mPath`
         * using `mPaint` as the `Paint`. We then translate the canvas down in the y coordinate
         * in order to get ready for the next `PathEffect`.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            canvas.drawColor(Color.WHITE)
            @SuppressLint("DrawAllocation") val bounds = RectF()
            mPath.computeBounds(bounds, false)
            canvas.translate(10 - bounds.left, 10 - bounds.top)
            makeEffects(mEffects, mPhase)
            mPhase += 1f
            invalidate()
            for (i in mEffects.indices) {
                mPaint.pathEffect = mEffects[i]
                mPaint.color = mColors[i]
                canvas.drawPath(mPath, mPaint)
                canvas.translate(0f, DISTANCE_BETWEEN_LINES.toFloat())
            }
        }

        /**
         * Called when a key down event has occurred. If the keycode we KEYCODE_DPAD_CENTER, we set
         * `Path mPath` to the new random `Path` returned from our method `makeFollowPath`
         * and return true, otherwise we return the value returned by our super's implementation of
         * `onKeyDown`.
         *
         * @param keyCode A key code that represents the button pressed,
         * @param event   The KeyEvent object that defines the button action.
         * @return true if we handled the event
         */
        override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_CENTER -> {
                    mPath = makeFollowPath()
                    return true
                }
            }
            return super.onKeyDown(keyCode, event)
        }

        companion object {
            /**
             * Unused so no comment.
             *
             * @param phase Offset into the intervals array
             * @return a `PathEffect` instance
             */
            @Suppress("unused")
            private fun makeDash(phase: Float): PathEffect {
                return DashPathEffect(floatArrayOf(15f, 5f, 8f, 5f), phase)
            }

            /**
             * Allocates and initializes 6 different `PathEffect` objects using the current value
             * of `phase` passed us:
             *
             *  *
             * e[0] - null for no path effect
             *
             *  *
             * e[1] - `CornerPathEffect(10)` Transforms geometries that are drawn (either
             * STROKE or FILL styles) by replacing any sharp angles between line segments into
             * rounded angles of the specified radius (10).
             *
             *  *
             * e[2] - `DashPathEffect` Dashed line with the first segment 10 pixels, followed
             * by 5 pixels off, then 5 pixels drawn, then 5 pixels off. `phase` is passed to
             * the constructor to select an offset into the dashes allowing for an animated dashed
             * line when this method is called with different values for `phase` every time
             * the line is to be drawn.
             *
             *  *
             * e[3] - `PathDashPathEffect` Dashes the drawn path by stamping it with the shape
             * of the `Path` returned by our method `makePathDash` (an arrow like shape).
             * It has a spacing of 12 between "stampings", passes `phase` to the constructor
             * to allow animation of the dashes, and uses PathDashPathEffect.Style.ROTATE (rotates
             * the shape about its center)
             *
             *  *
             * e[4] - `ComposePathEffect` A `PathEffect` which applies e[1] first (replaces
             * sharp corners with rounded angles) followed by e[2] (dashed line path effect).
             *
             *  *
             * e[5] - `ComposePathEffect` A `PathEffect` which applies e[1] first (replaces
             * sharp corners with rounded angles) followed by e[3] (dashed line using a shape to stamp
             * along the line).
             *
             *
             *
             * @param e     array of `PathEffect` objects to allocate and initialize.
             * @param phase Offset into the intervals array.
             */
            private fun makeEffects(e: Array<PathEffect?>, phase: Float) {
                e[0] = null // no effect
                e[1] = CornerPathEffect(10f)
                e[2] = DashPathEffect(floatArrayOf(10f, 5f, 5f, 5f), phase)
                e[3] = PathDashPathEffect(makePathDash(), 12f, phase, PathDashPathEffect.Style.ROTATE)
                e[4] = ComposePathEffect(e[2], e[1])
                e[5] = ComposePathEffect(e[3], e[1])
            }

            /**
             * Creates and returns a random `Path`. First we allocate a new `Path` instance
             * `Path p`. We move `p` to (0,0), then add 15 `lineTo` line segments with
             * a spacing of X_INCREMENT in the x direction and a random y between 0 and 35.
             *
             * @return random `Path`.
             */
            private fun makeFollowPath(): Path {
                val p = Path()
                p.moveTo(0f, 0f)
                for (i in 1..15) {
                    p.lineTo(i * X_INCREMENT.toFloat(), Math.random().toFloat() * 35)
                }
                return p
            }

            /**
             * Creates and returns a `Path` that looks rather like an arrowhead when drawn.
             *
             * @return `Path` drawing an arrowhead.
             */
            private fun makePathDash(): Path {
                val p = Path()
                p.moveTo(4f, 0f)
                p.lineTo(0f, -4f)
                p.lineTo(8f, -4f)
                p.lineTo(12f, 0f)
                p.lineTo(8f, 4f)
                p.lineTo(0f, 4f)
                return p
            }
        }

        /**
         * Our constructor. First we call our super's constructor, then we set our window to be focusable,
         * an focusable in touch mode. We initialize our field `Paint mPaint` with a new instance of
         * `Paint` with the anti alias flag set, set its style to STROKE, and set its stroke width
         * to 6. We initialize our field `Path mPath` with the value returned from our method
         * `makeFollowPath`. We allocate a 6 element array of `PathEffect` objects for our
         * field `PathEffect[] mEffects`, and initialize our field `int[] mColors` with 6
         * colors.
         *
         *
         * Finally we set our `OnClickListener` to an anonymous class which sets `mPath`
         * to a new random `Path` created by our method `makeFollowPath`.
         *
         * Parameter: context `Context` to use for resources, this when called from the `onCreate`
         * method of the `PathEffects` activity.
         */
        init {
            isFocusable = true
            isFocusableInTouchMode = true
            mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            mPaint.style = Paint.Style.STROKE
            mPaint.strokeWidth = 6f
            mPath = makeFollowPath()
            mEffects = arrayOfNulls(6)
            mColors = intArrayOf(Color.BLACK, Color.RED, Color.BLUE,
                    Color.GREEN, Color.MAGENTA, Color.BLACK
            )
            setOnClickListener { mPath = makeFollowPath() }
        }
    }

    companion object {
        var DISTANCE_BETWEEN_LINES = 28
        var X_INCREMENT = 20
    }
}
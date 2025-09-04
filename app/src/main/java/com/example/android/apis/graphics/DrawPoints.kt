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
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import com.example.android.apis.graphics.DrawPoints.SampleView.Companion.SEGS
import com.example.android.apis.graphics.DrawPoints.SampleView.Companion.SIZE
import kotlin.math.roundToInt

/**
 * Creates an array of points, draws RED lines between them using [Canvas.drawLines] then draws the
 * points by themselves in BLUE using [Canvas.drawPoints].
 */
class DrawPoints : GraphicsActivity() {
    /**
     * Called when the activity is starting. First we call our super's implementation of
     * `onCreate`, then we set our content view to a new instance of [SampleView]
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(SampleView(this))
    }

    /**
     * Demonstrates how to use the [Canvas.drawLines] and [Canvas.drawPoints] methods to
     * draw lines and points.
     *
     * @param context the [Context] to use for resources, `this` when called from onCreate
     * (See our init block for our constructor details)
     */
    private class SampleView(context: Context) : View(context) {
        /**
         * [Paint] that we use to draw with in our [onDraw] method.
         */
        private val mPaint = Paint()

        /**
         * Our array of points, allocated and initialized by our [buildPoints] method.
         */
        private lateinit var mPts: FloatArray

        /**
         * Allocates and initializes our [FloatArray] field [mPts] with an array of points. First
         * we determine the number of points `val ptCount` we will need given the value of [SEGS]
         * (twice the value of `SEGS+1`, twice because we will be dividing a line of [SIZE]
         * length into [SEGS] segments for both x and y axes, the +1 is because of the endpoint).
         * Next we allocate `2*ptCount` floats for `mPts` because each point will have an
         * x and a y coordinate.
         *
         * We initialize `val value` to 0.0 and calculate `val delta` to be [SIZE] divided by
         * [SEGS]. Then we loop for our [SEGS] segments assigning the values of four [mPts] entries
         * so that the first contains the x coordinate of the point along the x axis (goes from
         * [SIZE] down to 0 in steps of `delta`), the second contains the y coordinate of the point
         * along the x axis (always 0), the third contains the x coordinate of the point along the
         * y axis (always 0), and the fourth contains the y coordinate of the point along the y axis
         * (goes from 0 to [SIZE] in steps of `delta`). We then add `delta` to `value` and loop
         * around for the next segment.
         */
        private fun buildPoints() {
            val ptCount = (SEGS + 1) * 2
            mPts = FloatArray(ptCount * 2)
            var value = 0f
            val delta = SIZE / SEGS
            for (i in 0..SEGS) {
                mPts[i * 4 + X] = SIZE - value
                mPts[i * 4 + Y] = 0f
                mPts[i * 4 + X + 2] = 0f
                mPts[i * 4 + Y + 2] = value
                value += delta
            }
        }

        /**
         * We implement this to do our drawing. First we make a copy of our [Paint] field [mPaint]
         * `val paint`. Then we translate our [Canvas] parameter [canvas] parameter to the point
         * (10,10), and set it to all WHITE. We set the color of `paint` to RED, its stroke width
         * to 0, and use it in a call to the [Canvas.drawLines] method of [canvas] to draw a series
         * of lines with each line taken from 4 consecutive values in the [mPts] array.
         *
         * Next we set the color of `paint` to BLUE, its stroke width to 3, and use it to call
         * the [Canvas.drawPoints] method of [canvas] to draw a series of points, each point
         * centered at the coordinate specified by 2 consecutive values in the [mPts] array,
         * with its diameter specified by the paint's stroke width.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            val paint = mPaint
            canvas.translate(10f, 350f)
            canvas.drawColor(Color.WHITE)
            paint.color = Color.RED
            paint.strokeWidth = 0f
            canvas.drawLines(mPts, paint)
            paint.color = Color.BLUE
            paint.strokeWidth = 3f
            canvas.drawPoints(mPts, paint)
        }

        companion object {
            /**
             * Size of our mesh, set in our constructor to the pixel equivalent of 300 dp.
             */
            private var SIZE: Float = 300f

            /**
             * Number of line segments to divide our SIZE line into.
             */
            private const val SEGS = 32

            /**
             * Offset for the x coordinate of our point
             */
            private const val X = 0

            /**
             * Offset for the y coordinate of our point
             */
            private const val Y = 1
        }

        /**
         * The init block of iur constructor. We retrieve the current display metrics that are in
         * effect to initialize `DisplayMetrics` variable `val displayMetrics`, and use it to
         * calculate the value of 300dpi in pixels to initialize our field `SIZE`. Finally we call
         * our method `buildPoints` to allocate and initialize the array of points in our field
         * `FloatArray` field `mPts`.
         */
        init {
            val displayMetrics = context.resources.displayMetrics
            SIZE = (300 * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
                .toFloat()
            buildPoints()
        }
    }
}
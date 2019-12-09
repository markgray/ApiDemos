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
import android.graphics.RectF
import android.os.Bundle
import android.view.View

/**
 * Shows how to draw arcs and rectangles to a [Canvas] -- need to figure out what slows down
 * frame rate -- I'm guessing something inside native_drawArc
 */
class Arcs : GraphicsActivity() {
    /**
     * Called when the activity is staring. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to an instance of [SampleView].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(SampleView(this))
    }

    /**
     * Subclass of View which consists of one big circle inside a rectangle, and four little circles
     * inside rectangles. Rotating arcs are drawn inside each of these circles, with four different
     * paints and "use centers" arguments used for the small circles, and a rotation of those four
     * combos for the big circle which changes every 360 degrees.
     */
    private class SampleView(context: Context?) : View(context) {
        /**
         * The four [Paint] objects used for the small circles, and cycled through for the big one
         */
        private val mPaints: Array<Paint?> = arrayOfNulls(4)
        /**
         * [Paint] used for the rectangles drawn around the 5 circles.
         */
        private val mFramePaint: Paint
        /**
         * [Boolean] flag passed to `drawArc` for each of the circles, it is *true* for 2 and
         * false for 2. If *true*, includes the center of the oval in the arc, and close it if it
         * is being stroked. This will draw a wedge.
         */
        private val mUseCenters: BooleanArray = BooleanArray(4)
        /**
         * Rectangles for the four small circles in a rectangle, defined by (left, top, right, bottom)
         * float pixel values
         */
        private val mOvals: Array<RectF?> = arrayOfNulls(4)
        /**
         * Rectangle for the large circle in a rectangle, defined by (left, top, right, bottom)
         * float pixel values
         */
        private val mBigOval: RectF
        /**
         * Starting angle for the arcs being drawn, starts at 0 degrees, incremented by 15 degrees
         * every time [mSweep] reaches 360 degrees
         */
        private var mStart = 0f
        /**
         * Sweep angle for the arcs being drawn, starts at 0 degrees, incremented by 2 degrees every
         * time `onDraw` is called and reset to 0 when it reaches 360 degrees.
         */
        private var mSweep = 0f
        /**
         * Index 0-3 of values used by the large circle in a rectangle, chooses which of the small
         * circles in a rectangle the large circle mirrors, it is incremented modulo 4 every 360
         * degrees of arc sweep.
         */
        private var mBigIndex = 0

        /**
         * Draws the [RectF] rectangle [oval] passed it using [Paint] field [mFramePaint] as the
         * [Paint], then draws an arc of the circle enclosed by [oval] between [mStart] and [mSweep]
         * using [Paint] parameter [paint] as the [Paint] and passing the [Boolean] value of the
         * [useCenter] paramete to [Canvas.drawArc] to instruct it when to include the center when
         * drawing.
         *
         * @param canvas [Canvas] we are to draw to
         * @param oval Rectangle and enclosed circle we want to draw
         * @param useCenter if *true*, we are to include the center of the oval in the arc, and
         * close it drawing a wedge.
         * @param paint [Paint] to use for drawing
         */
        private fun drawArcs(canvas: Canvas, oval: RectF?, useCenter: Boolean, paint: Paint?) {
            canvas.drawRect(oval!!, mFramePaint)
            canvas.drawArc(oval, mStart, mSweep, useCenter, paint!!)
        }

        /**
         * We implement this to do our drawing. First we fill the entire [Canvas] parameter [canvas]
         * passed us with the color WHITE. Next we call our method [drawArcs] to draw the big circle
         * in the rectangle [mBigOval] using the [mUseCenters] and [mPaints] for the current value
         * of [mBigIndex] (cycles every 360 degrees through the values used by the small circles
         * described by the [RectF] array field [mOvals]). Next we call our method [drawArcs] to
         * draw each of the four small circles in rectangles using the values of [mUseCenters] and
         * [mPaints] assigned to each. Then we increment the value of [mSweep] (the end value of the
         * arcs drawn) by [SWEEP_INC], and if it is greater than 360 degrees we subtract 360 from
         * it, and increment [mStart] (the start angle of the arcs drawn) by [START_INC], and if it
         * is greater than 360 degrees we subtract 360 from it. The we increment [mBigIndex] (the
         * value of small circle parameters used by the big circle) modulo 4. Finally we invalidate
         * our [View] so that our [onDraw] method will be called again in the sweet by and by.
         *
         * @param canvas [Canvas] of our [View] to draw to
         */
        override fun onDraw(canvas: Canvas) {
            canvas.drawColor(Color.WHITE)
            drawArcs(canvas, mBigOval, mUseCenters[mBigIndex], mPaints[mBigIndex])
            for (i in 0..3) {
                drawArcs(canvas, mOvals[i], mUseCenters[i], mPaints[i])
            }
            mSweep += SWEEP_INC
            if (mSweep > 360) {
                mSweep -= 360f
                mStart += START_INC
                if (mStart >= 360) {
                    mStart -= 360f
                }
                mBigIndex = (mBigIndex + 1) % mOvals.size
            }
            invalidate()
        }

        /**
         * Our static constants
         */
        companion object {
            /**
             * Number of degrees that [mSweep] is incremented every time [onDraw] is called
             */
            private const val SWEEP_INC = 2f
            /**
             * Number of degrees that [mStart] is incremented every 360 degrees of arc sweep
             */
            private const val START_INC = 15f
        }

        /**
         * Constructor, allocates and initializes fields needed by our `onDraw` override. First
         * we call through to our super's constructor, next we allocate an array of four references
         * for each of the fields used by our small circles: `Paint` array `mPaints`, `Boolean`
         * array `mUseCenters` and `RectF` array `mOvals`. We configure the entries for `mPaints`
         * and `mUseCenters` as follows:
         *
         *  * [0] Sets its `mPaints[0]` to a `Paint` with antialias enabled, style FILL,
         *  and color 0x88FF0000 (RED), and sets its `mUseCenters[0]` to false
         *  * [1] Sets its `mPaints[1]` to a `Paint` with color 0x8800FF00 (GREEN), and sets
         *  its `mUseCenters[0]` to true.
         *  * [2] Sets its `mPaints[2]` to a `Paint` with style STROKE, a stroke width
         *  of 4 and color 0x880000FF (BLUE), and sets its `mUseCenters[0]` to false
         *  * [3] Sets its `mPaints[3]` to a `Paint` with color 0x88888888 (GRAY),
         *  and sets its `mUseCenters[0]` to true
         *
         * Next we allocate a rectangle for `Rect` field `mBigOval` with the values: left 40, top 10,
         * right 280, bottom 250 (a 240x240 rectangle whose top corner is at (40,10)), and for each
         * of the small circles in `RectF` array `mOvals` we allocate a 60x60 rectangle with the top
         * corners located at (10,270), (90,270), (170,270) and (250,270) respectively. Finally we
         * allocate a `Paint` for the `Paint` used to draw the rectangle around all five circles
         * `Paint` field `mFramePaint`, set its antialias flag to true, style to STROKE, and stroke
         * width to 0.
         *
         * Parameter: `Context` passed to super's constructor for resource access, *this*
         * is used from `onCreate` override of `Arcs`
         */
        init {
            mPaints[0] = Paint()
            mPaints[0]!!.isAntiAlias = true
            mPaints[0]!!.style = Paint.Style.FILL
            mPaints[0]!!.color = -0x77010000
            mUseCenters[0] = false
            mPaints[1] = Paint(mPaints[0])
            mPaints[1]!!.color = -0x77ff0100
            mUseCenters[1] = true
            mPaints[2] = Paint(mPaints[0])
            mPaints[2]!!.style = Paint.Style.STROKE
            mPaints[2]!!.strokeWidth = 4f
            mPaints[2]!!.color = -0x77ffff01
            mUseCenters[2] = false
            mPaints[3] = Paint(mPaints[2])
            mPaints[3]!!.color = -0x77777778
            mUseCenters[3] = true
            mBigOval = RectF(40f, 10f, 280f, 250f)
            mOvals[0] = RectF(10f, 270f, 70f, 330f)
            mOvals[1] = RectF(90f, 270f, 150f, 330f)
            mOvals[2] = RectF(170f, 270f, 230f, 330f)
            mOvals[3] = RectF(250f, 270f, 310f, 330f)
            mFramePaint = Paint()
            mFramePaint.isAntiAlias = true
            mFramePaint.style = Paint.Style.STROKE
            mFramePaint.strokeWidth = 0f
        }
    }
}
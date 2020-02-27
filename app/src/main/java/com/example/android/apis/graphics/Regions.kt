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
import android.graphics.*
import android.os.Bundle
import android.view.View

/**
 * Shows how to use the Region class to merge two or more Rectangle's in a Region using Union, Xor,
 * Difference, and Intersect operations.
 */
class Regions : GraphicsActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to a new instance of `SampleView`.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(SampleView(this))
    }

    /**
     * Our demo custom view, demonstrates the use of the Region class.
     */
    private class SampleView(context: Context?) : View(context) {
        /**
         * `Paint` instance used to do all our drawing.
         */
        private val mPaint = Paint()
        /**
         * `Rect` rectangle with the top left corner at (10.10) and bottom right corner at (100.80)
         */
        private val mRect1 = Rect()
        /**
         * `Rect` rectangle with the top left corner at (50,50) and bottom right corner at (130.110)
         */
        private val mRect2 = Rect()

        /**
         * Draws the original rectangles `Rect1` and `Rect2` using the parameter
         * `alpha` as the alpha value of the `Paint mPaint` we use to draw them.
         *
         *
         * First we set the style of `Paint mPaint` to STROKE, its color to RED, and its alpha
         * to our parameter `alpha`. We then call our method `drawCentered` to draw
         * `mRect1` on `Canvas canvas` using `mPaint`.
         *
         *
         * Next we set the color of `mPaint` to BLUE, and its alpha to our parameter
         * `alpha`. We then call our method `drawCentered` to draw `mRect2` on
         * `Canvas canvas` using `mPaint`.
         *
         *
         * Finally we restore the style of `mPaint` to FILL.
         *
         * @param canvas `Canvas` to draw to
         * @param alpha  alpha value to set the alpha value of `Paint mPaint` to before drawing.
         */
        private fun drawOriginalRects(canvas: Canvas, alpha: Int) {
            mPaint.style = Paint.Style.STROKE
            mPaint.color = Color.RED
            mPaint.alpha = alpha
            drawCentered(canvas, mRect1, mPaint)
            mPaint.color = Color.BLUE
            mPaint.alpha = alpha
            drawCentered(canvas, mRect2, mPaint)
            // restore style
            mPaint.style = Paint.Style.FILL
        }

        /**
         * Creates a `Region` by combining `Rect mRect1` and `Rect mRect2` using
         * our parameter `Region.Op op` and then draws it.
         *
         *
         * First if our parameter `String str` is not null, we set the color of `Paint mPaint`
         * to BLACK and draw `str` at the coordinates (80,24).
         *
         *
         * We allocate a new instance of `Region` for `Region rgn`, set it to `Rect Rect1`,
         * then perform `Region.Op op` on the `Region` and `Rect Rect2` (`rgn`
         * will become a collection of 1 or more `Rect` objects depending on the `Region.Op`
         * used when `Rect2` was included in the `Region`:
         *
         *  * UNION - 3 `Rect`: (10,10,100,50), (10,50,130,80), and (50,80,130,110)
         *  * XOR - 4 `Rect`: (10,10,100,50), (10,50,50,80), (100,50,130,80), and (50,80,130,110)
         *  * DIFFERENCE - 2 `Rect`: (10,10,100,50), and (10,50,50,80)
         *  * INTERSECT - 1 `Rect`: (50,50,100,80)
         *
         * Next we set the color of `Paint mPaint` to our parameter `color`, create an
         * `RegionIterator iter` for `Region rgm`, and allocate a new `Rect` for
         * `Rect r`. We move the `Canvas canvas` to (0,30) and set the color of
         * `Paint mPaint` to our parameter `color` one more time for luck.
         *
         *
         * Now we iterate through the `Rect` objects in `Region rgn` (using the iterator
         * `iter`) setting `Rect r` to each in turn, and then drawing that `Rect`
         * to `Canvas canvas` using `Paint mPaint`.
         *
         *
         * Finally we call our method `drawOriginalRects` to draw an outline of the original
         * `Rect mRect1` and `Rect mRect2` using an alpha of only 0x80.
         *
         * @param canvas `Canvas` we are to draw to
         * @param color  color to use for drawing
         * @param str    optional string to label our drawing
         * @param op     `Region.Op` to use in forming our `Region`
         */
        private fun drawRgn(canvas: Canvas, color: Int, str: String?, op: Region.Op) {
            if (str != null) {
                mPaint.color = Color.BLACK
                canvas.drawText(str, 80f, 24f, mPaint)
            }
            val rgn = Region()
            rgn.set(mRect1)
            rgn.op(mRect2, op)
            mPaint.color = color
            val iter = RegionIterator(rgn)
            val r = Rect()
            canvas.translate(0f, 30f)
            mPaint.color = color
            while (iter.next(r)) {
                canvas.drawRect(r, mPaint)
            }
            drawOriginalRects(canvas, 0x80)
        }

        /**
         * We implement this to do our drawing. First we set the entire `Canvas canvas` to
         * the color GRAY. We save the current matrix and clip of the canvas onto a private stack,
         * move the canvas to the point (80,5) and call our method `drawOriginalRects` to draw
         * `Rect mRect1` and `Rect mRect2` using an alpha of 0xFF, then we restore the
         * state of the canvas to its previous state.
         *
         *
         * Next we set the style of `Paint mPaint` to FILL. We save the current matrix and clip
         * of the canvas onto a private stack, move the canvas to the point (0,140) and call our method
         * `drawRgn` to form a `Region` from `mRect1` and `mRect2` using the
         * Region.Op.UNION, draw the result in RED and label it "Union". We then restore the state of
         * the canvas to its previous state before we called `save`.
         *
         *
         * Now we save the current matrix and clip of the canvas onto a private stack, move the canvas
         * to the point (0,280) and call our method `drawRgn` to form a `Region` from
         * `mRect1` and `mRect2` using the Region.Op.XOR, draw the result in BLUE and
         * label it "Xor". We then restore the state of the canvas to its previous state before we
         * called `save`.
         *
         *
         * We save the current matrix and clip of the canvas onto a private stack, move the canvas
         * to the point (160,140) and call our method `drawRgn` to form a `Region` from
         * `mRect1` and `mRect2` using the Region.Op.DIFFERENCE, draw the result in GREEN
         * and label it "Difference". We then restore the state of the canvas to its previous state
         * before we called `save`.
         *
         *
         * We save the current matrix and clip of the canvas onto a private stack, move the canvas
         * to the point (160,280) and call our method `drawRgn` to form a `Region` from
         * `mRect1` and `mRect2` using the Region.Op.INTERSECT, draw the result in WHITE
         * and label it "Intersect". We then restore the state of the canvas to its previous state
         * before we called `save`.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            canvas.drawColor(Color.GRAY)
            canvas.save()
            canvas.translate(80f, 5f)
            drawOriginalRects(canvas, 0xFF)
            canvas.restore()
            mPaint.style = Paint.Style.FILL
            canvas.save()
            canvas.translate(0f, 140f)
            drawRgn(canvas, Color.RED, "Union", Region.Op.UNION)
            canvas.restore()
            canvas.save()
            canvas.translate(0f, 280f)
            drawRgn(canvas, Color.BLUE, "Xor", Region.Op.XOR)
            canvas.restore()
            canvas.save()
            canvas.translate(160f, 140f)
            drawRgn(canvas, Color.GREEN, "Difference", Region.Op.DIFFERENCE)
            canvas.restore()
            canvas.save()
            canvas.translate(160f, 280f)
            drawRgn(canvas, Color.WHITE, "Intersect", Region.Op.INTERSECT)
            canvas.restore()
        }

        companion object {
            /**
             * Draws the `Rect r` passed it offset by half the stroke width of the `Paint p`
             * on `Canvas c` using `p` as the `Paint`. We calculate `inset` to
             * be half of the stroke width of `Paint p`, and if 0 set `inset` to 0.5. Then
             * we draw the rectangle passed us in `Rect r` with each coordinate offset by `inset`
             *
             * @param c `Canvas` to draw to
             * @param r `Rect` to draw
             * @param p `Paint` to use when drawing
             */
            private fun drawCentered(c: Canvas, r: Rect, p: Paint) {
                var inset = p.strokeWidth * 0.5f
                if (inset == 0f) { // catch hairlines
                    inset = 0.5f
                }
                c.drawRect(r.left + inset, r.top + inset, r.right - inset, r.bottom - inset, p)
            }
        }

        /**
         * Our constructor. First we call our super's constructor, then we enable our view to receive
         * focus. We set the anti alias flag of `Paint mPaint` to true, its text size to 16 and
         * its text alignment to CENTER. We then initialize our field `Rect mRect1` with a rectangle
         * with the top left corner at (10.10) and bottom right corner at (100.80), and `Rect mRect2`
         * with a rectangle with the top left corner at (50,50) and bottom right corner at (130.110).
         *
         * Parameter: context `Context` to use to access resources, this when called from the
         * `onCreate` method of the `Regions` activity.
         */
        init {
            isFocusable = true
            mPaint.isAntiAlias = true
            mPaint.textSize = 16f
            mPaint.textAlign = Paint.Align.CENTER
            mRect1[10, 10, 100] = 80
            mRect2[50, 50, 130] = 110
        }
    }
}
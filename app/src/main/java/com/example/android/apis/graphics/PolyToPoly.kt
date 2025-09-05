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
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.core.graphics.withSave
import androidx.core.graphics.withTranslation

/**
 * Shows how to use [Matrix.setPolyToPoly] to move and warp drawings done to a Canvas:
 *
 *  * translate (1 point)
 *  * rotate/uniform-scale (2 points)
 *  * rotate/skew (3 points)
 *  * perspective (4 points)
 */
class PolyToPoly : GraphicsActivity() {
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
     * Demo showing how to use [Matrix.setPolyToPoly] to move and warp drawings done to a Canvas
     *
     * @param context the [Context] of the activity using us
     * (See our `init` block for the details of our constructor)
     */
    private class SampleView(context: Context?) : View(context) {
        /**
         * [Paint] we use to draw with
         */
        private val mPaint = Paint(/* flags = */ Paint.ANTI_ALIAS_FLAG)

        /**
         * [Matrix] we use to hold transformation that warps our drawings when concatenated to
         * the current matrix of the canvas.
         */
        private val mMatrix = Matrix()

        /**
         * [Paint.FontMetrics] of the [Paint] field [mPaint] used to center text we draw.
         */
        private val mFontMetrics: Paint.FontMetrics

        /**
         * Draws a warped rectangle with two lines connecting the nonadjacent vertices and a number
         * in the center representing the number of points used in the [Matrix.setPolyToPoly]
         * transformation matrix that is used to warp it. The transformation matrix in [Matrix] field
         * [mMatrix] is created so that the points in the [FloatArray] parameter [src] are mapped to
         * the points in [FloatArray] parameter [dst].
         *
         * First we save the current matrix and clip of [Canvas] parameter [canvas] onto a private
         * stack. Then we set [Matrix] field [mMatrix] to a matrix such that the points specified in
         * our parameter [src] would map to the points specified by our parameter [dst]. We then
         * pre-concatenate [mMatrix] to the current matrix of [canvas].
         *
         * We set the color of [Paint] field [mPaint] to GRAY, set its style to STROKE, and then use
         * it to draw a rectangle, and two lines connecting the nonadjacent vertices to [canvas].
         *
         * We set the color of [mPaint] to RED, and set its style to FILL. Then we calculate the
         * center of our rectangle based on the size of the rectangle and the size of the font used
         * by [mPaint] then draw the number of points in our [src] array at the center of the
         * rectangle.
         *
         * Finally we restore the current matrix of the [canvas] to the state it was in before the
         * [Canvas.save] call at the beginning of our method.
         *
         * @param canvas `Canvas` to draw to
         * @param src    array of source points for a call to `setPolyToPoly`
         * @param dst    array of destination points for a call to `setPolyToPoly`
         */
        private fun doDraw(canvas: Canvas, src: FloatArray, dst: FloatArray) {
            canvas.withSave {
                mMatrix.setPolyToPoly(
                    /* src = */ src, /* srcIndex = */ 0,
                    /* dst = */ dst, /* dstIndex = */ 0,
                    /* pointCount = */ src.size shr 1
                )
                concat(mMatrix)
                mPaint.color = Color.GRAY
                mPaint.style = Paint.Style.STROKE
                drawRect(
                    /* left = */ 0f, /* top = */ 0f,
                    /* right = */ 64f, /* bottom = */ 64f,
                    /* paint = */ mPaint
                )
                drawLine(
                    /* startX = */ 0f, /* startY = */ 0f,
                    /* stopX = */ 64f, /* stopY = */ 64f,
                    /* paint = */ mPaint
                )
                drawLine(
                    /* startX = */ 0f, /* startY = */ 64f,
                    /* stopX = */ 64f, /* stopY = */ 0f,
                    /* paint = */ mPaint
                )
                mPaint.color = Color.RED
                mPaint.style = Paint.Style.FILL
                /**
                 * how to draw the text center on our square
                 * centering in X is easy... use alignment (and X at midpoint)
                 */
                val x = 64f / 2

                /**
                 * centering in Y, we need to measure ascent/descent first
                 */
                val y = 64f / 2 - (mFontMetrics.ascent + mFontMetrics.descent) / 2
                drawText(
                    /* text = */ "${src.size / 2}",
                    /* x = */ x, /* y = */ y,
                    /* paint = */ mPaint
                )
            }
        }

        /**
         * We implement this to do our drawing. First we set our entire [Canvas] parameter [canvas]
         * to the color WHITE. Then we produce four different examples of the use of the method
         * [Matrix.setPolyToPoly] each wrapped between matching calls to the [canvas] methods
         * [Canvas.save] and [Canvas.restore]:
         *
         *  * translate (1 point) - we move the canvas to (10,10) then call our method [doDraw]
         *  with one point, which produces and uses a transformation matrix which moves the
         *  point (0,0) to (5,5)
         *
         *  * rotate/uniform-scale (2 points) - we move the canvas to (160,10) then call our method
         *  [doDraw] with two points, which produces and uses a transformation matrix which
         *  maps (32,32) to (32,32) (the center does not move) and (64,32) to (64,48) thus rotating
         *  the drawing and enlarging it slightly.
         *
         *  * rotate/skew (3 points) - we move the canvas to (10,110) then call our method [doDraw]
         *  with three points, which produces and uses a transformation matrix which maps (0,0)
         *  to (0,0), (64,0) to (96,0) (top line stretched), and (0,64) to (24,64) (bottom line
         *  moved to right).
         *
         *  * perspective (4 points) - we move the canvas to (160,110) then call our method [doDraw]
         *  with four points, which produces and uses a transformation matrix which maps (0,0) to
         *  (0,0), (64,0) to (96,0) (top line stretched), (64,64) to (64,96) (right bottom corner
         *  moved down), and (0,64) to 0,64).
         *
         * @param canvas the [Canvas] on which the background will be drawn
         */
        @SuppressLint("DrawAllocation")
        override fun onDraw(canvas: Canvas) {
            canvas.drawColor(Color.WHITE)
            canvas.translate(0f, dpToPixel(160, context).toFloat())
            canvas.withTranslation(x = 10f, y = 10f) {
                // translate (1 point)
                doDraw(canvas = this, src = floatArrayOf(0f, 0f), dst = floatArrayOf(5f, 5f))
            }
            canvas.withTranslation(x = 160f, y = 10f) {
                // rotate/uniform-scale (2 points)
                doDraw(
                    canvas = this,
                    src = floatArrayOf(32f, 32f, 64f, 32f),
                    dst = floatArrayOf(32f, 32f, 64f, 48f)
                )
            }
            canvas.withTranslation(x = 10f, y = 110f) {
                // rotate/skew (3 points)
                doDraw(
                    canvas = this,
                    src = floatArrayOf(0f, 0f, 64f, 0f, 0f, 64f),
                    dst = floatArrayOf(0f, 0f, 96f, 0f, 24f, 64f)
                )
            }
            canvas.withTranslation(x = 160f, y = 110f) {
                // perspective (4 points)
                doDraw(
                    canvas = this,
                    src = floatArrayOf(0f, 0f, 64f, 0f, 64f, 64f, 0f, 64f),
                    dst = floatArrayOf(0f, 0f, 96f, 0f, 64f, 96f, 0f, 64f)
                )
            }
        }

        /**
         * This method converts dp unit to equivalent pixels, depending on device density. First we
         * fetch a [Resources] instance for `val resources`, then we fetch the current display
         * metrics that are in effect for this resource object to [DisplayMetrics] `val metrics`.
         * Finally we return our [dp] parameter multiplied by the the screen density expressed as
         * dots-per-inch, divided by the reference density used throughout the system.
         *
         * @param dp      A value in dp (density independent pixels) unit which we need to convert
         *                into pixels
         * @param context [Context] to get resources and device specific display metrics
         * @return An [Int] value to represent px equivalent to dp depending on device density
         */
        @Suppress("SameParameterValue")
        private fun dpToPixel(dp: Int, context: Context): Int {
            val resources: Resources = context.resources
            val metrics = resources.displayMetrics
            return dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
        }

        /**
         * The init block of our constructor. First we set the stroke width of `Paint` field `mPaint`
         * to 4, its text size to 40, and its text alignment to CENTER. Finally we initialize our
         * `Paint.FontMetrics` field `mFontMetrics` with a font metrics object filled with the
         * appropriate values given the text attributes of `mPaint`.
         */
        init {
            // for when the style is STROKE
            mPaint.strokeWidth = 4f
            // for when we draw text
            mPaint.textSize = 40f
            mPaint.textAlign = Paint.Align.CENTER
            mFontMetrics = mPaint.fontMetrics
        }
    }
}
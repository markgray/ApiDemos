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
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Path.FillType
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import kotlin.math.roundToInt

/**
 * Shows the effect of four different Path.FillType's on the same Path, which consists of two
 * intersecting circles: Path.FillType.WINDING, Path.FillType.EVEN_ODD, Path.FillType.INVERSE_WINDING,
 * and Path.FillType.INVERSE_EVEN_ODD.
 */
class PathFillTypes : GraphicsActivity() {
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
     * Sample view which draws two intersecting circles using 4 different `Path.FillType`.
     */
    private class SampleView(context: Context) : View(context) {
        /**
         * [Paint] used to draw all 4 examples.
         */
        private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        /**
         * [Path] used to draw our two circles for all 4 examples.
         */
        private val mPath: Path

        /**
         * Multiplier to convert dp units to pixels.
         */
        private val mPixelMultiplier: Float

        /**
         * Draws [Path] field [mPath] at different locations using different `Path.FillType` fill
         * types. First we save the current matrix and clip of [Canvas] parameter [canvas] onto a
         * private stack. Then we pre-concatenate the current matrix with a translation matrix to
         * (x,y), set the clip rectangle of [canvas] to a 120 dp square, then set the entire canvas
         * to the color white. We set the fill type of [mPath] to our [FillType] argument [ft], and
         * instruct [canvas] to draw that [Path] using our [Paint] argument [paint] as the [Paint].
         * Finally we remove all modifications to the [Canvas] matrix/clip state since the save call
         * at the beginning of this method.
         *
         * @param canvas [Canvas] we are supposed to draw on
         * @param x      x coordinate to use for our top left corner
         * @param y      y coordinate to use for our top left corner
         * @param ft     [FillType] to use when we draw [Path] field [mPath]
         * @param paint  [Paint] to use when drawing.
         */
        private fun showPath(canvas: Canvas, x: Int, y: Int, ft: FillType, paint: Paint) {
            canvas.save()
            canvas.translate(x.toFloat(), y.toFloat())
            canvas.clipRect(0f, 0f, 120 * mPixelMultiplier, 120 * mPixelMultiplier)
            canvas.drawColor(Color.WHITE)
            mPath.fillType = ft
            canvas.drawPath(mPath, paint)
            canvas.restore()
        }

        /**
         * We implement this to do our drawing. First we make a copy of the [Paint] field [mPaint]
         * in `val paint`. Next we set the entire [Canvas] parameter [canvas] to a darkish gray, and
         * translate the canvas by 20 dp in both the x and y direction. We set the anti alias flag of
         * `paint` to true, and define the constant `val m160` to be the pixel equivalent of 160 dp.
         *
         * We call our method [showPath] to draw our [Path] field [mPath] using 4 different fill types:
         *
         *  * (0,0) - WINDING - Specifies that "inside" is computed by a non-zero sum of signed edge
         *  crossings. The inside of both circles are colored black.
         *
         *  * (160,0) - EVEN_ODD - Specifies that "inside" is computed by an odd number of edge
         *  crossings. The intersection of the two circles is left white, the rest of the circle is
         *  black.
         *
         *  * (0,160) - INVERSE_WINDING - Same as WINDING, but draws outside of the path, rather than
         *  inside. The inside of both cirlces is left white, and outside of the circles i painted
         *  black.
         *
         *  * (160,160) - INVERSE_EVEN_ODD - Same as EVEN_ODD, but draws outside of the path, rather
         *  than inside. The area outside of the circles and the intersection of the circles is
         *  painted black, the rest of the inside of the circles is left white.
         *
         * @param canvas the [Canvas] on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            val paint = mPaint
            canvas.drawColor(-0x333334)
            canvas.translate(20 * mPixelMultiplier, 20 * mPixelMultiplier)
            paint.isAntiAlias = true
            val m160 = (160 * mPixelMultiplier).roundToInt()
            showPath(canvas, 0, 0, FillType.WINDING, paint)
            showPath(canvas, m160, 0, FillType.EVEN_ODD, paint)
            showPath(canvas, 0, m160, FillType.INVERSE_WINDING, paint)
            showPath(canvas, m160, m160, FillType.INVERSE_EVEN_ODD, paint)
        }

        /**
         * The init block of our constructor. We set our window to be focusable, and focusable in
         * touch mode. We initialize our field `mPixelMultiplier` with the return value of our method
         * `convertDpToPixel` (a multiplier to convert dp units to pixels for devices with different
         * pixel densities). We allocate a new instance of `Path` for our `Path` field `mPath`, then
         * add two intersecting circles to it (both use counter clockwise direction to wind the
         * circle's contour).
         */
        init {
            isFocusable = true
            isFocusableInTouchMode = true
            mPixelMultiplier = convertDpToPixel(1f, context)
            mPath = Path()
            mPath.addCircle(40 * mPixelMultiplier, 40 * mPixelMultiplier, 45 * mPixelMultiplier, Path.Direction.CCW)
            mPath.addCircle(80 * mPixelMultiplier, 80 * mPixelMultiplier, 45 * mPixelMultiplier, Path.Direction.CCW)
        }
    }

    companion object {
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
         * @return A float value to represent px equivalent to dp depending on device density
         */
        fun convertDpToPixel(dp: Float, context: Context): Float {
            val resources: Resources = context.resources
            val metrics = resources.displayMetrics
            return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }
    }
}
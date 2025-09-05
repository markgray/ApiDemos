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
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.ComposePathEffect
import android.graphics.CornerPathEffect
import android.graphics.DiscretePathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathEffect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.ArcShape
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.PathShape
import android.graphics.drawable.shapes.RectShape
import android.graphics.drawable.shapes.RoundRectShape
import android.graphics.drawable.shapes.Shape
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View

/**
 * Drawing using the Drawable class methods, many of them usable in `<shape>` xml drawables.
 */
class ShapeDrawable1 : GraphicsActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to a new instance of our class `SampleView`.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(SampleView(this))
    }

    /**
     * Custom view which holds and draws our 7 [ShapeDrawable] examples.
     *
     * @param context the [Context] of the activity using us.
     * (See our `init` block for the details of our constructor)
     */
    private class SampleView(context: Context?) : View(context) {
        /**
         * Contains references to our 7 [ShapeDrawable] examples.
         */
        private val mDrawables: Array<ShapeDrawable?>

        /**
         * Custom [ShapeDrawable] which draws its shape twice, once using the [Paint]
         * passed to its [onDraw] method, and once using its private [Paint] (which is
         * configured to be a stroked style paint, and can be accessed and customized using the
         * getter method `getStrokePaint`)
         *
         * @param s the [Shape] we are to pass to our [ShapeDrawable] super.
         * (See our `init` block for the details of our constructor)
         */
        private class MyShapeDrawable(s: Shape?) : ShapeDrawable(s) {
            /**
             * Returns a reference to our private [Paint] field `mStrokePaint` so that the user
             * can modify (or use) it.
             *
             * @return reference to our private [Paint] field `mStrokePaint`.
             */
            /**
             * [Paint] used to draw the shape a second time in our [onDraw] method. It is
             * configured to use the STROKE style, and can be accessed and customized using the
             * getter method `getStrokePaint`)
             */
            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG)

            /**
             * Called from the drawable's draw() method after the canvas has been set to
             * draw the shape at (0,0). Subclasses can override for special effects such
             * as multiple layers, stroking, etc.
             *
             * First we instruct the [Shape] parameter [s] to draw itself on [Canvas] parameter [c]
             * using [Paint] parameter [p], then instruct the [s] to draw itself again on [c] using
             * our [Paint] field `mStrokePaint`
             *
             * @param s [Shape] we are to draw
             * @param c [Canvas] we are to draw our [Shape] parameter [s] to position (0,0)
             * @param p [Paint] we are supposed to use.
             */
            override fun onDraw(s: Shape, c: Canvas, p: Paint) {
                s.draw(c, p)
                s.draw(c, strokePaint)
            }

            /**
             * Our constructor. First we call through to our super's constructor, then we set the
             * style of our `Paint` field `mStrokePaint` to STROKE (Geometry and text drawn with
             * this style will be stroked, respecting the stroke-related fields on the paint)
             *
             * Parameter: `Shape` we are to draw (an `ArcShape` in our case)
             */
            init {
                strokePaint.style = Paint.Style.STROKE
            }
        }

        /**
         * We implement this to do our drawing. First we fetch the logical density of the display
         * from the current display metrics that are in effect for the resources associated with
         * this view into our [Float] variable `val scale` (this is a rough conversion factor which
         * can be used to convert dp measurements to pixels). We initialize [Int] variable `val x`
         * (the x coordinate to use for each [ShapeDrawable]) to 10 dp scaled by `scale` into pixels,
         * and [Int] variable `val y` (the y coordinate to use for the next [ShapeDrawable]) to
         * 10 dp scaled by `scale` into pixels. We initialize [Int] variable `val width` (the width
         * of our [ShapeDrawable]) to 300 dp scaled by `scale` into pixels, and [Int] variable
         * `val height` (the height of our [ShapeDrawable]) to 50 dp scaled by `scale` into pixels.
         *
         * Then for each of the [ShapeDrawable] `dr` in our [ShapeDrawable] array field [mDrawables]
         * we set the bounds of `dr` to `(x,y,x+width,y+height)` ((left,top,right,bottom) positions
         * of the bounding rectangle for `dr`). We instruct `dr` to draw itself on our [Canvas]
         * parameter [canvas] and then advance `y` by the `height` plus 5 dp for padding between
         * `Drawable`'s in order to get ready for the next pass through the loop.
         *
         * @param canvas the [Canvas] on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            val scale = resources.displayMetrics.density
            val x = (10 * scale).toInt()
            var y = (10 * scale).toInt()
            val width = (300 * scale).toInt()
            val height = (50 * scale).toInt()
            canvas.translate(0f, dpToPixel(160, context).toFloat())
            for (dr in mDrawables) {
                dr!!.setBounds(x, y, x + width, y + height)
                dr.draw(canvas)
                y += height + (5 * scale).toInt()
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
         * Our static methods
         */
        companion object {
            /**
             * Creates and returns a [SweepGradient] type of [Shader] that draws a sweep gradient
             * around a center point (150,25), with the colors red, green, blue, red.
             *
             * @return [Shader] that draws a sweep gradient around a center point (150,25), with the
             * colors red, green, blue, red.
             */
            private fun makeSweep(): Shader {
                return SweepGradient(
                    150f, 25f,
                    intArrayOf(-0x10000, -0xff0100, -0xffff01, -0x10000),
                    null
                )
            }

            /**
             * Creates and returns a [Shader] that draws a [LinearGradient] along the line (0,0) to
             * (50,50) using the colors red, green, and blue evenly distributed along the line, and
             * using the tile mode MIRROR (repeats the shader's image horizontally and vertically,
             * alternating mirror images so that adjacent images always seam)
             *
             * @return a [LinearGradient] type of [Shader] that draws a linear gradient along the
             * line (0,0) to (50,50) using the colors red, green, and blue evenly distributed along
             * the line, and using the tile mode MIRROR (repeats the shader's image horizontally and
             * vertically, alternating mirror images so that adjacent images always seam)
             */
            private fun makeLinear(): Shader {
                return LinearGradient(
                    0f, 0f, 50f, 50f,
                    intArrayOf(-0x10000, -0xff0100, -0xffff01),
                    null, Shader.TileMode.MIRROR
                )
            }

            /**
             * Creates and returns a [BitmapShader] type [Shader] which "tiles" a shape using a
             * [Bitmap]. First we allocate and initialize an [Int] array `val pixels` c containing
             * the four colors Red, Green, Blue, and Black. Then we create the 2 by 2 [Bitmap]
             * `val bm` from `pixels` using the type ARGB_8888. Finally we return a [BitmapShader]
             * constructed from `bm` with a tile mode of REPEAT for both x and y directions.
             *
             * @return a `BitmapShader` `Shader` which "tiles" a shape using a `Bitmap`
             */
            private fun makeTiling(): Shader {
                val pixels = intArrayOf(-0x10000, -0xff0100, -0xffff01, 0)
                val bm = Bitmap.createBitmap(pixels, 2, 2, Bitmap.Config.ARGB_8888)
                return BitmapShader(bm, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
            }
        }

        /**
         * Constructor for our `SampleView`. First we call through to our super's constructor,
         * and then we enable our `View` to receive focus. Then we allocate and initialize 3
         * arrays we use later for defining `RoundRectShape`'s:
         *
         *  * `float[] outerR` is an array  of 8 radius values, for the outer round rect.
         *  The first two floats are for the top-left corner (remaining pairs correspond
         *  clockwise).
         *
         *  * `RectF inset` A RectF that specifies the distance from the inner rect to
         *  each side of the outer rect. For no inner, we pass null.
         *
         *  * `float[] innerR` An array of 8 radius values, for the inner round rect. The
         *  first two floats are for the top-left corner (remaining pairs correspond clockwise).
         *  For no rounded corners on the inner rectangle, we pass null. If inset parameter is
         *  null, this parameter is ignored.
         *
         * Next we define `Path` variable `val path` to be a diamond shaped rectangle which we will
         * later use for the `PathShape` used for `mDrawables[5]`. We initialize our field
         * `ShapeDrawable[] field `mDrawables` by allocating 7 `ShapeDrawable`'s. Then we initialize
         * each of these:
         *
         *  * `mDrawables[0]` is a new instance of `RectShape`, we will later set the
         *  color of its `Paint` to Red.
         *
         *  * `mDrawables[1]` is a new instance of `OvalShape`, we will later set the
         *  color of its `Paint` to Blue.
         *
         *  * `mDrawables[2]` is a new instance of `RoundRectShape` defined using only
         *  `outerR` as the outer round rect, we will later set the color of its `Paint`
         *  to Green.
         *
         *  * `mDrawables[3]` is a new instance of `RoundRectShape` defined using
         *  `outerR` as the outer round rect, and `inset` as the `RectF`
         *  that specifies the distance from the inner rect to each side of the outer rect.
         *  We will later set its `Shader` to a `SweepGradient Shader` created by
         *  our method `makeSweep`.
         *
         *  * `mDrawables[4]` is a new instance of `RoundRectShape` defined using
         *  `outerR` as the outer round rect, `inset` as the `RectF` that
         *  specifies the distance from the inner rect to each side of the outer rect, and
         *  `innerR` as the inner round rect. We will later set its `Shader` to a
         *  `LinearGradient Shader` created by our method `makeLinear`
         *
         *  * `mDrawables[5]` is a `PathShape` using `Path path` as its `Path`
         *  and 100 for both the standard width and standard height. We will later set its
         *  `Shader` to a `BitmapShader Shader` created by our method `makeTiling`.
         *
         *  * `mDrawables[6]` is a custom `MyShapeDrawable` wrapping an `ArcShape`
         *  spanning 45 degrees to -270 degrees. We later set the color of its `Paint` to
         *  0x88FF8844 (A Brown), and set the stroke width of the private `Paint` used to
         *  draw the `ArcShape` a second time after the native `Paint` is used to 4.
         *
         * For `mDrawables[3]` we create `PathEffect pe` (a `DiscretePathEffect` which
         * chops the path into lines of 10, randomly deviating from the original path by 4) and
         * `PathEffect pe2` (a `CornerPathEffect` which transforms geometries that are
         * drawn (either STROKE or FILL styles) by replacing any sharp angles between line segments
         * into rounded angles of the specified radius 4). We then set the `PathEffect` of
         * `mDrawables[3]`'s `Paint` to a `PathEffect` constructed from `pe2`
         * and `pe` whose effect is to apply first `pe` and then `pe2` when drawing.
         *
         * Parameter: context `Context` to use for resources, "this" `ShapeDrawable1` activity
         * when called from `onCreate` in our case.
         */
        init {
            isFocusable = true
            /*
             * Outer round rectangle for {@code RoundRectShape}
             */
            val outerR = floatArrayOf(12f, 12f, 12f, 12f, 0f, 0f, 0f, 0f)
            /*
             * {@code RectF} that specifies the distance from the inner rect to each side of the
             * outer rect when used for {@code RoundRectShape}
             */
            val inset = RectF(6f, 6f, 6f, 6f)
            /*
             * An array of 8 radius values, for the inner round rectangle used for {@code RoundRectShape}
             */
            val innerR = floatArrayOf(12f, 12f, 0f, 0f, 12f, 12f, 0f, 0f)
            /*
             * Diamond shaped path used for the {@code PathShape} used for {@code mDrawables[5]}
             */
            val path = Path()
            path.moveTo(50f, 0f)
            path.lineTo(0f, 50f)
            path.lineTo(50f, 100f)
            path.lineTo(100f, 50f)
            path.close()
            mDrawables = arrayOfNulls(7)
            mDrawables[0] = ShapeDrawable(RectShape())
            mDrawables[1] = ShapeDrawable(OvalShape())
            mDrawables[2] = ShapeDrawable(RoundRectShape(outerR, null, null))
            mDrawables[3] = ShapeDrawable(RoundRectShape(outerR, inset, null))
            mDrawables[4] = ShapeDrawable(RoundRectShape(outerR, inset, innerR))
            mDrawables[5] = ShapeDrawable(PathShape(path, 100f, 100f))
            mDrawables[6] = MyShapeDrawable(ArcShape(45f, -270f))
            mDrawables[0]!!.paint.color = -0x10000
            mDrawables[1]!!.paint.color = -0xff0100
            mDrawables[2]!!.paint.color = -0xffff01
            mDrawables[3]!!.paint.shader = makeSweep()
            mDrawables[4]!!.paint.shader = makeLinear()
            mDrawables[5]!!.paint.shader = makeTiling()
            mDrawables[6]!!.paint.color = -0x770077bc
            val pe: PathEffect = DiscretePathEffect(10f, 4f)
            val pe2: PathEffect = CornerPathEffect(4f)
            mDrawables[3]!!.paint.pathEffect = ComposePathEffect(pe2, pe)
            val msd = mDrawables[6] as MyShapeDrawable?
            msd!!.strokePaint.strokeWidth = 4f
        }
    }
}
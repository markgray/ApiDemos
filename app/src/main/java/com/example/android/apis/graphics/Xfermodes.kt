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
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Xfermode
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.graphics.createBitmap

/**
 * Shows the results of using different Xfermode's when drawing an overlapping square and a circle
 * to the same Canvas. It was way too small on the newer devices (froyo OK), so I modified it to
 * scale for dpi.
 */
@SuppressLint("ObsoleteSdkInt")
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class Xfermodes : GraphicsActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to a new instance of [SampleView]
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(SampleView(this))
    }

    /**
     * Our demo custom view. Shows the result of using the 16 different PorterDuff modes with a circle
     * as the "dst" image, and a rectangle for the "src" image.
     *
     * @param context The [Context] of the activity that is using this view.
     * (See our `init` block for details of our constructor.)
     */
    private class SampleView(context: Context?) : View(context) {

        /**
         * [Bitmap] used for the "src" image (a rectangle)
         */
        private val mSrcB: Bitmap

        /**
         * [Bitmap] used for the "dst" image (a circle)
         */
        private val mDstB: Bitmap

        /**
         * background checker-board pattern
         */
        private val mBG: Shader

        /**
         * We implement this to do our drawing. First we set the entire [Canvas] parameter [canvas]
         * to the color [Color.WHITE], and scale [canvas] by SCREEN_DENSITY in both x and y directions.
         * We allocate [Paint] `val labelP` with its antialias flag set, and set its text alignment
         * to CENTER. We allocate [Paint] `val paint` and clear its FILTER_BITMAP_FLAG.
         *
         * We move the canvas to (15,35) and initialize [Int] `val x` and `val y` both to 0.
         * Now we loop through the 16 [Xfermode] array field [sModes] using `var i` as the index.
         * We set the style of `paint` to STROKE, set its [Shader] to null, and use it to draw a
         * rectangle outline for the current xfermode example. Next we set the style of `paint`
         * to FILL, set its [Shader] to our [Shader] field [mBG] (a [BitmapShader]) and draw a
         * checkerboard rect for the background of the example. We call the [Canvas.saveLayer]
         * method of [canvas] saving its save level in `val sc` which allocates and redirects
         * drawing to an offscreen bitmap with the bounds of a rectangle whose top left corner is
         * taken from the point (x,y) of [canvas], and whose bottom right corner is (x+W,y+H) (the
         * `paint` argument is null so no `Paint` will be applied when the offscreen bitmap is
         * copied back to the canvas when the matching [Canvas.restoreToCount] is called). We move
         * [canvas] to (x,y), and draw our [Bitmap] field [mDstB] to it at (0,0) using [Paint]
         * `paint`. We set the xfermode of `paint` to the [Xfermode] in the [Xfermode] array field
         * [sModes] currently indexed by `i`, then use `paint` to draw the [Bitmap] field [mSrcB]
         * to [canvas] at (0,0). We set the xfermode of `paint` back to null, and call the method
         * [Canvas.restoreToCount] of [canvas] to restore  the `sc` canvas state to that it held
         * before our `saveLayer` call and to copy the offscreen bitmap to the onscreen canvas.
         * Next we draw the label from [String] array field [sLabels] which describes the `i`'th
         * PorterDuff xfermode in the proper place. Finally we add `W+10` to `x` and if we have
         * reached the end of a row (as defined by [ROW_MAX]) we set `x` to 0 and add `H+30` to
         * `y`, then loop back for the next PorterDuff xfermode in [Xfermode] array field [sModes].
         *
         * @param canvas the [Canvas] on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            canvas.drawColor(Color.WHITE)
            canvas.scale(SCREEN_DENSITY, SCREEN_DENSITY)
            @SuppressLint("DrawAllocation")
            val labelP = Paint(Paint.ANTI_ALIAS_FLAG)
            labelP.textAlign = Paint.Align.CENTER
            @SuppressLint("DrawAllocation")
            val paint = Paint()
            paint.isFilterBitmap = false
            canvas.translate(15f, 35f)
            var x = 0
            var y = 0
            for (i in sModes.indices) {
                // draw the border
                paint.style = Paint.Style.STROKE
                paint.shader = null
                canvas.drawRect(
                    x - 0.5f,
                    y - 0.5f,
                    x + W + 0.5f,
                    y + H + 0.5f,
                    paint
                )

                // draw the checker-board pattern
                paint.style = Paint.Style.FILL
                paint.shader = mBG
                canvas.drawRect(
                    /* left = */ x.toFloat(),
                    /* top = */ y.toFloat(),
                    /* right = */ x + W.toFloat(),
                    /* bottom = */ y + H.toFloat(),
                    /* paint = */ paint
                )

                // draw the src/dst example into our offscreen bitmap
                val sc = canvas.saveLayer(
                    /* left = */ x.toFloat(),
                    /* top = */ y.toFloat(),
                    /* right = */ x + W.toFloat(),
                    /* bottom = */ y + H.toFloat(),
                    /* paint = */ null
                )
                canvas.translate(/* dx = */ x.toFloat(), /* dy = */ y.toFloat())
                canvas.drawBitmap(
                    /* bitmap = */ mDstB,
                    /* left = */ 0f,
                    /* top = */ 0f,
                    /* paint = */ paint
                )
                paint.xfermode = sModes[i]
                canvas.drawBitmap(
                    /* bitmap = */ mSrcB,
                    /* left = */ 0f,
                    /* top = */ 0f,
                    /* paint = */ paint
                )
                paint.xfermode = null
                canvas.restoreToCount(/* saveCount = */ sc)

                // draw the label
                canvas.drawText(
                    /* text = */ sLabels[i],
                    /* x = */ x + W / 2.toFloat(),
                    /* y = */ y - labelP.textSize / 2,
                    /* paint = */ labelP
                )
                x += W + 10

                // wrap around when we've drawn enough for one row
                if (i % ROW_MAX == ROW_MAX - 1) {
                    x = 0
                    y += H + 30
                }
            }
        }

        companion object {
            /**
             * Logical screen density, used to scale the `Canvas` of the view to correctly display
             * the demo on high density screens.
             */
            private var SCREEN_DENSITY: Float = 1f

            /**
             * Width used for each of the 16 example PorterDuff xfermode squares.
             */
            private const val W = 64

            /**
             * Height used for each of the 16 example PorterDuff xfermode squares.
             */
            private const val H = 64

            /**
             * number of samples per row
             */
            private const val ROW_MAX = 4

            /**
             * Array containing the 16 `PorterDuffXfermode` possibilities.
             */
            private val sModes = arrayOf<Xfermode>(
                PorterDuffXfermode(/* mode = */ PorterDuff.Mode.CLEAR),
                PorterDuffXfermode(/* mode = */ PorterDuff.Mode.SRC),
                PorterDuffXfermode(/* mode = */ PorterDuff.Mode.DST),
                PorterDuffXfermode(/* mode = */ PorterDuff.Mode.SRC_OVER),
                PorterDuffXfermode(/* mode = */ PorterDuff.Mode.DST_OVER),
                PorterDuffXfermode(/* mode = */ PorterDuff.Mode.SRC_IN),
                PorterDuffXfermode(/* mode = */ PorterDuff.Mode.DST_IN),
                PorterDuffXfermode(/* mode = */ PorterDuff.Mode.SRC_OUT),
                PorterDuffXfermode(/* mode = */ PorterDuff.Mode.DST_OUT),
                PorterDuffXfermode(/* mode = */ PorterDuff.Mode.SRC_ATOP),
                PorterDuffXfermode(/* mode = */ PorterDuff.Mode.DST_ATOP),
                PorterDuffXfermode(/* mode = */ PorterDuff.Mode.XOR),
                PorterDuffXfermode(/* mode = */ PorterDuff.Mode.DARKEN),
                PorterDuffXfermode(/* mode = */ PorterDuff.Mode.LIGHTEN),
                PorterDuffXfermode(/* mode = */ PorterDuff.Mode.MULTIPLY),
                PorterDuffXfermode(/* mode = */ PorterDuff.Mode.SCREEN)
            )

            /**
             * Labels for the 16 `PorterDuffXfermode` example composite images.
             */
            private val sLabels = arrayOf(
                "Clear", "Src", "Dst", "SrcOver",
                "DstOver", "SrcIn", "DstIn", "SrcOut",
                "DstOut", "SrcATop", "DstATop", "Xor",
                "Darken", "Lighten", "Multiply", "Screen"
            )
        }

        /**
         * The init block of our constructor. First we initialize `SCREEN_DENSITY` to the logical
         * density of the current display. We initialize `Bitmap` field `mSrcB` with a W by H
         * rectangle created by our method `makeSrc`, and `mDstB` with an W by H oval created by
         * our method `makeDst` (since W=H=64 they are actually a square and a circle respectively).
         * We create `Bitmap` `val bm` to be a 2 by 2 image using the colors white, gray, gray,
         * white for the four pixels. Then we initialize our `Shader` field `mBG` to be a
         * `BitmapShader` formed from `bm` using a tile mode of REPEAT for both the x and y axis.
         * We create `Matrix` `val m`, set it to scale by 6 (both x and y), then set the local
         * matrix of `Shader` `mBG` to it.
         */
        init {
            SCREEN_DENSITY = resources.displayMetrics.density
            mSrcB = makeSrc(w = W, h = H)
            mDstB = makeDst(w = W, h = H)

            // make a checkerboard pattern
            val bm = Bitmap.createBitmap(
                /* colors = */ intArrayOf(
                    0xFFFFFFFF.toInt(),
                    0xFFCCCCCC.toInt(),
                    0xFFCCCCCC.toInt(),
                    0xFFFFFFFF.toInt()
                ),
                /* width = */ 2,
                /* height = */ 2,
                /* config = */ Bitmap.Config.RGB_565
            )
            mBG = BitmapShader(
                /* bitmap = */ bm,
                /* tileX = */ Shader.TileMode.REPEAT,
                /* tileY = */ Shader.TileMode.REPEAT
            )
            val m = Matrix()
            m.setScale(/* sx = */ 6f, /* sy = */ 6f)
            mBG.setLocalMatrix(/* localM = */ m)
        }
    }

    companion object {
        /**
         * Create a [Bitmap] with a circle, used for the "dst" image. First we create [Bitmap]
         * `val bm` to be `w` by `h` in size and using the ARGB_8888 configuration. Then we create
         * [Canvas] `val c` to draw into [Bitmap] `bm`. We create [Paint] `val p` with its antialias
         * flag set, and set its color to 0xFFFFCC44 (a yellowish orange). Then we use `p` to draw
         * an oval on [Canvas] `c` with a [RectF] defining its size to be `w*3/4` by `h*3/4`.
         * Finally we return [Bitmap] `bm` which now contains this oval to the caller.
         *
         * @param w width of the [Bitmap]
         * @param h height of the [Bitmap]
         * @return [Bitmap] containing a circle
         */
        fun makeDst(w: Int, h: Int): Bitmap {
            val bm = createBitmap(width = w, height = h)
            val c = Canvas(/* bitmap = */ bm)
            val p = Paint(/* flags = */ Paint.ANTI_ALIAS_FLAG)
            p.color = 0xFFFFCC44.toInt()
            c.drawOval(
                /* oval = */ RectF(
                    0f,
                    0f,
                    (w * 3 / 4).toFloat(),
                    (h * 3 / 4).toFloat()
                ),
                /* paint = */ p
            )
            return bm
        }

        /**
         * Create a [Bitmap] with a rect, used for the "src" image. First we create [Bitmap]
         * `val bm` to be `w` by `h` in size and using the ARGB_8888 configuration. Then we
         * create [Canvas] `val c` to draw into `bm`. We create [Paint] `val p` with its antialias
         * flag set, and set its color to 0xFF66AAFF (a light blue). Then we use `p` to draw a
         * rectangle on `c` with its top left corner at (w/3,h/3), and its bottom right corner at
         * (w*19/20,h*19/20). Finally we return [Bitmap] `bm` which now contains this rect to the
         * caller.
         *
         * @param w width of the [Bitmap]
         * @param h height of the [Bitmap]
         * @return [Bitmap] containing a rect.
         */
        fun makeSrc(w: Int, h: Int): Bitmap {
            val bm = createBitmap(width = w, height = h)
            val c = Canvas(/* bitmap = */ bm)
            val p = Paint(/* flags = */ Paint.ANTI_ALIAS_FLAG)
            p.color = 0xFF66AAFF.toInt()
            c.drawRect(
                /* left = */ w / 3.toFloat(),
                /* top = */ h / 3.toFloat(),
                /* right = */ w * 19 / 20.toFloat(),
                /* bottom = */ h * 19 / 20.toFloat(),
                /* paint = */ p
            )
            return bm
        }
    }
}
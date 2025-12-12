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
@file:Suppress("ReplaceNotNullAssertionWithElvisReturn")

package com.example.android.apis.graphics

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.core.graphics.createBitmap
import com.example.android.apis.graphics.CreateBitmap.Companion.SCREEN_DENSITY
import com.example.android.apis.graphics.CreateBitmap.SampleView.Companion.codec
import java.io.ByteArrayOutputStream

/**
 * Shows how to create bitmaps programmatically, how to compress them to JPG and PNG formats, and
 * shows how JPG looses information while PNG is loss-less.
 */
class CreateBitmap : GraphicsActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, and initialize our static variable [SCREEN_DENSITY] to the logical screen density
     * of our display. Then we set our content view to a new instance of [SampleView].
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SCREEN_DENSITY = resources.displayMetrics.density
        setContentView(SampleView(this))
    }

    /**
     * Custom View which creates and then draws an array of colors using different techniques.
     * (See our init block for our constructor details)
     *
     * @param context `Context` to use for resources, "this" `Activity` when called from `onCreate`
     * in our case.
     */
    private class SampleView(context: Context?) : View(context) {
        /**
         * Six [Bitmap]'s created from array of colors, the first three using [Bitmap.createBitmap]
         * directly for formats ARGB_8888, RGB_565, and ARGB_4444, the second three using
         * [Bitmap.setPixels].
         */
        private val mBitmaps: Array<Bitmap?>

        /**
         * [Bitmap]'s created from [mBitmaps] six [Bitmap]'s using our method [codec] to first
         * encode and compress our original [Bitmap] using [Bitmap.CompressFormat.JPEG],
         * then decoding it into [Bitmap] format again.
         */
        private val mJPEG: Array<Bitmap?>

        /**
         * [Bitmap]'s created from [mBitmaps] six [Bitmap]'s using our method [codec] to first
         * encode and compress our original [Bitmap] using [Bitmap.CompressFormat.PNG], then
         * decoding it into [Bitmap] format again.
         */
        private val mPNG: Array<Bitmap?>

        /**
         * The array of colors that we use to create our [Bitmap]'s from
         */
        private val mColors: IntArray

        /**
         * [Paint] instance we use to draw our [Bitmap]'s in our [onDraw] override
         */
        private val mPaint: Paint

        /**
         * We implement this to do our drawing. First we set the color of the entire [Canvas]
         * parameter [canvas] to WHITE, and scale [canvas] by the SCREEN_DENSITY of our display.
         * Then for each of the 6 [Bitmap]'s in our three arrays we first draw the [Bitmap] in
         * [mBitmaps], then the [Bitmap] in [mJPEG]  and the [Bitmap] in [mPNG] in a row, translate
         * the [Canvas] parameter [canvas] down by the height of the [Bitmap] in [mBitmaps] sub `i`
         * to get ready to draw the next row of the six.
         *
         * When done with the 18 [Bitmap]'s we created in our constructor, we next draw our
         * color array directly, w/o creating a bitmap object, first specifying that the array of
         * colors contains an alpha channel, but without using a [Paint] object, then specifying
         * that there is no alpha channel and using the [Paint] field [mPaint] as the [Paint].
         *
         * @param canvas the [Canvas] on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            canvas.drawColor(Color.WHITE)
            canvas.scale(SCREEN_DENSITY, SCREEN_DENSITY)
            canvas.translate(0f, 240f)
            for (i in mBitmaps.indices) {
                canvas.drawBitmap(mBitmaps[i]!!, 0f, 0f, null)
                canvas.drawBitmap(mJPEG[i]!!, 80f, 0f, null)
                canvas.drawBitmap(mPNG[i]!!, 160f, 0f, null)
                canvas.translate(0f, mBitmaps[i]!!.height.toFloat())
            }
            // draw the color array directly, w/o creating a bitmap object
            @Suppress("DEPRECATION")
            canvas.drawBitmap(
                mColors,
                0, STRIDE,
                0, 0,
                WIDTH, HEIGHT,
                true, null
            )
            canvas.translate(0f, HEIGHT.toFloat())
            @Suppress("DEPRECATION")
            canvas.drawBitmap(
                mColors,
                0, STRIDE,
                0, 0,
                WIDTH, HEIGHT,
                false, mPaint
            )
        }

        /**
         * Our static method.
         */
        companion object {
            /**
             * Compresses its input [Bitmap] parameter [src] using the [Bitmap.CompressFormat]
             * compression codec in the [format] parmeter (JPEG, or PNG in our case), then decoding
             * that compressed [Bitmap] into a new [Bitmap] which it returns.
             *
             * First we create a [ByteArrayOutputStream] for our variable `val os`, then we write a
             * compressed version of our [Bitmap] parameter [src] to it, compressed using the
             * [Bitmap.CompressFormat] in our parameter [format], and using an [Int] quality setting
             * of our parameter [quality]. Then we convert `os` to a byte array to initialize our
             * variable `val array`. Finally we return the [Bitmap] created by decoding the byte
             * array `array` to the caller.
             *
             * @param src     [Bitmap] we want to compress, then decode again into a new [Bitmap]
             * @param format  compression format we want to use
             * @param quality Hint to the compressor, 0-100. 0 meaning compress for small size, 100
             * meaning compress for max quality. Some formats, like PNG which is loss less, will
             * ignore the quality setting
             * @return New [Bitmap] created from [Bitmap] parameter [src] after first compressing
             * then decoding it.
             */
            private fun codec(src: Bitmap?, format: CompressFormat, quality: Int): Bitmap {
                val os = ByteArrayOutputStream()
                src!!.compress(format, quality, os)
                val array = os.toByteArray()
                return BitmapFactory.decodeByteArray(array, 0, array.size)
            }
        }

        /**
         * Constructor for our `SampleView` instance. First we call through to our super's
         * constructor, and enable our View to receive focus.
         *
         * We initialize our `IntArray` field `mColors` using our method `createColors`, and
         * then for some reason which escapes me, we make a local copy of `mColors` in
         * `val colors`.
         *
         * We allocate 6 `Bitmap`'s for our field `Bitmap[]` field `mBitmaps`, then fill the first
         * three of these by directly initializing ARGB_8888, RGB_565, and ARGB_4444 `Bitmap`'s
         * using `colors`. The second three we create empty `Bitmap`'s of the same types, then
         * use `Bitmap.setPixels` to set their pixels to `colors`.
         *
         * We initialize our `Paint` field `mPaint` with an instance of `Paint` and set its
         * dither flag to true.
         *
         * Next we allocate 6 `Bitmap`'s for our field `Bitmap[]` field `mJPEG` and our `Bitmap[]`
         * field `mPNG` and fill each of them with `Bitmap`'s created from their corresponding
         * `Bitmap[]` field `mBitmaps` by our method `codec` using JPEG compression for the `mJPEG`
         * `Bitmap` and PNG compression for the `mPNG` `Bitmap`.
         *
         * Parameter: `Context` to use for resources, "this" `Activity` when called
         * from `onCreate` in our case.
         */
        init {
            isFocusable = true
            mColors = createColors()
            val colors = mColors
            mBitmaps = arrayOfNulls(6)
            // these three are initialized with colors[]
            mBitmaps[0] = Bitmap.createBitmap(
                colors, 0,
                STRIDE, WIDTH, HEIGHT,
                Bitmap.Config.ARGB_8888
            )
            mBitmaps[1] = Bitmap.createBitmap(
                colors,
                0, STRIDE,
                WIDTH, HEIGHT,
                Bitmap.Config.RGB_565
            )
            @Suppress("DEPRECATION")
            mBitmaps[2] = Bitmap.createBitmap(
                colors,
                0, STRIDE,
                WIDTH, HEIGHT,
                Bitmap.Config.ARGB_4444
            )
            // these three will have their colors set later
            mBitmaps[3] = createBitmap(WIDTH, HEIGHT)
            mBitmaps[4] = createBitmap(WIDTH, HEIGHT, Bitmap.Config.RGB_565)
            @Suppress("DEPRECATION")
            mBitmaps[5] = createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_4444)
            for (i in 3..5) {
                mBitmaps[i]!!.setPixels(colors, 0, STRIDE, 0, 0, WIDTH, HEIGHT)
            }
            mPaint = Paint()
            mPaint.isDither = true
            // now encode/decode using JPEG and PNG
            mJPEG = arrayOfNulls(mBitmaps.size)
            mPNG = arrayOfNulls(mBitmaps.size)
            for (i in mBitmaps.indices) {
                mJPEG[i] = codec(mBitmaps[i], CompressFormat.JPEG, 80)
                mPNG[i] = codec(mBitmaps[i], CompressFormat.PNG, 0)
            }
        }
    }

    /**
     * Our static constants and methods.
     */
    companion object {
        /**
         * Logical screen density, used to scale the [Canvas] of the view to correctly display
         * the demo on high density screens.
         */
        var SCREEN_DENSITY: Float = 0f

        /**
         * Width of the [Bitmap]'s being created and displayed
         */
        private const val WIDTH = 50

        /**
         * Height of the [Bitmap]'s being created and displayed
         */
        private const val HEIGHT = 50

        /**
         * Number of colors in the array between rows (must be >= width or <= -width). Colors are
         * pulled from the array in order until WIDTH pixels have been assigned, then we skip the
         * rest of STRIDE to start the next row of pixels.
         */
        private const val STRIDE = 64

        /**
         * Creates an [IntArray] of colors designed to be fed to [Bitmap.createBitmap] or
         * [Bitmap.setPixels].
         *
         * First we allocate [IntArray] variable `val colors` to hold STRIDE*HEIGHT colors. Then for
         * the number of rows HEIGHT, we move column to column calculating a 32 bit ARGB color for
         * each value in `colors`, using STRIDE to skip to the next row when WIDTH columns have been
         * calculated. The color assigned is a function of the row and column position designed to
         * produce a pleasing color ramp (from the looks of it) and we will leave it at that.
         *
         * @return an [IntArray] array of colors filled with enough colors to specify a WIDTH*HEIGHT
         * [Bitmap] with a stride of STRIDE between rows (ie. only the first WIDTH of the STRIDE per
         * row colors are defined, after which the rest of STRIDE is skipped to move to the next row.)
         */
        private fun createColors(): IntArray {
            val colors = IntArray(STRIDE * HEIGHT)
            for (y in 0 until HEIGHT) {
                for (x in 0 until WIDTH) {
                    val r = x * 255 / (WIDTH - 1)
                    val g = y * 255 / (HEIGHT - 1)
                    val b = 255 - r.coerceAtMost(g)
                    val a = r.coerceAtLeast(g)
                    colors[y * STRIDE + x] = a shl 24 or (r shl 16) or (g shl 8) or b
                }
            }
            return colors
        }
    }
}
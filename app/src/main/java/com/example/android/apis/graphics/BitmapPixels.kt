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
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import java.nio.IntBuffer
import java.nio.ShortBuffer
import androidx.core.graphics.createBitmap

/**
 * Supposed to show Bitmap.Config.ARGB_8888, Bitmap.Config.RGB_565, and Bitmap.Config.ARGB_4444
 * configurations of the same color ramp, but ever since KitKat Bitmap.createBitmap will return
 * a Bitmap.Config.ARGB_8888 bitmap instead of Bitmap.Config.ARGB_4444 bitmap, so the call to
 * mBitmap3.copyPixelsFromBuffer(makeBuffer(data4444, N)); on line 147 will crash Lollipop.
 * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) kludge to make it work on newer versions.
 */
class BitmapPixels : GraphicsActivity() {
    /**
     * Called when the activity is starting. We call through to our super's implementation of
     * `onCreate`, then set our content view to a new instance of [SampleView].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(SampleView(this))
    }

    /**
     * Custom [View] subclass which displays three [Bitmap]'s displaying the same color ramp using
     * different values for their [Bitmap.Config]
     *
     * @param context the [Context] to use to retrieve resources
     */
    @SuppressLint("ObsoleteSdkInt")
    private class SampleView(context: Context?) : View(context) {
        /**
         * Color ramp [Bitmap] using [Bitmap.Config.ARGB_8888] as its resolution
         */
        private val mBitmap1: Bitmap

        /**
         * Color ramp [Bitmap] using [Bitmap.Config.RGB_565] as its resolution
         */
        private val mBitmap2: Bitmap

        /**
         * Color ramp [Bitmap] using [Bitmap.Config.ARGB_4444] as its resolution (since KITKAT this
         * resolution does not work, so we use [Bitmap.Config.RGB_565] again instead).
         */
        private var mBitmap3: Bitmap? = null

        /**
         * We implement this to do our drawing. First we set our entire [Canvas] parameter [canvas]
         * to 0xFFCCCCCC (a darkish gray). We initialize the `y` coordinate we use to draw our bitmaps
         * to 10, then draw [Bitmap] field [mBitmap1] on [canvas] at (10,y), increment `y` by 10 and
         * draw [Bitmap] field [mBitmap2] at (10,y), and increment `y` by 10 and draw [Bitmap] field
         * [mBitmap3] at (10,y).
         *
         * @param canvas the [Canvas] on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            canvas.drawColor(-0x333334)
            canvas.translate(0f, 240f)
            var y = 10
            canvas.drawBitmap(mBitmap1, 10f, y.toFloat(), null)
            y += mBitmap1.height + 10
            canvas.drawBitmap(mBitmap2, 10f, y.toFloat(), null)
            y += mBitmap2.height + 10
            canvas.drawBitmap(mBitmap3!!, 10f, y.toFloat(), null)
        }

        /**
         * Our static methods
         */
        companion object {
            /**
             * access the red component from a pre-multiplied color
             *
             * @param c 32 bit color
             * @return 8 bit red component of [c]
             */
            private fun getR32(c: Int): Int {
                return c shr 0 and 0xFF
            }

            /**
             * access the green component from a pre-multiplied color
             *
             * @param c 32 bit color
             * @return 8 bit green component of [c]
             */
            private fun getG32(c: Int): Int {
                return c shr 8 and 0xFF
            }

            /**
             * access the blue component from a pre-multiplied color
             *
             * @param c 32 bit color
             * @return 8 bit blue component of [c]
             */
            private fun getB32(c: Int): Int {
                return c shr 16 and 0xFF
            }

            /**
             * access the alpha component from a pre-multiplied color
             *
             * @param c 32 bit color
             * @return 8 bit alpha component of [c]
             */
            private fun getA32(c: Int): Int {
                return c shr 24 and 0xFF
            }

            /**
             * This takes components and packs them into an int in the correct device order.
             *
             * @param r red component
             * @param g green component
             * @param b blue component
             * @param a alpha component
             * @return 32 bit ARGB_8888 color
             */
            private fun pack8888(r: Int, g: Int, b: Int, a: Int): Int {
                return r shl 0 or (g shl 8) or (b shl 16) or (a shl 24)
            }

            /**
             * This packs the three color components into a 16 bit RGB_565 color.
             *
             * @param r red component
             * @param g green component
             * @param b blue component
             * @return 16 bit RGB_565 packed color
             */
            private fun pack565(r: Int, g: Int, b: Int): Short {
                return (r shl 11 or (g shl 5) or (b shl 0)).toShort()
            }

            /**
             * This packs the three color components and alpha component into a 16 bit RGB_4444 color.
             *
             * @param r red component
             * @param g green component
             * @param b blue component
             * @return 16 bit RGB_4444 packed color
             */
            private fun pack4444(r: Int, g: Int, b: Int, a: Int): Short {
                return (a shl 0 or (b shl 4) or (g shl 8) or (r shl 12)).toShort()
            }

            /**
             * Scales the color [c] by the alpha [a]. Rather unnecessary in our case since
             * [a] is always 255, and [c] is either 0 or 255, but what the hay.
             *
             * @param c color value (0-255)
             * @param a alpha value (0-255)
             * @return color value scaled by alpha (pre-multiplied if you would)
             */
            private fun mul255(c: Int, a: Int): Int {
                val prod = c * a + 128
                return prod + (prod shr 8) shr 8
            }

            /**
             * Turn a color int into a pre-multiplied device color. Does nothing in our case since
             * it is only called with the values Color.RED, and Color.GREEN where the alpha is 255
             * and the red and green color components are 255 as well, but what the hay.
             *
             * @param c color value
             * @return color with each color component pre-multiplied by the alpha value
             */
            private fun premultiplyColor(c: Int): Int {
                var r = Color.red(c)
                var g = Color.green(c)
                var b = Color.blue(c)
                val a = Color.alpha(c)
                /**
                 * now apply the alpha to r, g, b
                 */
                r = mul255(r, a)
                g = mul255(g, a)
                b = mul255(b, a)
                /**
                 * now pack it in the correct order
                 */
                return pack8888(r, g, b, a)
            }

            /**
             * Produces arrays containing smooth color transitions from a starting color to an ending
             * color for the three different color formats: ARGB_8888, RGB_565, and ARGB_4444. First
             * we extract the four components `r, g, b, and a` of the [from] color and multiply them
             * by 2**23, we do the same for the components of the [to] color, subtract the [from]
             * component and divide by `n-1` to create the "color steps" to use: `dr, dg, db, and da`.
             *
             * Then we loop for the [n] colors in our ramps, filling our output arrays [ramp8888],
             * [ramp565] and [ramp4444] with the ARGB_8888, RGB_565, and ARGB_4444 color format
             * valuea produced using our methods [pack8888], [pack565], and [pack4444] from the
             * current values of `r, g, b, and a`, rounded and normalized by dividing by 2**23. We
             * then advance `r, g, b, and a` by `dr, dg, db, and da` to get ready for the next pass
             * through the loop.
             *
             * @param from     start color of the ramp
             * @param to       end color of the ramp
             * @param n        number of colors in the ramp
             * @param ramp8888 ARGB_8888 color format ramp
             * @param ramp565  RGB_565 color format ramp
             * @param ramp4444 ARGB_4444 color format ramp
             */
            @Suppress("SameParameterValue")
            private fun makeRamp(
                from: Int, to: Int, n: Int,
                ramp8888: IntArray, ramp565: ShortArray,
                ramp4444: ShortArray
            ) {
                var r = getR32(from) shl 23
                var g = getG32(from) shl 23
                var b = getB32(from) shl 23
                var a = getA32(from) shl 23
                // now compute our step amounts per component (biased by 23 bits)
                val dr = ((getR32(to) shl 23) - r) / (n - 1)
                val dg = ((getG32(to) shl 23) - g) / (n - 1)
                val db = ((getB32(to) shl 23) - b) / (n - 1)
                val da = ((getA32(to) shl 23) - a) / (n - 1)
                for (i in 0 until n) {
                    ramp8888[i] = pack8888(r shr 23, g shr 23, b shr 23, a shr 23)
                    ramp565[i] = pack565(r shr 23 + 3, g shr 23 + 2, b shr 23 + 3)
                    ramp4444[i] = pack4444(r shr 23 + 4, g shr 23 + 4, b shr 23 + 4, a shr 23 + 4)
                    r += dr
                    g += dg
                    b += db
                    a += da
                }
            }

            /**
             * Creates and returns an [IntBuffer] consisting of [n] copies of its [IntArray]
             * parameter [src] (in our case, [n] rows of our ARGB_8888 color ramp). First we
             * allocate an [IntBuffer] for `val dst` with a capacity of [n] by [n]. Then we loop
             * for [n] rows "bulk putting" our parameter [src]. We rewind `dst` and return it to
             * the caller.
             *
             * @param src array of ARGB_8888 colors to make buffer from
             * @param n   Number of colors in the [src] array, and number of times to write that
             * array to the [IntBuffer] we return
             * @return an [IntBuffer] consisting of [n] copies of our parameter [src].
             */
            @Suppress("SameParameterValue")
            private fun makeBuffer(src: IntArray, n: Int): IntBuffer {
                val dst = IntBuffer.allocate(n * n)
                @Suppress("unused")
                for (i in 0 until n) {
                    dst.put(src)
                }
                dst.rewind()
                return dst
            }

            /**
             * Creates and returns a [ShortBuffer] consisting of [n] copies of its [ShortArray]
             * parameter [src] (in our case, [n] rows of our RGB_565 or ARGB_4444 color ramps).
             * First we allocate a [ShortBuffer] for `val dst` with a capacity of [n] times [n].
             * Then we loop for [n] rows "bulk putting" our parameter [src]. We rewind `dst` and
             * return it to the caller.
             *
             * @param src [ShortArray] of RGB_565 or ARGB_4444 colors to make buffer from
             * @param n   Number of colors in the [src] array, and number of times to write that
             * array to the [ShortBuffer] we return
             * @return a [ShortBuffer] consisting of [n] copies of our parameter [src].
             */
            @Suppress("SameParameterValue")
            private fun makeBuffer(src: ShortArray, n: Int): ShortBuffer {
                val dst = ShortBuffer.allocate(n * n)
                @Suppress("unused")
                for (i in 0 until n) {
                    dst.put(src)
                }
                dst.rewind()
                return dst
            }
        }

        /**
         * Constructs an instance of our custom View. First we call through to our super's constructor,
         * then we enable our view to receive focus. We initialize `Int` variable `val n` to be 100,
         * and allocate `n` entries for each of our color ramp arrays: `Int` array `val data8888`,
         * `short` array `val data565` and `short` array `val data4444`, then we call our method
         * `makeRamp` to produce smooth color ramps from RED to GREEN in them. We initialize our
         * `Bitmap` field `mBitmap1` with an `n by n` ARGB_8888 `Bitmap`, `Bitmap` field `mBitmap2`
         * with an `n by n` RGB_565 `Bitmap`, and `Bitmap` field `mBitmap3` with an `n by n` ARGB_4444
         * `Bitmap` for versions before KITKAT, and RGB_565 for KITKAT and later versions.
         *
         * Then we fill `mBitmap1` with a `Bitmap` created from the `IntBuffer` created by our method
         * `makeBuffer` from the `data8888` color ramp, `mBitmap2` with a `Bitmap` created from the
         * `ShortBuffer` created by our method `makeBuffer` from the `data565` color ramp, and for
         * versions KITKAT or newer `mBitmap3` with a `Bitmap` created from the `ShortBuffer` created
         * by our method `makeBuffer` from the `data565` color ramp, and for versions before KITKAT
         * we use the `data4444` color ramp.
         *
         * Parameter: `Context` to use to retrieve resources.
         */
        init {
            isFocusable = true
            val n = 100
            val data8888 = IntArray(n)
            val data565 = ShortArray(n)
            val data4444 = ShortArray(n)
            makeRamp(
                premultiplyColor(Color.RED),
                premultiplyColor(Color.GREEN),
                n, data8888, data565, data4444
            )
            mBitmap1 = createBitmap(n, n)
            mBitmap2 = createBitmap(n, n, Bitmap.Config.RGB_565)
            mBitmap3 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                createBitmap(n, n, Bitmap.Config.RGB_565)
            } else {
                @Suppress("DEPRECATION") // this works for SDK less than KITKAT
                createBitmap(n, n, Bitmap.Config.ARGB_4444)
            }
            mBitmap1.copyPixelsFromBuffer(makeBuffer(data8888, n))
            mBitmap2.copyPixelsFromBuffer(makeBuffer(data565, n))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mBitmap3!!.copyPixelsFromBuffer(makeBuffer(data565, n))
            } else {
                mBitmap3!!.copyPixelsFromBuffer(makeBuffer(data4444, n))
            }
        }
    }
}
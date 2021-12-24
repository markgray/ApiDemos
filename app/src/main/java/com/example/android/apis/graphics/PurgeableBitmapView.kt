/*
 * Copyright (C) 2009 The Android Open Source Project
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
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.example.android.apis.graphics.PurgeableBitmap.RefreshHandler
import java.io.ByteArrayOutputStream

/**
 * PurgeableBitmapView works with PurgeableBitmap to demonstrate the effects of setting
 * Bitmaps as being purgeable.
 *
 *
 * PurgeableBitmapView decodes an encoded bitstream to a Bitmap each time update()
 * is invoked(), and its onDraw() draws the Bitmap and a number to screen.
 * The number is used to indicate the number of Bitmaps that have been decoded.
 */
@SuppressLint("ViewConstructor")
class PurgeableBitmapView(context: Context?, isPurgeable: Boolean) : View(context) {
    /**
     * Contains a JPEG compressed bitmap which we try to load into each of the 200 members of our
     * `Bitmap[] mBitmapArray`.
     */
    private val bitstream: ByteArray

    /**
     * `Bitmap` pointer to last `Bitmap` decoded into `Bitmap[] mBitmapArray`,
     * used to draw it in our `onDraw` method.
     */
    private var mBitmap: Bitmap? = null

    /**
     * Number of bitmaps we will try to decode into `Bitmap[] mBitmapArray`.
     */
    private val mArraySize = 200

    /**
     * Array we decode our bitmaps onto.
     */
    private val mBitmapArray = arrayOfNulls<Bitmap>(mArraySize)

    /**
     * `Options` object we use in call to `decodeByteArray`, we set its field
     * `inPurgeable` to the value of the flag `isPurgeable` passed to our constructor.
     * (This field is ignored since Lollipop however).
     */
    private val mOptions = BitmapFactory.Options()

    /**
     * Number of bitmaps we have decoded into `Bitmap[] mBitmapArray` so far.
     */
    private var mDecodingCount = 0

    /**
     * `Paint` we use in our `onDraw` method to draw the text displaying the number of
     * bitmaps decoded so far.
     */
    private val mPaint = Paint()

    /**
     * Text size of the `Paint mPaint`, set in our constructor
     */
    private val textSize = 32

    /**
     * Calculates colors to fill an array and returns it.
     *
     * @return an array of color values.
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

    /**
     * Called to try to decode another `Bitmap`. Wrapped in a try block intended to catch
     * OutOfMemoryError exceptions we attempt to decode `byte[] bitstream` into the next bitmap
     * slot in the array `Bitmap[] mBitmapArray`. We also set our field `Bitmap mBitmap`
     * to point at this bitmap for the sake of our `onDraw` method. We next increment the value
     * of `mDecodingCount` (our count of the number of bitmaps successfully decoded). If there
     * is still more room in `Bitmap[] mBitmapArray`, we call the `sleep` method of our
     * `handler` parameter to schedule another call in 100 milliseconds and return 0 to the
     * caller (the `handleMessage` method of `handler`). If we have run out of slots in
     * `Bitmap[] mBitmapArray` we return the negated value of `mDecodingCount` to the
     * caller indicating successful decoding of all 200 bitmaps. If our decoding causes the throwing
     * of an OutOfMemoryError exception, we recycle all the `Bitmap` objects currently in
     * `Bitmap[] mBitmapArray`, and return the number of bitmaps which caused us to throw the
     * exception: `mDecodingCount+1`.
     *
     * @param handler `Handler` whose `sleep` method we call to schedule another call to us.
     * @return Zero if we still have bitmaps to decode, or if non-zero, the number of bitmaps successfully
     * decoded, negated if we did not run out of memory and  positive if we did run out of memory
     */
    fun update(handler: RefreshHandler): Int {
        return try {
            mBitmapArray[mDecodingCount] = BitmapFactory.decodeByteArray(bitstream, 0, bitstream.size, mOptions)
            mBitmap = mBitmapArray[mDecodingCount]
            mDecodingCount++
            if (mDecodingCount < mArraySize) {
                handler.sleep(delay.toLong())
                0
            } else {
                -mDecodingCount
            }
        } catch (error: OutOfMemoryError) {
            var i = 0
            while (i < mDecodingCount) {
                mBitmapArray[i]!!.recycle()
                i++
            }
            mDecodingCount + 1
        }
    }

    /**
     * We implement this to do our drawing. First we set the entire `Canvas canvas` to the
     * color WHITE, then we draw `Bitmap mBitmap` to it, and finally we draw the number of
     * bitmaps decoded so far on it.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.WHITE)
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap!!, 0f, 0f, null)
        }
        canvas.drawText(mDecodingCount.toString(), WIDTH / 2f - 20f, HEIGHT / 2f, mPaint)
    }

    /**
     * Create a compressed version of the `Bitmap` passed to it, and returns it in a `Byte[]`
     * array, First we allocate a new `ByteArrayOutputStream os`, and use the compress method
     * of `Bitmap src` to write a compressed version of the bitmap to this `ByteArrayOutputStream`
     * using our parameter `quality` for the quality hint, and our parameter `format` as
     * the compression format. Finally we return the contents of `os` as a byte array to the caller.
     *
     * @param src     `Bitmap` we are to compress into a byte array
     * @param format  Format of the compression that we are to use.
     * @param quality Hint to the compressor, 0-100. 0 meaning compress for small size, 100 meaning
     * compress for max quality.
     * @return `Byte[]` array containing compressed version of the `Bitmap src`
     */
    @Suppress("SameParameterValue")
    @SuppressLint("WrongThread")
    private fun generateBitstream(src: Bitmap, format: CompressFormat, quality: Int): ByteArray {
        val os = ByteArrayOutputStream()
        src.compress(format, quality, os)
        return os.toByteArray()
    }

    companion object {
        /**
         * Width of the `Bitmap` we create, then compress into a JPEG bitstream `byte[] bitstream`
         * and then decode back into a new `Bitmap` to fill our `Bitmap[] mBitmapArray`
         */
        private const val WIDTH = 150

        /**
         * Height of the `Bitmap` we create, then compress into a JPEG bitstream `byte[] bitstream`
         * and then decode back into a new `Bitmap` to fill our `Bitmap[] mBitmapArray`
         */
        private const val HEIGHT = 450

        /**
         * Stride of the `Bitmap` we create, then compress into a JPEG bitstream `byte[] bitstream`
         * and then decode back into a new `Bitmap` to fill our `Bitmap[] mBitmapArray` (this
         * is the number of colors between rows or the array of colors passed to `createBitmap`).
         */
        private const val STRIDE = 320 // must be >= WIDTH

        /**
         * Delay in milliseconds we use when we call the `sleep` method of the `PurgeableBitmap.RefreshHandler`
         * which was passed as a parameter to our `update` method ("this" when we are called from the
         * `handleMessage` method of the `PurgeableBitmap.RefreshHandler` which `PurgeableBitmap`
         * uses).
         */
        private const val delay = 100
    }

    /**
     * Our constructor. First we call our super's constructor, then we enable our view to receive
     * focus, and initialize the `inPurgeable` field of `Options mOptions` to the value
     * of our parameter `isPurgeable`. We call our method `createColors` to create an
     * array of colors for `int[] colors`, and use it to create `Bitmap src`. We then
     * initialize our field `byte[] bitstream` with the return value of our method
     * `generateBitstream` (a byte array of `Bitmap src` compressed using JPEG). Finally
     * we set the text size of `Paint mPaint` to `textSize`, and its color to GRAY.
     *
     *  context     `Context` to use to access resources.
     *  isPurgeable flag to use to set the `inPurgeable` of the `Options mOptions`
     * object we use when decoding our bitmaps.
     */
    init {
        isFocusable = true
        @Suppress("DEPRECATION")
        mOptions.inPurgeable = isPurgeable
        val colors = createColors()
        val src = Bitmap.createBitmap(colors, 0, STRIDE, WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        bitstream = generateBitstream(src, CompressFormat.JPEG, 80)
        mPaint.textSize = textSize.toFloat()
        mPaint.color = Color.GRAY
    }
}
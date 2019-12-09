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

@file:Suppress("DEPRECATION")

package com.example.android.apis.graphics

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
// Movie is deprecated, TODO: replace with android.graphics.drawable.AnimatedImageDrawable
import android.graphics.Movie
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import com.example.android.apis.R
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Shows how to decode various image file formats into displayable bitmaps: drawable/beach.jpg,
 * drawable/frog.gif (creates also an Bitmap.Config.ARGB_8888 bitmap, and a Bitmap.Config.ARGB_4444
 * bitmap from that bitmap), drawable/button.9.png, and drawable/animated_gif.gif which it animates
 * using android.graphics.Movie.java
 */
class BitmapDecode : GraphicsActivity() {
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
     * Custom View which draws 4 `Bitmap`'s, one `Drawable`, and an animated gif in a
     * `Movie` instance.
     */
    private class SampleView(context: Context) : View(context) {
        /**
         * Decoded R.raw.beach jpg, scaled down by 4
         */
        private val mBitmap: Bitmap?
        /**
         * Decoded R.raw.frog gif
         */
        private val mBitmap2: Bitmap
        /**
         * Deep copy of the pixels of `mBitmap2` using the Bitmap.Config ARGB_8888
         */
        private val mBitmap3: Bitmap
        /**
         * Deep copy of the pixels of `mBitmap2` using the Bitmap.Config ARGB_4444
         */
        private val mBitmap4: Bitmap
        /**
         * R.drawable.button.9.png `Drawable`
         */
        private val mDrawable: Drawable
        /**
         * `Movie` used to play the animated gif R.raw.animated_gif
         */
        @Suppress("DEPRECATION")
        private var mMovie: Movie? = null
        /**
         * Start time in milliseconds of the `Movie` animation, used to calculate the relative
         * time of the animation before asking `mMovie` to draw the frame that is scheduled for
         * that relative time.
         */
        private var mMovieStart: Long = 0

        /**
         * We implement this to do our drawing when requested to do so. First we set the entire
         * `Canvas canvas` to the color 0xFFCCCCCC (a darkish gray). Then if `Bitmap mBitmap`
         * is not null we draw it at location (10,10) on the `Canvas canvas`, and draw
         * `Bitmap mBitmap2` at (10,170), `Bitmap mBitmap3` at (110,170), and
         * `Bitmap mBitmap4` at (210,170). We draw `Drawable mDrawable` to `canvas`
         * (its bounds already position is correctly).
         *
         * Next we fetch the current system time to `long now`, and if our field `long mMovieStart`
         * is zero (first time we are called) we set `mMovieStart` to `now`. If `Movie mMovie`
         * is not null, we fetch the duration of `mMovie` to `int dur` (1000 for our gif), making
         * sure it is 1000 if `Movie.duration` returned zero. We calculate `int relTime` to be
         * modulo `dur` of the time in milliseconds since `mMovieStart`, set the time of `mMovie`
         * to `relTime` and instruct it to draw the current frame of the animated gif at the
         * bottom right corner of `Canvas canvas`.
         *
         * Finally we invalidate our `View` which will schedule us to run again for the next refresh
         * of the screen.
         *
         * @param canvas `Canvas` to draw our `View` onto
         */
        override fun onDraw(canvas: Canvas) {
            canvas.drawColor(-0x333334)
            if (mBitmap != null) {
                canvas.drawBitmap(mBitmap, 10f, 10f, null)
            }
            canvas.drawBitmap(mBitmap2, 10f, 170f, null)
            canvas.drawBitmap(mBitmap3, 110f, 170f, null)
            canvas.drawBitmap(mBitmap4, 210f, 170f, null)
            mDrawable.draw(canvas)
            val now = SystemClock.uptimeMillis()
            if (mMovieStart == 0L) { // first time
                mMovieStart = now
            }
            if (mMovie != null) {
                @Suppress("DEPRECATION")
                var dur = mMovie!!.duration()
                if (dur == 0) {
                    dur = 1000
                }
                val relTime = ((now - mMovieStart) % dur).toInt()
                @Suppress("DEPRECATION")
                mMovie!!.setTime(relTime)
                @Suppress("DEPRECATION")
                mMovie!!.draw(canvas, width - mMovie!!.width().toFloat(), height - mMovie!!.height().toFloat())
                invalidate()
            }
        }

        companion object {
            /**
             * TAG for logging
             */
            private const val TAG = "BitMapDecode"
            /**
             * Set to false to use `Movie.decodeByteArray` instead of `Movie.decodeStream`
             */
            private const val DECODE_STREAM = true

            /**
             * Used to read an `InputStream` into a `byte[]` array. First we allocate a
             * `ByteArrayOutputStream os` with an initial buffer capacity of 1024, and a
             * `byte[] buffer` with a capacity of 1024. Then wrapped in a try block intended to
             * catch IOException we read up to 1024 bytes at a time from `inputStream` into
             * `buffer` and write them to `os`, stopping when the read from `inputStream` is
             * less than 0 (end of file). Finally we return the current contents of the output stream
             * `os`, in a newly allocated byte array as returned by
             * `ByteArrayOutputStream.toByteArray()`.
             *
             * @param inputStream `InputStream` to read bytes from
             * @return a `byte[]` array containing the raw data from `inputStream`
             */
            private fun streamToBytes(inputStream: InputStream): ByteArray {
                val os = ByteArrayOutputStream(1024)
                val buffer = ByteArray(1024)
                var len: Int
                try {
                    while (inputStream.read(buffer).also { len = it } >= 0) {
                        os.write(buffer, 0, len)
                    }
                } catch (e: IOException) {
                    Log.i(TAG, e.localizedMessage!!)
                }
                return os.toByteArray()
            }
        }

        /**
         * Constructs and initializes an instance of `SampleView`. First we call through to our
         * super's constructor, then we enable this View to receive focus. We declare `InputStream inputStream`
         * and use it to open the resource jpg R.raw.beach. We create `BitmapFactory.Options opts`,
         * and declare `Bitmap bm`. We set the `inJustDecodeBounds` field of `opts` to
         * true (the decoder will return null (no bitmap), but the out... fields will still be set,
         * allowing the caller to query the bitmap without having to allocate the memory for its pixels),
         * and use it as the `Options` parameter when we call `decodeStream` on `inputStream`
         * after which the fields `opts.outWidth` and `opts.outHeight` contain the dimensions
         * of the bitmap that would be created from `inputStream` (null is returned instead of a bitmap).
         * We now set the `opts.inJustDecodeBounds` field to false, and `opts.inSampleSize`
         * to 4, rewind `inputStream` and decode it again this time into our `Bitmap bm`. This results
         * in a bitmap version of `inputStream` scaled down by 4. We set our field `Bitmap mBitmap` to
         * `bm`.
         *
         *
         * Now we use `inputStream` to open our resource gif R.raw.frog, and decode it into our field
         * `Bitmap mBitmap2`. We fetch the width of `mBitmap2` to `int w` and the
         * height to `int h` and allocate `int[] pixels` to contain `w*h` ints.
         * We copy all of the pixels from `Bitmap2` into `pixels`, then use `pixels`
         * to create `Bitmap mBitmap3` using a config of ARGB_8888, and create `Bitmap mBitmap4`
         * using a config of ARGB_4444. Then we load `Drawable mDrawable` from our resource file
         * R.drawable.button and set its bounds to (150, 20, 300, 100) (left,top,right,bottom). We open
         * our resource animated gif file R.raw.animated_gif using `inputStream`, and decode this stream
         * into `Movie mMovie` (If DECODE_STREAM is true that is, otherwise we read the raw bytes
         * of `inputStream` into `byte[] array` and decode that byte array into `Movie mMovie`).
         *
         * Parameter: `Context` to use to fetch resources, "this" when called from our
         * `onCreate` override
         */
        init {
            isFocusable = true
            var inputStream: InputStream = context.resources.openRawResource(R.raw.beach)
            val opts = BitmapFactory.Options()
            var bm: Bitmap?
            opts.inJustDecodeBounds = true
            @Suppress("UNUSED_VALUE")
            bm = BitmapFactory.decodeStream(inputStream, null, opts)
            // now opts.outWidth and opts.outHeight are the dimension of the
// bitmap, even though bm is null
            opts.inJustDecodeBounds = false // this will request the bm
            opts.inSampleSize = 4 // scaled down by 4
            try {
                inputStream.reset() // Need to rewind is in order to read it again.
            } catch (e: IOException) {
                e.printStackTrace()
            }
            bm = BitmapFactory.decodeStream(inputStream, null, opts)
            mBitmap = bm
            // decode an image with transparency
            inputStream = context.resources.openRawResource(R.raw.frog)
            mBitmap2 = BitmapFactory.decodeStream(inputStream)
            // create a deep copy of it using getPixels() into different configs
            val w = mBitmap2.width
            val h = mBitmap2.height
            val pixels = IntArray(w * h)
            mBitmap2.getPixels(pixels, 0, w, 0, 0, w, h)
            mBitmap3 = Bitmap.createBitmap(pixels, 0, w, w, h, Bitmap.Config.ARGB_8888)
            @Suppress("DEPRECATION")
            mBitmap4 = Bitmap.createBitmap(pixels, 0, w, w, h, Bitmap.Config.ARGB_4444)
            @Suppress("DEPRECATION")
            mDrawable = context.resources.getDrawable(R.drawable.button)
            mDrawable.setBounds(150, 20, 300, 100)
            inputStream = context.resources.openRawResource(R.raw.animated_gif)
            @Suppress("ConstantConditionIf")
            mMovie = if (DECODE_STREAM) {
                @Suppress("DEPRECATION")
                Movie.decodeStream(inputStream)
            } else {
                val array = streamToBytes(inputStream)
                @Suppress("DEPRECATION")
                Movie.decodeByteArray(array, 0, array.size)
            }
        }
    }
}
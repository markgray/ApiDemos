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
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.Window

/**
 * Draws a unicode chart, and cycles from page to page of the chart as you click it (or use dpad)
 */
class UnicodeChart : GraphicsActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we request the window feature FEATURE_NO_TITLE (turns off the title at
     * the top of the screen), and finally we set our content view to a new instance of `SampleView`.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(SampleView(this))
    }

    /**
     * Simple custom `View` which fills its canvas with a unicode chart.
     */
    private class SampleView(context: Context?) : View(context) {
        /**
         * `Paint` used to draw the unicode characters in our chart, text size is 15
         */
        private val mBigCharPaint: Paint

        /**
         * `Paint` used to draw the hex values of the characters under the unicode characters
         */
        private val mLabelPaint: Paint

        /**
         * The 256 characters in the current chart.
         */
        private val mChars = CharArray(256)

        /**
         * x and y coordinates of the characters in the chart.
         */
        private val mPos = FloatArray(512)

        /**
         * Which chart is to be shown (it becomes the upper byte of the unicode characters drawn)
         */
        private var mBase = 0

        /**
         * Computes and returns the X coordinate to place the hex version of the character on the screen.
         *
         * @param index index of the character being considered.
         * @return X coordinate to place the hex version of the character on the screen
         */
        private fun computeX(index: Int): Float {
            return ((index shr 4) * XMUL + 10).toFloat()
        }

        /**
         * Computes and returns the Y coordinate to place the hex version of the character on the screen.
         *
         * @param index index of the character being considered.
         * @return Y coordinate to place the hex version of the character on the screen
         */
        private fun computeY(index: Int): Float {
            return ((index and 0xF) * YMUL + YMUL).toFloat()
        }

        /**
         * Draws the unicode chart for the characters in page `base`. First we copy the `mChars`
         * pointer to `char[] chars`, then for each of the 256 characters `i` in the current page
         * we form the unicode value `int unichar` by adding `base` to `i` and cast
         * `unichar` to `char` to set `chars[ i ]`, then draw the hex version of `unichar`
         * underneath the location where the unicode character will be placed using `mLabelPaint` as
         * the `Paint`.
         *
         *
         * Having filled `chars` and labeled the grid, we now draw the unicode characters in `chars`
         * using `mPos` to position them, and `mBigCharPaint` as the `Paint`.
         *
         * @param canvas `Canvas` we are to draw to
         * @param base   which page we are to draw.
         */
        private fun drawChart(canvas: Canvas, base: Int) {
            val chars = mChars
            for (i in 0..255) {
                val unichar = base + i
                chars[i] = unichar.toChar()
                canvas.drawText(Integer.toHexString(unichar), computeX(i), computeY(i), mLabelPaint)
            }
            @Suppress("DEPRECATION")
            canvas.drawPosText(chars, 0, 256, mPos, mBigCharPaint)
        }

        /**
         * We implement this to do our drawing. First we fill the entire `Canvas canvas` with the
         * color WHITE, scale the canvas by the screen density, move it to (0,1), and then call our
         * method `drawChart` to draw the unicode characters in page `mBase` (where a page
         * is defined as 256 adjacent code points) on `Canvas canvas`.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            canvas.drawColor(Color.WHITE)
            canvas.scale(SCREEN_DENSITY, SCREEN_DENSITY)
            canvas.translate(0f, 1f)
            drawChart(canvas, mBase * 256)
        }

        /**
         * Called when a key down event has occurred. We switch on the value of `keyCode`:
         *
         *  *
         * KEYCODE_DPAD_LEFT - if `mBase` is greater than 0, we decrement it, invalidate
         * the view so it will drawn again, and return true
         *
         *  *
         * KEYCODE_DPAD_RIGHT - we increment `mBase`, invalidate the view so it will
         * drawn again, and return true
         *
         *
         * If the `keyCode` is neither KEYCODE_DPAD_LEFT nor KEYCODE_DPAD_RIGHT, we return the
         * value returned by our super's implementation of `onKeyDown`.
         *
         * @param keyCode The value in event.getKeyCode().
         * @param event   Description of the key event.
         * @return If you handled the event, return true.  If you want to allow
         * the event to be handled by the next receiver, return false.
         */
        override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    if (mBase > 0) {
                        mBase -= 1
                        invalidate()
                    }
                    return true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    mBase += 1
                    invalidate()
                    return true
                }
                else -> {
                }
            }
            return super.onKeyDown(keyCode, event)
        }

        companion object {
            /**
             * Size of the character cell in the X dimension.
             */
            private const val XMUL = 20

            /**
             * Size of the character cell in the Y dimension.
             */
            private const val YMUL = 28

            /**
             * Offset from the top of the screen for determining Y coordinate of the characters.
             */
            private const val YBASE = 18
            /**
             * The logical density of the display. This is a scaling factor for the Density Independent
             * Pixel unit, where one DIP is one pixel on an approximately 160 dpi screen (for example a
             * 240x320, 1.5"x2" screen), providing the baseline of the system's display. Thus on a 160dpi
             * screen this density value will be 1; on a 120 dpi screen it would be .75; etc.
             */
            private var SCREEN_DENSITY: Float = 1f
        }

        /**
         * Our constructor. First we enable our view to receive focus, and to receive focus in touch
         * mode. We initialize `SCREEN_DENSITY` to the logical density of the screen. We allocate
         * a new instance of `Paint` with its ANTI_ALIAS_FLAG flag set for our field
         * `Paint mBigCharPaint`, set its text size to 15, and set its text alignment to CENTER.
         * We allocate a new instance of `Paint` with its ANTI_ALIAS_FLAG flag set for our field
         * `Paint mLabelPaint`, set its text size to 8, and set its text alignment to CENTER.
         *
         *
         * We copy the `mPos` pointer to `float[] pos`, and initialize `int index`
         * to 0. Then we loop through the 16 columns and 16 rows calculating the value of the `x`
         * coordinate for the characters in the current column, before looping through the rows where
         * we set the `index` entry in `pos` to `x`, incrementing `index`, setting
         * the following entry to the Y coordinate for the current row and incrementing `index`.
         * The end result is to fill the `pos` (and hence the `mPos`) array with (x,y)
         * coordinate pairs for each of the 256 characters displayed in our chart, going down the rows
         * first, and then moving to the next column and going down the rows again etc.
         *
         *
         * Finally we set our `OnClickListener` to an anonymous class which advances to the next
         * page in the unicode space.
         *
         *  context `Context` for accessing resources, "this" when called from the
         * `onCreate` method of `UnicodeChart`
         */
        init {
            isFocusable = true
            isFocusableInTouchMode = true
            SCREEN_DENSITY = resources.displayMetrics.density
            mBigCharPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            mBigCharPaint.textSize = 15f
            mBigCharPaint.textAlign = Paint.Align.CENTER
            mLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            mLabelPaint.textSize = 8f
            mLabelPaint.textAlign = Paint.Align.CENTER

            // the position array is the same for all charts
            val pos = mPos
            var index = 0
            for (col in 0..15) {
                val x = col * XMUL + 10.toFloat()
                for (row in 0..15) {
                    pos[index++] = x
                    pos[index++] = (row * YMUL + YBASE).toFloat()
                }
            }
            setOnClickListener {
                mBase = (mBase + 1) % 256
                invalidate()
            }
        }
    }
}
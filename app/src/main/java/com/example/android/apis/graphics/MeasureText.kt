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
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.view.View

/**
 * Shows how to use the text measurement methods: [Paint.getTextWidths],
 * [Paint.measureText], and [Paint.getTextBounds] to determine what area
 * each character in a string as well as the complete string will occupy
 * when they are drawn using [Canvas.drawText], then draws a colored rectangle
 * around each string and a line under each at the baseline. Text was too
 * small so I modified it a bit.
 */
class MeasureText : GraphicsActivity() {
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
     * Custom view which displays three lines of text, and using the text measuring methods draws a
     * tight, light green rectangle around the text, and a RED line at the baseline of the text.
     *
     * @param context the context we are running in, through to the super's constructor
     * (See our init block for our constructor details)
     */
    private class SampleView(context: Context?) : View(context) {
        /**
         * [Paint] used to draw with in the method [showText] which is called from [onDraw].
         */
        private val mPaint: Paint

        /**
         * Starting x coordinate for our [onDraw] override to draw to its [Canvas]
         */
        private val mOriginX = 10f

        /**
         * Starting y coordinate for our [onDraw] override to draw to its [Canvas]
         */
        private val mOriginY = 320f

        /**
         * Convenience method to measure the [String] parameter [text], draw a light green rectangle
         * of that size, draw the [text], and draw a RED horizontal line across the baseline of
         * the text. First we allocate a [Rect] for `val bounds` which we will use to hold the
         * rectangle that encloses all of [text], and a [Float] array for `val widths` which will
         * hold the widths of each of the corresponding characters in [text]. We set the text size
         * of [Paint] field [mPaint] to 100, use the [Paint.getTextWidths] method of [mPaint] to
         * load the array `widths` with the widths of each of the characters in [text] and setting
         * `val count` to the number of code units in the text which the method returns. We use
         * the [Paint.measureText] method of [mPaint] to set [Float] variable `val w` to the width
         * of [text], and we use the [Paint.getTextBounds] method of [mPaint] to initialize `bounds`
         * to the smallest rectangle that encloses all of the characters, with an implied origin at
         * (0,0).
         *
         * Having retrieved all the measurements of [text] we proceed to set the color of [mPaint]
         * to a shade of green, use it to draw a rectangle on our [Canvas] parameter [canvas] using
         * `bounds` as the rectangle shape. We then change the color of [mPaint] to BLACK and use it
         * to draw the text [text] to [canvas].
         *
         * In order to draw the line at the baseline, we allocate a [Float] array for `val pts` to
         * be twice as long as `count` plus 2 entries for the starting point of (0,0) and loop
         * through `widths` accumulating the widths of the characters in [text] for the starting
         * x coordinate of each character with the y coordinate 0. After this `pts` thus contains
         * the (x,y) location of each character in the string [text]. We then set the color of
         * [mPaint] to RED, the stroke width to 0 (hairline mode) and draw a line across the
         * entire width of the text. We then set the stroke width of [mPaint] to 5 and draw
         * largish points at each of the (x,y) starting locations for the characters in [text]
         * which we have stored in `pts`.
         *
         * @param canvas `Canvas` we are to draw to
         * @param text   text string we are to draw
         * @param align  not used
         */
        @Suppress("UNUSED_PARAMETER")
        private fun showText(
            canvas: Canvas,
            text: String,
            align: Align
        ) { //   mPaint.setTextAlign(align);
            val bounds = Rect()
            val widths = FloatArray(text.length)
            mPaint.textSize = 100.0.toFloat()
            val count = mPaint.getTextWidths(text, 0, text.length, widths)
            val w = mPaint.measureText(text, 0, text.length)
            mPaint.getTextBounds(text, 0, text.length, bounds)
            mPaint.color = -0x770078
            canvas.drawRect(bounds, mPaint)
            mPaint.color = Color.BLACK
            canvas.drawText(text, 0f, 0f, mPaint)
            val pts = FloatArray(2 + count * 2)
            var x = 0f
            val y = 0f
            pts[0] = x
            pts[1] = y
            for (i in 0 until count) {
                x += widths[i]
                pts[2 + i * 2] = x
                pts[2 + i * 2 + 1] = y
            }
            mPaint.color = Color.RED
            mPaint.strokeWidth = 0f
            canvas.drawLine(0f, 0f, w, 0f, mPaint)
            mPaint.strokeWidth = 5f
            canvas.drawPoints(pts, 0, count + 1 shl 1, mPaint)
        }

        /**
         * We implement this to do our drawing. First we set the entire [Canvas] parameter [canvas]
         * to WHITE, then we move it to our starting point ([mOriginX], [mOriginY]) (10,10) in our
         * case. We call our method [showText] to measure and display the text "Measure", move the
         * canvas down 180 pixels and call [showText] to measure and display the text "wiggy!"
         * and finally move the canvas down a further 180 pixels and call [showText] to measure
         * and display the text "Text".
         *
         * @param canvas the [Canvas] on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            canvas.drawColor(Color.WHITE)
            canvas.translate(mOriginX, mOriginY)
            showText(canvas, "Measure", Align.LEFT)
            canvas.translate(0f, 180f)
            showText(canvas, "wiggy!", Align.CENTER)
            canvas.translate(0f, 180f)
            showText(canvas, "Text", Align.RIGHT)
        }

        /**
         * We enable our view to receive focus. We allocate a new `Paint` for our `Paint` field
         * `mPaint`, set its antialias flag to true, set the stroke width to 5, set the line cap
         * style to ROUND, text size to 64, and finally set its typeface to a typeface object of
         * the SERIF family, and ITALIC style
         */
        init {
            isFocusable = true
            mPaint = Paint()
            mPaint.isAntiAlias = true
            mPaint.strokeWidth = 5f
            mPaint.strokeCap = Paint.Cap.ROUND
            mPaint.textSize = 64f
            mPaint.typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        }
    }

    companion object {
        /**
         * Only used in `createColors` so unused
         */
        private const val WIDTH = 50

        /**
         * Only used in `createColors` so unused
         */
        private const val HEIGHT = 50

        /**
         * Only used in `createColors` so unused
         */
        private const val STRIDE = 64 // must be >= WIDTH

        /**
         * Unused, so who cares.
         *
         * @return an array of colors
         */
        @Suppress("unused")
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
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
import android.graphics.Path
import android.graphics.Typeface
import android.os.Bundle
import android.view.View

/**
 * Shows how to position text drawn to a Canvas using [Paint.setTextAlign], [Canvas.drawPosText],
 * and along an arbitrary path using [Canvas.drawTextOnPath]
 */
class TextAlign : GraphicsActivity() {
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
     * Our custom View, it simply displays our text samples.
     */
    private class SampleView(context: Context?) : View(context) {
        /**
         * [Paint] we use to draw our text and some spacing lines with.
         */
        private val mPaint: Paint

        /**
         * X coordinate of the center of the screen.
         */
        private var mX = 0f

        /**
         * x and y coordinates of each character in our "Positioned" text sample.
         */
        private val mPos: FloatArray

        /**
         * [Path] we use for our "Along a path" text sample.
         */
        private val mPath: Path

        /**
         * [Paint] we use to draw the [Path] under our "Along a path" text sample.
         */
        private val mPathPaint: Paint

        /**
         * Creates and returns an array of x, y coordinates for drawing each individual character in
         * the [String] parameter [text]. First we allocate an entry for every character in [text]
         * in [FloatArray] `val widths`. Then we fill it with the widths of all of the characters in
         * [text] using the [Paint.getTextWidths] method of our [Paint] parameter [paint], saving
         * the number of code units in the text in `val n`. We allocate an array of `2*n` [Float]
         * objects for [FloatArray] `val pos`.
         *
         * Then initializing `var accumulatedX` to 0, we loop through all `n` values in `widths`
         * setting two entries in `pos` for the coordinates of each character, the first value
         * being `accumulatedX`, and the second our parameter [y]. We then add the entry in `widths`
         * for this character to `accumulatedX` and loop back for the next character.
         *
         * Finally we return `pos` to the caller.
         *
         * @param text  [String] whose characters we are to determine the position of.
         * @param y     y coordinate position for all the characters.
         * @param paint [Paint] to use to get the text widths of the characters in [text]
         * @return An array of x,y coordinates for the position of each character in [text]
         * when is is drawn.
         */
        @Suppress("SameParameterValue")
        private fun buildTextPositions(text: String, y: Float, paint: Paint): FloatArray {
            val widths = FloatArray(text.length)
            // initially get the widths for each char
            val n = paint.getTextWidths(text, widths)
            // now populate the array, interleaving spaces for the Y values
            val pos = FloatArray(n * 2)
            var accumulatedX = 0f
            for (i in 0 until n) {
                pos[i * 2 + 0] = accumulatedX
                pos[i * 2 + 1] = y
                accumulatedX += widths[i]
            }
            return pos
        }

        /**
         * We implement this to do our drawing. First we draw the entire [Canvas] parameter [canvas]
         * WHITE. We make a copy of the [Paint] field [mPaint] for `val p`, copy the [Float] field
         * [mX]  to `val x`, set `val y` to 0, and make a copy of the [FloatArray] field [mPos]
         * pointer in `val pos`.
         *
         * We set the color of `p` to red and use it to draw a line from `(x,y)` to `(x,y+DY*3)`
         * (this is a line showing the center of the aligned text we are about to draw). We now set
         * the color of `p` to BLACK, translate the [canvas] down [DY] pixels, set the text alignment
         * of `p` to LEFT, and use it to draw the text TEXT_L ("Left") at location `(x,y)`. We move the
         * [canvas] down [DY] pixels, set the text alignment of `p` to CENTER, and use it to draw the
         * text TEXT_C ("Center") at location `(x,y)`. We move the [canvas] down [DY] pixels, set the
         * text alignment of `p` to RIGHT, and use it to draw the text TEXT_R ("Right") at location
         * `(x,y)`.
         *
         * Now we move on to the positioned text, first moving the [Canvas] parameter [canvas] to
         * the location `(100,DY*2)`, and setting the color of `p` to green. We loop through the
         * coordinates in `pos` drawing a vertical line between one [DY] above to two [DY] below
         * the `(x,y)` coordinates contained in `pos`.
         *
         * Then we set the color of `p` to BLACK, its text alignment to LEFT, and use the method
         * `canvas.drawPosText` to draw the text [POSTEXT] ("Positioned"), using `pos` as the
         * location of each character, and the [Paint] `p` as the [Paint]. We move [canvas] down
         * by [DY], set the text alignment of `p` to to CENTER, and use the method `canvas.drawPosText`
         * to draw the text [POSTEXT] ("Positioned"), using `pos` as the location of each character,
         * and the [Paint] `p` as the [Paint]. Finally we move [canvas] down by [DY], set the text
         * alignment of `p` to RIGHT, and use the method `canvas.drawPosText` to draw the text
         * [POSTEXT] ("Positioned"), using `pos` as the location of each character, and the [Paint]
         * `p` as the [Paint].
         *
         * We move the `Canvas canvas` to (-100,DY*2) (relative to its last position of course), and
         * draw our [Path] field [mPath] using the [Paint] field [mPathPaint]. We set the text
         * alignment of `p` to LEFT, and use the method `canvas.drawTextOnPath` to draw the text
         * [TEXTONPATH] ("Along a path") along the [Path] field [mPath] using `p` as the [Paint].
         * We move [canvas] down by `DY*1.5` pixels, and draw our [Path] field [mPath] using the
         * [Paint] field [mPathPaint]. We set the text alignment of `p` to CENTER, and use the
         * method `canvas.drawTextOnPath` to draw the text [TEXTONPATH] ("Along a path") along the
         * [Path] field [mPath] using `p` as the [Paint]. And finally we move [canvas] down by
         * `DY*1.5` pixels, and draw  our [Path] field [mPath] using the [Paint] field [mPathPaint].
         * We set the text alignment of `p` to RIGHT, and use the method `canvas.drawTextOnPath` to
         * draw the text [TEXTONPATH] ("Along a path") along [Path] field [mPath] using `p` as the
         * [Paint].
         *
         * @param canvas the [Canvas] on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            canvas.drawColor(Color.WHITE)
            val p = mPaint
            val x = mX
            val y = 0f
            val pos = mPos

            // draw the normal strings
            p.color = -0x7f010000
            canvas.drawLine(x, y, x, y + DY * 3, p)
            p.color = Color.BLACK
            canvas.translate(0f, DY.toFloat())
            p.textAlign = Paint.Align.LEFT
            canvas.drawText(TEXT_L, x, y, p)
            canvas.translate(0f, DY.toFloat())
            p.textAlign = Paint.Align.CENTER
            canvas.drawText(TEXT_C, x, y, p)
            canvas.translate(0f, DY.toFloat())
            p.textAlign = Paint.Align.RIGHT
            canvas.drawText(TEXT_R, x, y, p)
            canvas.translate(100f, DY * 2.toFloat())

            // now draw the positioned strings
            p.color = -0x44ff0100
            for (i in 0 until pos.size / 2) {
                canvas.drawLine(pos[i * 2 + 0], pos[i * 2 + 1] - DY,
                        pos[i * 2 + 0], pos[i * 2 + 1] + DY * 2, p)
            }
            p.color = Color.BLACK
            p.textAlign = Paint.Align.LEFT
            @Suppress("DEPRECATION")
            canvas.drawPosText(POSTEXT, pos, p)
            canvas.translate(0f, DY.toFloat())
            p.textAlign = Paint.Align.CENTER
            @Suppress("DEPRECATION")
            canvas.drawPosText(POSTEXT, pos, p)
            canvas.translate(0f, DY.toFloat())
            p.textAlign = Paint.Align.RIGHT
            @Suppress("DEPRECATION")
            canvas.drawPosText(POSTEXT, pos, p)

            // now draw the text on path
            canvas.translate(-100f, DY * 2.toFloat())
            canvas.drawPath(mPath, mPathPaint)
            p.textAlign = Paint.Align.LEFT
            canvas.drawTextOnPath(TEXTONPATH, mPath, 0f, 0f, p)
            canvas.translate(0f, DY * 1.5f)
            canvas.drawPath(mPath, mPathPaint)
            p.textAlign = Paint.Align.CENTER
            canvas.drawTextOnPath(TEXTONPATH, mPath, 0f, 0f, p)
            canvas.translate(0f, DY * 1.5f)
            canvas.drawPath(mPath, mPathPaint)
            p.textAlign = Paint.Align.RIGHT
            canvas.drawTextOnPath(TEXTONPATH, mPath, 0f, 0f, p)
        }

        /**
         * This is called during layout when the size of this view has changed. If you were just
         * added to the view hierarchy, you're called with the old values of 0. First we call
         * through to our super's implementation of `onSizeChanged`, then we initialize our field
         * [mX] to half of the new width of the view as given by [Int] parameter [w].
         *
         * @param w  Current width of this view.
         * @param h  Current height of this view.
         * @param ow Old width of this view.
         * @param oh Old height of this view.
         */
        override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
            super.onSizeChanged(w, h, ow, oh)
            mX = w * 0.5f // remember the center of the screen
        }

        companion object {
            /**
             * Distance between lines of text.
             */
            private const val DY = 30

            /**
             * String used for the "Left" aligned text.
             */
            private const val TEXT_L = "Left"

            /**
             * String used for the "Center" aligned text.
             */
            private const val TEXT_C = "Center"

            /**
             * String used for the "Right" aligned text.
             */
            private const val TEXT_R = "Right"

            /**
             * String used for the "Positioned" text.
             */
            private const val POSTEXT = "Positioned"

            /**
             * String used for the "Along a path" text sample.
             */
            private const val TEXTONPATH = "Along a path"

            /**
             * Fills its [Path] parameter [p] with a cubic bezier from (10,0) to (300,0) using the
             * control points (100,-50) and (200,50).
             *
             * @param p [Path] we are to initialize.
             */
            private fun makePath(p: Path) {
                p.moveTo(10f, 0f)
                p.cubicTo(100f, -50f, 200f, 50f, 300f, 0f)
            }
        }

        /**
         * The init block of our constructor. First we enable our view to receive focus. We initialize
         * our `Paint` field `mPaint` with a new instance of `Paint`, set its anti alias flag to true,
         * its text size to 30, and its typeface to SERIF. We initialize our `FloatArray` field `mPos`
         * with the character positions that the characters in the string POSTEXT ("Positioned") are
         * at when it is drawn. We allocate a new instance of `Path` for our field `mPath` and call
         * our method `makePath` to fill `mPath` with a cubic bezier from (10,0) to (300,0). Then we
         * allocate a new instance of `Paint` for our field `mPathPaint`, set its anti alias flag to
         * true, set its color to blue (with an alpha of 0x80), and set its style to STROKE.
         */
        init {
            isFocusable = true
            mPaint = Paint()
            mPaint.isAntiAlias = true
            mPaint.textSize = 30f
            mPaint.typeface = Typeface.SERIF
            mPos = buildTextPositions(POSTEXT, 0f, mPaint)
            mPath = Path()
            makePath(mPath)
            mPathPaint = Paint()
            mPathPaint.isAntiAlias = true
            mPathPaint.color = -0x7fffff01
            mPathPaint.style = Paint.Style.STROKE
        }
    }
}
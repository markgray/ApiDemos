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
package com.example.android.apis.graphics.spritetext

import android.graphics.Paint
import javax.microedition.khronos.opengles.GL10
import kotlin.math.ceil

/**
 * Label class for the ten numeric labels 0, 1, 2, 3, 4, 5, 6, 7, 8, and 9.
 */
class NumericSprite
/**
 * Our constructor, we simply initialize our field `String mText` to the empty string, and
 * set our field `LabelMaker mLabelMaker` to null.
 */
{
    /**
     * Our `LabelMaker`
     */
    private var mLabelMaker: LabelMaker? = null
    /**
     * Numeric text we are to draw, initially the empty string it is set to the number of milliseconds
     * per frame using our method `setValue` by the `drawMsPF` method of `SpriteTextRenderer`
     * which is called every time its `onDrawFrame` method is called
     */
    private var mText = ""
    /**
     * Width of each of our ten numeric labels
     */
    private val mWidth = IntArray(10)
    /**
     * Label index for each of our ten numeric labels.
     */
    private val mLabelId = IntArray(10)

    /**
     * Called from the `onSurfaceCreated` method of `SpriteTextRenderer` to initialize
     * this instance of `NumericSprite`. First we initialize `int height` to be the first
     * power of 2 number greater than the recommended line spacing of `Paint paint`. We set
     * `float interDigitGaps` to 9 pixels (the spacing between digits) and then initialize
     * `int width` to be the first power of 2 number greater than the sum of `interDigitGaps`
     * and the length of our string `String sStrike` in pixels when `paint` is used to
     * render it. We set our field `LabelMaker mLabelMaker` to a new instance of `LabelMaker`
     * specifying the use of full color, a width of `width` and a height of `height`. We
     * instruct `mLabelMaker` to initialize itself (transition to the STATE_INITIALIZED state),
     * then to transition to the state STATE_ADDING.
     *
     *
     * Then for each of the 10 digits in our `String sStrike` we extract a single digit at a
     * time and instruct `LabelMaker mLabelMaker` to add it to its labels saving the index
     * number of the label in our field `int[] mLabelId`, and we save the width of the label
     * created by `mLabelMaker` in our field `int[] mWidth`. When done adding the digits
     * to `mLabelMaker` we instruct it to transition from STATE_ADDING to STATE_INITIALIZED.
     *
     * @param gl    the gl interface
     * @param paint `Paint` to use, it comes from the field `Paint mLabelPaint` which is
     * created and configured in the constructor of `Paint mLabelPaint`.
     */
    fun initialize(gl: GL10?, paint: Paint) {
        val height = roundUpPower2(paint.fontSpacing.toInt())
        val interDigitGaps = 9 * 1.0f
        val width = roundUpPower2((interDigitGaps + paint.measureText(sStrike)).toInt())
        mLabelMaker = LabelMaker(true, width, height)
        mLabelMaker!!.initialize(gl!!)
        mLabelMaker!!.beginAdding(gl)
        for (i in 0..9) {
            val digit = sStrike.substring(i, i + 1)
            mLabelId[i] = mLabelMaker!!.add(gl, digit, paint)
            mWidth[i] = ceil(mLabelMaker!!.getWidth(i).toDouble()).toInt()
        }
        mLabelMaker!!.endAdding(gl)
    }

    /**
     * Called from the `onSurfaceCreated` method of `SpriteTextRenderer` when an instance
     * of us already exists for a previous creation of the surface, in order to force us to create a
     * new instance for the new surface. We simply call the `shutdown` method of `mLabelMaker`
     * and set it to null.
     *
     * @param gl the gl interface
     */
    fun shutdown(gl: GL10?) {
        mLabelMaker!!.shutdown(gl)
        mLabelMaker = null
    }

    /**
     * Find the smallest power of two >= the input value. (Doesn't work for negative numbers.) The
     * shifting and or'ing result in a value with all bits to the right of the most significant 1 bit
     * are also 1's and when 1 is added to that you get a power of 2 value. The initial subtract is
     * done in case the number was already that value.
     *
     * @param x number to round up
     * @return our input parameter rounded up to the smallest power of two above it
     */
    private fun roundUpPower2(x: Int): Int {
        var xVar = x
        xVar -= 1
        xVar = xVar or (xVar shr 1)
        xVar = xVar or (xVar shr 2)
        xVar = xVar or (xVar shr 4)
        xVar = xVar or (xVar shr 8)
        xVar = xVar or (xVar shr 16)
        return xVar + 1
    }

    /**
     * Formats its parameter `int value` as text and saves it in our field `String mText`.
     * `mText` is then drawn using the individual digit labels when our `draw` method is
     * called.
     *
     * @param value int value we want to print to our surface
     */
    fun setValue(value: Int) {
        mText = format(value)
    }

    /**
     * Called to draw the contents of our field `String mText` to the `SurfaceView` using
     * the individual digit labels we have created in our `LabelMaker mLabelMaker`. First we
     * set `int length` to the number of characters in `mText` and instruct our instance
     * of `LabelMaker mLabelMaker` to begin drawing (transition from the state STATE_INITIALIZED
     * to the state STATE_DRAWING). Then for each of the characters in `mText` we calculate the
     * index of the label for that character and instruct `mLabelMaker` to draw that label at
     * the location (x,y). We then advance `x` by the width of the label just drawn and move to
     * the next character.
     *
     *
     * When done drawing we instruct `mLabelMaker` to end the drawing (transition from the state
     * STATE_DRAWING to the state STATE_INITIALIZED).
     *
     * @param gl         the gl interface
     * @param x          x coordinate to start drawing at
     * @param y          y coordinate to start drawing at
     * @param viewWidth  width of the `SurfaceView`
     * @param viewHeight height of the `SurfaceView`
     */
    fun draw(gl: GL10?, x: Float, y: Float, viewWidth: Float, viewHeight: Float) {
        var xVar = x
        val length = mText.length
        mLabelMaker!!.beginDrawing(gl!!, viewWidth, viewHeight)
        for (i in 0 until length) {
            val c = mText[i]
            val digit = c - '0'
            mLabelMaker!!.draw(gl, xVar, y, mLabelId[digit])
            xVar += mWidth[digit]
        }
        mLabelMaker!!.endDrawing(gl)
    }

    /**
     * Adds up the width of each of the labels needed to print the contents of `String mText`
     * and returns the result to the caller. Called from the `drawMsPF` method of the class
     * `SpriteTextRenderer` in order to calculate the correct starting x location before calling
     * our method `draw`.
     *
     * @return width in pixels of the labels needed to render the contents of `String mText`
     */
    fun width(): Float {
        var width = 0.0f
        val length = mText.length
        for (i in 0 until length) {
            val c = mText[i]
            width += mWidth[c - '0']
        }
        return width
    }

    /**
     * Returns its parameter's `String` representation.
     *
     * @param value int value to format into text string
     * @return `String` object representing our parameter `int value`
     */
    private fun format(value: Int): String {
        return value.toString()
    }

    companion object {
        /**
         * String of digits used to initialize our ten numeric labels with the character they should draw.
         */
        private const val sStrike = "0123456789"
    }

}
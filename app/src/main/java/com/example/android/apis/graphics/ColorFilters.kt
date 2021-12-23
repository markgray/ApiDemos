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
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.android.apis.R


/**
 * Applies a PorterDuffColorFilter to tint some button like drawables using several
 * different colors, and both PorterDuff.Mode.SRC_ATOP, and PorterDuff.Mode.MULTIPLY
 * PorterDuff modes to apply them. Oddly only froyo spaces the drawables properly
 * probably due to the use of drawable-mdpi and drawable-hdpi versions. Checking
 * Drawable.getIntrinsicHeight() and modifying the spacing for hdpi fixes the problem.
 */
class ColorFilters : GraphicsActivity() {
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
     * View that displays 7 rows of the same 4 buttons, with each row using different Porter-Duff
     * color filters
     */
    private class SampleView(
            /**
             * `Activity` used to construct us, we use it to set the title bar of its window
             */
            private val mActivity: AppCompatActivity

    ) : View(mActivity) {
        /**
         * The resource button R.drawable.btn_default_normal
         */
        private val mDrawable: Drawable
        /**
         * The three resource buttons R.drawable.btn_circle_normal, R.drawable.btn_check_off, and
         * R.drawable.btn_check_on
         */
        private val mDrawables: Array<Drawable?>
        /**
         * [Paint] used to draw the text "Label" inside the button [Drawable] field [mDrawable],
         * it alternates between 0xFF000000 (Black) and 0xFFFFFFFF (White), and is used in
         * conjunction with its opposite color [Paint] field [mPaint2] with [mPaint2] being drawn
         * first offset by (1,1) and [mPaint] being drawn second.
         */
        private val mPaint: Paint
        /**
         * [Paint] used to draw the text "Label" inside the button [Drawable] field [mDrawable],
         * it alternates between 0xFF000000 (Black) and 0xFFFFFFFF (White), and is used in
         * conjunction with its opposite color [Paint] field [mPaint] with [mPaint2] being drawn
         * first offset by (1,1) and [mPaint] being drawn second.
         */
        private val mPaint2: Paint
        /**
         * 1/2 of the sum of the ascent and descent of the FontMetrics of text at size 16, it is
         * used to position the text "Label" when drawing it into [Drawable] field [mDrawable].
         */
        private val mPaintTextOffset: Float
        /**
         * An array containing the colors 0, 0xCC0000FF, 0x880000FF, 0x440000FF, 0xFFCCCCFF,
         * 0xFF8888FF, and 0xFF4444FF. They are used as an argument to construct a [PorterDuffColorFilter]
         * used for each of the 7 different rows. The other argument is from one of the entries in
         * [PorterDuff.Mode] array field [mModes]
         */
        private val mColors: IntArray
        /**
         * The two PortDuff modes PorterDuff.Mode.SRC_ATOP, and PorterDuff.Mode.MULTIPLY, They are
         * used as an argument to construct the [PorterDuffColorFilter] for all the rows of
         * buttons displayed except the one using the color 0 (the first row which uses null as the
         * color filter). The mode alternates every second touch event based on the current value
         * of the [mModeIndex] field.
         */
        private val mModes: Array<PorterDuff.Mode>
        /**
         * Index into the [PorterDuff.Mode] array field [mModes] used to choose which Porter-Duff
         * mode to use. It is incremented modulo the length of [mModes] every second time the canvas
         * is touched.
         */
        private var mModeIndex: Int
        /**
         * The height of each row of buttons, used to translate the canvas to the row to be drawn
         */
        private var mHeightOffset: Int

        /**
         * This method swaps the colors used by our [Paint] fields [mPaint] and [mPaint2]. If the
         * color of [mPaint] is currently 0xFF000000 (Black), we set it to 0xFFFFFFFF (White) and
         * set the color of [mPaint2] to 0xFF000000 (Black). Otherwise we set the color of [mPaint]
         * to 0xFF000000 (Black) and [mPaint2] to 0xFFFFFFFF (White). Finally we set the alpha
         * channel of [mPaint2] to 64.
         */
        private fun swapPaintColors() {
            if (mPaint.color == -0x1000000) {
                mPaint.color = -0x1
                mPaint2.color = -0x1000000
            } else {
                mPaint.color = -0x1000000
                mPaint2.color = -0x1
            }
            mPaint2.alpha = 64
        }

        /**
         * This is a convenience function to set the title bar of the activity to the string
         * representation of the [PorterDuff.Mode] value in field [mModes] currently selected
         * by the [mModeIndex] field.
         */
        private fun updateTitle() {
            mActivity.title = mModes[mModeIndex].toString()
        }

        /**
         * Draws a row of our four buttons starting at the position the [Canvas] parameter [canvas]
         * has been translated to, using the [ColorFilter] parameter [filter] requested for this row.
         * First we fetch the bounding rectangle of [Drawable] field [mDrawable] to our [Rect]
         * variable `val r` and use it to calculate the [Float] values of the `val x` and `val y`
         * location inside of [mDrawable] that we will use to draw the text "Label". We set the
         * Porter-Duff color filter of [mDrawable] to our [ColorFilter] parameter [filter] and
         * instruct [mDrawable] to draw itself. Then we draw the text "Label" using [Paint] field
         * [mPaint2] offset one pixel to the right and down from `(x,y)`, and draw the same text
         * using [Paint] field [mPaint] at (x,y). This causes the text to have a white or black
         * "shadow" (but it is impossible to see on high density screens).
         *
         * Finally for all the [Drawable] `dr` in the [Drawable] array field [mDrawables] we set the
         * color filter of `dr` to [filter] and instruct `dr` to draw itself.
         *
         * @param canvas translated [Canvas] we are to draw our 4 buttons to
         * @param filter Porter-Duff [ColorFilter] we are to use for our drawings
         */
        private fun drawSample(canvas: Canvas, filter: ColorFilter?) {
            val r = mDrawable.bounds
            val x = (r.left + r.right) * 0.5f
            val y = (r.top + r.bottom) * 0.5f - mPaintTextOffset
            mDrawable.colorFilter = filter
            mDrawable.draw(canvas)
            canvas.drawText("Label", x + 1, y + 1, mPaint2)
            canvas.drawText("Label", x, y, mPaint)
            for (dr in mDrawables) {
                dr!!.colorFilter = filter
                dr.draw(canvas)
            }
        }

        /**
         * We implement this to do our drawing. First we set the color of the entire [Canvas] parameter
         * [canvas] to 0xFFCCCCCC (a darkish gray). Then we Pre-concatenate the current matrix with
         * the translation to (8,[mHeightOffset]). Then for each of the 7 `color` colors in the [Int]
         * array field [mColors] we define [ColorFilter] `val filter`, and if the current `color` is
         * 0 we set it to *null*, otherwise we set it to a an instance of [PorterDuffColorFilter]
         * constructed from `color` and the [mModeIndex] entry in the [PorterDuff.Mode] array field
         * [mModes] which creates a Porter-Duff color filter using `color` and the current Porter-Duff
         * mode selected by [mModeIndex] from the array [mModes] (either PorterDuff.Mode.SRC_ATOP, or
         * PorterDuff.Mode.MULTIPLY). Then we call our method [drawSample] to draw a row of our 4
         * buttons on [canvas] using `filter` as the color filter. Finally we translate [canvas] down
         * by [mHeightOffset] to get ready for the next row.
         *
         * @param canvas the [Canvas] on which the background will be drawn
         */
        @SuppressLint("DrawAllocation")
        override fun onDraw(canvas: Canvas) {
            canvas.drawColor(-0x333334)
            canvas.translate(8f, mHeightOffset.toFloat())
            for (color in mColors) {
                val filter: ColorFilter? = if (color == 0) {
                    null
                } else {
                    PorterDuffColorFilter(color, mModes[mModeIndex])
                }
                drawSample(canvas, filter)
                canvas.translate(0f, mHeightOffset.toFloat())
            }
        }

        /**
         * We implement this method to handle touch screen motion events. We switch based on the action
         * reported in the `MotionEvent event`:
         *
         *  * ACTION_DOWN - ignore, but return true to consume it
         *  * ACTION_MOVE - ignore, but return true to consume it
         *  * ACTION_UP - we want to update the Porter-Duff mode every other time we change
         *  paint colors, so we check to see if the current color of [Paint] field [mPaint]
         *  is 0xFFFFFFFF (White) and if so we increment [mModeIndex] field modulo the length
         *  of [PorterDuff.Mode] array field [mModes] and call our method [updateTitle] to
         *  change the title bar to show the new mode. In either case we call our method
         *  [swapPaintColors] to swap the colors of [Paint] field [mPaint] and [Paint] field
         *  [mPaint2], then return true to consume the event.
         *
         * @param event The motion event.
         * @return *true* to indicate the event was handled
         */
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                }
                MotionEvent.ACTION_MOVE -> {
                }
                MotionEvent.ACTION_UP -> {
                    // update mode every other time we change paint colors
                    if (mPaint.color == -0x1) {
                        mModeIndex = (mModeIndex + 1) % mModes.size
                        updateTitle()
                    }
                    swapPaintColors()
                    invalidate()
                }
            }
            return true
        }

        /**
         * Our static method
         */
        companion object {
            /**
             * Specify a bounding rectangle for the [Drawable] parameter [curr] to the right of
             * [Drawable] parameter [prev]. This is where the drawable will draw when its draw()
             * method is called. First we fetch the bounding rectangle for [prev] to [Rect] `val r`.
             * We calculate `val x` to be 12 pixels further to the right from `r.right` of [prev]'s
             * bounding rectangle. We calculate `val center` midway between the top and bottom of
             * [prev]'s bounding rectangle. We fetch the intrinsic height of [curr] to `val h`, and
             * then calculate the `val y` position we want to use for the top of [curr]'s bounding
             * rectangle to be half of `h` higher than `center`. Finally we set the bounding rectangle
             * for [curr] to be:
             *
             *     (x,y,x+(intrinsic width of curr),y+h) ie: (left,top,right,bottom)
             *
             * @param curr [Drawable] we will set the bounds for
             * @param prev the [Drawable] that was positioned previously, used as the starting point
             * for placing [curr]
             */
            private fun addToTheRight(curr: Drawable?, prev: Drawable?) {
                val r = prev!!.bounds
                val x = r.right + 12
                val center = r.top + r.bottom shr 1
                val h = curr!!.intrinsicHeight
                val y = center - (h shr 1)
                curr.setBounds(x, y, x + curr.intrinsicWidth, y + h)
            }
        }

        /**
         * Constructs and initializes an instance of `SampleView`. First we call through to our
         * super's constructor. Then we initialize our field `AppCompatActivity` field `mActivity`
         * with our `AppCompatActivity` parameter `activity`, and (for no good reason) we also set
         * our `Context` variable `context` to it. We enable our view to receive focus.
         *
         * We initialize our ``Drawable`` field `mDrawable` with the drawable from the resource file
         * R.drawable.btn_default_normal. We initialize our `Int` field `mHeightOffset` to 55 and if
         * the intrinsic height `heightOfDrawable` of `mDrawable` is greater than 55, we set
         * `mHeightOffset` to `heightOfDrawable + 5`. We set the bounding rectangle of `mDrawable`
         * to (0,0,150,48) (the left,top,right,bottom location it will draw in when its `draw()`
         * method is called), and set its dither flag to true (This is ignored but what the hey!)
         *
         * We initialize `IntArray` `val resIDs` with an array containing the resource IDs for our
         * three other buttons: R.drawable.btn_circle_normal, R.drawable.btn_check_off, and
         * R.drawable.btn_check_on, then initialize our field `Drawable` array field `mDrawables`
         * with an array large enough to hold them when we fetch them. We then initialize `Drawable`
         * `var prev` to `mDrawable` and loop through the resource IDs in `resIDs` first fetching
         * each `Drawable` from our resources to the appropriate position ifn `mDrawables`, setting
         * its dither flag to true (ignored of course, but why not?) and calling our method
         * `addToTheRight` to add the present `mDrawables` to the right of `prev`, after which we
         * set `prev` to the present `mDrawables` to get ready for the next pass through the loop.
         *
         * We initialize our `Paint` field `mPaint` with a new instance, set its antialias flag
         * to true, its text size to 16, and its text alignment to CENTER. We create a copy of
         * `mPaint` to initialize `Paint` field `mPaint2`, and set its alpha channel to 64. We
         * fetch the font metrics of `mPaint` to `Paint.FontMetrics` `val fm`, and calculate the
         * value of our `Float` field `mPaintTextOffset` to be 1/2 of the sum of the ascent and
         * descent of `fm`. We allocate an array of `Int` for our `IntArray` field `mColors` and
         * initialize it with the colors: 0, 0xCC0000FF, 0x880000FF, 0x440000FF, 0xFFCCCCFF,
         * 0xFF8888FF, and 0xFF4444FF.
         *
         * We initialize our `PorterDuff.Mode` array filed `mModes` with an array containing the
         * modes PorterDuff.Mode.SRC_ATOP, and PorterDuff.Mode.MULTIPLY and set the index into that
         * array `mModeIndex` to 0. Finally we call our method `updateTitle()` to set the text of
         * our activity's title bar to the string value of SRC_ATOP.
         *
         * Parameter: `AppCompatActivity` used for the `Context` when fetching resources, "this"
         * when called from `onCreate` of our activity.
         */
        init {
            val context: Context = mActivity
            isFocusable = true
            mDrawable = ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.btn_default_normal,
                null
            )!!
            mHeightOffset = 55
            val heightOfDrawable = mDrawable.intrinsicHeight
            Log.i(TAG, "Height of drawable: $heightOfDrawable")
            if (heightOfDrawable > 55) {
                mHeightOffset = heightOfDrawable + 5
            }
            mDrawable.setBounds(0, 0, 150, 48)
            // mDrawable.setDither(true) this is ignored starting when?
            val resIDs = intArrayOf(
                    R.drawable.btn_circle_normal,
                    R.drawable.btn_check_off,
                    R.drawable.btn_check_on
            )
            mDrawables = arrayOfNulls(resIDs.size)
            var prev: Drawable? = mDrawable
            for (i in resIDs.indices) {
                mDrawables[i] = ResourcesCompat.getDrawable(resources, resIDs[i], null)!!
                // mDrawables[i]!!.setDither(true) this is ignored starting when?
                addToTheRight(mDrawables[i], prev)
                prev = mDrawables[i]
            }
            mPaint = Paint()
            mPaint.isAntiAlias = true
            mPaint.textSize = 16f
            mPaint.textAlign = Paint.Align.CENTER
            mPaint2 = Paint(mPaint)
            mPaint2.alpha = 64
            val fm = mPaint.fontMetrics
            mPaintTextOffset = (fm.descent + fm.ascent) * 0.5f
            mColors = intArrayOf(
                    0,
                    -0x33ffff01,
                    -0x77ffff01,
                    0x440000FF,
                    -0x333301,
                    -0x777701,
                    -0xbbbb01)
            mModes = arrayOf(
                    PorterDuff.Mode.SRC_ATOP,
                    PorterDuff.Mode.MULTIPLY)
            mModeIndex = 0
            updateTitle()
        }
    }

    /**
     * Our static constant
     */
    companion object {
        /**
         * TAG used for logging
         */
        const val TAG = "ColorFilters"
    }
}
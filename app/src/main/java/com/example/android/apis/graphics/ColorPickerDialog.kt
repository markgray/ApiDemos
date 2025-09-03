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
import android.app.Dialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * Custom `Dialog` to pick colors, used only by `FingerPaint`
 * Constructor for our [ColorPickerDialog] instance. First we call through to our super's
 * constructor, then we save our `OnColorChangedListener` parameter `listener` in our field
 * `mListener`, and `initialColor` in our field `mInitialColor`.
 *
 * @param context      `Context` to use for resources, in our case "this" when called from
 * `FingerPaint`'s `onOptionsItemSelected` override
 * @property mListener     `OnColorChangedListener` whose `colorChanged` method we should
 * call when user has selected a color, in our case "this" when called from
 * `FingerPaint`'s `onOptionsItemSelected` override
 * @property mInitialColor initial color currently being used by `FingerPaint`
 */
class ColorPickerDialog(
    context: Context?,
    private val mListener: OnColorChangedListener,
    private val mInitialColor: Int
) : Dialog(context!!) {
    /**
     * This interface defines the method `colorChanged` which we will call when the user has
     * chosen a new color
     */
    interface OnColorChangedListener {
        fun colorChanged(color: Int)
    }

    /**
     * Called when the `Dialog` is starting. First we call through to our super's implementation
     * of `onCreate`. Next we create an anonymous `OnColorChangedListener` class `val l` which
     * will call the `OnColorChangedListener.colorChanged` method in `FingerPaint` when
     * its own `colorChanged` method is called, then `dismiss` the `ColorPickerDialog`. It sets
     * the background of our `Window` to the transparent color android.R.color.transparent.
     * It then sets the content view of our dialog to a new instance of `ColorPickerView` created
     * using the `Context` this dialog is running in, the [OnColorChangedListener] `l`, and our
     * field [mInitialColor]. Finally we set our title to "Pick a Color".
     *
     * @param savedInstanceState we do not override [onSaveInstanceState]` so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val l: OnColorChangedListener = object : OnColorChangedListener {
            override fun colorChanged(color: Int) {
                mListener.colorChanged(color)
                dismiss()
            }
        }
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        setContentView(ColorPickerView(context, l, mInitialColor))
        setTitle("Pick a Color")
    }

    /**
     * Custom View that draws a color wheel that the user can choose a color from.
     */
    private class ColorPickerView(
        context: Context?,
        /**
         * The `OnColorChangedListener` we were constructed with, its `colorChanged` method
         * will be called once the user the has clicked "select color and dismiss" circle at
         * the center of the color wheel. Do not confuse with `ColorPickerDialog.mListener`.
         */
        private val mListener: OnColorChangedListener,
        color: Int
    ) : View(context) {
        /**
         * [Paint] with a [SweepGradient] shader used to draw a color spectrum circular
         * oval. Any user finger movement reported to our `onTouchEvent` callback that is found
         * to be located on that oval selects the color underneath the finger to be the new color
         * for our [Paint] field [mCenterPaint].
         */
        private val mPaint: Paint

        /**
         * [Paint] used to draw the "select color and dismiss" circle at the center of the color wheel.
         * It is set to the current color, either the one we are constructed with, or the one the user
         * has selected on the color wheel.
         */
        private val mCenterPaint: Paint

        /**
         * Colors used to construct the `SweepGradient` that is used as the shader for
         * [Paint] field [mPaint], which is used to draw the color wheel.
         */
        private val mColors: IntArray = intArrayOf(
            -0x10000, -0xff01, -0xffff01, -0xff0001,
            -0xff0100, -0x100, -0x10000
        )

        /**
         * Flag indicating whether the finger's last ACTION_DOWN [MotionEvent] was in the center
         * circle used to "select the current color" and is used to keep track of the finger movement
         * while waiting for an ACTION_UP also in the center circle to indicate the selection is
         * complete. This allows the user to move his finger out of the center circle if he changes
         * his mind and wishes to continue the search for a color.
         */
        private var mTrackingCenter = false

        /**
         * Flag indicating that a halo should be drawn around the center circle, it is set to the
         * [Boolean] value `inCenter` (a flag indicating the location is in the center circle)
         * when an ACTION_DOWN event occurs, and set to false if an ACTION_UP event occurs outside
         * the center circle (the finger has moved on)
         */
        private var mHighlightCenter = false

        /**
         * We implement this to do our drawing. First we calculate the [Float] radius `val r` of the
         * color spectrum circle, move the [Canvas] parameter [canvas] to the center of our view as
         * given by (CENTER_X, CENTER_X), draw our color spectrum circle using [Paint] field [mPaint],
         * and draw the center "select and dismiss" circle using [Paint] field [mCenterPaint]. Then
         * if our [Boolean] flag field [mTrackingCenter] is true (the last ACTION_DOWN event was in
         * the center circle), we first save the current color of [Paint] field [mCenterPaint] in
         * `val c`, then we set its style to STROKE, and if the [Boolean] field [mHighlightCenter]
         * flag is true (which it is after an ACTION_DOWN in the center circle and until an
         * ACTION_MOVE outside the circle) we set the alpha to the max 0xFF, otherwise we set it to
         * 0x80 (this has the effect of lightening the "halo" if the finger moves outside the center).
         * We then draw the center circle "halo" using [Paint] field [mCenterPaint]. Finally we reset
         * the style of [mCenterPaint] to FILL, and the color to the saved color `c`.
         *
         * @param canvas the [Canvas] on which the background will be drawn
         */
        @SuppressLint("DrawAllocation")
        override fun onDraw(canvas: Canvas) {
            val r = CENTER_X - mPaint.strokeWidth * 0.5f
            canvas.translate(CENTER_X.toFloat(), CENTER_X.toFloat())
            canvas.drawOval(RectF(-r, -r, r, r), mPaint)
            canvas.drawCircle(0f, 0f, CENTER_RADIUS.toFloat(), mCenterPaint)
            if (mTrackingCenter) {
                val c = mCenterPaint.color
                mCenterPaint.style = Paint.Style.STROKE
                if (mHighlightCenter) {
                    mCenterPaint.alpha = 0xFF
                } else {
                    mCenterPaint.alpha = 0x80
                }
                canvas.drawCircle(
                    0f, 0f,
                    CENTER_RADIUS + mCenterPaint.strokeWidth,
                    mCenterPaint
                )
                mCenterPaint.style = Paint.Style.FILL
                mCenterPaint.color = c
            }
        }

        /**
         * Measure the [View] and its content to determine the measured width and the measured
         * height. This method is invoked by [measure] and should be overridden by subclasses to
         * provide accurate and efficient measurement of their contents.
         *
         * We simply call [setMeasuredDimension] with a width of twice `CENTER_X`, and
         * a height of twice `CENTER_Y`.
         *
         * @param widthMeasureSpec  horizontal space requirements as imposed by the parent.
         * The requirements are encoded with [android.view.View.MeasureSpec].
         * @param heightMeasureSpec vertical space requirements as imposed by the parent.
         * The requirements are encoded with [android.view.View.MeasureSpec].
         */
        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            setMeasuredDimension(CENTER_X * 2, CENTER_Y * 2)
        }

        /**
         * We Implement this method to handle touch screen motion events. We first calculate [Float]
         * `val x` (the x coordinate) and `val y` (the y coordinate) of the [MotionEvent] parameter
         * [event]. We set the [Boolean] flag `val inCenter` based on a geometric calculation to
         * determine if the event occurred inside the center "select and dismiss" circle. We then
         * switch based on the action of the `MotionEvent event`:
         *
         *  * ACTION_DOWN - we set our flag `boolean mTrackingCenter` to the value of `inCenter`,
         *  and if the event occurred in the center circle we set our [Boolean] field [mHighlightCenter]
         *  to true and invalidate our view so it will be redrawn (this time with a "halo") and we
         *  return. If it was not in the center circle we check if our [mTrackingCenter] field is
         *  *true* (the previous ACTION_DOWN was in the center circle, but the current on is outside)
         *  and if so we check if the center circle is highlighted but should not be ([mHighlightCenter]
         *  is not equal to `inCenter`) in which case we want to set [mHighlightCenter] to `inCenter`
         *  and invalidate to remove the "halo". If the ACTION_DOWN occurs when [mTrackingCenter] is
         *  false we want to calculate where on the color circle the finger is, interpolate the
         *  color corresponding to that position, we the color of [mCenterPaint] to that new color
         *  and invalidate so that the center circle will be redrawn with the new color.
         *
         *  * ACTION_MOVE - if our [Boolean] field [mTrackingCenter] is true (the last ACTION_DOWN
         *  occurred in the center circle) we check to see if [Boolean] field [mHighlightCenter] is
         *  not equal to `inCenter` (the finger has moved in or out of the center circle) and if
         *  they are not we set [mHighlightCenter] to `inCenter` and invalidate the view so it will
         *  be redrawn with the new "halo" setting. If [mTrackingCenter] is false the last ACTION_DOWN
         *  was outside the center circle and the movement is intended to select a color from the
         *  color spectrum circle, so we determine the angle on the spectrum circle based on the x
         *  and y coordinate, convert that angle to a [Float] `var unit` between 0 and 1, then set
         *  the color of [Paint] field [mCenterPaint] to the color calculated by our method
         *  [interpColor] and invalidate our view causing it to be redrawn with the new color for
         *  the center circle.
         *
         *  * ACTION_UP - If our [Boolean] flag field [mTrackingCenter] is true (the last ACTION_DOWN
         *  occurred in the center circle) we check to see if the current event occurred still in
         *  the circle and if so we call our [OnColorChangedListener] field [mListener] callback
         *  `colorChanged` method to select the current color and dismiss the dialog. If the finger
         *  is now outside the center circle (the user changed his mind), we set [Boolean] field
         *  [mTrackingCenter] to false (the center circle will be drawn without the "halo") and
         *  invalidate our view causing it to be redrawn.
         *
         * Finally we return true to indicate that we have handled the [MotionEvent].
         *
         * @param event The motion event.
         * @return True if the event was handled, false otherwise.
         */
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(event: MotionEvent): Boolean {
            val x = event.x - CENTER_X
            val y = event.y - CENTER_Y
            val inCenter = hypot(x.toDouble(), y.toDouble()) <= CENTER_RADIUS
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mTrackingCenter = inCenter
                    if (inCenter) {
                        mHighlightCenter = true
                        invalidate()
                        return true
                    }
                    @Suppress("KotlinConstantConditions")
                    if (mTrackingCenter) {
                        if (mHighlightCenter != inCenter) {
                            mHighlightCenter = inCenter
                            invalidate()
                        }
                    } else {
                        val angle = atan2(y.toDouble(), x.toDouble()).toFloat()
                        // need to turn angle [-PI ... PI] into unit [0....1]
                        var unit = angle / (2 * PI)
                        if (unit < 0) {
                            unit += 1f
                        }
                        mCenterPaint.color = interpColor(mColors, unit)
                        invalidate()
                    }
                }

                MotionEvent.ACTION_MOVE -> if (mTrackingCenter) {
                    if (mHighlightCenter != inCenter) {
                        mHighlightCenter = inCenter
                        invalidate()
                    }
                } else {
                    val angle = atan2(y.toDouble(), x.toDouble()).toFloat()
                    var unit = angle / (2 * PI)
                    if (unit < 0) {
                        unit += 1f
                    }
                    mCenterPaint.color = interpColor(mColors, unit)
                    invalidate()
                }

                MotionEvent.ACTION_UP -> if (mTrackingCenter) {
                    if (inCenter) {
                        mListener.colorChanged(mCenterPaint.color)
                    }
                    mTrackingCenter = false // so we draw w/o halo
                    invalidate()
                }
            }
            return true
        }

        /**
         * Converts a floating point Red, Green or Blue color value to a rounded [Int]. It is
         * only used in the unused method `rotateColor` so it is itself unused in actuality.
         *
         * @param x [Float] value to round to a [Int]
         * @return rounded [Int] version of [x]
         */
        private fun floatToByte(x: Float): Int {
            return x.roundToInt()
        }

        /**
         * Clamps the argument to the byte range 0 to 255. It is only used in the unused method
         * `rotateColor` so it is itself unused in actuality.
         *
         * @param n [Int] value to be "clamped" to the byte range 0 to 255.
         * @return [n] clamped to the range 0 to 255
         */
        private fun pinToByte(n: Int): Int {
            var nTmp = n
            if (nTmp < 0) {
                nTmp = 0
            } else if (nTmp > 255) {
                nTmp = 255
            }
            return nTmp
        }

        /**
         * Interpolates between two different color channel values based on the [0..1) factor
         * of [Float] parameter [p].
         *
         * @param s First alpha, red, green, or blue color value 0-255
         * @param d Second alpha, red, green, or blue  color value 0-255
         * @param p location [0..1) between `s` and `d`
         * @return a color channel value 0-255 interpolated between `s` and `d`
         */
        private fun ave(s: Int, d: Int, p: Float): Int {
            return s + (p * (d - s)).roundToInt()
        }

        /**
         * Converts a position on the color spectrum circle to the color that is at that position.
         *
         * @param colors array of color spectrum colors to interpolate between
         * @param unit   where within the color wheel we are to choose a color
         * @return the color chosen by the user's finger location
         */
        private fun interpColor(colors: IntArray, unit: Float): Int {
            if (unit <= 0) {
                return colors[0]
            }
            if (unit >= 1) {
                return colors[colors.size - 1]
            }
            var p = unit * (colors.size - 1)
            val i = p.toInt()
            p -= i.toFloat()
            // now p is just the fractional part [0...1) and i is the index
            val c0 = colors[i]
            val c1 = colors[i + 1]
            val a = ave(Color.alpha(c0), Color.alpha(c1), p)
            val r = ave(Color.red(c0), Color.red(c1), p)
            val g = ave(Color.green(c0), Color.green(c1), p)
            val b = ave(Color.blue(c0), Color.blue(c1), p)
            return Color.argb(a, r, g, b)
        }

        /**
         * Unused, so I will skip commenting it.
         *
         * @param color color to be rotated
         * @param rad   angle to rotate the color
         * @return color rotated by the angle `float rad`
         */
        @Suppress("unused")
        private fun rotateColor(color: Int, rad: Float): Int {
            val deg = rad * 180 / 3.1415927f
            val r = Color.red(color)
            val g = Color.green(color)
            val b = Color.blue(color)
            val cm = ColorMatrix()
            val tmp = ColorMatrix()
            cm.setRGB2YUV()
            tmp.setRotate(0, deg)
            cm.postConcat(tmp)
            tmp.setYUV2RGB()
            cm.postConcat(tmp)
            val a = cm.array
            val ir = floatToByte(a[0] * r + a[1] * g + a[2] * b)
            val ig = floatToByte(a[5] * r + a[6] * g + a[7] * b)
            val ib = floatToByte(a[10] * r + a[11] * g + a[12] * b)
            return Color.argb(
                Color.alpha(color), pinToByte(ir),
                pinToByte(ig), pinToByte(ib)
            )
        }

        companion object {
            /**
             * x coordinate of the center of the center circle
             */
            private var CENTER_X = 100

            /**
             * y coordinate of the center of the center circle
             */
            private var CENTER_Y = 100

            /**
             * radius of the center circle
             */
            private var CENTER_RADIUS = 32

            /**
             * constant pi value used for circle calculations when a color on the wheel is being selected
             */
            private const val PI = 3.1415925f

            var density: Float = 1.0f
        }

        /**
         * We initialize our `Shader` variable `val s` with a `SweepGradient` which uses the array
         * of colors in our field `mColors` as the colors to be distributed between around the
         * center in order to describe a color wheel spectrum. We initialize our `Float` field
         * `density` to the logical density of the display (this is a scaling factor for the Density
         * Independent Pixel unit, where one DIP is one pixel on an approximately 160 dpi screen).
         * We allocate an instance of `Paint` with the anti-alias flag set to initialize our `Paint`
         * field `mPaint`, set its Shader to `s`, set the style to STROKE, and set the stroke width
         * to 32 times `density`.  We allocate an instance of `Paint` with the anti-alias flag set
         * to initialize our `Paint` field `mCenterPaint`, set its color to our parameter `color`,
         * and set the stroke width to 5 times `density`. We then scale our fields `CENTER_X`,
         * `CENTER_Y` and `CENTER_RADIUS` by `density`.
         */
        init {
            val s: Shader = SweepGradient(0f, 0f, mColors, null)
            density = context!!.resources.displayMetrics.density
            mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            mPaint.shader = s
            mPaint.style = Paint.Style.STROKE
            mPaint.strokeWidth = 32f * density
            mCenterPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            mCenterPaint.color = color
            mCenterPaint.strokeWidth = 5f * density
            CENTER_X = (100 * density).toInt()
            CENTER_Y = (100 * density).toInt()
            CENTER_RADIUS = (32 * density).toInt()
        }
    }

}
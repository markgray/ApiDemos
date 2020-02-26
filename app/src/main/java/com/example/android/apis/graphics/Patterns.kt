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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.view.MotionEvent
import android.view.View

/**
 * Creates two [Bitmap]'s: a blue rectangle on a red background, and a green circle on a clear
 * background. It uses these [Bitmap]'s to make two [BitmapShader]'s, and it rotates the Circle
 * [BitmapShader] by 30 degrees. In the [SampleView.onDraw] method it first draws using the
 * rectangle pattern, translate's the [Canvas] based on the current [MotionEvent] movement and
 * draws using the circle pattern. The effect is to allow you to move the circle pattern with
 * your finger while leaving the rectangle pattern stationary and partially visible through the
 * circle pattern on top.
 */
class Patterns : GraphicsActivity() {
    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we set our content view to a new instance of [SampleView].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(SampleView(this))
    }

    /**
     * A custom [View] consisting of two [Shader] objects, one stationary consisting of
     * a rectangle pattern, and one movable on top of it consisting of a circle pattern.
     */
    private class SampleView(context: Context?) : View(context) {
        /**
         * [Shader] consisting of a pattern of blue rectangles on a red background.
         */
        private val mShader1: Shader
        /**
         * [Shader] consisting of a pattern of green circles on an uncolored background.
         */
        private val mShader2: Shader
        /**
         * [Paint] used to draw to our [Canvas]
         */
        private val mPaint: Paint
        /**
         * [PaintFlagsDrawFilter] that clears the [Paint.FILTER_BITMAP_FLAG] and [Paint.DITHER_FLAG]
         * of the [Paint] used to draw to the [Canvas] when it is set as the draw filter of that
         * [Canvas]. [DrawFilter] field [mDF] is set to it when an ACTION_DOWN touch event is
         * received (and [mDF] is set to *null* on an ACTION_UP event). See [mDF].
         */
        private val mFastDF: DrawFilter
        /**
         * x coordinate of the last ACTION_DOWN event
         */
        private var mTouchStartX = 0f
        /**
         * y coordinate of the last ACTION_DOWN event
         */
        private var mTouchStartY = 0f
        /**
         * x coordinate of the last ACTION_MOVE event
         */
        private var mTouchCurrX = 0f
        /**
         * y coordinate of the last ACTION_MOVE event
         */
        private var mTouchCurrY = 0f
        /**
         * [DrawFilter] that is used as the draw filter of the [Canvas] we are drawing to.
         * It is set to [DrawFilter] field [mFastDF] on an ACTION_DOWN event and to *null*
         * on an ACTION_UP event.
         */
        private var mDF: DrawFilter? = null

        /**
         * We implement this to do our drawing. First we set the draw filter of our [Canvas]
         * parameter [canvas] to our [DrawFilter] field [mDF] (this will either be the
         * contents of our [DrawFilter] field [mFastDF] after an ACTION_DOWN event or *null*
         * after an ACTION_UP event). We set the [Shader] object of [Paint] field [mPaint] to
         * [mShader1] (blue rectangles on a red background) and fill the bitmap of the [Canvas]
         * parameter [canvas]  with that pattern. Then we move [canvas] by the movement in x and
         * y implied by the last ACTION_MOVE event ([mTouchCurrX] minus [mTouchStartX] in the x
         * direction and [mTouchCurrY] minus [mTouchStartY] in the y direction). We set the
         * [Shader] object of [Paint] field [mPaint] to [mShader2] (green circles on an uncolored
         * background) and fill the bitmap of [Canvas] parameter [canvas] with that pattern. The
         * rectangle pattern of [mShader1] will show through the uncolored background of [mShader1].
         *
         * @param canvas the [Canvas] on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            canvas.drawFilter = mDF
            mPaint.shader = mShader1
            canvas.drawPaint(mPaint)
            canvas.translate(mTouchCurrX - mTouchStartX,
                    mTouchCurrY - mTouchStartY)
            mPaint.shader = mShader2
            canvas.drawPaint(mPaint)
        }

        /**
         * Implement this method to handle touch screen motion events. First we fetch the x
         * coordinate of the [MotionEvent] parameter [event] to [Float] `val x` and the y
         * coordinate to [Float] `val y`. Then we switch based on the kind of action of [event]:
         *
         *  * ACTION_DOWN - We set our fields [mTouchStartX] and [mTouchCurrX] to `x` and our
         *  fields [mTouchStartY] and [mTouchCurrY] to `y`. We set our [DrawFilter] field [mDF]
         *  to the contents of [DrawFilter] field [mFastDF], and invalidate  our view so that our
         *  [onDraw] method will be called.
         *
         *  * ACTION_MOVE - We set our field [mTouchCurrX] to x, and [mTouchCurrY]
         *  to y and invalidate our view so that our [onDraw] method will be called.
         *
         *  * ACTION_UP - We set our [DrawFilter] field [mDF] to *null* and invalidate our
         *  view so that our [onDraw] method will be called.
         *
         * In all cases we return *true* to the caller to indicate that we handled the [MotionEvent]
         *
         * @param event The motion event.
         * @return *true* if the event was handled, *false* otherwise. We always return *true*.
         */
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(event: MotionEvent): Boolean {
            val x = event.x
            val y = event.y
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    run {
                        mTouchCurrX = x
                        mTouchStartX = mTouchCurrX
                    }
                    run {
                        mTouchCurrY = y
                        mTouchStartY = mTouchCurrY
                    }
                    mDF = mFastDF
                    invalidate()
                }
                MotionEvent.ACTION_MOVE -> {
                    mTouchCurrX = x
                    mTouchCurrY = y
                    invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    mDF = null
                    invalidate()
                }
            }
            return true
        }

        /**
         * The init block of our constructor. We enable our view to receive focus, and to receive
         * focus in touch mode. We initialize our `DrawFilter` field `mFastDF` with a new instance
         * of `PaintFlagsDrawFilter` configured to clear the `Paint` flags Paint.FILTER_BITMAP_FLAG
         * and Paint.DITHER_FLAG. We initialize our `Shader` field `mShader1` with a new instance of
         * `BitmapShader` created using the `Bitmap` returned from our method `makeBitmap1`
         * (a blue rectangle with a red background) and configured to repeat in both the x and y
         * directions. We initialize our `Shader` field `mShader2` with a new instance of
         * `BitmapShader` created using the `Bitmap` returned from our method `makeBitmap2`
         * (a green circle with an uncolored background) and configured to repeat in both the
         * x and y directions.
         *
         * We create a new instance for `Matrix` for `val m`, set the matrix to rotate about (0,0)
         * by 30 degrees, and use it to set the local matrix of `Shader` field `mShader2` (rotates
         * the pattern by 30 degrees when it is drawn).
         *
         * Finally we allocate a new instance of `Paint` for our `Paint` field `mPaint`, setting
         * the Paint.FILTER_BITMAP_FLAG on it (enables bilinear sampling on scaled bitmaps).
         */
        init {
            isFocusable = true
            isFocusableInTouchMode = true
            mFastDF = PaintFlagsDrawFilter(Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG, 0)
            mShader1 = BitmapShader(makeBitmap1(), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
            mShader2 = BitmapShader(makeBitmap2(), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
            val m = Matrix()
            m.setRotate(30f)
            mShader2.setLocalMatrix(m)
            mPaint = Paint(Paint.FILTER_BITMAP_FLAG)
        }
    }

    companion object {
        /**
         * Creates and returns a 40x40 pixel [Bitmap] containing a single blue 30x30 rectangle at
         * the center of its red background. First we create a new instance of a 40x40 [Bitmap]
         * for [Bitmap] variable `val bm`. We create a [Canvas] for `val c` that uses `bm` to draw
         * into and set the entire canvas to RED. We create a new instance of [Paint] for [Paint]
         * variable `val p` and set its color to BLUE. We use `p` to draw a rectangle on `c` whose
         * top left corner is at (5,5), and whose bottom right corner is at (35,35). We then return
         * the [Bitmap] `bm` that now contains that rectangle.
         *
         * @return 40x40 pixel [Bitmap] containing a single blue 30x30 rectangle at the center of
         * its red background.
         */
        private fun makeBitmap1(): Bitmap {
            val bm = Bitmap.createBitmap(40, 40, Bitmap.Config.RGB_565)
            val c = Canvas(bm)
            c.drawColor(Color.RED)
            val p = Paint()
            p.color = Color.BLUE
            c.drawRect(5f, 5f, 35f, 35f, p)
            return bm
        }

        /**
         * Creates and returns a 64x64 pixel [Bitmap] containing a single GREEN circle of radius 27
         * at the center of its uncolored background. First we create a new instance of a 64x64
         * [Bitmap] for [Bitmap] variable `val bm`. We create a [Canvas] `val c` that uses `bm` to
         * draw into. We create a new instance of [Paint] for [Paint] variable `val p` and set its
         * color to GREEN, and its alpha to 0xCC. We use `p` to draw a circle on `c` whose center
         * is at (32,32) and whose radius is 27. We then return the [Bitmap] `bm` that now contains
         * that circle.
         *
         * @return 64x64 pixel [Bitmap] containing a single GREEN circle of radius 27 at the center
         * of its uncolored background.
         */
        private fun makeBitmap2(): Bitmap {
            val bm = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
            val c = Canvas(bm)
            val p = Paint(Paint.ANTI_ALIAS_FLAG)
            p.color = Color.GREEN
            p.alpha = 0xCC
            c.drawCircle(32f, 32f, 27f, p)
            return bm
        }
    }
}
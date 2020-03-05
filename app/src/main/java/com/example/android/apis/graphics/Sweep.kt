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
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View

/**
 * Animates a SweepGradient Shader by rotating its setLocalMatrix(Matrix M), used to paint a circle.
 */
class Sweep : GraphicsActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to a new instance of [SampleView].
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        densityScale = resources.displayMetrics.density
        setContentView(SampleView(this))
    }

    /**
     * Our custom view, which simply draws a circle with an animated [SweepGradient] as its [Shader].
     */
    private class SampleView(context: Context?) : View(context) {
        /**
         * [Paint] used to draw our circle.
         */
        private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        /**
         * Angle in degrees to rotate our [Shader] field [mShader], advanced by 3 degrees every time
         * our [onDraw] method is called.
         */
        private var mRotate = 0f

        /**
         * [Matrix] we use to rotate our [Shader] field [mShader].
         */
        private val mMatrix = Matrix()

        /**
         * [SweepGradient] we use as [Shader] for our [Paint] field [mPaint].
         */
        private val mShader: Shader

        /**
         * Flag that enables timing how long it takes to draw our circle 20 times (can only be toggled
         * if you have a keyboard).
         */
        private var mDoTiming = false

        /**
         * We implement this to do our drawing. First we make a local copy of [Paint] field [mPaint]
         * for `val paint`, initialize `val x` to 160f, and `val y` to 100f. We set the color of the
         * entire [Canvas] parameter [canvas] to WHITE. We set [Matrix] field [mMatrix] to rotate
         * by field [mRotate] degrees around the point `(x,y)`, and set the local matrix of [Shader]
         * field [mShader] to it, then set the shader of [Paint] field [mPaint] to [mShader]. We
         * increment [mRotate] by 3 degrees and if the result is greater than or equal to 360 we set
         * it to 0. We then invalidate the view so we will be called again sometime in the future.
         *
         * If our [mDoTiming] flag is true, we set `var now` to the current time in milliseconds
         * since boot, loop drawing our circle 20 times, calculate how long this took and log the
         * result. If [mDoTiming] is false we simply use the [Paint] `paint` to draw a circle with a
         * radius of 80 pixels around the point (x,y).
         *
         * @param canvas the [Canvas] on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            val paint = mPaint
            val x = 160f * densityScale
            val y = 100f * densityScale
            canvas.drawColor(Color.WHITE)
            mMatrix.setRotate(mRotate, x, y)
            mShader.setLocalMatrix(mMatrix)
            mPaint.shader = mShader
            mRotate += 3f
            if (mRotate >= 360) {
                mRotate = 0f
            }
            invalidate()
            if (mDoTiming) {
                var now = System.currentTimeMillis()
                for (i in 0..19) {
                    canvas.drawCircle(x, y, 80f * densityScale, paint)
                }
                now = System.currentTimeMillis() - now
                Log.d("skia", "sweep ms = " + now / 20.0)
            } else {
                canvas.drawCircle(x, y, 80f * densityScale, paint)
            }
        }

        /**
         * Called when a key down event has occurred. If the key is KEYCODE_D, we toggle the DITHER_FLAG
         * of our `Paint mPaint`, invalidate the view and return true. If the key is KEYCODE_T,
         * we toggle our `mDoTiming` flag, invalidate the view and return true. Otherwise we
         * return whatever our super's implementation of `onKeyDown` returns.
         *
         * @param keyCode A key code that represents the button pressed.
         * @param event   The KeyEvent object that defines the button action.
         * @return true if we have consumed the `KeyEvent`.
         */
        override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
            when (keyCode) {
                KeyEvent.KEYCODE_D -> {
                    mPaint.isDither = !mPaint.isDither
                    invalidate()
                    return true
                }
                KeyEvent.KEYCODE_T -> {
                    mDoTiming = !mDoTiming
                    invalidate()
                    return true
                }
            }
            return super.onKeyDown(keyCode, event)
        }

        companion object {
            /**
             * The colors used to create our `SweepGradient`.
             */
            val COLORS = intArrayOf(Color.GREEN, Color.RED, Color.BLUE, Color.GREEN)
        }

        /**
         * The init block of our constructor. First we enable our view to receive focus, and to
         * receive focus in touch mode. We initialize `x` to 160 and `y` to 100 and use them to
         * create a `SweepGradient` to initialize our `Shader` field `mShader` with the center at
         * (x,y), using the array `COLORS` as the colors to be evenly distributed around the center.
         * Finally we set `mShader` as the shader of `Paint mPaint`.
         */
        init {
            isFocusable = true
            isFocusableInTouchMode = true
            val x = 160f * densityScale
            val y = 100f * densityScale
            mShader = SweepGradient(x, y, COLORS, null)
            mPaint.shader = mShader
        }
    }

    companion object {
        var densityScale = 1f
    }
}
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
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import com.example.android.apis.R

/**
 * Uses ColorMatrixColorFilter's to create three different versions of a jpeg, animating them
 * through different "contrasts". One changes both scale and translate, one changes scale only,
 * and one changes translate only. (The original is at top of left column, scale and translate
 * to right of it, scale only second row, and translate only is in the third row.)
 */
class ColorMatrixSample : GraphicsActivity() {
    /**
     * First we call through to our super's implementation of `onCreate`, then we set our
     * content view to a new instance of [SampleView].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(SampleView(this))
    }

    /**
     * Custom [View] class which displays the same jpg (R.drawable.balloons) four different ways,
     * one without any animation of the [ColorMatrix] used to draw it, and three with the
     * [ColorMatrix] used to draw it animated in different ways.
     */
    private class SampleView(context: Context) : View(context) {
        /**
         * Apparently created to prevent "draw allocation" warning, but warning is issued for the
         * allocation of [ColorMatrixColorFilter] any way? Used only in [onDraw] method and then
         * only after copying it to [Paint] variable `val paint`.
         */
        private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        /**
         * [Bitmap] of our resource jpg R.drawable.balloons
         */
        private val mBitmap: Bitmap =
                BitmapFactory.decodeResource(context.resources, R.drawable.balloons)
        /**
         * Animated angle [-180...180] incremented in steps of 2 degrees round robin every time the
         * [onDraw] method is called. It is used to create a contrast value of [-1..1], which is
         * used as an argument to our methods [setContrast], [setContrastScaleOnly], and
         * [setContrastTranslateOnly] which use it to modify the [ColorMatrixColorFilter] they are
         * passed as their second argument.
         */
        private var mAngle = 0f

        /**
         * We implement this to do our drawing. We make a local copy of our [Paint] field [mPaint]
         * (for no purpose that I can perceive), define [Float] variables `val x` and `val y` to
         * both be 20f (the location for the top drawing of [Bitmap] field [mBitmap]). We set the
         * entire [Canvas] parameter [canvas] to White, set the color filter of `paint` to be *null*
         * and draw our [Bitmap] field [mBitmap] at `(x,y)` using `paint`.
         *
         * New we allocate a new [ColorMatrix] for `val cm`, and advance our "animated angle" [Float]
         * field [mAngle] by 2 degrees, wrapping around to -180 if the result is greater than 180.
         * From [mAngle] we calculate a [Float] contrast factor `val contrast` to be `mAngle/180`.
         *
         * We then initialize [ColorMatrix] variable `cm` three different ways and use it to draw
         * [Bitmap] field [mBitmap] three more times:
         *
         *  * We call our method [setContrast] to set `cm` to use `contrast` to both scale and translate
         *  colors while drawing, set the color filter of [Paint] `paint` to a new copy of `cm` and
         *  again draw the [Bitmap] field [mBitmap] next to the first drawing offset by the width of
         *  [mBitmap] plus 10.
         *  * We call our method [setContrastScaleOnly] to set `cm` to use `contrast` to scale colors
         *  while drawing, set the color filter of [Paint] variable `paint` to a new copy of `cm` and
         *  again draw the [Bitmap] field [mBitmap] below the first drawing offset by the height of
         *  [mBitmap] plus 10.
         *  * We call our method [setContrastTranslateOnly] to set `cm` to use `contrast` to translate
         *  colors while drawing, set the color filter of [Paint] `paint` to a new copy of `cm` and
         *  again draw the [Bitmap] field [mBitmap] below the first drawing offset by twice the height
         *  of [mBitmap] plus 10.
         *
         * @param canvas the [Canvas] on which the background will be drawn
         */
        @SuppressLint("DrawAllocation")
        override fun onDraw(canvas: Canvas) {
            val paint = mPaint
            val x = 20f
            val y = 20f
            canvas.drawColor(Color.WHITE)
            paint.colorFilter = null
            canvas.drawBitmap(mBitmap, x, y, paint)
            val cm = ColorMatrix()
            mAngle += 2f
            if (mAngle > 180) {
                mAngle = -180f
            }
            //convert our animated angle [-180...180] to a contrast value of [-1..1]
            val contrast = mAngle / 180f
            setContrast(cm, contrast)
            paint.colorFilter = ColorMatrixColorFilter(cm)
            canvas.drawBitmap(mBitmap, x + mBitmap.width + 10, y, paint)
            setContrastScaleOnly(cm, contrast)
            paint.colorFilter = ColorMatrixColorFilter(cm)
            canvas.drawBitmap(mBitmap, x, y + mBitmap.height + 10, paint)
            setContrastTranslateOnly(cm, contrast)
            paint.colorFilter = ColorMatrixColorFilter(cm)
            canvas.drawBitmap(mBitmap, x, y + 2 * (mBitmap.height + 10), paint)
            invalidate()
        }

        /**
         * Our static methods
         */
        companion object {
            /**
             * Modifies its [ColorMatrix] parameter [cm] to be a [ColorMatrix] which changes the
             * colors by adding the [Float] parameters [dr], [dg], [db], and [da] to it. The results
             * when this matrix is applied when drawing are:
             *
             *  * `Red = 2*Red + dr`
             *  * `Green = 2*Green + dg`
             *  * `Blue = 2*Blue + db`
             *  * `Alpha = 1*Alpha + da`
             *
             * @param cm [ColorMatrix] we are to modify
             * @param dr Change in red component
             * @param dg Change in green component
             * @param db Change in blue component
             * @param da Change in alpha component
             */
            @Suppress("unused")
            private fun setTranslate(cm: ColorMatrix, dr: Float, dg: Float, db: Float, da: Float) {
                cm.set(floatArrayOf(
                        2f, 0f, 0f, 0f, dr,
                        0f, 2f, 0f, 0f, dg,
                        0f, 0f, 2f, 0f, db,
                        0f, 0f, 0f, 1f, da
                      )
                )
            }

            /**
             * Modifies its [ColorMatrix] parameter [cm] to be a [ColorMatrix] which changes the
             * colors according to the current [Float] contrast factor [contrast] by calculating
             * a [Float] `val scale` to multiply each color by, and a [Float] `val translate` to
             * add to each color.
             *
             * @param cm [ColorMatrix] we are to modify
             * @param contrast contrast factor (-1 .. 1)
             */
            private fun setContrast(cm: ColorMatrix, contrast: Float) {
                val scale = contrast + 1f
                val translate = (-.5f * scale + .5f) * 255f
                cm.set(floatArrayOf(
                        scale, 0f, 0f, 0f, translate,
                        0f, scale, 0f, 0f, translate,
                        0f, 0f, scale, 0f, translate,
                        0f, 0f, 0f, 1f, 0f
                       )
                )
            }

            /**
             * Modifies its [ColorMatrix] parameter [cm] to be a [ColorMatrix] which changes
             * the colors according to the current [Float] contrast factor [contrast] by calculating
             * a [Float] `val translate` to add to each color.
             *
             * @param cm [ColorMatrix] we are to modify
             * @param contrast contrast factor (-1 .. 1)
             */
            private fun setContrastTranslateOnly(cm: ColorMatrix, contrast: Float) {
                val scale = contrast + 1f
                val translate = (-.5f * scale + .5f) * 255f
                cm.set(floatArrayOf(1f, 0f, 0f, 0f, translate, 0f, 1f, 0f, 0f, translate, 0f, 0f, 1f, 0f, translate, 0f, 0f, 0f, 1f, 0f))
            }

            /**
             * Modifies its [ColorMatrix] parameter [cm] to be a [ColorMatrix] which changes
             * the colors according to the current [Float] contrast factor [contrast] by calculating
             * a [Float] `val scale` to multiply each color by.
             *
             * @param cm [ColorMatrix] we are to modify
             * @param contrast contrast factor (-1 .. 1)
             */
            private fun setContrastScaleOnly(cm: ColorMatrix, contrast: Float) {
                val scale = contrast + 1f
                @Suppress("UNUSED_VARIABLE")
                val translate = (-.5f * scale + .5f) * 255f
                cm.set(floatArrayOf(
                        scale, 0f, 0f, 0f, 0f, 0f,
                        scale, 0f, 0f, 0f, 0f, 0f,
                        scale, 0f, 0f, 0f, 0f, 0f, 1f, 0f
                ))
            }
        }

    }
}
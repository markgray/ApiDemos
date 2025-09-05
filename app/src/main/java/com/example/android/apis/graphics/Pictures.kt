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
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Picture
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View

/**
 * Shows how to use the [Picture] class to record drawing instructions performed on the [Canvas]
 * returned by [Picture.beginRecording], then after calling [Picture.endRecording] the Picture is
 * turned into a [Drawable] by calling [PictureDrawable] with that [Picture]. Then in the
 * [SampleView.onDraw] method we first draw the [Picture] using the [Canvas.drawPicture] method
 * then we stretch the [Picture] by drawing using the `Canvas.drawPicture(Picture,RectF)` method,
 * then we draw the [Drawable] we created using [PictureDrawable] on that [Picture] to draw using
 * [Drawable.draw] on the [Canvas].
 *
 * Before API 29 it would then write the Picture to a `ByteArrayOutputStream` and draw it by reading
 * that `ByteArrayOutputStream` back in and drawing it using the [Canvas.drawPicture] method that
 * used `Picture.createFromStream` but API 29 removed the long deprecated `Picture.writeToStream` and
 * `Picture.createFromStream`. The preferred method now is to draw the [Picture] into a `Bitmap`.
 */
class Pictures : GraphicsActivity() {
    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`
     * then we set our content view to a new instance of [SampleView].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(SampleView(context = this))
    }

    /**
     * Custom view demonstrating some of the features of the [Picture] class.
     *
     * @param context the [Context] of our activity, `this` in the `onCreate` override
     * (See our `init` block for the details of our constructor)
     */
    private class SampleView(context: Context?) : View(context) {
        /**
         * Our [Picture] consisting of a pink circle (the alpha of 0x88 lets the white background
         * lighten the red circle) with the green text "Pictures" partially obscuring it.
         */
        private val mPicture: Picture

        /**
         * [PictureDrawable] created out of [Picture] field [mPicture].
         */
        private val mDrawable: Drawable

        /**
         * We implement this to do our drawing. First we set the entire [Canvas] parameter [canvas]
         * to the color WHITE. Then we draw our [Picture] field [mPicture] 3 different ways:
         *
         *  * Using the [Canvas.drawPicture] method of [canvas]
         *
         *  * Using the [Canvas.drawPicture] method of [canvas] and specifying a [RectF]
         *  whose top left corner is at (0,100) and whose bottom right corner is at
         *  (`getWidth()`,200) where [getWidth] returns the width of our view. This
         *  has the effect of moving the [Picture] down 100 pixels and stretching it to
         *  fill our view.
         *
         *  * Setting the bounds of our [Drawable] field [mDrawable] version of [mPicture] to
         *  left of 0, top of 200, right of `getWidth` and bottom of 300, then using the
         *  [Drawable.draw] method of [mDrawable] to draw to the [Canvas] parameter [canvas]
         *
         * Notice the two different ways the [Picture] is located on the canvas, using
         * [RectF] to move it down by 100 pixels, and using [Drawable.setBounds] to move
         * it down by 200 pixels.
         *
         * @param canvas the [Canvas] on which the background will be drawn
         */
        @SuppressLint("DrawAllocation")
        override fun onDraw(canvas: Canvas) {
            canvas.drawColor(Color.WHITE)
            canvas.translate(0f, dpToPixel(160, context).toFloat())
            canvas.drawPicture(/* picture = */ mPicture)
            canvas.drawPicture(
                /* picture = */ mPicture,
                /* dst = */ RectF(0f, 100f, width.toFloat(), 200f)
            )
            mDrawable.setBounds(
                /* left = */ 0,
                /* top = */ 200,
                /* right = */ width,
                /* bottom = */ 300
            )
            mDrawable.draw(canvas)

            /* ***** ***** ******* writeToStream and createFromStream have been removed in API 29
 *          ByteArrayOutputStream os = new ByteArrayOutputStream();
 *          //noinspection deprecation
 *          mPicture.writeToStream(os);
 *          InputStream is = new ByteArrayInputStream(os.toByteArray());
 *          canvas.translate(0, 300);
 *          //noinspection deprecation
 *          canvas.drawPicture(Picture.createFromStream(is));
*/
        }

        /**
         * This method converts dp unit to equivalent pixels, depending on device density. First we
         * fetch a [Resources] instance for `val resources`, then we fetch the current display
         * metrics that are in effect for this resource object to [DisplayMetrics] `val metrics`.
         * Finally we return our [dp] parameter multiplied by the the screen density expressed as
         * dots-per-inch, divided by the reference density used throughout the system.
         *
         * @param dp      A value in dp (density independent pixels) unit which we need to convert
         *                into pixels
         * @param context [Context] to get resources and device specific display metrics
         * @return An [Int] value to represent px equivalent to dp depending on device density
         */
        @Suppress("SameParameterValue")
        private fun dpToPixel(dp: Int, context: Context): Int {
            val resources: Resources = context.resources
            val metrics = resources.displayMetrics
            return dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
        }

        companion object {
            /**
             * Draws a pink circle (the alpha of 0x88 lets the white background lighten the red
             * circle) with the green text "Pictures" partially obscuring it. First we allocate
             * a new instance of [Paint] for [Paint] variable `val p`, with the ANTI_ALIAS_FLAG
             * set. We set the color of `p` to red with an alpha of 0x88 and use it to draw a 40
             * pixel radius circle on [Canvas] parameter [canvas] with its center at (50,50). We
             * set the color of `p` to GREEN, set its text size to 30, and use it to draw the text
             * "Pictures" at location (60,60) on [canvas].
             *
             * @param canvas [Canvas] to draw to, it is created by calling the `beginRecording`
             * method of our [Picture] field [mPicture].
             */
            fun drawSomething(canvas: Canvas) {
                val p = Paint(Paint.ANTI_ALIAS_FLAG)
                p.color = -0x77010000
                canvas.drawCircle(50f, 50f, 40f, p)
                p.color = Color.GREEN
                p.textSize = 30f
                canvas.drawText("Pictures", 60f, 60f, p)
            }
        }

        /**
         * The init block of our constructor. We enable our view to receive focus, and to receive
         * focus in touch mode. We create a new instance of `Picture` for our `Picture` field
         * `mPicture`, call its `beginRecording` method specifying a width of 200, and a height of
         * 100, and pass the `Canvas` returned to our method `drawSomething`. When `drawSomething`
         * is done drawing to the `mPicture` canvas we call the `endRecording` method of `mPicture`.
         * Finally we create a `PictureDrawable` from `mPicture` for our `Drawable` field `mDrawable`.
         */
        init {
            isFocusable = true
            isFocusableInTouchMode = true
            mPicture = Picture()
            drawSomething(mPicture.beginRecording(200, 100))
            mPicture.endRecording()
            mDrawable = PictureDrawable(mPicture)
        }
    }
}
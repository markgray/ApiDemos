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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.os.Bundle
import android.view.View
import com.example.android.apis.R

/**
 * Shows how to use alpha channel compositing in 2D graphics using PorterDuffXfermode
 * and Bitmap.extractAlpha -- also shows how to create your own View in code alone.
 * See: https://developer.android.com/reference/android/graphics/PorterDuff.Mode.html
 * for details of the graphic algebra performed with the different PorterDuff modes
 * You can use the key below to understand the algebra that the Android docs use to
 * describe the other modes (see the article for a fuller description with similar terms).
 *
 *  * Sa Source alpha
 *  * Sc Source color
 *  * Da Destination alpha
 *  * Dc Destination color
 *
 * Where alpha is a value [0..1], and color is substituted once per channel (so use
 * the formula once for each of red, green and blue)
 *
 * The resulting values are specified as a pair in square braces as follows:
 *
 * [`<alpha-value>,<color-value>`]
 *
 * Where alpha-value and color-value are formulas for generating the resulting
 * alpha channel and each color channel respectively.
 */
class AlphaBitmap : GraphicsActivity() {
    /**
     * Called when the Activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to a new instance of our class [SampleView].
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(SampleView(this))
    }

    /**
     * This is a [View] which draws our three [Bitmap]'s into the [Canvas] its
     * `onDraw` override is passed.
     */
    private class SampleView(context: Context) : View(context) {
        /**
         * Contains the [Bitmap] decoded from our raw png image file R.raw.app_sample_code.
         */
        private val mBitmap: Bitmap
        /**
         * The alpha values of [Bitmap] field [mBitmap]. This may be drawn with [Canvas.drawBitmap],
         * where the color(s) will be taken from the paint that is passed to the draw call.
         */
        private val mBitmap2: Bitmap
        /**
         * [Bitmap] into which we draw a [android.graphics.LinearGradient] colored circle and text
         * using [Canvas.drawCircle] and [Canvas.drawText].
         */
        private val mBitmap3: Bitmap
        /**
         * [android.graphics.LinearGradient] instance [Shader] used to draw [mBitmap3]
         */
        private val mShader: Shader
        /**
         * [Paint] instance used in call to [Canvas.drawBitmap] for all the Bitmaps
         */
        private val p: Paint

        /**
         * Implement this to do your drawing. First we fill the entire canvas' bitmap with the color
         * WHITE, then we initialize our [Float] variable `var y` to 10 (we will use this as the y
         * pixel dimension within the [Canvas] parameter [canvas] at which we draw the next [Bitmap]
         * that we draw).
         *
         * Then we set the color of [Paint] field [p] to RED and draw [Bitmap] field [mBitmap]
         * at location (10,y) in [Canvas] parameter [canvas] using [p] as the [Paint]. We advance
         * `y` by the height of [mBitmap] plus 10, set color of [Paint] field [p] to GREEN and
         * draw [Bitmap] field [mBitmap] at location (10,y) in [Canvas] parameter [canvas] using [p]
         * as the [Paint]. This demonstrates that the colors in the [Bitmap] override the color of
         * the [Paint] used to draw it.
         *
         * We now advance `y` by the height of [mBitmap] plus 10, and draw [Bitmap] field [mBitmap2]
         * at location (10,y) in [Canvas] parameter [canvas] using [p] as the [Paint]. Because
         * [mBitmap2] consists only of the alpha channel copied from [mBitmap] the image drawn will
         * be drawn in the GREEN color setting of [Paint] field [p].
         *
         * We now advance `y` by the height of [mBitmap2] plus 10, set the [Shader] of [Paint] field
         * [p] to [Shader] field [mShader] and draw [Bitmap] field [mBitmap3] at location (10,y) in
         * [Canvas] parameter [canvas] using [p] as the [Paint].
         *
         * To demonstrate the [Shader] of a [Paint] overrides the color, we advance `y` by the height
         * of [mBitmap3] plus 10, set the color of [Paint] field [p] to RED again and draw [Bitmap]
         * field [mBitmap3] at location (10,y) in [Canvas] parameter [canvas] using [p] as the
         * [Paint]. The image is identical to the one above it.
         *
         * @param canvas the [Canvas] on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            canvas.drawColor(Color.WHITE)
            var y = 10f
            p.color = Color.RED
            canvas.drawBitmap(mBitmap, 10f, y, p)
            y += mBitmap.height + 10f
            p.color = Color.GREEN
            canvas.drawBitmap(mBitmap, 10f, y, p)
            y += mBitmap.height + 10f
            canvas.drawBitmap(mBitmap2, 10f, y, p)
            y += mBitmap2.height + 10f
            p.shader = mShader
            canvas.drawBitmap(mBitmap3, 10f, y, p)
            y += mBitmap3.height + 10f
            p.color = Color.RED
            canvas.drawBitmap(mBitmap3, 10f, y, p)
        }

        /**
         * Our static method
         */
        companion object {
            /**
             * Draws a circle and text into the [Bitmap] it is passed. We fetch the width of our
             * [Bitmap] parameter [bm] to [Float] variable `val x`, and the height to [Float]
             * variable `val y`. We construct a [Canvas] `val c` that will use [bm] to draw into,
             * and allocate a new instance of [Paint] for `val p`.
             *
             * We set the ANTI_ALIAS_FLAG of `p` to *true*, and set the alpha component of the
             * paint's color to 0x80 (1/2) and draw a circle into the [Canvas] `c` centered in the
             * middle with radius of half the width of the [Bitmap] parameter [bm] (x/2) using `p`
             * as the [Paint].
             *
             * Then we set the alpha component of `p` to 0x30, set the xfermode object of `p` to the
             * porter-duff mode SRC ([Sa, Sc] -- source alpha, and source color, the source pixels
             * replace the destination pixels.)
             * See: https://developer.android.com/reference/android/graphics/PorterDuff.Mode.html
             * Then we set the text size of `p` to 60, and its text alignment to CENTER. Next we use
             * [Paint] `p` to allocate a new [Paint.FontMetrics] object for `val fm` (getFontMetrics(fm)
             * is called by this version of the constructor, which returns the font's recommended
             * interline spacing to `fm`), given the Paint's settings for typeface, textSize, etc.
             * We use `fm.ascent` (the recommended distance above the baseline for singled spaced
             * text) to calculate how far down we have to move our `y` in order to center the text
             * in the [Bitmap] parameter [bm] when we call [Canvas.drawText] to write the string
             * "Alpha" using the [Paint] `p`.
             *
             * @param bm mutable [Bitmap] whose [Canvas] we want to draw into
             */
            private fun drawIntoBitmap(bm: Bitmap) {
                val x = bm.width.toFloat()
                val y = bm.height.toFloat()
                val c = Canvas(bm)
                val p = Paint()
                p.isAntiAlias = true
                p.alpha = 0x80
                c.drawCircle(x / 2, y / 2, x / 2, p)
                p.alpha = 0x30
                p.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
                p.textSize = 60f
                p.textAlign = Paint.Align.CENTER
                val fm = p.fontMetrics
                c.drawText("Alpha", x / 2, (y - fm.ascent) / 2, p)
            }
        }

        /**
         * Initializes the fields contained in this instance of `SampleView`. First we call
         * through to our super's constructor, then we set our `View` to be focusable. We
         * initialize our `Shader` field `mShader` to be a `LinearGradient` shader that
         * draws a linear gradient along the line `(0,0)->(100,70)`, using the colors RED,
         * GREEN, and BLUE distributed evenly along the gradient line, using the tile mode MIRROR
         * (repeating the shader's image horizontally and vertically, alternating mirror images so
         * that adjacent images always seam). We initialize our `Paint` field `p` with a new
         * instance.
         *
         * Next we open `InputStream` variable `val inputStream` to read our raw resource file
         * R.raw.app_sample_code, and use `BitmapFactory.decodeStream` to read and decode
         * `inputStream` into our `Bitmap` field `mBitmap`. We extract only the Alpha channel from
         * `mBitmap` to initialize our `Bitmap` field `mBitmap2`.
         *
         * Now we create an empty 200x200 `Bitmap` using Bitmap.Config.ALPHA_8 as the
         * `Bitmap.Config` (Each pixel is stored as a single translucency (alpha) channel.
         * no color information is stored. With this configuration, each pixel requires 1 byte of
         * memory), and set our `Bitmap` field `mBitmap3` to it, Finally we call our method
         * `drawIntoBitmap` to use `mBitmap3` for the canvas to draw its circle and text into.
         *
         * Parameter: `Context` to use to fetch resources
         */
        init {
            isFocusable = true
            mShader = LinearGradient(0F, 0F, 100F, 70F,
                    intArrayOf(Color.RED, Color.GREEN, Color.BLUE),
                    null, Shader.TileMode.MIRROR)
            p = Paint()
            val inputStream = context.resources.openRawResource(R.raw.app_sample_code)
            mBitmap = BitmapFactory.decodeStream(inputStream)
            mBitmap2 = mBitmap.extractAlpha()
            mBitmap3 = Bitmap.createBitmap(200, 200, Bitmap.Config.ALPHA_8)
            drawIntoBitmap(mBitmap3)
        }
    }
}
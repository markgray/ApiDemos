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

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.view.View

/**
 * Shows how to use android.graphics.Canvas methods clipPath and clipRect, as well as some other
 * Canvas drawing methods.
 */
class Clipping : GraphicsActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to a new instance of `SampleView`.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(SampleView(this))
    }

    /**
     * Custom View which draws the same scene 3 times using different clip settings for the
     * `Canvas` it is drawing to.
     */
    private class SampleView(context: Context?) : View(context) {
        /**
         * `Paint` we use to draw to the `Canvas`
         */
        private val mPaint: Paint
        /**
         * `Path` we create for a clip, and set on our `Canvas` using `Canvas.clipPath`
         */
        private val mPath: Path
        /**
         * logical density of the screen
         */
        @Suppress("PropertyName")
        var SCREEN_DENSITY: Float

        /**
         * Draws a "scene" consisting of a red line, a green circle, and the blue text "Clipping".
         * First we intersect the current clip of our parameter `Canvas canvas` with the
         * rectangle (0,0,100,100) ((left,top,right,bottom) -- this is relative to the already
         * translated `Canvas canvas` passed us and will prevent all drawing instructions
         * outside of this rectangle from having any effect, and will respect the "no draw" regions
         * of `canvas` specified by its current clip.) Then fill the entire canvas' bitmap
         * (restricted to the current clip) with the color white, using source over Porter-Duff mode.
         *
         *
         * We set the color of our field `Paint mPaint` to red, and use it to draw a line on
         * `canvas` from (0,0) to (100,100).
         *
         *
         * We set the color of `mPaint` to green and use it to draw a circle on `canvas`
         * centered at (30,70) with radius 30.
         *
         *
         * Finally we set the color of `mPaint` to blue and use it to write the text "Clipping"
         * to `canvas` starting at (100,30). The origin is interpreted based on the Align
         * setting in the paint which in our case is Paint.Align.RIGHT, so the end of the text is
         * at (100,30) not the beginning.
         *
         * @param canvas `Canvas` to draw our "scene" to, already translated to correct spot,
         * and clipped for the clip instructions to be demonstrated.
         */
        private fun drawScene(canvas: Canvas) {
            canvas.clipRect(0, 0, 100, 100)
            canvas.drawColor(Color.WHITE)
            mPaint.color = Color.RED
            canvas.drawLine(0f, 0f, 100f, 100f, mPaint)
            mPaint.color = Color.GREEN
            canvas.drawCircle(30f, 70f, 30f, mPaint)
            mPaint.color = Color.BLUE
            canvas.drawText("Clipping", 100f, 30f, mPaint)
        }

        /**
         * We implement this to do our drawing. We call our method `drawScene` to draw the same
         * "scene" 3 times using different matrix and clip states applied to the `Canvas canvas`
         * passed us.
         *
         *
         * First we scale the `Canvas canvas` passed us to the logical density of the display.
         * Next we set the entire `Canvas canvas` to the color GRAY. Then surrounded by matching
         * `Canvas.save` and `Canvas.restore` calls (which save the matrix and clips states
         * then restore them afterwards), we translate the `Canvas canvas` to the position we
         * want drawing to be directed to, apply all the clip state modifications (relative to the new
         * position) and call our method `drawScene` to draw to the translated and clipped
         * `Canvas canvas` restoring it to the untranslated un-clipped state on return. The 3
         * "scenes" positions and clip states are:
         *
         *  *
         * (10,10) No clipping added (apart from the clip rectangle used in `drawScene`
         *
         *  *
         * (160,10) We start with a clip rectangle excluding areas outside of (10,10,90,90)
         * then we add a clip rectangle which excludes areas inside of (30,30,70,70) (The
         * `Region.Op.DIFFERENCE` parameter to `clipRectangle` subtracts the
         * inside of the rectangle from the drawable canvas instead of the outside)
         *
         *  *
         * (10,160) We clear any lines and curves from `Path mPath`, making it empty,
         * then we add a circle centered at (50,50), of radius 50 and direction Counter clock
         * wise to `mPath`, then use it to replace the clip path of `canvas`.
         * Since the circle was drawn in the counter clockwise direction the clip will exclude
         * areas outside of the circle from the drawable canvas.
         *
         *
         *
         * @param canvas the canvas on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            canvas.scale(SCREEN_DENSITY, SCREEN_DENSITY)
            canvas.drawColor(Color.GRAY)
            canvas.save()
            canvas.translate(10f, 10f)
            drawScene(canvas)
            canvas.restore()
            canvas.save()
            canvas.translate(160f, 10f)
            canvas.clipRect(10, 10, 90, 90)
            @Suppress("DEPRECATION")
            canvas.clipRect(30f, 30f, 70f, 70f, Region.Op.DIFFERENCE)
            drawScene(canvas)
            canvas.restore()
            canvas.save()
            canvas.translate(10f, 160f)
            mPath.reset()
            mPath.addCircle(50f, 50f, 50f, Path.Direction.CCW)
            canvas.clipPath(mPath)
            drawScene(canvas)
            canvas.restore()
        }

        /**
         * Basic constructor for our class. First we call our super's constructor, then we enable our
         * view to receive focus. We initialize our field `float SCREEN_DENSITY` with the logical
         * density of the screen, then we allocate an instance of `Paint` for our field
         * `Paint mPaint`, set the ANTI_ALIAS_FLAG to true to enable antialiasing, set the stroke
         * width to 6, the text size to 16, and set the text alignment to be Paint.Align.RIGHT. Finally
         * we allocate a new instance of `Path` to initialize our field `Path mPath`.
         *
         * Parameter: the `Context` to use to retrieve resources, "this" when called from
         * `onCreate` override of our activity.
         */
        init {
            isFocusable = true
            SCREEN_DENSITY = resources.displayMetrics.density
            mPaint = Paint()
            mPaint.isAntiAlias = true
            mPaint.strokeWidth = 6f
            mPaint.textSize = 16f
            mPaint.textAlign = Paint.Align.RIGHT
            mPath = Path()
        }
    }
}
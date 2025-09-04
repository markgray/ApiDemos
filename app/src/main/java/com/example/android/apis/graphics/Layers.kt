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
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi

/**
 * Shows the use of Canvas.saveLayerAlpha() and Canvas.restore() to save and restore
 * Canvas settings while doing some drawing in an off screen buffer.
 */
@SuppressLint("ObsoleteSdkInt")
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class Layers : GraphicsActivity() {
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
     * This custom [View] consists solely of a BLUE circle drawn slightly offset on top of a
     * RED circle. The drawing is done to an offscreen bitmap allocated and redirected to by the
     * method `saveLayerAlpha`, then displayed onscreen by `restore`.
     *
     * @param context The Context the view is running in, through which it can
     * access the current theme, resources, etc.
     * (See our init block for our constructor details)
     */
    private class SampleView(context: Context?) : View(context) {
        /**
         * [Paint] used to draw in our [onDraw] method
         */
        private val mPaint: Paint

        /**
         * We implement this to do our drawing. First we set the entire [Canvas] parameter [canvas]
         * to WHITE, then we move the canvas to the location (10,10). We call the [Canvas.saveLayerAlpha]
         * method of [canvas] to allocate and redirect drawing instructions sent to [canvas] to an
         * offscreen [Bitmap] until the matching call to the `restore` method of [canvas] displays
         * that [Bitmap] and restores the canvas to its original settings. The bitmap is 200x200 with
         * 0x88 specified as the alpha value to be used when `restore` draws the [Bitmap] to the
         * [canvas].
         *
         * We then set the color of [Paint] field [mPaint] to RED and use it to draw a circle of
         * radius 75 pixels centered at the point (75,75), set the color of [mPaint] to BLUE and use
         * it to draw a circle of radius 75 pixels centered at the point (125,125). Both of these
         * `drawCircle` commands have been done to the offscreen bitmap that `canvas` is
         * redirecting commands to, and this bitmap is now transferred to `Canvas canvas` and
         * the settings saved by `saveLayerAlpha` are restored by a call to `canvas.restore`.
         *
         * @param canvas the [Canvas] on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            canvas.translate(0f, 240f)
            canvas.drawColor(Color.WHITE)
            canvas.translate(10f, 10f)
            // Saves future drawing commands to offscreen bitmap buffer
            canvas.saveLayerAlpha(0f, 0f, 200f, 200f, 0x88)
            mPaint.color = Color.RED
            canvas.drawCircle(75f, 75f, 75f, mPaint)
            mPaint.color = Color.BLUE
            canvas.drawCircle(125f, 125f, 75f, mPaint)
            // Transfers offscreen buffer to screen
            canvas.restore()
        }

        /**
         * We enable focus for our view, allocate a new instance of `Paint` for our `Paint` field
         * `mPaint` and set its antialias flag to true.
         */
        init {
            isFocusable = true
            mPaint = Paint()
            mPaint.isAntiAlias = true
        }
    }
}
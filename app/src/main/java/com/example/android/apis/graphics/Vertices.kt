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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import com.example.android.apis.R

/**
 * Shows how to use [Canvas.drawVertices] with [BitmapShader] to draw warp-able [Bitmap]'s. The
 * [Canvas.translate] before drawing the bottom version of the two is not far enough away from
 * the top for high dpi so it overlaps it in the original version, so I scaled it by the logical
 * screen density.
 */
class Vertices : GraphicsActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to a new instance of [SampleView].
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(SampleView(this))
    }

    private class SampleView(context: Context?) : View(context) {
        /**
         * [Paint] used to draw our bitmaps.
         */
        private val mPaint = Paint()

        /**
         * Array of vertices for the mesh (`verts` argument to [Canvas.drawVertices])
         */
        private val mVerts = FloatArray(10)

        /**
         * coordinates to sample into the current shader (`texs` argument to [Canvas.drawVertices])
         */
        private val mTexs = FloatArray(10)

        /**
         * `indices` argument to [Canvas.drawVertices] used to specify the index of each triangle,
         * rather than just walking through the arrays (`verts` and `texs`) in order.
         */
        private val mIndices = shortArrayOf(0, 1, 2, 3, 4, 1)

        /**
         * [Matrix] used to scale (by 0.8) and translate (to (20,20)) the [Canvas] before
         * drawing.
         */
        private val mMatrix = Matrix()

        /**
         * Inverse of [mMatrix], used to transpose coordinates received in a touch event into
         * coordinates on the canvas before [mMatrix] is applied in order to move the index 0
         * point of [mVerts] into the right position (ie. the position it should occupy before
         * the canvas has [mMatrix] concatenated to it).
         */
        private val mInverse = Matrix()

        /**
         * We implement this to do our drawing. First we set the entire [Canvas] parameter [canvas]
         * to a light gray. Then we save the current matrix and clip of [canvas] onto a private stack
         * and pre-concatenate the current matrix with [Matrix] field [mMatrix].
         *
         * We call the method [Canvas.drawVertices] of [canvas] to draw a TRIANGLE_FAN defined by
         * the vertices in [FloatArray] field [mVerts] to [canvas] using the shader of [Paint] field
         * [mPaint] with the coordinates to sample into the current shader specified by the vertices
         * in [FloatArray] field [mTexs] (initially the same coordinates stored in [mVerts] until a
         * touch event moves the vertex at index 0 of [mVerts] to warp the drawing). Since the
         * `indices` argument to `drawVertices` is null, the drawing uses the vertices in order:
         * (0,1,2) (0,2,3) (0,3,4), leaving a pie slice not drawn.
         *
         * We move the canvas down by 240*SCREEN_DENSITY, and call the [Canvas.drawVertices] method
         * of [canvas] to draw a TRIANGLE_FAN defined by the vertices in [mVerts] to [canvas] using
         * the shader of [mPaint] with the coordinates to sample into the current shader specified
         * by the vertices in [mTexs] (initially the same coordinates stored in [mVerts] until a
         * touch event moves the vertex at index 0 of [mVerts] to warp the drawing). This time the
         * `indices` argument to `drawVertices` is [FloatArray] field [mIndices]. The drawing now
         * uses the vertices in the order: (0,1,2) (0,2,3) (0,3,4) and (0,4,1) so the entire [Bitmap]
         * is drawn.
         *
         * Finally we call the [Canvas.restore] method of [canvas] to remove all modifications to
         * the matrix/clip state since the [Canvas.save] call at the beginning of our method.
         *
         * @param canvas the [Canvas] on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            canvas.drawColor(-0x333334)
            canvas.save()
            canvas.concat(mMatrix)
            canvas.drawVertices(
                    Canvas.VertexMode.TRIANGLE_FAN, 10,
                    mVerts,0,
                    mTexs, 0,
                    null, 0,
                    null, 0,
                    0, mPaint
            )
            canvas.translate(0f, 240 * SCREEN_DENSITY)
            canvas.drawVertices(
                    Canvas.VertexMode.TRIANGLE_FAN, 10,
                    mVerts, 0,
                    mTexs, 0,
                    null, 0,
                    mIndices, 0,
                    6, mPaint
            )
            canvas.restore()
        }

        /**
         * We implement this method to handle touch screen motion events. First we fill [FloatArray]
         * `val pt` with the x and y coordinates of the touch event. We then use [Matrix] field
         * [mInverse] to map the point `pt` to the coordinates it would have before [Matrix] field
         * [mMatrix] has been used to scale and translate the canvas of our view ([mInverse] is set
         * to the inverse of [mMatrix] in our constructor remember). We call our method [setXY] to
         * set index 0 of [mVerts] to the x and y coordinates of `pt`, call [invalidate] so the view
         * will be redrawn, and finally return true to our caller.
         *
         * @param event The motion event.
         * @return True if the event was handled, false otherwise. We always return true.
         */
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(event: MotionEvent): Boolean {
            val pt = floatArrayOf(event.x, event.y)
            mInverse.mapPoints(pt)
            setXY(mVerts, 0, pt[0], pt[1])
            invalidate()
            return true
        }

        companion object {
            /**
             * Logical screen density, used to scale the position of the second `Bitmap`.
             */
            private var SCREEN_DENSITY: Float = 1f

            /**
             * Stores its arguments [x] and [y] in the correct positions in [FloatArray] argument
             * [array] for the point with index [index].
             *
             * @param array vertex array to store [x] and [y] in
             * @param index index of the point to set to `(x,y)`.
             * @param x     the new X coordinate
             * @param y     the new Y coordinate
             */
            private fun setXY(array: FloatArray, index: Int, x: Float, y: Float) {
                array[index * 2 + 0] = x
                array[index * 2 + 1] = y
            }
        }

        /**
         * The init block of our constructor. First we enable our view to receive focus, and
         * initialize `SCREEN_DENSITY` to the logical density of the screen. We decode the
         * resource file beach.jpg (R.drawable.beach) into `Bitmap` `val bm`, then use `bm` to
         * create `Shader` `val s` with a tiling mode of CLAMP for both x and y (replicate the edge
         * color if the shader draws outside of its original bounds), and then set the shader of
         * `Paint` field `mPaint` to `s`.
         *
         * Next we initialize `Float` `val w` to the width of `bm`, and `Float` `val h` to the
         * height of `bm`. We use these to calculate the (x,y) coordinates of the vertices we
         * store in both `mTexs` and `mVerts`:
         *
         *  * 0 - (w/2,h/2) the center of the `Bitmap` and `Canvas`
         *  * 1 - (0,0) the top left of the `Bitmap` and `Canvas`
         *  * 2 - (w,0) the top right of the `Bitmap` and `Canvas`
         *  * 3 - (w,h) the bottom right of the `Bitmap` and `Canvas`
         *  * 4 - (0,h) the bottom left of the `Bitmap` and `Canvas`
         *
         * We initialize `Matrix` field `mMatrix` with a matrix that will scale by 0.8, and
         * pre-concatenate `mMatrix` with a translation to (20,20). Then we initialize
         * `Matrix` field `mInverse` with the inverse of `mMatrix`.
         */
        init {
            isFocusable = true
            SCREEN_DENSITY = resources.displayMetrics.density
            val bm = BitmapFactory.decodeResource(resources, R.drawable.beach)
            val s: Shader = BitmapShader(bm, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            mPaint.shader = s
            val w = bm.width.toFloat()
            val h = bm.height.toFloat()
            // construct our mesh
            setXY(mTexs, 0, w / 2, h / 2)
            setXY(mTexs, 1, 0f, 0f)
            setXY(mTexs, 2, w, 0f)
            setXY(mTexs, 3, w, h)
            setXY(mTexs, 4, 0f, h)
            setXY(mVerts, 0, w / 2, h / 2)
            setXY(mVerts, 1, 0f, 0f)
            setXY(mVerts, 2, w, 0f)
            setXY(mVerts, 3, w, h)
            setXY(mVerts, 4, 0f, h)
            mMatrix.setScale(0.8f, 0.8f)
            mMatrix.preTranslate(20f, 20f)
            mMatrix.invert(mInverse)
        }
    }
}
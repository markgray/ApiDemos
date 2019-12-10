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
import android.graphics.Canvas
import android.graphics.Matrix
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import com.example.android.apis.R
import kotlin.math.sqrt

/**
 * Uses android.graphics.Canvas method drawBitmapMesh to warp a bitmap near the  area it is touched.
 * Very subtle effect on Nexus 6 and Nexus 6P -> Marshmallow or just small high density screen?
 */
class BitmapMesh : GraphicsActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to a new instance of [SampleView]
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(SampleView(this))
    }

    /**
     * Custom [View] which contains a jpg which is warped by touch events received in our
     * [onTouchEvent] override.
     */
    private class SampleView(context: Context?) : View(context) {
        /**
         * [Bitmap] that contains our jpg to be displayed and warped.
         */
        private val mBitmap: Bitmap
        /**
         * Warped [Bitmap] mesh array of (x,y) vertices
         */
        private val mVerts = FloatArray(COUNT * 2)
        /**
         * Original un-warped [Bitmap] mesh array of (x,y) vertices
         */
        private val mOrig = FloatArray(COUNT * 2)
        /**
         * [Matrix] used to translate the [Canvas] parameter passed to our [onDraw] override to
         * (10,10) before drawing the [Bitmap]
         */
        private val mMatrix = Matrix()
        /**
         * The inverse of [Matrix] field [mMatrix] used to translate points received in touch events
         * to a (0,0) origin (ie. it converts points in the [Canvas] `canvas` coordinate space to
         * points in the [Matrix] field [mMatrix] translated [Bitmap] coordinate space).
         */
        private val mInverse = Matrix()

        /**
         * We implement this to do our drawing. First we set the color of the [Canvas] parameter
         * [canvas] to 0xFFCCCCCC (a darkish gray). Then we pre-concatenate [Matrix] field [mMatrix]
         * to the current [Matrix] of [canvas] and draw the [Bitmap] field [mBitmap] through the
         * mesh in our [Float] array field [mVerts].
         *
         * @param canvas the [Canvas] on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            canvas.drawColor(-0x333334)
            canvas.concat(mMatrix)
            canvas.drawBitmapMesh(
                    mBitmap,
                    WIDTH, HEIGHT,
                    mVerts, 0,
                    null, 0,
                    null
            )
        }

        /**
         * Creates a warped bitmap mesh version [Float] array field [mVerts] of the un-warped bitmap
         * mesh [Float] array field [mOrig] with the vertices closest to the point (cx,cy) being
         * "pulled" hardest towards that point.
         *
         * For notational cleanliness we copy the [Float] array reference [mOrig] to a local variable
         * `val src`, and [mVerts] to local variable `val dst`.
         *
         * Then we loop through each of the vertex coordinates in `src`, fetching the x coordinate
         * to `val x` and the y coordinate to `val y`, determine the x distance `val dx` of this
         * point from `cx`, and the y distance `val dy` of this point from `cy`, set `val dd` to
         * the sum of the squares of `dx` and `dy`, and set `val d` to the square root (distance)
         * of `dd`.
         *
         * The pull `var pull` on the point is then `K/(dd+0.000001f)` (with the 0.000001f added to
         * avoid possible divide by zero) with this divided again by `(d + 0.000001f)` to allow
         * multiplication by `dx` and `dy` later.
         *
         * Then if the `pull` is greater than or equal to 1 we are very close to the point `(cx,cy)`
         * so we set the vertex of the `dst` bitmap mesh to `(cx,cy)`, otherwise we set the x
         * coordinate to the `x` of the original plus `pull` times the `dx` distance from `cx`, and
         * the y coordinate to the `y` of the original plus `pull` times the `dy` distance from `cy`.
         *
         * @param cx x coordinate of the point we are space warping around
         * @param cy y coordinate of the point we are space warping around
         */
        private fun warp(cx: Float, cy: Float) {
            val src = mOrig
            val dst = mVerts
            var i = 0
            while (i < COUNT * 2) {
                val x = src[i + 0]
                val y = src[i + 1]
                val dx = cx - x
                val dy = cy - y
                val dd = dx * dx + dy * dy
                val d = sqrt(dd.toDouble()).toFloat()
                var pull = K / (dd + 0.000001f)
                pull /= d + 0.000001f
                //   android.util.Log.d("BitmapMesh", "index " + i + " dist=" + d + " pull=" + pull);
                if (pull >= 1) {
                    dst[i + 0] = cx
                    dst[i + 1] = cy
                } else {
                    dst[i + 0] = x + dx * pull
                    dst[i + 1] = y + dy * pull
                }
                i += 2
            }
        }

        /**
         * x coordinate of last touch event that we warped the bitmap mesh for
         */
        private var mLastWarpX = -9999 // don't match a valid touch coordinate to start with
        /**
         * y coordinate of last touch event that we warped the bitmap mesh for
         */
        private var mLastWarpY = 0

        /**
         * We implement this method to handle touch screen motion events. First we fetch the x and
         * y coordinates of our [MotionEvent] parameter [event] to initialize [Float] array `val pt`,
         * then we use [Matrix] field [mInverse] to translate this point from the [Canvas] coordinate
         * system to the coordinate system of the [Bitmap] field [mBitmap] (which is translated to
         * (10,10) by the [Matrix] field [mMatrix]). We cast the x coordinate of `pt` to an [Int]
         * in order to set `val x`, and the y coordinate of `pt` to an [Int] in order to set `val y`,
         * then compare `x` and `y` to [mLastWarpX] and [mLastWarpY] to see if the touch has moved,
         * and if it has we set [mLastWarpX] to `x` and [mLastWarpY] to `y`, call our method [warp]
         * to "warp" the bitmap mesh in [Float] array [mVerts] around `pt` and finally invalidate
         * our [View] so that our [onDraw] method will be called to render our bitmap through our
         * warped bitmap mesh.
         *
         * @param event The motion event.
         * @return *true* if the event was handled, *false* otherwise. (We always return *true*)
         */
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(event: MotionEvent): Boolean {
            val pt = floatArrayOf(event.x, event.y)
            mInverse.mapPoints(pt)
            val x = pt[0].toInt()
            val y = pt[1].toInt()
            if (mLastWarpX != x || mLastWarpY != y) {
                mLastWarpX = x
                mLastWarpY = y
                warp(pt[0], pt[1])
                invalidate()
            }
            return true
        }

        /**
         * Our static constants and method
         */
        companion object {
            /**
             * Gravitational constant used in [warp] method.
             */
            const val K = 10000f
            /**
             * Number of vertices in X dimension of Bitmap mesh.
             */
            private const val WIDTH = 20
            /**
             * Number of vertices in Y dimension of Bitmap mesh.
             */
            private const val HEIGHT = 20
            /**
             * Total number of vertices in Bitmap mesh
             */
            private const val COUNT = (WIDTH + 1) * (HEIGHT + 1)

            /**
             * Convenience method to set the (x,y) values for a vertex, it simply calculates the array
             * indices for the x and y values of the vertex in question based on two floats per vertex
             * and stores the parameters [x] and [y] where they belong.
             *
             * @param array Vertices array
             * @param index address of vertex to set the (x,y) coordinates of
             * @param x     x coordinate
             * @param y     y coordinate
             */
            private fun setXY(array: FloatArray, index: Int, x: Float, y: Float) {
                array[index * 2 + 0] = x
                array[index * 2 + 1] = y
            }
        }

        /**
         * Basic constructor that initializes the `View` and fields used by this instance of
         * `SampleView`. First we call through to our super's constructor, then we enable our
         * `View` to receive focus. Next we initialize our `Bitmap` field mBitmap` by decoding
         * the jpg resource file R.drawable.beach.
         *
         * We fetch the `Float` width of `mBitmap` to `val w` and the `Float` height to `val h`.
         * Then starting from `var index` of 0 we proceed to fill our two `Float` mesh vertex
         * arrays `mVerts` and `mOrig` with (x,y) vertex coordinates by looping through the HEIGHT
         * y dimension values (each y value being y/HEIGHT of the height of the `Bitmap` field
         * `mBitmap`), and for each of these values calculating the WIDTH x values possible (each
         * x value being x/WIDTH of the width of the `Bitmap` field `mBitmap`).
         *
         * Then we initialize our `Matrix` field `mMatrix` with a matrix to translate the canvas
         * to (10,10), and initialize `Matrix` field `mInverse` to be the inverse of this.
         *
         * Parameter: `Context` of `View` to use to fetch resources, `this` called from
         * `onCreate` override of the activity in our case
         */
        init {
            isFocusable = true
            mBitmap = BitmapFactory.decodeResource(resources, R.drawable.beach)
            val w = mBitmap.width.toFloat() // 1050 for our jpg
            val h = mBitmap.height.toFloat() // 788 for our jpg
            // construct our mesh
            var index = 0
            for (y in 0..HEIGHT) {
                val fy = h * y / HEIGHT
                for (x in 0..WIDTH) {
                    val fx = w * x / WIDTH
                    setXY(mVerts, index, fx, fy)
                    setXY(mOrig, index, fx, fy)
                    index += 1
                }
            }
            mMatrix.setTranslate(10f, 10f)
            mMatrix.invert(mInverse)
        }
    }
}
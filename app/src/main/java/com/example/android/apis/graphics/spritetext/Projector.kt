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
package com.example.android.apis.graphics.spritetext

import android.opengl.Matrix
import javax.microedition.khronos.opengles.GL10

/**
 * A utility that projects
 */
internal class Projector {
    /**
     * [MatrixGrabber] we use to get a copy of the GPU model view and projection matrices
     */
    private val mGrabber: MatrixGrabber = MatrixGrabber()
    /**
     * Flag to indicate whether our [FloatArray] field [mMVP] contains an up to date model view
     * projection matrix. If *false*, our method [project] must compute it before applying the
     * matrix to its vector input parameter.
     */
    private var mMVPComputed = false
    /**
     * Model view projection matrix we compute using the current GPU model view and projection
     * matrices
     */
    private val mMVP: FloatArray = FloatArray(16)
    /**
     * Used by our method [project] to hold the result of multiplying its input parameter vector
     * by our model view projection matrix [FloatArray] field [mMVP].
     */
    private val mV: FloatArray = FloatArray(4)
    /**
     * The `x` coordinate passed to our method [setCurrentView] by the `onSurfaceChanged`
     * method of [SpriteTextRenderer] (always 0). It is used as part of the calculation of the
     * projection of the input parameter vector of our method [project]. [project] is used
     * to find where to place a label on the rotating triangle.
     */
    private var mX = 0
    /**
     * The `y` coordinate passed to our method [setCurrentView] by the `onSurfaceChanged`
     * method of [SpriteTextRenderer] (always 0). It is used as part of the calculation of
     * the projection of the input parameter vector of our method [project].
     */
    private var mY = 0
    /**
     * The `width` dimension passed to our method [setCurrentView] by the `onSurfaceChanged`
     * method of [SpriteTextRenderer] (it is the same as the `w` parameter passed to it). It
     * is used as part of the calculation of the projection of the input parameter vector of
     * our method [project].
     */
    private var mViewWidth = 0
    /**
     * The `height` dimension passed to our method [setCurrentView] by the `onSurfaceChanged`
     * method of [SpriteTextRenderer] (it is the same as the `h` parameter passed to it). It
     * is used as part of the calculation of the projection of the input parameter vector of
     * our method [project].
     */
    private var mViewHeight = 0

    /**
     * Called from the `onSurfaceChanged` method of [SpriteTextRenderer] to save the values
     * it used to set the viewport in our fields [mX], [mY], [mViewWidth], and [mViewHeight]
     * for later use by our method [project].
     *
     * @param x      x coordinate of lower left corner of view
     * @param y      y coordinate of lower left corner of view
     * @param width  width of view
     * @param height height of view
     */
    fun setCurrentView(x: Int, y: Int, width: Int, height: Int) {
        mX = x
        mY = y
        mViewWidth = width
        mViewHeight = height
    }

    /**
     * Given the relative location of a label, we calculate the absolute location it must have when
     * the moving model view, projection, and view port are taken into consideration. In our case
     * this keeps the labels for the three vertices of the rotating triangle in sync with the
     * triangle.
     *
     * First we check if our flag [mMVPComputed] is *false*, and if so we multiply the current
     * projection matrix that our [MatrixGrabber] field [mGrabber] has retrieved to its `mProjection`
     * field by the model view matrix held in its `mModelView` field and save the results in our
     * [FloatArray] field [mMVP]. We then set [mMVPComputed] to *true*.
     *
     * Now that we have an up to date model view projection matrix in [mMVP] we multiply the
     * input vector [obj] by it and save the result in [FloatArray] field [mV]. We calculate the
     * value needed to normalize the vector `val rw` by calculating the inverse of the "w" coordinate
     * contained in `mV[3]` which has been de-normalized by the projection matrix (W is the fourth
     * coordinate of a three dimensional vertex; This vertex is called the homogeneous vertex coordinate.
     * In few words, the W component is a factor which divides the other vector components. When W is
     * 1.0, the homogeneous vertex coordinates are "normalized". To compare two vertices, you should
     * normalize the W value to 1.0).
     *
     * Finally we calculate the output vertex locations by normalizing each coordinate in turn, adding
     * 1.0 to the 0.0 to 1.0 result, and multiplying by 0.5 to move (0,0) to the center of the 0.0 to
     * 1.0 space. We multiply this by the view width [mViewWidth] and add the x coordinate of the
     * lower left corner to get the output x coordinate for `win[winOffset]`, and multiply this
     * by the view height [mViewHeight] and add the y coordinate of the lower left corner to get
     * the output y coordinate `win[winOffset+1]`. We also calculate the z coordinate but it not
     * used by our caller so I won't comment on the calculation
     *
     * @param obj       Relative (x,y.0,1) coordinates of label location.
     * @param objOffset Offset into `obj` for first element of the vector
     * @param win       Output vector for the absolute (x,y.z) coordinates of label location
     *                  in moving model view
     * @param winOffset Offset into `win` for first element of the vector
     */
    fun project(obj: FloatArray?, objOffset: Int, win: FloatArray, winOffset: Int) {
        if (!mMVPComputed) {
            Matrix.multiplyMM(
                    mMVP, 0,
                    mGrabber.mProjection, 0,
                    mGrabber.mModelView, 0
            )
            mMVPComputed = true
        }
        Matrix.multiplyMV(mV, 0, mMVP, 0, obj, objOffset)
        val rw = 1.0f / mV[3]
        win[winOffset] = mX + mViewWidth * (mV[0] * rw + 1.0f) * 0.5f
        win[winOffset + 1] = mY + mViewHeight * (mV[1] * rw + 1.0f) * 0.5f
        win[winOffset + 2] = (mV[2] * rw + 1.0f) * 0.5f
    }

    /**
     * Get the current projection matrix. Has the side-effect of setting current matrix mode to
     * GL_PROJECTION. We simply instruct our [MatrixGrabber] field [mGrabber] to fetch the
     * current projection matrix, and set our flag [mMVPComputed] to false so that the value
     * of [mMVP] will be recomputed.
     *
     * @param gl the gl interface
     */
    fun getCurrentProjection(gl: GL10?) {
        mGrabber.getCurrentProjection(gl!!)
        mMVPComputed = false
    }

    /**
     * Get the current model view matrix. Has the side-effect of setting current matrix mode to
     * GL_MODELVIEW. We simply instruct our [MatrixGrabber] field [mGrabber] to fetch the
     * current model view matrix, and set our flag [mMVPComputed] to false so that the value
     * of [mMVP] will be recomputed.
     *
     * @param gl the gl interface
     */
    fun getCurrentModelView(gl: GL10?) {
        mGrabber.getCurrentModelView(gl!!)
        mMVPComputed = false
    }

}
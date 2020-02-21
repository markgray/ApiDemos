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

import javax.microedition.khronos.opengles.GL10

/**
 * Class which fetches and saves the current model view and projection view matrices in its fields.
 */
internal class MatrixGrabber {
    /**
     * Our copy of the model view matrix
     */
    @JvmField
    var mModelView: FloatArray = FloatArray(16)
    /**
     * Our copy of the projection view matrix
     */
    @JvmField
    var mProjection: FloatArray = FloatArray(16)

    /**
     * Record the current modelView and projection matrix state. Has the side effect of setting the
     * current matrix state to GL_MODELVIEW -- UNUSED
     *
     * @param gl the [GL10] interface
     */
    @Suppress("unused")
    fun getCurrentState(gl: GL10) {
        getCurrentProjection(gl)
        getCurrentModelView(gl)
    }

    /**
     * Record the current modelView matrix state. Has the side effect of setting the current matrix
     * state to GL_MODELVIEW. We simply call our method [getMatrix] to read the current model
     * view matrix into our [Float] array field [mModelView]
     *
     * @param gl the [GL10] interface
     */
    fun getCurrentModelView(gl: GL10) {
        getMatrix(gl, GL10.GL_MODELVIEW, mModelView)
    }

    /**
     * Record the current projection matrix state. Has the side effect of setting the current matrix
     * state to GL_PROJECTION. We simply call our method [getMatrix] to read the current
     * projection matrix into our [Float] array field [mProjection].
     *
     * @param gl the [GL10] interface
     */
    fun getCurrentProjection(gl: GL10) {
        getMatrix(gl, GL10.GL_PROJECTION, mProjection)
    }

    /**
     * Sets the current matrix to its [Int] parameter [mode], and reads that matrix into its
     * [Float] array parameter [mat]. To do this we cast our [GL10] parameter [gl] to initialize
     * [MatrixTrackingGL] variable `val gl2`, use it to set the current matrix to [mode], and
     * then use the `gl2` method `getMatrix` to copy that matrix into our parameter
     * [mat].
     *
     * @param gl   the [GL10] interface
     * @param mode matrix we are interested in, either GL_MODELVIEW, or GL_PROJECTION
     * @param mat  [Float] array to hold the matrix requested
     */
    private fun getMatrix(gl: GL10, mode: Int, mat: FloatArray) {
        val gl2 = gl as MatrixTrackingGL
        gl2.glMatrixMode(mode)
        gl2.getMatrix(mat, 0)
    }

}
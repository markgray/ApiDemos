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
import java.nio.FloatBuffer
import java.nio.IntBuffer

/**
 * A matrix stack, similar to OpenGL ES's internal matrix stack.
 */
class MatrixStack {
    /**
     * Our stack of matrices, it is in our case a [FloatArray] which is allocated enough
     * storage for [DEFAULT_MAX_DEPTH] times [MATRIX_SIZE] entries.
     */
    private lateinit var mMatrix: FloatArray
    /**
     * Index of the current top of matrix stack, it goes from 0 to [MATRIX_SIZE] time the quantity
     * [DEFAULT_MAX_DEPTH] minus one in steps of [MATRIX_SIZE].
     */
    private var mTop = 0
    /**
     * Temporary storage for holding two matrices each having a size of [MATRIX_SIZE].
     */
    private lateinit var mTemp: FloatArray

    /**
     * Our constructor, we simply call our method [commonInit] to allocate the storage we need
     * for a matrix stack with DEFAULT_MAX_DEPTH matrices in it.
     */
    constructor() {
        commonInit(DEFAULT_MAX_DEPTH)
    }

    /**
     * Our constructor which allows the depth of the matrix stack to be specified UNUSED.
     *
     * @param maxDepth maximum depth of the matrix stack we are to hold
     */
    @Suppress("unused")
    constructor(maxDepth: Int) {
        commonInit(maxDepth)
    }

    /**
     * Initializes our instance by allocating storage for our [FloatArray] fields [mMatrix] and
     * [mTemp] with its [Int] argument [maxDepth] specifying how many matrices our matrix stack
     * needs to hold. It also calls our [glLoadIdentity] method to load the top of our stack with
     * the identity matrix.
     *
     * @param maxDepth depth of matrix stack.
     */
    private fun commonInit(maxDepth: Int) {
        mMatrix = FloatArray(maxDepth * MATRIX_SIZE)
        mTemp = FloatArray(MATRIX_SIZE * 2)
        glLoadIdentity()
    }

    /**
     * Loads the matrix at the top of the matrix stack with a projection matrix defined in terms
     * of the six clipping planes. We simply call the method [Matrix.frustumM] with [mMatrix] as
     * the output array using our parameter [mTop] as the index offset into that array, passing
     * our other parameters as the clipping planes it is to use.
     *
     * @param left   left vertical clipping plane
     * @param right  right vertical clipping plane
     * @param bottom bottom horizontal clipping plane
     * @param top    top horizontal clipping plane
     * @param near   near depth clipping planes
     * @param far    far depth clipping planes
     */
    fun glFrustumf(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float) {
        Matrix.frustumM(mMatrix, mTop, left, right, bottom, top, near, far)
    }

    /**
     * Loads the matrix at the top of the matrix stack with a projection matrix defined in terms of
     * six fixed point ([Int]) clipping planes. We simply convert the fixed values of our parameters
     * to float values using our method [fixedToFloat], then pass the call to our [Float] version of
     * [glFrustumf].
     *
     * @param left   left vertical clipping plane
     * @param right  right vertical clipping plane
     * @param bottom bottom horizontal clipping plane
     * @param top    top horizontal clipping plane
     * @param near   near depth clipping planes
     * @param far    far depth clipping planes
     */
    fun glFrustumx(left: Int, right: Int, bottom: Int, top: Int, near: Int, far: Int) {
        glFrustumf(fixedToFloat(left), fixedToFloat(right),
                fixedToFloat(bottom), fixedToFloat(top),
                fixedToFloat(near), fixedToFloat(far))
    }

    /**
     * Loads the top of our matrix stack with the identity matrix, we simply call the static method
     * [Matrix.setIdentityM] to do this for us.
     */
    fun glLoadIdentity() {
        Matrix.setIdentityM(mMatrix, mTop)
    }

    /**
     * Replace the matrix at the top of our stack with our [FloatArray] parameter [m]. We just use
     * the method [System.arraycopy] to do the copy for us.
     *
     * @param m      matrix to load to the top of the matrix stack
     * @param offset offset to first source location
     */
    fun glLoadMatrixf(m: FloatArray?, offset: Int) {
        System.arraycopy(m!!, offset, mMatrix, mTop, MATRIX_SIZE)
    }

    /**
     * Replace the matrix at the top of our stack with the matrix contained in [FloatBuffer]
     * parameter [m]. We use the relative bulk `get` method: [FloatBuffer.get] which transfers
     * [MATRIX_SIZE] floats from the buffer [m] into [FloatArray] field [mMatrix] starting at
     * offset [mTop] in [mMatrix].
     *
     * @param m [FloatBuffer] containing matrix to load to the top of the matrix stack
     */
    fun glLoadMatrixf(m: FloatBuffer) {
        m[mMatrix, mTop, MATRIX_SIZE]
    }

    /**
     * Replace the matrix at the top of our stack with the [IntArray] matrix [m] converted from
     * fixed point 16.16 format to [Float] format. We loop through the [IntArray] parameter [m]
     * starting from the [offset] element for [MATRIX_SIZE] elements, converting the element from
     * [m] to [Float] and storing the result in [mMatrix] starting at offset [mTop].
     *
     * @param m      matrix to load to the top of the matrix stack
     * @param offset offset to first source location
     */
    fun glLoadMatrixx(m: IntArray, offset: Int) {
        for (i in 0 until MATRIX_SIZE) {
            mMatrix[mTop + i] = fixedToFloat(m[offset + i])
        }
    }

    /**
     * Replace the matrix at the top of our stack with the matrix contained in [IntBuffer] parameter
     * [m] converted from fixed point 16.16 format to [Float] format.
     *
     * @param m [IntBuffer] containing 16.16 format matrix to load to the top of the matrix stack
     */
    fun glLoadMatrixx(m: IntBuffer) {
        for (i in 0 until MATRIX_SIZE) {
            mMatrix[mTop + i] = fixedToFloat(m.get())
        }
    }

    /**
     * Replaces our top of stack matrix by the results of multiplying our top of stack matrix (lhs)
     * by the parameter matrix `float[] m` (rhs). Note that when this result is then used to
     * multiply a vector it is as if the rhs `m` matrix is used to multiply first, then the old
     * lhs top of stack matrix (a result of the way matrix multiplication works which might run
     * counter to your intuition).
     *
     *
     * First we copy our top of stack matrix to `float[] mTemp`, then we use the static method
     * `android.opengl.Matrix.multiplyMM` to multiply `mTemp` by `m` placing the
     * result in our top of stack matrix.
     *
     * @param m      matrix to multiply our top of stack matrix by
     * @param offset offset to first source location in `float[] m`
     */
    fun glMultMatrixf(m: FloatArray?, offset: Int) {
        System.arraycopy(mMatrix, mTop, mTemp, 0, MATRIX_SIZE)
        Matrix.multiplyMM(mMatrix, mTop, mTemp, 0, m, offset)
    }

    /**
     * Replaces our top of stack matrix by the results of multiplying our top of stack matrix (lhs)
     * by the matrix contained in our parameter `FloatBuffer m` (rhs).
     *
     *
     * To do this we `get` the matrix into the second matrix in our `float[] mTemp` temp
     * matrix storage, then call our method `glMultMatrixf(float[], int)` using this copy of the
     * matrix that was contained in `m`.
     *
     * @param m `FloatBuffer` containing a matrix to multiply our top of stack matrix by
     */
    fun glMultMatrixf(m: FloatBuffer) {
        m[mTemp, MATRIX_SIZE, MATRIX_SIZE]
        glMultMatrixf(mTemp, MATRIX_SIZE)
    }

    /**
     * Replaces our top of stack matrix by the results of multiplying our top of stack matrix (lhs)
     * by the parameter matrix `int[] m` (rhs). To do this we convert the 16.16 fixed point
     * values of `m` to `float` values, storing the result in the second matrix in our
     * `float[] mTemp` temp matrix storage. We then call our method `glMultMatrixf(float[], int)`
     * using this converted copy of the matrix `m`.
     *
     * @param m      matrix to multiply our top of stack matrix by
     * @param offset offset to first source location
     */
    fun glMultMatrixx(m: IntArray, offset: Int) {
        for (i in 0 until MATRIX_SIZE) {
            mTemp[MATRIX_SIZE + i] = fixedToFloat(m[offset + i])
        }
        glMultMatrixf(mTemp, MATRIX_SIZE)
    }

    /**
     * Replaces our top of stack matrix by the results of multiplying our top of stack matrix (lhs)
     * by the matrix contained in our parameter `IntBuffer m`. To do this we `get` and
     * convert the 16.16 fixed point values of `m` to `float` values, storing the result
     * in the second matrix in our `float[] mTemp` temp matrix storage. We then call our method
     * `glMultMatrixf(float[], int)` using this converted copy of the matrix contained in
     * `m`.
     *
     * @param m `IntBuffer` containing 16.16 format matrix to multiply our top of stack matrix by.
     */
    fun glMultMatrixx(m: IntBuffer) {
        for (i in 0 until MATRIX_SIZE) {
            mTemp[MATRIX_SIZE + i] = fixedToFloat(m.get())
        }
        glMultMatrixf(mTemp, MATRIX_SIZE)
    }

    /**
     * Replaces our top of stack matrix with an orthographic projection matrix based on its input
     * parameters. We simply call the static method `android.opengl.Matrix.orthoM` to do all
     * our work for us.
     *
     * @param left   left vertical clipping plane
     * @param right  right vertical clipping plane
     * @param bottom bottom horizontal clipping plane
     * @param top    top horizontal clipping plane
     * @param near   near depth clipping plane
     * @param far    far depth clipping plane
     */
    fun glOrthof(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float) {
        Matrix.orthoM(mMatrix, mTop, left, right, bottom, top, near, far)
    }

    /**
     * Replaces our top of stack matrix with an orthographic projection matrix based on its input
     * parameters. We pass our input parameters converted from fixed point 16.16 format to `float`
     * to our method `glOrthof(float, float, float, float, float, float)`.
     *
     * @param left   left vertical clipping plane
     * @param right  right vertical clipping plane
     * @param bottom bottom horizontal clipping plane
     * @param top    top horizontal clipping plane
     * @param near   near depth clipping plane
     * @param far    far depth clipping plane
     */
    fun glOrthox(left: Int, right: Int, bottom: Int, top: Int, near: Int, far: Int) {
        glOrthof(fixedToFloat(left), fixedToFloat(right),
                fixedToFloat(bottom), fixedToFloat(top),
                fixedToFloat(near), fixedToFloat(far))
    }

    /**
     * Pop's the top matrix off of the stack replacing the current matrix with the one below it on
     * the stack (the one that was saved by a previous call to `glPushMatrix`). First we call
     * our method `preflight_adjust` to make sure there at least one matrix below us on the
     * stack, throwing an IllegalArgumentException if not, then we call our method `adjust`
     * to subtract the length of one matrix from our pointer `mTop`.
     */
    fun glPopMatrix() {
        preflightAdjust(-1)
        adjust(-1)
    }

    /**
     * Pushes the current matrix stack down by one, duplicating the current matrix. That is, after a
     * `glPushMatrix` call, the matrix on top of the stack is identical to the one below it.
     * First we call our method `preflight_adjust` to make sure there is enough room for another
     * matrix on our stack, then we copy the current top of stack matrix to the area above it in the
     * stack, and call our method `adjust` to add the length of one matrix to the top of stack
     * pointer `mTop` (to point to the new top of stack matrix).
     */
    fun glPushMatrix() {
        preflightAdjust(1)
        System.arraycopy(mMatrix, mTop, mMatrix, mTop + MATRIX_SIZE, MATRIX_SIZE)
        adjust(1)
    }

    /**
     * Multiply the current matrix by a rotation matrix. First we create a matrix for rotation by
     * angle `angle` (in degrees) around the axis (x, y, z) in our temp matrix storage
     * `mTemp`, then we copy the top of stack matrix to the second temp matrix storage
     * location just above. Finally we call `android.opengl.Matrix.multiplyMM` to multiply
     * the old top of stack matrix by our rotation matrix placing the result in the top of stack
     * matrix.
     *
     * @param angle angle in degrees to rotate
     * @param x     x coordinate of vector to rotate around
     * @param y     y coordinate of vector to rotate around
     * @param z     z coordinate of vector to rotate around
     */
    fun glRotatef(angle: Float, x: Float, y: Float, z: Float) {
        Matrix.setRotateM(mTemp, 0, angle, x, y, z)
        System.arraycopy(mMatrix, mTop, mTemp, MATRIX_SIZE, MATRIX_SIZE)
        Matrix.multiplyMM(mMatrix, mTop, mTemp, MATRIX_SIZE, mTemp, 0)
    }

    /**
     * Multiply the current matrix by a rotation matrix. The input coordinates for the vector to
     * rotate around are given in 16.16 fixed point format, so we simply convert them to float
     * format and call our method `glRotatef(float, float, float, float)`.
     *
     * @param angle angle in degrees to rotate
     * @param x     x coordinate of vector to rotate around
     * @param y     y coordinate of vector to rotate around
     * @param z     z coordinate of vector to rotate around
     */
    fun glRotatex(angle: Int, x: Int, y: Int, z: Int) {
        glRotatef(angle.toFloat(), fixedToFloat(x), fixedToFloat(y), fixedToFloat(z))
    }

    /**
     * Scales the top of stack matrix in place by its input parameters x, y, and z. We simply call
     * the method `android.opengl.Matrix.scaleM` to do all the work for us.
     *
     * @param x scale factor along the x axis
     * @param y scale factor along the y axis
     * @param z scale factor along the z axis
     */
    fun glScalef(x: Float, y: Float, z: Float) {
        Matrix.scaleM(mMatrix, mTop, x, y, z)
    }

    /**
     * Scales the top of stack matrix in place by its input parameters x, y, and z. The input
     * parameters are in 16.16 fixed point format, so we convert them to float format and pass the
     * call on to our method `glScalef(float, float, float)`.
     *
     * @param x scale factor along the x axis
     * @param y scale factor along the y axis
     * @param z scale factor along the z axis
     */
    fun glScalex(x: Int, y: Int, z: Int) {
        glScalef(fixedToFloat(x), fixedToFloat(y), fixedToFloat(z))
    }

    /**
     * Translates our top of stack matrix by its parameters x, y, and z in place. We simply call the
     * method `android.opengl.Matrix.translateM` to do all the work for us.
     *
     * @param x x coordinate of the translation vector
     * @param y y coordinate of the translation vector
     * @param z z coordinate of the translation vector
     */
    fun glTranslatef(x: Float, y: Float, z: Float) {
        Matrix.translateM(mMatrix, mTop, x, y, z)
    }

    /**
     * Translates our top of stack matrix by its parameters x, y, and z in place. The input
     * parameters are in 16.16 fixed point format, so we convert them to float format and pass the
     * call on to our method `glTranslatef(float, float, float)`.
     *
     * @param x x coordinate of the translation vector
     * @param y y coordinate of the translation vector
     * @param z z coordinate of the translation vector
     */
    fun glTranslatex(x: Int, y: Int, z: Int) {
        glTranslatef(fixedToFloat(x), fixedToFloat(y), fixedToFloat(z))
    }

    /**
     * Copies our top of stack matrix to its input parameter `float[] dest`.
     *
     * @param dest   destination matrix to copy to
     * @param offset index of first location to copy to
     */
    fun getMatrix(dest: FloatArray?, offset: Int) {
        System.arraycopy(mMatrix, mTop, dest!!, offset, MATRIX_SIZE)
    }

    /**
     * Convenience function to convert a 16.16 fixed point format value to `float` format.
     *
     * @param fixedValue 16.16 fixed point format value
     * @return input parameter converted to `float` format
     */
    private fun fixedToFloat(fixedValue: Int): Float {
        return fixedValue * (1.0f / 65536.0f)
    }

    /**
     * Sanity check to see if a push or pop of our matrix stack is a legal operation or not. Throws
     * an IllegalArgumentException if the operation would result in a value of `mTop` less
     * than zero, or greater than the storage allocated for our stack `float[] mMatrix`.
     *
     * @param dir number of matrices we want to push (positive number), or pop (negative number).
     */
    private fun preflightAdjust(dir: Int) {
        val newTop = mTop + dir * MATRIX_SIZE
        require(newTop >= 0) { "stack underflow" }
        require(newTop + MATRIX_SIZE <= mMatrix.size) { "stack overflow" }
    }

    /**
     * Adjusts our top of stack point `mTop` based on the number of matrices we wish to push
     * or to pop, leaving `mTop` pointing to the new top of stack matrix.
     *
     * @param dir number of matrices we want to push (positive number), or pop (negative number).
     */
    private fun adjust(dir: Int) {
        mTop += dir * MATRIX_SIZE
    }

    companion object {
        /**
         * Default depth of our matrix stack
         */
        private const val DEFAULT_MAX_DEPTH = 32
        /**
         * Size of each matrix in our stack
         */
        private const val MATRIX_SIZE = 16
    }
}
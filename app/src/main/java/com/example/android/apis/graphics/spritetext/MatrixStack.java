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

package com.example.android.apis.graphics.spritetext;

import android.opengl.Matrix;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * A matrix stack, similar to OpenGL ES's internal matrix stack.
 */
@SuppressWarnings("WeakerAccess")
public class MatrixStack {

    /**
     * Default depth of our matrix stack
     */
    private final static int DEFAULT_MAX_DEPTH = 32;
    /**
     * Size of each matrix in our stack
     */
    private final static int MATRIX_SIZE = 16;
    /**
     * Our stack of matrices, it is in our case a {@code float[]} array allocated enough storage for
     * DEFAULT_MAX_DEPTH*MATRIX_SIZE entries.
     */
    private float[] mMatrix;
    /**
     * Index of the current top of matrix stack, it goes from 0 to (DEFAULT_MAX_DEPTH-1)*MATRIX_SIZE
     * in steps of MATRIX_SIZE.
     */
    private int mTop;
    /**
     * Temporary storage for holding two matrices each having a size of MATRIX_SIZE.
     */
    private float[] mTemp;

    /**
     * Our constructor, we simply call our method {@code commonInit} to allocate the storage we need
     * for a matrix stack with DEFAULT_MAX_DEPTH matrices in it.
     */
    public MatrixStack() {
        commonInit(DEFAULT_MAX_DEPTH);
    }

    /**
     * Our constructor which allows the depth of the matrix stack to be specified UNUSED.
     *
     * @param maxDepth maximum depth of the matrix stack we are to hold
     */
    @SuppressWarnings("unused")
    public MatrixStack(int maxDepth) {
        commonInit(maxDepth);
    }

    /**
     * Initializes our instance by allocating storage for our fields {@code float[] mMatrix} and
     * {@code float[] mTemp} with its argument {@code int maxDepth} specifying how many matrices our
     * matrix stack needs to hold.
     *
     * @param maxDepth depth of matrix stack.
     */
    private void commonInit(int maxDepth) {
        mMatrix = new float[maxDepth * MATRIX_SIZE];
        mTemp = new float[MATRIX_SIZE * 2];
        glLoadIdentity();
    }

    /**
     * Loads the matrix at the top of the matrix stack with a projection matrix defined in terms of
     * the six clipping planes. We simply call the method {@code Matrix.frustumM} with {@code mMatrix}
     * as the output array using {@code mTop} as the index offset into that array, and passing our
     * parameters as the clipping planes.
     *
     * @param left   left vertical clipping plane
     * @param right  right vertical clipping plane
     * @param bottom bottom horizontal clipping plane
     * @param top    top horizontal clipping plane
     * @param near   near depth clipping planes
     * @param far    far depth clipping planes
     */
    public void glFrustumf(float left, float right, float bottom, float top, float near, float far) {
        Matrix.frustumM(mMatrix, mTop, left, right, bottom, top, near, far);
    }

    /**
     * Loads the matrix at the top of the matrix stack with a projection matrix defined in terms of
     * the six fixed point clipping planes. We simply convert the fixed values of our parameters to
     * float values using our method {@code fixedToFloat}, then pass the call to our method
     * {@code glFrustumf}.
     *
     * @param left   left vertical clipping plane
     * @param right  right vertical clipping plane
     * @param bottom bottom horizontal clipping plane
     * @param top    top horizontal clipping plane
     * @param near   near depth clipping planes
     * @param far    far depth clipping planes
     */
    public void glFrustumx(int left, int right, int bottom, int top, int near, int far) {
        glFrustumf(fixedToFloat(left), fixedToFloat(right),
                fixedToFloat(bottom), fixedToFloat(top),
                fixedToFloat(near), fixedToFloat(far));
    }

    /**
     * Loads the top of our matrix stack with the identity matrix, we simply call the static method
     * {@code android.opengl.Matrix.setIdentityM} to do this for us.
     */
    public void glLoadIdentity() {
        Matrix.setIdentityM(mMatrix, mTop);
    }

    /**
     * Replace the matrix at the top of our stack with the matrix {@code float[] m}.
     *
     * @param m      matrix to load to the top of the matrix stack
     * @param offset offset to first source location
     */
    public void glLoadMatrixf(float[] m, int offset) {
        System.arraycopy(m, offset, mMatrix, mTop, MATRIX_SIZE);
    }

    /**
     * Replace the matrix at the top of our stack with the matrix contained in {@code FloatBuffer m}.
     *
     * @param m {@code FloatBuffer} containing matrix to load to the top of the matrix stack
     */
    public void glLoadMatrixf(FloatBuffer m) {
        m.get(mMatrix, mTop, MATRIX_SIZE);
    }

    /**
     * Replace the matrix at the top of our stack with the matrix {@code int[] m} converted from fixed
     * point 16.16 format to {@code float} format.
     *
     * @param m      matrix to load to the top of the matrix stack
     * @param offset offset to first source location
     */
    public void glLoadMatrixx(int[] m, int offset) {
        for (int i = 0; i < MATRIX_SIZE; i++) {
            mMatrix[mTop + i] = fixedToFloat(m[offset + i]);
        }
    }

    /**
     * Replace the matrix at the top of our stack with the matrix contained in {@code IntBuffer m}
     * converted from fixed point 16.16 format to {@code float} format.
     *
     * @param m {@code IntBuffer} containing 16.16 format matrix to load to the top of the matrix stack
     */
    public void glLoadMatrixx(IntBuffer m) {
        for (int i = 0; i < MATRIX_SIZE; i++) {
            mMatrix[mTop + i] = fixedToFloat(m.get());
        }
    }

    /**
     * Replaces our top of stack matrix by the results of multiplying our top of stack matrix (lhs)
     * by the parameter matrix {@code float[] m} (rhs). Note that when this result is then used to
     * multiply a vector it is as if the rhs {@code m} matrix is used to multiply first, then the old
     * lhs top of stack matrix (a result of the way matrix multiplication works which might run
     * counter to your intuition).
     * <p>
     * First we copy our top of stack matrix to {@code float[] mTemp}, then we use the static method
     * {@code android.opengl.Matrix.multiplyMM} to multiply {@code mTemp} by {@code m} placing the
     * result in our top of stack matrix.
     *
     * @param m      matrix to multiply our top of stack matrix by
     * @param offset offset to first source location in {@code float[] m}
     */
    public void glMultMatrixf(float[] m, int offset) {
        System.arraycopy(mMatrix, mTop, mTemp, 0, MATRIX_SIZE);
        Matrix.multiplyMM(mMatrix, mTop, mTemp, 0, m, offset);
    }

    /**
     * Replaces our top of stack matrix by the results of multiplying our top of stack matrix (lhs)
     * by the matrix contained in our parameter {@code FloatBuffer m} (rhs).
     * <p>
     * To do this we {@code get} the matrix into the second matrix in our {@code float[] mTemp} temp
     * matrix storage, then call our method {@code glMultMatrixf(float[], int)} using this copy of the
     * matrix that was contained in {@code m}.
     *
     * @param m {@code FloatBuffer} containing a matrix to multiply our top of stack matrix by
     */
    public void glMultMatrixf(FloatBuffer m) {
        m.get(mTemp, MATRIX_SIZE, MATRIX_SIZE);
        glMultMatrixf(mTemp, MATRIX_SIZE);
    }

    /**
     * Replaces our top of stack matrix by the results of multiplying our top of stack matrix (lhs)
     * by the parameter matrix {@code int[] m} (rhs). To do this we convert the 16.16 fixed point
     * values of {@code m} to {@code float} values, storing the result in the second matrix in our
     * {@code float[] mTemp} temp matrix storage. We then call our method {@code glMultMatrixf(float[], int)}
     * using this converted copy of the matrix {@code m}.
     *
     * @param m      matrix to multiply our top of stack matrix by
     * @param offset offset to first source location
     */
    public void glMultMatrixx(int[] m, int offset) {
        for (int i = 0; i < MATRIX_SIZE; i++) {
            mTemp[MATRIX_SIZE + i] = fixedToFloat(m[offset + i]);
        }
        glMultMatrixf(mTemp, MATRIX_SIZE);
    }

    /**
     * Replaces our top of stack matrix by the results of multiplying our top of stack matrix (lhs)
     * by the matrix contained in our parameter {@code IntBuffer m}. To do this we {@code get} and
     * convert the 16.16 fixed point values of {@code m} to {@code float} values, storing the result
     * in the second matrix in our {@code float[] mTemp} temp matrix storage. We then call our method
     * {@code glMultMatrixf(float[], int)} using this converted copy of the matrix contained in
     * {@code m}.
     *
     * @param m {@code IntBuffer} containing 16.16 format matrix to multiply our top of stack matrix by.
     */
    public void glMultMatrixx(IntBuffer m) {
        for (int i = 0; i < MATRIX_SIZE; i++) {
            mTemp[MATRIX_SIZE + i] = fixedToFloat(m.get());
        }
        glMultMatrixf(mTemp, MATRIX_SIZE);
    }

    /**
     * Replaces our top of stack matrix with an orthographic projection matrix based on its input
     * parameters. We simply call the static method {@code android.opengl.Matrix.orthoM} to do all
     * our work for us.
     *
     * @param left   left vertical clipping plane
     * @param right  right vertical clipping plane
     * @param bottom bottom horizontal clipping plane
     * @param top    top horizontal clipping plane
     * @param near   near depth clipping plane
     * @param far    far depth clipping plane
     */
    public void glOrthof(float left, float right, float bottom, float top, float near, float far) {
        Matrix.orthoM(mMatrix, mTop, left, right, bottom, top, near, far);
    }

    /**
     * Replaces our top of stack matrix with an orthographic projection matrix based on its input
     * parameters. We pass our input parameters converted from fixed point 16.16 format to {@code float}
     * to our method {@code glOrthof(float, float, float, float, float, float)}.
     *
     * @param left   left vertical clipping plane
     * @param right  right vertical clipping plane
     * @param bottom bottom horizontal clipping plane
     * @param top    top horizontal clipping plane
     * @param near   near depth clipping plane
     * @param far    far depth clipping plane
     */
    public void glOrthox(int left, int right, int bottom, int top, int near, int far) {
        glOrthof(fixedToFloat(left), fixedToFloat(right),
                fixedToFloat(bottom), fixedToFloat(top),
                fixedToFloat(near), fixedToFloat(far));
    }

    /**
     * Pop's the top matrix off of the stack replacing the current matrix with the one below it on
     * the stack (the one that was saved by a previous call to {@code glPushMatrix}). First we call
     * our method {@code preflight_adjust} to make sure there at least one matrix below us on the
     * stack, throwing an IllegalArgumentException if not, then we call our method {@code adjust}
     * to subtract the length of one matrix from our pointer {@code mTop}.
     */
    public void glPopMatrix() {
        preflight_adjust(-1);
        adjust(-1);
    }

    /**
     * Pushes the current matrix stack down by one, duplicating the current matrix. That is, after a
     * {@code glPushMatrix} call, the matrix on top of the stack is identical to the one below it.
     * First we call our method {@code preflight_adjust} to make sure there is enough room for another
     * matrix on our stack, then we copy the current top of stack matrix to the area above it in the
     * stack, and call our method {@code adjust} to add the length of one matrix to the top of stack
     * pointer {@code mTop} (to point to the new top of stack matrix).
     */
    public void glPushMatrix() {
        preflight_adjust(1);
        System.arraycopy(mMatrix, mTop, mMatrix, mTop + MATRIX_SIZE, MATRIX_SIZE);
        adjust(1);
    }

    /**
     * Multiply the current matrix by a rotation matrix. First we create a matrix for rotation by
     * angle {@code angle} (in degrees) around the axis (x, y, z) in our temp matrix storage
     * {@code mTemp}, then we copy the top of stack matrix to the second temp matrix storage
     * location just above. Finally we call {@code android.opengl.Matrix.multiplyMM} to multiply
     * the old top of stack matrix by our rotation matrix placing the result in the top of stack
     * matrix.
     *
     * @param angle angle in degrees to rotate
     * @param x     x coordinate of vector to rotate around
     * @param y     y coordinate of vector to rotate around
     * @param z     z coordinate of vector to rotate around
     */
    public void glRotatef(float angle, float x, float y, float z) {
        Matrix.setRotateM(mTemp, 0, angle, x, y, z);
        System.arraycopy(mMatrix, mTop, mTemp, MATRIX_SIZE, MATRIX_SIZE);
        Matrix.multiplyMM(mMatrix, mTop, mTemp, MATRIX_SIZE, mTemp, 0);
    }

    /**
     * Multiply the current matrix by a rotation matrix. The input coordinates for the vector to
     * rotate around are given in 16.16 fixed point format, so we simply convert them to float
     * format and call our method {@code glRotatef(float, float, float, float)}.
     *
     * @param angle angle in degrees to rotate
     * @param x     x coordinate of vector to rotate around
     * @param y     y coordinate of vector to rotate around
     * @param z     z coordinate of vector to rotate around
     */
    public void glRotatex(int angle, int x, int y, int z) {
        glRotatef(angle, fixedToFloat(x), fixedToFloat(y), fixedToFloat(z));
    }

    /**
     * Scales the top of stack matrix in place by its input parameters x, y, and z. We simply call
     * the method {@code android.opengl.Matrix.scaleM} to do all the work for us.
     *
     * @param x scale factor along the x axis
     * @param y scale factor along the y axis
     * @param z scale factor along the z axis
     */
    public void glScalef(float x, float y, float z) {
        Matrix.scaleM(mMatrix, mTop, x, y, z);
    }

    /**
     * Scales the top of stack matrix in place by its input parameters x, y, and z. The input
     * parameters are in 16.16 fixed point format, so we convert them to float format and pass the
     * call on to our method {@code glScalef(float, float, float)}.
     *
     * @param x scale factor along the x axis
     * @param y scale factor along the y axis
     * @param z scale factor along the z axis
     */
    public void glScalex(int x, int y, int z) {
        glScalef(fixedToFloat(x), fixedToFloat(y), fixedToFloat(z));
    }

    /**
     * Translates our top of stack matrix by its parameters x, y, and z in place. We simply call the
     * method {@code android.opengl.Matrix.translateM} to do all the work for us.
     *
     * @param x x coordinate of the translation vector
     * @param y y coordinate of the translation vector
     * @param z z coordinate of the translation vector
     */
    public void glTranslatef(float x, float y, float z) {
        Matrix.translateM(mMatrix, mTop, x, y, z);
    }

    /**
     * Translates our top of stack matrix by its parameters x, y, and z in place. The input
     * parameters are in 16.16 fixed point format, so we convert them to float format and pass the
     * call on to our method {@code glTranslatef(float, float, float)}.
     *
     * @param x x coordinate of the translation vector
     * @param y y coordinate of the translation vector
     * @param z z coordinate of the translation vector
     */
    public void glTranslatex(int x, int y, int z) {
        glTranslatef(fixedToFloat(x), fixedToFloat(y), fixedToFloat(z));
    }

    /**
     * Copies our top of stack matrix to its input parameter {@code float[] dest}.
     *
     * @param dest   destination matrix to copy to
     * @param offset index of first location to copy to
     */
    public void getMatrix(float[] dest, int offset) {
        System.arraycopy(mMatrix, mTop, dest, offset, MATRIX_SIZE);
    }

    /**
     * Convenience function to convert a 16.16 fixed point format value to {@code float} format.
     *
     * @param fixedValue 16.16 fixed point format value
     * @return input parameter converted to {@code float} format
     */
    private float fixedToFloat(int fixedValue) {
        return fixedValue * (1.0f / 65536.0f);
    }

    /**
     * Sanity check to see if a push or pop of our matrix stack is a legal operation or not. Throws
     * an IllegalArgumentException if the operation would result in a value of {@code mTop} less
     * than zero, or greater than the storage allocated for our stack {@code float[] mMatrix}.
     *
     * @param dir number of matrices we want to push (positive number), or pop (negative number).
     */
    private void preflight_adjust(int dir) {
        int newTop = mTop + dir * MATRIX_SIZE;
        if (newTop < 0) {
            throw new IllegalArgumentException("stack underflow");
        }
        if (newTop + MATRIX_SIZE > mMatrix.length) {
            throw new IllegalArgumentException("stack overflow");
        }
    }

    /**
     * Adjusts our top of stack point {@code mTop} based on the number of matrices we wish to push
     * or to pop, leaving {@code mTop} pointing to the new top of stack matrix.
     *
     * @param dir number of matrices we want to push (positive number), or pop (negative number).
     */
    private void adjust(int dir) {
        mTop += dir * MATRIX_SIZE;
    }
}

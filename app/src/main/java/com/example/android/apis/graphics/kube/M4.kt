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
package com.example.android.apis.graphics.kube

/**
 * A 4x4 float matrix which is used to move `GLVertex` x,y,z locations
 */
class M4 {
    /**
     * Our 4x4 float matrix which are methods operate on.
     */
    var m = Array(4) { FloatArray(4) }

    /**
     * Our basic constructor, which does nothing, but gives us an all zero field `float[][] m`
     * to use.
     */
    constructor()

    /**
     * Constructor which clones another instance of `M4` using deep copy.
     *
     * @param other `M4` object we are to deep copy.
     */
    constructor(other: M4) {
        for (i in 0..3) {
            for (j in 0..3) {
                m[i][j] = other.m[i][j]
            }
        }
    }

    /**
     * Multiplies a `GLVertex src` by our field `float[][] m` and places the results in
     * `GLVertex dest`. Simple multiplication of a vector by a matrix. The `m[3]` array
     * appears to be superfluous since only `m[3][3]` is non-zero (set to 1.0 by the method
     * `setIdentity` and it is never used oddly enough.
     *
     * @param src Source `GLVertex` to multiply by our field `float[][] m`.
     * @param dest Destination `GLVertex` to place results in.
     */
    fun multiply(src: GLVertex, dest: GLVertex) {
        dest.x = src.x * m[0][0] + src.y * m[1][0] + src.z * m[2][0] + m[3][0]
        dest.y = src.x * m[0][1] + src.y * m[1][1] + src.z * m[2][1] + m[3][1]
        dest.z = src.x * m[0][2] + src.y * m[1][2] + src.z * m[2][2] + m[3][2]
    }

    /**
     * Simple 4x4 matrix multiplication, the 4x4 float matrix in the field `other.m` is
     * multiplied by our own field `float[][] m` and the result is returned to the caller.
     *
     * @param other `M4` matrix to multiply by our own matrix
     * @return the result of multiplying `M4 other` by our own matrix.
     */
    fun multiply(other: M4): M4 {
        val result = M4()
        val m1 = m
        val m2 = other.m
        for (i in 0..3) {
            for (j in 0..3) {
                result.m[i][j] = m1[i][0] * m2[0][j] + m1[i][1] * m2[1][j] + m1[i][2] * m2[2][j] + m1[i][3] * m2[3][j]
            }
        }
        return result
    }

    /**
     * Sets the contents of our field `float[][] m` to the identity matrix.
     */
    fun setIdentity() {
        for (i in 0..3) {
            for (j in 0..3) {
                m[i][j] = if (i == j) 1f else 0f
            }
        }
    }

    /**
     * Turns our field `float[][] m` into a string for debugging purposes.
     *
     * @return printable string version of our 4x4 float matrix
     */
    override fun toString(): String {
        val builder = StringBuilder("[ ")
        for (i in 0..3) {
            for (j in 0..3) {
                builder.append(m[i][j])
                builder.append(" ")
            }
            if (i < 2) builder.append("\n  ")
        }
        builder.append(" ]")
        return builder.toString()
    }
}
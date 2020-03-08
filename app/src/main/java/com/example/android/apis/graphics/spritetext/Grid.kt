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

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.CharBuffer
import java.nio.FloatBuffer
import javax.microedition.khronos.opengles.GL10

/**
 * A 2D rectangular mesh. Can be drawn textured or untextured. UNUSED!
 */
@Suppress("unused")
internal class Grid(w: Int, h: Int) {
    private val mVertexBuffer: FloatBuffer
    private val mTexCoordBuffer: FloatBuffer
    private val mIndexBuffer: CharBuffer
    private val mW: Int
    private val mH: Int
    private val mIndexCount: Int
    operator fun set(i: Int, j: Int, x: Float, y: Float, z: Float, u: Float, v: Float) {
        require(!(i < 0 || i >= mW)) { "i" }
        require(!(j < 0 || j >= mH)) { "j" }
        val index = mW * j + i
        val posIndex = index * 3
        mVertexBuffer.put(posIndex, x)
        mVertexBuffer.put(posIndex + 1, y)
        mVertexBuffer.put(posIndex + 2, z)
        val texIndex = index * 2
        mTexCoordBuffer.put(texIndex, u)
        mTexCoordBuffer.put(texIndex + 1, v)
    }

    fun draw(gl: GL10, useTexture: Boolean) {
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer)
        if (useTexture) {
            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY)
            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexCoordBuffer)
            gl.glEnable(GL10.GL_TEXTURE_2D)
        } else {
            gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY)
            gl.glDisable(GL10.GL_TEXTURE_2D)
        }
        gl.glDrawElements(GL10.GL_TRIANGLES, mIndexCount, GL10.GL_UNSIGNED_SHORT, mIndexBuffer)
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY)
    }

    init {
        require(!(w < 0 || w >= 65536)) { "w" }
        require(!(h < 0 || h >= 65536)) { "h" }
        require(w * h < 65536) { "w * h >= 65536" }
        mW = w
        mH = h
        val size = w * h
        @Suppress("LocalVariableName")
        val FLOAT_SIZE = 4
        @Suppress("LocalVariableName")
        val CHAR_SIZE = 2
        mVertexBuffer = ByteBuffer.allocateDirect(FLOAT_SIZE * size * 3)
                .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mTexCoordBuffer = ByteBuffer.allocateDirect(FLOAT_SIZE * size * 2)
                .order(ByteOrder.nativeOrder()).asFloatBuffer()
        val quadW = mW - 1
        val quadH = mH - 1
        val quadCount = quadW * quadH
        val indexCount = quadCount * 6
        mIndexCount = indexCount
        mIndexBuffer = ByteBuffer.allocateDirect(CHAR_SIZE * indexCount)
                .order(ByteOrder.nativeOrder()).asCharBuffer()

        /*
         * Initialize triangle list mesh.
         *
         *     [0]-----[  1] ...
         *      |    /   |
         *      |   /    |
         *      |  /     |
         *     [w]-----[w+1] ...
         *      |       |
         *
         */

        var i = 0
        for (y in 0 until quadH) {
            for (x in 0 until quadW) {
                val a = (y * mW + x).toChar()
                val b = (y * mW + x + 1).toChar()
                val c = ((y + 1) * mW + x).toChar()
                val d = ((y + 1) * mW + x + 1).toChar()
                mIndexBuffer.put(i++, a)
                mIndexBuffer.put(i++, b)
                mIndexBuffer.put(i++, c)
                mIndexBuffer.put(i++, b)
                mIndexBuffer.put(i++, c)
                mIndexBuffer.put(i++, d)
            }
        }

    }
}
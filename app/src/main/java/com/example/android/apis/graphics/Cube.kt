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
package com.example.android.apis.graphics

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import javax.microedition.khronos.opengles.GL10

/**
 * A vertex shaded cube.
 */
internal class Cube {
    /**
     * `VertexBuffer` for the eight corners of our cube.
     */
    private val mVertexBuffer: IntBuffer
    /**
     * Array of colors used to draw our cube's surface, one for each vertex. `glDrawElements`
     * will smoothly morph between the color at a vertex to the color of each connected vertex when
     * drawing the surface of the triangle defined by three vertices.
     */
    private val mColorBuffer: IntBuffer
    /**
     * Array of indices pointing to [mVertexBuffer] vertices with three indices per triangle
     * specified in a counter clockwise manner so that the normal points out of the triangle. There
     * are a total of 12 triangles used to draw the faces of our cube.
     */
    private val mIndexBuffer: ByteBuffer

    /**
     * Called when we should draw our cube. First we specify the orientation of front-facing
     * polygons to be GL_CW (clockwise winding). Next we call `glVertexPointer` to specify the
     * location of vertex coordinates to use when rendering to be [IntBuffer] field [mVertexBuffer],
     * using the size 3, type GL_FIXED, and stride of 0. We call `glColorPointer` to specify the
     * location of color components to use when rendering to be [IntBuffer] field [mColorBuffer],
     * with the size 4, type GL_FIXED, and stride 0. Finally we call `glDrawElements` to draw the
     * primitives of our cube using GL_TRIANGLES as the primitive type, 36 as the number of elements,
     * GL_UNSIGNED_BYTE as the type of values used in our index buffer, and [ByteBuffer] field
     * [mIndexBuffer] for the vertex indexes to use for the 12 triangles. When the triangles are
     * drawn each vertex references the colors for each vertex contained in [IntBuffer] field
     * [mColorBuffer] and the surface between the vertices is colored to morph smoothly from one
     * vertex color to the other.
     *
     * @param gl the GL interface.
     */
    fun draw(gl: GL10) {
        gl.glFrontFace(GL10.GL_CW)
        gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer)
        gl.glColorPointer(4, GL10.GL_FIXED, 0, mColorBuffer)
        gl.glDrawElements(GL10.GL_TRIANGLES, 36, GL10.GL_UNSIGNED_BYTE, mIndexBuffer)
    }

    /**
     * Default constructor for our Cube. We initialize IntBuffer mVertexBuffer with the values of
     * the int vertices[] array, IntBuffer mColorBuffer with the values of the int colors[] array,
     * and ByteBuffer mIndexBuffer with the values of the byte indices[] array for use in our draw
     * method.
     *
     * Next we allocate `ByteBuffer vbb` on the native heap, set its byte order to native order,
     * initialize our field `IntBuffer mVertexBuffer` with a view of `vbb` as an IntBuffer,
     * transfer the contents of `int vertices[]` to `mVertexBuffer`, and reposition it
     * to point to the first entry.
     *
     *
     * We allocate `ByteBuffer cbb` on the native heap, set its byte order to native order,
     * initialize our field `IntBuffer mColorBuffer` with a view of `cbb` as an IntBuffer,
     * transfer the contents of `int colors[]` to `mColorBuffer`, and reposition it
     * to point to the first entry.
     *
     *
     * Finally we allocate `ByteBuffer mIndexBuffer` on the native heap, transfer the contents
     * of `byte indices[]`  to it, and reposition it to point to the first entry.
     */
    init {
        val one = 0x10000 // Fixed point value to use for coordinates and rgba values
        /*
         * xyz coordinates of the eight corners of the cube
         */
        val vertices = intArrayOf(
                -one, -one, -one,
                one, -one, -one,
                one, one, -one,
                -one, one, -one,
                -one, -one, one,
                one, -one, one,
                one, one, one,
                -one, one, one)
        /*
         * Colors for the eight vertices of the cube
         */
        val colors = intArrayOf(
                0, 0, 0, one,
                one, 0, 0, one,
                one, one, 0, one,
                0, one, 0, one,
                0, 0, one, one,
                one, 0, one, one,
                one, one, one, one,
                0, one, one, one)
        /*
         * Indexes to define the 12 triangles used to draw the cube
         */
        val indices = byteArrayOf(
                0, 4, 5, 0, 5, 1,
                1, 5, 6, 1, 6, 2,
                2, 6, 7, 2, 7, 3,
                3, 7, 4, 3, 4, 0,
                4, 7, 6, 4, 6, 5,
                3, 0, 1, 3, 1, 2
        )
        /**
         * Buffers to be passed to gl*Pointer() functions
         * must be direct, i.e., they must be placed on the
         * native heap where the garbage collector cannot
         * move them.
         *
         *  Buffers with multi-byte data types (e.g., short, int, float)
         *  must have their byte order set to native order
         */
        val vbb = ByteBuffer.allocateDirect(vertices.size * 4)
        vbb.order(ByteOrder.nativeOrder())
        mVertexBuffer = vbb.asIntBuffer()
        mVertexBuffer.put(vertices)
        mVertexBuffer.position(0)
        val cbb = ByteBuffer.allocateDirect(colors.size * 4)
        cbb.order(ByteOrder.nativeOrder())
        mColorBuffer = cbb.asIntBuffer()
        mColorBuffer.put(colors)
        mColorBuffer.position(0)
        mIndexBuffer = ByteBuffer.allocateDirect(indices.size)
        mIndexBuffer.put(indices)
        mIndexBuffer.position(0)
    }
}
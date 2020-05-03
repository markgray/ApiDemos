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

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import java.nio.ShortBuffer
import java.util.ArrayList
import javax.microedition.khronos.opengles.GL10

/**
 * Handy class which contains lists of [GLShape] and [GLVertex] objects which can be
 * added to using the methods [addShape] and [addVertex] respectively. When done adding
 * these objects, you can call the method [generate] which translates these objects into the
 * format required to feed to the openGL method `glDrawElements` (the vertex buffer
 * [IntBuffer] field [mVertexBuffer], the color buffer [IntBuffer] field [mColorBuffer] and the
 * index buffer [ShortBuffer] field [mIndexBuffer]. It also contains the method [transformVertex]
 * which will apply an [M4] transformation matrix to a vertex so that the rubic cube can be animated.
 * The [draw] method is called from the `onDrawFrame` callback of [KubeRenderer] whenever it is
 * necessary to draw the latest version of our rubic cube ([generate] has to have been called first
 * of course).
 */
class GLWorld {
    /**
     * Count of number of times our [draw] method has been called (for debugging purposes?)
     */
    var count = 0
    /**
     * List of all the [GLShape] objects comprising our rubic cube.
     */
    private val mShapeList = ArrayList<GLShape>()
    /**
     * List of all the [GLVertex] objects comprising our rubic cube.
     */
    private val mVertexList = ArrayList<GLVertex>()
    /**
     * Count of number of indices required to divide our Rubic cube into GL_TRIANGLES, it is used to
     * allocate space for [ShortBuffer] field [mIndexBuffer] and as the `count` argument to the
     * call of `glDrawElements`.
     */
    private var mIndexCount = 0
    /**
     * Direct allocated buffer used as the vertex buffer in the call of `glDrawElements`, once
     * the entire Rubic cube has been generated, each [GLVertex] in our list [mVertexList]
     * is instructed to add its x,y,z coordinates to [mVertexBuffer] (in the same call to
     * [GLVertex.put] which adds its color to [IntBuffer] field [mColorBuffer]).
     */
    private var mVertexBuffer: IntBuffer? = null
    /**
     * Direct allocated buffer used as the color buffer in the call of `glDrawElements`, once
     * the entire Rubic cube has been generated, each [GLVertex] in our list [mVertexList]
     * is instructed to add its color to [IntBuffer] field [mColorBuffer] (in the same call to
     * [GLVertex.put] which adds its x,y,z coordinates to [mVertexBuffer]).
     */
    private var mColorBuffer: IntBuffer? = null
    /**
     * Direct allocated buffer used as the index buffer in the call of `glDrawElements`, it is
     * constructed in our method [generate] by requesting each [GLShape] in our list
     * `ArrayList<GLShape>` field [mShapeList] to add its indices to [mIndexBuffer] using its
     * method `putIndices`.
     */
    private var mIndexBuffer: ShortBuffer? = null

    /**
     * Adds its [GLShape] parameter [shape] to our list `ArrayList<GLShape>` field [mShapeList] and
     * updates [mIndexCount] by adding the number of indices required by the [GLShape] to store its
     * vertices.
     *
     * @param shape [GLShape] to add to `ArrayList<GLShape>` field [mShapeList]
     */
    fun addShape(shape: GLShape) {
        mShapeList.add(shape)
        mIndexCount += shape.indexCount
    }

    /**
     * Constructs a [GLVertex] `val vertex` from its parameters, adds it to our list of vertices in
     * `ArrayList<GLVertex>` field [mVertexList], and returns the new [GLVertex] to the caller.
     *
     * @param x x coordinate of new vertex
     * @param y y coordinate of new vertex
     * @param z z coordinate of new vertex
     * @return the [GLVertex] object we have constructed and added to our list of vertices in
     * `ArrayList<GLVertex>` field [mVertexList]
     */
    fun addVertex(x: Float, y: Float, z: Float): GLVertex {
        val vertex = GLVertex(x, y, z, mVertexList.size)
        mVertexList.add(vertex)
        return vertex
    }

    /**
     * Allocates and fills the direct allocated buffers required by the method `glDrawElements`
     * when it draws our Rubic cube: [IntBuffer] field [mColorBuffer] for the color buffer,
     * [IntBuffer] field [mVertexBuffer] for the vertex buffer, and [ShortBuffer] field [mIndexBuffer]
     * for the index buffer. First we direct allocate [ByteBuffer] `var bb` to be the correct number
     * of bytes required for a color buffer (4 bytes per component, and 4 components for each vertex
     * in `ArrayList<GLVertex>` field [mVertexList]), set its byte order to native order and
     * initialize [IntBuffer] field [mColorBuffer] with a view of `bb` as an int buffer. Then we
     * direct allocate `bb` to have the correct number of bytes required for a vertex buffer
     * (4 bytes per coordinate, and 3 coordinates per vertex), set its byte order to native
     * order and initialize [IntBuffer] field [mVertexBuffer] with a view of `bb` as an int buffer.
     * Next we direct allocate `bb` to have the correct number of bytes required for an index
     * buffer (2 bytes per index value), set its byte order to native order and initialize
     * [ShortBuffer] field [mIndexBuffer] with a view of `bb` as an short buffer.
     *
     * Now we iterate through all the [GLVertex] objects in `ArrayList<GLVertex>` field [mVertexList]
     * fetching the next [GLVertex] to `val vertex` and calling its `put` method to add its
     * coordinates to [IntBuffer] field [mVertexBuffer] and its color to [IntBuffer] field
     * [mColorBuffer].
     *
     * Finally we iterate through all the [GLShape] objects in `ArrayList<GLShape>` field
     * [mShapeList] fetching the next [GLShape] to `val shape` and calling its `putIndices` method
     * to add the index values to [ShortBuffer] field [mIndexBuffer] which will divide the [GLShape]
     * into GL_TRIANGLES for `glDrawElements` to draw.
     */
    fun generate() {
        var bb = ByteBuffer.allocateDirect(mVertexList.size * 4 * 4)
        bb.order(ByteOrder.nativeOrder())
        mColorBuffer = bb.asIntBuffer()
        bb = ByteBuffer.allocateDirect(mVertexList.size * 4 * 3)
        bb.order(ByteOrder.nativeOrder())
        mVertexBuffer = bb.asIntBuffer()
        bb = ByteBuffer.allocateDirect(mIndexCount * 2)
        bb.order(ByteOrder.nativeOrder())
        mIndexBuffer = bb.asShortBuffer()
        val iter2: Iterator<GLVertex> = mVertexList.iterator()
        while (iter2.hasNext()) {
            val vertex = iter2.next()
            vertex.put(mVertexBuffer!!, mColorBuffer!!)
        }
        val iter3: Iterator<GLShape> = mShapeList.iterator()
        while (iter3.hasNext()) {
            val shape = iter3.next()
            shape.putIndices(mIndexBuffer)
        }
    }

    /**
     * Applies its [M4] tranform matrix parameter [transform] to the coordinates of its [GLVertex]
     * parameter [vertex] and stores the result in our direct allocated vertex buffer [IntBuffer]
     * field [mVertexBuffer] ready for the next call to `draw` (the original coordinates of [vertex]
     * remain unchanged). Called from [GLShape.animateTransform] for each [GLVertex] in the [GLShape],
     * it is called from [Layer.setAngle], which is called from [Kube.animate], which is called from
     * [KubeRenderer.onDrawFrame]. We simply call the `update` method of [vertex] which multiplies
     * its coordinates by the [transform] transform matrix and places the new values in the correct
     * position in our [IntBuffer] field [mVertexBuffer].
     *
     * @param vertex    [GLVertex] to apply the [M4] transform matrix [transform] to
     * @param transform transform matrix that moves vertex to new position.
     */
    fun transformVertex(vertex: GLVertex, transform: M4?) {
        vertex.update(mVertexBuffer!!, transform)
    }

    /**
     * Called from [KubeRenderer.onDrawFrame] to draw our Rubic cube. First we reset the position
     * of the direct allocated buffers used by `glDrawElements`: [IntBuffer] field [mColorBuffer]
     * (color buffer), [IntBuffer] field [mVertexBuffer] (vertex buffer), and [ShortBuffer] field
     * [mIndexBuffer] (index buffer). Then we specify the orientation of front-facing polygons to
     * be GL_CW, and select flat shade model.
     *
     * We define an array of vertex data to have 3 coordinates per vertex, GL_FIXED as its data type,
     * 0 for its stride, and [IntBuffer] field [mVertexBuffer] as the pointer to the first element
     * in the array.
     *
     * We define an array of colors to have 4 color components per color, GL_FIXED as its data type,
     * 0 for its stride, and [IntBuffer] field [mColorBuffer] as a pointer to the first component of
     * the first color element in the array.
     *
     * Finally we call `glDrawElements` to render primitives from the array data we specified
     * above, using GL_TRIANGLES as the primitives to draw, [mIndexCount] the number of elements
     * to use from the arrays, and [ShortBuffer] field [mIndexBuffer] as the pointer to the location
     * where the indices are stored.
     *
     * We increment `count` for no apparent reason.
     *
     * @param gl the GL interface.
     */
    fun draw(gl: GL10) {
        mColorBuffer!!.position(0)
        mVertexBuffer!!.position(0)
        mIndexBuffer!!.position(0)
        gl.glFrontFace(GL10.GL_CW)
        gl.glShadeModel(GL10.GL_FLAT)
        gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer)
        gl.glColorPointer(4, GL10.GL_FIXED, 0, mColorBuffer)
        gl.glDrawElements(GL10.GL_TRIANGLES, mIndexCount, GL10.GL_UNSIGNED_SHORT, mIndexBuffer)
        count++
    }

    companion object {
        /**
         * Unused so who cares.
         *
         * @param x who cares
         * @return who cares
         */
        fun toFloat(x: Int): Float {
            return x / 65536.0f
        }
    }
}
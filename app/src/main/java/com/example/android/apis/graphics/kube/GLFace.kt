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

import android.util.Log
import java.nio.ShortBuffer
import java.util.*

/**
 * Class representing a flat surface of a [GLShape], a quadrilateral (square) in the case of
 * our [Cube] class which contains 6 [GLFace] objects.
 */
@Suppress("MemberVisibilityCanBePrivate")
class GLFace {
    /**
     * List of vertices which define our [GLFace]
     */
    private val mVertexList = ArrayList<GLVertex>()
    /**
     * Color of this face (and the last one of its vertices)
     */
    private var mColor: GLColor? = null

    /**
     * Unused so I won't comment on it.
     */
    @Suppress("unused")
    constructor()

    /**
     * Constructor for triangles. Unused
     *
     * @param v1 First `GLVertex` of our triangle
     * @param v2 Second `GLVertex` of our triangle
     * @param v3 Third `GLVertex` of our triangle
     */
    @Suppress("unused")
    constructor(v1: GLVertex, v2: GLVertex, v3: GLVertex) {
        addVertex(v1)
        addVertex(v2)
        addVertex(v3)
    }

    /**
     * Constructor for quadrilaterals, called from the constructor for a [Cube] object. We
     * simply call our method [addVertex] with each of our parameters and they are added in
     * order to the list of [GLVertex] objects in our `ArrayList<GLVertex>` field [mVertexList].
     *
     * @param v1 First [GLVertex] of our quadrilateral
     * @param v2 Second [GLVertex] of our quadrilateral
     * @param v3 Third [GLVertex] of our quadrilateral
     * @param v4 Fourth [GLVertex] of our quadrilateral
     */
    constructor(v1: GLVertex, v2: GLVertex, v3: GLVertex, v4: GLVertex) {
        addVertex(v1)
        addVertex(v2)
        addVertex(v3)
        addVertex(v4)
    }

    /**
     * Convenience function that adds its [GLVertex] parameter [v] to the list of our vertices
     * contained in our `ArrayList<GLVertex>` field [mVertexList]. Called only from our constructors.
     *
     * @param v [GLVertex] to add to our list of vertices in [mVertexList].
     */
    fun addVertex(v: GLVertex) {
        mVertexList.add(v)
    }

    /**
     * Sets the color used to draw this instance of [GLFace] to the [GLColor] parameter [c].
     * Must be called after all vertices are added, it is called by the [GLShape.setFaceColor]
     * method, which is called only from the [Kube.makeGLWorld] method . First we set [Int] variable
     * `val last` to the index of the last [GLVertex] in our `ArrayList<GLVertex>` field [mVertexList],
     * and if it is less than 2 we log it as an error and proceed, otherwise we fetch the [GLVertex]
     * `var vertex` from the last location in the list [mVertexList] and if our field [mColor] is
     * *null* (has never been set) we loop through our `ArrayList<GLVertex>` field [mVertexList] as
     * a ring list inserting the current last [GLVertex] at the beginning, removing it from the end,
     * and setting `vertex` to the new last [GLVertex], looping until we find a [GLVertex] `vertex`
     * which does not have its color already set. While obviously this process would never end if
     * all of our [GLVertex] objects already had their color set, in our case the first [GLVertex]
     * always has a null `color` field. In any case we then set the `color` field of `vertex` to
     * our [GLColor] parameter [c], and finally set our [GLColor] field [mColor] to [c].
     *
     * @param c `GLColor` to use for this instance of `GLFace`
     */
    fun setColor(c: GLColor?) {
        val last = mVertexList.size - 1
        if (last < 2) {
            Log.e("GLFace", "not enough vertices in setColor()")
        } else {
            var vertex = mVertexList[last]
            // only need to do this if the color has never been set
            if (mColor == null) {
                while (vertex.color != null) {
                    mVertexList.add(0, vertex)
                    mVertexList.removeAt(last + 1)
                    vertex = mVertexList[last]
                }
            }
            vertex.color = c
        }
        mColor = c
    }

    /**
     * Calculates and returns the number of indices required to draw the vertices in our list of
     * vertices `ArrayList<GLVertex>` field [mVertexList]. Called from [GLShape.getIndexCount],
     * which is called from [GLWorld.addShape], which is called from [Kube.makeGLWorld], which
     * is called from the `onCreate` override of [Kube] when it calls the constructor for
     * [KubeRenderer] to initialize its [KubeRenderer] field `mRenderer`. Since in our case
     * we always have 4 [GLVertex] objects in our `ArrayList<GLVertex>` field [mVertexList], we
     * always return the number 6.
     *
     * @return number of indices required for the `GLVertex` objects in our list of vertices
     * `ArrayList<GLVertex>` field [mVertexList].
     */
    val indexCount: Int
        get() = (mVertexList.size - 2) * 3

    /**
     * Divides our [GLFace] instance into GL_TRIANGLES by grouping the [GLVertex] objects in
     * `ArrayList<GLVertex>` field [mVertexList] in groups of three, then adding the `index`
     * field of the [GLVertex] objects to the [ShortBuffer] parameter [buffer] passed us (it is
     * used as the index buffer in a call to `glDrawElements` which draws the current state of the
     * entire rubic cube). First we set [Int] variable `val last` to the location of the last
     * [GLVertex] in our [mVertexList] list, we fetch the first item in the list to [GLVertex]
     * variable `var v0`, and the last [GLVertex] to `val vn`. Then we loop through the [GLVertex]
     * objects starting with the second in the list and ending with the item before the last in the
     * list fetching the item to [GLVertex] variable `val v1`. We then add the `index` field of `v0`,
     * `v1`, and `vn` to `buffer`, and advance `v0` to point to the present [GLVertex] in `v1` and
     * loop for the next item in the list. In the case of our quadrilateral [GLFace] this results in
     * two `GL_TRIANGLES` consisting of the [GLVertex] items (0,1,3) and (1,2,3).
     *
     * @param buffer [ShortBuffer] field `mIndexBuffer` which is directly allocated on the native
     * heap so it can be used as the index buffer for a `glDrawElements` call.
     */
    fun putIndices(buffer: ShortBuffer) {
        val last = mVertexList.size - 1
        var v0 = mVertexList[0]
        val vn = mVertexList[last]
        // push triangles into the buffer
        for (i in 1 until last) {
            val v1 = mVertexList[i]
            buffer.put(v0.index)
            buffer.put(v1.index)
            buffer.put(vn.index)
            v0 = v1
        }
    }
}
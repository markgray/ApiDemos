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
@file:Suppress("ReplaceNotNullAssertionWithElvisReturn")

package com.example.android.apis.graphics.kube

import java.nio.ShortBuffer

/**
 * Base class for [Cube], which uses its methods [addVertex] and [addFace] to add
 * [GLVertex] and [GLFace] instances and to add data describing a single instance of
 * [Cube] that is part of the 3 by 3 by 3 [Cube] rubic cube. Other classes then use the
 * other methods provided to modify the [Cube].
 */
@Suppress("MemberVisibilityCanBePrivate")
open class GLShape(

    /**
     * [GLWorld] we belong to, it is set in our constructor, which is called from the constructor
     * of [Cube], which is called from the [Kube.makeGLWorld] method (once for each [Cube] in our
     * rubic cube), which is called in the `onCreate` override of [Kube] which uses the [GLWorld]
     * object it creates when it creates its instance of [KubeRenderer].
     */
    var mWorld: GLWorld
) {
    /**
     * Current transformation matrix, it is used to move the vertices of our shape to the position
     * required for the current location of our [GLShape] (a [Cube] in our demo) using the method
     * [GLWorld.transformVertex] of [mWorld]. This happens whenever our method [animateTransform]
     * is called from [Layer.setAngle] which is called from the method [Kube.animate], which gets
     * called from the `onDrawFrame` method of [KubeRenderer].
     */
    var mTransform: M4? = null

    /**
     * Transform to multiply [mTransform] field [M4] by in order to move this instance of [GLShape]
     * to the next step in its animation. It is set by a call to our method [animateTransform],
     * which is called from [Layer.setAngle], which is called from [Kube.animate], which
     * gets called from the `onDrawFrame` method of [KubeRenderer].
     */
    var mAnimateTransform: M4? = null

    /**
     * List of [GLFace] faces making up our [GLShape], it is added to by our method
     * [addFace], which is called from the constructor for a [Cube] object (our one
     * and only subclass).
     */
    protected var mFaceList: ArrayList<GLFace> = ArrayList()

    /**
     * List of [GLVertex] vertices making up our [GLShape], it is added to by our method
     * [addVertex], which is called from the constructor for a [Cube] object (our one and
     * only subclass).
     */
    protected var mVertexList: ArrayList<GLVertex> = ArrayList()

    /**
     * We do not use, so who cares?
     */
    @Suppress("unused")
    protected var mIndexList: ArrayList<Int> = ArrayList() // make more efficient?

    /**
     * Adds a [GLFace] to our list `ArrayList<GLFace>` field [mFaceList]. Called only from the
     * constructor for a [Cube] object.
     *
     * @param face [GLFace] to add
     */
    fun addFace(face: GLFace) {
        mFaceList.add(face)
    }

    /**
     * Sets the color of the [GLFace] which is at position [face] in our `ArrayList<GLFace>`
     * field [mFaceList]. Called only from the method [Kube.makeGLWorld]. We retrieve a reference
     * to the [GLFace] at position [face] in our [mFaceList] list, and call its [GLFace.setColor]
     * method  which sets thecolor of all the vertices used by the [GLFace] to [GLColor] parameter
     * [color].
     *
     * @param face  index into our list `ArrayList<GLFace> mFaceList`
     * @param color `GLColor` that we want the `GLFace` to have.
     */
    fun setFaceColor(face: Int, color: GLColor?) {
        mFaceList[face].setColor(color)
    }

    /**
     * Adds all the indices used by all the [GLFace] objects contained in our `ArrayList<GLFace>`
     * field [mFaceList] to the index buffer passed us in [buffer]. Called only from the method
     * [GLWorld.generate] which adds all the indices used by all the [GLShape] objects in its
     * `ArrayList<GLShape>` field `mShapeList` to its [ShortBuffer] index buffer field `mIndexBuffer`.
     * First we set `Iterator<GLFace>` variable `val iter` to an iterator over all the elements in
     * our `ArrayList<GLFace>` field [mFaceList], then as long as there is a "next" element we fetch
     * a reference to that element to [GLFace] variable `val face` and call its method `putIndices`
     * to add all the indices of `face` to [buffer].
     *
     * @param buffer openGL index buffer to add all our indices to.
     */
    fun putIndices(buffer: ShortBuffer?) {
        val iter: Iterator<GLFace> = mFaceList.iterator()
        while (iter.hasNext()) {
            val face = iter.next()
            face.putIndices(buffer!!)
        }
    }

    /**
     * Adds up the number of indices used by all the faces in our `ArrayList<GLFace>` field
     * [mFaceList]. Called only from the method [GLWorld.addShape]. We initialize [Int] variable
     * `var count` to 0, then we set `Iterator<GLFace>` variable `val iter` to an iterator over
     * all the elements in the `ArrayList<GLFace>` field [mFaceList], and as long as there is a
     * "next" element we fetch a reference to that element to [GLFace] variable `val face` and add
     * the value returned by its method `getIndexCount` to `count`. When done we return `count` to
     * the called.
     *
     * @return total number of indices in all our faces
     */
    val indexCount: Int
        get() {
            var count = 0
            val iter: Iterator<GLFace> = mFaceList.iterator()
            while (iter.hasNext()) {
                val face = iter.next()
                count += face.indexCount
            }
            return count
        }

    /**
     * Finds a vertex with the coordinates (x,y.z) in `ArrayList<GLVertex>` field [mVertexList] (if
     * it exists), or creates a [GLVertex] with those coordinates and adds it to our list. The
     * [GLVertex] is then returned to the caller. Called only from the constructor for a [Cube]
     * object. First we set `Iterator<GLVertex>` variable `val iter` to an iterator over all the
     * elements in the `ArrayList<GLVertex>` list [mVertexList], then as long as there is a "next"
     * element we fetch a reference to that element to [GLVertex] variable `val vertex`, and if all
     * three coordinates of `vertex` match the (x,y,z) coordinates passed us we return `vertex`.
     * If we are unable to find a matching `GLVertex` in our list, we create a [GLVertex] `val vertex`
     * by calling the [GLWorld.addVertex] method of [mWorld], add it to `ArrayList<GLVertex>` field
     * [mVertexList] and return that [GLVertex] to the caller.
     *
     * @param x x coordinate of vertex
     * @param y y coordinate of vertex
     * @param z z coordinate of vertex
     * @return [GLVertex] we have either found in `ArrayList<GLVertex>` field [mVertexList] that
     * already has the same coordinates, or one that we have created by calling the method
     * [GLWorld.addVertex] method of [mWorld] and added to `ArrayList<GLVertex>` [mVertexList].
     */
    fun addVertex(x: Float, y: Float, z: Float): GLVertex { // look for an existing GLVertex first
        val iter: Iterator<GLVertex> = mVertexList.iterator()
        while (iter.hasNext()) {
            val vertex = iter.next()
            if (vertex.x == x && vertex.y == y && vertex.z == z) {
                return vertex
            }
        }
        // doesn't exist, so create new vertex
        val vertex = mWorld.addVertex(x, y, z)
        mVertexList.add(vertex)
        return vertex
    }

    /**
     * Applies [M4] parameter [transform] to all the vertices used by our [GLShape] instance.
     * First we save our [M4] parameter [transform] in our [M4] field [mAnimateTransform] then
     * if our [M4] field [mTransform] is not null (see note) we multiply it by our [M4] parameter
     * [transform] to get a new [transform]. We set `Iterator<GLVertex>` variable `val iter`
     * to an iterator over the elements in the `ArrayList<GLVertex>` field [mVertexList], then
     * while there is a next element in the list we fetch the next vertex to `GLVertex vertex`
     * and use the [GLWorld.transformVertex] method of [mWorld] to move `vertex` to the
     * position specified by [transform].
     *
     * Note: [mTransform] is set to non-null only by our method [endAnimation] which is
     * called from [Layer.endAnimation] which is called from [Kube.animate] when the
     * angle that the current layer is being rotated to has reached its end point.
     *
     * @param transform transform that will move our [GLShape] to its next position.
     */
    fun animateTransform(transform: M4?) {
        var transformLocal = transform
        mAnimateTransform = transformLocal
        if (mTransform != null) transformLocal = mTransform!!.multiply(transformLocal!!)
        val iter: Iterator<GLVertex> = mVertexList.iterator()
        while (iter.hasNext()) {
            val vertex = iter.next()
            mWorld.transformVertex(vertex, transformLocal)
        }
    }

    /**
     * Called from [Layer.startAnimation], which is called from [Kube.animate], which is
     * called from [KubeRenderer.onDrawFrame]. It is a no-op in our demo.
     */
    fun startAnimation() {}

    /**
     * Called from [Layer.endAnimation] for every [GLShape] in the current layer, which
     * is called from [Kube.animate] when the rotation of the layer has reached its endpoint,
     * which is called from [KubeRenderer.onDrawFrame]. If our current [M4] field [mTransform]
     * is null we set it to a copy of [M4] field [mAnimateTransform], otherwise we set it to itself
     * multiplied by [M4] field [mAnimateTransform].
     */
    fun endAnimation() {
        mTransform = if (mTransform == null) {
            M4(mAnimateTransform!!)
        } else {
            mTransform!!.multiply(mAnimateTransform!!)
        }
    }

}
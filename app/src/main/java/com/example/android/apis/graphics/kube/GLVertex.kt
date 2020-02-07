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

import java.nio.IntBuffer

/**
 * Class representing data for a vertex, the (x,y,z) coordinates, the index number this instance
 * occupies in the vertex table, and the color of the vertex.
 */
class GLVertex {
    /**
     * x coordinate of the vertex
     */
    @JvmField
    var x: Float
    /**
     * y coordinate of the vertex
     */
    @JvmField
    var y: Float
    /**
     * z coordinate of the vertex
     */
    @JvmField
    var z: Float
    /**
     * Index number of this vertex in the field `ArrayList<GLVertex> mVertexList` of our instance
     * of [GLWorld].
     */
    val index // index in vertex table
            : Short
    /**
     * Color of this vertex.
     */
    var color: GLColor? = null

    /**
     * Basic constructor, only used in our [update] method to create a temporary [GLVertex]
     * which we use as the destination vertex of the transform of our instance of [GLVertex]
     * by the transformation matrix argument `M4 transform`
     */
    internal constructor() {
        x = 0f
        y = 0f
        z = 0f
        index = -1
    }

    /**
     * Constructor for a new instance of [GLVertex], called from our [GLWorld] method
     * `addVertex`, which is called from [GLShape.addVertex], which is called from the
     * constructor of a [Cube] object for each of the six vertices making up a cube in the
     * Rubic cube.
     *
     * @param x     x coordinate
     * @param y     y coordinate
     * @param z     z coordinate
     * @param index Index number of this vertex in the field `ArrayList<GLVertex> mVertexList`
     * of our instance of [GLWorld].
     */
    internal constructor(x: Float, y: Float, z: Float, index: Int) {
        this.x = x
        this.y = y
        this.z = z
        this.index = index.toShort()
    }

    /**
     * Compares this instance with the specified object and indicates if they are equal. First we
     * make sure that our [Object] parameter [other] is an instance of [GLVertex] and if so, we
     * return return *true* if the `x`, `y`, and `z` fields of both are equal, *false* otherwise.
     *
     * @param other the object to compare this instance with.
     * @return true if the specified object is equal to this object; false otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (other is GLVertex) {
            return x == other.x && y == other.y && z == other.z
        }
        return false
    }

    /**
     * Adds the coordinates of this instance of [GLVertex] to the [IntBuffer] parameter [vertexBuffer],
     * and the color components to the [IntBuffer] parameter [colorBuffer]. [vertexBuffer] and
     * [colorBuffer] are the direct allocated byte buffers used by the openGL method
     * `glDrawElements` to render our Rubic cube. Called from [GLWorld.generate] for
     * every [GLVertex] in its vertex list `ArrayList<GLVertex> mVertexList`.
     *
     * We assume that the [IntBuffer] parameters [vertexBuffer] and [colorBuffer] are positioned
     * properly, then we convert our instances coordinates x, y, and z to [Int] and write them
     * in order to [IntBuffer] parameter[vertexBuffer]. If our [GLColor] field [color] is null we
     * write four 0's to [IntBuffer] parameter [colorBuffer], otherwise we write the fields
     * `red`, `green`, `blue`, and `alpha` of [color] to [IntBuffer] parameter [colorBuffer].
     *
     * @param vertexBuffer [IntBuffer] used by [GLWorld] as a vertex buffer.
     * @param colorBuffer  [IntBuffer] used by [GLWorld] as a color buffer.
     */
    fun put(vertexBuffer: IntBuffer, colorBuffer: IntBuffer) {
        vertexBuffer.put(toFixed(x))
        vertexBuffer.put(toFixed(y))
        vertexBuffer.put(toFixed(z))
        if (color == null) {
            colorBuffer.put(0)
            colorBuffer.put(0)
            colorBuffer.put(0)
            colorBuffer.put(0)
        } else {
            colorBuffer.put(color!!.red)
            colorBuffer.put(color!!.green)
            colorBuffer.put(color!!.blue)
            colorBuffer.put(color!!.alpha)
        }
    }

    /**
     * Applies the [M4] transform matrix in parameter [transform] to our coordinates x, y, and z and
     * stores them in their proper place in [IntBuffer] parameter [vertexBuffer]. Called from the
     * [GLWorld.transformVertex] method, which is called from [GLShape.animateTransform], which is
     * called from [Layer.setAngle], which is called from [Kube.animate], whichis called from
     * [KubeRenderer.onDrawFrame].
     *
     * First we position [IntBuffer] parameter [vertexBuffer] to our index in it. Then if our [M4]
     * transform matrix [transform] is null we simply write our unmodified x, y, and z coordinates
     * into [IntBuffer] parameter [vertexBuffer]. If [transform] is not null we create a new empty
     * [GLVertex] for `val temp`, apply the transform to our current coordinates saving the result
     * in `temp`, then we write the x, y, and z coordinates of `temp` into [IntBuffer] parameter
     * [vertexBuffer].
     *
     * @param vertexBuffer [IntBuffer] field `mVertexBuffer` field from our [GLWorld]
     * @param transform    transformation matrix to apply to our coordinates
     */
    fun update(vertexBuffer: IntBuffer, transform: M4?) {
        /**
         * skip to location of vertex in mVertex buffer
         */
        vertexBuffer.position(index * 3)
        if (transform == null) {
            vertexBuffer.put(toFixed(x))
            vertexBuffer.put(toFixed(y))
            vertexBuffer.put(toFixed(z))
        } else {
            val temp = GLVertex()
            transform.multiply(this, temp)
            vertexBuffer.put(toFixed(temp.x))
            vertexBuffer.put(toFixed(temp.y))
            vertexBuffer.put(toFixed(temp.z))
        }
    }

    /**
     * Generates a hash code from our contents that can be used as a key into a hashmap.
     */
    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + z.hashCode()
        result = 31 * result + index
        result = 31 * result + (color?.hashCode() ?: 0)
        return result
    }

    companion object {
        /**
         * Convenience function to convert our float fields (x,y,z) to an `Int` for storing in
         * an `IntBuffer vertexBuffer`. Used in our methods `put` and `update`.
         *
         * @param floatValue float value to be turned into an `int`
         * @return its argument converted to an `int`
         */
        fun toFixed(floatValue: Float): Int {
            return (floatValue * 65536.0f).toInt()
        }
    }
}
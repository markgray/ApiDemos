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

package com.example.android.apis.graphics.kube;

import java.nio.IntBuffer;

/**
 * Class representing data for a vertex, the (x,y,z) coordinates, the index number this instance
 * occupies in the vertex table, and the color of the vertex.
 */
@SuppressWarnings("WeakerAccess")
public class GLVertex {

    /**
     * x coordinate of the vertex
     */
    public float x;
    /**
     * y coordinate of the vertex
     */
    public float y;
    /**
     * z coordinate of the vertex
     */
    public float z;
    /**
     * Index number of this vertex in the field {@code ArrayList<GLVertex> mVertexList} of our instance
     * of {@code GLWorld}.
     */
    final short index; // index in vertex table
    /**
     * Color of this vertex.
     */
    GLColor color;

    /**
     * Basic constructor, only used in our {@code update} method to create a temporary {@code GLVertex}
     * which we use as the destination vertex of the transform of our instance of {@code GLVertex}
     * by the transformation matrix argument {@code M4 transform}
     */
    GLVertex() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.index = -1;
    }

    /**
     * Constructor for a new instance of {@code GLVertex}, called from our {@code GLWorld} method
     * {@code addVertex}, which is called from {@code GLShape.addVertex}, which is called from the
     * constructor of a {@code Cube} object for each of the six vertices making up a cube in the
     * Rubic cube.
     *
     * @param x     x coordinate
     * @param y     y coordinate
     * @param z     z coordinate
     * @param index Index number of this vertex in the field {@code ArrayList<GLVertex> mVertexList}
     *              of our instance of {@code GLWorld}.
     */
    GLVertex(float x, float y, float z, int index) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.index = (short) index;
    }

    /**
     * Compares this instance with the specified object and indicates if they are equal. First we
     * make sure that {@code Object other} is an instance of {@code GLVertex} and if so, we cast
     * {@code other} to {@code GLVertex v} and return return true if the {@code x}. {@code y}, and
     * {@code z} fields of both are equal, false otherwise.
     *
     * @param other the object to compare this instance with.
     * @return true if the specified object is equal to this object; false otherwise.
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof GLVertex) {
            GLVertex v = (GLVertex) other;
            return (x == v.x && y == v.y && z == v.z);
        }
        return false;
    }

    /**
     * Convenience function to convert our float fields (x,y,z) to an {@code int} for storing in
     * an {@code IntBuffer vertexBuffer}. Used in our methods {@code put} and {@code update}.
     *
     * @param floatValue float value to be turned into an {@code int}
     * @return its argument converted to an {@code int}
     */
    static public int toFixed(float floatValue) {
        return (int) (floatValue * 65536.0f);
    }

    /**
     * Adds the coordinates of this instance of {@code GLVertex} to the {@code IntBuffer vertexBuffer},
     * and the color components to {@code IntBuffer colorBuffer}. {@code vertexBuffer} and
     * {@code colorBuffer} are the direct allocated byte buffers used by the openGL method
     * {@code glDrawElements} to render our Rubic cube. Called from {@code GLWorld.generate} for
     * every {@code GLVertex} in its vertex list {@code ArrayList<GLVertex> mVertexList}.
     * <p>
     * We assume that the {@code IntBuffer vertexBuffer} and {@code IntBuffer colorBuffer} are positioned
     * properly, then we convert our instances coordinates x, y, and z to {@code int} and write them
     * in order to {@code IntBuffer vertexBuffer}. If our field {@code GLColor color} is null we write
     * four 0's to {@code IntBuffer colorBuffer}, otherwise we write the {@code GLColor color} fields
     * {@code red}, {@code green}, {@code blue}, and {@code alpha} to {@code IntBuffer colorBuffer}.
     *
     * @param vertexBuffer {@code IntBuffer} used by {@code GLWorld} as a vertex buffer.
     * @param colorBuffer  {@code IntBuffer} used by {@code GLWorld} as a color buffer.
     */
    public void put(IntBuffer vertexBuffer, IntBuffer colorBuffer) {
        vertexBuffer.put(toFixed(x));
        vertexBuffer.put(toFixed(y));
        vertexBuffer.put(toFixed(z));
        if (color == null) {
            colorBuffer.put(0);
            colorBuffer.put(0);
            colorBuffer.put(0);
            colorBuffer.put(0);
        } else {
            colorBuffer.put(color.red);
            colorBuffer.put(color.green);
            colorBuffer.put(color.blue);
            colorBuffer.put(color.alpha);
        }
    }

    /**
     * Applies the transform matrix {@code M4 transform} to our coordinates x, y, and z and stores
     * them in their proper place in {@code IntBuffer vertexBuffer}. Called from our {@code GLWorld}
     * object method {@code transformVertex}, which is called from {@code GLShape.animateTransform},
     * which is called from {@code Layer.setAngle}, which is called from {@code Kube.animate}, which
     * is called from {@code KubeRenderer.onDrawFrame}.
     * <p>
     * First we position {@code IntBuffer vertexBuffer} to our index in it. Then if {@code M4 transform}
     * is null we simply write our unmodified x, y, and z coordinates into {@code IntBuffer vertexBuffer}.
     * If {@code M4 transform} is not null we create a new empty {@code GLVertex temp}, apply the
     * transform to our current coordinates saving the result in {@code GLVertex temp}, then we write
     * the x, y, and z coordinates of {@code GLVertex temp} into {@code IntBuffer vertexBuffer}.
     *
     * @param vertexBuffer {@code IntBuffer mVertexBuffer} field from our {@code GLWorld}
     * @param transform    transformation matrix to apply to our coordinates
     */
    public void update(IntBuffer vertexBuffer, M4 transform) {
        // skip to location of vertex in mVertex buffer
        vertexBuffer.position(index * 3);

        if (transform == null) {
            vertexBuffer.put(toFixed(x));
            vertexBuffer.put(toFixed(y));
            vertexBuffer.put(toFixed(z));
        } else {
            GLVertex temp = new GLVertex();
            transform.multiply(this, temp);
            vertexBuffer.put(toFixed(temp.x));
            vertexBuffer.put(toFixed(temp.y));
            vertexBuffer.put(toFixed(temp.z));
        }
    }
}

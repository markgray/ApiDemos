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

import android.util.Log;

import java.nio.ShortBuffer;
import java.util.ArrayList;

/**
 * Class representing a flat surface of a {@code GLShape}, a quadrilateral (square) in the case of
 * our {@code Cube} class which contains 6 {@code GLFace} objects.
 */
@SuppressWarnings("WeakerAccess")
public class GLFace {

    /**
     * List of vertices which define our {@code GLFace}
     */
    private ArrayList<GLVertex> mVertexList = new ArrayList<>();
    /**
     * Color of this face (and the last one of its vertices)
     */
    private GLColor mColor;

    /**
     * Unused so I won't comment on it.
     */
    @SuppressWarnings("unused")
    public GLFace() {
    }

    /**
     * Constructor for triangles. Unused
     *
     * @param v1 First {@code GLVertex} of our triangle
     * @param v2 Second {@code GLVertex} of our triangle
     * @param v3 Third {@code GLVertex} of our triangle
     */
    @SuppressWarnings("unused")
    public GLFace(GLVertex v1, GLVertex v2, GLVertex v3) {
        addVertex(v1);
        addVertex(v2);
        addVertex(v3);
    }

    //

    /**
     * Constructor for quadrilaterals, called from the constructor for a {@code Cube} object. We
     * simply call our method {@code addVertex} with each of our parameters and they are added in
     * order to the list of {@code GLVertex} objects in our field {@code ArrayList<GLVertex> mVertexList}.
     *
     * @param v1 First {@code GLVertex} of our quadrilateral
     * @param v2 Second {@code GLVertex} of our quadrilateral
     * @param v3 Third {@code GLVertex} of our quadrilateral
     * @param v4 Fourth {@code GLVertex} of our quadrilateral
     */
    public GLFace(GLVertex v1, GLVertex v2, GLVertex v3, GLVertex v4) {
        addVertex(v1);
        addVertex(v2);
        addVertex(v3);
        addVertex(v4);
    }

    /**
     * Convenience function that adds its {@code GLVertex v} to the list of our vertices contained in
     * our field {@code ArrayList<GLVertex> mVertexList}. Called only from our constructors.
     *
     * @param v {@code GLVertex} to add to our list of vertices {@code ArrayList<GLVertex> mVertexList}.
     */
    public void addVertex(GLVertex v) {
        mVertexList.add(v);
    }

    /**
     * Sets the color used to draw this instance of {@code GLFace} to the parameter {@code GLColor c}.
     * Must be called after all vertices are added, it is called by the method {@code GLShape.setFaceColor},
     * which is called only from the method {@code Kube.makeGLWorld}. First we set {@code int last} to
     * the index of the last {@code GLVertex} in {@code ArrayList<GLVertex> mVertexList}, and if it is
     * less than 2 we log it as an error and proceed, otherwise we fetch {@code GLVertex vertex} from
     * the last location in the list {@code mVertexList} and if our field {@code mColor} is null (has
     * never been set) we loop through our {@code ArrayList<GLVertex> mVertexList} as a ring list
     * inserting the current last {@code GLVertex} at the beginning, removing it from the end, and
     * setting {@code GLVertex vertex} to the new last {@code GLVertex}, looping until we find a
     * {@code GLVertex vertex} which does not have its color already set. While obviously this process
     * would never end if all of our {@code GLVertex} objects already had their color set, in our case
     * the first {@code GLVertex} always has a null {@code color} field. In any case we then set the
     * {@code color} field of {@code vertex} to {@code GLColor c}, and finally set our field
     * {@code GLColor mColor} to {@code c}.
     *
     * @param c {@code GLColor} to use for this instance of {@code GLFace}
     */
    public void setColor(GLColor c) {

        int last = mVertexList.size() - 1;
        if (last < 2) {
            Log.e("GLFace", "not enough vertices in setColor()");
        } else {
            GLVertex vertex = mVertexList.get(last);

            // only need to do this if the color has never been set
            if (mColor == null) {
                while (vertex.color != null) {
                    mVertexList.add(0, vertex);
                    mVertexList.remove(last + 1);
                    vertex = mVertexList.get(last);
                }
            }

            vertex.color = c;
        }

        mColor = c;
    }

    /**
     * Calculates and returns the number of indices required to draw the vertices in our list of vertices
     * {@code ArrayList<GLVertex> mVertexList}. Called from {@code GLShape.getIndexCount}, which is
     * called from {@code GLWorld.addShape}, which is called from {@code GLWorld.makeGLWorld}, which
     * is called from the {@code onCreate} override of {@code Kube} when it calls the constructor for
     * {@code KubeRenderer} to initialize its field {@code KubeRenderer mRenderer}. Since in our case
     * we always have 4 {@code GLVertex} objects in our {@code ArrayList<GLVertex> mVertexList}, we
     * always return the number 6.
     *
     * @return number of indices required for the {@code GLVertex} objects in our list of vertices
     * {@code ArrayList<GLVertex> mVertexList}.
     */
    public int getIndexCount() {
        return (mVertexList.size() - 2) * 3;
    }

    /**
     * 
     *
     * @param buffer {@code ShortBuffer mIndexBuffer} which is directly allocated on the native heap
     *               so it can be used as the index buffer for a {@code glDrawElements} call.
     */
    public void putIndices(ShortBuffer buffer) {
        int last = mVertexList.size() - 1;

        GLVertex v0 = mVertexList.get(0);
        GLVertex vn = mVertexList.get(last);

        // push triangles into the buffer
        for (int i = 1; i < last; i++) {
            GLVertex v1 = mVertexList.get(i);
            buffer.put(v0.index);
            buffer.put(v1.index);
            buffer.put(vn.index);
            v0 = v1;
        }
    }
}

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

import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Base class for {@code Cube}, which uses its methods {@code addVertex} and {@code addFace} to add
 * {@code GLVertex} and {@code GLFace} instances to add data describing a single instance of
 * {@code Cube} that is part of the 3 by 3 by 3 {@code Cube} rubic cube. Other classes then use the
 * other methods provided to modify the {@code Cube}.
 */
@SuppressWarnings("WeakerAccess")
public class GLShape {
    /**
     * Current transformation matrix, it is used to move the vertices of our shape to the position
     * required for the current location of our {@code GLShape} (a {@code Cube} in our demo) using
     * the method {@code mWorld.transformVertex}. This happens whenever our method {@code animateTransform}
     * is called from {@code Layer.setAngle} which is called from the method {@code Kube.animate},
     * which gets called from the {@code onDrawFrame} method of {@code KubeRenderer}.
     */
    public M4 mTransform;
    /**
     * Transform to multiply {@code M4 mTransform} by in order to move this instance of {@code GLShape}
     * to the next step in its animation. It is set by a call to our method {@code animateTransform},
     * which is called from {@code Layer.setAngle}, which is called from {@code Kube.animate}, which
     * gets called from the {@code onDrawFrame} method of {@code KubeRenderer}.
     */
    public M4 mAnimateTransform;

    /**
     * List of {@code GLFace} faces making up our {@code GLShape}, it is added to by our method
     * {@code addFace}, which is called from the constructor for a {@code Cube} object (our one and
     * only subclass).
     */
    protected ArrayList<GLFace> mFaceList = new ArrayList<>();
    /**
     * List of {@code GLVertex} vertices making up our {@code GLShape}, it is added to by our method
     * {@code addVertex}, which is called from the constructor for a {@code Cube} object (our one and
     * only subclass).
     */
    protected ArrayList<GLVertex> mVertexList = new ArrayList<>();
    /**
     * We do not use, so who cares?
     */
    @SuppressWarnings("unused")
    protected ArrayList<Integer> mIndexList = new ArrayList<>();    // make more efficient?
    /**
     * {@code GLWorld} we belong to, it is set in our constructor, which is called from the constructor
     * of {@code Cube}, which is called from the {@code Kube.makeGLWorld} method (once for each
     * {@code Cube} in our rubic cube), which is called in the {@code onCreate} override of {@code Kube}
     * which uses the {@code GLWorld} object it creates when it creates its instance of
     * {@code KubeRenderer}.
     */
    protected GLWorld mWorld;

    public GLShape(GLWorld world) {
        mWorld = world;
    }

    public void addFace(GLFace face) {
        mFaceList.add(face);
    }

    public void setFaceColor(int face, GLColor color) {
        mFaceList.get(face).setColor(color);
    }

    public void putIndices(ShortBuffer buffer) {
        Iterator<GLFace> iter = mFaceList.iterator();
        //noinspection WhileLoopReplaceableByForEach
        while (iter.hasNext()) {
            GLFace face = iter.next();
            face.putIndices(buffer);
        }
    }

    public int getIndexCount() {
        int count = 0;
        Iterator<GLFace> iter = mFaceList.iterator();
        //noinspection WhileLoopReplaceableByForEach
        while (iter.hasNext()) {
            GLFace face = iter.next();
            count += face.getIndexCount();
        }
        return count;
    }

    public GLVertex addVertex(float x, float y, float z) {

        // look for an existing GLVertex first
        Iterator<GLVertex> iter = mVertexList.iterator();
        //noinspection WhileLoopReplaceableByForEach
        while (iter.hasNext()) {
            GLVertex vertex = iter.next();
            if (vertex.x == x && vertex.y == y && vertex.z == z) {
                return vertex;
            }
        }

        // doesn't exist, so create new vertex
        GLVertex vertex = mWorld.addVertex(x, y, z);
        mVertexList.add(vertex);
        return vertex;
    }

    public void animateTransform(M4 transform) {
        mAnimateTransform = transform;

        if (mTransform != null)
            transform = mTransform.multiply(transform);

        Iterator<GLVertex> iter = mVertexList.iterator();
        //noinspection WhileLoopReplaceableByForEach
        while (iter.hasNext()) {
            GLVertex vertex = iter.next();
            mWorld.transformVertex(vertex, transform);
        }
    }

    public void startAnimation() {
    }

    public void endAnimation() {
        if (mTransform == null) {
            mTransform = new M4(mAnimateTransform);
        } else {
            mTransform = mTransform.multiply(mAnimateTransform);
        }
    }
}

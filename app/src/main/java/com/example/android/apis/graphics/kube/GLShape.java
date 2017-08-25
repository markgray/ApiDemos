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
 * {@code GLVertex} and {@code GLFace} instances and to add data describing a single instance of
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

    /**
     * Our constructor, we simply save our parameter {@code GLWorld world} in our field
     * {@code GLWorld mWorld}. It is called in the constructor for a {@code Cube} instance, which
     * passes the {@code GLWorld} instance used when it is called. The constructor for a {@code Cube}
     * is called only from the {@code makeGLWorld} method of {@code Kube} which creates the single
     * {@code GLWorld} we use throughout, populates it with {@code Cube} objects, and calls the
     * {@code generate} method of that {@code GLWorld} to finish it off. The {@code makeGLWorld} method
     * is called when constructing the {@code KubeRenderer} instance in the {@code onCreate} method
     * of {@code Kube}.
     *
     * @param world {@code GLWorld} we belong to
     */
    public GLShape(GLWorld world) {
        mWorld = world;
    }

    /**
     * Adds a {@code GLFace} to our list {@code ArrayList<GLFace> mFaceList}. Called only from the
     * constructor for a {@code Cube} object.
     *
     * @param face {@code GLFace} to add
     */
    public void addFace(GLFace face) {
        mFaceList.add(face);
    }

    /**
     * Sets the color of the {@code GLFace} which is at position {@code int face} in our list
     * {@code ArrayList<GLFace> mFaceList}. Called only from the method {@code Kube.makeGLWorld}.
     * We retrieve a reference to the {@code GLFace} at position {@code face} in our list
     * {@code ArrayList<GLFace> mFaceList}, and call its method {@code setColor} which sets the
     * color of all the vertices used by the {@code GLFace} to {@code GLColor color}.
     * TODO: figure out how setColor actually works (seems only one vertex gets its color set)
     *
     * @param face  index into our list {@code ArrayList<GLFace> mFaceList}
     * @param color {@code GLColor} that we want the {@code GLFace} to have.
     */
    public void setFaceColor(int face, GLColor color) {
        mFaceList.get(face).setColor(color);
    }

    /**
     * Adds all the indices used by all the {@code GLFace} objects contained in our field
     * {@code ArrayList<GLFace> mFaceList} to the index buffer passed us. Called only from the
     * method {@code GLWorld.generate} which adds all the indices used by all the {@code GLShape}
     * objects in its field {@code ArrayList<GLShape> mShapeList} to its index buffer field
     * {@code ShortBuffer mIndexBuffer}. First we set {@code Iterator<GLFace> iter} to an iterator
     * over all the elements in the list {@code ArrayList<GLFace> mFaceList}, then as long as there
     * is a "next" element we fetch a reference to that element to {@code GLFace face} and call its
     * method {@code putIndices} to add all the indices of {@code face} to {@code buffer}.
     *
     * @param buffer openGL index buffer to add all our indices to.
     */
    public void putIndices(ShortBuffer buffer) {
        Iterator<GLFace> iter = mFaceList.iterator();
        //noinspection WhileLoopReplaceableByForEach
        while (iter.hasNext()) {
            GLFace face = iter.next();
            face.putIndices(buffer);
        }
    }

    /**
     * Adds up all the indices used by all the faces in our field {@code ArrayList<GLFace> mFaceList}.
     * Called only from the method {@code GLWorld.addShape}. We initialize {@code int count} to 0,
     * then we set {@code Iterator<GLFace> iter} to an iterator over all the elements in the list
     * {@code ArrayList<GLFace> mFaceList}, and as long as there is a "next" element we fetch a
     * reference to that element to {@code GLFace face} and add the value returned by its method
     * {@code getIndexCount} to {@code count}. When done we return {@code count} to the called.
     *
     * @return total number of indices in all our faces
     */
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

    /**
     * Finds a vertex with the coordinates (x,y.z) in {@code ArrayList<GLVertex> mVertexList} (if it
     * exists), or creates a {@code GLVertex} with those coordinates and adds it to our list. The
     * {@code GLVertex} is then returned to the caller. Called only from the constructor for a
     * {@code Cube} object. First we set {@code Iterator<GLVertex> iter} to an iterator over all the
     * elements in the list {@code ArrayList<GLVertex> mVertexList}, then as long as there is a "next"
     * element we fetch a reference to that element to {@code GLVertex vertex}, and if all three
     * coordinates of {@code vertex} match the (x,y,z) coordinates passed us we return {@code vertex}.
     * If we are unable to find a matching {@code GLVertex} in our list, we create {@code GLVertex vertex}
     * by calling the method {@code mWorld.addVertex}, add it to {@code ArrayList<GLVertex> mVertexList}
     * and return it to the caller.
     *
     * @param x x coordinate of vertex
     * @param y y coordinate of vertex
     * @param z z coordinate of vertex
     * @return {@code GLVertex} we have either found in {@code ArrayList<GLVertex> mVertexList} that
     * already has the same coordinates, or one that we have created by calling the method
     * {@code mWorld.addVertex} and added to {@code ArrayList<GLVertex> mVertexList}.
     */
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

    /**
     * Applies {@code M4 transform} to all the vertices used by our {@code GLShape} instance.
     * First we save our parameter {@code M4 transform} in our field {@code M4 mAnimateTransform}
     * then if our field {@code M4 mTransform} is not null (see note) we multiply it by our parameter
     * {@code M4 transform} to get a new {@code M4 transfer}. We set {@code Iterator<GLVertex> iter}
     * to an iterator over the elements in the list {@code ArrayList<GLVertex> mVertexList}, then
     * while there is a next element in the list we fetch the next vertex to {@code GLVertex vertex}
     * and use the method {@code GLWorld mWorld.transformVertex} to move {@code vertex} to the
     * position specified by {@code transform}.
     * <p>
     * Note: {@code mTransform} is set to non-null only by our method {@code endAnimation} which is
     * called from {@code Layer.endAnimation} which is called from {@code Kube.animate} when the
     * angle that the current layer is being rotated has reached its end point.
     *
     * @param transform transform that will move our {@code GLShape} to its next position.
     */
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

    /**
     * Called from {@code Layer.startAnimation}, which is called from {@code Kube.animate}, which is
     * called from {@code KubeRenderer.onDrawFrame}. It is a no-op in our demo.
     */
    public void startAnimation() {
    }

    /**
     * Called from {@code Layer.endAnimation} for every {@code GLShape} in the current layer, which
     * is called from {@code Kube.animate} when the rotation of the layer has reached its endpoint,
     * which is called from {@code KubeRenderer.onDrawFrame}. If our current {@code M4 mTransform}
     * is null we set it to a copy of {@code M4 mAnimateTransform}, otherwise we set it to itself
     * multiplied by {@code M4 mAnimateTransform}.
     */
    public void endAnimation() {
        if (mTransform == null) {
            mTransform = new M4(mAnimateTransform);
        } else {
            mTransform = mTransform.multiply(mAnimateTransform);
        }
    }
}

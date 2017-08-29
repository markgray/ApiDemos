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

/**
 * Class representing a single cube of the 3x3x3 cube Rubic cube.
 */
@SuppressWarnings("WeakerAccess")
public class Cube extends GLShape {

    /**
     * index number of the bottom {@code GLFace} of our {@code GLShape} in our super's list of faces
     * {@code ArrayList<GLFace> mFaceList} (determined by the order we call {@code GLShape.addFace})
     */
    public static final int kBottom = 0;
    /**
     * index number of the front {@code GLFace} of our {@code GLShape} in our super's list of faces
     * {@code ArrayList<GLFace> mFaceList} (determined by the order we call {@code GLShape.addFace})
     */
    public static final int kFront = 1;
    /**
     * index number of the left {@code GLFace} of our {@code GLShape} in our super's list of faces
     * {@code ArrayList<GLFace> mFaceList} (determined by the order we call {@code GLShape.addFace})
     */
    public static final int kLeft = 2;
    /**
     * index number of the right {@code GLFace} of our {@code GLShape} in our super's list of faces
     * {@code ArrayList<GLFace> mFaceList} (determined by the order we call {@code GLShape.addFace})
     */
    public static final int kRight = 3;
    /**
     * index number of the back {@code GLFace} of our {@code GLShape} in our super's list of faces
     * {@code ArrayList<GLFace> mFaceList} (determined by the order we call {@code GLShape.addFace})
     */
    public static final int kBack = 4;
    /**
     * index number of the top {@code GLFace} of our {@code GLShape} in our super's list of faces
     * {@code ArrayList<GLFace> mFaceList} (determined by the order we call {@code GLShape.addFace})
     */
    public static final int kTop = 5;

    /**
     * Constructor for a {@code Cube} instance located and sized by the parameters. First we call our
     * super's constructor, then we call our super's method {@code addVertex} to add the following
     * {@code GLVertex} objects to the list {@code ArrayList<GLVertex> GLShape.mVertexList} (vertices
     * belonging to our {@code GLShape}) as well as to the list of all {@code GLVertex} objects
     * {@code ArrayList<GLVertex> GLWorld.mVertexList}:
     * <ul>
     * <li>
     * {@code leftBottomBack} - with the x,y,z coordinates (left, bottom, back)
     * </li>
     * <li>
     * {@code rightBottomBack} - with the x,y,z coordinates (right, bottom, back)
     * </li>
     * <li>
     * {@code leftTopBack} - with the x,y,z coordinates (left, top, back)
     * </li>
     * <li>
     * {@code rightTopBack} - with the x,y,z coordinates (right, top, back)
     * </li>
     * <li>
     * {@code leftBottomFront} - with the x,y,z coordinates (left, bottom, front)
     * </li>
     * <li>
     * {@code rightBottomFront} - with the x,y,z coordinates (right, bottom, front)
     * </li>
     * <li>
     * {@code leftTopFront} - with the x,y,z coordinates (left, top, front)
     * </li>
     * <li>
     * {@code rightTopFront} - with the x,y,z coordinates (right, top, front)
     * </li>
     * </ul>
     * Having created and added these {@code GLVertex} objects, we now proceed to call our super's
     * method {@code addFace} to add {@code GLFace} objects to the list of faces of our {@code Cube}
     * {@code ArrayList<GLFace> mFaceList}:
     * <ul>
     * <li>
     * bottom face ({@code kBottom}) using the {@code GLVertex} objects leftBottomBack, leftBottomFront,
     * rightBottomFront, rightBottomBack
     * </li>
     * <li>
     * front face ({@code kFront}) using the {@code GLVertex} objects leftBottomFront, leftTopFront,
     * rightTopFront, rightBottomFront
     * </li>
     * <li>
     * left face ({@code kLeft}) using the {@code GLVertex} objects leftBottomBack, leftTopBack,
     * leftTopFront, leftBottomFront
     * </li>
     * <li>
     * right face ({@code kRight}) using the {@code GLVertex} objects rightBottomBack, rightBottomFront,
     * rightTopFront, rightTopBack
     * </li>
     * <li>
     * back face ({@code kBack}) using the {@code GLVertex} objects leftBottomBack, rightBottomBack,
     * rightTopBack, leftTopBack
     * </li>
     * <li>
     * top face ({@code kTop}) using the {@code GLVertex} objects leftTopBack, rightTopBack,
     * rightTopFront, leftTopFront
     * </li>
     * </ul>
     *
     * @param world  the {@code GLWorld} instance of our rubic cube
     * @param left   x coordinate of the left side of the cube
     * @param bottom y coordinate of the bottom of the cube
     * @param back   z coordinate of the back of the cube
     * @param right  x coordinate of the right side of the cube
     * @param top    y coordinate of the top of the cube
     * @param front  z coordinate of the front of the cube
     */
    public Cube(GLWorld world, float left, float bottom, float back, float right, float top, float front) {
        super(world);
        GLVertex leftBottomBack = addVertex(left, bottom, back);
        GLVertex rightBottomBack = addVertex(right, bottom, back);
        GLVertex leftTopBack = addVertex(left, top, back);
        GLVertex rightTopBack = addVertex(right, top, back);
        GLVertex leftBottomFront = addVertex(left, bottom, front);
        GLVertex rightBottomFront = addVertex(right, bottom, front);
        GLVertex leftTopFront = addVertex(left, top, front);
        GLVertex rightTopFront = addVertex(right, top, front);

        // vertices are added in a clockwise orientation (when viewed from the outside)
        // bottom
        addFace(new GLFace(leftBottomBack, leftBottomFront, rightBottomFront, rightBottomBack));
        // front
        addFace(new GLFace(leftBottomFront, leftTopFront, rightTopFront, rightBottomFront));
        // left
        addFace(new GLFace(leftBottomBack, leftTopBack, leftTopFront, leftBottomFront));
        // right
        addFace(new GLFace(rightBottomBack, rightBottomFront, rightTopFront, rightTopBack));
        // back
        addFace(new GLFace(leftBottomBack, rightBottomBack, rightTopBack, leftTopBack));
        // top
        addFace(new GLFace(leftTopBack, rightTopBack, rightTopFront, leftTopFront));

    }
}

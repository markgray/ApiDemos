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

/**
 * Class representing a single cube of the 3x3x3 cube Rubic cube.
 */
class Cube(
        world: GLWorld?,
        left: Float,
        bottom: Float,
        back: Float,
        right: Float,
        top: Float,
        front: Float
) : GLShape(world!!) {
    companion object {
        /**
         * index number of the bottom `GLFace` of our `GLShape` in our super's list of faces
         * `ArrayList<GLFace> mFaceList` (determined by the order we call `GLShape.addFace`)
         */
        const val kBottom = 0
        /**
         * index number of the front `GLFace` of our `GLShape` in our super's list of faces
         * `ArrayList<GLFace> mFaceList` (determined by the order we call `GLShape.addFace`)
         */
        const val kFront = 1
        /**
         * index number of the left `GLFace` of our `GLShape` in our super's list of faces
         * `ArrayList<GLFace> mFaceList` (determined by the order we call `GLShape.addFace`)
         */
        const val kLeft = 2
        /**
         * index number of the right `GLFace` of our `GLShape` in our super's list of faces
         * `ArrayList<GLFace> mFaceList` (determined by the order we call `GLShape.addFace`)
         */
        const val kRight = 3
        /**
         * index number of the back `GLFace` of our `GLShape` in our super's list of faces
         * `ArrayList<GLFace> mFaceList` (determined by the order we call `GLShape.addFace`)
         */
        const val kBack = 4
        /**
         * index number of the top `GLFace` of our `GLShape` in our super's list of faces
         * `ArrayList<GLFace> mFaceList` (determined by the order we call `GLShape.addFace`)
         */
        const val kTop = 5
    }

    /**
     * Constructor for a `Cube` instance located and sized by the parameters. First we call our
     * super's constructor, then we call our super's method `addVertex` to add the following
     * `GLVertex` objects to the list `ArrayList<GLVertex> GLShape.mVertexList` (vertices
     * belonging to our `GLShape`) as well as to the list of all `GLVertex` objects
     * `ArrayList<GLVertex> GLWorld.mVertexList`:
     *
     *  * `leftBottomBack` - with the x,y,z coordinates (left, bottom, back)
     *
     *  * `rightBottomBack` - with the x,y,z coordinates (right, bottom, back)
     *
     *  * `leftTopBack` - with the x,y,z coordinates (left, top, back)
     *
     *  * `rightTopBack` - with the x,y,z coordinates (right, top, back)
     *
     *  * `leftBottomFront` - with the x,y,z coordinates (left, bottom, front)
     *
     *  * `rightBottomFront` - with the x,y,z coordinates (right, bottom, front)
     *
     *  * `leftTopFront` - with the x,y,z coordinates (left, top, front)
     *
     *  * `rightTopFront` - with the x,y,z coordinates (right, top, front)
     *
     * Having created and added these `GLVertex` objects, we now proceed to call our super's
     * method `addFace` to add `GLFace` objects to the list of faces of our `Cube`
     * `ArrayList<GLFace> mFaceList`:
     *
     *  * bottom face (`kBottom`) using the `GLVertex` objects leftBottomBack, leftBottomFront,
     *  rightBottomFront, rightBottomBack
     *
     *  * front face (`kFront`) using the `GLVertex` objects leftBottomFront, leftTopFront,
     *  rightTopFront, rightBottomFront
     *
     *  * left face (`kLeft`) using the `GLVertex` objects leftBottomBack, leftTopBack,
     *  leftTopFront, leftBottomFront
     *
     *  * right face (`kRight`) using the `GLVertex` objects rightBottomBack, rightBottomFront,
     *  rightTopFront, rightTopBack
     *
     *  * back face (`kBack`) using the `GLVertex` objects leftBottomBack, rightBottomBack,
     *  rightTopBack, leftTopBack
     *
     *  * top face (`kTop`) using the `GLVertex` objects leftTopBack, rightTopBack,
     *  rightTopFront, leftTopFront
     *
     * Parameter: world  the `GLWorld` instance of our rubic cube
     * Parameter: left   x coordinate of the left side of the cube
     * Parameter: bottom y coordinate of the bottom of the cube
     * Parameter: back   z coordinate of the back of the cube
     * Parameter: right  x coordinate of the right side of the cube
     * Parameter: top    y coordinate of the top of the cube
     * Parameter: front  z coordinate of the front of the cube
     */
    init {
        val leftBottomBack = addVertex(left, bottom, back)
        val rightBottomBack = addVertex(right, bottom, back)
        val leftTopBack = addVertex(left, top, back)
        val rightTopBack = addVertex(right, top, back)
        val leftBottomFront = addVertex(left, bottom, front)
        val rightBottomFront = addVertex(right, bottom, front)
        val leftTopFront = addVertex(left, top, front)
        val rightTopFront = addVertex(right, top, front)
        /**
         * vertices are added in a clockwise orientation (when viewed from the outside)
          */
        // bottom
        addFace(GLFace(leftBottomBack, leftBottomFront, rightBottomFront, rightBottomBack))
        // front
        addFace(GLFace(leftBottomFront, leftTopFront, rightTopFront, rightBottomFront))
        // left
        addFace(GLFace(leftBottomBack, leftTopBack, leftTopFront, leftBottomFront))
        // right
        addFace(GLFace(rightBottomBack, rightBottomFront, rightTopFront, rightTopBack))
        // back
        addFace(GLFace(leftBottomBack, rightBottomBack, rightTopBack, leftTopBack))
        // top
        addFace(GLFace(leftTopBack, rightTopBack, rightTopFront, leftTopFront))
    }
}
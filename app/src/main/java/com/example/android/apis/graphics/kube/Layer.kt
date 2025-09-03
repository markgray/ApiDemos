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

import kotlin.math.cos
import kotlin.math.sin

/**
 * Class containing the 9 [Cube] ([GLShape]) objects which comprise one of the planes of
 * the Rubic cube. A plane is that group of objects which can be rotated around an axis, and there
 * are 9 of them - all contained in the array of [Layer] field `Kube.mLayers`.
 *
 * @property mAxis which axis do we rotate around? 0 for X, 1 for Y, 2 for Z
 */
@Suppress("MemberVisibilityCanBePrivate")
class Layer(var mAxis: Int) {

    /**
     * The [Cube] ([GLShape]) objects presently in this [Layer]. All 9 of the [Layer]
     * planes are initialized in the method [Kube.updateLayers] using a (0,1, ... 26) initial
     * `mPermutation` of the [Cube] objects in its [Cube] array field `mCubes`, and then
     * `mPermutation` is randomly chosen from one of the permutations in [Kube.mLayerPermutations]
     * to rotate the 9 [Layer] objects (also using the method [Kube.updateLayers])
     */
    var mShapes: Array<GLShape?> = arrayOfNulls(9)

    /**
     * Transform matrix which will rotate our layer instance around its x, y, or z axis, depending
     * on where in the Rubic cube we are located. It is used in our method [setAngle] to move
     * all the [GLShape] objects in [mShapes] by calling [GLShape.animateTransform] which applies
     * the transform matrix to all the vertices the [GLShape] is made from. [setAngle]
     * calculates the contents of [mTransform] using the [Float] parameter `angle`  it is
     * passed, which is the angle in radians to rotate this layer instance around its appropriate
     * [mAxis] axis.
     */
    var mTransform: M4 = M4()

    /**
     * Called from [Kube.animate], which is called from [KubeRenderer.onDrawFrame]. For
     * each of the [GLShape] `val shape` objects in our [GLShape] array [mShapes], we call its
     * method `startAnimation` ... which is a no-op in our case.
     */
    fun startAnimation() {
        for (i in mShapes.indices) {
            val shape = mShapes[i]
            shape?.startAnimation()
        }
    }

    /**
     * Called from [Kube.animate] when the [Layer] being rotated has reached its ending
     * angle. [Kube.animate] is called from [KubeRenderer.onDrawFrame]. For each of the
     * `val shape` [GLShape] objects in our [GLShape] array [mShapes], we call its method
     * `endAnimation` which updates its [M4] field `mTransform` to reflect the movement
     * which has been applied to the [GLShape] via its [M4] field `mAnimateTransform`
     * (which we have been setting in our [setAngle] method as the [Layer] rotates).
     * `mTransform` thus represents the cumulative transforms which have been applied to the
     * [GLShape] instance as the various layers it belongs to are rotated, resulting in its
     * current x,y,z location.
     */
    fun endAnimation() {
        for (i in mShapes.indices) {
            val shape = mShapes[i]
            shape?.endAnimation()
        }
    }

    /**
     * This is used by [Kube.animate] to set the angle of our layer. We do this by using our
     * [Float] parameter [angle]  to set our [M4] field [mTransform] to be a transform matrix
     * designed to move a [GLVertex] of the [GLShape] objects comprising our [Layer] to the
     * position it should be when our layer is rotated to [angle] radians around its [mAxis].
     *
     * First we make a local copy of our [Float] parameter [angle] in `var angleVar` and then we
     * normalize `angleVar` to be between 0.0 and 2.0 pi radians, then we set [Float]
     * `val sin` to be the sine of `angleVar`, and [Float] `val cos` to be the cosine of
     * `angleVar`. We fetch a reference to the `mTransform.m` field of our field [mTransform] to
     * the [Float] array variable `val m` to make the following code easier to read. Then we switch
     * on the value of our [mAxis] field (our rotation axis):
     *
     *  * `kAxisX` - rotation around the x axis
     *
     *  * `kAxisY` - rotation around the y axis
     *
     *  * `kAxisZ` - rotation around the z axis
     *
     * and set the values of the entries in `m` to the appropriate values for the axis in
     * question.
     *
     * Having calculated the new contents of `mTransform.m`, we call the [GLShape.mTransform]
     * method of each of the `val shape` [GLShape] objects in our [GLShape] array [mShapes] and
     * it applies the [mTransform] transform matrix to each of the [GLVertex] vertices used to
     * describe the [GLShape] (causing the [GLShape] to move the next time it is drawn).
     *
     * @param angle angle in radians to rotate our [Layer]
     */
    fun setAngle(angle: Float) { // normalize the angle
        var angleVar = angle
        val twopi = Math.PI.toFloat() * 2f
        while (angleVar >= twopi) angleVar -= twopi
        while (angleVar < 0f) angleVar += twopi
        //		mAngle = angle;
        val sin = sin(angleVar.toDouble()).toFloat()
        val cos = cos(angleVar.toDouble()).toFloat()
        val m = mTransform.m
        when (mAxis) {
            kAxisX -> {
                m[1][1] = cos
                m[1][2] = sin
                m[2][1] = -sin
                m[2][2] = cos
                m[0][0] = 1f
                run {
                    m[2][0] = 0f
                    m[1][0] = m[2][0]
                    m[0][2] = m[1][0]
                    m[0][1] = m[0][2]
                }
            }

            kAxisY -> {
                m[0][0] = cos
                m[0][2] = sin
                m[2][0] = -sin
                m[2][2] = cos
                m[1][1] = 1f
                run {
                    m[2][1] = 0f
                    m[1][2] = m[2][1]
                    m[1][0] = m[1][2]
                    m[0][1] = m[1][0]
                }
            }

            kAxisZ -> {
                m[0][0] = cos
                m[0][1] = sin
                m[1][0] = -sin
                m[1][1] = cos
                m[2][2] = 1f
                run {
                    m[1][2] = 0f
                    m[0][2] = m[1][2]
                    m[2][1] = m[0][2]
                    m[2][0] = m[2][1]
                }
            }
        }
        for (i in mShapes.indices) {
            val shape = mShapes[i]
            shape?.animateTransform(mTransform)
        }
    }

    @Suppress("ConstPropertyName")
    companion object {
        /**
         * Convenience constant for rotation around the x axis.
         */
        const val kAxisX: Int = 0

        /**
         * Convenience constant for rotation around the y axis.
         */
        const val kAxisY: Int = 1

        /**
         * Convenience constant for rotation around the z axis.
         */
        const val kAxisZ: Int = 2
    }

    /**
     * Constructor for a `Layer` instance, it initializes its `M4` field `mTransform` with an
     * identity matrix.
     */
    init {
        /**
         * start with identity matrix for transformation
         */
        mTransform.setIdentity()
    }
}
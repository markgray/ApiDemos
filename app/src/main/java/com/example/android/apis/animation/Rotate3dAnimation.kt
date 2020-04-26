/*
 * Copyright (C) 2007 The Android Open Source Project
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
package com.example.android.apis.animation

import android.graphics.Camera
import android.view.animation.Animation
import android.view.animation.Transformation

/**
 * An animation that rotates the view on the Y axis between two specified angles.
 * This animation also adds a translation on the Z axis (depth) to improve the effect.
 */
class Rotate3dAnimation
/**
 * Creates a new 3D rotation on the Y axis. The rotation is defined by its
 * start angle and its end angle. Both angles are in degrees. The rotation
 * is performed around a center point on the 2D space, definied by a pair
 * of X and Y coordinates, called [mCenterX] and [mCenterY]. When the animation
 * starts, a translation on the Z axis (depth) is performed. The length
 * of the translation can be specified, as well as whether the translation
 * should be reversed in time.
 *
 * @param mFromDegrees the start angle of the 3D rotation
 * @param mToDegrees the end angle of the 3D rotation
 * @param mCenterX the X center of the 3D rotation
 * @param mCenterY the Y center of the 3D rotation
 * @param mReverse true if the translation should be reversed, false otherwise
 */
(
        private val mFromDegrees: Float,
        private val mToDegrees: Float,
        private val mCenterX: Float,
        private val mCenterY: Float,
        private val mDepthZ: Float,
        private val mReverse: Boolean
) : Animation()

{
    /**
     * [Camera] instance that our [applyTransformation] override uses to compute 3D transformations
     * of the matrix of the [Transformation] it is passed
     */
    private var mCamera: Camera? = null

    /**
     * Initialize this animation with the dimensions of the object being
     * animated as well as the objects parents. (This is to support animation
     * sizes being specified relative to these dimensions.)
     *
     * Objects that interpret Animations should call this method when
     * the sizes of the object being animated and its parent are known, and
     * before calling `getTransformation`.
     *
     * We just call our super's implementation of `initialize` then initialize our [Camera] field
     * [mCamera] with a new instance.
     *
     * @param width Width of the object being animated
     * @param height Height of the object being animated
     * @param parentWidth Width of the animated object's parent
     * @param parentHeight Height of the animated object's parent
     */
    override fun initialize(width: Int, height: Int, parentWidth: Int, parentHeight: Int) {
        super.initialize(width, height, parentWidth, parentHeight)
        mCamera = Camera()
    }

    /**
     * Helper for getTransformation. Subclasses should implement this to apply
     * their transforms given an interpolation value. Implementations of this
     * method should always replace the specified Transformation or document
     * they are doing otherwise.
     *
     * We initialize our [Float] variable `val fromDegrees` to our field [mFromDegrees], then
     * calculate our [Float] variable `val degrees` to be the number of degrees we are to
     * animate to given the value of our [Float] parameter [interpolatedTime]. We initialize
     * our variable `val centerX` to our field [mCenterX], our variable `val centerY` to our
     * field [mCenterY], and our [Camera] variable `val camera` to our field [mCamera]. We
     * initialize our `Matrix` variable `val matrix` to the current `Matix` of our
     * [Transformation] parameter [t]. We save the camera state of `camera`, then branch on
     * the value of our [Boolean] field [mReverse]:
     *
     *  *true* - We translate `camera` in the Z direction by our [mDepthZ] field multiplied by
     *  our parameter [interpolatedTime]
     *
     *  *false* - We translate `camera` in the Z direction by our [mDepthZ] field multiplied by
     *  1.0f minus our parameter [interpolatedTime].
     *
     * We then rotate `camera` by `degrees` degrees around the Y axis, fetch the `Matrix` of
     * `camera` into `matrix` (which is the `Matrix` of our [Transformation] parameter [t]
     * recall), and restore the state of `camera` to that is held before we used it for our
     * calculations. Finally we pre-concatenate a translation of `matrix` to the location
     * minus `centerX`, minus `centerY`, and post-concatenate a translation of `matrix` to
     * the location `centerX`, `centerY`.
     *
     * @param interpolatedTime The value of the normalized time (0.0 to 1.0)
     *        after it has been run through the interpolation function.
     * @param t The Transformation object to fill in with the current
     *        transforms.
     */
    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        val fromDegrees = mFromDegrees
        val degrees = fromDegrees + (mToDegrees - fromDegrees) * interpolatedTime
        val centerX = mCenterX
        val centerY = mCenterY
        val camera = mCamera
        val matrix = t.matrix
        camera!!.save()
        if (mReverse) {
            camera.translate(0.0f, 0.0f, mDepthZ * interpolatedTime)
        } else {
            camera.translate(0.0f, 0.0f, mDepthZ * (1.0f - interpolatedTime))
        }
        camera.rotateY(degrees)
        camera.getMatrix(matrix)
        camera.restore()
        matrix.preTranslate(-centerX, -centerY)
        matrix.postTranslate(centerX, centerY)
    }

}
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
 * Class containing the 9 {@code Cube} ({@code GLShape}) objects which comprise one of the planes of
 * the Rubic cube. A plane is that group of objects which can be rotated around an axis, and there
 * are 9 of them - all contained in the field {@code Kube.mLayers}.
 */
@SuppressWarnings("WeakerAccess")
public class Layer {
    /**
     * The {@code Cube} ({@code GLShape}) objects presently in this {@code Layer}. All 9 of the
     * {@code Layer} planes are initialized in the method {@code Kube.updateLayers} using a
     * (0,1, ... 26) initial {@code mPermutation} of the {@code Cube} objects in the field
     * {@code Cube[] mCubes}, and then {@code mPermutation} is randomly chosen from one of the
     * permutations in {@code Kube.mLayerPermutations} to rotate the 9 {@code Layer} objects (also
     * using the method {@code Kube.updateLayers})
     */
    GLShape[] mShapes = new GLShape[9];
    /**
     * Transform matrix which will rotate our layer instance around its x, y, or z axis, depending
     * on where in the Rubic cube we are located. It is used in our method {@code setAngle} to move
     * all the {@code GLShape[] mShapes} by calling {@code GLShape.animateTransform} which applies
     * the transform matrix to all the vertices the {@code GLShape} is made from. {@code setAngle}
     * calculates the contents of {@code mTransform} using the {@code float angle} parameter it is
     * passed, which is the angle in radians to rotate this layer instance around its appropriate
     * {@code mAxis} axis.
     */
    M4 mTransform = new M4();
//	float mAngle;

    /**
     * which axis do we rotate around? 0 for X, 1 for Y, 2 for Z
     */
    int mAxis;
    /**
     * Convenience constant for rotation around the x axis.
     */
    static public final int kAxisX = 0;
    /**
     * Convenience constant for rotation around the y axis.
     */
    static public final int kAxisY = 1;
    /**
     * Convenience constant for rotation around the z axis.
     */
    static public final int kAxisZ = 2;

    /**
     * Constructor for a {@code Layer} instance, it saves the parameter {@code axis} (the x, y, or z
     * axis we are able to rotate about) in its field {@code mAxis} and initializes its field
     * {@code M4 mTransform} with an identity matrix.
     *
     * @param axis which axis do we rotate around? 0 for X, 1 for Y, 2 for Z
     */
    public Layer(int axis) {
        // start with identity matrix for transformation
        mAxis = axis;
        mTransform.setIdentity();
    }

    /**
     * Called from {@code Kube.animate}, which is called from {@code KubeRenderer.onDrawFrame}. For
     * each of the {@code GLShape shape} objects in our list {@code GLShape[] mShapes}, we call its
     * method {@code startAnimation} ... which is a no-op in our case.
     */
    public void startAnimation() {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < mShapes.length; i++) {
            GLShape shape = mShapes[i];
            if (shape != null) {
                shape.startAnimation();
            }
        }
    }

    /**
     * Called from {@code Kube.animate} when the {@code Layer} being rotated has reached its ending
     * angle. {@code Kube.animate} is called from {@code KubeRenderer.onDrawFrame}. For each of the
     * {@code GLShape shape} objects in our list {@code GLShape[] mShapes}, we call its method
     * {@code endAnimation} which updates its field {@code M4 mTransform} to reflect the movement
     * which has been applied to the {@code GLShape} via its field {@code M4 mAnimateTransform}
     * (which we have been setting in our {@code setAngle} method as the {@code Layer} rotates).
     * {@code mTransform} thus represents the cumulative transforms which have been applied to the
     * {@code GLShape} instance as the various layers it belongs to are rotated, resulting in its
     * current x,y,z location.
     */
    public void endAnimation() {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < mShapes.length; i++) {
            GLShape shape = mShapes[i];
            if (shape != null) {
                shape.endAnimation();
            }
        }
    }

    /**
     * This is used by {@code Kube.animate} to set the angle of our layer. We do this by using our
     * {@code float angle} parameter to set our field {@code M4 mTransform} to be a transform matrix
     * designed to move a {@code GLVertex} of the {@code GLShape} objects comprising our {@code Layer}
     * to the position it should be when our layer is rotated to {@code angle} radians around its
     * {@code int mAxis}.
     * <p>
     * First we normalize {@code angle} to be between 0.0 and 2.0 pi radians, then we set
     * {@code float sin} to be the sine of {@code angle}, and {@code float cos} to be the cosine of
     * {@code angle}. We fetch a reference to our fields field {@code mTransform.m} to the variable
     * {@code float[][] m} to make the following code easier to read. Then we switch on the value of
     * our field {@code int mAxis} (our rotation axis):
     * <ul>
     * <li>
     * {@code kAxisX} - rotation around the x axis
     * </li>
     * <li>
     * {@code kAxisY} - rotation around the y axis
     * </li>
     * <li>
     * {@code kAxisZ} - rotation around the z axis
     * </li>
     * </ul>
     * and set the values of the entries in {@code m} to the appropriate values for the axis in
     * question.
     * <p>
     * Having calculated the new contents of {@code mTransform.m}, we call the method
     * {@code animateTransform(mTransform} for each of the {@code GLShape shape} objects in our list
     * {@code GLShape[] mShapes} and it applies the transform matrix to each of the {@code GLVertex}
     * vertices used to describe the {@code GLShape} (causing the {@code GLShape} to move the next
     * time it is drawn).
     *
     * @param angle angle in radians to rotate our {@code Layer}
     */
    public void setAngle(float angle) {
        // normalize the angle
        float twopi = (float) Math.PI * 2f;
        while (angle >= twopi) angle -= twopi;
        while (angle < 0f) angle += twopi;
//		mAngle = angle;

        float sin = (float) Math.sin(angle);
        float cos = (float) Math.cos(angle);

        float[][] m = mTransform.m;
        switch (mAxis) {
            case kAxisX:
                m[1][1] = cos;
                m[1][2] = sin;
                m[2][1] = -sin;
                m[2][2] = cos;
                m[0][0] = 1f;
                m[0][1] = m[0][2] = m[1][0] = m[2][0] = 0f;
                break;
            case kAxisY:
                m[0][0] = cos;
                m[0][2] = sin;
                m[2][0] = -sin;
                m[2][2] = cos;
                m[1][1] = 1f;
                m[0][1] = m[1][0] = m[1][2] = m[2][1] = 0f;
                break;
            case kAxisZ:
                m[0][0] = cos;
                m[0][1] = sin;
                m[1][0] = -sin;
                m[1][1] = cos;
                m[2][2] = 1f;
                m[2][0] = m[2][1] = m[0][2] = m[1][2] = 0f;
                break;
        }

        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < mShapes.length; i++) {
            GLShape shape = mShapes[i];
            if (shape != null) {
                shape.animateTransform(mTransform);
            }
        }
    }
}

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
     * 
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

    public void endAnimation() {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < mShapes.length; i++) {
            GLShape shape = mShapes[i];
            if (shape != null) {
                shape.endAnimation();
            }
        }
    }

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

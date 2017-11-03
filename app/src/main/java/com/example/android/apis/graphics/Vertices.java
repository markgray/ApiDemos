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

package com.example.android.apis.graphics;

import com.example.android.apis.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;

/**
 * Shows how to use Canvas.drawVertices with BitmapShader to draw warp-able Bitmap's. The
 * Canvas.translate before drawing the bottom version of the two is not far enough away from
 * the top for high dpi so it overlaps it in the original version, so I scaled it by the logical
 * screen density.
 */
public class Vertices extends GraphicsActivity {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to a new instance of {@code SampleView}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SampleView(this));
    }

    private static class SampleView extends View {
        /**
         * {@code Paint} used to draw our bitmaps.
         */
        private final Paint mPaint = new Paint();
        /**
         * Array of vertices for the mesh ({@code verts} argument to {@code drawVertices})
         */
        private final float[] mVerts = new float[10];
        /**
         * coordinates to sample into the current shader ({@code texs} argument to {@code drawVertices})
         */
        private final float[] mTexs = new float[10];
        /**
         * {@code indices} argument to {@code drawVertices} used to specify the index of each triangle,
         * rather than just walking through the arrays ({@code verts} and {@code texs}) in order.
         */
        private final short[] mIndices = {0, 1, 2, 3, 4, 1};
        /**
         * Logical screen density, used to scale the position of the second {@code Bitmap}.
         */
        private float SCREEN_DENSITY;

        /**
         * {@code Matrix} used to scale (by 0.8) and translate (to (20,20)) the {@code Canvas} before
         * drawing.
         */
        private final Matrix mMatrix = new Matrix();
        /**
         * Inverse of {@code mMatrix}, used to transpose coordinates received in a touch event into
         * coordinates on the canvas before {@code mMatrix} is applied in order to move the index 0
         * point of {@code mVerts} into the right position (ie. the position it should occupy before
         * the canvas has {@code mMatrix} concatenated to it).
         */
        private final Matrix mInverse = new Matrix();

        /**
         * Stores its arguments {@code x} and {@code y} in the correct positions in {@code array[]}
         * for the point with index {@code index}.
         *
         * @param array vertex array to store {@code x} and {@code y} in
         * @param index index of the point to set to {@code (x,y)}.
         * @param x     the new X coordinate
         * @param y     the new Y coordinate
         */
        private static void setXY(float[] array, int index, float x, float y) {
            //noinspection PointlessArithmeticExpression
            array[index * 2 + 0] = x;
            array[index * 2 + 1] = y;
        }

        /**
         * Our constructor. First we call our super's constructor, we enable our view to receive focus,
         * and initialize {@code SCREEN_DENSITY} to the logical density of the screen. We decode the
         * resource file beach.jpg (R.drawable.beach) into {@code Bitmap bm}, then use {@code bm} to
         * create {@code Shader s} with a tiling mode of CLAMP for both x and y (replicate the edge
         * color if the shader draws outside of its original bounds), and then set the shader of
         * {@code Paint mPaint} to {@code s}.
         * <p>
         * Next we initialize {@code float w} to the width of {@code bm}, and {@code float h} to the
         * height of {@code bm}. We use these to calculate the (x,y) coordinates of the vertices we
         * store in both {@code mTexs} and {@code mVerts}:
         * <ul>
         * <li>
         * 0 - (w/2,h/2) the center of the {@code Bitmap} and {@code Canvas}
         * </li>
         * <li>
         * 1 - (0,0) the top left of the {@code Bitmap} and {@code Canvas}
         * </li>
         * <li>
         * 2 - (w,0) the top right of the {@code Bitmap} and {@code Canvas}
         * </li>
         * <li>
         * 3 - (w,h) the bottom right of the {@code Bitmap} and {@code Canvas}
         * </li>
         * <li>
         * 4 - (0,h) the bottom left of the {@code Bitmap} and {@code Canvas}
         * </li>
         * </ul>
         * We initialize {@code Matrix mMatrix} with a matrix that will scale by 0.8, and
         * pre-concatenate {@code mMatrix} with a translation to (20,20). Then we initialize
         * {@code Matrix mInverse} with the inverse of {@code mMatrix}.
         *
         * @param context {@code Context} to use to access resources, "this" in the {@code onCreate}
         *                method of {@code Vertices}
         */
        public SampleView(Context context) {
            super(context);
            setFocusable(true);
            SCREEN_DENSITY = getResources().getDisplayMetrics().density;

            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.beach);
            Shader s = new BitmapShader(bm, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            mPaint.setShader(s);

            float w = bm.getWidth();
            float h = bm.getHeight();
            // construct our mesh
            setXY(mTexs, 0, w / 2, h / 2);
            setXY(mTexs, 1, 0, 0);
            setXY(mTexs, 2, w, 0);
            setXY(mTexs, 3, w, h);
            setXY(mTexs, 4, 0, h);

            setXY(mVerts, 0, w / 2, h / 2);
            setXY(mVerts, 1, 0, 0);
            setXY(mVerts, 2, w, 0);
            setXY(mVerts, 3, w, h);
            setXY(mVerts, 4, 0, h);

            mMatrix.setScale(0.8f, 0.8f);
            mMatrix.preTranslate(20, 20);
            mMatrix.invert(mInverse);
        }

        /**
         * We implement this to do our drawing. First we set the entire {@code Canvas canvas} to a
         * light gray. Then we save the current matrix and clip of {@code canvas} onto a private stack
         * and pre-concatenate the current matrix with {@code Matrix mMatrix}.
         * <p>
         * We call the method {@code drawVertices} to draw a TRIANGLE_FAN defined by the vertices in
         * {@code mVerts[]} to {@code canvas} using the shader of {@code Paint mPaint} with the
         * coordinates to sample into the current shader specified by the vertices in {@code mTexs[]}
         * (initially the same coordinates stored in {@code mVerts[]} until a touch event moves index
         * 0 of {@code mVerts} to warp the drawing). Since the {@code indices} argument to
         * {@code drawVertices} is null, the drawing uses the vertices in order: (0,1,2) (0,2,3)
         * (0,3,4), leaving a pie slice not drawn.
         * <p>
         * We move the canvas down by 240*SCREEN_DENSITY, and call the method {@code drawVertices}
         * to draw a TRIANGLE_FAN defined by the vertices in {@code mVerts[]} to {@code canvas} using
         * the shader of {@code Paint mPaint} with the coordinates to sample into the current shader
         * specified by the vertices in {@code mTexs[]} (initially the same coordinates stored in
         * {@code mVerts[]} until a touch event moves index 0 of {@code mVerts} to warp the drawing).
         * This time the {@code indices} argument to {@code drawVertices} is {@code mIndices}. The
         * drawing now uses the vertices in order: (0,1,2) (0,2,3) (0,3,4) and (0,4,1) so the entire
         * {@code Bitmap} is drawn.
         * <p>
         * Finally we call the {@code restore} method of {@code canvas} to remove all modifications
         * to the matrix/clip state since save call at the beginning of our method.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(0xFFCCCCCC);
            canvas.save();
            canvas.concat(mMatrix);

            canvas.drawVertices(Canvas.VertexMode.TRIANGLE_FAN, 10, mVerts, 0,
                    mTexs, 0, null, 0, null, 0, 0, mPaint);

            canvas.translate(0, 240 * SCREEN_DENSITY);
            canvas.drawVertices(Canvas.VertexMode.TRIANGLE_FAN, 10, mVerts, 0,
                    mTexs, 0, null, 0, mIndices, 0, 6, mPaint);

            canvas.restore();
        }

        /**
         * We implement this method to handle touch screen motion events. First we fill {@code float[] pt}
         * with the x and y coordinates of the touch event. We then use {@code Matrix mInverse} to map
         * the point {@code pt} to the coordinates it would have before {@code Matrix mMatrix} has been
         * used to scale and translate the canvas of our view ({@code mInverse} is set to the inverse of
         * {@code mMatrix} in our constructor remember). We call our method {@code setXY} to set index
         * 0 of {@code mVerts} to the x and y coordinates of {@code pt}, call {@code invalidate} so the
         * view will be redrawn, and finally return true to our caller.
         *
         * @param event The motion event.
         * @return True if the event was handled, false otherwise. We always return true.
         */
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float[] pt = {event.getX(), event.getY()};
            mInverse.mapPoints(pt);
            setXY(mVerts, 0, pt[0], pt[1]);
            invalidate();
            return true;
        }

    }
}


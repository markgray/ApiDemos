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

import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;

/**
 * Uses android.graphics.Canvas method drawBitmapMesh to warp a bitmap near the  area it is touched.
 * Very subtle effect on Nexus 6 and Nexus 6P -> Marshmallow or just small high density screen?
 */
public class BitmapMesh extends GraphicsActivity {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to a new instance of {@code SampleView}
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SampleView(this));
    }

    /**
     * Custom {@code View} which contains a jpg which is warped by touch events received in our
     * {@code onTouchEvent} override.
     */
    private static class SampleView extends View {
        /**
         * Number of vertices in X dimension of Bitmap mesh.
         */
        private static final int WIDTH = 20;
        /**
         * Number of vertices in Y dimension of Bitmap mesh.
         */
        private static final int HEIGHT = 20;
        /**
         * Total number of vertices in Bitmap mesh
         */
        private static final int COUNT = (WIDTH + 1) * (HEIGHT + 1);

        /**
         * {@code Bitmap} that contains our jpg to be displayed and warped.
         */
        private final Bitmap mBitmap;
        /**
         * Warped Bitmap mesh array of (x,y) vertices
         */
        private final float[] mVerts = new float[COUNT * 2];
        /**
         * Original un-warped Bitmap mesh array of (x,y) vertices
         */
        private final float[] mOrig = new float[COUNT * 2];

        /**
         * {@code Matrix} used to translate the {@code Canvas canvas} to (10,10) before drawing the
         * {@code Bitmap}
         */
        private final Matrix mMatrix = new Matrix();
        /**
         * The inverse of {@code Matrix mMatrix} used to translate points received in touch events
         * to a (0,0) origin (ie. it converts points in the {@code Canvas canvas} coordinate space
         * to points in the {@code Matrix mMatrix} translated {@code Bitmap} coordinate space).
         */
        private final Matrix mInverse = new Matrix();

        /**
         * Convenience method to set the (x,y) values for a vertex, it simply calculates the array
         * indices for the x and y values of the vertex in question based on two floats per vertex
         * and stores the parameters {@code x} and {@code y} where they belong.
         *
         * @param array Vertices array
         * @param index address of vertex to set the (x,y) coordinates of
         * @param x     x coordinate
         * @param y     y coordinate
         */
        private static void setXY(float[] array, int index, float x, float y) {
            //noinspection PointlessArithmeticExpression
            array[index * 2 + 0] = x;
            array[index * 2 + 1] = y;
        }

        /**
         * Basic constructor that initializes the {@code View} and fields used by this instance of
         * {@code SampleView}. First we call through to our super's constructor, then we enable our
         * {@code View} to receive focus. Next we initialize our field {@code Bitmap mBitmap} by
         * decoding the jpg resource file R.drawable.beach.
         * <p>
         * We fetch the width of {@code mBitmap} to {@code float w} and the height to {@code float h}.
         * Then starting from {@code int index} of 0 we proceed to fill our two Bitmap mesh vertex
         * arrays {@code float[] mVerts} and {@code float[] mOrig} with (x,y) vertex coordinates by
         * looping through the HEIGHT y dimension values (each y value being y/HEIGHT of the height
         * of the {@code Bitmap mBitmap}), and for each of these values calculating the WIDTH x
         * values possible (each x value being x/WIDTH of the width of the {@code Bitmap mBitmap}).
         * <p>
         * Then we initialize our field {@code Matrix mMatrix} with a matrix to translate the canvas
         * to (10,10), and initialize {@code Matrix mInverse} to be the inverse of this.
         *
         * @param context {@code Context} of {@code View} to use to fetch resources, "this" called from
         *                {@code onCreate} override of the activity in our case
         */
        public SampleView(Context context) {
            super(context);
            setFocusable(true);

            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.beach);

            float w = mBitmap.getWidth();  // 1050 for our jpg
            float h = mBitmap.getHeight(); // 788 for our jpg
            // construct our mesh
            int index = 0;
            for (int y = 0; y <= HEIGHT; y++) {
                float fy = h * y / HEIGHT;
                for (int x = 0; x <= WIDTH; x++) {
                    float fx = w * x / WIDTH;
                    setXY(mVerts, index, fx, fy);
                    setXY(mOrig, index, fx, fy);
                    index += 1;
                }
            }

            mMatrix.setTranslate(10, 10);
            mMatrix.invert(mInverse);
        }

        /**
         * We implement this to do our drawing. First we set the color of the entire {@code Canvas canvas}
         * to 0xFFCCCCCC (a darkish gray). Then we pre-concatenate {@code Matrix mMatrix} to the current
         * {@code Matrix} of {@code canvas} and draw the bitmap {@code mBitmap} through the mesh
         * {@code float[] mVerts}.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(0xFFCCCCCC);

            canvas.concat(mMatrix);
            canvas.drawBitmapMesh(mBitmap, WIDTH, HEIGHT, mVerts, 0, null, 0, null);
        }

        /**
         * Creates a warped bitmap mesh version {@code float[] mVerts} of the un-warped bitmap mesh
         * {@code float[] mOrig} with the vertices closest to the point (cx,cy) being "pulled" hardest
         * towards that point.
         * <p>
         * First we declare a "gravitational constant" {@code float K}, and then for notational
         * cleanliness we copy the reference {@code float[] mOrig} to {@code float[] src}, and
         * {@code float[] mVerts} to {@code float[] dst}.
         * <p>
         * Then we loop through each of the vertex coordinates in {@code src}, fetching the x coordinate
         * to {@code float x} and the y coordinate to {@code float y}, determine the x distance
         * {@code float dx} of this point from {@code cx}, and the y distance {@code float dy} of
         * this point from {@code cy}, set {@code float dd} to the sum of the squares of {@code dx}
         * and {@code dy}, and set {@code float d} to the square root (distance) of {@code dd}.
         * <p>
         * The pull {@code float pull} on the point is then {@code K/(dd+0.000001f)} (with
         * the 0.000001f added to avoid possible divide by zero) with this divided again by
         * {@code (d + 0.000001f)} to allow multiplication by {@code dx} and {@code dy} later.
         * <p>
         * Then if the {@code pull} is greater than or equal to 1 we are very close to the point
         * {@code (cx,cy)} so we set the vertex of the {@code dst} bitmap mesh to {@code (cx,cy)},
         * otherwise we set the x coordinate to the {@code x} of the original plus {@code pull} times
         * the {@code dx} distance from {@code cx}, and the y coordinate to the {@code y} of the original
         * plus {@code pull} times the {@code dy} distance from {@code cy}.
         *
         * @param cx x coordinate of the point we are space warping around
         * @param cy y coordinate of the point we are space warping around
         */
        private void warp(float cx, float cy) {
            final float K = 10000;
            float[] src = mOrig;
            float[] dst = mVerts;
            for (int i = 0; i < COUNT * 2; i += 2) {
                //noinspection PointlessArithmeticExpression
                float x = src[i + 0];
                float y = src[i + 1];
                float dx = cx - x;
                float dy = cy - y;
                float dd = dx * dx + dy * dy;
                float d = (float) Math.sqrt(dd);
                float pull = K / (dd + 0.000001f);

                pull /= (d + 0.000001f);
                //   android.util.Log.d("BitmapMesh", "index " + i + " dist=" + d + " pull=" + pull);

                if (pull >= 1) {
                    //noinspection PointlessArithmeticExpression
                    dst[i + 0] = cx;
                    dst[i + 1] = cy;
                } else {
                    //noinspection PointlessArithmeticExpression
                    dst[i + 0] = x + dx * pull;
                    dst[i + 1] = y + dy * pull;
                }
            }
        }

        private int mLastWarpX = -9999; // don't match a valid touch coordinate to start with
        private int mLastWarpY;

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float[] pt = {event.getX(), event.getY()};
            mInverse.mapPoints(pt);

            int x = (int) pt[0];
            int y = (int) pt[1];
            if (mLastWarpX != x || mLastWarpY != y) {
                mLastWarpX = x;
                mLastWarpY = y;
                warp(pt[0], pt[1]);
                invalidate();
            }
            return true;
        }
    }
}


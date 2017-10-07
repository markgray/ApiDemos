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

package com.example.android.apis.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * Creates an array of points, draws RED lines between them using Canvas.drawLines() then draws the
 * points by themselves in BLUE using Canvas.drawPoints().
 */
public class DrawPoints extends GraphicsActivity {

    /**
     * Called when the activity is starting. First we call our super's implementation of
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
     * Demonstrates how to use the {@code Canvas.drawLines} and {@code Canvas.drawPoints} methods to
     * draw lines and points.
     */
    private static class SampleView extends View {
        /**
         * {@code Paint} that we use to draw with in our {@code onDraw} method.
         */
        private Paint mPaint = new Paint();
        /**
         * Our array of points, allocated and initialized by our {@code buildPoints} method.
         */
        private float[] mPts;

        /**
         * Size of our mesh, set in our constructor to the pixel equivalent of 300 dp.
         */
        private static float SIZE;
        /**
         * Number of line segments to divide our SIZE line into.
         */
        private static final int SEGS = 32;
        /**
         * Offset for the x coordinate of our point
         */
        private static final int X = 0;
        /**
         * Offset for the y coordinate of our point
         */
        private static final int Y = 1;

        /**
         * Allocates and initializes our field {@code float[] mPts} with an array of points. First we
         * determine the number of points {@code ptCount} we will need given the value of {@code SEGS}
         * (twice the value of {@code SEGS+1}, twice because we will be dividing a line of {@code SIZE}
         * length into {@code SEGS} segments for both x and y axes, the +1 is because of the endpoint).
         * Next we allocate {@code 2*ptCount} floats for {@code mPts} because each point will have an
         * x and a y coordinate.
         * <p>
         * We initialize {@code value} to 0.0 and calculate {@code delta} to be {@code SIZE/SEGS}.
         * Then we loop for our {@code SEGS} segments assigning the values of four {@code mPts} entries
         * so that the first contains the x coordinate of the point along the x axis (goes from {@code SIZE}
         * down to 0 in steps of {@code delta}), the second contains the y coordinate of the point along
         * the x axis (always 0), the third contains the x coordinate of the point along the y axis
         * (always 0), and the fourth contains the y coordinate of the point along the y axis (goes
         * from 0 to {@code SIZE} in steps of {@code delta}). We then add {@code delta} to {@code value}.
         */
        private void buildPoints() {
            final int ptCount = (SEGS + 1) * 2;
            mPts = new float[ptCount * 2];

            float value = 0;
            final float delta = SIZE / SEGS;
            for (int i = 0; i <= SEGS; i++) {
                mPts[i * 4 + X] = SIZE - value;
                mPts[i * 4 + Y] = 0;
                mPts[i * 4 + X + 2] = 0;
                mPts[i * 4 + Y + 2] = value;
                value += delta;
            }
        }

        /**
         * Our constructor. First we call our super's constructor. Then we retrieve the current display
         * metrics that are in effect to initialize {@code DisplayMetrics displayMetrics}, and use
         * it to calculate the value of 300dp in pixels to initialize our field {@code SIZE}. Finally
         * we call our method {@code buildPoints} to allocate and initialize the array of points in
         * our field {@code float[] mPts}.
         *
         * @param context {@code Context} to use to access resources.
         */
        public SampleView(Context context) {
            super(context);

            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            SIZE = Math.round(300 * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
            buildPoints();
        }

        /**
         * We implement this to do our drawing. First we make a copy of the pointer in our field
         * {@code Paint mPaint} for {@code Paint paint}. Then we translate our {@code Canvas canvas}
         * parameter to the point (10,10), and set it to all WHITE. We set the color of {@code paint}
         * to RED, its stroke width to 0, and use it to call the method {@code drawLines} of
         * {@code canvas} which draws a series of lines with each line taken from 4 consecutive values
         * in the {@code mPts} array.
         * <p>
         * Next we set the color of {@code paint} to BLUE, its stroke width to 3, and use it to call
         * the {@code drawPoints} method of {@code canvas} which draws a series of points, each point
         * is centered at the coordinate specified by 2 consecutive values in the {@code mPts} array,
         * and its diameter is specified by the paint's stroke width.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        @Override
        protected void onDraw(Canvas canvas) {
            Paint paint = mPaint;

            canvas.translate(10, 10);

            canvas.drawColor(Color.WHITE);

            paint.setColor(Color.RED);
            paint.setStrokeWidth(0);
            canvas.drawLines(mPts, paint);

            paint.setColor(Color.BLUE);
            paint.setStrokeWidth(3);
            canvas.drawPoints(mPts, paint);
        }
    }
}


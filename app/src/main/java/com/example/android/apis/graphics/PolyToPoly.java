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

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
//import com.example.android.apis.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.view.View;

/**
 * Shows how to use Matrix.setPolyToPoly to move and warp drawings done to a Canvas:
 * <ul>
 * <li>translate (1 point)</li>
 * <li>rotate/uniform-scale (2 points)</li>
 * <li>rotate/skew (3 points)</li>
 * <li>perspective (4 points)</li>
 * </ul>
 */
public class PolyToPoly extends GraphicsActivity {

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

    /**
     * Demo showing how to use Matrix.setPolyToPoly to move and warp drawings done to a Canvas
     */
    private static class SampleView extends View {
        /**
         * {@code Paint} we use to draw with
         */
        private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        /**
         * {@code Matrix} we use to hold transformation that warps our drawings when concatenated to
         * the current matrix of the canvas.
         */
        private Matrix mMatrix = new Matrix();
        /**
         * {@code FontMetrics} of the {@code Paint mPaint} used to center text we draw.
         */
        private Paint.FontMetrics mFontMetrics;

        /**
         * Draws a warped rectangle with two lines connecting the nonadjacent vertices and a number
         * in the center representing the number of points in the {@code setPolyToPoly} transformation
         * matrix used to warp it. The transformation matrix is created using the {@code float src[]}
         * and {@code float dst[]} parameters.
         * <p>
         * First we save the current matrix and clip of {@code Canvas canvas} onto a private stack.
         * Then we set {@code Matrix mMatrix} to a matrix such that the points specified in our parameter
         * {@code float src[]} would map to the points specified by our parameter {@code float dst[]}.
         * We then pre-concatenate {@code mMatrix} to the current matrix of {@code Canvas canvas}.
         * <p>
         * We set the color of {@code Paint mPaint} to GRAY, set its style to STROKE, and then use it
         * to draw a rectangle, and two lines connecting the nonadjacent vertices to {@code canvas}.
         * <p>
         * We set the color of {@code mPaint} to RED, and set its style to FILL. The calculate the
         * center of our rectangle based on the size of the rectangle and the size of the font used
         * by {@code mPaint} then draw the number of points in our {@code src} array at the center of
         * the rectangle.
         * <p>
         * Finally we restore the current matrix of the {@code Canvas canvas} to the state it was in
         * before the {@code save} call at the beginning of our method.
         *
         * @param canvas {@code Canvas} to draw to
         * @param src    array of source points for a call to {@code setPolyToPoly}
         * @param dst    array of destination points for a call to {@code setPolyToPoly
         */
        private void doDraw(Canvas canvas, float src[], float dst[]) {
            canvas.save();
            mMatrix.setPolyToPoly(src, 0, dst, 0, src.length >> 1);
            canvas.concat(mMatrix);

            mPaint.setColor(Color.GRAY);
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(0, 0, 64, 64, mPaint);
            canvas.drawLine(0, 0, 64, 64, mPaint);
            canvas.drawLine(0, 64, 64, 0, mPaint);

            mPaint.setColor(Color.RED);
            mPaint.setStyle(Paint.Style.FILL);
            // how to draw the text center on our square
            // centering in X is easy... use alignment (and X at midpoint)
            float x = 64 / 2;
            // centering in Y, we need to measure ascent/descent first
            float y = 64 / 2 - (mFontMetrics.ascent + mFontMetrics.descent) / 2;
            canvas.drawText(src.length / 2 + "", x, y, mPaint);

            canvas.restore();
        }

        /**
         * Our constructor. First we call our super's constructor, then we set the stroke width of
         * {@code Paint mPaint} to 4, its text size to 40, and its text alignment to CENTER. Finally
         * we initialize our field {@code Paint.FontMetrics mFontMetrics} with a font metrics object
         * filled with the appropriate values given the text attributes of {@code Paint mPaint}.
         *
         * @param context {@code Context} to access resources.
         */
        public SampleView(Context context) {
            super(context);

            // for when the style is STROKE
            mPaint.setStrokeWidth(4);
            // for when we draw text
            mPaint.setTextSize(40);
            mPaint.setTextAlign(Paint.Align.CENTER);
            mFontMetrics = mPaint.getFontMetrics();
        }

        /**
         * We implement this to do our drawing. First we set our entire {@code Canvas canvas} to the
         * color WHITE. Then we produce four different examples using the method {@code setPolyToPoly}
         * each wrapped between matching {@code canvas.save} and {@code canvas.restore} calls:
         * <ul>
         * <li>
         * translate (1 point) - we move the canvas to (10,10) then call our method {@code doDraw}
         * with one point, which produces and uses a transformation matrix which moves the
         * point (0,0) to (5,5)
         * </li>
         * <li>
         * rotate/uniform-scale (2 points) - we move the canvas to (160,10) then call our method
         * {@code doDraw} with two points, which produces and uses a transformation matrix which
         * maps (32,32) to (32,32) (the center does not move) and (64,32) to (64,48) thus rotating
         * the drawing and enlarging it slightly.
         * </li>
         * <li>
         * rotate/skew (3 points) - we move the canvas to (10,110) then call our method {@code doDraw}
         * with three points, which produces and uses a transformation matrix which maps (0,0)
         * to (0,0), (64,0) to (96,0) (top line stretched), and (0,64) to (24,64) (bottom line
         * moved to right).
         * </li>
         * <li>
         * perspective (4 points) - we move the canvas to (160,110) then call our method {@code doDraw}
         * with four points, which produces and uses a transformation matrix which maps (0,0) to
         * (0,0), (64,0) to (96,0) (top line stretched), (64,64) to (64,96) (right bottom corner
         * moved down), and (0,64) to 0,64).
         * </li>
         * </ul>
         *
         * @param canvas the canvas on which the background will be drawn
         */
        @SuppressLint("DrawAllocation")
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.WHITE);

            canvas.save();
            canvas.translate(10, 10);
            // translate (1 point)
            doDraw(canvas,
                    new float[]{0, 0},
                    new float[]{5, 5});
            canvas.restore();

            canvas.save();
            canvas.translate(160, 10);
            // rotate/uniform-scale (2 points)
            doDraw(canvas,
                    new float[]{32, 32, 64, 32},
                    new float[]{32, 32, 64, 48});
            canvas.restore();

            canvas.save();
            canvas.translate(10, 110);
            // rotate/skew (3 points)
            doDraw(canvas,
                    new float[]{0, 0, 64, 0, 0, 64},
                    new float[]{0, 0, 96, 0, 24, 64});
            canvas.restore();

            canvas.save();
            canvas.translate(160, 110);
            // perspective (4 points)
            doDraw(canvas,
                    new float[]{0, 0, 64, 0, 64, 64, 0, 64},
                    new float[]{0, 0, 96, 0, 64, 96, 0, 64});
            canvas.restore();
        }
    }
}

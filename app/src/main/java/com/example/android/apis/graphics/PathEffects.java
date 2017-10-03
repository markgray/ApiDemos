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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

/**
 * Shows how to use the PathEffect classes to make animated dashed lines, and smoothed, rounded lines.
 */
public class PathEffects extends GraphicsActivity {

    public static int DISTANCE_BETWEEN_LINES = 28;
    public static int X_INCREMENT = 20;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to a new instance of {@code SampleView}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final float scale = getResources().getDisplayMetrics().density;
        DISTANCE_BETWEEN_LINES = (int) (28 * scale);
        X_INCREMENT = (int) (20 * scale);
        setContentView(new SampleView(this));
    }

    /**
     * Class which displays several animated dashed lines, and smoothed, rounded lines.
     */
    private static class SampleView extends View {
        /**
         * {@code Paint} instance that we use to draw our lines.
         */
        private Paint mPaint;
        /**
         * Random zigzag {@code Path} we use to draw each line
         */
        private Path mPath;
        /**
         * Our 6 kinds of {@code PathEffect} examples (including null - no effect).
         */
        private PathEffect[] mEffects;
        /**
         * The 6 colors we use for our lines: Color.BLACK, Color.RED, Color.BLUE, Color.GREEN,
         * Color.MAGENTA, and Color.BLACK
         */
        private int[] mColors;
        /**
         * Offset into the intervals array (mod the sum of all of the intervals) used when creating
         * dash effect lines. By incrementing it in the {@code onDraw} method the lines appear to
         * move.
         */
        private float mPhase;

        /**
         * Unused so no comment.
         *
         * @param phase Offset into the intervals array
         * @return a {@code PathEffect} instance
         */
        @SuppressWarnings("unused")
        private static PathEffect makeDash(float phase) {
            return new DashPathEffect(new float[]{15, 5, 8, 5}, phase);
        }

        /**
         * Allocates and initializes 6 different {@code PathEffect} objects using the current value
         * of {@code phase} passed us.
         *
         * @param e array of {@code PathEffect} objects to allocate and initialize.
         * @param phase Offset into the intervals array.
         */
        private static void makeEffects(PathEffect[] e, float phase) {
            e[0] = null;     // no effect
            e[1] = new CornerPathEffect(10);
            e[2] = new DashPathEffect(new float[]{10, 5, 5, 5}, phase);
            e[3] = new PathDashPathEffect(makePathDash(), 12, phase, PathDashPathEffect.Style.ROTATE);
            e[4] = new ComposePathEffect(e[2], e[1]);
            e[5] = new ComposePathEffect(e[3], e[1]);
        }

        public SampleView(Context context) {
            super(context);
            setFocusable(true);
            setFocusableInTouchMode(true);

            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(6);

            mPath = makeFollowPath();

            mEffects = new PathEffect[6];

            mColors = new int[]{Color.BLACK, Color.RED, Color.BLUE,
                    Color.GREEN, Color.MAGENTA, Color.BLACK
            };
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPath = makeFollowPath();
                }
            });
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.WHITE);

            @SuppressLint("DrawAllocation")
            RectF bounds = new RectF();
            mPath.computeBounds(bounds, false);
            canvas.translate(10 - bounds.left, 10 - bounds.top);

            makeEffects(mEffects, mPhase);
            mPhase += 1;
            invalidate();

            for (int i = 0; i < mEffects.length; i++) {
                mPaint.setPathEffect(mEffects[i]);
                mPaint.setColor(mColors[i]);
                canvas.drawPath(mPath, mPaint);
                canvas.translate(0, DISTANCE_BETWEEN_LINES);
            }
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    mPath = makeFollowPath();
                    return true;
            }
            return super.onKeyDown(keyCode, event);
        }

        private static Path makeFollowPath() {
            Path p = new Path();
            p.moveTo(0, 0);
            for (int i = 1; i <= 15; i++) {
                p.lineTo(i * X_INCREMENT, (float) Math.random() * 35);
            }
            return p;
        }

        private static Path makePathDash() {
            Path p = new Path();
            p.moveTo(4, 0);
            p.lineTo(0, -4);
            p.lineTo(8, -4);
            p.lineTo(12, 0);
            p.lineTo(8, 4);
            p.lineTo(0, 4);
            return p;
        }
    }
}


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
         * of {@code phase} passed us:
         * <ul>
         * <li>
         * e[0] - null for no path effect
         * </li>
         * <li>
         * e[1] - {@code CornerPathEffect(10)} Transforms geometries that are drawn (either
         * STROKE or FILL styles) by replacing any sharp angles between line segments into
         * rounded angles of the specified radius (10).
         * </li>
         * <li>
         * e[2] - {@code DashPathEffect} Dashed line with the first segment 10 pixels, followed
         * by 5 pixels off, then 5 pixels drawn, then 5 pixels off. {@code phase} is passed to
         * the constructor to select an offset into the dashes allowing for an animated dashed
         * line when this method is called with different values for {@code phase} every time
         * the line is to be drawn.
         * </li>
         * <li>
         * e[3] - {@code PathDashPathEffect} Dashes the drawn path by stamping it with the shape
         * of the {@code Path} returned by our method {@code makePathDash} (an arrow like shape).
         * It has a spacing of 12 between "stampings", passes {@code phase} to the constructor
         * to allow animation of the dashes, and uses PathDashPathEffect.Style.ROTATE (rotates
         * the shape about its center)
         * </li>
         * <li>
         * e[4] - {@code ComposePathEffect} A {@code PathEffect} which applies e[1] first (replaces
         * sharp corners with rounded angles) followed by e[2] (dashed line path effect).
         * </li>
         * <li>
         * e[5] - {@code ComposePathEffect} A {@code PathEffect} which applies e[1] first (replaces
         * sharp corners with rounded angles) followed by e[3] (dashed line using a shape to stamp
         * along the line).
         * </li>
         * </ul>
         *
         * @param e     array of {@code PathEffect} objects to allocate and initialize.
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

        /**
         * Our constructor. First we call our super's constructor, then we set our window to be focusable,
         * an focusable in touch mode. We initialize our field {@code Paint mPaint} with a new instance of
         * {@code Paint} with the anti alias flag set, set its style to STROKE, and set its stroke width
         * to 6. We initialize our field {@code Path mPath} with the value returned from our method
         * {@code makeFollowPath}. We allocate a 6 element array of {@code PathEffect} objects for our
         * field {@code PathEffect[] mEffects}, and initialize our field {@code int[] mColors} with 6
         * colors.
         * <p>
         * Finally we set our {@code OnClickListener} to an anonymous class which sets {@code mPath}
         * to a new random {@code Path} created by our method {@code makeFollowPath}.
         *
         * @param context {@code Context} to use for resources, this when called from the {@code onCreate}
         *                method of the {@code PathEffects} activity.
         */
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
                /**
                 * Called when the view is clicked in order to create a new random {@code Path} for
                 * {@code mPath}.
                 *
                 * @param v {@code View} that was clicked
                 */
                @Override
                public void onClick(View v) {
                    mPath = makeFollowPath();
                }
            });
        }

        /**
         * We implement this to do our drawing. First we fill the {@code Canvas canvas} with the color
         * white. Then we allocate a new instance of {@code RectF} for {@code RectF bounds} and load
         * it with the bounds of the control points of the path {@code mPath}. We translate our
         * {@code Canvas canvas} in the x direction by 10 - {@code bounds.left} ({@code bounds.left}
         * is always 0) and in the y direction by 10 - {@code bounds.top} {@code bounds.top} is always
         * 0 as well).
         * <p>
         * Next we call our method {@code makeEffects} to generate new versions of {@code PathEffects}
         * for {@code mEffects} using the present value of {@code mPhase}, then increment {@code mPhase}
         * and call {@code invalidate} to request that a new call to this method {@code onDraw} in the
         * future.
         * <p>
         * Now we are ready to loop through each {@code PathEffect} in {@code mEffects}, setting the
         * path effect object for {@code Paint mPaint} to each in turn, setting the color of {@code mPaint}
         * to the next color, then instructing the {@code Canvas canvas} to draw the path {@code mPath}
         * using {@code mPaint} as the {@code Paint}. We then translate the canvas down in the y coordinate
         * in order to get ready for the next {@code PathEffect}.
         *
         * @param canvas the canvas on which the background will be drawn
         */
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

        /**
         * Called when a key down event has occurred. If the keycode we KEYCODE_DPAD_CENTER, we set
         * {@code Path mPath} to the new random {@code Path} returned from our method {@code makeFollowPath}
         * and return true, otherwise we return the value returned by our super's implementation of
         * {@code onKeyDown}.
         *
         * @param keyCode A key code that represents the button pressed,
         * @param event   The KeyEvent object that defines the button action.
         * @return true if we handled the event
         */
        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    mPath = makeFollowPath();
                    return true;
            }
            return super.onKeyDown(keyCode, event);
        }

        /**
         * Creates and returns a random {@code Path}. First we allocate a new {@code Path} instance
         * {@code Path p}. We move {@code p} to (0,0), then add 15 {@code lineTo} line segments with
         * a spacing of X_INCREMENT in the x direction and a random y between 0 and 35.
         *
         * @return random {@code Path}.
         */
        private static Path makeFollowPath() {
            Path p = new Path();
            p.moveTo(0, 0);
            for (int i = 1; i <= 15; i++) {
                p.lineTo(i * X_INCREMENT, (float) Math.random() * 35);
            }
            return p;
        }

        /**
         * Creates and returns a {@code Path} that looks rather like an arrowhead when drawn.
         *
         * @return {@code Path} drawing an arrowhead.
         */
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


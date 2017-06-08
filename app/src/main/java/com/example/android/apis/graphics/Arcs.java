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

import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.view.View;

/**
 * Shows how to draw arcs and rectangles to a Canvas -- need to figure out what slows down
 * frame rate -- I'm guessing something inside native_drawArc
 */
public class Arcs extends GraphicsActivity {

    /**
     * Called when the activity is staring. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to an instance of {@code SampleView}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SampleView(this));
    }

    /**
     * Subclass of View which consists of one big circle inside a rectangle, and four little circles
     * inside rectangles. Rotating arcs are drawn inside each of these circles, with four different
     * paints and "use centers" arguments used for the small circles, and a rotation of those four
     * combos for the big circle which changes every 360 degrees.
     */
    private static class SampleView extends View {
        /**
         * The four {@code Paint} objects used for the small circles, and cycled through for the big one
         */
        private Paint[] mPaints;
        /**
         * {@code Paint} used for the rectangles drawn around the 5 circles.
         */
        private Paint mFramePaint;
        /**
         * Boolean flag passed to {@code drawArc} for each of the circles, it is true for 2 and
         * false for 2. If true, includes the center of the oval in the arc, and close it if it
         * is being stroked. This will draw a wedge.
         */
        private boolean[] mUseCenters;
        /**
         * Rectangles for the four small circles in a rectangle, defined by (left, top, right, bottom)
         * float pixel values
         */
        private RectF[] mOvals;
        /**
         * Rectangle for the large circle in a rectangle, defined by (left, top, right, bottom)
         * float pixel values
         */
        private RectF mBigOval;
        /**
         * Starting angle for the arcs being drawn, starts at 0 degrees, incremented by 15 degrees
         * every time {@code mSweep} reaches 360 degrees
         */
        private float mStart;
        /**
         * Sweep angle for the arcs being drawn, starts at 0 degrees, incremented by 2 degrees every
         * time {@code onDraw} is called and reset to 0 when it reaches 360 degrees.
         */
        private float mSweep;
        /**
         * Index 0-3 of values used by the large circle in a rectangle, chooses which of the small
         * circles in a rectangle the large circle mirrors, it is incremented modulo 4 every 360
         * degrees of arc sweep.
         */
        private int mBigIndex;

        /**
         * Number of degrees that {@code mSweep} is incremented every time {@code onDraw} is called
         */
        private static final float SWEEP_INC = 2;
        /**
         * Number of degrees that {@code mStart} is incremented every 360 degrees of arc sweep
         */
        private static final float START_INC = 15;

        /**
         * Constructor, allocates and initializes fields needed by our {@code onDraw} override. First
         * we call through to our super's constructor, next we allocate an array of four references
         * for each of the fields used by our small circles: {@code Paint[] mPaints},
         * {@code boolean[] mUseCenters} and {@code RectF[] mOvals}. We configure the entries for
         * {@code mPaints} and {@code mUseCenters} as follows:
         * <ul>
         * <li>
         * [0] Sets its {@code mPaints[0]} to a {@code Paint} with antialias enabled, style FILL,
         * and color 0x88FF0000 (RED), and sets its {@code mUseCenters[0]} to false
         * </li>
         * <li>
         * [1] Sets its {@code mPaints[1]} to a {@code Paint} with color 0x8800FF00 (GREEN),
         * and sets its {@code mUseCenters[0]} to true.
         * </li>
         * <li>
         * [2] Sets its {@code mPaints[2]} to a {@code Paint} with style STROKE, a stroke width
         * of 4 and color 0x880000FF (BLUE), and sets its {@code mUseCenters[0]} to false
         * </li>
         * <li>
         * [3] Sets its {@code mPaints[3]} to a {@code Paint} with color 0x88888888 (GRAY),
         * and sets its {@code mUseCenters[0]} to true
         * </li>
         * </ul>
         * Next we allocate a rectangle for {@code RectF mBigOval} left 40, top 10, right 280, bottom
         * 250 (a 240x240 rectangle whose top corner is at (40,10)), and for each of the small circles
         * in {@code RectF[] mOvals} we allocate a 60x60 rectangle with the top corners located at
         * (10,270), (90,270), (170,270) and (250,270) respectively. Finally we allocate a {@code Paint}
         * for the {@code Paint} used to draw the rectangle around all five circles {@code Paint mFramePaint},
         * set its antialias flag to true, style to STROKE, and stroke width to 0.
         *
         * @param context {@code Context} passed to super's constructor for resource access, "this"
         *                is used from {@code onCreate} override of {@code Arcs}
         */
        public SampleView(Context context) {
            super(context);

            mPaints = new Paint[4];
            mUseCenters = new boolean[4];
            mOvals = new RectF[4];

            mPaints[0] = new Paint();
            mPaints[0].setAntiAlias(true);
            mPaints[0].setStyle(Paint.Style.FILL);
            mPaints[0].setColor(0x88FF0000);
            mUseCenters[0] = false;

            mPaints[1] = new Paint(mPaints[0]);
            mPaints[1].setColor(0x8800FF00);
            mUseCenters[1] = true;

            mPaints[2] = new Paint(mPaints[0]);
            mPaints[2].setStyle(Paint.Style.STROKE);
            mPaints[2].setStrokeWidth(4);
            mPaints[2].setColor(0x880000FF);
            mUseCenters[2] = false;

            mPaints[3] = new Paint(mPaints[2]);
            mPaints[3].setColor(0x88888888);
            mUseCenters[3] = true;

            mBigOval = new RectF(40, 10, 280, 250);

            mOvals[0] = new RectF(10, 270, 70, 330);
            mOvals[1] = new RectF(90, 270, 150, 330);
            mOvals[2] = new RectF(170, 270, 230, 330);
            mOvals[3] = new RectF(250, 270, 310, 330);

            mFramePaint = new Paint();
            mFramePaint.setAntiAlias(true);
            mFramePaint.setStyle(Paint.Style.STROKE);
            mFramePaint.setStrokeWidth(0);
        }

        /**
         * Draws the rectangle {@code RectF oval} passed it using {@code Paint mFramePaint} as the
         * {@code Paint}, then draws an arc of the circle enclosed by {@code oval} between {@code mStart}
         * and {@code mSweep} using {@code Paint paint} as the {@code Paint} and passing the value of
         * {@code useCenter} to {@code Canvas.drawArc()} to instruct it where to include the center when
         * drawing.
         *
         * @param canvas {@code Canvas} we are to draw to
         * @param oval {@code Rectangle} and enclosed circle we want to draw
         * @param useCenter if true, we are to include the center of the oval in the arc, and close it
         * @param paint {@code Paint} to use for drawing
         */
        private void drawArcs(Canvas canvas, RectF oval, boolean useCenter, Paint paint) {
            canvas.drawRect(oval, mFramePaint);
            canvas.drawArc(oval, mStart, mSweep, useCenter, paint);
        }

        /**
         * We implement this to do our drawing. First we fill the entire {@code Canvas canvas} passed
         * us with the color WHITE
         *
         * @param canvas {@code Canvas} of our View to draw to
         */
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.WHITE);

            drawArcs(canvas, mBigOval, mUseCenters[mBigIndex], mPaints[mBigIndex]);

            for (int i = 0; i < 4; i++) {
                drawArcs(canvas, mOvals[i], mUseCenters[i], mPaints[i]);
            }

            mSweep += SWEEP_INC;
            if (mSweep > 360) {
                mSweep -= 360;
                mStart += START_INC;
                if (mStart >= 360) {
                    mStart -= 360;
                }
                mBigIndex = (mBigIndex + 1) % mOvals.length;
            }
            invalidate();
        }
    }
}


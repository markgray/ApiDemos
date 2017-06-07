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
         * {@code Paint} used for the rectangle drawn around the 5 circles.
         */
        private Paint mFramePaint;
        private boolean[] mUseCenters;
        private RectF[] mOvals;
        private RectF mBigOval;
        private float mStart;
        private float mSweep;
        private int mBigIndex;

        private static final float SWEEP_INC = 2;
        private static final float START_INC = 15;

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

        private void drawArcs(Canvas canvas, RectF oval, boolean useCenter, Paint paint) {
            canvas.drawRect(oval, mFramePaint);
            canvas.drawArc(oval, mStart, mSweep, useCenter, paint);
        }

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


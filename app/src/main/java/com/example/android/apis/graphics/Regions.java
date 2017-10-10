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

import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.view.View;

/**
 * Shows how to use the Region class to merge two or more Rectangle's in a Region using Union, Xor,
 * Difference, and Intersect operations.
 */
public class Regions extends GraphicsActivity {

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
     * Our demo custom view, demonstrates the use of the Region class.
     */
    private static class SampleView extends View {
        /**
         * {@code Paint} instance used to do all our drawing.
         */
        private final Paint mPaint = new Paint();
        /**
         * {@code Rect} rectangle with the top left corner at (10.10) and bottom right corner at (100.80)
         */
        private final Rect mRect1 = new Rect();
        /**
         * {@code Rect} rectangle with the top left corner at (50,50) and bottom right corner at (130.110)
         */
        private final Rect mRect2 = new Rect();

        /**
         * Our constructor. First we call our super's constructor, then we enable our view to receive
         * focus. We set the anti alias flag of {@code Paint mPaint} to true, its text size to 16 and
         * its text alignment to CENTER. We then initialize our field {@code Rect mRect1} with a rectangle
         * with the top left corner at (10.10) and bottom right corner at (100.80), and {@code Rect mRect2}
         * with a rectangle with the top left corner at (50,50) and bottom right corner at (130.110).
         *
         * @param context {@code Context} to use to access resources, this when called from the
         *                {@code onCreate} method of the {@code Regions} activity.
         */
        public SampleView(Context context) {
            super(context);
            setFocusable(true);

            mPaint.setAntiAlias(true);
            mPaint.setTextSize(16);
            mPaint.setTextAlign(Paint.Align.CENTER);

            mRect1.set(10, 10, 100, 80);
            mRect2.set(50, 50, 130, 110);
        }

        /**
         * Draws the original rectangles {@code Rect1} and {@code Rect2} using the parameter
         * {@code alpha} as the alpha value of the {@code Paint mPaint} we use to draw them.
         *
         * @param canvas {@code Canvas} to draw to
         * @param alpha alpha value to set the alpha value of {@code Paint mPaint} to before drawing.
         */
        private void drawOriginalRects(Canvas canvas, int alpha) {
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(Color.RED);
            mPaint.setAlpha(alpha);
            drawCentered(canvas, mRect1, mPaint);
            mPaint.setColor(Color.BLUE);
            mPaint.setAlpha(alpha);
            drawCentered(canvas, mRect2, mPaint);

            // restore style
            mPaint.setStyle(Paint.Style.FILL);
        }

        private void drawRgn(Canvas canvas, int color, String str, Region.Op op) {
            if (str != null) {
                mPaint.setColor(Color.BLACK);
                canvas.drawText(str, 80, 24, mPaint);
            }

            Region rgn = new Region();
            rgn.set(mRect1);
            rgn.op(mRect2, op);

            mPaint.setColor(color);
            RegionIterator iter = new RegionIterator(rgn);
            Rect r = new Rect();

            canvas.translate(0, 30);
            mPaint.setColor(color);
            while (iter.next(r)) {
                canvas.drawRect(r, mPaint);
            }
            drawOriginalRects(canvas, 0x80);
        }

        private static void drawCentered(Canvas c, Rect r, Paint p) {
            float inset = p.getStrokeWidth() * 0.5f;
            if (inset == 0) {   // catch hairlines
                inset = 0.5f;
            }
            c.drawRect(r.left + inset, r.top + inset, r.right - inset, r.bottom - inset, p);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.GRAY);

            canvas.save();
            canvas.translate(80, 5);
            drawOriginalRects(canvas, 0xFF);
            canvas.restore();

            mPaint.setStyle(Paint.Style.FILL);

            canvas.save();
            canvas.translate(0, 140);
            drawRgn(canvas, Color.RED, "Union", Region.Op.UNION);
            canvas.restore();

            canvas.save();
            canvas.translate(0, 280);
            drawRgn(canvas, Color.BLUE, "Xor", Region.Op.XOR);
            canvas.restore();

            canvas.save();
            canvas.translate(160, 140);
            drawRgn(canvas, Color.GREEN, "Difference", Region.Op.DIFFERENCE);
            canvas.restore();

            canvas.save();
            canvas.translate(160, 280);
            drawRgn(canvas, Color.WHITE, "Intersect", Region.Op.INTERSECT);
            canvas.restore();
        }
    }
}


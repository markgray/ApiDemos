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
         * <p>
         * First we set the style of {@code Paint mPaint} to STROKE, its color to RED, and its alpha
         * to our parameter {@code alpha}. We then call our method {@code drawCentered} to draw
         * {@code mRect1} on {@code Canvas canvas} using {@code mPaint}.
         * <p>
         * Next we set the color of {@code mPaint} to BLUE, and its alpha to our parameter
         * {@code alpha}. We then call our method {@code drawCentered} to draw {@code mRect2} on
         * {@code Canvas canvas} using {@code mPaint}.
         * <p>
         * Finally we restore the style of {@code mPaint} to FILL.
         *
         * @param canvas {@code Canvas} to draw to
         * @param alpha  alpha value to set the alpha value of {@code Paint mPaint} to before drawing.
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

        /**
         * Creates a {@code Region} by combining {@code Rect mRect1} and {@code Rect mRect2} using
         * our parameter {@code Region.Op op} and then draws it.
         * <p>
         * First if our parameter {@code String str} is not null, we set the color of {@code Paint mPaint}
         * to BLACK and draw {@code str} at the coordinates (80,24).
         * <p>
         * We allocate a new instance of {@code Region} for {@code Region rgn}, set it to {@code Rect Rect1},
         * then perform {@code Region.Op op} on the {@code Region} and {@code Rect Rect2} ({@code rgn}
         * will become a collection of 1 or more {@code Rect} objects depending on the {@code Region.Op}
         * used when {@code Rect2} was included in the {@code Region}:
         * <ul>
         * <li>UNION - 3 {@code Rect}: (10,10,100,50), (10,50,130,80), and (50,80,130,110)</li>
         * <li>XOR - 4 {@code Rect}: (10,10,100,50), (10,50,50,80), (100,50,130,80), and (50,80,130,110)</li>
         * <li>DIFFERENCE - 2 {@code Rect}: (10,10,100,50), and (10,50,50,80)</li>
         * <li>INTERSECT - 1 {@code Rect}: (50,50,100,80)</li>
         * </ul>
         * Next we set the color of {@code Paint mPaint} to our parameter {@code color}, create an
         * {@code RegionIterator iter} for {@code Region rgm}, and allocate a new {@code Rect} for
         * {@code Rect r}. We move the {@code Canvas canvas} to (0,30) and set the color of
         * {@code Paint mPaint} to our parameter {@code color} one more time for luck.
         * <p>
         * Now we iterate through the {@code Rect} objects in {@code Region rgn} (using the iterator
         * {@code iter}) setting {@code Rect r} to each in turn, and then drawing that {@code Rect}
         * to {@code Canvas canvas} using {@code Paint mPaint}.
         * <p>
         * Finally we call our method {@code drawOriginalRects} to draw an outline of the original
         * {@code Rect mRect1} and {@code Rect mRect2} using an alpha of only 0x80.
         *
         * @param canvas {@code Canvas} we are to draw to
         * @param color  color to use for drawing
         * @param str    optional string to label our drawing
         * @param op     {@code Region.Op} to use in forming our {@code Region}
         */
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

        /**
         * Draws the {@code Rect r} passed it offset by half the stroke width of the {@code Paint p}
         * on {@code Canvas c} using {@code p} as the {@code Paint}. We calculate {@code inset} to
         * be half of the stroke width of {@code Paint p}, and if 0 set {@code inset} to 0.5. Then
         * we draw the rectangle passed us in {@code Rect r} with each coordinate offset by {@code inset}
         *
         * @param c {@code Canvas} to draw to
         * @param r {@code Rect} to draw
         * @param p {@code Paint} to use when drawing
         */
        private static void drawCentered(Canvas c, Rect r, Paint p) {
            float inset = p.getStrokeWidth() * 0.5f;
            if (inset == 0) {   // catch hairlines
                inset = 0.5f;
            }
            c.drawRect(r.left + inset, r.top + inset, r.right - inset, r.bottom - inset, p);
        }

        /**
         * We implement this to do our drawing. First we set the entire {@code Canvas canvas} to
         * the color GRAY. We save the current matrix and clip of the canvas onto a private stack,
         * move the canvas to the point (80,5) and call our method {@code drawOriginalRects} to draw
         * {@code Rect mRect1} and {@code Rect mRect2} using an alpha of 0xFF, then we restore the
         * state of the canvas to its previous state.
         * <p>
         * Next we set the style of {@code Paint mPaint} to FILL. We save the current matrix and clip
         * of the canvas onto a private stack, move the canvas to the point (0,140) and call our method
         * {@code drawRgn} to form a {@code Region} from {@code mRect1} and {@code mRect2} using the
         * Region.Op.UNION, draw the result in RED and label it "Union". We then restore the state of
         * the canvas to its previous state before we called {@code save}.
         * <p>
         * Now we save the current matrix and clip of the canvas onto a private stack, move the canvas
         * to the point (0,280) and call our method {@code drawRgn} to form a {@code Region} from
         * {@code mRect1} and {@code mRect2} using the Region.Op.XOR, draw the result in BLUE and
         * label it "Xor". We then restore the state of the canvas to its previous state before we
         * called {@code save}.
         * <p>
         * We save the current matrix and clip of the canvas onto a private stack, move the canvas
         * to the point (160,140) and call our method {@code drawRgn} to form a {@code Region} from
         * {@code mRect1} and {@code mRect2} using the Region.Op.DIFFERENCE, draw the result in GREEN
         * and label it "Difference". We then restore the state of the canvas to its previous state
         * before we called {@code save}.
         * <p>
         * We save the current matrix and clip of the canvas onto a private stack, move the canvas
         * to the point (160,280) and call our method {@code drawRgn} to form a {@code Region} from
         * {@code mRect1} and {@code mRect2} using the Region.Op.INTERSECT, draw the result in WHITE
         * and label it "Intersect". We then restore the state of the canvas to its previous state
         * before we called {@code save}.
         *
         * @param canvas the canvas on which the background will be drawn
         */
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


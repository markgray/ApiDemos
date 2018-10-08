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
 * Shows how to use android.graphics.Canvas methods clipPath and clipRect, as well as some other
 * Canvas drawing methods.
 */
public class Clipping extends GraphicsActivity {

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
     * Custom View which draws the same scene 3 times using different clip settings for the
     * {@code Canvas} it is drawing to.
     */
    private static class SampleView extends View {
        /**
         * {@code Paint} we use to draw to the {@code Canvas}
         */
        private Paint mPaint;
        /**
         * {@code Path} we create for a clip, and set on our {@code Canvas} using {@code Canvas.clipPath}
         */
        private Path mPath;

        /**
         * logical density of the screen
         */
        public float SCREEN_DENSITY;

        /**
         * Basic constructor for our class. First we call our super's constructor, then we enable our
         * view to receive focus. We initialize our field {@code float SCREEN_DENSITY} with the logical
         * density of the screen, then we allocate an instance of {@code Paint} for our field
         * {@code Paint mPaint}, set the ANTI_ALIAS_FLAG to true to enable antialiasing, set the stroke
         * width to 6, the text size to 16, and set the text alignment to be Paint.Align.RIGHT. Finally
         * we allocate a new instance of {@code Path} to initialize our field {@code Path mPath}.
         *
         * @param context the {@code Context} to use to retrieve resources, "this" when called from
         *                {@code onCreate} override of our activity.
         */
        public SampleView(Context context) {
            super(context);
            setFocusable(true);

            SCREEN_DENSITY = getResources().getDisplayMetrics().density;
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setStrokeWidth(6);
            mPaint.setTextSize(16);
            mPaint.setTextAlign(Paint.Align.RIGHT);

            mPath = new Path();
        }

        /**
         * Draws a "scene" consisting of a red line, a green circle, and the blue text "Clipping".
         * First we intersect the current clip of our parameter {@code Canvas canvas} with the
         * rectangle (0,0,100,100) ((left,top,right,bottom) -- this is relative to the already
         * translated {@code Canvas canvas} passed us and will prevent all drawing instructions
         * outside of this rectangle from having any effect, and will respect the "no draw" regions
         * of {@code canvas} specified by its current clip.) Then fill the entire canvas' bitmap
         * (restricted to the current clip) with the color white, using source over Porter-Duff mode.
         * <p>
         * We set the color of our field {@code Paint mPaint} to red, and use it to draw a line on
         * {@code canvas} from (0,0) to (100,100).
         * <p>
         * We set the color of {@code mPaint} to green and use it to draw a circle on {@code canvas}
         * centered at (30,70) with radius 30.
         * <p>
         * Finally we set the color of {@code mPaint} to blue and use it to write the text "Clipping"
         * to {@code canvas} starting at (100,30). The origin is interpreted based on the Align
         * setting in the paint which in our case is Paint.Align.RIGHT, so the end of the text is
         * at (100,30) not the beginning.
         *
         * @param canvas {@code Canvas} to draw our "scene" to, already translated to correct spot,
         *               and clipped for the clip instructions to be demonstrated.
         */
        private void drawScene(Canvas canvas) {
            canvas.clipRect(0, 0, 100, 100);

            canvas.drawColor(Color.WHITE);

            mPaint.setColor(Color.RED);
            canvas.drawLine(0, 0, 100, 100, mPaint);

            mPaint.setColor(Color.GREEN);
            canvas.drawCircle(30, 70, 30, mPaint);

            mPaint.setColor(Color.BLUE);
            canvas.drawText("Clipping", 100, 30, mPaint);
        }

        /**
         * We implement this to do our drawing. We call our method {@code drawScene} to draw the same
         * "scene" 3 times using different matrix and clip states applied to the {@code Canvas canvas}
         * passed us.
         * <p>
         * First we scale the {@code Canvas canvas} passed us to the logical density of the display.
         * Next we set the entire {@code Canvas canvas} to the color GRAY. Then surrounded by matching
         * {@code Canvas.save} and {@code Canvas.restore} calls (which save the matrix and clips states
         * then restore them afterwards), we translate the {@code Canvas canvas} to the position we
         * want drawing to be directed to, apply all the clip state modifications (relative to the new
         * position) and call our method {@code drawScene} to draw to the translated and clipped
         * {@code Canvas canvas} restoring it to the untranslated un-clipped state on return. The 3
         * "scenes" positions and clip states are:
         * <ul>
         *     <li>
         *         (10,10) No clipping added (apart from the clip rectangle used in {@code drawScene}
         *     </li>
         *     <li>
         *         (160,10) We start with a clip rectangle excluding areas outside of (10,10,90,90)
         *         then we add a clip rectangle which excludes areas inside of (30,30,70,70) (The
         *         {@code Region.Op.DIFFERENCE} parameter to {@code clipRectangle} subtracts the
         *         inside of the rectangle from the drawable canvas instead of the outside)
         *     </li>
         *     <li>
         *         (10,160) We clear any lines and curves from {@code Path mPath}, making it empty,
         *         then we add a circle centered at (50,50), of radius 50 and direction Counter clock
         *         wise to {@code mPath}, then use it to replace the clip path of {@code canvas}.
         *         Since the circle was drawn in the counter clockwise direction the clip will exclude
         *         areas outside of the circle from the drawable canvas.
         *     </li>
         * </ul>
         *
         * @param canvas the canvas on which the background will be drawn
         */
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.scale(SCREEN_DENSITY, SCREEN_DENSITY);
            canvas.drawColor(Color.GRAY);

            canvas.save();
            canvas.translate(10, 10);
            drawScene(canvas);
            canvas.restore();

            canvas.save();
            canvas.translate(160, 10);
            canvas.clipRect(10, 10, 90, 90);
            canvas.clipRect(30, 30, 70, 70, Region.Op.DIFFERENCE);
            drawScene(canvas);
            canvas.restore();

            canvas.save();
            canvas.translate(10, 160);
            mPath.reset();
            mPath.addCircle(50, 50, 50, Path.Direction.CCW);
            canvas.clipPath(mPath);
            drawScene(canvas);
            canvas.restore();
        }
    }
}


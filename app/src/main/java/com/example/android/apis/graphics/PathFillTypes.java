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

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
//import com.example.android.apis.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * Shows the effect of four different Path.FillType's on the same Path, which consists of two
 * intersecting circles: Path.FillType.WINDING, Path.FillType.EVEN_ODD, Path.FillType.INVERSE_WINDING,
 * and Path.FillType.INVERSE_EVEN_ODD.
 */
public class PathFillTypes extends GraphicsActivity {

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
     * This method converts dp unit to equivalent pixels, depending on device density. First we fetch
     * a {@code Resources} instance for {@code Resources resources}, then we fetch the current display
     * metrics that are in effect for this resource object to {@code DisplayMetrics metrics}. Finally
     * we return our {@code dp} parameter multiplied by the the screen density expressed as dots-per-inch,
     * divided by the reference density used throughout the system.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    /**
     * Sample view which draws two intersecting circles using 4 different {@code Path.FillType}.
     */
    private static class SampleView extends View {
        /**
         * {@code Paint} used to draw all 4 examples.
         */
        private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        /**
         * {@code Path} used to draw our two circles for all 4 examples.
         */
        private Path mPath;
        /**
         * Multiplier to convert dp units to pixels.
         */
        private float mPixelMultiplier;

        /**
         * Our constructor. First we call through to our super's constructor, then we set our window
         * to be focusable, and focusable in touch mode. We initialize our field {@code mPixelMultiplier}
         * with the return value of our method {@code convertDpToPixel} (a multiplier to convert dp
         * units to pixels for devices with different pixel densities).
         * <p>
         * We allocate a new instance of {@code Path} for our field {@code Path mPath}, then add two
         * intersecting circles to it (both use counter clockwise direction to wind the circle's contour).
         *
         * @param context {@code Context} used to access resources. "this" when called from the
         *                {@code onCreate} method of the {@code PathFillTypes} activity.
         */
        public SampleView(Context context) {
            super(context);
            setFocusable(true);
            setFocusableInTouchMode(true);
            mPixelMultiplier = convertDpToPixel(1, context);

            mPath = new Path();
            mPath.addCircle(40 * mPixelMultiplier, 40 * mPixelMultiplier, 45 * mPixelMultiplier, Path.Direction.CCW);
            mPath.addCircle(80 * mPixelMultiplier, 80 * mPixelMultiplier, 45 * mPixelMultiplier, Path.Direction.CCW);
        }

        /**
         * Draws {@code Path mPath} at different locations using different {@code Path.FillType} fill
         * types. First we save the current matrix and clip of {@code Canvas canvas} onto a private stack.
         * Then we pre-concatenate the current matrix with a translation matrix to (x,y), set the clip
         * rectangle of {@code canvas} to a 120 dp square, then set the entire canvas to the color white.
         * We set the fill type of {@code Path mPath} to our argument {@code Path.FillType ft}, and
         * instruct {@code canvas} to draw that {@code Path} using our argument {@code Paint paint}
         * as the {@code Paint}. Finally we remove all modifications to the {@code Canvas} matrix/clip
         * state since the save call at the beginning of this method.
         *
         * @param canvas {@code Canvas} we are supposed to draw on
         * @param x      x coordinate to use for our top left corner
         * @param y      y coordinate to use for our top left corner
         * @param ft     {@code Path.FillType} to use when we draw {@code Path mPath}
         * @param paint  {@code Paint} to use when drawing.
         */
        private void showPath(Canvas canvas, int x, int y, Path.FillType ft, Paint paint) {
            canvas.save();
            canvas.translate(x, y);
            canvas.clipRect(0, 0, 120 * mPixelMultiplier, 120 * mPixelMultiplier);
            canvas.drawColor(Color.WHITE);
            mPath.setFillType(ft);
            canvas.drawPath(mPath, paint);
            canvas.restore();
        }

        /**
         * We implement this to do our drawing. First we make a copy of the {@code Paint mPaint} pointer
         * in {@code Paint paint}. Next we set the entire {@code Canvas canvas} to a darkish gray, and
         * translate the canvas by 20 dp in both the x and y direction. We set the anti alias flag of
         * {@code paint} to true, and define the constant {@code m160} to be the pixel equivalent of
         * 160 dp.
         * <p>
         * We call our method {@code showPath} to draw our {@code Path mPath} using 4 different fill
         * types:
         * <ul>
         * <li>
         * (0,0) - WINDING - Specifies that "inside" is computed by a non-zero sum of signed edge crossings.
         * The inside of both circles are colored black.
         * </li>
         * <li>
         * (160,0) - EVEN_ODD - Specifies that "inside" is computed by an odd number of edge crossings.
         * The intersection of the two circles is left white, the rest of the circle is black.
         * </li>
         * <li>
         * (0,160) - INVERSE_WINDING - Same as WINDING, but draws outside of the path, rather than inside.
         * The inside of both cirlces is left white, and outside of the circles i painted black.
         * </li>
         * <li>
         * (160,160) - INVERSE_EVEN_ODD - Same as EVEN_ODD, but draws outside of the path, rather than inside.
         * The area outside of the circles and the intersection of the circles is painted black, the rest
         * of the inside of the circles is left white.
         * </li>
         * </ul>
         *
         * @param canvas the canvas on which the background will be drawn
         */
        @Override
        protected void onDraw(Canvas canvas) {
            Paint paint = mPaint;

            canvas.drawColor(0xFFCCCCCC);

            canvas.translate(20 * mPixelMultiplier, 20 * mPixelMultiplier);

            paint.setAntiAlias(true);
            int m160 = Math.round(160 * mPixelMultiplier);

            showPath(canvas, 0, 0, Path.FillType.WINDING, paint);
            showPath(canvas, m160, 0, Path.FillType.EVEN_ODD, paint);
            showPath(canvas, 0, m160, Path.FillType.INVERSE_WINDING, paint);
            showPath(canvas, m160, m160, Path.FillType.INVERSE_EVEN_ODD, paint);
        }
    }
}


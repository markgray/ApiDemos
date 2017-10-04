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
        private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Path mPath;
        private float mPixelMultiplier;

        public SampleView(Context context) {
            super(context);
            setFocusable(true);
            setFocusableInTouchMode(true);
            mPixelMultiplier = convertDpToPixel(1, context);

            mPath = new Path();
            mPath.addCircle(40 * mPixelMultiplier, 40 * mPixelMultiplier, 45 * mPixelMultiplier, Path.Direction.CCW);
            mPath.addCircle(80 * mPixelMultiplier, 80 * mPixelMultiplier, 45 * mPixelMultiplier, Path.Direction.CCW);
        }

        private void showPath(Canvas canvas, int x, int y, Path.FillType ft, Paint paint) {
            canvas.save();
            canvas.translate(x, y);
            canvas.clipRect(0, 0, 120 * mPixelMultiplier, 120 * mPixelMultiplier);
            canvas.drawColor(Color.WHITE);
            mPath.setFillType(ft);
            canvas.drawPath(mPath, paint);
            canvas.restore();
        }

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


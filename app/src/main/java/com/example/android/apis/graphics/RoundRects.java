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

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.Bundle;
import android.view.*;

/**
 * Shows how to use a GradientDrawable to draw rectangles with rounded corners, and three different
 * types of color gradient: GradientDrawable.LINEAR_GRADIENT, GradientDrawable.RADIAL_GRADIENT, and
 * GradientDrawable.SWEEP_GRADIENT.
 */
public class RoundRects extends GraphicsActivity {

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

    private static class SampleView extends View {
        /**
         * {@code Rect} we use to set the bounds of the {@code GradientDrawable mDrawable} we draw
         */
        private Rect mRect;
        /**
         * {@code GradientDrawable} that we draw using different gradient types.
         */
        private GradientDrawable mDrawable;

        /**
         * Our constructor.
         *
         * @param context {@code Context} to access resources, "this" when called from the {@code onCreate}
         *                method of the activity {@code RoundRects}.
         */
        public SampleView(Context context) {
            super(context);
            setFocusable(true);

            mRect = new Rect(0, 0, 120, 120);

            mDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                    new int[]{0xFFFF0000, 0xFF00FF00, 0xFF0000FF});
            mDrawable.setShape(GradientDrawable.RECTANGLE);
            mDrawable.setGradientRadius((float) (Math.sqrt(2) * 60));
        }

        static void setCornerRadii(GradientDrawable drawable, float r0, float r1, float r2, float r3) {
            drawable.setCornerRadii(new float[]{r0, r0, r1, r1, r2, r2, r3, r3});
        }

        @Override
        protected void onDraw(Canvas canvas) {

            mDrawable.setBounds(mRect);

            float r = 16;

            canvas.save();
            canvas.translate(10, 10);
            mDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
            setCornerRadii(mDrawable, r, r, 0, 0);
            mDrawable.draw(canvas);
            canvas.restore();

            canvas.save();
            canvas.translate(10 + mRect.width() + 10, 10);
            mDrawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
            setCornerRadii(mDrawable, 0, 0, r, r);
            mDrawable.draw(canvas);
            canvas.restore();

            canvas.translate(0, mRect.height() + 10);

            canvas.save();
            canvas.translate(10, 10);
            mDrawable.setGradientType(GradientDrawable.SWEEP_GRADIENT);
            setCornerRadii(mDrawable, 0, r, r, 0);
            mDrawable.draw(canvas);
            canvas.restore();

            canvas.save();
            canvas.translate(10 + mRect.width() + 10, 10);
            mDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
            setCornerRadii(mDrawable, r, 0, 0, r);
            mDrawable.draw(canvas);
            canvas.restore();

            canvas.translate(0, mRect.height() + 10);

            canvas.save();
            canvas.translate(10, 10);
            mDrawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
            setCornerRadii(mDrawable, r, 0, r, 0);
            mDrawable.draw(canvas);
            canvas.restore();

            canvas.save();
            canvas.translate(10 + mRect.width() + 10, 10);
            mDrawable.setGradientType(GradientDrawable.SWEEP_GRADIENT);
            setCornerRadii(mDrawable, 0, r, 0, r);
            mDrawable.draw(canvas);
            canvas.restore();
        }
    }
}

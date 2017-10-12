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
         * Our constructor. First we call our super's constructor, then we enable our view to receive
         * focus. We initialize our field {@code Rect mRect} with a new 120x120 pixel rectangle. We
         * initialize our field {@code GradientDrawable mDrawable} with a new instance whose orientation
         * is TL_BR (draws the gradient from the top-left to the bottom-right) and whose colors array
         * is red, green and blue. We set the shape of {@code mDrawable} to RECTANGLE and set the
         * gradient radius to 60 times the square root of 2.
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

        /**
         * Convenience function for calling {@code setCornerRadii (float[] radii)}. We simply stuff
         * the radius arguments into an anonymous float, repeating the single radius value for both
         * the x and y radius, then call the {@code setCornerRadii} method of our parameter
         * {@code GradientDrawable drawable}.
         *
         * @param drawable {@code GradientDrawable} we are to set the corner radii on
         * @param r0       top-left radius (for both x and y)
         * @param r1       top-right radius (for both x and y)
         * @param r2       bottom-right radius (for both x and y)
         * @param r3       bottom-left radius (for both x and y)
         */
        static void setCornerRadii(GradientDrawable drawable, float r0, float r1, float r2, float r3) {
            drawable.setCornerRadii(new float[]{r0, r0, r1, r1, r2, r2, r3, r3});
        }

        /**
         * We implement this to do our drawing. First we set the bounds (Specify a bounding rectangle)
         * for our field {@code GradientDrawable mDrawable} to our field {@code Rect mRect}. Then we
         * define a constant {@code float r=16} to use for the radii of our {@code mDrawable}. We
         * then proceed to demonstrate 6 different types of gradients, each wrapped between matching
         * saves of the {@code Canvas canvas} current matrix, and restores of that state:
         * <ul>
         * <li>
         * LINEAR_GRADIENT - we move the {@code Canvas} to (10,10), set the gradient type of
         * {@code mDrawable} to LINEAR_GRADIENT, set the corner radii of the top-left and
         * top-right to {@code r}, the other radii to 0 and draw it. This causes a Linear
         * color change from red at the top-left corner, to blue at the bottom-right.
         * </li>
         * <li>
         * RADIAL_GRADIENT - we move the {@code Canvas} to a point 20 pixels to the right of
         * our LINEAR_GRADIENT rectangle, set the gradient type of {@code mDrawable} to
         * RADIAL_GRADIENT, set the corner radii of the bottom-right and bottom-left to
         * {@code r}, the other radii to 0 and draw it. This causes a Linear color change
         * from red at the center of the rectangle to blue at the corners. We interpolate
         * a move of the canvas to 10 pixels below the first row of rectangles now to get
         * ready for the second row.
         * </li>
         * <li>
         * SWEEP_GRADIENT - we move the {@code Canvas} to (10,10), set the gradient type of
         * {@code mDrawable} to SWEEP_GRADIENT, set the corner radii of the top-right and
         * bottom-right to {@code r}, the other radii to 0 and draw it. This creates a
         * circular sweep of blended colour around the rectangle, starting with red at the
         * 0 degree location, transitioning to green, then to blue as it comes back to the
         * 0 degree location.
         * </li>
         * <li>
         * LINEAR_GRADIENT - same as the first LINEAR_GRADIENT except for its location on the
         * second row to the right of the SWEEP_GRADIENT example and the use of rounded corners
         * for the top-left and bottom-left corners. We interpolate a move of the canvas to
         * 10 pixels below the second row of rectangles now to get ready for the third row.
         * </li>
         * <li>
         * RADIAL_GRADIENT - same as the first RADIAL_GRADIENT except for its location on the
         * third row first column, and the use of rounded corners for the top-left and bottom-right
         * corners.
         * </li>
         * <li>
         * SWEEP_GRADIENT - same as the first SWEEP_GRADIENT except for its location on the
         * third row second column, and the use of rounded corners for the top-right and bottom-left
         * corners.
         * </li>
         * </ul>
         *
         * @param canvas the canvas on which the background will be drawn
         */
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

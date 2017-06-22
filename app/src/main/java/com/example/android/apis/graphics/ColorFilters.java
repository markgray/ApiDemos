/*
 * Copyright (C) 2009 The Android Open Source Project
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

import com.example.android.apis.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.Bundle;
import android.util.Log;
import android.view.*;

/**
 * Applies a PorterDuffColorFilter to tint some button like drawables using several
 * different colors, and both PorterDuff.Mode.SRC_ATOP, and PorterDuff.Mode.MULTIPLY
 * PorterDuff modes to apply them. Oddly only froyo spaces the drawables properly
 * probably due to the use of drawable-mdpi and drawable-hdpi versions. Checking
 * Drawable.getIntrinsicHeight() and modifying the spacing for hdpi fixes the problem.
 */
public class ColorFilters extends GraphicsActivity {
    /**
     * TAG used for logging
     */
    public static final String TAG = "ColorFilters";

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
     * View that displays 7 rows of the same 4 buttons, with each row using different Porter-Duff
     * color filters
     */
    private static class SampleView extends View {
        /**
         * {@code Activity} used to construct us, we use it to set the title bar of its window
         */
        private Activity mActivity;
        /**
         * The resource button R.drawable.btn_default_normal
         */
        private Drawable mDrawable;
        /**
         * The three resource buttons R.drawable.btn_circle_normal, R.drawable.btn_check_off, and
         * R.drawable.btn_check_on
         */
        private Drawable[] mDrawables;
        /**
         * {@code Paint} used to draw the text "Label" inside the button {@code Drawable mDrawable},
         * it alternates between 0xFF000000 (Black) and 0xFFFFFFFF (White), and is used in conjunction
         * with its opposite color {@code Paint mPaint2} with {@code mPaint2} being drawn first offset
         * by (1,1) and {@code mPaint} being drawn second.
         */
        private Paint mPaint;
        /**
         * {@code Paint} used to draw the text "Label" inside the button {@code Drawable mDrawable},
         * it alternates between 0xFF000000 (Black) and 0xFFFFFFFF (White), and is used in conjunction
         * with its opposite color {@code Paint mPaint} with {@code mPaint2} being drawn first offset
         * by (1,1) and {@code mPaint} being drawn second.
         */
        private Paint mPaint2;
        /**
         * 1/2 of the sum of the ascent and descent of the FontMetrics of text at size 16, it is used
         * to position the text "Label" when drawing it into {@code Drawable mDrawable}.
         */
        private float mPaintTextOffset;
        /**
         * An array containing the colors 0, 0xCC0000FF, 0x880000FF, 0x440000FF, 0xFFCCCCFF, 0xFF8888FF,
         * and 0xFF4444FF. They are used as an argument to construct a {@code PorterDuffColorFilter}
         * used for each of the 7 different rows. The other argument is from {@code PorterDuff.Mode[] mModes}
         */
        private int[] mColors;
        /**
         * The two PortDuff modes PorterDuff.Mode.SRC_ATOP, and PorterDuff.Mode.MULTIPLY, They are
         * used as an argument to construct the {@code PorterDuffColorFilter} for all the rows of
         * buttons displayed except the one using the color 0 (the first row which uses null as the
         * color filter). The mode alternates every second touch event based on {@code mModeIndex}.
         */
        private PorterDuff.Mode[] mModes;
        /**
         * Index into {@code PorterDuff.Mode[] mModes} used to choose which Porter-Duff mode to use
         * it is incremented modulo the length of {@code mModes} every second time the canvas is
         * touched.
         */
        private int mModeIndex;
        /**
         * The height of each row of buttons, used to translate the canvas to the row to be drawn
         */
        private int mHeightOffset;

        /**
         * Specify a bounding rectangle for the {@code Drawable curr} to the right of {@code Drawable prev}.
         * This is where the drawable will draw when its draw() method is called. First we fetch the
         * bounding rectangle for {@code prev} to {@code Rect r}. We calculate {@code int x} to be
         * 12 pixels further to the right from {@code r.right} of {@code prev}'s bounding rectangle. We
         * calculate {@code int center} midway between the top and bottom of {@code prev}'s bounding rectangle.
         * We fetch the intrinsic height of {@code curr} to {@code int h}, and then calculate the {@code int y}
         * position we want to use for the top of {@code curr}'s bounding rectangle to be half of {@code h}
         * higher than {@code center}. Finally we set the bounding rectangle for {@code curr} to be
         * (x,y,x+(intrinsic width of {@code curr},y+h) (left,top,right,bottom)
         *
         * @param curr {@code Drawable} we will set the bounds for
         * @param prev the {@code Drawable} that was positioned previously, used as the starting point
         *             for placing {@code curr}
         */
        private static void addToTheRight(Drawable curr, Drawable prev) {
            Rect r = prev.getBounds();
            int x = r.right + 12;
            int center = (r.top + r.bottom) >> 1;
            int h = curr.getIntrinsicHeight();
            int y = center - (h >> 1);

            curr.setBounds(x, y, x + curr.getIntrinsicWidth(), y + h);
        }

        /**
         * Constructs and initializes an instance of {@code SampleView}. First we call through to our
         * super's constructor. Then we initialize our field {@code Activity mActivity} with our
         * parameter {@code Activity activity}, and (for no good reason) we also set our variable
         * {@code Context context} to it. We enable our view to receive focus.
         *
         * @param activity used for the {@code Context} when fetching resources, "this" when called
         *                 from {@code onCreate} of our activity.
         */
        public SampleView(Activity activity) {
            super(activity);
            mActivity = activity;
            @SuppressWarnings("UnnecessaryLocalVariable")
            Context context = activity;
            setFocusable(true);

            //noinspection deprecation
            mDrawable = context.getResources().getDrawable(R.drawable.btn_default_normal);
            mHeightOffset = 55;
            //noinspection ConstantConditions
            int heightOfDrawable = mDrawable.getIntrinsicHeight();
            Log.i(TAG, "Height of drawable: " + heightOfDrawable);
            if (heightOfDrawable > 55) {
                mHeightOffset = heightOfDrawable + 5;
            }
            //noinspection ConstantConditions
            mDrawable.setBounds(0, 0, 150, 48);
            //noinspection deprecation
            mDrawable.setDither(true);

            int[] resIDs = new int[]{
                    R.drawable.btn_circle_normal,
                    R.drawable.btn_check_off,
                    R.drawable.btn_check_on
            };
            mDrawables = new Drawable[resIDs.length];
            Drawable prev = mDrawable;
            for (int i = 0; i < resIDs.length; i++) {
                //noinspection deprecation
                mDrawables[i] = context.getResources().getDrawable(resIDs[i]);
                //noinspection deprecation
                mDrawables[i].setDither(true);
                addToTheRight(mDrawables[i], prev);
                prev = mDrawables[i];
            }

            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setTextSize(16);
            mPaint.setTextAlign(Paint.Align.CENTER);

            mPaint2 = new Paint(mPaint);
            mPaint2.setAlpha(64);

            Paint.FontMetrics fm = mPaint.getFontMetrics();
            mPaintTextOffset = (fm.descent + fm.ascent) * 0.5f;

            mColors = new int[]{
                    0,
                    0xCC0000FF,
                    0x880000FF,
                    0x440000FF,
                    0xFFCCCCFF,
                    0xFF8888FF,
                    0xFF4444FF,
            };

            mModes = new PorterDuff.Mode[]{
                    PorterDuff.Mode.SRC_ATOP,
                    PorterDuff.Mode.MULTIPLY,
            };
            mModeIndex = 0;

            updateTitle();
        }

        private void swapPaintColors() {
            if (mPaint.getColor() == 0xFF000000) {
                mPaint.setColor(0xFFFFFFFF);
                mPaint2.setColor(0xFF000000);
            } else {
                mPaint.setColor(0xFF000000);
                mPaint2.setColor(0xFFFFFFFF);
            }
            mPaint2.setAlpha(64);
        }

        private void updateTitle() {
            mActivity.setTitle(mModes[mModeIndex].toString());
        }

        private void drawSample(Canvas canvas, ColorFilter filter) {
            Rect r = mDrawable.getBounds();
            float x = (r.left + r.right) * 0.5f;
            float y = (r.top + r.bottom) * 0.5f - mPaintTextOffset;

            mDrawable.setColorFilter(filter);
            mDrawable.draw(canvas);
            canvas.drawText("Label", x + 1, y + 1, mPaint2);
            canvas.drawText("Label", x, y, mPaint);

            for (Drawable dr : mDrawables) {
                dr.setColorFilter(filter);
                dr.draw(canvas);
            }
        }

        @SuppressLint("DrawAllocation")
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(0xFFCCCCCC);

            canvas.translate(8, mHeightOffset);
            for (int color : mColors) {
                ColorFilter filter;
                if (color == 0) {
                    filter = null;
                } else {
                    filter = new PorterDuffColorFilter(color, mModes[mModeIndex]);
                }
                drawSample(canvas, filter);
                canvas.translate(0, mHeightOffset);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    // update mode every other time we change paint colors
                    if (mPaint.getColor() == 0xFFFFFFFF) {
                        mModeIndex = (mModeIndex + 1) % mModes.length;
                        updateTitle();
                    }
                    swapPaintColors();
                    invalidate();
                    break;
            }
            return true;
        }
    }
}

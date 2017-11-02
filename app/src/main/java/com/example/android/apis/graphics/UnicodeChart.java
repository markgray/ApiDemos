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
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;

/**
 * Draws a unicode chart, and if you had a dpad it would draw more than one chart. Way too small on
 * the newer devices (froyo OK).
 */
public class UnicodeChart extends GraphicsActivity {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we request the window feature FEATURE_NO_TITLE (turns off the title at
     * the top of the screen), and finally we set our content view to a new instance of {@code SampleView}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(new SampleView(this));
    }

    /**
     * Simple custom {@code View} which fills its canvas with a unicode chart.
     */
    private static class SampleView extends View {
        /**
         * The logical density of the display. This is a scaling factor for the Density Independent
         * Pixel unit, where one DIP is one pixel on an approximately 160 dpi screen (for example a
         * 240x320, 1.5"x2" screen), providing the baseline of the system's display. Thus on a 160dpi
         * screen this density value will be 1; on a 120 dpi screen it would be .75; etc.
         */
        private float SCREEN_DENSITY;
        /**
         * {@code Paint} used to draw the unicode characters in our chart, text size is 15
         */
        private Paint mBigCharPaint;
        /**
         * {@code Paint} used to draw the hex values of the characters under the unicode characters
         */
        private Paint mLabelPaint;
        /**
         * The 256 characters in the current chart.
         */
        private final char[] mChars = new char[256];
        /**
         * x and y coordinates of the characters in the chart.
         */
        private final float[] mPos = new float[512];

        /**
         * Which chart is to be shown (it becomes the upper byte of the unicode characters drawn)
         */
        private int mBase;

        /**
         * Size of the character cell in the X dimension.
         */
        private static final int XMUL = 20;
        /**
         * Size of the character cell in the Y dimension.
         */
        private static final int YMUL = 28;
        /**
         * Offset from the top of the screen for determining Y coordinate of the characters.
         */
        private static final int YBASE = 18;

        /**
         * Our constructor. First we enable our view to receive focus, and to receive focus in touch
         * mode. We initialize {@code SCREEN_DENSITY} to the logical density of the screen. We allocate
         * a new instance of {@code Paint} with its ANTI_ALIAS_FLAG flag set for our field
         * {@code Paint mBigCharPaint}, set its text size to 15, and set its text alignment to CENTER.
         * We allocate a new instance of {@code Paint} with its ANTI_ALIAS_FLAG flag set for our field
         * {@code Paint mLabelPaint}, set its text size to 8, and set its text alignment to CENTER.
         * <p>
         * We copy the {@code mPos} pointer to {@code float[] pos}, and initialize {@code int index}
         * to 0. Then we loop through the 16 columns and 16 rows calculating the value of the {@code x}
         * coordinate for the characters in the current column, before looping through the rows where
         * we set the {@code index} entry in {@code pos} to {@code x}, incrementing {@code index}, setting
         * the following entry to the Y coordinate for the current row and incrementing {@code index}.
         * The end result is to fill the {@code pos} (and hence the {@code mPos}) array with (x,y)
         * coordinate pairs for each of the 256 characters displayed in our chart, going down the rows
         * first, and then moving to the next column and going down the rows again etc.
         * <p>
         * Finally we set our {@code OnClickListener} to an anonymous class which advances to the next
         * page in the unicode space.
         *
         * @param context {@code Context} for accessing resources, "this" when called from the
         *                {@code onCreate} method of {@code UnicodeChart}
         */
        public SampleView(Context context) {
            super(context);
            setFocusable(true);
            setFocusableInTouchMode(true);

            SCREEN_DENSITY = getResources().getDisplayMetrics().density;
            mBigCharPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mBigCharPaint.setTextSize(15);
            mBigCharPaint.setTextAlign(Paint.Align.CENTER);

            mLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mLabelPaint.setTextSize(8);
            mLabelPaint.setTextAlign(Paint.Align.CENTER);

            // the position array is the same for all charts
            float[] pos = mPos;
            int index = 0;
            for (int col = 0; col < 16; col++) {
                final float x = col * XMUL + 10;
                for (int row = 0; row < 16; row++) {
                    pos[index++] = x;
                    pos[index++] = row * YMUL + YBASE;
                }
            }
            setOnClickListener(new OnClickListener() {
                /**
                 *
                 *
                 * @param v {@code View} that has been clicked
                 */
                @Override
                public void onClick(View v) {
                    mBase = (mBase + 1) % 256;
                    invalidate();
                }
            });
        }

        /**
         * Computes and returns the X coordinate to place the hex version of the character on the screen.
         *
         * @param index index of the character being considered.
         * @return X coordinate to place the hex version of the character on the screen
         */
        private float computeX(int index) {
            return (index >> 4) * XMUL + 10;
        }

        /**
         * Computes and returns the Y coordinate to place the hex version of the character on the screen.
         *
         * @param index index of the character being considered.
         * @return Y coordinate to place the hex version of the character on the screen
         */
        private float computeY(int index) {
            return (index & 0xF) * YMUL + YMUL;
        }

        /**
         * Draws the unicode chart for the characters in page {@code base}. First we copy the {@code mChars}
         * pointer to {@code char[] chars}, then for each of the 256 characters {@code i} in the current page
         * we form the unicode value {@code int unichar} by adding {@code base} to {@code i} and cast
         * {@code unichar} to {@code char} to set {@code chars[i]}, then draw the hex version of {@code unichar}
         * underneath the location where the unicode character will be placed using {@code mLabelPaint} as
         * the {@code Paint}.
         * <p>
         * Having filled {@code chars} and labeled the grid, we now draw the unicode characters in {@code chars}
         * using {@code mPos} to position them, and {@code mBigCharPaint} as the {@code Paint}.
         *
         * @param canvas {@code Canvas} we are to draw to
         * @param base   which page we are to draw.
         */
        private void drawChart(Canvas canvas, int base) {
            char[] chars = mChars;
            for (int i = 0; i < 256; i++) {
                int unichar = base + i;
                chars[i] = (char) unichar;

                canvas.drawText(Integer.toHexString(unichar), computeX(i), computeY(i), mLabelPaint);
            }
            //noinspection deprecation
            canvas.drawPosText(chars, 0, 256, mPos, mBigCharPaint);
        }

        /**
         * We implement this to do our drawing. First we fill the entire {@code Canvas canvas} with the
         * color WHITE, scale the canvas by the screen density, move it to (0,1), and then call our
         * method {@code drawChart} to draw the unicode characters in page {@code mBase} (where a page
         * is defined as 256 adjacent code points) on {@code Canvas canvas}.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.WHITE);

            canvas.scale(SCREEN_DENSITY, SCREEN_DENSITY);
            canvas.translate(0, 1);
            drawChart(canvas, mBase * 256);
        }

        /**
         * Called when a key down event has occurred. We switch on the value of {@code keyCode}:
         * <ul>
         * <li>
         * KEYCODE_DPAD_LEFT - if {@code mBase} is greater than 0, we decrement it, invalidate
         * the view so it will drawn again, and return true
         * </li>
         * <li>
         * KEYCODE_DPAD_RIGHT - we increment {@code mBase}, invalidate the view so it will
         * drawn again, and return true
         * </li>
         * </ul>
         * If the {@code keyCode} is neither KEYCODE_DPAD_LEFT nor KEYCODE_DPAD_RIGHT, we return the
         * value returned by our super's implementation of {@code onKeyDown}.
         *
         * @param keyCode The value in event.getKeyCode().
         * @param event   Description of the key event.
         * @return If you handled the event, return true.  If you want to allow
         * the event to be handled by the next receiver, return false.
         */
        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if (mBase > 0) {
                        mBase -= 1;
                        invalidate();
                    }
                    return true;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    mBase += 1;
                    invalidate();
                    return true;
                default:
                    break;
            }
            return super.onKeyDown(keyCode, event);
        }
    }
}

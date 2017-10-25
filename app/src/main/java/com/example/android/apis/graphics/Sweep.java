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
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

/**
 * Animates a SweepGradient Shader by rotating its setLocalMatrix(Matrix M), used to paint a circle.
 */
public class Sweep extends GraphicsActivity {

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
     * Our custom view, which simply draws a circle with an animated {@code SweepGradient} as its
     * {@code Shader}.
     */
    private static class SampleView extends View {
        /**
         * The colors used to create our {@code SweepGradient}.
         */
        public static final int[] COLORS = {Color.GREEN, Color.RED, Color.BLUE, Color.GREEN};
        /**
         * {@code Paint} used to draw our circle.
         */
        private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        /**
         * Angle in degrees to rotate our {@code Shader mShader}, advanced by 3 degrees every time
         * our {@code onDraw} method is called.
         */
        private float mRotate;
        /**
         * {@code Matrix} we use to rotate our {@code Shader mShader}.
         */
        private Matrix mMatrix = new Matrix();
        /**
         * {@code SweepGradient} we use as {@code Shader} for our {@code Paint mPaint}.
         */
        private Shader mShader;
        /**
         * Flag that enables timing how long it takes to draw our circle 20 times (can only be toggled
         * if you have a keyboard).
         */
        private boolean mDoTiming;

        /**
         * Our constructor. First we call through to our super's constructor, then we enable our view
         * to receive focus, and to receive focus in touch mode. We initialize {@code x} to 160 and
         * {@code y} to 100 and use them to create a {@code SweepGradient} to initialize our field
         * {@code Shader mShader} with the center at (x,y), using the array {@code COLORS} as the
         * colors to be evenly distributed around the center. Finally we set {@code mShader} as the
         * shader of {@code Paint mPaint}.
         *
         * @param context {@code Context} to use to access resources.
         */
        public SampleView(Context context) {
            super(context);
            setFocusable(true);
            setFocusableInTouchMode(true);

            float x = 160;
            float y = 100;
            mShader = new SweepGradient(x, y, COLORS, null);
            mPaint.setShader(mShader);
        }

        /**
         * We implement this to do our drawing. First we make a local copy of {@code Paint mPaint}
         * for {@code Paint paint}, initialize {@code x} to 160, and {@code y} to 100. We set the
         * color of the entire {@code Canvas canvas} to WHITE. We set {@code Matrix mMatrix} to rotate
         * by {@code mRotate} degrees around the point (x,y), and set the local matrix of {@code mShader}
         * to it, then set the shader of {@code Paint mPaint} to {@code mShader}. We increment
         * {@code mRotate} by 3 degrees and if the result is greater than or equal to 360 we set it
         * to 0. We then invalidate the view so we will be called again sometime in the future.
         *
         * If our {@code mDoTiming} flag is true, we set {@code now} to the current time in milliseconds
         * since boot, loop drawing our circle 20 times, calculate how long this took and log the result.
         * If {@code mDoTiming} is false we simply use {@code Paint paint} to draw a circle with a
         * radius of 80 pixels around the point (x,y).
         *
         * @param canvas the canvas on which the background will be drawn
         */
        @Override
        protected void onDraw(Canvas canvas) {
            Paint paint = mPaint;
            float x = 160;
            float y = 100;

            canvas.drawColor(Color.WHITE);

            mMatrix.setRotate(mRotate, x, y);
            mShader.setLocalMatrix(mMatrix);
            mPaint.setShader(mShader);
            mRotate += 3;
            if (mRotate >= 360) {
                mRotate = 0;
            }
            invalidate();

            if (mDoTiming) {
                long now = System.currentTimeMillis();
                for (int i = 0; i < 20; i++) {
                    canvas.drawCircle(x, y, 80, paint);
                }
                now = System.currentTimeMillis() - now;
                android.util.Log.d("skia", "sweep ms = " + (now / 20.));
            } else {
                canvas.drawCircle(x, y, 80, paint);
            }
        }

        /**
         * Called when a key down event has occurred. If the key is KEYCODE_D, we toggle the DITHER_FLAG
         * of our {@code Paint mPaint}, invalidate the view and return true. If the key is KEYCODE_T,
         * we toggle our {@code mDoTiming} flag, invalidate the view and return true. Otherwise we
         * return whatever our super's implementation of {@code onKeyDown} returns.
         *
         * @param keyCode A key code that represents the button pressed.
         * @param event   The KeyEvent object that defines the button action.
         * @return true if we have consumed the {@code KeyEvent}.
         */
        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_D:
                    mPaint.setDither(!mPaint.isDither());
                    invalidate();
                    return true;
                case KeyEvent.KEYCODE_T:
                    mDoTiming = !mDoTiming;
                    invalidate();
                    return true;
            }
            return super.onKeyDown(keyCode, event);
        }
    }
}


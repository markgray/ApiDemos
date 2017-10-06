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
import android.view.MotionEvent;
import android.view.*;

/**
 * Creates two Bitmap's: a blue rectangle on a red background, and a green circle on a clear background.
 * It uses these Bitmap's to make two BitmapShader's, and it rotates the Circle BitmapShader by 30
 * degrees. In the onDraw method it first draws using the rectangle pattern, translate's the Canvas
 * based on the current MotionEvent movement and draws using the circle pattern. The effect is to
 * allow you to move the circle pattern with your finger while leaving the rectangle pattern stationary
 * and partially visible through the circle pattern on top.
 */
public class Patterns extends GraphicsActivity {

    /**
     * Called when the activity is starting. First we call our super's implementation of {@code onCreate},
     * then we set our content view to a new instance of {@code SampleView}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SampleView(this));
    }

    /**
     * Creates and returns a 40x40 pixel {@code Bitmap} containing a single blue 30x30 rectangle at
     * the center of its red background. First we create a new instance of a 40x40 {@code Bitmap}
     * for {@code Bitmap bm}. We create a {@code Canvas c} that uses {@code bm} to draw into and set
     * the entire canvas to RED. We create a new instance of {@code Paint} for {@code Paint p} and set
     * its color to BLUE. We use {@code p} to draw a rectangle on {@code c} whose top left corner is
     * at (5,5), and whose bottom right corner is at (35,35). We then return the {@code Bitmap bm} that
     * now contains that rectangle.
     *
     * @return 40x40 pixel {@code Bitmap} containing a single blue 30x30 rectangle at the center of
     * its red background.
     */
    private static Bitmap makeBitmap1() {
        Bitmap bm = Bitmap.createBitmap(40, 40, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bm);
        c.drawColor(Color.RED);
        Paint p = new Paint();
        p.setColor(Color.BLUE);
        c.drawRect(5, 5, 35, 35, p);
        return bm;
    }

    /**
     * Creates and returns a 64x64 pixel {@code Bitmap} containing a single GREEN circle of radius 27
     * at the center of its uncolored background. First we create a new instance of a 64x64 {@code Bitmap}
     * for {@code Bitmap bm}. We create a {@code Canvas c} that uses {@code bm} to draw into. We create a
     * new instance of {@code Paint} for {@code Paint p} and set its color to GREEN, and its alpha to
     * 0xCC. We use {@code p} to draw a circle on {@code c} whose center is at (32,32) and whose radius
     * is 27. We then return the {@code Bitmap bm} that now contains that circle.
     *
     * @return 64x64 pixel {@code Bitmap} containing a single GREEN circle of radius 27 at the center
     * of its uncolored background.
     */
    private static Bitmap makeBitmap2() {
        Bitmap bm = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.GREEN);
        p.setAlpha(0xCC);
        c.drawCircle(32, 32, 27, p);
        return bm;
    }

    /**
     * A custom {@code View} consisting of two {@code Shader} objects, one stationary consisting of
     * a rectangle pattern, and one movable on top of it consisting of a circle pattern.
     */
    private static class SampleView extends View {
        /**
         * {@code Shader} consisting of a pattern of blue rectangles on a red background.
         */
        private final Shader mShader1;
        /**
         * {@code Shader} consisting of a pattern of green circles on an uncolored background.
         */
        private final Shader mShader2;
        /**
         * {@code Paint} used to draw to our {@code Canvas}
         */
        private final Paint mPaint;
        /**
         * {@code PaintFlagsDrawFilter} that clears the Paint.FILTER_BITMAP_FLAG and Paint.DITHER_FLAG
         * of the {@code Paint} used to draw to the {@code Canvas} when it is set as the draw filter
         * of that {@code Canvas}. {@code DrawFilter mDF} is set to it when an ACTION_DOWN touch event
         * is received (and {@code mDF} is set to null on an ACTION_UP event). See {@code mDF}.
         */
        private final DrawFilter mFastDF;

        /**
         * x coordinate of the last ACTION_DOWN event
         */
        private float mTouchStartX;
        /**
         * y coordinate of the last ACTION_DOWN event
         */
        private float mTouchStartY;
        /**
         * x coordinate of the last ACTION_MOVE event
         */
        private float mTouchCurrX;
        /**
         * y coordinate of the last ACTION_MOVE event
         */
        private float mTouchCurrY;
        /**
         * {@code DrawFilter} that is used as the draw filter of the {@code Canvas} we are drawing to.
         * It is set to {@code DrawFilter mFastDF} on an ACTION_DOWN event and to null on an ACTION_UP
         * event.
         */
        private DrawFilter mDF;

        /**
         * Our constructor. First we call our super's constructor, then we enable our view to receive
         * focus, and to receive focus in touch mode. We initialize our field {@code DrawFilter mFastDF}
         * with a new instance of {@code PaintFlagsDrawFilter} configured to clear the {@code Paint}
         * flags Paint.FILTER_BITMAP_FLAG and Paint.DITHER_FLAG if it is set as the draw filter of a
         * canvas. We initialize our field {@code Shader mShader1} with a new instance of
         * {@code BitmapShader} created using the {@code Bitmap} returned from our method {@code makeBitmap1}
         * (a blue rectangle with a red background) and configured to repeat in both the x and y
         * directions. We initialize our field {@code Shader mShader2} with a new instance of
         * {@code BitmapShader} created using the {@code Bitmap} returned from our method {@code makeBitmap2}
         * (a green circle with an uncolored background) and configured to repeat in both the x and y
         * directions.
         * <p>
         * We create a new instance for {@code Matrix m}, set the matrix to rotate about (0,0) by
         * 30 degrees, and use it to set the local matrix of {@code Shader mShader2} (rotates the
         * pattern by 30 degrees when it is drawn).
         * <p>
         * Finally we allocate a new instance of {@code Paint} for our field {@code Paint mPaint},
         * setting the Paint.FILTER_BITMAP_FLAG on it (enables bilinear sampling on scaled bitmaps).
         *
         * @param context {@code Context} to use to access resources, "this" when called from the
         *                {@code onCreate} method of the {@code Patterns} activity.
         */
        public SampleView(Context context) {
            super(context);
            setFocusable(true);
            setFocusableInTouchMode(true);

            mFastDF = new PaintFlagsDrawFilter(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG, 0);

            mShader1 = new BitmapShader(makeBitmap1(), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            mShader2 = new BitmapShader(makeBitmap2(), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

            Matrix m = new Matrix();
            m.setRotate(30);
            mShader2.setLocalMatrix(m);

            mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        }

        /**
         * We implement this to do our drawing. First we set the draw filter of our parameter
         * {@code Canvas canvas} to our field {@code DrawFilter mDF} (this will either be the
         * contents of our field {@code DrawFilter mFastDF} after an ACTION_DOWN event or null
         * after an ACTION_UP event). We set the {@code Shader} object of {@code Paint mPaint} to
         * {@code mShader1} (blue rectangles on a red background) and fill the {@code Canvas canvas}
         * bitmap with that pattern. Then we move the {@code Canvas} by the movement in x and y implied
         * by the last ACTION_MOVE event ({@code mTouchCurrX - mTouchStartX} in the x direction and
         * {@code mTouchCurrY - mTouchStartY} in the y direction). We set the {@code Shader} object
         * of {@code Paint mPaint} to {@code mShader2} (green circles on an uncolored background) and
         * fill the {@code Canvas canvas} bitmap with that pattern. The rectangle pattern of
         * {@code mShader1} will show through the uncolored background of {@code mShader1}.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.setDrawFilter(mDF);

            mPaint.setShader(mShader1);
            canvas.drawPaint(mPaint);

            canvas.translate(mTouchCurrX - mTouchStartX,
                    mTouchCurrY - mTouchStartY);

            mPaint.setShader(mShader2);
            canvas.drawPaint(mPaint);
        }

        /**
         * Implement this method to handle touch screen motion events. First we fetch the x coordinate
         * of the {@code MotionEvent event} to {@code float x} and the y coordinate to {@code float y}.
         * Then we switch based on the kind of action of the {@code event}:
         * <ul>
         * <li>
         * ACTION_DOWN - We set our fields {@code mTouchStartX} and {@code mTouchCurrX} to x
         * and our fields {@code mTouchStartY} and {@code mTouchCurrY}. We set our field
         * {@code DrawFilter mDF} to the contents of {@code DrawFilter mFastDF}, and invalidate
         * our view so that our {@code onDraw} method will be called.
         * </li>
         * <li>
         * ACTION_MOVE - We set our field {@code mTouchCurrX} to x, and {@code mTouchCurrY}
         * to y and invalidate our view so that our {@code onDraw} method will be called.
         * </li>
         * <li>
         * ACTION_UP - We set our field {@code DrawFilter mDF} to null and invalidate our
         * view so that our {@code onDraw} method will be called.
         * </li>
         * </ul>
         * In all cases we return true to the called to indicate that we handled the {@code MotionEvent}.
         *
         * @param event The motion event.
         * @return True if the event was handled, false otherwise. We always return true.
         */
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mTouchStartX = mTouchCurrX = x;
                    mTouchStartY = mTouchCurrY = y;
                    mDF = mFastDF;
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    mTouchCurrX = x;
                    mTouchCurrY = y;
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    mDF = null;
                    invalidate();
                    break;
            }
            return true;
        }
    }
}


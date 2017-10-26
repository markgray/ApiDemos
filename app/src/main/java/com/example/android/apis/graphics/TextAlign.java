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
import android.view.*;

/**
 * Shows how to position text drawn to a Canvas using Paint.setTextAlign, Canvas.drawPosText, and
 * along an arbitrary path using Canvas.drawTextOnPath
 */
public class TextAlign extends GraphicsActivity {

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
     * Our custom View, it simply displays our text samples.
     */
    private static class SampleView extends View {
        /**
         * {@code Paint} we use to draw our text and some spacing lines with.
         */
        private Paint mPaint;
        /**
         * X coordinate of the center of the screen.
         */
        private float mX;
        /**
         * x and y coordinates of each character in our "Positioned" text sample.
         */
        private float[] mPos;

        /**
         * {@code Path} we use for our "Along a path" text sample.
         */
        private Path mPath;
        /**
         * {@code Paint} we use to draw the {@code Path} under our "Along a path" text sample.
         */
        private Paint mPathPaint;

        /**
         * Distance between lines of text.
         */
        private static final int DY = 30;
        /**
         * String used for the "Left" aligned text.
         */
        private static final String TEXT_L = "Left";
        /**
         * String used for the "Center" aligned text.
         */
        private static final String TEXT_C = "Center";
        /**
         * String used for the "Right" aligned text.
         */
        private static final String TEXT_R = "Right";
        /**
         * String used for the "Positioned" text.
         */
        private static final String POSTEXT = "Positioned";
        /**
         * String used for the "Along a path" text sample.
         */
        private static final String TEXTONPATH = "Along a path";

        /**
         * Our constructor. First we call our super's constructor, and then enable our view to receive
         * focus. We initialize our field {@code Paint mPaint} with a new instance of {@code Paint},
         * set its anti alias flag to true, its text size to 30, and its typeface to SERIF. We initialize
         * our field {@code float[] mPos} with the character positions that the characters in the
         * string POSTEXT ("Positioned") when it is drawn. We allocate a new instance of {@code Path}
         * for {@code Path mPath} and call our method {@code makePath} to fill {@code mPath} with a
         * cubic bezier from (10,0) to (300,0). Then we allocate a new instance of {@code Paint} for
         * {@code Paint mPathPaint}, set its anti alias flag to true, set its color to blue (with an
         * alpha of 0x80), and set its style to STROKE.
         *
         * @param context {@code Content} to access resources, "this" in the {@code onCreate} method
         *                of {@code TextAlign}.
         */
        public SampleView(Context context) {
            super(context);
            setFocusable(true);

            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setTextSize(30);
            mPaint.setTypeface(Typeface.SERIF);

            mPos = buildTextPositions(POSTEXT, 0, mPaint);

            mPath = new Path();
            makePath(mPath);

            mPathPaint = new Paint();
            mPathPaint.setAntiAlias(true);
            mPathPaint.setColor(0x800000FF);
            mPathPaint.setStyle(Paint.Style.STROKE);
        }

        /**
         * Fills its parameter {@code Path p} with a cubic bezier from (10,0) to (300,0) using the
         * control points (100,-50) and (200,50).
         *
         * @param p {@code Path} we are to initialize.
         */
        private static void makePath(Path p) {
            p.moveTo(10, 0);
            p.cubicTo(100, -50, 200, 50, 300, 0);
        }

        /**
         * Creates and returns an array of x, y coordinates for drawing each individual character in
         * the parameter {@code String text}.
         *
         * @param text  String whose characters we are to determine the position of.
         * @param y     y coordinate position for all the characters.
         * @param paint {@code Paint} to use to get the text widths of the characters in {@code text}
         * @return An array of x,y coordinates for the position of each character in {@code text}
         * when is is drawn.
         */
        @SuppressWarnings("SameParameterValue")
        private float[] buildTextPositions(String text, float y, Paint paint) {
            float[] widths = new float[text.length()];
            // initially get the widths for each char
            int n = paint.getTextWidths(text, widths);
            // now populate the array, interleaving spaces for the Y values
            float[] pos = new float[n * 2];
            float accumulatedX = 0;
            for (int i = 0; i < n; i++) {
                //noinspection PointlessArithmeticExpression
                pos[i * 2 + 0] = accumulatedX;
                pos[i * 2 + 1] = y;
                accumulatedX += widths[i];
            }
            return pos;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.WHITE);

            Paint p = mPaint;
            float x = mX;
            float y = 0;
            float[] pos = mPos;

            // draw the normal strings

            p.setColor(0x80FF0000);
            canvas.drawLine(x, y, x, y + DY * 3, p);
            p.setColor(Color.BLACK);

            canvas.translate(0, DY);
            p.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(TEXT_L, x, y, p);

            canvas.translate(0, DY);
            p.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(TEXT_C, x, y, p);

            canvas.translate(0, DY);
            p.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(TEXT_R, x, y, p);

            canvas.translate(100, DY * 2);

            // now draw the positioned strings

            p.setColor(0xBB00FF00);
            for (int i = 0; i < pos.length / 2; i++) {
                //noinspection PointlessArithmeticExpression
                canvas.drawLine(pos[i * 2 + 0], pos[i * 2 + 1] - DY,
                        pos[i * 2 + 0], pos[i * 2 + 1] + DY * 2, p);
            }
            p.setColor(Color.BLACK);

            p.setTextAlign(Paint.Align.LEFT);
            //noinspection deprecation
            canvas.drawPosText(POSTEXT, pos, p);

            canvas.translate(0, DY);
            p.setTextAlign(Paint.Align.CENTER);
            //noinspection deprecation
            canvas.drawPosText(POSTEXT, pos, p);

            canvas.translate(0, DY);
            p.setTextAlign(Paint.Align.RIGHT);
            //noinspection deprecation
            canvas.drawPosText(POSTEXT, pos, p);

            // now draw the text on path

            canvas.translate(-100, DY * 2);

            canvas.drawPath(mPath, mPathPaint);
            p.setTextAlign(Paint.Align.LEFT);
            canvas.drawTextOnPath(TEXTONPATH, mPath, 0, 0, p);

            canvas.translate(0, DY * 1.5f);
            canvas.drawPath(mPath, mPathPaint);
            p.setTextAlign(Paint.Align.CENTER);
            canvas.drawTextOnPath(TEXTONPATH, mPath, 0, 0, p);

            canvas.translate(0, DY * 1.5f);
            canvas.drawPath(mPath, mPathPaint);
            p.setTextAlign(Paint.Align.RIGHT);
            canvas.drawTextOnPath(TEXTONPATH, mPath, 0, 0, p);
        }

        @Override
        protected void onSizeChanged(int w, int h, int ow, int oh) {
            super.onSizeChanged(w, h, ow, oh);
            mX = w * 0.5f;  // remember the center of the screen
        }
    }
}


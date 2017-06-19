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
     * Custom View which draws the same scene 6 times using different clip settings for the
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
         * Basic constructor for our class. First we call our super's constructor, then we enable our
         * view to receive focus. We allocate an instance of {@code Paint} for our field {@code Paint mPaint},
         * set the ANTI_ALIAS_FLAG to true to enable antialiasing, set the stroke width to 6, the text
         * size to 16, and set the text alignment to be Paint.Align.RIGHT. Finally we allocate a new
         * instance of {@code Path} to initialize our field {@code Path mPath}.
         *
         * @param context the {@code Context} to use to retrieve resources, "this" when called from
         *                {@code onCreate} override of our activity.
         */
        public SampleView(Context context) {
            super(context);
            setFocusable(true);

            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setStrokeWidth(6);
            mPaint.setTextSize(16);
            mPaint.setTextAlign(Paint.Align.RIGHT);

            mPath = new Path();
        }

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

        @Override
        protected void onDraw(Canvas canvas) {
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
            canvas.clipPath(mPath); // makes the clip empty
            mPath.addCircle(50, 50, 50, Path.Direction.CCW);
            canvas.clipPath(mPath, Region.Op.REPLACE);
            drawScene(canvas);
            canvas.restore();

            canvas.save();
            canvas.translate(160, 160);
            canvas.clipRect(0, 0, 60, 60);
            canvas.clipRect(40, 40, 100, 100, Region.Op.UNION);
            drawScene(canvas);
            canvas.restore();

            canvas.save();
            canvas.translate(10, 310);
            canvas.clipRect(0, 0, 60, 60);
            canvas.clipRect(40, 40, 100, 100, Region.Op.XOR);
            drawScene(canvas);
            canvas.restore();

            canvas.save();
            canvas.translate(160, 310);
            canvas.clipRect(0, 0, 60, 60);
            canvas.clipRect(40, 40, 100, 100, Region.Op.REVERSE_DIFFERENCE);
            drawScene(canvas);
            canvas.restore();
        }
    }
}


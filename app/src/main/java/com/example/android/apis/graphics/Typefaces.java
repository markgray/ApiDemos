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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;

/**
 * Shows how to load and use a custom Typeface.
 */
public class Typefaces extends GraphicsActivity {

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
     * Simple custom view which just draws two lines of text, one using the default font and one
     * using the custom font "fonts/samplefont.ttf"
     */
    private static class SampleView extends View {
        /**
         * {@code Paint} we use to draw our text
         */
        private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        /**
         * Our custom font, loaded from "fonts/samplefont.ttf"
         */
        private Typeface mFace;

        /**
         * Our constructor. First we call our super's constructor, then we initialize our field
         * {@code Typeface mFace} with a new typeface created from the font data loaded from our
         * resource file "fonts/samplefont.ttf". Finally we set the text size of {@code Paint mPaint}
         * to 64.
         *
         * @param context {@code Context} to use to access resources, "this" in the {@code onCreate}
         *                method of {@code TypeFaces}.
         */
        public SampleView(Context context) {
            super(context);

            mFace = Typeface.createFromAsset(context.getAssets(), "fonts/samplefont.ttf");
            mPaint.setTextSize(64);
        }

        /**
         * We implement this to do our drawing. First we set the entire {@code Canvas canvas} to
         * {@code Color.WHITE}. We set the type face of {@code Paint mPaint} to null (so it will
         * use the default), and use it to draw the text "Draw with Default:" at (10,100) on
         * {@code canvas}, then draw the text "  SAMPLE TEXT" at (10,200), "Draw with Custom Font"
         * at (10,400), and "(Custom Font draws 'A' with solid triangle.)" at (10,500). Next we set
         * the typeface of {@code mPaint} to our custom {@code Typeface mFace}, and use it to draw
         * the text "  SAMPLE TEXT" at (10,600) on {@code canvas}.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.WHITE);
            mPaint.setTypeface(null);
            canvas.drawText("Draw with Default:", 10, 100, mPaint);
            canvas.drawText("  SAMPLE TEXT", 10, 200, mPaint);
            canvas.drawText("Draw with Custom Font", 10, 400, mPaint);
            canvas.drawText("(Custom Font draws 'A' with solid triangle.)", 10, 500, mPaint);
            mPaint.setTypeface(mFace);
            canvas.drawText("  SAMPLE TEXT", 10, 600, mPaint);
        }
    }
}


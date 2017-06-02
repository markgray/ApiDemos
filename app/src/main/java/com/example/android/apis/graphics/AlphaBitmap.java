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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.View;

import com.example.android.apis.R;

import java.io.InputStream;

/**
 * Shows how to use alpha channel compositing in 2D graphics using PorterDuffXfermode
 * and Bitmap.extractAlpha -- also shows how to create your own View in code alone.
 * See: https://developer.android.com/reference/android/graphics/PorterDuff.Mode.html
 * for details of the graphic algebra performed with the different PorterDuff modes
 * You can use the key below to understand the algebra that the Android docs use to
 * describe the other modes (see the article for a fuller description with similar terms).
 * <ul>
 * <li>Sa Source alpha</li>
 * <li>Sc Source color</li>
 * <li>Da Destination alpha</li>
 * <li>Dc Destination color</li>
 * </ul>
 * Where alpha is a value [0..1], and color is substituted once per channel (so use
 * the formula once for each of red, green and blue)
 * <p>
 * The resulting values are specified as a pair in square braces as follows:
 * <p>
 * [{@code <alpha-value>,<color-value>}]
 * <p>
 * Where alpha-value and color-value are formulas for generating the resulting
 * alpha channel and each color channel respectively.
 */
public class AlphaBitmap extends GraphicsActivity {

    /**
     * Called when the Activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to a new instance of our class {@code SampleView}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SampleView(this));
    }

    /**
     * This is a {@code View} which draws our three {@code Bitmap}'s into the {@code Canvas} its
     * {@code onDraw} override is passed.
     */
    private static class SampleView extends View {
        /**
         * Contains the Bitmap decoded from our raw png image file R.raw.app_sample_code.
         */
        private Bitmap mBitmap;
        /**
         * The alpha values of {@code Bitmap mBitmap}. This may be drawn with {@code Canvas.drawBitmap()},
         * where the color(s) will be taken from the paint that is passed to the draw call.
         */
        private Bitmap mBitmap2;
        /**
         * {@code Bitmap} into which we draw a {@code LinerGradient} colored circle and text into
         * using {@code Canvas.drawCircle} and {@code Canvas.drawText}.
         */
        private Bitmap mBitmap3;
        private Shader mShader;
        private Paint p;

        private static void drawIntoBitmap(Bitmap bm) {
            float x = bm.getWidth();
            float y = bm.getHeight();
            Canvas c = new Canvas(bm);
            Paint p = new Paint();
            p.setAntiAlias(true);

            p.setAlpha(0x80);
            c.drawCircle(x / 2, y / 2, x / 2, p);

            p.setAlpha(0x30);
            p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
            p.setTextSize(60);
            p.setTextAlign(Paint.Align.CENTER);
            Paint.FontMetrics fm = p.getFontMetrics();
            c.drawText("Alpha", x / 2, (y - fm.ascent) / 2, p);
        }

        public SampleView(Context context) {
            super(context);
            setFocusable(true);

            InputStream is = context.getResources().openRawResource(R.raw.app_sample_code);
            mBitmap = BitmapFactory.decodeStream(is);
            mBitmap2 = mBitmap.extractAlpha();
            mBitmap3 = Bitmap.createBitmap(200, 200, Bitmap.Config.ALPHA_8);
            drawIntoBitmap(mBitmap3);

            mShader = new LinearGradient(0, 0, 100, 70, new int[]{
                    Color.RED, Color.GREEN, Color.BLUE},
                    null, Shader.TileMode.MIRROR);
            p = new Paint();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.WHITE);
            float y = 10;

            p.setColor(Color.RED);
            canvas.drawBitmap(mBitmap, 10, y, p);
            y += mBitmap.getHeight() + 10;
            p.setColor(Color.GREEN);
            canvas.drawBitmap(mBitmap, 10, y, p);
            y += mBitmap.getHeight() + 10;
            canvas.drawBitmap(mBitmap2, 10, y, p);
            y += mBitmap2.getHeight() + 10;
            p.setShader(mShader);
            canvas.drawBitmap(mBitmap3, 10, y, p);
            y += mBitmap3.getHeight() + 10;
            p.setColor(Color.RED);
            canvas.drawBitmap(mBitmap3, 10, y, p);
        }
    }
}


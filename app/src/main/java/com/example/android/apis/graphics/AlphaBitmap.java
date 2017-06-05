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
        /**
         * {@code LinearGradient} {@code Shader} used to draw {@code mBitmap3}
         */
        private Shader mShader;
        /**
         * {@code Paint} instance used in call to {@code drawBitmap} for all the Bitmaps
         */
        private Paint p;

        /**
         * Draws a circle and text into the {@code Bitmap} it is passed. We fetch the width of our
         * {@code Bitmap bm} to {@code float x}, and the height to {@code float y}. We construct a
         * {@code Canvas c} that will use {@code bm} to draw into, and allocate a new instance of
         * {@code Paint p}.
         * <p>
         * We set the ANTI_ALIAS_FLAG of {@code Paint p} to true, and set the alpha component of the
         * paint's color to 0x80 (1/2) and draw a circle into the {@code Canvas c} centered in the
         * middle with radius of half the width of the {@code Bitmap bm} using {@code p} as the
         * {@code Paint}.
         * <p>
         * Then we set the alpha component of {@code p} to 0x30, set the xfermode object of {@code p}
         * to the porter-duff mode SRC ([Sa, Sc] -- source alpha, and source color, the source pixels
         * replace the destination pixels.)
         * See: https://developer.android.com/reference/android/graphics/PorterDuff.Mode.html
         * Then we set the text size of {@code p} to 60, paint's text alignment to CENTER. Next we
         * use {@code Paint p} to allocate a new {@code FontMetrics} object for {@code Paint.FontMetrics fm},
         * getFontMetrics(fm) is called by this version of the constructor, which returns the font's recommended
         * interline spacing to {@code fm}, given the Paint's settings for typeface, textSize, etc. We use {@code fm.ascent}
         * (the recommended distance above the baseline for singled spaced text) to calculate how far
         * down we have to move our {@code y} in order to center the text in the {@code Bitmap bm} when
         * we call {@code Canvas.drawText} to write the string "Alpha" using the {@code Paint p}.
         *
         * @param bm mutable {@code Bitmap} whose {@code Canvas} we want to draw into
         */
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

        /**
         * Initializes the fields contained in this instance of {@code SampleView}. First we call
         * through to our super's constructor, then we set our {@code View} to be focusable. We
         * initialize our field {@code Shader mShader} to be a {@code LinearGradient} shader that
         * draws a linear gradient along the line {@code (0,0)->(100,70)}, using the colors RED,
         * GREEN, and BLUE distributed evenly along the gradient line, using the tile mode MIRROR
         * (repeating the shader's image horizontally and vertically, alternating mirror images so
         * that adjacent images always seam). We initialize our field {@code Paint p} with a new
         * instance.
         * <p>
         * Next we open {@code InputStream is} to read our raw resource file R.raw.app_sample_code,
         * and use {@code BitmapFactory.decodeStream} to read and decode {@code is} into our field
         * {@code Bitmap mBitmap}. We extract only the Alpha channel from {@code mBitmap} to initialize
         * our field {@code Bitmap mBitmap2}.
         * <p>
         * Now we create an empty 200x200 {@code Bitmap} using Bitmap.Config.ALPHA_8 as the
         * {@code Bitmap.Config} (Each pixel is stored as a single translucency (alpha) channel.
         * no color information is stored. With this configuration, each pixel requires 1 byte of
         * memory), and set our field {@code Bitmap mBitmap3} to it, Finally we call our method
         * {@code drawIntoBitmap} to use {@code mBitmap3} for the canvas to draw its circle and text
         * into.
         *
         * @param context {@code Context} to use to fetch resources
         */
        public SampleView(Context context) {
            super(context);
            setFocusable(true);
            mShader = new LinearGradient(0, 0, 100, 70, new int[]{
                    Color.RED, Color.GREEN, Color.BLUE},
                    null, Shader.TileMode.MIRROR);
            p = new Paint();

            InputStream is = context.getResources().openRawResource(R.raw.app_sample_code);
            mBitmap = BitmapFactory.decodeStream(is);
            mBitmap2 = mBitmap.extractAlpha();
            mBitmap3 = Bitmap.createBitmap(200, 200, Bitmap.Config.ALPHA_8);
            drawIntoBitmap(mBitmap3);
        }

        /**
         * Implement this to do your drawing. First we fill the entire canvas' bitmap with the color
         * WHITE, then we initialize our variable {@code float y} to 10 (we will use this as the y
         * pixel dimension within the {@code Canvas canvas} at which we draw the next {@code Bitmap}
         * to be drawn.)
         * <p>
         * Then we set the color of {@code Paint p} to RED and draw {@code Bitmap mBitmap}
         * at location (10,y) in {@code Canvas canvas} using {@code p} as the {@code Paint}. We advance
         * {@code y} by the height of {@code mBitmap + 10}, set color of {@code Paint p} to GREEN and
         * draw {@code Bitmap mBitmap} at location (10,y) in {@code Canvas canvas} using {@code p} as
         * the {@code Paint}. This demonstrates that the colors in the {@code Bitmap} override the
         * color of the {@code Paint} used to draw it.
         * <p>
         * We now advance {@code y} by the height of {@code mBitmap + 10}, and draw {@code Bitmap mBitmap2}
         * at location (10,y) in {@code Canvas canvas} using {@code p} as the {@code Paint}. Because
         * {@code mBitmap2} consists only of the alpha channel copied from {@code mBitmap} the image
         * drawn will be drawn in the GREEN color setting of {@code Paint p}.
         * <p>
         * We now advance {@code y} by the height of {@code mBitmap2 + 10}, set the {@code Shader} of
         * {@code Paint p} to {@code Shader mShader} and draw {@code Bitmap mBitmap3} at location (10,y)
         * in {@code Canvas canvas} using {@code p} as the {@code Paint}.
         * <p>
         * To demonstrate the the {@code Shader} of a {@code Paint} overrides the color, we advance
         * {@code y} by the height of {@code mBitmap3 + 10}, set the color of {@code Paint p} to RED
         * again and draw {@code Bitmap mBitmap3} at location (10,y) in {@code Canvas canvas} using
         * {@code p} as the {@code Paint}. The image is identical to the one above it.
         *
         * @param canvas the canvas on which the background will be drawn
         */
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


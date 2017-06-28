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

import java.io.ByteArrayOutputStream;

/**
 * Shows how to create bitmaps programmatically, how to compress them to JPG and PNG formats, and
 * shows how JPG looses information while PNG is loss-less.
 */
public class CreateBitmap extends GraphicsActivity {

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
     * Width of the {@code Bitmap}'s being created and displayed
     */
    private static final int WIDTH = 50;
    /**
     * Height of the {@code Bitmap}'s being created and displayed
     */
    private static final int HEIGHT = 50;
    /**
     * Number of colors in the array between rows (must be >= width or <= -width). Colors are pulled
     * from the array in order until WIDTH pixels have been assigned, then we skip the rest of STRIDE
     * to start the next row of pixels.
     */
    private static final int STRIDE = 64;   // must be >= WIDTH

    /**
     * Creates an {@code int[]} array of colors designed to be fed to {@code Bitmap.createBitmap} or
     * {@code Bitmap.setPixels}.
     * <p>
     * First we allocate {@code int[] colors} to hold STRIDE*HEIGHT colors. Then for the number
     * of rows HEIGHT, we move column to column calculating a 32 bit ARGB color for each value in
     * {@code colors}, using STRIDE to skip to the next row when WIDTH columns have been calculated.
     * The color assigned is a function of the row and column position designed to produce a pleasing
     * color ramp (from the looks of it) and we will leave it at that.
     *
     * @return an {@code int[]} array of colors filled with enough colors to specify a WIDTH*HEIGHT
     * Bitmap with a stride of STRIDE between rows (ie. only the first WIDTH of the STRIDE per row
     * colors are defined, after which the rest of STRIDE is skipped to move to the next row.)
     */
    private static int[] createColors() {
        int[] colors = new int[STRIDE * HEIGHT];
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int r = x * 255 / (WIDTH - 1);
                int g = y * 255 / (HEIGHT - 1);
                int b = 255 - Math.min(r, g);
                int a = Math.max(r, g);
                colors[y * STRIDE + x] = (a << 24) | (r << 16) | (g << 8) | b;
            }
        }
        return colors;
    }

    /**
     * Custom View which creates and then draws an array of colors using different techniques.
     */
    private static class SampleView extends View {
        /**
         * Six {@code BitMap}'s created from array of colors, the first three using {@code Bitmap.createBitmap}
         * directly for formats ARGB_8888, RGB_565, and ARGB_4444, the second three using {@code Bitmap.setPixels}.
         */
        private Bitmap[] mBitmaps;
        /**
         * {@code Bitmap}'s created from {@code mBitmaps[i]} using our method codec to first encode
         * and compress our original {@code Bitmap} using Bitmap.CompressFormat.JPEG, then decoding
         * it into {@code Bitmap} format again.
         */
        private Bitmap[] mJPEG;
        /**
         * {@code Bitmap}'s created from {@code mBitmaps[i]} using our method codec to first encode
         * and compress our original {@code Bitmap} using Bitmap.CompressFormat.PNG, then decoding
         * it into {@code Bitmap} format again.
         */
        private Bitmap[] mPNG;
        /**
         * The array of colors that we use to create our {@code Bitmap}'s from
         */
        private int[] mColors;
        /**
         * {@code Paint} instance we use to draw our {@code Bitmap}'s in our {@code onDraw} override
         */
        private Paint mPaint;

        /**
         * Compresses its input {@code Bitmap src} using the {@code Bitmap.CompressFormat format}
         * compression codec (JPEG, or PNG in our case), then decoding that compressed {@code Bitmap}
         * into a new {@code Bitmap} which it returns.
         * <p>
         * First we create {@code ByteArrayOutputStream os}, then we write a compressed version of
         * the {@code Bitmap src} to it, compressed using {@code Bitmap.CompressFormat format} using
         * a quality setting of {@code int quality}. Then we convert {@code os} to a byte array
         * {@code byte[] array}. Finally we return the {@code Bitmap} created by decoding the byte
         * array {@code array} to the caller.
         *
         * @param src     {@code Bitmap} we want to compress, then decode again into a new {@code Bitmap}
         * @param format  compression format we want to use
         * @param quality Hint to the compressor, 0-100. 0 meaning compress for small size, 100
         *                meaning compress for max quality. Some formats, like PNG which is loss
         *                less, will ignore the quality setting
         * @return New {@code Bitmap} created from {@code Bitmap src} after first compressing then
         * decoding it.
         */
        private static Bitmap codec(Bitmap src, Bitmap.CompressFormat format, int quality) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            src.compress(format, quality, os);

            byte[] array = os.toByteArray();
            return BitmapFactory.decodeByteArray(array, 0, array.length);
        }

        /**
         * Constructor for our {@code SampleView} instance. First we call through to our super's
         * constructor, and enable our View to receive focus.
         * <p>
         * We initialize our field {@code int[] mColors} using our method {@code createColors}, and
         * then for some reason which escapes me, we make a local copy of {@code mColors} in
         * {@code int[] colors}.
         * <p>
         * We allocate 6 {@code Bitmap}'s for our field {@code Bitmap[] mBitmaps}, then fill the first
         * three of these by directly initializing ARGB_8888, RGB_565, and ARGB_4444 {@code Bitmap}'s
         * using colors. The second three we create empty {@code Bitmap}'s of the same types, then
         * use {@code Bitmap.setPixels} to set their pixels to {@code colors}.
         * <p>
         * We initialize our field {@code Paint mPaint} with an instance of {@code Paint} and set its
         * dither flag to true.
         * <p>
         * Next we allocate 6 {@code Bitmap}'s for our field {@code Bitmap[] mJPEG} and our field
         * {@code Bitmap[] mPNG} and fill each of them with {@code Bitmap}'s created from their
         * corresponding {@code Bitmap[] mBitmaps} by our method {@code codec} using JPEG compression
         * for the {@code mJPEG} {@code Bitmap} and PNG compression for the {@code mPNG} {@code Bitmap}.
         *
         * @param context {@code Context} to use for resources, "this" {@code Activity} when called
         *                from {@code onCreate} in our case.
         */
        public SampleView(Context context) {
            super(context);
            setFocusable(true);

            mColors = createColors();
            int[] colors = mColors;

            mBitmaps = new Bitmap[6];
            // these three are initialized with colors[]
            mBitmaps[0] = Bitmap.createBitmap(colors, 0, STRIDE, WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
            mBitmaps[1] = Bitmap.createBitmap(colors, 0, STRIDE, WIDTH, HEIGHT, Bitmap.Config.RGB_565);
            mBitmaps[2] = Bitmap.createBitmap(colors, 0, STRIDE, WIDTH, HEIGHT, Bitmap.Config.ARGB_4444);

            // these three will have their colors set later
            mBitmaps[3] = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
            mBitmaps[4] = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.RGB_565);
            mBitmaps[5] = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_4444);
            for (int i = 3; i <= 5; i++) {
                mBitmaps[i].setPixels(colors, 0, STRIDE, 0, 0, WIDTH, HEIGHT);
            }

            mPaint = new Paint();
            mPaint.setDither(true);

            // now encode/decode using JPEG and PNG
            mJPEG = new Bitmap[mBitmaps.length];
            mPNG = new Bitmap[mBitmaps.length];
            for (int i = 0; i < mBitmaps.length; i++) {
                mJPEG[i] = codec(mBitmaps[i], Bitmap.CompressFormat.JPEG, 80);
                mPNG[i] = codec(mBitmaps[i], Bitmap.CompressFormat.PNG, 0);
            }
        }

        /**
         * We implement this to do our drawing. First we set the color of the entire {@code Canvas canvas}
         * to WHITE. Then for each of the 6 {@code Bitmap}'s in our three arrays we first draw the
         * {@code mBitmaps} {@code Bitmap}, then the {@code mJPEG} {@code Bitmap} and {@code mPNG}
         * {@code Bitmap} in a row, translate the {@code Canvas canvas} down by the height of the
         * {@code mBitmaps} to get ready to draw the next row of the six.
         * <p>
         * When done with the 18 {@code Bitmap}'s we created in our constructor, we next draw our
         * color array directly, w/o creating a bitmap object, first specifying that the array of
         * colors contains an alpha channel, but without using a {@code Paint} object, then
         * specifying that there is no alpha channel and using the {@code Paint mPaint} as the
         * {@code Paint} object.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.WHITE);

            for (int i = 0; i < mBitmaps.length; i++) {
                canvas.drawBitmap(mBitmaps[i], 0, 0, null);
                canvas.drawBitmap(mJPEG[i], 80, 0, null);
                canvas.drawBitmap(mPNG[i], 160, 0, null);
                canvas.translate(0, mBitmaps[i].getHeight());
            }

            // draw the color array directly, w/o creating a bitmap object
            //noinspection deprecation
            canvas.drawBitmap(mColors, 0, STRIDE, 0, 0, WIDTH, HEIGHT, true, null);
            canvas.translate(0, HEIGHT);
            //noinspection deprecation
            canvas.drawBitmap(mColors, 0, STRIDE, 0, 0, WIDTH, HEIGHT, false, mPaint);
        }
    }
}


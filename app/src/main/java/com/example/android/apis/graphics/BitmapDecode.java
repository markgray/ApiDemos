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

import com.example.android.apis.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.Bundle;
import android.util.Log;
import android.view.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

/**
 * Shows how to decode various image file formats into displayable bitmaps: drawable/beach.jpg,
 * drawable/frog.gif (creates also an Bitmap.Config.ARGB_8888 bitmap, and a Bitmap.Config.ARGB_4444
 * bitmap from that bitmap), drawable/button.9.png, and drawable/animated_gif.gif which it animates
 * using android.graphics.Movie.java
 */
public class BitmapDecode extends GraphicsActivity {

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
     * Custom View which draws 4 {@code Bitmap}'s, one {@code Drawable}, and an animated gif in a
     * {@code Movie} instance.
     */
    private static class SampleView extends View {
        /**
         * TAG for logging
         */
        private static final String TAG = "BitMapDecode";
        /**
         * Decoded R.raw.beach jpg, scaled down by 4
         */
        private Bitmap mBitmap;
        /**
         * Decoded R.raw.frog gif
         */
        private Bitmap mBitmap2;
        /**
         * Deep copy of the pixels of {@code mBitmap2} using the Bitmap.Config ARGB_8888
         */
        private Bitmap mBitmap3;
        /**
         * Deep copy of the pixels of {@code mBitmap2} using the Bitmap.Config ARGB_4444
         */
        private Bitmap mBitmap4;
        /**
         * R.drawable.button.9.png {@code Drawable}
         */
        private Drawable mDrawable;

        /**
         * {@code Movie} used to play the animated gif R.raw.animated_gif
         */
        private Movie mMovie;
        /**
         * Start time in milliseconds of the {@code Movie} animation, used to calculate the relative
         * time of the animation before asking {@code mMovie} to draw the frame that is scheduled for
         * that relative time.
         */
        private long mMovieStart;

        /**
         * Set to false to use {@code Movie.decodeByteArray} instead of {@code Movie.decodeStream}
         */
        private static final boolean DECODE_STREAM = true;

        /**
         * Used to read an {@code InputStream} into a {@code byte[]} array. First we allocate a
         * {@code ByteArrayOutputStream os} with an initial buffer capacity of 1024, and a
         * {@code byte[] buffer} with a capacity of 1024. Then wrapped in a try block intended to
         * catch IOException we read up to 1024 bytes at a time from {@code is} into {@code buffer}
         * and write them to {@code os}, stopping when the read from {@code is} is less than 0 (end
         * of file). Finally we return the current contents of the output stream {@code os}, in a
         * newly allocated byte array as returned by {@code ByteArrayOutputStream.toByteArray()}.
         *
         * @param is {@code InputStream} to read bytes from
         * @return a {@code byte[]} array containing the raw data from {@code is}
         */
        private static byte[] streamToBytes(InputStream is) {
            ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
            byte[] buffer = new byte[1024];
            int len;
            try {
                while ((len = is.read(buffer)) >= 0) {
                    os.write(buffer, 0, len);
                }
            } catch (java.io.IOException e) {
                Log.i(TAG, e.getLocalizedMessage());
            }
            return os.toByteArray();
        }

        /**
         * Constructs and initializes an instance of {@code SampleView}. First we call through to our
         * super's constructor, then we enable this View to receive focus.
         *
         * @param context {@code Context} to use to fetch resources, "this" when called from our
         *                {@code onCreate} override
         */
        public SampleView(Context context) {
            super(context);
            setFocusable(true);

            java.io.InputStream is;
            is = context.getResources().openRawResource(R.raw.beach);

            BitmapFactory.Options opts = new BitmapFactory.Options();
            Bitmap bm;

            opts.inJustDecodeBounds = true;
            //noinspection UnusedAssignment
            bm = BitmapFactory.decodeStream(is, null, opts);

            // now opts.outWidth and opts.outHeight are the dimension of the
            // bitmap, even though bm is null

            opts.inJustDecodeBounds = false;    // this will request the bm
            opts.inSampleSize = 4;             // scaled down by 4
            try {
                is.reset(); // Need to rewind is in order to read it again.
            } catch (IOException e) {
                e.printStackTrace();
            }
            bm = BitmapFactory.decodeStream(is, null, opts);

            mBitmap = bm;

            // decode an image with transparency
            is = context.getResources().openRawResource(R.raw.frog);
            mBitmap2 = BitmapFactory.decodeStream(is);

            // create a deep copy of it using getPixels() into different configs
            int w = mBitmap2.getWidth();
            int h = mBitmap2.getHeight();
            int[] pixels = new int[w * h];
            mBitmap2.getPixels(pixels, 0, w, 0, 0, w, h);
            mBitmap3 = Bitmap.createBitmap(pixels, 0, w, w, h, Bitmap.Config.ARGB_8888);
            mBitmap4 = Bitmap.createBitmap(pixels, 0, w, w, h, Bitmap.Config.ARGB_4444);

            //noinspection deprecation
            mDrawable = context.getResources().getDrawable(R.drawable.button);
            //noinspection ConstantConditions
            mDrawable.setBounds(150, 20, 300, 100);

            is = context.getResources().openRawResource(R.raw.animated_gif);

            if (DECODE_STREAM) {
                mMovie = Movie.decodeStream(is);
            } else {
                byte[] array = streamToBytes(is);
                mMovie = Movie.decodeByteArray(array, 0, array.length);
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(0xFFCCCCCC);

            @SuppressLint("DrawAllocation") Paint p = new Paint();
            p.setAntiAlias(true);

            if (mBitmap != null) {
                canvas.drawBitmap(mBitmap, 10, 10, null);
            }
            canvas.drawBitmap(mBitmap2, 10, 170, null);
            canvas.drawBitmap(mBitmap3, 110, 170, null);
            canvas.drawBitmap(mBitmap4, 210, 170, null);

            mDrawable.draw(canvas);

            long now = android.os.SystemClock.uptimeMillis();
            if (mMovieStart == 0) {   // first time
                mMovieStart = now;
            }
            if (mMovie != null) {
                int dur = mMovie.duration();
                if (dur == 0) {
                    dur = 1000;
                }
                int relTime = (int) ((now - mMovieStart) % dur);
                mMovie.setTime(relTime);
                mMovie.draw(canvas, getWidth() - mMovie.width(), getHeight() - mMovie.height());
                invalidate();
            }
        }
    }
}

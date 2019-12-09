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
import android.graphics.Movie;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.android.apis.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
         * catch IOException we read up to 1024 bytes at a time from {@code inputStream} into
         * {@code buffer} and write them to {@code os}, stopping when the read from {@code inputStream} is
         * less than 0 (end of file). Finally we return the current contents of the output stream
         * {@code os}, in a newly allocated byte array as returned by
         * {@code ByteArrayOutputStream.toByteArray()}.
         *
         * @param inputStream {@code InputStream} to read bytes from
         * @return a {@code byte[]} array containing the raw data from {@code inputStream}
         */
        private static byte[] streamToBytes(InputStream inputStream) {
            ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
            byte[] buffer = new byte[1024];
            int len;
            try {
                while ((len = inputStream.read(buffer)) >= 0) {
                    os.write(buffer, 0, len);
                }
            } catch (java.io.IOException e) {
                //noinspection ConstantConditions
                Log.i(TAG, e.getLocalizedMessage());
            }
            return os.toByteArray();
        }

        /**
         * Constructs and initializes an instance of {@code SampleView}. First we call through to our
         * super's constructor, then we enable this View to receive focus. We declare {@code InputStream inputStream}
         * and use it to open the resource jpg R.raw.beach. We create {@code BitmapFactory.Options opts},
         * and declare {@code Bitmap bm}. We set the {@code inJustDecodeBounds} field of {@code opts} to
         * true (the decoder will return null (no bitmap), but the out... fields will still be set,
         * allowing the caller to query the bitmap without having to allocate the memory for its pixels),
         * and use it as the {@code Options} parameter when we call {@code decodeStream} on {@code inputStream}
         * after which the fields {@code opts.outWidth} and {@code opts.outHeight} contain the dimensions
         * of the bitmap that would be created from {@code inputStream} (null is returned instead of a bitmap).
         * We now set the {@code opts.inJustDecodeBounds} field to false, and {@code opts.inSampleSize}
         * to 4, rewind {@code inputStream} and decode it again this time into our {@code Bitmap bm}. This results
         * in a bitmap version of {@code inputStream} scaled down by 4. We set our field {@code Bitmap mBitmap} to
         * {@code bm}.
         * <p>
         * Now we use {@code inputStream} to open our resource gif R.raw.frog, and decode it into our field
         * {@code Bitmap mBitmap2}. We fetch the width of {@code mBitmap2} to {@code int w} and the
         * height to {@code int h} and allocate {@code int[] pixels} to contain {@code w*h} ints.
         * We copy all of the pixels from {@code Bitmap2} into {@code pixels}, then use {@code pixels}
         * to create {@code Bitmap mBitmap3} using a config of ARGB_8888, and create {@code Bitmap mBitmap4}
         * using a config of ARGB_4444. Then we load {@code Drawable mDrawable} from our resource file
         * R.drawable.button and set its bounds to (150, 20, 300, 100) (left,top,right,bottom). We open
         * our resource animated gif file R.raw.animated_gif using {@code inputStream}, and decode this stream
         * into {@code Movie mMovie} (If DECODE_STREAM is true that is, otherwise we read the raw bytes
         * of {@code inputStream} into {@code byte[] array} and decode that byte array into {@code Movie mMovie}).
         *
         * @param context {@code Context} to use to fetch resources, "this" when called from our
         *                {@code onCreate} override
         */
        public SampleView(Context context) {
            super(context);
            setFocusable(true);

            java.io.InputStream inputStream;
            inputStream = context.getResources().openRawResource(R.raw.beach);

            BitmapFactory.Options opts = new BitmapFactory.Options();
            Bitmap bm;

            opts.inJustDecodeBounds = true;
            //noinspection UnusedAssignment
            bm = BitmapFactory.decodeStream(inputStream, null, opts);

            // now opts.outWidth and opts.outHeight are the dimension of the
            // bitmap, even though bm is null

            opts.inJustDecodeBounds = false;    // this will request the bm
            opts.inSampleSize = 4;             // scaled down by 4
            try {
                inputStream.reset(); // Need to rewind is in order to read it again.
            } catch (IOException e) {
                e.printStackTrace();
            }
            bm = BitmapFactory.decodeStream(inputStream, null, opts);

            mBitmap = bm;

            // decode an image with transparency
            inputStream = context.getResources().openRawResource(R.raw.frog);
            mBitmap2 = BitmapFactory.decodeStream(inputStream);

            // create a deep copy of it using getPixels() into different configs
            int w = mBitmap2.getWidth();
            int h = mBitmap2.getHeight();
            int[] pixels = new int[w * h];
            mBitmap2.getPixels(pixels, 0, w, 0, 0, w, h);
            mBitmap3 = Bitmap.createBitmap(pixels, 0, w, w, h, Bitmap.Config.ARGB_8888);
            mBitmap4 = Bitmap.createBitmap(pixels, 0, w, w, h, Bitmap.Config.ARGB_4444);

            mDrawable = context.getResources().getDrawable(R.drawable.button);
            mDrawable.setBounds(150, 20, 300, 100);

            inputStream = context.getResources().openRawResource(R.raw.animated_gif);

            if (DECODE_STREAM) {
                mMovie = Movie.decodeStream(inputStream);
            } else {
                byte[] array = streamToBytes(inputStream);
                mMovie = Movie.decodeByteArray(array, 0, array.length);
            }
        }

        /**
         * We implement this to do our drawing when requested to do so. First we set the entire
         * {@code Canvas canvas} to the color 0xFFCCCCCC (a darkish gray). Then if {@code Bitmap mBitmap}
         * is not null we draw it at location (10,10) on the {@code Canvas canvas}, and draw
         * {@code Bitmap mBitmap2} at (10,170), {@code Bitmap mBitmap3} at (110,170), and
         * {@code Bitmap mBitmap4} at (210,170). We draw {@code Drawable mDrawable} to {@code canvas}
         * (its bounds already position is correctly).
         *
         * Next we fetch the current system time to {@code long now}, and if our field {@code long mMovieStart}
         * is zero (first time we are called) we set {@code mMovieStart} to {@code now}. If {@code Movie mMovie}
         * is not null, we fetch the duration of {@code mMovie} to {@code int dur} (1000 for our gif), making
         * sure it is 1000 if {@code Movie.duration} returned zero. We calculate {@code int relTime} to be
         * modulo {@code dur} of the time in milliseconds since {@code mMovieStart}, set the time of {@code mMovie}
         * to {@code relTime} and instruct it to draw the current frame of the animated gif at the
         * bottom right corner of {@code Canvas canvas}.
         *
         * Finally we invalidate our {@code View} which will schedule us to run again for the next refresh
         * of the screen.
         *
         * @param canvas {@code Canvas} to draw our {@code View} onto
         */
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(0xFFCCCCCC);

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

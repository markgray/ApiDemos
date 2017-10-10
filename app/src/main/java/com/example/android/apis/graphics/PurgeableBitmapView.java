/*
 * Copyright (C) 2009 The Android Open Source Project
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.BitmapFactory.Options;
import android.view.View;

import java.io.ByteArrayOutputStream;

/**
 * PurgeableBitmapView works with PurgeableBitmap to demonstrate the effects of setting
 * Bitmaps as being purgeable.
 * <p>
 * PurgeableBitmapView decodes an encoded bitstream to a Bitmap each time update()
 * is invoked(), and its onDraw() draws the Bitmap and a number to screen.
 * The number is used to indicate the number of Bitmaps that have been decoded.
 */
@SuppressWarnings("FieldCanBeLocal")
@SuppressLint("ViewConstructor")
public class PurgeableBitmapView extends View {
    /**
     * Contains a JPEG compressed bitmap which we try to load into each of the 200 members of our
     * {@code Bitmap[] mBitmapArray}.
     */
    private final byte[] bitstream;

    /**
     * {@code Bitmap} pointer to last {@code Bitmap} decoded into {@code Bitmap[] mBitmapArray},
     * used to draw it in our {@code onDraw} method.
     */
    private Bitmap mBitmap;
    /**
     * Number of bitmaps we will try to decode into {@code Bitmap[] mBitmapArray}.
     */
    private final int mArraySize = 200;
    /**
     * Array we decode our bitmaps onto.
     */
    private final Bitmap[] mBitmapArray = new Bitmap[mArraySize];
    /**
     * {@code Options} object we use in call to {@code decodeByteArray}, we set its field
     * {@code inPurgeable} to the value of the flag {@code isPurgeable} passed to our constructor.
     * (This field is ignored since Lollipop however).
     */
    private final Options mOptions = new Options();
    /**
     * Width of the {@code Bitmap} we create, then compress into a JPEG bitstream {@code byte[] bitstream}
     * and then decode back into a new {@code Bitmap} to fill our {@code Bitmap[] mBitmapArray}
     */
    private static final int WIDTH = 150;
    /**
     * Height of the {@code Bitmap} we create, then compress into a JPEG bitstream {@code byte[] bitstream}
     * and then decode back into a new {@code Bitmap} to fill our {@code Bitmap[] mBitmapArray}
     */
    private static final int HEIGHT = 450;
    /**
     * Stride of the {@code Bitmap} we create, then compress into a JPEG bitstream {@code byte[] bitstream}
     * and then decode back into a new {@code Bitmap} to fill our {@code Bitmap[] mBitmapArray} (this
     * is the number of colors between rows or the array of colors passed to {@code createBitmap}).
     */
    private static final int STRIDE = 320;   // must be >= WIDTH
    /**
     * Number of bitmaps we have decoded into {@code Bitmap[] mBitmapArray} so far.
     */
    private int mDecodingCount = 0;
    /**
     * {@code Paint} we use in our {@code onDraw} method to draw the text displaying the number of
     * bitmaps decoded so far.
     */
    private final Paint mPaint = new Paint();
    /**
     * Text size of the {@code Paint mPaint}, set in our constructor
     */
    private final int textSize = 32;
    /**
     * Delay in milliseconds we use when we call the {@code sleep} method of the {@code PurgeableBitmap.RefreshHandler}
     * which was passed as a parameter to our {@code update} method ("this" when we are called from the
     * {@code handleMessage} method of the {@code PurgeableBitmap.RefreshHandler} which {@code PurgeableBitmap}
     * uses).
     */
    private static int delay = 100;

    /**
     * Our constructor. First we call our super's constructor, then we enable our view to receive
     * focus, and initialize the {@code inPurgeable} field of {@code Options mOptions} to the value
     * of our parameter {@code isPurgeable}. We call our method {@code createColors} to create an
     * array of colors for {@code int[] colors}, and use it to create {@code Bitmap src}. We then
     * initialize our field {@code byte[] bitstream} with the return value of our method
     * {@code generateBitstream} (a byte array of {@code Bitmap src} compressed using JPEG). Finally
     * we set the text size of {@code Paint mPaint} to {@code textSize}, and its color to GRAY.
     *
     * @param context     {@code Context} to use to access resources.
     * @param isPurgeable flag to use to set the {@code inPurgeable} of the {@code Options mOptions}
     *                    object we use when decoding our bitmaps.
     */
    public PurgeableBitmapView(Context context, boolean isPurgeable) {
        super(context);
        setFocusable(true);
        //noinspection deprecation
        mOptions.inPurgeable = isPurgeable;

        int[] colors = createColors();
        Bitmap src = Bitmap.createBitmap(colors, 0, STRIDE, WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        bitstream = generateBitstream(src, Bitmap.CompressFormat.JPEG, 80);

        mPaint.setTextSize(textSize);
        mPaint.setColor(Color.GRAY);
    }

    /**
     * Calculates colors to fill an array and returns it.
     *
     * @return an array of color values.
     */
    private int[] createColors() {
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
     * Called to try to decode another {@code Bitmap}. Wrapped in a try block intended to catch
     * OutOfMemoryError exceptions we attempt to decode {@code byte[] bitstream} into the next bitmap
     * slot in the array {@code Bitmap[] mBitmapArray}. We also set our field {@code Bitmap mBitmap}
     * to point at this bitmap for the sake of our {@code onDraw} method. We next increment the value
     * of {@code mDecodingCount} (our count of the number of bitmaps successfully decoded). If there
     * is still more room in {@code Bitmap[] mBitmapArray}, we call the {@code sleep} method of our
     * {@code handler} parameter to schedule another call in 100 milliseconds and return 0 to the
     * caller (the {@code handleMessage} method of {@code handler}). If we have run out of slots in
     * {@code Bitmap[] mBitmapArray} we return the negated value of {@code mDecodingCount} to the
     * caller indicating successful decoding of all 200 bitmaps. If our decoding causes the throwing
     * of an OutOfMemoryError exception, we recycle all the {@code Bitmap} objects currently in
     * {@code Bitmap[] mBitmapArray}, and return the number of bitmaps which caused us to throw the
     * exception: {@code mDecodingCount+1}.
     *
     * @param handler {@code Handler} whose {@code sleep} method we call to schedule another call to us.
     * @return Zero if we still have bitmaps to decode, or if non-zero, the number of bitmaps successfully
     * decoded, negated if we did not run out of memory and  positive if we did run out of memory
     */
    public int update(PurgeableBitmap.RefreshHandler handler) {
        try {
            mBitmapArray[mDecodingCount] = BitmapFactory.decodeByteArray(bitstream, 0, bitstream.length, mOptions);
            mBitmap = mBitmapArray[mDecodingCount];
            mDecodingCount++;
            if (mDecodingCount < mArraySize) {
                handler.sleep(delay);
                return 0;
            } else {
                return -mDecodingCount;
            }

        } catch (OutOfMemoryError error) {
            for (int i = 0; i < mDecodingCount; i++) {
                mBitmapArray[i].recycle();
            }
            return mDecodingCount + 1;
        }
    }

    /**
     * We implement this to do our drawing. First we set the entire {@code Canvas canvas} to the
     * color WHITE, then we draw {@code Bitmap mBitmap} to it, and finally we draw the number of
     * bitmaps decoded so far on it.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(mBitmap, 0, 0, null);
        canvas.drawText(String.valueOf(mDecodingCount), WIDTH / 2 - 20, HEIGHT / 2, mPaint);
    }

    /**
     * Create a compressed version of the {@code Bitmap} passed to it, and returns it in a {@code Byte[]}
     * array, First we allocate a new {@code ByteArrayOutputStream os}, and use the compress method
     * of {@code Bitmap src} to write a compressed version of the bitmap to this {@code ByteArrayOutputStream}
     * using our parameter {@code quality} for the quality hint, and our parameter {@code format} as
     * the compression format. Finally we return the contents of {@code os} as a byte array to the caller.
     *
     * @param src     {@code Bitmap} we are to compress into a byte array
     * @param format  Format of the compression that we are to use.
     * @param quality Hint to the compressor, 0-100. 0 meaning compress for small size, 100 meaning
     *                compress for max quality.
     * @return {@code Byte[]} array containing compressed version of the {@code Bitmap src}
     */
    private byte[] generateBitstream(Bitmap src, Bitmap.CompressFormat format, int quality) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        src.compress(format, quality, os);
        return os.toByteArray();
    }

}

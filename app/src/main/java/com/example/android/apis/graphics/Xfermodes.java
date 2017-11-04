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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Xfermode;
import android.os.Bundle;
import android.view.View;

/**
 * Shows the results of using different Xfermode's when drawing an overlapping square and a circle
 * to the same Canvas. It was way too small on the newer devices (froyo OK), so I modified it to
 * scale for dpi.
 */
public class Xfermodes extends GraphicsActivity {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to a new instance of {@code SampleView}
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SampleView(this));
    }

    /**
     * Create a bitmap with a circle, used for the "dst" image. First we create {@code Bitmap bm} to
     * be {@code w} by {@code h} in size and using the ARGB_8888 configuration. Then we create
     * {@code Canvas c} to draw into {@code Bitmap bm}. We create {@code Paint p} with its antialias
     * flag set, and set its color to 0xFFFFCC44 (a yellowish orange). Then we use {@code Paint p} to
     * draw an oval on {@code Canvas c} with a {@code RectF} defining its size to be {@code w*3/4} by
     * {@code h*3/4}. Finally we return {@code Bitmap bm} which now contains this oval to the caller.
     *
     * @param w width of the {@code Bitmap}
     * @param h height of the {@code Bitmap}
     * @return {@code Bitmap} containing a circle
     */
    static Bitmap makeDst(int w, int h) {
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        p.setColor(0xFFFFCC44);
        c.drawOval(new RectF(0, 0, w * 3 / 4, h * 3 / 4), p);
        return bm;
    }

    /**
     * Create a bitmap with a rect, used for the "src" image. First we create {@code Bitmap bm} to
     * be {@code w} by {@code h} in size and using the ARGB_8888 configuration. Then we create
     * {@code Canvas c} to draw into {@code Bitmap bm}. We create {@code Paint p} with its antialias
     * flag set, and set its color to 0xFF66AAFF (a light blue). Then we use {@code Paint p} to draw
     * a rectangle on {@code Canvas c} with its top left corner at (w/3,h/3), and its bottom right
     * corner at (w*19/20,h*19/20). Finally we return {@code Bitmap bm} which now contains this rect
     * to the caller.
     *
     * @param w width of the {@code Bitmap}
     * @param h height of the {@code Bitmap}
     * @return {@code Bitmap} containing a rect.
     */
    static Bitmap makeSrc(int w, int h) {
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        p.setColor(0xFF66AAFF);
        c.drawRect(w / 3, h / 3, w * 19 / 20, h * 19 / 20, p);
        return bm;
    }

    /**
     * Our demo custom view. Shows the result of using the 16 different PorterDuff modes with a circle
     * as the "dst" image, and a rectangle for the "src" image.
     */
    private static class SampleView extends View {
        /**
         * Logical screen density, used to scale the {@code Canvas} of the view to correctly display
         * the demo on high density screens.
         */
        private float SCREEN_DENSITY;
        /**
         * Width used for each of the 16 example PorterDuff xfermode squares.
         */
        private static final int W = 64;
        /**
         * Height used for each of the 16 example PorterDuff xfermode squares.
         */
        private static final int H = 64;
        /**
         * number of samples per row
         */
        private static final int ROW_MAX = 4;

        /**
         * {@code Bitmap} used for the "src" image (a rectangle)
         */
        private Bitmap mSrcB;
        /**
         * {@code Bitmap} used for the "dst" image (a circle)
         */
        private Bitmap mDstB;
        /**
         * background checker-board pattern
         */
        private Shader mBG;

        /**
         * Array containing the 16 {@code PorterDuffXfermode} possibilities.
         */
        private static final Xfermode[] sModes = {
                new PorterDuffXfermode(PorterDuff.Mode.CLEAR),
                new PorterDuffXfermode(PorterDuff.Mode.SRC),
                new PorterDuffXfermode(PorterDuff.Mode.DST),
                new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER),
                new PorterDuffXfermode(PorterDuff.Mode.DST_OVER),
                new PorterDuffXfermode(PorterDuff.Mode.SRC_IN),
                new PorterDuffXfermode(PorterDuff.Mode.DST_IN),
                new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT),
                new PorterDuffXfermode(PorterDuff.Mode.DST_OUT),
                new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP),
                new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP),
                new PorterDuffXfermode(PorterDuff.Mode.XOR),
                new PorterDuffXfermode(PorterDuff.Mode.DARKEN),
                new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN),
                new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY),
                new PorterDuffXfermode(PorterDuff.Mode.SCREEN)
        };

        /**
         * Labels for the 16 {@code PorterDuffXfermode} example composite images.
         */
        private static final String[] sLabels = {
                "Clear", "Src", "Dst", "SrcOver",
                "DstOver", "SrcIn", "DstIn", "SrcOut",
                "DstOut", "SrcATop", "DstATop", "Xor",
                "Darken", "Lighten", "Multiply", "Screen"
        };

        /**
         * Our constructor. First we call our super's constructor. Then we initialize {@code SCREEN_DENSITY}
         * to the logical density of the current display. We initialize {@code Bitmap mSrcB} with a
         * W by H rectangle created by our method {@code makeSrc}, and {@code mDstB} with an W by H oval
         * created by our method {@code makeDst} (since W=H=64 they are actually a square and a circle
         * respectively). We create {@code Bitmap bm} to be a 2 by 2 image using the colors white, gray,
         * gray, white for the four pixels. Then we initialize our field {@code Shader mBG} to be a
         * {@code BitmapShader} formed from {@code bm} using a tile mode of REPEAT for both the x and
         * y axis. We create {@code Matrix m}, set it to scale by 6 (both x and y), then set the local
         * matrix of {@code Shader mBG} to it.
         *
         * @param context {@code Context} to use to access resources, "this" in the {@code onCreate}
         *                method of {@code Xfermodes}
         */
        public SampleView(Context context) {
            super(context);

            SCREEN_DENSITY = getResources().getDisplayMetrics().density;
            mSrcB = makeSrc(W, H);
            mDstB = makeDst(W, H);

            // make a checkerboard pattern
            Bitmap bm = Bitmap.createBitmap(new int[]{0xFFFFFFFF, 0xFFCCCCCC, 0xFFCCCCCC, 0xFFFFFFFF},
                    2, 2, Bitmap.Config.RGB_565);
            mBG = new BitmapShader(bm, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            Matrix m = new Matrix();
            m.setScale(6, 6);
            mBG.setLocalMatrix(m);
        }

        /**
         * We implement this to do our drawing. First we set the entire {@code Canvas canvas} to the
         * color WHITE, and scale {@code canvas} by SCREEN_DENSITY in both x and y directions. We
         * allocate {@code Paint labelP} with its antialias flag set, and set its text alignment to
         * CENTER. We allocate {@code Paint paint} and clear its FILTER_BITMAP_FLAG.
         *
         * We move the canvas to (15,35) and initialize {@code int x} and {@code int y} both to 0.
         * Now we loop through the 16 {@code Xfermode[] sModes} using {@code int i} as the index.
         * We set the style of {@code Paint paint} to STROKE, set its shader to null, and use it to
         * draw a rectangle outline for the current xfermode example. Next we set the style of
         * {@code paint} to FILL, set its shader to {@code Shader mBG} and draw a checkerboard rect
         * for the background of the example. We call the method {@code canvas.saveLayer} saving its
         * save level in {@code int sc} which allocates and redirects drawing to an offscreen bitmap
         * with bounds of a rectangle whose top left corner is taken from the point (x,y) of the
         * {@code Canvas canvas}, and whose bottom right corner is (x+W,y+H) (the {@code paint} argument
         * is null so no {@code Paint} will be applied when the offscreen bitmap is copied back to the
         * canvas when the matching {@code restoreToCount} is called). We move the {@code Canvas canvas}
         * to (x,y), and draw the {@code Bitmap mDstB} to it at (0,0) using {@code Paint paint}. We set
         * the xfermode of {@code paint} to the {@code Xfermode[] sModes} currently indexed by {@code i},
         * then use {@code paint} to draw the {@code Bitmap mSrcB} to the canvas at (0,0). We set the
         * xfermode of {@code paint} back to null, and call {@code canvas.restoreToCount(sc)} to restore
         * the canvas state to that is held before our {@code saveLayer} call and to copy the offscreen
         * bitmap to the onscreen canvas. Next we draw the label from {@code String[] sLabels} which
         * describes the {@code i}'th PorterDuff xfermode in the proper place. Finally we add {@code W+10}
         * to {@code x} and if we have reached the end of a row (as defined by {@code ROW_MAX}) we set
         * {@code x} to 0 and add {@code H+30} to {@code y}, then loop back for the next PorterDuff
         * xfermode in {@code Xfermode[] sModes}.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.WHITE);

            canvas.scale(SCREEN_DENSITY, SCREEN_DENSITY);
            @SuppressLint("DrawAllocation")
            Paint labelP = new Paint(Paint.ANTI_ALIAS_FLAG);
            labelP.setTextAlign(Paint.Align.CENTER);

            @SuppressLint("DrawAllocation")
            Paint paint = new Paint();
            paint.setFilterBitmap(false);

            canvas.translate(15, 35);

            int x = 0;
            int y = 0;
            for (int i = 0; i < sModes.length; i++) {
                // draw the border
                paint.setStyle(Paint.Style.STROKE);
                paint.setShader(null);
                canvas.drawRect(x - 0.5f, y - 0.5f, x + W + 0.5f, y + H + 0.5f, paint);

                // draw the checker-board pattern
                paint.setStyle(Paint.Style.FILL);
                paint.setShader(mBG);
                canvas.drawRect(x, y, x + W, y + H, paint);

                // draw the src/dst example into our offscreen bitmap
                @SuppressLint("WrongConstant")
                int sc = canvas.saveLayer(x, y, x + W, y + H, null,
                        Canvas.MATRIX_SAVE_FLAG |
                                Canvas.CLIP_SAVE_FLAG |
                                Canvas.HAS_ALPHA_LAYER_SAVE_FLAG |
                                Canvas.FULL_COLOR_LAYER_SAVE_FLAG |
                                Canvas.CLIP_TO_LAYER_SAVE_FLAG);
                canvas.translate(x, y);
                canvas.drawBitmap(mDstB, 0, 0, paint);
                paint.setXfermode(sModes[i]);
                canvas.drawBitmap(mSrcB, 0, 0, paint);
                paint.setXfermode(null);
                canvas.restoreToCount(sc);

                // draw the label
                canvas.drawText(sLabels[i], x + W / 2, y - labelP.getTextSize() / 2, labelP);

                x += W + 10;

                // wrap around when we've drawn enough for one row
                if ((i % ROW_MAX) == ROW_MAX - 1) {
                    x = 0;
                    y += H + 30;
                }
            }
        }
    }
}


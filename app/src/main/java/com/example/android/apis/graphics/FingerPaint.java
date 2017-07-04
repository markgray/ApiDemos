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
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

/**
 * Shows off some Canvas drawing methods and {@code View.onTouchEvent} usage.
 * {@code ColorPickerDialog} produces a color wheel which is too small
 * TODO: fix size of ColorPickerDialog
 */
public class FingerPaint extends GraphicsActivity
        implements ColorPickerDialog.OnColorChangedListener {
    /**
     * Current {@code Paint} to use for drawing loci of finger movements.
     */
    private Paint mPaint;
    /**
     * An {@code EmbossMaskFilter} which can be added to {@code mPaint} using the options menu,
     * applies a shadow like effect to the line being drawn.
     */
    private MaskFilter mEmboss;
    /**
     * A {@code BlurMaskFilter} which can be added to {@code mPaint} using the options menu,
     * applies a blur effect to the line being drawn.
     */
    private MaskFilter mBlur;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to an instance of {@code MyView}. Next we
     * allocate a {@code Paint mPaint}, set its antialias flag, set its dither flag, set its color
     * to RED, set the style to STROKE, set the stroke join to ROUND, set its stroke cap to ROUND,
     * and set its stroke width to 12. We initialize {@code MaskFilter mEmboss} with an instance of
     * {@code EmbossMaskFilter} configured to use a light source direction of (1,1,1), ambient light
     * value of 0.4f,coefficient for specular highlights of 6.0, and blur before lighting of 3.5f.
     * <p>
     * Finally we initialize our field {@code MaskFilter mBlur} with a {@code BlurMaskFilter} configured
     * to use a blur radius of 8.0f, and a blur type of NORMAL.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new MyView(this));

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFF0000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);

        mEmboss = new EmbossMaskFilter(new float[]{1, 1, 1},
                0.4f, 6, 3.5f);

        mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
    }

    /**
     * This method will be called when the user has chosen a new color using {@code ColorPickerDialog}.
     * We simply set the color of {@code Paint mPaint} to the {@code color} the user chose using the
     * dialog.
     *
     * @param color new color chosen by user using {@code ColorPickerDialog}
     */
    public void colorChanged(int color) {
        mPaint.setColor(color);
    }

    /**
     * Custom View which displays the loci drawn by the user's finger.
     */
    public class MyView extends View {

        @SuppressWarnings("unused")
        private static final float MINP = 0.25f;
        @SuppressWarnings("unused")
        private static final float MAXP = 0.75f;

        /**
         * {@code Bitmap} which is used to save all lines drawn. It is updated with the latest
         * {@code Path mPath} in method {@code touch_up} every time we receive the event
         * MotionEvent.ACTION_UP by drawing to {@code Canvas mCanvas} (which is a {@code Canvas}
         * created from {@code mBitmap}) and used in our {@code onDraw} override to draw the old
         * lines before drawing the current {@code Path mPath}.
         */
        private Bitmap mBitmap;
        /**
         * {@code Canvas} for the {@code Bitmap mBitmap} created in {@code onSizeChanged} that allows
         * us to draw into {@code mBitmap}
         */
        private Canvas mCanvas;
        /**
         * 
         */
        private Path mPath;
        private Paint mBitmapPaint;

        public MyView(Context c) {
            super(c);

            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(0xFFAAAAAA);

            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

            canvas.drawPath(mPath, mPaint);
        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }

        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;
            }
        }

        private void touch_up() {
            mPath.lineTo(mX, mY);
            // commit the path to our offscreen
            mCanvas.drawPath(mPath, mPaint);
            // kill this so we don't double draw
            mPath.reset();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }
    }

    private static final int COLOR_MENU_ID = Menu.FIRST;
    private static final int EMBOSS_MENU_ID = Menu.FIRST + 1;
    private static final int BLUR_MENU_ID = Menu.FIRST + 2;
    private static final int ERASE_MENU_ID = Menu.FIRST + 3;
    private static final int SRCATOP_MENU_ID = Menu.FIRST + 4;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, COLOR_MENU_ID, 0, "Color").setShortcut('3', 'c');
        menu.add(0, EMBOSS_MENU_ID, 0, "Emboss").setShortcut('4', 's');
        menu.add(0, BLUR_MENU_ID, 0, "Blur").setShortcut('5', 'z');
        menu.add(0, ERASE_MENU_ID, 0, "Erase").setShortcut('5', 'z');
        menu.add(0, SRCATOP_MENU_ID, 0, "SrcATop").setShortcut('5', 'z');

        /*   Is this the mechanism to extend with filter effects?
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(
                              Menu.ALTERNATIVE, 0,
                              new ComponentName(this, NotesList.class),
                              null, intent, 0, null);
        */
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xFF);

        switch (item.getItemId()) {
            case COLOR_MENU_ID:
                new ColorPickerDialog(this, this, mPaint.getColor()).show();
                return true;
            case EMBOSS_MENU_ID:
                if (mPaint.getMaskFilter() != mEmboss) {
                    mPaint.setMaskFilter(mEmboss);
                } else {
                    mPaint.setMaskFilter(null);
                }
                return true;
            case BLUR_MENU_ID:
                if (mPaint.getMaskFilter() != mBlur) {
                    mPaint.setMaskFilter(mBlur);
                } else {
                    mPaint.setMaskFilter(null);
                }
                return true;
            case ERASE_MENU_ID:
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                return true;
            case SRCATOP_MENU_ID:
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
                mPaint.setAlpha(0x80);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

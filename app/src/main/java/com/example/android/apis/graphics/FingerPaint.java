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
         * {@code Path} traced by user's finger, collected from {@code MotionEvent}'s received in our
         * {@code MyView.onTouchEvent} method.
         */
        private Path mPath;
        /**
         * {@code Paint} used to draw {@code Bitmap mBitmap} (the accumulated finger loci tracing)
         * used in our {@code onDraw} override.
         */
        private Paint mBitmapPaint;

        /**
         * Basic constructor for {@code MyView}, first we call through to our super's constructor,
         * then we initialize our field {@code Path mPath} with a new instance of {@code Path}, and
         * our field {@code Paint mBitmapPaint} with a {@code Paint} with the DITHER_FLAG set.
         *
         * @param c {@code Context} to use for resources, "this" {@code FingerPaint} activity when
         *          called from {@code onCreate} in our case
         */
        public MyView(Context c) {
            super(c);

            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        }

        /**
         * This is called during layout when the size of this view has changed. If
         * you were just added to the view hierarchy, you're called with the old
         * values of 0.
         * <p>
         * First we call through to our super's implementation of {@code onSizeChanged}, then we set
         * out field {@code Bitmap mBitmap} to a w by h {@code Bitmap} with a config of ARGB_8888.
         * Finally we set our field {@code Canvas mCanvas} to a canvas that can be used to draw into
         * the bitmap {@code mBitmap}.
         *
         * @param w    Current width of this view.
         * @param h    Current height of this view.
         * @param oldw Old width of this view.
         * @param oldh Old height of this view.
         */
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

        /**
         * We implement this to do our drawing. First we fill the entire {@code Canvas canvas} with
         * the color 0xFFAAAAAA (a light gray), then we draw {@code Bitmap mBitmap} (our accumulated
         * finger tracing lines) using {@code Paint mBitmapPaint}, and finally we draw the current
         * finger loci being built in {@code Path mPath} using {@code Paint mPaint}.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(0xFFAAAAAA);

            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

            canvas.drawPath(mPath, mPaint);
        }

        /**
         * Current location of the finger
         */
        private float mX, mY;
        /**
         * Finger movements below this value are ignored.
         */
        private static final float TOUCH_TOLERANCE = 4;

        /**
         * Called when our {@code onTouchEvent} override receives a ACTION_DOWN motion event. First
         * we clear all lines and curves from our current finger loci {@code Path mPath} making it
         * empty. Then we set the beginning of the next contour of {@code mPath} to (x,y), and save
         * the position in our fields {@code mX} and {@code mY}.
         *
         * @param x x coordinate of the {@code MotionEvent}
         * @param y y coordinate of the {@code MotionEvent}
         */
        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }

        /**
         * Called when our {@code onTouchEvent} override receives a ACTION_MOVE motion event. First
         * we calculate how far the motion event moved {@code dx} from {@code mX} in the x direction
         * and {@code dy} fro {@code mY} in the y direction and if either of these is greater than
         * or equal to TOUCH_TOLERANCE we add a quadratic bezier from the last point {@code mPath}
         * was moved to, approaching control point (mX,mY), and ending at the point given by
         * [(x+mX)/2, (y+mY)/2]. We then save (x,y) in our fields mX and mY respectively.
         *
         * @param x x coordinate of the {@code MotionEvent}
         * @param y y coordinate of the {@code MotionEvent}
         */
        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;
            }
        }

        /**
         * Called when our {@code onTouchEvent} override receives an ACTION_UP motion event. First we
         * add a line to {@code Path mPath} from the last point to the point (mX,mY), then we commit
         * the {@code Path mPath} to our offscreen {@code Bitmap mBitmap} by writing to it using
         * {@code Canvas mCanvas}, then we clear all lines and curves from our current finger loci
         * {@code Path mPath} making it empty.
         */
        private void touch_up() {
            mPath.lineTo(mX, mY);
            // commit the path to our offscreen
            mCanvas.drawPath(mPath, mPaint);
            // kill this so we don't double draw
            mPath.reset();
        }

        /**
         * We implement this method to handle touch screen motion events. First we fetch the x
         * coordinate of the {@code MotionEvent event} to {@code float x} and the y coordinate of
         * the {@code MotionEvent event} to {@code float y}. Then we switch based on the action
         * that is being reported in {@code event}:
         * <ul>
         * <li>
         * ACTION_DOWN - we call our method {@code touch_start} with the coordinate (x,y) in
         * order to begin recording a new loci of finger tracings, then call {@code invalidate}
         * to request that our view be redrawn.
         * </li>
         * <li>
         * ACTION_MOVE - we call our method {@code touch_move} with the coordinate (x,y) in
         * order to draw a bezier curve from the last location to this new location, then
         * call {@code invalidate} to request that our view be redrawn.
         * </li>
         * <li>
         * ACTION_UP - we call our method {@code touch_up} which finishes {@code Path mPath}
         * by drawing a line to our last point at (mX,mY), commits {@code mPath} to our
         * offscreen accumulated finger tracings contained in {@code Bitmap mBitmap} and
         * empties {@code mPath}, then call {@code invalidate} to request that our view be
         * redrawn.
         * </li>
         * </ul>
         * Finally we return true to the caller to indicate that we have consumed the {@code MotionEvent}.
         *
         * @param event The motion event.
         * @return True if the event was handled, false otherwise.
         */
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

    /**
     * Menu ID for our "Color" option
     */
    private static final int COLOR_MENU_ID = Menu.FIRST;
    /**
     * Menu ID for our "Emboss" option
     */
    private static final int EMBOSS_MENU_ID = Menu.FIRST + 1;
    /**
     * Menu ID for our "Blur" option
     */
    private static final int BLUR_MENU_ID = Menu.FIRST + 2;
    /**
     * Menu ID for our "Erase" option
     */
    private static final int ERASE_MENU_ID = Menu.FIRST + 3;
    /**
     * Menu ID for our "SrcATop" option
     */
    private static final int SRCATOP_MENU_ID = Menu.FIRST + 4;

    /**
     * Initialize the contents of the Activity's standard options menu. First we call through to our
     * super's implementation of {@code onCreateOptionsMenu}. Then we add our menu options to
     * {@code Menu menu}:
     * <ul>
     * <li>
     * COLOR_MENU_ID - "Color" allows the user to select a color
     * </li>
     * <li>
     * EMBOSS_MENU_ID - "Emboss" allows you to toggle the usage of the {@code EmbossMaskFilter}
     * </li>
     * <li>
     * BLUR_MENU_ID - "Blur" allows you to toggle the usage of the {@code BlurMaskFilter}
     * </li>
     * <li>
     * ERASE_MENU_ID - "Erase" sets the Porter-Duff transfer mode to CLEAR
     * </li>
     * <li>
     * SRCATOP_MENU_ID - "SrcATop" sets the Porter-Duff transfer mode to SRC_ATOP
     * </li>
     * </ul>
     * Finally we return true so the menu will be displayed.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown
     */
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

    /**
     * Prepare the Screen's standard options menu to be displayed.  This is
     * called right before the menu is shown, every time it is shown.  You can
     * use this method to efficiently enable/disable items or otherwise
     * dynamically modify the contents.
     * <p>
     * We simply call through to our super's implementation of {@code onPrepareOptionsMenu} and return
     * true so the menu will be displayed
     *
     * @param menu The options menu as last shown or first initialized by
     *             onCreateOptionsMenu().
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     * <p>
     * First we clear the previous Porter-Duff transfer mode of {@code Paint mPaint}, and set the
     * alpha to its max. Then we switch on the item ID of {@code MenuItem item}:
     * <ul>
     * <li>
     * COLOR_MENU_ID - "Color" allows the user to select a color. We create and {@code show} an
     * instance of {@code ColorPickerDialog} which will allow the user to select a new color using
     * a "color wheel" and upon selection our method {@code colorChanged} will be called with that
     * selection which it will use to set the color of {@code Paint mPaint}.
     * </li>
     * <li>
     * EMBOSS_MENU_ID - "Emboss" allows you to toggle the usage of the {@code EmbossMaskFilter}. If
     * the current {@code MaskFilter} for {@code Paint mPaint} is not {@code MaskFilter mEmboss} we
     * set the {@code MaskFilter} to {@code mEmboss}, and if it is we set it to null.
     * </li>
     * <li>
     * BLUR_MENU_ID - "Blur" allows you to toggle the usage of the {@code BlurMaskFilter}. If
     * the current {@code MaskFilter} for {@code Paint mPaint} is not {@code MaskFilter mBlur} we
     * set the {@code MaskFilter} to {@code mBlur}, and if it is we set it to null.
     * </li>
     * <li>
     * ERASE_MENU_ID - "Erase" sets the Porter-Duff transfer mode to CLEAR
     * </li>
     * <li>
     * SRCATOP_MENU_ID - "SrcATop" sets the Porter-Duff transfer mode to SRC_ATOP, and sets the
     * alpha component to the max value.
     * </li>
     * </ul>
     * Finally we return the value returned by our super's implementation of {@code onOptionsItemSelected}
     * to the caller
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     */
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

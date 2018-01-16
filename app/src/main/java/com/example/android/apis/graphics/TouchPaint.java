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
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

/**
 * Demonstrates the handling of touch screen, stylus, mouse and trackball events to
 * implement a simple painting app.
 * <p>
 * Drawing with a touch screen is accomplished by drawing a point at the
 * location of the touch. When pressure information is available, it is used
 * to change the intensity of the color. When size and orientation information
 * is available, it is used to directly adjust the size and orientation of the
 * brush.
 * <p>
 * Drawing with a stylus is similar to drawing with a touch screen, with a
 * few added refinements. First, there may be multiple tools available including
 * an eraser tool.  Second, the tilt angle and orientation of the stylus can be
 * used to control the direction of paint. Third, the stylus buttons can be used
 * to perform various actions. Here we use one button to cycle colors and the
 * other to airbrush from a distance.
 * <p>
 * Drawing with a mouse is similar to drawing with a touch screen, but as with
 * a stylus we have extra buttons. Here we use the primary button to draw,
 * the secondary button to cycle colors and the tertiary button to airbrush.
 * <p>
 * Drawing with a trackball is a simple matter of using the relative motions
 * of the trackball to move the paint brush around. The trackball may also
 * have a button, which we use to cycle through colors.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TouchPaint extends GraphicsActivity {
    /**
     * Used as a pulse to gradually fade the contents of the window.
     */
    private static final int MSG_FADE = 1;

    /**
     * Menu ID for the command to clear the window.
     */
    private static final int CLEAR_ID = Menu.FIRST;

    /**
     * Menu ID for the command to toggle fading.
     */
    private static final int FADE_ID = Menu.FIRST + 1;

    /**
     * How often to fade the contents of the window (in ms).
     */
    private static final int FADE_DELAY = 100;

    /**
     * Colors to cycle through.
     */
    static final int[] COLORS = new int[]{
            Color.WHITE, Color.RED, Color.YELLOW, Color.GREEN,
            Color.CYAN, Color.BLUE, Color.MAGENTA,
    };

    /**
     * Background color.
     */
    static final int BACKGROUND_COLOR = Color.BLACK;

    /**
     * The view responsible for drawing the window.
     */
    PaintView mView;

    /**
     * Is fading mode enabled?
     */
    boolean mFading;

    /**
     * {@code Handler} which calls the {@code fade} method of {@code PaintView mView} every FADE_DELAY
     * (100ms) to "fade" the finger painting.
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // Upon receiving the fade pulse, we have the view perform a
                // fade and then enqueue a new message to pulse at the desired
                // next time.
                case MSG_FADE: {
                    mView.fade();
                    scheduleFade();
                    break;
                }
                default:
                    super.handleMessage(msg);
            }
        }
    };

    /**
     * Mode we are painting in:
     * <ul>
     * <li>Draw - we draw a loci of the movement of the finger</li>
     * <li>Splat - random splatter across the canvas ala Jackson Pollock</li>
     * <li>Erase - erases where the finger moves</li>
     * </ul>
     * Without a keyboard, only Draw is used.
     */
    enum PaintMode {
        Draw,
        Splat,
        Erase,
    }

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, initialize our field {@code PaintView mView} with a new instance of
     * {@code PaintView}, set our content view to it, and enable it to receive focus. Then if our
     * parameter {@code savedInstanceState} is not null we set our field {@code boolean mFading} with
     * the value stored in {@code savedInstanceState} under the key "fading" (defaulting to true if
     * no value was found), and we set the field {@code mColorIndex} of our {@code PaintView mView}
     * to the value saved under the key "color" (defaulting to 0 if no value was found). If our
     * parameter {@code savedInstanceState} is null (first time running) we set {@code boolean mFading}
     * to true, and the field {@code mColorIndex} of our {@code PaintView mView} to 0.
     *
     * @param savedInstanceState In our {@code onSaveInstanceState} we save the value of our field
     *                           {@code mFading} and the value of the field {@code mColorIndex} of our
     *                           {@code PaintView mView} under the keys "fading" and "color" respectively,
     *                           so if {@code savedInstanceState} is not null, we restore them here.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create and attach the view that is responsible for painting.
        mView = new PaintView(this);
        setContentView(mView);
        mView.requestFocus();

        // Restore the fading option if we are being thawed from a
        // previously saved state.  Note that we are not currently remembering
        // the contents of the bitmap.
        if (savedInstanceState != null) {
            mFading = savedInstanceState.getBoolean("fading", true);
            mView.mColorIndex = savedInstanceState.getInt("color", 0);
        } else {
            mFading = true;
            mView.mColorIndex = 0;
        }
    }

    /**
     * We initialize the contents of the Activity's standard options menu here, adding our menu items
     * to the {@code Menu menu} parameter. We add a menu item with the id CLEAR_ID and the title
     * "Clear" to {@code menu}, and a menu item with the id FADE_ID and the title "Fade" to {@code menu}
     * setting its checkable state to true. Finally we return the value returned by our super's
     * implementation {@code onCreateOptionsMenu}.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed; if you return false it will not
     * be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, CLEAR_ID, 0, "Clear");
        menu.add(0, FADE_ID, 0, "Fade").setCheckable(true);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * We prepare the Screen's standard options menu to be displayed here. We find our item with the
     * id FADE_ID in our parameter {@code Menu menu} and set its checkable state to the value of our
     * field {@code boolean mFading}. Then we return the value returned by our super's implementation
     * {@code onPrepareOptionsMenu} to our caller.
     *
     * @param menu The options menu as last shown or first initialized by onCreateOptionsMenu().
     * @return You must return true for the menu to be displayed; if you return false it will not be shown.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(FADE_ID).setChecked(mFading);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * This hook is called whenever an item in our options menu is selected. We switch on the value
     * of the identifier of our parameter {@code MenuItem item}:
     * <ul>
     * <li>
     * CLEAR_ID - we call the {@code clear} method of {@code PaintView mView} then return true
     * </li>
     * <li>
     * FADE_ID - we toggle the value of our field {@code boolean mFading}, and if the new value is
     * true we call our method {@code startFading}, if false we call our method {@code stopFading}.
     * In either case we return true to our caller.
     * </li>
     * <li>
     * default - we return the value returned by our super's implementation of {@code onOptionsItemSelected}
     * </li>
     * </ul>
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case CLEAR_ID:
                mView.clear();
                return true;
            case FADE_ID:
                mFading = !mFading;
                if (mFading) {
                    startFading();
                } else {
                    stopFading();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Called after {@link #onRestoreInstanceState}, {@link #onRestart}, or {@link #onPause}, for
     * our activity to start interacting with the user. First we call through to our super's
     * implementation of {@code onResume}, then if our field {@code boolean mFading} is true we call
     * our method {@code startFading}.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // If fading mode is enabled, then as long as we are resumed we want
        // to run pulse to fade the contents.
        if (mFading) {
            startFading();
        }
    }

    /**
     * Called to retrieve per-instance state from an activity before being killed so that the state
     * can be restored in {@link #onCreate} or {@link #onRestoreInstanceState} (the {@link Bundle}
     * populated by this method will be passed to both). First we call through to our super's
     * implementation of {@code onSaveInstanceState}, then we save the value of our field
     * {@code boolean mFading} in our parameter {@code Bundle outState} under the key "fading", and
     * the value of the field {@code mColorIndex} of our {@code PaintView mView} under the key "color".
     *
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save away the fading state to restore if needed later.  Note that
        // we do not currently save the contents of the display.
        outState.putBoolean("fading", mFading);
        outState.putInt("color", mView.mColorIndex);
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. First we call through to our super's implementation of {@code onPause},
     * then we call our method {@code stopFading} to stop the pulse that fades the screen.
     */
    @Override
    protected void onPause() {
        super.onPause();

        // Make sure to never run the fading pulse while we are paused or
        // stopped.
        stopFading();
    }

    /**
     * Start up the pulse to fade the screen, first clearing any existing pulse to ensure that we
     * don't have multiple pulses running at a time. Then we call our method {@code scheduleFade} to
     * schedule a new FADE_DELAY message to our {@code Handler mHandler}.
     */
    void startFading() {
        mHandler.removeMessages(MSG_FADE);
        scheduleFade();
    }

    /**
     * Stop the pulse to fade the screen. To do this we simply remove all of the MSG_FADE messages
     * in the queue of {@code Handler mHandler}.
     */
    void stopFading() {
        mHandler.removeMessages(MSG_FADE);
    }

    /**
     * Schedule a fade message for later. We simply call the {@code sendMessageDelayed} method of
     * {@code Handler mHandler} to send a MSG_FADE message with a delay of FADE_DELAY (100ms).
     */
    void scheduleFade() {
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_FADE), FADE_DELAY);
    }

    /**
     * This view implements the drawing canvas.
     * <p>
     * It handles all of the input events and drawing functions.
     */
    public static class PaintView extends View {
        /**
         * Alpha used by {@code Paint mFadePaint} to fade the finger painting.
         */
        private static final int FADE_ALPHA = 0x06;
        /**
         * Maximum number of times our fade thread is run (89).
         */
        private static final int MAX_FADE_STEPS = 256 / (FADE_ALPHA / 2) + 4;
        /**
         * Constant used by the method {@code onTrackballEvent} to multiply the value returned by
         * {@code getXPrecision} and {@code getYPrecision} to scale the precision of the coordinates
         * being reported by {@code getX} and {@code getY} (as well as the values returned by
         * {@code getHistoricalX} and {@code getHistoricalY}) when a trackball is used to "finger
         * paint" using the {@code moveTrackball} method.
         */
        private static final int TRACKBALL_SCALE = 10;

        /**
         * Number of random splat vectors generated and drawn by the method {@code drawSplat}.
         */
        private static final int SPLAT_VECTORS = 40;

        /**
         * Random number generator used by the {@code drawSplat} method to create random splat vectors.
         */
        private final Random mRandom = new Random();
        /**
         * Bitmap used by {@code Canvas mCanvas} to draw into. Our method {@code onSizeChanged} also
         * uses it to remember what has been drawn when the size of the window changes.
         */
        private Bitmap mBitmap;
        /**
         * {@code Canvas} we draw on, and when our {@code onDraw} method is called we draw the
         * {Bitmap mBitmap} that {@code mCanvas} draws into the {@code Canvas canvas} passed as
         * a parameter to {@code onDraw} (our view's {@code Canvas}).
         */
        private Canvas mCanvas;
        /**
         * {@code Paint} we use to draw with.
         */
        private final Paint mPaint = new Paint();
        /**
         * {@code Paint} our fade thread uses to "fade" the finger painting.
         */
        private final Paint mFadePaint = new Paint();
        /**
         * Last known X coordinate of a move by finger or trackball.
         */
        private float mCurX;
        /**
         * Last known Y coordinate of a move by finger or trackball.
         */
        private float mCurY;
        /**
         * Old state of all buttons that are pressed such as a mouse or stylus button, used to tell
         * when one of the button has changed state.
         */
        private int mOldButtonState;
        /**
         * Number of times the fade thread {@code Handler mHandler} has called our method {@code fade}
         * to fade our finger painting. When it reaches MAX_FADE_STEPS our {@code fade} method stops
         * "fading". It is set to 0 to start fading again when our methods {@code paint} and {@code text}
         * are called.
         */
        private int mFadeSteps = MAX_FADE_STEPS;

        /**
         * The index of the current color to use.
         */
        int mColorIndex;

        /**
         * Our constructor. First we call our super's constructor, then we call our method {@code init}
         * to initialize our instance.
         *
         * @param c {@code Context} to use to access resources, "this" in the {@code onCreate} method
         *          of {@code TouchPaint}
         */
        public PaintView(Context c) {
            super(c);
            init();
        }

        /**
         * Constructor that is called when our view is inflated from xml. {@code GameActivity.Content}
         * extends us, and it is inflated in the layout file of {@code GameActivity} R.layout.game.
         * First we call through to our super's constructor, then we call our method {@code init} to
         * initialize our instance.
         *
         * @param c     {@code Context} our view is running in, through which we can access the current
         *              theme, resources, etc.
         * @param attrs attributes of the XML tag that is inflating this view.
         */
        public PaintView(Context c, AttributeSet attrs) {
            super(c, attrs);
            init();
        }

        /**
         * Our initialization method, called from our constructors. First we enable our view to receive
         * focus, then we set the anti alias flag of {@code Paint mPaint}, set the color of
         * {@code Paint mFadePaint} to BACKGROUND_COLOR ({@code Color.BLACK}, and set its alpha to
         * FADE_ALPHA (0x06).
         */
        private void init() {
            setFocusable(true);

            mPaint.setAntiAlias(true);

            mFadePaint.setColor(BACKGROUND_COLOR);
            mFadePaint.setAlpha(FADE_ALPHA);
        }

        /**
         * Clears the {@code Canvas mCanvas}. If {@code mCanvas} is not null, we set the color of
         * {@code Paint mPaint} to BACKGROUND_COLOR ({@code Color.BLACK}), fill the entire {@code mCanvas}
         * to the color of {@code mPaint}, call invalidate to schedule {@code onDraw} to be called
         * to copy {@code mCanvas} to the view's {@code Canvas}, and finally set {@code mFadeSteps}
         * to MAX_FADE_STEPS (89).
         */
        public void clear() {
            if (mCanvas != null) {
                mPaint.setColor(BACKGROUND_COLOR);
                mCanvas.drawPaint(mPaint);
                invalidate();

                mFadeSteps = MAX_FADE_STEPS;
            }
        }

        /**
         * "Fades" the {@code Canvas mCanvas}. If {@code mCanvas} is not null, and if {@code mFadeSteps}
         * is less than MAX_FADE_STEPS (89) we fill the entire {@code mCanvas} with {@code Paint mFadePaint},
         * and call invalidate so {@code onDraw} will be called to copy {@code mCanvas} to the view's
         * {@code Canvas}. Finally we increment {@code mFadeSteps}.
         */
        public void fade() {
            if (mCanvas != null && mFadeSteps < MAX_FADE_STEPS) {
                mCanvas.drawPaint(mFadePaint);
                invalidate();

                mFadeSteps++;
            }
        }

        /**
         * Draws the {@code String text} to {@code Canvas mCanvas} and causes {@code onDraw} to copy
         * {@code mCanvas} to the view's {@code Canvas}. Before doing anything, we make sure that
         * {@code Bitmap mBitmap} is not null, returning having done nothing if it is null. Otherwise
         * we set {@code int width} to the width of {@code mBitmap}, and {@code int height} to the
         * height of {@code mBitmap}. We set the color of {@code Paint mPaint} to the color currently
         * selected by {@code COLORS[mColorIndex]}, and its alpha to 255. We set {@code int size} to
         * {@code height}, and set the text size of {@code mPaint} to {@code size}. We create a
         * {@code Rect bounds}, and fetch the text bounds of {@code String text} drawn using {@code mPaint}
         * to {@code bounds}. We set {@code int twidth} to the width of {@code bounds}, then increment
         * it by one quarter of itself. If {@code twidth} is greater than {@code width}, we set size
         * to {@code (size*width)/twidth}, set the text size of {@code mPaint} to {@code size}, and
         * retrieve the text bounds of {@code text} drawn using {@code mPaint} to {@code bounds}. We
         * fetch the font metrics of {@code mPaint} to {@code Paint.FontMetrics fm}, so that we can
         * use the {@code fm.ascent} field. We then call the {@code mCanvas.drawText} method to draw
         * the {@code String text} using {@code Paint mPaint} with the x coordinate calculated to
         * center the text in the middle of the {@code Canvas}, and the y coordinate calculated to
         * position the text in a weird part of the screen (probably a bug?). We set {@code mFadeSteps}
         * to 0 so that fading will start again, and call {@code invalidate} so that a call to our
         * {@code onDraw} method will be scheduled to copy {@code mCanvas} to the view's {@code Canvas}.
         *
         * @param text String to display
         */
        public void text(String text) {
            if (mBitmap != null) {
                final int width = mBitmap.getWidth();
                final int height = mBitmap.getHeight();
                mPaint.setColor(COLORS[mColorIndex]);
                mPaint.setAlpha(255);
                int size = height;
                mPaint.setTextSize(size);
                Rect bounds = new Rect();
                mPaint.getTextBounds(text, 0, text.length(), bounds);
                int twidth = bounds.width();
                twidth += (twidth / 4);
                if (twidth > width) {
                    size = (size * width) / twidth;
                    mPaint.setTextSize(size);
                    mPaint.getTextBounds(text, 0, text.length(), bounds);
                }
                Paint.FontMetrics fm = mPaint.getFontMetrics();
                mCanvas.drawText(text, (width - bounds.width()) / 2, ((height - size) / 2) - fm.ascent, mPaint);
                mFadeSteps = 0;
                invalidate();
            }
        }

        /**
         * This is called during layout when the size of this view has changed. If you were just added
         * to the view hierarchy, you're called with the old values of 0. If {@code Bitmap mBitmap} is
         * not null we set {@code int curW} to the width of {@code mBitmap} and {@code int curH} to
         * the height of {@code mBitmap}, if it is null we set them both to 0. If {@code curW} is
         * greater than or equal to {@code w} and {@code curH} is greater than or equal to {@code h}
         * we return having done nothing.
         * <p>
         * If {@code curW} is less than {@code w} we set it to {@code w}, and if {@code curH} is less
         * than {@code h} we set it to {@code h}. We create {@code Bitmap newBitmap} to be {@code curW}
         * by {@code curH} using the ARGB_8888 format. We allocate a new {@code Canvas newCanvas} and
         * set {@code newBitmap} to be the bitmap for it to draw into. If {@code Bitmap mBitmap} is
         * not null we draw it into {@code newCanvas} (this function will take care of automatically
         * scaling the bitmap to draw at the same density as the canvas). Then we set our fields
         * {@code Bitmap mBitmap} to {@code newBitmap}, and {@code Canvas mCanvas} to {@code newCanvas}.
         * <p>
         * Finally we set {@code mFadeSteps} to MAX_FADE_STEPS so that fading will pause until new
         * finger painting starts.
         *
         * @param w    Current width of this view.
         * @param h    Current height of this view.
         * @param oldw Old width of this view.
         * @param oldh Old height of this view.
         */
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            int curW = mBitmap != null ? mBitmap.getWidth() : 0;
            int curH = mBitmap != null ? mBitmap.getHeight() : 0;
            if (curW >= w && curH >= h) {
                return;
            }

            if (curW < w) curW = w;
            if (curH < h) curH = h;

            Bitmap newBitmap = Bitmap.createBitmap(curW, curH, Bitmap.Config.ARGB_8888);
            Canvas newCanvas = new Canvas();
            newCanvas.setBitmap(newBitmap);
            if (mBitmap != null) {
                newCanvas.drawBitmap(mBitmap, 0, 0, null);
            }
            mBitmap = newBitmap;
            mCanvas = newCanvas;
            mFadeSteps = MAX_FADE_STEPS;
        }

        /**
         * We implement this to do our drawing. If {@code Bitmap mBitmap} is not null we draw it to
         * our argument {@code Canvas canvas}.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        @Override
        protected void onDraw(Canvas canvas) {
            if (mBitmap != null) {
                canvas.drawBitmap(mBitmap, 0, 0, null);
            }
        }

        /**
         * We implement this method to handle trackball motion events. I do not have a trackball
         * connected to an Android device, so I will not comment.
         *
         * @param event The motion event.
         * @return True if the event was handled, false otherwise.
         */
        @Override
        public boolean onTrackballEvent(MotionEvent event) {
            final int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                // Advance color when the trackball button is pressed.
                advanceColor();
            }

            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                final int N = event.getHistorySize();
                final float scaleX = event.getXPrecision() * TRACKBALL_SCALE;
                final float scaleY = event.getYPrecision() * TRACKBALL_SCALE;
                for (int i = 0; i < N; i++) {
                    moveTrackball(event.getHistoricalX(i) * scaleX,
                            event.getHistoricalY(i) * scaleY);
                }
                moveTrackball(event.getX() * scaleX, event.getY() * scaleY);
            }
            return true;
        }

        /**
         * Adds the change in x and y to {@code mCurW} and {@code mCurH} and draws an oval at the new
         * point.
         *
         * @param deltaX X coordinate change
         * @param deltaY Y coordinate change
         */
        private void moveTrackball(float deltaX, float deltaY) {
            final int curW = mBitmap != null ? mBitmap.getWidth() : 0;
            final int curH = mBitmap != null ? mBitmap.getHeight() : 0;

            mCurX = Math.max(Math.min(mCurX + deltaX, curW - 1), 0);
            mCurY = Math.max(Math.min(mCurY + deltaY, curH - 1), 0);
            paint(PaintMode.Draw, mCurX, mCurY);
        }

        /**
         * We implement this method to handle touch screen motion events. We simply return the value
         * returned by our method {@code onTouchOrHoverEvent} when its {@code isTouch} argument is
         * true.
         *
         * @param event The motion event.
         * @return True if the event was handled, false otherwise.
         */
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            return onTouchOrHoverEvent(event, true /*isTouch*/);
        }

        /**
         * We implement this method to handle hover events. We simply return the value returned by
         * our method {@code onTouchOrHoverEvent} when its {@code isTouch} argument is false.
         *
         * @param event The motion event that describes the hover.
         * @return True if the view handled the hover event.
         */
        @Override
        public boolean onHoverEvent(MotionEvent event) {
            return onTouchOrHoverEvent(event, false /*isTouch*/);
        }

        /**
         * Handles both touch and hover events. First we fetch the state of all buttons that are
         * pressed to {@code buttonState}, and isolate the bits that have changed from the state saved
         * in {@code mOldButtonState} to {@code pressedButtons}. We then set {@code mOldButtonState}
         * to {@code buttonState}. If the newly pressed buttons in {@code pressedButtons} includes
         * the BUTTON_SECONDARY button, we call our method {@code advanceColor} to change the color
         * used to draw to the next one in line.
         * <p>
         * Next we declare {@code PaintMode mode}, and if the BUTTON_TERTIARY is pressed we set
         * {@code mode} to {@code PaintMode.Splat}, if the event is a touch event or BUTTON_PRIMARY
         * is pressed we set {@code mode} to {@code PaintMode.Draw}. Otherwise we return having done
         * nothing.
         * <p>
         * We initialize {@code action} to the masked action being performed in {@code event}. If the
         * {@code action} is ACTION_DOWN, or ACTION_MOVE, or ACTION_HOVER_MOVE we initialize {@code N}
         * to the number of historical points in {@code event}, and {@code P} to the number of pointers
         * of data contained in {@code event}. We loop through the {@code N} historical points, and
         * for each of the {@code P} pointers we call our method {@code paint} to paint an oval at the
         * historical (x,y) for the pointer and event index of the historical point in question, using
         * the tool type of the pointer to decide whether to use our current {@code mode} or to use
         * {@code PaintMode.Erase} if the tool type was TOOL_TYPE_ERASER. We also pass it the historical
         * pressure, historical touch major axis coordinate, historical touch minor axis coordinate,
         * historical orientation coordinate, historical value of the AXIS_DISTANCE axis, and the
         * historical value of the AXIS_TILT axis for the data point being processed.
         * <p>
         * Once we have painted all of the historical points, we do the same thing using the values
         * for the current data point, and set our fields {@code mCurX} and {@code mCurY} to the x
         * and y coordinates of the first pointer index. Then we return true to the caller whether
         * we had anything to draw or not.
         *
         * @param event   The motion event.
         * @param isTouch true if the event was a touch event, false if it was a hover event
         * @return True if the event was handled, false otherwise.
         */
        private boolean onTouchOrHoverEvent(MotionEvent event, boolean isTouch) {
            final int buttonState = event.getButtonState();
            int pressedButtons = buttonState & ~mOldButtonState;
            mOldButtonState = buttonState;

            if ((pressedButtons & MotionEvent.BUTTON_SECONDARY) != 0) {
                // Advance color when the right mouse button or first stylus button
                // is pressed.
                advanceColor();
            }

            PaintMode mode;
            if ((buttonState & MotionEvent.BUTTON_TERTIARY) != 0) {
                // Splat paint when the middle mouse button or second stylus button is pressed.
                mode = PaintMode.Splat;
            } else if (isTouch || (buttonState & MotionEvent.BUTTON_PRIMARY) != 0) {
                // Draw paint when touching or if the primary button is pressed.
                mode = PaintMode.Draw;
            } else {
                // Otherwise, do not paint anything.
                return false;
            }

            final int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE
                    || action == MotionEvent.ACTION_HOVER_MOVE) {
                final int N = event.getHistorySize();
                final int P = event.getPointerCount();
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < P; j++) {
                        paint(getPaintModeForTool(event.getToolType(j), mode),
                                event.getHistoricalX(j, i),
                                event.getHistoricalY(j, i),
                                event.getHistoricalPressure(j, i),
                                event.getHistoricalTouchMajor(j, i),
                                event.getHistoricalTouchMinor(j, i),
                                event.getHistoricalOrientation(j, i),
                                event.getHistoricalAxisValue(MotionEvent.AXIS_DISTANCE, j, i),
                                event.getHistoricalAxisValue(MotionEvent.AXIS_TILT, j, i));
                    }
                }
                for (int j = 0; j < P; j++) {
                    paint(getPaintModeForTool(event.getToolType(j), mode),
                            event.getX(j),
                            event.getY(j),
                            event.getPressure(j),
                            event.getTouchMajor(j),
                            event.getTouchMinor(j),
                            event.getOrientation(j),
                            event.getAxisValue(MotionEvent.AXIS_DISTANCE, j),
                            event.getAxisValue(MotionEvent.AXIS_TILT, j));
                }
                mCurX = event.getX();
                mCurY = event.getY();
            }
            return true;
        }

        /**
         * Returns the correct {@code PaintMode} to use to paint, either {@code PaintMode.Erase} is
         * the {@code toolType} is TOOL_TYPE_ERASER, or {@code defaultMode}.
         *
         * @param toolType    the type of tool used to make contact such as a finger or stylus, if known.
         * @param defaultMode {@code PaintMode} to return if the tool type is not TOOL_TYPE_ERASER
         * @return either {@code PaintMode.Erase} is the {@code toolType} is TOOL_TYPE_ERASER, otherwise
         * returns {@code defaultMode}.
         */
        private PaintMode getPaintModeForTool(int toolType, PaintMode defaultMode) {
            if (toolType == MotionEvent.TOOL_TYPE_ERASER) {
                return PaintMode.Erase;
            }
            return defaultMode;
        }

        /**
         * Increments {@code mColorIndex} modulo {@code COLORS.length}. {@code mColorIndex} is Used
         * to select a color from the array {@code int[] COLORS} in order to set the color of
         * {@code Paint mPaint}.
         */
        private void advanceColor() {
            mColorIndex = (mColorIndex + 1) % COLORS.length;
        }

        /**
         * Convenience method to call {@code paint} specifying only the {@code PaintMode mode},
         * {@code x} and {@code y} parameters. The other parameters are given default values.
         *
         * @param mode {@code PaintMode} to use, one of "Draw", "Erase", or "Splat".
         * @param x    x coordinate of oval to be drawn
         * @param y    y coordinate of oval to be drawn
         */
        private void paint(PaintMode mode, float x, float y) {
            paint(mode, x, y, 1.0f, 0, 0, 0, 0, 0);
        }

        /**
         * Draws an oval in the manner specified by its parameters. If our field {@code Bitmap mBitmap}
         * is not null we have a {@code Canvas mCanvas} that we can use to draw into {@code mBitmap}
         * and we proceed to do so. First we make sure that both of parameters {@code major} and
         * {@code minor} are greater than 0, and if not we set them to the default value 16. Then we
         * switch based on the value of our parameter {@code PaintMode mode}:
         * <ul>
         * <li>
         * Draw: we set the color of {@code Paint mPaint} to the color in the array {@code COLORS}
         * pointed to by {@code mColorIndex}, set its alpha to the lesser of {@code pressure*128}
         * and 255, then call our method {@code drawOval} to use {@code mPaint} to draw an oval
         * on {@code mCanvas} at ({@code x},{@code y}) with the size of the containing {@code RectF}
         * being {@code minor} by {@code major} in size, and rotated by {@code orientation} radians.
         * </li>
         * <li>
         * Erase: we set the color of {@code Paint mPaint} to the color BACKGROUND_COLOR, set its
         * alpha to the lesser of {@code pressure*128} and 255, then call our method {@code drawOval}
         * to use {@code mPaint} to draw an oval on {@code mCanvas} at ({@code x},{@code y}) with
         * the size of the containing {@code RectF} being {@code minor} by {@code major} in size,
         * and rotated by {@code orientation} radians.
         * </li>
         * <li>
         * Erase: we set the color of {@code Paint mPaint} to the color in the array {@code COLORS}
         * pointed to by {@code mColorIndex}, set its alpha to 64, and use our method {@code drawSplat}
         * to use {@code mPaint} "splatter" paint on {@code mCanvas} using the other parameters to
         * control where and how much paint is randomly splattered to the canvas.
         * </li>
         * Whether we did any drawing or not, we set {@code mFadeSteps} to 0, and invalidate the view
         * so that the current {@code mBitmap} (if it exists) will be drawn to the views canvas by
         * our {@code onDraw} method, and fading will start if it was stopped.
         * </ul>
         *
         * @param mode        {@code PaintMode} to use, one of "Draw", "Erase", or "Splat".
         * @param x           x coordinate of oval to be drawn
         * @param y           y coordinate of oval to be drawn
         * @param pressure    "Pressure" of the touch, used to set the alpha of {@code Paint mPaint}.
         * @param major       used to calculate the x size of the {@code RectF} of the oval to be drawn.
         * @param minor       used to calculate the y size of the {@code RectF} of the oval to be drawn.
         * @param orientation used to rotate the canvas before drawing the oval.
         * @param distance    "Distance" to splat the paint if {@code PaintMode} is "Splat". It is the
         *                    value of the AXIS_DISTANCE axis of the motion event. For a stylus, reports
         *                    the distance of the stylus from the screen. A value of 0.0 indicates direct
         *                    contact and larger values indicate increasing distance from the surface.
         * @param tilt        "Tilt" to use to splat the paint if {@code PaintMode} is "Splat". It is the
         *                    value of the AXIS_TILT tilt axis of a motion event. Which for a stylus,
         *                    reports the tilt angle of the stylus in radians where 0 radians indicates
         *                    that the stylus is being held perpendicular to the surface, and PI/2 radians
         *                    indicates that the stylus is being held flat against the surface.
         */
        private void paint(PaintMode mode, float x, float y, float pressure,
                           float major, float minor, float orientation,
                           float distance, float tilt) {
            if (mBitmap != null) {
                if (major <= 0 || minor <= 0) {
                    // If size is not available, use a default value.
                    major = minor = 16;
                }

                switch (mode) {
                    case Draw:
                        mPaint.setColor(COLORS[mColorIndex]);
                        mPaint.setAlpha(Math.min((int) (pressure * 128), 255));
                        drawOval(mCanvas, x, y, major, minor, orientation, mPaint);
                        break;

                    case Erase:
                        mPaint.setColor(BACKGROUND_COLOR);
                        mPaint.setAlpha(Math.min((int) (pressure * 128), 255));
                        drawOval(mCanvas, x, y, major, minor, orientation, mPaint);
                        break;

                    case Splat:
                        mPaint.setColor(COLORS[mColorIndex]);
                        mPaint.setAlpha(64);
                        drawSplat(mCanvas, x, y, orientation, distance, tilt, mPaint);
                        break;
                }
            }
            mFadeSteps = 0;
            invalidate();
        }

        /**
         * {@code RectF} used to size the oval drawn by our method {@code drawOval}.
         */
        private final RectF mReusableOvalRect = new RectF();

        /**
         * Draw an oval. When the orientation is 0 radians, orients the major axis vertically, angles
         * less than or greater than 0 radians rotate the major axis left or right. First we save the
         * current matrix and clip of our parameter {@code Canvas canvas} onto a private stack. Then
         * we rotate the current matrix of {@code Canvas canvas} by our parameter {@code orientation}
         * (after first converting it to degrees). We configure {@code RectF mReusableOvalRect} to be
         * the size specified by our parameters {@code minor} and {@code major} centered at the point
         * {@code (x,y)}, then we use it to draw an oval on {@code canvas} using {@code paint} as the
         * {@code Paint}. Finally we restore the state of the current matrix and clip of {@code canvas}
         * to that it had when our method was called.
         *
         * @param canvas      {@code Canvas} to draw our oval on
         * @param x           X coordinate of center of our oval
         * @param y           Y coordinate of center of our oval
         * @param major       size of our bounding {@code RectF} on Y axis
         * @param minor       size of our bounding {@code RectF} on X axis
         * @param orientation radians clockwise from vertical to rotate the oval
         * @param paint       {@code Paint} to use to draw our oval
         */
        private void drawOval(Canvas canvas, float x, float y, float major, float minor, float orientation, Paint paint) {
            canvas.save();
            canvas.rotate((float) (orientation * 180 / Math.PI), x, y);
            mReusableOvalRect.left = x - minor / 2;
            mReusableOvalRect.right = x + minor / 2;
            mReusableOvalRect.top = y - major / 2;
            mReusableOvalRect.bottom = y + major / 2;
            canvas.drawOval(mReusableOvalRect, paint);
            canvas.restore();
        }

        /**
         * Splatter paint in an area.
         * <p>
         * Chooses random vectors describing the flow of paint from a round nozzle
         * across a range of a few degrees. Then adds this vector to the direction
         * indicated by the orientation and tilt of the tool and throws paint at
         * the canvas along that vector.
         * <p>
         * Repeats the process until a masterpiece is born.
         *
         * @param canvas      We ignore this, and splatter our paint on {@code Canvas mCanvas} instead.
         * @param x           X coordinate of the center of the splatter
         * @param y           Y coordinate of the center of the splatter
         * @param orientation angle describes the direction of movement since last position event.
         * @param distance    "Distance" to splat the paint. It is the value of the AXIS_DISTANCE axis
         *                    of the motion event. For a stylus, the distance of the stylus from the screen.
         * @param tilt        "Tilt" to use to splat the paint. It is the value of the AXIS_TILT tilt axis
         *                    of a motion event. Which for a stylus, reports the tilt angle of the stylus in
         *                    radians where 0 radians indicates that the stylus is being held perpendicular
         *                    to the surface, and PI/2 radians indicates that the stylus is being held flat
         *                    against the surface.
         * @param paint       {@code Paint} to use to splatter the {@code Canvas mCanvas}.
         */
        @SuppressWarnings("UnusedParameters")
        private void drawSplat(Canvas canvas, float x, float y, float orientation,
                               float distance, float tilt, Paint paint) {
            float z = distance * 2 + 10;

            // Calculate the center of the spray.
            float nx = (float) (Math.sin(orientation) * Math.sin(tilt));
            float ny = (float) (-Math.cos(orientation) * Math.sin(tilt));
            float nz = (float) Math.cos(tilt);
            if (nz < 0.05) {
                return;
            }
            float cd = z / nz;
            float cx = nx * cd;
            float cy = ny * cd;

            for (int i = 0; i < SPLAT_VECTORS; i++) {
                // Make a random 2D vector that describes the direction of a speck of paint
                // ejected by the nozzle in the nozzle's plane, assuming the tool is
                // perpendicular to the surface.
                double direction = mRandom.nextDouble() * Math.PI * 2;
                double dispersion = mRandom.nextGaussian() * 0.2;
                double vx = Math.cos(direction) * dispersion;
                double vy = Math.sin(direction) * dispersion;
                double vz = 1;

                // Apply the nozzle tilt angle.
                double temp = vy;
                vy = temp * Math.cos(tilt) - vz * Math.sin(tilt);
                vz = temp * Math.sin(tilt) + vz * Math.cos(tilt);

                // Apply the nozzle orientation angle.
                temp = vx;
                vx = temp * Math.cos(orientation) - vy * Math.sin(orientation);
                vy = temp * Math.sin(orientation) + vy * Math.cos(orientation);

                // Determine where the paint will hit the surface.
                if (vz < 0.05) {
                    continue;
                }
                float pd = (float) (z / vz);
                float px = (float) (vx * pd);
                float py = (float) (vy * pd);

                // Throw some paint at this location, relative to the center of the spray.
                mCanvas.drawCircle(x + px - cx, y + py - cy, 1.0f, paint);
            }
        }
    }
}

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

        public PaintView(Context c, AttributeSet attrs) {
            super(c, attrs);
            init();
        }

        private void init() {
            setFocusable(true);

            mPaint.setAntiAlias(true);

            mFadePaint.setColor(BACKGROUND_COLOR);
            mFadePaint.setAlpha(FADE_ALPHA);
        }

        public void clear() {
            if (mCanvas != null) {
                mPaint.setColor(BACKGROUND_COLOR);
                mCanvas.drawPaint(mPaint);
                invalidate();

                mFadeSteps = MAX_FADE_STEPS;
            }
        }

        public void fade() {
            if (mCanvas != null && mFadeSteps < MAX_FADE_STEPS) {
                mCanvas.drawPaint(mFadePaint);
                invalidate();

                mFadeSteps++;
            }
        }

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

        @Override
        protected void onDraw(Canvas canvas) {
            if (mBitmap != null) {
                canvas.drawBitmap(mBitmap, 0, 0, null);
            }
        }

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

        private void moveTrackball(float deltaX, float deltaY) {
            final int curW = mBitmap != null ? mBitmap.getWidth() : 0;
            final int curH = mBitmap != null ? mBitmap.getHeight() : 0;

            mCurX = Math.max(Math.min(mCurX + deltaX, curW - 1), 0);
            mCurY = Math.max(Math.min(mCurY + deltaY, curH - 1), 0);
            paint(PaintMode.Draw, mCurX, mCurY);
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            return onTouchOrHoverEvent(event, true /*isTouch*/);
        }

        @Override
        public boolean onHoverEvent(MotionEvent event) {
            return onTouchOrHoverEvent(event, false /*isTouch*/);
        }

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

        private PaintMode getPaintModeForTool(int toolType, PaintMode defaultMode) {
            if (toolType == MotionEvent.TOOL_TYPE_ERASER) {
                return PaintMode.Erase;
            }
            return defaultMode;
        }

        private void advanceColor() {
            mColorIndex = (mColorIndex + 1) % COLORS.length;
        }

        private void paint(PaintMode mode, float x, float y) {
            paint(mode, x, y, 1.0f, 0, 0, 0, 0, 0);
        }

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
         * Draw an oval.
         * <p>
         * When the orientation is 0 radians, orients the major axis vertically,
         * angles less than or greater than 0 radians rotate the major axis left or right.
         */
        private final RectF mReusableOvalRect = new RectF();

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
         * across a range of a few degrees.  Then adds this vector to the direction
         * indicated by the orientation and tilt of the tool and throws paint at
         * the canvas along that vector.
         * <p>
         * Repeats the process until a masterpiece is born.
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

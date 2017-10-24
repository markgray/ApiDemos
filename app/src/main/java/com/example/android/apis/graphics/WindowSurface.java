package com.example.android.apis.graphics;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * Demonstrates how to take over the Surface from a window to do direct drawing to it (without going
 * through the view hierarchy). Good example of how to use a background thread to do your drawing.
 * Shows use of life cycle callbacks when a thread is running in the background.
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class WindowSurface extends Activity implements SurfaceHolder.Callback2 {
    /**
     * TAG for logging.
     */
    private static final String TAG = "WindowSurface";
    /**
     * Our {@code DrawingThread} instance for background drawing.
     */
    public final DrawingThread mDrawingThread = new DrawingThread();

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we retrieve the current Window for the activity and take ownership of
     * the window's surface (The window's view hierarchy will no longer draw into the surface, though
     * it will otherwise continue to operate (such as for receiving input events). Our implementation
     * of the {@code SurfaceHolder.Callback2} interface will be used to tell us about state changes
     * to the surface). Finally we start our background drawing thread {@code DrawingThread mDrawingThread}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Tell the activity's window that we want to do our own drawing
        // to its surface.  This prevents the view hierarchy from drawing to
        // it, though we can still add views to capture input if desired.
        getWindow().takeSurface(this);

        // This is the thread that will be drawing to our surface.
        mDrawingThread.start();
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. First we call our super's implementation of {@code onPause}, then
     * synchronizing on {@code mDrawingThread} we set the {@code mRunning} field of {@code mDrawingThread}
     * to false and wake it up (it will be blocking on its "this" waiting for us).
     */
    @Override
    protected void onPause() {
        super.onPause();

        // Make sure the drawing thread is not running while we are paused.
        synchronized (mDrawingThread) {
            mDrawingThread.mRunning = false;
            mDrawingThread.notify();
        }
    }

    /**
     * Called after {@link #onRestoreInstanceState}, {@link #onRestart}, or {@link #onPause}, for our
     * activity to start interacting with the user. First we call through to our super's implementation
     * of {@code onResume}, then synchronizing on {@code mDrawingThread} we set the {@code mRunning}
     * field of {@code mDrawingThread} to true and wake it up (it will be blocking on its "this"
     * waiting for us).
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Let the drawing thread resume running.
        synchronized (mDrawingThread) {
            mDrawingThread.mRunning = true;
            mDrawingThread.notify();
        }
    }

    /**
     * Perform any final cleanup before our activity is destroyed. First we call through to our super's
     * implementation of {@code onDestroy}, then synchronizing on {@code mDrawingThread} we set the
     * {@code mQuit} field of {@code mDrawingThread} to true and wake it up (it will be blocking on
     * its "this" waiting for us).
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure the drawing thread goes away.
        synchronized (mDrawingThread) {
            mDrawingThread.mQuit = true;
            mDrawingThread.notify();
        }
    }

    /**
     * This is called immediately after the surface is first created. Synchronizing on {@code mDrawingThread}
     * we set the {@code mSurface} field of {@code mDrawingThread} to our parameter {@code SurfaceHolder holder}
     * and wake it up (it will be blocking on its "this" waiting for us).
     *
     * @param holder The SurfaceHolder whose surface is being created.
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Tell the drawing thread that a surface is available.
        synchronized (mDrawingThread) {
            mDrawingThread.mSurface = holder;
            mDrawingThread.notify();
        }
    }

    /**
     * This is called immediately after any structural changes (format or size) have been made to the
     * surface. We do nothing.
     *
     * @param holder The SurfaceHolder whose surface has changed.
     * @param format The new PixelFormat of the surface.
     * @param width  The new width of the surface.
     * @param height The new height of the surface.
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Don't need to do anything here; the drawing thread will pick up
        // new sizes from the canvas.
    }

    /**
     * Called when the application needs to redraw the content of its surface, after it is resized
     * or for some other reason. We do nothing.
     *
     * @param holder The SurfaceHolder whose surface has changed.
     */
    @Override
    public void surfaceRedrawNeeded(SurfaceHolder holder) {
    }

    /**
     * This is called immediately before a surface is destroyed. It is called after {@code onPause}
     * so the {@code mRunning} field of {@code mDrawingThread} is false at this point so the thread
     * will be looping waiting for it to become true (after a call to {@code onResume}). Synchronizing
     * on {@code mDrawingThread} we set the {@code mSurface} field of {@code mDrawingThread} to our
     * parameter {@code SurfaceHolder holder} and wake it up. Then while the {@code mActive} field of
     * {@code mDrawingThread} is true we temporarily relinquish the lock and {@code wait} for that
     * thread to call the {@code notify} method. We loop here until {@code mActive} is false because
     * we should not return from this method until our thread stops drawing to the surface.
     *
     * @param holder The SurfaceHolder whose surface is being destroyed.
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // We need to tell the drawing thread to stop, and block until
        // it has done so.
        synchronized (mDrawingThread) {
            mDrawingThread.mSurface = holder;
            mDrawingThread.notify();
            while (mDrawingThread.mActive) {
                try {
                    mDrawingThread.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Tracking of a single point that is moving on the screen.
     */
    @SuppressWarnings("WeakerAccess")
    static final class MovingPoint {
        /**
         * Our current x coordinate
         */
        float x;
        /**
         * Our current y coordinate
         */
        float y;
        /**
         * Our current change in x coordinate for the next call to our method {@code step}
         */
        float dx;
        /**
         * Our current change in y coordinate for the next call to our method {@code step}
         */
        float dy;

        /**
         * Initializes our fields with random numbers based on the constraints of the input parameters.
         * We initialize our field {@code x} to a random number between 0 and {@code width-1}, and
         * {@code y} to a random number between 0 and {@code height-1}. We initialize our fields
         * {@code dx} and {@code dy} to random numbers between 1 and 2 times {@code minStep} plus 1.
         *
         * @param width   maximum value for the x coordinate (or red color)
         * @param height  maximum value for the y coordinate (or blue color)
         * @param minStep minimum step to use for changing x and y.
         */
        void init(int width, int height, float minStep) {
            x = (float) ((width - 1) * Math.random());
            y = (float) ((height - 1) * Math.random());
            dx = (float) (Math.random() * minStep * 2) + 1;
            dy = (float) (Math.random() * minStep * 2) + 1;
        }

        /**
         * Calculates a new random delta value for {@code cur} given the constraints of {@code minStep}
         * and {@code maxStep}. First we add to {@code cur} a random number between {@code -minStep/2}
         * and {@code +minStep/2}. Then if the new {@code cur} is less than 0, but greater than
         * {@code -minStep} we set it to {@code -minStep}. If it's greater than or equal to 0 and less
         * than {@code minStep} we set it to {@code minStep}. If it's greater than {@code maxStep} we
         * set it to {@code maxStep}, and if it's less than {@code -maxStep} we set it to {@code -maxStep}.
         * Finally we return {@code cur} to the caller.
         *
         * @param cur     current delta value
         * @param minStep minimum allowed delta value
         * @param maxStep maximum allowed delta value
         * @return a new random delta value between {@code -maxStep} and {@code +maxStep} but greater
         * whose absolute value is greater than or equal to {@code minStep}.
         */
        float adjDelta(float cur, float minStep, float maxStep) {
            cur += (Math.random() * minStep) - (minStep / 2);
            if (cur < 0 && cur > -minStep) cur = -minStep;
            if (cur >= 0 && cur < minStep) cur = minStep;
            if (cur > maxStep) cur = maxStep;
            if (cur < -maxStep) cur = -maxStep;
            return cur;
        }

        /**
         * Adds {@code dx} to {@code x} and {@code dy} to {@code y}, and if either is outside the
         * bounds set for them (0 to {@code width} for x, and 0 to {@code height} for {@code y}),
         * we set them to those bounds and call our method {@code adjDelta} to calculate a new value
         * for {@code dx} and/or {@code dy}.
         *
         * @param width   maximum value for x
         * @param height  maximum value for y
         * @param minStep minimum change for both x and y
         * @param maxStep maximum change for both x and y
         */
        void step(int width, int height, float minStep, float maxStep) {
            x += dx;
            if (x <= 0 || x >= (width - 1)) {
                if (x <= 0) x = 0;
                else if (x >= (width - 1)) x = width - 1;
                dx = adjDelta(-dx, minStep, maxStep);
            }
            y += dy;
            if (y <= 0 || y >= (height - 1)) {
                if (y <= 0) y = 0;
                else if (y >= (height - 1)) y = height - 1;
                dy = adjDelta(-dy, minStep, maxStep);
            }
        }
    }

    /**
     * This is a thread that will be running a loop, drawing into the
     * window's surface.
     */
    @SuppressWarnings("WeakerAccess")
    class DrawingThread extends Thread {
        // These are protected by the Thread's lock.
        /**
         * Our interface to someone holding our display surface, we use it to obtain a {@code Canvas}
         * to draw to using the method {@code lockCanvas}, and to call {@code unlockCanvasAndPost} to
         * finish editing pixels in the surface so they can be displayed.
         */
        SurfaceHolder mSurface;
        /**
         * Flag indicating whether our thread should be running (true) or not (false). If is set to
         * true in the {@code onResume} method of {@code WindowSurface} and to false in its
         * {@code onPause} method.
         */
        boolean mRunning;
        /**
         * Flag indicating that we are currently running and drawing to the surface. The main thread's
         * {@code surfaceDestroyed} uses this flag to loop before returning to its caller to make
         * sure all drawing to the surface has stopped.
         */
        boolean mActive;
        /**
         * Flag to indicate that we should stop running by returning to the caller of our {@code run}
         * method. It is set to true by the {@code onDestroy} method of the main thread.
         */
        boolean mQuit;

        // Internal state.
        /**
         * Width of the lines that we draw, calculated using the current logical display density.
         */
        int mLineWidth;
        /**
         * Minimum change in x and y coordinate, calculated to be 2 times {@code mLineWidth}
         */
        float mMinStep;
        /**
         * Maximum change in x and y coordinate, calculated to be 3 times {@code mMinStep}
         */
        float mMaxStep;

        /**
         * Flag to indicate whether we have initialized everything. Set to true first time our drawing
         * loop is run so that we only call the {@code init} methods of the end points of our lines
         * {@code MovingPoint mPoint1} and {@code MovingPoint mPoint2} and the associated color value
         * {@code MovingPoint mColor} once and only once.
         */
        boolean mInitialized;
        /**
         * First point of the newest line to be drawn
         */
        final MovingPoint mPoint1 = new MovingPoint();
        /**
         * Second point of the newest line to be drawn
         */
        final MovingPoint mPoint2 = new MovingPoint();

        /**
         * Number of old lines to remember for next display refresh.
         */
        static final int NUM_OLD = 100;
        /**
         * Current number of old lines in our refresh buffer.
         */
        int mNumOld = 0;
        /**
         * Array to hold our old line endpoints so they can be redrawn.
         */
        final float[] mOld = new float[NUM_OLD * 4];
        /**
         * Array to hold the colors of our old lines so they can be redrawn.
         */
        final int[] mOldColor = new int[NUM_OLD];
        /**
         * Ranges from -2 to NUM_OLD*2 then back to -2, it is incremented by 2 every time the surface
         * is drawn. It is used to determine which of the old lines are shaded to green by the method
         * {@code makeGreen} (those whose {@code index} is within +/-10 of {@code mBrightLine}
         */
        int mBrightLine = 0;

        // X is red, Y is blue.
        /**
         * Random Color used for the newest line.
         */
        final MovingPoint mColor = new MovingPoint();

        /**
         * {@code Paint} used to contain the color for the background, its color is set to black near
         * the beginning of the {@code run} method and the color is retrieved for a call to the
         * {@code drawColor} method of our {@code Canvas canvas}.
         */
        final Paint mBackground = new Paint();
        /**
         * {@code Paint} used to draw our lines.
         */
        final Paint mForeground = new Paint();

        /**
         * Calculates whether the old line at {@code index} should have its color tinged green and
         * by how much based on its distance from the current value of {@code mBrightLine}. First we
         * calculate the absolute difference {@code dist} between our parameter {@code index} and our
         * field {@code mBrightLine}. If this is greater than 10, we return 0 (no green tinging to
         * do). Otherwise we return a value between 255 ({@code dist} is 0) to 25 ({@code dist} is 9)
         * left shifted into the correct position for the color green in an ARGB color.
         *
         * @param index index number of the old line that is being drawn
         * @return 0 if {@code index} is farther away than +/-10 from {@code mBrightLine}, otherwise
         * a shade of green proportional to the distance apart between 255 and 25 (shifted into the
         * byte used for the color green).
         */
        int makeGreen(int index) {
            int dist = Math.abs(mBrightLine - index);
            if (dist > 10) return 0;
            return (255 - (dist * (255 / 10))) << 8;
        }

        /**
         * Our drawing loop. We retrieve the current logical density of the display and set our field
         * {@code mLineWidth} to 1.5 times that. If the result was less than 1, we set {@code mLineWidth}
         * to 1. Then we set our field {@code mMinStep} to 2 times {@code mLineWidth}, and {@code mMaxStep}
         * to 3 times {@code mMinStep}. We set the color of the {@code Paint mBackground} to black (for
         * no good reason, since we only retrieve the color to draw the canvas black, and have no other
         * use for the {@code Paint}. We set the color of the {@code Paint mBackground} to a dark
         * blue/green shade. set its anti alias flag to false, and set its stroke width to {@code mLineWidth}
         * (setting the color is useless, since it is set again before the {@code Paint} is used, but
         * what the hay).
         * <p>
         * Now we loop almost for ever (until our flag {@code mQuit} is changed to true by the main
         * thread, it does this in its {@code onDestroy} callback). Synchronizing on "this" we loop
         * waiting for our field {@code mSurface} to become non-null (it is set to the {@code SurfaceHolder}
         * passed to the {@code surfaceCreated} and {@code surfaceDestroyed} callbacks of the main thread)
         * and our field {@code mRunning} to become true (it is set to true in the {@code onResume}
         * callback of the main thread). While waiting it our {@code mActive} field is true we set it
         * to false and wake up the main thread (it waits for it to become false in its {@code surfaceDestroyed}
         * callback before returning to its caller). Then if our {@code mQuit} field has become true
         * we return to our caller, ending the thread (it is set to true in the {@code onDestroy} callback
         * of the main thread).
         * <p>
         * Now we wait for the main thread to release its lock on this instance, and once it has if
         * {@code mActive} is false we set it to true and notify the main thread (just in case its
         * {@code surfaceDestroyed} has been called in the meantime I suppose, since we hold the lock
         * it won't be able to proceed until we finish the {@code synchronized} block which releases
         * the lock until we try to get it again at the top of the while loop).
         * <p>
         * We now lock the {@code Canvas canvas} of {@code SurfaceHolder mSurface} for drawing. If
         * {@code canvas} is null after this (surface has not been created or otherwise cannot be edited)
         * we skip our drawing code and loop back to the start of our while loop. Next we check to see
         * if our {@code mInitialized} flag is false, and if so we set it to true, initialize both
         * {@code mPoint1} and {@code mPoint2} to the width and height of the {@code Canvas canvas},
         * with a minimum step size of {@code mMinStep}, then initialize {@code mColor} to a red of
         * 127, a blue of 127 and a minimum step size of 1. If we have already initialized, we call
         * the {@code step} of {@code mPoint1} and {@code mPoint2} to move the points to a new random
         * position with a maximum width of the width of the canvas, maximum height of the height of
         * the canvas, a minimum step of {@code mMinStep} and a maximum step of {@code mMaxStep}. We
         * call the {@code step} method of {@code mColor} to "step" it to a random color with a maximum
         * red of 127, maximum blue of 127, a minimum step of 1, and a maximum step of 3.
         * <p>
         * Next we advance {@code mBrightLine} by 2, and if the result is greater than {@code NUM_OLD*2}
         * we set it to -2.
         * <p>
         * We set the entire {@code Canvas canvas} to the color of {@code Paint mBackground} (which is
         * set to black at the beginning of this method).
         * <p>
         * We next proceed to draw all the lines stored in the array {@code mOld}, with their colors
         * stored in the array {@code mOldColor} (or'ed with the green tinge appropriate for this line
         * as determined by the method {@code makeGreen}). To do this we loop starting with the oldest
         * line currently stored, setting the color of {@code Paint mForeground} to the old color
         * stored for it in {@code mOldColor} or'ed with the color returned by the method {@code makeGreen}
         * for that line number, and setting the alpha of the {@code Paint} color to that goes from
         * 255 for the newest line to {@code 255/NUM_OLD} for the oldest possible line. Finally we
         * draw the line using {@code mForeground} as the {@code Paint} (each line occupies 4 locations
         * in {@code mOld} with each of those 4 values used as a coordinate for the two endpoints).
         * <p>
         * Now we draw the new line. We determine the color {@code color} using the latest value of
         * {@code mColor} by adding 128 to the value of {@code mColor.x} as the red, and 128 to the
         * value of {@code mColor.y} as the blue (limiting both to a maximum of 255). We shift and or
         * these values together with an alpha of 255 to make a proper ARGB format color for {@code color}.
         * Then we set the color of {@code Paint mForeground} to {@code color} or'ed with the green
         * tinge returned by our method {@code makeGreen} for an index of -2. We then draw a line
         * from {@code mPaint1} to {@code mPaint2} using {@code mForeground} as the {@code Paint}.
         * <p>
         * To add the new line to the old lines we first move all the old entries in {@code mOld} up
         * by 4 positions, and {@code mOldColor} up by 1 position. If {@code mNumOld} is less than
         * {@code NUM_OLD} we increment {@code mNumOld}. Then we copy {@code mPoint1.x} to {@code mOld[0]},
         * {@code mPoint1.y} to {@code mOld[1]}. {@code mPoint2.x} to {@code mOld[2]}, {@code mPoint2.y}
         * to {@code mOld[3]}, and we copy {@code color} to {@code mOldColor[0]}.
         * <p>
         * Finally we call the method {@code unlockCanvasAndPost} which finishes editing pixels in
         * the surface, and loop back to the beginning of the while loop. (After this call, the
         * surface's current pixels will be shown on the screen, but its content is lost, in particular
         * there is no guarantee that the content of the Surface will remain unchanged when the method
         * {@code lockCanvas()} is called again.)
         */
        @Override
        public void run() {
            mLineWidth = (int) (getResources().getDisplayMetrics().density * 1.5);
            if (mLineWidth < 1) mLineWidth = 1;
            mMinStep = mLineWidth * 2;
            mMaxStep = mMinStep * 3;

            mBackground.setColor(0xff000000);
            mForeground.setColor(0xff00ffff);
            mForeground.setAntiAlias(false);
            mForeground.setStrokeWidth(mLineWidth);

            while (true) {
                // Synchronize with activity: block until the activity is ready
                // and we have a surface; report whether we are active or inactive
                // at this point; exit thread when asked to quit.
                synchronized (this) {
                    while (mSurface == null || !mRunning) {
                        if (mActive) {
                            mActive = false;
                            notify();
                        }
                        if (mQuit) {
                            return;
                        }
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            Log.i(TAG, e.getLocalizedMessage());
                        }
                    }

                    if (!mActive) {
                        mActive = true;
                        notify();
                    }

                    // Lock the canvas for drawing.
                    Canvas canvas = mSurface.lockCanvas();
                    if (canvas == null) {
                        Log.i("WindowSurface", "Failure locking canvas");
                        continue;
                    }

                    // Update graphics.
                    if (!mInitialized) {
                        mInitialized = true;
                        mPoint1.init(canvas.getWidth(), canvas.getHeight(), mMinStep);
                        mPoint2.init(canvas.getWidth(), canvas.getHeight(), mMinStep);
                        mColor.init(127, 127, 1);
                    } else {
                        mPoint1.step(canvas.getWidth(), canvas.getHeight(), mMinStep, mMaxStep);
                        mPoint2.step(canvas.getWidth(), canvas.getHeight(), mMinStep, mMaxStep);
                        mColor.step(127, 127, 1, 3);
                    }
                    mBrightLine += 2;
                    if (mBrightLine > (NUM_OLD * 2)) {
                        mBrightLine = -2;
                    }

                    // Clear background.
                    canvas.drawColor(mBackground.getColor());

                    // Draw old lines.
                    for (int i = mNumOld - 1; i >= 0; i--) {
                        mForeground.setColor(mOldColor[i] | makeGreen(i));
                        mForeground.setAlpha(((NUM_OLD - i) * 255) / NUM_OLD);
                        int p = i * 4;
                        canvas.drawLine(mOld[p], mOld[p + 1], mOld[p + 2], mOld[p + 3], mForeground);
                    }

                    // Draw new line.
                    int red = (int) mColor.x + 128;
                    if (red > 255) red = 255;
                    int blue = (int) mColor.y + 128;
                    if (blue > 255) blue = 255;
                    int color = 0xff000000 | (red << 16) | blue;
                    mForeground.setColor(color | makeGreen(-2));
                    canvas.drawLine(mPoint1.x, mPoint1.y, mPoint2.x, mPoint2.y, mForeground);

                    // Add in the new line.
                    if (mNumOld > 1) {
                        System.arraycopy(mOld, 0, mOld, 4, (mNumOld - 1) * 4);
                        System.arraycopy(mOldColor, 0, mOldColor, 1, mNumOld - 1);
                    }
                    if (mNumOld < NUM_OLD) mNumOld++;
                    mOld[0] = mPoint1.x;
                    mOld[1] = mPoint1.y;
                    mOld[2] = mPoint2.x;
                    mOld[3] = mPoint2.y;
                    mOldColor[0] = color;

                    // All done!
                    mSurface.unlockCanvasAndPost(canvas);
                }
            }
        }
    }
}

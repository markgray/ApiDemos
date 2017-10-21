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
         * {@code y} to a random number between 0 and {@code height-1}.
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

        float adjDelta(float cur, float minStep, float maxStep) {
            cur += (Math.random() * minStep) - (minStep / 2);
            if (cur < 0 && cur > -minStep) cur = -minStep;
            if (cur >= 0 && cur < minStep) cur = minStep;
            if (cur > maxStep) cur = maxStep;
            if (cur < -maxStep) cur = -maxStep;
            return cur;
        }

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
        SurfaceHolder mSurface;
        boolean mRunning;
        boolean mActive;
        boolean mQuit;

        // Internal state.
        int mLineWidth;
        float mMinStep;
        float mMaxStep;

        boolean mInitialized;
        final MovingPoint mPoint1 = new MovingPoint();
        final MovingPoint mPoint2 = new MovingPoint();

        static final int NUM_OLD = 100;
        int mNumOld = 0;
        final float[] mOld = new float[NUM_OLD * 4];
        final int[] mOldColor = new int[NUM_OLD];
        int mBrightLine = 0;

        // X is red, Y is blue.
        final MovingPoint mColor = new MovingPoint();

        final Paint mBackground = new Paint();
        final Paint mForeground = new Paint();

        int makeGreen(int index) {
            int dist = Math.abs(mBrightLine - index);
            if (dist > 10) return 0;
            return (255 - (dist * (255 / 10))) << 8;
        }

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

package com.example.android.apis.graphics

import android.annotation.TargetApi
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs

/**
 * Demonstrates how to take over the Surface from a window to do direct drawing to it (without going
 * through the view hierarchy). Good example of how to use a background thread to do your drawing.
 * Shows use of life cycle callbacks when a thread is running in the background.
 */
@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
class WindowSurface : AppCompatActivity(), SurfaceHolder.Callback2 {
    /**
     * Our `DrawingThread` instance for background drawing.
     */
    val mDrawingThread = DrawingThread()

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we retrieve the current Window for the activity and take ownership of
     * the window's surface (The window's view hierarchy will no longer draw into the surface, though
     * it will otherwise continue to operate (such as for receiving input events). Our implementation
     * of the `SurfaceHolder.Callback2` interface will be used to tell us about state changes
     * to the surface). Finally we start our background drawing thread `DrawingThread mDrawingThread`.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Tell the activity's window that we want to do our own drawing
// to its surface.  This prevents the view hierarchy from drawing to
// it, though we can still add views to capture input if desired.
        window.takeSurface(this)
        // This is the thread that will be drawing to our surface.
        mDrawingThread.start()
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. First we call our super's implementation of `onPause`, then
     * synchronizing on `mDrawingThread` we set the `mRunning` field of `mDrawingThread`
     * to false and wake it up (it will be blocking on its "this" waiting for us).
     */
    override fun onPause() {
        super.onPause()
        // Make sure the drawing thread is not running while we are paused.
        synchronized(mDrawingThread) {
            mDrawingThread.mRunning = false
            (mDrawingThread as Object).notify()
        }
    }

    /**
     * Called after [.onRestoreInstanceState], [.onRestart], or [.onPause], for our
     * activity to start interacting with the user. First we call through to our super's implementation
     * of `onResume`, then synchronizing on `mDrawingThread` we set the `mRunning`
     * field of `mDrawingThread` to true and wake it up (it will be blocking on its "this"
     * waiting for us).
     */
    override fun onResume() {
        super.onResume()
        // Let the drawing thread resume running.
        synchronized(mDrawingThread) {
            mDrawingThread.mRunning = true
            (mDrawingThread as Object).notify()
        }
    }

    /**
     * Perform any final cleanup before our activity is destroyed. First we call through to our super's
     * implementation of `onDestroy`, then synchronizing on `mDrawingThread` we set the
     * `mQuit` field of `mDrawingThread` to true and wake it up (it will be blocking on
     * its "this" waiting for us).
     */
    override fun onDestroy() {
        super.onDestroy()
        // Make sure the drawing thread goes away.
        synchronized(mDrawingThread) {
            mDrawingThread.mQuit = true
            (mDrawingThread as Object).notify()
        }
    }

    /**
     * This is called immediately after the surface is first created. Synchronizing on `mDrawingThread`
     * we set the `mSurface` field of `mDrawingThread` to our parameter `SurfaceHolder holder`
     * and wake it up (it will be blocking on its "this" waiting for us).
     *
     * @param holder The SurfaceHolder whose surface is being created.
     */
    override fun surfaceCreated(holder: SurfaceHolder) { // Tell the drawing thread that a surface is available.
        synchronized(mDrawingThread) {
            mDrawingThread.mSurface = holder
            (mDrawingThread as Object).notify()
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
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) { // Don't need to do anything here; the drawing thread will pick up
// new sizes from the canvas.
    }

    /**
     * Called when the application needs to redraw the content of its surface, after it is resized
     * or for some other reason. We do nothing.
     *
     * @param holder The SurfaceHolder whose surface has changed.
     */
    override fun surfaceRedrawNeeded(holder: SurfaceHolder) {}

    /**
     * This is called immediately before a surface is destroyed. It is called after `onPause`
     * so the `mRunning` field of `mDrawingThread` is false at this point so the thread
     * will be looping waiting for it to become true (after a call to `onResume`). Synchronizing
     * on `mDrawingThread` we set the `mSurface` field of `mDrawingThread` to our
     * parameter `SurfaceHolder holder` and wake it up. Then while the `mActive` field of
     * `mDrawingThread` is true we temporarily relinquish the lock and `wait` for that
     * thread to call the `notify` method. We loop here until `mActive` is false because
     * we should not return from this method until our thread stops drawing to the surface.
     *
     * @param holder The SurfaceHolder whose surface is being destroyed.
     */
    override fun surfaceDestroyed(holder: SurfaceHolder) { // We need to tell the drawing thread to stop, and block until
// it has done so.
        synchronized(mDrawingThread) {
            mDrawingThread.mSurface = holder
            (mDrawingThread as Object).notify()
            while (mDrawingThread.mActive) {
                try {
                    mDrawingThread.wait()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Tracking of a single point that is moving on the screen.
     */
    class MovingPoint {
        /**
         * Our current x coordinate
         */
        var x = 0f
        /**
         * Our current y coordinate
         */
        var y = 0f
        /**
         * Our current change in x coordinate for the next call to our method `step`
         */
        var dx = 0f
        /**
         * Our current change in y coordinate for the next call to our method `step`
         */
        var dy = 0f

        /**
         * Initializes our fields with random numbers based on the constraints of the input parameters.
         * We initialize our field `x` to a random number between 0 and `width-1`, and
         * `y` to a random number between 0 and `height-1`. We initialize our fields
         * `dx` and `dy` to random numbers between 1 and 2 times `minStep` plus 1.
         *
         * @param width   maximum value for the x coordinate (or red color)
         * @param height  maximum value for the y coordinate (or blue color)
         * @param minStep minimum step to use for changing x and y.
         */
        fun init(width: Int, height: Int, minStep: Float) {
            x = ((width - 1) * Math.random()).toFloat()
            y = ((height - 1) * Math.random()).toFloat()
            dx = (Math.random() * minStep * 2).toFloat() + 1
            dy = (Math.random() * minStep * 2).toFloat() + 1
        }

        /**
         * Calculates a new random delta value for `cur` given the constraints of `minStep`
         * and `maxStep`. First we add to `cur` a random number between `-minStep/2`
         * and `+minStep/2`. Then if the new `cur` is less than 0, but greater than
         * `-minStep` we set it to `-minStep`. If it's greater than or equal to 0 and less
         * than `minStep` we set it to `minStep`. If it's greater than `maxStep` we
         * set it to `maxStep`, and if it's less than `-maxStep` we set it to `-maxStep`.
         * Finally we return `cur` to the caller.
         *
         * @param cur     current delta value
         * @param minStep minimum allowed delta value
         * @param maxStep maximum allowed delta value
         * @return a new random delta value between `-maxStep` and `+maxStep` but greater
         * whose absolute value is greater than or equal to `minStep`.
         */
        fun adjDelta(cur: Float, minStep: Float, maxStep: Float): Float {
            val deltaCur = (Math.random() * minStep - minStep / 2).toFloat()
            var curVar = cur + deltaCur
            if (curVar < 0 && curVar > -minStep) curVar = -minStep
            if (curVar >= 0 && curVar < minStep) curVar = minStep
            if (curVar > maxStep) curVar = maxStep
            if (curVar < -maxStep) curVar = -maxStep
            return curVar
        }

        /**
         * Adds `dx` to `x` and `dy` to `y`, and if either is outside the
         * bounds set for them (0 to `width` for x, and 0 to `height` for `y`),
         * we set them to those bounds and call our method `adjDelta` to calculate a new value
         * for `dx` and/or `dy`.
         *
         * @param width   maximum value for x
         * @param height  maximum value for y
         * @param minStep minimum change for both x and y
         * @param maxStep maximum change for both x and y
         */
        fun step(width: Int, height: Int, minStep: Float, maxStep: Float) {
            x += dx
            if (x <= 0 || x >= width - 1) {
                if (x <= 0) x = 0f else if (x >= width - 1) x = width - 1.toFloat()
                dx = adjDelta(-dx, minStep, maxStep)
            }
            y += dy
            if (y <= 0 || y >= height - 1) {
                if (y <= 0) y = 0f else if (y >= height - 1) y = height - 1.toFloat()
                dy = adjDelta(-dy, minStep, maxStep)
            }
        }
    }

    /**
     * This is a thread that will be running a loop, drawing into the
     * window's surface.
     */
    inner class DrawingThread : Thread() {
        // These are protected by the Thread's lock.
        /**
         * Our interface to someone holding our display surface, we use it to obtain a `Canvas`
         * to draw to using the method `lockCanvas`, and to call `unlockCanvasAndPost` to
         * finish editing pixels in the surface so they can be displayed.
         */
        var mSurface: SurfaceHolder? = null
        /**
         * Flag indicating whether our thread should be running (true) or not (false). If is set to
         * true in the `onResume` method of `WindowSurface` and to false in its
         * `onPause` method.
         */
        var mRunning = false
        /**
         * Flag indicating that we are currently running and drawing to the surface. The main thread's
         * `surfaceDestroyed` uses this flag to loop before returning to its caller to make
         * sure all drawing to the surface has stopped.
         */
        var mActive = false
        /**
         * Flag to indicate that we should stop running by returning to the caller of our `run`
         * method. It is set to true by the `onDestroy` method of the main thread.
         */
        var mQuit = false
        // Internal state.
        /**
         * Width of the lines that we draw, calculated using the current logical display density.
         */
        var mLineWidth = 0
        /**
         * Minimum change in x and y coordinate, calculated to be 2 times `mLineWidth`
         */
        var mMinStep = 0f
        /**
         * Maximum change in x and y coordinate, calculated to be 3 times `mMinStep`
         */
        var mMaxStep = 0f
        /**
         * Flag to indicate whether we have initialized everything. Set to true first time our drawing
         * loop is run so that we only call the `init` methods of the end points of our lines
         * `MovingPoint mPoint1` and `MovingPoint mPoint2` and the associated color value
         * `MovingPoint mColor` once and only once.
         */
        var mInitialized = false
        /**
         * First point of the newest line to be drawn
         */
        val mPoint1 = MovingPoint()
        /**
         * Second point of the newest line to be drawn
         */
        val mPoint2 = MovingPoint()
        /**
         * Current number of old lines in our refresh buffer.
         */
        var mNumOld = 0
        /**
         * Array to hold our old line endpoints so they can be redrawn.
         */
        val mOld = FloatArray(Companion.NUM_OLD * 4)
        /**
         * Array to hold the colors of our old lines so they can be redrawn.
         */
        val mOldColor = IntArray(Companion.NUM_OLD)
        /**
         * Ranges from -2 to NUM_OLD*2 then back to -2, it is incremented by 2 every time the surface
         * is drawn. It is used to determine which of the old lines are shaded to green by the method
         * `makeGreen` (those whose `index` is within +/-10 of `mBrightLine`
         */
        var mBrightLine = 0
        // X is red, Y is blue.
        /**
         * Random Color used for the newest line.
         */
        val mColor = MovingPoint()
        /**
         * `Paint` used to contain the color for the background, its color is set to black near
         * the beginning of the `run` method and the color is retrieved for a call to the
         * `drawColor` method of our `Canvas canvas`.
         */
        val mBackground = Paint()
        /**
         * `Paint` used to draw our lines.
         */
        val mForeground = Paint()

        /**
         * Calculates whether the old line at `index` should have its color tinged green and
         * by how much based on its distance from the current value of `mBrightLine`. First we
         * calculate the absolute difference `dist` between our parameter `index` and our
         * field `mBrightLine`. If this is greater than 10, we return 0 (no green tinging to
         * do). Otherwise we return a value between 255 (`dist` is 0) to 25 (`dist` is 9)
         * left shifted into the correct position for the color green in an ARGB color.
         *
         * @param index index number of the old line that is being drawn
         * @return 0 if `index` is farther away than +/-10 from `mBrightLine`, otherwise
         * a shade of green proportional to the distance apart between 255 and 25 (shifted into the
         * byte used for the color green).
         */
        fun makeGreen(index: Int): Int {
            val dist = abs(n = mBrightLine - index)
            return if (dist > 10) 0 else 255 - dist * (255 / 10) shl 8
        }

        /**
         * Our drawing loop. We retrieve the current logical density of the display and set our field
         * `mLineWidth` to 1.5 times that. If the result was less than 1, we set `mLineWidth`
         * to 1. Then we set our field `mMinStep` to 2 times `mLineWidth`, and `mMaxStep`
         * to 3 times `mMinStep`. We set the color of the `Paint mBackground` to black (for
         * no good reason, since we only retrieve the color to draw the canvas black, and have no other
         * use for the `Paint`. We set the color of the `Paint mBackground` to a dark
         * blue/green shade. set its anti alias flag to false, and set its stroke width to `mLineWidth`
         * (setting the color is useless, since it is set again before the `Paint` is used, but
         * what the hay).
         *
         *
         * Now we loop almost for ever (until our flag `mQuit` is changed to true by the main
         * thread, it does this in its `onDestroy` callback). Synchronizing on "this" we loop
         * waiting for our field `mSurface` to become non-null (it is set to the `SurfaceHolder`
         * passed to the `surfaceCreated` and `surfaceDestroyed` callbacks of the main thread)
         * and our field `mRunning` to become true (it is set to true in the `onResume`
         * callback of the main thread). While waiting it our `mActive` field is true we set it
         * to false and wake up the main thread (it waits for it to become false in its `surfaceDestroyed`
         * callback before returning to its caller). Then if our `mQuit` field has become true
         * we return to our caller, ending the thread (it is set to true in the `onDestroy` callback
         * of the main thread).
         *
         *
         * Now we wait for the main thread to release its lock on this instance, and once it has if
         * `mActive` is false we set it to true and notify the main thread (just in case its
         * `surfaceDestroyed` has been called in the meantime I suppose, since we hold the lock
         * it won't be able to proceed until we finish the `synchronized` block which releases
         * the lock until we try to get it again at the top of the while loop).
         *
         *
         * We now lock the `Canvas canvas` of `SurfaceHolder mSurface` for drawing. If
         * `canvas` is null after this (surface has not been created or otherwise cannot be edited)
         * we skip our drawing code and loop back to the start of our while loop. Next we check to see
         * if our `mInitialized` flag is false, and if so we set it to true, initialize both
         * `mPoint1` and `mPoint2` to the width and height of the `Canvas canvas`,
         * with a minimum step size of `mMinStep`, then initialize `mColor` to a red of
         * 127, a blue of 127 and a minimum step size of 1. If we have already initialized, we call
         * the `step` of `mPoint1` and `mPoint2` to move the points to a new random
         * position with a maximum width of the width of the canvas, maximum height of the height of
         * the canvas, a minimum step of `mMinStep` and a maximum step of `mMaxStep`. We
         * call the `step` method of `mColor` to "step" it to a random color with a maximum
         * red of 127, maximum blue of 127, a minimum step of 1, and a maximum step of 3.
         *
         *
         * Next we advance `mBrightLine` by 2, and if the result is greater than `NUM_OLD*2`
         * we set it to -2.
         *
         *
         * We set the entire `Canvas canvas` to the color of `Paint mBackground` (which is
         * set to black at the beginning of this method).
         *
         *
         * We next proceed to draw all the lines stored in the array `mOld`, with their colors
         * stored in the array `mOldColor` (or'ed with the green tinge appropriate for this line
         * as determined by the method `makeGreen`). To do this we loop starting with the oldest
         * line currently stored, setting the color of `Paint mForeground` to the old color
         * stored for it in `mOldColor` or'ed with the color returned by the method `makeGreen`
         * for that line number, and setting the alpha of the `Paint` color to that goes from
         * 255 for the newest line to `255/NUM_OLD` for the oldest possible line. Finally we
         * draw the line using `mForeground` as the `Paint` (each line occupies 4 locations
         * in `mOld` with each of those 4 values used as a coordinate for the two endpoints).
         *
         *
         * Now we draw the new line. We determine the color `color` using the latest value of
         * `mColor` by adding 128 to the value of `mColor.x` as the red, and 128 to the
         * value of `mColor.y` as the blue (limiting both to a maximum of 255). We shift and or
         * these values together with an alpha of 255 to make a proper ARGB format color for `color`.
         * Then we set the color of `Paint mForeground` to `color` or'ed with the green
         * tinge returned by our method `makeGreen` for an index of -2. We then draw a line
         * from `mPaint1` to `mPaint2` using `mForeground` as the `Paint`.
         *
         *
         * To add the new line to the old lines we first move all the old entries in `mOld` up
         * by 4 positions, and `mOldColor` up by 1 position. If `mNumOld` is less than
         * `NUM_OLD` we increment `mNumOld`. Then we copy `mPoint1.x` to `mOld[0]`,
         * `mPoint1.y` to `mOld[1]`. `mPoint2.x` to `mOld[2]`, `mPoint2.y`
         * to `mOld[3]`, and we copy `color` to `mOldColor[0]`.
         *
         *
         * Finally we call the method `unlockCanvasAndPost` which finishes editing pixels in
         * the surface, and loop back to the beginning of the while loop. (After this call, the
         * surface's current pixels will be shown on the screen, but its content is lost, in particular
         * there is no guarantee that the content of the Surface will remain unchanged when the method
         * `lockCanvas()` is called again.)
         */
        override fun run() {
            mLineWidth = (resources.displayMetrics.density * 1.5).toInt()
            if (mLineWidth < 1) mLineWidth = 1
            mMinStep = mLineWidth * 2.toFloat()
            mMaxStep = mMinStep * 3
            mBackground.color = -0x1000000
            mForeground.color = -0xff0001
            mForeground.isAntiAlias = false
            mForeground.strokeWidth = mLineWidth.toFloat()
            while (true) { // Synchronize with activity: block until the activity is ready
// and we have a surface; report whether we are active or inactive
// at this point; exit thread when asked to quit.
                synchronized(this) {
                    while (mSurface == null || !mRunning) {
                        if (mActive) {
                            mActive = false
                            (this as Object).notify()
                        }
                        if (mQuit) {
                            return
                        }
                        try {
                            (this as Object).wait()
                        } catch (e: InterruptedException) {
                            Log.i(TAG, e.localizedMessage!!)
                        }
                    }
                    if (!mActive) {
                        mActive = true
                        (this as Object).notify()
                    }
                    // Lock the canvas for drawing.
                    val canvas = mSurface!!.lockCanvas()
                    if (canvas == null) {
                        Log.i("WindowSurface", "Failure locking canvas")
                        return
                    }
                    // Update graphics.
                    if (!mInitialized) {
                        mInitialized = true
                        mPoint1.init(canvas.width, canvas.height, mMinStep)
                        mPoint2.init(canvas.width, canvas.height, mMinStep)
                        mColor.init(127, 127, 1f)
                    } else {
                        mPoint1.step(canvas.width, canvas.height, mMinStep, mMaxStep)
                        mPoint2.step(canvas.width, canvas.height, mMinStep, mMaxStep)
                        mColor.step(127, 127, 1f, 3f)
                    }
                    mBrightLine += 2
                    if (mBrightLine > Companion.NUM_OLD * 2) {
                        mBrightLine = -2
                    }
                    // Clear background.
                    canvas.drawColor(mBackground.color)
                    // Draw old lines.
                    for (i in mNumOld - 1 downTo 0) {
                        mForeground.color = mOldColor[i] or makeGreen(i)
                        mForeground.alpha = (Companion.NUM_OLD - i) * 255 / Companion.NUM_OLD
                        val p = i * 4
                        canvas.drawLine(mOld[p], mOld[p + 1], mOld[p + 2], mOld[p + 3], mForeground)
                    }
                    // Draw new line.
                    var red = mColor.x.toInt() + 128
                    if (red > 255) red = 255
                    var blue = mColor.y.toInt() + 128
                    if (blue > 255) blue = 255
                    val color = -0x1000000 or (red shl 16) or blue
                    mForeground.color = color or makeGreen(-2)
                    canvas.drawLine(mPoint1.x, mPoint1.y, mPoint2.x, mPoint2.y, mForeground)
                    // Add in the new line.
                    if (mNumOld > 1) {
                        System.arraycopy(mOld, 0, mOld, 4, (mNumOld - 1) * 4)
                        System.arraycopy(mOldColor, 0, mOldColor, 1, mNumOld - 1)
                    }
                    if (mNumOld < Companion.NUM_OLD) mNumOld++
                    mOld[0] = mPoint1.x
                    mOld[1] = mPoint1.y
                    mOld[2] = mPoint2.x
                    mOld[3] = mPoint2.y
                    mOldColor[0] = color
                    // All done!
                    mSurface!!.unlockCanvasAndPost(canvas)
                }
            }
        }


    }

    companion object {
        /**
         * TAG for logging.
         */
        private const val TAG = "WindowSurface"
        /**
         * Number of old lines to remember for next display refresh.
         */
        const val NUM_OLD = 100
    }
}
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
package com.example.android.apis.graphics

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import com.example.android.apis.graphics.TouchPaint.Companion.BACKGROUND_COLOR
import com.example.android.apis.graphics.TouchPaint.Companion.COLORS
import com.example.android.apis.graphics.TouchPaint.Companion.FADE_DELAY
import com.example.android.apis.graphics.TouchPaint.Companion.MSG_FADE
import com.example.android.apis.graphics.TouchPaint.PaintView.Companion.FADE_ALPHA
import com.example.android.apis.graphics.TouchPaint.PaintView.Companion.MAX_FADE_STEPS
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withSave

/**
 * Demonstrates the handling of touch screen, stylus, mouse and trackball events to
 * implement a simple painting app.
 *
 * Drawing with a touch screen is accomplished by drawing a point at the
 * location of the touch. When pressure information is available, it is used
 * to change the intensity of the color. When size and orientation information
 * is available, it is used to directly adjust the size and orientation of the
 * brush.
 *
 * Drawing with a stylus is similar to drawing with a touch screen, with a
 * few added refinements. First, there may be multiple tools available including
 * an eraser tool.  Second, the tilt angle and orientation of the stylus can be
 * used to control the direction of paint. Third, the stylus buttons can be used
 * to perform various actions. Here we use one button to cycle colors and the
 * other to airbrush from a distance.
 *
 * Drawing with a mouse is similar to drawing with a touch screen, but as with
 * a stylus we have extra buttons. Here we use the primary button to draw,
 * the secondary button to cycle colors and the tertiary button to airbrush.
 *
 * Drawing with a trackball is a simple matter of using the relative motions
 * of the trackball to move the paint brush around. The trackball may also
 * have a button, which we use to cycle through colors.
 * RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
 */
class TouchPaint : GraphicsActivity() {
    /**
     * The view responsible for drawing the window.
     */
    var mView: PaintView? = null

    /**
     * Is fading mode enabled?
     */
    private var mFading = false

    /**
     * [Handler] which calls the [PaintView.fade] method of [PaintView] field [mView] every
     * [FADE_DELAY] (100ms) to "fade" the finger painting (it does this only if the message
     * received is a [MSG_FADE] message).
     */
    private val mHandler: Handler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_FADE -> {
                    mView!!.fade()
                    scheduleFade()
                }

                else -> super.handleMessage(msg)
            }
        }
    }

    /**
     * Mode we are painting in:
     *
     *  * Draw - we draw a loci of the movement of the finger
     *  * Splat - random splatter across the canvas ala Jackson Pollock
     *  * Erase - erases where the finger moves
     *
     * Without a keyboard, only Draw is used.
     */
    internal enum class PaintMode {
        Draw, Splat, Erase
    }

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, initialize our [PaintView] field [mView] with a new instance of [PaintView], set
     * our content view to it, and enable it to receive focus. Then if our parameter [savedInstanceState]
     * is not null we set our [Boolean] field [mFading] with the value stored in [savedInstanceState]
     * under the key "fading" (defaulting to true if no value was found), and we set the field
     * `mColorIndex` of our [PaintView] field [mView] to the value saved under the key "color"
     * (defaulting to 0 if no value was found). If our parameter [savedInstanceState] is null
     * (first time running) we set [mFading] to true, and the field `mColorIndex` of [mView] to 0.
     *
     * @param savedInstanceState In our [onSaveInstanceState] we save the value of our field
     * [mFading] and the value of the field `mColorIndex` of our [PaintView] field [mView] under
     * the keys "fading" and "color" respectively, so if [savedInstanceState] is not null, we set
     * them to the values stored in it here.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create and attach the view that is responsible for painting.
        mView = PaintView(this)
        setContentView(mView!!)
        mView!!.requestFocus()

        // Restore the fading option if we are being thawed from a
        // previously saved state.  Note that we are not currently remembering
        // the contents of the bitmap.
        if (savedInstanceState != null) {
            mFading = savedInstanceState.getBoolean("fading", true)
            mView!!.mColorIndex = savedInstanceState.getInt("color", 0)
        } else {
            mFading = true
            mView!!.mColorIndex = 0
        }
    }

    /**
     * We initialize the contents of the Activity's standard options menu here, adding our menu items
     * to the [Menu] parameter [menu]. We add a menu item with the id CLEAR_ID and the title "Clear"
     * to [menu], and a menu item with the id FADE_ID and the title "Fade" to [menu] setting its
     * checkable state to true. Finally we return the value returned by our super's implementation
     * of `onCreateOptionsMenu`.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed; if you return false it will not
     * be shown.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, CLEAR_ID, 0, "Clear")
        menu.add(0, FADE_ID, 0, "Fade").isCheckable = true
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * We prepare the Screen's standard options menu to be displayed here. We find our item with the
     * id FADE_ID in our [Menu] parameter [menu] and set its checkable state to the value of our
     * [Boolean] field [mFading]. Then we return the value returned by our super's implementation
     * `onPrepareOptionsMenu` to our caller.
     *
     * @param menu The options menu as last shown or first initialized by onCreateOptionsMenu().
     * @return You must return true for the menu to be displayed; if you return false it will not be shown.
     */
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(FADE_ID).isChecked = mFading
        return super.onPrepareOptionsMenu(menu)
    }

    /**
     * This hook is called whenever an item in our options menu is selected. We switch on the value
     * of the identifier of our [MenuItem] parameter [item]:
     *
     *  * CLEAR_ID - we call the `clear` method of `PaintView mView` then return true
     *
     *  * FADE_ID - we toggle the value of our field `boolean mFading`, and if the new value is
     *  true we call our method `startFading`, if false we call our method `stopFading`.
     *  In either case we return true to our caller.
     *
     *  * default - we return the value returned by our super's implementation of `onOptionsItemSelected`
     *
     * @param item The menu item that was selected.
     * @return [Boolean] return false to allow normal menu processing to proceed, true to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            CLEAR_ID -> {
                mView!!.clear()
                true
            }

            FADE_ID -> {
                mFading = !mFading
                if (mFading) {
                    startFading()
                } else {
                    stopFading()
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Called after [onRestoreInstanceState], [onRestart], or [onPause], for our activity to start
     * interacting with the user. First we call through to our super's implementation of `onResume`,
     * then if our[Boolean] field [mFading] is true we call our method [startFading].
     */
    override fun onResume() {
        super.onResume()

        // If fading mode is enabled, then as long as we are resumed we want
        // to run pulse to fade the contents.
        if (mFading) {
            startFading()
        }
    }

    /**
     * Called to retrieve per-instance state from an activity before being killed so that the state
     * can be restored in [onCreate] or [onRestoreInstanceState] (the [Bundle] populated by this
     * method will be passed to both). First we call through to our super's implementation of
     * `onSaveInstanceState`, then we save the value of our [Boolean] field [mFading] in our [Bundle]
     * [outState] under the key "fading", and the value of the field `mColorIndex` of our [PaintView]
     * field [mView] under the key "color".
     *
     * @param outState [Bundle] in which to place your saved state.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Save away the fading state to restore if needed later.  Note that
        // we do not currently save the contents of the display.
        outState.putBoolean("fading", mFading)
        outState.putInt("color", mView!!.mColorIndex)
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. First we call through to our super's implementation of `onPause`,
     * then we call our method [stopFading] to stop the pulse that fades the screen.
     */
    override fun onPause() {
        super.onPause()

        // Make sure to never run the fading pulse while we are paused or stopped.
        stopFading()
    }

    /**
     * Start up the pulse to fade the screen, first clearing any existing pulse to ensure that we
     * don't have multiple pulses running at a time. Then we call our method [scheduleFade] to
     * schedule a new [FADE_DELAY] message to our [Handler] field [mHandler].
     */
    private fun startFading() {
        mHandler.removeMessages(MSG_FADE)
        scheduleFade()
    }

    /**
     * Stop the pulse to fade the screen. To do this we simply remove all of the [MSG_FADE] messages
     * in the queue of [Handler] field [mHandler].
     */
    private fun stopFading() {
        mHandler.removeMessages(MSG_FADE)
    }

    /**
     * Schedule a fade message for later. We simply call the [Handler.sendMessageDelayed] method of
     * [Handler] field [mHandler] to send a [MSG_FADE] message with a delay of [FADE_DELAY] (100ms).
     */
    fun scheduleFade() {
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_FADE), FADE_DELAY.toLong())
    }

    /**
     * This view implements the drawing canvas. It handles all of the input events and drawing
     * functions.
     */
    open class PaintView : View {
        /**
         * Random number generator used by the [drawSplat] method to create random splat vectors.
         */
        private val mRandom = Random()

        /**
         * [Bitmap] used by [Canvas] field [mCanvas] to draw into. Our method [onSizeChanged] also
         * uses it to remember what has been drawn when the size of the window changes.
         */
        private var mBitmap: Bitmap? = null

        /**
         * [Canvas] we draw on. When our [onDraw] method is called we draw the [Bitmap] field
         * [mBitmap] that [mCanvas] draws onto on the [Canvas] passed as a parameter to [onDraw]
         * (our view's [Canvas]).
         */
        private var mCanvas: Canvas? = null

        /**
         * [Paint] we use to draw with.
         */
        private val mPaint = Paint()

        /**
         * [Paint] our fade thread uses to "fade" the finger painting.
         */
        private val mFadePaint = Paint()

        /**
         * Last known X coordinate of a move by finger or trackball.
         */
        private var mCurX = 0f

        /**
         * Last known Y coordinate of a move by finger or trackball.
         */
        private var mCurY = 0f

        /**
         * Old state of all buttons that are pressed such as a mouse or stylus button, used to tell
         * when one of the button has changed state.
         */
        private var mOldButtonState = 0

        /**
         * Number of times the fade thread of [Handler] field [mHandler] has called our method
         * [fade] to fade our finger painting. When it reaches [MAX_FADE_STEPS] our [fade] method
         * stops "fading". It is set to 0 to start fading again when our methods [paint] and [text]
         * are called.
         */
        private var mFadeSteps = MAX_FADE_STEPS

        /**
         * The index of the current color to use.
         */
        var mColorIndex: Int = 0

        /**
         * Our constructor. First we call our super's constructor, then we call our method [init]
         * to initialize our instance.
         *
         * @param c [Context] to use to access resources, "this" in the `onCreate` method
         * of [TouchPaint]
         */
        constructor(c: Context?) : super(c) {
            init()
        }

        /**
         * Constructor that is called when our view is inflated from xml. `GameActivity.Content`
         * extends us, and it is inflated in the layout file of `GameActivity` R.layout.game.
         * First we call through to our super's constructor, then we call our method [init] to
         * initialize our instance.
         *
         * @param c     [Context] our view is running in, through which we can access the current
         * theme, resources, etc.
         * @param attrs attributes of the XML tag that is inflating this view.
         */
        constructor(c: Context?, attrs: AttributeSet?) : super(c, attrs) {
            init()
        }

        /**
         * Our initialization method, called from our constructors. First we enable our view to
         * receive focus, then we set the anti alias flag of [Paint] field [mPaint], set the color
         * of [Paint] field [mFadePaint] to [BACKGROUND_COLOR] ([Color.BLACK], and set its alpha to
         * [FADE_ALPHA] (0x06).
         */
        private fun init() {
            isFocusable = true
            mPaint.isAntiAlias = true
            mFadePaint.color = BACKGROUND_COLOR
            mFadePaint.alpha = FADE_ALPHA
        }

        /**
         * Clears the [Canvas] field [mCanvas]. If [mCanvas] is not null, we set the color of [Paint]
         * field [mPaint] to [BACKGROUND_COLOR] ([Color.BLACK]), fill the entire [mCanvas] with the
         * color of [mPaint], call [invalidate] to schedule [onDraw] to be called to copy [mCanvas]
         * to the view's [Canvas], and finally set [mFadeSteps] to [MAX_FADE_STEPS] (89).
         */
        fun clear() {
            if (mCanvas != null) {
                mPaint.color = BACKGROUND_COLOR
                mCanvas!!.drawPaint(mPaint)
                invalidate()
                mFadeSteps = MAX_FADE_STEPS
            }
        }

        /**
         * "Fades" the [Canvas] field [mCanvas]. If [mCanvas] is not null, and if [mFadeSteps]
         * is less than [MAX_FADE_STEPS] (89) we fill the entire [mCanvas] with [Paint] field
         * [mFadePaint], and call [invalidate] so [onDraw] will be called to copy [mCanvas] to
         * the view's [Canvas]. Finally we increment [mFadeSteps].
         */
        fun fade() {
            if (mCanvas != null && mFadeSteps < MAX_FADE_STEPS) {
                mCanvas!!.drawPaint(mFadePaint)
                invalidate()
                mFadeSteps++
            }
        }

        /**
         * Draws the [String] parameter [text] to [Canvas] field [mCanvas] and causes [onDraw] to
         * copy [mCanvas] to the view's [Canvas]. Before doing anything, we make sure that [Bitmap]
         * field [mBitmap] is not null, returning having done nothing if it is null. Otherwise we
         * set [Int] `val width` to the width of [mBitmap], and [Int] `val height` to the height of
         * [mBitmap]. We set the color of [Paint] field [mPaint] to the color currently selected by
         * [mColorIndex] in the [IntArray] field [COLORS] , and its alpha to 255. We set `var size`
         * to `height`, and set the text size of [mPaint] to `size`. We create a [Rect] `val bounds`,
         * and fetch the text bounds of [String] parameter [text] drawn using [mPaint] to `bounds`.
         * We set `var twidth` to the width of `bounds`, then increment it by one quarter of itself.
         * If `twidth` is greater than `width`, we set size to `(size*width)/twidth`, set the text
         * size of [mPaint] to `size`, and retrieve the text bounds of `text` drawn using [mPaint]
         * to `bounds`. We fetch the font metrics of [mPaint] to [Paint.FontMetrics] `val fm`, so
         * that we can use the `fm.ascent` field. We then call the [Canvas.drawText] method of
         * [mCanvas] to draw the [String] parameter [text] using [mPaint] with the x coordinate
         * calculated to center the text in the middle of the [Canvas], and the y coordinate
         * calculated to position the text in a weird part of the screen (probably a bug?). We set
         * [mFadeSteps] to 0 so that fading will start again, and call [invalidate] so that a call
         * to our [onDraw] method will be scheduled to copy [mCanvas] to the view's [Canvas].
         *
         * @param text String to display
         */
        fun text(text: String) {
            if (mBitmap != null) {
                val width = mBitmap!!.width
                val height = mBitmap!!.height
                mPaint.color = COLORS[mColorIndex]
                mPaint.alpha = 255
                var size = height
                mPaint.textSize = size.toFloat()
                val bounds = Rect()
                mPaint.getTextBounds(text, 0, text.length, bounds)
                var twidth = bounds.width()
                twidth += twidth / 4
                if (twidth > width) {
                    size = size * width / twidth
                    mPaint.textSize = size.toFloat()
                    mPaint.getTextBounds(text, 0, text.length, bounds)
                }
                val fm = mPaint.fontMetrics
                mCanvas!!.drawText(
                    text,
                    (width - bounds.width()) / 2f,
                    (height - size) / 2f - fm.ascent,
                    mPaint
                )
                mFadeSteps = 0
                invalidate()
            }
        }

        /**
         * This is called during layout when the size of this view has changed. If you were just
         * added to the view hierarchy, you're called with the old values of 0. If [Bitmap] field
         * [mBitmap] is not null we set [Int] `var curW` to the width of [mBitmap] and `var curH`
         * to the height of [mBitmap], if it is null we set them both to 0. If `curW` is greater
         * than or equal to [Int] parameter [w] and `curH` is greater than or equal to [Int]
         * parameter [h] we return having done nothing.
         *
         * If `curW` is less than [w] we set it to [w], and if `curH` is less than [h] we set it to
         * [h]. We create [Bitmap] `val newBitmap` to be `curW` by `curH` using the ARGB_8888 format.
         * We allocate a new [Canvas] `val newCanvas` and set `newBitmap` to be the [Bitmap] for it
         * to draw into. If [Bitmap] field [mBitmap] is not null we draw it into `newCanvas` (this
         * function will take care of automatically scaling the bitmap to draw at the same density
         * as the canvas drawn into). Then we set our [Bitmap] field [mBitmap]` to `newBitmap`, and
         * [Canvas] field [mCanvas] to `newCanvas`.
         *
         * Finally we set [mFadeSteps] to [MAX_FADE_STEPS] so that fading will pause until new
         * finger painting starts.
         *
         * @param w    Current width of this view.
         * @param h    Current height of this view.
         * @param oldw Old width of this view.
         * @param oldh Old height of this view.
         */
        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            var curW = if (mBitmap != null) mBitmap!!.width else 0
            var curH = if (mBitmap != null) mBitmap!!.height else 0
            if (curW >= w && curH >= h) {
                return
            }
            if (curW < w) curW = w
            if (curH < h) curH = h
            val newBitmap = createBitmap(width = curW, height = curH)
            val newCanvas = Canvas()
            newCanvas.setBitmap(newBitmap)
            if (mBitmap != null) {
                newCanvas.drawBitmap(mBitmap!!, 0f, 0f, null)
            }
            mBitmap = newBitmap
            mCanvas = newCanvas
            mFadeSteps = MAX_FADE_STEPS
        }

        /**
         * We implement this to do our drawing. If [Bitmap] field [mBitmap] is not null we draw it
         * to our [Canvas] argument [canvas].
         *
         * @param canvas the [Canvas] on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            if (mBitmap != null) {
                canvas.drawBitmap(mBitmap!!, 0f, 0f, null)
            }
        }

        /**
         * We implement this method to handle trackball motion events. I do not have a trackball
         * connected to an Android device, so I will not comment.
         *
         * @param event The motion event.
         * @return True if the event was handled, false otherwise.
         */
        override fun onTrackballEvent(event: MotionEvent): Boolean {
            val action = event.actionMasked
            if (action == MotionEvent.ACTION_DOWN) {
                // Advance color when the trackball button is pressed.
                advanceColor()
            }
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                val n = event.historySize
                val scaleX = event.xPrecision * TRACKBALL_SCALE
                val scaleY = event.yPrecision * TRACKBALL_SCALE
                for (i in 0 until n) {
                    moveTrackball(
                        deltaX = event.getHistoricalX(i) * scaleX,
                        deltaY = event.getHistoricalY(i) * scaleY
                    )
                }
                moveTrackball(deltaX = event.x * scaleX, deltaY = event.y * scaleY)
            }
            return true
        }

        /**
         * Adds the change in x and y to [mCurX] and [mCurY] and draws an oval at the new point.
         *
         * @param deltaX X coordinate change
         * @param deltaY Y coordinate change
         */
        private fun moveTrackball(deltaX: Float, deltaY: Float) {
            val curW = if (mBitmap != null) mBitmap!!.width else 0
            val curH = if (mBitmap != null) mBitmap!!.height else 0
            mCurX = (mCurX + deltaX)
                .coerceAtMost(curW - 1.toFloat())
                .coerceAtLeast(0f)
            mCurY = (mCurY + deltaY)
                .coerceAtMost(curH - 1.toFloat())
                .coerceAtLeast(0f)
            paint(PaintMode.Draw, mCurX, mCurY)
        }

        /**
         * We implement this method to handle touch screen motion events. We simply return the value
         * returned by our method [onTouchOrHoverEvent] when its `isTouch` argument is
         * true.
         *
         * @param event The motion event.
         * @return True if the event was handled, false otherwise.
         */
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(event: MotionEvent): Boolean {
            return onTouchOrHoverEvent(event, true /*isTouch*/)
        }

        /**
         * We implement this method to handle hover events. We simply return the value returned by
         * our method [onTouchOrHoverEvent] when its `isTouch` argument is false.
         *
         * @param event The motion event that describes the hover.
         * @return True if the view handled the hover event.
         */
        override fun onHoverEvent(event: MotionEvent): Boolean {
            return onTouchOrHoverEvent(event, false /*isTouch*/)
        }

        /**
         * Handles both touch and hover events. First we fetch the state of all buttons that are
         * pressed to `buttonState`, and isolate the bits that have changed from the state saved
         * in `mOldButtonState` to `pressedButtons`. We then set `mOldButtonState`
         * to `buttonState`. If the newly pressed buttons in `pressedButtons` includes
         * the BUTTON_SECONDARY button, we call our method `advanceColor` to change the color
         * used to draw to the next one in line.
         *
         * Next we declare `PaintMode mode`, and if the BUTTON_TERTIARY is pressed we set
         * `mode` to `PaintMode.Splat`, if the event is a touch event or BUTTON_PRIMARY
         * is pressed we set `mode` to `PaintMode.Draw`. Otherwise we return having done
         * nothing.
         *
         * We initialize `action` to the masked action being performed in `event`. If the
         * `action` is ACTION_DOWN, or ACTION_MOVE, or ACTION_HOVER_MOVE we initialize `N`
         * to the number of historical points in `event`, and `P` to the number of pointers
         * of data contained in `event`. We loop through the `N` historical points, and
         * for each of the `P` pointers we call our method `paint` to paint an oval at the
         * historical (x,y) for the pointer and event index of the historical point in question, using
         * the tool type of the pointer to decide whether to use our current `mode` or to use
         * `PaintMode.Erase` if the tool type was TOOL_TYPE_ERASER. We also pass it the historical
         * pressure, historical touch major axis coordinate, historical touch minor axis coordinate,
         * historical orientation coordinate, historical value of the AXIS_DISTANCE axis, and the
         * historical value of the AXIS_TILT axis for the data point being processed.
         *
         * Once we have painted all of the historical points, we do the same thing using the values
         * for the current data point, and set our fields `mCurX` and `mCurY` to the x
         * and y coordinates of the first pointer index. Then we return true to the caller whether
         * we had anything to draw or not.
         *
         * @param event   The motion event.
         * @param isTouch true if the event was a touch event, false if it was a hover event
         * @return True if the event was handled, false otherwise.
         */
        private fun onTouchOrHoverEvent(event: MotionEvent, isTouch: Boolean): Boolean {
            val buttonState = event.buttonState
            val pressedButtons = buttonState and mOldButtonState.inv()
            mOldButtonState = buttonState
            if (pressedButtons and MotionEvent.BUTTON_SECONDARY != 0) {
                // Advance color when the right mouse button or first stylus button
                // is pressed.
                advanceColor()
            }
            val mode: PaintMode = if (buttonState and MotionEvent.BUTTON_TERTIARY != 0) {
                // Splat paint when the middle mouse button or second stylus button is pressed.
                PaintMode.Splat
            } else if (isTouch || buttonState and MotionEvent.BUTTON_PRIMARY != 0) {
                // Draw paint when touching or if the primary button is pressed.
                PaintMode.Draw
            } else {
                // Otherwise, do not paint anything.
                return false
            }
            val action = event.actionMasked
            if (action == MotionEvent.ACTION_DOWN ||
                action == MotionEvent.ACTION_MOVE ||
                action == MotionEvent.ACTION_HOVER_MOVE
            ) {
                val n = event.historySize
                val p = event.pointerCount
                for (i in 0 until n) {
                    for (j in 0 until p) {
                        paint(
                            mode = getPaintModeForTool(
                                toolType = event.getToolType(/* pointerIndex = */ j),
                                defaultMode = mode
                            ),
                            x = event.getHistoricalX(j, i),
                            y = event.getHistoricalY(j, i),
                            pressure = event.getHistoricalPressure(j, i),
                            major = event.getHistoricalTouchMajor(j, i),
                            minor = event.getHistoricalTouchMinor(j, i),
                            orientation = event.getHistoricalOrientation(j, i),
                            distance = event.getHistoricalAxisValue(
                                /* axis = */ MotionEvent.AXIS_DISTANCE,
                                /* pointerIndex = */ j,
                                /* pos = */ i
                            ),
                            tilt = event.getHistoricalAxisValue(
                                /* axis = */ MotionEvent.AXIS_TILT,
                                /* pointerIndex = */ j,
                                /* pos = */ i
                            )
                        )
                    }
                }
                for (j in 0 until p) {
                    paint(
                        mode = getPaintModeForTool(
                            toolType = event.getToolType(/* pointerIndex = */ j),
                            defaultMode = mode
                        ),
                        x = event.getX(j),
                        y = event.getY(j),
                        pressure = event.getPressure(j),
                        major = event.getTouchMajor(j),
                        minor = event.getTouchMinor(j),
                        orientation = event.getOrientation(j),
                        distance = event.getAxisValue(MotionEvent.AXIS_DISTANCE, j),
                        tilt = event.getAxisValue(MotionEvent.AXIS_TILT, j)
                    )
                }
                mCurX = event.x
                mCurY = event.y
            }
            return true
        }

        /**
         * Returns the correct `PaintMode` to use to paint, either `PaintMode.Erase` is
         * the `toolType` is TOOL_TYPE_ERASER, or `defaultMode`.
         *
         * @param toolType    the type of tool used to make contact such as a finger or stylus, if known.
         * @param defaultMode `PaintMode` to return if the tool type is not TOOL_TYPE_ERASER
         * @return either `PaintMode.Erase` is the `toolType` is TOOL_TYPE_ERASER, otherwise
         * returns `defaultMode`.
         */
        private fun getPaintModeForTool(toolType: Int, defaultMode: PaintMode): PaintMode {
            return if (toolType == MotionEvent.TOOL_TYPE_ERASER) {
                PaintMode.Erase
            } else defaultMode
        }

        /**
         * Increments `mColorIndex` modulo `COLORS.length`. `mColorIndex` is Used
         * to select a color from the array `int[] COLORS` in order to set the color of
         * `Paint mPaint`.
         */
        private fun advanceColor() {
            mColorIndex = (mColorIndex + 1) % COLORS.size
        }

        /**
         * Draws an oval in the manner specified by its parameters. If our field `Bitmap mBitmap`
         * is not null we have a `Canvas mCanvas` that we can use to draw into `mBitmap`
         * and we proceed to do so. First we make sure that both of parameters `major` and
         * `minor` are greater than 0, and if not we set them to the default value 16. Then we
         * switch based on the value of our parameter `PaintMode mode`:
         *
         *  * Draw: we set the color of `Paint mPaint` to the color in the array `COLORS`
         *  pointed to by `mColorIndex`, set its alpha to the lesser of `pressure*128`
         *  and 255, then call our method `drawOval` to use `mPaint` to draw an oval
         *  on `mCanvas` at (`x`,`y`) with the size of the containing `RectF`
         *  being `minor` by `major` in size, and rotated by `orientation` radians.
         *
         *  * Erase: we set the color of `Paint mPaint` to the color BACKGROUND_COLOR, set its
         *  alpha to the lesser of `pressure*128` and 255, then call our method `drawOval`
         *  to use `mPaint` to draw an oval on `mCanvas` at (`x`,`y`) with
         *  the size of the containing `RectF` being `minor` by `major` in size,
         *  and rotated by `orientation` radians.
         *
         *  * Erase: we set the color of `Paint mPaint` to the color in the array `COLORS`
         *  pointed to by `mColorIndex`, set its alpha to 64, and use our method `drawSplat`
         *  to use `mPaint` "splatter" paint on `mCanvas` using the other parameters to
         *  control where and how much paint is randomly splattered to the canvas.
         *
         * Whether we did any drawing or not, we set `mFadeSteps` to 0, and invalidate the view
         * so that the current `mBitmap` (if it exists) will be drawn to the views canvas by
         * our `onDraw` method, and fading will start if it was stopped.
         *
         *
         * @param mode        `PaintMode` to use, one of "Draw", "Erase", or "Splat".
         * @param x           x coordinate of oval to be drawn
         * @param y           y coordinate of oval to be drawn
         * @param pressure    "Pressure" of the touch, used to set the alpha of `Paint mPaint`.
         * @param major       used to calculate the x size of the `RectF` of the oval to be drawn.
         * @param minor       used to calculate the y size of the `RectF` of the oval to be drawn.
         * @param orientation used to rotate the canvas before drawing the oval.
         * @param distance    "Distance" to splat the paint if `PaintMode` is "Splat". It is the
         * value of the AXIS_DISTANCE axis of the motion event. For a stylus, reports
         * the distance of the stylus from the screen. A value of 0.0 indicates direct
         * contact and larger values indicate increasing distance from the surface.
         * @param tilt        "Tilt" to use to splat the paint if `PaintMode` is "Splat". It is the
         * value of the AXIS_TILT tilt axis of a motion event. Which for a stylus,
         * reports the tilt angle of the stylus in radians where 0 radians indicates
         * that the stylus is being held perpendicular to the surface, and PI/2 radians
         * indicates that the stylus is being held flat against the surface.
         */
        private fun paint(
            mode: PaintMode, x: Float, y: Float, pressure: Float = 1.0f,
            major: Float = 0f, minor: Float = 0f, orientation: Float = 0f,
            distance: Float = 0f, tilt: Float = 0f
        ) {
            var majorVar = major
            var minorVar = minor
            if (mBitmap != null) {
                if (majorVar <= 0 || minorVar <= 0) {
                    // If size is not available, use a default value.
                    minorVar = 16f
                    majorVar = minorVar
                }
                when (mode) {
                    PaintMode.Draw -> {
                        mPaint.color = COLORS[mColorIndex]
                        mPaint.alpha = (pressure * 128).toInt().coerceAtMost(maximumValue = 255)
                        drawOval(
                            canvas = mCanvas,
                            x = x,
                            y = y,
                            major = majorVar,
                            minor = minorVar,
                            orientation = orientation,
                            paint = mPaint
                        )
                    }

                    PaintMode.Erase -> {
                        mPaint.color = BACKGROUND_COLOR
                        mPaint.alpha = (pressure * 128).toInt().coerceAtMost(255)
                        drawOval(
                            canvas = mCanvas,
                            x = x,
                            y = y,
                            major = majorVar,
                            minor = minorVar,
                            orientation = orientation,
                            paint = mPaint
                        )
                    }

                    PaintMode.Splat -> {
                        mPaint.color = COLORS[mColorIndex]
                        mPaint.alpha = 64
                        drawSplat(
                            canvas = mCanvas,
                            x = x,
                            y = y,
                            orientation = orientation,
                            distance = distance,
                            tilt = tilt,
                            paint = mPaint
                        )
                    }
                }
            }
            mFadeSteps = 0
            invalidate()
        }

        /**
         * `RectF` used to size the oval drawn by our method `drawOval`.
         */
        private val mReusableOvalRect = RectF()

        /**
         * Draw an oval. When the orientation is 0 radians, orients the major axis vertically, angles
         * less than or greater than 0 radians rotate the major axis left or right. First we save the
         * current matrix and clip of our parameter `Canvas canvas` onto a private stack. Then
         * we rotate the current matrix of `Canvas canvas` by our parameter `orientation`
         * (after first converting it to degrees). We configure `RectF mReusableOvalRect` to be
         * the size specified by our parameters `minor` and `major` centered at the point
         * `(x,y)`, then we use it to draw an oval on `canvas` using `paint` as the
         * `Paint`. Finally we restore the state of the current matrix and clip of `canvas`
         * to that it had when our method was called.
         *
         * @param canvas      `Canvas` to draw our oval on
         * @param x           X coordinate of center of our oval
         * @param y           Y coordinate of center of our oval
         * @param major       size of our bounding `RectF` on Y axis
         * @param minor       size of our bounding `RectF` on X axis
         * @param orientation radians clockwise from vertical to rotate the oval
         * @param paint       `Paint` to use to draw our oval
         */
        private fun drawOval(
            canvas: Canvas?,
            x: Float,
            y: Float,
            major: Float,
            minor: Float,
            orientation: Float,
            paint: Paint
        ) {
            canvas!!.withSave {
                canvas.rotate((orientation * 180 / Math.PI).toFloat(), x, y)
                mReusableOvalRect.left = x - minor / 2
                mReusableOvalRect.right = x + minor / 2
                mReusableOvalRect.top = y - major / 2
                mReusableOvalRect.bottom = y + major / 2
                canvas.drawOval(mReusableOvalRect, paint)
            }
        }

        /**
         * Splatter paint in an area.
         *
         * Chooses random vectors describing the flow of paint from a round nozzle
         * across a range of a few degrees. Then adds this vector to the direction
         * indicated by the orientation and tilt of the tool and throws paint at
         * the canvas along that vector.
         *
         * Repeats the process until a masterpiece is born.
         *
         * @param canvas      We ignore this, and splatter our paint on `Canvas mCanvas` instead.
         * @param x           X coordinate of the center of the splatter
         * @param y           Y coordinate of the center of the splatter
         * @param orientation angle describes the direction of movement since last position event.
         * @param distance    "Distance" to splat the paint. It is the value of the AXIS_DISTANCE axis
         * of the motion event. For a stylus, the distance of the stylus from the screen.
         * @param tilt        "Tilt" to use to splat the paint. It is the value of the AXIS_TILT tilt axis
         * of a motion event. Which for a stylus, reports the tilt angle of the stylus in
         * radians where 0 radians indicates that the stylus is being held perpendicular
         * to the surface, and PI/2 radians indicates that the stylus is being held flat
         * against the surface.
         * @param paint       `Paint` to use to splatter the `Canvas mCanvas`.
         */
        @Suppress("UNUSED_PARAMETER")
        private fun drawSplat(
            canvas: Canvas?, x: Float, y: Float, orientation: Float,
            distance: Float, tilt: Float, paint: Paint
        ) {
            val z = distance * 2 + 10

            // Calculate the center of the spray.
            val nx = (sin(orientation.toDouble()) * sin(tilt.toDouble())).toFloat()
            val ny = (-cos(orientation.toDouble()) * sin(tilt.toDouble())).toFloat()
            val nz = cos(tilt.toDouble()).toFloat()
            if (nz < 0.05) {
                return
            }
            val cd = z / nz
            val cx = nx * cd
            val cy = ny * cd
            for (i in 0 until SPLAT_VECTORS) {
                // Make a random 2D vector that describes the direction of a speck of paint
                // ejected by the nozzle in the nozzle's plane, assuming the tool is
                // perpendicular to the surface.
                val direction = mRandom.nextDouble() * Math.PI * 2
                val dispersion = mRandom.nextGaussian() * 0.2
                var vx = cos(direction) * dispersion
                var vy = sin(direction) * dispersion
                var vz = 1.0

                // Apply the nozzle tilt angle.
                var temp = vy
                vy = temp * cos(tilt.toDouble()) - vz * sin(tilt.toDouble())
                vz = temp * sin(tilt.toDouble()) + vz * cos(tilt.toDouble())

                // Apply the nozzle orientation angle.
                temp = vx
                vx = temp * cos(orientation.toDouble()) - vy * sin(orientation.toDouble())
                vy = temp * sin(orientation.toDouble()) + vy * cos(orientation.toDouble())

                // Determine where the paint will hit the surface.
                if (vz < 0.05) {
                    continue
                }
                val pd = (z / vz).toFloat()
                val px = (vx * pd).toFloat()
                val py = (vy * pd).toFloat()

                // Throw some paint at this location, relative to the center of the spray.
                mCanvas!!.drawCircle(x + px - cx, y + py - cy, 1.0f, paint)
            }
        }

        companion object {
            /**
             * Alpha used by `Paint mFadePaint` to fade the finger painting.
             */
            private const val FADE_ALPHA = 0x06

            /**
             * Maximum number of times our fade thread is run (89).
             */
            private const val MAX_FADE_STEPS = 256 / (FADE_ALPHA / 2) + 4

            /**
             * Constant used by the method `onTrackballEvent` to multiply the value returned by
             * `getXPrecision` and `getYPrecision` to scale the precision of the coordinates
             * being reported by `getX` and `getY` (as well as the values returned by
             * `getHistoricalX` and `getHistoricalY`) when a trackball is used to "finger
             * paint" using the `moveTrackball` method.
             */
            private const val TRACKBALL_SCALE = 10

            /**
             * Number of random splat vectors generated and drawn by the method `drawSplat`.
             */
            private const val SPLAT_VECTORS = 40
        }
    }

    companion object {
        /**
         * Used as a pulse to gradually fade the contents of the window.
         */
        private const val MSG_FADE = 1

        /**
         * Menu ID for the command to clear the window.
         */
        private const val CLEAR_ID = Menu.FIRST

        /**
         * Menu ID for the command to toggle fading.
         */
        private const val FADE_ID = Menu.FIRST + 1

        /**
         * How often to fade the contents of the window (in ms).
         */
        private const val FADE_DELAY = 100

        /**
         * Colors to cycle through.
         */
        val COLORS: IntArray = intArrayOf(
            Color.WHITE, Color.RED, Color.YELLOW, Color.GREEN,
            Color.CYAN, Color.BLUE, Color.MAGENTA
        )

        /**
         * Background color.
         */
        const val BACKGROUND_COLOR: Int = Color.BLACK
    }
}
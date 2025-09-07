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
package com.example.android.apis.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View

/**
 * A view that has a known number of selectable rows, and maintains a notion of which
 * row is selected. The rows take up the entire width of the view.  The height of the
 * view is divided evenly among the rows.
 *
 * Notice what this view does to be a good citizen w.r.t its internal selection:
 *
 *  * 1) calls [requestRectangleOnScreen] each time the selection changes due to internal navigation.
 *
 *  * 2) overrides [getFocusedRect] by filling in the rectangle of the currently selected row
 *
 *  * 3) overrides [onFocusChanged] and sets selection appropriately according to the previously
 *  focused rectangle.
 */
@Suppress("MemberVisibilityCanBePrivate")
class InternalSelectionView : View {
    /**
     * [Paint] used to draw our rectangles
     */
    private val mPainter = Paint()

    /**
     * [Paint] used to draw the "row number" text at the top of a rectangle
     */
    private val mTextPaint = Paint()

    /**
     * [Rect] used to draw the rectangles.
     */
    private val mTempRect = Rect()

    /**
     * Number of rows of rectangles we are to draw, defaults to 5 but is settable by several of our
     * constructors.
     */
    var mNumRows: Int = 5
        private set

    /**
     * Which row is selected (it will be drawn in RED, unselected rows will be BLACK).
     */
    var mSelectedRow: Int = 0
        private set

    /**
     * Flag to indicate whether our selection is moving down when moved by clicking
     */
    private var mDown = true

    /**
     * Guess of how big our rectangles should be not including padding. Used in our method
     * [measureHeight] to get a ballpark figure to give to [setMeasuredDimension] if
     * [mDesiredHeight] is null (which it always is).
     */
    private val mEstimatedPixelHeight = 10

    /**
     * Can be set using our method [setDesiredHeight] to use an exact height to pass to
     * [setMeasuredDimension] (but [setDesiredHeight] is unused so it is always null).
     */
    private var mDesiredHeight: Int? = null

    /**
     * Label that is set to a string by one of our constructors (the one used only by the activity
     * [InternalSelectionFocus], but the field is never accessed or used for anything).
     */
    var mLabel: String? = null
        private set

    /**
     * The constructor used by the activity [InternalSelectionFocus] (and internally by us).
     * First we call through to our super's constructor, then we save our [Int] parameter
     * [numRows] in our field [mNumRows], and our [String] parameter [label] in our field
     * [mLabel]. Finally we call our method [init] to perform initialization for
     * our instance.
     *
     * @param context [Context] to use to access resources
     * @param numRows number of rows of rectangles to draw.
     * @param label   [String] to save in our field [mLabel] (and never actually use)
     */
    @JvmOverloads
    constructor(context: Context?, numRows: Int, label: String? = "") : super(context) {
        this.mNumRows = numRows
        this.mLabel = label
        init()
    }

    /**
     * Constructor that is called when inflating a view from XML. This is called when a view is being
     * constructed from an XML file, supplying attributes that were specified in the XML file. First
     * we call our super's constructor, then we call our method [init] to perform initialization
     * for our instance. UNUSED.
     *
     * @param context The [Context] the view is running in, through which it can
     * access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    /**
     * Initialization method used by our constructors. First we enable our [View] to receive
     * focus, and to receive focus in touch mode, then we set the antialias flag of our [Paint]
     * field [mTextPaint] to true, its text size to 10 times the logical density of the display,
     * and its color to WHITE.
     */
    private fun init() {
        isFocusable = true
        isFocusableInTouchMode = true
        mTextPaint.isAntiAlias = true
        mTextPaint.textSize = 10 * resources.displayMetrics.density
        mTextPaint.color = Color.WHITE
    }

    /**
     * Setter for our field [mDesiredHeight] UNUSED.
     */
    @Suppress("unused")
    fun setDesiredHeight(desiredHeight: Int) {
        mDesiredHeight = desiredHeight
    }

    /**
     * Measure the view and its content to determine the measured width and the measured height.
     * We simply call [setMeasuredDimension] using the width returned by our method
     * [measureWidth], and the height returned by our method [measureHeight].
     *
     * @param widthMeasureSpec  horizontal space requirements as imposed by the parent encoded in a
     * [View.MeasureSpec]
     * @param heightMeasureSpec vertical space requirements as imposed by the parent encoded in a
     * [View.MeasureSpec]
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            /* measuredWidth = */ measureWidth(widthMeasureSpec),
            /* measuredHeight = */ measureHeight(heightMeasureSpec)
        )
    }

    /**
     * Returns a desired width for our [View] subject to the constraints imposed by our [Int]
     * parameter [measureSpec]. First we extract the mode from [measureSpec] to initialize
     * [Int] `val specMode`, and then the size to initialize [Int] `val specSize`. We calculate
     * an estimate of the width we would like: [Int] `val desiredWidth` which would be 300 pixels
     * plus our left and right padding. If `specMode` is EXACTLY we return `specSize`, and if
     * `specMode` is AT_MOST we return the lesser of `desiredWidth` or `specSize`. Otherwise we
     * return `desiredWidth`.
     *
     * @param measureSpec horizontal space requirements as imposed by the parent encoded in a
     * [View.MeasureSpec]
     * @return Width in pixels we would like to have.
     */
    private fun measureWidth(measureSpec: Int): Int {
        val specMode: Int = MeasureSpec.getMode(measureSpec)
        val specSize: Int = MeasureSpec.getSize(measureSpec)
        val desiredWidth = 300 + paddingLeft + paddingRight
        return when (specMode) {
            MeasureSpec.EXACTLY -> {
                // We were told how big to be
                specSize
            }

            MeasureSpec.AT_MOST -> {
                desiredWidth.coerceAtMost(maximumValue = specSize)
            }

            else -> {
                desiredWidth
            }
        }
    }

    /**
     * Returns a desired height for our [View] subject to the constraints imposed by our [Int]
     * parameter [measureSpec]. First we extract the mode from [measureSpec] to initialize [Int]
     * `val specMode`, and then the size to initialize [Int] `val specSize`. If our [Int] field
     * [mDesiredHeight] is not null we initialize our [Int] variable `val desiredHeight` to it,
     * otherwise we set it to an estimated height using [mEstimatedPixelHeight] for each of the
     * [mNumRows] rows, also adding the top and bottom padding of our view to this. If `specMode`
     * is EXACTLY we return `specSize`, and if `specMode` is AT_MOST we return the lesser of
     * `desiredWidth` or `specSize`. Otherwise we return `desiredWidth`.
     *
     * @param measureSpec vertical space requirements as imposed by the parent encoded in a
     * [View.MeasureSpec]
     * @return Width in pixels we would like to have.
     */
    private fun measureHeight(measureSpec: Int): Int {
        val specMode: Int = MeasureSpec.getMode(measureSpec)
        val specSize: Int = MeasureSpec.getSize(measureSpec)
        val desiredHeight: Int = (
            if (mDesiredHeight != null)
                mDesiredHeight!!
            else mNumRows * mEstimatedPixelHeight + paddingTop + paddingBottom
            )
        return when (specMode) {
            MeasureSpec.EXACTLY -> {
                // We were told how big to be
                specSize
            }

            MeasureSpec.AT_MOST -> {
                desiredHeight.coerceAtMost(maximumValue = specSize)
            }

            else -> {
                desiredHeight
            }
        }
    }

    /**
     * We implement this to do our drawing. We initialize our [Int] variable `val rowHeight` with
     * the value returned by our method `getRowHeight` (kotlin prefers to call this the property
     * [rowHeight]) which is just the height of our view, minus the top and bottom padding, all
     * divided by the number of rows: [mNumRows]). We initialize our [Int] variables `var rectTop`
     * to the top padding, `var rectLeft` to the left padding, and `var rectRight` to the width of
     * our view minus the right padding.
     *
     * Now we loop over `i` for [mNumRows] rows, first setting the color of [Paint] field [mPainter]
     * to black and its alpha to 0x20. We set the coordinates of our [Rect] field [mTempRect] to
     * `(rectLeft,rectTop)` for the top left corner and `(rectRight,rectTop+rowHeight)` for the
     * bottom right corner, then instruct our [Canvas] parameter [canvas] to draw the rectangle
     * [mTempRect] using [mPainter] as the paint.
     *
     * If our index `i` is equal to [mSelectedRow] (our current row is the selected one) and our
     * view has the focus we set the color of [mPainter] to RED, and its alpha to 0xF0, and set
     * the alpha of [mTextPaint] to 0xFF. Otherwise we set the color of [mPainter] to BLACK, and
     * its alpha to 0x40, and set the alpha of [mTextPaint] to 0xF0.
     *
     * We now set the coordinates of our [Rect] field [mTempRect] to `(rectLeft+2,rectTop+2)`
     * for the top left corner and `(rectRight-2,rectTop+rowHeight-2)` for the bottom right
     * corner, then instruct [Canvas] parameter [canvas] to draw the rectangle [mTempRect] again
     * using [mPainter] as the paint.
     *
     * We now instruct [canvas] to draw the string value of `i` using [mTextPaint] as the paint,
     * with `rectLeft+2` as the X coordinate and `rectTop+2` minus the ascent of [mTextPaint] for
     * the Y coordinate.
     *
     * Finally we add the row height `rowHeight` to `rectTop` and loop back for the next
     * row.
     *
     * @param canvas the [Canvas] on which the background will be drawn
     */
    override fun onDraw(canvas: Canvas) {
        val rowHeight: Int = rowHeight
        var rectTop: Int = paddingTop
        val rectLeft: Int = paddingLeft
        val rectRight: Int = width - paddingRight
        for (i in 0 until mNumRows) {
            mPainter.color = Color.BLACK
            mPainter.alpha = 0x20

            // draw background rect
            mTempRect[rectLeft, rectTop, rectRight] = rectTop + rowHeight
            canvas.drawRect(/* r = */ mTempRect, /* paint = */ mPainter)

            // draw foreground rect
            if (i == mSelectedRow && hasFocus()) {
                mPainter.color = Color.RED
                mPainter.alpha = 0xF0
                mTextPaint.alpha = 0xFF
            } else {
                mPainter.color = Color.BLACK
                mPainter.alpha = 0x40
                mTextPaint.alpha = 0xF0
            }
            mTempRect[rectLeft + 2, rectTop + 2, rectRight - 2] = rectTop + rowHeight - 2
            canvas.drawRect(/* r = */ mTempRect, /* paint = */ mPainter)

            // draw text to help when visually inspecting
            canvas.drawText(
                /* text = */ i.toString(),
                /* x = */ rectLeft + 2f,
                /* y = */ rectTop + 2f - mTextPaint.ascent(),
                /* paint = */ mTextPaint
            )
            rectTop += rowHeight
        }
    }

    /**
     * Calculates the height of an individual row. To do this we subtract the top and bottom padding
     * from the height of our view, and divide the result by the number of rows of rectangles in our
     * view given by our field [mNumRows]. We return the resulting value to the caller.
     *
     * @return the height of an individual row.
     */
    private val rowHeight: Int
        get() = (height - paddingTop - paddingBottom) / mNumRows

    /**
     * Calculates the coordinates of the rectangle in row [row] and sets the coordinates of [Rect]
     * parameter [rect] to them. We initialize our [Int] variable `val rowHeight` to the row height
     * value calculated by our method `getRowHeight` (kotlin prefers to call this our property
     * [rowHeight], and our [Int] variable `val top` to the value of `row*rowHeight` plus our view's
     * top padding. Then we set the coordinates of [Rect] parameter [rect] using the value of the
     * left padding for the X coordinate of the top left corner and `top` for the Y, the width of
     * our view minus the right padding of our view for the Y coordinate of the bottom right corner
     * and `top` plus `rowHeight` for the* Y coordinate.
     *
     * @param rect [Rect] whose coordinates we are to set
     * @param row  row number whose `Rect` we are to "get" to set `Rect rect`
     */
    fun getRectForRow(rect: Rect, row: Int) {
        val rowHeight: Int = rowHeight
        val top: Int = paddingTop + row * rowHeight
        rect[paddingLeft, top, width - paddingRight] = top + rowHeight
    }

    /**
     * Requests that the rectangle for the selected row of this view (given by field [mSelectedRow])
     * be visible on the screen, scrolling if necessary just enough. First we call our method
     * [getRectForRow] to load the coordinates for row [mSelectedRow] into [mTempRect], then we
     * call the system method [requestRectangleOnScreen] with [mTempRect] as the rectangle that
     * we want to scroll onto the screen if necessary.
     */
    fun ensureRectVisible() {
        getRectForRow(rect = mTempRect, row = mSelectedRow)
        requestRectangleOnScreen(/* rectangle = */ mTempRect)
    }

    /**
     * Perform press of the view when `KEYCODE_DPAD_CENTER` or `KEYCODE_ENTER` is released, if the
     * view is enabled and clickable. We switch based on the value of the keycode in our [KeyEvent]
     * parameter [event]:
     *
     *  * KEYCODE_DPAD_UP - if [mSelectedRow] is greater than 0 we decrement it, invalidate
     *  our view, call our method [ensureRectVisible] to scroll to the new selected row in
     *  [mSelectedRow], and return true to the caller.
     *
     *  * KEYCODE_DPAD_DOWN - if [mSelectedRow] is less than the last row (as given by
     *  [mNumRows] minus 1, we increment [mSelectedRow], invalidate our view, call our
     *  method [ensureRectVisible] to scroll to the new selected row in [mSelectedRow],
     *  and return true to the caller.
     *
     * If the keycode is not one we know about, or [mSelectedRow] had already reached one of
     * the ends of our view, we return false to the caller.
     *
     * @param keyCode A key code that represents the button pressed
     * @param event   The [KeyEvent] object that defines the button action.
     * @return If you handled the event, return true. If you want to allow the
     * event to be handled by the next receiver, return false.
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (event.keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> if (mSelectedRow > 0) {
                mSelectedRow--
                invalidate()
                ensureRectVisible()
                return true
            }

            KeyEvent.KEYCODE_DPAD_DOWN -> if (mSelectedRow < mNumRows - 1) {
                mSelectedRow++
                invalidate()
                ensureRectVisible()
                return true
            }
        }
        return false
    }

    /**
     * Implement this method to handle touch screen motion events. First we call through to our super's
     * implementation of [onTouchEvent], then if the action of the [MotionEvent] parameter [event]
     * is not ACTION_DOWN we return false to the caller without doing anything. If it is ACTION_DOWN
     * we request focus for our view, then if our [Boolean] field [mDown] is true we increment our
     * field [mSelectedRow] and if the result is greater than or equal to [mNumRows] we subtract
     * 2 from it again and set [mDown] to false. In either case we invalidate our view, call our
     * method [ensureRectVisible] to scroll the new rectangle onto the screen if necessary, and
     * return true to our caller.
     *
     * If [mDown] was false we make sure [mSelectedRow] is greater than 0 before decrementing it,
     * if it was already 0 we set [mSelectedRow] to 1 and [mDown] to true. In either case we now
     * invalidate our view and call our method [ensureRectVisible] to scroll the new rectangle
     * onto the screen if necessary, and return true to our caller.
     *
     * @param event The motion event.
     * @return `true` if the event was handled, `false` otherwise.
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        if (event.action != MotionEvent.ACTION_DOWN) {
            return false
        }
        requestFocus()
        if (mDown) {
            mSelectedRow++
            if (mSelectedRow >= mNumRows) {
                mSelectedRow -= 2
                mDown = false
            }
            invalidate()
            ensureRectVisible()
            return true
        }
        if (mSelectedRow > 0) {
            mSelectedRow--
        } else {
            mSelectedRow = 1
            mDown = true
        }
        invalidate()
        ensureRectVisible()
        return true
    }

    /**
     * Loads the coordinates of our [Rect] parameter [r] with those used by the [Rect] in row
     * [mSelectedRow]. To do this we just call our method [getRectForRow].
     *
     * @param r [Rect] whose coordinates we are to set to those used by the [Rect] in row
     * [mSelectedRow].
     */
    override fun getFocusedRect(r: Rect) {
        getRectForRow(rect = r, row = mSelectedRow)
    }

    /**
     * Called by the view system when the focus state of this view changes. First we call through to
     * our super's implementation of `onFocusChanged`. Then if [focused] is true (indicating we have
     * the focus), we switch based on the value of our parameter [direction]:
     *
     *  * FOCUS_DOWN - we set [mSelectedRow] to 0 and break.
     *
     *  * FOCUS_UP - we set [mSelectedRow] to the last row ([mNumRows] minus 1) and break.
     *
     *  * FOCUS_LEFT - we fall through to execute the same code as FOCUS_RIGHT
     *
     *  * FOCUS_RIGHT - if our [Rect] parameter [previouslyFocusedRect] is not null, we use it
     *  to calculate which of our rows is closest to that [Rect]. We do that by adding half of
     *  the height of [previouslyFocusedRect] to its top Y coordinate to set [Int] `val y`.
     *  We calculate the value [Int] `val yPerRow` by dividing our height by the number of rows in
     *  our view ([mNumRows]). Then we set our selected row [mSelectedRow] to `y` divided by
     *  `yPerRow`. If [previouslyFocusedRect] was null, we set our selected row [mSelectedRow] to
     *  0, and then break whether it was null or not.
     *
     *  * default - We just return because we can't gleam any useful information about what internal
     *  selection should be...
     *
     * After setting [mSelectedRow] in our switch code above, we invalidate our view before returning.
     *
     * @param focused   True if the View has focus; false otherwise.
     * @param direction The direction focus has moved when requestFocus() is called to
     *                  give this view focus. Values are `FOCUS_UP`, `FOCUS_DOWN`,
     *                  `FOCUS_LEFT`, `FOCUS_RIGHT`, `FOCUS_FORWARD`, or
     *                  `FOCUS_BACKWARD`.
     * @param previouslyFocusedRect The rectangle, in this view's coordinate system, of the previously
     *                              focused view. If applicable, this will be passed in as finer grained
     *                              information about where the focus is coming from (in addition to
     *                              direction). Will be null otherwise.
     */
    @SuppressLint("SwitchIntDef")
    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(
            /* gainFocus = */ focused,
            /* direction = */ direction,
            /* previouslyFocusedRect = */ previouslyFocusedRect
        )
        if (focused) {
            when (direction) {
                FOCUS_DOWN -> mSelectedRow = 0
                FOCUS_UP -> mSelectedRow = mNumRows - 1
                FOCUS_LEFT, FOCUS_RIGHT ->                     // set the row that is closest to the rect
                    mSelectedRow = if (previouslyFocusedRect != null) {
                        val y = (previouslyFocusedRect.top
                            + previouslyFocusedRect.height() / 2)
                        val yPerRow = height / mNumRows
                        y / yPerRow
                    } else {
                        0
                    }

                else ->
                    /**
                     * can't gleam any useful information about what internal selection should be...
                     */
                    return
            }
            invalidate()
        }
    }

    /**
     * If our [String] field [mLabel] is not null, we return it to the caller, otherwise we return
     * the value returned by our super's implementation of `toString`.
     *
     * @return Returns a string appropriate for our instance.
     */
    override fun toString(): String {
        return if (mLabel != null) {
            mLabel!!
        } else super.toString()
    }
}
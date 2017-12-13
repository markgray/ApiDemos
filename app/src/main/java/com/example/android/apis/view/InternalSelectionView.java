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

package com.example.android.apis.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

/**
 * A view that has a known number of selectable rows, and maintains a notion of which
 * row is selected. The rows take up the entire width of the view.  The height of the
 * view is divided evenly among the rows.
 * <p>
 * Notice what this view does to be a good citizen w.r.t its internal selection:
 * <ul>
 * <li>
 * 1) calls {@code requestRectangleOnScreen} each time the selection changes due to internal navigation.
 * </li>
 * <li>
 * 2) overrides {@code getFocusedRect} by filling in the rectangle of the currently selected row
 * </li>
 * <li>
 * 3) overrides {@code onFocusChanged} and sets selection appropriately according to the previously
 * focused rectangle.
 * </li>
 * </ul>
 */
public class InternalSelectionView extends View {
    /**
     * {@code Paint} used to draw our rectangles
     */
    private Paint mPainter = new Paint();
    /**
     * {@code Paint} used to draw the "row number" text at the top of a rectangle
     */
    private Paint mTextPaint = new Paint();
    /**
     * {@code Rect} used to draw the rectangles.
     */
    private Rect mTempRect = new Rect();

    /**
     * Number of rows of rectangles we are to draw, defaults to 5 but is settable by several of our
     * constructors.
     */
    private int mNumRows = 5;
    /**
     * Which row is selected (it will be drawn in RED, unselected rows will be BLACK).
     */
    private int mSelectedRow = 0;
    /**
     * Flag to indicate whether our selection is moving down when moved by clicking
     */
    private boolean mDown = true;
    /**
     * Guess of how big our rectangles should be not including padding. Used in our method
     * {@code measureHeight} to get a ballpark figure to give to {@code setMeasuredDimension} if
     * {@code mDesiredHeight} is null (which it always is).
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final int mEstimatedPixelHeight = 10;

    /**
     * Can be set using our method {@code setDesiredHeight} to use an exact height to pass to
     * {@code setMeasuredDimension} (but {@code setDesiredHeight} is unused so it is always null).
     */
    private Integer mDesiredHeight = null;
    /**
     * Label that is set to a string by one of our constructors (the one used only by the activity
     * {@code InternalSelectionFocus}, but the field is never accessed or used for anything).
     */
    private String mLabel = null;

    /**
     * The constructor used by the activity {@code InternalSelectionScroll}, we simply call our
     * {@code InternalSelectionView(Context context, int numRows, String label)} constructor with
     * the empty string ("") as the label.
     *
     * @param context {@code Context} to use to access resources, "this" in the {@code onCreate}
     *                method of the activity {@code InternalSelectionScroll}.
     * @param numRows number of rows of rectangles to draw.
     */
    public InternalSelectionView(Context context, int numRows) {
        this(context, numRows, "");
    }

    /**
     * The constructor used by the activity {@code InternalSelectionFocus} (and internally by us).
     * First we call through to our super's constructor, then we save our parameter {@code int numRows}
     * in our field {@code int mNumRows}, and our parameter {@code String label} in our field
     * {@code String mLabel}. Finally we call our method {@code init} to perform initialization for
     * our instance.
     *
     * @param context {@code Context} to use to access resources
     * @param numRows number of rows of rectangles to draw.
     * @param label   String to save in our field {@code String mLabel} (and never actually use)
     */
    public InternalSelectionView(Context context, int numRows, String label) {
        super(context);
        mNumRows = numRows;
        mLabel = label;
        init();
    }

    /**
     * Constructor that is called when inflating a view from XML. This is called when a view is being
     * constructed from an XML file, supplying attributes that were specified in the XML file. First
     * we call our super's constructor, then we call our method {@code init} to perform initialization
     * for our instance. UNUSED.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public InternalSelectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Initialization method used by our constructors. First we enable our {@code View} to receive
     * focus, and to receive focus in touch mode, then we set the antialias flag of our field
     * {@code Paint mTextPaint} to true, its text size to 10 times the logical density of the display,
     * and its color to WHITE.
     */
    private void init() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(10 * getResources().getDisplayMetrics().density);
        mTextPaint.setColor(Color.WHITE);
    }

    /**
     * Getter for our field {@code int mNumRows} UNUSED.
     *
     * @return The current value of our field {@code int mNumRows}
     */
    @SuppressWarnings("unused")
    public int getNumRows() {
        return mNumRows;
    }

    /**
     * Getter for our field {@code int mSelectedRow} UNUSED.
     *
     * @return The current value of our field {@code int mSelectedRow}
     */
    @SuppressWarnings("unused")
    public int getSelectedRow() {
        return mSelectedRow;
    }

    /**
     * Setter for our field {@code int desiredHeight} UNUSED.
     */
    @SuppressWarnings("unused")
    public void setDesiredHeight(int desiredHeight) {
        mDesiredHeight = desiredHeight;
    }

    /**
     * Getter for our field {@code String mLabel} UNUSED.
     *
     * @return The current value of our field {@code String mLabel}
     */
    @SuppressWarnings("unused")
    public String getLabel() {
        return mLabel;
    }

    /**
     * Measure the view and its content to determine the measured width and the measured height.
     * We simply call {@code setMeasuredDimension} using the width returned by our method
     * {@code measureWidth}, and the height returned by our method {@code measureHeight}.
     *
     * @param widthMeasureSpec  horizontal space requirements as imposed by the parent encoded in a
     *                          {@code View.MeasureSpec}
     * @param heightMeasureSpec vertical space requirements as imposed by the parent encoded in a
     *                          {@code View.MeasureSpec}
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
                measureWidth(widthMeasureSpec),
                measureHeight(heightMeasureSpec));
    }

    /**
     * Returns a desired width for our {@code View} subject to the constraints imposed by our parameter
     * {@code int measureSpec}. First we extract the mode from {@code int measureSpec} to initialize
     * {@code int specMode}, and then the size to initialize {@code int specSize}. We calculate an
     * estimate of the width we would like: {@code int desiredWidth} which would be 300 pixels plus
     * our left and right padding. If {@code specMode} is EXACTLY we return {@code specSize}, and if
     * {@code specMode} is AT_MOST we return the lesser of {@code desiredWidth} or {@code specSize}.
     * Otherwise we return {@code desiredWidth}.
     *
     * @param measureSpec horizontal space requirements as imposed by the parent encoded in a
     *                    {@code View.MeasureSpec}
     * @return Width in pixels we would like to have.
     */
    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        int desiredWidth = 300 + getPaddingLeft() + getPaddingRight();
        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            return specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            return desiredWidth < specSize ? desiredWidth : specSize;
        } else {
            return desiredWidth;
        }
    }

    /**
     * Returns a desired height for our {@code View} subject to the constraints imposed by our parameter
     * {@code int measureSpec}. First we extract the mode from {@code int measureSpec} to initialize
     * {@code int specMode}, and then the size to initialize {@code int specSize}. If our field
     * {@code Integer mDesiredHeight} is not null we initialize our variable {@code int desiredHeight}
     * to it, otherwise we set it to an estimated height using {@code mEstimatedPixelHeight} for each
     * of the {@code mNumRows} rows, also adding the top and bottom padding of our view to this. If
     * {@code specMode} is EXACTLY we return {@code specSize}, and if {@code specMode} is AT_MOST we
     * return the lesser of {@code desiredWidth} or {@code specSize}. Otherwise we return
     * {@code desiredWidth}.
     *
     * @param measureSpec vertical space requirements as imposed by the parent encoded in a
     *                    {@code View.MeasureSpec}
     * @return Width in pixels we would like to have.
     */
    private int measureHeight(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        int desiredHeight = mDesiredHeight != null ?
                mDesiredHeight :
                mNumRows * mEstimatedPixelHeight + getPaddingTop() + getPaddingBottom();
        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            return specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            return desiredHeight < specSize ? desiredHeight : specSize;
        } else {
            return desiredHeight;
        }
    }

    /**
     * We implement this to do our drawing. We initialize our variable {@code int rowHeight} with the
     * value returned by our method {@code getRowHeight} (which is just the height of our view, minus
     * the top and bottom padding, all divided by the number of rows: {@code mNumRows}). We initialize
     * our variable {@code int rectTop} to the top padding, {@code int rectLeft} to the left padding,
     * and {@code int rectRight} to the width of our view minus the right padding.
     * <p>
     * Now we loop over {@code int i} for {@code mNumRows} rows, first setting the color of
     * {@code Paint mPainter} to black and its alpha to 0x20. We set the coordinates of our field
     * {@code Rect mTempRect} to {@code (rectLeft,rectTop)} for the top left corner and
     * {@code (rectRight,rectTop+rowHeight)} for the bottom right corner, then instruct {@code canvas}
     * to draw the rectangle {@code mTempRect} using {@code mPainter} as the paint.
     * <p>
     * If our index {@code i} is equal to {@code mSelectedRow} (our current row is the selected one)
     * and our view has the focus we set the color of {@code mPainter} to RED, and its alpha to 0xF0,
     * and set the alpha of {@code mTextPaint} to 0xFF. Otherwise we set the color of {@code mPainter}
     * to BLACK, and its alpha to 0x40, and set the alpha of {@code mTextPaint} to 0xF0.
     * <p>
     * We now set the coordinates of our field {@code Rect mTempRect} to {@code (rectLeft+2,rectTop+2)}
     * for the top left corner and {@code (rectRight-2,rectTop+rowHeight-2)} for the bottom right corner,
     * then instruct {@code canvas} to draw the rectangle {@code mTempRect} again using {@code mPainter}
     * as the paint.
     * <p>
     * We now instruct {@code canvas} to draw the string value of {@code i} using {@code mTextPaint}
     * as the paint, with {@code rectLeft+2} as the X coordinate and {@code rectTop+2} minus the
     * ascent of {@code mTextPaint} for the Y coordinate.
     * <p>
     * Finally we add the row height {@code rowHeight} to {@code rectTop} and loop back for the next
     * row.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {

        int rowHeight = getRowHeight();

        int rectTop = getPaddingTop();
        int rectLeft = getPaddingLeft();
        int rectRight = getWidth() - getPaddingRight();
        for (int i = 0; i < mNumRows; i++) {

            mPainter.setColor(Color.BLACK);
            mPainter.setAlpha(0x20);

            // draw background rect
            mTempRect.set(rectLeft, rectTop, rectRight, rectTop + rowHeight);
            canvas.drawRect(mTempRect, mPainter);

            // draw foreground rect
            if (i == mSelectedRow && hasFocus()) {
                mPainter.setColor(Color.RED);
                mPainter.setAlpha(0xF0);
                mTextPaint.setAlpha(0xFF);
            } else {
                mPainter.setColor(Color.BLACK);
                mPainter.setAlpha(0x40);
                mTextPaint.setAlpha(0xF0);
            }
            mTempRect.set(rectLeft + 2, rectTop + 2,
                    rectRight - 2, rectTop + rowHeight - 2);
            canvas.drawRect(mTempRect, mPainter);

            // draw text to help when visually inspecting
            canvas.drawText(
                    Integer.toString(i),
                    rectLeft + 2,
                    rectTop + 2 - (int) mTextPaint.ascent(),
                    mTextPaint);

            rectTop += rowHeight;
        }
    }

    /**
     * Calculates the height of an individual row. To do this we subtract the top and bottom padding
     * from the height of our view, and divide the result by the number of rows of rectangles in our
     * view {@code mNumRows}. We return the resulting value to the caller.
     *
     * @return the height of an individual row.
     */
    private int getRowHeight() {
        return (getHeight() - getPaddingTop() - getPaddingBottom()) / mNumRows;
    }

    /**
     * Calculates the coordinates of the rectangle in row {@code int row} and sets the coordinates
     * of {@code Rect rect} to them. We initialize our variable {@code int rowHeight} to the row
     * height value calculated by our method {@code getRowHeight}, and our variable {@code int top}
     * to the value of {@code row*rowHeight} plus our view's top padding. Then we set the coordinates
     * of {@code Rect rect} using the value of the left padding for the X coordinate of the top left
     * corner and {@code top} for the Y, the width of our view minus the right padding of our view
     * for the Y coordinate of the bottom right corner and {@code top} plus {@code rowHeight} for the
     * Y coordinate.
     *
     * @param rect {@code Rect} whose coordinates we are to set
     * @param row  row number whose {@code Rect} we are to "get" to set {@code Rect rect}
     */
    public void getRectForRow(Rect rect, int row) {
        final int rowHeight = getRowHeight();
        final int top = getPaddingTop() + row * rowHeight;
        rect.set(getPaddingLeft(),
                top,
                getWidth() - getPaddingRight(),
                top + rowHeight);
    }

    /**
     * Requests that the rectangle for the selected row {@code mSelectedRow} of this view be visible
     * on the screen, scrolling if necessary just enough. First we call our method {@code getRectForRow}
     * to load the coordinates for row {@code mSelectedRow} into {@code mTempRect}, then we call the
     * system method {@code requestRectangleOnScreen} with {@code mTempRect} as the rectangle that
     * we want to scroll onto the screen if necessary.
     */
    void ensureRectVisible() {
        getRectForRow(mTempRect, mSelectedRow);
        requestRectangleOnScreen(mTempRect);
    }

    /**
     * Perform press of the view when {@code KEYCODE_DPAD_CENTER} or {@code KEYCODE_ENTER} is
     * released, if the view is enabled and clickable. We switch based on the value of the keycode
     * in our parameter {@code KeyEvent event}:
     * <ul>
     * <li>
     * KEYCODE_DPAD_UP - if {@code mSelectedRow} is greater than 0 we decrement it, invalidate
     * our view, call our method {@code ensureRectVisible} to scroll to the new selected row
     * {@code mSelectedRow}, and return true to the caller.
     * </li>
     * <li>
     * KEYCODE_DPAD_DOWN - if {@code mSelectedRow} is less than the last row (as given by
     * {@code mNumRows-1}, we increment {@code mSelectedRow}, invalidate our view, call our
     * method {@code ensureRectVisible} to scroll to the new selected row {@code mSelectedRow},
     * and return true to the caller.
     * </li>
     * </ul>
     * If the keycode is not one we know about, or {@code mSelectedRow} had already reached one of
     * the ends of our view, we return false to the caller.
     *
     * @param keyCode A key code that represents the button pressed
     * @param event   The KeyEvent object that defines the button action.
     * @return If you handled the event, return true. If you want to allow the
     * event to be handled by the next receiver, return false.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_UP:
                if (mSelectedRow > 0) {
                    mSelectedRow--;
                    invalidate();
                    ensureRectVisible();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (mSelectedRow < (mNumRows - 1)) {
                    mSelectedRow++;
                    invalidate();
                    ensureRectVisible();
                    return true;
                }
                break;
        }
        return false;
    }

    /**
     * Implement this method to handle touch screen motion events. First we call through to our super's
     * implementation of {@code onTouchEvent}, then if the action of the parameter {@code MotionEvent event}
     * is not ACTION_DOWN we return false to the caller without doing anything. If it is ACTION_DOWN
     * we request focus for our view, then if our field {@code mDown} is true we increment our field
     * {@code mSelectedRow} and if the result is greater than or equal to {@code mNumRows} we subtract
     * 2 from it again and set {@code mDown} to false. In either case we invalidate our view, call our
     * method {@code ensureRectVisible} to scroll the new rectangle onto the screen if necessary, and
     * return true to our caller.
     * <p>
     * If {@code mDown} was false we make sure {@code mSelectedRow} is greater than 0 before decrementing
     * it, if it was already 0 we set {@code mSelectedRow} to 1 and {@code mDown} to true. In either case
     * we now invalidate our view and call our method {@code ensureRectVisible} to scroll the new rectangle
     * onto the screen if necessary, and return true to our caller.
     *
     * @param event The motion event.
     * @return True if the event was handled, false otherwise.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (event.getAction() != MotionEvent.ACTION_DOWN) {
            return false;
        }
        requestFocus();
        if (mDown) {
            mSelectedRow++;
            if (mSelectedRow >= mNumRows) {
                mSelectedRow -= 2;
                mDown = false;
            }
            invalidate();
            ensureRectVisible();
            return true;
        }
        if (mSelectedRow > 0) {
            mSelectedRow--;
        } else {
            mSelectedRow = 1;
            mDown = true;
        }
        invalidate();
        ensureRectVisible();
        return true;
    }

    /**
     * Loads the coordinates of our parameter {@code Rect r} with those used by the {@code Rect} in row
     * {@code mSelectedRow}. To do this we just call our method {@code getRectForRow}.
     *
     * @param r {@code Rect} whose coordinates we are to set to those used by the {@code Rect} in row
     *          {@code mSelectedRow}.
     */
    @Override
    public void getFocusedRect(Rect r) {
        getRectForRow(r, mSelectedRow);
    }

    /**
     * Called by the view system when the focus state of this view changes. First we call through to
     * our super's implementation of {@code onFocusChanged}. Then if {@code focused} is true (indicating
     * we have the focus), we switch based on the value of our parameter {@code direction}:
     * <ul>
     * <li>
     * FOCUS_DOWN - we set {@code mSelectedRow} to 0 and break.
     * </li>
     * <li>
     * FOCUS_UP - we set {@code mSelectedRow} to the last row ({@code mNumRows-1}) and break.
     * </li>
     * <li>
     * FOCUS_LEFT - we fall through to execute the same code as FOCUS_RIGHT
     * </li>
     * <li>
     * FOCUS_RIGHT - if our parameter {@code Rect previouslyFocusedRect} is not null, we use it
     * to calculate which of our rows is closest to that {@code Rect}. We do that by adding half
     * of the height of {@code previouslyFocusedRect} to its top Y coordinate to set {@code int y}.
     * We calculate the value {@code yPerRow} by dividing our height by the number of rows in
     * our view {@code mNumRows}. Then we set our selected row {@code mSelectedRow} to {@code y}
     * divided by {@code yPerRow}. If {@code previouslyFocusedRect} was null, we set our selected
     * row {@code mSelectedRow} to 0, and then break whether it was null or not.
     * </li>
     * <li>
     * default - We just return because we can't gleam any useful information about what internal
     * selection should be...
     * </li>
     * </ul>
     * After setting {@code mSelectedRow} in our switch code above, we invalidate our view before returning.
     *
     * @param focused               True if the View has focus; false otherwise.
     * @param direction             The direction focus has moved when requestFocus() is called to
     *                              give this view focus. Values are {@code FOCUS_UP}, {@code FOCUS_DOWN},
     *                              {@code FOCUS_LEFT}, {@code FOCUS_RIGHT}, {@code FOCUS_FORWARD}, or
     *                              {@code FOCUS_BACKWARD}.
     * @param previouslyFocusedRect The rectangle, in this view's coordinate system, of the previously
     *                              focused view. If applicable, this will be passed in as finer grained
     *                              information about where the focus is coming from (in addition to
     *                              direction). Will be null otherwise.
     */
    @SuppressLint("SwitchIntDef")
    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);

        if (focused) {
            switch (direction) {
                case View.FOCUS_DOWN:
                    mSelectedRow = 0;
                    break;
                case View.FOCUS_UP:
                    mSelectedRow = mNumRows - 1;
                    break;
                case View.FOCUS_LEFT:  // fall through
                case View.FOCUS_RIGHT:
                    // set the row that is closest to the rect
                    if (previouslyFocusedRect != null) {
                        int y = previouslyFocusedRect.top
                                + (previouslyFocusedRect.height() / 2);
                        int yPerRow = getHeight() / mNumRows;
                        mSelectedRow = y / yPerRow;
                    } else {
                        mSelectedRow = 0;
                    }
                    break;
                default:
                    // can't gleam any useful information about what internal
                    // selection should be...
                    return;
            }
            invalidate();
        }
    }

    /**
     * If our field {@code String mLabel} is not null, we return it to the caller, otherwise we return
     * the value returned by our super's implementation of {@code toString}.
     *
     * @return Returns a string appropriate for our instance.
     */
    @SuppressWarnings("unused")
    @Override
    public String toString() {
        if (mLabel != null) {
            return mLabel;
        }
        return super.toString();
    }
}

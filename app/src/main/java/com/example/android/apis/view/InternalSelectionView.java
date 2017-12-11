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
     * focus, then we set the antialias flag of {@code Paint mTextPaint} to true, its text size to
     * 10 times the logical density of the display, and its color to WHITE.
     */
    private void init() {
        setFocusable(true);
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
     * {@code int measureSpec}.
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

    private int getRowHeight() {
        return (getHeight() - getPaddingTop() - getPaddingBottom()) / mNumRows;
    }

    public void getRectForRow(Rect rect, int row) {
        final int rowHeight = getRowHeight();
        final int top = getPaddingTop() + row * rowHeight;
        rect.set(getPaddingLeft(),
                top,
                getWidth() - getPaddingRight(),
                top + rowHeight);
    }

    void ensureRectVisible() {
        getRectForRow(mTempRect, mSelectedRow);
        requestRectangleOnScreen(mTempRect);
    }

    /**
     * Perform press of the view when {@code KEYCODE_DPAD_CENTER} or {@code KEYCODE_ENTER} is
     * released, if the view is enabled and clickable.
     *
     * @param keyCode A key code that represents the button pressed
     * @param event   The KeyEvent object that defines the button action.
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
     * Implement this method to handle touch screen motion events.
     * <p/>
     * If this method is used to detect click actions, it is recommended that
     * the actions be performed by implementing and calling
     * {@link #performClick()}. This will ensure consistent system behavior,
     * including:
     * <ul>
     * <li>obeying click sound preferences
     * <li>dispatching OnClickListener calls
     * <li>handling AccessibilityNodeInfo#ACTION_CLICK ACTION_CLICK when
     * accessibility features are enabled
     * </ul>
     *
     * @param event The motion event.
     * @return True if the event was handled, false otherwise.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (mSelectedRow > 0) {
            mSelectedRow--;
            invalidate();
            ensureRectVisible();
            return true;
        }
        if (mSelectedRow < (mNumRows - 1)) {
            mSelectedRow++;
            invalidate();
            ensureRectVisible();
            return true;
        }
        return true;
    }

    @Override
    public void getFocusedRect(Rect r) {
        getRectForRow(r, mSelectedRow);
    }

    @SuppressLint("SwitchIntDef")
    @Override
    protected void onFocusChanged(boolean focused, int direction,
                                  Rect previouslyFocusedRect) {
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

    @SuppressWarnings("unused")
    @Override
    public String toString() {
        if (mLabel != null) {
            return mLabel;
        }
        return super.toString();
    }
}

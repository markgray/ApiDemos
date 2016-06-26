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
 * row is selected. The rows take up the
 * entire width of the view.  The height of the view is divided evenly among
 * the rows.
 *
 * Notice what this view does to be a good citizen w.r.t its internal selection:
 * 1) calls {@link View#requestRectangleOnScreen} each time the selection changes due to
 *    internal navigation.
 * 2) overrides {@link View#getFocusedRect} by filling in the rectangle of the currently
 *    selected row
 * 3) overrides {@link View#onFocusChanged} and sets selection appropriately according to
 *    the previously focused rectangle.
 */
public class InternalSelectionView extends View {

    private Paint mPainter = new Paint();
    private Paint mTextPaint = new Paint();
    private Rect mTempRect = new Rect();

    private int mNumRows = 5;
    private int mSelectedRow = 0;
    @SuppressWarnings("FieldCanBeLocal")
    private final int mEstimatedPixelHeight = 10;

    private Integer mDesiredHeight = null;
    private String mLabel = null;


    public InternalSelectionView(Context context, int numRows) {
        this(context, numRows, "");
    }
    
    public InternalSelectionView(Context context, int numRows, String label) {
        super(context);
        mNumRows = numRows;
        mLabel = label;
        init();
    }

    public InternalSelectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setFocusable(true);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(10);
        mTextPaint.setColor(Color.WHITE);
    }

    @SuppressWarnings("unused")
    public int getNumRows() {
        return mNumRows;
    }

    @SuppressWarnings("unused")
    public int getSelectedRow() {
        return mSelectedRow;
    }

    @SuppressWarnings("unused")
    public void setDesiredHeight(int desiredHeight) {
        mDesiredHeight = desiredHeight;
    }

    public String getLabel() {
        return mLabel;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
            measureWidth(widthMeasureSpec),
            measureHeight(heightMeasureSpec));
    }

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


    /* (non-Javadoc)
    * @see android.view.KeyEvent.Callback#onKeyDown(int, android.view.KeyEvent)
    */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(event.getKeyCode()) {
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

    @Override
    public String toString() {
        if (mLabel != null) {
            return mLabel;
        }
        return super.toString();
    }
}

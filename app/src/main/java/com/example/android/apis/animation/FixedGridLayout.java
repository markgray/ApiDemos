/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.example.android.apis.animation;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * A layout that arranges its children in a grid.  The size of the
 * cells is set by the setCellSize method and the
 * android:cell_width and android:cell_height attributes in XML.
 * The number of rows and columns is determined at runtime.  Each
 * cell contains exactly one view, and they flow in the natural
 * child order (the order in which they were added, or the index
 * in addViewAt.  Views can not span multiple cells.
 *
 * <p>This class was copied from the FixedGridLayout Api demo; see that demo for
 * more information on using the layout.</p>
 */
public class FixedGridLayout extends ViewGroup {
    int mCellWidth;
    int mCellHeight;

    /**
     * Creates a FixedGridLayout with the Context context
     *
     * @param context Context of the activity creating the ViewGroup
     */
    public FixedGridLayout(Context context) {
        super(context);
    }

    /**
     * Sets the mCellWidth field and schedules a layout pass of the view tree.
     *
     * @param px cell width in pixels
     */
    public void setCellWidth(int px) {
        mCellWidth = px;
        requestLayout();
    }

    /**
     * Sets the mCellHeight field and schedules a layout pass of the view tree.
     *
     * @param px cell height in pixels
     */
    public void setCellHeight(int px) {
        mCellHeight = px;
        requestLayout();
    }

    /**
     * Measure the view and its content to determine the measured width and the
     * measured height. This method is invoked by {@link #measure(int, int)} and
     * should be overridden by subclasses to provide accurate and efficient
     * measurement of their contents. It first creates MeasureSpec's encoding
     * mCellWidth and mCellHeight for the mode AT_MOST, it then tells each child
     * View (Button's in our case) to measure itself given these two constraints.
     * Finally it calls setMeasuredDimension telling it we will use the size our
     * parents gave us, but default to a minimum size to avoid clipping transitioning
     * children
     *
     * @param widthMeasureSpec horizontal space requirements as imposed by the parent.
     *                         The requirements are encoded with
     *                         {@link android.view.View.MeasureSpec}.
     * @param heightMeasureSpec vertical space requirements as imposed by the parent.
     *                         The requirements are encoded with
     *                         {@link android.view.View.MeasureSpec}.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int cellWidthSpec = MeasureSpec.makeMeasureSpec(mCellWidth,
                MeasureSpec.AT_MOST);
        int cellHeightSpec = MeasureSpec.makeMeasureSpec(mCellHeight,
                MeasureSpec.AT_MOST);

        int count = getChildCount();
        for (int index=0; index<count; index++) {
            final View child = getChildAt(index);
            child.measure(cellWidthSpec, cellHeightSpec);
        }
        // Use the size our parents gave us, but default to a minimum size to avoid
        // clipping transitioning children
        int minCount =  count > 3 ? count : 3;
        setMeasuredDimension(resolveSize(mCellWidth * minCount, widthMeasureSpec),
                resolveSize(mCellHeight * minCount, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int cellWidth = mCellWidth;
        int cellHeight = mCellHeight;
        int columns = (r - l) / cellWidth;
        if (columns < 0) {
            columns = 1;
        }
        int x = 0;
        int y = 0;
        int i = 0;
        int count = getChildCount();
        for (int index=0; index<count; index++) {
            final View child = getChildAt(index);

            int w = child.getMeasuredWidth();
            int h = child.getMeasuredHeight();

            int left = x + ((cellWidth-w)/2);
            int top = y + ((cellHeight-h)/2);

            child.layout(left, top, left+w, top+h);
            if (i >= (columns-1)) {
                // advance to next row
                i = 0;
                x = 0;
                y += cellHeight;
            } else {
                i++;
                x += cellWidth;
            }
        }
    }
}


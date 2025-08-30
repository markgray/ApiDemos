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

package com.example.android.apis.animation

import android.content.Context
import android.view.ViewGroup

/**
 * A layout that arranges its children in a grid.  The size of the cells is set by the setCellSize
 * method and the android:cell_width and android:cell_height attributes in XML. The number of rows
 * and columns is determined at runtime. Each cell contains exactly one view, and they flow in the
 * natural child order (the order in which they were added, or the index in addViewAt. Views can
 * not span multiple cells.
 *
 * This class was copied from the FixedGridLayout Api demo; see that demo for more information on
 * using the layout.
 *
 * Creates a FixedGridLayout with the Context context. We just call our super's constructor.
 *
 * @param context Context of the activity creating the ViewGroup
 */
@Suppress("MemberVisibilityCanBePrivate")
class FixedGridLayout(context: Context) : ViewGroup(context) {
    /**
     * Width of each of our children's views.
     */
    internal var mCellWidth: Int = 0

    /**
     * Height of each of our children's views.
     */
    internal var mCellHeight: Int = 0

    /**
     * Sets the mCellWidth field and schedules a layout pass of the view tree.
     *
     * @param px cell width in pixels
     */
    fun setCellWidth(px: Int) {
        mCellWidth = px
        requestLayout()
    }

    /**
     * Sets the mCellHeight field and schedules a layout pass of the view tree.
     *
     * @param px cell height in pixels
     */
    fun setCellHeight(px: Int) {
        mCellHeight = px
        requestLayout()
    }

    /**
     * Measure the view and its content to determine the measured width and the
     * measured height. This method is invoked by [measure] and
     * should be overridden by subclasses to provide accurate and efficient
     * measurement of their contents. It first creates MeasureSpec's encoding
     * mCellWidth and mCellHeight for the mode AT_MOST, it then tells each child
     * View (Button's in our case) to measure itself given these two constraints.
     * Finally it calls setMeasuredDimension telling it we will use the size our
     * parents gave us, but default to a minimum size to avoid clipping transitioning
     * children. It does this by calling resolveSize(int,int) for both width and
     * height to decide between our desired size and the size imposed by our parent
     * and using the returned MEASURED_SIZE_MASK for width and height in our call to
     * setMeasuredDimension.
     *
     * @param widthMeasureSpec horizontal space requirements as imposed by the parent.
     * The requirements are encoded with
     * [android.view.View.MeasureSpec].
     * @param heightMeasureSpec vertical space requirements as imposed by the parent.
     * The requirements are encoded with
     * [android.view.View.MeasureSpec].
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val cellWidthSpec = MeasureSpec.makeMeasureSpec(
            mCellWidth,
            MeasureSpec.AT_MOST
        )
        val cellHeightSpec = MeasureSpec.makeMeasureSpec(
            mCellHeight,
            MeasureSpec.AT_MOST
        )

        val count = childCount
        for (index in 0 until count) {
            val child = getChildAt(index)
            child.measure(cellWidthSpec, cellHeightSpec)
        }
        // Use the size our parents gave us, but default to a minimum size to avoid
        // clipping transitioning children
        val minCount = if (count > 3) count else 3
        setMeasuredDimension(
            /* measuredWidth = */ resolveSize(mCellWidth * minCount, widthMeasureSpec),
            /* measuredHeight = */ resolveSize(mCellHeight * minCount, heightMeasureSpec)
        )
    }

    /**
     * Called from layout when this view should assign a size and position to each of
     * its children. Given the positions l, t, r, and b (left, top, right and bottom)
     * assigned to us by our parent we calculate left, top, right and bottom for each
     * of our children and call View.layout() for each child to inform them (via their
     * own onLayout callbacks) of their new positions. The calculation of the position
     * of each child involves determining the number of columns we can create based on
     * the space between our right and left position as allowed by our parent, and the
     * cell width we are using. Then for each child we assign a column starting in column
     * 0 and row 0 and advancing the row number, and resetting the column to 0 every time
     * we fill all the columns in a row.
     *
     * @param changed This is a new size or position for this view
     * @param l       Left position, relative to parent
     * @param t       Top position, relative to parent
     * @param r       Right position, relative to parent
     * @param b       Bottom position, relative to parent
     */
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val cellWidth = mCellWidth
        val cellHeight = mCellHeight
        var columns = (r - l) / cellWidth
        if (columns < 0) {
            columns = 1
        }
        var x = 0
        var y = 0
        var i = 0
        val count = childCount
        for (index in 0 until count) {
            val child = getChildAt(index)

            val w = child.measuredWidth
            val h = child.measuredHeight

            val left = x + (cellWidth - w) / 2
            val top = y + (cellHeight - h) / 2

            child.layout(left, top, left + w, top + h)
            if (i >= columns - 1) {
                // advance to next row
                i = 0
                x = 0
                y += cellHeight
            } else {
                i++
                x += cellWidth
            }
        }
    }
}

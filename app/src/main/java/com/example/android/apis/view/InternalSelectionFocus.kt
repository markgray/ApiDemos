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

import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

/**
 * [android.view.View.requestFocus] and
 * `android.view.View.onFocusChanged(boolean, int, android.graphics.Rect)`
 * work together to give a newly focused item a hint about the most interesting
 * rectangle of the previously focused view.  The view taking focus can use this
 * to set an internal selection more appropriate using this rect.
 *
 *
 * This Activity exercises that behavior using three adjacent [InternalSelectionView]
 * that report interesting rects when giving up focus, and use interesting rects
 * when taking focus to best select the internal row to show as selected.
 *
 *
 * Were [InternalSelectionView] not to override [android.view.View.getFocusedRect], or
 * `android.view.View.onFocusChanged(boolean, int, android.graphics.Rect)`, the focus would
 * jump to some default internal selection (the top) and not allow for the smooth hand-off.
 *
 *
 * Need keys to move focus, so I do not know what this does.
 */
class InternalSelectionFocus : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. We create `LinearLayout layout`, set its orientation to HORIZONTAL,
     * and set its layout parameters for width and height both to MATCH_PARENT. We create an new
     * instance for `LinearLayout.LayoutParams params` with a width of 0, height of MATCH_PARENT,
     * and a weight of 1.
     *
     *
     * We create `InternalSelectionView leftColumn` with 5 rows and the label "left column",
     * set its layout parameters to `param`, its padding to 10 on all 4 sides and add it to
     * `layout`.
     *
     *
     * We create `InternalSelectionView middleColumn` with 5 rows and the label "middle column",
     * set its layout parameters to `param`, its padding to 10 on all 4 sides and add it to
     * `layout`.
     *
     *
     * We create `InternalSelectionView rightColumn` with 5 rows and the label "right column",
     * set its layout parameters to `param`, its padding to 10 on all 4 sides and add it to
     * `layout`.
     *
     *
     * Finally we set our content view to `layout`.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.HORIZONTAL
        layout.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        val params = LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, 1f)
        val leftColumn = InternalSelectionView(this, 5, "left column")
        leftColumn.layoutParams = params
        leftColumn.setPadding(10, 10, 10, 10)
        layout.addView(leftColumn)
        val middleColumn = InternalSelectionView(this, 5, "middle column")
        middleColumn.layoutParams = params
        middleColumn.setPadding(10, 10, 10, 10)
        layout.addView(middleColumn)
        val rightColumn = InternalSelectionView(this, 5, "right column")
        rightColumn.layoutParams = params
        rightColumn.setPadding(10, 10, 10, 10)
        layout.addView(rightColumn)
        setContentView(layout)
    }
}
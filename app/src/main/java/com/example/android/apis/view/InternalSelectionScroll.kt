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
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.graphics.Utilities.id2p

/**
 * Demonstrates how a well behaved view with internal selection [InternalSelectionView]
 * can cause its parent android.widget.ScrollView to scroll to keep the internally
 * interesting rectangle on the screen. [InternalSelectionView] achieves this by calling
 * android.view.View.requestRectangleOnScreen each time its internal selection changes.
 * android.widget.ScrollView, in turn, implements android.view.View.requestRectangleOnScreen
 * thereby achieving the result.  Note that android.widget.ListView also implements the
 * method, so views that call android.view.View.requestRectangleOnScreen that are embedded
 * within either android.widget.ScrollView's or android.widget.ListView's can expect to
 * keep their internal interesting rectangle visible. Needs keyboard and a fix to
 * background colors in InternalSelectView.java
 */
class InternalSelectionScroll : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. Next we create a new instance for our [ScrollView] variable `val sv`, and
     * a new instance for our [ViewGroup.LayoutParams] variable `val svLp` with the width set to
     * MATCH_PARENT, and the height set to WRAP_CONTENT. We create a new instance for [LinearLayout]
     * `val ll`, set its layout parameters to `svLp`, and add it to `sv`. We create a new instance
     * for [InternalSelectionView] `val isv` with 10 rows. We fetch the height of our display to
     * initialize [Int] `val screenHeight`, then create [LinearLayout.LayoutParams] `val llLp` with
     * the width set to MATCH_PARENT and the height set to 2 times `screenHeight`, then use it to
     * set the layout parameters of `isv`, and add `isv` to `ll`. Finally we set our content
     * view to `sv`.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sv = ScrollView(this)
        sv.setPadding(0, id2p(120), 0, id2p(60))
        val svLp: ViewGroup.LayoutParams = FrameLayout.LayoutParams(
            /* width = */ ViewGroup.LayoutParams.MATCH_PARENT,
            /* height = */ ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val ll = LinearLayout(this)
        ll.layoutParams = svLp
        sv.addView(ll)
        val isv = InternalSelectionView(this, 10)

        @Suppress("DEPRECATION")
        val screenHeight = windowManager.defaultDisplay.height
        val llLp = LinearLayout.LayoutParams(
            /* width = */ ViewGroup.LayoutParams.MATCH_PARENT,
            /* height = */ 2 * screenHeight
        ) // 2x screen height to ensure scrolling
        isv.layoutParams = llLp
        ll.addView(isv)
        setContentView(sv)
    }
}
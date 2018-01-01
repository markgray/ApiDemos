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

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

/**
 * Demonstrates how a well behaved view with internal selection InternalSelectionView
 * can cause its parent android.widget.ScrollView to scroll to keep the internally
 * interesting rectangle on the screen. InternalSelectionView achieves this by calling
 * android.view.View#requestRectangleOnScreen each time its internal selection changes.
 * android.widget.ScrollView, in turn, implements android.view.View#requestRectangleOnScreen
 * thereby achieving the result.  Note that android.widget.ListView also implements the
 * method, so views that call android.view.View#requestRectangleOnScreen that are embedded
 * within either android.widget.ScrollView's or android.widget.ListView's can expect to
 * keep their internal interesting rectangle visible. Needs keyboard and a fix to
 * background colors in InternalSelectView.java
 */
public class InternalSelectionScroll extends Activity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}. Next we create a new instance for our variable {@code ScrollView sv}, and
     * a new instance for {@code ViewGroup.LayoutParams svLp} with the width set to MATCH_PARENT,
     * and the height set to WRAP_CONTENT. We create a new instance for {@code LinearLayout ll}, set
     * its layout parameters to {@code svLp}, and add it to {@code sv}. We create a new instance for
     * {@code InternalSelectionView isv} with 10 rows. We fetch the height of our display to initialize
     * {@code int screenHeight}, then create {@code LinearLayout.LayoutParams llLp} with the width
     * set to MATCH_PARENT and the height set to 2 times {@code screenHeight}, then use it to set the
     * layout parameters of {@code isv}, and add {@code isv} to {@code ll}. Finally we set our content
     * view to {@code sv}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView sv = new ScrollView(this);
        ViewGroup.LayoutParams svLp = new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        LinearLayout ll = new LinearLayout(this);
        ll.setLayoutParams(svLp);
        sv.addView(ll);

        InternalSelectionView isv = new InternalSelectionView(this, 10);
        @SuppressWarnings("deprecation")
        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        LinearLayout.LayoutParams llLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                2 * screenHeight);  // 2x screen height to ensure scrolling
        isv.setLayoutParams(llLp);
        ll.addView(isv);

        setContentView(sv);
    }
}

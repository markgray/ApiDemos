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

import com.example.android.apis.R;

import android.app.Activity;
import android.os.Bundle;

/**
 * Its layout file R.layout.layout_animation_7 contains a android:layoutAnimation
 * attribute specifying @anim/layout_animation_table as its animation. This creates
 * an animation of the layout files TableLayout, with the layout of each TableRow
 * animated using an android:layoutAnimation so that alternating rows use
 * anim/layout_animation_row_right_slide and anim/layout_animation_row_left_slide.
 * Way too fast to see effect well even on Excite 10
 */
public class LayoutAnimation7 extends Activity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.layout_animation_7.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_animation_7);
    }
}

/*
 * Copyright (C) 2008 The Android Open Source Project
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
import android.view.View;

import com.example.android.apis.R;

/**
 * Demonstrates scrolling with ScrollView's with android:scrollbarStyle set to
 * outsideOverlay and outsideInset in the xml, and one set programmatically using
 * setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET)
 */
public class ScrollBar3 extends Activity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.scrollbar3.
     * Then we find the view with ID R.id.view3 and set its scrollbar style to SCROLLBARS_INSIDE_INSET.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.scrollbar3);

        findViewById(R.id.view3).setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
    }
}

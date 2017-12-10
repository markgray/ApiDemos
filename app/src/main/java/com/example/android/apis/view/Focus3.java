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
import android.widget.Button;

import com.example.android.apis.R;

/**
 * Uses android:nextFocusDown="@+id/bottom", android:nextFocusLeft="@+id/left",
 * android:nextFocusUp="@+id/top", and android:nextFocusRight="@+id/right" to
 * specify focus behavior that would be difficult with default focus calculation
 * algorithm -- need input device suitable for changing focus.
 */
public class Focus3 extends Activity {
    /**
     * Button in our layout file with ID R.id.top
     */
    private Button mTopButton;
    /**
     * Button in our layout file with ID R.id.bottom
     */
    private Button mBottomButton;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.focus_3. Next we
     * initialize our field {@code Button mTopButton} by finding the view with ID R.id.top, and our
     * field {@code Button mBottomButton} by finding the view with ID R.id.bottom.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.focus_3);

        mTopButton = (Button) findViewById(R.id.top);
        mBottomButton = (Button) findViewById(R.id.bottom);
    }

    /**
     * Getter for {@code Button mTopButton}. UNUSED.
     *
     * @return the value of {@code Button mTopButton}
     */
    @SuppressWarnings("unused")
    public Button getTopButton() {
        return mTopButton;
    }

    /**
     * Getter for {@code Button mBottomButton}. UNUSED.
     *
     * @return the value of {@code Button mBottomButton}
     */
    @SuppressWarnings("unused")
    public Button getBottomButton() {
        return mBottomButton;
    }
}

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

package com.example.android.apis.graphics;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Wrapper around {@code Activity} which optionally places the {@code View} that is passed to
 * {@code setContentView} from the {@code onCreate} override in a class that extends this class
 * inside an instance of {@code PictureLayout} if TEST_PICTURE is true. {@code PictureLayout} is
 * a {@code ViewGroup} which displays its one and only child in the four corners of the display.
 */
@SuppressLint("Registered")
class GraphicsActivity extends AppCompatActivity {
    // set to true to test Picture
    private static final boolean TEST_PICTURE = false;

    /**
     * Called when the activity is starting. We simply call through to our super's implementation of
     * {@code onCreate}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Set the activity content to an explicit view. If our debug flag TEST_PICTURE is true, we create
     * an instance of {@code PictureLayout} {@code ViewGroup vg} and add our parameter {@code View view}
     * to it. {@code PictureLayout} extends {@code ViewGroup} to mirror any single {@code View} added to
     * it in the four corners of its canvas. We then set {@code view} to {@code vg}.
     * <p>
     * Finally we call our super's implementation of {@code setContentView} with {@code view} (modified or
     * not).
     *
     * @param view The desired content to display.
     */
    @Override
    public void setContentView(View view) {
        if (TEST_PICTURE) {
            ViewGroup vg = new PictureLayout(this);
            vg.addView(view);
            view = vg;
        }

        super.setContentView(view);
    }
}
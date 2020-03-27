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
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Uses android:nextFocusDown="@+id/bottom", android:nextFocusLeft="@+id/left",
 * android:nextFocusUp="@+id/top", and android:nextFocusRight="@+id/right" to
 * specify focus behavior that would be difficult with default focus calculation
 * algorithm -- need input device suitable for changing focus.
 */
@Suppress("MemberVisibilityCanBePrivate")
class Focus3 : AppCompatActivity() {
    /**
     * [Button] in our layout file with ID R.id.top
     */
    var mTopButton: Button? = null
        private set

    /**
     * [Button] in our layout file with ID R.id.bottom
     */
    var mBottomButton: Button? = null
        private set

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.focus_3. Next we
     * initialize our [Button] field [mTopButton] by finding the view with ID R.id.top, and our
     * [Button] field [mBottomButton] by finding the view with ID R.id.bottom.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.focus_3)
        mTopButton = findViewById(R.id.top)
        mBottomButton = findViewById(R.id.bottom)
    }

}
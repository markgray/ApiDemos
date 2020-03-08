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
package com.example.android.apis.graphics

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity

/**
 * Wrapper around [AppCompatActivity] which optionally places the [View] that is passed to
 * `setContentView` from the `onCreate` override in a class that extends this class
 * inside an instance of [PictureLayout] if TEST_PICTURE is true. [PictureLayout] is
 * a [ViewGroup] which displays its one and only child in the four corners of the display.
 */
@SuppressLint("Registered")
open class GraphicsActivity : AppCompatActivity() {
    /**
     * Called when the activity is starting. We simply call through to our super's implementation of
     * `onCreate`.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    @Suppress("RedundantOverride")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * Set the activity content to an explicit view. First we make a local copy of our [View]
     * parameter [view] in `var viewVar`. If our debug flag TEST_PICTURE is true, we create an
     * instance of [PictureLayout] for [ViewGroup] `val vg` and add our `viewVar` to it.
     * [PictureLayout] extends [ViewGroup] to mirror any single [View] added to it in the four
     * corners of its canvas. We then set `viewVar` to `vg`. Finally we call our super's
     * implementation of `setContentView` with `viewVar` (modified or not).
     *
     * @param view The desired content to display.
     */
    override fun setContentView(view: View) {
        var viewVar: View? = view
        @Suppress("ConstantConditionIf")
        if (TEST_PICTURE) {
            val vg: ViewGroup = PictureLayout(this)
            vg.addView(viewVar)
            viewVar = vg
        }
        super.setContentView(viewVar)
    }

    companion object {
        /**
         * set to true to test Picture
         */
        private const val TEST_PICTURE = false
    }
}
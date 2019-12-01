/*
 * Copyright (C) 2011 The Android Open Source Project
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
package com.example.android.apis.content

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * The layouts included by the `FrameLayout` inside R.layout.resources_width_and_height use
 * layout-wNNNdp and layout-hNNNdp to select between different versions based on the size of the
 * screen. Those from layout-hNNNdp are included first using @layout/resources_height and these
 * different layouts in turn include layouts from layout-hNNNdp by asking for @layout/resources_width.
 */
class ResourcesWidthAndHeight : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to R.layout.resources_width_and_height (our
     * layout file which includes the layout @layout/resources_height which is in fact a different
     * layout for devices with different screen heights. The different @layout/resources_height
     * layouts in turn include the layout @layout/resources_width which is a different layout file
     * for different screen widths.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /**
         * This layout uses different configurations to adjust
         * what is shown based on the current screen width and height.
         */
        setContentView(R.layout.resources_width_and_height)
    }
}
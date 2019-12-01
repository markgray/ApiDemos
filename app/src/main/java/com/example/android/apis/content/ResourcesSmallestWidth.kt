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
 * Layout file `include`'s layouts from layout, and layout-sw**` in order to create custom
 * FrameLayout area for different smallest width (sw480dp, sw600dp, and sw720dp). These in turn
 * `include` other FrameLayout's also with different versions for the smallest width
 * (cute - huh?)
 */
class ResourcesSmallestWidth : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.resources_smallest_width.
     * The layout contains the line `<include layout="@layout/resources_smallest_width_inner" />`
     * and Android chooses between four different versions of this included layout file based on the
     * smallest width:
     *
     *  * Default res/layout/resources_smallest_width_inner.xml
     *  * Smallest width 480dpi res/layout-sw480dp/resources_smallest_width_inner.xml
     *  * Smallest width 600dpi res/layout-sw600dp/resources_smallest_width_inner.xml
     *  * Smallest width 720dpi res/layout-sw720dp/resources_smallest_width_inner.xml
     *
     * Each of these has lines for `<include layout="@layout/resources_smallest_width_row" />`
     * which Android again chooses from between four different versions based on the same smallest
     * screen width (located in the same directories as the resources_smallest_width_inner.xml
     * which includes them of course).
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /**
         * This layout uses different configurations to adjust
         * what is shown based on the smallest width that will occur.
         */
        setContentView(R.layout.resources_smallest_width)
    }
}
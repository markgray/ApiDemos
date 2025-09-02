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

package com.example.android.apis.app

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R


/**
 * Fancy Blur Activity
 *
 * This demonstrates the how to write an activity that is translucent, allowing windows underneath
 * to show through, with a fancy blur compositing effect.
 *
 * This is the same as .app.TranslucentActivity, with the background blurred on
 * older api's - WindowManager.LayoutParams.FLAG_BLUR_BEHIND is no longer
 * supported as of API level 14, and Lollipop+ does not blur at all so the result
 * is just to leave the background even more visible than the foreground --
 * TranslucentActivity has a much better look.
 */
class TranslucentBlurActivity : AppCompatActivity() {
    /**
     * Initialization of the Activity after it is first created. First we call through to our
     * super's implementation of `onCreate`, then we set the flag FLAG_BLUR_BEHIND of or our
     * window (Blurring is no longer supported however, so the result is just to leave the
     * background even more visible than the foreground.
     *
     * @param icicle always null since `onSaveInstanceState` is not overridden
     */
    override fun onCreate(icicle: Bundle?) {
        // Be sure to call the super class.
        super.onCreate(icicle)

        // Have the system blur any windows behind this one.

        window.setFlags(
            WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
            WindowManager.LayoutParams.FLAG_BLUR_BEHIND
        )

        // See assets/res/any/layout/translucent_background.xml for this
        // view layout definition, which is being set here as
        // the content of our screen.
        setContentView(R.layout.translucent_background)
    }
}

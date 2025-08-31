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
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R


/**
 * Target of Example of removing yourself from the history stack after forwarding to
 * another activity, see [Forwarding].
 */
class ForwardTarget : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation
     * of onCreate, then we set our content view to our layout file R.layout.forward_target.
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.forward_target)
    }
}


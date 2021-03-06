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
import androidx.appcompat.app.AppCompatActivity
// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R


/**
 * A gallery of the different styles of buttons. Shows three different buttons,
 * all totally defined in layout/buttons_1.xml: a default button, a small button
 * using style="?android:attr/buttonStyleSmall", and a ToggleButton. The small
 * button is same size as normal button on M.
 */
class Buttons1 : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.buttons_1.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.buttons_1)
    }
}
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
 * Simple example of writing an application Activity.
 * Hello World
 *
 * This demonstrates the basic code needed to write a Screen activity.
 *
 * Demo App/Activity/Hello World
 *
 * Source files:
 *  - src/com.example.android.apis/app/HelloWorld.java The Hello World Screen implementation
 *  - res/any/layout/hello_world.xml</td> Defines contents of the screen
 */
class HelloWorld : AppCompatActivity() {
    /**
     * Initialization of the Activity after it is first created.  Must at least call
     * [setContentView()][androidx.appcompat.app.AppCompatActivity.setContentView] to
     * describe what is to be displayed in the screen.
     *
     * First we call through to our super's implementation of onCreate, then we set out content
     * view to our layout file R.layout.hello_world
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState)

        // See assets/res/any/layout/hello_world.xml for this
        // view layout definition, which is being set here as
        // the content of our screen.
        setContentView(R.layout.hello_world)
    }
}

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

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Demonstrates wrapping a LONG LinearLayout in a ScrollView (All but the first
 * TextView and Button in the LinearLayout are Created and .addView'd programmatically)
 */
class ScrollView2 : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.scroll_view_2. We
     * initialize our [LinearLayout] variable `val layout` by finding the view with ID R.id.layout.
     * Then we loop for i=2 to i=63, creating a new instance for [TextView] `val textView`, setting
     * its text to the string formed by concatenating "Text View " and the string value of `i`,
     * then creating [LinearLayout.LayoutParams] `val p` with the width set to MATCH_PARENT and the
     * height set to WRAP_CONTENT, and using it when we add the view `textView` to `layout`. We next
     * create [Button] `val buttonView`, set its text to the string formed by concatenating "Button "
     * and the string value of `i`, then using `p` as the `LayoutParams` add it to `layout`.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scroll_view_2)
        val layout = findViewById<LinearLayout>(R.id.layout)
        for (i in 2..63) {
            val textView = TextView(this)
            textView.text = "Text View $i"
            val p = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layout.addView(textView, p)
            val buttonView = Button(this)
            buttonView.text = "Button $i"
            layout.addView(buttonView, p)
        }
    }
}
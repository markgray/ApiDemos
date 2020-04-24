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
package com.example.android.apis.text

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Using a [LogTextBox] to display a scrollable text area to which text is
 * appended. LogTextBox.java is a custom `TextView` that is Editable and by
 * default scrollable, like `EditText` without a cursor.
 */
class LogTextBox1 : AppCompatActivity() {
    /**
     * [LogTextBox] in our layout with id R.id.text
     */
    private var mText: LogTextBox? = null

    /**
     * Line number of the text that has been added to [LogTextBox] field [mText], post incremented
     * every time the button with id R.id.add ("Add") is clicked.
     */
    private var lineNumber = 0

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.log_text_box_1. We
     * initialize our [LogTextBox] field [mText] by finding the view in our layout with id
     * R.id.text, and initialize our [Button] variable `val addButton` by finding the view with id
     * R.id.add. Finally we set the `OnClickListener` of `addButton` to an a lambda which appends
     * to [mText] a string formed by concatenating the string "This is a test " to the string value
     * of `lineNumber` (which we post increment) followed by a newline character.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_text_box_1)
        mText = findViewById(R.id.text)
        val addButton = findViewById<Button>(R.id.add)
        addButton.setOnClickListener {
            mText!!.append("This is a test ${lineNumber++}\n")
        }
    }
}
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
import android.widget.TableLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Button uses setColumnShrinkable to toggle the shrinkColumns attribute of a table which
 * has a column so long it forces the last columns off the screen.
 */
class TableLayout9 : AppCompatActivity() {
    /**
     * Flag which indicates the current column shrinkable state of column 0 (toggled by the button
     * with ID R.id.toggle ("Toggle Shrink").
     */
    private var mShrink = false

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.table_layout_9.
     * We initialize [TableLayout] variable `val table` by finding the view with ID R.id.menu.
     * We initialize [Button] variable `val button` by finding the view with ID R.id.toggle
     * ("Toggle Shrink") and set its `OnClickListener` to a lambda whose `onClick` override toggles
     * the value of [mShrink] and uses it to set the column shrinkable state of column 1 of `table`.
     * We then set [mShrink] to the current column shrinkable state of column 1 of `table` (it
     * starts out with the default value of false).
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.table_layout_9)
        val table = findViewById<TableLayout>(R.id.menu)
        val button = findViewById<Button>(R.id.toggle)
        button.setOnClickListener {
            mShrink = !mShrink
            table.setColumnShrinkable(0, mShrink)
        }
        mShrink = table.isColumnShrinkable(0)
    }
}
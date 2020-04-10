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
import android.view.Gravity
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Uses Button to toggle stretch attribute of one column. Includes programmatically built
 * [TableRow] along with xml TableRows
 */
class TableLayout8 : AppCompatActivity() {
    /**
     * Flag to indicate whether column 1 is stretched or not
     * (toggled by the button with ID R.id.toggle)
     */
    private var mStretch = false

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.table_layout_8.
     * We initialize [TableLayout] variable `val table` by finding the view with ID R.id.menu. We
     * initialize [Button] `val button` by finding the view with ID R.id.toggle and set its
     * `OnClickListener` to an a lambda whose `onClick` override toggles the value of [mStretch] and
     * uses it to set the column stretch of column 1 of `table`. We then set [mStretch] to the
     * current stretchable state of column 1 of `table` (it starts out with the default value of
     * false). Finally we call our method [appendRow] to programmatically add a row
     * to `table`.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.table_layout_8)
        val table = findViewById<TableLayout>(R.id.menu)
        val button = findViewById<Button>(R.id.toggle)
        button.setOnClickListener {
            mStretch = !mStretch
            table.setColumnStretchable(1, mStretch)
        }
        mStretch = table.isColumnStretchable(1)
        appendRow(table)
    }

    /**
     * Builds, configures, and adds a [TableRow] to its [TableLayout] parameter [table]. First we
     * create a new instance for [TableRow] variable `val row`. Then we create a new instance for
     * [TextView] variable `val label`, set its text to "Quit", and set its padding to 3 on all
     * sides. We create a new instance for [TextView] variable `val shortcut`, set its text to
     * "Ctrl-Q", set its padding to 3 on all sides, and set its gravity to RIGHT or'ed with TOP.
     * We add `label` to `row` using an instance of [TableRow.LayoutParams] specifying column 1,
     * and add `shortcut` to `row` with a default instance of [TableRow.LayoutParams] (putting it
     * in the next column, column 2). Finally we add `row` to `table` using a default instance of
     * [TableLayout.LayoutParams].
     *
     * @param table [TableLayout] we are to add a [TableRow] to
     */
    @SuppressLint("RtlHardcoded")
    private fun appendRow(table: TableLayout) {
        val row = TableRow(this)
        val label = TextView(this)
        label.setText(R.string.table_layout_8_quit)
        label.setPadding(3, 3, 3, 3)
        val shortcut = TextView(this)
        shortcut.setText(R.string.table_layout_8_ctrlq)
        shortcut.setPadding(3, 3, 3, 3)
        shortcut.gravity = Gravity.RIGHT or Gravity.TOP
        row.addView(label, TableRow.LayoutParams(1))
        row.addView(shortcut, TableRow.LayoutParams())
        table.addView(row, TableLayout.LayoutParams())
    }
}
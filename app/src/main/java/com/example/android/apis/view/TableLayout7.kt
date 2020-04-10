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
 * Button's toggle collapsed state of column 0 or column 2, also show how to build a
 * [TableRow] and add it to a [TableLayout] programmatically. Note that the attribute
 * android:collapseColumns="2" starts column 2 in a collapsed state.
 */
class TableLayout7 : AppCompatActivity() {
    /**
     * True if column number 2 (the shortcuts column) is collapsed, toggled by the button with ID
     * R.id.toggle1 ("Toggle Shortcuts")
     */
    private var mShortcutsCollapsed = false

    /**
     * True if column number 0 (the checkmarks column) is collapsed, toggled by the button with ID
     * R.id.toggle2 ("Toggle Checkmarks")
     */
    private var mCheckmarksCollapsed = false

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.table_layout_7.
     * We initialize our [TableLayout] variable `val table` by finding the view with ID R.id.menu.
     * We initialize [Button] `var button` by finding the view with ID R.id.toggle1 and set its
     * `OnClickListener` to a lambda whose `onClick` method toggles the current value of our field
     * [mShortcutsCollapsed] and uses it to set the collapsed state of column 2 in `table`. We then
     * set `button` by finding the view with ID R.id.toggle2 and set its `OnClickListener` to a
     * lambda whose `onClick` method toggles the current value of our field [mCheckmarksCollapsed]
     * and uses it to set the collapsed state of column 0 in `table`.
     *
     * Next we set [mCheckmarksCollapsed] to the current collapsed state of column 0 of `table`
     * and [mShortcutsCollapsed] to the current collapsed state of column 0 of `table`.
     * Finally we call our method [appendRow] to programmatically build a [TableRow] and
     * add it to `table`.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.table_layout_7)
        val table = findViewById<TableLayout>(R.id.menu)
        var button = findViewById<Button>(R.id.toggle1)
        button.setOnClickListener {
            mShortcutsCollapsed = !mShortcutsCollapsed
            table.setColumnCollapsed(2, mShortcutsCollapsed)
        }
        button = findViewById(R.id.toggle2)
        button.setOnClickListener {
            mCheckmarksCollapsed = !mCheckmarksCollapsed
            table.setColumnCollapsed(0, mCheckmarksCollapsed)
        }
        mCheckmarksCollapsed = table.isColumnCollapsed(0)
        mShortcutsCollapsed = table.isColumnCollapsed(2)
        appendRow(table)
    }

    /**
     * Programmatically builds, configures, and adds a [TableRow] to its [TableLayout] argument
     * [table]. First we create a new instance for our [TableRow] variable `val row`. Next we
     * create [TextView] `val label`, set its text to the string "Quit", and set its padding
     * to 3 on all sides. We create [TextView] `val shortcut`, set its text to the string "Ctrl-Q",
     * set its padding to 3 on all sides, and set its gravity to RIGHT or'ed with TOP. We add
     * `label` to `row` with a [TableRow.LayoutParams] instance that specifies column 1,
     * and add `shortcut` to `row` with a default instance of [TableRow.LayoutParams].
     * Finally we add `row` to `table` using a default instance of [TableLayout.LayoutParams].
     *
     * @param table [TableLayout] we are to add a row to.
     */
    @SuppressLint("RtlHardcoded")
    private fun appendRow(table: TableLayout) {
        val row = TableRow(this)
        val label = TextView(this)
        label.setText(R.string.table_layout_7_quit)
        label.setPadding(3, 3, 3, 3)
        val shortcut = TextView(this)
        shortcut.setText(R.string.table_layout_7_ctrlq)
        shortcut.setPadding(3, 3, 3, 3)
        shortcut.gravity = Gravity.RIGHT or Gravity.TOP
        row.addView(label, TableRow.LayoutParams(1))
        row.addView(shortcut, TableRow.LayoutParams())
        table.addView(row, TableLayout.LayoutParams())
    }
}
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

package com.example.android.apis.view;

import com.example.android.apis.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Button's toggle collapsed state of column 0 or column 2, also show how to build a
 * TableRow and add it to a TableLayout programmatically. Note that the attribute
 * android:collapseColumns="2" starts column 2 in a collapsed state.
 */
public class TableLayout7 extends Activity {
    /**
     * True if column number 2 (the shortcuts column) is collapsed, toggled by the button with ID
     * R.id.toggle1 ("Toggle Shortcuts")
     */
    private boolean mShortcutsCollapsed;
    /**
     * True is column number 0 (the checkmarks column) is collapsed, toggled by the button with ID
     * R.id.toggle2 ("Toggle Checkmarks")
     */
    private boolean mCheckmarksCollapsed;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.table_layout_7.
     * We initialize our variable {@code TableLayout table} by finding the view with ID R.id.menu.
     * We initialize {@code Button button} by finding the view with ID R.id.toggle1 and set its
     * {@code OnClickListener} to an anonymous class whose {@code onClick} method toggles the current
     * value of our field {@code mShortcutsCollapsed} and uses it to set the collapsed state of column
     * 2 in {@code table}. We then set {@code button} by finding the view with ID R.id.toggle2 and set its
     * {@code OnClickListener} to an anonymous class whose {@code onClick} method toggles the current
     * value of our field {@code mCheckmarksCollapsed} and uses it to set the collapsed state of column
     * 0 in {@code table}.
     * <p>
     * Next we set {@code mCheckmarksCollapsed} to the current collapsed state of column 0 of {@code table}
     * and {@code mShortcutsCollapsed} to the current collapsed state of column 0 of {@code table}.
     * Finally we call our method {@code appendRow} to programmatically build a {@code TableRow} and
     * add it to {@code table}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_layout_7);

        final TableLayout table = (TableLayout) findViewById(R.id.menu);
        Button button = (Button) findViewById(R.id.toggle1);
        button.setOnClickListener(new Button.OnClickListener() {
            /**
             * Called when the button with ID R.id.toggle1 ("Toggle Shortcuts") is clicked. First we
             * toggle the value of our field {@code mShortcutsCollapsed}, then we use it to set the
             * collapsed state of column 2 in {@code TableLayout table}.
             *
             * @param v View that was clicked
             */
            @Override
            public void onClick(View v) {
                mShortcutsCollapsed = !mShortcutsCollapsed;
                table.setColumnCollapsed(2, mShortcutsCollapsed);
            }
        });
        button = (Button) findViewById(R.id.toggle2);
        button.setOnClickListener(new Button.OnClickListener() {
            /**
             * Called when the button with ID R.id.toggle2 ("Toggle Checkmarks") is clicked. First we
             * toggle the value of our field {@code mCheckmarksCollapsed}, then we use it to set the
             * collapsed state of column 0 in {@code TableLayout table}.
             *
             * @param v View that was clicked
             */
            @Override
            public void onClick(View v) {
                mCheckmarksCollapsed = !mCheckmarksCollapsed;
                table.setColumnCollapsed(0, mCheckmarksCollapsed);
            }
        });

        mCheckmarksCollapsed = table.isColumnCollapsed(0);
        mShortcutsCollapsed = table.isColumnCollapsed(2);

        appendRow(table);
    }

    /**
     * Programmatically builds, configures, and adds a {@code TableRow} to its argument
     * {@code TableLayout table}. First we create a new instance for our variable {@code TableRow row}.
     * Next we create {@code TextView label}, set its text to the string "Quit", and set its padding
     * to 3 on all sides. We create {@code TextView shortcut}, set its text to the string "Ctrl-Q",
     * set its padding to 3 on all sides, and set its gravity to RIGHT or'ed with TOP. We add
     * {@code label} to {@code row} with a {@code LayoutParams} instance that specifies column 1,
     * and add {@code shortcut} to {@code row} with a default instance of {@code LayoutParams}.
     * Finally we add {@code row} to {@code table} using a default instance of {@code LayoutParams}.
     *
     * @param table {@code TableLayout} we are to add a row to.
     */
    private void appendRow(TableLayout table) {
        TableRow row = new TableRow(this);

        TextView label = new TextView(this);
        label.setText(R.string.table_layout_7_quit);
        label.setPadding(3, 3, 3, 3);

        TextView shortcut = new TextView(this);
        shortcut.setText(R.string.table_layout_7_ctrlq);
        shortcut.setPadding(3, 3, 3, 3);
        shortcut.setGravity(Gravity.RIGHT | Gravity.TOP);

        row.addView(label, new TableRow.LayoutParams(1));
        row.addView(shortcut, new TableRow.LayoutParams());

        table.addView(row, new TableLayout.LayoutParams());
    }
}

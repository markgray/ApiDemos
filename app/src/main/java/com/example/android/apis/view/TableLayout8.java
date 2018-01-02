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
import android.widget.TableLayout;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

/**
 * Uses Button to toggle stretch attribute of one column. Includes programmatically built
 * TableRow along with xml TableRows
 */
public class TableLayout8 extends Activity {
    /**
     * Flag to indicate whether column 1 is stretched or not
     * (toggled by the button with ID R.id.toggle)
     */
    private boolean mStretch;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.table_layout_8.
     * We initialize {@code TableLayout table} by finding the view with ID R.id.menu. We initialize
     * {@code Button button} by finding the view with ID R.id.toggle and set its {@code OnClickListener}
     * to an anonymous class whose {@code onClick} override toggles the value of {@code mStretch} and
     * uses it to set the column stretch of column 1 of {@code table}. We then set {@code mStretch}
     * to the current stretchable state of column 1 of {@code table} (it starts out with the default
     * value of false). Finally we call our method {@code appendRow} to programmatically add a row
     * to {@code table}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_layout_8);

        final TableLayout table = (TableLayout) findViewById(R.id.menu);
        Button button = (Button) findViewById(R.id.toggle);
        button.setOnClickListener(new Button.OnClickListener() {
            /**
             * Called when the button with ID R.id.toggle ("Toggle Stretch") is clicked, we toggle
             * the value of our field {@code mStretch} and use it to set the stretchable state of
             * column 1 of {@code table}.
             *
             * @param v View that was clicked
             */
            @Override
            public void onClick(View v) {
                mStretch = !mStretch;
                table.setColumnStretchable(1, mStretch);
            }
        });

        mStretch = table.isColumnStretchable(1);

        appendRow(table);
    }

    /**
     * Builds, configures, and adds a {@code TableRow} to its parameter {@code TableLayout table}.
     * First we create a new instance for {@code TableRow row}. Then we create a new instance for
     * {@code TextView label}, set its text to "Quit", and set its padding to 3 on all sides. We
     * create a new instance for {@code TextView shortcut}, set its text to "Ctrl-Q", set its padding
     * to 3 on all sides, and set its gravity to RIGHT or'ed with TOP. We add {@code label} to
     * {@code row} using an instance of {@code LayoutParams} specifying column 1, and add {@code shortcut}
     * to {@code row} with a default instance of {@code LayoutParams} (putting it in the next column,
     * column 2). Finally we add {@code row} to {@code table} using a default instance of
     * {@code LayoutParams}.
     *
     * @param table {@code TableLayout} we are to add a {@code TableRow} to
     */
    private void appendRow(TableLayout table) {
        TableRow row = new TableRow(this);

        TextView label = new TextView(this);
        label.setText(R.string.table_layout_8_quit);
        label.setPadding(3, 3, 3, 3);

        TextView shortcut = new TextView(this);
        shortcut.setText(R.string.table_layout_8_ctrlq);
        shortcut.setPadding(3, 3, 3, 3);
        shortcut.setGravity(Gravity.RIGHT | Gravity.TOP);

        row.addView(label, new TableRow.LayoutParams(1));
        row.addView(shortcut, new TableRow.LayoutParams());

        table.addView(row, new TableLayout.LayoutParams());
    }
}

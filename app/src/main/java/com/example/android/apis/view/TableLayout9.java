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
import android.os.Bundle;
import android.view.View;

/**
 * Button uses setColumnShrinkable to toggle the shrinkColumns attribute of a table which
 * has a column so long it forces the last columns off the screen.
 */
public class TableLayout9 extends Activity {
    /**
     * Flag which indicates the current column shrinkable state of column 0 (toggled by the button
     * with ID R.id.toggle ("Toggle Shrink").
     */
    private boolean mShrink;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.table_layout_9.
     * We initialize {@code TableLayout table} by finding the view with ID R.id.menu. We initialize
     * {@code Button button} by finding the view with ID R.id.toggle ("Toggle Shrink") and set its
     * {@code OnClickListener} to an anonymous class whose {@code onClick} override toggles the value
     * of {@code mShrink} and uses it to set the column shrinkable state of column 1 of {@code table}.
     * We then set {@code mShrink} to the current column shrinkable state of column 1 of {@code table}
     * (it starts out with the default value of false).
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_layout_9);

        final TableLayout table = (TableLayout) findViewById(R.id.menu);
        Button button = (Button) findViewById(R.id.toggle);
        button.setOnClickListener(new Button.OnClickListener() {
            /**
             * Called when the button with ID R.id.toggle ("Toggle Shrink") is clicked, we toggle the
             * value of {@code mShrink} and then use it to set the column shrinkable state of column 1
             * of {@code table}.
             *
             * @param v View that was clicked
             */
            @Override
            public void onClick(View v) {
                mShrink = !mShrink;
                table.setColumnShrinkable(0, mShrink);
            }
        });

        mShrink = table.isColumnShrinkable(0);
    }
}

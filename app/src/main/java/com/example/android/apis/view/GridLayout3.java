/*
 * Copyright (C) 2011 The Android Open Source Project
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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
import static android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD;
import static android.widget.GridLayout.ALIGN_BOUNDS;
import static android.widget.GridLayout.BASELINE;
import static android.widget.GridLayout.CENTER;
import static android.widget.GridLayout.FILL;
import static android.widget.GridLayout.LEFT;
import static android.widget.GridLayout.LayoutParams;
import static android.widget.GridLayout.RIGHT;
import static android.widget.GridLayout.Spec;
import static android.widget.GridLayout.spec;

/**
 * A form, showing use of the GridLayout API from java code. Here we demonstrate use of the
 * row/column order preserved property which allows rows and or columns to pass over each other
 * when needed. The two buttons in the bottom right corner need to be separated from the other
 * UI elements. This can either be done by separating rows or separating columns - but we don't
 * need to do both and may only have enough space to do one or the other.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
@SuppressLint("SetTextI18n")
public class GridLayout3 extends AppCompatActivity {
    /**
     * Creates, builds, configures and returns a {@code GridLayout} to use for our UI. First we
     * initialize our variable {@code GridLayout p} with a new instance, set it to use the default
     * margins, and set its alignment mode to ALIGN_BOUNDS. We initialize {@code Configuration configuration}
     * with the current configuration that is in effect for our {@code context}. If the {@code orientation}
     * field of {@code configuration} is ORIENTATION_PORTRAIT we configure {@code p} to be at liberty to
     * place the horizontal column boundaries in whatever order best fits its constraints. Otherwise
     * we configure {@code p} to be at liberty to place the vertical row boundaries in whatever order
     * best fits its constraints.
     * <p>
     * Next we define the grid indices and alignment for the rows of {@code Grid} cells. We define
     * {@code Spec titleRow} to start in row 0, {@code Spec introRow} to start row 1, {@code Spec emailRow}
     * to start in row 2 with BASELINE alignment, {@code Spec passwordRow} to start row 3 with BASELINE
     * alignment, {@code Spec button1Row} to start row 5, and {@code Spec button2Row} to start row 6.
     * <p>
     * We define the grid indices, size, and alignment of the columns of cells as follows:
     * {@code Spec centerInAllColumns} starts in column 0, with size 4 and CENTER alignment,
     * {@code Spec leftAlignInAllColumns} starts in column 0, with size 4 and LEFT alignment,
     * {@code Spec labelColumn} starts in column 0, with RIGHT alignment, {@code Spec fieldColumn}
     * starts in column 1, with LEFT alignment, {@code Spec defineLastColumn} starts in column 3,
     * {@code Spec fillLastColumn} starts in column 3 with FILL alignment (Indicates that a view
     * should expanded to fit the boundaries of its cell group).
     * <p>
     * Now we create some views and use the above {@code Spec} objects to position them. We create
     * {@code TextView c}, set its text size to 32, set its text to "Email setup" and add it to
     * {@code p} using a new instance of {@code LayoutParams} which uses {@code titleRow} as the
     * row spec, and {@code centerInAllColumns} as the column spec. We then create a new instance
     * of {@code TextView c}, set its text size to 16, set its text to "You can configure email in
     * a few simple steps:", and add it to {@code p} using a new instance of {@code LayoutParams}
     * which uses {@code introRow} as the row spec, and {@code leftAlignInAllColumns} as the column
     * spec. We then create a new instance of {@code TextView c}, set its text to "Email address:",
     * and add it to {@code p} using a new instance of {@code LayoutParams} which uses {@code emailRow}
     * as the row spec, and {@code labelColumn} as the column spec. We create a new instance for
     * {@code EditText c}, set its size to 10 ems, set its input type to TYPE_CLASS_TEXT or'ed with
     * TYPE_TEXT_VARIATION_EMAIL_ADDRESS, and add it to {@code p} using a new instance of
     * {@code LayoutParams} which uses {@code emailRow} as the row spec, and {@code fieldColumn} as
     * the column spec. We create a new instance for {@code TextView c}, set its text to "Password:",
     * and add it to {@code p} using a new instance of {@code LayoutParams} which uses {@code passwordRow}
     * as the row spec, and {@code labelColumn} as the column spec. We create a new instance of
     * {@code EditText} for {@code TextView c}, set its size to 8 ems, set its input type to TYPE_CLASS_TEXT
     * or'ed with TYPE_TEXT_VARIATION_PASSWORD, and add it to {@code p} using a new instance of
     * {@code LayoutParams} which uses {@code passwordRow} as the row spec, and {@code fieldColumn}
     * as the column spec. We create a new instance for {@code Button c}, set its text to "Manual setup",
     * and add it to {@code p} using a new instance of {@code LayoutParams} which uses {@code button1Row}
     * as the row spec, and {@code defineLastColumn} as the column spec. We create a new instance for
     * {@code Button c}, set its text to "Next", and add it to {@code p} using a new instance of
     * {@code LayoutParams} which uses {@code button2Row} as the row spec, and {@code fillLastColumn}
     * as the column spec.
     * <p>
     * Finally we return {@code p} to the caller.
     *
     * @param context {@code Context} to use to access resources, "this" when called from our
     *                {@code onCreate} override.
     * @return a {@code GridLayout} containing our UI
     */
    public static View create(Context context) {
        GridLayout p = new GridLayout(context);
        p.setUseDefaultMargins(true);
        p.setAlignmentMode(ALIGN_BOUNDS);
        Configuration configuration = context.getResources().getConfiguration();
        if ((configuration.orientation == Configuration.ORIENTATION_PORTRAIT)) {
            p.setColumnOrderPreserved(false);
        } else {
            p.setRowOrderPreserved(false);
        }

        Spec titleRow = spec(0);
        Spec introRow = spec(1);
        Spec emailRow = spec(2, BASELINE);
        Spec passwordRow = spec(3, BASELINE);
        Spec button1Row = spec(5);
        Spec button2Row = spec(6);

        Spec centerInAllColumns = spec(0, 4, CENTER);
        Spec leftAlignInAllColumns = spec(0, 4, LEFT);
        Spec labelColumn = spec(0, RIGHT);
        Spec fieldColumn = spec(1, LEFT);
        Spec defineLastColumn = spec(3);
        Spec fillLastColumn = spec(3, FILL);

        {
            TextView c = new TextView(context);
            c.setTextSize(32);
            c.setText("Email setup");
            p.addView(c, new LayoutParams(titleRow, centerInAllColumns));
        }
        {
            TextView c = new TextView(context);
            c.setTextSize(16);
            c.setText("You can configure email in a few simple steps:");
            p.addView(c, new LayoutParams(introRow, leftAlignInAllColumns));
        }
        {
            TextView c = new TextView(context);
            c.setText("Email address:");
            p.addView(c, new LayoutParams(emailRow, labelColumn));
        }
        {
            EditText c = new EditText(context);
            c.setEms(10);
            c.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            p.addView(c, new LayoutParams(emailRow, fieldColumn));
        }
        {
            TextView c = new TextView(context);
            c.setText("Password:");
            p.addView(c, new LayoutParams(passwordRow, labelColumn));
        }
        {
            TextView c = new EditText(context);
            c.setEms(8);
            c.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_PASSWORD);
            p.addView(c, new LayoutParams(passwordRow, fieldColumn));
        }
        {
            Button c = new Button(context);
            c.setText("Manual setup");
            p.addView(c, new LayoutParams(button1Row, defineLastColumn));
        }
        {
            Button c = new Button(context);
            c.setText("Next");
            p.addView(c, new LayoutParams(button2Row, fillLastColumn));
        }

        return p;
    }

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to the {@code GridLayout} built and configured
     * by our method {@code create}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(create(this));
    }
}
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;

/**
 * Demonstrates wrapping a LONG LinearLayout in a ScrollView (All but the first
 * TextView and Button in the LinearLayout are Created and .addView'd programmatically)
 */
public class ScrollView2 extends Activity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.scroll_view_2.
     * We initialize our variable {@code LinearLayout layout} by finding the view with ID R.id.layout.
     * Then we loop for i=2 to i=63, creating a new instance for {@code TextView textView}, setting
     * its text to the string formed by concatenating "Text View " and the string value of {@code i},
     * then creating {@code LayoutParams p} with the width set to MATCH_PARENT and the height set to
     * WRAP_CONTENT, and using it when we add the view {@code textView} to {@code layout}. We next
     * create {@code Button buttonView}, set its text to the string formed by concatenating "Button "
     * and the string value of {@code i}, then using {@code p} as the {@code LayoutParams} add it to
     * {@code layout}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scroll_view_2);

        LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
        for (int i = 2; i < 64; i++) {
            TextView textView = new TextView(this);
            textView.setText("Text View " + i);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layout.addView(textView, p);

            Button buttonView = new Button(this);
            buttonView.setText("Button " + i);
            layout.addView(buttonView, p);
        }
    }
}

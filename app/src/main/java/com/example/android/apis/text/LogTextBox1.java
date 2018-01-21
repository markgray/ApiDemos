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

package com.example.android.apis.text;

import com.example.android.apis.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Using a LogTextBox to display a scrollable text area to which text is
 * appended. LogTextBox.java is a custom TextView that is Editable and by
 * default scrollable, like EditText without a cursor.
 */
public class LogTextBox1 extends Activity {
    /**
     * {@code LogTextBox} in our layout with id R.id.text
     */
    private LogTextBox mText;
    /**
     * Line number of the text that has been added to {@code LogTextBox mText}, post incremented every
     * time the button with id R.id.add ("Add") is clicked.
     */
    public int lineNumber;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.log_text_box_1. We
     * initialize our field {@code LogTextBox mText} by finding the view in our layout with id
     * R.id.text, and initialize our variable {@code Button addButton} by finding the view with id
     * R.id.add. Finally we set the {@code OnClickListener} of {@code addButton} to an anonymous
     * class which appends to {@code mText} a string formed by concatenating the string "This is a
     * test " to the string value of {@code lineNumber} (which we post increment) followed by a
     * newline character.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.log_text_box_1);

        mText = (LogTextBox) findViewById(R.id.text);

        Button addButton = (Button) findViewById(R.id.add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mText.append("This is a test " + lineNumber++ + "\n");
            }
        });
    }
}

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

package com.example.android.apis.content;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


/**
 * Demonstration of styled text resources. Shows a resource string being used directly in the layout,
 * and by being retrieved and assigned programmatically.
 */
public class StyledText extends AppCompatActivity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.styled_text which
     * contains an android:text="@string/styled_text" attribute for one of its {@code TextView}'s.
     * We now fetch {@code CharSequence str} from our resource for that same stylized String
     * R.string.styled_text, locate the {@code TextView tv} at R.id.text and set its text to
     * {@code str}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // See assets/res/any/layout/styled_text.xml for this
        // view layout definition.
        setContentView(R.layout.styled_text);

        // Programmatically retrieve a string resource with style
        // information and apply it to the second text view.  Note the
        // use of CharSequence instead of String so we don't lose
        // the style info.
        CharSequence str = getText(R.string.styled_text);
        TextView tv = findViewById(R.id.text);
        tv.setText(str);
    }
}


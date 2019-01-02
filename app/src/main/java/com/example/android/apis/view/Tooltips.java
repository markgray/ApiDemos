/*
 * Copyright (C) 2016 The Android Open Source Project
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
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;

/**
 * A demo for tooltips, it is implemented almost entirely in the layout file layout/tooltips.xml using
 * the android:tooltipText attribute for the views, but the {@code TextView} with id R.id.tooltips_code
 * has its tooltip set using its {@code setTooltipText} method in our {@code onCreate} override.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class Tooltips extends Activity {

    /**
     * Called when the activity is starting. First we call our super's implementation of {@code onCreate},
     * then we set our content view to our layout file R.layout.tooltips. We find the view in our layout
     * file with id R.id.tooltips_code and call its {@code setTooltipText} method to set its tooltip text
     * to the string "This tooltip is set in code".
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tooltips);

        findViewById(R.id.tooltips_code).setTooltipText("This tooltip is set in code");
    }
}

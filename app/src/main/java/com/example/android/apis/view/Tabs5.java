/*
 * Copyright (C) 2008 The Android Open Source Project
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
import android.app.TabActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

/**
 * Demonstrates the Tab scrolling when too many tabs are displayed to fit
 * in the screen. In the layout a HorizontalScrollView contains the TabWidget
 * and 30 tabs are added to it using TabHost.addTab(TabHost.TabSpec tabSpec)
 * the callback createTabContent creates the content as each tab is selected.
 */
@SuppressWarnings("deprecation")
public class Tabs5 extends TabActivity implements TabHost.TabContentFactory {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.tabs_scroll. Next
     * we initialize our variable {@code TabHost tabHost} by retrieving the TabHost our activity is
     * using to host its tabs. We loop over {@code int i} from 1 to 30 creating {@code String name}
     * by concatenating the string "Tab " with the string value of {@code i}, then using the
     * {@code tabHost} method {@code addTab} to add a {@code TabSpec} created using {@code name} as
     * the tag and the indicator label, and setting the {@code TabContentFactory} to "this".
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.tabs_scroll);

        final TabHost tabHost = getTabHost();

        for (int i = 1; i <= 30; i++) {
            String name = "Tab " + i;
            tabHost.addTab(tabHost.newTabSpec(name)
                    .setIndicator(name)
                    .setContent(this));
        }
    }

    /**
     * Callback to make the tab contents. We initialize our variable {@code TextView tv} with a new
     * instance, set its text to a string formed by concatenating the string "Content for tab with tag "
     * with our parameter {@code String tag}, and return {@code tv} to the caller.
     *
     * @param tag Which tab was selected.
     * @return The view to display the contents of the selected tab.
     */
    @SuppressLint("SetTextI18n")
    public View createTabContent(String tag) {
        final TextView tv = new TextView(this);
        tv.setText("Content for tab with tag " + tag);
        return tv;
    }
}

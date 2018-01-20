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
 * Example of using a tab content factory for the content via
 * TabHost.TabSpec#setContent(android.widget.TabHost.TabContentFactory)
 * It also demonstrates using an icon on one of the tabs via
 * TabHost.TabSpec#setIndicator(CharSequence, android.graphics.drawable.Drawable)
 * but this does not work using the default Theme as of Ice Cream Sandwich.
 */
@SuppressWarnings("deprecation")
public class Tabs2 extends TabActivity implements TabHost.TabContentFactory {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}. Then we initialize our variable {@code TabHost tabHost} by retrieving the
     * TabHost our activity is using to host its tabs. We add three tabs to {@code TabHost tabHost}
     * using its {@code addTab} method to add tab specs created with the tags "tab1", "tab2", and
     * "tab3", using the same string to set the indicator label, and setting the content of the tabs
     * to use "this" as the TabHost.TabContentFactory to use to create the content of all tabs.
     * Note that the "tab1" call to the method {@code setIndicator} also specifies a drawable to
     * use but this stopped working for the default Theme as of Ice Cream Sandwich.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final TabHost tabHost = getTabHost();
        tabHost.addTab(tabHost.newTabSpec("tab1")
                .setIndicator("tab1", getResources().getDrawable(R.drawable.star_big_on))
                .setContent(this));
        tabHost.addTab(tabHost.newTabSpec("tab2")
                .setIndicator("tab2")
                .setContent(this));
        tabHost.addTab(tabHost.newTabSpec("tab3")
                .setIndicator("tab3")
                .setContent(this));
    }

    /**
     * Callback to make the tab contents. We initialize our variable {@code TextView tv} with a new
     * instance, set its text to a string formed by concatenating the string "Content for tab with tag "
     * with our parameter {@code String tag}, then return {@code tv} to the caller.
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

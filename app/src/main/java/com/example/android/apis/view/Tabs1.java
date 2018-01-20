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

import android.app.TabActivity;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.view.LayoutInflater;

import com.example.android.apis.R;

/**
 * An example of tabs that uses labels ({@link TabSpec#setIndicator(CharSequence)})
 * for its indicators and views by id from a layout file ({@link TabSpec#setContent(int)}).
 */
@SuppressWarnings("deprecation")
public class Tabs1 extends TabActivity {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}. Then we initialize our variable {@code TabHost tabHost} by retrieving the
     * TabHost our activity is using to host its tabs. We obtain the {@code LayoutInflater} for the
     * context "this", and use it to inflate our layout file R.layout.tabs1 using the tab content
     * view of {@code TabHost tabHost} as its root, and attaching our inflated view to this root.
     * We add three tabs to {@code TabHost tabHost} using its {@code addTab} method to add tab specs
     * created with the tags "tab1", "tab2", and "tab3", using the same string to set the indicator
     * label, and setting the content of the tabs to use the views with resource id's R.id.view1,
     * R.id.view2, and R.id.view3 respectively.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TabHost tabHost = getTabHost();

        LayoutInflater.from(this).inflate(R.layout.tabs1, tabHost.getTabContentView(), true);

        tabHost.addTab(tabHost.newTabSpec("tab1")
                .setIndicator("tab1")
                .setContent(R.id.view1));
        tabHost.addTab(tabHost.newTabSpec("tab2")
                .setIndicator("tab2")
                .setContent(R.id.view2));
        tabHost.addTab(tabHost.newTabSpec("tab3")
                .setIndicator("tab3")
                .setContent(R.id.view3));
    }
}

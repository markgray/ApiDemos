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

package com.example.android.apis.content;

import com.example.android.apis.R;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Shows how to write layout resource references in values-* directories, so that you can define
 * multiple different configurations of a layout resource that refer to one actual XML definition.
 * References are defined in layout.xml files contained in res/values-xlarge and res/values-sw600dp
 * which point to @layout/resources_layout_reference_tablet and @layout/resources_layout_reference_tablet
 * respectively (uh... both same file.) There is also a default resources_layout_reference.xml file in
 * res/layout/ which is used for other size screens.
 */
public class ResourcesLayoutReference extends AppCompatActivity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}. Then we set our content view to one of our layout files depending on the
     * screen size:
     * <ul>
     *     <li>res/layout/resources_layout_reference.xml for the default screen sizes</li>
     *     <li>res/layout/resources_layout_reference_tablet.xml for sw600dp screens (smallest width 600dpi)</li>
     *     <li>res/layout/resources_layout_reference_tablet.xml for xlarge screens (at least 960dp x 720dp)</li>
     * </ul>
     * The choice of using resources_layout_reference_tablet.xml is made by using the resource references
     * contained in the files res/values-xlarge/layout.xml, and res/values-sw600dp/layout.xml
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This layout uses different configurations to adjust
        // what is shown based on the smallest width that will occur.
        setContentView(R.layout.resources_layout_reference);
    }
}

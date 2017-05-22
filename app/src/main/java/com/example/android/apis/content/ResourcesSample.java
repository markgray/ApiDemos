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

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TextView;


/**
 * Demonstration of loading resources.
 * <p>
 * Each context has a resources object that you can access.  Additionally,
 * the Context class (an Activity is a Context) has a getString convenience
 * method getString() that looks up a string resource.
 * <p>
 * see content.StyledText for more depth about using styled text, both with getString()
 * and in the layout xml files.
 */
public class ResourcesSample extends Activity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.resources. We declare
     * a {@code TextView tv} to hold a reference to the various {@code TextView}'s in our layout during
     * their configuration, a {@code CharSequence cs} to hold Strings containing styled text retrieved
     * from our resources, and {@code String str} to hold the same Strings without the style information.
     * <p>
     * We now proceed to show the results of retrieving the same String resource R.string.styled_text
     * ({@code Plain, <b>bold</b>, <i>italic</i>, <b><i>bold-italic</i></b>}) using three different
     * methods:
     * <ul>
     * <li>
     * As a {@code CharSequence cs} using the {@code getText} method, which we then display
     * in the {@code TextView} R.id.styled_text
     * </li>
     * <li>
     * As a {@code String str} using the {@code getString} method, which we then display
     * in the {@code TextView} R.id.plain_text
     * </li>
     * <li>
     * Again as a {@code CharSequence cs} using the {@code getText} method, but this time
     * we explicitly use the {@code Context context} we retrieve from "this" to get a
     * {@code Resources res} using the method {@code context.getResources()}, and then
     * using {@code res} to retrieve the {@code CharSequence cs}, which we then display in
     * the {@code TextView} R.id.res1.
     * </li>
     * </ul>
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // See res/any/layout/resources.xml for this view layout definition.
        setContentView(R.layout.resources);

        TextView tv;
        CharSequence cs;
        String str;

        // ====== Using the Context.getText() convenience method ===========

        // Using the getText() convenience method, retrieve a string
        // resource that happens to have style information.  Note the use of
        // CharSequence instead of String so we don't lose the style info.
        cs = getText(R.string.styled_text);
        tv = (TextView) findViewById(R.id.styled_text);
        tv.setText(cs);

        // Use the same resource, but convert it to a string, which causes it
        // to lose the style information.
        str = getString(R.string.styled_text);
        tv = (TextView) findViewById(R.id.plain_text);
        tv.setText(str);

        // ====== Using the Resources object =================================

        // You might need to do this if your code is not in an activity.
        // For example View has a protected mContext field you can use.
        // In this case it's just 'this' since Activity is a context.
        Context context = this;

        // Get the Resources object from our context
        Resources res = context.getResources();

        // Get the string resource, like above.
        cs = res.getText(R.string.styled_text);
        tv = (TextView) findViewById(R.id.res1);
        tv.setText(cs);

        // Note that the Resources class has methods like getColor(),
        // getDimen(), getDrawable() because themes are stored in resources.
        // You can use them, but you might want to take a look at the view
        // examples to see how to make custom widgets.

    }
}


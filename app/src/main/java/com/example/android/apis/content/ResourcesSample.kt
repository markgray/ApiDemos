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
package com.example.android.apis.content

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R

/**
 * Demonstration of loading resources. *
 *
 * Each context has a resources object that you can access.  Additionally,
 * the [Context] class (an `Activity` is a [Context]) has a `getString` convenience
 * method that looks up a string resource.
 *
 * see content.StyledText for more depth about using styled text, both with `getString`
 * and in the layout xml files.
 */
class ResourcesSample : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.resources. We declare
     * [CharSequence] variable `var cs` to hold Strings containing styled text retrieved from our
     * resources and then initialize it to the [CharSequence] retrieved by the [getText] method
     * of our [Context] for the resource ID R.string.styled_text (`Plain, <b>bold</b>, <i>italic</i>,
     * <b><i>bold-italic</i></b>`). We initialize [TextView] `var tv` by finding the View with ID
     * R.id.styled_text, then set its text to `cs`. Next we initialize [String] variable `val str`
     * to the [String] returned by the [getString] of our [Context], set `tv` to the [TextView] with
     * ID R.id.plain_text, and then set the text of `tv` to `str`. Finally we explicitly use the
     * [Context] `val context` of *this* (Activity) to fetch a `Resources` instance from it to our
     * variable `val res`, and then set `cs` to the [CharSequence] that the [Resources.getText]
     * method of `res` returns for the resource ID R.string.styled_text, set `tv` to the View with
     * ID R.id.res1, and then set the text of `tv` to `cs`.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // See res/any/layout/resources.xml for this view layout definition.
        setContentView(R.layout.resources)
        var cs: CharSequence?
        /**
         * ====== Using the Context.getText() convenience method ===========
         * Using the getText() convenience method, retrieve a string
         * resource that happens to have style information.  Note the use of
         * CharSequence instead of String so we don't lose the style info.
         */
        cs = getText(R.string.styled_text)
        var tv: TextView = findViewById(R.id.styled_text)
        tv.text = cs
        /**
         * Use the same resource, but convert it to a string, which causes it
         * to lose the style information.
         */
        val str: String = getString(R.string.styled_text)
        tv = findViewById(R.id.plain_text)
        tv.text = str
        /**
         * ====== Using the Resources object =================================
         * You might need to do this if your code is not in an activity.
         * For example View has a protected mContext field you can use.
         * In this case it's just 'this' since Activity is a context.
         */
        val context: Context = this
        /**
         * Get the Resources object from our context
         */
        val res = context.resources
        /**
         * Get the string resource, like above.
         */
        cs = res.getText(R.string.styled_text)
        tv = findViewById(R.id.res1)
        tv.text = cs
        /**
         * Note that the Resources class has methods like getColor(),
         * getDimen(), getDrawable() because themes are stored in resources.
         * You can use them, but you might want to take a look at the view
         * examples to see how to make custom widgets.
         */
    }
}
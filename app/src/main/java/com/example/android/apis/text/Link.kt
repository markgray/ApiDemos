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
package com.example.android.apis.text

import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Four different ways to generate html like links and styled text: text1 shows
 * the android:autoLink property, which automatically creates links for things
 * like URLs and phone numbers found in the text. No java code is needed to make
 * this work; text2 has links specified by putting `<a>` tags in the string resource
 * By default these links will appear but not respond to user input. To make
 * them active, you need to call `setMovementMethod()` on the TextView object;
 * text3 shows creating text with links from HTML in the Java code, rather than
 * from a string resource. Note that for a fixed string, using a (localizable)
 * resource as shown above is usually a better way to go, this example is
 * intended to illustrate how you might display text that came from a dynamic
 * source (eg, the network); text4 illustrates constructing a styled string
 * containing a link without using HTML at all. Again, for a fixed string you
 * should probably be using a string resource, not a hardcoded value.
 */
class Link : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.link. Then we initialize
     * our [TextView] variable `val t2` by finding the view with id R.id.text2 and set its movement
     * method (arrow key handler) to a new instance of a movement method that traverses links in the
     * text buffer and scrolls if necessary. We initialize [TextView] variable `val t3` by finding
     * the view with id R.id.text3, set its text to displayable styled text from a HTML string, and
     * set its movement method (arrow key handler) to a new instance of a movement method that
     * traverses links in the text buffer and scrolls if necessary. We initialize our [SpannableString]
     * variable `val ss` with a new instance of [SpannableString], set a span of style Typeface.BOLD
     * for 0 to 30, set a [URLSpan] with a phone number URL for 31+6 to 31+10. We then initialize
     * our [TextView] variable `val t4` by finding the view with id R.id.text4, set its text to `ss`
     * and set its movement method (arrow key handler) to a new instance of a movement method that
     * traverses links in the text buffer and scrolls if necessary.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.link)

        // text1 shows the android:autoLink property, which
        // automatically creates links for things like URLs and phone numbers
        // found in the text. No java code is needed to make this
        // work.

        // text2 has links specified by putting <a> tags in the string
        // resource. By default these links will appear but not
        // respond to user input. To make them active, you need to
        // call setMovementMethod() on the TextView object.
        val t2 = findViewById<TextView>(R.id.text2)
        t2.movementMethod = LinkMovementMethod.getInstance()

        // text3 shows creating text with links from HTML in the Java
        // code, rather than from a string resource. Note that for a
        // fixed string, using a (localizable) resource as shown above
        // is usually a better way to go; this example is intended to
        // illustrate how you might display text that came from a
        // dynamic source (eg, the network).
        val t3 = findViewById<TextView>(R.id.text3)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            t3.text = Html.fromHtml(
                "<b>text3: Constructed from HTML programmatically.</b>  Text with a " +
                    "<a href=\"http://www.google.com\">link</a> " +
                    "created in the Java source code using HTML.",
                Html.FROM_HTML_MODE_LEGACY
            )
        } else {
            @Suppress("DEPRECATION")
            t3.text = Html.fromHtml(
                "<b>text3: Constructed from HTML programmatically.</b>  Text with a " +
                    "<a href=\"http://www.google.com\">link</a> " +
                    "created in the Java source code using HTML."
            )
        }
        t3.movementMethod = LinkMovementMethod.getInstance()

        // text4 illustrates constructing a styled string containing a
        // link without using HTML at all. Again, for a fixed string
        // you should probably be using a string resource, not a
        // hardcoded value.
        val ss = SpannableString(
            "text4: Manually created spans. Click here to dial the phone."
        )
        ss.setSpan(
            StyleSpan(Typeface.BOLD),
            0, 30,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ss.setSpan(
            URLSpan("tel:4155551212"),
            31 + 6, 31 + 10,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val t4 = findViewById<TextView>(R.id.text4)
        t4.text = ss
        t4.movementMethod = LinkMovementMethod.getInstance()
    }
}
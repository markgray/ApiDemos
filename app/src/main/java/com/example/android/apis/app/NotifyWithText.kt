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

package com.example.android.apis.app

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * When you push the button on this Activity, it creates a [Toast] object and shows it.
 * @see Toast
 *
 * @see Toast.makeText
 * @see Toast.makeText
 * @see Toast.LENGTH_SHORT
 *
 * @see Toast.LENGTH_LONG
 */
class NotifyWithText : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.notify_with_text.
     * We declare `Button button`, then locate the Button R.id.short_notify in our the layout
     * and set its `OnClickListener` to an anonymous class which makes a `Toast` with
     * the text R.string.short_notification_text ("Short notification") and shows it. Next we locate
     * the Button R.id.long_notify in our layout and set its `OnClickListener` to an anonymous
     * class which makes a `Toast` with the text R.string.long_notification_text ("This is a
     * long notification. See, you might need a second more to read it.")
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.notify_with_text)

        var button: Button = findViewById(R.id.short_notify)

        // short notification
        button.setOnClickListener {

            // Note that we create the Toast object and call the show() method
            // on it all on one line.  Most uses look like this, but there
            // are other methods on Toast that you can call to configure how
            // it appears.
            //
            // Note also that we use the version of makeText that takes a
            // resource id (R.string.short_notification_text).  There is also
            // a version that takes a CharSequence if you must construct
            // the text yourself.
            Toast.makeText(this@NotifyWithText, R.string.short_notification_text,
                    Toast.LENGTH_SHORT).show()
        }


        // long notification
        // The only difference here is that the notification stays up longer.
        // You might want to use this if there is more text that they're going
        // to read.
        button = findViewById(R.id.long_notify)
        /**
         * Shows the toast "This is a long notification. See, you might need a second more to read it."
         *
         * Parameter: View of the Button that was clicked
         */
        button.setOnClickListener {
            Toast.makeText(this@NotifyWithText, R.string.long_notification_text,
                    Toast.LENGTH_LONG).show()
        }

    }
}

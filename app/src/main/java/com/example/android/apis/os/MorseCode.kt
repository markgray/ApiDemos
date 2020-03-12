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
package com.example.android.apis.os

import android.content.Context
import android.os.Bundle
import android.os.Vibrator
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
/**
 * <h3>App that vibrates the vibrator with the Morse Code for a string.</h3>
 *
 *
 *
 * This demonstrates the [android.os.Vibrator] class.
 *
 *
 * <h4>Demo</h4>
 * OS / Morse Code Vibrator
 *
 *
 * <h4>Source files</h4>
 * <table class="LinkTable">
 * <tr>
 * <td>src/com.example.android.apis/os/MorseCode.java</td>
 * <td>The Morse Code Vibrator</td>
</tr> *
 * <tr>
 * <td>res/any/layout/morse_code.xml</td>
 * <td>Defines contents of the screen</td>
</tr> *
</table> *
 */
@Suppress("MemberVisibilityCanBePrivate")
class MorseCode : AppCompatActivity() {
    /**
     * Our text view with ID R.id.text, used to enter text for us to convert to Morse code.
     */
    private var mTextView: TextView? = null

    /**
     * Initialization of the Activity after it is first created. First we call through to our
     * super's implementation of `onCreate`, then we set our content view to our layout file
     * R.layout.morse_code. We locate the `Button` in our layout with ID R.id.button ("Vibrate")
     * and set its `OnClickListener` to our field `OnClickListener mClickListener`. Then
     * we locate the `EditText` in our layout file with ID R.id.text and save a reference to it
     * in our field `TextView mTextView`.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState)

        // See assets/res/any/layout/morse_code.xml for this
        // view layout definition, which is being set here as
        // the content of our screen.
        setContentView(R.layout.morse_code)

        // Set the OnClickListener for the button so we see when it's pressed.
        findViewById<View>(R.id.button).setOnClickListener(mClickListener)

        // Save the text view so we don't have to look it up each time
        mTextView = findViewById(R.id.text)
    }

    /**
     * Called when the button with ID R.id.button ("Vibrate") is pushed
     */
    /**
     * Called when the button with ID R.id.button ("Vibrate") is pushed. First we retrieve the
     * string that the user has entered in `TextView mTextView` to `String text`.
     * Then we call the method `MorseCodeConverter.pattern` to convert `text` to
     * the `long[] pattern` array representing the Morse code version of the text. We fetch
     * a handle to the system level service VIBRATOR_SERVICE to `Vibrator vibrator` and
     * call its method `vibrate` to vibrate the Morse code in `pattern`.
     *
     *  v the `View` that was clicked.
     */
    var mClickListener: View.OnClickListener = View.OnClickListener {
        // Get the text out of the view
        val text = mTextView!!.text.toString()

        // convert it using the function defined above.  See the docs for
        // android.os.Vibrator for more info about the format of this array
        val pattern = MorseCodeConverter.pattern(text)

        // Start the vibration
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        @Suppress("DEPRECATION")
        vibrator.vibrate(pattern, -1)
    }
}
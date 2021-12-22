/*
 * Copyright (C) 2013 The Android Open Source Project
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

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.text.method.KeyListener
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Simple example of using an UndoManager for editing text in a TextView.
 */
@SuppressLint("SetTextI18n")
class TextUndoActivity : AppCompatActivity() {
    /**
     * ID R.id.default_text in layout file, it is the "TextView with the default Control-Z undo behavior."
     */
    private lateinit var mDefaultText: EditText
    /**
     * ID R.id.length_limit_text in layout file, it is the "TextView with a length limit InputFilter."
     */
    private lateinit var mLengthLimitText: EditText
    /**
     * ID R.id.credit_card_text in the layout file, it is the "Credit card input field with a TextWatcher."
     */
    private lateinit var mCreditCardText: EditText

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to the R.layout.text_undo layout file. We
     * initialize our [EditText] field [mDefaultText] to the [EditText] with ID R.id.default_text,
     * then we set the `OnClickListener`'s of the three Buttons below `mDefaultText`:
     *
     *  * R.id.set_text "SetText" will set the text of [mDefaultText]
     *  * R.id.append_text "Append" will append some text to [mDefaultText]
     *  * R.id.insert_text "Insert" will insert some text into [mDefaultText]
     *
     * We initialize our [EditText] field [mLengthLimitText] to be the R.id.length_limit_text
     * [EditText] and set its filters to an [InputFilter.LengthFilter] that will constrain
     * its length to 4 characters. We initialize our [EditText] field [mCreditCardText] to be the
     * R.id.credit_card_text [EditText], set its [KeyListener] to an instance of [DigitsKeyListener]
     * which accepts only the characters that appear in the [String] constant [CREDIT_CARD_CHARS],
     * and add to it a [TextWatcher] instance created from our class [CreditCardTextWatcher].
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.text_undo)
        mDefaultText = findViewById(R.id.default_text)
        findViewById<View>(R.id.set_text).setOnClickListener { mDefaultText.setText("some text") }
        findViewById<View>(R.id.append_text).setOnClickListener { mDefaultText.append(" append") }
        findViewById<View>(R.id.insert_text).setOnClickListener {
            val editable = mDefaultText.text
            editable.insert(0, "insert ")
        }
        mLengthLimitText = findViewById(R.id.length_limit_text)
        mLengthLimitText.filters = arrayOf<InputFilter>(LengthFilter(4))
        mCreditCardText = findViewById(R.id.credit_card_text)
        mCreditCardText.keyListener = DigitsKeyListener.getInstance(CREDIT_CARD_CHARS)
        mCreditCardText.addTextChangedListener(CreditCardTextWatcher())
    }

    /**
     * A simple credit card input formatter that adds spaces every 4 characters.
     */
    private class CreditCardTextWatcher : TextWatcher {
        /**
         * This method is called to notify you that, within our [CharSequence] parameter [s], the
         * [count] characters beginning at [start] are about to be replaced by new text with length
         * [after]. It is an error to attempt to make changes to [s] in this callback. We ignore it.
         *
         * @param s     The [String] that is going to be changed
         * @param start Beginning index of characters to be changed
         * @param count Number of characters to be changed
         * @param after Length of new text that will replace that span of characters
         */
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        /**
         * This method is called to notify you that, within our [CharSequence] parameter [s], the
         * [count] characters beginning at [start] have just replaced old text that had length
         * [before]. It is an error to attempt to make changes to [s] in this callback. We ignore it.
         *
         * @param s      The String that has changed
         * @param start  Beginning index of characters that were changed
         * @param before Length of text that replaced old text
         * @param count  Number of characters that have been replaced in the old text
         */
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

        /**
         * This method is called to notify you that, somewhere within our [CharSequence] parameter
         * [s], the text has been changed. It is legitimate to make further changes to [s] in this
         * callback, but be careful not to get yourself into an infinite loop, because any changes
         * you make will cause this method to be called again recursively.
         *
         * First we initialize [String] `val original` to the [String] version of our [Editable]
         * parameter [s]. Then we use our method [getNumbers] to extract only the numbers from
         * `original` and pass this [CharSequence] to our method [addSpaces] to place a space
         * character every 4 characters, saving the result in [String] `val formatted`. If `formatted`
         * is not equal to the `original` [String], we replace every character of [s] with the
         * contents of `formatted`.
         *
         * @param s Text which has changed somewhere.
         */
        override fun afterTextChanged(s: Editable) {
            val original = s.toString()
            val formatted = addSpaces(getNumbers(original))
            /**
             * This is an ugly way to avoid infinite recursion, but it's common in app code.
             */
            if (formatted != original) {
                s.replace(0, s.length, formatted)
            }
        }

        /**
         * Our static methods.
         */
        companion object {
            /**
             * Adds spaces every 4 characters. First we create an instance of [StringBuilder] for
             * `val builder`, then stepping 4 characters at a time for the length of [CharSequence]
             * parameter [str] we append the next 4 characters to `builder` followed by a space (so
             * long as there are more than than 4 characters remaining in [str] -- we are careful to
             * not add a space to the end of the String we are building). Finally we return a string
             * representing the data in `builder`.
             *
             * @param str [CharSequence] to have spaces inserted into
             * @return Returns a string with a space added every 4 characters.
             */
            private fun addSpaces(str: CharSequence): String {
                val builder = StringBuilder()
                val len = str.length
                var i = 0
                while (i < len) {
                    if (i + 4 < len) {
                        builder.append(str.subSequence(i, i + 4))
                        builder.append(' ')
                    } else { // Don't put a space after the end.
                        builder.append(str.subSequence(i, len))
                    }
                    i += 4
                }
                return builder.toString()
            }

            /**
             * Returns a [String] consisting only of the digits contained in its input parameter.
             * First we create a [StringBuilder] `val sb` with an initial capacity of 16 characters.
             * Then starting from character 0, for the length of our [CharSequence] input parameter
             * [cc] we fetch [Char] `val c` from the next character in [cc] and if our [isNumber]
             * method determines that it is a digit, we append `c` to `sb`. Finally we return a
             * [String] representing the data in `sb`.
             *
             * @return Returns a string containing only the digits from a character sequence.
             */
            private fun getNumbers(cc: CharSequence): String {
                val sb = StringBuilder(16)
                var i = 0
                val count = cc.length
                while (i < count) {
                    val c = cc[i]
                    if (isNumber(c)) {
                        sb.append(c)
                    }
                    ++i
                }
                return sb.toString()
            }

            /**
             * Returns *true* if the [Char] parameter [c] lies in the range of characters 0-9,
             * *false* otherwise.
             *
             * @param c character to check wheter it is a digit or not
             * @return *true* if the [Char] parameter [c] lies in the range of characters 0-9,
             * *false* otherwise
             */
            private fun isNumber(c: Char): Boolean {
                return c in '0'..'9'
            }
        }
    }

    /**
     * Our static constant.
     */
    companion object {
        /**
         * Characters allowed as input in the credit card field.
         */
        private const val CREDIT_CARD_CHARS = "0123456789 "
    }
}
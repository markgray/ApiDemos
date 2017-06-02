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

package com.example.android.apis.content;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.widget.EditText;

import com.example.android.apis.R;

/**
 * Simple example of using an UndoManager for editing text in a TextView.
 */
@SuppressLint("SetTextI18n")
public class TextUndoActivity extends Activity {
    /**
     * Characters allowed as input in the credit card field.
     */
    private static final String CREDIT_CARD_CHARS = "0123456789 ";

    /**
     * ID R.id.default_text in layout file, it is the "TextView with the default Control-Z undo behavior."
     */
    EditText mDefaultText;
    /**
     * ID R.id.length_limit_text in layout file, it is the "TextView with a length limit InputFilter."
     */
    EditText mLengthLimitText;
    /**
     * ID R.id.credit_card_text in the layout file, it is the "Credit card input field with a TextWatcher."
     */
    EditText mCreditCardText;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to the R.layout.text_undo layout file. We
     * initialize our field {@code EditText mDefaultText} to the {@code EditText} with ID R.id.default_text,
     * then we set the {@code OnClickListener}'s of the three Buttons below {@code mDefaultText}:
     * <ul>
     * <li>R.id.set_text "SetText" will set the text of {@code mDefaultText}</li>
     * <li>R.id.append_text "Append" will append some text to {@code mDefaultText}</li>
     * <li>R.id.insert_text "Insert" will insert some text into {@code mDefaultText}</li>
     * </ul>
     * We initialize our field {@code EditText mLengthLimitText} to be the R.id.length_limit_text
     * {@code EditText} and set its filters to an {@code InputFilter.LengthFilter} that will constrain
     * its length to 4 characters. We initialize our field {@code EditText mCreditCardText} to be the
     * R.id.credit_card_text {@code EditText}, set its {@code KeyListener} to an instance of
     * {@code DigitsKeyListener} which accepts only the characters that appear in the String
     * CREDIT_CARD_CHARS, and add to it a {@code TextWatcher} instance created from our class
     * {@code CreditCardTextWatcher}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text_undo);

        mDefaultText = (EditText) findViewById(R.id.default_text);
        findViewById(R.id.set_text).setOnClickListener(new View.OnClickListener() {
            /**
             * Sets the text of {@code EditText mDefaultText} to "some text"
             *
             * @param v View of the Button that was clicked
             */
            @Override
            public void onClick(View v) {
                mDefaultText.setText("some text");
            }
        });
        findViewById(R.id.append_text).setOnClickListener(new View.OnClickListener() {
            /**
             * Appends the text " append" to {@code EditText mDefaultText}
             *
             * @param v View of the Button that was clicked
             */
            @Override
            public void onClick(View v) {
                mDefaultText.append(" append");
            }
        });
        findViewById(R.id.insert_text).setOnClickListener(new View.OnClickListener() {
            /**
             * Inserts the text "insert " into {@code EditText mDefaultText}
             *
             * @param v View of the Button that was clicked
             */
            @Override
            public void onClick(View v) {
                Editable editable = mDefaultText.getText();
                editable.insert(0, "insert ");
            }
        });

        mLengthLimitText = (EditText) findViewById(R.id.length_limit_text);
        mLengthLimitText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        mCreditCardText = (EditText) findViewById(R.id.credit_card_text);
        mCreditCardText.setKeyListener(DigitsKeyListener.getInstance(CREDIT_CARD_CHARS));
        mCreditCardText.addTextChangedListener(new CreditCardTextWatcher());
    }

    /**
     * A simple credit card input formatter that adds spaces every 4 characters.
     */
    private static class CreditCardTextWatcher implements TextWatcher {
        /**
         * This method is called to notify you that, within <code>s</code>,
         * the <code>count</code> characters beginning at <code>start</code>
         * are about to be replaced by new text with length <code>after</code>.
         * It is an error to attempt to make changes to <code>s</code> from
         * this callback. We ignore it.
         *
         * @param s     The String that is going to be changed
         * @param start Beginning index of characters to be changed
         * @param count Number of characters to be changed
         * @param after Length of new text that will replace that span of characters
         */
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        /**
         * This method is called to notify you that, within <code>s</code>,
         * the <code>count</code> characters beginning at <code>start</code>
         * have just replaced old text that had length <code>before</code>.
         * It is an error to attempt to make changes to <code>s</code> from
         * this callback. We ignore it.
         *
         * @param s      The String that has changed
         * @param start  Beginning index of characters that were changed
         * @param before Length of text that replaced old text
         * @param count  Number of characters that have been replaced in the old text
         */
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        /**
         * This method is called to notify you that, somewhere within
         * <code>s</code>, the text has been changed.
         * It is legitimate to make further changes to <code>s</code> from
         * this callback, but be careful not to get yourself into an infinite
         * loop, because any changes you make will cause this method to be
         * called again recursively.
         * <p>
         * First we set {@code String original} to the String version of {@code Editable s}. Then we
         * use our method {@code getNumbers} to extract only the numbers from {@code original} and
         * pass this {@code CharSequence} to our method {@code addSpaces} to place a space character
         * every 4 characters, saving the result in {@code String formatted}. If {@code formatted} is
         * not equal to the {@code original} String, we replace every character of {@code Editable s}
         * with the contents of {@code formatted}.
         *
         * @param s Text which has changed somewhere.
         */
        @Override
        public void afterTextChanged(Editable s) {
            String original = s.toString();
            String formatted = addSpaces(getNumbers(original));
            // This is an ugly way to avoid infinite recursion, but it's common in app code.
            if (!formatted.equals(original)) {
                s.replace(0, s.length(), formatted);
            }
        }

        /**
         * Adds spaces every 4 characters. First we create an instance of {@code StringBuilder builder},
         * then stepping 4 characters at a time for the length of {@code CharSequence str} we append
         * the next 4 characters to {@code builder} followed by a space (so long as there are more than
         * than 4 characters remaining in {@code str} -- we are careful to not add a space to the end
         * of the String we are building). finally we return a string representing the data in
         * {@code builder}.
         *
         * @param str CharSequence to have spaces inserted into
         * @return Returns a string with a space added every 4 characters.
         */
        private static String addSpaces(CharSequence str) {
            StringBuilder builder = new StringBuilder();
            int len = str.length();
            for (int i = 0; i < len; i += 4) {
                if (i + 4 < len) {
                    builder.append(str.subSequence(i, i + 4));
                    builder.append(' ');
                } else {
                    // Don't put a space after the end.
                    builder.append(str.subSequence(i, len));
                }
            }
            return builder.toString();
        }

        /**
         * Returns a String consisting only of the digits contained in its input parameter. First we
         * create a {@code StringBuilder sb} with an initial capacity of 16 characters. Then starting
         * from character 0, for the length of our input parameter {@code CharSequence cc} we fetch
         * {@code char c} from the next character in {@code cc} and if our method {@code isNumber}
         * determines that it is a digit, we append {@code c} to {@code sb}. Finally we return a
         * string representing the data in {@code StringBuilder sb}.
         *
         * @return Returns a string containing only the digits from a character sequence.
         */
        private static String getNumbers(CharSequence cc) {
            StringBuilder sb = new StringBuilder(16);
            for (int i = 0, count = cc.length(); i < count; ++i) {
                char c = cc.charAt(i);
                if (isNumber(c)) {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        /**
         * Returns true if the {@code char c} lies in the range of characters 0-9, false otherwise.
         *
         * @param c character to check wheter it is a digit or not
         * @return true if the {@code char c} lies in the range of characters 0-9, false otherwise
         */
        private static boolean isNumber(char c) {
            return c >= '0' && c <= '9';
        }

    }
}

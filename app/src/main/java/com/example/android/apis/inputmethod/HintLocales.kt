/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.example.android.apis.inputmethod

import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.widget.EditText
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

/**
 * Provide some [EditText] with specifying
 * [android.view.inputmethod.EditorInfo.hintLocales] so that IME developers can test their
 * IMEs.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
class HintLocales : AppCompatActivity() {
    /**
     * Creates a new instance of [EditText] that is configured to specify the given
     * [LocaleList] to [android.view.inputmethod.EditorInfo.hintLocales] so that
     * developers can locally test how the current input method behaves for the given hint locales.
     *
     *
     * **Note:** [android.view.inputmethod.EditorInfo.hintLocales] is just a hint for
     * the input method. IME developers can decide how to use it.
     *
     *
     * First we initialize `EditText exitText` with a new instance. If our parameter
     * `LocaleList hintLocales` is null we set the text to be displayed when the text of the
     * `exitText` is empty to the string "EditorInfo#hintLocales: (null)", otherwise we set
     * that text to the string "EditorInfo#hintLocales: " concatenated to the String representation
     * of the language tags in `LocaleList hintLocales`. We then call the `setImeHintLocales`
     * method of `exitText` to change the "hint" locales associated with the text view to
     * `hintLocales`, which will be reported to an IME with `EditorInfo.hintLocales`
     * when it has focus. Finally we return `exitText` to the caller.
     *
     * @return A new instance of [EditText], which specifies
     * [android.view.inputmethod.EditorInfo.hintLocales] with the given [LocaleList].
     * @param hintLocales an ordered list of locales to be specified to
     * [android.view.inputmethod.EditorInfo.hintLocales].
     */
    private fun createEditTextWithImeHintLocales(hintLocales: LocaleList?): EditText {
        val exitText = EditText(this)
        if (hintLocales == null) {
            exitText.hint = "EditorInfo#hintLocales: (null)"
        } else {
            exitText.hint = "EditorInfo#hintLocales: " + hintLocales.toLanguageTags()
        }
        // Both null and non-null locale list are supported.
        exitText.imeHintLocales = hintLocales
        return exitText
    }

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`.
     * Then we initialize `LinearLayout layout` with a new instance and set its orientation to
     * VERTICAL. We then proceed to add the views that our method `createEditTextWithImeHintLocales`
     * creates for various `LocaleList` objects created by the `LocaleList.forLanguageTags`
     * method to `layout`.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        // Test EditorInfo#hintLocales = null. This is the default behavior and should be the same
        // to the behavior in Android M and prior.
        layout.addView(createEditTextWithImeHintLocales(null))

        // This gives a hint that the application is confident that the user wants to input text
        // for "en-US" in this text field.  Note that IME developers can decide how to use this
        // hint.
        layout.addView(createEditTextWithImeHintLocales(LocaleList.forLanguageTags("en-US")))

        // Likewise, this gives a hint as a list of locales in the order of likelihood.
        layout.addView(createEditTextWithImeHintLocales(
                LocaleList.forLanguageTags("en-GB,en-US,en")))

        // Being able to support 3-letter language code correctly is really important.
        layout.addView(createEditTextWithImeHintLocales(LocaleList.forLanguageTags("fil-ph")))

        // Likewise, test some more locales.
        layout.addView(createEditTextWithImeHintLocales(LocaleList.forLanguageTags("fr")))
        layout.addView(createEditTextWithImeHintLocales(LocaleList.forLanguageTags("zh_CN")))
        layout.addView(createEditTextWithImeHintLocales(LocaleList.forLanguageTags("ja")))

        // Test more complex BCP 47 language tag.  Here the subtag starts with "x-" is a private-use
        // sub-tags.
        layout.addView(createEditTextWithImeHintLocales(
                LocaleList.forLanguageTags("ryu-Kana-JP-x-android")))
        setContentView(layout)
    }
}
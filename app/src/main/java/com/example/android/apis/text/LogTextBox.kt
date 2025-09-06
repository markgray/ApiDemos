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

import android.content.Context
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.method.MovementMethod
import android.text.method.ScrollingMovementMethod
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 * This is a TextView that is Editable and by default scrollable,
 * like EditText without a cursor.
 *
 * **XML attributes**
 *
 * See
 * android.R.styleable#TextView TextView Attributes,
 * android.R.styleable#View View Attributes
 */
class LogTextBox
/**
 * Perform inflation from XML and apply a class-specific base style from a theme attribute or style
 * resource. This constructor of View allows subclasses to use their own base style when they are
 * inflating.
 *
 * @param context  The [Context] the view is running in, through which it can access the current
 *                 theme, resources, etc.
 * @param attrs    The attributes of the XML tag that is inflating the view.
 * @param defStyle An attribute in the current theme that contains a reference to a style resource
 *                 that supplies default values for the view. Can be 0 to not look for defaults.
 */
@JvmOverloads
constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyle: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context!!, attrs, defStyle) {
    /**
     * Subclasses override this to specify a default movement method. We return an instance of
     * [ScrollingMovementMethod] (A movement method that interprets movement keys by scrolling
     * the text buffer).
     *
     * @return the default [MovementMethod] (Provides cursor positioning, scrolling and text
     * selection functionality in a `TextView`), we return an instance of [ScrollingMovementMethod]
     * (A movement method that interprets movement keys by scrolling the text buffer).
     */
    override fun getDefaultMovementMethod(): MovementMethod {
        return ScrollingMovementMethod.getInstance()
    }

    /**
     * Return the text the `TextView` is displaying. If `setText()` was called with
     * an argument of BufferType.SPANNABLE or BufferType.EDITABLE, you can cast
     * the return value from this method to Spannable or Editable, respectively.
     * We just return the value returned by our super's implementation of `getText`
     * cast to an [Editable].
     *
     * @return The contents of our `TextView`.
     */
    override fun getText(): Editable {
        val textViewString: CharSequence = super.getText()
        return SpannableStringBuilder(textViewString)
    }

    /**
     * Sets the text that this `TextView` is to display and also sets whether it is stored
     * in a styleable/spannable buffer and whether it is editable. We just call our super's
     * implementation with EDITABLE instead of our `BufferType` parameter [type].
     *
     * @param text [CharSequence] to set our text to.
     * @param type `BufferType` one of EDITABLE, NORMAL, or SPANNABLE. We ignore and call our
     *             super's implementation with EDITABLE
     */
    override fun setText(text: CharSequence, type: BufferType) {
        super.setText(text, BufferType.EDITABLE)
    }
}
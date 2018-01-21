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

package com.example.android.apis.text;

import android.content.Context;
import android.text.Editable;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;

/**
 * This is a TextView that is Editable and by default scrollable,
 * like EditText without a cursor.
 * <p>
 * <p>
 * <b>XML attributes</b>
 * <p>
 * See
 * android.R.styleable#TextView TextView Attributes,
 * android.R.styleable#View View Attributes
 */
public class LogTextBox extends android.support.v7.widget.AppCompatTextView {
    /**
     * Unused constructor.
     *
     * @param context {@code Context} to use to access resources
     */
    public LogTextBox(Context context) {
        this(context, null);
    }

    /**
     * Perform inflation from XML. We just call our three argument constructor specifying a default
     * style of android.R.attr.textViewStyle (The Default TextView style)
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public LogTextBox(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    /**
     * Perform inflation from XML and apply a class-specific base style from a
     * theme attribute or style resource. This constructor of View allows
     * subclasses to use their own base style when they are inflating.
     *
     * @param context  The Context the view is running in, through which it can
     *                 access the current theme, resources, etc.
     * @param attrs    The attributes of the XML tag that is inflating the view.
     * @param defStyle An attribute in the current theme that contains a
     *                 reference to a style resource that supplies default values for
     *                 the view. Can be 0 to not look for defaults.
     */
    public LogTextBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Subclasses override this to specify a default movement method. We return an instance of
     * {@code ScrollingMovementMethod} (A movement method that interprets movement keys by scrolling
     * the text buffer).
     *
     * @return the default {@code MovementMethod} (Provides cursor positioning, scrolling and text
     * selection functionality in a TextView), we return an instance of {@code ScrollingMovementMethod}
     * (A movement method that interprets movement keys by scrolling the text buffer).
     */
    @Override
    protected MovementMethod getDefaultMovementMethod() {
        return ScrollingMovementMethod.getInstance();
    }

    /**
     * Return the text the TextView is displaying. If setText() was called with
     * an argument of BufferType.SPANNABLE or BufferType.EDITABLE, you can cast
     * the return value from this method to Spannable or Editable, respectively.
     * We just return the value returned by our super's implementation of {@code getText}
     * cast to an {@code Editable}.
     *
     * @return The contents of our {@code TextView}.
     */
    @Override
    public Editable getText() {
        return (Editable) super.getText();
    }

    /**
     * Sets the text that this TextView is to display and also sets whether it is stored
     * in a styleable/spannable buffer and whether it is editable. We just call our super's
     * implementation with EDITABLE instead of the parameter {@code BufferType type}.
     *
     * @param text {@code CharSequence} to set our text to.
     * @param type {@code BufferType} one of EDITABLE, NORMAL, or SPANNABLE. We ignore and call our
     *             super's implementation with EDITABLE
     */
    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, BufferType.EDITABLE);
    }
}

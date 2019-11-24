/*
 * Copyright (c) 2010, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.apis.content

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ClipboardManager.OnPrimaryClipChangedListener
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Shows how to copy to, and paste from the clipboard using the different conversion methods available
 */
@Suppress("MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
@SuppressLint("SetTextI18n")
class ClipboardSample : AppCompatActivity() {
    /**
     * Handle to the CLIPBOARD_SERVICE system-level service (Interface to the clipboard service,
     * for placing and retrieving text in the global clipboard)
     */
    var mClipboard: ClipboardManager? = null
    /**
     * `Spinner` in our UI with ID R.id.clip_type, it is filled from the string array resource
     * R.array.clip_data_types and allows the user to choose options as to how to deal with the
     * contents of the clipboard.
     */
    var mSpinner: Spinner? = null
    /**
     * Mime types describing the current primary clip on the clipboard
     */
    var mMimeTypes: TextView? = null
    /**
     * Output `TextView` for displaying the contents of the clipboard as coerced by the selection
     * in the `Spinner mSpinner`.
     */
    var mDataText: TextView? = null
    /**
     * `CharSequence` used to hold the styled text resource String R.string.styled_text
     * ("`Plain, <b>bold</b>, <i>italic</i>, <b><i>bold-italic</i></b>`")
     */
    var mStyledText: CharSequence? = null
    /**
     * `String` containing the result of converting `CharSequence mStyledText` to a
     * plain text string using `toString`.
     */
    var mPlainText: String? = null
    /**
     * The constant HTML String `"<b>Link:</b> <a href=\"http://www.android.com\">Android</a>"`
     */
    var mHtmlText: String? = null
    /**
     * The constant String "Link: http://www.android.com"
     */
    var mHtmlPlainText: String? = null
    /**
     * Listener callback that is invoked when the primary clip on the clipboard changes.
     */
    /**
     * Callback that is invoked by [android.content.ClipboardManager] when the primary clip
     * changes. We just call our method `updateClipData(true)`, which retrieves the current
     * primary clip on the clipboard, extracts the mime types of the `ClipData` in order to
     * display them, positions the `Spinner mSpinner` appropriately, and updates the contents
     * of the various `TextView`'s in the UI.
     */
    var mPrimaryChangeListener: OnPrimaryClipChangedListener = OnPrimaryClipChangedListener {
        updateClipData(true)
    }

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.clipboard. Next we
     * initialize our field `ClipboardManager mClipboard` with a handle to the system level
     * CLIPBOARD_SERVICE service. We declare `TextView tv` to use later on when setting the
     * text of the various `TextView`'s in our UI.
     *
     *
     * We initialize our field `CharSequence mStyledText` with the contents of our resource
     * String R.string.styled_text ("`Plain, <b>bold</b>, <i>italic</i>, <b><i>bold-italic</i></b>`"),
     * find the `TextView` with ID R.id.styled_text and set its text to `mStyledText`.
     *
     *
     * We initialize our field `String mPlainText` by converting `CharSequence mStyledText`
     * to a String, find the `TextView` with ID R.id.plain_text and set its text to `mPlainText`.
     *
     *
     * We initialize our field `String mHtmlText` with the constant String
     * "`<b>Link:</b> <a href=\"http://www.android.com\">Android</a>`", and our field
     * `String mHtmlPlainText` with the constant String "Link: http://www.android.com", find
     * the `TextView` with ID R.id.html_text and set its text to `mHtmlText`.
     *
     *
     * We initialize our field `Spinner mSpinner` with the location in the UI of the `Spinner`
     * with ID R.id.clip_type. We create `ArrayAdapter<CharSequence> adapter` using the String
     * array resource R.array.clip_data_types, set its layout resource to create the drop down views
     * to android.R.layout.simple_spinner_dropdown_item, and set `adapter` to be the `SpinnerAdapter`
     * of `Spinner mSpinner`.
     *
     *
     * We set the `OnItemSelectedListener` of `mSpinner` to an anonymous class which calls
     * our method `updateClipData(false)` when a new item is selected in the `Spinner`.
     *
     *
     * We initialize our field `TextView mMimeTypes` with the location of the `TextView`
     * with ID R.id.clip_mime_types, and `TextView mDataText` with the location of the
     * `TextView` with ID R.id.clip_text. We set the `OnPrimaryClipChangedListener`
     * of `ClipboardManager mClipboard` to `OnPrimaryClipChangedListener mPrimaryChangeListener`,
     * and finally call our method `updateClipData(true)` to initialize the contents of the UI.
     *
     * @param savedInstanceState we do not override `onCreateInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.clipboard)
        mClipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        mStyledText = getText(R.string.styled_text)
        var tv: TextView = findViewById(R.id.styled_text)
        tv.text = mStyledText
        mPlainText = mStyledText.toString()
        tv = findViewById(R.id.plain_text)
        tv.text = mPlainText
        mHtmlText = "<b>Link:</b> <a href=\"http://www.android.com\">Android</a>"
        mHtmlPlainText = "Link: http://www.android.com"
        tv = findViewById(R.id.html_text)
        tv.text = mHtmlText
        mSpinner = findViewById(R.id.clip_type)
        val adapter = ArrayAdapter.createFromResource(this,
                R.array.clip_data_types,
                android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mSpinner!!.adapter = adapter
        mSpinner!!.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                updateClipData(false)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        mMimeTypes = findViewById(R.id.clip_mime_types)
        mDataText = findViewById(R.id.clip_text)
        mClipboard!!.addPrimaryClipChangedListener(mPrimaryChangeListener)
        updateClipData(true)
    }

    /**
     * Perform any final cleanup before an activity is destroyed. First we call through to our super's
     * implementation of `onDestroy`, then we remove our `OnPrimaryClipChangedListener`
     * from `ClipboardManager mClipboard`.
     */
    override fun onDestroy() {
        super.onDestroy()
        mClipboard!!.removePrimaryClipChangedListener(mPrimaryChangeListener)
    }

    /**
     * The `Button` with the ID "copy_styled_text" specifies this method to be its
     * `OnClickListener` using the attribute android:onClick="pasteStyledText".
     * It creates a `ClipData` holding data of the type MIMETYPE_TEXT_PLAIN, with
     * the user-visible label for the clip data "Styled Text" out of our field
     * `CharSequence mStyledText`, and sets the current primary clip on the clipboard
     * to it.
     *
     * @param button View of the button that was clicked.
     */
    @Suppress("UNUSED_PARAMETER")
    fun pasteStyledText(button: View?) {
        mClipboard!!.setPrimaryClip(ClipData.newPlainText("Styled Text", mStyledText))
    }

    /**
     * The `Button` with the ID "copy_plain_text" specifies this method to be its
     * `OnClickListener` using the attribute android:onClick="pastePlainText".
     * It creates a `ClipData` holding data of the type MIMETYPE_TEXT_PLAIN, with
     * the user-visible label for the clip data "Styled Text" out of our field
     * `CharSequence mPlainText`, and sets the current primary clip on the clipboard
     * to it.
     *
     * @param button View of the button that was clicked.
     */
    @Suppress("UNUSED_PARAMETER")
    fun pastePlainText(button: View?) {
        mClipboard!!.setPrimaryClip(ClipData.newPlainText("Styled Text", mPlainText))
    }

    /**
     * The `Button` with the ID "copy_html_text" specifies this method to be its
     * `OnClickListener` using the attribute android:onClick="pasteHtmlText".
     * It creates a `ClipData` holding data of the type MIMETYPE_TEXT_HTML, with
     * the user-visible label for the clip data "HTML Text" out of our field
     * `CharSequence mHtmlPlainText` (for the plain text version for receivers that
     * don't handle html) and our field `String mHtmlText` (the actual HTML text in
     * the clip) and sets the current primary clip on the clipboard to it.
     *
     * @param button View of the button that was clicked.
     */
    @Suppress("UNUSED_PARAMETER")
    fun pasteHtmlText(button: View?) {
        mClipboard!!.setPrimaryClip(ClipData.newHtmlText("HTML Text", mHtmlPlainText, mHtmlText))
    }

    /**
     * The `Button` with the ID "copy_intent" specifies this method to be its
     * `OnClickListener` using the attribute android:onClick="pasteIntent".
     * It creates an `Intent intent` with the action ACTION_VIEW and the Intent data uri
     * "http://www.android.com/". It then creates a `ClipData` holding data of the
     * type MIMETYPE_TEXT_INTENT, with the user-visible label for the clip data "VIEW intent",
     * and this `Intent intent` and sets the current primary clip on the clipboard to it.
     *
     * @param button View of the button that was clicked.
     */
    @Suppress("UNUSED_PARAMETER")
    fun pasteIntent(button: View?) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.android.com/"))
        mClipboard!!.setPrimaryClip(ClipData.newIntent("VIEW intent", intent))
    }

    /**
     * The `Button` with the ID "copy_uri" specifies this method to be its
     * `OnClickListener` using the attribute android:onClick="pasteUri".
     * It creates a `ClipData` holding data of the type  MIMETYPE_TEXT_URILIST, with
     * the user-visible label for the clip data "URI" with a `Uri` created from the
     * String "http://www.android.com/", and sets the current primary clip on the clipboard
     * to it.
     *
     * @param button View of the button that was clicked.
     */
    @Suppress("UNUSED_PARAMETER")
    fun pasteUri(button: View?) {
        mClipboard!!.setPrimaryClip(ClipData.newRawUri("URI", Uri.parse("http://www.android.com/")))
    }

    /**
     * Called to update our UI to reflect the current contents of the clipboard and the selection
     * chosen in the `Spinner mSpinner`. It is called from the callback `onPrimaryClipChanged`
     * with `updateType` set to true when the primary clipboard contents changes, from the
     * `onItemSelected` callback of the `Spinner mSpinner` with `updateType` set to
     * false when an item in the Spinner is selected, and from the `onCreate` callback with
     * `updateType` set to true when the activity is first created.
     *
     *
     * First we set `ClipData clip` to the current primary clip on the clipboard. Then if `clip`
     * is not null we set `String[] mimeTypes` to all the mime types in the clip, otherwise we set
     * `mimeTypes` to null. Then if `mimeTypes` is not null we append all the mime types
     * Strings to the `TextView mMimeTypes` (otherwise we set it to the String "NULL".
     *
     *
     * Next we check if `updateType` is true indicating that the caller wants us to update the
     * selection of `Spinner mSpinner` based on the contents of `ClipData clip`. If so, and
     * `clip` is not null, we fetch the first item inside the clip data to `ClipData.Item`
     * and set the selection as follows:
     *
     *  * `getHtmlText` is not null -- 2 "HTML Text clip"
     *  * `getText` is not null -- 1 "Text clip"
     *  * `getIntent` is not null -- 3 "Intent clip"
     *  * `getUri` is not null -- 4 "Uri clip"
     *  * Otherwise we set it to 0 "No data in clipboard"
     *
     * Then if `ClipData clip` is not null we fetch the contents of the first item inside the
     * clip data to `ClipData.Item` and display it in `TextView mDataText` based on the
     * selection of `Spinner mSpinner` as follows:
     *
     *  * 0: No data in clipboard -- "(No data)"
     *  * 1: Text clip -- the value returned by `getText`
     *  * 2: HTML Text clip -- the value returned by `getHtmlText`
     *  * 3: Intent clip -- the value returned by `getIntent` converted to a URI if not null, "(No Intent)" if null
     *  * 4: Uri clip -- the value returned by `getUri` converted to a string if not null, "(No URI)" if null
     *  * 5: Coerce to text -- the value returned by `coerceToText`
     *  * 6: Coerce to styled text -- the value returned by `coerceToStyledText`
     *  * 7: Coerce to HTML text -- the value returned by `coerceToHtmlText`
     *  * Otherwise we display "Unknown option: " with the selection number appended
     *
     * If `clip` is null we display "(NULL clip)".
     *
     *
     * Finally if the device has a keyboard, we set the movement method (arrow key handler) to be
     * used for `TextView mDataText` to an instance of `LinkMovementMethod` (A movement
     * method that traverses links in the text buffer and scrolls if necessary. Supports clicking
     * on links with DPad Center or Enter.)
     *
     * @param updateType if true it will update the selection of `Spinner mSpinner` to point to
     * the type of the current clipboard contents.
     */
    fun updateClipData(updateType: Boolean) {
        val clip = mClipboard!!.primaryClip
        val mimeTypes = clip?.description?.filterMimeTypes("*/*")
        if (mimeTypes != null) {
            mMimeTypes!!.text = ""
            for (i in mimeTypes.indices) {
                if (i > 0) {
                    mMimeTypes!!.append("\n")
                }
                mMimeTypes!!.append(mimeTypes[i])
            }
        } else {
            mMimeTypes!!.text = "NULL"
        }
        if (updateType) {
            if (clip != null) {
                val item = clip.getItemAt(0)
                when {
                    item.htmlText != null -> {
                        mSpinner!!.setSelection(2) // HTML Text clip
                    }
                    item.text != null -> {
                        mSpinner!!.setSelection(1) // Text clip
                    }
                    item.intent != null -> {
                        mSpinner!!.setSelection(3) // Intent clip
                    }
                    item.uri != null -> {
                        mSpinner!!.setSelection(4) // Uri clip
                    }
                    else -> {
                        mSpinner!!.setSelection(0) // No data in clipboard
                    }
                }
            } else {
                mSpinner!!.setSelection(0) // No data in clipboard
            }
        }
        if (clip != null) {
            val item = clip.getItemAt(0)
            when (mSpinner!!.selectedItemPosition) {
                0 -> mDataText!!.text = "(No data)"
                1 -> mDataText!!.text = item.text
                2 -> mDataText!!.text = item.htmlText
                3 -> {
                    val itemIntent = item.intent
                    if (itemIntent != null) {
                        mDataText!!.text = itemIntent.toUri(0)
                    } else {
                        mDataText!!.text = "(No Intent)"
                    }
                }
                4 -> {
                    val itemUri = item.uri
                    if (itemUri != null) {
                        mDataText!!.text = itemUri.toString()
                    } else {
                        mDataText!!.text = "(No URI)"
                    }
                }
                5 -> mDataText!!.text = item.coerceToText(this)
                6 -> mDataText!!.text = item.coerceToStyledText(this)
                7 -> mDataText!!.text = item.coerceToHtmlText(this)
                else -> mDataText!!.text = "Unknown option: " + mSpinner!!.selectedItemPosition
            }
        } else {
            mDataText!!.text = "(NULL clip)"
        }
        mDataText!!.movementMethod = LinkMovementMethod.getInstance()
    }
}
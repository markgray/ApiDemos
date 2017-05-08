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

package com.example.android.apis.content;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.android.apis.R;

/**
 * Shows how to copy to, and paste from the clipboard using the different conversion methods available
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
@SuppressLint("SetTextI18n")
public class ClipboardSample extends Activity {
    /**
     * Handle to the CLIPBOARD_SERVICE system-level service (Interface to the clipboard service,
     * for placing and retrieving text in the global clipboard)
     */
    ClipboardManager mClipboard;

    /**
     * {@code Spinner} in our UI with ID R.id.clip_type, it is filled from the string array resource
     * R.array.clip_data_types and allows the user to choose options as to how to deal with the
     * contents of the clipboard.
     */
    Spinner mSpinner;
    /**
     * Mime types describing the current primary clip on the clipboard
     */
    TextView mMimeTypes;
    /**
     * Output {@code TextView} for displaying the contents of the clipboard as coerced by the selection
     * in the {@code Spinner mSpinner}.
     */
    TextView mDataText;

    /**
     * {@code CharSequence} used to hold the styled text resource String R.string.styled_text
     * ("{@code Plain, <b>bold</b>, <i>italic</i>, <b><i>bold-italic</i></b>}")
     */
    CharSequence mStyledText;
    /**
     * {@code String} containing the result of converting {@code CharSequence mStyledText} to a
     * plain text string using {@code toString}.
     */
    String mPlainText;
    /**
     * The constant HTML String {@code "<b>Link:</b> <a href=\"http://www.android.com\">Android</a>"}
     */
    String mHtmlText;
    /**
     * The constant String "Link: http://www.android.com"
     */
    String mHtmlPlainText;

    /**
     * Listener callback that is invoked when the primary clip on the clipboard changes.
     */
    ClipboardManager.OnPrimaryClipChangedListener mPrimaryChangeListener = new ClipboardManager.OnPrimaryClipChangedListener() {
        /**
         * Callback that is invoked by {@link android.content.ClipboardManager} when the primary clip
         * changes. We just call our method {@code updateClipData(true)}, which retrieves the current
         * primary clip on the clipboard, extracts the mime types of the {@code ClipData} in order to
         * display them, positions the {@code Spinner mSpinner} appropriately, and updates the contents
         * of the various {@code TextView}'s in the UI.
         */
        @Override
        public void onPrimaryClipChanged() {
            updateClipData(true);
        }
    };

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.clipboard. Next we
     * initialize our field {@code ClipboardManager mClipboard} with a handle to the system level
     * CLIPBOARD_SERVICE service. We declare {@code TextView tv} to use later on when setting the
     * text of the various {@code TextView}'s in our UI.
     * <p>
     * We initialize our field {@code CharSequence mStyledText} with the contents of our resource
     * String R.string.styled_text ("{@code Plain, <b>bold</b>, <i>italic</i>, <b><i>bold-italic</i></b>}"),
     * find the {@code TextView} with ID R.id.styled_text and set its text to {@code mStyledText}.
     * <p>
     * We initialize our field {@code String mPlainText} by converting {@code CharSequence mStyledText}
     * to a String, find the {@code TextView} with ID R.id.plain_text and set its text to {@code mPlainText}.
     * <p>
     * We initialize our field {@code String mHtmlText} with the constant String
     * "{@code <b>Link:</b> <a href=\"http://www.android.com\">Android</a>}", and our field
     * {@code String mHtmlPlainText} with the constant String "Link: http://www.android.com", find
     * the {@code TextView} with ID R.id.html_text and set its text to {@code mHtmlText}.
     * <p>
     * We initialize our field {@code Spinner mSpinner} with the location in the UI of the {@code Spinner}
     * with ID R.id.clip_type. We create {@code ArrayAdapter<CharSequence> adapter} using the String
     * array resource R.array.clip_data_types, set its layout resource to create the drop down views
     * to android.R.layout.simple_spinner_dropdown_item, and set {@code adapter} to be the {@code SpinnerAdapter}
     * of {@code Spinner mSpinner}.
     * <p>
     * We set the {@code OnItemSelectedListener} of {@code mSpinner} to an anonymous class which calls
     * our method {@code updateClipData(false)} when a new item is selected in the {@code Spinner}.
     * <p>
     * We initialize our field {@code TextView mMimeTypes} with the location of the {@code TextView}
     * with ID R.id.clip_mime_types, and {@code TextView mDataText} with the location of the
     * {@code TextView} with ID R.id.clip_text. We set the {@code OnPrimaryClipChangedListener}
     * of {@code ClipboardManager mClipboard} to {@code OnPrimaryClipChangedListener mPrimaryChangeListener},
     * and finally call our method {@code updateClipData(true)} to initialize the contents of the UI.
     *
     * @param savedInstanceState we do not override {@code onCreateInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clipboard);

        mClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        TextView tv;

        mStyledText = getText(R.string.styled_text);
        tv = (TextView) findViewById(R.id.styled_text);
        tv.setText(mStyledText);

        mPlainText = mStyledText.toString();
        tv = (TextView) findViewById(R.id.plain_text);
        tv.setText(mPlainText);

        mHtmlText = "<b>Link:</b> <a href=\"http://www.android.com\">Android</a>";
        mHtmlPlainText = "Link: http://www.android.com";
        tv = (TextView) findViewById(R.id.html_text);
        tv.setText(mHtmlText);

        mSpinner = (Spinner) findViewById(R.id.clip_type);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.clip_data_types,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        updateClipData(false);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

        mMimeTypes = (TextView) findViewById(R.id.clip_mime_types);
        mDataText = (TextView) findViewById(R.id.clip_text);

        mClipboard.addPrimaryClipChangedListener(mPrimaryChangeListener);
        updateClipData(true);
    }

    /**
     * Perform any final cleanup before an activity is destroyed. First we call through to our super's
     * implementation of {@code onDestroy}, then we remove our {@code OnPrimaryClipChangedListener}
     * from {@code ClipboardManager mClipboard}.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mClipboard.removePrimaryClipChangedListener(mPrimaryChangeListener);
    }

    /**
     * The {@code Button} with the ID "copy_styled_text" specifies this method to be its
     * {@code OnClickListener} using the attribute android:onClick="pasteStyledText".
     * It creates a {@code ClipData} holding data of the type MIMETYPE_TEXT_PLAIN, with
     * the user-visible label for the clip data "Styled Text" out of our field
     * {@code CharSequence mStyledText}, and sets the current primary clip on the clipboard
     * to it.
     *
     * @param button View of the button that was clicked.
     */
    public void pasteStyledText(View button) {
        mClipboard.setPrimaryClip(ClipData.newPlainText("Styled Text", mStyledText));
    }

    /**
     * The {@code Button} with the ID "copy_plain_text" specifies this method to be its
     * {@code OnClickListener} using the attribute android:onClick="pastePlainText".
     * It creates a {@code ClipData} holding data of the type MIMETYPE_TEXT_PLAIN, with
     * the user-visible label for the clip data "Styled Text" out of our field
     * {@code CharSequence mPlainText}, and sets the current primary clip on the clipboard
     * to it.
     *
     * @param button View of the button that was clicked.
     */
    public void pastePlainText(View button) {
        mClipboard.setPrimaryClip(ClipData.newPlainText("Styled Text", mPlainText));
    }

    /**
     * The {@code Button} with the ID "copy_html_text" specifies this method to be its
     * {@code OnClickListener} using the attribute android:onClick="pasteHtmlText".
     * It creates a {@code ClipData} holding data of the type MIMETYPE_TEXT_HTML, with
     * the user-visible label for the clip data "HTML Text" out of our field
     * {@code CharSequence mHtmlPlainText} (for the plain text version for receivers that
     * don't handle html) and our field {@code String mHtmlText} (the actual HTML text in
     * the clip) and sets the current primary clip on the clipboard to it.
     *
     * @param button View of the button that was clicked.
     */
    public void pasteHtmlText(View button) {
        mClipboard.setPrimaryClip(ClipData.newHtmlText("HTML Text", mHtmlPlainText, mHtmlText));
    }

    /**
     * The {@code Button} with the ID "copy_intent" specifies this method to be its
     * {@code OnClickListener} using the attribute android:onClick="pasteIntent".
     * It creates an {@code Intent intent} with the action ACTION_VIEW and the Intent data uri
     * "http://www.android.com/". It then creates a {@code ClipData} holding data of the
     * type MIMETYPE_TEXT_INTENT, with the user-visible label for the clip data "VIEW intent",
     * and this {@code Intent intent} and sets the current primary clip on the clipboard to it.
     *
     * @param button View of the button that was clicked.
     */
    public void pasteIntent(View button) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.android.com/"));
        mClipboard.setPrimaryClip(ClipData.newIntent("VIEW intent", intent));
    }

    /**
     * The {@code Button} with the ID "copy_uri" specifies this method to be its
     * {@code OnClickListener} using the attribute android:onClick="pasteUri".
     * It creates a {@code ClipData} holding data of the type  MIMETYPE_TEXT_URILIST, with
     * the user-visible label for the clip data "URI" with a {@code Uri} created from the
     * String "http://www.android.com/", and sets the current primary clip on the clipboard
     * to it.
     *
     * @param button View of the button that was clicked.
     */
    public void pasteUri(View button) {
        mClipboard.setPrimaryClip(ClipData.newRawUri("URI", Uri.parse("http://www.android.com/")));
    }

    /**
     * Called to update our UI to reflect the current contents of the clipboard and the selection
     * chosen in the {@code Spinner mSpinner}. It is called from the callback {@code onPrimaryClipChanged}
     * with {@code updateType} set to true when the primary clipboard contents changes, from the
     * {@code onItemSelected} callback of the {@code Spinner mSpinner} with {@code updateType} set to
     * false when an item in the Spinner is selected, and from the {@code onCreate} callback with
     * {@code updateType} set to true when the activity is first created.
     *
     * First we set {@code ClipData clip} to the current primary clip on the clipboard. Then if {@code clip}
     * is not null we set {@code String[] mimeTypes} to all the mime types in the clip, otherwise we set
     * {@code mimeTypes} to null. Then if {@code mimeTypes} is not null we append all the mime types
     * Strings to the {@code TextView mMimeTypes} (otherwise we set it to the String "NULL".
     *
     * @param updateType if true it will update the selection of {@code Spinner mSpinner} to point to
     *                   the type of the current clipboard contents.
     */
    void updateClipData(boolean updateType) {
        ClipData clip = mClipboard.getPrimaryClip();
        String[] mimeTypes = clip != null ? clip.getDescription().filterMimeTypes("*/*") : null;
        if (mimeTypes != null) {
            mMimeTypes.setText("");
            for (int i = 0; i < mimeTypes.length; i++) {
                if (i > 0) {
                    mMimeTypes.append("\n");
                }
                mMimeTypes.append(mimeTypes[i]);
            }
        } else {
            mMimeTypes.setText("NULL");
        }

        if (updateType) {
            if (clip != null) {
                ClipData.Item item = clip.getItemAt(0);
                if (item.getHtmlText() != null) {
                    mSpinner.setSelection(2); // HTML Text clip
                } else if (item.getText() != null) {
                    mSpinner.setSelection(1); // Text clip
                } else if (item.getIntent() != null) {
                    mSpinner.setSelection(3); // Intent clip
                } else if (item.getUri() != null) {
                    mSpinner.setSelection(4); // Uri clip
                } else {
                    mSpinner.setSelection(0); // No data in clipboard
                }
            } else {
                mSpinner.setSelection(0); // No data in clipboard
            }
        }

        if (clip != null) {
            ClipData.Item item = clip.getItemAt(0);
            switch (mSpinner.getSelectedItemPosition()) {
                case 0: // No data in clipboard
                    mDataText.setText("(No data)");
                    break;
                case 1: // Text clip
                    mDataText.setText(item.getText());
                    break;
                case 2: // HTML Text clip
                    mDataText.setText(item.getHtmlText());
                    break;
                case 3: // Intent clip
                    final Intent itemIntent = item.getIntent();
                    if (itemIntent != null) {
                        mDataText.setText(itemIntent.toUri(0));
                    } else {
                        mDataText.setText("(No Intent)");
                    }
                    break;
                case 4: // Uri clip
                    final Uri itemUri = item.getUri();
                    if (itemUri != null) {
                        mDataText.setText(itemUri.toString());
                    } else {
                        mDataText.setText("(No URI)");
                    }
                    break;
                case 5: // Coerce to text
                    mDataText.setText(item.coerceToText(this));
                    break;
                case 6: // Coerce to styled text
                    mDataText.setText(item.coerceToStyledText(this));
                    break;
                case 7: // Coerce to HTML text
                    mDataText.setText(item.coerceToHtmlText(this));
                    break;
                default:
                    mDataText.setText("Unknown option: " + mSpinner.getSelectedItemPosition());
                    break;
            }
        } else {
            mDataText.setText("(NULL clip)");
        }
        mDataText.setMovementMethod(LinkMovementMethod.getInstance());
    }
}

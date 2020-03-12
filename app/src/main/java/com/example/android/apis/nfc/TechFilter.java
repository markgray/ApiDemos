/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.example.android.apis.nfc;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.apis.R;

/**
 * No idea, but nfc related somehow. Having looked at it more closely, it does not look like it does
 * anything at all
 */
@SuppressLint("SetTextI18n")
public class TechFilter extends AppCompatActivity {
    /**
     * {@code TextView} in our layout with ID R.id.text, we use it to display either a count of the
     * number of NFC tags discovered, or the instructions "Scan a tag".
     */
    private TextView mText;
    /**
     * Number of NFC tags discovered
     */
    private int mCount = 0;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.foreground_dispatch.
     * We initialize our field {@code TextView mText} by locating the {@code TextView} with ID
     * R.id.text. Next we fetch the {@code Intent} which launched us to {@code Intent intent}, and
     * the action of {@code intent} to {@code String action}. If {@code action} is ACTION_TECH_DISCOVERED
     * ("android.nfc.action.TECH_DISCOVERED") we set the text of {@code mText} to the string "Discovered
     * tag" with the string value of {@code mCount} concatenated to it (we then post increment {@code mCount}
     * here), with the string "with intent:" concatenated to that, and the string value of {@code intent}
     * concatenated to the end. Otherwise we set the text of {@code mText} to the string "Scan a tag".
     *
     * @param savedState we do not override {@code onSaveInstanceState}, so do not use
     */
    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        setContentView(R.layout.foreground_dispatch);
        mText = findViewById(R.id.text);

        Intent intent = getIntent();
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            mText.setText("Discovered tag " + ++mCount + " with intent: " + intent);
        } else {
            mText.setText("Scan a tag");
        }
    }

    /**
     * This is called for activities that set launchMode to "singleTop" in their package, or if a
     * client used the {@link Intent#FLAG_ACTIVITY_SINGLE_TOP} flag when calling {@link #startActivity}.
     * In either case, when the activity is re-launched while at the top of the activity stack instead
     * of a new instance of the activity being started, onNewIntent() will be called on the existing
     * instance with the Intent that was used to re-launch it.
     * <p>
     * We simply set the text of {@code mText} to the string "Discovered tag" with the string value
     * of {@code mCount} concatenated to it (we then post increment {@code mCount} here), with the
     * string "with intent:" concatenated to that, and the string value of {@code intent} concatenated
     * to the end.
     *
     * @param intent The new intent that was started for the activity.
     */
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mText.setText("Discovered tag " + ++mCount + " with intent: " + intent);
    }
}

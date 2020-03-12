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
package com.example.android.apis.nfc

import android.annotation.SuppressLint
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * No idea, but nfc related somehow. Having looked at it more closely, it does not look like it does
 * anything at all
 */
@SuppressLint("SetTextI18n")
class TechFilter : AppCompatActivity() {
    /**
     * `TextView` in our layout with ID R.id.text, we use it to display either a count of the
     * number of NFC tags discovered, or the instructions "Scan a tag".
     */
    private var mText: TextView? = null

    /**
     * Number of NFC tags discovered
     */
    private var mCount = 0

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.foreground_dispatch.
     * We initialize our field `TextView mText` by locating the `TextView` with ID
     * R.id.text. Next we fetch the `Intent` which launched us to `Intent intent`, and
     * the action of `intent` to `String action`. If `action` is ACTION_TECH_DISCOVERED
     * ("android.nfc.action.TECH_DISCOVERED") we set the text of `mText` to the string "Discovered
     * tag" with the string value of `mCount` concatenated to it (we then post increment `mCount`
     * here), with the string "with intent:" concatenated to that, and the string value of `intent`
     * concatenated to the end. Otherwise we set the text of `mText` to the string "Scan a tag".
     *
     * @param savedState we do not override `onSaveInstanceState`, so do not use
     */
    public override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        setContentView(R.layout.foreground_dispatch)
        mText = findViewById(R.id.text)
        val intent = intent
        val action = intent.action
        if (NfcAdapter.ACTION_TECH_DISCOVERED == action) {
            mText!!.text = "Discovered tag " + ++mCount + " with intent: " + intent
        } else {
            mText!!.text = "Scan a tag"
        }
    }

    /**
     * This is called for activities that set launchMode to "singleTop" in their package, or if a
     * client used the [Intent.FLAG_ACTIVITY_SINGLE_TOP] flag when calling [.startActivity].
     * In either case, when the activity is re-launched while at the top of the activity stack instead
     * of a new instance of the activity being started, onNewIntent() will be called on the existing
     * instance with the Intent that was used to re-launch it.
     *
     *
     * We simply set the text of `mText` to the string "Discovered tag" with the string value
     * of `mCount` concatenated to it (we then post increment `mCount` here), with the
     * string "with intent:" concatenated to that, and the string value of `intent` concatenated
     * to the end.
     *
     * @param intent The new intent that was started for the activity.
     */
    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        mText!!.text = "Discovered tag " + ++mCount + " with intent: " + intent
    }
}
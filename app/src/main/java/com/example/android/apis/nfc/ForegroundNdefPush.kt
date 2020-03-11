/*
 * Copyright (C) 2011 The Android Open Source Project
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
 * limitations under the License
 */
package com.example.android.apis.nfc

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * An example of how to use the NFC foreground NDEF push APIs to push an url to another android device.
 */
@SuppressLint("SetTextI18n")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
class ForegroundNdefPush : AppCompatActivity() {
    /**
     * The default NFC Adapter of our device.
     */
    private var mAdapter: NfcAdapter? = null

    /**
     * `TextView` in our layout file with ID R.id.text, we use it to display the instruction
     * "Tap another Android phone with NFC to push a URL", and if NFC is unavailable the message
     * "This phone is not NFC enabled."
     */
    private var mText: TextView? = null

    /**
     * The `NdefMessage` we create to send to another device, contains an NDEF Record containing
     * the uri "http://www.android.com".
     */
    private var mMessage: NdefMessage? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we initialize our field `NfcAdapter mAdapter` with the default
     * NFC adaptor of our device, initialize `NdefMessage mMessage` with an `NdefMessage`
     * containing the the uri "http://www.android.com", set our content view to our layout file
     * R.layout.foreground_dispatch, and initialize `TextView mText` by locating the TextView
     * in our layout file with ID R.id.text.
     *
     *
     * Finally if `mAdapter` is not null, we set `mMessage` as the static NdefMessage to
     * send using Android Beam, and set the text of `mText` to "Tap another Android phone with
     * NFC to push a URL". If `mAdapter` is null we set the text of `mText` to "This phone
     * is not NFC enabled.".
     *
     * @param savedState we do not override `onSaveInstanceState`, so do not use
     */
    public override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        mAdapter = NfcAdapter.getDefaultAdapter(this)

        // Create an NDEF message a URL
        mMessage = NdefMessage(NdefRecord.createUri("http://www.android.com"))
        setContentView(R.layout.foreground_dispatch)
        mText = findViewById(R.id.text)
        if (mAdapter != null) {
            @Suppress("DEPRECATION")
            mAdapter!!.setNdefPushMessage(mMessage, this)
            mText!!.text = "Tap another Android phone with NFC to push a URL"
        } else {
            mText!!.text = "This phone is not NFC enabled."
        }
    }
}
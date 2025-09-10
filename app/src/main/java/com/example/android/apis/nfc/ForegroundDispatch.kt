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
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentFilter.MalformedMimeTypeException
import android.nfc.NfcAdapter
import android.nfc.tech.NfcF
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * An example of how to use the NFC foreground dispatch APIs. This will intercept any MIME data
 * based NDEF dispatch as well as all dispatched for NfcF tags.
 * RequiresApi(Build.VERSION_CODES.GINGERBREAD_MR1)
 */
@SuppressLint("SetTextI18n")
class ForegroundDispatch : AppCompatActivity() {
    /**
     * Default NFC Adapter
     */
    private var mAdapter: NfcAdapter? = null

    /**
     * [PendingIntent] that will be delivered to this activity. The NFC stack will fill in the
     * intent with the details of the discovered tag before delivering to this activity. This intent
     * will be delivered to our method [onNewIntent].
     */
    private var mPendingIntent: PendingIntent? = null

    /**
     * The [IntentFilter]'s to override dispatching for, one entry for the action ACTION_NDEF_DISCOVERED,
     * with the data type star slash star. We use it in our call to the `enableForegroundDispatch`
     * method of [NfcAdapter] field [mAdapter].
     */
    private lateinit var mFilters: Array<IntentFilter>

    /**
     * The tech lists used to perform matching for dispatching of the ACTION_TECH_DISCOVERED intent
     * of our call to the `enableForegroundDispatch` method of [NfcAdapter] field [mAdapter].
     * In our case it is just the single entry `NfcF.class.getName()` ("android.nfc.tech.NfcF")
     */
    private lateinit var mTechLists: Array<Array<String>>

    /**
     * The [TextView] in our layout file with ID R.id.text, we use it to display both the
     * instructions "Scan a tag" and the result ""Discovered tag" with the count of tags discovered
     * and the string representation of the [Intent] passed to our method [onNewIntent]
     */
    private var mText: TextView? = null

    /**
     * Counter for the number of times our method [onNewIntent] has been called with an Intent
     * reporting a discovered tag.
     */
    private var mCount = 0

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.foreground_dispatch.
     * We initialize our [TextView] field [mText] by locating the [TextView] in our layout with ID
     * R.id.text, and set its text to the string "Scan a tag". We initialize our [NfcAdapter] field
     * [mAdapter] with the default NFC Adapter of our device. We initialize our [PendingIntent] field
     * [mPendingIntent] with a [PendingIntent] which contains an [Intent] to launch this activity with
     * the flag FLAG_ACTIVITY_SINGLE_TOP (so the [Intent] will be delivered to our method [onNewIntent]
     * instead of starting a new instance). The request code of the [PendingIntent] is 0, and the flags
     * are also zero. Then we create [IntentFilter] `val ndef` with the action ACTION_NDEF_DISCOVERED,
     * add the data type "star slash star" (mime type for all types of data), and use it as the single
     * entry in the [IntentFilter] array we create to initialize [IntentFilter] array field [mFilters].
     * We initialize our [String] array field [mTechLists] with an array containing the single entry
     * `NfcF.class.getName()` ("android.nfc.tech.NfcF")
     *
     * @param savedState we do not override [onSaveInstanceState] so do not use.
     */
    public override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        setContentView(R.layout.foreground_dispatch)
        mText = findViewById(R.id.text)
        mText!!.text = "Scan a tag"
        mAdapter = NfcAdapter.getDefaultAdapter(this)

        // Create a generic PendingIntent that will be deliver to this activity. The NFC stack
        // will fill in the intent with the details of the discovered tag before delivering to
        // this activity.
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                mPendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, javaClass)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    PendingIntent.FLAG_MUTABLE
                )
            }

            else -> {
                @SuppressLint("UnspecifiedImmutableFlag")
                mPendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, javaClass)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    0
                )
            }
        }

        // Setup an intent filter for all MIME based dispatches
        val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        try {
            ndef.addDataType("*/*")
        } catch (e: MalformedMimeTypeException) {
            throw RuntimeException("fail", e)
        }
        mFilters = arrayOf(ndef)

        // Setup a tech list for all NfcF tags
        mTechLists = arrayOf(arrayOf(NfcF::class.java.name))
    }

    /**
     * Called after [onRestoreInstanceState], [onRestart], or [onPause], for our activity to start
     * interacting with the user. First we call through to our super's implementation of `onResume`,
     * then if our [NfcAdapter] field [mAdapter] is not null we call its method `enableForegroundDispatch`
     * to Enable foreground dispatch to this activity as specified in the [PendingIntent] field
     * [mPendingIntent], filtering based on the contents of [IntentFilter] array field [mFilters] and
     * also matching the tech lists in our [String] array field [mTechLists].
     */
    public override fun onResume() {
        super.onResume()
        if (mAdapter != null) {
            mAdapter!!.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists)
        }
    }

    /**
     * This is called for activities that set launchMode to "singleTop" in their package, or if a
     * client used the [Intent.FLAG_ACTIVITY_SINGLE_TOP] flag when calling [startActivity]. In
     * either case, when the activity is re-launched while at the top of the activity stack instead
     * of a new instance of the activity being started, [onNewIntent] will be called on the existing
     * instance with the [Intent] that was used to re-launch it.
     *
     * An activity will always be paused before receiving a new intent, so you can count on
     * [onResume] being called after this method.
     *
     * We simply set the text of our [TextView] field [mText] to display the string "Discovered tag",
     * concatenated with the value of our [mCount] field (which we then increment), concatenated with
     * the string "with intent:", concatenated with the string representation of the [Intent] parameter
     * [intent] which was passed to us.
     *
     * @param intent The new [Intent] that was started for the activity.
     */
    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.i("Foreground dispatch", "Discovered tag with intent: $intent")
        mText!!.text = "Discovered tag " + ++mCount + " with intent: " + intent
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. First we call through to our super's implementation of `onPause`,
     * then if our [NfcAdapter] field [mAdapter] is not null we call its `disableForegroundDispatch`
     * method to disable foreground dispatch to us.
     */
    public override fun onPause() {
        super.onPause()
        if (mAdapter != null) {
            mAdapter!!.disableForegroundDispatch(this)
        }
    }
}
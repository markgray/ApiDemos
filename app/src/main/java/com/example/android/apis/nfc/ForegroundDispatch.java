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

package com.example.android.apis.nfc;

import com.example.android.apis.R;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcF;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/**
 * An example of how to use the NFC foreground dispatch APIs. This will intercept any MIME data
 * based NDEF dispatch as well as all dispatched for NfcF tags.
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
@SuppressLint("SetTextI18n")
public class ForegroundDispatch extends Activity {
    /**
     * Default NFC Adapter
     */
    private NfcAdapter mAdapter;
    /**
     * {@code PendingIntent} that will be delivered to this activity. The NFC stack will fill in the
     * intent with the details of the discovered tag before delivering to this activity. This intent
     * will be delivered to our method {@code onNewIntent}.
     */
    private PendingIntent mPendingIntent;
    /**
     * The IntentFilters to override dispatching for, one entry for the action ACTION_NDEF_DISCOVERED,
     * with the data type "&#42;&#47;&#42;". We use it in our call to the {@code enableForegroundDispatch}
     * method of {@code NfcAdapter mAdapter}.
     */
    private IntentFilter[] mFilters;
    /**
     * The tech lists used to perform matching for dispatching of the ACTION_TECH_DISCOVERED intent
     * of our call to the {@code enableForegroundDispatch} method of {@code NfcAdapter mAdapter}.
     * In our case it is just the single entry {@code NfcF.class.getName()} ("android.nfc.tech.NfcF")
     */
    private String[][] mTechLists;
    /**
     * The {@code TextView} in our layout file with ID R.id.text, we use it to display both the
     * instructions "Scan a tag" and the result ""Discovered tag" with the count of tags discovered
     * and the string representation of the {@code Intent} passed to our method {@code onNewIntent}
     */
    private TextView mText;
    /**
     * Counter for the number of times our method {@code onNewIntent} has been called with an Intent
     * reporting a discovered tag.
     */
    private int mCount = 0;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.foreground_dispatch.
     * We initialize our field {@code TextView mText} by locating the {@code TextView} in our layout
     * with ID R.id.text, and set its text to the string "Scan a tag". We initialize our field
     * {@code NfcAdapter mAdapter} with the default NFC Adapter of our device. We initialize our field
     * {@code PendingIntent mPendingIntent} with a {@code PendingIntent} which contains an Intent to
     * launch this activity with the flag FLAG_ACTIVITY_SINGLE_TOP (so the Intent will be delivered
     * to our method {@code onNewIntent} instead of starting a new instance). The request code of the
     * {@code PendingIntent} is 0, and the flags are also zero. Then we create {@code IntentFilter ndef}
     * with the action ACTION_NDEF_DISCOVERED, add the data type "&#42;&#47;&#42;" (mime type for all
     * types of data), and use it as the single entry in the {@code IntentFilter[]} array we create
     * to initialize {@code IntentFilter[] mFilters}. We initialize our field {@code String[][] mTechLists}
     * with an array containing the single entry {@code NfcF.class.getName()} ("android.nfc.tech.NfcF")
     *
     * @param savedState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        setContentView(R.layout.foreground_dispatch);
        mText = (TextView) findViewById(R.id.text);
        mText.setText("Scan a tag");

        mAdapter = NfcAdapter.getDefaultAdapter(this);

        // Create a generic PendingIntent that will be deliver to this activity. The NFC stack
        // will fill in the intent with the details of the discovered tag before delivering to
        // this activity.
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // Setup an intent filter for all MIME based dispatches
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        mFilters = new IntentFilter[] {
                ndef,
        };

        // Setup a tech list for all NfcF tags
        mTechLists = new String[][] { new String[] { NfcF.class.getName() } };
    }

    /**
     * Called after {@link #onRestoreInstanceState}, {@link #onRestart}, or {@link #onPause}, for
     * your activity to start interacting with the user. First we call through to our super's
     * implementation of {@code onResume}, then if our field {@code NfcAdapter mAdapter} is not null
     * we call its method {@code enableForegroundDispatch} to Enable foreground dispatch to this
     * activity as specified in the {@code PendingIntent mPendingIntent}, filtering based on the
     * contents of {@code IntentFilter[] mFilters} and also matching the tech lists in our field
     * {@code String[][] mTechLists}.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null) {
            mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
        }
    }

    /**
     * This is called for activities that set launchMode to "singleTop" in their package, or if a
     * client used the {@link Intent#FLAG_ACTIVITY_SINGLE_TOP} flag when calling {@link #startActivity}.
     * In either case, when the activity is re-launched while at the top of the activity stack instead
     * of a new instance of the activity being started, onNewIntent() will be called on the existing
     * instance with the Intent that was used to re-launch it.
     *
     * An activity will always be paused before receiving a new intent, so you can count on
     * {@link #onResume} being called after this method.
     *
     * We simply we the text of our {@code TextView mText} to display the string "Discovered tag",
     * concatenated with the value of {@code mCount} (which we then increment), concatenated with
     * the string "with intent:", concatenated with the string representation of the {@code Intent intent}
     * which was passed to us.
     *
     * @param intent The new intent that was started for the activity.
     */
    @Override
    public void onNewIntent(Intent intent) {
        Log.i("Foreground dispatch", "Discovered tag with intent: " + intent);
        mText.setText("Discovered tag " + ++mCount + " with intent: " + intent);
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. First we call through to our super's implementation of {@code onPause},
     * then if our field {@code NfcAdapter mAdapter} is not null we call its {@code disableForegroundDispatch}
     * to disable foreground dispatch to us.
     */
    @Override
    public void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
        }
    }
}

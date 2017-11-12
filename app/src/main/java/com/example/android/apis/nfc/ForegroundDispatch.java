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
    private String[][] mTechLists;
    private TextView mText;
    private int mCount = 0;

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

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null) {
            mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
        }
    }

    /**
     * This is called for activities that set launchMode to "singleTop" in
     * their package, or if a client used the {@link Intent#FLAG_ACTIVITY_SINGLE_TOP}
     * flag when calling {@link #startActivity}.  In either case, when the
     * activity is re-launched while at the top of the activity stack instead
     * of a new instance of the activity being started, onNewIntent() will be
     * called on the existing instance with the Intent that was used to
     * re-launch it.
     *
     * <p>An activity will always be paused before receiving a new intent, so
     * you can count on {@link #onResume} being called after this method.
     *
     * <p>Note that {@link #getIntent} still returns the original Intent.  You
     * can use {@link #setIntent} to update it to this new Intent.
     *
     * @param intent The new intent that was started for the activity.
     */
    @Override
    public void onNewIntent(Intent intent) {
        Log.i("Foreground dispatch", "Discovered tag with intent: " + intent);
        mText.setText("Discovered tag " + ++mCount + " with intent: " + intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
        }
    }
}

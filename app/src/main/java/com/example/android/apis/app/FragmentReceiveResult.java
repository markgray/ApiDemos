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
 * limitations under the License.
 */

package com.example.android.apis.app;

import com.example.android.apis.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
/**
 *  FragmentReceiveResult builds a FrameLayout in java -- no xml layout. To this it adds
 *  the Fragment ReceiveResultFragment which uses the layout R.layout.receive_result. Then
 *  it starts the activity SendResult (startActivityForResult) which returns the users
 *  input in an intent setResult(RESULT_OK, (new Intent()).setAction("Corky!")) which
 *  FragmentReceiveResult receives in the callback onActivityResult and then appends
 *  it to the Editable TextView id R.id.results contained in the layout R.layout.receive_result.
 */
public class FragmentReceiveResult extends Activity {
    static final String TAG = "FragmentReceiveResult"; // TAG for logging

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate. We then create a <b>FrameLayout.LayoutParams lp</b> with both width and height set
     * to MATCH_PARENT. We create a <b>FrameLayout frame</b>, set its id to R.id.simple_fragment,
     * and then set our content view to <b>frame</b> using <b>lp</b> for the Layout parameters.
     * If our parameter <b>Bundle savedInstanceState</b> is null this is the first time we are being
     * created so we create and instance of <b>ReceiveResultFragment</b>: <b>Fragment newFragment</b>,
     * begin a <b>FragmentTransaction ft</b>, which we use to add <b>newFragment</b> to the view with
     * id R.id.simple_fragment (the id we have given to our FrameLayout programmatically), and then
     * we commit the <b>FragmentTransaction</b>. If <b>savedInstanceState</b> is not null then we are
     * being recreated after an orientation change and the framework will take care of restoring the
     * Fragment contained in our content view because its view has an id, but that Fragment will need
     * to do something about restoring its own view using onSaveInstanceState
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     *                           down then this Bundle contains the data it most recently supplied
     *                           in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        FrameLayout frame = new FrameLayout(this);
        frame.setId(R.id.simple_fragment);
        setContentView(frame, lp);

        if (savedInstanceState == null) {
            // Do first time initialization -- add fragment. 
            Fragment newFragment = new ReceiveResultFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.simple_fragment, newFragment).commit();
        } else {
            Log.i(TAG, "savedInstanceState is not null");
        }
    }

    /**
     * Fragment that launches the SendResult Activity using startActivityForResult, then retrieves and
     * displays the results returned by SendResult
     */
    public static class ReceiveResultFragment extends Fragment {

        static final private int GET_CODE = 0; // Definition of the one requestCode we use for receiving results.
        private TextView mResults; // TextView we use to write results to (R.id.results)
        private String mLastString = ""; // String saved and restored

        /**
         * OnClickListener used for the Button R.id.get, launches the SendResult Activity for a result.
         */
        private OnClickListener mGetListener = new OnClickListener() {
            /**
             * Called when the Button R.id.get is clicked.  We create an <b>Intent intent</b> designed
             * to start the Activity SendResult, then call startActivityForResult(Intent, int) from
             * the fragment's containing Activity using the request code GET_CODE.
             *
             * @param v View of the Button that was clicked.
             */
            @Override
            public void onClick(View v) {
                // Start the activity whose result we want to retrieve.  The
                // result will come back with request code GET_CODE.
                Intent intent = new Intent(getActivity(), SendResult.class);
                startActivityForResult(intent, GET_CODE);
            }
        };

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState != null) {
                mLastString = savedInstanceState.getString("savedText");
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            mLastString = mResults.getText().toString();
            outState.putString("savedText", mLastString);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.receive_result, container, false);
            
            // Retrieve the TextView widget that will display results.
            mResults = (TextView)v.findViewById(R.id.results);

            // This allows us to later extend the text buffer.
            mResults.setText(mLastString, TextView.BufferType.EDITABLE);

            // Watch for button clicks.
            Button getButton = (Button)v.findViewById(R.id.get);
            getButton.setOnClickListener(mGetListener);
            
            return v;
        }

        /**
         * This method is called when the sending activity has finished, with the
         * result it supplied.
         */
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            // You can use the requestCode to select between multiple child
            // activities you may have started.  Here there is only one thing
            // we launch.
            if (requestCode == GET_CODE) {

                // We will be adding to our text.
                Editable text = (Editable)mResults.getText();

                // This is a standard resultCode that is sent back if the
                // activity doesn't supply an explicit result.  It will also
                // be returned if the activity failed to launch.
                if (resultCode == RESULT_CANCELED) {
                    text.append("(cancelled)");

                // Our protocol with the sending activity is that it will send
                // text in 'data' as its result.
                } else {
                    text.append("(okay ");
                    text.append(Integer.toString(resultCode));
                    text.append(") ");
                    if (data != null) {
                        text.append(data.getAction());
                    }
                }

                text.append("\n");
            }
        }
    }
}

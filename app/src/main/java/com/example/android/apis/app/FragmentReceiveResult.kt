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

package com.example.android.apis.app

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.android.apis.R

/**
 * FragmentReceiveResult builds a FrameLayout in java -- no xml layout. To this it adds
 * the Fragment ReceiveResultFragment which uses the layout R.layout.receive_result. Then
 * it starts the activity SendResult (startActivityForResult) which returns the users
 * input in an intent setResult(RESULT_OK, (new Intent()).setAction("Corky!")) which
 * FragmentReceiveResult receives in the callback onActivityResult and then appends
 * it to the Editable TextView id R.id.results contained in the layout R.layout.receive_result.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class FragmentReceiveResult : FragmentActivity() {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate. We then create a **FrameLayout.LayoutParams lp** with both width and height set
     * to MATCH_PARENT. We create a **FrameLayout frame**, set its id to R.id.simple_fragment,
     * and then set our content view to **frame** using **lp** for the Layout parameters.
     * If our parameter **Bundle savedInstanceState** is null this is the first time we are being
     * created so we create and instance of **ReceiveResultFragment**: **Fragment newFragment**,
     * begin a **FragmentTransaction ft**, which we use to add **newFragment** to the view with
     * id R.id.simple_fragment (the id we have given to our FrameLayout programmatically), and then
     * we commit the **FragmentTransaction**. If **savedInstanceState** is not null then we are
     * being recreated after an orientation change and the framework will take care of restoring the
     * Fragment contained in our content view because its view has an id, but that Fragment will need
     * to do something about restoring its own view using onSaveInstanceState
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     * down then this Bundle contains the data it most recently supplied
     * in [.onSaveInstanceState].  ***Note: Otherwise it is null.***
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lp = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        val frame = FrameLayout(this)
        frame.id = R.id.simple_fragment
        setContentView(frame, lp)

        if (savedInstanceState == null) {
            // Do first time initialization -- add fragment.
            val newFragment = ReceiveResultFragment()
            val ft = supportFragmentManager.beginTransaction()
            ft.add(R.id.simple_fragment, newFragment).commit()
        } else {
            Log.i(TAG, "savedInstanceState is not null")
        }
    }

    /**
     * Fragment that launches the SendResult Activity using startActivityForResult, then retrieves and
     * displays the results returned by SendResult
     */
    class ReceiveResultFragment : Fragment() {
        private var mResults: TextView? = null // TextView we use to write results to (R.id.results)
        private var mLastString: String? = "" // String saved and restored

        /**
         * OnClickListener used for the Button R.id.get, launches the SendResult Activity for a
         * result. We create an **Intent intent** designed to start the Activity SendResult, then
         * call `startActivityForResult(Intent, int)` from the fragment's containing Activity using
         * the request code GET_CODE.
         *
         * Parameter: View of the Button that was clicked.
         */
        private val mGetListener = OnClickListener {
            // Start the activity whose result we want to retrieve.  The
            // result will come back with request code GET_CODE.
            val intent = Intent(activity, SendResult::class.java)
            startActivityForResult(intent, GET_CODE)
        }

        /**
         * Called to do initial creation of a fragment. First we call through to our super's implementation
         * of onCreate, and then if our parameter **Bundle savedInstanceState** is not null we retrieve
         * the value of **String mLastString** that our onSaveInstanceState saved under the key "savedText"
         *
         * @param savedInstanceState if not null we are being recreated after an orientation change
         * so we retrieve the value of **String mLastString** that our
         * onSaveInstanceState saved under the key "savedText"
         */
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            if (savedInstanceState != null) {
                mLastString = savedInstanceState.getString("savedText")
            }
        }

        /**
         * Called to ask the fragment to save its current dynamic state, so it
         * can later be reconstructed in a new instance of its process is
         * restarted.  If a new instance of the fragment later needs to be
         * created, the data you place in the Bundle here will be available
         * in the Bundle given to [.onCreate],
         * [.onCreateView], and
         * [.onActivityCreated].
         *
         *
         * First we call through to our super's implementation of onSaveInstanceState, then we retrieve
         * the text currently being displayed in our output **TextView mResults** to our field
         * **String mLastString** and then we insert this String value into the mapping of  the
         * **Bundle outState**.
         *
         * @param outState Bundle in which to place your saved state.
         */
        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            mLastString = mResults!!.text.toString()
            outState.putString("savedText", mLastString)
        }

        /**
         * Called to have the fragment instantiate its user interface view. First we inflate our
         * layout file R.layout.receive_result into **View v**. Then we initialize our field
         * **TextView mResults** to the TextView in our layout with the id R.id.results, and we
         * set the text of this TextView to our field **String mLastString** with the BufferType
         * EDITABLE so that we can extend the contents later. Next we locate **Button getButton**
         * with id R.id.get and set its OnClickListener to our field **OnClickListener mGetListener**.
         * Finally we return our inflated layout **View v**.
         *
         * @param inflater           The LayoutInflater object that can be used to inflate
         * any views in the fragment,
         * @param container          If non-null, this is the parent view that the fragment's
         * UI should be attached to.  The fragment should not add the view itself,
         * but this can be used to generate the LayoutParams of the view.
         * @param savedInstanceState We do not use in this method.
         * @return Return the View for the fragment's UI, or null.
         */
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val v = inflater.inflate(R.layout.receive_result, container, false)

            // Retrieve the TextView widget that will display results.
            mResults = v.findViewById(R.id.results)

            // This allows us to later extend the text buffer.
            mResults!!.setText(mLastString, TextView.BufferType.EDITABLE)

            // Watch for button clicks.
            val getButton = v.findViewById<Button>(R.id.get)
            getButton.setOnClickListener(mGetListener)

            return v
        }

        /**
         * This method is called when the sending activity has finished, with the
         * result it supplied. First we check to see if the result is for the request
         * we sent (requestCode == GET_CODE), and if not we do nothing. (This is merely
         * a formality which is only necessary when an Activity makes multiple kinds of
         * requests.) If it is the result from a GET_CODE request, we retrieve an Editable
         * reference to the current text in our results **TextView mResults**. If the
         * resultCode == RESULT_CANCELED (the back button was pressed instead of an "answer"
         * Button), we append the String "(cancelled)" to the text being displayed, otherwise
         * we append the String "(okay " followed by the value of **resultCode**, followed
         * by ") ", followed by the value that the **SendResult** Activity returned as the
         * action of the result using Intent.setAction(String) (with the String being either
         * "Corky!" or "Violet!" depending on the Button pressed by the user. (We are careful
         * to check whether the **Intent data** returned to us is not null before calling
         * **getAction** on it, but only an unforeseen bug would cause it to be null in our
         * case.) Finally we append a newline to the end of **text** for both cases of
         * **resultCode** we consider.
         *
         * @param requestCode The integer request code originally supplied to
         * startActivityForResult(), allowing you to identify who this
         * result came from. (Should be GET_CODE in our case.)
         * @param resultCode  The integer result code returned by the child activity
         * through its setResult().
         * @param data        An Intent, which can return result data to the caller
         * (various data can be attached to Intent "extras").
         */
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            // You can use the requestCode to select between multiple child
            // activities you may have started.  Here there is only one thing
            // we launch.
            if (requestCode == GET_CODE) {

                // We will be adding to our text.
                val text = mResults!!.text as Editable

                // This is a standard resultCode that is sent back if the
                // activity doesn't supply an explicit result.  It will also
                // be returned if the activity failed to launch.
                if (resultCode == Activity.RESULT_CANCELED) {
                    text.append("(cancelled)")

                    // Our protocol with the sending activity is that it will send
                    // text in 'data' as its result.
                } else {
                    text.append("(okay ")
                    text.append(resultCode.toString())
                    text.append(") ")
                    if (data != null) {
                        text.append(data.action)
                    }
                }

                text.append("\n")
            }
        }

        companion object {

            private const val GET_CODE = 0 // Definition of the one requestCode we use for receiving results.
        }
    }

    companion object {
        internal const val TAG = "FragmentReceiveResult" // TAG for logging
    }
}

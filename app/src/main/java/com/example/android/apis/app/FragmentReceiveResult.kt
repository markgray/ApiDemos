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
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.android.apis.R
import com.example.android.apis.app.FragmentReceiveResult.ReceiveResultFragment

/**
 * [FragmentReceiveResult] builds a [FrameLayout] in java -- no xml layout. To this it adds
 * a [ReceiveResultFragment] fragment which uses the layout R.layout.receive_result. Then
 * it starts the activity [SendResult] using the method [startActivityForResult] which returns
 * the users input in an intent passed to the [setResult] method which [FragmentReceiveResult]
 * receives in the callback [onActivityResult] and then appends it to the Editable [TextView]
 * with the ID R.id.results contained in the layout R.layout.receive_result.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class FragmentReceiveResult : FragmentActivity() {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. We then create a [FrameLayout.LayoutParams] to initialize our variable `val lp`
     * with both width and height set to MATCH_PARENT. We create a [FrameLayout] to initialize our
     * variable `val frame`, set its id to R.id.simple_fragment, and then set our content view to
     * `frame` using `lp` for the Layout parameters. If our [Bundle] parameter [savedInstanceState]
     * is *null* this is the first time we are being created so we create an instance of
     * [ReceiveResultFragment] to initialize our variable `val newFragment`, and use the support
     * `FragmentManager` to begin a `FragmentTransaction` to initialize our variable `val ft`, which
     * we use to add `newFragment` to the view with the ID R.id.simple_fragment (the ID we have
     * given to our [FrameLayout] programmatically), and then we commit `ft`. If [savedInstanceState]
     * is not *null* then we are being recreated after an orientation change and the framework will
     * will have taken care of restoring the Fragment contained in our content view because its view
     * has an ID, but that Fragment will need to do something about restoring its own view using
     * its override of `onSaveInstanceState`.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     * down then this Bundle contains the data it most recently supplied in [onSaveInstanceState].
     * The framework also saves state in [savedInstanceState] for fragments.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lp = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
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
     * [Fragment] that launches the [SendResult] Activity using [startActivityForResult], then
     * retrieves and displays the results returned by [SendResult]
     */
    class ReceiveResultFragment : Fragment() {
        /**
         * [TextView] we use to write results to (R.id.results)
         */
        private var mResults: TextView? = null

        /**
         * [String] saved and restored
         */
        private var mLastString: String? = ""

        /**
         * [OnClickListener] used for the Button R.id.get, launches the [SendResult] Activity for a
         * result. We create an [Intent] to initialize our variable `val intent` designed to start
         * the Activity [SendResult], then call the [ActivityResultLauncher.launch] method of our
         * field [resultLauncher] to have it launch the activity and process the result in the
         * lambda argument of its call to the [registerForActivityResult] method.
         *
         * Parameter: View of the Button that was clicked.
         */
        private val mGetListener = OnClickListener {
            // Start the activity whose result we want to retrieve.  The
            // result will come back with request code GET_CODE.
            val intent = Intent(activity, SendResult::class.java)
            resultLauncher.launch(intent)
        }

        /**
         * This is the [ActivityResultLauncher] that replaces the use of `startActivityForResult`.
         * It is launched by calling its `launch` method with an [Intent] that is constructed to
         * launch the activity whose result we want, and the lambda argument to the
         * [registerForActivityResult] method replaces the override of [onActivityResult] that
         * would receive the result had the `startActivityForResult` method been used.
         *
         * First we retrieve an [Editable] reference to the current text in our results [TextView]
         * field [mResults] to initialise our variable `val text`. If the `resultCode` property of
         * the [ActivityResult] returned to us in our `result` parameter is RESULT_CANCELED (the back
         * button was pressed instead of an "answer" [Button]), we append the [String] "(cancelled)"
         * to the `text` being displayed, otherwise we append the String "(okay " followed by the
         * value of `result.resultCode`, followed by ") ". We then initialize our [Intent] variable
         * `val data` to the [Intent] stored in the `data` field of `result`, and if this is not
         * `null` we append the value that the [SendResult] Activity returned as the action of the
         * [Intent] returned with the [String] being either "Corky!" or "Violet!" depending on the
         * Button pressed by the user. Finally we append a newline to the end of `text` for both
         * cases of `resultCode` we consider.
         */
        private val resultLauncher: ActivityResultLauncher<Intent> =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                // We will be adding to our text.
                val text = mResults!!.text as Editable

                // This is a standard resultCode that is sent back if the
                // activity doesn't supply an explicit result.  It will also
                // be returned if the activity failed to launch.
                if (result.resultCode == Activity.RESULT_CANCELED) {
                    text.append("(cancelled)")

                    // Our protocol with the sending activity is that it will send
                    // text in 'data' as its result.
                } else {
                    text.append("(okay ")
                    text.append(result.resultCode.toString())
                    text.append(") ")
                    val data: Intent? = result.data
                    if (data != null) {
                        text.append(data.action)
                    }
                }

                text.append("\n")
            }

        /**
         * Called to do initial creation of a fragment. First we call through to our super's
         * implementation of `onCreate`, and then if our [Bundle] parameter [savedInstanceState]
         * is not *null* we retrieve the value of our [String] field [mLastString] that our
         * [onSaveInstanceState] override saved in it under the key "savedText"
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
         * restarted. If a new instance of the fragment later needs to be
         * created, the data you place in the Bundle here will be available
         * in the Bundle given to [onCreate], [onCreateView], and [onActivityCreated].
         *
         * First we call through to our super's implementation of `onSaveInstanceState`, then we
         * retrieve the text currently being displayed in our output [TextView] field [mResults]
         * to reinitialize our [String] field [mLastString] and then we insert this [String] value
         * into the mapping of our [Bundle] parameter [outState] under the key "savedText"
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
         * layout file R.layout.receive_result into our [View] variable `val v`. Then we initialize
         * our [TextView] field [mResults] to the [TextView] in our layout with the ID R.id.results,
         * and we set the text of this [TextView] to our [String] field [mLastString] with the
         * [TextView.BufferType] specified to be [TextView.BufferType.EDITABLE] so that we can
         * extend the contents later. Next we locate the [Button] with id R.id.get to initialize
         * our variable `val getButton` and set its [OnClickListener] to our [OnClickListener] field
         * [mGetListener]. Finally we return our inflated layout contained in `v`.
         *
         * @param inflater The [LayoutInflater] object that can be used to inflate any views
         * @param container If non-null, this is the parent view that the fragment's UI will be
         * attached to. The fragment should not add the view itself, but this can be used to
         * generate the `LayoutParams` of the view.
         * @param savedInstanceState We do not use in this method.
         * @return the [View] for the fragment's UI.
         */
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
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
    }

    /**
     * Our static constant
     */
    companion object {
        /**
         * TAG for logging
         */
        internal const val TAG = "FragmentReceiveResult"
    }
}

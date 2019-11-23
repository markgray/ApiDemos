/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Sample code that invokes the speech recognition intent API.
 */
@SuppressLint("SetTextI18n")
class VoiceRecognition : AppCompatActivity(), View.OnClickListener {
    /**
     * `ListView` used to display the different strings the recognizer thought it could have
     * heard.
     */
    private var mList: ListView? = null
    /**
     * `Handler` running on the UI thread, which other threads can post a `Runnable` to.
     */
    private var mHandler: Handler? = null
    /**
     * `Spinner` in the UI which allows one to choose the language used by the recognizer.
     */
    private var mSupportedLanguageView: Spinner? = null

    /**
     * Called with the activity is first created. First we call through to our super's implementation
     * of `onCreate`, then we initialize our field `Handler mHandler` with a `Handler`
     * which is associated with the Looper for the current (UI) thread for other threads to use for
     * posting Runnable's to. Next we set our content view to our layout file R.layout.voice_recognition,
     * locate the R.id.btn_speak ("Speak") `Button` to set `Button speakButton`, initialize
     * our field `ListView mList` with the location of our output `ListView` R.id.list,
     * and initialize our field `Spinner mSupportedLanguageView` with the location of our
     * selected language `Spinner` R.id.supported_languages.
     *
     *
     * Next we set `PackageManager pm` PackageManager instance to find global package information,
     * and use it to create `List<ResolveInfo> activities` containing all activities that can be
     * performed for the intent RecognizerIntent.ACTION_RECOGNIZE_SPEECH. If there are one or more
     * activities available we set the `OnClickListener` of `Button speakButton` to "this",
     * otherwise we disable that Button and set its text to "Recognizer not present".
     *
     *
     * Finally we call our method `refreshVoiceSettings()` which sends a broadcast intent to
     * retrieve the languages supported by the recognizer which we then use to fill the UI widget
     * `Spinner mSupportedLanguageView` (When the results are returned to the `onReceive`
     * override in `SupportedLanguageBroadcastReceiver`).
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mHandler = Handler()
        // Inflate our UI from its XML layout description.
        setContentView(R.layout.voice_recognition)
        // Get display items for later interaction
        val speakButton = findViewById<Button>(R.id.btn_speak)
        mList = findViewById(R.id.list)
        mSupportedLanguageView = findViewById(R.id.supported_languages)
        // Check to see if a recognition activity is present
        val pm = packageManager
        val activities = pm.queryIntentActivities(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0)
        if (activities.size != 0) {
            speakButton.setOnClickListener(this)
        } else {
            speakButton.isEnabled = false
            speakButton.text = "Recognizer not present"
        }
        // Most of the applications do not have to handle the voice settings. If the application
// does not require a recognition in a specific language (i.e., different from the system
// locale), the application does not need to read the voice settings.
        refreshVoiceSettings()
    }

    /**
     * Handle the click on the start recognition button. If the ID of the View clicked was R.id.btn_speak
     * (Our "Speak!" Button) we call our method `startVoiceRecognitionActivity()`.
     *
     * @param v View of the Button that was clicked
     */
    override fun onClick(v: View) {
        if (v.id == R.id.btn_speak) {
            startVoiceRecognitionActivity()
        }
    }

    /**
     * Fire an intent to start the speech recognition activity. First we create `Intent intent`
     * with the action ACTION_RECOGNIZE_SPEECH, add an extra for EXTRA_CALLING_PACKAGE with our package
     * name, add an extra for EXTRA_PROMPT "Speech recognition demo" (will be shown to the user when
     * requesting him to speak), add an extra for EXTRA_LANGUAGE_MODEL -- LANGUAGE_MODEL_FREE_FORM
     * (Use a language model based on free-form speech recognition), add an extra for EXTRA_MAX_RESULTS
     * "5" (limit on the maximum number of results to return), and if the language selected by the
     * `Spinner mSupportedLanguageView` is not "Default" we add an extra for EXTRA_LANGUAGE using
     * the language we retrieve from `mSupportedLanguageView`
     *
     *
     * Finally we call `startActivityForResult` to launch our `Intent intent` using
     * VOICE_RECOGNITION_REQUEST_CODE as the request code to be returned to `onActivityResult`.
     */
    private fun startVoiceRecognitionActivity() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        // Specify the calling package to identify your application
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, javaClass.getPackage()!!.name)
        // Display an hint to the user about what he should say.
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition demo")
        // Given an hint to the recognizer about what the user is going to say
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        // Specify how many results you want to receive. The results will be sorted
// where the first result is the one with higher confidence.
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        // Specify the recognition language. This parameter has to be specified only if the
// recognition has to be done in a specific language and not the default one (i.e., the
// system locale). Most of the applications do not have to set this parameter.
        if (mSupportedLanguageView!!.selectedItem.toString() != "Default") {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, mSupportedLanguageView!!.selectedItem.toString())
        }
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE)
    }

    /**
     * Handle the results from the recognition activity. First we make sure that the result is for our
     * request code VOICE_RECOGNITION_REQUEST_CODE, and the result code is RESULT_OK (if not we do nothing).
     * If this is our result we then fetch `ArrayList<String> matches` from `Intent data`
     * using the key EXTRA_RESULTS, and set the adapter of `ListView mList` to an `ArrayAdapter`
     * created using `matches`.
     *
     *
     * Finally we call through to our super's implementation of `onActivityResult`.
     *
     * @param requestCode The integer request code originally supplied to
     * startActivityForResult(), allowing you to identify who this
     * result came from.
     * @param resultCode  The integer result code returned by the child activity
     * through its setResult().
     * @param data        An Intent, which can return result data to the caller
     * (various data can be attached to Intent "extras").
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == Activity.RESULT_OK) { // Fill the list view with the strings the recognizer thought it could have heard
            val matches = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            mList!!.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, matches!!)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Sends a broadcast `Intent` asking to receive details from the package that implements
     * voice search using an instance of `SupportedLanguageBroadcastReceiver` as the
     * `BroadcastReceiver` to receive the results of the broadcast.
     */
    private fun refreshVoiceSettings() {
        Log.i(TAG, "Sending broadcast")
        sendOrderedBroadcast(
                RecognizerIntent.getVoiceDetailsIntent(this),
                null,
                SupportedLanguageBroadcastReceiver(),
                null,
                Activity.RESULT_OK,
                null,
                null
        )
    }

    /**
     * Called from our `SupportedLanguageBroadcastReceiver` override of `onReceive` when
     * the extra Bundle in the broadcast returned from `getResultExtras(false)` contains a value
     * stored under the key EXTRA_SUPPORTED_LANGUAGES. We first add the String "Default" to the
     * beginning of our parameter `List<String> languages` to simulate the default language,
     * then we create `SpinnerAdapter adapter` from an `ArrayAdapter<CharSequence>`
     * constructed using the system layout file android.R.layout.simple_spinner_item for the TextView
     * to use when instantiating views, and an array of Objects (Strings) created from
     * `List<String> languages` to represent in the adapter. Finally we set the adapter of
     * `Spinner mSupportedLanguageView` to `SpinnerAdapter adapter`.
     *
     * @param languages the string array list that is stored under the key EXTRA_SUPPORTED_LANGUAGES
     * in the result extra Bundle returned to `SupportedLanguageBroadcastReceiver`
     * from the speech recognizer activity, (The key to the extra in the Bundle
     * returned by ACTION_GET_LANGUAGE_DETAILS which is an ArrayList of Strings that
     * represents the languages supported by this implementation of voice recognition,
     * a list of strings like "en-US", "cmn-Hans-CN", etc.)
     */
    private fun updateSupportedLanguages(languages: MutableList<String>?) { // We add "Default" at the beginning of the list to simulate default language.
        languages!!.add(0, "Default")
        val adapter: SpinnerAdapter = ArrayAdapter<CharSequence>(this,
                android.R.layout.simple_spinner_item,
                languages.toTypedArray())
        mSupportedLanguageView!!.adapter = adapter
    }

    /**
     * Called from our `SupportedLanguageBroadcastReceiver` override of `onReceive` when
     * the extra Bundle in the broadcast returned from `getResultExtras(false)` contains a value
     * stored under the key EXTRA_LANGUAGE_PREFERENCE. We locate the `TextView textView` with
     * ID R.id.language_preference, and set its text to our parameter `String language`.
     *
     * @param language String stored under the key EXTRA_LANGUAGE_PREFERENCE in the result extra
     * Bundle returned to `SupportedLanguageBroadcastReceiver` from the speech
     * recognizer activity,
     */
    private fun updateLanguagePreference(language: String?) {
        val textView = findViewById<TextView>(R.id.language_preference)
        textView.text = language
    }

    /**
     * Handles the response of the broadcast request about the recognizer supported languages.
     *
     *
     * The receiver is required only if the application wants to do recognition in a specific
     * language.
     */
    private inner class SupportedLanguageBroadcastReceiver : BroadcastReceiver() {
        /**
         * This method is called when the BroadcastReceiver is receiving an Intent broadcast.
         * During this time you can use the other methods of BroadcastReceiver to view/modify
         * the current result values. This method is always called within the main thread of
         * its process, unless you explicitly asked for it to be scheduled on a different thread
         * using `registerReceiver(BroadcastReceiver, IntentFilter, String, android.os.Handler)`.
         *
         *
         * First we retrieve the current result extra data, as set by the previous receiver into
         * `Bundle extra`. Then if the current result code, as set by the previous receiver is
         * not RESULT_OK we post a `Runnable` which will toast the message "Error code:" with
         * the current result code, as set by the previous receiver appended to it. If `extra`
         * is null we post a `Runnable` which will toast the message "No extra". (We probably
         * should return at this point because the next two checks expect a non-null `extra`).
         *
         *
         * Next we check if the recognizer returned data stored in the `Bundle extra` under the
         * key EXTRA_SUPPORTED_LANGUAGES, and if so we post a `Runnable` which calls our method
         * `updateSupportedLanguages` with the `ArrayList<String>` which is stored under
         * that key.
         *
         *
         * If the recognizer returned data stored in the `Bundle extra` under the key
         * EXTRA_LANGUAGE_PREFERENCE, we post a `Runnable` which calls our method
         * `updateLanguagePreference` with the String stored under that key.
         *
         * @param context The Context in which the receiver is running.
         * @param intent  The Intent being received.
         */
        override fun onReceive(context: Context, intent: Intent) {
            Log.i(TAG, "Receiving broadcast $intent")
            val extra = getResultExtras(false)
            if (resultCode != Activity.RESULT_OK) {
                mHandler!!.post { showToast("Error code:$resultCode") }
            }
            if (extra == null) {
                mHandler!!.post { showToast("No extra") }
            }
            if (extra!!.containsKey(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES)) {
                mHandler!!.post {
                    updateSupportedLanguages(
                            extra.getStringArrayList(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES)
                    )
                }
            }
            if (extra.containsKey(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE)) {
                mHandler!!.post {
                    updateLanguagePreference(
                            extra.getString(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE)
                    )
                }
            }
        }

        /**
         * Toasts its argument to the display.
         *
         * @param text `String` to toast
         */
        private fun showToast(text: String) {
            Toast.makeText(this@VoiceRecognition, text, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        /**
         * TAG for logging
         */
        private const val TAG = "VoiceRecognition"
        /**
         * Request code used when the Intent for the `RecognizerIntent.ACTION_RECOGNIZE_SPEECH`
         * Activity is started using `startActivityForResult`, it is returned in the call to
         * the callback `onActivityResult` when the voice recognition completes.
         */
        private const val VOICE_RECOGNITION_REQUEST_CODE = 1234
    }
}
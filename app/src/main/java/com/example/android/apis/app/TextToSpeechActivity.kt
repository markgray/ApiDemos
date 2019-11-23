/*
 * Copyright (C) 2009 The Android Open Source Project
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
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R
import java.util.*

/**
 * Demonstrates text-to-speech (TTS). Please note the following steps:
 *
 *  1. Construct the [TextToSpeech] object.
 *  1. Handle initialization callback in the `onInit` method.
 *  The activity implements [TextToSpeech.OnInitListener] for this purpose.
 *  1. Call [TextToSpeech.speak] to synthesize speech.
 *  1. Shutdown [TextToSpeech] in onDestroy.
 *
 * Documentation:
 * http://developer.android.com/reference/android/speech/tts/package-summary.html
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class TextToSpeechActivity : AppCompatActivity(), OnInitListener {
    /**
     * [TextToSpeech] instance we use to read our text aloud.
     */
    private var mTts: TextToSpeech? = null
    /**
     * R.id.again_button The "Again" [Button] in our layout, says a random phrase when clicked
     */
    private var mAgainButton: Button? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.text_to_speech.
     * We create an instance of [TextToSpeech] for [TextToSpeech] field [mTts] using *this* as
     * the context and *this* as the [TextToSpeech.OnInitListener]. We log the maximum string
     * length supported by the TTS engine.
     *
     * Finally we locate the R.id.again_button ("Again") Button in our layout in order to initialize
     * our [Button] field [mAgainButton], and we set its `OnClickListener` to an a lambda which
     * calls our method [sayHello] when the [Button] is clicked.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.text_to_speech)
        /**
         * Initialize text-to-speech. This is an asynchronous operation. The
         * [TextToSpeech.OnInitListener] (second argument) is called after
         * initialization completes.
         */
        mTts = TextToSpeech(
                this, // For our Context
                this // TextToSpeech.OnInitListener
        )
        Log.i(TAG, "getMaxSpeechInputLength: " + TextToSpeech.getMaxSpeechInputLength())
        /**
         * The button is disabled in the layout. It will be enabled upon
         * initialization of the TTS engine.
         */
        mAgainButton = findViewById(R.id.again_button)
        mAgainButton!!.setOnClickListener { sayHello() }
    }

    /**
     * Perform any final cleanup before our activity is destroyed. If we have allocated an instance
     * for [TextToSpeech] field [mTts], we interrupt the current utterance and discard all other
     * utterances in the queue, then release the resources used by the [TextToSpeech] engine.
     * Finally we call our super's implementation of `onDestroy`.
     */
    public override fun onDestroy() {
        /**
         * Don't forget to shutdown!
         */
        if (mTts != null) {
            mTts!!.stop()
            mTts!!.shutdown()
        }
        super.onDestroy()
    }

    /**
     * Implements [TextToSpeech.OnInitListener]. Called to signal the completion of the [TextToSpeech]
     * engine initialization, it is the only method in the interface [TextToSpeech.OnInitListener].
     * If the [status] parameter is not SUCCESS, we log the message "Could not initialize TextToSpeech".
     * If it is SUCCESS we try to set the text-to-speech language to Locale.US, and if the result of
     * the call is either LANG_MISSING_DATA or LANG_NOT_SUPPORTED we log the error. Otherwise we
     * enable the [Button] field [mAgainButton], and call our method [sayHello] to utter the first
     * random utterance.
     *
     * @param status [TextToSpeech.SUCCESS] or [TextToSpeech.ERROR].
     */
    override fun onInit(status: Int) {
        /**
         * status can be either [TextToSpeech.SUCCESS] or [TextToSpeech.ERROR].
         */
        if (status == TextToSpeech.SUCCESS) {
            /**
             * Set preferred language to US english. Note that a language may not be available,
             * and the result will indicate this.
             */
            val result = mTts!!.setLanguage(Locale.US)
            /**
             * Try this someday for some interesting results:
             * `result = mTts.setLanguage(Locale.FRANCE);`
             */
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                /**
                 * Language data is missing or the language is not supported.
                 */
                Log.e(TAG, "Language is not available.")
            } else {
                /**
                 * Check the documentation for other possible result codes.
                 * For example, the language may be available for the locale,
                 * but not for the specified country and variant.
                 * The TTS engine has been successfully initialized.
                 * Allow the user to press the button for the app to speak again.
                 */
                mAgainButton!!.isEnabled = true
                /**
                 * Greet the user.
                 */
                sayHello()
            }
        } else {
            /**
             * Initialization failed.
             */
            Log.e(TAG, "Could not initialize TextToSpeech.")
        }
    }

    /**
     * Use the [TextToSpeech] engine to speak a random sentence. First we initialize our variable
     * `val helloLength` to the number of Strings in our `String[]` array [HELLOS], and then use it
     * along with our random number generator in [Random] field [RANDOM]` to pick a String from
     * [HELLOS] to initialize [String] variable `val hello`, which we then pass to the `speak`
     * method of [TextToSpeech] field [mTts], using the flag QUEUE_FLUSH to tell it to drop all
     * currently pending entries in it queue, a *null* [Bundle] for the parameters, and `hello` as
     * the unique identifier for this request.
     */
    private fun sayHello() { // Select a random hello.
        val helloLength = HELLOS.size
        val hello = HELLOS[RANDOM.nextInt(helloLength)]
        val dummy: Bundle? = null
        mTts!!.speak(hello,
                TextToSpeech.QUEUE_FLUSH, // Drop all pending entries in the playback queue.
                dummy,
                hello)
    }

    /**
     * Our static constants
     */
    companion object {
        /**
         * TAG for logging
         */
        private const val TAG = "TextToSpeechDemo"
        /**
         * random number generator used to select random phrase to utter
         */
        private val RANDOM = Random()
        /**
         * Text strings used by [sayHello] to `speak`
         */
        private val HELLOS = arrayOf(
                "Hello",
                "Salutations",
                "Greetings",
                "Howdy",
                "What's crack-a-lackin?",
                "That explains the stench!",
                "And God said, Behold, I have given you every herb bearing " +
                "seed, which is upon the face of all the earth, and every tree, " +
                "in the which is the fruit of a tree yielding seed; to you it " +
                "shall be for meat."
        )
    }
}
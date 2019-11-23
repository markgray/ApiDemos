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

package com.example.android.apis.app;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.apis.R;

import java.util.Locale;
import java.util.Random;

/**
 * Demonstrates text-to-speech (TTS). Please note the following steps:
 * <ol>
 * <li>Construct the TextToSpeech object.</li>
 * <li>Handle initialization callback in the onInit method.
 * The activity implements TextToSpeech.OnInitListener for this purpose.</li>
 * <li>Call TextToSpeech.speak to synthesize speech.</li>
 * <li>Shutdown TextToSpeech in onDestroy.</li>
 * </ol>
 * Documentation:
 * http://developer.android.com/reference/android/speech/tts/package-summary.html
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class TextToSpeechActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    /**
     * TAG for logging
     */
    private static final String TAG = "TextToSpeechDemo";

    /**
     * {@code TextToSpeech} instance we use to read our text aloud.
     */
    private TextToSpeech mTts;
    /**
     * R.id.again_button  Button in our layout, says a random phrase when clicked
     */
    private Button mAgainButton;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.text_to_speech.
     * We create an instance of {@code TextToSpeech} for {@code TextToSpeech mTts} using "this" as
     * the context and "this" as the {@code TextToSpeech.OnInitListener}. We log the maximum string
     * length supported by the TTS engine.
     *
     * Finally we locate the R.id.again_button ("Again") Button in our layout in order to initialize
     * our field {@code Button mAgainButton}, and we set its {@code OnClickListener} to an anonymous
     * class which calls our method {@code sayHello} when the Button is clicked.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text_to_speech);

        // Initialize text-to-speech. This is an asynchronous operation.
        // The OnInitListener (second argument) is called after initialization completes.
        mTts = new TextToSpeech(this,
            this  // TextToSpeech.OnInitListener
            );

        Log.i(TAG, "getMaxSpeechInputLength: " + TextToSpeech.getMaxSpeechInputLength());
        // The button is disabled in the layout.
        // It will be enabled upon initialization of the TTS engine.
        mAgainButton = findViewById(R.id.again_button);

        mAgainButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sayHello();
            }
        });
    }

    /**
     * Perform any final cleanup before our activity is destroyed. If we have allocated an instance
     * for {@code TextToSpeech mTts}, we interrupt the current utterance and discard other utterances
     * in the queue, then release the resources used by the TextToSpeech engine. Finally we call our
     * super's implementation of {@code onDestroy}.
     */
    @Override
    public void onDestroy() {
        // Don't forget to shutdown!
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }

        super.onDestroy();
    }

    // Implements TextToSpeech.OnInitListener.

    /**
     * Called to signal the completion of the TextToSpeech engine initialization, it is the only
     * method in the interface {@code TextToSpeech.OnInitListener}. If the {@code status} is not
     * SUCCESS, we log the message "Could not initialize TextToSpeech". If it is SUCCESS we try to
     * set the text-to-speech language to Locale.US, and if the result of the call is either
     * LANG_MISSING_DATA or LANG_NOT_SUPPORTED we log the error. Otherwise we enable the
     * {@code Button mAgainButton}, and call our method {@code sayHello()} to utter the first
     * random utterance.
     *
     * @param status {@link TextToSpeech#SUCCESS} or {@link TextToSpeech#ERROR}.
     */
    public void onInit(int status) {
        // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS) {
            // Set preferred language to US english.
            // Note that a language may not be available, and the result will indicate this.
            int result = mTts.setLanguage(Locale.US);
            // Try this someday for some interesting results.
            // int result mTts.setLanguage(Locale.FRANCE);
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
               // Language data is missing or the language is not supported.
                Log.e(TAG, "Language is not available.");
            } else {
                // Check the documentation for other possible result codes.
                // For example, the language may be available for the locale,
                // but not for the specified country and variant.

                // The TTS engine has been successfully initialized.
                // Allow the user to press the button for the app to speak again.
                mAgainButton.setEnabled(true);
                // Greet the user.
                sayHello();
            }
        } else {
            // Initialization failed.
            Log.e(TAG, "Could not initialize TextToSpeech.");
        }
    }

    /**
     * random number generator used to select random phrase to utter
     */
    private static final Random RANDOM = new Random();
    /**
     * Text strings used by {@code sayHello} to {@code speak}
     */
    private static final String[] HELLOS = {
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
    };

    /**
     * Use the TextToSpeech engine to speak a random sentence. First we initialize {@code helloLength}
     * to the number of Strings in our {@code String[] HELLOS}, and then use it along with our random
     * number generator {@code Random RANDOM} to pick a String from {@code HELLOS[]} for
     * {@code String hello}, which we then pass to {@code speak} method of {@code TextToSpeech mTts},
     * using the flag QUEUE_FLUSH to tell it to drop all currently pending entries in it queue.
     *
     */
    private void sayHello() {
        // Select a random hello.
        int helloLength = HELLOS.length;
        String hello = HELLOS[RANDOM.nextInt(helloLength)];
        mTts.speak(hello,
            TextToSpeech.QUEUE_FLUSH,  // Drop all pending entries in the playback queue.
            null);
    }

}

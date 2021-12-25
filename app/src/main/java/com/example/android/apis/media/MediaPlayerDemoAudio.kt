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
package com.example.android.apis.media

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Activity launched by `MediaPlayerDemo` to play an audio file
 */
class MediaPlayerDemoAudio : AppCompatActivity() {
    /**
     * [MediaPlayer] we use to play our audio files, both local files, and raw files read from
     * our resources.
     */
    private var mMediaPlayer: MediaPlayer? = null

    /**
     * local audio file path (an empty string at the moment, so LOCAL_AUDIO does not work)
     */
    private var path: String? = null

    /**
     * [TextView] we use as our content view. Currently displays the string "Playing audio..."
     * if and only if we are used to play the file from our raw resources.
     */
    private var tx: TextView? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we create a new instance of [TextView] to initialize our field [tx], and set
     * our content view to it. Next we fetch a map of the extended data from our intent to set
     * [Bundle] `val extras` and call our method [playAudio] with the data stored under the key
     * MEDIA ("media").
     *
     * @param icicle we do not override [onSaveInstanceState] so do not use
     */
    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        tx = TextView(this)
        setContentView(tx)
        val extras = intent.extras
        playAudio(extras!!.getInt(MEDIA))
    }

    /**
     * Creates and fires up a [MediaPlayer] to play some audio, either LOCAL_AUDIO or
     * RESOURCES_AUDIO depending on our [Int] parameter [media]. Wrapped in a try block
     * intended to catch any Exception we switch based the value of `media`:
     *
     *  * LOCAL_AUDIO - The code needs to be edited to modify the assignment to our field
     *  `String path` to point to a local audio file path or it just toasts a message
     *  stating that it needs to be set to a valid file path. Assuming it has been edited, we
     *  initialize our field `MediaPlayer mMediaPlayer` with a new instance, set its data
     *  source file-path to `path`, calls its `prepare` method (which prepares the
     *  player for playback, synchronously, and blocks until MediaPlayer is ready for playback),
     *  and call its method `start` to resume playing (or stop had it been playing).
     *
     *  * RESOURCES_AUDIO - Initializes our [MediaPlayer] field [mMediaPlayer] with a [MediaPlayer]
     *  created for the resource id R.raw.test_cbr (On success, `prepare()` will already have been
     *  called), and call its method `start` to resume playing (or stop had it been playing).
     *
     * After doing the above, we set the text of [TextView] field [tx] to "Playing audio..."
     *
     * @param media The type of audio we are to play, either [LOCAL_AUDIO] or [RESOURCES_AUDIO]
     */
    @SuppressLint("SetTextI18n")
    private fun playAudio(media: Int) {
        try {
            when (media) {
                LOCAL_AUDIO -> {
                    /*
                     * TODO: Set the path variable to a local audio file path.
                     */
                    path = ""
                    if (path === "") {
                        // Tell the user to provide an audio file URL.
                        Toast.makeText(this@MediaPlayerDemoAudio,
                                "Please edit MediaPlayer_Audio Activity, "
                                        + "and set the path variable to your audio file path."
                                        + " Your audio file must be stored on sdcard.",
                                Toast.LENGTH_LONG).show()
                    }
                    mMediaPlayer = MediaPlayer()
                    mMediaPlayer!!.setDataSource(path)
                    mMediaPlayer!!.prepare()
                    mMediaPlayer!!.start()
                }
                RESOURCES_AUDIO -> {
                    mMediaPlayer = MediaPlayer.create(this, R.raw.test_cbr)
                    mMediaPlayer!!.start()
                }
            }
            tx!!.text = "Playing audio..."
        } catch (e: Exception) {
            Log.e(TAG, "error: " + e.message, e)
        }
    }

    /**
     * Perform any final cleanup before an activity is destroyed. First we call through to our super's
     * implementation of `onDestroy`, then if our [MediaPlayer] field [mMediaPlayer] is not null, we
     * call its `release` method (Releases resources associated with this [MediaPlayer] object) and
     * set [mMediaPlayer] to null.
     */
    override fun onDestroy() {
        super.onDestroy()
        // TODO Auto-generated method stub
        if (mMediaPlayer != null) {
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    companion object {
        /**
         * TAG for logging
         */
        private const val TAG = "MediaPlayerDemo"

        /**
         * The name of the extra data included in the intent used to launch us. The extra data is one
         * of [LOCAL_AUDIO], [STREAM_AUDIO], [RESOURCES_AUDIO], [LOCAL_VIDEO], or [STREAM_VIDEO].
         */
        private const val MEDIA = "media"

        /**
         * "media" extra data which causes us to play a local audio file
         */
        private const val LOCAL_AUDIO = 1

        /**
         * Unused
         */
        @Suppress("unused")
        private const val STREAM_AUDIO = 2

        /**
         * "media" extra data which causes us to play an audio file from our raw resources (the mp3
         * R.raw.test_cbr).
         */
        private const val RESOURCES_AUDIO = 3

        /**
         * Unused by us
         */
        @Suppress("unused")
        private const val LOCAL_VIDEO = 4

        /**
         * Unused by us
         */
        @Suppress("unused")
        private const val STREAM_VIDEO = 5
    }
}
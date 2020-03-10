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

package com.example.android.apis.media;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.apis.R;

/**
 * Activity launched by {@code MediaPlayerDemo} to play a video file
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class MediaPlayerDemo_Audio extends AppCompatActivity {

    /**
     * TAG for logging
     */
    private static final String TAG = "MediaPlayerDemo";

    /**
     * {@code MediaPlayer} we use to play our audio files, both local files, and raw files read from
     * our resources.
     */
    private MediaPlayer mMediaPlayer;
    /**
     * The name of the extra data included in the intent used to launch us. The extra data is one of
     * LOCAL_AUDIO, STREAM_AUDIO, RESOURCES_AUDIO, LOCAL_VIDEO, STREAM_VIDEO, or RESOURCES_VIDEO.
     */
    private static final String MEDIA = "media";
    /**
     * "media" extra data which causes us to play a local audio file
     */
    private static final int LOCAL_AUDIO = 1;
    /**
     * Unused
     */
    private static final int STREAM_AUDIO = 2;
    /**
     * "media" extra data which causes us to play an audio file from our raw resources (the mp3 R.raw.test_cbr).
     */
    private static final int RESOURCES_AUDIO = 3;
    /**
     * Unused by us
     */
    private static final int LOCAL_VIDEO = 4;
    /**
     * Unused by us
     */
    private static final int STREAM_VIDEO = 5;
    /**
     * local audio file path (an empty string at the moment, so LOCAL_AUDIO does not work)
     */
    private String path;

    /**
     * {@code TextView} we use as our content view. Currently displays the string "Playing audio..."
     * if and only if we are used to play the file from our raw resources.
     */
    private TextView tx;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we create a new instance of {@code TextView} to initialize our field
     * {@code TextView tx}, and set our content view to it. Next we fetch a map of the extended data
     * from our intent to set {@code Bundle extras} and call our method {@code playAudio} with the
     * data stored under the key MEDIA ("media").
     *
     * @param icicle we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        tx = new TextView(this);
        setContentView(tx);

        Bundle extras = getIntent().getExtras();
        //noinspection ConstantConditions
        playAudio(extras.getInt(MEDIA));
    }

    /**
     * Creates and fires up a {@code MediaPlayer} to play some audio, either LOCAL_AUDIO or
     * RESOURCES_AUDIO depending on our parameter {@code Integer media}. Wrapped in a try block
     * intended to catch any Exception we switch based the value of {@code media}:
     * <ul>
     *     <li>
     *         LOCAL_AUDIO - The code needs to be edited to modify the assignment to our field
     *         {@code String path} to point to a local audio file path or it just toasts a message
     *         stating that it needs to be set to a valid file path. Assuming it has been edited, we
     *         initialize our field {@code MediaPlayer mMediaPlayer} with a new instance, set its data
     *         source file-path to {@code path}, calls its {@code prepare} method (which prepares the
     *         player for playback, synchronously, and blocks until MediaPlayer is ready for playback),
     *         and call its method {@code start} to resume playing (or stop had it been playing).
     *     </li>
     *     <li>
     *         RESOURCES_AUDIO - Initializes our field {@code MediaPlayer mMediaPlayer} with a
     *         {@code MediaPlayer} created for the resource id R.raw.test_cbr (On success,
     *         {@code prepare()} will already have been called), and call its method {@code start}
     *         to resume playing (or stop had it been playing).
     *     </li>
     * </ul>
     * After doing the above, we set the text of {@code TextView tx} to "Playing audio..."
     *
     * @param media The type of audio we are to play, either LOCAL_AUDIO or RESOURCES_AUDIO
     */
    @SuppressLint("SetTextI18n")
    private void playAudio(Integer media) {
        try {
            switch (media) {
                case LOCAL_AUDIO:
                    /*
                     * TODO: Set the path variable to a local audio file path.
                     */
                    path = "";
                    //noinspection StringEquality
                    if (path == "") {
                        // Tell the user to provide an audio file URL.
                        Toast.makeText(MediaPlayerDemo_Audio.this,
                                        "Please edit MediaPlayer_Audio Activity, "
                                                + "and set the path variable to your audio file path."
                                                + " Your audio file must be stored on sdcard.",
                                        Toast.LENGTH_LONG).show();
                    }
                    mMediaPlayer = new MediaPlayer();
                    mMediaPlayer.setDataSource(path);
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                    break;
                case RESOURCES_AUDIO:
                    mMediaPlayer = MediaPlayer.create(this, R.raw.test_cbr);
                    mMediaPlayer.start();
            }
            tx.setText("Playing audio...");

        } catch (Exception e) {
            Log.e(TAG, "error: " + e.getMessage(), e);
        }

    }

    /**
     * Perform any final cleanup before an activity is destroyed. First we call through to our super's
     * implementation of {@code onDestroy}, then if our field {@code MediaPlayer mMediaPlayer} is not
     * null, we call its {@code release} method (Releases resources associated with this MediaPlayer
     * object) and set {@code mMediaPlayer} to null.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO Auto-generated method stub
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

    }
}

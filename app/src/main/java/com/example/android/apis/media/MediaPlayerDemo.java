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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.apis.R;

/**
 * Shows how to use the MediaPlayer class to control playback of audio/video files and streams.
 * TODO: Set the path variables to a local audio and video file path.
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class MediaPlayerDemo extends AppCompatActivity {
    /**
     * "Play Video from Local File" Button in our layout file (ID R.id.localvideo)
     */
    private Button mlocalvideo;
    /**
     * Unused
     */
    private Button mresourcesvideo;
    /**
     * "Play Streaming Video" Button in our layout file (ID R.id.streamvideo)
     */
    private Button mstreamvideo;
    /**
     * "Play Audio from Local File" Button in our layout file (ID R.id.localaudio)
     */
    private Button mlocalaudio;
    /**
     * "Play Audio from Resources" Button in our layout file (ID R.id.resourcesaudio)
     */
    private Button mresourcesaudio;
    /**
     * Unused
     */
    private Button mstreamaudio;
    /**
     * The name of the extra data included in the intent used to launch both {@code MediaPlayerDemo_Audio}
     * and {@code MediaPlayerDemo_Video}. The extra data is one of LOCAL_AUDIO, STREAM_AUDIO, RESOURCES_AUDIO,
     * LOCAL_VIDEO, STREAM_VIDEO, or RESOURCES_VIDEO.
     */
    private static final String MEDIA = "media";
    /**
     * "media" extra data which causes {@code MediaPlayerDemo_Audio} to play a local audio file
     */
    private static final int LOCAL_AUDIO = 1;
    /**
     * Unused
     */
    private static final int STREAM_AUDIO = 2;
    /**
     * "media" extra data which causes {@code MediaPlayerDemo_Audio} to play an audio file from our
     * raw resources (the mp3 R.raw.test_cbr).
     */
    private static final int RESOURCES_AUDIO = 3;
    /**
     * "media" extra data which causes {@code MediaPlayerDemo_Video} to play a local video file
     */
    private static final int LOCAL_VIDEO = 4;
    /**
     * "media" extra data which causes {@code MediaPlayerDemo_Video} to play a stream-able mp4 or
     * 3gpp format URL
     */
    private static final int STREAM_VIDEO = 5;
    /**
     * Unused
     */
    private static final int RESOURCES_VIDEO = 6;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.mediaplayer_1. We
     * initialize our field {@code Button mlocalaudio} by locating the "Play Audio from Local File"
     * button in our layout file (ID R.id.localaudio), and set its {@code OnClickListener} to
     * {@code mLocalAudioListener}. We initialize our field {@code Button mresourcesaudio} by locating
     * the "Play Audio from Resources" button in our layout file (ID R.id.resourcesaudio), and set its
     * {@code OnClickListener} to {@code mResourcesAudioListener}. We initialize our field
     * {@code Button mlocalvideo} by locating the "Play Video from Local File" button in our layout file
     * (ID R.id.localvideo), and set its {@code OnClickListener} to {@code mLocalVideoListener}. Finally,
     * we initialize our field {@code Button mstreamvideo} by locating the "Play Streaming Video" button
     * in our layout file (ID R.id.streamvideo), and set its {@code OnClickListener} to
     * {@code mStreamVideoListener}.
     *
     * @param icicle we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.mediaplayer_1);

        mlocalaudio = findViewById(R.id.localaudio);
        mlocalaudio.setOnClickListener(mLocalAudioListener);
        mresourcesaudio = findViewById(R.id.resourcesaudio);
        mresourcesaudio.setOnClickListener(mResourcesAudioListener);

        mlocalvideo = findViewById(R.id.localvideo);
        mlocalvideo.setOnClickListener(mLocalVideoListener);
        mstreamvideo = findViewById(R.id.streamvideo);
        mstreamvideo.setOnClickListener(mStreamVideoListener);
    }

    /**
     * {@code OnClickListener} for {@code Button mlocalaudio} "Play Audio from Local File" (ID R.id.localaudio).
     * It creates an intent to launch the activity {@code MediaPlayerDemo_Audio}, adding the extra
     * LOCAL_AUDIO under the key MEDIA ("media"), and starts that activity.
     */
    private OnClickListener mLocalAudioListener = new OnClickListener() {
        /**
         * When clicked we create an intent to launch the activity {@code MediaPlayerDemo_Audio},
         * adding the extra LOCAL_AUDIO under the key MEDIA ("media"), and start that activity.
         *
         * @param v view that has been clicked
         */
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MediaPlayerDemo.this.getApplication(), MediaPlayerDemo_Audio.class);
            intent.putExtra(MEDIA, LOCAL_AUDIO);
            startActivity(intent);
        }
    };
    /**
     * {@code OnClickListener} for {@code Button mresourcesaudio} "Play Audio from Resources" (ID R.id.resourcesaudio)
     * It creates an intent to launch the activity {@code MediaPlayerDemo_Audio}, adding the extra
     * RESOURCES_AUDIO under the key MEDIA ("media"), and starts that activity.
     */
    private OnClickListener mResourcesAudioListener = new OnClickListener() {
        /**
         * When clicked we create an intent to launch the activity {@code MediaPlayerDemo_Audio},
         * adding the extra RESOURCES_AUDIO under the key MEDIA ("media"), and start that activity.
         *
         * @param v view that has been clicked
         */
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MediaPlayerDemo.this.getApplication(), MediaPlayerDemo_Audio.class);
            intent.putExtra(MEDIA, RESOURCES_AUDIO);
            startActivity(intent);
        }
    };
    /**
     * {@code OnClickListener} for {@code Button mlocalvideo} "Play Video from Local File" (ID R.id.localvideo)
     * It creates an intent to launch the activity {@code MediaPlayerDemo_Video}, adding the extra
     * LOCAL_VIDEO under the key MEDIA ("media"), and starts that activity.
     */
    private OnClickListener mLocalVideoListener = new OnClickListener() {
        /**
         * When clicked we create an intent to launch the activity {@code MediaPlayerDemo_Video},
         * adding the extra LOCAL_VIDEO under the key MEDIA ("media"), and start that activity.
         *
         * @param v view that has been clicked
         */
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MediaPlayerDemo.this, MediaPlayerDemo_Video.class);
            intent.putExtra(MEDIA, LOCAL_VIDEO);
            startActivity(intent);
        }
    };
    /**
     * {@code OnClickListener} for {@code Button mstreamvideo} "Play Streaming Video" (ID R.id.streamvideo)
     * It creates an intent to launch the activity {@code MediaPlayerDemo_Video}, adding the extra
     * STREAM_VIDEO under the key MEDIA ("media"), and starts that activity.
     */
    private OnClickListener mStreamVideoListener = new OnClickListener() {
        /**
         * When clicked we create an intent to launch the activity {@code MediaPlayerDemo_Video},
         * adding the extra STREAM_VIDEO under the key MEDIA ("media"), and start that activity.
         *
         * @param v view that has been clicked
         */
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MediaPlayerDemo.this, MediaPlayerDemo_Video.class);
            intent.putExtra(MEDIA, STREAM_VIDEO);
            startActivity(intent);
        }
    };



}

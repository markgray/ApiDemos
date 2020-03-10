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

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Shows how to use the MediaPlayer class to control playback of audio/video files and streams.
 * TODO: Set the path variables to a local audio and video file path.
 */
class MediaPlayerDemo : AppCompatActivity() {
    /**
     * "Play Video from Local File" [Button] in our layout file (ID R.id.localvideo)
     */
    private var mlocalvideo: Button? = null

    /**
     * Unused
     */
    @Suppress("unused")
    private val mresourcesvideo: Button? = null

    /**
     * "Play Streaming Video" [Button] in our layout file (ID R.id.streamvideo)
     */
    private var mstreamvideo: Button? = null

    /**
     * "Play Audio from Local File" [Button] in our layout file (ID R.id.localaudio)
     */
    private var mlocalaudio: Button? = null

    /**
     * "Play Audio from Resources" [Button] in our layout file (ID R.id.resourcesaudio)
     */
    private var mresourcesaudio: Button? = null

    /**
     * Unused
     */
    @Suppress("unused")
    private val mstreamaudio: Button? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.mediaplayer_1. We
     * initialize our [Button] field [mlocalaudio] by locating the "Play Audio from Local File"
     * button in our layout file (ID R.id.localaudio), and set its [View.OnClickListener] to
     * field [mLocalAudioListener]. We initialize our [Button] field [mresourcesaudio] by locating
     * the "Play Audio from Resources" [Button] in our layout file (ID R.id.resourcesaudio), and set
     * its [View.OnClickListener] to field [mResourcesAudioListener]. We initialize our [Button]
     * field [mlocalvideo] by locating the "Play Video from Local File" [Button] in our layout file
     * (ID R.id.localvideo), and set its [View.OnClickListener] to field [mLocalVideoListener].
     * Finally, we initialize our [Button] field [mstreamvideo] by locating the "Play Streaming Video"
     * [Button] in our layout file (ID R.id.streamvideo), and set its [View.OnClickListener] to field
     * [mStreamVideoListener].
     *
     * @param icicle we do not override [onSaveInstanceState] so do not use
     */
    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.mediaplayer_1)
        mlocalaudio = findViewById(R.id.localaudio)
        mlocalaudio!!.setOnClickListener(mLocalAudioListener)
        mresourcesaudio = findViewById(R.id.resourcesaudio)
        mresourcesaudio!!.setOnClickListener(mResourcesAudioListener)
        mlocalvideo = findViewById(R.id.localvideo)
        mlocalvideo!!.setOnClickListener(mLocalVideoListener)
        mstreamvideo = findViewById(R.id.streamvideo)
        mstreamvideo!!.setOnClickListener(mStreamVideoListener)
    }

    /**
     * [View.OnClickListener] for [Button] field [mlocalaudio] "Play Audio from Local File" (ID
     * R.id.localaudio). It creates an intent to launch the activity [MediaPlayerDemo_Audio], adding
     * the extra LOCAL_AUDIO under the key MEDIA ("media"), and starts that activity.
     */
    private val mLocalAudioListener: View.OnClickListener = View.OnClickListener {
        val intent = Intent(this@MediaPlayerDemo.application, MediaPlayerDemo_Audio::class.java)
        intent.putExtra(MEDIA, LOCAL_AUDIO)
        startActivity(intent)
    }

    /**
     * [View.OnClickListener] for [Button] field [mresourcesaudio] "Play Audio from Resources" (ID
     * R.id.resourcesaudio) It creates an intent to launch the activity [MediaPlayerDemo_Audio],
     * adding the extra RESOURCES_AUDIO under the key MEDIA ("media"), and starts that activity.
     */
    private val mResourcesAudioListener: View.OnClickListener = View.OnClickListener {
        val intent = Intent(this@MediaPlayerDemo.application, MediaPlayerDemo_Audio::class.java)
        intent.putExtra(MEDIA, RESOURCES_AUDIO)
        startActivity(intent)
    }

    /**
     * [View.OnClickListener] for [Button] field [mlocalvideo] "Play Video from Local File" (ID
     * R.id.localvideo) It creates an intent to launch the activity [MediaPlayerDemo_Video], adding
     * the extra LOCAL_VIDEO under the key MEDIA ("media"), and starts that activity.
     */
    private val mLocalVideoListener: View.OnClickListener = View.OnClickListener {
        val intent = Intent(this@MediaPlayerDemo, MediaPlayerDemo_Video::class.java)
        intent.putExtra(MEDIA, LOCAL_VIDEO)
        startActivity(intent)
    }

    /**
     * [View.OnClickListener] for [Button] field [mstreamvideo] "Play Streaming Video" (ID
     * R.id.streamvideo) It creates an intent to launch the activity [MediaPlayerDemo_Video],
     * adding the extra STREAM_VIDEO under the key MEDIA ("media"), and starts that activity.
     */
    private val mStreamVideoListener: View.OnClickListener = View.OnClickListener {
        val intent = Intent(this@MediaPlayerDemo, MediaPlayerDemo_Video::class.java)
        intent.putExtra(MEDIA, STREAM_VIDEO)
        startActivity(intent)
    }

    companion object {
        /**
         * The name of the extra data included in the intent used to launch both [MediaPlayerDemo_Audio]
         * and `MediaPlayerDemo_Video`. The extra data is one of [LOCAL_AUDIO], [STREAM_AUDIO],
         * [RESOURCES_AUDIO], [LOCAL_VIDEO], [STREAM_VIDEO], or [RESOURCES_VIDEO].
         */
        private const val MEDIA = "media"

        /**
         * "media" extra data which causes [MediaPlayerDemo_Audio] to play a local audio file
         */
        private const val LOCAL_AUDIO = 1

        /**
         * Unused
         */
        @Suppress("unused")
        private const val STREAM_AUDIO = 2

        /**
         * "media" extra data which causes [MediaPlayerDemo_Audio] to play an audio file from our
         * raw resources (the mp3 R.raw.test_cbr).
         */
        private const val RESOURCES_AUDIO = 3

        /**
         * "media" extra data which causes [MediaPlayerDemo_Video] to play a local video file
         */
        private const val LOCAL_VIDEO = 4

        /**
         * "media" extra data which causes [MediaPlayerDemo_Video] to play a stream-able mp4 or
         * 3gpp format URL
         */
        private const val STREAM_VIDEO = 5

        /**
         * Unused
         */
        @Suppress("unused")
        private const val RESOURCES_VIDEO = 6
    }
}
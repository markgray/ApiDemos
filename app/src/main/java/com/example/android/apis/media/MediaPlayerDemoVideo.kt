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

import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.*
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Activity launched by `MediaPlayerDemo` to play a video file
 */
class MediaPlayerDemoVideo : AppCompatActivity(), OnBufferingUpdateListener, OnCompletionListener, OnPreparedListener, OnVideoSizeChangedListener, SurfaceHolder.Callback {
    /**
     * The width of the video, set by our `onVideoSizeChanged` override, and used to set the
     * surface we are using display our video to a fixed size.
     */
    private var mVideoWidth = 0

    /**
     * The height of the video, set by our `onVideoSizeChanged` override, and used to set the
     * surface we are using display our video to a fixed size.
     */
    private var mVideoHeight = 0

    /**
     * `MediaPlayer` we use to play our video.
     */
    private var mMediaPlayer: MediaPlayer? = null

    /**
     * `SurfaceView` in our layout file (R.id.surface) which we use to display our video file.
     */
    private var mPreview: SurfaceView? = null

    /**
     * `SurfaceHolder` providing access and control over the underlying surface of
     * `SurfaceView mPreview`, which `mMediaPlayer` uses for displaying the video
     * portion of the media.
     */
    private var holder: SurfaceHolder? = null

    /**
     * local or stream video file path or url (an empty string at the moment, so no video is played).
     */
    private var path: String? = null

    /**
     * `Bundle` of data included in the intent that launched us. It contains only an int stored
     * under the key MEDIA ("media"), either LOCAL_VIDEO or STREAM_VIDEO.
     */
    private var extras: Bundle? = null

    /**
     * Flag to indicate that our callback `onVideoSizeChanged` has been called and initialized
     * the width and height of the video stored in `mVideoWidth` and `mVideoHeight`.
     */
    private var mIsVideoSizeKnown = false

    /**
     * Flag to indicate that our `onPrepared` callback has been called (it is called when the
     * media file is ready for playback).
     */
    private var mIsVideoReadyToBePlayed = false

    /**
     * Called when the activity is first created. First we call through to our super's implementation
     * of `onCreate`, then we set our content view to our layout file R.layout.mediaplayer_2.
     * We initialize our field `SurfaceView mPreview` by finding the view in our layout with id
     * R.id.surface. We initialize our field `SurfaceHolder holder` with the `SurfaceHolder`
     * providing access and control over the underlying surface of `mPreview`, add "this" as
     * the `SurfaceHolder.Callback` of `holder`, and set the type of `holder` to
     * SURFACE_TYPE_PUSH_BUFFERS (which is ignored, and set automatically if needed). Finally we set
     * our field `Bundle extras` to a map of the extended data from the intent that launched us.
     *
     * @param icicle we do not override `onSaveInstanceState` so do not use.
     */
    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.mediaplayer_2)
        mPreview = findViewById(R.id.surface)
        holder = mPreview!!.holder
        holder!!.addCallback(this)
        @Suppress("DEPRECATION")
        holder!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        extras = intent.extras
    }

    /**
     * Called from our `surfaceCreated` callback to begin playing the video. First we call our
     * method `doCleanup` to set the width and height used to display the video both to 0, and
     * both `mIsVideoReadyToBePlayed` and `mIsVideoSizeKnown` to false, so that no playback will
     * occur until the `onVideoSizeChanged` callback is called (which sets the dimensions to the
     * current value and sets `mIsVideoSizeKnown` to true, `mIsVideoReadyToBePlayed` is only
     * set to true in our `onPrepared` callback). Then wrapped in a try block intended to catch any
     * exceptions, we switch based on the parameter `Integer Media`:
     *
     *  *
     * LOCAL_VIDEO - needs to be modified to set `String path` to a local media file path
     * before it do can do anything other than toast a message saying that this file needs to be
     * edited.
     *
     *  *
     * STREAM_VIDEO - needs to be modified to set `String path` to an http url for a stream
     * before it do can do anything other than toast a message saying that this file needs to be
     * edited.
     *
     *
     * Once the field `path` has been properly set, we initialize `MediaPlayer mMediaPlayer`
     * with a new instance, set its data source to `path`, set the `SurfaceHolder` for it to
     * use for displaying the video portion of the media to `SurfaceHolder holder`, and then call
     * its method `prepare` to prepare the player for synchronous playback (it will block until
     * ready, then call our callback `onPrepared` before returning). We set its `OnBufferingUpdateListener`,
     * `OnCompletionListener`, `OnPreparedListener`, and `OnVideoSizeChangedListener` to
     * "this", and set its audio stream type to the audio stream for music playback STREAM_MUSIC.
     *
     * @param Media data stored in the extras of the intent that launched us under the key MEDIA
     * ("media"), either LOCAL_VIDEO or STREAM_VIDEO.
     */
    private fun playVideo(Media: Int) {
        doCleanUp()
        try {
            when (Media) {
                LOCAL_VIDEO -> {
                    /*
                     * TODO: Set the path variable to a local media file path.
                     */path = ""
                    if (path === "") {
                        // Tell the user to provide a media file URL.
                        Toast
                                .makeText(
                                        this@MediaPlayerDemoVideo,
                                        "Please edit MediaPlayerDemo_Video Activity, "
                                                + "and set the path variable to your media file path."
                                                + " Your media file must be stored on sdcard.",
                                        Toast.LENGTH_LONG).show()
                    }
                }
                STREAM_VIDEO -> {
                    /*
                     * TODO: Set path variable to progressive stream-able mp4 or
                     * 3gpp format URL. Http protocol should be used.
                     * MediaPlayer can only play "progressive stream-able
                     * contents" which basically means: 1. the movie atom has to
                     * precede all the media data atoms. 2. The clip has to be
                     * reasonably interleaved.
                     *
                     */path = ""
                    if (path === "") {
                        // Tell the user to provide a media file URL.
                        Toast
                                .makeText(
                                        this@MediaPlayerDemoVideo, "Please edit MediaPlayerDemo_Video Activity,"
                                        + " and set the path variable to your media file URL.",
                                        Toast.LENGTH_LONG).show()
                    }
                }
            }

            // Create a new media player and set the listeners
            mMediaPlayer = MediaPlayer()
            mMediaPlayer!!.setDataSource(path)
            mMediaPlayer!!.setDisplay(holder)
            mMediaPlayer!!.prepare()
            mMediaPlayer!!.setOnBufferingUpdateListener(this)
            mMediaPlayer!!.setOnCompletionListener(this)
            mMediaPlayer!!.setOnPreparedListener(this)
            mMediaPlayer!!.setOnVideoSizeChangedListener(this)
            @Suppress("DEPRECATION")
            mMediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        } catch (e: Exception) {
            Log.e(TAG, "error: " + e.message, e)
        }
    }

    /**
     * Called to update status in buffering a media stream received through progressive HTTP download.
     * The received buffering percentage indicates how much of the content has been buffered or played.
     * For example a buffering update of 80 percent when half the content has already been played
     * indicates that the next 30 percent of the content to play has been buffered. We simply log the
     * value of our parameter `percent`.
     *
     * @param arg0    the MediaPlayer the update pertains to
     * @param percent the percentage (0-100) of the content that has been buffered or played thus far
     */
    override fun onBufferingUpdate(arg0: MediaPlayer, percent: Int) {
        Log.d(TAG, "onBufferingUpdate percent:$percent")
    }

    /**
     * Called when the end of a media source is reached during playback. We simply log this fact.
     *
     * @param arg0 the MediaPlayer that reached the end of the file
     */
    override fun onCompletion(arg0: MediaPlayer) {
        Log.d(TAG, "onCompletion called")
    }

    /**
     * Called to indicate the video size. The video size (width and height) could be 0 if there was
     * no video, no display surface was set, or the value was not determined yet.
     *
     *
     * It either of our parameters `width` or `height` is 0, we simply log this error and
     * return. Otherwise we set our flag `mIsVideoSizeKnown` to true, store the new `width`
     * in our field `mVideoWidth`, and the new `height` in `mVideoHeight`. If both
     * `mIsVideoReadyToBePlayed`, and `mIsVideoSizeKnown` are now both true we call our method
     * `startVideoPlayback` (`onPrepared` sets `mIsVideoReadyToBePlayed` to true when
     * it is called, we put this check here because we don't care which of us is called first as long
     * as both have been called).
     *
     * @param mp     the MediaPlayer associated with this callback
     * @param width  the width of the video
     * @param height the height of the video
     */
    override fun onVideoSizeChanged(mp: MediaPlayer, width: Int, height: Int) {
        Log.v(TAG, "onVideoSizeChanged called")
        if (width == 0 || height == 0) {
            Log.e(TAG, "invalid video width($width) or height($height)")
            return
        }
        mIsVideoSizeKnown = true
        mVideoWidth = width
        mVideoHeight = height
        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
            startVideoPlayback()
        }
    }

    /**
     * Called when the media file is ready for playback. We set the flag `mIsVideoReadyToBePlayed`
     * to true, and if both `mIsVideoReadyToBePlayed`, and `mIsVideoSizeKnown` are now both
     * true we call our method `startVideoPlayback` (`onVideoSizeChanged` sets
     * `mIsVideoSizeKnown` to true when it is called, we put this check here because we don't
     * care which of us is called first as long as both have been called).
     *
     * @param mediaplayer the MediaPlayer that is ready for playback
     */
    override fun onPrepared(mediaplayer: MediaPlayer) {
        Log.d(TAG, "onPrepared called")
        mIsVideoReadyToBePlayed = true
        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
            startVideoPlayback()
        }
    }

    /**
     * This is called immediately after any structural changes (format or size) have been made to the
     * surface. We simply log that we were called.
     *
     * @param surfaceholder The SurfaceHolder whose surface has changed.
     * @param i             The new PixelFormat of the surface.
     * @param j             The new width of the surface.
     * @param k             The new height of the surface.
     */
    override fun surfaceChanged(surfaceholder: SurfaceHolder, i: Int, j: Int, k: Int) {
        Log.d(TAG, "surfaceChanged called")
    }

    /**
     * This is called immediately before a surface is being destroyed. We simply log that we were
     * called.
     *
     * @param surfaceholder The SurfaceHolder whose surface is being destroyed.
     */
    override fun surfaceDestroyed(surfaceholder: SurfaceHolder) {
        Log.d(TAG, "surfaceDestroyed called")
    }

    /**
     * This is called immediately after the surface is first created. We simply call our method
     * `playVideo` with the data stored under the key MEDIA ("media") in the `Bundle extras`
     * which was included in the intent which launched this activity (`extras` was retrieved from
     * the intent in our activity's `onCreate` method).
     *
     * @param holder The SurfaceHolder whose surface is being created.
     */
    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceCreated called")
        playVideo(extras!!.getInt(MEDIA))
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. First we call through to our super's implementation of `onPause`,
     * then we call our method `releaseMediaPlayer` which releases resources associated with
     * `MediaPlayer mMediaPlayer`, and sets `mMediaPlayer` to null. Finally we call our
     * method `doCleanUp` which sets both `mVideoWidth` and `mVideoHeight` to 0, and
     * sets the flags `mIsVideoReadyToBePlayed` and `mIsVideoSizeKnown` to false.
     */
    override fun onPause() {
        super.onPause()
        releaseMediaPlayer()
        doCleanUp()
    }

    /**
     * Perform any final cleanup before an activity is destroyed. First we call through to our super's
     * implementation of `onDestroy`, then we call our method `releaseMediaPlayer` which
     * releases resources associated with `MediaPlayer mMediaPlayer`, and sets `mMediaPlayer`
     * to null. Finally we call our method `doCleanUp` which sets both `mVideoWidth` and
     * `mVideoHeight` to 0, and sets the flags `mIsVideoReadyToBePlayed` and `mIsVideoSizeKnown`
     * to false.
     */
    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
        doCleanUp()
    }

    /**
     * If our field `MediaPlayer mMediaPlayer` is not null, we call its method `release`
     * which releases resources associated with it, then we set it to null.
     */
    private fun releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    /**
     * Cleans up by setting both `mVideoWidth` and `mVideoHeight` to 0, and the flags
     * `mIsVideoReadyToBePlayed` and `mIsVideoSizeKnown` to false.
     */
    private fun doCleanUp() {
        mVideoWidth = 0
        mVideoHeight = 0
        mIsVideoReadyToBePlayed = false
        mIsVideoSizeKnown = false
    }

    /**
     * Starts the video playing. First we set the surface controlled by `SurfaceHolder holder`
     * to a fixed size (`mVideoWidth` by `mVideoHeight`), and then we start the playback
     * of `MediaPlayer mMediaPlayer`.
     */
    private fun startVideoPlayback() {
        Log.v(TAG, "startVideoPlayback")
        holder!!.setFixedSize(mVideoWidth, mVideoHeight)
        mMediaPlayer!!.start()
    }

    companion object {
        /**
         * TAG for logging
         */
        private const val TAG = "MediaPlayerDemo"

        /**
         * The name of the extra data included in the intent used to launch us. The extra data is one of
         * LOCAL_AUDIO, STREAM_AUDIO, RESOURCES_AUDIO, LOCAL_VIDEO, STREAM_VIDEO, or RESOURCES_VIDEO.
         */
        private const val MEDIA = "media"

        /**
         * Unused by us
         */
        @Suppress("unused")
        private const val LOCAL_AUDIO = 1

        /**
         * Unused by us
         */
        @Suppress("unused")
        private const val STREAM_AUDIO = 2

        /**
         * Unused by us
         */
        @Suppress("unused")
        private const val RESOURCES_AUDIO = 3

        /**
         * Should play a video file located in the local file system, but since `path` is not set
         * it does nothing.
         */
        private const val LOCAL_VIDEO = 4

        /**
         * Should play a video stream located by a http url, but since `path` is not set
         * it does nothing.
         */
        private const val STREAM_VIDEO = 5
    }
}
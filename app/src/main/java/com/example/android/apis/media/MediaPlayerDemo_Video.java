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

import com.example.android.apis.R;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

/**
 * Activity launched by {@code MediaPlayerDemo} to play a video file
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class MediaPlayerDemo_Video extends Activity implements
        OnBufferingUpdateListener, OnCompletionListener,
        OnPreparedListener, OnVideoSizeChangedListener, SurfaceHolder.Callback {

    /**
     * TAG for logging
     */
    private static final String TAG = "MediaPlayerDemo";
    /**
     * The width of the video, set by our {@code onVideoSizeChanged} override, and used to set the
     * surface we are using display our video to a fixed size.
     */
    private int mVideoWidth;
    /**
     * The height of the video, set by our {@code onVideoSizeChanged} override, and used to set the
     * surface we are using display our video to a fixed size.
     */
    private int mVideoHeight;
    /**
     * {@code MediaPlayer} we use to play our video.
     */
    private MediaPlayer mMediaPlayer;
    /**
     * {@code SurfaceView} in our layout file (R.id.surface) which we use to display our video file.
     */
    private SurfaceView mPreview;
    /**
     * {@code SurfaceHolder} providing access and control over the underlying surface of
     * {@code SurfaceView mPreview}, which {@code mMediaPlayer} uses for displaying the video
     * portion of the media.
     */
    private SurfaceHolder holder;
    /**
     * local or stream video file path or url (an empty string at the moment, so no video is played).
     */
    private String path;
    /**
     * {@code Bundle} of data included in the intent that launched us. It contains only an int stored
     * under the key MEDIA ("media"), either LOCAL_VIDEO or STREAM_VIDEO.
     */
    private Bundle extras;
    /**
     * The name of the extra data included in the intent used to launch us. The extra data is one of
     * LOCAL_AUDIO, STREAM_AUDIO, RESOURCES_AUDIO, LOCAL_VIDEO, STREAM_VIDEO, or RESOURCES_VIDEO.
     */
    private static final String MEDIA = "media";
    /**
     * Unused by us
     */
    private static final int LOCAL_AUDIO = 1;
    /**
     * Unused by us
     */
    private static final int STREAM_AUDIO = 2;
    /**
     * Unused by us
     */
    private static final int RESOURCES_AUDIO = 3;
    /**
     * Should play a video file located in the local file system, but since {@code path} is not set
     * it does nothing.
     */
    private static final int LOCAL_VIDEO = 4;
    /**
     * Should play a video stream located by a http url, but since {@code path} is not set
     * it does nothing.
     */
    private static final int STREAM_VIDEO = 5;
    /**
     * Flag to indicate that our callback {@code onVideoSizeChanged} has been called and initialized
     * the width and height of the video stored in {@code mVideoWidth} and {@code mVideoHeight}.
     */
    private boolean mIsVideoSizeKnown = false;
    /**
     * Flag to indicate that our {@code onPrepared} callback has been called (it is called when the
     * media file is ready for playback).
     */
    private boolean mIsVideoReadyToBePlayed = false;

    /**
     * Called when the activity is first created. First we call through to our super's implementation
     * of {@code onCreate}, then we set our content view to our layout file R.layout.mediaplayer_2.
     * We initialize our field {@code SurfaceView mPreview} by finding the view in our layout with id
     * R.id.surface. We initialize our field {@code SurfaceHolder holder} with the {@code SurfaceHolder}
     * providing access and control over the underlying surface of {@code mPreview}, add "this" as
     * the {@code SurfaceHolder.Callback} of {@code holder}, and set the type of {@code holder} to
     * SURFACE_TYPE_PUSH_BUFFERS (which is ignored, and set automatically if needed). Finally we set
     * our field {@code Bundle extras} to a map of the extended data from the intent that launched us.
     *
     * @param icicle we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.mediaplayer_2);

        mPreview = (SurfaceView) findViewById(R.id.surface);
        holder = mPreview.getHolder();
        holder.addCallback(this);
        //noinspection deprecation
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        extras = getIntent().getExtras();
    }

    /**
     * Called from our {@code surfaceCreated} callback to begin playing the video.
     *
     * @param Media data stored in the extras of the intent that launched us under the key MEDIA
     *              ("media"), either LOCAL_VIDEO or STREAM_VIDEO.
     */
    private void playVideo(Integer Media) {
        doCleanUp();
        try {

            switch (Media) {
                case LOCAL_VIDEO:
                    /*
                     * TODO: Set the path variable to a local media file path.
                     */
                    path = "";
                    //noinspection StringEquality
                    if (path == "") {
                        // Tell the user to provide a media file URL.
                        Toast
                                .makeText(
                                        MediaPlayerDemo_Video.this,
                                        "Please edit MediaPlayerDemo_Video Activity, "
                                                + "and set the path variable to your media file path."
                                                + " Your media file must be stored on sdcard.",
                                        Toast.LENGTH_LONG).show();

                    }
                    break;
                case STREAM_VIDEO:
                    /*
                     * TODO: Set path variable to progressive stream-able mp4 or
                     * 3gpp format URL. Http protocol should be used.
                     * MediaPlayer can only play "progressive stream-able
                     * contents" which basically means: 1. the movie atom has to
                     * precede all the media data atoms. 2. The clip has to be
                     * reasonably interleaved.
                     * 
                     */
                    path = "";
                    //noinspection StringEquality
                    if (path == "") {
                        // Tell the user to provide a media file URL.
                        Toast
                                .makeText(
                                        MediaPlayerDemo_Video.this,
                                        "Please edit MediaPlayerDemo_Video Activity,"
                                                + " and set the path variable to your media file URL.",
                                        Toast.LENGTH_LONG).show();

                    }

                    break;


            }

            // Create a new media player and set the listeners
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.setDisplay(holder);
            mMediaPlayer.prepare();
            mMediaPlayer.setOnBufferingUpdateListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnVideoSizeChangedListener(this);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        } catch (Exception e) {
            Log.e(TAG, "error: " + e.getMessage(), e);
        }
    }

    public void onBufferingUpdate(MediaPlayer arg0, int percent) {
        Log.d(TAG, "onBufferingUpdate percent:" + percent);

    }

    public void onCompletion(MediaPlayer arg0) {
        Log.d(TAG, "onCompletion called");
    }

    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        Log.v(TAG, "onVideoSizeChanged called");
        if (width == 0 || height == 0) {
            Log.e(TAG, "invalid video width(" + width + ") or height(" + height + ")");
            return;
        }
        mIsVideoSizeKnown = true;
        mVideoWidth = width;
        mVideoHeight = height;
        //noinspection ConstantConditions
        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
            startVideoPlayback();
        }
    }

    public void onPrepared(MediaPlayer mediaplayer) {
        Log.d(TAG, "onPrepared called");
        mIsVideoReadyToBePlayed = true;
        //noinspection ConstantConditions
        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
            startVideoPlayback();
        }
    }

    public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
        Log.d(TAG, "surfaceChanged called");

    }

    public void surfaceDestroyed(SurfaceHolder surfaceholder) {
        Log.d(TAG, "surfaceDestroyed called");
    }


    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated called");
        playVideo(extras.getInt(MEDIA));
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaPlayer();
        doCleanUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
        doCleanUp();
    }

    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void doCleanUp() {
        mVideoWidth = 0;
        mVideoHeight = 0;
        mIsVideoReadyToBePlayed = false;
        mIsVideoSizeKnown = false;
    }

    private void startVideoPlayback() {
        Log.v(TAG, "startVideoPlayback");
        holder.setFixedSize(mVideoWidth, mVideoHeight);
        mMediaPlayer.start();
    }
}

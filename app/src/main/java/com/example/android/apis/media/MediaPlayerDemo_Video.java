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
     * Called from our {@code surfaceCreated} callback to begin playing the video. First we call our
     * method {@code doCleanup} to set the width and height used to display the video both to 0, and
     * both {@code mIsVideoReadyToBePlayed} and {@code mIsVideoSizeKnown} to false, so that no playback will
     * occur until the {@code onVideoSizeChanged} callback is called (which sets the dimensions to the
     * current value and sets {@code mIsVideoSizeKnown} to true, {@code mIsVideoReadyToBePlayed} is only
     * set to true in our {@code onPrepared} callback). Then wrapped in a try block intended to catch any
     * exceptions, we switch based on the parameter {@code Integer Media}:
     * <ul>
     * <li>
     * LOCAL_VIDEO - needs to be modified to set {@code String path} to a local media file path
     * before it do can do anything other than toast a message saying that this file needs to be
     * edited.
     * </li>
     * <li>
     * STREAM_VIDEO - needs to be modified to set {@code String path} to an http url for a stream
     * before it do can do anything other than toast a message saying that this file needs to be
     * edited.
     * </li>
     * </ul>
     * Once the field {@code path} has been properly set, we initialize {@code MediaPlayer mMediaPlayer}
     * with a new instance, set its data source to {@code path}, set the {@code SurfaceHolder} for it to
     * use for displaying the video portion of the media to {@code SurfaceHolder holder}, and then call
     * its method {@code prepare} to prepare the player for synchronous playback (it will block until
     * ready, then call our callback {@code onPrepared} before returning). We set its {@code OnBufferingUpdateListener},
     * {@code OnCompletionListener}, {@code OnPreparedListener}, and {@code OnVideoSizeChangedListener} to
     * "this", and set its audio stream type to the audio stream for music playback STREAM_MUSIC.
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

    /**
     * Called to update status in buffering a media stream received through progressive HTTP download.
     * The received buffering percentage indicates how much of the content has been buffered or played.
     * For example a buffering update of 80 percent when half the content has already been played
     * indicates that the next 30 percent of the content to play has been buffered. We simply log the
     * value of our parameter {@code percent}.
     *
     * @param arg0    the MediaPlayer the update pertains to
     * @param percent the percentage (0-100) of the content that has been buffered or played thus far
     */
    public void onBufferingUpdate(MediaPlayer arg0, int percent) {
        Log.d(TAG, "onBufferingUpdate percent:" + percent);
    }

    /**
     * Called when the end of a media source is reached during playback. We simply log this fact.
     *
     * @param arg0 the MediaPlayer that reached the end of the file
     */
    public void onCompletion(MediaPlayer arg0) {
        Log.d(TAG, "onCompletion called");
    }

    /**
     * Called to indicate the video size. The video size (width and height) could be 0 if there was
     * no video, no display surface was set, or the value was not determined yet.
     * <p>
     * It either of our parameters {@code width} or {@code height} is 0, we simply log this error and
     * return. Otherwise we set our flag {@code mIsVideoSizeKnown} to true, store the new {@code width}
     * in our field {@code mVideoWidth}, and the new {@code height} in {@code mVideoHeight}. If both
     * {@code mIsVideoReadyToBePlayed}, and {@code mIsVideoSizeKnown} are now both true we call our method
     * {@code startVideoPlayback} ({@code onPrepared} sets {@code mIsVideoReadyToBePlayed} to true when
     * it is called, we put this check here because we don't care which of us is called first as long
     * as both have been called).
     *
     * @param mp     the MediaPlayer associated with this callback
     * @param width  the width of the video
     * @param height the height of the video
     */
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

    /**
     * Called when the media file is ready for playback. We set the flag {@code mIsVideoReadyToBePlayed}
     * to true, and if both {@code mIsVideoReadyToBePlayed}, and {@code mIsVideoSizeKnown} are now both
     * true we call our method {@code startVideoPlayback} ({@code onVideoSizeChanged} sets
     * {@code mIsVideoSizeKnown} to true when it is called, we put this check here because we don't
     * care which of us is called first as long as both have been called).
     *
     * @param mediaplayer the MediaPlayer that is ready for playback
     */
    public void onPrepared(MediaPlayer mediaplayer) {
        Log.d(TAG, "onPrepared called");
        mIsVideoReadyToBePlayed = true;
        //noinspection ConstantConditions
        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
            startVideoPlayback();
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
    public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
        Log.d(TAG, "surfaceChanged called");

    }

    /**
     * This is called immediately before a surface is being destroyed. We simply log that we were
     * called.
     *
     * @param surfaceholder The SurfaceHolder whose surface is being destroyed.
     */
    public void surfaceDestroyed(SurfaceHolder surfaceholder) {
        Log.d(TAG, "surfaceDestroyed called");
    }

    /**
     * This is called immediately after the surface is first created. We simply call our method
     * {@code playVideo} with the data stored under the key MEDIA ("media") in the {@code Bundle extras}
     * which was included in the intent which launched this activity ({@code extras} was retrieved from
     * the intent in our activity's {@code onCreate} method).
     *
     * @param holder The SurfaceHolder whose surface is being created.
     */
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated called");
        playVideo(extras.getInt(MEDIA));
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. First we call through to our super's implementation of {@code onPause},
     * then we call our method {@code releaseMediaPlayer} which releases resources associated with
     * {@code MediaPlayer mMediaPlayer}, and sets {@code mMediaPlayer} to null. Finally we call our
     * method {@code doCleanUp} which sets both {@code mVideoWidth} and {@code mVideoHeight} to 0, and
     * sets the flags {@code mIsVideoReadyToBePlayed} and {@code mIsVideoSizeKnown} to false.
     */
    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaPlayer();
        doCleanUp();
    }

    /**
     * Perform any final cleanup before an activity is destroyed. First we call through to our super's
     * implementation of {@code onDestroy}, then we call our method {@code releaseMediaPlayer} which
     * releases resources associated with {@code MediaPlayer mMediaPlayer}, and sets {@code mMediaPlayer}
     * to null. Finally we call our method {@code doCleanUp} which sets both {@code mVideoWidth} and
     * {@code mVideoHeight} to 0, and sets the flags {@code mIsVideoReadyToBePlayed} and {@code mIsVideoSizeKnown}
     * to false.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
        doCleanUp();
    }

    /**
     * If our field {@code MediaPlayer mMediaPlayer} is not null, we call its method {@code release}
     * which releases resources associated with it, then we set it to null.
     */
    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    /**
     * Cleans up by setting both {@code mVideoWidth} and {@code mVideoHeight} to 0, and the flags
     * {@code mIsVideoReadyToBePlayed} and {@code mIsVideoSizeKnown} to false.
     */
    private void doCleanUp() {
        mVideoWidth = 0;
        mVideoHeight = 0;
        mIsVideoReadyToBePlayed = false;
        mIsVideoSizeKnown = false;
    }

    /**
     * Starts the video playing. First we set the surface controlled by {@code SurfaceHolder holder}
     * to a fixed size ({@code mVideoWidth} by {@code mVideoHeight}), and then we start the playback
     * of {@code MediaPlayer mMediaPlayer}.
     */
    private void startVideoPlayback() {
        Log.v(TAG, "startVideoPlayback");
        holder.setFixedSize(mVideoWidth, mVideoHeight);
        mMediaPlayer.start();
    }
}

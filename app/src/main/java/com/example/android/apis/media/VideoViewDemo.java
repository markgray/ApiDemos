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

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.android.apis.R;

/**
 * Shows how to use the VideoView class in an application to play a video, as well as a
 * MediaController to provide control buttons for the playback.
 * TODO: Set the path variable to a streaming video URL or a local media file path.
 */
public class VideoViewDemo extends Activity {
    /**
     * {@code VideoView} in our layout that we use to display our video (ID R.id.surface_view)
     */
    @SuppressWarnings("FieldCanBeLocal")
    private VideoView mVideoView;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.videoview. We then
     * initialize our field {@code VideoView mVideoView} by locating the {@code VideoView} in our
     * layout with ID R.id.surface_view, set its video url to address the file videoviewdemo.mp4 in
     * our raw resources, set its media controller to a new instance of {@code MediaController}, and
     * finally request focus for it.
     *
     * @param icicle we do not implement {@code onSaveInstanceState} so do not use
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.videoview);
        mVideoView = (VideoView) findViewById(R.id.surface_view);
        /*
         * Alternatively, you can use mVideoView.setVideoPath(<path>);
         */
        mVideoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.videoviewdemo));
        mVideoView.setMediaController(new MediaController(this));
        mVideoView.requestFocus();
    }
}

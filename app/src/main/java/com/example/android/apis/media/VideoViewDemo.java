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

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.android.apis.R;

/**
 * Shows how to use the VideoView class in an application to play a video, as well as a
 * MediaController to provide control buttons for the playback.
 */
@TargetApi(Build.VERSION_CODES.N)
public class VideoViewDemo extends Activity {

    /**
     * {@code VideoView} in our layout that we use to display our video (ID R.id.surface_view)
     */
    private VideoView mVideoView;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.videoview. We then
     * initialize our field {@code VideoView mVideoView} by locating the {@code VideoView} in our
     * layout with ID R.id.surface_view. We then call our method {@code initPlayer} to set its video
     * url to address the file videoviewdemo.mp4 in our raw resources, set its media controller to a
     * new instance of {@code MediaController}, and request focus for it. Finally we call the
     * {@code setOnDragListener} of {@code mVideoView} to our field {@code OnDragListener mDragListener}.
     *
     * @param icicle we do not implement {@code onSaveInstanceState} so do not use
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.videoview);
        mVideoView = findViewById(R.id.surface_view);

        initPlayer(Uri.parse("android.resource://" + getPackageName() +
                "/" + R.raw.videoviewdemo));

        mVideoView.setOnDragListener(mDragListener);
    }

    /**
     * Initializes our {@code VideoView mVideoView} player. We set the video {@code Uri} that
     * {@code VideoView mVideoView} should use to our parameter {@code Uri uri}, set its media
     * controller to a new instance of {@code MediaController}, and request focus for it.
     *
     * @param uri Video {@code Uri} that our {@code VideoView mVideoView} should use.
     */
    private void initPlayer(Uri uri) {
        mVideoView.setVideoURI(uri);
        mVideoView.setMediaController(new MediaController(this));
        mVideoView.requestFocus();
    }

    /**
     * {@code OnDragListener} for our {@code VideoView mVideoView} (Resource id R.id.surface_view in
     * our layout file).
     */
    private View.OnDragListener mDragListener = new View.OnDragListener() {
        /**
         * Called when a drag event is dispatched to our view. If the action of our parameter
         * {@code DragEvent event} is not ACTION_DROP (which would signal to our View that the user
         * has released the drag shadow, and the drag point is within the bounding box of the View and
         * not within a descendant view that can accept the data) we return true to the caller as we
         * are only interested in ACTION_DROP. If it is ACTION_DROP we initialize {@code ClipData clipData}
         * with the {@code ClipData} object sent to the system by the caller of {@code startDragAndDrop}.
         * If the number of items in {@code clipData} is not equal to 1 we return false to the caller.
         * Otherwise we initialize {@code ClipData.Item item} with the item at index 0 in {@code clipData}.
         * Then we initialize {@code Uri uri} with the raw URI contained in {@code item}. If {@code uri}
         * is null we return false to the caller. We call the {@code requestDragAndDropPermissions} method
         * to create a {@code DragAndDropPermissions} object bound to this activity and controlling the
         * access permissions for content URIs associated with {@code event}, and if it returns null
         * (no content URIs are associated with the event or if permissions could not be granted) we
         * return false to the caller. Otherwise we call our method {@code initPlayer} to initialize
         * our {@code VideoView mVideoView} player to play {@code uri}. We set the {@code OnPreparedListener}
         * of {@code mVideoView} to an anonymous class which calls the {@code start} method of {@code mVideoView}
         * to start it playing, then return true to the caller to consume the drag event.
         *
         * @param v     The View that received the drag event.
         * @param event The {@link android.view.DragEvent} object for the drag event.
         * @return {@code true} if the drag event was handled successfully, or {@code false}
         * if the drag event was not handled.
         */
        @Override
        public boolean onDrag(View v, DragEvent event) {
            if (event.getAction() != DragEvent.ACTION_DROP) {
                return true;
            }
            ClipData clipData = event.getClipData();
            if (clipData.getItemCount() != 1) {
                return false;
            }
            ClipData.Item item = clipData.getItemAt(0);
            Uri uri = item.getUri();
            if (uri == null) {
                return false;
            }
            if (requestDragAndDropPermissions(event) == null) {
                return false;
            }
            initPlayer(uri);
            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                /**
                 * Called when the media file is ready for playback. We just call the {@code start}
                 * method of {@code mVideoView}.
                 *
                 * @param mediaPlayer the MediaPlayer that is ready for playback
                 */
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mVideoView.start();
                }
            });
            return true;
        }
    };
}

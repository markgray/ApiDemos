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

import android.content.ClipData
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.DragAndDropPermissions
import android.view.DragEvent
import android.view.View
import android.view.View.OnDragListener
import android.widget.MediaController
import android.widget.VideoView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.example.android.apis.R

/**
 * Shows how to use the [VideoView] class in an application to play a video, as well as a
 * [MediaController] to provide control buttons for the playback.
 */
@RequiresApi(Build.VERSION_CODES.N)
class VideoViewDemo : AppCompatActivity() {
    /**
     * [VideoView] in our layout that we use to display our video (ID R.id.surface_view)
     */
    private var mVideoView: VideoView? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.videoview. We then
     * initialize our [VideoView] field [mVideoView] by locating the [VideoView] in our layout with
     * ID R.id.surface_view. We then call our method [initPlayer] to set its video url to address
     * the file videoviewdemo.mp4 in our raw resources, set its media controller to a new instance
     * of [MediaController], and request focus for it. Finally we call the [View.setOnDragListener]
     * method of [mVideoView] to set its [OnDragListener] our field [mDragListener].
     *
     * @param icicle we do not implement [onSaveInstanceState] so do not use
     */
    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.videoview)
        mVideoView = findViewById(R.id.surface_view)
        initPlayer(("android.resource://" + packageName + "/" + R.raw.videoviewdemo).toUri())
        mVideoView!!.setOnDragListener(mDragListener)
    }

    /**
     * Initializes our [VideoView] field [mVideoView] video player. We set the video [Uri] that
     * [mVideoView] should use to our [Uri] parameter [uri], set its media controller to a new
     * instance of [MediaController], and request focus for it.
     *
     * @param uri Video [Uri] that our [VideoView] field [mVideoView] should use.
     */
    private fun initPlayer(uri: Uri) {
        mVideoView!!.setVideoURI(uri)
        mVideoView!!.setMediaController(MediaController(this))
        mVideoView!!.requestFocus()
    }

    /**
     * [OnDragListener] for our [VideoView] field [mVideoView] (Resource id R.id.surface_view in
     * our layout file).
     */
    private val mDragListener = OnDragListener { _, event ->

        /**
         * Called when a drag event is dispatched to our view. If the action of our parameter
         * [DragEvent] parameter [event] is not ACTION_DROP (which would signal to our View that
         * the user has released the drag shadow, and the drag point is within the bounding box of
         * the View and not within a descendant view that can accept the data) we return true to
         * the caller as we are only interested in ACTION_DROP. If it is ACTION_DROP we initialize
         * [ClipData] `val clipData` with the [ClipData] object sent to the system by the caller of
         * `startDragAndDrop`. If the number of items in `clipData` is not equal to 1 we return false
         * to the caller. Otherwise we initialize [ClipData.Item] `val item` with the item at index 0
         * in `clipData`. Then we initialize [Uri] `val uri` with the raw URI contained in `item`.
         * If `uri` is null we return false to the caller. We call the `requestDragAndDropPermissions`
         * method to create a [DragAndDropPermissions] object bound to this activity and controlling
         * the access permissions for content URIs associated with `event`, and if it returns null
         * (no content URIs are associated with the event or if permissions could not be granted) we
         * return false to the caller. Otherwise we call our method [initPlayer] to initialize
         * our [VideoView] field [mVideoView] video player to play `uri`. We set the
         * [MediaPlayer.OnPreparedListener] of [mVideoView] to a lambda which calls the `start`
         * method of [mVideoView] to start it playing, then return true to the caller to consume
         * the drag event.
         *
         * @param event The [android.view.DragEvent] object for the drag event.
         * @return `true` if the drag event was handled successfully, or `false`
         * if the drag event was not handled.
         */
        if (event.action != DragEvent.ACTION_DROP) {
            return@OnDragListener true
        }
        val clipData: ClipData = event.clipData
        if (clipData.itemCount != 1) {
            return@OnDragListener false
        }
        val item = clipData.getItemAt(0)
        val uri = item.uri ?: return@OnDragListener false
        if (requestDragAndDropPermissions(event) == null) {
            return@OnDragListener false
        }
        initPlayer(uri)
        mVideoView!!.setOnPreparedListener { mVideoView!!.start() }
        true
    }
}
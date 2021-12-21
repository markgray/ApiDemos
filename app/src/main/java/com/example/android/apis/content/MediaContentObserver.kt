/*
 * Copyright (c) 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.apis.content

import android.Manifest
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

@Suppress("MemberVisibilityCanBePrivate")
@RequiresApi(api = Build.VERSION_CODES.N)
class MediaContentObserver : AppCompatActivity() {
    /**
     * [ContentObserver] which receives call backs for changes to content, its `onChange`
     * override appends the changed content Uri to our [TextView] field [mDataText]
     */
    var mContentObserver: ContentObserver? = null
    /**
     * `Button` in our layout with id R.id.schedule_media_job ("Schedule media job"), its
     * `OnClickListener` calls the `scheduleJob` method of [MediaContentJob] to schedule a
     * [MediaContentJob] job to monitor when there is a change to any media content URI.
     */
    var mScheduleMediaJob: View? = null
    /**
     * `Button` in our layout with id R.id.cancel_media_job ("Cancel media job"), its
     * `OnClickListener` calls the `cancelJob` method of [MediaContentJob] to cancel the
     * [MediaContentJob]
     */
    var mCancelMediaJob: View? = null
    /**
     * `Button` in our layout with id R.id.schedule_photos_job ("Schedule photos job"), its
     * `OnClickListener` requests the permission READ_EXTERNAL_STORAGE if it is not already
     * granted, or if it is already granted calls the `scheduleJob` method of [PhotosContentJob]
     * to schedule a [PhotosContentJob] job to monitor when there is a change to photos in the
     * media provider.
     */
    var mSchedulePhotosJob: View? = null
    /**
     * `Button` in our layout with id R.id.cancel_photos_job ("Cancel photos job"), its
     * `OnClickListener` calls the `cancelJob` method of [PhotosContentJob] to cancel the
     * [PhotosContentJob]
     */
    var mCancelPhotosJob: View? = null
    /**
     * [TextView] in our layout with id R.id.changes_text, the `onChange` override of
     * [ContentObserver] field [mContentObserver] appends the changed content Uri it
     * receives to us.
     */
    var mDataText: TextView? = null

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we set our content view to our layout file R.layout.media_content_observer. We initialize
     * our [ContentObserver] field [mContentObserver] with an anonymous class whose [Handler] uses
     * the looper from our thread, and whose `onChange` override appends the string value of its
     * [Uri] parameter `uri` to our [TextView] field [mDataText] (followed by a newline). We then
     * log the message "Observing: " followed by the string value of the [Uri] MEDIA_URI of the
     * class [MediaContentJob] which it monitors using a `TriggerContentUri`.
     *
     * We now proceed to save references to views in our layout in their respective fields:
     *
     *  * [View] field [mScheduleMediaJob]: the view with id R.id.schedule_media_job
     *  * [View] field [mCancelMediaJob]: the view with id R.id.cancel_media_job
     *  * [View] field [mSchedulePhotosJob]: the view with id R.id.schedule_photos_job
     *  * [View] field [mCancelPhotosJob]: the view with id R.id.cancel_photos_job
     *  * [TextView] field [mDataText]: the view with id R.id.changes_text
     *
     * Now we set the `OnClickListener` of each of the four buttons in our layout:
     *
     *  * [mScheduleMediaJob]: its `onClick` override calls the `scheduleJob` method of
     *  [MediaContentJob] to schedule a [MediaContentJob] job to monitor when there is a
     *  change to any media content URI, then calls our [updateButtons] method to update
     *  the enabled/disabled state of the buttons.
     *  * [mCancelMediaJob]: its `onClick` override calls the `cancelJob` method of
     *  [MediaContentJob] to cancel the [MediaContentJob] job, then calls our [updateButtons]
     *  method to update the enabled/disabled state of the buttons.
     *  * [mSchedulePhotosJob]: its `onClick` override branches on whether the method
     *  [checkSelfPermission] returns PERMISSION_GRANTED:
     *      * no: We call the method [requestPermissions] to ask the user for the
     *      READ_EXTERNAL_STORAGE permission using REQ_PHOTOS_PERM as the request code
     *      that will be passed to our [onRequestPermissionsResult] callback.
     *      * yes: we call the `scheduleJob` method of [PhotosContentJob] to schedule a
     *      [PhotosContentJob] job to monitor when there is a change to photos in the media
     *      provider, then call our [updateButtons] method to update the enabled/disabled
     *      state of the buttons.
     *  * [mCancelPhotosJob]: its `onClick` override calls the `cancelJob` method of
     *  [PhotosContentJob] to cancel the [PhotosContentJob] job, then calls our [updateButtons]
     *  method to update the enabled/disabled state of the buttons.
     *
     * We call our [updateButtons] method to update the enabled/disabled state of the buttons,
     * then we use a `ContentResolver` instance for our application's package to register
     * `ContentObserver` field [mContentObserver] as an observer that will get callbacks when data
     * identified by the content URI `MediaContentJob.MEDIA_URI` ("content://media/") changes.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.media_content_observer)
        mContentObserver = object : ContentObserver(Handler(Looper.myLooper()!!)) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                mDataText!!.append(uri.toString())
                mDataText!!.append("\n")
            }
        }
        Log.d("foo", "Observing: " + MediaContentJob.MEDIA_URI)
        mScheduleMediaJob = findViewById(R.id.schedule_media_job)
        mCancelMediaJob = findViewById(R.id.cancel_media_job)
        mSchedulePhotosJob = findViewById(R.id.schedule_photos_job)
        mCancelPhotosJob = findViewById(R.id.cancel_photos_job)
        mDataText = findViewById(R.id.changes_text)
        mScheduleMediaJob!!.setOnClickListener {
            MediaContentJob.scheduleJob(this@MediaContentObserver)
            updateButtons()
        }
        mCancelMediaJob!!.setOnClickListener {
            MediaContentJob.cancelJob(this@MediaContentObserver)
            updateButtons()
        }
        mSchedulePhotosJob!!.setOnClickListener {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQ_PHOTOS_PERM)
            } else {
                PhotosContentJob.scheduleJob(this@MediaContentObserver)
                updateButtons()
            }
        }
        mCancelPhotosJob!!.setOnClickListener {
            PhotosContentJob.cancelJob(this@MediaContentObserver)
            updateButtons()
        }
        updateButtons()
        contentResolver.registerContentObserver(MediaContentJob.MEDIA_URI, true,
                mContentObserver!!)
    }

    /**
     * Callback for the result from requesting permissions. This method is invoked for every call on
     * [requestPermissions]. If our parameter [requestCode] is the same REQ_PHOTOS_PERM (1) used in
     * our call to [requestPermissions], we check that our `String[]` parameter [permissions] is
     * not empty, and the value stored in `grantResults[0]` is [PackageManager.PERMISSION_GRANTED]
     * before calling the `scheduleJob` method of [PhotosContentJob] to schedule a [PhotosContentJob]
     * job to monitor when there is a change to photos in the media provider, and then call our
     * [updateButtons] method to update the enabled/disabled state of the buttons.
     *
     * @param requestCode The request code passed in [requestPermissions].
     * @param permissions The requested permissions. Never *null*.
     * @param grantResults The grant results for the corresponding permissions
     * which is either [PackageManager.PERMISSION_GRANTED] or [PackageManager.PERMISSION_DENIED].
     * Never *null*.
     */
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_PHOTOS_PERM) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                PhotosContentJob.scheduleJob(this@MediaContentObserver)
                updateButtons()
            }
        }
    }

    /**
     * Updates the enabled/disabled state of the four buttons in our UI depending on whether
     * [MediaContentJob] and/or [PhotosContentJob] are scheduled or not. If the `isScheduled`
     * method of [MediaContentJob] returns *true* we disable the button [mScheduleMediaJob] and
     * enable the button [mCancelMediaJob] otherwise we enable the button [mScheduleMediaJob] and
     * disable the button [mCancelMediaJob]. If the `isScheduled` method of [PhotosContentJob]
     * returns *true* we disable the button [mSchedulePhotosJob] and enable the button
     * [mCancelPhotosJob] otherwise we enable the button [mSchedulePhotosJob] and disable the
     * button [mCancelPhotosJob].
     */
    fun updateButtons() {
        if (MediaContentJob.isScheduled(this)) {
            mScheduleMediaJob!!.isEnabled = false
            mCancelMediaJob!!.isEnabled = true
        } else {
            mScheduleMediaJob!!.isEnabled = true
            mCancelMediaJob!!.isEnabled = false
        }
        if (PhotosContentJob.isScheduled(this)) {
            mSchedulePhotosJob!!.isEnabled = false
            mCancelPhotosJob!!.isEnabled = true
        } else {
            mSchedulePhotosJob!!.isEnabled = true
            mCancelPhotosJob!!.isEnabled = false
        }
    }

    /**
     * Perform any final cleanup before our activity is destroyed. First we call our super's
     * implementation of `onDestroy`, then we use a `ContentResolver` instance for our application's
     * package to unregister our change [ContentObserver] observer field [mContentObserver].
     */
    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(mContentObserver!!)
    }

    /**
     * Our static constant
     */
    companion object {
        /**
         * Request code passed to [requestPermissions] when we request READ_EXTERNAL_STORAGE,
         * later passed to our [onRequestPermissionsResult] callback when the user responds.
         */
        const val REQ_PHOTOS_PERM = 1
    }
}
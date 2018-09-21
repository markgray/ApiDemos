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

package com.example.android.apis.content;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.android.apis.R;

@RequiresApi(api = Build.VERSION_CODES.N)
public class MediaContentObserver extends Activity {
    /**
     * Request code passed to {@code requestPermissions} when we request READ_EXTERNAL_STORAGE,
     * later passed to our {@code onRequestPermissionsResult} callback when the use responds.
     */
    public static final int REQ_PHOTOS_PERM = 1;

    /**
     * {@code ContentObserver} which receives call backs for changes to content, its {@code onChange}
     * override appends the changed content Uri to our {@code TextView mDataText}
     */
    ContentObserver mContentObserver;
    /**
     * Button in our layout with id R.id.schedule_media_job ("Schedule media job"), its {@code OnClickListener}
     * calls the {@code scheduleJob} method of {@code MediaContentJob} to schedule a {@code MediaContentJob}
     * job to monitor when there is a change to any media content URI.
     */
    View mScheduleMediaJob;
    /**
     * Button in our layout with id R.id.cancel_media_job ("Cancel media job"), its {@code OnClickListener}
     * calls the {@code cancelJob} method of {@code MediaContentJob} to cancel the {@code MediaContentJob}
     */
    View mCancelMediaJob;
    /**
     * Button in our layout with id R.id.schedule_photos_job ("Schedule photos job"), its {@code OnClickListener}
     * requests the permission READ_EXTERNAL_STORAGE if it is not already granted, or if it is already granted
     * calls the {@code scheduleJob} method of {@code PhotosContentJob} to schedule a {@code PhotosContentJob}
     * job to monitor when there is a change to photos in the media provider.
     */
    View mSchedulePhotosJob;
    /**
     * Button in our layout with id R.id.cancel_photos_job ("Cancel photos job"), its {@code OnClickListener}
     * calls the {@code cancelJob} method of {@code PhotosContentJob} to cancel the {@code PhotosContentJob}
     */
    View mCancelPhotosJob;
    /**
     * {@code TextView} in our layout with id R.id.changes_text, the {@code onChange} override of
     * {@code ContentObserver mContentObserver} appends the changed content Uri it receives to us.
     */
    TextView mDataText;

    /**
     * Called when the activity is starting. First we call our super's implementation of {@code onCreate},
     * then we set our content view to our layout file R.layout.media_content_observer. We initialize our
     * field {@code ContentObserver mContentObserver} with an anonymous class whose {@code Handler} uses
     * the looper from our thread, and whose {@code onChange} override appends the string value of its
     * {@code Uri uri} parameter to our {@code TextView mDataText} (followed by a newline). We then log
     * the message "Observing: " followed by the string value of the {@code Uri MEDIA_URI} of the class
     * {@code MediaContentJob} which it monitors using a {@code TriggerContentUri}.
     * <p>
     * We now proceed to save references to views in our layout in their respective fields:
     * <ul>
     *     <li>
     *         {@code View mScheduleMediaJob}: the view with id R.id.schedule_media_job
     *     </li>
     *     <li>
     *         {@code View mCancelMediaJob}: the view with id R.id.cancel_media_job
     *     </li>
     *     <li>
     *         {@code View mSchedulePhotosJob}: the view with id R.id.schedule_photos_job
     *     </li>
     *     <li>
     *         {@code View mCancelPhotosJob}: the view with id R.id.cancel_photos_job
     *     </li>
     *     <li>
     *         {@code TextView mDataText}: the view with id R.id.changes_text
     *     </li>
     * </ul>
     * Now we set the {@code OnClickListener} of each of the four buttons in our layout:
     * <ul>
     *     <li>
     *         {@code mScheduleMediaJob}: its {@code onClick} override calls the {@code scheduleJob}
     *         method of {@code MediaContentJob} to schedule a {@code MediaContentJob} job to monitor
     *         when there is a change to any media content URI, then calls our {@code updateButtons}
     *         to update the enabled/disabled state of the buttons.
     *     </li>
     *     <li>
     *         {@code mCancelMediaJob}: its {@code onClick} override calls the {@code cancelJob}
     *         method of {@code MediaContentJob} to cancel the {@code MediaContentJob} job, then
     *         calls our {@code updateButtons} to update the enabled/disabled state of the buttons.
     *     </li>
     *     <li>
     *         {@code mSchedulePhotosJob}: its {@code onClick} override 
     *     </li>
     * </ul>
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_content_observer);

        mContentObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                mDataText.append(uri.toString());
                mDataText.append("\n");
            }
        };

        Log.d("foo", "Observing: " + MediaContentJob.MEDIA_URI);

        mScheduleMediaJob = findViewById(R.id.schedule_media_job);
        mCancelMediaJob = findViewById(R.id.cancel_media_job);
        mSchedulePhotosJob = findViewById(R.id.schedule_photos_job);
        mCancelPhotosJob = findViewById(R.id.cancel_photos_job);
        mDataText = findViewById(R.id.changes_text);

        mScheduleMediaJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaContentJob.scheduleJob(MediaContentObserver.this);
                updateButtons();
            }
        });
        mCancelMediaJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaContentJob.cancelJob(MediaContentObserver.this);
                updateButtons();
            }
        });
        mSchedulePhotosJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                            REQ_PHOTOS_PERM);
                } else {
                    PhotosContentJob.scheduleJob(MediaContentObserver.this);
                    updateButtons();
                }
            }
        });
        mCancelPhotosJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotosContentJob.cancelJob(MediaContentObserver.this);
                updateButtons();
            }
        });
        updateButtons();

        getContentResolver().registerContentObserver(MediaContentJob.MEDIA_URI, true,
                mContentObserver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQ_PHOTOS_PERM) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                PhotosContentJob.scheduleJob(MediaContentObserver.this);
                updateButtons();
            }
        }
    }

    void updateButtons() {
        if (MediaContentJob.isScheduled(this)) {
            mScheduleMediaJob.setEnabled(false);
            mCancelMediaJob.setEnabled(true);
        } else {
            mScheduleMediaJob.setEnabled(true);
            mCancelMediaJob.setEnabled(false);
        }
        if (PhotosContentJob.isScheduled(this)) {
            mSchedulePhotosJob.setEnabled(false);
            mCancelPhotosJob.setEnabled(true);
        } else {
            mSchedulePhotosJob.setEnabled(true);
            mCancelPhotosJob.setEnabled(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(mContentObserver);
    }

}

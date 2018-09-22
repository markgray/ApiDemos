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

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * Example stub job to monitor when there is a change to any media: content URI.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class MediaContentJob extends JobService {
    /**
     * {@code Uri} we observe using our {@code TriggerContentUri}
     */
    static final Uri MEDIA_URI = Uri.parse("content://" + MediaStore.AUTHORITY + "/");

    /**
     * {@code Handler} we use to delay our work by 10 seconds so we can see batching happen.
     */
    final Handler mHandler = new Handler();
    /**
     * {@code Runnable} which does all our "work" after a ten second delay?
     */
    final Runnable mWorker = new Runnable() {
        @Override
        public void run() {
            scheduleJob(MediaContentJob.this);
            jobFinished(mRunningParams, false);
        }
    };

    JobParameters mRunningParams;

    public static void scheduleJob(Context context) {
        JobScheduler js = context.getSystemService(JobScheduler.class);
        JobInfo.Builder builder = new JobInfo.Builder(JobIds.MEDIA_CONTENT_JOB,
                new ComponentName(context, MediaContentJob.class));
        builder.addTriggerContentUri(new JobInfo.TriggerContentUri(MEDIA_URI,
                JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS));
        //noinspection ConstantConditions
        js.schedule(builder.build());
        Log.i("MediaContentJob", "JOB SCHEDULED!");
    }

    public static boolean isScheduled(Context context) {
        JobScheduler js = context.getSystemService(JobScheduler.class);
        //noinspection ConstantConditions
        List<JobInfo> jobs = js.getAllPendingJobs();
        //noinspection ConstantConditions
        if (jobs == null) {
            return false;
        }
        for (int i=0; i<jobs.size(); i++) {
            if (jobs.get(i).getId() == JobIds.MEDIA_CONTENT_JOB) {
                return true;
            }
        }
        return false;
    }

    public static void cancelJob(Context context) {
        JobScheduler js = context.getSystemService(JobScheduler.class);
        //noinspection ConstantConditions
        js.cancel(JobIds.MEDIA_CONTENT_JOB);
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i("MediaContentJob", "JOB STARTED!");
        mRunningParams = params;
        StringBuilder sb = new StringBuilder();
        sb.append("Media content has changed:\n");
        if (params.getTriggeredContentAuthorities() != null) {
            sb.append("Authorities: ");
            boolean first = true;
            for (String auth : params.getTriggeredContentAuthorities()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(auth);
            }
            if (params.getTriggeredContentUris() != null) {
                for (Uri uri : params.getTriggeredContentUris()) {
                    sb.append("\n");
                    sb.append(uri);
                }
            }
        } else {
            sb.append("(No content)");
        }
        Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();
        Log.i("MediaContentJob", "onStartJob called: " + sb.toString());
        // We will emulate taking some time to do this work, so we can see batching happen.
        mHandler.postDelayed(mWorker, 10*1000);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        mHandler.removeCallbacks(mWorker);
        return false;
    }
}

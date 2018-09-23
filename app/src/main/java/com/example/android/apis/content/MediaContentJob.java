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
     * {@code Runnable} which does all our "work" after a ten second delay
     */
    final Runnable mWorker = new Runnable() {
        /**
         * When an object implementing interface <code>Runnable</code> is used to create a thread,
         * starting the thread causes the object's <code>run</code> method to be called in that
         * separately executing thread. We call our method {@code scheduleJob} to schedule this
         * {@code MediaContentJob} to run, then we call the {@code jobFinished} to inform the
         * JobScheduler that the job has finished its work, with false as the wants reschedule
         * to specify that we do not want the job rescheduled.
         */
        @Override
        public void run() {
            scheduleJob(MediaContentJob.this);
            jobFinished(mRunningParams, false);
        }
    };

    /**
     * {@code JobParameters} passed to our {@code onStartJob} override, used to retrieve information
     * about which content authorities have triggered the job, and to identify the job when calling
     * {@code jobFinished}.
     */
    JobParameters mRunningParams;

    /**
     * Called to Schedule a {@code MediaContentJob} job to be executed. We initialize {@code JobScheduler js}
     * with a handle to the system level service which has the class {@code JobScheduler.class}. We initialize
     * {@code JobInfo.Builder builder} with a new instance which uses JobIds.MEDIA_CONTENT_JOB as the job id,
     * and {@code MediaContentJob} (our JobService) to receive the callback from the JobScheduler. We add to
     * {@code builder} the {@code TriggerContentUri} for the Uri {@code Uri MEDIA_URI} ("content://media/"),
     * and the flags FLAG_NOTIFY_FOR_DESCENDANTS (also trigger if any descendants of the given URI change).
     * We then use {@code js} to schedule the job built from {@code builder}, and log the fact that we
     * have scheduled the {@code MediaContentJob}.
     *
     * @param context {@code Context} to use to access activity resources, {@code MediaContentJob.this}
     *                when called by the {@code Runnable mWorker}, and {@code MediaContentObserver.this}
     *                when called by the {@code OnClickListener} {@code onClick} override of the UI
     *                button with id R.id.schedule_media_job ("Schedule media job") in the
     *                {@code MediaContentObserver} activity.
     */
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

    /**
     * Called to determine if there is currently a job with id {@code JobIds.MEDIA_CONTENT_JOB} in
     * the list of all jobs that have been scheduled by our application. First we initialize our
     * variable {@code JobScheduler js} with a handle to the system level service whose class is
     * {@code JobScheduler.class}. We use {@code js} to initialize {@code List<JobInfo> jobs} with
     * the list of all jobs that have been scheduled by our application. If this is null we return
     * false to the caller. Otherwise we loop over i for all the {@code JobInfo} objects in {@code jobs}
     * returning true if the job id of the i'th entry is {@code JobIds.MEDIA_CONTENT_JOB}. If none of
     * the jobs in {@code jobs} match we return false to the caller.
     *
     * @param context {@code Context} to use to get a handle to the {@code JobScheduler}.
     * @return true if there is a job with id {@code JobIds.MEDIA_CONTENT_JOB} (ours) in the list of
     * all jobs that have been scheduled by our application.
     */
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

    /**
     * Called to cancel the job with id {@code JobIds.MEDIA_CONTENT_JOB} (ours). First we initialize
     * our variable {@code JobScheduler js} with a handle to the system level service whose class is
     * {@code JobScheduler.class}, then use it to cancel the job with id {@code JobIds.MEDIA_CONTENT_JOB}.
     *
     * @param context {@code Context} to use to get a handle to the {@code JobScheduler}.
     */
    public static void cancelJob(Context context) {
        JobScheduler js = context.getSystemService(JobScheduler.class);
        //noinspection ConstantConditions
        js.cancel(JobIds.MEDIA_CONTENT_JOB);
    }

    /**
     * Called to indicate that the job has begun executing. Override this method with the logic for
     * your job. Like all other component lifecycle callbacks, this method executes on your application's
     * main thread.
     * <p>
     * Return {@code true} from this method if your job needs to continue running. If you do this,
     * the job remains active until you call {@link #jobFinished(JobParameters, boolean)} to tell
     * the system that it has completed its work, or until the job's required constraints are no
     * longer satisfied.
     * <p>
     * The system holds a wakelock on behalf of your app as long as your job is executing.
     * This wakelock is acquired before this method is invoked, and is not released until either
     * you call {@link #jobFinished(JobParameters, boolean)}, or after the system invokes
     * {@link #onStopJob(JobParameters)} to notify your job that it is being shut down
     * prematurely.
     * <p>
     * Returning {@code false} from this method means your job is already finished.  The
     * system's wakelock for the job will be released, and {@link #onStopJob(JobParameters)}
     * will not be invoked.
     * <p>
     * First we log the fact that our MediaContentJob has started.
     *
     * @param params Parameters specifying info about this job, including the optional
     *     extras configured with {@link JobInfo.Builder#setExtras(android.os.PersistableBundle).
     *     This object serves to identify this specific running job instance when calling
     *     {@link #jobFinished(JobParameters, boolean)}.
     * @return {@code true} if your service will continue running, using a separate thread
     *     when appropriate.  {@code false} means that this job has completed its work.
     */
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

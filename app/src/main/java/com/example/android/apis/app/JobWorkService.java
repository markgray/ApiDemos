/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.example.android.apis.app;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.app.job.JobWorkItem;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.example.android.apis.R;

/**
 * This is an example of implementing a {@link JobService} that dispatches work enqueued in
 * to it. The {@link JobWorkServiceActivity} class shows how to interact with the service.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class JobWorkService extends JobService {

    /**
     * Handle to the NOTIFICATION_SERVICE system level service.
     */
    private NotificationManager mNM;
    /**
     * The id of the primary notification channel
     */
    public static final String PRIMARY_CHANNEL = "default";

    /**
     * The {@code AsyncTask} we are using to process our current job in the background.
     */
    private CommandProcessor mCurProcessor;

    /**
     * This is a task to dequeue and process work in the background.
     */
    @SuppressLint("StaticFieldLeak") // We may indeed leak instances! TODO: check this out.
    final class CommandProcessor extends AsyncTask<Void, Void, Void> {
        /**
         * The {@code JobParameters} we were constructed to process.
         */
        private final JobParameters mParams;

        /**
         * Our constructor, we just save our parameters in our field {@code JobParameters mParams}.
         *
         * @param params the {@code JobParameters} passed to the {@code onStartJob} override
         */
        CommandProcessor(JobParameters params) {
            mParams = params;
        }

        /**
         * Override this method to perform a computation on a background thread. Our while loop is
         * designed to first check whether this task has been canceled by calling {@code isCanceled}
         * saving the return value in {@code canceled} and if we have not been canceled we dequeue
         * the next pending {@link JobWorkItem} from our field {@code JobParameters mParams} into
         * {@code work} and if that is not null we continue:
         * <ul>
         *     <li>
         *         We initialize {@code String txt} by retrieving the {@code Intent} from {@code work}
         *         and fetching the string extra stored under the key "name" in it.
         *     </li>
         *     <li>
         *         We log a message describing what we are doing with {@code text}
         *     </li>
         *     <li>
         *         We call our {@code showNotification} method to display a notification about {@code txt}
         *     </li>
         *     <li>
         *         Wrapped in a try block intended to catch and log {@code InterruptedException} we
         *         sleep for 5 seconds.
         *     </li>
         *     <li>
         *         We call our {@code hideNotification} method to cancel our notification.
         *     </li>
         *     <li>
         *         We log the fact that we are done processing {@code work}
         *     </li>
         *     <li>
         *         We call the {@code completeWork} method of {@code mParams} report the completion of
         *         executing {@code JobWorkItem work} (tells the system you are done with the work
         *         associated with that item, so it will not be returned again).
         *     </li>
         * </ul>
         * When we exit the while loop we check if that was because we were canceled and if so we log
         * this fact. Then we return null to the caller.
         *
         * @param params we do not have any
         * @return we do not have a return value.
         */
        @Override
        protected Void doInBackground(Void... params) {
            /*
             * true if task was cancelled before it completed, we set every loop of our while loop
             */
            boolean cancelled;
            /*
             * JobWorkItem removed from queue to be performed
             */
            JobWorkItem work;

            /*
             * Iterate over available work.  Once dequeueWork() returns null, the
             * job's work queue is empty and the job has stopped, so we can let this
             * async task complete.
             */
            while (!(cancelled=isCancelled()) && (work=mParams.dequeueWork()) != null) {
                String txt = work.getIntent().getStringExtra("name");
                Log.i("JobWorkService", "Processing work: " + work + ", msg: " + txt);
                showNotification(txt);

                // Process work here...  we'll pretend by sleeping.
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Log.i("JobWorkService", "Interrupted while sleeping");
                }

                hideNotification();

                // Tell system we have finished processing the work.
                Log.i("JobWorkService", "Done with: " + work);
                mParams.completeWork(work);
            }

            if (cancelled) {
                Log.i("JobWorkService", "CANCELLED!");
            }

            return null;
        }
    }

    /**
     * Called by the system when the service is first created. First We initialize our field
     * {@code NotificationManager mNM} with a handle to the NOTIFICATION_SERVICE system level
     * service. We initialize {@code NotificationChannel chan1} with a new instance whose id and
     * user visible name are both PRIMARY_CHANNEL ("default"), and whose importance is IMPORTANCE_DEFAULT
     * (shows everywhere, makes noise, but does not visually intrude). We set the notification light
     * color of {@code chan1} to GREEN, and set its lock screen visibility to VISIBILITY_PRIVATE
     * (shows this notification on all lockscreens, but conceal sensitive or private information on
     * secure lockscreens). We then have {@code mNM} create notification channel {@code chan1}.
     * Finally we toast the string with resource id R.string.service_created ("Service created.")
     */
    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel chan1 = new NotificationChannel(PRIMARY_CHANNEL, PRIMARY_CHANNEL,
                NotificationManager.IMPORTANCE_DEFAULT);
        chan1.setLightColor(Color.GREEN);
        chan1.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        mNM.createNotificationChannel(chan1);
        Toast.makeText(this, R.string.service_created, Toast.LENGTH_SHORT).show();
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed. We
     * call our method {@code hideNotification} to cancel our notification then we toast the string
     * with resource id R.string.service_destroyed ("Service destroyed.").
     */
    @Override
    public void onDestroy() {
        hideNotification();
        Toast.makeText(this, R.string.service_destroyed, Toast.LENGTH_SHORT).show();
    }

    /**
     * Called to indicate that the job has begun executing. First we initialize our field
     * {@code CommandProcessor mCurProcessor} with an instance constructed to process our parameter
     * {@code JobParameters params}. Then we call its {@code executeOnExecutor} method to start it
     * running in the background using the executor THREAD_POOL_EXECUTOR (an executor which allows
     * multiple tasks to run in parallel on a pool of threads managed by AsyncTask). Finally we
     * return true so that our job will continue running while we process work.
     *
     * @param params Parameters specifying info about this job.
     * @return {@code true} if your service will continue running, using a separate thread
     *     when appropriate.  {@code false} means that this job has completed its work.
     */
    @Override
    public boolean onStartJob(JobParameters params) {
        // Start task to pull work out of the queue and process it.
        mCurProcessor = new CommandProcessor(params);
        mCurProcessor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        // Allow the job to continue running while we process work.
        return true;
    }

    /**
     * This method is called if the system has determined that you must stop execution of your job
     * even before you've had a chance to call {@link #jobFinished(JobParameters, boolean)}.
     *
     * @param params The parameters identifying this job, as supplied to
     *               the job in the {@link #onStartJob(JobParameters)} callback.
     * @return {@code true} to indicate to the JobManager whether you'd like to reschedule
     * this job based on the retry criteria provided at job creation-time; or {@code false}
     * to end the job entirely.  Regardless of the value returned, your job must stop executing.
     */
    @Override
    public boolean onStopJob(JobParameters params) {
        // Have the processor cancel its current work.
        mCurProcessor.cancel(true);

        // Tell the system to reschedule the job -- the only reason we would be here is
        // because the job needs to stop for some reason before it has completed all of
        // its work, so we would like it to remain to finish that work in the future.
        return true;
    }

    /**
     * Show a notification while this service is running. We initialize {@code PendingIntent contentIntent}
     * with an instance intended to launch the activity {@code JobWorkServiceActivity} with request code
     * 0. We initialize {@code Notification.Builder noteBuilder} with a new instance using notification
     * channel PRIMARY_CHANNEL ("default"), set its small icon to R.drawable.stat_sample, its ticker
     * text to our parameter {@code String text}, its time stamp to now, its second line of text to
     * {@code text} and its {@link PendingIntent} to be sent when the notification is clicked to
     * {@code contentIntent}. We then set it to be an "ongoing" notification (ongoing notifications
     * cannot be dismissed by the user, so your application or service must take care of canceling them).
     * Finally we use {@code NotificationManager mNM} to post the notification build from {@code noteBuilder}
     * using the resource id R.string.job_service_created as its id.
     *
     * @param text string to use as both the ticker text (for accessibility) and content text
     */
    private void showNotification(String text) {
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, JobWorkServiceActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification.Builder noteBuilder = new Notification.Builder(this, PRIMARY_CHANNEL)
                .setSmallIcon(R.drawable.stat_sample)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.service_start_arguments_label))  // the label
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent);  // The intent to send when the entry is clicked

        // We show this for as long as our service is processing a command.
        noteBuilder.setOngoing(true);

        // Send the notification.
        // We use a string id because it is a unique number.  We use it later to cancel.
        mNM.notify(R.string.job_service_created, noteBuilder.build());
    }

    /**
     * Cancels the notification with id R.string.job_service_created.
     */
    private void hideNotification() {
        mNM.cancel(R.string.job_service_created);
    }
}

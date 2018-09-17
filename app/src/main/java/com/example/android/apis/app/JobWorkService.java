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
         * Override this method to perform a computation on a background thread.
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

    @Override
    public void onDestroy() {
        hideNotification();
        Toast.makeText(this, R.string.service_destroyed, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        // Start task to pull work out of the queue and process it.
        mCurProcessor = new CommandProcessor(params);
        mCurProcessor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        // Allow the job to continue running while we process work.
        return true;
    }

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
     * Show a notification while this service is running.
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

    private void hideNotification() {
        mNM.cancel(R.string.service_created);
    }
}

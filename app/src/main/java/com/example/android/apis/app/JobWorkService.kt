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

package com.example.android.apis.app

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.app.job.JobWorkItem
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import androidx.annotation.RequiresApi
import android.util.Log
import android.widget.Toast

import com.example.android.apis.R

/**
 * This is an example of implementing a [JobService] that dispatches work enqueued in
 * to it. The [JobWorkServiceActivity] class shows how to interact with the service.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
class JobWorkService : JobService() {

    /**
     * Handle to the NOTIFICATION_SERVICE system level service.
     */
    private var mNM: NotificationManager? = null

    /**
     * The `AsyncTask` we are using to process our current job in the background.
     */
    private var mCurProcessor: CommandProcessor? = null

    /**
     * This is a task to dequeue and process work in the background.
     */
    @SuppressLint("StaticFieldLeak") // We may indeed leak instances! TODO: check this out.
    internal inner class CommandProcessor
    /**
     * Our constructor, we just save our parameters in our field `JobParameters mParams`.
     *
     * Parameter: the `JobParameters` passed to the `onStartJob` override
     */
    (
            /**
             * The `JobParameters` we were constructed to process.
             */
            private val mParams: JobParameters) : AsyncTask<Void, Void, Void>() {

        /**
         * Override this method to perform a computation on a background thread. Our while loop is
         * designed to first check whether this task has been canceled by calling `isCanceled`
         * saving the return value in `canceled` and if we have not been canceled we dequeue
         * the next pending [JobWorkItem] from our field `JobParameters mParams` into
         * `work` and if that is not null we continue:
         *
         *  *
         * We initialize `String txt` by retrieving the `Intent` from `work`
         * and fetching the string extra stored under the key "name" in it.
         *
         *  *
         * We log a message describing what we are doing with `text`
         *
         *  *
         * We call our `showNotification` method to display a notification about `txt`
         *
         *  *
         * Wrapped in a try block intended to catch and log `InterruptedException` we
         * sleep for 5 seconds.
         *
         *  *
         * We call our `hideNotification` method to cancel our notification.
         *
         *  *
         * We log the fact that we are done processing `work`
         *
         *  *
         * We call the `completeWork` method of `mParams` report the completion of
         * executing `JobWorkItem work` (tells the system you are done with the work
         * associated with that item, so it will not be returned again).
         *
         *
         * When we exit the while loop we check if that was because we were canceled and if so we log
         * this fact. Then we return null to the caller.
         *
         * @param params we do not have any
         * @return we do not have a return value.
         */
        override fun doInBackground(vararg params: Void): Void? {
            /*
             * true if task was cancelled before it completed, we set every loop of our while loop
             */
            var cancelled: Boolean
            /*
             * JobWorkItem removed from queue to be performed
             */
            var work: JobWorkItem?

            /*
             * Iterate over available work.  Once dequeueWork() returns null, the
             * job's work queue is empty and the job has stopped, so we can let this
             * async task complete.
             */
            var b: Boolean = run {
                cancelled = isCancelled
                work = mParams.dequeueWork()
                !(cancelled) && (work) != null
            }
            while (b) {
                val txt = work!!.intent.getStringExtra("name")
                Log.i("JobWorkService", "Processing work: $work, msg: $txt")
                showNotification(txt)

                // Process work here...  we'll pretend by sleeping.
                try {
                    Thread.sleep(5000)
                } catch (e: InterruptedException) {
                    Log.i("JobWorkService", "Interrupted while sleeping")
                }

                hideNotification()

                // Tell system we have finished processing the work.
                Log.i("JobWorkService", "Done with: $work")
                mParams.completeWork(work as JobWorkItem)
                b = run {
                    cancelled = isCancelled
                    work = mParams.dequeueWork()
                    !(cancelled) && (work) != null
                }
            }

            if (cancelled) {
                Log.i("JobWorkService", "CANCELLED!")
            }

            return null
        }
    }

    /**
     * Called by the system when the service is first created. First We initialize our field
     * `NotificationManager mNM` with a handle to the NOTIFICATION_SERVICE system level
     * service. We initialize `NotificationChannel chan1` with a new instance whose id and
     * user visible name are both PRIMARY_CHANNEL ("default"), and whose importance is IMPORTANCE_DEFAULT
     * (shows everywhere, makes noise, but does not visually intrude). We set the notification light
     * color of `chan1` to GREEN, and set its lock screen visibility to VISIBILITY_PRIVATE
     * (shows this notification on all lockscreens, but conceal sensitive or private information on
     * secure lockscreens). We then have `mNM` create notification channel `chan1`.
     * Finally we toast the string with resource id R.string.service_created ("Service created.")
     */
    override fun onCreate() {
        mNM = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val chan1 = NotificationChannel(PRIMARY_CHANNEL, PRIMARY_CHANNEL,
                NotificationManager.IMPORTANCE_DEFAULT)
        chan1.lightColor = Color.GREEN
        chan1.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        mNM!!.createNotificationChannel(chan1)
        Toast.makeText(this, R.string.service_created, Toast.LENGTH_SHORT).show()
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed. We
     * call our method `hideNotification` to cancel our notification then we toast the string
     * with resource id R.string.service_destroyed ("Service destroyed.").
     */
    override fun onDestroy() {
        hideNotification()
        Toast.makeText(this, R.string.service_destroyed, Toast.LENGTH_SHORT).show()
    }

    /**
     * Called to indicate that the job has begun executing. First we initialize our field
     * `CommandProcessor mCurProcessor` with an instance constructed to process our parameter
     * `JobParameters params`. Then we call its `executeOnExecutor` method to start it
     * running in the background using the executor THREAD_POOL_EXECUTOR (an executor which allows
     * multiple tasks to run in parallel on a pool of threads managed by AsyncTask). Finally we
     * return true so that our job will continue running while we process work.
     *
     * @param params Parameters specifying info about this job.
     * @return `true` if your service will continue running, using a separate thread
     * when appropriate.  `false` means that this job has completed its work.
     */
    override fun onStartJob(params: JobParameters): Boolean {
        // Start task to pull work out of the queue and process it.
        mCurProcessor = CommandProcessor(params)
        mCurProcessor!!.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

        // Allow the job to continue running while we process work.
        return true
    }

    /**
     * This method is called if the system has determined that you must stop execution of your job
     * even before you've had a chance to call [.jobFinished].
     *
     * @param params The parameters identifying this job, as supplied to
     * the job in the [.onStartJob] callback.
     * @return `true` to indicate to the JobManager whether you'd like to reschedule
     * this job based on the retry criteria provided at job creation-time; or `false`
     * to end the job entirely.  Regardless of the value returned, your job must stop executing.
     */
    override fun onStopJob(params: JobParameters): Boolean {
        // Have the processor cancel its current work.
        mCurProcessor!!.cancel(true)

        // Tell the system to reschedule the job -- the only reason we would be here is
        // because the job needs to stop for some reason before it has completed all of
        // its work, so we would like it to remain to finish that work in the future.
        return true
    }

    /**
     * Show a notification while this service is running. We initialize `PendingIntent contentIntent`
     * with an instance intended to launch the activity `JobWorkServiceActivity` with request code
     * 0. We initialize `Notification.Builder noteBuilder` with a new instance using notification
     * channel PRIMARY_CHANNEL ("default"), set its small icon to R.drawable.stat_sample, its ticker
     * text to our parameter `String text`, its time stamp to now, its second line of text to
     * `text` and its [PendingIntent] to be sent when the notification is clicked to
     * `contentIntent`. We then set it to be an "ongoing" notification (ongoing notifications
     * cannot be dismissed by the user, so your application or service must take care of canceling them).
     * Finally we use `NotificationManager mNM` to post the notification build from `noteBuilder`
     * using the resource id R.string.job_service_created as its id.
     *
     * @param text string to use as both the ticker text (for accessibility) and content text
     */
    private fun showNotification(text: String?) {
        // The PendingIntent to launch our activity if the user selects this notification
        val contentIntent = PendingIntent.getActivity(this, 0,
                Intent(this, JobWorkServiceActivity::class.java), 0)

        // Set the info for the views that show in the notification panel.
        val noteBuilder = Notification.Builder(this, PRIMARY_CHANNEL)
                .setSmallIcon(R.drawable.stat_sample)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.service_start_arguments_label))  // the label
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked

        // We show this for as long as our service is processing a command.
        noteBuilder.setOngoing(true)

        // Send the notification.
        // We use a string id because it is a unique number.  We use it later to cancel.
        mNM!!.notify(R.string.job_service_created, noteBuilder.build())
    }

    /**
     * Cancels the notification with id R.string.job_service_created.
     */
    private fun hideNotification() {
        mNM!!.cancel(R.string.job_service_created)
    }

    companion object {
        /**
         * The id of the primary notification channel
         */
        const val PRIMARY_CHANNEL = "default"
    }
}

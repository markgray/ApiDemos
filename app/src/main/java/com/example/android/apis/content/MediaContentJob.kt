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

import android.app.job.JobInfo
import android.app.job.JobInfo.TriggerContentUri
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi

/**
 * Example stub job to monitor when there is a change to any media: content URI.
 */
@Suppress("MemberVisibilityCanBePrivate")
@RequiresApi(api = Build.VERSION_CODES.N)
class MediaContentJob : JobService() {
    /**
     * `Handler` we use to delay our work by 10 seconds so we can see batching happen.
     */
    val mHandler = Handler(Looper.myLooper()!!)
    /**
     * [Runnable] which does all our "work" after a ten second delay. When an object implementing
     * interface [Runnable] is used to create a thread, starting the thread causes the object's
     * `run` method to be called in that separately executing thread. We call our method
     * [scheduleJob] to schedule *this* [MediaContentJob] to run, then we call the [jobFinished]
     * method to inform the [JobScheduler] that the job has finished its work, with *false* as the
     * wants reschedule argument to specify that we do not want the job rescheduled.
     */
    val mWorker: Runnable = Runnable {
        scheduleJob(this@MediaContentJob)
        jobFinished(mRunningParams, false)
    }

    /**
     * [JobParameters] passed to our [onStartJob] override, used to retrieve information about which
     * content authorities have triggered the job, and to identify the job when calling [jobFinished]
     */
    var mRunningParams: JobParameters? = null

    /**
     * Called to indicate that the job has begun executing. Override this method with the logic for
     * your job. Like all other component lifecycle callbacks, this method executes on your
     * application's main thread.
     *
     * Return *true* from this method if your job needs to continue running. If you do this,
     * the job remains active until you call [jobFinished] to tell the system that it has completed
     * its work, or until the job's required constraints are no longer satisfied.
     *
     * The system holds a wakelock on behalf of your app as long as your job is executing. This
     * wakelock is acquired before this method is invoked, and is not released until either you
     * call [jobFinished], or after the system invokes [onStopJob] to notify your job that it is
     * being shut down prematurely.
     *
     * Returning *false* from this method means your job is already finished. The system's wakelock
     * for the job will be released, and [onStopJob] will not be invoked.
     *
     * First we log the fact that our [MediaContentJob] has started, then we save our parameters in
     * our [JobParameters] field [mRunningParams]. We initialize [StringBuilder] variable `val sb`
     * with a new instance and append the string "Media content has changed:\n" to it. We then
     * branch on whether the `getTriggeredContentAuthorities` method of [params] is *null* or not
     * (list of which content authorities have triggered this job):
     *  * not *null*: we append the string "Authorities: " to `sb`, and initialize [Boolean]
     *  variable `val first` to *true*. Then for each [String] `auth` in the list of strings
     *  returned by the `getTriggeredContentAuthorities` method of [params]: If `first` is *true*
     *  we set it to *false*, if it is *false* we append ", " to `sb` then append `auth` to `sb`.
     *  When done with the list of content authorities that triggered us, we check whether the
     *  `getTriggeredContentUris` method of [params] is not *null* (returns which URIs have
     *  triggered  this job), and if it is not we loop through the [Uri] `uri` in the list of [Uri]
     *  returned by `getTriggeredContentUris` appending a newline followed by the string value of
     *  `uri` to `sb`.
     *  * *null*: we append the string "(No content)" to `sb`
     *
     * We now toast a message containing `sb` converted to a [String], and log it as well, then
     * add [Runnable] field [mWorker] to the queue of [Handler] field [mHandler] to be run after a
     * delay of 10 seconds. Finally we return *true* to indicate that our service will continue
     * running.
     *
     * @param params Parameters specifying info about this job, including the optional extras
     * configured with [JobInfo.Builder.setExtras] (This object serves to identify this specific
     * running job instance when calling [jobFinished])
     * @return *true* if your service will continue running, using a separate thread
     * when appropriate. *false* means that this job has completed its work.
     */
    override fun onStartJob(params: JobParameters): Boolean {
        Log.i("MediaContentJob", "JOB STARTED!")
        mRunningParams = params
        val sb = StringBuilder()
        sb.append("Media content has changed:\n")
        if (params.triggeredContentAuthorities != null) {
            sb.append("Authorities: ")
            var first = true
            for (auth: String? in params.triggeredContentAuthorities!!) {
                if (first) {
                    first = false
                } else {
                    sb.append(", ")
                }
                sb.append(auth)
            }
            if (params.triggeredContentUris != null) {
                for (uri: Uri? in params.triggeredContentUris!!) {
                    sb.append("\n")
                    sb.append(uri)
                }
            }
        } else {
            sb.append("(No content)")
        }
        Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show()
        Log.i("MediaContentJob", "onStartJob called: $sb")
        // We will emulate taking some time to do this work, so we can see batching happen.
        mHandler.postDelayed(mWorker, 10 * 1000.toLong())
        return true
    }

    /**
     * This method is called if the system has determined that you must stop execution of your job
     * even before you've had a chance to call [jobFinished]. We remove any pending posts of
     * [Runnable] field [mWorker] from the queue of [Handler] field [mHandler] and return *false*
     * to end our job entirely.
     *
     * @param params The parameters identifying this job, as supplied to
     * the job in the [onStartJob] callback.
     * @return *true* to indicate to the `JobManager` you'd like to reschedule this job based on
     * the retry criteria provided at job creation-time; or *false* to end the job entirely.
     * Regardless of the value returned, your job must stop executing.
     */
    override fun onStopJob(params: JobParameters): Boolean {
        mHandler.removeCallbacks(mWorker)
        return false
    }

    /**
     * Our static constants and static methods.
     */
    companion object {
        /**
         * [Uri] we observe using our [TriggerContentUri]
         */
        val MEDIA_URI: Uri = Uri.parse("content://" + MediaStore.AUTHORITY + "/")

        /**
         * Called to Schedule a [MediaContentJob] job to be executed. We initialize [JobScheduler]
         * varible `val js` with a handle to the system level service which has the class
         * `JobScheduler::class.java` (API for scheduling various types of jobs against the
         * framework that will be executed in your application's own process). We initialize
         * [JobInfo.Builder] `val builder` with a new instance which uses JobIds.MEDIA_CONTENT_JOB
         * as the job id, and [MediaContentJob] (our [JobService]) to receive the callback from the
         * [JobScheduler]. We add to `builder` the [TriggerContentUri] for the [Uri] field [MEDIA_URI]
         * ("content://media/"), and the flags FLAG_NOTIFY_FOR_DESCENDANTS (also trigger if any
         * descendants of the given URI change). We then use `js` to schedule the job built from
         * `builder`, and log the fact that we have scheduled the [MediaContentJob].
         *
         * @param context [Context] to use to access activity resources, `MediaContentJob.this`
         * when called by the [Runnable] field [mWorker], and `MediaContentObserver.this` when
         * called by the `OnClickListener` `onClick` override of the UI button with id
         * [MediaContentObserver] activity.
         */
        fun scheduleJob(context: Context) {
            val js = context.getSystemService(JobScheduler::class.java)
            val builder = JobInfo.Builder(JobIds.MEDIA_CONTENT_JOB,
                    ComponentName(context, MediaContentJob::class.java))
            builder.addTriggerContentUri(TriggerContentUri(MEDIA_URI,
                    TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS))
            js!!.schedule(builder.build())
            Log.i("MediaContentJob", "JOB SCHEDULED!")
        }

        /**
         * Called to determine if there is currently a job with id `JobIds.MEDIA_CONTENT_JOB` in
         * the list of all jobs that have been scheduled by our application. First we initialize our
         * [JobScheduler] variable `val js` with a handle to the system level service whose class is
         * `JobScheduler.class`. We use `js` to initialize `List<JobInfo>` variable `val jobs` with
         * the list of all jobs that have been scheduled by our application. We loop over `i` for
         * all the [JobInfo] objects in `jobs` returning *true* if the job id of the `i`'th entry is
         * `JobIds.MEDIA_CONTENT_JOB`. If none of the jobs in `jobs` match we return *false* to the
         * caller.
         *
         * @param context [Context] to use to get a handle to the [JobScheduler].
         * @return *true* if there is a job with id `JobIds.MEDIA_CONTENT_JOB` (ours) in the list of
         * all jobs that have been scheduled by our application.
         */
        fun isScheduled(context: Context): Boolean {
            val js = context.getSystemService(JobScheduler::class.java)
            val jobs: MutableList<JobInfo> = js!!.allPendingJobs
            for (i in jobs.indices) {
                if (jobs[i].id == JobIds.MEDIA_CONTENT_JOB) {
                    return true
                }
            }
            return false
        }

        /**
         * Called to cancel the job with id `JobIds.MEDIA_CONTENT_JOB` (ours). First we initialize
         * our [JobScheduler] variable `val js` with a handle to the system level service whose
         * class is `JobScheduler.class`, then use it to cancel the job with id
         * `JobIds.MEDIA_CONTENT_JOB`.
         *
         * @param context `Context` to use to get a handle to the `JobScheduler`.
         */
        fun cancelJob(context: Context) {
            val js = context.getSystemService(JobScheduler::class.java)
            js!!.cancel(JobIds.MEDIA_CONTENT_JOB)
        }
    }
}
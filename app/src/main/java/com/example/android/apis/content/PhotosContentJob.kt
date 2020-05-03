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
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.util.ArrayList

/**
 * Example stub job to monitor when there is a change to photos in the media provider.
 */
@Suppress("MemberVisibilityCanBePrivate")
@RequiresApi(api = Build.VERSION_CODES.N)
class PhotosContentJob : JobService() {
    companion object {
        /**
         * The root URI of the media provider, to monitor for generic changes
         * to its content: "content://media/"
         */
        val MEDIA_URI: Uri = Uri.parse("content://" + MediaStore.AUTHORITY + "/")
        /**
         * Path segments for image-specific URIs in the provider: "external", "images", and "media".
         */
        val EXTERNAL_PATH_SEGMENTS: MutableList<String> = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.pathSegments
        /**
         * The columns we want to retrieve about a particular image.
         */
        @Suppress("DEPRECATION")
        val PROJECTION = arrayOf(
                MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA
        )
        /**
         * Column number of the _ID column in the [Cursor] returned by our query
         */
        const val PROJECTION_ID = 0
        /**
         * Column number of the DATA column in the [Cursor] returned by our query
         */
        const val PROJECTION_DATA = 1
        /**
         * This is the external storage directory where cameras place pictures: /storage/emulated/0/DCIM
         */
        @Suppress("DEPRECATION")
        val DCIM_DIR: String = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM).path
        /**
         * A pre-built JobInfo we use for scheduling our job, built in our *init* block.
         */
        var JOB_INFO: JobInfo? = null

        /**
         * Called to Schedule a [PhotosContentJob] job to be executed, replacing any existing one.
         * We initialize [JobScheduler] variable `val js` with a handle to the system level service
         * which has the class `JobScheduler.class`. We then use `js` to schedule the job described
         * by the prebuilt [JobInfo] static field [JOB_INFO] and log the fact that we have scheduled
         * the [PhotosContentJob].
         *
         * @param context [Context] to use to access activity resources, `PhotosContentJob.this`
         * when called by the [Runnable] field [mWorker], and `MediaContentObserver.this` when
         * called by the `OnClickListener` `onClick` override of the UI button with id
         * R.id.schedule_media_job ("Schedule media job") in the [MediaContentObserver] activity.
         */
        fun scheduleJob(context: Context) {
            val js = context.getSystemService(JobScheduler::class.java)
            js!!.schedule((JOB_INFO)!!)
            Log.i("PhotosContentJob", "JOB SCHEDULED!")
        }

        /**
         * Called to determine if there is currently a job with id [JobIds.PHOTOS_CONTENT_JOB] in
         * the list of all jobs that have been scheduled by our application. First we initialize our
         * [JobScheduler] variable `val js` with a handle to the system level service whose class is
         * `JobScheduler.class`. We use `js` to initialize `MutableList<JobInfo>` variable `val jobs`
         * with the list of all jobs that have been scheduled by our application. If this is *null*
         * we return *false* to the caller. Otherwise we loop over `i` for all the [JobInfo] objects
         * in `jobs` returning *true* if the job id of the `i`'th entry is [JobIds.PHOTOS_CONTENT_JOB].
         * If none of the jobs in `jobs` match we return *false* to the caller.
         *
         * @param context `Context` to use to get a handle to the `JobScheduler`.
         * @return true if there is a job with id `JobIds.PHOTOS_CONTENT_JOB` (ours) in the list of
         * all jobs that have been scheduled by our application.
         */
        fun isScheduled(context: Context): Boolean {
            val js = context.getSystemService(JobScheduler::class.java)
            val jobs: MutableList<JobInfo> = js!!.allPendingJobs
            for (i in jobs.indices) {
                if (jobs[i].id == JobIds.PHOTOS_CONTENT_JOB) {
                    return true
                }
            }
            return false
        }

        /**
         * Called to cancel the job with id [JobIds.PHOTOS_CONTENT_JOB] (ours). First we initialize
         * our [JobScheduler] variable `val js` with a handle to the system level service whose class
         * is `JobScheduler.class`, then use it to cancel the job with id [JobIds.PHOTOS_CONTENT_JOB]
         *
         * @param context [Context] to use to get a handle to the [JobScheduler]
         */
        fun cancelJob(context: Context) {
            val js = context.getSystemService(JobScheduler::class.java)
            js!!.cancel(JobIds.PHOTOS_CONTENT_JOB)
        }

        /**
         * Builds a `JobInfo` for our field `JOB_INFO`. We initialize `JobInfo.Builder` variable
         * `val builder' with a new instance with a job id of `Jobs.PHOTOS_CONTENT_JOB` (2) with the
         * class of `PhotosContentJob` designated as the endpoint that will receive the callback
         * from the `JobScheduler`. We add a `TriggerContentUri` for EXTERNAL_CONTENT_URI (a `Uri`
         * for "content://media/external/images/media") with the flag FLAG_NOTIFY_FOR_DESCENDANTS
         * (also triggers if any descendants of the given URI change). We also add a `TriggerContentUri`
         * for MEDIA_URI ("content://media/") with no flags. Finally we build `builder` and use the
         * `JobInfo` it creates to initialize our field `JOB_INFO`
         */
        init {
            val builder = JobInfo.Builder(JobIds.PHOTOS_CONTENT_JOB,
                    ComponentName("com.example.android.apis", PhotosContentJob::class.java.name))
            // Look for specific changes to images in the provider.
           builder.addTriggerContentUri(TriggerContentUri(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS))
            // Also look for general reports of changes in the overall provider.
            builder.addTriggerContentUri(TriggerContentUri(MEDIA_URI, 0))
            JOB_INFO = builder.build()
        }
    }

    // Fake job work.  A real implementation would do some work on a separate thread.

    /**
     * [Handler] we use to delay our work by 10 seconds so we can see batching happen.
     */
    val mHandler = Handler()

    /**
     * [Runnable] which does all our "work" after a ten second delay. When an object implementing
     * interface [Runnable] is used to create a thread, starting the thread causes the object's
     * `run` method to be called in that separately executing thread. We call our method [scheduleJob]
     * to schedule *this* [PhotosContentJob] to run, then we call the [jobFinished] to inform the
     * [JobScheduler] that the job has finished its work, with *false* as the wants reschedule argument
     * to specify that we do not want the job rescheduled.
     */
    val mWorker: Runnable = Runnable {
        scheduleJob(this@PhotosContentJob)
        jobFinished(mRunningParams, false)
    }

    /**
     * [JobParameters] passed to our [onStartJob] override, used to retrieve information about which
     * content authorities have triggered the job, and to identify the job when calling [jobFinished]
     */
    var mRunningParams: JobParameters? = null

    /**
     * Called to indicate that the job has begun executing. Override this method with the logic for
     * your job. Like all other component lifecycle callbacks, this method executes on your application's
     * main thread.
     *
     * Return *true* from this method if your job needs to continue running. If you do this, the job
     * remains active until you call [jobFinished] to tell the system that it has completed its work,
     * or until the job's required constraints are no longer satisfied.
     *
     * The system holds a wakelock on behalf of your app as long as your job is executing. This
     * wakelock is acquired before this method is invoked, and is not released until either you
     * call [jobFinished], or after the system invokes [onStopJob] to notify your job that it is
     * being shut down prematurely.
     *
     * Returning *false* from this method means your job is already finished. The system's wakelock
     * for the job will be released, and [onStopJob] will **not** be invoked.
     *
     * First we log the fact that our [PhotosContentJob] has started, then we save our parameters in
     * our [JobParameters] field [mRunningParams]. We initialize [StringBuilder] variable `val sb`
     * with a new instance. If the list of content authorities that have triggered our job is *null*
     * we just append the string "(No photos content)" to `sb`. If it is not *null* we initialize our
     * [Boolean] varible `var rescanNeeded` to *false* (this flag if *true* is used to flag the fact
     * that the data that we read from our [JobParameters] parameter [params] suggest that to properly
     * list the content changes will require a rescan of the photos, in which case we append the string
     * "Photos rescan needed!" to `sb`). If the list of URIs that have triggered the job returned by
     * the `getTriggeredContentUris` method of [params] is not *null* we want to iterate through
     * them and collect either the ids that were impacted or note that a generic change has happened,
     * so we initialize `ArrayList<String>` variable `val ids` with a new instance then for each
     * [Uri] `uri` in the list of URIs that have triggered the job returned by the `getTriggeredContentUris`
     * method of [params] we initialize `MutableList<String>` variable `val path` with the decoded
     * path segments of `uri`, each without a leading or trailing '/'. If `path` is not *null* and
     * its size is one more than the size of EXTERNAL_PATH_SEGMENTS then the last entry in `path` is
     * a filename so we add that last entry to our list `ids`, otherwise there is some general
     * change in the photos so we set `rescanNeeded` to true.
     *
     * When done processing the changed URIs in [params], we check if the size of `ids` is greater
     * than 0 (we found some ids that changed), and if so we proceed to determine what they are.
     * To do this we initialize [StringBuilder] variable `val selection` with a new instance then
     * loop over `i` for all the entries in `ids` first appending the string " OR " to `selection`
     * following every selection clause in `selection` (by skipping doing this if `selection` is not
     * empty). We then append the string "_id" followed by the string "='" followed by the `i`'th
     * entry in `ids` followed by the string "'" (forming a selection query like _id='88').
     *
     * When done forming our selection string in `selection` we initialize [Cursor] variable
     * `var cursor` to *null* and [Boolean] `var haveFiles` to *false*. Then wrapped in a try block
     * intended to catch [SecurityException], and whose finally block closes [Cursor] `cursor` if it
     * is not *null* we:
     *
     *  * We use a `ContentResolver` instance for our application's package to query the URI
     *  EXTERNAL_CONTENT_URI ("content://media/external/images/media") for the projection
     *  PROJECTION (the _ID and DATA columns) with the selection consisting of the string
     *  value of `selection` with *null* for the selection arguments and *null* for the sort
     *  order saving the [Cursor] returned in `cursor`.
     *  * We now loop while the `moveToNext` method of `cursor` returns *true* when moving to
     *  the next row (*false* is returned if it is past the last entry). We initialize [String]
     *  variable `val dir` by retrieving the string in column PROJECTION_DATA of `cursor`. If
     *  `dir` starts with the string DCIM_DIR, we check if `haveFiles` is *false* (first time
     *  through) and if so we set it to *true* and append the string "New photos:\n" to `sb`.
     *  Then we append the string value of the int in column PROJECTION_ID of `cursor` to `sb`,
     *  followed by the string ": ", followed by `dir` followed by a newline, then loop back
     *  for the next row.
     *
     * After adding any filenames that have changed to `sb`, or adding text to the effect that
     * we did not find any we toast the string value of `sb`, then add a delayed execution of
     * [Runnable] field [mWorker] to the queue of [Handler] field [mHandler], and finally we
     * return *true* to the caller indicating that our service will continue running.
     *
     * @param params [JobParameters] specifying info about this job, including the optional extras
     * configured with [JobInfo.Builder.setExtras] (serves to identify this specific running job
     * instance when calling [jobFinished])
     * @return *true* if your service will continue running, using a separate thread when
     * appropriate. *false* means that this job has completed its work.
     */
    override fun onStartJob(params: JobParameters): Boolean {
        Log.i("PhotosContentJob", "JOB STARTED!")
        mRunningParams = params
        /**
         * Instead of real work, we are going to build a string to show to the user.
         */
        val sb = StringBuilder()
        /**
         * Did we trigger due to a content change?
         */
        if (params.triggeredContentAuthorities != null) {
            var rescanNeeded = false
            if (params.triggeredContentUris != null) {
                /**
                 * If we have details about which URIs changed, then iterate through them
                 * and collect either the ids that were impacted or note that a generic
                 * change has happened.
                 */
                val ids = ArrayList<String>()
                for (uri: Uri in params.triggeredContentUris!!) {
                    val path = uri.pathSegments
                    if (path != null && path.size == EXTERNAL_PATH_SEGMENTS.size + 1) {
                        /**
                         * This is a specific file.
                         */
                        ids.add(path[path.size - 1])
                    } else {
                        /**
                         * Oops, there is some general change!
                         */
                        rescanNeeded = true
                    }
                }
                if (ids.size > 0) {
                    /**
                     * If we found some ids that changed, we want to determine what they are.
                     * First, we do a query with content provider to ask about all of them.
                     */
                    val selection = StringBuilder()
                    for (i in ids.indices) {
                        if (selection.isNotEmpty()) {
                            selection.append(" OR ")
                        }
                        selection.append(MediaStore.Images.ImageColumns._ID)
                        selection.append("='")
                        selection.append(ids[i])
                        selection.append("'")
                    }
                    /**
                     * Now we iterate through the query, looking at the filenames of
                     * the items to determine if they are ones we are interested in.
                     */
                    var cursor: Cursor? = null
                    var haveFiles = false
                    try {
                        cursor = contentResolver.query(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                PROJECTION, selection.toString(), null, null)
                        while (cursor!!.moveToNext()) {
                            /**
                             * We only care about files in the DCIM directory.
                             */
                            val dir = cursor.getString(PROJECTION_DATA)
                            if (dir.startsWith(DCIM_DIR)) {
                                if (!haveFiles) {
                                    haveFiles = true
                                    sb.append("New photos:\n")
                                }
                                sb.append(cursor.getInt(PROJECTION_ID))
                                sb.append(": ")
                                sb.append(dir)
                                sb.append("\n")
                            }
                        }
                    } catch (e: SecurityException) {
                        sb.append("Error: no access to media!")
                    } finally {
                        cursor?.close()
                    }
                }
            } else {
                /**
                 * We don't have any details about URIs (because too many changed at once),
                 * so just note that we need to do a full rescan.
                 */
                rescanNeeded = true
            }
            if (rescanNeeded) {
                sb.append("Photos rescan needed!")
            }
        } else {
            sb.append("(No photos content)")
        }
        Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show()
        /**
         * We will emulate taking some time to do this work, so we can see batching happen.
         */
        mHandler.postDelayed(mWorker, 10 * 1000.toLong())
        return true
    }

    /**
     * This method is called if the system has determined that you must stop execution of your job
     * even before you've had a chance to call [jobFinished]. We remove any pending posts of
     * [Runnable] field [mWorker] from the queue of [Handler] field [mHandler] and return *false* to
     * end our job entirely.
     *
     * @param params The [JobParameters] identifying this job, as supplied to the job in the
     * [onStartJob] callback.
     * @return *true* to indicate to the `JobManager` whether you'd like to reschedule this job
     * based on the retry criteria provided at job creation-time; or *false* to end the job entirely.
     * Regardless of the value returned, your job must stop executing.
     */
    override fun onStopJob(params: JobParameters): Boolean {
        mHandler.removeCallbacks(mWorker)
        return false
    }
}
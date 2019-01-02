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
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import androidx.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Example stub job to monitor when there is a change to photos in the media provider.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class PhotosContentJob extends JobService {
    /**
     * The root URI of the media provider, to monitor for generic changes
     * to its content: "content://media/"
     */
    static final Uri MEDIA_URI = Uri.parse("content://" + MediaStore.AUTHORITY + "/");

    /**
     * Path segments for image-specific URIs in the provider: "external", "images", and "media".
     */
    static final List<String> EXTERNAL_PATH_SEGMENTS
            = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.getPathSegments();

    /**
     * The columns we want to retrieve about a particular image.
     */
    static final String[] PROJECTION = new String[] {
            MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA
    };
    /**
     * Column number of the _ID column in the {@code Cursor} returned by our query
     */
    static final int PROJECTION_ID = 0;
    /**
     * Column number of the DATA column in the {@code Cursor} returned by our query
     */
    static final int PROJECTION_DATA = 1;

    /**
     * This is the external storage directory where cameras place pictures: /storage/emulated/0/DCIM
     */
    static final String DCIM_DIR = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DCIM).getPath();

    /**
     * A pre-built JobInfo we use for scheduling our job, built in the following static block.
     */
    static final JobInfo JOB_INFO;

    /*
     * Builds a JobInfo for our field JOB_INFO. We initialize 'JobInfo.Builder builder' with a new
     * instance with a job id of PHOTOS_CONTENT_JOB (2) with the class of PhotosContentJob designated
     * as the endpoint that will receive the callback from the JobScheduler. We add a TriggerContentUri
     * for EXTERNAL_CONTENT_URI (a Uri for "content://media/external/images/media") with the flag
     * FLAG_NOTIFY_FOR_DESCENDANTS (also triggers if any descendants of the given URI change). We also
     * add a TriggerContentUri for MEDIA_URI ("content://media/") with no flags. Finally we build
     * 'builder' and use the JobInfo it creates to initialize JOB_INFO
     */
    static {
        JobInfo.Builder builder = new JobInfo.Builder(JobIds.PHOTOS_CONTENT_JOB,
                new ComponentName("com.example.android.apis", PhotosContentJob.class.getName()));
        // Look for specific changes to images in the provider.
        builder.addTriggerContentUri(new JobInfo.TriggerContentUri(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS));
        // Also look for general reports of changes in the overall provider.
        builder.addTriggerContentUri(new JobInfo.TriggerContentUri(MEDIA_URI, 0));
        JOB_INFO = builder.build();
    }

    // Fake job work.  A real implementation would do some work on a separate thread.
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
         * {@code PhotosContentJob} to run, then we call the {@code jobFinished} to inform the
         * JobScheduler that the job has finished its work, with false as the wants reschedule
         * to specify that we do not want the job rescheduled.
         */
        @Override
        public void run() {
            scheduleJob(PhotosContentJob.this);
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
     * Called to Schedule a {@code PhotosContentJob} job to be executed, replacing any existing one.
     * We initialize {@code JobScheduler js} with a handle to the system level service which has the
     * class {@code JobScheduler.class}. We then use {@code js} to schedule the job described by the
     * prebuilt {@code JobInfo JOB_INFO} and log the fact that we have scheduled the {@code PhotosContentJob}.
     *
     * @param context {@code Context} to use to access activity resources, {@code PhotosContentJob.this}
     *                when called by the {@code Runnable mWorker}, and {@code MediaContentObserver.this}
     *                when called by the {@code OnClickListener} {@code onClick} override of the UI
     *                button with id R.id.schedule_media_job ("Schedule media job") in the
     *                {@code MediaContentObserver} activity.
     */
    public static void scheduleJob(Context context) {
        JobScheduler js = context.getSystemService(JobScheduler.class);
        //noinspection ConstantConditions
        js.schedule(JOB_INFO);
        Log.i("PhotosContentJob", "JOB SCHEDULED!");
    }

    /**
     * Called to determine if there is currently a job with id {@code JobIds.PHOTOS_CONTENT_JOB} in
     * the list of all jobs that have been scheduled by our application. First we initialize our
     * variable {@code JobScheduler js} with a handle to the system level service whose class is
     * {@code JobScheduler.class}. We use {@code js} to initialize {@code List<JobInfo> jobs} with
     * the list of all jobs that have been scheduled by our application. If this is null we return
     * false to the caller. Otherwise we loop over i for all the {@code JobInfo} objects in {@code jobs}
     * returning true if the job id of the i'th entry is {@code JobIds.PHOTOS_CONTENT_JOB}. If none of
     * the jobs in {@code jobs} match we return false to the caller.
     *
     * @param context {@code Context} to use to get a handle to the {@code JobScheduler}.
     * @return true if there is a job with id {@code JobIds.PHOTOS_CONTENT_JOB} (ours) in the list of
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
            if (jobs.get(i).getId() == JobIds.PHOTOS_CONTENT_JOB) {
                return true;
            }
        }
        return false;
    }

    /**
     * Called to cancel the job with id {@code JobIds.PHOTOS_CONTENT_JOB} (ours). First we initialize
     * our variable {@code JobScheduler js} with a handle to the system level service whose class is
     * {@code JobScheduler.class}, then use it to cancel the job with id {@code JobIds.PHOTOS_CONTENT_JOB}.
     *
     * @param context {@code Context} to use to get a handle to the {@code JobScheduler}.
     */
    public static void cancelJob(Context context) {
        JobScheduler js = context.getSystemService(JobScheduler.class);
        //noinspection ConstantConditions
        js.cancel(JobIds.PHOTOS_CONTENT_JOB);
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
     * First we log the fact that our PhotosContentJob has started, then we save our parameters in our
     * field {@code JobParameters mRunningParams}. We initialize {@code StringBuilder sb} with a new
     * instance. If the list of content authorities that have triggered our job is null we just append
     * the string "(No photos content)" to {@code sb}. If it is not null we initialize our flag
     * {@code boolean rescanNeeded} to false (this flag if true is used to flag the fact that the data
     * that we read from our parameter {@code JobParameters params} suggest that to properly list the
     * content changes will require a rescan of the photos, in which case we append the string "Photos
     * rescan needed!" to {@code sb}). If the list of URIs that have triggered the job returned by the
     * {@code getTriggeredContentUris} method of {@code params} is not null we want to iterate through
     * them and collect either the ids that were impacted or note that a generic change has happened,
     * so we initialize {@code ArrayList<String> ids} with a new instance then for each {@code Uri uri}
     * in the list of URIs that have triggered the job returned by the {@code getTriggeredContentUris}
     * method of {@code params} we initialize {@code List<String> path} with the decoded path segments
     * of {@code uri}, each without a leading or trailing '/'. If {@code path} is not null and its size
     * is one more than the size of EXTERNAL_PATH_SEGMENTS then the last entry in {@code path} is a
     * filename so we add that last entry to our list {@code ids}, otherwise there is some general
     * change in the photos so we set {@code rescanNeeded} to true.
     * <p>
     * When done processing the changed URIs in {@code params}, we check if the size of {@code ids}
     * is greater than 0 (we found some ids that changed), and if so we proceed to determine what they
     * are. To do this we initialize {@code StringBuilder selection} with a new instance then loop over
     * i for all the entries in {@code ids} first appending the string " OR " to {@code selection} following
     * every selection clause in {@code selection} (by skipping this if the size of {@code selection}
     * is 0). We then append the string "_id" followed by the string "='" followed by the i'th entry
     * in {@code ids} followed by the string "'" (forming a selection query like _id='88').
     * <p>
     * When done forming our selection string in {@code selection} we initialize {@code Cursor cursor}
     * to null and {@code boolean haveFiles} to false. Then wrapped in a try block intended to catch
     * SecurityException, and whose finally block closes {@code Cursor cursor} if it is not null we:
     * <ul>
     *     <li>
     *         We use a ContentResolver instance for our application's package to query the URI
     *         EXTERNAL_CONTENT_URI ("content://media/external/images/media") for the projection
     *         PROJECTION (the _ID and DATA columns) with the selection consisting of the string
     *         value of {@code selection} with null for the selection arguments and null for the
     *         sort order saving the {@code Cursor} returned in {@code cursor}.
     *     </li>
     *     <li>
     *         We now loop while the {@code moveToNext} method of {@code cursor} returns true when
     *         moving to the next row (false is returned if it is past the last entry). We initialize
     *         {@code String dir} by retrieving the string in column PROJECTION_DATA of {@code cursor}.
     *         If {@code dir} starts with the string DCIM_DIR, we check if {@code haveFiles} is false
     *         (first time through) and if so we set it to true and append the string "New photos:\n"
     *         to {@code sb}. Then we append the string value of the int in column PROJECTION_ID of
     *         {@code cursor} to {@code sb}, followed by the string ": ", followed by {@code dir}
     *         followed by a newline, then loop back for the next row.
     *     </li>
     * </ul>
     * After adding any filenames that have changed to {@code sb}, or adding text to the effect that
     * we did not find any we toast the string value of {@code sb}, then add a delayed execution of
     * {@code Runnable mWorker} to the queue of {@code Handler mHandler} we return true to the caller
     * indicating that our service will continue running.
     *
     * @param params Parameters specifying info about this job, including the optional
     *     extras configured with {@link JobInfo.Builder#setExtras(android.os.PersistableBundle).
     *     This object serves to identify this specific running job instance when calling
     *     {@link #jobFinished(JobParameters, boolean)}.
     * @return {@code true} if your service will continue running, using a separate thread
     *     when appropriate. {@code false} means that this job has completed its work.
     */
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i("PhotosContentJob", "JOB STARTED!");
        mRunningParams = params;

        // Instead of real work, we are going to build a string to show to the user.
        StringBuilder sb = new StringBuilder();

        // Did we trigger due to a content change?
        if (params.getTriggeredContentAuthorities() != null) {
            boolean rescanNeeded = false;

            if (params.getTriggeredContentUris() != null) {
                // If we have details about which URIs changed, then iterate through them
                // and collect either the ids that were impacted or note that a generic
                // change has happened.
                ArrayList<String> ids = new ArrayList<>();
                for (Uri uri : params.getTriggeredContentUris()) {
                    List<String> path = uri.getPathSegments();
                    if (path != null && path.size() == EXTERNAL_PATH_SEGMENTS.size()+1) {
                        // This is a specific file.
                        ids.add(path.get(path.size()-1));
                    } else {
                        // Oops, there is some general change!
                        rescanNeeded = true;
                    }
                }

                if (ids.size() > 0) {
                    // If we found some ids that changed, we want to determine what they are.
                    // First, we do a query with content provider to ask about all of them.
                    StringBuilder selection = new StringBuilder();
                    for (int i=0; i<ids.size(); i++) {
                        if (selection.length() > 0) {
                            selection.append(" OR ");
                        }
                        selection.append(MediaStore.Images.ImageColumns._ID);
                        selection.append("='");
                        selection.append(ids.get(i));
                        selection.append("'");
                    }

                    // Now we iterate through the query, looking at the filenames of
                    // the items to determine if they are ones we are interested in.
                    Cursor cursor = null;
                    boolean haveFiles = false;
                    //noinspection TryFinallyCanBeTryWithResources
                    try {
                        cursor = getContentResolver().query(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                PROJECTION, selection.toString(), null, null);
                        //noinspection ConstantConditions
                        while (cursor.moveToNext()) {
                            // We only care about files in the DCIM directory.
                            String dir = cursor.getString(PROJECTION_DATA);
                            if (dir.startsWith(DCIM_DIR)) {
                                if (!haveFiles) {
                                    haveFiles = true;
                                    sb.append("New photos:\n");
                                }
                                sb.append(cursor.getInt(PROJECTION_ID));
                                sb.append(": ");
                                sb.append(dir);
                                sb.append("\n");
                            }
                        }
                    } catch (SecurityException e) {
                        sb.append("Error: no access to media!");
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }

            } else {
                // We don't have any details about URIs (because too many changed at once),
                // so just note that we need to do a full rescan.
                rescanNeeded = true;
            }

            if (rescanNeeded) {
                sb.append("Photos rescan needed!");
            }
        } else {
            sb.append("(No photos content)");
        }
        Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();

        // We will emulate taking some time to do this work, so we can see batching happen.
        mHandler.postDelayed(mWorker, 10*1000);
        return true;
    }

    /**
     * This method is called if the system has determined that you must stop execution of your job
     * even before you've had a chance to call {@link #jobFinished(JobParameters, boolean)}. We
     * remove any pending posts of {@code Runnable mWorker} from the queue of {@code Handler mHandler}
     * and return false to end our job entirely.
     *
     * @param params The parameters identifying this job, as supplied to
     *               the job in the {@link #onStartJob(JobParameters)} callback.
     * @return {@code true} to indicate to the JobManager whether you'd like to reschedule
     * this job based on the retry criteria provided at job creation-time; or {@code false}
     * to end the job entirely.  Regardless of the value returned, your job must stop executing.
     */
    @Override
    public boolean onStopJob(JobParameters params) {
        mHandler.removeCallbacks(mWorker);
        return false;
    }
}

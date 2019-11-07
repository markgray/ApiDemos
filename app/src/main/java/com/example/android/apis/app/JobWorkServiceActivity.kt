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

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.app.job.JobWorkItem
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.view.View
import android.widget.Button

import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

import com.example.android.apis.R

/**
 * Example of interacting with [JobWorkService].
 */
@Suppress("MemberVisibilityCanBePrivate")
@RequiresApi(api = Build.VERSION_CODES.O)
class JobWorkServiceActivity : AppCompatActivity() {
    /**
     * Handle to the JOB_SCHEDULER_SERVICE system level service
     */
    internal lateinit var mJobScheduler: JobScheduler
    /**
     * Container of data passed to the [android.app.job.JobScheduler] fully encapsulating the
     * parameters required to schedule work against the calling application. Uses the resource id
     * R.string.job_service_created as the job id, and specifies the class of [JobWorkService]
     * to receive the callback from the JobScheduler.
     */
    internal lateinit var mJobInfo: JobInfo

    /**
     * Called when the button with id R.id.enqueue1 is clicked. We use our [JobScheduler] field
     * [mJobScheduler] to enqueue a [JobWorkItem] whose [Intent] has an action of
     * "com.example.android.apis.ONE" and the string "One" stored as an extra under the key "name"
     * to the work queue of the work service specified in our [JobInfo] field [mJobInfo] (the
     * `JobService` class [JobWorkService]).
     *
     * Parameter: View that was clicked.
     */
    private val mEnqueue1Listener = View.OnClickListener {
        mJobScheduler.enqueue(mJobInfo, JobWorkItem(
                Intent("com.example.android.apis.ONE").putExtra("name", "One")))
    }

    /**
     * Called when the button with id R.id.enqueue2 is clicked. We use our [JobScheduler] field
     * [mJobScheduler] to enqueue a [JobWorkItem] whose [Intent] has an action of
     * "com.example.android.apis.TWO" and the string "Two" stored as an extra under the key "name"
     * to the work queue of the work service specified in our [JobInfo] field [mJobInfo] (the
     * `JobService` class [JobWorkService]).
     *
     * Parameter: View that was clicked.
     */
    private val mEnqueue2Listener = View.OnClickListener {
        mJobScheduler.enqueue(mJobInfo, JobWorkItem(
                Intent("com.example.android.apis.TWO").putExtra("name", "Two")))
    }

    /**
     * Called when the button with id R.id.enqueue3 is clicked. We use our [JobScheduler] field
     * [mJobScheduler] to enqueue a [JobWorkItem] whose [Intent] has an action of
     * "com.example.android.apis.THREE" and the string "Three" stored as an extra under the key
     * "name" to the work queue of the work service specified in our [JobInfo] field [mJobInfo] (the
     * `JobService` class [JobWorkService]).
     *
     * Parameter: View that was clicked.
     */
    private val mEnqueue3Listener = View.OnClickListener {
        mJobScheduler.enqueue(mJobInfo, JobWorkItem(
                Intent("com.example.android.apis.THREE").putExtra("name", "Three")))
    }

    /**
     * Called when the button with id R.id.kill is clicked. We call the [Process.killProcess]
     * method with the pid of this process.
     *
     * Parameter: View that was clicked.
     */
    private val mKillListener = View.OnClickListener {
        // This is to simulate the service being killed while it is
        // running in the background.
        Process.killProcess(Process.myPid())
    }

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.job_work_service_activity.
     * Next we initialize our [JobScheduler] field [mJobScheduler] with a handle to the
     * JOB_SCHEDULER_SERVICE system level service. We initialize our [JobInfo] field [mJobInfo] by
     * using a [JobInfo.Builder] specifying the resource id R.string.job_service_created as the job
     * id, and the class of [JobWorkService] to receive the callback from the `JobScheduler`,
     * setting its deadline to 0 milliseconds and then building the [JobInfo].
     * We find the buttons in our UI in order to set their `OnClickListener` as follows:
     *  - R.id.enqueue1: `OnClickListener` field [mEnqueue1Listener] enqueues a [JobWorkItem]
     * for [JobWorkService] with an `Intent` whose action is "com.example.android.apis.ONE"
     * and which has an extra storing the string "One" under the key "name".
     *  - R.id.enqueue2: `OnClickListener` field [mEnqueue2Listener] enqueues a [JobWorkItem]
     * for [JobWorkService] with an `Intent` whose action is "com.example.android.apis.TWO"
     * and which has an extra storing the string "Two" under the key "name".
     *  - R.id.enqueue3: `OnClickListener` field [mEnqueue3Listener] enqueues a [JobWorkItem]
     * for [JobWorkService] with an `Intent` whose action is "com.example.android.apis.THREE"
     * and which has an extra storing the string "Three" under the key "name".
     *  - R.id.kill: `OnClickListener` field [mKillListener] calls the [Process.killProcess]
     * method with the pid of this process.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.job_work_service_activity)

        mJobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        mJobInfo = JobInfo.Builder(R.string.job_service_created,
                ComponentName(this, JobWorkService::class.java)).setOverrideDeadline(0).build()

        // Watch for button clicks.
        var button = findViewById<Button>(R.id.enqueue1)
        button.setOnClickListener(mEnqueue1Listener)
        button = findViewById(R.id.enqueue2)
        button.setOnClickListener(mEnqueue2Listener)
        button = findViewById(R.id.enqueue3)
        button.setOnClickListener(mEnqueue3Listener)
        button = findViewById(R.id.kill)
        button.setOnClickListener(mKillListener)
    }
}

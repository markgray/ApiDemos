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

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.job.JobWorkItem;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.view.View;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android.apis.R;

/**
 * Example of interacting with {@link JobWorkService}.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class JobWorkServiceActivity extends AppCompatActivity {
    /**
     * Handle to the JOB_SCHEDULER_SERVICE system level service
     */
    JobScheduler mJobScheduler;
    /**
     * Container of data passed to the {@link android.app.job.JobScheduler} fully encapsulating the
     * parameters required to schedule work against the calling application. Uses the resource id
     * R.string.job_service_created as the job id, and specifies the class of {@code JobWorkService}
     * to receive the callback from the JobScheduler.
     */
    JobInfo mJobInfo;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.job_work_service_activity.
     * Next we initialize our field {@code JobScheduler mJobScheduler} with a handle to the JOB_SCHEDULER_SERVICE
     * system level service. We initialize our field {@code JobInfo mJobInfo} by using a {@code JobInfo.Builder}
     * specifying the resource id R.string.job_service_created as the job id, and the class of {@code JobWorkService}
     * to receive the callback from the JobScheduler, setting its deadline to 0 milliseconds and then
     * building the {@code JobInfo}.
     * <p>
     * We find the buttons in our UI in order to set their {@code OnClickListener} as follows:
     * <ul>
     *     <li>
     *         R.id.enqueue1: {@code OnClickListener mEnqueue1Listener} enqueues a {@code JobWorkItem}
     *         for {@code JobWorkService} with an {@code Intent} whose action is "com.example.android.apis.ONE"
     *         and which has an extra storing the string "One" under the key "name".
     *     </li>
     *     <li>
     *         R.id.enqueue2: {@code OnClickListener mEnqueue2Listener} enqueues a {@code JobWorkItem}
     *         for {@code JobWorkService} with an {@code Intent} whose action is "com.example.android.apis.TWO"
     *         and which has an extra storing the string "Two" under the key "name".
     *     </li>
     *     <li>
     *         R.id.enqueue3: {@code OnClickListener mEnqueue3Listener} enqueues a {@code JobWorkItem}
     *         for {@code JobWorkService} with an {@code Intent} whose action is "com.example.android.apis.THREE"
     *         and which has an extra storing the string "Three" under the key "name".
     *     </li>
     *     <li>
     *         R.id.kill: {@code OnClickListener mKillListener} calls the {@code Process.killProcess}
     *         method with the pid of this process.
     *     </li>
     * </ul>
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.job_work_service_activity);

        mJobScheduler = (JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE);
        mJobInfo = new JobInfo.Builder(R.string.job_service_created,
                new ComponentName(this, JobWorkService.class)).setOverrideDeadline(0).build();

        // Watch for button clicks.
        Button button = findViewById(R.id.enqueue1);
        button.setOnClickListener(mEnqueue1Listener);
        button = findViewById(R.id.enqueue2);
        button.setOnClickListener(mEnqueue2Listener);
        button = findViewById(R.id.enqueue3);
        button.setOnClickListener(mEnqueue3Listener);
        button = findViewById(R.id.kill);
        button.setOnClickListener(mKillListener);
    }

    /**
     * {@code OnClickListener} for the button with id R.id.enqueue1
     */
    private View.OnClickListener mEnqueue1Listener = new View.OnClickListener() {
        /**
         * Called when the button with id R.id.enqueue1 is clicked. We use {@code JobScheduler mJobScheduler}
         * to enqueue a {@code JobWorkItem} whose {@code Intent} has an action of "com.example.android.apis.ONE"
         * and the string "One" stored as an extra under the key "name" to the work queue of the work service
         * specified in {@code JobInfo mJobInfo} (the {@code JobService} class {@code JobWorkService}).
         *
         * @param v View that was clicked.
         */
        @Override
        public void onClick(View v) {
            mJobScheduler.enqueue(mJobInfo, new JobWorkItem(
                    new Intent("com.example.android.apis.ONE").putExtra("name", "One")));
        }
    };

    /**
     * {@code OnClickListener} for the button with id R.id.enqueue2
     */
    private View.OnClickListener mEnqueue2Listener = new View.OnClickListener() {
        /**
         * Called when the button with id R.id.enqueue2 is clicked. We use {@code JobScheduler mJobScheduler}
         * to enqueue a {@code JobWorkItem} whose {@code Intent} has an action of "com.example.android.apis.TWO"
         * and the string "Two" stored as an extra under the key "name" to the work queue of the work service
         * specified in {@code JobInfo mJobInfo} (the {@code JobService} class {@code JobWorkService}).
         *
         * @param v View that was clicked.
         */
        @Override
        public void onClick(View v) {
            mJobScheduler.enqueue(mJobInfo, new JobWorkItem(
                    new Intent("com.example.android.apis.TWO").putExtra("name", "Two")));
        }
    };

    /**
     * {@code OnClickListener} for the button with id R.id.enqueue3
     */
    private View.OnClickListener mEnqueue3Listener = new View.OnClickListener() {
        /**
         * Called when the button with id R.id.enqueue3 is clicked. We use {@code JobScheduler mJobScheduler}
         * to enqueue a {@code JobWorkItem} whose {@code Intent} has an action of "com.example.android.apis.THREE"
         * and the string "Three" stored as an extra under the key "name" to the work queue of the work service
         * specified in {@code JobInfo mJobInfo} (the {@code JobService} class {@code JobWorkService}).
         *
         * @param v View that was clicked.
         */
        @Override
        public void onClick(View v) {
            mJobScheduler.enqueue(mJobInfo, new JobWorkItem(
                    new Intent("com.example.android.apis.THREE").putExtra("name", "Three")));
        }
    };

    /**
     * {@code OnClickListener} for the button with id R.id.kill
     */
    private View.OnClickListener mKillListener = new View.OnClickListener() {
        /**
         * Called when the button with id R.id.kill is clicked. We call the {@code Process.killProcess}
         * method with the pid of this process.
         *
         * @param v View that was clicked.
         */
        @Override
        public void onClick(View v) {
            // This is to simulate the service being killed while it is
            // running in the background.
            Process.killProcess(Process.myPid());
        }
    };
}

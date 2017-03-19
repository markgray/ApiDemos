/*
 * Copyright (C) 2007 The Android Open Source Project
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

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import com.example.android.apis.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Controller to start and stop a service. The service will update a status bar
 * notification every 5 seconds for a minute.
 */
public class NotifyingController extends Activity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.notifying_controller.
     * We locate {@code Button button} at ID R.id.notifyStart and set its {@code OnClickListener} to
     * {@code OnClickListener mStartListener} which will start the service {@code NotifyingService}
     * running when clicked, and set the {@code OnClickListener} of the Button with ID R.id.notifyStop
     * to {@code OnClickListener mStopListener} which will stop the service {@code NotifyingService}
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.notifying_controller);

        Button button = (Button) findViewById(R.id.notifyStart);
        button.setOnClickListener(mStartListener);
        button = (Button) findViewById(R.id.notifyStop);
        button.setOnClickListener(mStopListener);
    }

    /**
     * {@code OnClickListener} for the Button R.id.notifyStart, it starts the service {@code NotifyingService}
     * running in its {@code onClick} callback.
     */
    private OnClickListener mStartListener = new OnClickListener() {
        /**
         * Called when the R.id.notifyStart Button is clicked, it simply creates an {@code Intent} to
         * start the service {@code NotifyingService} and starts it running.
         *
         * @param v View of the Button that was clicked
         */
        @Override
        public void onClick(View v) {
            startService(new Intent(NotifyingController.this,
                    NotifyingService.class));
        }
    };

    /**
     * {@code OnClickListener} for the Button R.id.notifyStop, it stops the service {@code NotifyingService}
     * running in its {@code onClick} callback.
     */
    private OnClickListener mStopListener = new OnClickListener() {
        /**
         * Called when the R.id.notifyStopButton is clicked, it simply creates an {@code Intent} for
         * the service {@code NotifyingService} and stops it running.
         *
         * @param v View of the Button that was clicked
         */
        @Override
        public void onClick(View v) {
            stopService(new Intent(NotifyingController.this,
                    NotifyingService.class));
        }
    };
}


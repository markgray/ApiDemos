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

package com.example.android.apis.app

import android.content.Intent
import android.os.Bundle
import android.view.View.OnClickListener
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Controller to start and stop a service. The service will update a status bar
 * notification every 5 seconds for a minute.
 */
class NotifyingController : AppCompatActivity() {

    /**
     * Called when the R.id.notifyStart Button is clicked, it simply creates an [Intent] to
     * start the service [NotifyingService] and starts it running.
     *
     * Parameter: View of the [Button] that was clicked
     */
    private val mStartListener = OnClickListener {
        startService(
            Intent(this@NotifyingController, NotifyingService::class.java)
        )
    }

    /**
     * Called when the R.id.notifyStopButton is clicked, it simply creates an [Intent] for
     * the service [NotifyingService] and stops it running.
     *
     * Parameter: View of the Button that was clicked
     */
    private val mStopListener = OnClickListener {
        stopService(
            Intent(this@NotifyingController, NotifyingService::class.java)
        )
    }

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.notifying_controller.
     * We locate the [Button] with ID R.id.notifyStart to initialize our variabel `var button` and
     * set its [OnClickListener] to our [OnClickListener] field [mStartListener] which will start
     * the service [NotifyingService] running when clicked, and set the [OnClickListener] of the
     * [Button] with ID R.id.notifyStop to our [OnClickListener] field [mStopListener] which will
     * stop the service [NotifyingService]
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.notifying_controller)

        var button = findViewById<Button>(R.id.notifyStart)
        button.setOnClickListener(mStartListener)
        button = findViewById(R.id.notifyStop)
        button.setOnClickListener(mStopListener)
    }
}


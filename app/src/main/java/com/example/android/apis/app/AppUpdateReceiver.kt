/*
 * Copyright (C) 2011 The Android Open Source Project
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
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.android.apis.R

/**
 * Executed when a new version of the application is installed. It has an `action` intent filter
 * for the action android:name="android.intent.action.MY_PACKAGE_REPLACED"
 */
class AppUpdateReceiver : BroadcastReceiver() {
    /**
     * This method is called when the [BroadcastReceiver] is receiving an [Intent] broadcast for the
     * action android:name="android.intent.action.MY_PACKAGE_REPLACED". We just toast the fact that
     * our `ApiDemos` app has been updated.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context, R.string.app_update_received, Toast.LENGTH_SHORT).show()
    }
}
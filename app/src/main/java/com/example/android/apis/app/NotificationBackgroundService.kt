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

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View.OnClickListener
import android.widget.Button

import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R

/**
 * Example service that gets launched from a notification and runs in the background.
 * Its label is "App/Notification/Background Service"
 */
@RequiresApi(api = Build.VERSION_CODES.O)
class NotificationBackgroundService : Service() {
    /**
     * Called by the system every time a client explicitly starts the service by calling
     * [android.content.Context.startService], providing the arguments it supplied and a
     * unique integer token representing the start request. We fetch a handle to the
     * NOTIFICATION_SERVICE system level service and use it to cancel the notification with
     * id `R.layout.notification_background_service` (the one that launched us). We then call
     * the [stopSelf] method with our parameter [startId] to stop this service. Finally we
     * return START_NOT_STICKY to take the service out of the started state and prevent it
     * from being recreated until a future explicit call to [Context.startService].
     *
     * @param intent The [Intent] supplied to [android.content.Context.startService]. Unused
     * @param flags Additional data about this start request. Unused
     * @param startId A unique integer representing this specific request to start.
     * @return The return value indicates what semantics the system should use for the service's
     * current started state. It may be one of the constants associated with the
     * [Service.START_CONTINUATION_MASK] bits. We return [Service.START_NOT_STICKY] (if this
     * service's process is killed while it is started (after returning from `onStartCommand`),
     * and there are no new start intents to deliver to it, then take the service out of the
     * started state and don't recreate until a future explicit call to [Context.startService].
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .cancel(R.layout.notification_background_service)
        stopSelf(startId)
        return START_NOT_STICKY
    }

    /**
     * Return the communication channel to the service. We return *null* because clients cannot
     * bind to us.
     *
     * @param intent The [Intent] that was used to bind to this service
     * @return Return an [IBinder] through which clients can call on to the service, we return
     * *null* because clients cannot bind to us
     */
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    /**
     * Demo UI that allows the user to post the notification.
     */
    class Controller : AppCompatActivity() {
        /**
         * Handle to the NOTIFICATION_SERVICE system level service.
         */
        private var mNM: NotificationManager? = null

        /**
         * Called when the button with id R.id.notify is clicked. We just call our method
         * [showNotification] with the text "Selecting this will cause a background
         * service to run."
         *
         * Parameter: the `View` that was clicked
         */
        private val mNotify = OnClickListener {
            showNotification("Selecting this will cause a background service to run.")
        }

        /**
         * Called when the activity is starting. First we call our super's implementation of
         * `onCreate`, then we set our content view to our layout file
         * R.layout.notification_background_service. We then initialize our [NotificationManager]
         * field [mNM] with a handle to the NOTIFICATION_SERVICE system level service. We initialize
         * our [NotificationChannel] variable `val chan1` with a new instance whose id and user
         * visible name are both PRIMARY_CHANNEL ("default"), and whose importance is
         * IMPORTANCE_DEFAULT (shows everywhere, makes noise, but does not visually intrude).
         * We set the notification light color of `chan1` to GREEN, and set its lock screen
         * visibility to VISIBILITY_PRIVATE (shows this notification on all lockscreens, but
         * conceals sensitive or private information on secure lockscreens). We then have [mNM]
         * create notification channel `chan1`. Finally we initialize our [Button] variable
         * `val button` by finding the view with id R.id.notify and set its `OnClickListener`
         * to our field [mNotify].
         *
         * @param savedInstanceState we do not override `onSaveInstanceState` so do not use
         */
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.notification_background_service)

            mNM = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val chan1 = NotificationChannel(PRIMARY_CHANNEL, PRIMARY_CHANNEL,
                    NotificationManager.IMPORTANCE_DEFAULT)
            chan1.lightColor = Color.GREEN
            chan1.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            mNM!!.createNotificationChannel(chan1)

            val button = findViewById<Button>(R.id.notify)
            button.setOnClickListener(mNotify)
        }

        /**
         * Called by the `onClick` override of our [OnClickListener] field [mNotify], we
         * build and post a notification using our [CharSequence] parameter [text] as the text.
         * We initialize our [PendingIntent] variable `val contentIntent` with an instance intended
         * to launch the service [NotificationBackgroundService] with request code 0. We initialize
         * our [Notification] variable `val notification` by building a [Notification.Builder] for
         * notification channel PRIMARY_CHANNEL ("default"), set its small icon to R.drawable.stat_sample,
         * its ticker text to our [CharSequence] parameter [text], its time stamp to now, its second
         * line of text to [text] and its [PendingIntent] to be sent when the notification is clicked
         * to `contentIntent`. Finally we use our [NotificationManager] field [mNM] to post
         * `notification` using the resource id R.string.notification_background_service as its id.
         *
         * @param text text to display in our notification.
         */
        @Suppress("SameParameterValue")
        private fun showNotification(text: CharSequence) {
            // The PendingIntent to launch our activity if the user selects this notification
            val contentIntent = PendingIntent.getService(this, 0,
                    Intent(this, NotificationBackgroundService::class.java), 0)

            // Set the info for the views that show in the notification panel.
            val notification = Notification.Builder(this, PRIMARY_CHANNEL)
                    .setSmallIcon(R.drawable.stat_sample)  // the status icon
                    .setTicker(text)  // the status text
                    .setWhen(System.currentTimeMillis())  // the time stamp
                    .setContentTitle(getText(R.string.notification_background_label))  // the label of the entry
                    .setContentText(text)  // the contents of the entry
                    .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                    .build()

            // Send the notification.
            // We use a layout id because it is a unique number.  We use it later to cancel.
            mNM!!.notify(R.layout.notification_background_service, notification)
        }

        /**
         * Our static constant.
         */
        companion object {
            /**
             * The id of the primary notification channel
             */
            const val PRIMARY_CHANNEL = "default"
        }
    }
}


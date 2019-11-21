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

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.example.android.apis.R

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
/**
 * This is an example of implementing an application service that runs locally
 * in the same process as the application.  The [LocalServiceActivities.Controller]
 * and [LocalServiceActivities.Binding] classes show how to interact with the
 * service.
 *
 *
 *
 * Notice the use of the [NotificationManager] when interesting things
 * happen in the service.  This is generally how background services should
 * interact with the user, rather than doing something more disruptive such as
 * calling startActivity().
 */
@TargetApi(Build.VERSION_CODES.O)
class LocalService : Service() {
    private var mNM // handle to the system level NOTIFICATION_SERVICE service
            : NotificationManager? = null


    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    inner class LocalBinder : Binder() {
        val service: LocalService
            get() = this@LocalService
    }

    /**
     * Called by the system when the service is first created. First we initialize our field
     * `NotificationManager mNM` with a handle to the system level NOTIFICATION_SERVICE service.
     * Then we initialize `NotificationChannel chan1` with a new instance using PRIMARY_CHANNEL
     * ("default") as both the ID and the user visible name of the channel with its importance set to
     * IMPORTANCE_DEFAULT (shows everywhere, makes noise, but does not visually intrude). We then set
     * the notification light color of `chan1` to GREEN and its lock screen visibility to
     * VISIBILITY_PRIVATE (show this notification on all lockscreens, but conceal sensitive or private
     * information on secure lockscreens). Then we use `mNM` to create the notification channel
     * `chan1`, and call our method `showNotification` to display the notification that
     * we are running
     */
    override fun onCreate() {
        mNM = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val chan1 = NotificationChannel(PRIMARY_CHANNEL, PRIMARY_CHANNEL,
                NotificationManager.IMPORTANCE_DEFAULT)
        chan1.lightColor = Color.GREEN
        chan1.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        mNM!!.createNotificationChannel(chan1)
        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification()
    }

    /**
     * Called by the system every time a client explicitly starts the service by calling
     * [android.content.Context.startService], providing the arguments it supplied and a
     * unique integer token representing the start request. Note that the system calls this on your
     * service's main thread. A service's main thread is the same thread where UI operations take
     * place for Activities running in the same process. You should always avoid stalling the main
     * thread's event loop.  When doing long-running operations, network calls, or heavy disk I/O,
     * you should kick off a new thread, or use [android.os.AsyncTask].
     *
     *
     * We simply log the start intent, and return START_NOT_STICKY (if this service's process is
     * killed while it is started (after returning from onStartCommand(Intent, int, int)), and
     * there are no new start intents to deliver to it, then take the service out of the started
     * state and don't recreate until a future explicit call to Context.startService(Intent).)
     *
     * @param intent  The Intent supplied to [android.content.Context.startService],
     * as given.  This may be null if the service is being restarted after
     * its process has gone away, and it had previously returned anything
     * except [.START_STICKY_COMPATIBILITY].
     * @param flags   Additional data about this start request.  Currently either
     * 0, [.START_FLAG_REDELIVERY], or [.START_FLAG_RETRY].
     * @param startId A unique integer representing this specific request to
     * start.  Use with [.stopSelfResult].
     * @return The return value indicates what semantics the system should
     * use for the service's current started state.  It may be one of the
     * constants associated with the [.START_CONTINUATION_MASK] bits.
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i("LocalService", "Received start id $startId: $intent")
        return START_NOT_STICKY
    }

    /**
     * Our only service, which is accessible using the LocalService.this reference returned from
     * `LocalBinder.getservice()`. We just toast a message to show we can be used to do
     * something.
     */
    fun doSomeThing() {
        Toast.makeText(this, "I AM doing something", Toast.LENGTH_SHORT).show()
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed. First
     * we cancel the notification that we are running, then we toast a message: "Local service has stopped"
     * to inform the user.
     */
    override fun onDestroy() { // Cancel the persistent notification.
        mNM!!.cancel(NOTIFICATION)
        // Tell the user we stopped.
        Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show()
    }

    /**
     * Return the communication channel to the service. We return `IBinder mBinder` which
     * is an instance of `LocalBinder`, which contains a single method `getService`
     * which returns a reference to the instance of `LocalService` which is running. That
     * reference can then be used to call methods and access fields of `LocalService` since
     * it is running in the same process.
     *
     * @param intent The Intent that was used to bind to this service.
     * @return Return an IBinder through which clients can call on to the
     * service.
     */
    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    // This is the object that receives interactions from clients.  See
// RemoteService for a more complete example.
    private val mBinder: IBinder = LocalBinder()

    /**
     * Show a notification while this service is running. First we fetch the resource String with ID
     * R.string.local_service_started ("Local service has started") to initialize `CharSequence text`,
     * then we create `Intent intent` which will launch `LocalServiceActivities.Controller`,
     * and use it to make `PendingIntent contentIntent`. We build `Notification notification`
     * using R.drawable.stat_sample as the icon, `text` as the ticker text, set the timestamp to
     * the current system time, set the first line of text to R.string.local_service_label ("Sample
     * Local Service"), set the second line of text to `text`, and set the `PendingIntent`
     * to be sent when the notification is clicked to `contentIntent`. We then use
     * `NotificationManager mNM` to post the notification using NOTIFICATION as the ID (so that
     * we can later use it to cancel the notification.)
     */
    private fun showNotification() { // In this sample, we'll use the same text for the ticker and the expanded notification
        val text = getText(R.string.local_service_started)
        // The PendingIntent to launch our activity if the user selects this notification
        val intent = Intent(this, LocalServiceActivities.Controller::class.java)
        val contentIntent = PendingIntent.getActivity(this, 0, intent, 0)
        // Set the info for the views that show in the notification panel.
        val notification = Notification.Builder(this, PRIMARY_CHANNEL)
                .setSmallIcon(R.drawable.stat_sample) // the status icon
                .setTicker(text) // the status text
                .setWhen(System.currentTimeMillis()) // the time stamp
                .setContentTitle(getText(R.string.local_service_label)) // the label of the entry
                .setContentText(text) // the contents of the entry
                .setContentIntent(contentIntent) // The intent to send when the entry is clicked
                .build()
        // Send the notification.
        mNM!!.notify(NOTIFICATION, notification)
    }

    companion object {
        /**
         * The id of the primary notification channel
         */
        const val PRIMARY_CHANNEL = "default"
        // Unique Identification Number for the Notification.
// We use it on Notification start, and to cancel it.
        private const val NOTIFICATION = R.string.local_service_started
    }
}
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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.ConditionVariable
import android.os.IBinder
import android.os.Parcel
import android.os.RemoteException
import androidx.annotation.RequiresApi
import com.example.android.apis.R

/**
 * Updates a notification every 5 seconds from a background thread for a minute. Note use of
 * a [ConditionVariable] to implement the condition variable locking paradigm, blocking
 * for 5*1000 milliseconds after every notification (very useful approach).
 */
@RequiresApi(Build.VERSION_CODES.O)
class NotifyingService : Service() {

    /**
     * variable which controls the notification thread
     */
    private var mCondition: ConditionVariable? = null

    /**
     * Handle to the system level service NOTIFICATION_SERVICE
     */
    private var mNM: NotificationManager? = null

    /**
     * The task that is being run in the background thread `Thread notifyingThread`. It loops
     * through 3 different notifications 4 times, pausing 5 seconds between each notification. This
     * happens only so long as the `ConditionVariable mCondition` remains closed -- if it is
     * opened (as happens in the `onDestroy` callback) it breaks out of the loop and stops
     * immediately. We loop 4 times, calling our method [showNotification] to show three different
     * notifications, pausing after each call to block on the [ConditionVariable] field [mCondition]
     * for 5 seconds, and if we return from `block` before the 5 seconds are over ([mCondition] is
     * opened causing a "true" return), we break out of the for loop prematurely. Whether we execute
     * all four iterations or break out of the for loop we stop the service using the method
     * [stopSelf].
     */
    private val mTask = Runnable {
        @Suppress("unused")
        for (i in 0..3) {
            showNotification(
                R.drawable.stat_happy,
                R.string.status_bar_notifications_happy_message
            )
            if (mCondition!!.block(5_000L)) break
            showNotification(
                R.drawable.stat_neutral,
                R.string.status_bar_notifications_ok_message
            )
            if (mCondition!!.block(5_000L)) break
            showNotification(
                R.drawable.stat_sad,
                R.string.status_bar_notifications_sad_message
            )
            if (mCondition!!.block(5_000L)) break
        }
        // Done with our work...  stop the service!
        this@NotifyingService.stopSelf()
    }

    /**
     * This is the object that receives interactions from clients. See
     * RemoteService for a more complete example.
     * Does not appear to be used(?) so could just as well be null?
     * TODO: study aidl example in the Remote Service applications first
     */
    private val mBinder = object : Binder() {
        @Throws(RemoteException::class)
        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            return super.onTransact(code, data, reply, flags)
        }
    }

    /**
     * Called by the system when the service is first created. First we initialize our field
     * [NotificationManager] field [mNM] with a handle to the system level NOTIFICATION_SERVICE
     * service. We initialize our [NotificationChannel] variable `val chan1` with a new instance
     * whose id and user visible name are both PRIMARY_CHANNEL ("default"), and whose importance is
     * IMPORTANCE_DEFAULT (shows everywhere, makes noise, but does not visually intrude). We set the
     * notification light color of `chan1` to GREEN, and set its lock screen visibility to
     * VISIBILITY_PRIVATE (shows this notification on all lockscreens, but conceals sensitive or
     * private information on secure lockscreens). We then have [mNM] create notification channel
     * `chan1`. Next we create a [Thread] to initialize our variable `val notifyingThread` which
     * runs our [Runnable] field [mTask] using the name "NotifyingService". We initialize our
     * [ConditionVariable] field [mCondition] to an instance of an initially closed [ConditionVariable].
     * Finally we start `notifyingThread` running.
     */
    override fun onCreate() {
        mNM = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val chan1 = NotificationChannel(
            PRIMARY_CHANNEL, PRIMARY_CHANNEL,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        chan1.lightColor = Color.GREEN
        chan1.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        mNM!!.createNotificationChannel(chan1)

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
        val notifyingThread = Thread(null, mTask, "NotifyingService")
        mCondition = ConditionVariable(false)
        notifyingThread.start()
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.
     * First we call the `cancel` method of the [NotificationManager] to remove our notification
     * from the status bar, then we open our [ConditionVariable] field [mCondition] which will
     * cause our background task's [Runnable] in field [mTask] to "break" out of its "for" loop
     * and terminate before its 4 iterations are completed.
     */
    override fun onDestroy() {
        // Cancel the persistent notification.
        mNM!!.cancel(MOOD_NOTIFICATIONS)
        // Stop the thread from generating further notifications
        mCondition!!.open()
    }

    /**
     * Return the communication channel to the service. We simply return our stub `IBinder mBinder`
     * to the caller.
     *
     * @param intent The [Intent] that was used to bind to this service.
     * @return an [IBinder] through which clients can call on to the service.
     */
    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    /**
     * Builds and posts a notification using resource ID's for the icon and the contents String.
     * First we fetch the [CharSequence] with the resource ID `textID` to initialize our variable
     * `val text`. Next we create a [PendingIntent] to initialize our variable `val contentIntent`
     * which will launch the Activity [NotifyingController] if the [Notification] we post is
     * selected by the user. We use a [Notification.Builder] for the [NotificationChannel]
     * PRIMARY_CHANNEL to build a [Notification] for our variable `val notification` using our
     * parameter [moodId] as the small icon, the current system time as the timestamp, the
     * [String] "Mood ring" as the title, `text` as the contents, and `contentIntent` as
     * the [PendingIntent] to be sent when the notification is clicked. Finally we post
     * `notification` to be shown in the status bar.
     *
     * @param moodId Resource ID for small icon of the notification
     * @param textId Resource ID for String to use for second line of text in the platform
     * notification template
     */
    private fun showNotification(moodId: Int, textId: Int) {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        val text = getText(textId)

        // The PendingIntent to launch our activity if the user selects this notification
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, NotifyingController::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        // Set the icon and timestamp.
        // Note that in this example, we do not set the tickerText.  We update the icon enough that
        // it is distracting to show the ticker text every time it changes.  We strongly suggest
        // that you do this as well.  (Think of of the "New hardware found" or "Network connection
        // changed" messages that always pop up)
        // Set the info for the views that show in the notification panel.
        val notification = Notification.Builder(this, PRIMARY_CHANNEL)
            .setSmallIcon(moodId)
            .setWhen(System.currentTimeMillis())
            .setContentTitle(getText(R.string.status_bar_notifications_mood_title))
            .setContentText(text)  // the contents of the entry
            .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
            .build()

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNM!!.notify(MOOD_NOTIFICATIONS, notification)
    }

    /**
     * Our static constants
     */
    companion object {

        /**
         * Use a layout id for a unique identifier
         */
        private const val MOOD_NOTIFICATIONS = 42

        /**
         * The id of the primary notification channel
         */
        const val PRIMARY_CHANNEL: String = "default"
    }
}

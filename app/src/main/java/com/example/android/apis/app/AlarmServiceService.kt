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

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Parcel
import android.os.RemoteException
import android.util.Log
import android.widget.Toast

/**
 * This is an example of implementing an application service that will run in
 * response to an alarm, allowing us to move long duration work out of an
 * intent receiver.
 *
 * @see AlarmService
 *
 * @see AlarmServiceService
 */
@Suppress("MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.O)
class AlarmServiceService : Service() {
    /**
     * Handle to the NOTIFICATION_SERVICE system level service,
     * used to notify the user of events that happen
     */
    internal lateinit var mNM: NotificationManager

    /**
     * The function that runs in our worker thread. Starts executing the active part of the class'
     * code. This method is called when a thread is started that has been created with a class which
     * implements [Runnable].
     *
     * We set our [Long] variable `val endTime` to the current time in milliseconds plus 15 seconds,
     * then we loop until the current time is greater than or equal to `endTime` (15 seconds have
     * elapsed). In the loop we call wait to wait that 15 seconds, but the loop is necessary
     * just in case we are interrupted. After the wait is up we stop this service.
     */
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    internal var mTask: Runnable = Runnable {
        // Normally we would do some work here...  for our sample, we will
        // just sleep for 30 seconds.
        val endTime = System.currentTimeMillis() + 15 * 1000
        while (System.currentTimeMillis() < endTime) {
            synchronized(mBinder) {
                try {
                    (mBinder as Object).wait(endTime - System.currentTimeMillis())
                } catch (e: Exception) {
                    Log.e("AlarmService: ", e.message, e)
                }

            }
        }

        // Done with our work...  stop the service!
        this@AlarmServiceService.stopSelf()
    }

    /**
     * This is the object that receives interactions from clients.  See `RemoteService`
     * for a more complete example. We just implement `onTransact` to call
     * through to our super's implementation of `onTransact`.
     */
    private val mBinder = object : Binder() {
        /**
         * Default implementation is a stub that returns false. You will want to override this to do
         * the appropriate un-marshalling of transactions. We simply return the return value of our
         * super's implementation of `onTransact`.
         *
         * @param code The action to perform. This should be a number between FIRST_CALL_TRANSACTION
         * and LAST_CALL_TRANSACTION.
         * @param data  Marshalled data to send to the target. Must not be null. If you are not
         * sending any data, you must create an empty Parcel that is given here.
         * @param reply Marshalled data to be received from the target. May be null if you are not
         * interested in the return value.
         * @param flags Additional operation flags. Either 0 for a normal RPC, or FLAG_ONEWAY
         * for a one-way RPC.
         * @return Our super's "reaction" to the request: true if code == INTERFACE_TRANSACTION or
         * code == DUMP_TRANSACTION, false otherwise.
         *
         * @throws RemoteException if there is a Binder remote-invocation error
         */
        @Throws(RemoteException::class)
        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            return super.onTransact(code, data, reply, flags)
        }
    }

    /**
     * Called by the system when the service is first created. First we initialize our
     * [NotificationManager] field [mNM] with a handle to the NotificationManager system service.
     * We initialize our [NotificationChannel] variable `val chan1` with a new instance whose id
     * and user visible name are both PRIMARY_CHANNEL ("default"), and whose importance is
     * IMPORTANCE_DEFAULT (shows everywhere, makes noise, but does not visually intrude). We set
     * the notification light color of `chan1` to GREEN, and set its lock screen visibility to
     * VISIBILITY_PRIVATE (shows this notification on all lockscreens, but conceal sensitive or
     * private information on secure lockscreens). We then have [mNM] create notification channel
     * `chan1`. Next we show our icon in the status bar by calling our method [showNotification].
     * Then we create a new [Thread] variable `val thr` to run our [Runnable] field [mTask] with
     * the name "AlarmService_Service". Finally we start `thr`.
     */
    override fun onCreate() {
        mNM = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val chan1 = NotificationChannel(PRIMARY_CHANNEL, PRIMARY_CHANNEL,
                NotificationManager.IMPORTANCE_DEFAULT)
        chan1.lightColor = Color.GREEN
        chan1.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        mNM.createNotificationChannel(chan1)
        // show the icon in the status bar
        showNotification()

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
        val thr = Thread(null, mTask, "AlarmService_Service")
        thr.start()
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed. The
     * service should clean up any resources it holds (threads, registered receivers, etc) at this
     * point. Upon return, there will be no more calls in to this Service object and it is
     * effectively dead. Do not call this method directly.
     *
     * We use our [NotificationManager] field [mNM] to cancel our notification using the same
     * identifier we used to start it in the method [showNotification], then we show a Toast stating:
     *
     * "The alarm service has finished running"
     */
    override fun onDestroy() {
        // Cancel the notification -- we use the same ID that we had used to start it
        mNM.cancel(R.string.alarm_service_started)

        // Tell the user we stopped.
        Toast.makeText(this, R.string.alarm_service_finished, Toast.LENGTH_SHORT).show()
    }

    /**
     * Return the communication channel to the service. We simply return our minimalist
     * [IBinder] field [mBinder].
     *
     * @param intent The Intent that was used to bind to this service, as given to
     * [android.content.Context.bindService]. Note that any extras that were included with the
     * [Intent] at that point will *not* be seen here.
     *
     * @return Return an [IBinder] through which clients can call on to the service.
     */
    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    /**
     * Show a notification while this service is running. First we fetch the resource String
     * R.string.alarm_service_started ("The alarm service has started running") into our
     * [CharSequence] variable `val text`, then we create a [PendingIntent] for our variable
     * `val contentIntent` to start the Activity AlarmService (the Activity that launched us).
     * Next we build our [Notification] variable `val notification` using a method chain starting
     * with a new instance of [Notification.Builder] for [NotificationChannel] PRIMARY_CHANNEL. It
     * consists of a small icon R.drawable.stat_sample, [CharSequence] `text` as the "ticker" text
     * which is sent to accessibility services, a timestamp of the current time in milliseconds, a
     * title of R.string.alarm_service_label ("Sample Alarm Service"), `text` as the second line of
     * text in the platform notification template, and our [PendingIntent] variable `contentIntent`
     * as the [PendingIntent] to be sent when the notification is clicked. Finally we post our
     * notification to be shown in the status bar using an `id` consisting of our resource id
     * R.string.alarm_service_started.
     */
    private fun showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        val text = getText(R.string.alarm_service_started)

        // The PendingIntent to launch our activity if the user selects this notification
        val contentIntent = PendingIntent.getActivity(this, 0,
                Intent(this, AlarmService::class.java), 0)

        // Set the info for the views that show in the notification panel.
        val notification = Notification.Builder(this, PRIMARY_CHANNEL)
                .setSmallIcon(R.drawable.stat_sample)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.alarm_service_label))  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build()

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNM.notify(R.string.alarm_service_started, notification)
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


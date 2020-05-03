/*
 * Copyright (C) 2010 The Android Open Source Project
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
import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.widget.Toast

import com.example.android.apis.R

import java.util.*

/**
 * This is an example of implementing an application service that uses the
 * [Messenger] class for communicating with clients. This allows for
 * remote interaction with a service, without needing to define an AIDL
 * interface.
 *
 * Notice the use of the [NotificationManager] when interesting things
 * happen in the service. This is generally how background services should
 * interact with the user, rather than doing something more disruptive such as
 * calling `startActivity()`.
 */
@Suppress("MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.O)
class MessengerService : Service() {
    /**
     * For showing and hiding our notification.
     */
    var mNM: NotificationManager? = null
    /**
     * Keeps track of all current registered clients.
     */
    var mClients = ArrayList<Messenger>()
    /**
     * Holds last value set by a client.
     */
    var mValue = 0

    /**
     * Handler of incoming messages from clients.
     */
    @SuppressLint("HandlerLeak")
    internal inner class IncomingHandler : Handler() {
        /**
         * Subclasses must implement this to receive messages. We switch based on the `what`
         * field of the `Message msg` we have received:
         *
         *  * MSG_REGISTER_CLIENT - we add the value contained in the `replyTo` field to the
         *  list of clients we maintain in `ArrayList<Messenger>` field [mClients].
         *  * MSG_UNREGISTER_CLIENT - we remove the value contained in the `replyTo` field from
         *  the list of clients we maintain in `ArrayList<Messenger>` field [mClients].
         *  * MSG_SET_VALUE - we store the value sent in the field `arg1` of `Message msg`
         *  in our field `int mValue`, then looping backwards through all the clients
         *  in `ArrayList<Messenger>` field [mClients] we try to send a message with the `what`
         *  field set to MSG_SET_VALUE, and `arg1` field set to `mValue`. If we
         *  catch a [RemoteException] we remove that client from [mClients] (safe because
         *  we are going through the list in backwards order).
         *  * default - we pass the `msg` on to our super's implementation of `handleMessage`.
         *
         * @param msg [Message] received by the [Messenger] we are the [Handler] for
         */
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_REGISTER_CLIENT -> mClients.add(msg.replyTo)
                MSG_UNREGISTER_CLIENT -> mClients.remove(msg.replyTo)
                MSG_SET_VALUE -> {
                    mValue = msg.arg1
                    var i = mClients.size - 1
                    while (i >= 0) {
                        try {
                            mClients[i].send(Message.obtain(null, MSG_SET_VALUE, mValue, 0))
                        } catch (e: RemoteException) {
                            /**
                             * The client is dead. Remove it from the list;
                             * we are going through the list from back to front
                             * so this is safe to do inside the loop.
                             */
                            mClients.removeAt(i)
                        }
                        i--
                    }
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    /**
     * Target we publish for clients to send messages to [IncomingHandler].
     */
    val mMessenger = Messenger(IncomingHandler())

    /**
     * Called by the system when the service is first created. We initialize our field
     * [NotificationManager] field [mNM] with a handle to the system level service
     * NOTIFICATION_SERVICE, then we initialize [NotificationChannel] variable `val chan1` with a
     * new instance whose id and user visible name are both PRIMARY_CHANNEL ("default"), and whose
     * importance is IMPORTANCE_DEFAULT (shows everywhere, makes noise, but does not visually
     * intrude). We set the notification light color of `chan1` to GREEN, and set its lock screen
     * visibility to VISIBILITY_PRIVATE (shows this notification on all lockscreens, but conceals
     * sensitive or private information on secure lockscreens). We then have [mNM] create
     * notification channel `chan1`. Finally we call our method [showNotification] to display the
     * notification that we are running.
     */
    override fun onCreate() {
        mNM = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val chan1 = NotificationChannel(PRIMARY_CHANNEL, PRIMARY_CHANNEL,
                NotificationManager.IMPORTANCE_DEFAULT)
        chan1.lightColor = Color.GREEN
        chan1.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        mNM!!.createNotificationChannel(chan1)
        // Display a notification about us starting.
        showNotification()
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed. We
     * cancel our notification, and toast the message "Remote service has stopped".
     */
    override fun onDestroy() {
        /**
         * Cancel the persistent notification.
         */
        mNM!!.cancel(R.string.remote_service_started)
        /**
         * Tell the user we stopped.
         */
        Toast.makeText(this, R.string.remote_service_stopped, Toast.LENGTH_SHORT).show()
    }

    /**
     * Return the communication channel to the service. When a client binds to our service, we return
     * the [IBinder] interface to our [Messenger] vield [mMessenger] for sending messages to this
     * service.
     *
     * @param intent The [Intent] that was used to bind to this service, as given to
     * [android.content.Context.bindService]. Note that any extras that were included with
     * the [Intent] at that point will *not* be seen here.
     * @return Return an [IBinder] through which clients can call on to the service.
     */
    override fun onBind(intent: Intent): IBinder? {
        return mMessenger.binder
    }

    /**
     * Show a notification while this service is running. First we initialize [CharSequence] variable
     * `val text` with the resource String "Remote service has started". The we create [PendingIntent]
     * variable `val contentIntent` with a target [Intent] for `MessengerServiceActivities.Binding`.)
     *
     * We then build a PRIMARY_CHANNEL [Notification] for variable `val notification` using
     * R.drawable.stat_sample as the small icon, `text` as the "ticker" text, the current system
     * time as the timestamp, the resource [String] "Sample Local Service" as the first line of text,
     * `text` as the second line of text, and `contentIntent` as the [PendingIntent] to be sent when
     * the notification is clicked. Finally we use [NotificationManager] field [mNM] to post the
     * notification using R.string.remote_service_started as the ID.
     */
    private fun showNotification() {
        /**
         * In this sample, we'll use the same text for the ticker and the expanded notification
         */
        val text = getText(R.string.remote_service_started)
        /**
         * The PendingIntent to launch our controlling activity if the user selects this notification
         */
        val contentIntent = PendingIntent.getActivity(
                this,
                0,
                Intent(this, MessengerServiceActivities.Binding::class.java),
                0
        )
        /**
         * Set the info for the views that show in the notification panel.
         */
        val notification = Notification.Builder(this, PRIMARY_CHANNEL)
                .setSmallIcon(R.drawable.stat_sample) // the status icon
                .setTicker(text) // the status text
                .setWhen(System.currentTimeMillis()) // the time stamp
                .setContentTitle(getText(R.string.local_service_label)) // the label of the entry
                .setContentText(text) // the contents of the entry
                .setContentIntent(contentIntent) // The intent to send when the entry is clicked
                .build()
        /**
         * Send the notification. We use a string id because it is a unique number.
         * We use it later to cancel.
         */
        mNM!!.notify(R.string.remote_service_started, notification)
    }

    /**
     * Our static constants
     */
    companion object {
        /**
         * The id of the primary notification channel
         */
        const val PRIMARY_CHANNEL = "default"
        /**
         * Command to the service to register a client, receiving callbacks
         * from the service.  The Message's replyTo field must be a Messenger of
         * the client where callbacks should be sent.
         */
        const val MSG_REGISTER_CLIENT = 1
        /**
         * Command to the service to unregister a client, ot stop receiving callbacks
         * from the service.  The Message's replyTo field must be a Messenger of
         * the client as previously given with MSG_REGISTER_CLIENT.
         */
        const val MSG_UNREGISTER_CLIENT = 2
        /**
         * Command to service to set a new value.  This can be sent to the
         * service to supply a new value, and will be sent by the service to
         * any registered clients with the new value.
         */
        const val MSG_SET_VALUE = 3
    }
}
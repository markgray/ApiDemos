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
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity

import com.example.android.apis.R
import com.example.android.apis.app.ServiceStartArguments.Controller

/**
 * This is an example of implementing an application service that runs locally
 * in the same process as the application.  The [Controller]
 * class shows how to interact with the service.
 *
 *
 * Notice the use of the [NotificationManager] when interesting things
 * happen in the service.  This is generally how background services should
 * interact with the user, rather than doing something more disruptive such as
 * calling startActivity().
 *
 *
 * For applications targeting Android 1.5 or beyond, you may want consider
 * using the [android.app.IntentService] class, which takes care of all the
 * work of creating the extra thread and dispatching commands to it.
 */
@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
@TargetApi(Build.VERSION_CODES.O)
class ServiceStartArguments : Service() {
    /**
     * Handle to the system level `NotificationManager` service
     */
    private var mNM: NotificationManager? = null
    /**
     * `Looper` for the `HandlerThread` background thread we create to run our service in
     */
    @Volatile
    private var mServiceLooper: Looper? = null
    /**
     * `Handler` for messages sent to our service thread
     */
    @Volatile
    private var mServiceHandler: ServiceHandler? = null

    /**
     * This is a `Handler` class which is used to receive messages sent to the thread that is
     * running our service.
     */
    private inner class ServiceHandler
    /**
     * We just pass our parameter through to our super's constructor so that it uses this
     * `Looper` instead of the default one.
     *
     * @param looper `Looper` for `HandlerThread` that is running our service
     */(looper: Looper?) : Handler(looper!!) {
        /**
         * Subclasses must implement this to receive messages. First we retrieve `Bundle arguments`
         * from the `Object obj` of our `Message msg` parameter. Then we extract `String txt`
         * which is stored in `arguments` under the key "name", and `boolean redeliver` which
         * is stored under the key "redeliver" (if set, otherwise we default to false). We then log the
         * `msg` we have received. If `redeliver` is false we prepend "New cmd #" to txt,
         * otherwise we prepend "Re-delivered #". We call our method `showNotification` to display
         * a notification containing `txt`.
         *
         *
         * Then we wait for 5 seconds before calling our method `hideNotification` to dismiss our
         * notification, log a message "Done with #", and stop ourselves.
         *
         * @param msg A [Message][android.os.Message] object
         */
        override fun handleMessage(msg: Message) {
            val arguments = msg.obj as Bundle
            var txt = arguments.getString("name")
            val redeliver = arguments.getBoolean("redeliver", false)
            Log.i(TAG, "Message: " + msg + ", " + arguments.getString("name"))
            txt = if (!redeliver) {
                "New cmd #" + msg.arg1 + ": " + txt
            } else {
                "Re-delivered #" + msg.arg1 + ": " + txt
            }
            showNotification(txt)
            // Normally we would do some work here...  for our sample, we will
// just sleep for 5 seconds.
            val endTime = System.currentTimeMillis() + 5 * 1000
            while (System.currentTimeMillis() < endTime) {
                synchronized(this) {
                    try {
                        (this as Object).wait(endTime - System.currentTimeMillis())
                    } catch (e: Exception) {
                        Log.i(TAG, e.localizedMessage!!)
                    }
                }
            }
            hideNotification()
            Log.i(TAG, "Done with #" + msg.arg1)
            stopSelf(msg.arg1)
        }
    }

    /**
     * Called by the system when the service is first created. First we initialize our field
     * `NotificationManager mNM` with a handle to the NOTIFICATION_SERVICE system level
     * service. We initialize `NotificationChannel chan1` with a new instance whose id and
     * user visible name are both PRIMARY_CHANNEL ("default"), and whose importance is IMPORTANCE_DEFAULT
     * (shows everywhere, makes noise, but does not visually intrude). We set the notification light
     * color of `chan1` to GREEN, and set its lock screen visibility to VISIBILITY_PRIVATE
     * (shows this notification on all lockscreens, but conceal sensitive or private information on
     * secure lockscreens). We then have `mNM` create notification channel `chan1`.
     * Then we display a toast with the message "Service created." We next create `HandlerThread thread`
     * with the thread name "ServiceStartArgumentsBackground", and the priority THREAD_PRIORITY_BACKGROUND.
     * We start the `thread` running, then retrieve the `Looper mServiceLooper` of the thread
     * and use it to construct an instance of `ServiceHandler` for `ServiceHandler mServiceHandler`.
     */
    override fun onCreate() {
        mNM = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val chan1 = NotificationChannel(PRIMARY_CHANNEL, PRIMARY_CHANNEL,
                NotificationManager.IMPORTANCE_DEFAULT)
        chan1.lightColor = Color.GREEN
        chan1.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        mNM!!.createNotificationChannel(chan1)
        Toast.makeText(this, R.string.service_created, Toast.LENGTH_SHORT).show()
        // Start up the thread running the service.  Note that we create a
// separate thread because the service normally runs in the process's
// main thread, which we don't want to block.  We also make it
// background priority so CPU-intensive work will not disrupt our UI.
        val thread = HandlerThread("ServiceStartArgumentsBackground", Process.THREAD_PRIORITY_BACKGROUND)
        thread.start()
        mServiceLooper = thread.looper
        mServiceHandler = ServiceHandler(mServiceLooper)
    }

    /**
     * Called by the system every time a client explicitly starts the service by calling
     * [android.content.Context.startService], providing the arguments it supplied and a
     * unique integer token representing the start request. Note that the system calls this on your
     * service's main thread. First we log a message informing the user that we are "Starting #",
     * with the `startId` request number, and the contents of the extras included in the
     * `Intent intent` that started us. We obtain `Message msg` from our background
     * `ServiceHandler mServiceHandler`, set field `arg1` to `startId`, field
     * `arg2` to `flags` and field `obj` to the extras in `intent`. We then
     * push `msg` onto the end of the message queue for `mServiceHandler` after all
     * pending messages before the current time. It will be received in handleMessage(Message), in
     * the thread attached to this handler. Then we log as message "Sending: " with `msg`
     * appended to it.
     *
     *
     * Then we check to see if we were started using the "Start Failed Delivery" `Button`, and
     * if so we kill our process to simulate a failed delivery (but only if this is not a retry
     * call to `onStartCommand` as indicated by `flags` having the START_FLAG_RETRY bits
     * set.)
     *
     *
     * Finally if the extras contain a "redeliver" flag set to true we return START_REDELIVER_INTENT
     * (if this service's process is killed while it is started, it will be scheduled for a restart
     * and the last delivered Intent re-delivered to it again), otherwise we return START_NOT_STICKY
     * (if this service's process is killed while it is started and there are no new start intents
     * to deliver to it, then take the service out of the started state and don't recreate until a
     * future explicit call).
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
        Log.i(TAG, "Starting #" + startId + ": " + intent.extras)
        val msg = mServiceHandler!!.obtainMessage()
        msg.arg1 = startId
        msg.arg2 = flags
        msg.obj = intent.extras
        mServiceHandler!!.sendMessage(msg)
        Log.i(TAG, "Sending: $msg")
        // For the start fail button, we will simulate the process dying
// for some reason in onStartCommand().
        if (intent.getBooleanExtra("fail", false)) { // Don't do this if we are in a retry... the system will
// eventually give up if we keep crashing.
            if (flags and START_FLAG_RETRY == 0) { // Since the process hasn't finished handling the command,
// it will be restarted with the command again, regardless of
// whether we return START_REDELIVER_INTENT.
                Process.killProcess(Process.myPid())
            }
        }
        // Normally we would consistently return one kind of result...
// however, here we will select between these two, so you can see
// how they impact the behavior.  Try killing the process while it
// is in the middle of executing the different commands.
        return if (intent.getBooleanExtra("redeliver", false)) START_REDELIVER_INTENT else START_NOT_STICKY
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed. First
     * we tell our `Looper mServiceLooper` which is receiving messages to terminate without
     * processing any more messages in the message queue. Then we call our method `hideNotification`
     * which cancels our notification, and toast the message "Service destroyed."
     */
    override fun onDestroy() {
        mServiceLooper!!.quit()
        hideNotification()
        // Tell the user we stopped.
        Toast.makeText(this@ServiceStartArguments, R.string.service_destroyed, Toast.LENGTH_SHORT).show()
    }

    /**
     * Return the communication channel to the service.  May return null if
     * clients can not bind to the service, and that is what we do.
     *
     * @param intent The Intent that was used to bind to this service.
     * @return Return an IBinder through which clients can call on to the
     * service.
     */
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    /**
     * Show a notification while this service is running. First we create `PendingIntent contentIntent`
     * which will launch `Controller`. We then construct `Notification.Builder noteBuilder`
     * to use PRIMARY_CHANNEL as its `NotificationChannel`, setting its small icon to R.drawable.stat_sample,
     * its ticker text to `text`, its timestamp to the current system time, its label to "Sample Service
     * Start Arguments", its text to `text`, and `contentIntent` as the `PendingIntent`
     * to be sent when the notification is clicked. We set the ongoing flag to true so that it cannot be
     * dismissed by the user, then build and post the notification with the ID R.string.service_created.
     *
     * @param text text to display in our notification
     */
    private fun showNotification(text: String) { // The PendingIntent to launch our activity if the user selects this notification
        val contentIntent = PendingIntent.getActivity(this, 0, Intent(this, Controller::class.java), 0)
        // Set the info for the views that show in the notification panel.
        val noteBuilder = Notification.Builder(this, PRIMARY_CHANNEL)
                .setSmallIcon(R.drawable.stat_sample) // the status icon
                .setTicker(text) // the status text
                .setWhen(System.currentTimeMillis()) // the time stamp
                .setContentTitle(getText(R.string.service_start_arguments_label)) // the label
                .setContentText(text) // the contents of the entry
                .setContentIntent(contentIntent) // The intent to send when the entry is clicked
        // We show this for as long as our service is processing a command.
        noteBuilder.setOngoing(true)
        // Send the notification.
// We use a string id because it is a unique number.  We use it later to cancel.
        mNM!!.notify(R.string.service_created, noteBuilder.build())
    }

    /**
     * Cancels the notification with ID R.string.service_created,
     */
    private fun hideNotification() {
        mNM!!.cancel(R.string.service_created)
    }
    // ----------------------------------------------------------------------
    /**
     * Example of explicitly starting the [ServiceStartArguments].
     * Note that this is implemented as an inner class only keep the sample
     * all together; typically this code would appear in some separate class.
     */
    class Controller : AppCompatActivity() {
        /**
         * Called when the activity is starting. First we call through to our super's implementation
         * of `onCreate`, then we set our content view to our layout file
         * R.layout.service_start_arguments_controller.
         *
         *
         * We locate the `Button`'s in our layout and set their `OnClickListener`'s:
         *
         *  * R.id.start1 "Start One no redeliver" `mStart1Listener`
         *  * R.id.start2 "Start Two no redeliver" `mStart2Listener`
         *  * R.id.start3 "Start Three w/redeliver" `mStart3Listener`
         *  * R.id.startfail "Start failed delivery" `mStartFailListener`
         *  * R.id.kill "Kill Process" `mKillListener`
         *
         *
         * @param savedInstanceState we do not override `onSaveInstanceState` so do not use
         */
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.service_start_arguments_controller)
            // Watch for button clicks.
            var button = findViewById<Button>(R.id.start1)
            button.setOnClickListener(mStart1Listener)
            button = findViewById(R.id.start2)
            button.setOnClickListener(mStart2Listener)
            button = findViewById(R.id.start3)
            button.setOnClickListener(mStart3Listener)
            button = findViewById(R.id.startfail)
            button.setOnClickListener(mStartFailListener)
            button = findViewById(R.id.kill)
            button.setOnClickListener(mKillListener)
        }

        /**
         * `OnClickListener` for the R.id.start1 "Start One no redeliver" Button
         */
        private val mStart1Listener = View.OnClickListener {
            startService(Intent(this@Controller, ServiceStartArguments::class.java)
                    .putExtra("name", "One"))
        }
        /**
         * `OnClickListener` for the R.id.start2 "Start Two no redeliver" Button
         */
        private val mStart2Listener = View.OnClickListener {
            startService(Intent(this@Controller, ServiceStartArguments::class.java)
                    .putExtra("name", "Two"))
        }
        /**
         * `OnClickListener` for the R.id.start3 "Start Three w/redeliver" Button
         */
        private val mStart3Listener = View.OnClickListener {
            startService(Intent(this@Controller, ServiceStartArguments::class.java)
                    .putExtra("name", "Three")
                    .putExtra("redeliver", true))
        }
        /**
         * `OnClickListener` for the R.id.startfail "Start failed delivery" Button
         */
        private val mStartFailListener = View.OnClickListener {
            startService(Intent(this@Controller, ServiceStartArguments::class.java)
                    .putExtra("name", "Failure")
                    .putExtra("fail", true))
        }
        /**
         * `OnClickListener` for the R.id.kill "Kill Process" Button
         */
        private val mKillListener = View.OnClickListener {
            // This is to simulate the service being killed while it is
// running in the background.
            Process.killProcess(Process.myPid())
        }
    }

    companion object {
        /**
         * TAG for logging
         */
        const val TAG = "ServiceStartArguments"
        /**
         * The id of the primary notification channel
         */
        const val PRIMARY_CHANNEL = "default"
    }
}
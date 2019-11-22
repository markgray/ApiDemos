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

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.app.RemoteService.Binding
import com.example.android.apis.app.RemoteService.Controller

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R

/**
 * This is an example of implementing an application service that runs in a
 * different process than the application. Because it can be in another
 * process, we must use IPC to interact with it. The [Controller] and
 * [Binding] classes show how to interact with the service. Uses the
 * aidl files  IRemoteService.aidl, IRemoteServiceCallback.aidl and ISecondary.aidl
 *
 * Note that most applications **do not** need to deal with
 * the complexity shown here. If your application simply has a service
 * running in its own process, the [LocalService] sample shows a much
 * simpler way to interact with it.
 */
@Suppress("MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.O)
@SuppressLint("SetTextI18n")
class RemoteService : Service() {
    /**
     * This is a list of callbacks that have been registered with the
     * service. Note that this is public scoped (instead of private) so
     * that it can be accessed more efficiently from inner classes.
     */
    val mCallbacks = RemoteCallbackList<IRemoteServiceCallback>()
    /**
     * Value that we increment and send to our clients
     */
    var mValue = 0
    /**
     * Handle to the system level NOTIFICATION_SERVICE service
     */
    var mNM: NotificationManager? = null

    /**
     * Called by the system when the service is first created. First we initialize our
     * [NotificationManager] field [mNM] with a handle to the system level NOTIFICATION_SERVICE
     * service. Then we initialize [NotificationChannel] variable `val chan1` with a new instance
     * whose id and user visible name are both PRIMARY_CHANNEL ("default"), and whose importance
     * is IMPORTANCE_DEFAULT (shows everywhere, makes noise, but does not visually intrude). We
     * set the notification light color of `chan1` to GREEN, and set its lock screen visibility to
     * VISIBILITY_PRIVATE (shows this notification on all lockscreens, but conceals sensitive or
     * private information on secure lockscreens). We then have [mNM] create notification channel
     * `chan1`. Next we call our method [showNotification] to post a notification that we are
     * running. Then we use our [Handler] field [mHandler] to send a message with the `what` field
     * set to REPORT_MSG to all the clients registered with us.
     */
    override fun onCreate() {
        mNM = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val chan1 = NotificationChannel(PRIMARY_CHANNEL, PRIMARY_CHANNEL,
                NotificationManager.IMPORTANCE_DEFAULT)
        chan1.lightColor = Color.GREEN
        chan1.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        mNM!!.createNotificationChannel(chan1)
        /**
         * Display a notification about us starting.
         */
        showNotification()
        /**
         * While this service is running, it will continually increment a
         * number.  Send the first message that is used to perform the
         * increment.
         */
        mHandler.sendEmptyMessage(REPORT_MSG)
        // android.os.Debug.waitForDebugger(); // wait for the debugger to attach
    }

    /**
     * Called by the system every time a client explicitly starts the service by calling
     * [android.content.Context.startService], providing the arguments it supplied and a
     * unique integer token representing the start request. Note that the system calls this on
     * your service's main thread. This callback is NOT  called if the service is started by a
     * call to [bindService].
     *
     * We simply log the fact that we have been started by an explicit call to [startService],
     * toast a message to the same effect, and return START_NOT_STICKY (Means: if this service's
     * process is killed while it is started (after returning from [onStartCommand], and there are
     * no new start intents to deliver to it, then take the service out of the started state and
     * don't recreate until a future explicit call to `Context.startService(Intent)`. The service
     * will not receive a `onStartCommand(Intent, int, int)` call with a null [Intent] because it
     * will not be re-started if there are no pending Intents to deliver.)
     *
     * @param intent  The [Intent] supplied to [android.content.Context.startService],
     * as given. This may be null if the service is being restarted after its process
     * has gone away, and it had previously returned anything except START_STICKY_COMPATIBILITY.
     * @param flags   Additional data about this start request. Currently either 0,
     * START_FLAG_REDELIVERY, or START_FLAG_RETRY
     * @param startId A unique integer representing this specific request to
     * start.  Use with [stopSelfResult].
     * @return The return value indicates what semantics the system should
     * use for the service's current started state.  It may be one of the
     * constants associated with the START_CONTINUATION_MASK bits.
     * @see [stopSelfResult]
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i("LocalService", "Received start id $startId: $intent")
        Toast.makeText(this, "onStartCommand has been called", Toast.LENGTH_LONG).show()
        return START_NOT_STICKY
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed. We
     * cancel our notification, toast a message "Remote service has stopped", disable the callback
     * list `RemoteCallbackList<IRemoteServiceCallback>` field [mCallbacks] (all registered callbacks
     * are unregistered, and the list is disabled so that future calls to `register(E)` will fail).
     * Finally we remove any pending posts of messages with code 'what' set to REPORT_MSG that are
     * in the message queue.
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
        /**
         * Unregister all callbacks.
         */
        mCallbacks.kill()
        /**
         * Remove the next pending message to increment the counter, stopping the increment loop.
         */
        mHandler.removeMessages(REPORT_MSG)
    }

    /**
     * Return the communication channel to the service. We check the action contained in the
     * `Intent intent` that was used to bind to us and if it is:
     *
     *  * IRemoteService - we return our [IRemoteService.Stub] field [mBinder]
     *  * ISecondary - we return our [ISecondary.Stub] field [mSecondaryBinder]
     *
     * Otherwise we return *null*.
     *
     * @param intent The [Intent] that was used to bind to this service, as given to
     * [android.content.Context.bindService]. Note that any extras that were included with
     * the [Intent] at that point will *not* be seen here.
     * @return Return an [IBinder] through which clients can call on to the service.
     */
    override fun onBind(intent: Intent): IBinder? {
        /**
         * Select the interface to return. If your service only implements a single
         * interface, you can just return it here without checking the [Intent].
         */
        if ((IRemoteService::class.java.name == intent.action)) {
            return mBinder
        }
        return if ((ISecondary::class.java.name == intent.action)) {
            mSecondaryBinder
        } else null
    }

    /**
     * The [IRemoteService] interface is defined through the IDL file `IRemoteService.aidl`,
     * it defines two methods `registerCallback` (adds the [IRemoteServiceCallback] parameter
     * `cb` to our `RemoteCallbackList<IRemoteServiceCallback>` field [mCallbacks] by calling its
     * method `register`, and `unregisterCallback` which removes the [IRemoteServiceCallback]
     * parameter `cb` from [mCallbacks], and we implement them here. The bound client then
     * accesses them using its [IRemoteService] field `mService` which is initialized in its
     * `onServiceConnected` callback from the [IBinder] `service` passed it by the service (uses
     * the method `IRemoteService.Stub.asInterface(service)` to convert the [IBinder] to an
     * instance of [IRemoteService])
     */
    @Suppress("SENSELESS_COMPARISON")
    private val mBinder: IRemoteService.Stub = object : IRemoteService.Stub() {
        /**
         * Often you want to allow a service to call back to its clients.
         * This shows how to do so, by registering a callback interface with
         * the service.
         *
         * @param cb the [IRemoteServiceCallback] we should register
         */
        override fun registerCallback(cb: IRemoteServiceCallback) {
            if (cb != null) mCallbacks.register(cb)
        }

        /**
         * Remove a previously registered callback interface.
         *
         * @param cb the [IRemoteServiceCallback] we should register
         */
        override fun unregisterCallback(cb: IRemoteServiceCallback) {
            if (cb != null) mCallbacks.unregister(cb)
        }
    }
    /**
     * A secondary interface to the service is defined through the IDL file `ISecondary.aidl`,
     * and consists of two methods which are accessible in much the same way as the methods in
     * the [IRemoteService.Stub] field [mBinder]
     */
    private val mSecondaryBinder: ISecondary.Stub = object : ISecondary.Stub() {
        /**
         * Request the PID of this service, to do evil things with it.
         *
         * @return PID of this `RemoteService` process, it is used by the "Kill" Button
         */
        override fun getPid(): Int {
            return Process.myPid()
        }

        /**
         * This demonstrates the basic types that you can use as parameters
         * and return values in AIDL.
         *
         * @param anInt unused
         * @param aLong unused
         * @param aBoolean unused
         * @param aFloat unused
         * @param aDouble unused
         * @param aString unused
         */
        override fun basicTypes(anInt: Int, aLong: Long, aBoolean: Boolean,
                                aFloat: Float, aDouble: Double, aString: String) {
        }
    }

    /**
     * This is called if the service is currently running and the user has
     * removed a task that comes from the service's application.  If you have
     * set [android.content.pm.ServiceInfo.FLAG_STOP_WITH_TASK] then you will
     * not receive this callback; instead, the service will simply be stopped.
     *
     * @param rootIntent The original root [Intent] that was used to launch
     * the task that is being removed.
     */
    override fun onTaskRemoved(rootIntent: Intent) {
        Toast.makeText(this, "Task removed: $rootIntent", Toast.LENGTH_LONG).show()
    }

    /**
     * Our Handler used to execute operations on the main thread. This is used to schedule
     * increments of our value.
     */
    @SuppressLint("HandlerLeak")
    private val mHandler: Handler = object : Handler() {
        /**
         * Subclasses of [Handler] must implement this to receive messages. We switch based on
         * the `what` field of our [Message] parameter [msg] and if it is not REPORT_MSG,
         * we pass it on to our super's implementation of `handleMessage`. If it is REPORT_MSG
         * we increment our field [mValue] while saving a copy in variable `val value`, then we
         * prepare to start making calls to the currently registered callbacks in our field
         * `RemoteCallbackList<IRemoteServiceCallback>` [mCallbacks] (initializing `n` to
         * the number of callbacks in the broadcast) (Note: [mCallbacks] has been filled by
         * clients calling the `registerCallback` method of our [IRemoteService] interface).
         * Then we proceed to loop through all the [IRemoteServiceCallback]'s retrieved from
         * [mCallbacks] and call their method `valueChanged(value)` (Note that we have to try/catch
         * [RemoteException] in case one of the callbacks has gone away). After looping through
         * all the callbacks we clean up the state of the broadcast by calling the method
         * `finishBroadcast`. Finally we enqueue a new REPORT_MSG message into the message
         * queue with a delay of 1000 milliseconds.
         *
         * @param msg [Message] sent to us, we only use REPORT_MSG
         */
        override fun handleMessage(msg: Message) { // It is time to bump the value!
            if (msg.what == REPORT_MSG) { // Up it goes.
                val value = ++mValue
                /**
                 * Broadcast to all clients the new value.
                 */
                val n = mCallbacks.beginBroadcast()
                for (i in 0 until n) {
                    try {
                        mCallbacks.getBroadcastItem(i).valueChanged(value)
                    } catch (e: RemoteException) {
                        /**
                         * The RemoteCallbackList will take care of removing the dead object for us.
                         */
                    }
                }
                mCallbacks.finishBroadcast()
                /**
                 * Repeat every 1 second.
                 */
                sendMessageDelayed(obtainMessage(REPORT_MSG), 1000)
            } else {
                super.handleMessage(msg)
            }
        }
    }

    /**
     * Show a notification while this service is running. First we fetch the resource String "Remote
     * service has started" to initialize [CharSequence] variable `val text`, then we create a
     * [PendingIntent] to launch the activity [Controller] for [PendingIntent] varible
     * `val contentIntent`. We build a [Notification] for variable `val notification` for the
     * [NotificationChannel] PRIMARY_CHANNEL using R.drawable.stat_sample as the small icon, `text`
     * for the ticker text, the current system time as the time stamp, "Sample Remote Service" as
     * the first line of the notification, `text` as the second line, and `contentIntent` as the
     * [PendingIntent] to be sent when the notification is clicked. Finally we use [mNM] to post
     * the notification.
     */
    private fun showNotification() {
        /**
         * In this sample, we'll use the same text for the ticker and the expanded notification
         */
        val text = getText(R.string.remote_service_started)
        /**
         * The [PendingIntent] to launch our activity if the user selects this notification
         */
        val contentIntent = PendingIntent.getActivity(this, 0, Intent(this, Controller::class.java), 0)
        /**
         * Set the info for the views that show in the notification panel.
         */
        val notification = Notification.Builder(this, PRIMARY_CHANNEL)
                .setSmallIcon(R.drawable.stat_sample) // the status icon
                .setTicker(text) // the status text
                .setWhen(System.currentTimeMillis()) // the time stamp
                .setContentTitle(getText(R.string.remote_service_label)) // the label of the entry
                .setContentText(text) // the contents of the entry
                .setContentIntent(contentIntent) // The intent to send when the entry is clicked
                .build()
        /**
         * Send the notification. We use a string id because it is a unique number.
         * We use it later to cancel.
         */
        mNM!!.notify(R.string.remote_service_started, notification)
    }

    // ----------------------------------------------------------------------

    /**
     * Example of explicitly starting and stopping the remove service.
     * This demonstrates the implementation of a service that runs in a different
     * process than the rest of the application, which is explicitly started and stopped
     * as desired.
     *
     * Note that this is implemented as an inner class only to keep the sample
     * all together; typically this code would appear in some separate class.
     */
    class Controller : AppCompatActivity() {
        /**
         * Called when the activity is starting. First we call through to our super's implementation
         * of `onCreate`, then we set our content view to our layout file R.layout.remote_service_controller.
         * We locate the [Button] with ID R.id.start ("Start Service") to initialize our variable
         * `var button` and set its `OnClickListener` to `OnClickListener` field [mStartListener]
         * (will call [startService] when clicked), and then set `button` by locating the [Button]
         * with R.id.stop ("Stop Service") and set its `OnClickListener` to `OnClickListener` field
         * [mStopListener] (will call [stopService] when clicked.)
         *
         * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
         */
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.remote_service_controller)
            // Watch for button clicks.
            var button = findViewById<Button>(R.id.start)
            button.setOnClickListener(mStartListener)
            button = findViewById(R.id.stop)
            button.setOnClickListener(mStopListener)
        }

        /**
         * `OnClickListener` for the R.id.start Button ("Start Service") it starts the
         * [RemoteService] service when clicked. We simply create an [Intent] for
         * `RemoteService.class` and use it to call the method [startService]
         *
         * Parameter: view of [Button] that was clicked
         */
        private val mStartListener: View.OnClickListener = View.OnClickListener {
            /**
             * Make sure the service is started.  It will continue running
             * until someone calls [stopService].
             */
            startService(Intent(this@Controller, RemoteService::class.java))
        }

        /**
         * `OnClickListener` for the R.id.stop Button ("Stop Service") it stops the
         * [RemoteService] service when clicked. We simply create an [Intent] for
         * `RemoteService.class` and use it to call the method [stopService].
         *
         * Parameter: view of Button that was clicked
         */
        private val mStopListener: View.OnClickListener = View.OnClickListener {
            /**
             * Cancel a previous call to [startService]. Note that the
             * service will not actually stop at this point if there are
             * still bound clients.
             */
            stopService(Intent(this@Controller, RemoteService::class.java))
        }
    }

    // ----------------------------------------------------------------------

    /**
     * Example of binding and unbinding to the remote service. This demonstrates the implementation
     * of a service which the client will bind to, interacting with it through an aidl interface.
     *
     * Note that this is implemented as an inner class only keep the sample
     * all together; typically this code would appear in some separate class.
     */
    class Binding : AppCompatActivity() {
        /**
         * The primary interface we will be calling on the service. This class is generated from
         * the file app/src/main/aidl/com/example/android/apis/app/ISecondary.aidl
         */
        var mService: IRemoteService? = null
        /**
         * Another interface we use on the service. This class is generated from
         * the file app/src/main/aidl/com/example/android/apis/app/IRemoteService.aidl
         */
        var mSecondaryService: ISecondary? = null
        /**
         * [Button] in our UI which kills the service process when clicked
         */
        var mKillButton: Button? = null
        /**
         * [TextView] we use to display status information
         */
        var mCallbackText: TextView? = null
        /**
         * Flag to keep track of whether we are bound to the service or not
         */
        private var mIsBound = false

        /**
         * Standard initialization of this activity. First we call through to our super's implementation
         * of `onCreate`, then we set our content view to our layout file R.layout.remote_service_binding.
         * We locate the [Button] with ID R.id.bind ("Bind Service") to initialize our variable
         * `var button` and set its `OnClickListener` to `OnClickListener` field [mBindListener]
         * (binds us to the service when clicked), set `button` to the [Button] with ID R.id.unbind
         * ("Unbind Service") and set its `OnClickListener` to `OnClickListener` field [mUnbindListener]
         * (unbinds us from the service when clicked), and set `button` to the [Button] with ID
         * R.id.kill ("Kill Process") and set its `OnClickListener` to `OnClickListener` field
         * [mKillListener] (uses the service method `getPid()` to obtain the service PID, and issues
         * a `killProcess` request). Finally we locate the [TextView] with ID R.id.callback to
         * initialize [TextView] field [mCallbackText] and set its text to ("Not attached.").
         *
         * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
         */
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.remote_service_binding)
            /**
             * Watch for button clicks.
             */
            var button = findViewById<Button>(R.id.bind)
            button.setOnClickListener(mBindListener)
            button = findViewById(R.id.unbind)
            button.setOnClickListener(mUnbindListener)
            mKillButton = findViewById(R.id.kill)
            mKillButton!!.setOnClickListener(mKillListener)
            mKillButton!!.isEnabled = false
            mCallbackText = findViewById(R.id.callback)
            mCallbackText!!.text = "Not attached."
        }

        /**
         * Class for interacting with the main interface of the service. Implements the two methods
         * `onServiceConnected` and `onServiceDisconnected` of the [ServiceConnection] interface.
         */
        private val mConnection: ServiceConnection = object : ServiceConnection {
            /**
             * Called when a connection to the Service has been established, with the
             * [android.os.IBinder] of the communication channel to the Service.
             *
             * First we use the IDL helper method `IRemoteService.Stub.asInterface` to cast the
             * [IBinder] service` object passed us into an com.example.android.apis.app.IRemoteService
             * interface, generating a proxy if needed. Then we enable the [Button] field [mKillButton]
             * ("Kill Process") and set the text of [TextView] field [mCallbackText] to "Attached.".
             *
             * Wrapped in a try block intended to catch [RemoteException] (if the service has crashed
             * already), we call the method `registerCallback` of the [IRemoteService] we cast the
             * [IBinder] parameter [service] to, thereby registering [IRemoteServiceCallback] field
             * [mCallback] as a callback interface for the service to use. [mCallback] has a
             * `valueChanged` method which the service will call when there is a new value to display
             * (every 1000 milliseconds). `valueChanged` formats and sends a Message to our [Handler]
             * field [mHandler] which will in turn display this new value on the UI thread.
             *
             * Finally we toast the message: "Connected to remote service".
             *
             * @param className The concrete component name of the service that has
             * been connected.
             * @param service   The [IBinder] of the Service's communication channel,
             * which you can now make calls on.
             */
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                /**
                 * This is called when the connection with the service has been
                 * established, giving us the service object we can use to
                 * interact with the service. We are communicating with our
                 * service through an IDL interface, so get a client-side
                 * representation of that from the raw service object.
                 */
                mService = IRemoteService.Stub.asInterface(service)
                mKillButton!!.isEnabled = true
                mCallbackText!!.text = "Attached."
                /**
                 * We want to monitor the service for as long as we are connected to it.
                 */
                try {
                    mService!!.registerCallback(mCallback)
                } catch (e: RemoteException) {
                    /**
                     * In this case the service has crashed before we could even
                     * do anything with it; we can count on soon being
                     * disconnected (and then reconnected if it can be restarted)
                     * so there is no need to do anything here.
                     */
                }
                /**
                 * As part of the sample, tell the user what happened.
                 */
                Toast.makeText(this@Binding, R.string.remote_service_connected, Toast.LENGTH_SHORT).show()
            }

            /**
             * Called when a connection to the Service has been lost. This typically
             * happens when the process hosting the service has crashed or been killed.
             * This does *not* remove the [ServiceConnection] itself -- this binding to
             * the service will remain active, and you will receive a call to
             * [onServiceConnected] when the Service is next running. And indeed this
             * is what happens when you kill the service process - it is restarted and
             * your binding continues to exist, and [onServiceConnected] is called again.
             *
             * We set [IRemoteService] field [mService] to null, disable [Button] field
             * [mKillButton], set the text of [TextView] field [mCallbackText] to "Disconnected.",
             * and toast the message "Disconnected from remote service".
             *
             * @param className The concrete component name of the service
             * whose connection has been lost.
             */
            override fun onServiceDisconnected(className: ComponentName) {
                /**
                 * This is called when the connection with the service has been
                 * unexpectedly disconnected -- that is, its process crashed.
                 */
                mService = null
                mKillButton!!.isEnabled = false
                mCallbackText!!.text = "Disconnected."
                /**
                 * As part of the sample, tell the user what happened.
                 */
                Toast.makeText(this@Binding, R.string.remote_service_disconnected, Toast.LENGTH_SHORT).show()
            }
        }
        /**
         * Class for interacting with the secondary interface of the service.
         */
        private val mSecondaryConnection: ServiceConnection = object : ServiceConnection {
            /**
             * Called when a connection to the Service has been established, with the
             * [android.os.IBinder] of the communication channel to the Service.
             *
             * First we use the IDL helper method [ISecondary.Stub.asInterface] to cast the [IBinder]
             * parameter [service] object passed to us into an com.example.android.apis.app.ISecondary
             * interface, generating a proxy if needed. Then we enable the [Button] field [mKillButton]
             * ("Kill Process").
             *
             * @param className The concrete component name of the service that has
             * been connected.
             * @param service   The [IBinder] of the Service's communication channel,
             * which you can now make calls on.
             */
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                /**
                 * Connecting to a secondary interface is the same as any other interface.
                 */
                mSecondaryService = ISecondary.Stub.asInterface(service)
                mKillButton!!.isEnabled = true
            }

            /**
             * Called when a connection to the Service has been lost.  This typically
             * happens when the process hosting the service has crashed or been killed.
             * This does *not* remove the [ServiceConnection] itself -- this binding
             * to the service will remain active, and you will receive a call to
             * [onServiceConnected] when the Service is next running. And indeed this
             * is what happens when you kill the service process - it is restarted and
             * your binding continues to exist, and `onServiceConnected` is called again.
             *
             * We set [ISecondary] field [mSecondaryService] to null, and disable [Button] field
             * [mKillButton].
             *
             * @param className The concrete component name of the service whose
             * connection has been lost.
             */
            override fun onServiceDisconnected(className: ComponentName) {
                mSecondaryService = null
                mKillButton!!.isEnabled = false
            }
        }

        /**
         * Called when the R.id.bind ("Bind Service") `Button` is clicked. First we create
         * [Intent] variable `val intent` targeting `RemoteService.class`. We set the action of
         * `intent` to the class name [IRemoteService], then we use intent to bind to the service
         * with the flag BIND_AUTO_CREATE (automatically create the service as long as the binding
         * exists), and using [ServiceConnection] field [mConnection] as the [ServiceConnection]
         * object to receive callbacks when the service connects (`onServiceConnected`) or
         * disconnects (`onServiceDisconnected`).
         *
         * We then set the action of `intent` to the class name [ISecondary] and use the intent to
         * bind to the service with the flag BIND_AUTO_CREATE (automatically create the service as
         * long as the binding exists), and using [ServiceConnection] field [mSecondaryConnection]
         * as the [ServiceConnection] object to receive callbacks when the service connects
         * (`onServiceConnected`) or disconnects (`onServiceDisconnected`).
         *
         * We then set our flag field [mIsBound] to *true* and set the text of [TextView] field
         * [mCallbackText] to "Binding."
         *
         * Parameter: View of the Button that was clicked
         */
        private val mBindListener: View.OnClickListener = View.OnClickListener {
            /**
             * Establish a couple connections with the service, binding
             * by interface names.  This allows other applications to be
             * installed that replace the remote service by implementing
             * the same interface.
             */
            val intent = Intent(this@Binding, RemoteService::class.java)
            intent.action = IRemoteService::class.java.name
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
            intent.action = ISecondary::class.java.name
            bindService(intent, mSecondaryConnection, Context.BIND_AUTO_CREATE)
            mIsBound = true
            mCallbackText!!.text = "Binding."
        }

        /**
         * Called when the R.id.unbind ("Unbind Service") [Button] is clicked. If our flag
         * field [mIsBound] is *true*, we check to see if our `onServiceConnected` method
         * has been called and initialized [IRemoteService] field [mService] with the [IBinder]
         * sent from the service, and if so we use [mService] to access the server method
         * `unregisterCallback` to unregister our [IRemoteServiceCallback] field [mCallback]
         * we previously registered using the `registerCallback` method of [mService]. Then
         * we unbind our [ServiceConnection] field [mConnection] and [ServiceConnection] field
         * [mSecondaryConnection], set our flag field [mIsBound] to *false*, and set the text
         * of [TextView] field [mCallbackText] to "Unbinding."
         *
         * Parameter: View of the Button that was clicked
         */
        private val mUnbindListener: View.OnClickListener = View.OnClickListener {
            if (mIsBound) {
                /**
                 * If we have received the service, and hence registered with
                 * it, then now is the time to unregister.
                 */
                if (mService != null) {
                    try {
                        mService!!.unregisterCallback(mCallback)
                    } catch (e: RemoteException) {
                        /**
                         * There is nothing special we need to do if the service has crashed.
                         */
                    }
                }
                /**
                 * Detach our existing connection.
                 */
                unbindService(mConnection)
                unbindService(mSecondaryConnection)
                mKillButton!!.isEnabled = false
                mIsBound = false
                mCallbackText!!.text = "Unbinding."
            }
        }

        /**
         * Called when the R.id.kill ("Kill Process") [Button] is clicked. First we make sure
         * that our interface to the remote service [ISecondary] field [mSecondaryService] is not
         * *null*, then wrapped in a try block intended to catch [RemoteException] we use the
         * service `getPid()` method of filed [mSecondaryService]` to fetch the PID of the service
         * to initialize [Int] variable `val pid`. We use `pid` in a call to `Process.killProcess(pid)`
         * to kill the PID of the service, and finally we set the text of [TextView] field
         * [mCallbackText] to "Killed service process."
         *
         * Parameter: View of Button that was clicked
         */
        private val mKillListener: View.OnClickListener = View.OnClickListener {
            /**
             * To kill the process hosting our service, we need to know its
             * PID. Conveniently our service has a call that will return
             * to us that information.
             */
            if (mSecondaryService != null) {
                try {
                    val pid = mSecondaryService!!.pid
                    /**
                     * Note that, though this API allows us to request to
                     * kill any process based on its PID, the kernel will
                     * still impose standard restrictions on which PIDs you
                     * are actually able to kill. Typically this means only
                     * the process running your application and any additional
                     * processes created by that app as shown here; packages
                     * sharing a common UID will also be able to kill each
                     * other's processes.
                     */
                    Process.killProcess(pid)
                    mCallbackText!!.text = "Killed service process."
                } catch (ex: RemoteException) {
                    /**
                     * Recover gracefully from the process hosting the server dying.
                     * Just for purposes of the sample, put up a notification.
                     */
                    Toast.makeText(this@Binding, R.string.remote_call_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }
        // ----------------------------------------------------------------------

        // Code showing how to deal with callbacks.

        // ----------------------------------------------------------------------

        /**
         * This implementation is used to receive callbacks from the remote
         * service.
         */
        private val mCallback: IRemoteServiceCallback = object : IRemoteServiceCallback.Stub() {
            /**
             * This is called by the remote service regularly to tell us about
             * new values. Note that IPC calls are dispatched through a thread
             * pool running in each process, so the code executing here will
             * NOT be running in our main thread like most other things -- so,
             * to update the UI, we need to use a [Handler] to hop over there.
             * We send that [Handler] field [mHandler] a message with the `what`
             * field set to BUMP_MSG, and our [value] parameter as the `arg1` field.
             *
             * @param value Value that the service increments once a second, and broadcasts to all
             * registered callbacks (including this one, once we register it in our
             * `onServiceConnected` callback using the method [IRemoteService.registerCallback])
             */
            override fun valueChanged(value: Int) {
                mHandler.sendMessage(mHandler.obtainMessage(BUMP_MSG, value, 0))
            }
        }
        /**
         * [Handler] running on the UI thread which other threads can use to post text into
         * the [TextView] field [mCallbackText].
         */
        @SuppressLint("HandlerLeak")
        private val mHandler: Handler = object : Handler() {
            /**
             * Subclasses must implement this to receive messages. We switch on the `what` field
             * of the `Message msg` parameter, defaulting to passing `msg` on to our super's
             * implementation of `handleMessage`. If `what` contained BUMP_MSG, we set the
             * text of [TextView] field [mCallbackText] to "Received from service: ", with the
             * value of field `msg.arg1` concatenated to the end.
             *
             * @param msg Message sent us by `mHandler.sendMessage`
             */
            override fun handleMessage(msg: Message) {
                if (msg.what == BUMP_MSG) {
                    mCallbackText!!.text = "Received from service: " + msg.arg1
                } else {
                    super.handleMessage(msg)
                }
            }
        }

        /**
         * Our static constant
         */
        companion object {
            /**
             * Message `what` field for receiving a new value from the service, value will be in `arg1`
             */
            private const val BUMP_MSG = 1
        }
    }

    // ----------------------------------------------------------------------

    /**
     * Examples of behavior of different bind flags.
     */
    class BindingOptions : AppCompatActivity() {
        /**
         * [ServiceConnection] we use to receive information as the service is started and stopped
         * after calling [bindService]
         */
        var mCurConnection: ServiceConnection? = null
        /**
         * [TextView] used to display status information about our connection
         */
        var mCallbackText: TextView? = null
        /**
         * [Intent] used to Bind to the [RemoteService] service
         */
        var mBindIntent: Intent? = null

        /**
         * Contains our implementation of the [ServiceConnection] interface, consisting of the
         * two methods `onServiceConnected` and `onServiceDisconnected`.
         */
        internal inner class MyServiceConnection : ServiceConnection {
            /**
             * Flag used to unbind in `onServiceDisconnected` when true (set to true for option
             * BIND_WAIVE_PRIORITY
             */
            val mUnbindOnDisconnect: Boolean

            /**
             * Default constructor, sets [mUnbindOnDisconnect] flag field to *false*
             */
            constructor() {
                mUnbindOnDisconnect = false
            }

            /**
             * This version of the constructor is only called for option BIND_WAIVE_PRIORITY, and
             * there the value of [mUnbindOnDisconnect] is specified to be set to true.
             *
             * @param unbindOnDisconnect value to set `mUnbindOnDisconnect` to
             */
            constructor(unbindOnDisconnect: Boolean) {
                mUnbindOnDisconnect = unbindOnDisconnect
            }

            /**
             * Called when a connection to the Service has been established, with the
             * [android.os.IBinder] of the communication channel to the Service. For some
             * unknown reason, if the current [ServiceConnection] field [mCurConnection]
             * is not equal to *this* we return having done nothing. Otherwise we set the
             * text of [TextView] field [mCallbackText] to "Attached.", and toast the message
             * "Connected to remote service".
             *
             * @param className The concrete component name of the service that has
             * been connected.
             * @param service   The [IBinder] of the Service's communication channel,
             * which you can now make calls on.
             */
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                if (mCurConnection !== this) {
                    return
                }
                mCallbackText!!.text = "Attached."
                Toast.makeText(this@BindingOptions, R.string.remote_service_connected, Toast.LENGTH_SHORT).show()
            }

            /**
             * Called when a connection to the Service has been lost.  This typically
             * happens when the process hosting the service has crashed or been killed.
             * This does *not* remove the ServiceConnection itself -- this
             * binding to the service will remain active, and you will receive a call
             * to [onServiceConnected] when the Service is next running.
             *
             * For some unknown reason, if the current [ServiceConnection] field [mCurConnection]
             * is not equal to *this* we return having done nothing. Otherwise we set the text of
             * [TextView] field [mCallbackText] to "Disconnected.", toast the message "Disconnected
             * from remote service". Then only if [mUnbindOnDisconnect] is *true*, we unbind from
             * the service, set [ServiceConnection] field [mCurConnection] to *null* and toast the
             * message "Unbinding due to disconnect"
             *
             * @param className The concrete component name of the service whose
             * connection has been lost.
             */
            override fun onServiceDisconnected(className: ComponentName) {
                if (mCurConnection !== this) {
                    return
                }
                mCallbackText!!.text = "Disconnected."
                Toast.makeText(this@BindingOptions, R.string.remote_service_disconnected, Toast.LENGTH_SHORT).show()
                if (mUnbindOnDisconnect) {
                    unbindService(this)
                    mCurConnection = null
                    Toast.makeText(this@BindingOptions, R.string.remote_service_unbind_disconn, Toast.LENGTH_SHORT).show()
                }
            }
        }

        /**
         * Called when the activity is starting. First we call through to our super's implementation
         * of `onCreate`, then we set our content view to our layout file R.layout.remote_binding_options.
         * We locate the [Button]'s in our layout file and set their `OnClickListener` as follows:
         *
         *  * R.id.bind_normal "Normal" - [mBindNormalListener]
         *  * R.id.bind_not_foreground "Not Foreground" - [mBindNotForegroundListener]
         *  * R.id.bind_above_client "Above Client" [mBindAboveClientListener]
         *  * R.id.bind_allow_oom "Allow OOM Management" [mBindAllowOomListener]
         *  * R.id.bind_waive_priority "Waive Priority" [mBindWaivePriorityListener]
         *  * R.id.bind_important "Important" [mBindImportantListener]
         *  * R.id.bind_with_activity "Adjust With Activity" [mBindWithActivityListener]
         *  * R.id.unbind "Unbind Service" [mUnbindListener]
         *
         * Then we locate the [TextView] with ID R.id.callback to initialize field [mCallbackText]
         * and set its text to "Not attached.", create an [Intent] for field [mBindIntent] to target
         * `RemoteService.class`, and set its action to [IRemoteService].
         *
         * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
         */
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.remote_binding_options)
            // Watch for button clicks.
            var button = findViewById<Button>(R.id.bind_normal)
            button.setOnClickListener(mBindNormalListener)
            button = findViewById(R.id.bind_not_foreground)
            button.setOnClickListener(mBindNotForegroundListener)
            button = findViewById(R.id.bind_above_client)
            button.setOnClickListener(mBindAboveClientListener)
            button = findViewById(R.id.bind_allow_oom)
            button.setOnClickListener(mBindAllowOomListener)
            button = findViewById(R.id.bind_waive_priority)
            button.setOnClickListener(mBindWaivePriorityListener)
            button = findViewById(R.id.bind_important)
            button.setOnClickListener(mBindImportantListener)
            button = findViewById(R.id.bind_with_activity)
            button.setOnClickListener(mBindWithActivityListener)
            button = findViewById(R.id.unbind)
            button.setOnClickListener(mUnbindListener)
            mCallbackText = findViewById(R.id.callback)
            mCallbackText!!.text = "Not attached."
            mBindIntent = Intent(this, RemoteService::class.java)
            mBindIntent!!.action = IRemoteService::class.java.name
        }

        /**
         * `OnClickListener` for the R.id.bind_normal "Normal" [Button]. When clicked we check to
         * see if our [ServiceConnection] field [mCurConnection] is connected, and if so disconnect
         * [mCurConnection] and set it to *null*. Then we create a new instance of [MyServiceConnection]
         * for our variable `val conn`, use it for the callback when we Bind to the service specified
         * by [Intent] field [mBindIntent] ([RemoteService] with the action [IRemoteService] in our
         * case) using the flag BIND_AUTO_CREATE, and if we succeed in binding we set [ServiceConnection]
         * field [mCurConnection] to `conn`.
         *
         * Parameter: View of Button that was clicked
        */
        private val mBindNormalListener: View.OnClickListener = View.OnClickListener {
            if (mCurConnection != null) {
                unbindService(mCurConnection!!)
                mCurConnection = null
            }
            val conn: ServiceConnection = MyServiceConnection()
            if (bindService(mBindIntent, conn, Context.BIND_AUTO_CREATE)) {
                mCurConnection = conn
            }
        }
        /**
         * `OnClickListener` for the R.id.bind_not_foreground "Not Foreground" [Button].
         * When clicked we check to see if our [ServiceConnection] field [mCurConnection] is
         * connected, and if so disconnect [mCurConnection] and set it to *null*. Then we create a
         * new instance of [MyServiceConnection] for variable `val conn`, use it for the callback
         * when we Bind to the service specified by [Intent] field [mBindIntent] ([RemoteService]
         * with the action [IRemoteService] in our case) using the flag BIND_AUTO_CREATE and
         * BIND_NOT_FOREGROUND, and if we succeed in binding we set [ServiceConnection] field
         * [mCurConnection] to `conn`.
         *
         * Parameter: View of Button that was clicked
         */
        private val mBindNotForegroundListener: View.OnClickListener = View.OnClickListener {
            if (mCurConnection != null) {
                unbindService(mCurConnection!!)
                mCurConnection = null
            }
            val conn: ServiceConnection = MyServiceConnection()
            val flags = Context.BIND_AUTO_CREATE or Context.BIND_NOT_FOREGROUND
            if (bindService(mBindIntent, conn, flags)) {
                mCurConnection = conn
            }
        }
        /**
         * `OnClickListener` for the R.id.bind_above_client "Above Client" [Button].
         * When clicked we check to see if our [ServiceConnection] field [mCurConnection] is
         * connected, and if so disconnect [mCurConnection] and set it to *null*. Then we create a
         * new instance of [MyServiceConnection] for variable `val conn`, use it for the callback
         * when we Bind to the service specified by [Intent] field [mBindIntent] ([RemoteService]
         * with the action [IRemoteService] in our case) using the flag BIND_AUTO_CREATE and
         * BIND_ABOVE_CLIENT, and if we succeed in binding we set our [ServiceConnection] field
         * [mCurConnection] to `conn`.
         *
         * Parameter: View of Button that was clicked
         */
        private val mBindAboveClientListener: View.OnClickListener = View.OnClickListener {
            if (mCurConnection != null) {
                unbindService(mCurConnection!!)
                mCurConnection = null
            }
            val conn: ServiceConnection = MyServiceConnection()
            val flags = Context.BIND_AUTO_CREATE or Context.BIND_ABOVE_CLIENT
            if (bindService(mBindIntent, conn, flags)) {
                mCurConnection = conn
            }
        }
        /**
         * `OnClickListener` for the R.id.bind_allow_oom "Allow OOM Management" [Button].
         * When clicked we check to see if our [ServiceConnection] field [mCurConnection] is
         * connected, and if so disconnect [mCurConnection] and set it to *null*. Then we create
         * a new instance of [MyServiceConnection] for variable `val conn`, use it for the callback
         * when we Bind to the service specified by [Intent] field [mBindIntent] ([RemoteService]
         * with the action [IRemoteService] in our case) using the flag BIND_AUTO_CREATE and
         * BIND_ALLOW_OOM_MANAGEMENT, and if we succeed in binding we set [ServiceConnection]field
         * [mCurConnection] to `conn`.
         *
         * Parameter: View of Button that was clicked
         */
        private val mBindAllowOomListener: View.OnClickListener = View.OnClickListener {
            if (mCurConnection != null) {
                unbindService(mCurConnection!!)
                mCurConnection = null
            }
            val conn: ServiceConnection = MyServiceConnection()
            val flags = Context.BIND_AUTO_CREATE or Context.BIND_ALLOW_OOM_MANAGEMENT
            if (bindService(mBindIntent, conn, flags)) {
                mCurConnection = conn
            }
        }
        /**
         * `OnClickListener` for the R.id.bind_waive_priority "Waive Priority" [Button].
         * When clicked we check to see if our [ServiceConnection] field [mCurConnection] is
         * connected, and if so disconnect [mCurConnection] and set it to *null*. Then we create
         * a new instance of [MyServiceConnection] for variable `val conn` specifying that we
         * should unbind if disconnected, (it sets the flag field `mUnbindOnDisconnect` to true in
         * the constructor) use it for the callback when we Bind to the service specified by
         * [Intent] field [mBindIntent] ([RemoteService] with the action [IRemoteService] in our
         * case) using the flag BIND_AUTO_CREATE and BIND_WAIVE_PRIORITY, and if we succeed in
         * binding we set [ServiceConnection] field [mCurConnection] to `conn`.
         *
         * Parameter: View of Button that was clicked
         */
        private val mBindWaivePriorityListener: View.OnClickListener = View.OnClickListener {
            if (mCurConnection != null) {
                unbindService(mCurConnection!!)
                mCurConnection = null
            }
            val conn: ServiceConnection = MyServiceConnection(true)
            val flags = Context.BIND_AUTO_CREATE or Context.BIND_WAIVE_PRIORITY
            if (bindService(mBindIntent, conn, flags)) {
                mCurConnection = conn
            }
        }
        /**
         * `OnClickListener` for the R.id.bind_important "Important" [Button].
         * When clicked we check to see if our [ServiceConnection] field [mCurConnection] is
         * connected, and if so disconnect [mCurConnection] and set it to *null&. Then we create
         * a new instance of [MyServiceConnection] for variable `val conn`, use it for the callback
         * when we Bind to the service specified by [Intent] field [mBindIntent] ([RemoteService]
         * with the action [IRemoteService] in our case) using the flag BIND_AUTO_CREATE and
         * BIND_IMPORTANT, and if we succeed in binding we set [ServiceConnection] field
         * [mCurConnection] to `conn`.
         *
         * Parameter: View of Button that was clicked
         */
        private val mBindImportantListener: View.OnClickListener = View.OnClickListener {
            if (mCurConnection != null) {
                unbindService(mCurConnection!!)
                mCurConnection = null
            }
            val conn: ServiceConnection = MyServiceConnection()
            val flags = Context.BIND_AUTO_CREATE or Context.BIND_IMPORTANT
            if (bindService(mBindIntent, conn, flags)) {
                mCurConnection = conn
            }
        }
        /**
         * `OnClickListener` for the R.id.bind_with_activity "Adjust With Activity" [Button].
         * When clicked we check to see if our [ServiceConnection] field [mCurConnection] is
         * connected, and if so disconnect [mCurConnection] and set it to *null*. Then we create
         * a new instance of [MyServiceConnection] to initialize `val conn`, use it for the
         * callback when we Bind to the service specified by [Intent] field [mBindIntent]
         * ([RemoteService] with the action [IRemoteService] in our case) using the flag
         * BIND_AUTO_CREATE, BIND_ADJUST_WITH_ACTIVITY, and BIND_WAIVE_PRIORITY, and if we succeed
         * in binding we set [ServiceConnection] field [mCurConnection] to `conn`.
         *
         * Parameter: View of Button that was clicked
         */
        private val mBindWithActivityListener: View.OnClickListener = View.OnClickListener {
            if (mCurConnection != null) {
                unbindService(mCurConnection!!)
                mCurConnection = null
            }
            val conn: ServiceConnection = MyServiceConnection()
            val flags = (Context.BIND_AUTO_CREATE
                    or Context.BIND_ADJUST_WITH_ACTIVITY
                    or Context.BIND_WAIVE_PRIORITY)
            if (bindService(mBindIntent, conn, flags)) {
                mCurConnection = conn
            }
        }
        /**
         * `OnClickListener` for the R.id.unbind "Unbind Service" [Button].
         * If [ServiceConnection] field [mCurConnection] is connected to the service, we unbind
         * [mCurConnection] and set it to *null*.
         *
         * Parameter: View of the Button that was clicked
         */
        private val mUnbindListener: View.OnClickListener = View.OnClickListener {
            if (mCurConnection != null) {
                unbindService(mCurConnection!!)
                mCurConnection = null
            }
        }
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
         * Used as `what` field of message sent to `mHandler` which causes `mValue`
         * to be incremented and broadcast
         */
        private const val REPORT_MSG = 1
    }
}
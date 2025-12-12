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
@file:Suppress("ReplaceNotNullAssertionWithElvisReturn")

package com.example.android.apis.app

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R
import com.example.android.apis.app.LocalService.LocalBinder


@RequiresApi(Build.VERSION_CODES.O)
@Suppress("MemberVisibilityCanBePrivate")
class LocalServiceActivities {
    /**
     * Example of explicitly starting and stopping the local service.
     * This demonstrates the implementation of a service that runs in the same
     * process as the rest of the application, which is explicitly started and stopped
     * as desired.
     *
     * Note that this is implemented as an inner class to only keep the sample
     * all together; typically this code would appear in some separate class.
     */
    class Controller : AppCompatActivity() {
        /**
         * Called when the activity is starting. First we call our super's implementation of `onCreate`,
         * then we set our content view to our layout file R.layout.local_service_controller. We
         * initialize [Button] variable `var button` by finding the view with id R.id.start ("Start
         * Service") and set its `OnClickListener` to our `OnClickListener` field [mStartListener],
         * then find for `button` the view with id R.id.stop ("Stop Service") and set its
         * `OnClickListener` to our field [mStopListener].
         *
         * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
         */
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.local_service_controller)
            // Watch for button clicks.
            var button = findViewById<Button>(R.id.start)
            button.setOnClickListener(mStartListener)
            button = findViewById(R.id.stop)
            button.setOnClickListener(mStopListener)
        }

        /**
         * Called when the `Button` with id id R.id.start ("Start Service") is clicked.
         * We call the [startService] method to start the `Service` with the class
         * name [LocalService] running in this context.
         *
         * Parameter: `View` that was clicked.
         */
        private val mStartListener: View.OnClickListener = View.OnClickListener {
            /**
             * Make sure the service is started. It will continue running
             * until someone calls [stopService]. The [Intent] we use to find
             * the service explicitly specifies our service component, because
             * we want it running in our own process and don't want other
             * applications to replace it.
             */
            startService(Intent(this@Controller, LocalService::class.java))
        }

        /**
         * Called when the [Button] with id id R.id.stop ("Stop Service") is clicked.
         * We call the [stopService] method to stop the `Service` with the class
         * name [LocalService] running.
         *
         * Parameter: `View` that was clicked.
         */
        private val mStopListener: View.OnClickListener = View.OnClickListener {
            /**
             * Cancel a previous call to `startService()`. Note that the
             * service will not actually stop at this point if there are
             * still bound clients.
             */
            stopService(Intent(this@Controller, LocalService::class.java))
        }
    }

    // ----------------------------------------------------------------------

    /**
     * Example of binding and unbinding to the local service. When we bind to a service, we receive
     * an object through which we can communicate with the service.
     *
     * Note that this is implemented as an inner class only keep the sample
     * all together; typically this code would appear in some separate class.
     */
    class Binding : AppCompatActivity() {
        /**
         * Flag indicating that we have bound to the service. Don't attempt to unbind from the
         * service unless the client has received some information about the service's state.
         */
        private var mShouldUnbind = false

        /**
         * Service object we use to interact with the service. Since [LocalService] service
         * always runs in the same process as its clients, the [IBinder] `service` passed to
         * our `onServiceConnected` callback can be cast to a concrete class to directly access
         * it and its `getService` method just returns "this".
         */
        private var mBoundService: LocalService? = null

        /**
         * [ServiceConnection] object passed to the `bindService`, receives information
         * in its callbacks as the service is started and stopped.
         */
        private val mConnection: ServiceConnection = object : ServiceConnection {
            /**
             * Called when a connection to the Service has been established, with the
             * [android.os.IBinder] of the communication channel to the Service.
             *
             * We cast our [IBinder] parameter [service] to [LocalService.LocalBinder] and
             * call its `getService` method which returns a pointer to its *this* which we save
             * in our [LocalService] field [mBoundService]. We then toast the string with resource
             * id R.string.local_service_connected ("Connected to local service").
             *
             * @param className The concrete component name of the service that has been connected.
             * @param service   The [IBinder] of the Service's communication channel, which you can
             * now make calls on.
             */
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                /**
                 * This is called when the connection with the service has been
                 * established, giving us the service object we can use to
                 * interact with the service.  Because we have bound to a explicit
                 * service that we know is running in our own process, we can
                 * cast its [IBinder] to a concrete class and directly access it.
                 */
                mBoundService = (service as LocalBinder).service
                /**
                 * Tell the user about this for our demo.
                 */
                Toast.makeText(
                    this@Binding, R.string.local_service_connected,
                    Toast.LENGTH_SHORT
                ).show()
            }

            /**
             * Called when a connection to the Service has been lost. This typically
             * happens when the process hosting the service has crashed or been killed.
             * This does *not* remove the [ServiceConnection] itself -- this
             * binding to the service will remain active, and you will receive a call
             * to [onServiceConnected] when the Service is next running.
             *
             * We set our [LocalService] field [mBoundService] to *null*, and toast the string with
             * resource id R.string.local_service_disconnected ("Disconnected from local service").
             *
             * @param className The concrete component name of the service whose connection
             * has been lost.
             */
            override fun onServiceDisconnected(className: ComponentName) {
                /**
                 * This is called when the connection with the service has been
                 * unexpectedly disconnected -- that is, its process crashed.
                 * Because it is running in our same process, we should never
                 * see this happen.
                 */
                mBoundService = null
                Toast.makeText(
                    this@Binding, R.string.local_service_disconnected,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        /**
         * Called from `OnClickListener` field [mBindListener] which is the `OnClickListener` for
         * the button with id R.id.bind ("Bind Service"), this method attempts to establish a
         * connection with the service with the clas `LocalService.class`. We call the [bindService]
         * method to bind to [LocalService] giving [ServiceConnection] field [mConnection] as the
         * callback object to receive information about the connection using the flag BIND_AUTO_CREATE
         * (automatically create the service as long as the binding exists), and if it returns true
         * (the system is in the process of bringing up a service that your client has permission to
         * bind to) we set our flag [mShouldUnbind] to *true*, if it returns *false* we log an error
         * message.
         */
        fun doBindService() {
            /**
             * Attempts to establish a connection with the service.  We use an
             * explicit class name because we want a specific service
             * implementation that we know will be running in our own process
             * (and thus won't be supporting component replacement by other
             * applications).
             */
            if (bindService(
                    Intent(this@Binding, LocalService::class.java),
                    mConnection, BIND_AUTO_CREATE
                )
            ) {
                mShouldUnbind = true
            } else {
                Log.e(
                    "MY_APP_TAG", "Error: The requested service doesn't " +
                        "exist, or this client isn't allowed access to it."
                )
            }
        }

        /**
         * Called to disconnect from the application service we bound to in our [doBindService]
         * method. If our flag [mShouldUnbind] is true we call the [unbindService] method
         * with [ServiceConnection] field [mConnection], which is the same connection interface
         * previously supplied to [bindService].
         */
        fun doUnbindService() {
            if (mShouldUnbind) {
                /**
                 * Release information about the service's state.
                 */
                unbindService(mConnection)
                mShouldUnbind = false
            }
        }

        /**
         * Perform any final cleanup before an activity is destroyed. First we call our super's
         * implementation of `onDestroy`, then we call our [doUnbindService] method
         * to disconnect from the application service if we are connected to it.
         */
        override fun onDestroy() {
            super.onDestroy()
            doUnbindService()
        }

        /**
         * Called when the button with id R.id.bind ("Bind Service") is clicked. We just call
         * our [doBindService] method to attempt to establish a connection to the service
         * `LocalService.class`.
         *
         * Parameter: `View` that was clicked.
         */
        private val mBindListener: View.OnClickListener = View.OnClickListener {
            doBindService()
        }

        /**
         * Called when the button with id R.id.unbind ("Unbind Service") is clicked. We just call
         * our [doUnbindService] method to disconnect from the service `LocalService.class`
         * if need be.
         *
         * Parameter: `View` that was clicked.
         */
        private val mUnbindListener: View.OnClickListener = View.OnClickListener {
            doUnbindService()
        }

        /**
         * Called when the activity is starting. First we call our super's implementation of `onCreate`,
         * then we set our content view to our layout file R.layout.local_service_binding. We initialize
         * [Button] variable `var button` by finding the view with id R.id.bind ("Bind Service") and
         * set its `OnClickListener` to the field [mBindListener], set `button` again by finding the
         * view with id R.id.unbind ("Unbind Service") and set its `OnClickListener` to
         * [mUnbindListener]. Finally we set `button` by finding the view with id R.id.do_something
         * ("Do something") and set its `OnClickListener` to an a lambda whose `onClick` override
         * calls the `doSomeThing` method of our binding to the service in [LocalService] field
         * [mBoundService] if it is not *null*.
         *
         * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
         */
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.local_service_binding)
            // Watch for button clicks.
            var button = findViewById<Button>(R.id.bind)
            button.setOnClickListener(mBindListener)
            button = findViewById(R.id.unbind)
            button.setOnClickListener(mUnbindListener)
            button = findViewById(R.id.do_something)
            /**
             * If `LocalService mBoundService` is not null (we have bound to [LocalService])
             * we use the [LocalService] field [mBoundService] to execute the method
             * `LocalService.doSomeThing`.
             *
             * Parameter: View of Button that was clicked
             */
            button.setOnClickListener {
                if (mBoundService != null) {
                    mBoundService!!.doSomeThing()
                }
            }
        }
    }
}
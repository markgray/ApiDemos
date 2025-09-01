/*
 * Copyright (C) 2012 The Android Open Source Project
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
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteCallbackList
import android.os.RemoteException
import android.util.Log

/**
 * This is an example if implementing a Service that uses android:isolatedProcess. When set to true,
 * this service will run under a special process that is isolated from the rest of the system.
 * The only communication with it is through the Service API (binding and starting).
 * It uses IRemoteServiceCallback.aidl and IRemoteService.aidl to specify its interface.
 */
@SuppressLint("SetTextI18n")
open class IsolatedService : Service() {
    /**
     * This is a list of callbacks that have been registered with the
     * service.  Note that this is package scoped (instead of private) so
     * that it can be accessed more efficiently from inner classes.
     */
    val mCallbacks: RemoteCallbackList<IRemoteServiceCallback> =
        RemoteCallbackList<IRemoteServiceCallback>()

    /**
     * No idea what this was intended for, suspect it to be a relic of code pasting.
     */
    @Suppress("unused")
    var mValue: Int = 0

    /**
     * Called by the system when the service is first created. We simply log the fact that we have
     * been created.
     */
    override fun onCreate() {
        Log.i("IsolatedService", "Creating IsolatedService: $this")
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed. We
     * log the fact that we are being destroyed, then disable the callback list of
     * `RemoteCallbackList<IRemoteServiceCallback>` objects in our field [mCallbacks]. All
     * registered callbacks are unregistered, and the list is disabled so that future calls to
     * `register(E)` will fail. This should be used when a [Service] is stopping, to prevent
     * clients from registering callbacks after it is stopped.
     */
    override fun onDestroy() {
        Log.i("IsolatedService", "Destroying IsolatedService: $this")
        // Unregister all callbacks.
        mCallbacks.kill()
    }

    /**
     * Return the communication channel to the service. We simply return our field
     * [IRemoteService.Stub] field [mBinder] which is defined in the aidl file
     * IRemoteService.aidl
     *
     * @param intent The [Intent] that was used to bind to this service,
     * @return Return an [IBinder] through which clients can call on to the service.
     */
    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    /**
     * The [IRemoteService] interface is defined through IDL in the file IRemoteService.aidl
     */
    @Suppress("SENSELESS_COMPARISON")
    private val mBinder: IRemoteService.Stub = object : IRemoteService.Stub() {
        override fun registerCallback(cb: IRemoteServiceCallback) {
            if (cb != null) mCallbacks.register(cb)
        }

        override fun unregisterCallback(cb: IRemoteServiceCallback) {
            if (cb != null) mCallbacks.unregister(cb)
        }
    }

    /**
     * This is called if the service is currently running and the user has
     * removed a task that comes from the service's application.  If you have
     * set [ServiceInfo.FLAG_STOP_WITH_TASK][android.content.pm.ServiceInfo.FLAG_STOP_WITH_TASK]
     * then you will not receive this callback; instead, the service will simply
     * be stopped.
     *
     * We log the fact that a task has been removed, then stop our service.
     *
     * @param rootIntent The original root [Intent] that was used to launch
     * the task that is being removed.
     */
    override fun onTaskRemoved(rootIntent: Intent) {
        Log.i("IsolatedService", "Task removed in $this: $rootIntent")
        stopSelf()
    }

    /**
     * Example of how to broadcast to all the callbacks that have been registered with your service.
     * (NOT USED). First we prepare `RemoteCallbackList<IRemoteServiceCallback>` field [mCallbacks]
     * to start making calls to the currently registered callbacks, saving the number of callbacks
     * in the broadcast in `val n` to use to end our loop. Then we loop through each of the items
     * calling their overload of `valueChanged`. When done we clean up the state of the
     * broadcast of `mCallbacks`.
     *
     * @param value an [Int] that should be broadcast to all the currently registered callbacks
     * override of the `valueChanged(int)` method defined in IRemoteServiceCallback.aidl
     */
    @Suppress("unused")
    private fun broadcastValue(value: Int) { // Broadcast to all clients the new value.
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
    }
}
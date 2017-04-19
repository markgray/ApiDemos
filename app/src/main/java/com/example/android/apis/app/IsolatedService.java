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

package com.example.android.apis.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R;

/**
 * This is an example if implementing a Service that uses android:isolatedProcess. When set to true,
 * this service will run under a special process that is isolated from the rest of the system.
 * The only communication with it is through the Service API (binding and starting).
 * It uses IRemoteServiceCallback.aidl and IRemoteService.aidl to specify its interface.
 */
@SuppressLint("SetTextI18n")
public class IsolatedService extends Service {
    /**
     * This is a list of callbacks that have been registered with the
     * service.  Note that this is package scoped (instead of private) so
     * that it can be accessed more efficiently from inner classes.
     */
    final RemoteCallbackList<IRemoteServiceCallback> mCallbacks = new RemoteCallbackList<>();

    @SuppressWarnings("unused")
    int mValue = 0; // No idea

    /**
     * Called by the system when the service is first created. We simply log the fact that we have
     * been created.
     */
    @Override
    public void onCreate() {
        Log.i("IsolatedService", "Creating IsolatedService: " + this);
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed. We
     * log the fact that we are being destroyed, then disable the callback list
     * {@code RemoteCallbackList<IRemoteServiceCallback> mCallbacks}. All registered callbacks are
     * unregistered, and the list is disabled so that future calls to register(E) will fail. This
     * should be used when a Service is stopping, to prevent clients from registering callbacks
     * after it is stopped.
     */
    @Override
    public void onDestroy() {
        Log.i("IsolatedService", "Destroying IsolatedService: " + this);
        // Unregister all callbacks.
        mCallbacks.kill();
    }

    /**
     * Return the communication channel to the service. We simply return our field
     * {@code IRemoteService.Stub mBinder} which is defined in the aidl file
     * IRemoteService.aidl
     *
     * @param intent The Intent that was used to bind to this service,
     * @return Return an IBinder through which clients can call on to the
     * service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * The IRemoteInterface is defined through IDL in the file IRemoteService.aidl
     */
    private final IRemoteService.Stub mBinder = new IRemoteService.Stub() {
        @Override
        public void registerCallback(IRemoteServiceCallback cb) {
            if (cb != null) mCallbacks.register(cb);
        }

        @Override
        public void unregisterCallback(IRemoteServiceCallback cb) {
            if (cb != null) mCallbacks.unregister(cb);
        }
    };

    /**
     * This is called if the service is currently running and the user has
     * removed a task that comes from the service's application.  If you have
     * set {@link android.content.pm.ServiceInfo#FLAG_STOP_WITH_TASK ServiceInfo.FLAG_STOP_WITH_TASK}
     * then you will not receive this callback; instead, the service will simply
     * be stopped.
     * <p>
     * We log the fact that a task has been removed, then stop our service.
     *
     * @param rootIntent The original root Intent that was used to launch
     *                   the task that is being removed.
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i("IsolatedService", "Task removed in " + this + ": " + rootIntent);
        stopSelf();
    }

    /**
     * Example of how to broadcast to all the callbacks that have been registered with your service.
     * (NOT USED). First we prepare {@code RemoteCallbackList<IRemoteServiceCallback> mCallbacks} to
     * start making calls to the currently registered callbacks, saving the number of callbacks in
     * the broadcast in {@code N} to use to end our loop. Then we loop through each of the items
     * calling their overload of {@code valueChanged}. When done we clean up the state of the
     * broadcast of {@code mCallbacks}.
     *
     * @param value an int that should be broadcast to all the currently registered callbacks override
     *              of the {@code valueChanged(int)} method defined in IRemoteServiceCallback.aidl
     */
    @SuppressWarnings("unused")
    private void broadcastValue(int value) {
        // Broadcast to all clients the new value.
        final int N = mCallbacks.beginBroadcast();
        for (int i = 0; i < N; i++) {
            try {
                mCallbacks.getBroadcastItem(i).valueChanged(value);
            } catch (RemoteException e) {
                // The RemoteCallbackList will take care of removing
                // the dead object for us.
            }
        }
        mCallbacks.finishBroadcast();
    }

    // ----------------------------------------------------------------------

    /**
     * Controller UI for our IsolatedService and IsolatedService2 demo.
     * Note that this is implemented as an inner class only keep the sample
     * all together; typically this code would appear in some separate class.
     */
    public static class Controller extends Activity {
        /**
         * Static inner class used to hold and manipulate our two services IsolateService and
         * IsolatedService2
         */
        static class ServiceInfo {
            final Activity mActivity; // "this" Activity in our call from onCreate of Controller Activity
            final Class<?> mClz; // Class of the service to be controlled
            final TextView mStatus; // TextView to display status in
            boolean mServiceBound; // boolean flag for whether we are bound to or not
            IRemoteService mService; // Defined in IRemoteService.aidl

            /**
             * This constructor initializes fields needed to start the {@code Class<?> clz} service,
             * sets the {@code OnClickListener} of Buttons used to control the service, and saves a
             * reference to the {@code TextView} to use to display the status of the service.
             *
             * First we initialize our field {@code Activity mActivity} to our parameter {@code Activity activity},
             * and our field {@code Class<?> mClz} to our parameter {@code Class<?> clz}. Then we locate
             * the {@code Button} with resource ID {@code start} and set its {@code OnClickListener} to
             * {@code OnClickListener mStartListener}, locate the {@code Button} with resource ID {@code stop}
             * and set its {@code OnClickListener} to {@code OnClickListener mStopListener}, and locate
             * the {@code CheckBox} with resource ID {@code bind} and set its {@code OnClickListener} to
             * {@code OnClickListener mBindListener}. Finally we initialize our field {@code TextView mStatus}
             * by locating the {@code TextView} with resource ID {@code status}.
             *
             * @param activity "this" when called from {@code onCreate} of {@code Controller} Activity
             * @param clz class of the service, either {@code IsolatedService.class} or {@code IsolatedService2.class}
             * @param start resource ID for the start Button for the service
             * @param stop resource ID for the stop Button for the service
             * @param bind resource ID for the bind Button for the service
             * @param status resource ID for the status TextView for the service
             */
            ServiceInfo(Activity activity, Class<?> clz, int start, int stop, int bind, int status) {
                mActivity = activity;
                mClz = clz;
                Button button = (Button) mActivity.findViewById(start);
                button.setOnClickListener(mStartListener);
                button = (Button) mActivity.findViewById(stop);
                button.setOnClickListener(mStopListener);
                CheckBox cb = (CheckBox) mActivity.findViewById(bind);
                cb.setOnClickListener(mBindListener);
                mStatus = (TextView) mActivity.findViewById(status);
            }

            /**
             * Called from the {@code onDestroy} callback of the {@code Controller} activity. If we
             * are currently bound to our service, we disconnect from it.
             */
            void destroy() {
                if (mServiceBound) {
                    mActivity.unbindService(mConnection);
                }
            }

            /**
             * {@code OnClickListener} for the Button with resource ID {@code start}, we create an
             * {@code Intent} to start the service {@code Class<?> mClz} and start it when the Button
             * is clicked.
             */
            private OnClickListener mStartListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.startService(new Intent(mActivity, mClz));
                }
            };

            /**
             * {@code OnClickListener} for the Button with resource ID {@code stop}, we create an
             * {@code Intent} to stop the service {@code Class<?> mClz} and stop it when the Button
             * is clicked.
             */
            private OnClickListener mStopListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.stopService(new Intent(mActivity, mClz));
                }
            };

            /**
             * {@code OnClickListener} for the {@code CheckBox} with resource ID {@code bind}. When
             * we are clicked, we check to see if our {@code CheckBox} is checked and if so we check
             * to make sure we are not already bound ({@code mServiceBound} is true), and if not
             * we bind to the service {@code Class<?> mClz} using {@code ServiceConnection mConnection}
             * as the {@code ServiceConnection} object, set our flag {@code mServiceBound} to true,
             * and set the text of {@code TextView mStatus} to "BOUND". If it is not checked and we
             * are bound to the service, we disconnect from the service, set our flag {@code mServiceBound}
             * to false and set the text of {@code mStatus} to the empty String.
             */
            private OnClickListener mBindListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((CheckBox) v).isChecked()) {
                        if (!mServiceBound) {
                            if (mActivity.bindService(new Intent(mActivity, mClz), mConnection, Context.BIND_AUTO_CREATE)) {
                                mServiceBound = true;
                                mStatus.setText("BOUND");
                            }
                        }
                    } else {
                        if (mServiceBound) {
                            mActivity.unbindService(mConnection);
                            mServiceBound = false;
                            mStatus.setText("");
                        }
                    }
                }
            };

            private ServiceConnection mConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName className, IBinder service) {
                    mService = IRemoteService.Stub.asInterface(service);
                    if (mServiceBound) {
                        mStatus.setText("CONNECTED");
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName className) {
                    // This is called when the connection with the service has been
                    // unexpectedly disconnected -- that is, its process crashed.
                    mService = null;
                    if (mServiceBound) {
                        mStatus.setText("DISCONNECTED");
                    }
                }
            };
        }

        ServiceInfo mService1;
        ServiceInfo mService2;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.isolated_service_controller);

            mService1 = new ServiceInfo(this, IsolatedService.class, R.id.start1, R.id.stop1, R.id.bind1, R.id.status1);
            mService2 = new ServiceInfo(this, IsolatedService2.class, R.id.start2, R.id.stop2, R.id.bind2, R.id.status2);
        }

        /**
         * Perform any final cleanup before an activity is destroyed.  This can
         * happen either because the activity is finishing (someone called
         * {@link #finish} on it, or because the system is temporarily destroying
         * this instance of the activity to save space.  You can distinguish
         * between these two scenarios with the {@link #isFinishing} method.
         *
         * First we call through to our super's implementation of {@code onDestroy}, then we instruct
         * the services referenced by {@code ServiceInfo mService1} and {@code ServiceInfo mService2}
         * to unbind from their service if they are currently bound to it.
         */
        @Override
        protected void onDestroy() {
            super.onDestroy();
            mService1.destroy();
            mService2.destroy();
        }
    }
}

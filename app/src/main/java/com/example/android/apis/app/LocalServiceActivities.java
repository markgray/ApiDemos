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

package com.example.android.apis.app;

import com.example.android.apis.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * This Class consists of two Activities: ".app.LocalServiceActivities$Binding", and
 * ".app.LocalServiceActivities$Controller", which are accessed from the main app by
 * "App/Service/Local Service Binding" and "App/Service/Local Service Controller"
 * respectively. Both of these activities use the service implemented in
 * {@code LocalService}.
 */
public class LocalServiceActivities {
    /**
     * Example of explicitly starting and stopping the local service.
     * This demonstrates the implementation of a service that runs in the same
     * process as the rest of the application, which is explicitly started and stopped
     * as desired.
     */
    public static class Controller extends Activity {
        /**
         * Called when the activity is starting. First we call through to our super's implementation
         * of {@code onCreate}, then we set our content view to our layout file
         * R.layout.local_service_controller. We locate the Button with ID R.id.start and set its
         * {@code OnClickListener} to {@code OnClickListener mStartListener}, and we locate the Button
         * with ID R.id.stop and set its {@code OnClickListener} to {@code OnClickListener mStopListener}.
         * When clicked these will request that the application service {@code LocalService} be started,
         * and stopped respectively.
         *
         * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
         */
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.local_service_controller);

            // Watch for button clicks.
            Button button = (Button) findViewById(R.id.start);
            button.setOnClickListener(mStartListener);
            button = (Button) findViewById(R.id.stop);
            button.setOnClickListener(mStopListener);
        }

        /**
         * {@code OnClickListener} used for the R.id.start "Start Service" Button, it creates an Intent
         * for the service {@code LocalService} and starts it.
         */
        private OnClickListener mStartListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Make sure the service is started.  It will continue running
                // until someone calls stopService().  The Intent we use to find
                // the service explicitly specifies our service component, because
                // we want it running in our own process and don't want other
                // applications to replace it.
                startService(new Intent(Controller.this, LocalService.class));
            }
        };

        /**
         * {@code OnClickListener} used for the R.id.stop "Stop Service" Button, it creates an Intent
         * for the service {@code LocalService} and uses it to stop it.
         */
        private OnClickListener mStopListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cancel a previous call to startService().  Note that the
                // service will not actually stop at this point if there are
                // still bound clients.
                stopService(new Intent(Controller.this, LocalService.class));
            }
        };
    }

    // ----------------------------------------------------------------------

    /**
     * Example of binding and unbinding to the local service.
     * This demonstrates the implementation of a service which the client will
     * bind to, receiving an object through which it can communicate with the service.
     * <p>
     * Note that this is implemented as an inner class only keep the sample
     * all together; typically this code would appear in some separate class.
     */
    public static class Binding extends Activity {
        private boolean mIsBound; // Flag to indicate whether we are bound to the LocalService service
        private LocalService mBoundService; // Handle to the LocalService service instance we are bound to.

        /**
         * Called when the activity is starting. First we call through to our super's implementation
         * of {@code onCreate}, then we set our content view to our layout file R.layout.local_service_binding.
         * We locate the Button with ID R.id.bind ("BIND SERVICE") and set its {@code OnClickListener}
         * to {@code OnClickListener mBindListener} which will call our method {@code doBindService}
         * to bind to {@code LocalService}. We locate the Button with ID R.id.unbind ("UNBIND SERVICE")
         * and set its {@code OnClickListener} to {@code OnClickListener mUnbindListener}. We locate
         * the Button with ID R.id.do_something ("DO SOMETHING"), and set its {@code OnClickListener}
         * to an anonymous class which will call the {@code LocalService} method {@code doSomeThing}
         * when the Button is clicked.
         *
         * @param savedInstanceState We do not override {@code onSaveInstanceState} so do not use
         */
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.local_service_binding);

            // Watch for button clicks.
            Button button = (Button) findViewById(R.id.bind);
            button.setOnClickListener(mBindListener);
            button = (Button) findViewById(R.id.unbind);
            button.setOnClickListener(mUnbindListener);
            button = (Button) findViewById(R.id.do_something);
            button.setOnClickListener(new OnClickListener() {
                /**
                 * If {@code LocalService mBoundService} is not null (we have bound to {@code LocalService})
                 * we use {@code mBoundService} to execute the method {@code LocalService.doSomeThing}.
                 *
                 * @param v View of Button that was clicked
                 */
                @Override
                public void onClick(View v) {
                    if (mBoundService != null) {
                        mBoundService.doSomeThing();
                    }
                }
            });
        }

        /**
         * Perform any final cleanup before an activity is destroyed.  This can
         * happen either because the activity is finishing (someone called
         * {@link #finish} on it, or because the system is temporarily destroying
         * this instance of the activity to save space.  You can distinguish
         * between these two scenarios with the {@link #isFinishing} method.
         * First we call through to our super's implementation of {@code onDestroy},
         * then we call our method {@code doUnbindService} which will disconnect from
         * the {@code LocalService} service.
         */
        @Override
        protected void onDestroy() {
            super.onDestroy();
            doUnbindService();
        }

        /**
         * {@code OnClickListener} for the R.id.bind Button ("BIND SERVICE"), it simply calls our
         * method {@code doBindService} to bind to {@code LocalService}
         */
        private OnClickListener mBindListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                doBindService();
            }
        };

        /**
         * {@code OnClickListener} for the R.id.unbind Button ("UNBIND SERVICE"), it simply calls our
         * method {@code doUnbindService} to disconnect from {@code LocalService}
         */
        private OnClickListener mUnbindListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                doUnbindService();
            }
        };

        /**
         * {@code ServiceConnection} used in call to {@code bindService}, and {@code unbindService}.
         * It consists of callbacks {@code onServiceConnected} which is called when we have successfully
         * bound to the service, and {@code onServiceDisconnected} when we are unbound to the service.
         */
        private ServiceConnection mConnection = new ServiceConnection() {
            /**
             * Called when a connection to the Service has been established, with
             * the {@link android.os.IBinder} of the communication channel to the
             * Service.
             * <p>
             * We cast our parameter {@code IBinder service} to an instance of
             * {@code LocalService.LocalBinder} and use its method {@code getService}
             * to fetch the value of "this" for the instance of {@code LocalService} which
             * we have bound to. This we use to initialize {@code LocalService mBoundService}
             * which we can use to access methods and fields of our connected service. We also
             * pop up a toast "Connected to local service" to inform the user that we have
             * successfully bound to the service
             *
             * @param className The concrete component name of the service that has
             *                  been connected.
             * @param service   The IBinder of the Service's communication channel,
             *                  which you can now make calls on.
             */
            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                // This is called when the connection with the service has been
                // established, giving us the service object we can use to
                // interact with the service.  Because we have bound to a explicit
                // service that we know is running in our own process, we can
                // cast its IBinder to a concrete class and directly access it.
                mBoundService = ((LocalService.LocalBinder) service).getService();

                // Tell the user about this for our demo.
                Toast.makeText(Binding.this, R.string.local_service_connected, Toast.LENGTH_SHORT).show();
            }

            /**
             * Called when a connection to the Service has been lost.  This typically
             * happens when the process hosting the service has crashed or been killed.
             * This does <em>not</em> remove the ServiceConnection itself -- this
             * binding to the service will remain active, and you will receive a call
             * to {@link #onServiceConnected} when the Service is next running.
             *
             * We set {@code LocalService mBoundService} to null, and toast "Disconnected from local service"
             * to inform the user that we have unbound from our service connection
             *
             * @param className The concrete component name of the service whose
             *                  connection has been lost.
             */
            @Override
            public void onServiceDisconnected(ComponentName className) {
                // This is called when the connection with the service has been
                // unexpectedly disconnected -- that is, its process crashed.
                // Because it is running in our same process, we should never
                // see this happen.
                // NOTE: it is also called when we call unbindService(ServiceConnection) if we started
                // LocalService by calling bindService with BIND_AUTO_CREATE. When LocalService is
                // started using the Controller Activity LocalService continues to run even if we
                // call unbindService in this Activity.
                mBoundService = null;
                Toast.makeText(Binding.this, R.string.local_service_disconnected, Toast.LENGTH_SHORT).show();
            }
        };

        /**
         * Establish a connection with the service. We create an Intent using "this" Activity as the
         * Context and {@code LocalService} as the class of the service we wish to bind to, supply
         * {@code ServiceConnection mConnection} to receive the callbacks for the connection, and
         * BIND_AUTO_CREATE as the flag (automatically create the service as long as the binding exists).
         * Finally we set our flag {@code mIsBound} to true.
         */
        void doBindService() {
            // c  We use an explicit
            // class name because we want a specific service implementation that
            // we know will be running in our own process (and thus won't be
            // supporting component replacement by other applications).
            bindService(new Intent(Binding.this, LocalService.class), mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }

        /**
         * Disconnect from {@code LocalService} if we are currently bound to it. If our flag {@code mIsBound}
         * is true we call {@code unbindService} to disconnect from our service, and set {@code mIsBound} to
         * false.
         */
        void doUnbindService() {
            if (mIsBound) {
                // Detach our existing connection.
                unbindService(mConnection);
                mIsBound = false;
            }
        }
    }
}

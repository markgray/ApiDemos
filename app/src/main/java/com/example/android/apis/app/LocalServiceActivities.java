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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.example.android.apis.R;

public class LocalServiceActivities {
    /**
     * <p>Example of explicitly starting and stopping the local service.
     * This demonstrates the implementation of a service that runs in the same
     * process as the rest of the application, which is explicitly started and stopped
     * as desired.</p>
     *
     * <p>Note that this is implemented as an inner class only keep the sample
     * all together; typically this code would appear in some separate class.
     */
    public static class Controller extends Activity {
        /**
         * Called when the activity is starting. First we call our super's implementation of {@code onCreate},
         * then we set our content view to our layout file R.layout.local_service_controller. We initialize
         * {@code Button button} by finding the view with id R.id.start ("Start Service") and set its
         * {@code OnClickListener} to our field {@code OnClickListener mStartListener}, then find for
         * {@code button} the view with id R.id.stop ("Stop Service") and set its {@code OnClickListener}
         * to our field {@code OnClickListener mStopListener}.
         *
         * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
         */
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.local_service_controller);

            // Watch for button clicks.
            Button button = findViewById(R.id.start);
            button.setOnClickListener(mStartListener);
            button = findViewById(R.id.stop);
            button.setOnClickListener(mStopListener);
        }

        /**
         * {@code OnClickListener} for the {@code Button} with id id R.id.start ("Start Service"),
         * the {@code onClick} override starts the {@code LocalService} {@code Service}.
         */
        private OnClickListener mStartListener = new OnClickListener() {
            /**
             * Called when the {@code Button} with id id R.id.start ("Start Service") is clicked.
             * We call the {@code startService} method to start the {@code Service} with the class
             * name {@code LocalService} running in this context.
             *
             * @param v {@code View} that was clicked.
             */
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
         * {@code OnClickListener} for the {@code Button} with id id R.id.stop ("Stop Service"),
         * the {@code onClick} override stops the {@code LocalService} {@code Service}.
         */
        private OnClickListener mStopListener = new OnClickListener() {
            /**
             * Called when the {@code Button} with id id R.id.stop ("Stop Service") is clicked.
             * We call the {@code stopService} method to stop the {@code Service} with the class
             * name {@code LocalService} running.
             *
             * @param v {@code View} that was clicked.
             */
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
     * bind to, receiving an object through which it can communicate with the service.
     * <p>
     * Note that this is implemented as an inner class only keep the sample
     * all together; typically this code would appear in some separate class.
     */
    public static class Binding extends Activity {
        /**
         * Flag indicating that we have bound to the service. Don't attempt to unbind from the service
         * unless the client has received some information about the service's state.
         */
        private boolean mShouldUnbind;

        /**
         * Service object we use to interact with the service. Since {@code LocalService} service
         * always runs in the same process as its clients, the {@code IBinder service} passed to
         * our {@code onServiceConnected} callback can be cast to a concrete class to directly access
         * it and its {@code getService} method just returns "this".
         */
        private LocalService mBoundService;

        /**
         * {@code ServiceConnection} object passed to the {@code bindService}, receives information
         * in its callbacks as the service is started and stopped.
         */
        private ServiceConnection mConnection = new ServiceConnection() {
            /**
             * Called when a connection to the Service has been established, with the
             * {@link android.os.IBinder} of the communication channel to the Service.
             * <p>
             * We cast our parameter {@code IBinder service} to {@code LocalService.LocalBinder} and
             * call its {@code getService} method which returns a pointer to its 'this' which we save
             * in our field {@code LocalService mBoundService}. We then toast the string with resource
             * id R.string.local_service_connected ("Connected to local service").
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
                mBoundService = ((LocalService.LocalBinder)service).getService();

                // Tell the user about this for our demo.
                Toast.makeText(Binding.this, R.string.local_service_connected,
                        Toast.LENGTH_SHORT).show();
            }

            /**
             * Called when a connection to the Service has been lost. This typically
             * happens when the process hosting the service has crashed or been killed.
             * This does <em>not</em> remove the ServiceConnection itself -- this
             * binding to the service will remain active, and you will receive a call
             * to {@link #onServiceConnected} when the Service is next running.
             * <p>
             * We set our field {@code LocalService mBoundService} to null, and toast the string with
             * resource id R.string.local_service_disconnected ("Disconnected from local service").
             *
             * @param className The concrete component name of the service whose
             * connection has been lost.
             */
            @Override
            public void onServiceDisconnected(ComponentName className) {
                // This is called when the connection with the service has been
                // unexpectedly disconnected -- that is, its process crashed.
                // Because it is running in our same process, we should never
                // see this happen.
                mBoundService = null;
                Toast.makeText(Binding.this, R.string.local_service_disconnected,
                        Toast.LENGTH_SHORT).show();
            }
        };

        /**
         * Called from {@code OnClickListener mBindListener} which is the {@code OnClickListener} for
         * the button with id R.id.bind ("Bind Service"), this method attempts to establish a connection
         * with the service {@code LocalService.class}. We call the {@code bindService} method to bind
         * to {@code LocalService} giving {@code ServiceConnection mConnection} as the callback object
         * to receive information about the connection using the flag BIND_AUTO_CREATE (automatically
         * create the service as long as the binding exists), and if it returns true (the system is
         * in the process of bringing up a service that your client has permission to bind to) we set
         * our flag {@code mShouldUnbind} to true, if it returns false we log an error message.
         */
        void doBindService() {
            // Attempts to establish a connection with the service.  We use an
            // explicit class name because we want a specific service
            // implementation that we know will be running in our own process
            // (and thus won't be supporting component replacement by other
            // applications).
            if (bindService(new Intent(Binding.this, LocalService.class),
                    mConnection, Context.BIND_AUTO_CREATE)) {
                mShouldUnbind = true;
            } else {
                Log.e("MY_APP_TAG", "Error: The requested service doesn't " +
                        "exist, or this client isn't allowed access to it.");
            }
        }

        /**
         * Called to disconnect from the application service we bound to in our {@code doBindService}
         * method. If our flag {@code mShouldUnbind} is true we call the {@code unbindService} method
         * with {@code ServiceConnection mConnection}, which is the same connection interface previously
         * supplied to {@code bindService}.
         */
        void doUnbindService() {
            if (mShouldUnbind) {
                // Release information about the service's state.
                unbindService(mConnection);
                mShouldUnbind = false;
            }
        }

        /**
         * Perform any final cleanup before an activity is destroyed. First we call our super's
         * implementation of {@code onDestroy}, then we call our {@code doUnbindService} method
         * to disconnect from the application service if we are connected to it.
         */
        @Override
        protected void onDestroy() {
            super.onDestroy();
            doUnbindService();
        }

        /**
         * {@code OnClickListener} for the button with id R.id.bind ("Bind Service"), the
         * {@code onClick} override we just calls our {@code doBindService} method to attempt to
         * establish a connection to the service {@code LocalService.class}.
         */
        private OnClickListener mBindListener = new OnClickListener() {
            /**
             * Called when the button with id R.id.bind ("Bind Service") is clicked. We just call
             * our {@code doBindService} method to attempt to establish a connection to the service
             * {@code LocalService.class}.
             *
             * @param v {@code View} that was clicked.
             */
            @Override
            public void onClick(View v) {
                doBindService();
            }
        };

        /**
         * {@code OnClickListener} for the button with id R.id.unbind ("Unbind Service"), the
         * {@code onClick} override we just calls our {@code doUnbindService} method to disconnect
         * from the service {@code LocalService.class} if need be.
         */
        private OnClickListener mUnbindListener = new OnClickListener() {
            /**
             * Called when the button with id R.id.unbind ("Unbind Service") is clicked. We just call
             * our {@code doUnbindService} method to disconnect from the service {@code LocalService.class}
             * if need be.
             *
             * @param v {@code View} that was clicked.
             */
            @Override
            public void onClick(View v) {
                doUnbindService();
            }
        };

        /**
         * Called when the activity is starting. First we call our super's implementation of {@code onCreate},
         * then we set our content view to our layout file R.layout.local_service_binding. We initialize
         * {@code Button button} by finding the view with id R.id.bind ("Bind Service") and set its
         * {@code OnClickListener} to {@code mBindListener}, set {@code button} again by finding the
         * view with id R.id.unbind ("Unbind Service") and set its {@code OnClickListener} to
         * {@code mUnbindListener}. Finally we set {@code button} by finding the view with id
         * R.id.do_something ("Do something") and set its {@code OnClickListener} to an anonymous
         * class whose {@code onClick} override calls the {@code doSomeThing} method of our binding
         * to the service in {@code LocalService mBoundService} if it is not null.
         *
         * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
         */
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.local_service_binding);

            // Watch for button clicks.
            Button button = findViewById(R.id.bind);
            button.setOnClickListener(mBindListener);
            button = findViewById(R.id.unbind);
            button.setOnClickListener(mUnbindListener);
            button = findViewById(R.id.do_something);
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
    }
}

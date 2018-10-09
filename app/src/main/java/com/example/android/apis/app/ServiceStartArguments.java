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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.example.android.apis.R;

/**
 * This is an example of implementing an application service that runs locally
 * in the same process as the application.  The {@link Controller}
 * class shows how to interact with the service.
 * <p>
 * Notice the use of the {@link NotificationManager} when interesting things
 * happen in the service.  This is generally how background services should
 * interact with the user, rather than doing something more disruptive such as
 * calling startActivity().
 * <p>
 * For applications targeting Android 1.5 or beyond, you may want consider
 * using the {@link android.app.IntentService} class, which takes care of all the
 * work of creating the extra thread and dispatching commands to it.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ServiceStartArguments extends Service {
    /**
     * TAG for logging
     */
    final static String TAG = "ServiceStartArguments";
    /**
     * Handle to the system level {@code NotificationManager} service
     */
    private NotificationManager mNM;

    /**
     * {@code Looper} for the {@code HandlerThread} background thread we create to run our service in
     */
    private volatile Looper mServiceLooper;
    /**
     * {@code Handler} for messages sent to our service thread
     */
    private volatile ServiceHandler mServiceHandler;

    /**
     * This is a {@code Handler} class which is used to receive messages sent to the thread that is
     * running our service.
     */
    @SuppressWarnings("WeakerAccess")
    private final class ServiceHandler extends Handler {
        /**
         * We just pass our parameter through to our super's constructor so that it uses this
         * {@code Looper} instead of the default one.
         *
         * @param looper {@code Looper} for {@code HandlerThread} that is running our service
         */
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        /**
         * Subclasses must implement this to receive messages. First we retrieve {@code Bundle arguments}
         * from the {@code Object obj} of our {@code Message msg} parameter. Then we extract {@code String txt}
         * which is stored in {@code arguments} under the key "name", and {@code boolean redeliver} which
         * is stored under the key "redeliver" (if set, otherwise we default to false). We then log the
         * {@code msg} we have received. If {@code redeliver} is false we prepend "New cmd #" to txt,
         * otherwise we prepend "Re-delivered #". We call our method {@code showNotification} to display
         * a notification containing {@code txt}.
         * <p>
         * Then we wait for 5 seconds before calling our method {@code hideNotification} to dismiss our
         * notification, log a message "Done with #", and stop ourselves.
         *
         * @param msg A {@link android.os.Message Message} object
         */
        @Override
        public void handleMessage(Message msg) {
            Bundle arguments = (Bundle) msg.obj;

            String txt = arguments.getString("name");
            boolean redeliver = arguments.getBoolean("redeliver", false);

            Log.i(TAG, "Message: " + msg + ", " + arguments.getString("name"));

            if (!redeliver) {
                txt = "New cmd #" + msg.arg1 + ": " + txt;
            } else {
                txt = "Re-delivered #" + msg.arg1 + ": " + txt;
            }

            showNotification(txt);

            // Normally we would do some work here...  for our sample, we will
            // just sleep for 5 seconds.
            long endTime = System.currentTimeMillis() + 5 * 1000;
            while (System.currentTimeMillis() < endTime) {
                synchronized (this) {
                    try {
                        wait(endTime - System.currentTimeMillis());
                    } catch (Exception e) {
                        Log.i(TAG, e.getLocalizedMessage());
                    }
                }
            }

            hideNotification();

            Log.i(TAG, "Done with #" + msg.arg1);
            stopSelf(msg.arg1);
        }

    }

    /**
     * Called by the system when the service is first created. First we initialize our field
     * {@code NotificationManager mNM} with a handle to the NOTIFICATION_SERVICE system level
     * service, then we display a toast with the message "Service created." We next create
     * {@code HandlerThread thread} with the thread name "ServiceStartArgumentsBackground", and
     * the priority THREAD_PRIORITY_BACKGROUND. We start the {@code thread} running, then retrieve
     * the {@code Looper mServiceLooper} of the thread and use it to construct an instance of
     * {@code ServiceHandler} for {@code ServiceHandler mServiceHandler}.
     */
    @Override
    public void onCreate() {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Toast.makeText(this, R.string.service_created, Toast.LENGTH_SHORT).show();

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArgumentsBackground", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    /**
     * Called by the system every time a client explicitly starts the service by calling
     * {@link android.content.Context#startService}, providing the arguments it supplied and a
     * unique integer token representing the start request. Note that the system calls this on your
     * service's main thread. First we log a message informing the user that we are "Starting #",
     * with the {@code startId} request number, and the contents of the extras included in the
     * {@code Intent intent} that started us. We obtain {@code Message msg} from our background
     * {@code ServiceHandler mServiceHandler}, set field {@code arg1} to {@code startId}, field
     * {@code arg2} to {@code flags} and field {@code obj} to the extras in {@code intent}. We then
     * push {@code msg} onto the end of the message queue for {@code mServiceHandler} after all
     * pending messages before the current time. It will be received in handleMessage(Message), in
     * the thread attached to this handler. Then we log as message "Sending: " with {@code msg}
     * appended to it.
     * <p>
     * Then we check to see if we were started using the "Start Failed Delivery" {@code Button}, and
     * if so we kill our process to simulate a failed delivery (but only if this is not a retry
     * call to {@code onStartCommand} as indicated by {@code flags} having the START_FLAG_RETRY bits
     * set.)
     * <p>
     * Finally if the extras contain a "redeliver" flag set to true we return START_REDELIVER_INTENT
     * (if this service's process is killed while it is started, it will be scheduled for a restart
     * and the last delivered Intent re-delivered to it again), otherwise we return START_NOT_STICKY
     * (if this service's process is killed while it is started and there are no new start intents
     * to deliver to it, then take the service out of the started state and don't recreate until a
     * future explicit call).
     *
     * @param intent  The Intent supplied to {@link android.content.Context#startService},
     *                as given.  This may be null if the service is being restarted after
     *                its process has gone away, and it had previously returned anything
     *                except {@link #START_STICKY_COMPATIBILITY}.
     * @param flags   Additional data about this start request.  Currently either
     *                0, {@link #START_FLAG_REDELIVERY}, or {@link #START_FLAG_RETRY}.
     * @param startId A unique integer representing this specific request to
     *                start.  Use with {@link #stopSelfResult(int)}.
     * @return The return value indicates what semantics the system should
     * use for the service's current started state.  It may be one of the
     * constants associated with the {@link #START_CONTINUATION_MASK} bits.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting #" + startId + ": " + intent.getExtras());
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.arg2 = flags;
        msg.obj = intent.getExtras();
        mServiceHandler.sendMessage(msg);
        Log.i(TAG, "Sending: " + msg);

        // For the start fail button, we will simulate the process dying
        // for some reason in onStartCommand().
        if (intent.getBooleanExtra("fail", false)) {
            // Don't do this if we are in a retry... the system will
            // eventually give up if we keep crashing.
            if ((flags & START_FLAG_RETRY) == 0) {
                // Since the process hasn't finished handling the command,
                // it will be restarted with the command again, regardless of
                // whether we return START_REDELIVER_INTENT.
                Process.killProcess(Process.myPid());
            }
        }

        // Normally we would consistently return one kind of result...
        // however, here we will select between these two, so you can see
        // how they impact the behavior.  Try killing the process while it
        // is in the middle of executing the different commands.
        return intent.getBooleanExtra("redeliver", false)
                ? START_REDELIVER_INTENT : START_NOT_STICKY;
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed. First
     * we tell our {@code Looper mServiceLooper} which is receiving messages to terminate without
     * processing any more messages in the message queue. Then we call our method {@code hideNotification}
     * which cancels our notification, and toast the message "Service destroyed."
     */
    @Override
    public void onDestroy() {
        mServiceLooper.quit();

        hideNotification();

        // Tell the user we stopped.
        Toast.makeText(ServiceStartArguments.this, R.string.service_destroyed, Toast.LENGTH_SHORT).show();
    }

    /**
     * Return the communication channel to the service.  May return null if
     * clients can not bind to the service, and that is what we do.
     *
     * @param intent The Intent that was used to bind to this service.
     * @return Return an IBinder through which clients can call on to the
     * service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Show a notification while this service is running. First we create {@code PendingIntent contentIntent}
     * which will launch {@code Controller}. We then construct {@code Notification.Builder noteBuilder},
     * setting its small icon to R.drawable.stat_sample, its ticker text to {@code text}, its timestamp
     * to the current system time, its label to "Sample Service Start Arguments", its text to {@code text},
     * and {@code contentIntent} as the {@code PendingIntent} to be sent when the notification is clicked.
     * We set the ongoing flag to true so that it cannot be dismissed by the user, then build and post
     * the notification with the ID R.string.service_created.
     *
     * @param text text to display in our notification
     */
    private void showNotification(String text) {
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, Controller.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification.Builder noteBuilder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.stat_sample)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.service_start_arguments_label))  // the label
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent);  // The intent to send when the entry is clicked

        // We show this for as long as our service is processing a command.
        noteBuilder.setOngoing(true);

        // Send the notification.
        // We use a string id because it is a unique number.  We use it later to cancel.
        mNM.notify(R.string.service_created, noteBuilder.build());
    }

    /**
     * Cancels the notification with ID R.string.service_created,
     */
    private void hideNotification() {
        mNM.cancel(R.string.service_created);
    }

    // ----------------------------------------------------------------------

    /**
     * Example of explicitly starting the {@link ServiceStartArguments}.
     * Note that this is implemented as an inner class only keep the sample
     * all together; typically this code would appear in some separate class.
     */
    public static class Controller extends Activity {
        /**
         * Called when the activity is starting. First we call through to our super's implementation
         * of {@code onCreate}, then we set our content view to our layout file
         * R.layout.service_start_arguments_controller.
         * <p>
         * We locate the {@code Button}'s in our layout and set their {@code OnClickListener}'s:
         * <ul>
         * <li>R.id.start1 "Start One no redeliver" {@code mStart1Listener}</li>
         * <li>R.id.start2 "Start Two no redeliver" {@code mStart2Listener}</li>
         * <li>R.id.start3 "Start Three w/redeliver" {@code mStart3Listener}</li>
         * <li>R.id.startfail "Start failed delivery" {@code mStartFailListener}</li>
         * <li>R.id.kill "Kill Process" {@code mKillListener}</li>
         * </ul>
         *
         * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
         */
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.service_start_arguments_controller);

            // Watch for button clicks.
            Button button = findViewById(R.id.start1);
            button.setOnClickListener(mStart1Listener);
            button = findViewById(R.id.start2);
            button.setOnClickListener(mStart2Listener);
            button = findViewById(R.id.start3);
            button.setOnClickListener(mStart3Listener);
            button = findViewById(R.id.startfail);
            button.setOnClickListener(mStartFailListener);
            button = findViewById(R.id.kill);
            button.setOnClickListener(mKillListener);
        }

        /**
         * {@code OnClickListener} for the R.id.start1 "Start One no redeliver" Button
         */
        private OnClickListener mStart1Listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(Controller.this, ServiceStartArguments.class)
                        .putExtra("name", "One"));
            }
        };

        /**
         * {@code OnClickListener} for the R.id.start2 "Start Two no redeliver" Button
         */
        private OnClickListener mStart2Listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(Controller.this, ServiceStartArguments.class)
                        .putExtra("name", "Two"));
            }
        };

        /**
         * {@code OnClickListener} for the R.id.start3 "Start Three w/redeliver" Button
         */
        private OnClickListener mStart3Listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(Controller.this, ServiceStartArguments.class)
                        .putExtra("name", "Three")
                        .putExtra("redeliver", true));
            }
        };

        /**
         * {@code OnClickListener} for the R.id.startfail "Start failed delivery" Button
         */
        private OnClickListener mStartFailListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(Controller.this, ServiceStartArguments.class)
                        .putExtra("name", "Failure")
                        .putExtra("fail", true));
            }
        };

        /**
         * {@code OnClickListener} for the R.id.kill "Kill Process" Button
         */
        private OnClickListener mKillListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                // This is to simulate the service being killed while it is
                // running in the background.
                Process.killProcess(Process.myPid());
            }
        };
    }
}


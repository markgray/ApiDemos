/*
 * Copyright (C) 2009 The Android Open Source Project
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
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import androidx.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.android.apis.R;
// Need the preceding import to get access to the app resources, since this
// class is in a sub-package.

/**
 * This is an example of implementing an application service that can
 * run in the "foreground".  It shows how to code this to work well by using
 * the improved Android 2.0 APIs when available and otherwise falling back
 * to the original APIs.  Yes: you can take this exact code, compile it
 * against the Android 2.0 SDK, and it will run against everything down to
 * Android 1.0.
 */
@SuppressWarnings("FieldCanBeLocal")
public class ForegroundService extends Service {
    /**
     * Action of the {@code Intent} that will launch us in the foreground.
     */
    static final String ACTION_FOREGROUND = "com.example.android.apis.FOREGROUND";
    /**
     * Action of the {@code Intent} that will launch us in the foreground with a WAKELOCK.
     */
    static final String ACTION_FOREGROUND_WAKELOCK = "com.example.android.apis.FOREGROUND_WAKELOCK";
    /**
     * Action of the {@code Intent} that will launch us in the background.
     */
    static final String ACTION_BACKGROUND = "com.example.android.apis.BACKGROUND";
    /**
     * Action of the {@code Intent} that will launch us in the background with a WAKELOCK.
     */
    static final String ACTION_BACKGROUND_WAKELOCK = "com.example.android.apis.BACKGROUND_WAKELOCK";

    /**
     * Handle to the system level service NOTIFICATION_SERVICE
     */
    private NotificationManager mNM;

    /**
     * The id of the primary notification channel
     */
    public static final String PRIMARY_CHANNEL = "default";

    /**
     * {@code WakeLock} we acquire when the action launching us is either FOREGROUND_WAKELOCK or
     * BACKGROUND_WAKELOCK.
     */
    private PowerManager.WakeLock mWakeLock;
    /**
     * {@code Handler} we use to run our {@code Runnable mPulser} every 5 seconds.
     */
    private Handler mHandler = new Handler();
    /**
     * LOGS the message "PULSE!" every 5 seconds while we are running whether foreground or background
     */
    private Runnable mPulser = new Runnable() {
        /**
         * LOGS the message "PULSE!" every 5 seconds
         */
        @Override
        public void run() {
            Log.i("ForegroundService", "PULSE!");
            mHandler.postDelayed(this, 5*1000);
        }
    };

    /**
     * Called by the system when the service is first created. First we initialize our field
     * {@code NotificationManager mNM} with a handle to a system-level service NOTIFICATION_SERVICE
     * ("notification"). Then we initialize {@code NotificationChannel chan1} with a new instance
     * using PRIMARY_CHANNEL ("default") as both the ID and the user visible name of the channel with
     * its importance set to IMPORTANCE_DEFAULT (shows everywhere, makes noise, but does not visually
     * intrude). We then set the notification light color of {@code chan1} to GREEN and its lock screen
     * visibility to VISIBILITY_PRIVATE (show this notification on all lockscreens, but conceal sensitive
     * or private information on secure lockscreens). Finally we use {@code mNM} to create the notification
     * channel {@code chan1}.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel chan1 = new NotificationChannel(PRIMARY_CHANNEL, PRIMARY_CHANNEL,
                NotificationManager.IMPORTANCE_DEFAULT);
        chan1.setLightColor(Color.GREEN);
        chan1.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        mNM.createNotificationChannel(chan1);
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed. First
     * we call our method {@code handleDestroy} to release any wakelocks we may have acquired and to
     * remove any {@code Runnable} that {@code Handler mHandler} may have queued up. Then we call the
     * {@code stopForeground} method with the flag STOP_FOREGROUND_REMOVE (the notification previously
     * provided to {@link #startForeground} will be removed) removing this service from foreground state,
     * allowing it to be killed.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onDestroy() {
        handleDestroy();
        // Make sure our notification is gone.
        stopForeground(Service.STOP_FOREGROUND_REMOVE);
    }

    /**
     * Called by the system every time a client explicitly starts the service by calling
     * {@link android.content.Context#startService}, providing the arguments it supplied and a
     * unique integer token representing the start request.
     * <p>
     * If the action of our parameter {@code Intent intent} is either ACTION_FOREGROUND or
     * ACTION_FOREGROUND_WAKELOCK we initialize {@code CharSequence text} with the string whose
     * resource id is R.string.foreground_service_started ("Service is in the foreground"). We
     * initialize {@code PendingIntent contentIntent} with an intent that will start our activity
     * {@code ForegroundService.Controller} with a request code of 0 and 0 flags. We then build
     * {@code Notification notification} with a {@code Notification.Builder} for notification channel
     * PRIMARY_CHANNEL ("default"), setting its small icon to R.drawable.stat_sample, its status text
     * which is sent to accessibility services to {@code text}, its time stamp to the current time,
     * its label (first line) to the the string with resource id R.string.alarm_service_label ("Sample
     * Alarm Service"), its contents (second line of text) to {@code text} and the {@link PendingIntent}
     * to be sent when the notification is clicked to {@code contentIntent}. Finally we call the method
     * {@code startForeground} to make this service run in the foreground, supplying {@code notification}
     * for the ongoing notification to be shown to the user while in this state, and R.string.foreground_service_started
     * as the identifier for this notification. If it is not one of the foreground actions but is either
     * ACTION_BACKGROUND or ACTION_BACKGROUND_WAKELOCK we call the {@code stopForeground} with the flag
     * STOP_FOREGROUND_DETACH (notification will remain shown, but be completely detached from the service
     * and so no longer changed except through direct calls to the notification manager) to remove this
     * service from foreground state, allowing it to be killed if more memory is needed.
     * <p>
     * Now if the action of {@code intent} is either ACTION_FOREGROUND_WAKELOCK or ACTION_BACKGROUND_WAKELOCK
     * we branch on whether our field {@code WakeLock mWakeLock} is null:
     * <ul>
     *     <li>
     *         null: We initialize {@code mWakeLock} by using a handle to the system level service with
     *         class {@code PowerManager.class} to create a new wake lock with the level PARTIAL_WAKE_LOCK
     *         (Ensures that the CPU is running; the screen and keyboard back-light will be allowed to go off)
     *         and the tag "wake-service" (for debugging purposes). We then call the {@code acquire} method
     *         of {@code mWakeLock} to acquire the wake lock with a timeout of 30 seconds.
     *     </li>
     *     <li>
     *         not null: We call our {@code releaseWakeLock} method to release {@code mWakeLock} and
     *         set it to null.
     *     </li>
     * </ul>
     * We then remove any pending posts of Runnable {@code mPulser} that are in the message queue of
     * {@code Handler mHandler} and then call the {@code run} method of {@code mPulser} to start it
     * running. Finally we return START_STICKY to the caller since we want this service to continue
     * running until it is explicitly stopped.
     *
     * @param intent  The Intent supplied to {@link android.content.Context#startService}, as given.
     *                This may be null if the service is being restarted after its process has gone
     *                away, and it had previously returned anything except {@link #START_STICKY_COMPATIBILITY}.
     * @param flags   Additional data about this start request.
     * @param startId A unique integer representing this specific request to start. Use with
     *                {@link #stopSelfResult(int)}.
     * @return The return value indicates what semantics the system should use for the service's
     * current started state. It may be one of the constants associated with the
     * {@link #START_CONTINUATION_MASK} bits. We return START_STICKY since we want this service to
     * continue running until it is explicitly stopped.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_FOREGROUND.equals(intent.getAction())
                || ACTION_FOREGROUND_WAKELOCK.equals(intent.getAction())) {
            // In this sample, we'll use the same text for the ticker and the expanded notification
            CharSequence text = getText(R.string.foreground_service_started);

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, Controller.class), 0);

            // Set the info for the views that show in the notification panel.
            Notification notification = new Notification.Builder(this, PRIMARY_CHANNEL)
                    .setSmallIcon(R.drawable.stat_sample)  // the status icon
                    .setTicker(text)  // the status text
                    .setWhen(System.currentTimeMillis())  // the time stamp
                    .setContentTitle(getText(R.string.alarm_service_label))  // the label
                    .setContentText(text)  // the contents of the entry
                    .setContentIntent(contentIntent)  // The intent to send when clicked
                    .build();

            startForeground(R.string.foreground_service_started, notification);

        } else if (ACTION_BACKGROUND.equals(intent.getAction())
                || ACTION_BACKGROUND_WAKELOCK.equals(intent.getAction())) {
            stopForeground(Service.STOP_FOREGROUND_DETACH);
        }

        if (ACTION_FOREGROUND_WAKELOCK.equals(intent.getAction())
                || ACTION_BACKGROUND_WAKELOCK.equals(intent.getAction())) {
            if (mWakeLock == null) {
                //noinspection ConstantConditions
                mWakeLock = getSystemService(PowerManager.class).newWakeLock(
                        PowerManager.PARTIAL_WAKE_LOCK, "wake-service");
                mWakeLock.acquire(30000);
            } else {
                releaseWakeLock();
            }
        }

        mHandler.removeCallbacks(mPulser);
        mPulser.run();

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    /**
     * Convenience function to release the wakelock {@code mWakeLock} if it is not null then set it to
     * null.
     */
    void releaseWakeLock() {
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    /**
     * Convenience function called from our {@code onDestroy} override to release our wakelock and to
     * remove any pending posts of Runnable {@code mPulser} that are in the message queue of
     * {@code Handler mHandler}
     */
    void handleDestroy() {
        releaseWakeLock();
        mHandler.removeCallbacks(mPulser);
    }

    /**
     * Return the communication channel to the service. We return null since clients can not bind to
     * this service.
     *
     * @param intent The Intent that was used to bind to this service
     * @return null since clients can not bind to this service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    // ----------------------------------------------------------------------

    /**
     * <p>Example of explicitly starting and stopping the {@link ForegroundService}.
     * 
     * <p>Note that this is implemented as an inner class only to keep the sample
     * all together; typically this code would appear in some separate class.
     */
    public static class Controller extends Activity {
        /**
         * Called when the activity is starting. First we call our super's implementation of {@code onCreate},
         * then we set our content view to our layout file R.layout.foreground_service_controller. We
         * then proceed to set {@code Button button} by finding the buttons in our layout file in order
         * to set their {@code OnClickListener} as follows:
         * <ul>
         *     <li>
         *         R.id.start_foreground: {@code mForegroundListener} (starts {@code ForegroundService}
         *         running in the foreground).
         *     </li>
         *     <li>
         *         R.id.start_foreground_wakelock: {@code mForegroundWakelockListener} (starts {@code ForegroundService}
         *         running in the foreground with a wakelock).
         *     </li>
         *     <li>
         *         R.id.start_background: {@code mBackgroundListener} (starts {@code ForegroundService}
         *         running in the background).
         *     </li>
         *     <li>
         *         R.id.start_background_wakelock: {@code mBackgroundWakelockListener} (starts {@code ForegroundService}
         *         running in the background with a wakelock).
         *     </li>
         *     <li>
         *         R.id.stop: {@code mStopListener} calls the {@code stopService} method to stop
         *         {@code ForegroundService}.
         *     </li>
         *     <li>
         *         R.id.start_foreground_2: {@code mForegroundListener2} starts {@code ForegroundService2}
         *         running in the foreground.
         *     </li>
         *     <li>
         *         R.id.stop_2: {@code mStopListener2} calls the {@code stopService} method to stop
         *         {@code ForegroundService2}.
         *     </li>
         *     <li>
         *         R.id.start_foreground_2_alarm: {@code mForegroundAlarmListener} uses an alarm to
         *         start {@code ForegroundService2} running in the foreground 15 seconds from now.
         *     </li>
         * </ul>
         *
         * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
         */
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.foreground_service_controller);

            // Watch for button clicks.
            Button button = findViewById(R.id.start_foreground);
            button.setOnClickListener(mForegroundListener);
            button = findViewById(R.id.start_foreground_wakelock);
            button.setOnClickListener(mForegroundWakelockListener);
            button = findViewById(R.id.start_background);
            button.setOnClickListener(mBackgroundListener);
            button = findViewById(R.id.start_background_wakelock);
            button.setOnClickListener(mBackgroundWakelockListener);
            button = findViewById(R.id.stop);
            button.setOnClickListener(mStopListener);
            button = findViewById(R.id.start_foreground_2);
            button.setOnClickListener(mForegroundListener2);
            button = findViewById(R.id.stop_2);
            button.setOnClickListener(mStopListener2);
            button = findViewById(R.id.start_foreground_2_alarm);
            button.setOnClickListener(mForegroundAlarmListener);
        }

        /**
         * {@code OnClickListener} for the button with id R.id.start_foreground
         */
        private OnClickListener mForegroundListener = new OnClickListener() {
            /**
             * Called when the button with id R.id.start_foreground is clicked. We initialize
             * {@code Intent intent} with a new instance whose action is ACTION_FOREGROUND, set the
             * class it is to launch to {@code ForegroundService} then call the {@code startService}
             * method with it to request that that application service be started.
             *
             * @param v {@code View} that was clicked.
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForegroundService.ACTION_FOREGROUND);
                intent.setClass(Controller.this, ForegroundService.class);
                startService(intent);
            }
        };

        /**
         * {@code OnClickListener} for the button with id R.id.start_foreground_wakelock
         */
        private OnClickListener mForegroundWakelockListener = new OnClickListener() {
            /**
             * Called when the button with id R.id.start_foreground_wakelock is clicked. We initialize
             * {@code Intent intent} with a new instance whose action is ACTION_FOREGROUND_WAKELOCK, set the
             * class it is to launch to {@code ForegroundService} then call the {@code startService}
             * method with it to request that that application service be started.
             *
             * @param v {@code View} that was clicked.
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForegroundService.ACTION_FOREGROUND_WAKELOCK);
                intent.setClass(Controller.this, ForegroundService.class);
                startService(intent);
            }
        };

        /**
         * {@code OnClickListener} for the button with id R.id.start_background
         */
        private OnClickListener mBackgroundListener = new OnClickListener() {
            /**
             * Called when the button with id R.id.start_background is clicked. We initialize
             * {@code Intent intent} with a new instance whose action is ACTION_BACKGROUND, set the
             * class it is to launch to {@code ForegroundService} then call the {@code startService}
             * method with it to request that that application service be started.
             *
             * @param v {@code View} that was clicked.
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForegroundService.ACTION_BACKGROUND);
                intent.setClass(Controller.this, ForegroundService.class);
                startService(intent);
            }
        };

        /**
         * {@code OnClickListener} for the button with id R.id.start_background_wakelock
         */
        private OnClickListener mBackgroundWakelockListener = new OnClickListener() {
            /**
             * Called when the button with id R.id.start_background_wakelock is clicked. We initialize
             * {@code Intent intent} with a new instance whose action is ACTION_BACKGROUND_WAKELOCK, set the
             * class it is to launch to {@code ForegroundService} then call the {@code startService}
             * method with it to request that that application service be started.
             *
             * @param v {@code View} that was clicked.
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForegroundService.ACTION_BACKGROUND_WAKELOCK);
                intent.setClass(Controller.this, ForegroundService.class);
                startService(intent);
            }
        };

        /**
         * {@code OnClickListener} for the button with id R.id.stop
         */
        private OnClickListener mStopListener = new OnClickListener() {
            /**
             * Called when the button with id R.id.stop is clicked. We the {@code stopService} method
             * to stop {@code ForegroundService}.
             *
             * @param v {@code View} that was clicked.
             */
            @Override
            public void onClick(View v) {
                stopService(new Intent(Controller.this, ForegroundService.class));
            }
        };

        /**
         * {@code OnClickListener} for the button with id R.id.start_foreground_2
         */
        private OnClickListener mForegroundListener2 = new OnClickListener() {
            /**
             * Called when the button with id R.id.start_foreground_2 is clicked. We initialize
             * {@code Intent intent} with a new instance whose action is ACTION_FOREGROUND, set the
             * class it is to launch to {@code ForegroundService2} then call the {@code startService}
             * method with it to request that that application service be started.
             *
             * @param v {@code View} that was clicked.
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForegroundService.ACTION_FOREGROUND);
                intent.setClass(Controller.this, ForegroundService2.class);
                startService(intent);
            }
        };

        /**
         * {@code OnClickListener} for the button with id R.id.start_foreground_2_alarm
         */
        private OnClickListener mForegroundAlarmListener = new OnClickListener() {
            /**
             * Called when the button with id R.id.start_foreground_2_alarm is clicked. First we initialize
             * {@code Context ctx} with the context of this {@code Controller} activity. We initialize
             * {@code Intent intent} with a new instance whose action is ACTION_FOREGROUND, and set its
             * class to that of {@code ForegroundService2}. We initialize {@code PendingIntent pi} with
             * a PendingIntent that will start the foreground service specified by {@code intent} (which
             * is {@code ForegroundService2} as you recall), the request code is 0 and no flags are used.
             * We initialize {@code AlarmManager am} with a handle to the system level service ALARM_SERVICE,
             * and use it to schedule an alarm to be delivered precisely 15 seconds in ELAPSED_REALTIME from
             * now (does not wake the device up; if it goes off while the device is asleep, it will not be
             * delivered until the next time the device wakes up) with {@code pi} as the operation that will
             * be performed at that time. Finally we log that we are starting that service in 15 seconds.
             *
             * @param v {@code View} that was clicked.
             */
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                final Context ctx = Controller.this;

                final Intent intent = new Intent(ForegroundService.ACTION_FOREGROUND);
                intent.setClass(ctx, ForegroundService2.class);

                PendingIntent pi = PendingIntent.getForegroundService(ctx, 0, intent, 0);
                AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
                //noinspection ConstantConditions
                am.setExact(AlarmManager.ELAPSED_REALTIME,
                        SystemClock.elapsedRealtime() + 15_000,
                        pi);
                Log.i("ForegroundService", "Starting service in 15 seconds");
            }
        };

        /**
         * {@code OnClickListener} for the button with id R.id.stop_2
         */
        private OnClickListener mStopListener2 = new OnClickListener() {
            /**
             * Called when the button with id R.id.stop_2 is clicked. We the {@code stopService} method
             * to stop {@code ForegroundService2}.
             *
             * @param v {@code View} that was clicked.
             */
            @Override
            public void onClick(View v) {
                stopService(new Intent(Controller.this, ForegroundService2.class));
            }
        };

    }
}

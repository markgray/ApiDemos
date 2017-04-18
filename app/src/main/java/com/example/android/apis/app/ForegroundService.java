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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R;

/**
 * This is an example of implementing an application service that can
 * run in the "foreground".  It shows how to code this to work well by using
 * the improved Android 2.0 APIs when available and otherwise falling back
 * to the original APIs.  Yes: you can take this exact code, compile it
 * against the Android 2.0 SDK, and it will against everything down to
 * Android 1.0.
 * <p>
 * Note: Since {@code setForeground} has been turned into a no-op we no longer support below KITKAT
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class ForegroundService extends Service {
    /**
     * Action used in {@code Intent} to start our service running in the foreground, it is used
     * when the "Start Service Foreground" {@code Button} R.id.start_foreground is clicked
     */
    static final String ACTION_FOREGROUND = "com.example.android.apis.FOREGROUND";
    /**
     * Action used in {@code Intent} to start our service running in the background, it is used
     * when the "Start Service Background" {@code Button} R.id.start_background is clicked
     */
    static final String ACTION_BACKGROUND = "com.example.android.apis.BACKGROUND";

    /**
     * List of parameters used to retrieve the {@code Method} "setForeground" using {@code getMethod},
     * and initializing {@code Method mSetForeground}. This is not actually used since we no longer
     * support lower than KITKAT
     */
    private static final Class<?>[] mSetForegroundSignature = new Class[]{
            boolean.class
    };
    /**
     * List of parameters used to retrieve the {@code Method} "startForeground" using {@code getMethod},
     * and initializing {@code Method mStartForeground}.
     */
    private static final Class<?>[] mStartForegroundSignature = new Class[]{
            int.class,
            Notification.class
    };
    /**
     * List of parameters used to retrieve the {@code Method} "stopForeground" using {@code getMethod},
     * and initializing {@code Method mStopForeground}.
     */
    private static final Class<?>[] mStopForegroundSignature = new Class[]{
            boolean.class
    };

    /**
     * A handle to the system wide Service NOTIFICATION_SERVICE.
     */
    private NotificationManager mNM;
    /**
     * {@code Method} used to invoke the method {@code void setForeground(boolean isForeground)} on
     * older APIs (Note: we do not use this)
     */
    private Method mSetForeground;
    /**
     * {@code Method} used to invoke the method {@code startForeground(int id, Notification notification)}
     */
    private Method mStartForeground;
    /**
     * {@code Method} used to invoke the method {@code stopForeground(boolean removeNotification)}
     */
    private Method mStopForeground;
    /**
     * Argument array used to invoke the method {@code void setForeground(boolean isForeground)}
     */
    private Object[] mSetForegroundArgs = new Object[1];
    /**
     * Argument array used to invoke the method {@code startForeground(int id, Notification notification)}
     */
    private Object[] mStartForegroundArgs = new Object[2];
    /**
     * Argument array used to invoke the method {@code stopForeground(boolean removeNotification)}
     */
    private Object[] mStopForegroundArgs = new Object[1];

    /**
     * Wraps a try block around a call to {@code method.invoke(this, args)}, logging warning messages
     * if it catches either InvocationTargetException or IllegalAccessException.
     *
     * @param method {@code Method} to invoke
     * @param args   argument array to pass to the method
     */
    void invokeMethod(Method method, Object[] args) {
        try {
            method.invoke(this, args);
        } catch (InvocationTargetException e) {
            // Should not happen.
            Log.w("ApiDemos", "Unable to invoke method", e);
        } catch (IllegalAccessException e) {
            // Should not happen.
            Log.w("ApiDemos", "Unable to invoke method IllegalAccess", e);
        }
    }

    /**
     * This is a wrapper around the new startForeground method, using the older APIs if it is not
     * available. Note: the older APIs are not available since we are targeting KITKAT and above, so
     * the new APIs are always used.
     * <p>
     * Make this service run in the foreground, supplying the ongoing
     * notification to be shown to the user while in this state.
     * By default services are background, meaning that if the system needs to
     * kill them to reclaim more memory (such as to display a large page in a
     * web browser), they can be killed without too much harm.  You can set this
     * flag if killing your service would be disruptive to the user, such as
     * if your service is performing background music playback, so the user
     * would notice if their music stopped playing.
     * <p>
     * First we check to see if {@code Method mStartForeground} is not null (new APIs - always used
     * now so check is unneeded) and if so we load {@code Object[] mStartForegroundArgs} with our
     * two parameters, call our method {@code invokeMethod} to run {@code mStartForeground} with these
     * parameters, and return. If {@code Method mStartForeground} was null we used to store true in
     * {@code Object[] mSetForegroundArgs}, invoke {@code Method mSetForeground} using that parameter,
     * and post the {@code Notification notification} using {@code id} as its ID.
     *
     * @param id           The identifier for this notification as per
     *                     {@link NotificationManager#notify(int, Notification)
     *                     NotificationManager.notify(int, Notification)}; must not be 0.
     * @param notification The Notification to be displayed.
     */
    void startForegroundCompat(int id, Notification notification) {
        // If we have the new startForeground API, then use it.
        if (mStartForeground != null) {
            //noinspection UnnecessaryBoxing
            mStartForegroundArgs[0] = Integer.valueOf(id);
            mStartForegroundArgs[1] = notification;
            invokeMethod(mStartForeground, mStartForegroundArgs);
            return;
        }

        // Fall back on the old API.
        mSetForegroundArgs[0] = Boolean.TRUE;
        invokeMethod(mSetForeground, mSetForegroundArgs);
        mNM.notify(id, notification);
    }

    /**
     * This is a wrapper around the new stopForeground method, using the older APIs if it is not
     * available. Note: since we target KITKAT and above, the new APIs are always used.
     * <p>
     * Remove this service from foreground state, allowing it to be killed if more memory is needed.
     * First we check to see if {@code Method mStopForeground} is not null (it is never null in our
     * case) and if so we load {@code Object[] mStopForegroundArgs} with true, and use it as the
     * parameters when we invoke {@code mStopForeground}. We then return to the caller. If it was
     * null (never) we used to cancel the notification with ID {@code id}, load the parameter array
     * {@code Object[] mSetForegroundArgs} with false, and invoke {@code Method mSetForeground}
     * with it as the parameter list.
     *
     * @param id ID of the notification posted by {@code startForegroundCompat}
     */
    void stopForegroundCompat(int id) {
        // If we have the new stopForeground API, then use it.
        if (mStopForeground != null) {
            mStopForegroundArgs[0] = Boolean.TRUE;
            invokeMethod(mStopForeground, mStopForegroundArgs);
            return;
        }

        // Fall back on the old API.  Note to cancel BEFORE changing the
        // foreground state, since we could be killed at that point.
        mNM.cancel(id);
        mSetForegroundArgs[0] = Boolean.FALSE;
        invokeMethod(mSetForeground, mSetForegroundArgs);
    }

    /**
     * Called by the system when the service is first created. First we fetch a handle to the system
     * level service NOTIFICATION_SERVICE to {@code NotificationManager mNM}. Then wrapped in a try
     * block we attempt to use reflection to initialize {@code Method mStartForeground} to the method
     * {@code startForeground} that uses the list of parameters specified by the array
     * {@code Class<?>[] mStartForegroundSignature} (int, Notification), {@code Method mStopForeground}
     * to the method {@code stopForeground} that uses the list of parameters specified by the array
     * {@code Class<?>[] mStopForegroundSignature} (boolean). If these reflections both succeed we
     * return.
     * <p>
     * If we catch NoSuchMethodException we set both {@code mStartForeground} and {@code mStopForeground}
     * to null and wrapped in another try block we attempt to use reflection to initialize
     * {@code Method mSetForeground} to the method {@code setForeground} that has the parameter list
     * specified by the array {@code Class<?>[] mSetForegroundSignature} (boolean). If we catch
     * NoSuchMethodException we throw an IllegalStateException complaining "OS doesn't have
     * Service.startForeground OR Service.setForeground!" Note: the first try block will always
     * succeed so this try block will never executed (assuming Android does not remove or change
     * the API for foreground services).
     */
    @Override
    public void onCreate() {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        try {
            mStartForeground = getClass().getMethod("startForeground", mStartForegroundSignature);
            mStopForeground = getClass().getMethod("stopForeground", mStopForegroundSignature);
            return;
        } catch (NoSuchMethodException e) {
            // Running on an older platform.
            mStartForeground = mStopForeground = null;
        }
        try {
            mSetForeground = getClass().getMethod("setForeground", mSetForegroundSignature);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("OS doesn't have Service.startForeground OR Service.setForeground!");
        }
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed. We
     * simply call our method {@code stopForegroundCompat} using the same ID for the notification that
     * we used for {@code startForegroundCompat}.
     */
    @Override
    public void onDestroy() {
        // Make sure our notification is gone.
        stopForegroundCompat(R.string.foreground_service_started);
    }

    /**
     * This is the old onStart method that will be called on the pre-2.0 platform. On 2.0 or later
     * we override onStartCommand() so this method will not be called. We simply call our method
     * {@code handleCommand} with the {@code Intent intent} parameter passed us. (The method
     * {@code handleCommand} is also used by our override of {@code onStartCommand})
     *
     * @param intent  The Intent supplied to {@link android.content.Context#startService},
     *                as given.  This may be null if the service is being restarted after
     *                its process has gone away, and it had previously returned anything
     *                except {@link #START_STICKY_COMPATIBILITY}.
     * @param startId A unique integer representing this specific request to start.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {
        handleCommand(intent);
    }

    /**
     * Called by the system every time a client explicitly starts the service by calling
     * {@link android.content.Context#startService}, providing the arguments it supplied and a
     * unique integer token representing the start request. We simply pass the {@code Intent intent}
     * parameter to our method {@code handleCommand} and return START_STICKY to the caller.
     * <p>
     * (START_STICKY means that if this service's process is killed while it is started (after
     * returning from onStartCommand(Intent, int, int)), then leave it in the started state but
     * don't retain this delivered intent. Later the system will try to re-create the service.
     * Because it is in the started state, it will guarantee to call onStartCommand(Intent, int, int)
     * after creating the new service instance; if there are not any pending start commands to be
     * delivered to the service, it will be called with a null intent object, so you must take care
     * to check for this. This mode makes sense for things that will be explicitly started and
     * stopped to run for arbitrary periods of time, such as a service performing background music
     * playback.
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
        handleCommand(intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    /**
     * Handles the command (Action) contained in the {@code Intent intent} that was used to start
     * us. If the action of the {@code Intent intent} is ACTION_FOREGROUND, we initialize
     * {@code CharSequence text} with the resource String R.string.foreground_service_started
     * ("Service is in the foreground"), create {@code PendingIntent contentIntent} which will start
     * our inner {@code Activity} class {@code Controller} (which started us in the first place!)
     * Then we build {@code Notification notification} using R.drawable.stat_sample as the small icon,
     * {@code text} as the "ticker" text, the current time as the timestamp, R.string.alarm_service_label
     * as the first line of text, {@code text} as the second line of text, and {@code contentIntent}
     * as the PendingIntent to be sent when the notification is clicked. We then call our method
     * {@code startForegroundCompat} with {@code notification} as the {@code Notification} to be
     * displayed while we run in the foreground, and R.string.foreground_service_started as the
     * ID for our {@code Notification}.
     * <p>
     * If the action of the {@code Intent intent} is ACTION_BACKGROUND, we call our method
     * {@code stopForegroundCompat} with R.string.foreground_service_started as the ID of the
     * {@code Notification} to be canceled.
     *
     * @param intent The Intent supplied to {@link android.content.Context#startService},
     *               as given.  This may be null if the service is being restarted after
     *               its process has gone away, and it had previously returned anything
     *               except {@link #START_STICKY_COMPATIBILITY}.
     */
    void handleCommand(Intent intent) {
        if (ACTION_FOREGROUND.equals(intent.getAction())) {
            // In this sample, we'll use the same text for the ticker and the expanded notification
            CharSequence text = getText(R.string.foreground_service_started);

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, Controller.class), 0);

            // Set the info for the views that show in the notification panel.
            Notification notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.stat_sample)  // the status icon
                    .setTicker(text)  // the status text
                    .setWhen(System.currentTimeMillis())  // the time stamp
                    .setContentTitle(getText(R.string.alarm_service_label))  // the label
                    .setContentText(text)  // the contents of the entry
                    .setContentIntent(contentIntent)  // The intent to send when clicked
                    .build();

            startForegroundCompat(R.string.foreground_service_started, notification);

        } else if (ACTION_BACKGROUND.equals(intent.getAction())) {
            stopForegroundCompat(R.string.foreground_service_started);
        }
    }

    /**
     * Return the communication channel to the service.  May return null if  clients can not bind to
     * the service, and we do return null.
     *
     * @param intent The Intent that was used to bind to this service,
     *               as given to {@link android.content.Context#bindService
     *               Context.bindService}.  Note that any extras that were included with
     *               the Intent at that point will <em>not</em> be seen here.
     * @return Return an IBinder through which clients can call on to the
     * service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // ----------------------------------------------------------------------

    /**
     * <p>Example of explicitly starting and stopping the {@link ForegroundService}.
     * <p>
     * <p>Note that this is implemented as an inner class only keep the sample
     * all together; typically this code would appear in some separate class.
     */
    public static class Controller extends Activity {
        /**
         * Called when the activity is starting. First we call through to our super's implementation
         * of {@code onCreate}, then we set our content view to our layout resource file
         * R.layout.foreground_service_controller. We locate the {@code Button} R.id.start_foreground
         * and set its {@code OnClickListener} to {@code OnClickListener mForegroundListener}, locate
         * the {@code Button} R.id.start_background and set its {@code OnClickListener} to
         * {@code OnClickListener mBackgroundListener}, and locate the {@code Button} R.id.stop
         * and set its {@code OnClickListener} to {@code OnClickListener mStopListener}.
         *
         * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
         */
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.foreground_service_controller);

            // Watch for button clicks.
            Button button = (Button) findViewById(R.id.start_foreground);
            button.setOnClickListener(mForegroundListener);
            button = (Button) findViewById(R.id.start_background);
            button.setOnClickListener(mBackgroundListener);
            button = (Button) findViewById(R.id.stop);
            button.setOnClickListener(mStopListener);
        }

        /**
         * {@code OnClickListener} for the {@code Button} R.id.start_foreground, it creates an
         * {@code Intent} with the action ForegroundService.ACTION_FOREGROUND, sets the component
         * to handle the intent to the class name of ForegroundService.class, and uses the Intent
         * to start the service.
         */
        private OnClickListener mForegroundListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForegroundService.ACTION_FOREGROUND);
                intent.setClass(Controller.this, ForegroundService.class);
                startService(intent);
            }
        };

        /**
         * {@code OnClickListener} for the {@code Button} R.id.start_background, it creates an
         * {@code Intent} with the action ForegroundService.ACTION_BACKGROUND, sets the component
         * to handle the intent to the class name of ForegroundService.class, and uses the Intent
         * to start the service.
         */
        private OnClickListener mBackgroundListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForegroundService.ACTION_BACKGROUND);
                intent.setClass(Controller.this, ForegroundService.class);
                startService(intent);
            }
        };

        /**
         * {@code OnClickListener} for the {@code Button} R.id.stop, it creates an {@code Intent}
         * for the ForegroundService.class and uses it to request that the application service be
         * stopped.
         */
        private OnClickListener mStopListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(Controller.this, ForegroundService.class));
            }
        };
    }
}

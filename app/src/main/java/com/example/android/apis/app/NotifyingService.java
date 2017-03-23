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

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.ConditionVariable;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import com.example.android.apis.R;

/**
 * Updates a notification every 5 seconds from a background thread for a minute. Note use of
 * a {@code ConditionVariable} to implement the condition variable locking paradigm, blocking
 * for 5*1000 milliseconds after every notification (very useful approach).
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class NotifyingService extends Service {

    /**
     * Use a layout id for a unique identifier
     */
    private static int MOOD_NOTIFICATIONS = R.layout.status_bar_notifications;

    /**
     * variable which controls the notification thread
     */
    private ConditionVariable mCondition;

    /**
     * Handle to the system level service NOTIFICATION_SERVICE
     */
    private NotificationManager mNM;

    /**
     * Called by the system when the service is first created. First we initialize our field
     * {@code NotificationManager mNM} with a handle to the system level service NOTIFICATION_SERVICE.
     * Next we create {@code Thread notifyingThread} to run our {@code Runnable mTask} using the name
     * "NotifyingService". We initialize our field {@code ConditionVariable mCondition} to an instance
     * of an initially closed {@code ConditionVariable}. Finally we start {@code notifyingThread}
     * running.
     */
    @Override
    public void onCreate() {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
        Thread notifyingThread = new Thread(null, mTask, "NotifyingService");
        mCondition = new ConditionVariable(false);
        notifyingThread.start();
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed. First
     * we call the {@code cancel} method of the {@code NotificationManager} to remove our notification
     * from the status bar, then we open our {@code ConditionVariable mCondition} which will cause
     * our background task's {@code Runnable mTask} to "break" out of its "for" loop and terminate
     * before its 4 iterations are completed.
     */
    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(MOOD_NOTIFICATIONS);
        // Stop the thread from generating further notifications
        mCondition.open();
    }

    /**
     * The task that is being run in the background thread {@code Thread notifyingThread}. It loops
     * through 3 different notifications 4 times, pausing 5 seconds between each notification. This
     * happens only so long as the {@code ConditionVariable mCondition} remains closed -- if it is
     * opened (as happens in the {@code onDestroy} callback) it breaks out of the loop and stops
     * immediately.
     */
    private Runnable mTask = new Runnable() {
        /**
         * Starts executing the active part of the class' code. We loop 4 times, calling our method
         * {@code showNotification} to show three different notifications, pausing after each call
         * to block on the {@code ConditionVariable mCondition} for 5 seconds, and if we return from
         * {@code block} before the 5 seconds are over ({@code mCondition} is opened causing a "true"
         * return), we break out of the for loop prematurely. Whether we execute all four iterations
         * or break out of the for loop we stop the service using the method {@code stopSelf}.
         */
        @Override
        public void run() {
            for (int i = 0; i < 4; ++i) {
                showNotification(R.drawable.stat_happy,
                        R.string.status_bar_notifications_happy_message);
                if (mCondition.block(5 * 1000)) break;
                showNotification(R.drawable.stat_neutral,
                        R.string.status_bar_notifications_ok_message);
                if (mCondition.block(5 * 1000)) break;
                showNotification(R.drawable.stat_sad,
                        R.string.status_bar_notifications_sad_message);
                if (mCondition.block(5 * 1000)) break;
            }
            // Done with our work...  stop the service!
            NotifyingService.this.stopSelf();
        }
    };

    /**
     * Return the communication channel to the service. We simply return our stub {@code IBinder mBinder}
     * to the caller.
     *
     * @param intent The Intent that was used to bind to this service.
     *
     * @return an IBinder through which clients can call on to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Builds and posts a notification using resource ID for the icon and the contents String. First
     * we fetch {@code CharSequence text} for the resource ID {@code textID}. Next we create a
     * {@code PendingIntent contentIntent} which will launch the Activity {@code NotifyingController}
     * if the {@code Notification} we post is selected by the user. We use a {@code Notification.Builder}
     * to build {@code Notification notification} using our parameter {@code moodId} as the small icon,
     * the current system time as the timestamp, the String "Mood ring" as the title, {@code text}
     * as the contents, and {@code contentIntent} as the {@code PendingIntent} to be sent when the
     * notification is clicked. Finally we post {@code notification} to be shown in the status bar.
     *
     * @param moodId Resource ID for small icon of the notification
     * @param textId Resource ID for String to use for second line of text in the platform
     *               notification template
     */
    private void showNotification(int moodId, int textId) {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(textId);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, NotifyingController.class), 0);

        // Set the icon and timestamp.
        // Note that in this example, we do not set the tickerText.  We update the icon enough that
        // it is distracting to show the ticker text every time it changes.  We strongly suggest
        // that you do this as well.  (Think of of the "New hardware found" or "Network connection
        // changed" messages that always pop up)
        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(moodId)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getText(R.string.status_bar_notifications_mood_title))
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNM.notify(MOOD_NOTIFICATIONS, notification);
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    // Does not appear to be used(?) so could just as well be null?
    // TODO: study aidl example in the Remote Service applications first
    private final IBinder mBinder = new Binder() {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags)
                throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };
}

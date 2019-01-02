/*
 * Copyright (C) 2017 The Android Open Source Project
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
import com.example.android.apis.R;

import android.app.Activity;
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
import android.os.IBinder;
import androidx.annotation.RequiresApi;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Example service that gets launched from a notification and runs in the background.
 * Its label is "App/Notification/Background Service"
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class NotificationBackgroundService extends Service {
    /**
     * Called by the system every time a client explicitly starts the service by calling
     * {@link android.content.Context#startService}, providing the arguments it supplied and a
     * unique integer token representing the start request. We fetch a handle to the NOTIFICATION_SERVICE
     * system level service and use it to cancel the notification with id {@code layout.notification_background_service}
     * (the one that launched us). We then call the {@code stopSelf} method with our parameter {@code startId}
     * to stop this service. Finally we return START_NOT_STICKY to take the service out of the started state and
     * prevent it from being recreated until a future explicit call to {@link Context#startService Context.startService(Intent)}.
     *
     * @param intent The Intent supplied to {@link android.content.Context#startService}, Unused
     * @param flags Additional data about this start request. Unused
     * @param startId A unique integer representing this specific request to start.
     * @return The return value indicates what semantics the system should use for the service's
     * current started state. It may be one of the constants associated with the
     * {@link #START_CONTINUATION_MASK} bits. We return START_NOT_STICKY (if this service's process
     * is killed while it is started (after returning from {@code onStartCommand}), and there are
     * no new start intents to deliver to it, then take the service out of the started state and
     * don't recreate until a future explicit call to {@link Context#startService Context.startService(Intent)}.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //noinspection ConstantConditions
        ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE))
                .cancel(R.layout.notification_background_service);
        stopSelf(startId);
        return START_NOT_STICKY;
    }

    /**
     * Return the communication channel to the service. We return null because clients cannot bind to us.
     *
     * @param intent The Intent that was used to bind to this service
     * @return Return an IBinder through which clients can call on to the service, we return null
     * because clients cannot bind to us
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Demo UI that allows the user to post the notification.
     */
    public static class Controller extends Activity {
        /**
         * Handle to the NOTIFICATION_SERVICE system level service.
         */
        private NotificationManager mNM;
        /**
         * The id of the primary notification channel
         */
        public static final String PRIMARY_CHANNEL = "default";

        /**
         * Called when the activity is starting. First we call our super's implementation of {@code onCreate},
         * then we set our content view to our layout file R.layout.notification_background_service.
         * We then initialize our field {@code NotificationManager mNM} with a handle to the NOTIFICATION_SERVICE
         * system level service. We initialize {@code NotificationChannel chan1} with a new instance whose id and
         * user visible name are both PRIMARY_CHANNEL ("default"), and whose importance is IMPORTANCE_DEFAULT
         * (shows everywhere, makes noise, but does not visually intrude). We set the notification light
         * color of {@code chan1} to GREEN, and set its lock screen visibility to VISIBILITY_PRIVATE
         * (shows this notification on all lockscreens, but conceal sensitive or private information on
         * secure lockscreens). We then have {@code mNM} create notification channel {@code chan1}.
         * Finally we initialize {@code Button button} by finding the view with id R.id.notify and set
         * its {@code OnClickListener} to our field {@code mNotify}.
         *
         * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
         */
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.notification_background_service);

            mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel chan1 = new NotificationChannel(PRIMARY_CHANNEL, PRIMARY_CHANNEL,
                    NotificationManager.IMPORTANCE_DEFAULT);
            chan1.setLightColor(Color.GREEN);
            chan1.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            mNM.createNotificationChannel(chan1);

            Button button = findViewById(R.id.notify);
            button.setOnClickListener(mNotify);
        }

        /**
         * Called by the {@code onClick} override of our field {@code OnClickListener mNotify}, we
         * build and post a notification using our parameter {@code CharSequence text} as the text.
         * We initialize {@code PendingIntent contentIntent} with an instance intended to launch the
         * service  {@code NotificationBackgroundService} with request code 0. We initialize
         * {@code Notification notification} by building a {@code Notification.Builder} for notification
         * channel PRIMARY_CHANNEL ("default"), set its small icon to R.drawable.stat_sample, its ticker
         * text to our parameter {@code String text}, its time stamp to now, its second line of text to
         * {@code text} and its {@link PendingIntent} to be sent when the notification is clicked to
         * {@code contentIntent}. Finally we use {@code NotificationManager mNM} to post {@code notification}
         * using the resource id R.string.notification_background_service as its id.
         *
         * @param text text to display in our notification.
         */
        @SuppressWarnings("SameParameterValue")
        private void showNotification(CharSequence text) {
            // The PendingIntent to launch our activity if the user selects this notification
            PendingIntent contentIntent = PendingIntent.getService(this, 0,
                    new Intent(this, NotificationBackgroundService.class), 0);

            // Set the info for the views that show in the notification panel.
            Notification notification = new Notification.Builder(this, PRIMARY_CHANNEL)
                    .setSmallIcon(R.drawable.stat_sample)  // the status icon
                    .setTicker(text)  // the status text
                    .setWhen(System.currentTimeMillis())  // the time stamp
                    .setContentTitle(getText(R.string.notification_background_label))  // the label of the entry
                    .setContentText(text)  // the contents of the entry
                    .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                    .build();

            // Send the notification.
            // We use a layout id because it is a unique number.  We use it later to cancel.
            mNM.notify(R.layout.notification_background_service, notification);
        }

        /**
         * {@code OnClickListener} for the button with id R.id.notify, our {@code onClick} override
         * calls our method {@code showNotification} with the text "Selecting this will cause a
         * background service to run."
         */
        private OnClickListener mNotify = new OnClickListener() {
            /**
             * Called when the button with id R.id.notify is clicked. We just call our method
             * {@code showNotification} with the text "Selecting this will cause a background
             * service to run."
             *
             * @param v the {@code View} that was clicked
             */
            @Override
            public void onClick(View v) {
                showNotification("Selecting this will cause a background service to run.");
            }
        };
    }
}


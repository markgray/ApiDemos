/*
 * Copyright (C) 2010 The Android Open Source Project
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
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

import com.example.android.apis.R;

import java.util.ArrayList;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

/**
 * This is an example of implementing an application service that uses the
 * {@link Messenger} class for communicating with clients.  This allows for
 * remote interaction with a service, without needing to define an AIDL
 * interface.
 * <p>
 * Notice the use of the {@link NotificationManager} when interesting things
 * happen in the service.  This is generally how background services should
 * interact with the user, rather than doing something more disruptive such as
 * calling startActivity().
 */
@TargetApi(Build.VERSION_CODES.O)
public class MessengerService extends Service {
    /**
     * For showing and hiding our notification.
     */
    NotificationManager mNM;
    /**
     * The id of the primary notification channel
     */
    public static final String PRIMARY_CHANNEL = "default";

    /**
     * Keeps track of all current registered clients.
     */
    ArrayList<Messenger> mClients = new ArrayList<>();
    /**
     * Holds last value set by a client.
     */
    int mValue = 0;

    /**
     * Command to the service to register a client, receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client where callbacks should be sent.
     */
    static final int MSG_REGISTER_CLIENT = 1;

    /**
     * Command to the service to unregister a client, ot stop receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client as previously given with MSG_REGISTER_CLIENT.
     */
    static final int MSG_UNREGISTER_CLIENT = 2;

    /**
     * Command to service to set a new value.  This can be sent to the
     * service to supply a new value, and will be sent by the service to
     * any registered clients with the new value.
     */
    static final int MSG_SET_VALUE = 3;

    /**
     * Handler of incoming messages from clients.
     */
    @SuppressWarnings("WeakerAccess")
    @SuppressLint("HandlerLeak")
    class IncomingHandler extends Handler {
        /**
         * Subclasses must implement this to receive messages. We switch based on the {@code what}
         * field of the {@code Message msg} we have received:
         * <ul>
         * <li>
         * MSG_REGISTER_CLIENT - we add the value contained in the {@code replyTo} field to the
         * list of clients we maintain in {@code ArrayList<Messenger> mClients}
         * </li>
         * <li>
         * MSG_UNREGISTER_CLIENT - we remove the value contained in the {@code replyTo} field from
         * the list of clients we maintain in {@code ArrayList<Messenger> mClients}
         * </li>
         * <li>
         * MSG_SET_VALUE - we store the value sent in the field {@code arg1} of {@code Message msg}
         * in our field {@code int mValue}, then looping backwards through all the clients
         * in {@code ArrayList<Messenger> mClients} we try to send a message with the {@code what}
         * field set to MSG_SET_VALUE, and {@code arg1} field set to {@code mValue}. If we
         * catch a RemoteException we remove that client from {@code mClients} (safe because
         * we are going through the list in backwards order).
         * </li>
         * <li>
         * default - we pass the {@code msg} on to our super's implementation of {@code handleMessage}.
         * </li>
         * </ul>
         *
         * @param msg {@code Message} received by the {@code Messenger} we are the {@code Handler} for
         */
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_SET_VALUE:
                    mValue = msg.arg1;
                    for (int i = mClients.size() - 1; i >= 0; i--) {
                        try {
                            mClients.get(i).send(Message.obtain(null, MSG_SET_VALUE, mValue, 0));
                        } catch (RemoteException e) {
                            // The client is dead.  Remove it from the list;
                            // we are going through the list from back to front
                            // so this is safe to do inside the loop.
                            mClients.remove(i);
                        }
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /**
     * Called by the system when the service is first created. We initialize our field
     * {@code NotificationManager mNM} with a handle to the system level service NOTIFICATION_SERVICE,
     * then we initialize {@code NotificationChannel chan1} with a new instance whose id and user
     * visible name are both PRIMARY_CHANNEL ("default"), and whose importance is IMPORTANCE_DEFAULT
     * (shows everywhere, makes noise, but does not visually intrude). We set the notification light
     * color of {@code chan1} to GREEN, and set its lock screen visibility to VISIBILITY_PRIVATE
     * (shows this notification on all lockscreens, but conceal sensitive or private information on
     * secure lockscreens). We then have {@code mNM} create notification channel {@code chan1}. Finally
     * we call our method {@code showNotification} to display the notification that we are running.
     */
    @Override
    public void onCreate() {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel chan1 = new NotificationChannel(PRIMARY_CHANNEL, PRIMARY_CHANNEL,
                NotificationManager.IMPORTANCE_DEFAULT);
        chan1.setLightColor(Color.GREEN);
        chan1.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        mNM.createNotificationChannel(chan1);

        // Display a notification about us starting.
        showNotification();
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed. We
     * cancel our notification, and toast the message "Remote service has stopped".
     */
    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(R.string.remote_service_started);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.remote_service_stopped, Toast.LENGTH_SHORT).show();
    }

    /**
     * Return the communication channel to the service. When a client binds to our service, we return
     * the {@code IBinder} interface to our {@code Messenger mMessenger} for sending messages to this
     * service.
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
        return mMessenger.getBinder();
    }

    /**
     * Show a notification while this service is running. First we initialize {@code CharSequence text}
     * with the resource String "Remote service has started". The we create {@code PendingIntent contentIntent}
     * with a target Intent for {@code Controller.class} (NOTE: This was a mistake caused by code pasting,
     * my version uses {@code MessengerServiceActivities.Binding} instead.)
     * <p>
     * We then build a PRIMARY_CHANNEL {@code Notification notification} using R.drawable.stat_sample
     * as the small icon, {@code text} as the "ticker" text, the current system time as the timestamp,
     * the resource String "Sample Local Service" as the first line of text, {@code text} as the second
     * line of text, and {@code contentIntent} as the {@code PendingIntent} to be sent when the notification
     * is clicked. Finally we use {@code NotificationManager mNM} to post the notification using
     * R.string.remote_service_started as the ID.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.remote_service_started);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MessengerServiceActivities.Binding.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this, PRIMARY_CHANNEL)
                .setSmallIcon(R.drawable.stat_sample)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.local_service_label))  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        // We use a string id because it is a unique number.  We use it later to cancel.
        mNM.notify(R.string.remote_service_started, notification);
    }
}


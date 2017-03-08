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

import java.util.Random;

import com.example.android.apis.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * UI for posting an example notification. Allows you to choose between a regular app notification,
 * and an interstitial notification and shows how to launch {@code IncomingMessageView} from either
 * type of notification. The interstitial choice launches {@code IncomingMessageInterstitial} which
 * on clicking the "Switch to App" button uses the same function as the non-interstitial notification
 * to launch {@code IncomingMessageView}.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class IncomingMessage extends Activity {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.incoming_message.
     * We locate the {@code Button} R.id.notify_app to initialize {@code Button button}, then set
     * its {@code OnClickListener} to an anonymous class which calls the method {@code showAppNotification()}
     * when the {@code Button} is clicked. Then we locate the {@code Button} R.id.notify_interstitial
     * and set its {@code OnClickListener} to an anonymous class which calls the method
     * {@code showInterstitialNotification()}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.incoming_message);

        Button button = (Button) findViewById(R.id.notify_app);
        button.setOnClickListener(new Button.OnClickListener() {
            /**
             * Called when the {@code Button} is clicked, we simply call the method
             * {@code showAppNotification()}.
             *
             * @param v {@code View} of the {@code Button} that was clicked
             */
            @Override
            public void onClick(View v) {
                showAppNotification();
            }
        });

        button = (Button) findViewById(R.id.notify_interstitial);
        button.setOnClickListener(new Button.OnClickListener() {
            /**
             * Called when the {@code Button} is clicked, we simply call the method
             * {@code showInterstitialNotification()}.
             *
             * @param v {@code View} of the {@code Button} that was clicked
             */
            @Override
            public void onClick(View v) {
                showInterstitialNotification();
            }
        });
    }


    /**
     * This method creates an array of Intent objects representing the activity stack for the
     * incoming message details state that the application should be in when launching it from
     * a notification. It is used to Supply a PendingIntent to be sent when the notification is
     * clicked, both in {@code IncomingMessage.showAppNotification} as well as in
     * {@code IncomingMessageInterstitial.switchToApp}.
     * <p>
     * First we create an array to hold 4 Intents {@code Intent[] intents}. The 0'th {@code Intent}
     * in {@code intents} is set to an {@code Intent} that can be used to re-launch the root activity
     * ({@code ApiDemos}) in its base state. The next {@code Intent} in {@code intents} is set to an
     * {@code Intent} to launch {@code ApiDemos} with extra data specifying that {@code ApiDemos}
     * should use "App" as the path when displaying its {@code ListView} of choices, and
     * {@code intents[2]} is set to an {@code Intent} to launch {@code ApiDemos} with extra data
     * specifying that {@code ApiDemos} should use "App/Notification" as the path when displaying its
     * {@code ListView} of choices.
     * <p>
     * The last {@code Intent} in {@code intents} is set to an {@code Intent} which will launch the
     * {@code Activity IncomingMessageView}, with our parameter {@code CharSequence from} stored as
     * an extra under the key KEY_FROM ("from"), and our parameter {@code CharSequence msg} stored
     * as an extra under the key KEY_MESSAGE ("message"). Finally we return {@code intents} to the
     * callers who use it as the argument to {@code startActivities(stack)}.
     * <p>
     * Note: {@code startActivities(stack)}, will Launch multiple new activities. This is generally
     * the same as calling startActivity(Intent) for the first Intent in the array, that activity
     * during its creation calling startActivity(Intent) for the second entry, etc. Note that unlike
     * that approach, generally none of the activities except the last in the array will be created
     * at this point, but rather will be created when the user first visits them (due to pressing
     * back from the activity on top).
     *
     * @param context "this" when called from either {@code IncomingMessage} or {@code IncomingMessageInterstitial},
     *                it is the {@code Context} of the {@code Activity} it is called from.
     * @param from    Who sent the message
     * @param msg     the message content
     * @return a properly configured back stack for launching the {@code Activity} {@code IncomingMessageView}
     */
    static Intent[] makeMessageIntentStack(Context context, CharSequence from, CharSequence msg) {
        // A typical convention for notifications is to launch the user deeply
        // into an application representing the data in the notification; to
        // accomplish this, we can build an array of intents to insert the back
        // stack stack history above the item being displayed.
        Intent[] intents = new Intent[4];

        // First: root activity of ApiDemos.
        // This is a convenient way to make the proper Intent to launch and
        // reset an application's task.
        intents[0] = Intent.makeRestartActivityTask(new ComponentName(context,
                com.example.android.apis.ApiDemos.class));

        // "App"
        intents[1] = new Intent(context, com.example.android.apis.ApiDemos.class);
        intents[1].putExtra("com.example.android.apis.Path", "App");
        // "App/Notification"
        intents[2] = new Intent(context, com.example.android.apis.ApiDemos.class);
        intents[2].putExtra("com.example.android.apis.Path", "App/Notification");

        // Now the activity to display to the user.  Also fill in the data it
        // should display.
        intents[3] = new Intent(context, IncomingMessageView.class);
        intents[3].putExtra(IncomingMessageView.KEY_FROM, from);
        intents[3].putExtra(IncomingMessageView.KEY_MESSAGE, msg);

        return intents;
    }


    /**
     * Shows a notification in the status bar, consisting of our icon and associated expanded entry.
     * We are called when the {@code Button} R.id.notify_app ("Show App Notification") is clicked.
     * First we fetch a handle to the system-level service NOTIFICATION_SERVICE and save it in our
     * variable {@code NotificationManager nm}. We create a fake message to receive consisting of
     * a {@code CharSequence from} and a random {@code CharSequence message}. Next we create a
     * {@code PendingIntent contentIntent} which will be launched when our notification is clicked.
     * It consists of a back stack of Intents created by our method {@code makeMessageIntentStack}
     * and the flag FLAG_CANCEL_CURRENT (Flag indicating that if the described PendingIntent already
     * exists, the current one should be canceled before generating a new one).
     * <p>
     * We initialize the String {@code String tickerText} by using our resource string
     * R.string.imcoming_message_ticker_text as the format for our {@code message}. We create a
     * {@code Notification.Builder notifBuilder} and chain together methods which:
     * <ul>
     * <li>
     * Set the small icon to R.drawable.stat_sample
     * </li>
     * <li>
     * Set the "ticker" text which is sent to accessibility services to {@code tickerText}
     * </li>
     * <li>
     * Add a timestamp pertaining to the notification to be the current time in milliseconds
     * </li>
     * <li>
     * Set the first line of text in the platform notification template to be {@code from}
     * </li>
     * <li>
     * Set the second line of text in the platform notification template to be {@code message}
     * </li>
     * <li>
     * Supply {@code contentIntent} as the PendingIntent to be sent when the notification is clicked
     * </li>
     * </ul>
     * We set which notification properties will be inherited from system defaults to be DEFAULT_ALL
     * (all the default values).
     * <p>
     * Finally we use {@code NotificationManager nm} to post the notification built from
     * {@code notifBuilder} in the status bar.
     */
    void showAppNotification() {
        // look up the notification manager service
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // The details of our fake message
        CharSequence from = "Joe";
        CharSequence message;
        switch ((new Random().nextInt()) % 3) {
            case 0:
                message = "r u hungry?  i am starved";
                break;
            case 1:
                message = "im nearby u";
                break;
            default:
                message = "kthx. meet u for dinner. cul8r";
                break;
        }

        // The PendingIntent to launch our activity if the user selects this
        // notification.  Note the use of FLAG_CANCEL_CURRENT so that, if there
        // is already an active matching pending intent, cancel it and replace
        // it with the new array of Intents.
        PendingIntent contentIntent = PendingIntent.getActivities(this, 0,
                makeMessageIntentStack(this, from, message),
                PendingIntent.FLAG_CANCEL_CURRENT);

        // The ticker text, this uses a formatted string so our message could be localized
        String tickerText = getString(R.string.imcoming_message_ticker_text, message);

        // Set the info for the views that show in the notification panel.
        Notification.Builder notifBuilder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.stat_sample)  // the status icon
                .setTicker(tickerText)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(from)  // the label of the entry
                .setContentText(message)  // the contents of the entry
                .setContentIntent(contentIntent);  // The intent to send when the entry is clicked

        // We'll have this notification do the default sound, vibration, and led.
        // Note that if you want any of these behaviors, you should always have
        // a preference for the user to turn them off.
        notifBuilder.setDefaults(Notification.DEFAULT_ALL);

        // Note that we use R.layout.incoming_message_panel as the ID for
        // the notification.  It could be any integer you want, but we use
        // the convention of using a resource id for a string related to
        // the notification.  It will always be a unique number within your
        // application.
        nm.notify(R.string.imcoming_message_ticker_text, notifBuilder.build());
    }


    /**
     * The notification is the icon and associated expanded entry in the
     * status bar.
     */
    void showInterstitialNotification() {
        // look up the notification manager service
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // The details of our fake message
        CharSequence from = "Dianne";
        CharSequence message;
        switch ((new Random().nextInt()) % 3) {
            case 0:
                message = "i am ready for some dinner";
                break;
            case 1:
                message = "how about thai down the block?";
                break;
            default:
                message = "meet u soon. dont b late!";
                break;
        }

        // The PendingIntent to launch our activity if the user selects this
        // notification.  Note the use of FLAG_CANCEL_CURRENT so that, if there
        // is already an active matching pending intent, cancel it and replace
        // it with the new Intent.
        Intent intent = new Intent(this, IncomingMessageInterstitial.class);
        intent.putExtra(IncomingMessageView.KEY_FROM, from);
        intent.putExtra(IncomingMessageView.KEY_MESSAGE, message);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // The ticker text, this uses a formatted string so our message could be localized
        String tickerText = getString(R.string.imcoming_message_ticker_text, message);

        // Set the info for the views that show in the notification panel.
        Notification.Builder notifBuilder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.stat_sample)  // the status icon
                .setTicker(tickerText)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(from)  // the label of the entry
                .setContentText(message)  // the contents of the entry
                .setContentIntent(contentIntent);  // The intent to send when the entry is clicked

        // We'll have this notification do the default sound, vibration, and led.
        // Note that if you want any of these behaviors, you should always have
        // a preference for the user to turn them off.
        notifBuilder.setDefaults(Notification.DEFAULT_ALL);

        // Note that we use R.layout.incoming_message_panel as the ID for
        // the notification.  It could be any integer you want, but we use
        // the convention of using a resource id for a string related to
        // the notification.  It will always be a unique number within your
        // application.
        nm.notify(R.string.imcoming_message_ticker_text, notifBuilder.build());
    }

}

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
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;

import com.example.android.apis.R;

/**
 * Demonstrates adding notifications to the status bar. Displays icon, and text when
 * buttons pressed. Marquee does not work lollipop.
 */
@TargetApi(Build.VERSION_CODES.O)
public class StatusBarNotifications extends Activity {

    /**
     * Our Handle to the {@code NotificationManager} system-level service
     */
    private NotificationManager mNotificationManager;
    /**
     * The id of the primary notification channel
     */
    public static final String PRIMARY_CHANNEL = "default";

    /**
     * Use our layout id for a unique identifier for our notifications
     */
    private static int MOOD_NOTIFICATIONS = R.layout.status_bar_notifications;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.status_bar_notifications.
     * We declare {@code Button button} for later use, and fetch a handle to the {@code NotificationManager}
     * system-level service to initialize our field {@code NotificationManager mNotificationManager}.
     * We initialize {@code NotificationChannel chan1} with a new instance whose id and user visible
     * name are both PRIMARY_CHANNEL ("default"), and whose importance is IMPORTANCE_DEFAULT (shows
     * everywhere, makes noise, but does not visually intrude). We set the notification light color
     * of {@code chan1} to GREEN, and set its lock screen visibility to VISIBILITY_PRIVATE (shows
     * this notification on all lockscreens, but conceal sensitive or private information on secure
     * lockscreens). We then have {@code mNotificationManager} create notification channel {@code chan1}.
     * <p>
     * Next we locate each of the {@code Button}'s in our layout and set their {@code OnClickListener}
     * to an anonymous class which will call one or another of our methods with parameters set to
     * accomplish their purpose:
     * <ul>
     * <li>
     * R.id.happy - calls {@code setMood} to use icon R.drawable.stat_happy, the text
     * R.string.status_bar_notifications_happy_message, and false so it will not display the marquee
     * ticker text.
     * </li>
     * <li>
     * R.id.neutral - calls {@code setMood} to use icon R.drawable.stat_neutral, the text
     * R.string.status_bar_notifications_ok_message, and false so it will not display the marquee
     * ticker text.
     * </li>
     * <li>
     * R.id.sad - calls {@code setMood} to use icon R.drawable.stat_sad, the text
     * R.string.status_bar_notifications_sad_message, and false so it will not display the marquee
     * </li>
     * <li>
     * R.id.happyMarquee - same as R.id.happy except it passes true to display the marquee ticker text
     * </li>
     * <li>
     * R.id.neutralMarquee - same as R.id.neutral except it passes true to display the marquee ticker text
     * </li>
     * <li>
     * R.id.sadMarquee - same as R.id.sad except it passes true to display the marquee ticker text
     * </li>
     * <li>
     * R.id.happyViews - calls {@code setMoodView} to display the icon R.drawable.stat_happy, and the
     * text R.string.status_bar_notifications_happy_message
     * </li>
     * <li>
     * R.id.neutralViews - calls {@code setMoodView} to display the icon R.drawable.stat_neutral
     * and the text R.string.status_bar_notifications_ok_message
     * </li>
     * <li>
     * R.id.sadViews - calls {@code setMoodView} to display the icon R.drawable.stat_sad and the
     * text R.string.status_bar_notifications_sad_message
     * </li>
     * <li>
     * R.id.defaultSound - calls our method {@code setDefault} which adds the default notification
     * sound to its notification
     * </li>
     * <li>
     * R.id.defaultVibrate - calls our method {@code setDefault} which adds vibrate to its notification
     * </li>
     * <li>
     * R.id.defaultAll - calls our method {@code setDefault} which adds both vibrate and the
     * default notification sound to its notification
     * </li>
     * <li>
     * R.id.clear - clears any notification we have posted to the status bar.
     * </li>
     * </ul>
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.status_bar_notifications);

        Button button;

        // Get the notification manager service.
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel chan1 = new NotificationChannel(PRIMARY_CHANNEL, PRIMARY_CHANNEL,
                NotificationManager.IMPORTANCE_DEFAULT);
        chan1.setLightColor(Color.GREEN);
        chan1.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        mNotificationManager.createNotificationChannel(chan1);

        button = findViewById(R.id.happy);
        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMood(R.drawable.stat_happy, R.string.status_bar_notifications_happy_message, false);
            }
        });

        button = findViewById(R.id.neutral);
        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMood(R.drawable.stat_neutral, R.string.status_bar_notifications_ok_message, false);
            }
        });

        button = findViewById(R.id.sad);
        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMood(R.drawable.stat_sad, R.string.status_bar_notifications_sad_message, false);
            }
        });

        button = findViewById(R.id.happyMarquee);
        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMood(R.drawable.stat_happy, R.string.status_bar_notifications_happy_message, true);
            }
        });

        button = findViewById(R.id.neutralMarquee);
        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMood(R.drawable.stat_neutral, R.string.status_bar_notifications_ok_message, true);
            }
        });

        button = findViewById(R.id.sadMarquee);
        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMood(R.drawable.stat_sad, R.string.status_bar_notifications_sad_message, true);
            }
        });

        button = findViewById(R.id.happyViews);
        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMoodView(R.drawable.stat_happy, R.string.status_bar_notifications_happy_message);
            }
        });

        button = findViewById(R.id.neutralViews);
        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMoodView(R.drawable.stat_neutral, R.string.status_bar_notifications_ok_message);
            }
        });

        button = findViewById(R.id.sadViews);
        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMoodView(R.drawable.stat_sad, R.string.status_bar_notifications_sad_message);
            }
        });

        button = findViewById(R.id.defaultSound);
        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDefault(Notification.DEFAULT_SOUND);
            }
        });

        button = findViewById(R.id.defaultVibrate);
        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDefault(Notification.DEFAULT_VIBRATE);
            }
        });

        button = findViewById(R.id.defaultAll);
        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDefault(Notification.DEFAULT_ALL);
            }
        });

        button = findViewById(R.id.clear);
        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNotificationManager.cancel(R.layout.status_bar_notifications);
            }
        });
    }

    /**
     * Create a {@code PendingIntent} to launch {@code NotificationDisplay}, instructing it to display
     * the icon with the resource ID {@code moodId} by including it as an extra under the key "moodimg"
     *
     * @param moodId resource ID of icon to send as an extra in the {@code PendingIntent} we create
     * @return {@code PendingIntent} to launch the activity {@code NotificationDisplay}
     */
    private PendingIntent makeMoodIntent(int moodId) {
        // The PendingIntent to launch our activity if the user selects this
        // notification.  Note the use of FLAG_UPDATE_CURRENT so that if there
        // is already an active matching pending intent, we will update its
        // extras (and other Intents in the array) to be the ones passed in here.
        //noinspection UnnecessaryLocalVariable
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, NotificationDisplay.class).putExtra("moodimg", moodId),
                PendingIntent.FLAG_UPDATE_CURRENT);
        return contentIntent;
    }

    /**
     * Creates a {@code PendingIntent} which contains {@code Intent}'s representing a back stack
     * history. First we create an array to hold 4 Intents {@code Intent[] intents}. The 0'th
     * {@code Intent} in {@code intents} is set to an {@code Intent} that can be used to re-launch
     * the root activity ({@code ApiDemos}) in its base state. The next {@code Intent} in
     * {@code intents} is set to an {@code Intent} to launch {@code ApiDemos} with extra data
     * specifying that {@code ApiDemos} should use "App" as the path when displaying its
     * {@code ListView} of choices, and {@code intents[2]} is set to an {@code Intent} to launch
     * {@code ApiDemos} with extra data specifying that {@code ApiDemos} should use "App/Notification"
     * as the path when displaying its {@code ListView} of choices.
     * <p>
     * The last {@code Intent} in {@code intents} is set to an {@code Intent} which will launch the
     * present activity {@code StatusBarNotifications}. We then use these {@code Intent}'s to create
     * a {@code PendingIntent contentIntent} which we return to the caller.
     *
     * @return a back stack of 4 {@code Intent}'s with the last one launching this activity
     */
    private PendingIntent makeDefaultIntent() {
        // A typical convention for notifications is to launch the user deeply
        // into an application representing the data in the notification; to
        // accomplish this, we can build an array of intents to insert the back
        // stack stack history above the item being displayed.
        Intent[] intents = new Intent[4];

        // First: root activity of ApiDemos.
        // This is a convenient way to make the proper Intent to launch and
        // reset an application's task.
        intents[0] = Intent.makeRestartActivityTask(new ComponentName(this,
                com.example.android.apis.ApiDemos.class));

        // "App"
        intents[1] = new Intent(this, com.example.android.apis.ApiDemos.class);
        intents[1].putExtra("com.example.android.apis.Path", "App");
        // "App/Notification"
        intents[2] = new Intent(this, com.example.android.apis.ApiDemos.class);
        intents[2].putExtra("com.example.android.apis.Path", "App/Notification");

        // Now the activity to display to the user.
        intents[3] = new Intent(this, StatusBarNotifications.class);

        // The PendingIntent to launch our activity if the user selects this
        // notification.  Note the use of FLAG_UPDATE_CURRENT so that if there
        // is already an active matching pending intent, we will update its
        // extras (and other Intents in the array) to be the ones passed in here.
        //noinspection UnnecessaryLocalVariable
        PendingIntent contentIntent = PendingIntent.getActivities(this, 0,
                intents, PendingIntent.FLAG_UPDATE_CURRENT);
        return contentIntent;
    }

    /**
     * Builds and posts a notification using resource ID's for the small icon and the second line of
     * text in the platform notification template. If the parameter {@code boolean showTicker} is true
     * it sets the "ticker" text which is sent to accessibility services to the same text it uses for
     * the notification's second line. First we retrieve {@code CharSequence text} from the resource
     * ID specified by our parameter {@code textId}, then we retrieve {@code CharSequence title} from
     * resource ID R.string.status_bar_notifications_mood_title ("Mood ring"). We construct and configure
     * a {@code NotificationChannel} PRIMARY_CHANNEL {@code Notification.Builder notifBuilder} to use
     * our parameter {@code moodId} as the resource ID for the small icon of a notification, the current
     * time as the timestamp, {@code title} for the first line "title" of the notification, {@code text},
     * {@code text} as the second line "details" of the notification, and the {@code PendingIntent}
     * returned by our method {@code makeMoodIntent} for the {@code Intent} to be fired when our
     * notification is clicked. If our parameter {@code showTicker} is true we set the "ticker" text
     * which is sent to accessibility services to use the resource ID {@code textId}. Finally we use
     * our field {@code NotificationManager mNotificationManager} to post the notification we
     * {@code build()} from {@code notifBuilder} using MOOD_NOTIFICATIONS as the ID.
     *
     * @param moodId     Resource ID for the small icon of the notification
     * @param textId     Resource ID for the text of the notification
     * @param showTicker true to set the "ticker" text which is sent to accessibility services.
     */
    private void setMood(int moodId, int textId, boolean showTicker) {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(textId);

        // In this sample, we'll use this text for the title of the notification
        CharSequence title = getText(R.string.status_bar_notifications_mood_title);

        // Set the info for the views that show in the notification panel.
        Notification.Builder notifBuilder = new Notification.Builder(this, PRIMARY_CHANNEL) // the context to use
                .setSmallIcon(moodId)  // the status icon
                .setWhen(System.currentTimeMillis())  // the timestamp for the notification
                .setContentTitle(title)  // the title for the notification
                .setContentText(text)  // the details to display in the notification
                .setContentIntent(makeMoodIntent(moodId));  // The intent to send clicked

        if (showTicker) {
            // include the ticker text
            notifBuilder.setTicker(getString(textId));
        }

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNotificationManager.notify(MOOD_NOTIFICATIONS, notifBuilder.build());
    }

    /**
     * Builds a {@code Notification} manually using a custom {@code RemoteViews} to display the icon
     * and text passed us. First we create an empty {@code Notification notif} for {@code NotificationChannel}
     * PRIMARY_CHANNEL, set its field {@code contentIntent} (the {@code PendingIntent} that should be
     * launched if our notification is clicked) to that returned by our method {@code makeMoodIntent(moodId)}.
     * We retrieve {@code CharSequence text} from the resource string with ID {@code textId}, and set
     * the field {@code notif.tickerText} to it. We set the field {@code notif.icon} to {@code moodId}.
     * We create {@code RemoteViews contentView} using our layout file R.layout.status_bar_balloon,
     * and set its text to {@code text}, and its icon to {@code moodId}. We then set the field
     * {@code notif.contentView} to {@code contentView}. Finally we use our handle to the system
     * level NotificationManager service {@code NotificationManager mNotificationManager} to post the
     * {@code Notification notif} using the id MOOD_NOTIFICATIONS.
     *
     * @param moodId resource ID for the icon to use in notification
     * @param textId resource ID for the text to use in notification
     */
    private void setMoodView(int moodId, int textId) {
        // Instead of the normal constructor, we're going to use the one with no args and fill
        // in all of the data ourselves.  The normal one uses the default layout for notifications.
        // You probably want that in most cases, but if you want to do something custom, you
        // can set the contentView field to your own RemoteViews object.
        Notification notif = new Notification.Builder(this, PRIMARY_CHANNEL).build();

        // This is who should be launched if the user selects our notification.
        notif.contentIntent = makeMoodIntent(moodId);

        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(textId);
        notif.tickerText = text;

        // the icon for the status bar
        //noinspection deprecation
        notif.icon = moodId;

        // our custom view
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.status_bar_balloon);
        contentView.setTextViewText(R.id.text, text);
        contentView.setImageViewResource(R.id.icon, moodId);
        notif.contentView = contentView;

        // we use a string id because is a unique number.  we use it later to cancel the
        // notification
        mNotificationManager.notify(MOOD_NOTIFICATIONS, notif);
    }

    /**
     * Builds and posts a notification with the addition of a call to {@code setDefaults} when
     * building it in order to dictate which notification properties will be inherited from system
     * defaults: DEFAULT_SOUND, DEFAULT_VIBRATE, or DEFAULT_ALL. First we use our method
     * {@code makeDefaultIntent} to create {@code PendingIntent contentIntent} (it is composed of
     * a new back stack for a relaunch of our {@code StatusBarNotifications} activity when our
     * notification is clicked). We fetch {@code CharSequence text} from our resource ID
     * R.string.status_bar_notifications_happy_message, and {@code CharSequence title} from
     * R.string.status_bar_notifications_mood_title. Then we use a {@code Notification.Builder} for
     * {@code NotificationChannel} PRIMARY_CHANNEL to build {@code Notification notification} setting
     * the small icon to R.drawable.stat_happy, the ticker text to {@code text}, the timestamp to the
     * current time, the first line of the notification to to {@code title}, the second line to
     * {@code text}, we use {@code contentIntent} as the PendingIntent to be sent when the notification
     * is clicked, set which notification properties will be inherited from system defaults to our
     * parameter {@code defaults} and build the notification. Finally we use our handle to the system
     * level NotificationManager service {@code NotificationManager mNotificationManager} to post the
     * {@code Notification notification} using the id MOOD_NOTIFICATIONS.
     *
     * @param defaults which notification properties will be inherited from system defaults
     */
    private void setDefault(int defaults) {

        // This is who should be launched if the user selects our notification.
        PendingIntent contentIntent = makeDefaultIntent();

        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.status_bar_notifications_happy_message);

        // In this sample, we'll use this text for the title of the notification
        CharSequence title = getText(R.string.status_bar_notifications_mood_title);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this, PRIMARY_CHANNEL) // the context to use
                .setSmallIcon(R.drawable.stat_happy)  // the status icon
                .setTicker(text)  // the text to display in the ticker
                .setWhen(System.currentTimeMillis())  // the timestamp for the notification
                .setContentTitle(title)  // the title for the notification
                .setContentText(text)  // the details to display in the notification
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .setDefaults(defaults)
                .build();

        mNotificationManager.notify(
                MOOD_NOTIFICATIONS, // we use a string id because it is a unique
                // number.  we use it later to cancel the notification
                notification);
    }
}

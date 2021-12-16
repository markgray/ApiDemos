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

package com.example.android.apis.app

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.RemoteViews

import androidx.appcompat.app.AppCompatActivity

import com.example.android.apis.R

/**
 * Demonstrates adding notifications to the status bar. Displays icon, and text when
 * buttons pressed. Marquee does not work lollipop.
 */
@TargetApi(Build.VERSION_CODES.O)
class StatusBarNotifications : AppCompatActivity() {

    /**
     * Our Handle to the [NotificationManager] system-level service
     */
    private var mNotificationManager: NotificationManager? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.status_bar_notifications.
     * We fetch a handle to the [NotificationManager] system-level service to initialize our field
     * [mNotificationManager], and initialize our [NotificationChannel] variable `val chan1` with a
     * new instance whose id and user visible name are both PRIMARY_CHANNEL ("default"), and whose
     * importance is IMPORTANCE_DEFAULT (shows everywhere, makes noise, but does not visually
     * intrude). We set the notification light color of `chan1` to GREEN, and set its lock screen
     * visibility to VISIBILITY_PRIVATE (shows this notification on all lockscreens, but conceals
     * sensitive or private information on secure lockscreens). We then have [mNotificationManager]
     * create notification channel `chan1`.
     *
     * Next we locate each of the [Button]'s in our layout and set their `OnClickListener`
     * to an a lambda which will call one or another of our methods with parameters set to
     * accomplish their purpose:
     *  * R.id.happy - calls [setMood] to use icon R.drawable.stat_happy, the text
     *  R.string.status_bar_notifications_happy_message, and false so it will not display the
     *  marquee ticker text.
     *  * R.id.neutral - calls [setMood] to use icon R.drawable.stat_neutral, the text
     *  R.string.status_bar_notifications_ok_message, and false so it will not display the
     *  marquee ticker text.
     *  * R.id.sad - calls [setMood] to use icon R.drawable.stat_sad, the text
     *  R.string.status_bar_notifications_sad_message, and false so it will not display the
     *  marquee ticker text.
     *  * R.id.happyMarquee - same as R.id.happy except it passes true to display the marquee
     *  ticker text
     *  * R.id.neutralMarquee - same as R.id.neutral except it passes true to display the marquee
     *  ticker text
     *  * R.id.sadMarquee - same as R.id.sad except it passes true to display the marquee
     *  ticker text
     *  * R.id.happyViews - calls [setMoodView] to display the icon R.drawable.stat_happy, and
     *  the text R.string.status_bar_notifications_happy_message
     *  * R.id.neutralViews - calls [setMoodView] to display the icon R.drawable.stat_neutral
     *  and the text R.string.status_bar_notifications_ok_message
     *  * R.id.sadViews - calls [setMoodView] to display the icon R.drawable.stat_sad and the
     *  text R.string.status_bar_notifications_sad_message
     *  * R.id.defaultSound - calls our method [setDefault] which adds the default notification
     *  sound to its notification
     *  * R.id.defaultVibrate - calls our method [setDefault] which adds vibrate to its notification
     *  * R.id.defaultAll - calls our method [setDefault] which adds both vibrate and the
     *  default notification sound to its notification
     *  * R.id.clear - clears any notification we have posted to the status bar.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.status_bar_notifications)

        // Get the notification manager service.
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val chan1 = NotificationChannel(PRIMARY_CHANNEL, PRIMARY_CHANNEL,
            NotificationManager.IMPORTANCE_DEFAULT)
        chan1.lightColor = Color.GREEN
        chan1.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        mNotificationManager!!.createNotificationChannel(chan1)

        var button: Button = findViewById(R.id.happy)
        button.setOnClickListener {
            setMood(R.drawable.stat_happy, R.string.status_bar_notifications_happy_message, false)
        }

        button = findViewById(R.id.neutral)
        button.setOnClickListener {
            setMood(R.drawable.stat_neutral, R.string.status_bar_notifications_ok_message, false)
        }

        button = findViewById(R.id.sad)
        button.setOnClickListener {
            setMood(R.drawable.stat_sad, R.string.status_bar_notifications_sad_message, false)
        }

        button = findViewById(R.id.happyMarquee)
        button.setOnClickListener {
            setMood(R.drawable.stat_happy, R.string.status_bar_notifications_happy_message, true)
        }

        button = findViewById(R.id.neutralMarquee)
        button.setOnClickListener {
            setMood(R.drawable.stat_neutral, R.string.status_bar_notifications_ok_message, true)
        }

        button = findViewById(R.id.sadMarquee)
        button.setOnClickListener {
            setMood(R.drawable.stat_sad, R.string.status_bar_notifications_sad_message, true)
        }

        button = findViewById(R.id.happyViews)
        button.setOnClickListener {
            setMoodView(R.drawable.stat_happy, R.string.status_bar_notifications_happy_message)
        }

        button = findViewById(R.id.neutralViews)
        button.setOnClickListener {
            setMoodView(R.drawable.stat_neutral, R.string.status_bar_notifications_ok_message)
        }

        button = findViewById(R.id.sadViews)
        button.setOnClickListener {
            setMoodView(R.drawable.stat_sad, R.string.status_bar_notifications_sad_message)
        }

        button = findViewById(R.id.defaultSound)
        button.setOnClickListener {
            setDefault(Notification.DEFAULT_SOUND)
        }

        button = findViewById(R.id.defaultVibrate)
        button.setOnClickListener {
            setDefault(Notification.DEFAULT_VIBRATE)
        }

        button = findViewById(R.id.defaultAll)
        button.setOnClickListener {
            setDefault(Notification.DEFAULT_ALL)
        }

        button = findViewById(R.id.clear)
        button.setOnClickListener {
            mNotificationManager!!.cancel(R.layout.status_bar_notifications)
        }
    }

    /**
     * Create a [PendingIntent] to launch [NotificationDisplay], instructing it to display
     * the icon with the resource ID [moodId] by including it as an extra under the key "moodimg"
     *
     * @param moodId resource ID of icon to send as an extra in the [PendingIntent] we create
     * @return [PendingIntent] to launch the activity [NotificationDisplay]
     */
    private fun makeMoodIntent(moodId: Int): PendingIntent {
        // The PendingIntent to launch our activity if the user selects this
        // notification.  Note the use of FLAG_UPDATE_CURRENT so that if there
        // is already an active matching pending intent, we will update its
        // extras (and other Intents in the array) to be the ones passed in here.

        return PendingIntent.getActivity(
            this,
            0,
            Intent(this, NotificationDisplay::class.java).putExtra("moodimg", moodId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Creates a [PendingIntent] which contains [Intent]'s representing a back stack
     * history. First we create an array to hold 4 [Intent]'s `val intents`. The 0'th
     * [Intent] in `intents` is set to an [Intent] that can be used to re-launch
     * the root activity (`ApiDemos`) in its base state. The next [Intent] in
     * `intents` is set to an [Intent] to launch `ApiDemos` with extra data
     * specifying that `ApiDemos` should use "App" as the path when displaying its
     * `ListView` of choices, and `intents[2]` is set to an [Intent] to launch
     * `ApiDemos` with extra data specifying that `ApiDemos` should use "App/Notification"
     * as the path when displaying its `ListView` of choices.
     *
     * The last [Intent] in `intents` is set to an [Intent] which will launch the
     * present activity [StatusBarNotifications]. We then use these [Intent]'s to create
     * a [PendingIntent] which we return to the caller.
     *
     * @return a back stack of 4 `Intent`'s with the last one launching this activity
     */
    private fun makeDefaultIntent(): PendingIntent {
        // A typical convention for notifications is to launch the user deeply
        // into an application representing the data in the notification; to
        // accomplish this, we can build an array of intents to insert the back
        // stack stack history above the item being displayed.
        val intents = arrayOfNulls<Intent>(4)

        // First: root activity of ApiDemos.
        // This is a convenient way to make the proper Intent to launch and
        // reset an application's task.
        intents[0] = Intent.makeRestartActivityTask(ComponentName(this,
            com.example.android.apis.ApiDemos::class.java))

        // "App"
        intents[1] = Intent(this, com.example.android.apis.ApiDemos::class.java)
        intents[1]!!.putExtra("com.example.android.apis.Path", "App")
        // "App/Notification"
        intents[2] = Intent(this, com.example.android.apis.ApiDemos::class.java)
        intents[2]!!.putExtra("com.example.android.apis.Path", "App/Notification")

        // Now the activity to display to the user.
        intents[3] = Intent(this, StatusBarNotifications::class.java)

        // The PendingIntent to launch our activity if the user selects this
        // notification.  Note the use of FLAG_UPDATE_CURRENT so that if there
        // is already an active matching pending intent, we will update its
        // extras (and other Intents in the array) to be the ones passed in here.

        return PendingIntent.getActivities(
            this,
            0,
            intents,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Builds and posts a notification using resource ID's for the small icon and the second line of
     * text in the platform notification template. If the [Boolean] parameter [showTicker] is true
     * it sets the "ticker" text which is sent to accessibility services to the same text it uses
     * for the notification's second line. First we retrieve the [CharSequence] with the resource
     * ID specified by our parameter [textId] to initialize our variable `val text`, then we
     * retrieve the [CharSequence] with resource ID R.string.status_bar_notifications_mood_title
     * ("Mood ring") to initialize our variable `val title`. We construct and configure
     * a [Notification.Builder] for [NotificationChannel] PRIMARY_CHANNEL to initialize our variable
     * `val notifBuilder` which uses our parameter [moodId] as the resource ID for the small icon
     * of a notification, the current time as the timestamp, `title` for the first line "title" of
     * the notification, `text` as the second line "details" of the notification, and the
     * [PendingIntent] returned by our method [makeMoodIntent] for the [Intent] to be fired when our
     * notification is clicked. If our parameter [showTicker] is true we set the "ticker" text
     * which is sent to accessibility services to use the resource ID [textId]. Finally we use
     * our [NotificationManager] field [mNotificationManager] to post the notification we `build()`
     * from `notifBuilder` using MOOD_NOTIFICATIONS as the ID.
     *
     * @param moodId     Resource ID for the small icon of the notification
     * @param textId     Resource ID for the text of the notification
     * @param showTicker true to set the "ticker" text which is sent to accessibility services.
     */
    private fun setMood(moodId: Int, textId: Int, showTicker: Boolean) {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        val text = getText(textId)

        // In this sample, we'll use this text for the title of the notification
        val title = getText(R.string.status_bar_notifications_mood_title)

        // Set the info for the views that show in the notification panel.
        val notifBuilder = Notification.Builder(this, PRIMARY_CHANNEL) // the context to use
            .setSmallIcon(moodId)  // the status icon
            .setWhen(System.currentTimeMillis())  // the timestamp for the notification
            .setContentTitle(title)  // the title for the notification
            .setContentText(text)  // the details to display in the notification
            .setContentIntent(makeMoodIntent(moodId))  // The intent to send clicked

        if (showTicker) {
            // include the ticker text
            notifBuilder.setTicker(getString(textId))
        }

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNotificationManager!!.notify(MOOD_NOTIFICATIONS, notifBuilder.build())
    }

    /**
     * Builds a [Notification] manually using a custom [RemoteViews] to display the icon and text
     * passed us. First we create an empty [Notification] for [NotificationChannel] PRIMARY_CHANNEL
     * to initialize our variable `val notif`, set its field `contentIntent` (the [PendingIntent]
     * that should be launched if our notification is clicked) to that returned by our method
     * [makeMoodIntent] for the [moodId] resource ID. We initialize our variable `val text` to the
     * [CharSequence] with the resource string ID [textId], and set the field `tickerText` of
     * `notif` to it. We set the field `icon` of `notif` to [moodId]. We create a [RemoteViews] to
     * initialize our variable `val contentView` using our layout file R.layout.status_bar_balloon,
     * and set its text to `text`, and its icon to [moodId]. We then set the field `contentView` of
     * `notif` to `contentView`. Finally we use our handle to the system level NotificationManager
     * service in our [NotificationManager] field [mNotificationManager] to post the [Notification]
     * `notif` using the id MOOD_NOTIFICATIONS.
     *
     * @param moodId resource ID for the icon to use in notification
     * @param textId resource ID for the text to use in notification
     */
    private fun setMoodView(moodId: Int, textId: Int) {
        // Instead of the normal constructor, we're going to use the one with no args and fill
        // in all of the data ourselves.  The normal one uses the default layout for notifications.
        // You probably want that in most cases, but if you want to do something custom, you
        // can set the contentView field to your own RemoteViews object.
        val notif = Notification.Builder(this, PRIMARY_CHANNEL)
            .setSmallIcon(moodId) // the icon for the status bar
            .build()

        // This is who should be launched if the user selects our notification.
        notif.contentIntent = makeMoodIntent(moodId)

        // In this sample, we'll use the same text for the ticker and the expanded notification
        val text = getText(textId)
        notif.tickerText = text

        // our custom view
        val contentView = RemoteViews(packageName, R.layout.status_bar_balloon)
        contentView.setTextViewText(R.id.text, text)
        contentView.setImageViewResource(R.id.icon, moodId)
        @Suppress("DEPRECATION")
        notif.contentView = contentView

        // we use a string id because is a unique number.  we use it later to cancel the
        // notification
        mNotificationManager!!.notify(MOOD_NOTIFICATIONS, notif)
    }

    /**
     * Builds and posts a notification with the addition of a call to `setDefaults` when
     * building it in order to dictate which notification properties will be inherited from system
     * defaults: DEFAULT_SOUND, DEFAULT_VIBRATE, or DEFAULT_ALL. First we use our method
     * [makeDefaultIntent] to create a [PendingIntent] to initialize our variable `val contentIntent`
     * (it is composed of a new back stack for a relaunch of our [StatusBarNotifications] activity
     * when our notification is clicked). We fetch the [CharSequence] from our resource ID
     * R.string.status_bar_notifications_happy_message to initialize our variable `val text`, and
     * the [CharSequence] from R.string.status_bar_notifications_mood_title to initialize our variable
     * `val title`. Then we use a [Notification.Builder] for [NotificationChannel] PRIMARY_CHANNEL
     * to build a [Notification] for our variable `val notification` setting the small icon to
     * R.drawable.stat_happy, the ticker text to `text`, the timestamp to the current time, the
     * first line of the notification to to `title`, the second line to `text`, we use `contentIntent`
     * as the [PendingIntent] to be sent when the notification is clicked, set which notification
     * properties will be inherited from system defaults to our parameter `defaults` and build the
     * notification. Finally we use our handle to the system level [NotificationManager] service in
     * our field [mNotificationManager] to post the `notification` using the id MOOD_NOTIFICATIONS.
     *
     * @param defaults which notification properties will be inherited from system defaults
     */
    private fun setDefault(defaults: Int) {

        // This is who should be launched if the user selects our notification.
        val contentIntent = makeDefaultIntent()

        // In this sample, we'll use the same text for the ticker and the expanded notification
        val text = getText(R.string.status_bar_notifications_happy_message)

        // In this sample, we'll use this text for the title of the notification
        val title = getText(R.string.status_bar_notifications_mood_title)

        // Set the info for the views that show in the notification panel.
        @Suppress("DEPRECATION")
        val notification = Notification.Builder(this, PRIMARY_CHANNEL) // the context to use
            .setSmallIcon(R.drawable.stat_happy)  // the status icon
            .setTicker(text)  // the text to display in the ticker
            .setWhen(System.currentTimeMillis())  // the timestamp for the notification
            .setContentTitle(title)  // the title for the notification
            .setContentText(text)  // the details to display in the notification
            .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
            .setDefaults(defaults)
            .build()

        mNotificationManager!!.notify(
            MOOD_NOTIFICATIONS, // we use a string id because it is a unique
            // number.  we use it later to cancel the notification
            notification)
    }

    /**
     * Our static constants.
     */
    companion object {
        /**
         * The id of the primary notification channel
         */
        const val PRIMARY_CHANNEL = "default"

        /**
         * Use our layout id for a unique identifier for our notifications
         */
        private const val MOOD_NOTIFICATIONS = R.layout.status_bar_notifications
    }
}

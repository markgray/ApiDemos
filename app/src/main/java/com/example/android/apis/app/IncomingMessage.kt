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

import androidx.appcompat.app.AppCompatActivity

import com.example.android.apis.R
import java.util.*

/**
 * UI for posting an example notification. Allows you to choose between a regular app notification,
 * and an interstitial notification and shows how to launch [IncomingMessageView] from either
 * type of notification. The interstitial choice launches [IncomingMessageInterstitial] which
 * on clicking the "Switch to App" button uses the same function as the non-interstitial notification
 * to launch [IncomingMessageView].
 */
@Suppress("MemberVisibilityCanBePrivate")
@TargetApi(Build.VERSION_CODES.O)
class IncomingMessage : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.incoming_message.
     * We locate the [Button] with ID R.id.notify_app to initialize our variable `val button`,
     * then set its `OnClickListener` to a lambda which calls our method [showAppNotification]
     * when the [Button] is clicked. Then we set `button` to the [Button] with the ID
     * R.id.notify_interstitial and set its `OnClickListener` to a lambda which calls the method
     * [showInterstitialNotification].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.incoming_message)

        var button = findViewById<Button>(R.id.notify_app)
        /**
         * Called when the `Button` is clicked, we simply call the method
         * `showAppNotification()`.
         *
         * Parameter: `View` of the `Button` that was clicked
         */
        button.setOnClickListener {
            showAppNotification()
        }

        button = findViewById(R.id.notify_interstitial)
        /**
         * Called when the `Button` is clicked, we simply call the method
         * `showInterstitialNotification()`.
         *
         * Parameter: `View` of the `Button` that was clicked
         */
        button.setOnClickListener {
            showInterstitialNotification()
        }
    }

    /**
     * Shows a notification in the status bar, consisting of our icon and associated expanded entry.
     * We are called when the [Button] with ID R.id.notify_app ("Show App Notification") is clicked.
     * First we fetch a handle to the system-level service NOTIFICATION_SERVICE and save it in our
     * [NotificationManager] variable `val nm`. We initialize our [NotificationChannel] variable
     * `val chan1` with a new instance whose id and user visible name are both PRIMARY_CHANNEL
     * ("default"), and whose importance is IMPORTANCE_DEFAULT (shows everywhere, makes noise, but
     * does not visually intrude). We set the notification light color of `chan1` to GREEN, and set
     * its lock screen visibility to VISIBILITY_PRIVATE (shows this notification on all lockscreens,
     * but conceals sensitive or private information on secure lockscreens). We then have `nm`
     * create notification channel `chan1`. We create a fake message to receive consisting of a
     * [String] `val from` of "Joe",  and a random [CharSequence] `val message`. Next we create a
     * [PendingIntent] to initialize our variable `val contentIntent` which will be launched when
     * our notification is clicked. It consists of a back stack of [Intent]'s created by our method
     * [makeMessageIntentStack] and the flag FLAG_CANCEL_CURRENT (Flag indicating that if the
     * described [PendingIntent] already exists, the current one should be canceled before generating
     * a new one). We initialize the [String] variable `val tickerText` by using our resource string
     * R.string.incoming_message_ticker_text as the format for our `message`. We create a
     * [Notification.Builder] `val notifBuilder` for the [NotificationChannel] PRIMARY_CHANNEL and
     * chain together methods which:
     *  * Set the small icon to R.drawable.stat_sample
     *  * Set the "ticker" text which is sent to accessibility services to `tickerText`
     *  * Add a timestamp pertaining to the notification to be the current time in milliseconds
     *  * Set the first line of text in the platform notification template to be `from`
     *  * Set the second line of text in the platform notification template to be `message`
     *  * Supply `contentIntent` as the [PendingIntent] to be sent when the notification is clicked
     *
     * We set which notification properties will be inherited from system defaults to be DEFAULT_ALL
     * (all the default values). Finally we use [NotificationManager] `nm` to post the notification
     * built from `notifBuilder` in the status bar.
     */
    internal fun showAppNotification() {
        // look up the notification manager service
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val chan1 = NotificationChannel(PRIMARY_CHANNEL, PRIMARY_CHANNEL,
                NotificationManager.IMPORTANCE_DEFAULT)
        chan1.lightColor = Color.GREEN
        chan1.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        nm.createNotificationChannel(chan1)

        // The details of our fake message
        val from = "Joe"
        val message: CharSequence = when (Random().nextInt() % 3) {
            0 -> "r u hungry?  i am starved"
            1 -> "im nearby u"
            else -> "kthx. meet u for dinner. cul8r"
        }

        // The PendingIntent to launch our activity if the user selects this
        // notification.  Note the use of FLAG_CANCEL_CURRENT so that, if there
        // is already an active matching pending intent, cancel it and replace
        // it with the new array of Intents.
        val contentIntent = PendingIntent.getActivities(this, 0,
                makeMessageIntentStack(this, from, message),
                PendingIntent.FLAG_CANCEL_CURRENT)

        // The ticker text, this uses a formatted string so our message could be localized
        val tickerText = getString(R.string.imcoming_message_ticker_text, message)

        // Set the info for the views that show in the notification panel.
        val notifBuilder = Notification.Builder(this, PRIMARY_CHANNEL)
                .setSmallIcon(R.drawable.stat_sample)  // the status icon
                .setTicker(tickerText)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(from)  // the label of the entry
                .setContentText(message)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked

        // We'll have this notification do the default sound, vibration, and led.
        // Note that if you want any of these behaviors, you should always have
        // a preference for the user to turn them off.
        @Suppress("DEPRECATION")
        notifBuilder.setDefaults(Notification.DEFAULT_ALL)

        // Note that we use R.layout.incoming_message_panel as the ID for
        // the notification.  It could be any integer you want, but we use
        // the convention of using a resource id for a string related to
        // the notification.  It will always be a unique number within your
        // application.
        nm.notify(R.string.imcoming_message_ticker_text, notifBuilder.build())
    }

    /**
     * Builds a notification which will launch the [IncomingMessageInterstitial] activity when
     * it is clicked on. First we fetch a handle to the system wide service NOTIFICATION_SERVICE to
     * initialize our [NotificationManager] variable `val nm`. We initialize [NotificationChannel]
     * variable `val chan1` with a new instance whose id and user visible name are both
     * PRIMARY_CHANNEL ("default"), and whose importance is IMPORTANCE_DEFAULT (shows everywhere,
     * makes noise, but does not visually intrude). We set the notification light color of `chan1`
     * to GREEN, and set its lock screen visibility to VISIBILITY_PRIVATE (shows this notification
     * on all lockscreens, but conceals sensitive or private information on secure lockscreens). We
     * then have `nm` create notification channel `chan1`. We create a fake message to receive
     * consisting of a [String] `val from` and a randomly chosen [CharSequence] `val message`. Next
     * we create an [Intent] to initialize our variable `val intent` which will launch the Activity
     * [IncomingMessageInterstitial] when our notification is clicked. We store our `from` as an
     * extra in the [Intent] using the key KEY_FROM, and `message` as an extra using the key
     * KEY_MESSAGE. We set the flags of the Intent for FLAG_ACTIVITY_NEW_TASK and
     * FLAG_ACTIVITY_CLEAR_TASK. Next we create a new instance of [PendingIntent] to initialize our
     * variable `val contentIntent` which will launch the `intent` we just created and configured,
     * we initialize the [String] variable `val tickerText` by using our resource string
     * R.string.incoming_message_ticker_text as the format for our `message`. We create a
     * [Notification.Builder] to initialize our variable `val notifBuilder` for [NotificationChannel]
     * PRIMARY_CHANNEL and chain together methods which:
     *  * Set the small icon to R.drawable.stat_sample
     *  * Set the "ticker" text which is sent to accessibility services to `tickerText`
     *  * Add a timestamp pertaining to the notification to be the current time in milliseconds
     *  * Set the first line of text in the platform notification template to be `from`
     *  * Set the second line of text in the platform notification template to be `message`
     *  * Supply `contentIntent` as the PendingIntent to be sent when the notification is clicked
     *
     * We set which notification properties will be inherited from system defaults to be DEFAULT_ALL
     * (all the default values). Finally we use [NotificationManager] `nm` to post the notification
     * built from `notifBuilder` in the status bar.
     */
    internal fun showInterstitialNotification() {
        // look up the notification manager service
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val chan1 = NotificationChannel(PRIMARY_CHANNEL, PRIMARY_CHANNEL,
                NotificationManager.IMPORTANCE_DEFAULT)
        chan1.lightColor = Color.GREEN
        chan1.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        nm.createNotificationChannel(chan1)

        // The details of our fake message
        val from = "Dianne"
        val message: CharSequence = when (Random().nextInt() % 3) {
            0 -> "i am ready for some dinner"
            1 -> "how about thai down the block?"
            else -> "meet u soon. dont b late!"
        }

        // The PendingIntent to launch our activity if the user selects this
        // notification.  Note the use of FLAG_CANCEL_CURRENT so that, if there
        // is already an active matching pending intent, cancel it and replace
        // it with the new Intent.
        val intent = Intent(this, IncomingMessageInterstitial::class.java)
        intent.putExtra(IncomingMessageView.KEY_FROM, from)
        intent.putExtra(IncomingMessageView.KEY_MESSAGE, message)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val contentIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT)

        // The ticker text, this uses a formatted string so our message could be localized
        val tickerText = getString(R.string.imcoming_message_ticker_text, message)

        // Set the info for the views that show in the notification panel.
        val notifBuilder = Notification.Builder(this, PRIMARY_CHANNEL)
                .setSmallIcon(R.drawable.stat_sample)  // the status icon
                .setTicker(tickerText)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(from)  // the label of the entry
                .setContentText(message)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked

        // We'll have this notification do the default sound, vibration, and led.
        // Note that if you want any of these behaviors, you should always have
        // a preference for the user to turn them off.
        @Suppress("DEPRECATION")
        notifBuilder.setDefaults(Notification.DEFAULT_ALL)

        // Note that we use R.layout.incoming_message_panel as the ID for
        // the notification.  It could be any integer you want, but we use
        // the convention of using a resource id for a string related to
        // the notification.  It will always be a unique number within your
        // application.
        nm.notify(R.string.imcoming_message_ticker_text, notifBuilder.build())
    }

    /**
     * Our static constant and static method.
     */
    companion object {
        /**
         * The id of the primary notification channel
         */
        const val PRIMARY_CHANNEL = "default"

        /**
         * This method creates an array of Intent objects representing the activity stack for the
         * incoming message details state that the application should be in when launching it from
         * a notification. It is used to Supply a PendingIntent to be sent when the notification is
         * clicked, both in [IncomingMessage.showAppNotification] as well as in
         * [IncomingMessageInterstitial.switchToApp].
         *
         * First we create an array to hold 4 [Intent]'s `val intents`. The 0'th [Intent]
         * in `intents` is set to an [Intent] that can be used to re-launch the root activity
         * (`ApiDemos`) in its base state. The next [Intent] in `intents` is set to an
         * [Intent] to launch `ApiDemos` with extra data specifying that `ApiDemos`
         * should use "App" as the path when displaying its `ListView` of choices, and
         * `intents[2]` is set to an [Intent] to launch `ApiDemos` with extra data
         * specifying that `ApiDemos` should use "App/Notification" as the path when displaying its
         * `ListView` of choices.
         *
         * The last [Intent] in `intents` is set to an [Intent] which will launch the `Activity`
         * [IncomingMessageView], with our [CharSequence] parameter [from] stored as an extra under
         * the key KEY_FROM ("from"), and our [CharSequence] parameter [msg] stored as an extra
         * under the key KEY_MESSAGE ("message"). Finally we return `intents` to the callers who use
         * it as the argument to [startActivities].
         *
         * Note: [startActivities], will Launch multiple new activities. This is generally the same
         * as calling `startActivity(Intent)` for the first [Intent] in the array, that activity
         * during its creation calling `startActivity(Intent)` for the second entry, etc. Note that
         * unlike that approach, generally none of the activities except the last in the array will
         * be created at this point, but rather will be created when the user first visits them (due
         * to pressing back from the activity on top).
         *
         * @param context "this" when called from either `IncomingMessage` or `IncomingMessageInterstitial`,
         * it is the `Context` of the `Activity` it is called from.
         * @param from    Who sent the message
         * @param msg     the message content
         * @return a properly configured back stack for launching the `Activity` `IncomingMessageView`
         */
        fun makeMessageIntentStack(
                context: Context,
                from: CharSequence,
                msg: CharSequence
        ): Array<Intent?> {
            // A typical convention for notifications is to launch the user deeply
            // into an application representing the data in the notification; to
            // accomplish this, we can build an array of intents to insert the back
            // stack stack history above the item being displayed.
            val intents = arrayOfNulls<Intent>(4)

            // First: root activity of ApiDemos.
            // This is a convenient way to make the proper Intent to launch and
            // reset an application's task.
            intents[0] = Intent.makeRestartActivityTask(ComponentName(context,
                    com.example.android.apis.ApiDemos::class.java))

            // "App"
            intents[1] = Intent(context, com.example.android.apis.ApiDemos::class.java)
            intents[1]!!.putExtra("com.example.android.apis.Path", "App")
            // "App/Notification"
            intents[2] = Intent(context, com.example.android.apis.ApiDemos::class.java)
            intents[2]!!.putExtra("com.example.android.apis.Path", "App/Notification")

            // Now the activity to display to the user.  Also fill in the data it
            // should display.
            intents[3] = Intent(context, IncomingMessageView::class.java)
            intents[3]!!.putExtra(IncomingMessageView.KEY_FROM, from)
            intents[3]!!.putExtra(IncomingMessageView.KEY_MESSAGE, msg)

            return intents
        }
    }
}

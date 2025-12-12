@file:Suppress("ReplaceNotNullAssertionWithElvisReturn")

package com.example.android.apis.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.android.apis.R
import com.example.android.apis.app.ForegroundService.Companion.PRIMARY_CHANNEL

@Suppress("MemberVisibilityCanBePrivate")
@RequiresApi(Build.VERSION_CODES.O)
open class ForegroundService : Service() {
    /**
     * Handle to the system level service NOTIFICATION_SERVICE
     */
    var mNM: NotificationManager? = null

    /**
     * `WakeLock` we acquire when the action launching us is either FOREGROUND_WAKELOCK or
     * BACKGROUND_WAKELOCK.
     */
    var mWakeLock: WakeLock? = null

    /**
     * `Handler` we use to run our `Runnable mPulser` every 5 seconds.
     */
    val mHandler: Handler = Handler(Looper.myLooper()!!)

    /**
     * LOGS the message "PULSE!" every 5 seconds while we are running whether foreground or background
     */
    val mPulser: Runnable = Runnable {
        Log.i("ForegroundService", "PULSE!")
        repulse()
    }

    /**
     * Schedules the [Runnable] field [mPulser] to run again 5 seconds from now.
     */
    fun repulse() {
        mHandler.postDelayed(mPulser, 5 * 1000)
    }

    /**
     * Called by the system when the service is first created. First we initialize our
     * [NotificationManager] field [mNM] with a handle to a system-level service NOTIFICATION_SERVICE
     * ("notification"). Then we initialize our [NotificationChannel] variable `val chan1` with a new
     * instance using [PRIMARY_CHANNEL] ("default") as both the ID and the user visible name of the
     * channel with its importance set to IMPORTANCE_DEFAULT (shows everywhere, makes noise, but does
     * not visually intrude). We then set the notification light color of `chan1` to GREEN and its
     * lock screen visibility to VISIBILITY_PRIVATE (show this notification on all lockscreens, but
     * conceal sensitive or private information on secure lockscreens). Finally we use [mNM] to
     * create the notification channel `chan1`.
     */
    override fun onCreate() {
        mNM = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val chan1 = NotificationChannel(
            PRIMARY_CHANNEL, PRIMARY_CHANNEL,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        chan1.lightColor = Color.GREEN
        chan1.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        mNM!!.createNotificationChannel(chan1)
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed. First
     * we call our method [handleDestroy] to release any wakelocks we may have acquired and to
     * remove any [Runnable] that our [Handler] field [mHandler] may have queued up. Then we call
     * the [stopForeground] method with the flag STOP_FOREGROUND_REMOVE (the notification previously
     * provided to [startForeground] will be removed) removing this service from foreground state,
     * allowing it to be killed.
     */
    override fun onDestroy() {
        handleDestroy()
        // Make sure our notification is gone.
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    /**
     * Called by the system every time a client explicitly starts the service by calling
     * [android.content.Context.startService], providing the arguments it supplied and a
     * unique integer token representing the start request.
     *
     * If the action of our [Intent] parameter [intent] is either ACTION_FOREGROUND or
     * ACTION_FOREGROUND_WAKELOCK we initialize our [CharSequence] variable `val text` with the
     * string whose resource id is R.string.foreground_service_started ("Service is in the
     * foreground"). We initialize our [PendingIntent] variable `val contentIntent` with an intent
     * that will start our activity [ForegroundServiceController] with a request code of 0 and 0
     * flags. We then build a [Notification] for `val notification` with a [Notification.Builder]
     * for notification channel PRIMARY_CHANNEL ("default"), setting its small icon to
     * R.drawable.stat_sample, its status text which is sent to accessibility services to `text`,
     * its time stamp to the current time, its label (first line) to the the string with resource
     * id R.string.alarm_service_label ("Sample Alarm Service"), its contents (second line of text)
     * to `text` and the [PendingIntent] to be sent when the notification is clicked to `contentIntent`.
     * Finally we call the method [startForeground] to make this service run in the foreground,
     * supplying `notification` for the ongoing notification to be shown to the user while in this
     * state, and R.string.foreground_service_started as the identifier for this notification.
     *
     * If it is not one of the foreground actions but is either ACTION_BACKGROUND or
     * ACTION_BACKGROUND_WAKELOCK we call the [stopForeground] with the flag STOP_FOREGROUND_DETACH
     * (notification will remain shown, but be completely detached from the service and so no longer
     * changed except through direct calls to the notification manager) to remove this service from
     * foreground state, allowing it to be killed if more memory is needed.
     *
     * Now if the action of `intent` is either ACTION_FOREGROUND_WAKELOCK or ACTION_BACKGROUND_WAKELOCK
     * we branch on whether our field `WakeLock mWakeLock` is null:
     *
     *  * *null*: We initialize [mWakeLock] by using a handle to the system level service with
     *  class `PowerManager.class` to create a new wake lock with the level PARTIAL_WAKE_LOCK
     *  (Ensures that the CPU is running; the screen and keyboard back-light will be allowed to go off)
     *  and the tag "myapp:wake-service" (for debugging purposes). We then call the `acquire` method
     *  of `mWakeLock` to acquire the wake lock with a timeout of 30 seconds.
     *  * not *null*: We call our [releaseWakeLock] method to release [mWakeLock] and
     *  set it to null.
     *
     * We then remove any pending posts of our [Runnable] field [mPulser] that are in the message
     * queue of our [Handler] field [mHandler] and then call the `run` method of [mPulser] to start
     * it running. Finally we return START_STICKY to the caller since we want this service to
     * continue running until it is explicitly stopped.
     *
     * @param intent  The [Intent] supplied to [android.content.Context.startService], as given.
     * This may be *null* if the service is being restarted after its process has gone
     * away, and it had previously returned anything except START_STICKY_COMPATIBILITY.
     * @param flags   Additional data about this start request.
     * @param startId A unique integer representing this specific request to start. Use with
     * [stopSelfResult].
     * @return The return value indicates what semantics the system should use for the service's
     * current started state. It may be one of the constants associated with the
     * START_CONTINUATION_MASK bits. We return START_STICKY since we want this service to
     * continue running until it is explicitly stopped.
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (ACTION_FOREGROUND == intent.action || ACTION_FOREGROUND_WAKELOCK == intent.action) {
            // In this sample, we'll use the same text for the ticker and the expanded notification
            val text = getText(R.string.foreground_service_started)
            val contentIntent = PendingIntent.getActivity(
                this,
                0,
                Intent(this, ForegroundServiceController::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
            // Set the info for the views that show in the notification panel.
            val notification = Notification.Builder(this, PRIMARY_CHANNEL)
                .setSmallIcon(R.drawable.stat_sample) // the status icon
                .setTicker(text) // the status text
                .setWhen(System.currentTimeMillis()) // the time stamp
                .setContentTitle(getText(R.string.alarm_service_label)) // the label
                .setContentText(text) // the contents of the entry
                .setContentIntent(contentIntent) // The intent to send when clicked
                .build()
            startForeground(R.string.foreground_service_started, notification)
        } else if (ACTION_BACKGROUND == intent.action ||
            ACTION_BACKGROUND_WAKELOCK == intent.action
        ) {
            stopForeground(STOP_FOREGROUND_DETACH)
        }
        if (ACTION_FOREGROUND_WAKELOCK == intent.action ||
            ACTION_BACKGROUND_WAKELOCK == intent.action
        ) {
            if (mWakeLock == null) {
                mWakeLock = getSystemService(PowerManager::class.java)!!.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK, "myapp:wake-service"
                )
                mWakeLock!!.acquire(30000)
            } else {
                releaseWakeLock()
            }
        }
        mHandler.removeCallbacks(mPulser)
        mPulser.run()
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY
    }

    /**
     * Convenience function to release the wakelock [mWakeLock] if it is not *null* then set it to
     * *null*.
     */
    fun releaseWakeLock() {
        if (mWakeLock != null) {
            mWakeLock!!.release()
            mWakeLock = null
        }
    }

    /**
     * Convenience function called from our [onDestroy] override to release our wakelock and to
     * remove any pending posts of [Runnable] field [mPulser] that are in the message queue of
     * [Handler] field [mHandler]
     */
    fun handleDestroy() {
        releaseWakeLock()
        mHandler.removeCallbacks(mPulser)
    }


    /**
     * Return the communication channel to the service.  May return *null* if
     * clients can not bind to the service.
     *
     * @param intent The [Intent] that was used to bind to this service,
     * as given to [ Context.bindService][android.content.Context.bindService].
     * Note that any extras that were included with
     * the Intent at that point will *not* be seen here.
     *
     * @return Return an [IBinder] through which clients can call on to the
     * service.
     */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        /**
         * Action of the [Intent] that will launch us in the foreground.
         */
        const val ACTION_FOREGROUND: String = "com.example.android.apis.FOREGROUND"

        /**
         * Action of the [Intent] that will launch us in the foreground with a WAKELOCK.
         */
        const val ACTION_FOREGROUND_WAKELOCK: String =
            "com.example.android.apis.FOREGROUND_WAKELOCK"

        /**
         * Action of the [Intent] that will launch us in the background.
         */
        const val ACTION_BACKGROUND: String = "com.example.android.apis.BACKGROUND"

        /**
         * Action of the [Intent] that will launch us in the background with a WAKELOCK.
         */
        const val ACTION_BACKGROUND_WAKELOCK: String =
            "com.example.android.apis.BACKGROUND_WAKELOCK"

        /**
         * The id of the primary notification channel
         */
        const val PRIMARY_CHANNEL: String = "default"
    }
}
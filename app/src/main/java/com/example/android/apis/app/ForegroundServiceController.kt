package com.example.android.apis.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 *
 * Example of explicitly starting and stopping the [ForegroundService].
 */
@RequiresApi(Build.VERSION_CODES.O)
class ForegroundServiceController : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we set our content view to our layout file R.layout.foreground_service_controller. We
     * then proceed to set our [Button] variable `var button` by finding the buttons in our layout
     * file in order to set their `OnClickListener` as follows:
     *
     *  * R.id.start_foreground: [mForegroundListener] (starts [ForegroundService] running in the
     *  foreground).
     *  * R.id.start_foreground_wakelock: [mForegroundWakelockListener] (starts [ForegroundService]
     *  running in the foreground with a wakelock).
     *  * R.id.start_background: [mBackgroundListener] (starts [ForegroundService] running in the
     *  background).
     *  * R.id.start_background_wakelock: [mBackgroundWakelockListener] (starts [ForegroundService]
     *  running in the background with a wakelock).
     *  * R.id.stop: [mStopListener] calls the [stopService] method to stop [ForegroundService].
     *  * R.id.start_foreground_2: [mForegroundListener2] starts [ForegroundService2] running in
     *  the foreground.
     *  * R.id.stop_2: [mStopListener2] calls the [stopService] method to stop [ForegroundService2]
     *  * R.id.start_foreground_2_alarm: [mForegroundAlarmListener] uses an alarm to start
     *  [mForegroundAlarmListener] running in the foreground 15 seconds from now.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.foreground_service_controller)
        // Watch for button clicks.
        var button = findViewById<Button>(R.id.start_foreground)
        button.setOnClickListener(mForegroundListener)
        button = findViewById(R.id.start_foreground_wakelock)
        button.setOnClickListener(mForegroundWakelockListener)
        button = findViewById(R.id.start_background)
        button.setOnClickListener(mBackgroundListener)
        button = findViewById(R.id.start_background_wakelock)
        button.setOnClickListener(mBackgroundWakelockListener)
        button = findViewById(R.id.stop)
        button.setOnClickListener(mStopListener)
        button = findViewById(R.id.start_foreground_2)
        button.setOnClickListener(mForegroundListener2)
        button = findViewById(R.id.stop_2)
        button.setOnClickListener(mStopListener2)
        button = findViewById(R.id.start_foreground_2_alarm)
        button.setOnClickListener(mForegroundAlarmListener)
    }

    /**
     * Called when the button with id R.id.start_foreground is clicked. We initialize our [Intent]
     * variable `val intent` with a new instance whose action is ACTION_FOREGROUND, set the
     * class it is to launch to [ForegroundService] then call the [startService]
     * method with it to request that that application service be started.
     *
     * Parameter: `View` that was clicked.
     */
    private val mForegroundListener: View.OnClickListener = View.OnClickListener {
        val intent = Intent(ForegroundService.ACTION_FOREGROUND)
        intent.setClass(this@ForegroundServiceController, ForegroundService::class.java)
        startService(intent)
    }

    /**
     * Called when the button with id R.id.start_foreground_wakelock is clicked. We initialize our
     * [Intent] variable `val intent` with a new instance whose action is ACTION_FOREGROUND_WAKELOCK,
     * set the class it is to launch to [ForegroundService] then call the [startService] method with
     * it to request that that application service be started.
     *
     * Parameter: `View` that was clicked.
     */
    private val mForegroundWakelockListener: View.OnClickListener = View.OnClickListener {
        val intent = Intent(ForegroundService.ACTION_FOREGROUND_WAKELOCK)
        intent.setClass(this@ForegroundServiceController, ForegroundService::class.java)
        startService(intent)
    }

    /**
     * Called when the button with id R.id.start_background is clicked. We initialize our [Intent]
     * variable `val intent` with a new instance whose action is ACTION_BACKGROUND, set the
     * class it is to launch to [ForegroundService] then call the [startService] method with it to
     * request that that application service be started.
     *
     * Parameter: `View` that was clicked.
     */
    private val mBackgroundListener: View.OnClickListener = View.OnClickListener {
        val intent = Intent(ForegroundService.ACTION_BACKGROUND)
        intent.setClass(this@ForegroundServiceController, ForegroundService::class.java)
        startService(intent)
    }

    /**
     * Called when the button with id R.id.start_background_wakelock is clicked. We initialize our
     * [Intent] variable `val intent` with a new instance whose action is ACTION_BACKGROUND_WAKELOCK,
     * set the class it is to launch to [ForegroundService] then call the [startService] method with
     * it to request that that application service be started.
     *
     * Parameter: `View` that was clicked.
     */
    private val mBackgroundWakelockListener: View.OnClickListener = View.OnClickListener {
        val intent = Intent(ForegroundService.ACTION_BACKGROUND_WAKELOCK)
        intent.setClass(this@ForegroundServiceController, ForegroundService::class.java)
        startService(intent)
    }

    /**
     * Called when the button with id R.id.stop is clicked. We the [stopService] method
     * to stop [ForegroundService].
     *
     * Parameter: `View` that was clicked.
     */
    private val mStopListener: View.OnClickListener = View.OnClickListener {
        stopService(Intent(this@ForegroundServiceController, ForegroundService::class.java))
    }

    /**
     * Called when the button with id R.id.start_foreground_2 is clicked. We initialize our [Intent]
     * variable `val intent` with a new instance whose action is ACTION_FOREGROUND, set the class it
     * is to launch to [ForegroundService2] then call the [startService] method with it to request
     * that that application service be started.
     *
     * Parameter: `View` that was clicked.
     */
    private val mForegroundListener2: View.OnClickListener = View.OnClickListener {
        val intent = Intent(ForegroundService.ACTION_FOREGROUND)
        intent.setClass(this@ForegroundServiceController, ForegroundService2::class.java)
        startService(intent)
    }

    /**
     * Called when the button with id R.id.start_foreground_2_alarm is clicked. First we initialize
     * our [Context] variable `val ctx` with the context of this [ForegroundServiceController]
     * activity. We initialize our [Intent] variable `val intent` with a new instance whose action
     * is ACTION_FOREGROUND, and set its class to that of [ForegroundService2]. We initialize
     * our [PendingIntent] variable `val pi` with a [PendingIntent] that will start the foreground
     * service specified by `intent` (which is [ForegroundService2] as you recall), the request code
     * is 0 and no flags are used. We initialize our [AlarmManager] variable `val am` with a handle
     * to the system level service ALARM_SERVICE, and use it to schedule an alarm to be delivered
     * precisely 15 seconds in ELAPSED_REALTIME from now (does not wake the device up; if it goes
     * off while the device is asleep, it will not be delivered until the next time the device wakes
     * up) with `pi` as the operation that will be performed at that time. Finally we log that we
     * are starting that service in 15 seconds.
     *
     * Parameter: `View` that was clicked.
     */
    private val mForegroundAlarmListener: View.OnClickListener = View.OnClickListener {
        val ctx: Context = this@ForegroundServiceController
        val intent = Intent(ForegroundService.ACTION_FOREGROUND)
        intent.setClass(ctx, ForegroundService2::class.java)
        var pi = PendingIntent.getForegroundService(
            ctx,
            0,
            intent,
            FLAG_MUTABLE
        )
        val am = ctx.getSystemService(ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            am.canScheduleExactAlarms()
        }
        am.setExact(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 15000,
            pi
        )
        Log.i("ForegroundService", "Starting service in 15 seconds")
    }

    /**
     * Called when the button with id R.id.stop_2 is clicked. We call the [stopService] method
     * to stop [ForegroundService2].
     *
     * Parameter: `View` that was clicked.
     */
    private val mStopListener2: View.OnClickListener = View.OnClickListener {
        stopService(Intent(this@ForegroundServiceController, ForegroundService2::class.java))
    }
}
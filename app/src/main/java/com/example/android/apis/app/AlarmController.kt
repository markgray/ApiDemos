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

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.android.apis.R
import java.util.*

/**
 * Example of scheduling one-shot and repeating alarms. See [OneShotAlarm] for the code run when the
 * one-shot alarm goes off, and [RepeatingAlarm] for the code run when the repeating alarm goes off.
 *
 * Demo
 * App/Service/Alarm Controller
 *
 * Source files:
 *  - src/com.example.android.apis/app/AlarmController.kt
 *  The activity that lets you schedule alarms
 *  - src/com.example.android.apis/app/OneShotAlarm.kt
 *  This is an intent receiver that executes when the
 *  one-shot alarm goes off
 *  - src/com.example.android.apis/app/RepeatingAlarm.kt
 *  This is an intent receiver that executes when the repeating alarm goes off
 *  - /res/any/layout/alarm_controller.xml
 *  Defines contents of the screen<
 */
@Suppress("MemberVisibilityCanBePrivate")
@RequiresApi(api = Build.VERSION_CODES.M)
class AlarmController : Activity() {
    /**
     * Latest Toast we have shown.
     */
    internal var mToast: Toast? = null

    /**
     * `OnClickListener` used for buttons R.id.one_shot ("One Shot Alarm") and R.id.one_shot_while_idle
     * ("One Shot While-Idle Alarm"), its `onClick` override switches on the id of the view that
     * triggered it to differentiate.
     */
    private val mOneShotListener = OnClickListener { v ->
        /**
         * Called when our view has been clicked. We initialize `Intent intent` with an instance
         * intended for the `BroadcastReceiver` with the class `OneShotAlarm.class`, then
         * initialize `PendingIntent sender` with an instance that will perform a broadcast of
         * `intent` using the request code of 0, and no flags. We initialize `Calendar calendar`
         * with a new instance, set its time to the current time in milliseconds, then add 30 seconds to
         * it. We initialize `AlarmManager am` with a handle to the ALARM_SERVICE system level service.
         * We then switch on the id of our parameter `View view`:
         *  - R.id.one_shot: ("One Shot Alarm" button) we call the `set` method of `am` to schedule
         *  an RTC_WAKEUP alarm (wall clock time in UTC which will wake up the device when it goes
         *  off) to fire at the time in milliseconds of `calendar` when it will broadcast `sender`.
         *  - default: ("One Shot While-Idle Alarm" button in our case) we call the
         *  `setExactAndAllowWhileIdle` method of `am` to schedule an RTC_WAKEUP alarm (wall clock
         *  time in UTC which will wake up the device when it goes off) to fire at the time in
         *  milliseconds of `calendar` when it will broadcast `sender`. This is like the call to
         *  `AlarmManager.set` but is also allowed even when the system is in low-power idle modes
         *
         * If our field `Toast mToast` is not null we cancel it. We then set `mToast` to
         * an instance that will display the string with id R.string.one_shot_scheduled ("One-shot alarm
         * will go off in 30 seconds...") and show it to the user.
         *
         * Parameter: The view that was clicked.
         */
        // When the alarm goes off, we want to broadcast an Intent to our
        // BroadcastReceiver.  Here we make an Intent with an explicit class
        // name to have our own receiver (which has been published in
        // AndroidManifest.xml) instantiated and called, and then create an
        // IntentSender to have the intent executed as a broadcast.
        val intent = Intent(this@AlarmController, OneShotAlarm::class.java)
        val sender = PendingIntent.getBroadcast(this@AlarmController,
                0, intent, 0)

        // We want the alarm to go off 30 seconds from now.
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.SECOND, 30)

        // Schedule the alarm!
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager


        when (v.id) {
            R.id.one_shot ->
                am.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, sender)
            else ->
                am.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, calendar.timeInMillis, sender)
        }

        // Tell the user about what we did.
        if (mToast != null) {
            mToast!!.cancel()
        }
        mToast = Toast.makeText(this@AlarmController, R.string.one_shot_scheduled, Toast.LENGTH_LONG)
        mToast!!.show()
    }

    /**
     * Called when the button with id R.id.start_repeating ("Start Repeating Alarm") is clicked.
     * We initialize `Intent intent` with an instance intended for the `BroadcastReceiver`
     * with the class `RepeatingAlarm.class`, then initialize `PendingIntent sender`
     * with an instance that will perform a broadcast of `intent` using the request code of 0,
     * and no flags. We initialize `long firstTime` with the milliseconds since boot then add
     * 15,000 to it. We initialize `AlarmManager am` with a handle to the ALARM_SERVICE system
     * level service, then call its `setRepeating` method to schedule a repeating alarm of type
     * ELAPSED_REALTIME_WAKEUP (time since boot, including sleep, which will wake up the device when
     * it goes off) which will first go off at `firstTime` then again at 15 second intervals
     * broadcasting `sender` to its `BroadcastReceiver`. If our field `Toast mToast`
     * is not null we cancel it. We then set `mToast` to an instance that will display the string
     * with id R.string.repeating_scheduled ("Repeating alarm will go off in 15 seconds..") and show
     * it to the user.
     *
     * Parameter: `View` that was clicked.
     */
    @SuppressLint("ShortAlarm")
    private val mStartRepeatingListener = OnClickListener {
        // When the alarm goes off, we want to broadcast an Intent to our
        // BroadcastReceiver.  Here we make an Intent with an explicit class
        // name to have our own receiver (which has been published in
        // AndroidManifest.xml) instantiated and called, and then create an
        // IntentSender to have the intent executed as a broadcast.
        // Note that unlike above, this IntentSender is configured to
        // allow itself to be sent multiple times.
        val intent = Intent(this@AlarmController, RepeatingAlarm::class.java)
        val sender = PendingIntent.getBroadcast(this@AlarmController,
                0, intent, 0)

        // We want the alarm to go off 30 seconds from now.
        var firstTime = SystemClock.elapsedRealtime()
        firstTime += (15 * 1000).toLong()

        // Schedule the alarm!
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                firstTime, (15 * 1000).toLong(), sender)

        // Tell the user about what we did.
        if (mToast != null) {
            mToast!!.cancel()
        }
        mToast = Toast.makeText(this@AlarmController, R.string.repeating_scheduled, Toast.LENGTH_LONG)
        mToast!!.show()
    }

    /**
     * Called when the button with id R.id.stop_repeating ("Stop Repeating Alarm") is clicked.
     * We initialize `Intent intent` with an instance intended for the `BroadcastReceiver`
     * with the class `RepeatingAlarm.class`, then initialize `PendingIntent sender`
     * with an instance that will perform a broadcast of `intent` using the request code of 0,
     * and no flags. We initialize `AlarmManager am` with a handle to the ALARM_SERVICE system
     * level service, then call its `cancel` method to remove any alarms with an [Intent]
     * matching `sender` (the one we scheduled with the R.id.start_repeating ("Start Repeating Alarm")
     * button in our case). If our field `Toast mToast` is not null we cancel it. We then set
     * `mToast` to an instance that will display the string with id R.string.repeating_unscheduled
     * ("Repeating alarm has been unscheduled") and show it to the user.
     *
     * Parameter: `View` that was clicked.
     */
    private val mStopRepeatingListener = OnClickListener {
        // Create the same intent, and thus a matching IntentSender, for
        // the one that was scheduled.
        val intent = Intent(this@AlarmController, RepeatingAlarm::class.java)
        val sender = PendingIntent.getBroadcast(this@AlarmController,
                0, intent, 0)

        // And cancel the alarm.
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        am.cancel(sender)

        // Tell the user about what we did.
        if (mToast != null) {
            mToast!!.cancel()
        }
        mToast = Toast.makeText(this@AlarmController, R.string.repeating_unscheduled, Toast.LENGTH_LONG)
        mToast!!.show()
    }

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we set our content view to our layout file R.layout.alarm_controller. Next we find the three
     * buttons in our layout by their id and set their `OnClickListener` as follows:
     *  - R.id.one_shot ("One Shot Alarm") set to `OnClickListener mOneShotListener`, its `onClick`
     *  override will call `AlarmManager.set` for this button id.
     *  - R.id.one_shot_while_idle ("One Shot While-Idle Alarm") set to `OnClickListener mOneShotListener`,
     *  its `onClick` override will call `AlarmManager.setExactAndAllowWhileIdle` for this button id.
     *  - R.id.start_repeating ("Start Repeating Alarm") set to `OnClickListener mStartRepeatingListener`,
     *  its `onClick` override will call `AlarmManager.setRepeating`
     *  - R.id.stop_repeating ("Stop Repeating Alarm") set to `OnClickListener mStopRepeatingListener`,
     *  its `onClick` override will call `AlarmManager.cancel` for the `PendingIntent`
     *  started by the "Start Repeating Alarm" button.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.alarm_controller)

        // Watch for button clicks.
        var button = findViewById<Button>(R.id.one_shot)
        button.setOnClickListener(mOneShotListener)
        button = findViewById(R.id.one_shot_while_idle)
        button.setOnClickListener(mOneShotListener)
        button = findViewById(R.id.start_repeating)
        button.setOnClickListener(mStartRepeatingListener)
        button = findViewById(R.id.stop_repeating)
        button.setOnClickListener(mStopRepeatingListener)
    }
}


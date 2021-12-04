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

import android.annotation.SuppressLint
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
import androidx.appcompat.app.AppCompatActivity

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R


/**
 * This demonstrates how you can schedule an alarm that causes a service to
 * be started. This is useful when you want to schedule alarms that initiate
 * long-running operations, such as retrieving recent e-mails.
 *
 * Note: as of API 19, all repeating alarms are inexact. If your application needs
 * precise delivery times then it must use one-time exact alarms, rescheduling each
 * time. Legacy applications whose targetSdkVersion is earlier than API 19 will
 * continue to have all of their alarms, including repeating alarms, treated as exact.
 */
@SuppressLint("ShortAlarm")
class AlarmService : AppCompatActivity() {
    /**
     * IntentSender used to launch our service
     */
    private var mAlarmSender: PendingIntent? = null

    /**
     * OnClickListener for Button R.id.start_alarm ("Start Alarm Service") starts the alarm service.
     * We first fetch the milliseconds since boot, including time spent in sleep to our [Long] variable
     * `val firstTime`. Then we get a handle to the [AlarmManager] system service to initialize our
     * [AlarmManager] variable `val am`, and use it to schedule a repeating alarm of type
     * ELAPSED_REALTIME_WAKEUP (which will wake up the device when it goes off), with the current
     * milliseconds contained in firstTime` as the time that the alarm should first go off, the
     * interval in milliseconds between subsequent repeats of the alarm set to 30 seconds, and our
     * [PendingIntent] field [mAlarmSender] as the action to perform when the alarm goes off.
     * Finally we display a Toast stating: "Repeating alarm will go off in 15 seconds and every 15
     * seconds after based on the elapsed realtime clock"
     *
     * The message is wrong of course, but who cares.
     *
     * Parameter: View of Button that was clicked
     */
    private val mStartAlarmListener = OnClickListener {
        // We want the alarm to go off 30 seconds from now.
        val firstTime = SystemClock.elapsedRealtime()

        // Schedule the alarm!
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
            firstTime, (30 * 1000).toLong(), mAlarmSender)

        // Tell the user about what we did.
        Toast.makeText(this@AlarmService, R.string.repeating_scheduled,
            Toast.LENGTH_LONG).show()
    }

    /**
     * OnClickListener for Button R.id.stop_alarm ("Stop Alarm Service") stops the alarm service.
     * First we get a handle to the [AlarmManager] system service to initialize our [AlarmManager]
     * variable `val am`, and use it to cancel any alarms with an [Intent] matching our [PendingIntent]
     * field [mAlarmSender]. Finally we show a Toast stating: "Repeating alarm has been unscheduled".
     *
     * Parameter: View of Button that was clicked
     */
    private val mStopAlarmListener = OnClickListener {
        // And cancel the alarm.
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        am.cancel(mAlarmSender)

        // Tell the user about what we did.
        Toast.makeText(this@AlarmService, R.string.repeating_unscheduled,
            Toast.LENGTH_LONG).show()
    }

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.alarm_service. Next we
     * initialize our [PendingIntent] field [mAlarmSender] to a new instance intended to launch
     * the `Service` [AlarmServiceService] which is declared to be a service in AndroidManifest.xml
     * using the element:
     *
     * `<service android:name=".app.AlarmService_Service" android:process=":remote"></service>`
     *
     * Then we locate [Button] R.id.start_alarm ("Start Alarm Service") and set its [OnClickListener]
     * to our [OnClickListener] field [mStartAlarmListener] which starts the alarm service when the
     * Button is clicked, and locate the Button R.id.stop_alarm ("Stop Alarm Service") and set its
     * [OnClickListener] to our [OnClickListener] field [mStopAlarmListener] which stops the alarm
     * service when the [Button] is clicked.
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_service)

        // Create an IntentSender that will launch our service, to be scheduled
        // with the alarm manager.
        mAlarmSender = PendingIntent.getService(
            this@AlarmService,
            0,
            Intent(this@AlarmService, AlarmServiceService::class.java),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }
        )


        // Watch for button clicks.
        var button = findViewById<Button>(R.id.start_alarm)
        button.setOnClickListener(mStartAlarmListener)
        button = findViewById(R.id.stop_alarm)
        button.setOnClickListener(mStopAlarmListener)
    }
}

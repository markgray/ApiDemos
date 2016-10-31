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

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.SystemClock;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;


/**
 * This demonstrates how you can schedule an alarm that causes a service to
 * be started.  This is useful when you want to schedule alarms that initiate
 * long-running operations, such as retrieving recent e-mails.
 *
 * Note: as of API 19, all repeating alarms are inexact. If your application needs
 * precise delivery times then it must use one-time exact alarms, rescheduling each
 * time. Legacy applications whose targetSdkVersion is earlier than API 19 will
 * continue to have all of their alarms, including repeating alarms, treated as exact.
 */
@SuppressLint("ShortAlarm")
public class AlarmService extends Activity {
    private PendingIntent mAlarmSender; // IntentSender used to launch our service

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, then we set our content view to our layout file R.layout.alarm_service. Next we
     * create PendingIntent mAlarmSender which is intended to launch Service AlarmService_Service
     * which is declared to be a service in AndroidManifest.xml using the element:
     *
     *    <service
     *        android:name=".app.AlarmService_Service"
     *        android:process=":remote" />
     *
     * Then we locate Button R.id.start_alarm ("Start Alarm Service") and set its OnClickListener to
     * OnClickListener mStartAlarmListener which starts the alarm service when the Button is clicked,
     * and locate the Button R.id.stop_alarm ("Stop Alarm Service") and set its OnClickListener to
     * OnClickListener mStopAlarmListener which stops the alarm service when the Button is clicked.
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_service);

        // Create an IntentSender that will launch our service, to be scheduled
        // with the alarm manager.
        mAlarmSender = PendingIntent.getService(AlarmService.this,
                0, new Intent(AlarmService.this, AlarmService_Service.class), 0);


        // Watch for button clicks.
        Button button = (Button)findViewById(R.id.start_alarm);
        button.setOnClickListener(mStartAlarmListener);
        button = (Button)findViewById(R.id.stop_alarm);
        button.setOnClickListener(mStopAlarmListener);
    }

    /**
     * OnClickListener for Button R.id.start_alarm ("Start Alarm Service") starts the alarm service
     */
    private OnClickListener mStartAlarmListener = new OnClickListener() {
        /**
         * Called when the Button R.id.start_alarm is clicked. We first fetch the milliseconds since
         * boot, including time spent in sleep to our variable long firstTime. Then we get a handle
         * to the AlarmManager system service in AlarmManager am, and use it to schedule a repeating
         * alarm  of type ELAPSED_REALTIME_WAKEUP (which will wake up the device when it goes off),
         * with the current milliseconds contained in firstTime as the time that the alarm should
         * first go off, the interval in milliseconds between subsequent repeats of the alarm set to
         * 30 seconds, and PendingIntent mAlarmSender as the action to perform when the alarm goes
         * off. Finally we display a Toast stating:
         *
         *      Repeating alarm will go off in 15 seconds and every
         *      15 seconds after based on the elapsed realtime clock
         *
         * The message is wrong of course, but who cares.
         *
         * @param v View of Button that was clicked
         */
        @Override
        public void onClick(View v) {
            // We want the alarm to go off 30 seconds from now.
            long firstTime = SystemClock.elapsedRealtime();

            // Schedule the alarm!
            AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
            am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            firstTime, 30*1000, mAlarmSender);

            // Tell the user about what we did.
            Toast.makeText(AlarmService.this, R.string.repeating_scheduled,
                    Toast.LENGTH_LONG).show();
        }
    };

    /**
     * OnClickListener for Button R.id.stop_alarm ("Stop Alarm Service") stops the alarm service
     */
    private OnClickListener mStopAlarmListener = new OnClickListener() {
        /**
         * Called when the Button R.id.stop_alarm is clicked. First we get a handle to the
         * AlarmManager system service in AlarmManager am, and use it to cancel any alarms with
         * an Intent matching PendingIntent mAlarmSender. Finally we show a Toast stating:
         *
         *        Repeating alarm has been unscheduled
         *
         * @param v View of Button that was clicked
         */
        @Override
        public void onClick(View v) {
            // And cancel the alarm.
            AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
            am.cancel(mAlarmSender);

            // Tell the user about what we did.
            Toast.makeText(AlarmService.this, R.string.repeating_unscheduled,
                    Toast.LENGTH_LONG).show();

        }
    };
}

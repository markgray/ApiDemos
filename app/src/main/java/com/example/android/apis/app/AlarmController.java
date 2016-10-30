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

import java.util.Calendar;

/**
 * Example of scheduling one-shot and repeating alarms.  See
 * {@link OneShotAlarm} for the code run when the one-shot alarm goes off, and
 * {@link RepeatingAlarm} for the code run when the repeating alarm goes off.
 * <h4>Demo</h4>
App/Service/Alarm Controller
 
<h4>Source files</h4>
<table class="LinkTable">
        <tr>
            <td class="LinkColumn">src/com.example.android.apis/app/AlarmController.java</td>
            <td class="DescrColumn">The activity that lets you schedule alarms</td>
        </tr>
        <tr>
            <td class="LinkColumn">src/com.example.android.apis/app/OneShotAlarm.java</td>
            <td class="DescrColumn">This is an intent receiver that executes when the
                one-shot alarm goes off</td>
        </tr>
        <tr>
            <td class="LinkColumn">src/com.example.android.apis/app/RepeatingAlarm.java</td>
            <td class="DescrColumn">This is an intent receiver that executes when the
                repeating alarm goes off</td>
        </tr>
        <tr>
            <td class="LinkColumn">/res/any/layout/alarm_controller.xml</td>
            <td class="DescrColumn">Defines contents of the screen</td>
        </tr>
</table>

 */
public class AlarmController extends Activity {
    Toast mToast; // Toast instance latest Toast

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, then we set our content view to our layout file R.layout.alarm_controller. Next we
     * locate the three Button's in our layout and set their OnClickListener's as follows:
     *
     *   R.id.one_shot "ONE SHOT ALARM" is set to OnClickListener mOneShotListener
     *   R.id.start_repeating "START REPEATING ALARM" is set to OnClickListener mStartRepeatingListener
     *   R.id.stop_repeating "STOP REPEATING ALARM" is set to OnClickListener mStopRepeatingListener
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.alarm_controller);

        // Watch for button clicks.
        Button button = (Button)findViewById(R.id.one_shot);
        button.setOnClickListener(mOneShotListener);
        button = (Button)findViewById(R.id.start_repeating);
        button.setOnClickListener(mStartRepeatingListener);
        button = (Button)findViewById(R.id.stop_repeating);
        button.setOnClickListener(mStopRepeatingListener);
    }

    /**
     * OnClickListener for Button R.id.one_shot "ONE SHOT ALARM"
     */
    private OnClickListener mOneShotListener = new OnClickListener() {
        /**
         * Called when the R.id.one_shot Button is clicked. We create an Intent to launch our
         * BroadcastReceiver OneShotAlarm. Then we create a PendingIntent sender that will perform
         * a broadcast. We create a Calendar calendar, set its current time to the current time in
         * milliseconds, and add 30 seconds to this calendar. Then we create an AlarmManager am, and
         * use it to schedule an alarm of type RTC_WAKEUP which will wake up the device when it goes
         * off, set to trigger at Calendar calendar, and using PendingIntent sender as the action to
         * perform when the alarm goes off. Then we make sure any previous Toast mToast has been
         * cancelled, and show a Toast mToast stating:
         *
         *   One-shot alarm will go off in 30 seconds based on the real time clock.
         *   Try changing the current time before then!
         *
         * @param v View of Button that was clicked.
         */
        @Override
        public void onClick(View v) {
            // When the alarm goes off, we want to broadcast an Intent to our
            // BroadcastReceiver.  Here we make an Intent with an explicit class
            // name to have our own receiver (which has been published in
            // AndroidManifest.xml) instantiated and called, and then create an
            // IntentSender to have the intent executed as a broadcast.
            Intent intent = new Intent(AlarmController.this, OneShotAlarm.class);
            PendingIntent sender = PendingIntent.getBroadcast(AlarmController.this,
                    0, intent, 0);

            // We want the alarm to go off 30 seconds from now.
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.SECOND, 30);

            // Schedule the alarm!
            AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);

            // Tell the user about what we did.
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(AlarmController.this, R.string.one_shot_scheduled,
                    Toast.LENGTH_LONG);
            mToast.show();
        }
    };

    /**
     * OnClickListener for Button R.id.start_repeating "START REPEATING ALARM"
     */
    @SuppressLint("ShortAlarm")
    private OnClickListener mStartRepeatingListener = new OnClickListener() {
        /**
         * Called when the R.id.start_repeating Button is clicked. We create an Intent to launch our
         * BroadcastReceiver RepeatingAlarm. Then we create a PendingIntent sender that will perform
         * a broadcast. We fetch the milliseconds since boot, including time spent in sleep to our
         * variable long firstTime and add 15 seconds to it. Then we create an AlarmManager am, and
         * use it to schedule an alarm of type ELAPSED_REALTIME_WAKEUP (Alarm time to use is
         * SystemClock.elapsedRealtime() (time since boot, including sleep), which will wake up the
         * device when it goes off), set to trigger at firstTime and using PendingIntent sender as
         * the action to perform when the alarm goes off. Then we make sure any previous Toast mToast
         * has been cancelled, and show a Toast mToast stating:
         *
         *        Repeating alarm will go off in 15 seconds and every
         *        15 seconds after based on the elapsed realtime clock
         *
         * @param v View of the Button that has been clicked.
         */
        @Override
        public void onClick(View v) {
            // When the alarm goes off, we want to broadcast an Intent to our
            // BroadcastReceiver.  Here we make an Intent with an explicit class
            // name to have our own receiver (which has been published in
            // AndroidManifest.xml) instantiated and called, and then create an
            // IntentSender to have the intent executed as a broadcast.
            // Note that unlike above, this IntentSender is configured to
            // allow itself to be sent multiple times.
            Intent intent = new Intent(AlarmController.this, RepeatingAlarm.class);
            PendingIntent sender = PendingIntent.getBroadcast(AlarmController.this,
                    0, intent, 0);
            
            // We want the alarm to go off 30 seconds from now.
            long firstTime = SystemClock.elapsedRealtime();
            firstTime += 15*1000;

            // Schedule the alarm!
            AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
            am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            firstTime, 15*1000, sender);

            // Tell the user about what we did.
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(AlarmController.this, R.string.repeating_scheduled,
                    Toast.LENGTH_LONG);
            mToast.show();
        }
    };

    /**
     * OnClickListener for Button R.id.stop_repeating "STOP REPEATING ALARM"
     */
    private OnClickListener mStopRepeatingListener = new OnClickListener() {
        /**
         * Called when the R.id.stop_repeating Button is clicked. We create an Intent that is
         * identical to the one used to start the repeating alarm, and a PendingIntent for a
         * Broadcast of that Intent intent. Then we create an AlarmManager am, and use it to
         * remove any alarms with an Intent matching PendingIntent sender. Then we make sure
         * any previous Toast mToast has been cancelled, and show a Toast mToast stating:
         *
         *       Repeating alarm has been unscheduled
         *
         * @param v View of the Button that was clicked
         */
        @Override
        public void onClick(View v) {
            // Create the same intent, and thus a matching IntentSender, for
            // the one that was scheduled.
            Intent intent = new Intent(AlarmController.this, RepeatingAlarm.class);
            PendingIntent sender = PendingIntent.getBroadcast(AlarmController.this,
                    0, intent, 0);
            
            // And cancel the alarm.
            AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
            am.cancel(sender);

            // Tell the user about what we did.
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(AlarmController.this, R.string.repeating_unscheduled,
                    Toast.LENGTH_LONG);
            mToast.show();
        }
    };
}


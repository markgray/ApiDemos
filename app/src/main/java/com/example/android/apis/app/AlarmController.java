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
import android.os.Build;
import android.os.SystemClock;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
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
@RequiresApi(api = Build.VERSION_CODES.M)
public class AlarmController extends Activity {
    /**
     * Latest Toast we have shown.
     */
    Toast mToast;

    /**
     * Called when the activity is starting. First we call our super's implementation of {@code onCreate},
     * then we set our content view to our layout file R.layout.alarm_controller. Next we find the three
     * buttons in our layout by their id and set their {@code OnClickListener} as follows:
     * <ul>
     *     <li>
     *         R.id.one_shot ("One Shot Alarm") set to {@code OnClickListener mOneShotListener}, its
     *         {@code onClick} override will call {@code AlarmManager.set} for this button id.
     *     </li>
     *     <li>
     *         R.id.one_shot_while_idle ("One Shot While-Idle Alarm") set to {@code OnClickListener mOneShotListener},
     *         its {@code onClick} override will call {@code AlarmManager.setExactAndAllowWhileIdle} for this button id.
     *     </li>
     *     <li>
     *         R.id.start_repeating ("Start Repeating Alarm") set to {@code OnClickListener mStartRepeatingListener},
     *         its {@code onClick} override will call {@code AlarmManager.setRepeating}
     *     </li>
     *     <li>
     *         R.id.stop_repeating ("Stop Repeating Alarm") set to {@code OnClickListener mStopRepeatingListener},
     *         its {@code onClick} override will call {@code AlarmManager.cancel} for the {@code PendingIntent}
     *         started by the "Start Repeating Alarm" button.
     *     </li>
     * </ul>
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.alarm_controller);

        // Watch for button clicks.
        Button button = findViewById(R.id.one_shot);
        button.setOnClickListener(mOneShotListener);
        button = findViewById(R.id.one_shot_while_idle);
        button.setOnClickListener(mOneShotListener);
        button = findViewById(R.id.start_repeating);
        button.setOnClickListener(mStartRepeatingListener);
        button = findViewById(R.id.stop_repeating);
        button.setOnClickListener(mStopRepeatingListener);
    }

    /**
     * {@code OnClickListener} used for buttons R.id.one_shot ("One Shot Alarm") and R.id.one_shot_while_idle
     * ("One Shot While-Idle Alarm"), its {@code onClick} override switches on the id of the view that
     * triggered it to differentiate.
     */
    private OnClickListener mOneShotListener = new OnClickListener() {
        /**
         * Called when our view has been clicked. We initialize {@code Intent intent} with an instance
         * intended for the {@code BroadcastReceiver} with the class {@code OneShotAlarm.class}, then
         * initialize {@code PendingIntent sender} with an instance that will perform a broadcast of
         * {@code intent} using the request code of 0, and no flags. We initialize {@code Calendar calendar}
         * with a new instance, set its time to the current time in milliseconds, then add 30 seconds to
         * it. We initialize {@code AlarmManager am} with a handle to the ALARM_SERVICE system level service.
         * We then switch on the id of our parameter {@code View view}:
         * <ul>
         *     <li>
         *         R.id.one_shot: ("One Shot Alarm" button) we call the {@code set} method of {@code am}
         *         to schedule an RTC_WAKEUP alarm (wall clock time in UTC which will wake up the device
         *         when it goes off) to fire at the time in milliseconds of {@code calendar} when it will
         *         broadcast {@code sender}.
         *     </li>
         *     <li>
         *         default: ("One Shot While-Idle Alarm" button in our case) we call the {@code setExactAndAllowWhileIdle}
         *         method of {@code am} to schedule an RTC_WAKEUP alarm (wall clock time in UTC which will wake up the device
         *         when it goes off) to fire at the time in milliseconds of {@code calendar} when it will broadcast {@code sender}.
         *         This is like the call to {@code AlarmManager.set} but is also allowed even when the system is in low-power
         *         idle modes.
         *     </li>
         * </ul>
         * If our field {@code Toast mToast} is not null we cancel it. We then set {@code mToast} to
         * an instance that will display the string with id R.string.one_shot_scheduled ("One-shot alarm
         * will go off in 30 seconds...") and show it to the user.
         *
         * @param v The view that was clicked.
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

            switch (v.getId()) {
                case R.id.one_shot:
                    //noinspection ConstantConditions
                    am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
                    break;
                default:
                    //noinspection ConstantConditions
                    am.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
                    break;
            }

            // Tell the user about what we did.
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(AlarmController.this, R.string.one_shot_scheduled, Toast.LENGTH_LONG);
            mToast.show();
        }
    };

    /**
     * {@code OnClickListener} used for the button R.id.start_repeating ("Start Repeating Alarm")
     *
     */
    private OnClickListener mStartRepeatingListener = new OnClickListener() {
        /**
         * Called when the button with id R.id.start_repeating ("Start Repeating Alarm") is clicked.
         * We initialize {@code Intent intent} with an instance intended for the {@code BroadcastReceiver}
         * with the class {@code RepeatingAlarm.class}, then initialize {@code PendingIntent sender}
         * with an instance that will perform a broadcast of {@code intent} using the request code of 0,
         * and no flags. We initialize {@code long firstTime} with the milliseconds since boot then add
         * 15,000 to it. We initialize {@code AlarmManager am} with a handle to the ALARM_SERVICE system
         * level service, then call its {@code setRepeating} method to schedule a repeating alarm of type
         * ELAPSED_REALTIME_WAKEUP (time since boot, including sleep, which will wake up the device when
         * it goes off) which will first go off at {@code firstTime} then again at 15 second intervals
         * broadcasting {@code sender} to its {@code BroadcastReceiver}. If our field {@code Toast mToast}
         * is not null we cancel it. We then set {@code mToast} to an instance that will display the string
         * with id R.string.repeating_scheduled ("Repeating alarm will go off in 15 seconds..") and show
         * it to the user.
         *
         * @param v {@code View} that was clicked.
         */
        @SuppressLint("ShortAlarm")
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
            //noinspection ConstantConditions
            am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            firstTime, 15*1000, sender);

            // Tell the user about what we did.
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(AlarmController.this, R.string.repeating_scheduled, Toast.LENGTH_LONG);
            mToast.show();
        }
    };

    /**
     * {@code OnClickListener} used for the button R.id.stop_repeating ("Stop Repeating Alarm")
     */
    private OnClickListener mStopRepeatingListener = new OnClickListener() {
        /**
         * Called when the button with id R.id.stop_repeating ("Stop Repeating Alarm") is clicked.
         * We initialize {@code Intent intent} with an instance intended for the {@code BroadcastReceiver}
         * with the class {@code RepeatingAlarm.class}, then initialize {@code PendingIntent sender}
         * with an instance that will perform a broadcast of {@code intent} using the request code of 0,
         * and no flags. We initialize {@code AlarmManager am} with a handle to the ALARM_SERVICE system
         * level service, then call its {@code cancel} method to remove any alarms with an {@link Intent}
         * matching {@code sender} (the one we scheduled with the R.id.start_repeating ("Start Repeating Alarm")
         * button in our case). If our field {@code Toast mToast} is not null we cancel it. We then set
         * {@code mToast} to an instance that will display the string with id R.string.repeating_unscheduled
         * ("Repeating alarm has been unscheduled") and show it to the user.
         *
         * @param v {@code View} that was clicked.
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
            //noinspection ConstantConditions
            am.cancel(sender);

            // Tell the user about what we did.
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(AlarmController.this, R.string.repeating_unscheduled, Toast.LENGTH_LONG);
            mToast.show();
        }
    };
}


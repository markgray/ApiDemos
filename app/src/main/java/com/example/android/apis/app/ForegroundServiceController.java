package com.example.android.apis.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android.apis.R;

/**
 * <p>Example of explicitly starting and stopping the {@link ForegroundService}.
 */
public class ForegroundServiceController extends AppCompatActivity {
    /**
     * Called when the activity is starting. First we call our super's implementation of {@code onCreate},
     * then we set our content view to our layout file R.layout.foreground_service_controller. We
     * then proceed to set {@code Button button} by finding the buttons in our layout file in order
     * to set their {@code OnClickListener} as follows:
     * <ul>
     *     <li>
     *         R.id.start_foreground: {@code mForegroundListener} (starts {@code ForegroundService}
     *         running in the foreground).
     *     </li>
     *     <li>
     *         R.id.start_foreground_wakelock: {@code mForegroundWakelockListener} (starts {@code ForegroundService}
     *         running in the foreground with a wakelock).
     *     </li>
     *     <li>
     *         R.id.start_background: {@code mBackgroundListener} (starts {@code ForegroundService}
     *         running in the background).
     *     </li>
     *     <li>
     *         R.id.start_background_wakelock: {@code mBackgroundWakelockListener} (starts {@code ForegroundService}
     *         running in the background with a wakelock).
     *     </li>
     *     <li>
     *         R.id.stop: {@code mStopListener} calls the {@code stopService} method to stop
     *         {@code ForegroundService}.
     *     </li>
     *     <li>
     *         R.id.start_foreground_2: {@code mForegroundListener2} starts {@code ForegroundService2}
     *         running in the foreground.
     *     </li>
     *     <li>
     *         R.id.stop_2: {@code mStopListener2} calls the {@code stopService} method to stop
     *         {@code ForegroundService2}.
     *     </li>
     *     <li>
     *         R.id.start_foreground_2_alarm: {@code mForegroundAlarmListener} uses an alarm to
     *         start {@code ForegroundService2} running in the foreground 15 seconds from now.
     *     </li>
     * </ul>
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.foreground_service_controller);

        // Watch for button clicks.
        Button button = findViewById(R.id.start_foreground);
        button.setOnClickListener(mForegroundListener);
        button = findViewById(R.id.start_foreground_wakelock);
        button.setOnClickListener(mForegroundWakelockListener);
        button = findViewById(R.id.start_background);
        button.setOnClickListener(mBackgroundListener);
        button = findViewById(R.id.start_background_wakelock);
        button.setOnClickListener(mBackgroundWakelockListener);
        button = findViewById(R.id.stop);
        button.setOnClickListener(mStopListener);
        button = findViewById(R.id.start_foreground_2);
        button.setOnClickListener(mForegroundListener2);
        button = findViewById(R.id.stop_2);
        button.setOnClickListener(mStopListener2);
        button = findViewById(R.id.start_foreground_2_alarm);
        button.setOnClickListener(mForegroundAlarmListener);
    }

    /**
     * {@code OnClickListener} for the button with id R.id.start_foreground
     */
    private View.OnClickListener mForegroundListener = new View.OnClickListener() {
        /**
         * Called when the button with id R.id.start_foreground is clicked. We initialize
         * {@code Intent intent} with a new instance whose action is ACTION_FOREGROUND, set the
         * class it is to launch to {@code ForegroundService} then call the {@code startService}
         * method with it to request that that application service be started.
         *
         * @param v {@code View} that was clicked.
         */
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ForegroundService.ACTION_FOREGROUND);
            intent.setClass(ForegroundServiceController.this, ForegroundService.class);
            startService(intent);
        }
    };

    /**
     * {@code OnClickListener} for the button with id R.id.start_foreground_wakelock
     */
    private View.OnClickListener mForegroundWakelockListener = new View.OnClickListener() {
        /**
         * Called when the button with id R.id.start_foreground_wakelock is clicked. We initialize
         * {@code Intent intent} with a new instance whose action is ACTION_FOREGROUND_WAKELOCK, set the
         * class it is to launch to {@code ForegroundService} then call the {@code startService}
         * method with it to request that that application service be started.
         *
         * @param v {@code View} that was clicked.
         */
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ForegroundService.ACTION_FOREGROUND_WAKELOCK);
            intent.setClass(ForegroundServiceController.this, ForegroundService.class);
            startService(intent);
        }
    };

    /**
     * {@code OnClickListener} for the button with id R.id.start_background
     */
    private View.OnClickListener mBackgroundListener = new View.OnClickListener() {
        /**
         * Called when the button with id R.id.start_background is clicked. We initialize
         * {@code Intent intent} with a new instance whose action is ACTION_BACKGROUND, set the
         * class it is to launch to {@code ForegroundService} then call the {@code startService}
         * method with it to request that that application service be started.
         *
         * @param v {@code View} that was clicked.
         */
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ForegroundService.ACTION_BACKGROUND);
            intent.setClass(ForegroundServiceController.this, ForegroundService.class);
            startService(intent);
        }
    };

    /**
     * {@code OnClickListener} for the button with id R.id.start_background_wakelock
     */
    private View.OnClickListener mBackgroundWakelockListener = new View.OnClickListener() {
        /**
         * Called when the button with id R.id.start_background_wakelock is clicked. We initialize
         * {@code Intent intent} with a new instance whose action is ACTION_BACKGROUND_WAKELOCK, set the
         * class it is to launch to {@code ForegroundService} then call the {@code startService}
         * method with it to request that that application service be started.
         *
         * @param v {@code View} that was clicked.
         */
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ForegroundService.ACTION_BACKGROUND_WAKELOCK);
            intent.setClass(ForegroundServiceController.this, ForegroundService.class);
            startService(intent);
        }
    };

    /**
     * {@code OnClickListener} for the button with id R.id.stop
     */
    private View.OnClickListener mStopListener = new View.OnClickListener() {
        /**
         * Called when the button with id R.id.stop is clicked. We the {@code stopService} method
         * to stop {@code ForegroundService}.
         *
         * @param v {@code View} that was clicked.
         */
        @Override
        public void onClick(View v) {
            stopService(new Intent(ForegroundServiceController.this, ForegroundService.class));
        }
    };

    /**
     * {@code OnClickListener} for the button with id R.id.start_foreground_2
     */
    private View.OnClickListener mForegroundListener2 = new View.OnClickListener() {
        /**
         * Called when the button with id R.id.start_foreground_2 is clicked. We initialize
         * {@code Intent intent} with a new instance whose action is ACTION_FOREGROUND, set the
         * class it is to launch to {@code ForegroundService2} then call the {@code startService}
         * method with it to request that that application service be started.
         *
         * @param v {@code View} that was clicked.
         */
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ForegroundService.ACTION_FOREGROUND);
            intent.setClass(ForegroundServiceController.this, ForegroundService2.class);
            startService(intent);
        }
    };

    /**
     * {@code OnClickListener} for the button with id R.id.start_foreground_2_alarm
     */
    private View.OnClickListener mForegroundAlarmListener = new View.OnClickListener() {
        /**
         * Called when the button with id R.id.start_foreground_2_alarm is clicked. First we initialize
         * {@code Context ctx} with the context of this {@code Controller} activity. We initialize
         * {@code Intent intent} with a new instance whose action is ACTION_FOREGROUND, and set its
         * class to that of {@code ForegroundService2}. We initialize {@code PendingIntent pi} with
         * a PendingIntent that will start the foreground service specified by {@code intent} (which
         * is {@code ForegroundService2} as you recall), the request code is 0 and no flags are used.
         * We initialize {@code AlarmManager am} with a handle to the system level service ALARM_SERVICE,
         * and use it to schedule an alarm to be delivered precisely 15 seconds in ELAPSED_REALTIME from
         * now (does not wake the device up; if it goes off while the device is asleep, it will not be
         * delivered until the next time the device wakes up) with {@code pi} as the operation that will
         * be performed at that time. Finally we log that we are starting that service in 15 seconds.
         *
         * @param v {@code View} that was clicked.
         */
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onClick(View v) {
            final Context ctx = ForegroundServiceController.this;

            final Intent intent = new Intent(ForegroundService.ACTION_FOREGROUND);
            intent.setClass(ctx, ForegroundService2.class);

            PendingIntent pi = PendingIntent.getForegroundService(ctx, 0, intent, 0);
            AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
            //noinspection ConstantConditions
            am.setExact(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + 15_000,
                    pi);
            Log.i("ForegroundService", "Starting service in 15 seconds");
        }
    };

    /**
     * {@code OnClickListener} for the button with id R.id.stop_2
     */
    private View.OnClickListener mStopListener2 = new View.OnClickListener() {
        /**
         * Called when the button with id R.id.stop_2 is clicked. We the {@code stopService} method
         * to stop {@code ForegroundService2}.
         *
         * @param v {@code View} that was clicked.
         */
        @Override
        public void onClick(View v) {
            stopService(new Intent(ForegroundServiceController.this, ForegroundService2.class));
        }
    };

}

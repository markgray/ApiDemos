/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.example.android.apis.os;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import com.example.android.apis.R;

/**
 * <h3>Application showing the Trigger Sensor API for the Significant Motion sensor. </h3>
 * <p>
 * This demonstrates the {@link android.hardware.SensorManager android.hardware.SensorManager
 * android.hardware.TriggerEventListener} class. Shows how to use the Sensor.TYPE_SIGNIFICANT_MOTION
 * sensor as a TriggerSensor and respond to TriggerEvent it receives as a TriggerEventListener.
 * <h4>Demo</h4>
 * OS / TriggerSensors
 * <p>
 * <h4>Source files</h4>
 * <p>
 * src/com.example.android.apis/os/TriggerSensors.java
 * <p>
 * src/main/res/layout/trigger_sensors.xml
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class TriggerSensors extends Activity {
    /**
     * A {@code SensorManager} we use for accessing sensors.
     */
    private SensorManager mSensorManager;
    /**
     * The default sensor for the TYPE_SIGNIFICANT_MOTION sensor (significant motion trigger sensor)
     */
    private Sensor mSigMotion;
    /**
     * Our instance of our class {@code TriggerListener} (extends {@code TriggerEventListener})
     */
    private TriggerListener mListener;
    /**
     * {@code TextView} in our layout with ID R.id.text, used to display information about what is
     * happening as it happens, by appending lines of text to it.
     */
    private TextView mTextView;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to our layout file R.layout.trigger_sensors.
     * We initialize our field {@code SensorManager mSensorManager} by fetching a handle to the
     * system level service SENSOR_SERVICE ("sensor"), initialize our field {@code Sensor mSigMotion}
     * with the default sensor for the type TYPE_SIGNIFICANT_MOTION (significant motion trigger sensor),
     * initialize our field {@code TextView mTextView} by locating the view with ID R.id.text in our
     * layout, and initialize our field {@code TriggerListener mListener} with a new instance of
     * {@code TriggerListener} using {@code mTextView} as the {@code TextView} for it to display any
     * information it wants the user to see.
     * <p>
     * Finally if {@code mSigMotion} is null we append the text R.string.no_sig_motion ("Significant
     * Motion Sensor Not Detected") to {@code mTextView}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trigger_sensors);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //noinspection ConstantConditions
        mSigMotion = mSensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);
        mTextView = (TextView) findViewById(R.id.text);
        mListener = new TriggerListener(this, mTextView);
        if (mSigMotion == null) {
            mTextView.append(getString(R.string.no_sig_motion) + "\n");
        }
    }

    /**
     * Called after {@link #onRestoreInstanceState}, {@link #onRestart}, or {@link #onPause}, for
     * our activity to start interacting with the user. First we call through to our super's
     * implementation of {@code onResume}, then if our field {@code Sensor mSigMotion} is not null,
     * and the {@code requestTriggerSensor} of {@code SensorManager mSensorManager} returns true
     * when we enable {@code Sensor mSigMotion} with our listener {@code TriggerListener mListener}
     * to receive its events (true indicates that the sensor was successfully enabled), we then append
     * the string R.string.sig_motion_enabled ("Significant Motion Sensor Enabled") to {@code mTextView}.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (mSigMotion != null && mSensorManager.requestTriggerSensor(mListener, mSigMotion))
            mTextView.append(getString(R.string.sig_motion_enabled) + "\n");
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. First we call through to our super's implementation of {@code onPause},
     * then if {@code mSigMotion} is not null we cancel the receiving of trigger events by {@code mListener}
     * for {@code mSigMotion}.
     */
    @Override
    protected void onPause() {
        super.onPause();
        // Call disable only if needed for cleanup.
        // The sensor is auto disabled when triggered.
        if (mSigMotion != null) mSensorManager.cancelTriggerSensor(mListener, mSigMotion);
    }


    /**
     * Called when you are no longer visible to the user. If {@code mSigMotion} is not null we cancel
     * the receiving of trigger events by {@code mListener} for {@code mSigMotion}, then we call our
     * super's implementation of {@code onStop}
     */
    @Override
    protected void onStop() {
        if (mSigMotion != null) mSensorManager.cancelTriggerSensor(mListener, mSigMotion);
        super.onStop();
    }
}

/**
 * {@code TriggerEventListener} for the TYPE_SIGNIFICANT_MOTION sensor (significant motion trigger sensor)
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class TriggerListener extends TriggerEventListener {
    /**
     * {@code Context} to use for accessing resources
     */
    private Context mContext;
    /**
     * {@code TextView} to use to display information to the user in.
     */
    private TextView mTextView;

    /**
     * Our constructor. We just save our parameters {@code Context context} and {@code TextView textView}
     * in our fields {@code Context mContext} and {@code TextView mTextView} respectively.
     *
     * @param context  {@code Context} to use for accessing resources, this when called from the {@code onCreate}
     *                 method of {@code TriggerSensors}.
     * @param textView {@code TextView} to use to display information to user in.
     */
    TriggerListener(Context context, TextView textView) {
        mContext = context;
        mTextView = textView;
    }

    /**
     * The method that will be called when the sensor is triggered. If the field {@code values[0]} of
     * our parameter {@code TriggerEvent event} is equal to 1, we append two lines of text to {@code mTextView}:
     * <ul>
     * <li>
     * R.string.sig_motion - "Significant Motion Sensor Detected"
     * </li>
     * <li>
     * R.string.sig_motion_auto_disabled - "Significant Motion Sensor Auto Disabled"
     * </li>
     * </ul>
     *
     * @param event The details of the event.
     */
    @Override
    public void onTrigger(TriggerEvent event) {
        if (event.values[0] == 1) {
            mTextView.append(mContext.getString(R.string.sig_motion) + "\n");
            mTextView.append(mContext.getString(R.string.sig_motion_auto_disabled) + "\n");
        }
        // Sensor is auto disabled.
    }
}

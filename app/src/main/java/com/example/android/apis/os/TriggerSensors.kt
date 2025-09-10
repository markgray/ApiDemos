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
package com.example.android.apis.os

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.TriggerEvent
import android.hardware.TriggerEventListener
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Application showing the Trigger Sensor API for the Significant Motion sensor.
 *
 * This demonstrates the [SensorManager] class. Shows how to use the [Sensor.TYPE_SIGNIFICANT_MOTION]
 * sensor as a `TriggerSensor` and respond to [TriggerEvent] it receives as a [TriggerEventListener].
 *
 * Demo OS / TriggerSensors
 *
 * Source files
 *
 *  * os/TriggerSensors.java
 *  * res/layout/trigger_sensors.xml
 *
 * RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
 */
class TriggerSensors : AppCompatActivity() {
    /**
     * A [SensorManager] we use for accessing sensors.
     */
    private var mSensorManager: SensorManager? = null

    /**
     * The default sensor for the TYPE_SIGNIFICANT_MOTION sensor (significant motion trigger sensor)
     */
    private var mSigMotion: Sensor? = null

    /**
     * Our instance of our class [TriggerListener] (extends [TriggerEventListener])
     */
    private var mListener: TriggerListener? = null

    /**
     * [TextView] in our layout with ID R.id.text, used to display information about what is
     * happening as it happens, by appending lines of text to it.
     */
    private var mTextView: TextView? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.trigger_sensors. We
     * initialize our [SensorManager] field [mSensorManager] by fetching a handle to the system
     * level service [Context.SENSOR_SERVICE] ("sensor"), initialize our [Sensor] field [mSigMotion]
     * with the default sensor for the type [Sensor.TYPE_SIGNIFICANT_MOTION] (significant motion
     * trigger sensor), initialize our [TextView] field [mTextView] by locating the view with ID
     * R.id.text in our layout, and initialize our [TriggerListener] field [mListener] with a new
     * instance of [TriggerListener] using [mTextView] as the [TextView] for it to display any
     * information it wants the user to see.
     *
     * Finally if [mSigMotion] is null we append the text R.string.no_sig_motion ("Significant
     * Motion Sensor Not Detected") to [mTextView].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.trigger_sensors)
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mSigMotion = mSensorManager!!.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION)
        mTextView = findViewById(R.id.text)
        mListener = TriggerListener(this, mTextView)
        if (mSigMotion == null) {
            mTextView!!.append(getString(R.string.no_sig_motion) + "\n")
        }
    }

    /**
     * Called after [onRestoreInstanceState], [onRestart], or [onPause], for our activity to start
     * interacting with the user. First we call through to our super's implementation of `onResume`,
     * then if our [Sensor] field [mSigMotion] is not null, and the `requestTriggerSensor` method of
     * [SensorManager] field [mSensorManager] returns true when we enable [Sensor] field [mSigMotion]
     * with our listener [TriggerListener] field [mListener] to receive its events (true indicates
     * that the sensor was successfully enabled), we then append the string R.string.sig_motion_enabled
     * ("Significant Motion Sensor Enabled") to [mTextView].
     */
    override fun onResume() {
        super.onResume()
        if (mSigMotion != null && mSensorManager!!.requestTriggerSensor(mListener, mSigMotion))
            mTextView!!.append(getString(R.string.sig_motion_enabled) + "\n")
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. First we call through to our super's implementation of `onPause`,
     * then if our [mSigMotion] field is not null we cancel the receiving of trigger events by
     * our [mListener] field for our [mSigMotion] field.
     */
    override fun onPause() {
        super.onPause()
        // Call disable only if needed for cleanup.
        // The sensor is auto disabled when triggered.
        if (mSigMotion != null) mSensorManager!!.cancelTriggerSensor(mListener, mSigMotion)
    }

    /**
     * Called when you are no longer visible to the user. If [mSigMotion] is not null we cancel
     * the receiving of trigger events by [mListener] for [mSigMotion], then we call our super's
     * implementation of `onStop`
     */
    override fun onStop() {
        if (mSigMotion != null) mSensorManager!!.cancelTriggerSensor(mListener, mSigMotion)
        super.onStop()
    }
}

/**
 * [TriggerEventListener] for the TYPE_SIGNIFICANT_MOTION sensor (significant motion trigger sensor)
 *
 * @property mContext [Context] to use for accessing resources
 * @property mTextView [TextView] to use to display information to the user in.
 * RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
 */
internal class TriggerListener(
    private val mContext: Context,
    private val mTextView: TextView?

) : TriggerEventListener() {

    /**
     * The method that will be called when the sensor is triggered. If the field `values[0]` of
     * our parameter [TriggerEvent] parameter [event] is equal to 1, we append two lines of text
     * to [mTextView]:
     *
     *  * R.string.sig_motion - "Significant Motion Sensor Detected"
     *  * R.string.sig_motion_auto_disabled - "Significant Motion Sensor Auto Disabled"
     *
     * @param event The details of the event.
     */
    override fun onTrigger(event: TriggerEvent) {
        if (event.values[0] == 1f) {
            mTextView!!.append(mContext.getString(R.string.sig_motion) + "\n")
            mTextView.append(mContext.getString(R.string.sig_motion_auto_disabled) + "\n")
        }
        // Sensor is auto disabled.
    }

}
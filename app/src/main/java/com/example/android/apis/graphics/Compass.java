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

package com.example.android.apis.graphics;

import android.content.Context;
import android.graphics.*;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * Displays a compass pointer which it rotates according to SensorEvent it receives in its
 * override of the onSensorChanged method. The rotation is done using the matrix pre-concatenated
 * by Canvas.rotate(float degrees)
 */
@SuppressWarnings({"ConstantIfStatement", "ConstantConditions"})
public class Compass extends GraphicsActivity {

    /**
     * TAG used for logging
     */
    private static final String TAG = "Compass";

    /**
     * Handle to an instance of the system service Context.SENSOR_SERVICE. A {@code SensorManager}
     * lets you access the device's sensors.
     */
    private SensorManager mSensorManager;
    /**
     * Default {@code Sensor} for the type Sensor.TYPE_ORIENTATION, an orientation sensor type.
     */
    private Sensor mSensor;
    /**
     * An instance of {@code SampleView} which is used as our content view and displays the compass
     * pointer which is updated as our orientation sensor changes directions.
     */
    private SampleView mView;
    /**
     * Rotation vector returned in the latest {@code SensorEvent} received from our orientation sensor.
     * It consists of values[0]: Azimuth, angle between the magnetic north direction and the y-axis,
     * around the z-axis (0 to 359). 0=North, 90=East, 180=South, 270=West; values[1]: Pitch, rotation
     * around x-axis (-180 to 180), with positive values when the z-axis moves toward the y-axis;
     * values[2]: Roll, rotation around the y-axis (-90 to 90) increasing as the device moves clockwise.
     */
    private float[] mValues;

    /**
     * Listener we register for updates to our {@code Sensor mSensor} orientation sensor
     */
    private final SensorEventListener mListener = new SensorEventListener() {
        /**
         * Called when sensor values have changed. First we save a copy of the {@code SensorEvent event}
         * field {@code float SensorEvent.values} in our field {@code float[] mValues}, then is our
         * field {@code SampleView mView} is not null we invalidate the whole view so it will be redrawn.
         *
         * @param event the {@link android.hardware.SensorEvent SensorEvent}.
         */
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (false) {
                Log.d(TAG, "sensorChanged (" + event.values[0] + ", " + event.values[1] + ", " + event.values[2] + ")");
            }
            mValues = event.values;
            if (mView != null) {
                mView.invalidate();
            }
        }

        /**
         * Called when the accuracy of the registered sensor has changed.
         *
         * @param sensor {@code Sensor} whose accuracy has changed
         * @param accuracy The new accuracy of this sensor, one of
         *         {@code SensorManager.SENSOR_STATUS_*}
         */
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    /**
     * Called when the activity is starting.
     *
     * @param icicle We do not override {@code onSaveInstanceState} so do not use
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mView = new SampleView(this);
        setContentView(mView);
    }

    @Override
    protected void onResume() {
        if (false) Log.d(TAG, "onResume");
        super.onResume();

        mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onStop() {
        if (false) Log.d(TAG, "onStop");
        mSensorManager.unregisterListener(mListener);
        super.onStop();
    }

    private class SampleView extends View {
        private Paint mPaint = new Paint();
        private Path mPath = new Path();
        private boolean mAnimate;

        public SampleView(Context context) {
            super(context);

            // Construct a wedge-shaped path
            mPath.moveTo(0, -50);
            mPath.lineTo(-20, 60);
            mPath.lineTo(0, 50);
            mPath.lineTo(20, 60);
            mPath.close();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            Paint paint = mPaint;

            canvas.drawColor(Color.WHITE);

            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);

            int w = canvas.getWidth();
            int h = canvas.getHeight();
            int cx = w / 2;
            int cy = h / 2;

            canvas.translate(cx, cy);
            if (mValues != null) {
                canvas.rotate(-mValues[0]);
            }
            canvas.drawPath(mPath, mPaint);
        }

        @Override
        protected void onAttachedToWindow() {
            mAnimate = true;
            if (false) Log.d(TAG, "onAttachedToWindow. mAnimate=" + mAnimate);
            super.onAttachedToWindow();
        }

        @Override
        protected void onDetachedFromWindow() {
            mAnimate = false;
            if (false) Log.d(TAG, "onDetachedFromWindow. mAnimate=" + mAnimate);
            super.onDetachedFromWindow();
        }
    }
}

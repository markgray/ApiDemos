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
 * This sample only draws a nice looking arrow, and logs sensor readings.
 * No meaningful graphics lessons here?
 */
@SuppressWarnings("FieldCanBeLocal")
public class SensorTest extends GraphicsActivity {
    /**
     * TAG used for logging
     */
    private final String TAG = "SensorTest";

    /**
     * {@code SensorManager} for accessing sensors
     */
    private SensorManager mSensorManager;
    /**
     * Default sensor for the accelerometer sensor type TYPE_ACCELEROMETER
     */
    private Sensor mSensor;
    /**
     * Our display of an arrow
     */
    private SampleView mView;
    /**
     * The {@code onDraw} method of {@code SampleView} rotates the canvas based on the value of
     * {@code mValues[0]} - must have been copied from the compass example.
     */
    private float[] mValues = new float[1];

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}. We fetch the handle to the system level service SENSOR_SERVICE to set our
     * field {@code SensorManager mSensorManager}, then use it to initialize our field {@code Sensor mSensor}
     * with the default sensor for the accelerometer sensor type. Finally we create a new instance of
     * {@code SampleView} for our field {@code SampleView mView} and set our content view to it.
     *
     * @param icicle We do not call {@code onSaveInstanceState} so do not use
     */
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mView = new SampleView(this);
        setContentView(mView);
    }

    /**
     * Called after {@link #onRestoreInstanceState}, {@link #onRestart}, or {@link #onPause}, for
     * your activity to start interacting with the user. First we call through to our super's
     * implementation of {@code onResume}, then we register {@code SensorEventListener mListener} as
     * the listener for our sensor {@code Sensor mSensor} using SENSOR_DELAY_FASTEST (get sensor data
     * as quickly as possible).
     */
    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    /**
     * Called when you are no longer visible to the user. We unregister {@code SensorEventListener mListener}
     * as a listener for its sensors, then call through to our super's implementation of {@code onStop}.
     */
    @Override
    protected void onStop() {
        mSensorManager.unregisterListener(mListener);
        super.onStop();
    }

    /**
     * Unused so I won't comment on it.
     */
    @SuppressWarnings("unused")
    private static class RunAve {
        private final float[] mWeights;
        private final float mWeightScale;
        private final float[] mSamples;
        private final int mDepth;
        private int mCurr;

        public RunAve(float[] weights) {
            mWeights = weights;

            float sum = 0;
            for (float weight : weights) {
                sum += weight;
            }
            mWeightScale = 1 / sum;

            mDepth = weights.length;
            mSamples = new float[mDepth];
            mCurr = 0;
        }

        public void addSample(float value) {
            mSamples[mCurr] = value;
            mCurr = (mCurr + 1) % mDepth;
        }

        public float computeAve() {
            final int depth = mDepth;
            int index = mCurr;
            float sum = 0;
            for (int i = 0; i < depth; i++) {
                sum += mWeights[i] * mSamples[index];
                index -= 1;
                if (index < 0) {
                    index = depth - 1;
                }
            }
            return sum * mWeightScale;
        }
    }

    /**
     * An anonymous {@code SensorEventListener} that we use to listen to {@code Sensor mSensor}
     */
    private final SensorEventListener mListener = new SensorEventListener() {

        /**
         * Scale factors used in calculating whether the sensor has changed enough to constitute a
         * "serious" move left/right or up/down.
         */
        private final float[] mScale = new float[]{2, 2.5f, 0.5f};   // acceleration
        /**
         * Values of the previous {@code SensorEvent.values[]} array, used to detect change in the
         * sensor readings.
         */
        private float[] mPrev = new float[3];
        /**
         * Time in milliseconds since boot of the last time we logged a gesture. Used to limit that
         * output to 1 per second.
         */
        private long mLastGestureTime;

        /**
         * Called when sensor values have changed. First we set our flag {@code show} to false (we set
         * it to true if the sensor readings have changed enough to justify logging them (which we then
         * do)). We allocate 3 floats for {@code float[] diff}, then loop through the 3 readings in
         * {@code event.values[]} and 3 previous readings in our field {@code mPrev[]}, scaling each
         * change in readings then rounding them to the nearest {@code int} before assigning the result
         * to the corresponding {@code diff[]}. If the absolute value is greater than 0, we set {@code show}
         * to true. We then save the current sensor reading in {@code mPrev[]} to the next time we are
         * called and loop back for the next element of {@code values[]}.
         *
         * When done with the sensor readings we check if {@code show} is now true, and if so log the
         * sensor changes, increment {@code mValues[0]} modulo 360, and invalidate {@code SampleView mView}
         * so that the compass arrow angle will change (why not?).
         *
         * We fetch the milliseconds since boot for {@code long now}, and if 1000 milliseconds have passed
         * since {@code mLastGestureTime} was last set we set {@code mLastGestureTime} to 0 copy {@code diff[0]}
         * to {@code float x} and {@code diff[1]} to {@code float y}. We set {@code boolean gestX} to true
         * if the absolute value of {@code x} is greater than 3 and {@code boolean gestY} to true if the
         * absolute value of {@code y} is greater than 3. Then if either {@code gestX} or {@code gestY}
         * is true, but both are not true we have a gesture to log. If {@code gestX} was the one that was
         * true, a value for {@code x} less than 0 is logged as a LEFT gesture, otherwise it is logged
         * as a RITE gesture. If {@code gestY} was the one that was true, a value of {@code y} less than
         * -2 is logged as UP, otherwise it is logged as DOWN. If we logged a gesture we now set the
         * value of {@code mLastGestureTime} to {@code now}.
         *
         * @param event the {@link android.hardware.SensorEvent SensorEvent}.
         */
        @Override
        public void onSensorChanged(SensorEvent event) {
            boolean show = false;
            float[] diff = new float[3];

            for (int i = 0; i < 3; i++) {
                diff[i] = Math.round(mScale[i] * (event.values[i] - mPrev[i]) * 0.45f);
                if (Math.abs(diff[i]) > 0) {
                    show = true;
                }
                mPrev[i] = event.values[i];
            }

            if (show) {
                // only shows if we think the delta is big enough, in an attempt
                // to detect "serious" moves left/right or up/down
                Log.e(TAG, "sensorChanged " + event.sensor.getName() +
                        " (" + event.values[0] + ", " + event.values[1] + ", " +
                        event.values[2] + ")" + " diff(" + diff[0] +
                        " " + diff[1] + " " + diff[2] + ")");
                mValues[0] = (mValues[0] + 5) % 360;
                mView.invalidate();
            }

            long now = android.os.SystemClock.uptimeMillis();
            if (now - mLastGestureTime > 1000) {
                mLastGestureTime = 0;

                float x = diff[0];
                float y = diff[1];
                boolean gestX = Math.abs(x) > 3;
                boolean gestY = Math.abs(y) > 3;

                if ((gestX || gestY) && !(gestX && gestY)) {
                    if (gestX) {
                        if (x < 0) {
                            Log.e("test", "<<<<<<<< LEFT <<<<<<<<<<<<");
                        } else {
                            Log.e("test", ">>>>>>>>> RITE >>>>>>>>>>>");
                        }
                    } else {
                        if (y < -2) {
                            Log.e("test", "<<<<<<<< UP <<<<<<<<<<<<");
                        } else {
                            Log.e("test", ">>>>>>>>> DOWN >>>>>>>>>>>");
                        }
                    }
                    mLastGestureTime = now;
                }
            }
        }

        /**
         * Called when the accuracy of the registered sensor has changed. We do nothing.
         *
         * @param sensor The {@code Sensor} whose accuracy has changed
         * @param accuracy The new accuracy of this sensor, one of {@code SensorManager.SENSOR_STATUS_*}
         */
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    /**
     * Custom {@code View} which just displays a compass arrow, rotated by the value of {@code -mValues[0]}
     * for no apparent reason.
     */
    private class SampleView extends View {
        /**
         * {@code Paint} we use to draw our compass arrow.
         */
        private Paint mPaint = new Paint();
        /**
         * {@code Path} that draws a compass arrow.
         */
        private Path mPath = new Path();
        /**
         * Flag which we do not actually use, but set to true when our method {@code onAttachedToWindow}
         * is called and set to false when our method {@code onDetachedFromWindow} is called.
         */
        private boolean mAnimate;

        /**
         * Our constructor. First we call our super's constructor, then we construct a wedge shaped
         * path in {@code Path mPath} which our {@code onDraw} method will use to draw our compass
         * arrow.
         *
         * @param context {@code Context} used to retrieve resources, this when called from the
         *                {@code onCreate} method of the {@code SensorTest} activity.
         */
        public SampleView(Context context) {
            super(context);

            // Construct a wedge-shaped path
            mPath.moveTo(0, -50);
            mPath.lineTo(-20, 60);
            mPath.lineTo(0, 50);
            mPath.lineTo(20, 60);
            mPath.close();
        }

        /**
         * We implement this to do our drawing. First we make a local copy {@code Paint paint} of the
         * pointer in our field {@code Paint mPaint}. Then we fill the entire {@code Canvas canvas}
         * with the color WHITE. We set the antialias flag of {@code paint} to true, its color to
         * BLACK, and its style to FILL. We initialize {@code int w} with the width of the {@code canvas}
         * and {@code int h} with the height. We calculate the center of the canvas from these values
         * and move the canvas to that point. Then is our field {@code mValues} is not null we rotate
         * the canvas by {@code -mValues[0]}. Finally we draw the compass arrow contained in {@code Path mPath}
         * to the canvas using {@code Paint mPaint} as the paint.
         *
         * @param canvas the canvas on which the background will be drawn
         */
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

        /**
         * This is called when the view is attached to a window. At this point it has a Surface and
         * will soon start drawing. We set our flag {@code mAnimate} to true for no apparent reason
         * and call through to our super's implementation of {@code onAttachedToWindow}.
         */
        @Override
        protected void onAttachedToWindow() {
            mAnimate = true;
            //noinspection ConstantIfStatement,ConstantConditions
            if (false) Log.d(TAG, "onAttachedToWindow. mAnimate=" + mAnimate);
            super.onAttachedToWindow();
        }

        /**
         * This is called when the view is detached from a window. At this point it no longer has a
         * surface for drawing. We set our flag {@code mAnimate} to false for no apparent reason
         * and call through to our super's implementation of {@code onDetachedFromWindow}.
         */
        @Override
        protected void onDetachedFromWindow() {
            mAnimate = false;
            //noinspection ConstantIfStatement,ConstantConditions
            if (false) Log.d(TAG, "onDetachedFromWindow. mAnimate=" + mAnimate);
            super.onDetachedFromWindow();
        }
    }
}

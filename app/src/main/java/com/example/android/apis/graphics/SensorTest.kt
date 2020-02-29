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
package com.example.android.apis.graphics

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * This sample only draws a nice looking arrow, and logs sensor readings.
 * No meaningful graphics lessons here?
 */
class SensorTest : GraphicsActivity() {

    /**
     * `SensorManager` for accessing sensors
     */
    private var mSensorManager: SensorManager? = null
    /**
     * Default sensor for the accelerometer sensor type TYPE_ACCELEROMETER
     */
    private var mSensor: Sensor? = null
    /**
     * Our display of an arrow
     */
    private var mView: SampleView? = null
    /**
     * The `onDraw` method of `SampleView` rotates the canvas based on the value of
     * `mValues[0]` - must have been copied from the compass example.
     */
    private val mValues: FloatArray? = FloatArray(1)

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. We fetch the handle to the system level service SENSOR_SERVICE to set our
     * field `SensorManager mSensorManager`, then use it to initialize our field `Sensor mSensor`
     * with the default sensor for the accelerometer sensor type. Finally we create a new instance of
     * `SampleView` for our field `SampleView mView` and set our content view to it.
     *
     * @param icicle We do not call `onSaveInstanceState` so do not use
     */
    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mView = SampleView(this)
        setContentView(mView)
    }

    /**
     * Called after [.onRestoreInstanceState], [.onRestart], or [.onPause], for
     * your activity to start interacting with the user. First we call through to our super's
     * implementation of `onResume`, then we register `SensorEventListener mListener` as
     * the listener for our sensor `Sensor mSensor` using SENSOR_DELAY_FASTEST (get sensor data
     * as quickly as possible).
     */
    override fun onResume() {
        super.onResume()
        mSensorManager!!.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_FASTEST)
    }

    /**
     * Called when you are no longer visible to the user. We unregister `SensorEventListener mListener`
     * as a listener for its sensors, then call through to our super's implementation of `onStop`.
     */
    override fun onStop() {
        mSensorManager!!.unregisterListener(mListener)
        super.onStop()
    }

    companion object {
        /**
         * TAG used for logging
         */
        private const val TAG = "SensorTest"
    }

    /**
     * Unused so I won't comment on it.
     */
    @Suppress("unused")
    private class RunAve(private val mWeights: FloatArray) {
        private val mWeightScale: Float
        private val mSamples: FloatArray
        private val mDepth: Int
        private var mCurr: Int
        fun addSample(value: Float) {
            mSamples[mCurr] = value
            mCurr = (mCurr + 1) % mDepth
        }

        fun computeAve(): Float {
            val depth = mDepth
            var index = mCurr
            var sum = 0f
            for (i in 0 until depth) {
                sum += mWeights[i] * mSamples[index]
                index -= 1
                if (index < 0) {
                    index = depth - 1
                }
            }
            return sum * mWeightScale
        }

        init {
            var sum = 0f
            for (weight in mWeights) {
                sum += weight
            }
            mWeightScale = 1 / sum
            mDepth = mWeights.size
            mSamples = FloatArray(mDepth)
            mCurr = 0
        }
    }

    /**
     * An anonymous `SensorEventListener` that we use to listen to `Sensor mSensor`
     */
    private val mListener: SensorEventListener = object : SensorEventListener {
        /**
         * Scale factors used in calculating whether the sensor has changed enough to constitute a
         * "serious" move left/right or up/down.
         */
        private val mScale = floatArrayOf(2f, 2.5f, 0.5f) // acceleration
        /**
         * Values of the previous `SensorEvent.values[]` array, used to detect change in the
         * sensor readings.
         */
        private val mPrev = FloatArray(3)
        /**
         * Time in milliseconds since boot of the last time we logged a gesture. Used to limit that
         * output to 1 per second.
         */
        private var mLastGestureTime: Long = 0

        /**
         * Called when sensor values have changed. First we set our flag `show` to false (we set
         * it to true if the sensor readings have changed enough to justify logging them (which we then
         * do)). We allocate 3 floats for `float[] diff`, then loop through the 3 readings in
         * `event.values[]` and 3 previous readings in our field `mPrev[]`, scaling each
         * change in readings then rounding them to the nearest `int` before assigning the result
         * to the corresponding `diff[]`. If the absolute value is greater than 0, we set `show`
         * to true. We then save the current sensor reading in `mPrev[]` to the next time we are
         * called and loop back for the next element of `values[]`.
         *
         * When done with the sensor readings we check if `show` is now true, and if so log the
         * sensor changes, increment `mValues[0]` modulo 360, and invalidate `SampleView mView`
         * so that the compass arrow angle will change (why not?).
         *
         * We fetch the milliseconds since boot for `long now`, and if 1000 milliseconds have passed
         * since `mLastGestureTime` was last set we set `mLastGestureTime` to 0 copy `diff[0]`
         * to `float x` and `diff[1]` to `float y`. We set `boolean gestX` to true
         * if the absolute value of `x` is greater than 3 and `boolean gestY` to true if the
         * absolute value of `y` is greater than 3. Then if either `gestX` or `gestY`
         * is true, but both are not true we have a gesture to log. If `gestX` was the one that was
         * true, a value for `x` less than 0 is logged as a LEFT gesture, otherwise it is logged
         * as a RITE gesture. If `gestY` was the one that was true, a value of `y` less than
         * -2 is logged as UP, otherwise it is logged as DOWN. If we logged a gesture we now set the
         * value of `mLastGestureTime` to `now`.
         *
         * @param event the [SensorEvent][android.hardware.SensorEvent].
         */
        override fun onSensorChanged(event: SensorEvent) {
            var show = false
            val diff = FloatArray(3)
            for (i in 0..2) {
                diff[i] = (mScale[i] * (event.values[i] - mPrev[i]) * 0.45f).roundToInt().toFloat()
                if (abs(diff[i]) > 0) {
                    show = true
                }
                mPrev[i] = event.values[i]
            }
            if (show) { // only shows if we think the delta is big enough, in an attempt
// to detect "serious" moves left/right or up/down
                Log.e(TAG, "sensorChanged " + event.sensor.name +
                        " (" + event.values[0] + ", " + event.values[1] + ", " +
                        event.values[2] + ")" + " diff(" + diff[0] +
                        " " + diff[1] + " " + diff[2] + ")")
                mValues!![0] = (mValues[0] + 5) % 360
                mView!!.invalidate()
            }
            val now = SystemClock.uptimeMillis()
            if (now - mLastGestureTime > 1000) {
                mLastGestureTime = 0
                val x = diff[0]
                val y = diff[1]
                val gestX = abs(x) > 3
                val gestY = abs(y) > 3
                if ((gestX || gestY) && !(gestX && gestY)) {
                    if (gestX) {
                        if (x < 0) {
                            Log.e("test", "<<<<<<<< LEFT <<<<<<<<<<<<")
                        } else {
                            Log.e("test", ">>>>>>>>> RITE >>>>>>>>>>>")
                        }
                    } else {
                        if (y < -2) {
                            Log.e("test", "<<<<<<<< UP <<<<<<<<<<<<")
                        } else {
                            Log.e("test", ">>>>>>>>> DOWN >>>>>>>>>>>")
                        }
                    }
                    mLastGestureTime = now
                }
            }
        }

        /**
         * Called when the accuracy of the registered sensor has changed. We do nothing.
         *
         * @param sensor The `Sensor` whose accuracy has changed
         * @param accuracy The new accuracy of this sensor, one of `SensorManager.SENSOR_STATUS_*`
         */
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    /**
     * Custom `View` which just displays a compass arrow, rotated by the value of `-mValues[0]`
     * for no apparent reason.
     */
    private inner class SampleView(context: Context?) : View(context) {
        /**
         * `Paint` we use to draw our compass arrow.
         */
        private val mPaint = Paint()
        /**
         * `Path` that draws a compass arrow.
         */
        private val mPath = Path()
        /**
         * Flag which we do not actually use, but set to true when our method `onAttachedToWindow`
         * is called and set to false when our method `onDetachedFromWindow` is called.
         */
        private var mAnimate = false

        /**
         * We implement this to do our drawing. First we make a local copy `Paint paint` of the
         * pointer in our field `Paint mPaint`. Then we fill the entire `Canvas canvas`
         * with the color WHITE. We set the antialias flag of `paint` to true, its color to
         * BLACK, and its style to FILL. We initialize `int w` with the width of the `canvas`
         * and `int h` with the height. We calculate the center of the canvas from these values
         * and move the canvas to that point. Then is our field `mValues` is not null we rotate
         * the canvas by `-mValues[0]`. Finally we draw the compass arrow contained in `Path mPath`
         * to the canvas using `Paint mPaint` as the paint.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            val paint = mPaint
            canvas.drawColor(Color.WHITE)
            paint.isAntiAlias = true
            paint.color = Color.BLACK
            paint.style = Paint.Style.FILL
            val w = width
            val h = height
            val cx = w / 2
            val cy = h / 2
            canvas.translate(cx.toFloat(), cy.toFloat())
            if (mValues != null) {
                canvas.rotate(-mValues[0])
            }
            canvas.drawPath(mPath, mPaint)
        }

        /**
         * This is called when the view is attached to a window. At this point it has a Surface and
         * will soon start drawing. We set our flag `mAnimate` to true for no apparent reason
         * and call through to our super's implementation of `onAttachedToWindow`.
         */
        override fun onAttachedToWindow() {
            mAnimate = true
            @Suppress("ConstantConditionIf")
            if (false) Log.d(TAG, "onAttachedToWindow. mAnimate=$mAnimate")
            super.onAttachedToWindow()
        }

        /**
         * This is called when the view is detached from a window. At this point it no longer has a
         * surface for drawing. We set our flag `mAnimate` to false for no apparent reason
         * and call through to our super's implementation of `onDetachedFromWindow`.
         */
        override fun onDetachedFromWindow() {
            mAnimate = false
            @Suppress("ConstantConditionIf")
            if (false) Log.d(TAG, "onDetachedFromWindow. mAnimate=$mAnimate")
            super.onDetachedFromWindow()
        }

        /**
         * Our constructor. First we call our super's constructor, then we construct a wedge shaped
         * path in `Path mPath` which our `onDraw` method will use to draw our compass
         * arrow.
         *
         * context `Context` used to retrieve resources, this when called from the
         * `onCreate` method of the `SensorTest` activity.
         */
        init {
            // Construct a wedge-shaped path
            mPath.moveTo(0f, -50f)
            mPath.lineTo(-20f, 60f)
            mPath.lineTo(0f, 50f)
            mPath.lineTo(20f, 60f)
            mPath.close()
        }
    }
}
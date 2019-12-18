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

import android.annotation.SuppressLint
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
import android.util.Log
import android.view.View

/**
 * Displays a compass pointer which it rotates according to SensorEvent it receives in its
 * override of the onSensorChanged method. The rotation is done using the matrix pre-concatenated
 * by Canvas.rotate(float degrees)
 */
class Compass : GraphicsActivity() {
    /**
     * Handle to an instance of the system service Context.SENSOR_SERVICE. A [SensorManager]
     * lets you access the device's sensors.
     */
    private var mSensorManager: SensorManager? = null
    /**
     * Default [Sensor] for the type Sensor.TYPE_ORIENTATION, an orientation sensor type.
     */
    private var mSensor: Sensor? = null
    /**
     * An instance of [SampleView] which is used as our content view and displays the compass
     * pointer which is updated as our orientation sensor changes directions.
     */
    private var mView: SampleView? = null
    /**
     * Rotation vector returned in the latest [SensorEvent] received from our orientation sensor.
     * It consists of values[0]: Azimuth, angle between the magnetic north direction and the y-axis,
     * around the z-axis (0 to 359). 0=North, 90=East, 180=South, 270=West; values[1]: Pitch,
     * rotation around x-axis (-180 to 180), with positive values when the z-axis moves toward the
     * y-axis; values[2]: Roll, rotation around the y-axis (-90 to 90) increasing as the device
     * moves clockwise.
     */
    private var mValues: FloatArray? = null
    /**
     * Listener we register for updates to our [Sensor] field [mSensor] orientation sensor
     */
    @Suppress("ConstantConditionIf")
    private val mListener: SensorEventListener = object : SensorEventListener {
        /**
         * Called when sensor values have changed. First we save a reference to our [SensorEvent]
         * parameter [event]'s field [SensorEvent.values] in our [Float] array field [mValues], then
         * if our [SampleView] field  [mView] is not *null* we invalidate the whole view so it will
         * be redrawn.
         *
         * @param event the [SensorEvent][android.hardware.SensorEvent].
         */
        override fun onSensorChanged(event: SensorEvent) {
            if (false) {
                Log.d(TAG, "sensorChanged (${event.values[0]}, ${event.values[1]}, ${event.values[2]})")
            }
            mValues = event.values
            if (mView != null) {
                mView!!.invalidate()
            }
        }

        /**
         * Called when the accuracy of the registered sensor has changed.
         *
         * @param sensor [Sensor] whose accuracy has changed
         * @param accuracy The new accuracy of this sensor, one of `SensorManager.SENSOR_STATUS_*`
         */
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we initialize our [SensorManager] field [mSensorManager] with a handle to an
     * instance of the system service SENSOR_SERVICE, and our [Sensor] field [mSensor] with the
     * default sensor for the type TYPE_ORIENTATION. We initialize our [SampleView] field [mView]
     * with an instance of our view subclass [SampleView] and set our content view to it.
     *
     * @param icicle We do not override [onSaveInstanceState] so do not use
     */
    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        @Suppress("DEPRECATION")
        mSensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ORIENTATION)
        mView = SampleView(this)
        setContentView(mView)
    }

    /**
     * Called after [onRestoreInstanceState], [onRestart], or [onPause], for your activity to start
     * interacting with the user.
     *
     * First we call through to our  super's implementation of `onResume`, then we register
     * our [SensorEventListener] field [mListener] as a listener for the [Sensor] field [mSensor]
     * with SENSOR_DELAY_GAME as the rate to deliver sensor data at.
     */
    override fun onResume() {
        @Suppress("ConstantConditionIf")
        if (false) Log.d(TAG, "onResume")
        super.onResume()
        mSensorManager!!.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_GAME)
    }

    /**
     * Called when you are no longer visible to the user. You will next receive either [onRestart],
     * [onDestroy], or nothing, depending on later user activity.
     *
     * We unregister our [SensorEventListener] field [mListener] as a listener, and call through to
     * our super's implementation of `onStop`.
     */
    override fun onStop() {
        @Suppress("ConstantConditionIf")
        if (false) Log.d(TAG, "onStop")
        mSensorManager!!.unregisterListener(mListener)
        super.onStop()
    }

    /**
     * Custom view which displays our compass pointer.
     */
    private inner class SampleView(context: Context?) : View(context) {
        /**
         * [Paint] instance we use in our [onDraw] method
         */
        private val mPaint = Paint()
        /**
         * `Path` containing a wedge-shaped path which is used as our compass pointer
         */
        private val mPath = Path()
        /**
         * [Boolean] flag which is set to *true* in [onAttachedToWindow] and to *false* in
         * [onDetachedFromWindow] which we do not actually use for anything?
         */
        private var mAnimate = false

        /**
         * We implement this to do our drawing. We copy the reference to the [Paint] field [mPaint]
         * to `val paint` (for no good reason I can discern?), we set the color of the entire
         * [Canvas] parameter [canvas] to WHITE, set the antialias flag for `paint`, set its color
         * to BLACK and set its style to FILL. We fetch the width of [canvas] to [Int] `val w`
         * and the height to [Int] `val h` and calculate the center point of [canvas] (cx,cy)
         * and translate the [Canvas] to that point. If our [Float] array field [mValues] is not
         * *null* (we have received an orientation reading already) we rotate [canvas] by the value
         * of `-mValues[0]` (which is the Azimuth, angle between the magnetic north direction and
         * the y-axis, around the z-axis (0 to 359). 0=North, 90=East, 180=South, 270=West)
         *
         * Finally we instruct the [Canvas] parameter [canvas] to draw the path [Path] field [mPath]
         * using [Paint] field [mPaint] as the paint.
         *
         * @param canvas the [Canvas] on which the background will be drawn
         */
        @SuppressLint("CanvasSize")
        override fun onDraw(canvas: Canvas) {
            val paint = mPaint
            canvas.drawColor(Color.WHITE)
            paint.isAntiAlias = true
            paint.color = Color.BLACK
            paint.style = Paint.Style.FILL
            val w = canvas.width
            val h = canvas.height
            val cx = w / 2
            val cy = h / 2
            canvas.translate(cx.toFloat(), cy.toFloat())
            if (mValues != null) {
                canvas.rotate(-mValues!![0])
            }
            canvas.drawPath(mPath, mPaint)
        }

        /**
         * This is called when the view is attached to a window. At this point it has a `Surface`
         * and will start drawing. Note that this function is guaranteed to be called before
         * [onDraw], however it may be called any time before the first call to [onDraw] --
         * including before or after [onMeasure].
         *
         * We simply set our (unused) [Boolean] flag field [mAnimate] to *true* and call through to
         * our super's implementation of `onAttachedToWindow`.
         */
        override fun onAttachedToWindow() {
            mAnimate = true
            @Suppress("ConstantConditionIf")
            if (false) Log.d(TAG, "onAttachedToWindow. mAnimate=$mAnimate")
            super.onAttachedToWindow()
        }

        /**
         * This is called when the view is detached from a window. At this point it no longer has a
         * surface for drawing.
         *
         * We simply set our (unused) [Boolean] flag field [mAnimate] to *false* and call through to
         * our super's implementation of `onDetachedFromWindow`
         */
        override fun onDetachedFromWindow() {
            mAnimate = false
            @Suppress("ConstantConditionIf")
            if (false) Log.d(TAG, "onDetachedFromWindow. mAnimate=$mAnimate")
            super.onDetachedFromWindow()
        }

        /**
         * Constructs and initializes our `View`. First we call our super's constructor, then
         * we initialize our `Path` field `mPath` using `Path` methods to draw a wedge-shaped
         * path which we will use as our compass pointer.
         *
         * Parameter: Activity `Context` of "this" when called from `onCreate`
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

    /**
     * Our static constants
     */
    companion object {
        /**
         * TAG used for logging
         */
        private const val TAG = "Compass"
    }
}
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
package com.example.android.apis.os

import android.annotation.TargetApi
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Wrapper activity demonstrating the use of the new
 * [rotation vector sensor][SensorEvent.values]
 * ([TYPE_ROTATION_VECTOR][Sensor.TYPE_ROTATION_VECTOR]).
 *
 * @see Sensor
 *
 * @see SensorEvent
 *
 * @see SensorManager
 *
 *
 * Uses output from Sensor.TYPE_ROTATION_VECTOR to change the rotation matrix of an openGL Cube.
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
class RotationVectorDemo : AppCompatActivity() {
    /**
     * The `GLSurfaceView` we use as our content view, contains a `MyRenderer.Cube`
     * which `MyRenderer` rotates based on the rotation vector sensor.
     */
    private var mGLSurfaceView: GLSurfaceView? = null

    /**
     * Handle to the system level service SENSOR_SERVICE ("sensor")
     */
    private var mSensorManager: SensorManager? = null

    /**
     * The instance of `MyRenderer` we use as the renderer of `mGLSurfaceView`.
     */
    private var mRenderer: MyRenderer? = null

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we initialize or field `SensorManager mSensorManager` with a
     * handle to the system level service SENSOR_SERVICE ("sensor"). We create a new instance for
     * `MyRenderer mRenderer`, and a new instance for `GLSurfaceView mGLSurfaceView`,
     * set the renderer of `GLSurfaceView mGLSurfaceView` to `MyRenderer mRenderer` and
     * set our content view to `GLSurfaceView mGLSurfaceView`.
     *
     * @param savedInstanceState we do not override `onSaveInstanceState` so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get an instance of the SensorManager
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Create our Preview view and set it as the content of our
        // Activity
        mRenderer = MyRenderer()
        mGLSurfaceView = GLSurfaceView(this)
        mGLSurfaceView!!.setRenderer(mRenderer)
        setContentView(mGLSurfaceView)
    }

    /**
     * Called after [.onRestoreInstanceState], [.onRestart], or [.onPause], for
     * our activity to start interacting with the user. First we call through to our super's implementation
     * of `onResume`, then we call the `start` method of `MyRenderer mRenderer`,
     * and the `onResume` method of `GLSurfaceView mGLSurfaceView`.
     */
    override fun onResume() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume()
        mRenderer!!.start()
        mGLSurfaceView!!.onResume()
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. First we call through to our super's implementation of `onPause`,
     * then we call the `stop` method of `MyRenderer mRenderer`, and the `onPause`
     * method of `GLSurfaceView mGLSurfaceView`.
     */
    override fun onPause() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause()
        mRenderer!!.stop()
        mGLSurfaceView!!.onPause()
    }

    /**
     * The renderer for `GLSurfaceView mGLSurfaceView`, it draws a `Cube` after modifying
     * the rotation matrix `mRotationMatrix` based on the TYPE_ROTATION_VECTOR sensor readings.
     * Our `onDrawFrame` method then multiplies the GL_MODELVIEW (model view) matrix by
     * `mRotationMatrix` before telling `Cube mCube` to draw itself.
     */
    internal inner class MyRenderer : GLSurfaceView.Renderer, SensorEventListener {
        /**
         * The `Cube` instance which we rotate.
         */
        private val mCube: Cube

        /**
         * The default sensor for TYPE_ROTATION_VECTOR (rotation vector sensor type)
         */
        private val mRotationVectorSensor: Sensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        /**
         * Rotation vector that we multiply the GL_MODELVIEW matrix by before telling our `Cube mCube`
         * to draw itself. It is set in our `onSensorChanged` override based on the rotation vector
         * passed it in its `SensorEvent event` parameter.
         */
        private val mRotationMatrix = FloatArray(16)

        /**
         * Enables `Sensor mRotationVectorSensor` asking for 10 millisecond updates, and registers
         * "this" as the listener for the sensor.
         */
        fun start() {
            // enable our sensor when the activity is resumed, ask for
            // 10 ms updates.
            mSensorManager!!.registerListener(this, mRotationVectorSensor, 10000)
        }

        /**
         * Unregisters "this" as a listener for all sensor events.
         */
        fun stop() {
            // make sure to turn our sensor off when the activity is paused
            mSensorManager!!.unregisterListener(this)
        }

        /**
         * Called when sensor values have changed. If the sensor that generated the `event` is
         * of type TYPE_ROTATION_VECTOR, we call the `getRotationMatrixFromVector` method of
         * `SensorManager` to convert the rotation vector in the `values` field of
         * `event` to a rotation matrix in `mRotationMatrix`.
         *
         * @param event the [SensorEvent][android.hardware.SensorEvent].
         */
        override fun onSensorChanged(event: SensorEvent) {
            // we received a sensor event. it is a good practice to check
            // that we received the proper event
            if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                // convert the rotation-vector to a 4x4 matrix. the matrix
                // is interpreted by Open GL as the inverse of the
                // rotation-vector, which is what we want.
                SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values)
            }
        }

        /**
         * Called to draw the current frame. First we clear the screen, then we set the current matrix
         * to the GL_MODELVIEW matrix, load it with the identity matrix, translate it to (0,0,-3), and
         * multiply it by `mRotationMatrix`. We then enable the client side capability GL_VERTEX_ARRAY.
         * and GL_COLOR_ARRAY and tell `Cube mCube` to draw itself.
         *
         * @param gl the GL interface.
         */
        override fun onDrawFrame(gl: GL10) {
            // clear screen
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT)

            // set-up modelview matrix
            gl.glMatrixMode(GL10.GL_MODELVIEW)
            gl.glLoadIdentity()
            gl.glTranslatef(0f, 0f, -3.0f)
            gl.glMultMatrixf(mRotationMatrix, 0)

            // draw our object
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
            gl.glEnableClientState(GL10.GL_COLOR_ARRAY)
            mCube.draw(gl)
        }

        /**
         * Called when the surface changed size. First we set our view port to have the lower left
         * corner at (0,0), and a width of `int width` and a height of `int height`. Then
         * we calculate th aspect ratio `float ratio` to be `width/height`. We set the
         * current matrix mode to GL_PROJECTION (The projection matrix defines the properties of the
         * camera that views the objects in the world coordinate frame. Here you typically set the
         * zoom factor, aspect ratio and the near and far clipping planes). We load the matrix with
         * the identity matrix, and then set the left clipping plane to `-ratio` the right
         * clipping plane to `ratio`, the bottom clipping plane to -1, the top clipping plane
         * to 1, the near clipping plane to 1, and the far clipping plane to 10.
         *
         * @param gl     the GL interface.
         * @param width  new width of the surface.
         * @param height new height of the surface.
         */
        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            // set view-port
            gl.glViewport(0, 0, width, height)
            // set projection matrix
            val ratio = width.toFloat() / height
            gl.glMatrixMode(GL10.GL_PROJECTION)
            gl.glLoadIdentity()
            gl.glFrustumf(-ratio, ratio, -1f, 1f, 1f, 10f)
        }

        /**
         * Called when the surface is created or recreated. First we disable the server side capability
         * GL_DITHER (disables the dithering of color components and indices before they are written
         * to the color buffer), and then we set the clear color to white.
         *
         * @param gl     the GL interface.
         * @param config the EGLConfig of the created surface.
         */
        override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
            // dither is enabled by default, we don't need it
            gl.glDisable(GL10.GL_DITHER)
            // clear screen in white
            gl.glClearColor(1f, 1f, 1f, 1f)
        }

        /**
         * Implements a openGL cube.
         */
        internal inner class Cube {
            // initialize our cube
            /**
             * Vertex buffer for a cube, loaded in the constructor from an array, passed to the openGL
             * system by the method `glVertexPointer` to define the array of vertex data to use
             * for rendering.
             */
            private val mVertexBuffer: FloatBuffer

            /**
             * Color buffer for a cube, loaded in the constructor from an array, passed to the openGL
             * system by the method `glColorPointer` to define the array of colors to use for
             * rendering.
             */
            private val mColorBuffer: FloatBuffer

            /**
             * Index buffer for a cube, loaded in the constructor from an array, passed to the openGL
             * system by the method `glDrawElements` to draw a series of GL_TRIANGLES primitives
             * using the index buffer to specify the coordinates of each point addressed by the indices,
             * and color buffer to specify the color.
             */
            private val mIndexBuffer: ByteBuffer

            /**
             * Called by `MyRenderer.onDrawFrame` to draw our `Cube`. First we enable the
             * server side capability GL_CULL_FACE (culls polygons based on their winding in window
             * coordinates). We set the orientation of front-facing polygons to GL_CW (clockwise),
             * and set the shade model to GL_SMOOTH (causes the computed colors of vertices to be
             * interpolated as the primitive is rasterized, typically assigning different colors to
             * each resulting pixel fragment). Next we specify the location and data format of the
             * array of vertex coordinates to use when rendering to be `FloatBuffer mVertexBuffer`,
             * with a size of 3, a data type of GL_FLOAT, and a stride of 0. We specify the location
             * and data format of an array of color components to use when rendering to be our field
             * `FloatBuffer mColorBuffer`, with a size of 4, a data type of GL_FLOAT, and a
             * stride of 0.
             *
             *
             * Finally we call `glDrawElements` to draw 36 GL_TRIANGLES using the GL_UNSIGNED_BYTE
             * data in `ByteBuffer mIndexBuffer` as the indices into the vertex and color buffers.
             *
             * @param gl the GL interface.
             */
            fun draw(gl: GL10) {
                gl.glEnable(GL10.GL_CULL_FACE)
                gl.glFrontFace(GL10.GL_CW)
                gl.glShadeModel(GL10.GL_SMOOTH)
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer)
                gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer)
                gl.glDrawElements(GL10.GL_TRIANGLES, 36, GL10.GL_UNSIGNED_BYTE, mIndexBuffer)
            }

            /**
             * Our constructor. We allocate a new direct byte buffer for `ByteBuffer vbb` large
             * enough to hold our constant array `vertices[]`, set its byte order to the native
             * order for our device, and create a view of it as a float buffer in order to initialize
             * our field `FloatBuffer mVertexBuffer`. We then transfer the entire content of
             * `vertices[]` into `mVertexBuffer`, and position it to its beginning.
             *
             *
             * We allocate a new direct byte buffer for `ByteBuffer cbb` large enough to hold
             * our constant array `colors[]`, set its byte order to the native order for our
             * device, and create a view of it as a float buffer in order to initialize our field
             * `FloatBuffer mColorBuffer`. We then transfer the entire content of
             * `colors[]` into `mColorBuffer`, and position it to its beginning.
             *
             *
             * Finally We allocate a new direct byte buffer for `ByteBuffer mIndexBuffer` large
             * enough to hold our constant array `indices[]`, and transfer the entire content of
             * `indices[]` into `mIndexBuffer`, and position it to its beginning.
             */
            init {
                val vertices = floatArrayOf(-1f, -1f, -1f, 1f, -1f, -1f, 1f, 1f, -1f, -1f, 1f, -1f, -1f, -1f, 1f, 1f, -1f, 1f, 1f, 1f, 1f, -1f, 1f, 1f)
                val colors = floatArrayOf(0f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 1f, 1f, 1f, 1f, 0f, 1f, 1f, 1f)
                val indices = byteArrayOf(
                        0, 4, 5, 0, 5, 1,
                        1, 5, 6, 1, 6, 2,
                        2, 6, 7, 2, 7, 3,
                        3, 7, 4, 3, 4, 0,
                        4, 7, 6, 4, 6, 5,
                        3, 0, 1, 3, 1, 2
                )
                val vbb = ByteBuffer.allocateDirect(vertices.size * 4)
                vbb.order(ByteOrder.nativeOrder())
                mVertexBuffer = vbb.asFloatBuffer()
                mVertexBuffer.put(vertices)
                mVertexBuffer.position(0)
                val cbb = ByteBuffer.allocateDirect(colors.size * 4)
                cbb.order(ByteOrder.nativeOrder())
                mColorBuffer = cbb.asFloatBuffer()
                mColorBuffer.put(colors)
                mColorBuffer.position(0)
                mIndexBuffer = ByteBuffer.allocateDirect(indices.size)
                mIndexBuffer.put(indices)
                mIndexBuffer.position(0)
            }
        }

        /**
         * Called when the accuracy of the registered sensor has changed. We ignore it.
         *
         * @param sensor   the `Sensor` whose accuracy has changed
         * @param accuracy The new accuracy of this sensor
         */
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

        /**
         * Our constructor. First we initialize `Sensor mRotationVectorSensor` with the default
         * TYPE_ROTATION_VECTOR sensor, then we create a new instance for `Cube mCube`, and
         * initialize `mRotationMatrix` to the identity matrix.
         */
        init {
            // find the rotation-vector sensor
            mCube = Cube()
            // initialize the rotation matrix to identity
            mRotationMatrix[0] = 1f
            mRotationMatrix[4] = 1f
            mRotationMatrix[8] = 1f
            mRotationMatrix[12] = 1f
        }
    }
}
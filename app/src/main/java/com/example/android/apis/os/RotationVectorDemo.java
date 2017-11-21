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

package com.example.android.apis.os;

import android.annotation.TargetApi;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Wrapper activity demonstrating the use of the new
 * {@link SensorEvent#values rotation vector sensor}
 * ({@link Sensor#TYPE_ROTATION_VECTOR TYPE_ROTATION_VECTOR}).
 *
 * @see Sensor
 * @see SensorEvent
 * @see SensorManager
 * <p>
 * Uses output from Sensor.TYPE_ROTATION_VECTOR to change the rotation matrix of an openGL Cube.
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class RotationVectorDemo extends Activity {
    /**
     * The {@code GLSurfaceView} we use as our content view, contains a {@code MyRenderer.Cube}
     * which {@code MyRenderer} rotates based on the rotation vector sensor.
     */
    private GLSurfaceView mGLSurfaceView;
    /**
     * Handle to the system level service SENSOR_SERVICE ("sensor")
     */
    private SensorManager mSensorManager;
    /**
     * The instance of {@code MyRenderer} we use as the renderer of {@code mGLSurfaceView}.
     */
    private MyRenderer mRenderer;

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we initialize or field {@code SensorManager mSensorManager} with a
     * handle to the system level service SENSOR_SERVICE ("sensor"). We create a new instance for
     * {@code MyRenderer mRenderer}, and a new instance for {@code GLSurfaceView mGLSurfaceView},
     * set the renderer of {@code GLSurfaceView mGLSurfaceView} to {@code MyRenderer mRenderer} and
     * set our content view to {@code GLSurfaceView mGLSurfaceView}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get an instance of the SensorManager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Create our Preview view and set it as the content of our
        // Activity
        mRenderer = new MyRenderer();
        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setRenderer(mRenderer);
        setContentView(mGLSurfaceView);
    }

    /**
     * Called after {@link #onRestoreInstanceState}, {@link #onRestart}, or {@link #onPause}, for
     * our activity to start interacting with the user. First we call through to our super's implementation
     * of {@code onResume}, then we call the {@code start} method of {@code MyRenderer mRenderer},
     * and the {@code onResume} method of {@code GLSurfaceView mGLSurfaceView}.
     */
    @Override
    protected void onResume() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();
        mRenderer.start();
        mGLSurfaceView.onResume();
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but
     * has not (yet) been killed. First we call through to our super's implementation of {@code onPause},
     * then we call the {@code stop} method of {@code MyRenderer mRenderer}, and the {@code onPause}
     * method of {@code GLSurfaceView mGLSurfaceView}.
     */
    @Override
    protected void onPause() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
        mRenderer.stop();
        mGLSurfaceView.onPause();
    }

    /**
     * The renderer for {@code GLSurfaceView mGLSurfaceView}, it draws a {@code Cube} after modifying
     * the rotation matrix {@code mRotationMatrix} based on the TYPE_ROTATION_VECTOR sensor readings.
     * Our {@code onDrawFrame} method then multiplies the GL_MODELVIEW (model view) matrix by
     * {@code mRotationMatrix} before telling {@code Cube mCube} to draw itself.
     */
    @SuppressWarnings("WeakerAccess")
    class MyRenderer implements GLSurfaceView.Renderer, SensorEventListener {
        /**
         * The {@code Cube} instance which we rotate.
         */
        private Cube mCube;
        /**
         * The default sensor for TYPE_ROTATION_VECTOR (rotation vector sensor type)
         */
        private Sensor mRotationVectorSensor;
        /**
         * Rotation vector that we multiply the GL_MODELVIEW matrix by before telling our {@code Cube mCube}
         * to draw itself. It is set in our {@code onSensorChanged} override based on the rotation vector
         * passed it in its {@code SensorEvent event} parameter.
         */
        private final float[] mRotationMatrix = new float[16];

        /**
         * Our constructor. First we initialize {@code Sensor mRotationVectorSensor} with the default
         * TYPE_ROTATION_VECTOR sensor, then we create a new instance for {@code Cube mCube}, and
         * initialize {@code mRotationMatrix} to the identity matrix.
         */
        public MyRenderer() {
            // find the rotation-vector sensor
            mRotationVectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

            mCube = new Cube();
            // initialize the rotation matrix to identity
            mRotationMatrix[0] = 1;
            mRotationMatrix[4] = 1;
            mRotationMatrix[8] = 1;
            mRotationMatrix[12] = 1;
        }

        /**
         * Enables {@code Sensor mRotationVectorSensor} asking for 10 millisecond updates, and registers
         * "this" as the listener for the sensor.
         */
        public void start() {
            // enable our sensor when the activity is resumed, ask for
            // 10 ms updates.
            mSensorManager.registerListener(this, mRotationVectorSensor, 10000);
        }

        /**
         * Unregisters "this" as a listener for all sensor events.
         */
        public void stop() {
            // make sure to turn our sensor off when the activity is paused
            mSensorManager.unregisterListener(this);
        }

        /**
         * Called when sensor values have changed. If the sensor that generated the {@code event} is
         * of type TYPE_ROTATION_VECTOR, we call the {@code getRotationMatrixFromVector} method of
         * {@code SensorManager} to convert the rotation vector in the {@code values} field of
         * {@code event} to a rotation matrix in {@code mRotationMatrix}.
         *
         * @param event the {@link android.hardware.SensorEvent SensorEvent}.
         */
        @Override
        public void onSensorChanged(SensorEvent event) {
            // we received a sensor event. it is a good practice to check
            // that we received the proper event
            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                // convert the rotation-vector to a 4x4 matrix. the matrix
                // is interpreted by Open GL as the inverse of the
                // rotation-vector, which is what we want.
                SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
            }
        }

        /**
         * Called to draw the current frame. First we clear the screen, then we set the current matrix
         * to the GL_MODELVIEW matrix, load it with the identity matrix, translate it to (0,0,-3), and
         * multiply it by {@code mRotationMatrix}. We then enable the client side capability GL_VERTEX_ARRAY.
         * and GL_COLOR_ARRAY and tell {@code Cube mCube} to draw itself.
         *
         * @param gl the GL interface.
         */
        @Override
        public void onDrawFrame(GL10 gl) {
            // clear screen
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

            // set-up modelview matrix
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();
            gl.glTranslatef(0, 0, -3.0f);
            gl.glMultMatrixf(mRotationMatrix, 0);

            // draw our object
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

            mCube.draw(gl);
        }

        /**
         * Called when the surface changed size.
         *
         * @param gl the GL interface.
         * @param width new width of the surface.
         * @param height new height of the surface.
         */
        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            // set view-port
            gl.glViewport(0, 0, width, height);
            // set projection matrix
            float ratio = (float) width / height;
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // dither is enabled by default, we don't need it
            gl.glDisable(GL10.GL_DITHER);
            // clear screen in white
            gl.glClearColor(1, 1, 1, 1);
        }

        class Cube {
            // initialize our cube
            private FloatBuffer mVertexBuffer;
            private FloatBuffer mColorBuffer;
            private ByteBuffer mIndexBuffer;

            public Cube() {
                final float vertices[] = {
                        -1, -1, -1, 1, -1, -1,
                        1, 1, -1, -1, 1, -1,
                        -1, -1, 1, 1, -1, 1,
                        1, 1, 1, -1, 1, 1,
                };

                final float colors[] = {
                        0, 0, 0, 1, 1, 0, 0, 1,
                        1, 1, 0, 1, 0, 1, 0, 1,
                        0, 0, 1, 1, 1, 0, 1, 1,
                        1, 1, 1, 1, 0, 1, 1, 1,
                };

                final byte indices[] = {
                        0, 4, 5, 0, 5, 1,
                        1, 5, 6, 1, 6, 2,
                        2, 6, 7, 2, 7, 3,
                        3, 7, 4, 3, 4, 0,
                        4, 7, 6, 4, 6, 5,
                        3, 0, 1, 3, 1, 2
                };

                ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
                vbb.order(ByteOrder.nativeOrder());
                mVertexBuffer = vbb.asFloatBuffer();
                mVertexBuffer.put(vertices);
                mVertexBuffer.position(0);

                ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
                cbb.order(ByteOrder.nativeOrder());
                mColorBuffer = cbb.asFloatBuffer();
                mColorBuffer.put(colors);
                mColorBuffer.position(0);

                mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
                mIndexBuffer.put(indices);
                mIndexBuffer.position(0);
            }

            public void draw(GL10 gl) {
                gl.glEnable(GL10.GL_CULL_FACE);
                gl.glFrontFace(GL10.GL_CW);
                gl.glShadeModel(GL10.GL_SMOOTH);
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
                gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);
                gl.glDrawElements(GL10.GL_TRIANGLES, 36, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}
